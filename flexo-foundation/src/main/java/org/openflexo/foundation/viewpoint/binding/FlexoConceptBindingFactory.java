/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2013 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openflexo.foundation.viewpoint.binding;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingPathElement;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.binding.Function;
import org.openflexo.antar.binding.FunctionPathElement;
import org.openflexo.antar.binding.JavaBindingFactory;
import org.openflexo.antar.binding.SimplePathElement;
import org.openflexo.antar.binding.TypeUtils;
import org.openflexo.antar.expr.Constant.StringConstant;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.view.ViewObject;
import org.openflexo.foundation.viewpoint.AbstractActionScheme;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.FlexoConceptInstanceType;
import org.openflexo.foundation.viewpoint.FlexoBehaviour;
import org.openflexo.foundation.viewpoint.FlexoBehaviourActionType;
import org.openflexo.foundation.viewpoint.FlexoBehaviourParameter;
import org.openflexo.foundation.viewpoint.FlexoBehaviourParametersType;
import org.openflexo.foundation.viewpoint.FlexoBehaviourParametersValuesType;
import org.openflexo.foundation.viewpoint.FlexoBehaviourType;
import org.openflexo.foundation.viewpoint.FlexoRole;
import org.openflexo.foundation.viewpoint.TechnologySpecificCustomType;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.VirtualModel;

public final class FlexoConceptBindingFactory extends JavaBindingFactory {
	static final Logger logger = Logger.getLogger(FlexoConceptBindingFactory.class.getPackage().getName());

	private Map<BindingPathElement, Map<Object, SimplePathElement>> storedBindingPathElements;
	private ViewPoint viewPoint;

	private Map<BindingPathElement, Map<FlexoConcept, List<FlexoBehaviourPathElement>>> flexoBehaviourPathElements;

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

		Type pType = parent.getType();
		
		if (pType instanceof TechnologySpecificCustomType) {
			TechnologySpecificCustomType parentType = (TechnologySpecificCustomType) pType;
			TechnologyAdapter ta = parentType.getTechnologyAdapter();
			if (ta != null && ta.getTechnologyAdapterBindingFactory().handleType(parentType)) {
				List<? extends SimplePathElement> returned = ta.getTechnologyAdapterBindingFactory()
						.getAccessibleSimplePathElements(parent);
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
		}
		else if (pType instanceof FlexoBehaviourParametersValuesType) {
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
			FlexoConcept ep = ((FlexoConceptInstanceType) pType).getFlexoConcept();
			
			if (ep instanceof VirtualModel) {
				VirtualModel vm = (VirtualModel) ep;
				for (ModelSlot ms : vm.getModelSlots()) {
					returned.add(getSimplePathElement(ms, parent));
				}
			}
			for (FlexoRole<?> pr : ep.getFlexoRoles()) {
				returned.add(getSimplePathElement(pr, parent));
			}
			// TODO: performance issue
			if (ep.getInspector().getRenderer().isSet() && ep.getInspector().getRenderer().isValid()) {
				returned.add(new EPIRendererPathElement(parent));
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
