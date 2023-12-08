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

package org.openflexo.foundation.fml;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.ParseException;
import org.openflexo.connie.binding.AbstractConstructor;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.Function;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.NewInstancePathElement;
import org.openflexo.connie.binding.SimpleMethodPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.fml.binding.ContainerPathElement;
import org.openflexo.foundation.fml.binding.EPIRendererPathElement;
import org.openflexo.foundation.fml.binding.EnumValuesPathElement;
import org.openflexo.foundation.fml.binding.FMLNativeProperty;
import org.openflexo.foundation.fml.binding.FlexoBehaviourParameterDefinitionPathElement;
import org.openflexo.foundation.fml.binding.FlexoBehaviourParameterValuePathElement;
import org.openflexo.foundation.fml.binding.FlexoBehaviourParametersValuesPathElement;
import org.openflexo.foundation.fml.binding.FlexoBehaviourPathElement;
import org.openflexo.foundation.fml.binding.FlexoConceptInstancePathElement;
import org.openflexo.foundation.fml.binding.FlexoConceptTypePathElement;
import org.openflexo.foundation.fml.binding.ResourceCenterPathElement;
import org.openflexo.foundation.fml.binding.VirtualModelTypePathElement;
import org.openflexo.foundation.fml.expr.FMLConstant.ObjectConstant;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.FlexoResourceCenter;
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
public class FMLBindingFactory extends AbstractFMLBindingFactory {
	static final Logger logger = Logger.getLogger(FMLBindingFactory.class.getPackage().getName());

	@Deprecated
	public static final String FLEXO_CONCEPT_INSTANCE = "flexoConceptInstance";
	@Deprecated
	public static final String VIRTUAL_MODEL_INSTANCE = "virtualModelInstance";

	public static final String RESOURCE_CENTER_PROPERTY_NAME = "resourceCenter";
	public static final FMLNativeProperty RESOURCE_CENTER_PROPERTY = new FMLNativeProperty(RESOURCE_CENTER_PROPERTY_NAME,
			FlexoResourceCenter.class);

	public static final String VIRTUAL_MODEL_PROPERTY_NAME = "virtualModelType";
	public static final FMLNativeProperty VIRTUAL_MODEL_PROPERTY = new FMLNativeProperty(VIRTUAL_MODEL_PROPERTY_NAME, VirtualModel.class);

	public static final String FLEXO_CONCEPT_PROPERTY_NAME = "conceptType";
	public static final FMLNativeProperty FLEXO_CONCEPT_PROPERTY = new FMLNativeProperty(FLEXO_CONCEPT_PROPERTY_NAME, FlexoConcept.class);

	public static final String ENUM_VALUES_PROPERTY_NAME = "enumValues";
	public static final FMLNativeProperty ENUM_VALUES_PROPERTY = new FMLNativeProperty(ENUM_VALUES_PROPERTY_NAME, List.class);

	private VirtualModel virtualModel;

	private final Map<IBindingPathElement, Map<Object, SimplePathElement<?>>> storedBindingPathElements;
	private final Map<IBindingPathElement, BehavioursForConcepts> flexoBehaviourPathElements;

	public FMLBindingFactory(VirtualModel virtualModel) {
		super();
		storedBindingPathElements = new HashMap<>();
		flexoBehaviourPathElements = new HashMap<>();
		this.virtualModel = virtualModel;
	}

	public FMLBindingFactory(FMLModelFactory modelFactory) {
		super(modelFactory);
		storedBindingPathElements = new HashMap<>();
		flexoBehaviourPathElements = new HashMap<>();
	}

	public VirtualModel getVirtualModel() {
		return virtualModel;
	}

	@Override
	public FMLModelFactory getFMLModelFactory() {
		if (virtualModel != null) {
			return virtualModel.getFMLModelFactory();
		}
		return super.getFMLModelFactory();
	}

	@Override
	public Expression parseExpression(String expressionAsString, Bindable bindable) throws ParseException {
		if (virtualModel.getDeclaringCompilationUnitResource() != null) {
			return virtualModel.getDeclaringCompilationUnitResource().parseExpression(expressionAsString, bindable);
		}
		return null;
	}

