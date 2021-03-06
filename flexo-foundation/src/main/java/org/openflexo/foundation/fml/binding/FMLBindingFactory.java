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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.expr.Constant.ObjectConstant;
import org.openflexo.connie.java.JavaBindingFactory;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourActionType;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoBehaviourParametersType;
import org.openflexo.foundation.fml.FlexoBehaviourParametersValuesType;
import org.openflexo.foundation.fml.FlexoBehaviourType;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.TechnologySpecificType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterBindingFactory;

/**
 * This is the FML binding factory, which allow to define how to browse inside FML model<br>
 * A {@link FMLBindingFactory} should be build using a {@link VirtualModel} which defines the scope of types beeing managed in this
 * BindingFactory (generally defined for the top-level {@link VirtualModel})
 * 
 * @author sylvain
 *
 */
// TODO: manage a map for structural properties (same as for behaviors)
public class FMLBindingFactory extends JavaBindingFactory {
	static final Logger logger = Logger.getLogger(FMLBindingFactory.class.getPackage().getName());

	@Deprecated
	public static final String FLEXO_CONCEPT_INSTANCE = "flexoConceptInstance";
	@Deprecated
	public static final String VIRTUAL_MODEL_INSTANCE = "virtualModelInstance";
	public static final String RESOURCE_CENTER = "resourceCenter";

	private final Map<IBindingPathElement, Map<Object, SimplePathElement>> storedBindingPathElements;
	// Unused private final VirtualModel virtualModel;

	private final Map<IBindingPathElement, BehavioursForConcepts> flexoBehaviourPathElements;

	public FMLBindingFactory(VirtualModel virtualModel) {
		storedBindingPathElements = new HashMap<>();
		flexoBehaviourPathElements = new HashMap<>();
		// Unused this.virtualModel = virtualModel;
	}

	protected SimplePathElement getSimplePathElement(Object object, IBindingPathElement parent) {
		Map<Object, SimplePathElement> storedValues = storedBindingPathElements.get(parent);
		if (storedValues == null) {
			storedValues = new HashMap<>();
			storedBindingPathElements.put(parent, storedValues);
		}
		SimplePathElement returned = storedValues.get(object);
		if (returned == null) {
			returned = makeSimplePathElement(object, parent);
			storedValues.put(object, returned);
		}
		return returned;
	}

	protected SimplePathElement makeSimplePathElement(Object object, IBindingPathElement parent) {
		if (object instanceof ModelSlot) {
			return new ModelSlotPathElement<ModelSlot<?>>(parent, (ModelSlot<?>) object);
		}
		if (object instanceof FlexoProperty) {
			return new FlexoPropertyPathElement<FlexoProperty<?>>(parent, (FlexoProperty<?>) object);
		}
		if (object instanceof FlexoBehaviourParameter) {
			if (parent.getType() instanceof FlexoBehaviourParametersType) {
				return new FlexoBehaviourParameterDefinitionPathElement(parent, (FlexoBehaviourParameter) object);
			}
			else if (parent.getType() instanceof FlexoBehaviourParametersValuesType) {
				return new FlexoBehaviourParameterValuePathElement(parent, (FlexoBehaviourParameter) object);
			}
		}
		logger.warning("Unexpected " + object + " for parent=" + parent);
		return null;
	}

	@Override
	public List<? extends SimplePathElement> getAccessibleSimplePathElements(IBindingPathElement parent) {

		if (parent != null) {

			if (parent instanceof FlexoBehaviourParameterValuePathElement) {
				List<SimplePathElement> returned = new ArrayList<>(_getAccessibleSimplePathElements(parent));
				FlexoBehaviourParameter p = ((FlexoBehaviourParameterValuePathElement) parent).getParameter();
				returned.add(0, new FlexoBehaviourParameterDefinitionPathElement(parent, p));
				return returned;
			}

		}

		return _getAccessibleSimplePathElements(parent);
	}

