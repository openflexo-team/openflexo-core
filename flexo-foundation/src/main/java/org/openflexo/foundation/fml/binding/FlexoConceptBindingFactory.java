/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */


package org.openflexo.foundation.fml.binding;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.Function;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.expr.Constant.StringConstant;
import org.openflexo.connie.java.JavaBindingFactory;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.AbstractActionScheme;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourActionType;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoBehaviourParametersType;
import org.openflexo.foundation.fml.FlexoBehaviourParametersValuesType;
import org.openflexo.foundation.fml.FlexoBehaviourType;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.TechnologySpecificCustomType;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.ViewObject;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterBindingFactory;

public final class FlexoConceptBindingFactory extends JavaBindingFactory {
	static final Logger logger = Logger.getLogger(FlexoConceptBindingFactory.class.getPackage().getName());

	private final Map<BindingPathElement, Map<Object, SimplePathElement>> storedBindingPathElements;
	private final ViewPoint viewPoint;

	private final Map<BindingPathElement, Map<FlexoConcept, List<FlexoBehaviourPathElement>>> flexoBehaviourPathElements;

	public FlexoConceptBindingFactory(ViewPoint viewPoint) {
		storedBindingPathElements = new HashMap<BindingPathElement, Map<Object, SimplePathElement>>();
		flexoBehaviourPathElements = new HashMap<BindingPathElement, Map<FlexoConcept, List<FlexoBehaviourPathElement>>>();
		this.viewPoint = viewPoint;
	}

	protected SimplePathElement getSimplePathElement(Object object, BindingPathElement parent) {
		Map<Object, SimplePathElement> storedValues = storedBindingPathElements.get(parent);
		if (storedValues == null) {
			storedValues = new HashMap<Object, SimplePathElement>();
			storedBindingPathElements.put(parent, storedValues);
		}
		SimplePathElement returned = storedValues.get(object);
		if (returned == null) {
			returned = makeSimplePathElement(object, parent);
			storedValues.put(object, returned);
		}
		return returned;
	}

	protected SimplePathElement makeSimplePathElement(Object object, BindingPathElement parent) {
		if (object instanceof FlexoRole) {
			return new FlexoConceptPatternRolePathElement<FlexoRole<?>>(parent, (FlexoRole<?>) object);
		}
		if (object instanceof ModelSlot) {
			return new VirtualModelModelSlotPathElement<ModelSlot>(parent, (ModelSlot) object);
		}
		if (object instanceof FlexoBehaviourParameter) {
			if (parent.getType() instanceof FlexoBehaviourParametersType) {
				return new FlexoBehaviourParameterDefinitionPathElement(parent, (FlexoBehaviourParameter) object);
			} else if (parent.getType() instanceof FlexoBehaviourParametersValuesType) {
				return new FlexoBehaviourParameterValuePathElement(parent, (FlexoBehaviourParameter) object);
			}
		}
		logger.warning("Unexpected " + object + " for parent=" + parent);
		return null;
	}