	protected SimplePathElement<?> getSimplePathElement(Object object, IBindingPathElement parent, Bindable bindable) {
		Map<Object, SimplePathElement<?>> storedValues = storedBindingPathElements.get(parent);
		if (storedValues == null) {
			storedValues = new HashMap<>();
			storedBindingPathElements.put(parent, storedValues);
		}
		SimplePathElement<?> returned = storedValues.get(object);
		if (returned == null) {
			returned = makeSimplePathElement(object, parent, bindable);
			storedValues.put(object, returned);
		}
		return returned;
	}

	protected SimplePathElement<?> makeSimplePathElement(Object object, IBindingPathElement parent, Bindable bindable) {
		if (getFMLModelFactory() == null) {
			logger.warning("Unexpected null FMLModelFactory");
			return null;
		}
		if (object instanceof ModelSlot) {
			return getFMLModelFactory().newModelSlotPathElement(parent, (ModelSlot<?>) object, bindable);
			// return new ModelSlotPathElement<ModelSlot<?>>(parent, (ModelSlot<?>) object);
		}
		if (object instanceof FlexoProperty && getFMLModelFactory() != null) {
			return getFMLModelFactory().newFlexoPropertyPathElement(parent, (FlexoProperty<?>) object, bindable);
			// return new FlexoPropertyPathElement<FlexoProperty<?>>(parent, (FlexoProperty<?>) object);
		}
		if (object instanceof FlexoBehaviourParameter) {
			if (parent.getType() instanceof FlexoBehaviourParametersType) {
				return new FlexoBehaviourParameterDefinitionPathElement(parent, (FlexoBehaviourParameter) object, bindable);
			}
			else if (parent.getType() instanceof FlexoBehaviourParametersValuesType) {
				return new FlexoBehaviourParameterValuePathElement(parent, (FlexoBehaviourParameter) object, bindable);
			}
		}
		logger.warning("Unexpected " + object + " for parent=" + parent);
		return null;
	}

	@Override
	public List<? extends SimplePathElement<?>> getAccessibleSimplePathElements(IBindingPathElement parent, Bindable bindable) {

		if (parent != null) {

			if (parent instanceof FlexoBehaviourParameterValuePathElement) {
				List<SimplePathElement<?>> returned = new ArrayList<>(_getAccessibleSimplePathElements(parent, bindable));
				FlexoBehaviourParameter p = ((FlexoBehaviourParameterValuePathElement) parent).getParameter();
				returned.add(0, new FlexoBehaviourParameterDefinitionPathElement(parent, p, bindable));
				return returned;
			}

		}

		return _getAccessibleSimplePathElements(parent, bindable);
	}