	private List<? extends SimplePathElement> _getAccessibleSimplePathElements(IBindingPathElement parent) {

		if (parent != null) {

			Type pType = parent.getType();

			if (pType instanceof TechnologySpecificType) {
				TechnologySpecificType<?> parentType = (TechnologySpecificType<?>) pType;
				TechnologyAdapter<?> ta = parentType.getSpecificTechnologyAdapter();
				if (ta != null) {
					TechnologyAdapterBindingFactory bf = ta.getTechnologyAdapterBindingFactory();
					if (bf != null && bf.handleType(parentType)) {
						List<? extends SimplePathElement> returned = bf.getAccessibleSimplePathElements(parent);
						Collections.sort(returned, BindingPathElement.COMPARATOR);
						return returned;
					}
				}

			}

			if (pType instanceof FlexoBehaviourParametersType) {
				List<SimplePathElement> returned = new ArrayList<>();
				FlexoBehaviour es = ((FlexoBehaviourParametersType) pType).getFlexoBehaviour();
				for (FlexoBehaviourParameter p : es.getParameters()) {
					returned.add(getSimplePathElement(p, parent));
				}
				Collections.sort(returned, BindingPathElement.COMPARATOR);
				return returned;
			}
			else if (pType instanceof FlexoBehaviourParametersValuesType) {
				List<SimplePathElement> returned = new ArrayList<>();
				FlexoBehaviour es = ((FlexoBehaviourParametersValuesType) pType).getFlexoBehaviour();
				for (FlexoBehaviourParameter p : es.getParameters()) {
					returned.add(getSimplePathElement(p, parent));
				}
				Collections.sort(returned, BindingPathElement.COMPARATOR);
				return returned;
			}
			else if (pType instanceof FlexoConceptInstanceType) {
				List<SimplePathElement> returned = new ArrayList<>();
				FlexoConcept concept = ((FlexoConceptInstanceType) pType).getFlexoConcept();

				if (concept instanceof FlexoEnum) {
					returned.add(new EnumValuesPathElement(parent, (FlexoEnum) concept));
				}

				if (concept != null) {
					for (FlexoProperty<?> pr : concept.getAccessibleProperties()) {
						returned.add(getSimplePathElement(pr, parent));
					}
					if (concept.getInspector() != null && concept.getInspector().getRenderer().isSet()
							&& concept.getInspector().getRenderer().isValid()) {
						returned.add(new EPIRendererPathElement(parent));
					}
					returned.add(new FlexoConceptTypePathElement(parent, concept));
					if (concept instanceof VirtualModel) {
						returned.add(new VirtualModelTypePathElement(parent, concept));
					}

					if (concept instanceof VirtualModel && ((VirtualModel) concept).getContainerVirtualModel() == null) {
						// Special case where there is no container
					}
					else {
						returned.add(new ContainerPathElement(parent, concept));
					}

					returned.add(new ResourceCenterPathElement(parent));
				}
				return returned;
			}
			else if (pType instanceof FlexoBehaviourType) {
				List<SimplePathElement> returned = new ArrayList<>();
				FlexoBehaviour flexoBehaviour = ((FlexoBehaviourType) pType).getFlexoBehaviour();
				returned.add(new FlexoBehaviourParametersValuesPathElement(parent, flexoBehaviour));
				for (FlexoProperty<?> pr : flexoBehaviour.getFlexoConcept().getAccessibleProperties()) {
					returned.add(getSimplePathElement(pr, parent));
				}
				return returned;
			}
			else if (pType instanceof FlexoBehaviourActionType) {
				List<SimplePathElement> returned = new ArrayList<>();
				FlexoBehaviour flexoBehaviour = ((FlexoBehaviourActionType) pType).getFlexoBehaviour();
				returned.add(new FlexoBehaviourParametersValuesPathElement(parent, flexoBehaviour));
				for (FlexoProperty<?> pr : flexoBehaviour.getFlexoConcept().getAccessibleProperties()) {
					returned.add(getSimplePathElement(pr, parent));
				}
				if (!(flexoBehaviour instanceof CreationScheme)) {
					returned.add(new FlexoConceptInstancePathElement(parent, FLEXO_CONCEPT_INSTANCE, flexoBehaviour.getFlexoConcept()));
				}
				if (flexoBehaviour.getFlexoConcept().getOwningVirtualModel() != null) {
					returned.add(new ContainerPathElement(parent, flexoBehaviour.getFlexoConcept().getOwningVirtualModel()));
				}
				else {
					logger.warning("No owning virtual model declared for behaviour: " + flexoBehaviour);
				}
				returned.add(new ResourceCenterPathElement(parent));
				return returned;
			}

			// In all other cases, consider it using Java rules
			return super.getAccessibleSimplePathElements(parent);
		}
		logger.warning("Trying to find accessible path elements for a NULL parent");
		return Collections.emptyList();
	}