	@Override
	public List<? extends SimplePathElement> getAccessibleSimplePathElements(BindingPathElement parent) {

		if (parent != null) {
			Type pType = parent.getType();

			if (pType instanceof TechnologySpecificCustomType) {
				TechnologySpecificCustomType parentType = (TechnologySpecificCustomType) pType;
				TechnologyAdapter ta = parentType.getSpecificTechnologyAdapter();
				TechnologyAdapterBindingFactory bf = ta.getTechnologyAdapterBindingFactory();
				if (ta != null && bf != null && bf.handleType(parentType)) {
					List<? extends SimplePathElement> returned = bf.getAccessibleSimplePathElements(parent);
					Collections.sort(returned, BindingPathElement.COMPARATOR);
					return returned;
				}
			}

			if (pType instanceof FlexoBehaviourParametersType) {
				List<SimplePathElement> returned = new ArrayList<SimplePathElement>();
				FlexoBehaviour es = ((FlexoBehaviourParametersType) pType).getFlexoBehaviour();
				for (FlexoBehaviourParameter p : es.getParameters()) {
					returned.add(getSimplePathElement(p, parent));
				}
				Collections.sort(returned, BindingPathElement.COMPARATOR);
				return returned;
			} else if (pType instanceof FlexoBehaviourParametersValuesType) {
				List<SimplePathElement> returned = new ArrayList<SimplePathElement>();
				FlexoBehaviour es = ((FlexoBehaviourParametersValuesType) pType).getFlexoBehaviour();
				for (FlexoBehaviourParameter p : es.getParameters()) {
					returned.add(getSimplePathElement(p, parent));
				}
				Collections.sort(returned, BindingPathElement.COMPARATOR);
				return returned;
			}
			/*if (parent instanceof EditionSchemeParametersBindingVariable) {
				List<SimplePathElement> returned = new ArrayList<SimplePathElement>();
				FlexoBehaviour es = ((EditionSchemeParametersBindingVariable) parent).getEditionScheme();
				for (FlexoBehaviourParameter p : es.getParameters()) {
					returned.add(getSimplePathElement(p, parent));
				}
				Collections.sort(returned, BindingPathElement.COMPARATOR);
				return returned;
			} else if (parent instanceof EditionSchemeParametersPathElement) {
				List<SimplePathElement> returned = new ArrayList<SimplePathElement>();
				FlexoBehaviour es = ((EditionSchemeParametersPathElement) parent).getEditionScheme();
				for (FlexoBehaviourParameter p : es.getParameters()) {
					returned.add(getSimplePathElement(p, parent));
				}
				Collections.sort(returned, BindingPathElement.COMPARATOR);
				return returned;
			} else if (parent instanceof FlexoBehaviourParametersValuesPathElement) {
				List<SimplePathElement> returned = new ArrayList<SimplePathElement>();
				FlexoBehaviour es = ((FlexoBehaviourParametersValuesPathElement) parent).getEditionScheme();
				for (FlexoBehaviourParameter p : es.getParameters()) {
					returned.add(getSimplePathElement(p, parent));
				}
				Collections.sort(returned, BindingPathElement.COMPARATOR);
				return returned;
			}*//*else if (TypeUtils.isTypeAssignableFrom(FlexoConcept.class, pType)) {
				List<SimplePathElement> returned = new ArrayList<SimplePathElement>();
					FlexoConcept ep = (FlexoConcept) pType;
					for (FlexoRole<?> pr : ep.getPatternRoles()) {
						returned.add(getSimplePathElement(pr, parent));
					}
				return returned;
				} */
			else if (pType instanceof FlexoConceptInstanceType) {
				List<SimplePathElement> returned = new ArrayList<SimplePathElement>();
				FlexoConcept concept = ((FlexoConceptInstanceType) pType).getFlexoConcept();

				if (concept != null) {
					if (concept instanceof VirtualModel) {
						VirtualModel vm = (VirtualModel) concept;
						for (ModelSlot ms : vm.getModelSlots()) {
							returned.add(getSimplePathElement(ms, parent));
						}
					}
					for (FlexoRole<?> pr : concept.getFlexoRoles()) {
						returned.add(getSimplePathElement(pr, parent));
					}
					// TODO: performance issue
					if (concept.getInspector().getRenderer().isSet() && concept.getInspector().getRenderer().isValid()) {
						returned.add(new EPIRendererPathElement(parent));
					}
				}
				return returned;
			} else if (pType instanceof FlexoBehaviourType) {
				List<SimplePathElement> returned = new ArrayList<SimplePathElement>();
				FlexoBehaviour flexoBehaviour = ((FlexoBehaviourType) pType).getFlexoBehaviour();
				returned.add(new FlexoBehaviourParametersValuesPathElement(parent, flexoBehaviour));
				returned.add(new FlexoBehaviourParametersDefinitionsPathElement(parent, flexoBehaviour));
				for (FlexoRole<?> pr : flexoBehaviour.getFlexoConcept().getFlexoRoles()) {
					returned.add(getSimplePathElement(pr, parent));
				}
				return returned;
			} else if (pType instanceof FlexoBehaviourActionType) {
				List<SimplePathElement> returned = new ArrayList<SimplePathElement>();
				FlexoBehaviour flexoBehaviour = ((FlexoBehaviourActionType) pType).getFlexoBehaviour();
				returned.add(new FlexoBehaviourParametersValuesPathElement(parent, flexoBehaviour));
				returned.add(new FlexoBehaviourParametersDefinitionsPathElement(parent, flexoBehaviour));
				for (FlexoRole<?> pr : flexoBehaviour.getFlexoConcept().getFlexoRoles()) {
					returned.add(getSimplePathElement(pr, parent));
				}
				return returned;
			}

			// In all other cases, consider it using Java rules
			return super.getAccessibleSimplePathElements(parent);
		} else {
			logger.warning("Trying to find accessible path elements for a NULL parent");
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public List<? extends FunctionPathElement> getAccessibleFunctionPathElements(BindingPathElement parent) {

		Type pType = parent.getType();
		if (pType instanceof FlexoConceptInstanceType) {
			return getFlexoBehaviourPathElements(parent, ((FlexoConceptInstanceType) pType).getFlexoConcept());
		}
		return super.getAccessibleFunctionPathElements(parent);
	}

	public List<FlexoBehaviourPathElement> getFlexoBehaviourPathElements(BindingPathElement parent, FlexoConcept ep) {
		Map<FlexoConcept, List<FlexoBehaviourPathElement>> map = flexoBehaviourPathElements.get(parent);
		if (map == null) {
			map = new HashMap<FlexoConcept, List<FlexoBehaviourPathElement>>();
			flexoBehaviourPathElements.put(parent, map);
		}
		List<FlexoBehaviourPathElement> returned = map.get(ep);
		if (returned == null) {
			returned = new ArrayList<FlexoBehaviourPathElement>();
			for (AbstractActionScheme as : ep.getAbstractActionSchemes()) {
				returned.add(new FlexoBehaviourPathElement(parent, as, null));
			}
			map.put(ep, returned);
		}
		return returned;
	}

	@Override
	public SimplePathElement makeSimplePathElement(BindingPathElement parent, String propertyName) {
		// We want to avoid code duplication, so iterate on all accessible simple path element and choose the right one
		SimplePathElement returned = null;
		for (SimplePathElement e : getAccessibleSimplePathElements(parent)) {
			if (e.getLabel().equals(propertyName)) {
				returned = e;
			}
		}
		// We cannot find a simple path element at this level, retrieve from java
		if (returned == null) {
			returned = super.makeSimplePathElement(parent, propertyName);
		}
		// Hook to specialize type returned by FlexoBehaviourAction.getEditionScheme()
		// This method is used while executing DiagramElement inspectors
		if (propertyName.equals("editionScheme") && (parent.getType() instanceof FlexoBehaviourActionType)) {
			returned.setType(FlexoBehaviourType.getFlexoBehaviourType(((FlexoBehaviourActionType) parent.getType()).getFlexoBehaviour()));
		}
		return returned;
	}

	@Override
	public FunctionPathElement makeFunctionPathElement(BindingPathElement parent, Function function, List<DataBinding<?>> args) {
		// System.out.println("makeFunctionPathElement with " + parent + " function=" + function + " args=" + args);
		if (parent.getType() == null) {
			return null;
		}
		if (parent.getType() instanceof FlexoConceptInstanceType && function instanceof FlexoBehaviour) {
			return new FlexoBehaviourPathElement(parent, (FlexoBehaviour) function, args);
		}
		FunctionPathElement returned = super.makeFunctionPathElement(parent, function, args);
		// Hook to specialize type returned by getFlexoConceptInstance(String)
		// This method is used while executing DiagramElement inspectors
		if (function.getName().equals("getFlexoConceptInstance")) {
			if (TypeUtils.isTypeAssignableFrom(ViewObject.class, parent.getType()) && args.size() == 1 && args.get(0).isStringConstant()) {
				String flexoConceptId = ((StringConstant) args.get(0).getExpression()).getValue();
				FlexoConcept ep = viewPoint.getFlexoConcept(flexoConceptId);
				returned.setType(FlexoConceptInstanceType.getFlexoConceptInstanceType(ep));
			}
		}
		return returned;
	}

	@Override
	public Function retrieveFunction(Type parentType, String functionName, List<DataBinding<?>> args) {
		if (parentType instanceof FlexoConceptInstanceType) {
			return ((FlexoConceptInstanceType) parentType).getFlexoConcept().getFlexoBehaviour(functionName);
		}
		return super.retrieveFunction(parentType, functionName, args);
	}
}