	private List<? extends SimplePathElement<?>> _getAccessibleSimplePathElements(IBindingPathElement parent, Bindable bindable) {

		if (parent != null) {

			Type pType = parent.getActualType();

			if (pType instanceof TechnologySpecificType) {
				TechnologySpecificType<?> parentType = (TechnologySpecificType<?>) pType;
				TechnologyAdapter<?> ta = parentType.getSpecificTechnologyAdapter();
				if (ta != null) {
					TechnologyAdapterBindingFactory bf = ta.getTechnologyAdapterBindingFactory();
					if (bf != null && bf.handleType(parentType)) {
						List<? extends SimplePathElement<?>> returned = bf.getAccessibleSimplePathElements(parent, bindable);
						Collections.sort(returned, BindingPathElement.COMPARATOR);
						return returned;
					}
				}

			}

			if (pType instanceof FlexoBehaviourParametersType) {
				List<SimplePathElement<?>> returned = new ArrayList<>();
				FlexoBehaviour es = ((FlexoBehaviourParametersType) pType).getFlexoBehaviour();
				for (FlexoBehaviourParameter p : es.getParameters()) {
					returned.add(getSimplePathElement(p, parent, bindable));
				}
				Collections.sort(returned, BindingPathElement.COMPARATOR);
				return returned;
			}
			else if (pType instanceof FlexoBehaviourParametersValuesType) {
				List<SimplePathElement<?>> returned = new ArrayList<>();
				FlexoBehaviour es = ((FlexoBehaviourParametersValuesType) pType).getFlexoBehaviour();
				for (FlexoBehaviourParameter p : es.getParameters()) {
					returned.add(getSimplePathElement(p, parent, bindable));
				}
				Collections.sort(returned, BindingPathElement.COMPARATOR);
				return returned;
			}
			else if (pType instanceof FlexoConceptInstanceType) {
				List<SimplePathElement<?>> returned = new ArrayList<>();
				FlexoConcept concept = ((FlexoConceptInstanceType) pType).getFlexoConcept();

				if (concept instanceof FlexoEnum) {
					returned.add(new EnumValuesPathElement(parent, (FlexoEnum) concept, bindable));
				}

				if (concept != null) {
					for (FlexoProperty<?> pr : concept.getAccessibleProperties()) {
						returned.add(getSimplePathElement(pr, parent, bindable));
					}
					if (concept.getInspector() != null && concept.getInspector().getRenderer().isSet()
							&& concept.getInspector().getRenderer().isValid()) {
						returned.add(new EPIRendererPathElement(parent, bindable));
					}
					returned.add(new FlexoConceptTypePathElement(parent, concept, bindable));
					if (concept instanceof VirtualModel) {
						returned.add(new VirtualModelTypePathElement(parent, concept, bindable));
					}

					if (concept instanceof VirtualModel && ((VirtualModel) concept).getContainerVirtualModel() == null) {
						// Special case where there is no container
					}
					else {
						returned.add(new ContainerPathElement(parent, concept, bindable));
					}

					returned.add(new ResourceCenterPathElement(parent, bindable));
				}
				return returned;
			}
			else if (pType instanceof FlexoBehaviourType) {
				List<SimplePathElement<?>> returned = new ArrayList<>();
				FlexoBehaviour flexoBehaviour = ((FlexoBehaviourType) pType).getFlexoBehaviour();
				returned.add(new FlexoBehaviourParametersValuesPathElement(parent, flexoBehaviour, bindable));
				for (FlexoProperty<?> pr : flexoBehaviour.getFlexoConcept().getAccessibleProperties()) {
					returned.add(getSimplePathElement(pr, parent, bindable));
				}
				return returned;
			}
			else if (pType instanceof FlexoBehaviourActionType) {
				List<SimplePathElement<?>> returned = new ArrayList<>();
				FlexoBehaviour flexoBehaviour = ((FlexoBehaviourActionType) pType).getFlexoBehaviour();
				returned.add(new FlexoBehaviourParametersValuesPathElement(parent, flexoBehaviour, bindable));
				for (FlexoProperty<?> pr : flexoBehaviour.getFlexoConcept().getAccessibleProperties()) {
					returned.add(getSimplePathElement(pr, parent, bindable));
				}
				if (!(flexoBehaviour instanceof CreationScheme)) {
					returned.add(new FlexoConceptInstancePathElement(parent, FLEXO_CONCEPT_INSTANCE, flexoBehaviour.getFlexoConcept(),
							bindable));
				}
				if (flexoBehaviour.getFlexoConcept().getOwningVirtualModel() != null) {
					returned.add(new ContainerPathElement(parent, flexoBehaviour.getFlexoConcept().getOwningVirtualModel(), bindable));
				}
				else {
					logger.warning("No owning virtual model declared for behaviour: " + flexoBehaviour);
				}
				returned.add(new ResourceCenterPathElement(parent, bindable));
				return returned;
			}

			// In all other cases, consider it using Java rules
			return super.getAccessibleSimplePathElements(parent, bindable);
		}
		logger.warning("Trying to find accessible path elements for a NULL parent");
		return Collections.emptyList();
	}