	@Override
	public List<? extends FunctionPathElement> getAccessibleFunctionPathElements(IBindingPathElement parent) {

		Type pType = parent.getType();
		if (pType instanceof FlexoConceptInstanceType) {
			return getFlexoBehaviourPathElements(parent, ((FlexoConceptInstanceType) pType).getFlexoConcept(),
					parent.isNotifyingBindingPathChanged());
		}
		return super.getAccessibleFunctionPathElements(parent);
	}

	/**
	 * An internal map implementation storing list of {@link FlexoBehaviour} associated to {@link FlexoConcept} instances
	 * 
	 * @author sylvain
	 *
	 */
	@SuppressWarnings("serial")
	class BehavioursForConcepts extends HashMap<FlexoConcept, List<FlexoBehaviourPathElement>> implements PropertyChangeListener {

		@Override
		public List<FlexoBehaviourPathElement> put(FlexoConcept concept, List<FlexoBehaviourPathElement> value) {
			List<FlexoBehaviourPathElement> returned = super.put(concept, value);
			if (concept != null && concept.getPropertyChangeSupport() != null) {
				concept.getPropertyChangeSupport().addPropertyChangeListener(this);
			}
			return returned;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() instanceof FlexoConcept && evt.getPropertyName().equals(FlexoConcept.FLEXO_BEHAVIOURS_KEY)) {
				FlexoConcept source = (FlexoConcept) evt.getSource();
				// We have to clear all entries for all concepts which are sub-concept of this concept
				for (FlexoConcept key : new ArrayList<>(keySet())) {
					if (source.isAssignableFrom(key)) {
						remove(key);
					}
				}
			}
		}

		@Override
		public List<FlexoBehaviourPathElement> remove(Object key) {
			if (key instanceof FlexoConcept && ((FlexoConcept) key).getPropertyChangeSupport() != null) {
				((FlexoConcept) key).getPropertyChangeSupport().removePropertyChangeListener(this);
			}
			return super.remove(key);
		}

		@Override
		public void clear() {
			for (FlexoConcept concept : new ArrayList<>(keySet())) {
				if (concept.getPropertyChangeSupport() != null) {
					concept.getPropertyChangeSupport().removePropertyChangeListener(this);
				}
			}
			super.clear();
		}
	}

	public List<FlexoBehaviourPathElement> getFlexoBehaviourPathElements(IBindingPathElement parent, FlexoConcept concept,
			boolean forceUpdate) {

		BehavioursForConcepts map = flexoBehaviourPathElements.get(parent);
		if (map == null) {
			map = new BehavioursForConcepts();
			flexoBehaviourPathElements.put(parent, map);
		}
		List<FlexoBehaviourPathElement> returned = map.get(concept);
		if (returned != null && forceUpdate) {
			returned.clear();
		}

		if (returned == null || forceUpdate) {
			returned = new ArrayList<>();
			if (concept != null) {
				for (FlexoBehaviour behaviour : concept.getAccessibleFlexoBehaviours(true)) {
					returned.add(new FlexoBehaviourPathElement(parent, behaviour, null));
				}
				map.put(concept, returned);
			}
		}
		return returned;
	}

	@Override
	public SimplePathElement makeSimplePathElement(IBindingPathElement parent, String propertyName) {

		// We want to avoid code duplication, so iterate on all accessible simple path element and choose the right one
		SimplePathElement returned = null;
		List<? extends SimplePathElement> accessibleSimplePathElements = getAccessibleSimplePathElements(parent);
		if (accessibleSimplePathElements != null) {
			for (SimplePathElement e : accessibleSimplePathElements) {
				if (e.getLabel() != null && e.getLabel().equals(propertyName)) {
					returned = e;
				}
			}
		}

		// We cannot find a simple path element at this level, retrieve from java
		if (returned == null) {
			returned = super.makeSimplePathElement(parent, propertyName);
		}
		// Hook to specialize type returned by FlexoBehaviourAction.getEditionScheme()
		// This method is used while executing DiagramElement inspectors
		// TODO: (sylvain) still required ???
		if (returned != null && propertyName.equals("editionScheme") && (parent.getType() instanceof FlexoBehaviourActionType)) {
			returned.setType(FlexoBehaviourType.getFlexoBehaviourType(((FlexoBehaviourActionType) parent.getType()).getFlexoBehaviour()));
		}
		return returned;
	}

	@Override
	public FunctionPathElement makeFunctionPathElement(IBindingPathElement parent, Function function, List<DataBinding<?>> args) {
		if (parent.getType() == null) {
			return null;
		}
		if (parent.getType() instanceof FlexoConceptInstanceType && function instanceof FlexoBehaviour) {
			return new FlexoBehaviourPathElement(parent, (FlexoBehaviour) function, args);
		}
		FunctionPathElement returned = super.makeFunctionPathElement(parent, function, args);
		// Hook to specialize type returned by getFlexoConceptInstance(String)
		// This method is used while executing DiagramElement inspectors
		/*if (function.getName().equals("getFlexoConceptInstance")) {
			if (TypeUtils.isTypeAssignableFrom(ViewObject.class, parent.getType()) && args.size() == 1 && args.get(0).isStringConstant()) {
				String flexoConceptId = ((StringConstant) args.get(0).getExpression()).getValue();
				FlexoConcept ep = virtualModel.getFlexoConcept(flexoConceptId);
				returned.setType(FlexoConceptInstanceType.getFlexoConceptInstanceType(ep));
			}
		}*/
		return returned;
	}

	@Override
	public Function retrieveFunction(Type parentType, String functionName, List<DataBinding<?>> args) {
		if (parentType instanceof FlexoConceptInstanceType) {
			FlexoConcept conceptType = ((FlexoConceptInstanceType) parentType).getFlexoConcept();
			// System.out.println("Looking for behaviour " + functionName + " avec " + args);
			// System.out.println("conceptType=" + conceptType);
			if (conceptType != null) {
				Type[] paramsTypes = new Type[args.size()];
				for (int i = 0; i < args.size(); i++) {
					if (args.get(i).getExpression() instanceof ObjectConstant) {
						Object value = ((ObjectConstant) args.get(i).getExpression()).getValue();
						if (value instanceof VirtualModelInstance) {
							paramsTypes[i] = ((VirtualModelInstance<?, ?>) value).getVirtualModel().getInstanceType();
						}
						else if (value instanceof FlexoConceptInstance) {
							paramsTypes[i] = ((FlexoConceptInstance) value).getFlexoConcept().getInstanceType();
						}
						else {
							paramsTypes[i] = value.getClass();
						}
					}
					else {
						paramsTypes[i] = args.get(i).getAnalyzedType();
					}
					// System.out.println("> " + args.get(i) + " of " + paramsTypes[i]);
				}
				// System.out.println("Returned: " + conceptType.getFlexoBehaviour(functionName, paramsTypes));
				Function returned = conceptType.getFlexoBehaviour(functionName, paramsTypes);
				if (returned != null) {
					return returned;
				}
			}
		}
		return super.retrieveFunction(parentType, functionName, args);
	}

	@Override
	public Type getTypeForObject(Object object) {
		if (object instanceof FlexoConceptInstance) {
			return ((FlexoConceptInstance) object).getFlexoConcept().getInstanceType();
		}
		return super.getTypeForObject(object);
	}
}