	@Override
	public List<? extends FunctionPathElement<?>> getAccessibleFunctionPathElements(IBindingPathElement parent, Bindable bindable) {

		Type pType = parent.getType();
		if (pType instanceof FlexoConceptInstanceType) {
			return getFlexoBehaviourPathElements(parent, ((FlexoConceptInstanceType) pType).getFlexoConcept(), bindable,
					parent.isNotifyingBindingPathChanged());
		}
		return super.getAccessibleFunctionPathElements(parent, bindable);
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
			Bindable bindable, boolean forceUpdate) {

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
					returned.add(new FlexoBehaviourPathElement(parent, behaviour, null, bindable));
				}
				map.put(concept, returned);
			}
		}
		return returned;
	}

	@Override
	public SimplePathElement<?> makeSimplePathElement(IBindingPathElement parent, String propertyName, Bindable bindable) {

		// We want to avoid code duplication, so iterate on all accessible simple path element and choose the right one
		SimplePathElement<?> returned = null;
		List<? extends SimplePathElement<?>> accessibleSimplePathElements = getAccessibleSimplePathElements(parent, bindable);
		if (accessibleSimplePathElements != null) {
			for (SimplePathElement<?> e : accessibleSimplePathElements) {
				if (e != null && e.getLabel() != null && e.getLabel().equals(propertyName)) {
					returned = e;
				}
			}
		}

		// We cannot find a simple path element at this level, retrieve from java
		if (returned == null) {
			returned = super.makeSimplePathElement(parent, propertyName, bindable);
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
	public SimpleMethodPathElement<?> makeSimpleMethodPathElement(IBindingPathElement parent, String methodName, List<DataBinding<?>> args,
			Bindable bindable) {
		/*if (function instanceof CreationScheme && !(parent instanceof SuperBindingVariable)) {
			return new CreationSchemePathElement(parent, (CreationScheme) function, args);
		}*/
		if (parent != null) {

			if (parent.getLabel().startsWith(FMLKeywords.Super.getKeyword())) {
				return new FlexoBehaviourPathElement(parent, methodName, args, bindable);
			}

			if (parent.getType() instanceof FlexoConceptInstanceType) {
				return new FlexoBehaviourPathElement(parent, methodName, args, bindable);
			}
		}

		if (methodName.equals(FMLKeywords.Super.getKeyword()) && parent == null) {
			// In this case, this a call to super() constructor
			return new FlexoBehaviourPathElement(null, FMLKeywords.Super.getKeyword(), args, bindable);
		}

		return super.makeSimpleMethodPathElement(parent, methodName, args, bindable);
		// Hook to specialize type returned by getFlexoConceptInstance(String)
		// This method is used while executing DiagramElement inspectors
		/*if (function.getName().equals("getFlexoConceptInstance")) {
			if (TypeUtils.isTypeAssignableFrom(ViewObject.class, parent.getType()) && args.size() == 1 && args.get(0).isStringConstant()) {
				String flexoConceptId = ((StringConstant) args.get(0).getExpression()).getValue();
				FlexoConcept ep = virtualModel.getFlexoConcept(flexoConceptId);
				returned.setType(FlexoConceptInstanceType.getFlexoConceptInstanceType(ep));
			}
		}*/
		// return returned;
	}

	@Override
	public NewInstancePathElement<?> makeNewInstancePathElement(Type type, IBindingPathElement parent, String functionName,
			List<DataBinding<?>> args, Bindable bindable) {
		if (type instanceof FlexoConceptInstanceType) {
			return getFMLModelFactory().newCreationSchemePathElement((FlexoConceptInstanceType) type, parent, functionName, args, bindable);
		}
		return super.makeNewInstancePathElement(type, parent, functionName, args, bindable);
	}

	@Override
	public AbstractConstructor retrieveConstructor(Type declaringType, Type innerAccessType, String constructorName,
			List<DataBinding<?>> arguments) {
		if (declaringType instanceof FlexoConceptInstanceType) {
			FlexoConcept concept = ((FlexoConceptInstanceType) declaringType).getFlexoConcept();
			if (concept != null) {
				if (constructorName == null) {
					return concept.getAnonymousCreationScheme(buildArgumentList(arguments));
				}
				else {
					return concept.getCreationScheme(constructorName, buildArgumentList(arguments));
				}
			}
			return null;
		}
		else {
			return super.retrieveConstructor(declaringType, innerAccessType, constructorName, arguments);
		}
	}

	private Type[] buildArgumentList(List<DataBinding<?>> args) {
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
		return paramsTypes;
	}

	@Override
	public Function retrieveFunction(Type parentType, String functionName, List<DataBinding<?>> args) {
		if (parentType instanceof FlexoConceptInstanceType) {
			FlexoConcept conceptType = ((FlexoConceptInstanceType) parentType).getFlexoConcept();
			// System.out.println("Looking for behaviour " + functionName + " avec " + args);
			// System.out.println("conceptType=" + conceptType);
			if (conceptType != null) {
				FlexoBehaviour returned = conceptType.getFlexoBehaviour(functionName, buildArgumentList(args));
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
