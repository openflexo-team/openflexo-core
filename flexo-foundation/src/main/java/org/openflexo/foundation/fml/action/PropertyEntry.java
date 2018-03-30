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
package org.openflexo.foundation.fml.action;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.toolbox.StringUtils;

/**
 * A specification of an existing {@link FlexoProperty} or of a {@link FlexoProperty} to be created<br>
 * Used in FML actions and wizards
 * 
 * @author sylvain
 *
 */
public class PropertyEntry<TA extends TechnologyAdapter<TA>> extends PropertyChangedSupportDefaultImplementation implements Bindable {

	public static enum PropertyType {
		PRIMITIVE,
		MODEL_SLOT,
		TECHNOLOGY_ROLE,
		FLEXO_CONCEPT_INSTANCE,
		EXPRESSION_PROPERTY,
		GET_PROPERTY,
		GET_SET_PROPERTY,
		ABSTRACT_PROPERTY;
	}

	private static PropertyType[] PRIMITIVE_TYPES_ARRAY = { PropertyType.PRIMITIVE, PropertyType.EXPRESSION_PROPERTY,
			PropertyType.GET_PROPERTY, PropertyType.GET_SET_PROPERTY, PropertyType.ABSTRACT_PROPERTY };
	private static PropertyType[] VMI_TYPES_ARRAY = { PropertyType.FLEXO_CONCEPT_INSTANCE, PropertyType.MODEL_SLOT,
			PropertyType.EXPRESSION_PROPERTY, PropertyType.GET_PROPERTY, PropertyType.GET_SET_PROPERTY, PropertyType.ABSTRACT_PROPERTY };
	private static PropertyType[] FCI_TYPES_ARRAY = { PropertyType.FLEXO_CONCEPT_INSTANCE, PropertyType.EXPRESSION_PROPERTY,
			PropertyType.GET_PROPERTY, PropertyType.GET_SET_PROPERTY, PropertyType.ABSTRACT_PROPERTY };
	private static PropertyType[] RESOURCE_DATA_TYPES_ARRAY = { PropertyType.MODEL_SLOT, PropertyType.EXPRESSION_PROPERTY,
			PropertyType.GET_PROPERTY, PropertyType.GET_SET_PROPERTY, PropertyType.ABSTRACT_PROPERTY };
	private static PropertyType[] ROLE_TYPES_ARRAY = { PropertyType.TECHNOLOGY_ROLE, PropertyType.MODEL_SLOT,
			PropertyType.EXPRESSION_PROPERTY, PropertyType.GET_PROPERTY, PropertyType.GET_SET_PROPERTY, PropertyType.ABSTRACT_PROPERTY };

	private static List<PropertyType> PRIMITIVE_TYPES = Arrays.asList(PRIMITIVE_TYPES_ARRAY);
	private static List<PropertyType> VMI_TYPES = Arrays.asList(VMI_TYPES_ARRAY);
	private static List<PropertyType> FCI_TYPES = Arrays.asList(FCI_TYPES_ARRAY);
	private static List<PropertyType> RESOURCE_DATA_TYPES = Arrays.asList(RESOURCE_DATA_TYPES_ARRAY);
	private static List<PropertyType> ROLE_TYPES = Arrays.asList(ROLE_TYPES_ARRAY);
	static List<PropertyType> ALL_TYPES = Arrays.asList(PropertyType.values());

	public static List<PropertyType> getAvailablePropertyTypes(Type type) {
		Class<?> baseClass = TypeUtils.getBaseClass(type);
		if (TypeUtils.isPrimitive(type) || (type instanceof Class && TypeUtils.isWrapperClass((Class<?>) type)) || TypeUtils.isString(type))
			return PRIMITIVE_TYPES;
		else if (type instanceof VirtualModelInstanceType)
			return VMI_TYPES;
		else if (type instanceof FlexoConceptInstanceType)
			return FCI_TYPES;
		else if (TechnologyObject.class.isAssignableFrom(baseClass)) {
			if (ResourceData.class.isAssignableFrom(baseClass))
				return RESOURCE_DATA_TYPES;
			return ROLE_TYPES;
		}
		return ALL_TYPES;
	}

	private String name;
	private Type type;
	private PropertyCardinality cardinality = PropertyCardinality.ZeroOne;

	private boolean required = true;
	private String description;
	private PropertyType propertyType;

	private DataBinding<?> defaultValue;
	private DataBinding<?> container;

	private final LocalizedDelegate locales;

	// The context in which the PropertyEntry has been defined
	private FlexoConceptObject context;

	private TA technologyAdapter;
	private Class<? extends ModelSlot<?>> modelSlotClass;
	private Class<? extends FlexoRole<?>> flexoRoleClass;

	private VirtualModelResource virtualModelResource;
	private FlexoMetaModelResource<?, ?, ?> metaModelResource;
	private FlexoConcept flexoConcept;

	private Map<TechnologyAdapter<?>, List<Class<? extends FlexoRole<?>>>> availableFlexoRoleTypes = new HashMap<>();

	public PropertyEntry(String paramName, LocalizedDelegate locales, FlexoConceptObject context) {
		super();
		this.name = paramName;
		this.locales = locales;
		this.context = context;
	}

	public String getDisplayableName() {
		return getName() + " of " + TypeUtils.simpleRepresentation(getType()) + " cardinality=" + cardinality;
	}

	public void delete() {
		name = null;
		description = null;
		type = null;
	}

	public LocalizedDelegate getLocales() {
		return locales;
	}

	public String getName() {
		if (name == null) {
			return "param";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
		getPropertyChangeSupport().firePropertyChange("name", null, name);
	}

	public Type getType() {
		if (getPropertyType() == PropertyType.MODEL_SLOT) {
			if (isVirtualModelModelSlot() && getVirtualModelResource() != null) {
				return getVirtualModelResource().getVirtualModel().getInstanceType();
			}
			if (getModelSlotClass() != null) {
				return TypeUtils.getTypeArgument(getModelSlotClass(), ModelSlot.class, 0);
			}
		}
		else if (getPropertyType() == PropertyType.TECHNOLOGY_ROLE) {
			Type genericRoleClass = Object.class;
			if (getFlexoRoleClass() != null) {
				genericRoleClass = TypeUtils.getTypeArgument(getFlexoRoleClass(), FlexoRole.class, 0);
			}
			// System.out.println("type for " + getFlexoRoleClass() + " is " + genericRoleClass);
			// System.out.println("type=" + type);
			// System.out.println("assignable=" + TypeUtils.isTypeAssignableFrom(genericRoleClass, type));
			if (type != null && TypeUtils.isTypeAssignableFrom(genericRoleClass, type)) {
				// System.out.println("Returning " + type + " for " + getName());
				return type;
			}
			return genericRoleClass;
		}
		else if (getPropertyType() == PropertyType.FLEXO_CONCEPT_INSTANCE) {
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getInstanceType();
			}
			return FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;
		}
		return type;
	}

	public void setType(Type aType) {
		if ((aType == null && this.type != null) || (aType != null && !aType.equals(this.type))) {
			Type oldValue = this.type;
			this.type = aType;
			getPropertyChangeSupport().firePropertyChange("type", oldValue, aType);
			getPropertyChangeSupport().firePropertyChange("availablePropertyTypes", null, getAvailablePropertyTypes());
			getPropertyChangeSupport().firePropertyChange("technologyAdapter", null, getTechnologyAdapter());
			if (defaultValue != null) {
				defaultValue.setDeclaredType(getType());
			}
			if (!getAvailablePropertyTypes().contains(getPropertyType()) && getAvailablePropertyTypes().size() > 0) {
				setPropertyType(getAvailablePropertyTypes().get(0));
			}
		}
	}

	public PropertyCardinality getCardinality() {
		return cardinality;
	}

	public void setCardinality(PropertyCardinality cardinality) {
		if ((cardinality == null && this.cardinality != null) || (cardinality != null && !cardinality.equals(this.cardinality))) {
			PropertyCardinality oldValue = this.cardinality;
			this.cardinality = cardinality;
			getPropertyChangeSupport().firePropertyChange("cardinality", oldValue, cardinality);
		}
	}

	public PropertyType getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(PropertyType propertyType) {
		if ((propertyType == null && this.propertyType != null) || (propertyType != null && !propertyType.equals(this.propertyType))) {
			PropertyType oldValue = this.propertyType;
			this.propertyType = propertyType;
			getPropertyChangeSupport().firePropertyChange("propertyType", oldValue, propertyType);
			getPropertyChangeSupport().firePropertyChange("technologyAdapter", oldValue, technologyAdapter);
			getPropertyChangeSupport().firePropertyChange("availableFlexoRoleTypes", null, getAvailableFlexoRoleTypes());
			getPropertyChangeSupport().firePropertyChange("availableModelSlotTypes", null, getAvailableModelSlotTypes());
			getPropertyChangeSupport().firePropertyChange("modelSlotClass", null, getModelSlotClass());
			getPropertyChangeSupport().firePropertyChange("flexoRoleClass", null, getFlexoRoleClass());
			getPropertyChangeSupport().firePropertyChange("isTypeAwareModelSlot", !isTypeAwareModelSlot(), isTypeAwareModelSlot());
			getPropertyChangeSupport().firePropertyChange("isVirtualModelModelSlot", !isVirtualModelModelSlot(), isVirtualModelModelSlot());
			getPropertyChangeSupport().firePropertyChange("type", null, getType());
		}
	}

	public List<PropertyType> getAvailablePropertyTypes() {
		return ALL_TYPES;
		// return AbstractCreateFlexoConcept.getAvailablePropertyTypes(getType());
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		getPropertyChangeSupport().firePropertyChange("description", null, description);
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
		getPropertyChangeSupport().firePropertyChange("required", null, required);
	}

	public String getConfigurationErrorMessage() {

		if (StringUtils.isEmpty(getName())) {
			return getLocales().localizedForKey("please_supply_valid_property_name");
		}
		if (getType() == null) {
			return getLocales().localizedForKey("no_property_type_defined_for") + " " + getName();
		}
		if (getPropertyType() == null) {
			return getLocales().localizedForKey("please_define_a_kind_of_property_for") + " " + getName();
		}

		return null;
	}

	public String getConfigurationWarningMessage() {
		if (StringUtils.isEmpty(getDescription())) {
			return getLocales().localizedForKey("it_is_recommanded_to_describe_property") + " " + getName();
		}
		return null;

	}

	/*public int getIndex() {
		if (getPropertiesEntries() != null) {
			return getPropertiesEntries().indexOf(this);
		}
		return -1;
	}*/

	public DataBinding<?> getDefaultValue() {
		if (defaultValue == null) {
			defaultValue = new DataBinding<>(this, getType(), BindingDefinitionType.GET);
			defaultValue.setBindingName("defaultValue");
		}
		return defaultValue;
	}

	public void setDefaultValue(DataBinding<?> defaultValue) {
		if (defaultValue != null) {
			defaultValue.setOwner(this);
			defaultValue.setBindingName("defaultValue");
			defaultValue.setDeclaredType(getType());
			defaultValue.setBindingDefinitionType(BindingDefinitionType.GET);
		}
		this.defaultValue = defaultValue;
	}

	public DataBinding<?> getContainer() {
		if (container == null) {
			container = new DataBinding<>(this, Object.class, BindingDefinitionType.GET);
			container.setBindingName("container");
		}
		return container;
	}

	public void setContainer(DataBinding<?> container) {
		if (container != null) {
			container.setOwner(this);
			container.setBindingName("container");
			container.setDeclaredType(Object.class);
			container.setBindingDefinitionType(BindingDefinitionType.GET);
		}
		this.container = container;
	}

	/**
	 * Return technology adapter
	 * 
	 * @return
	 */
	public TA getTechnologyAdapter() {
		if (technologyAdapter == null && context != null) {
			/*Class<?> baseClass = TypeUtils.getBaseClass(getType());
			if (TechnologyObject.class.isAssignableFrom(baseClass)) {
				Class<? extends TechnologyAdapter> taClass = (Class<? extends TechnologyAdapter>) TypeUtils.getTypeArgument(baseClass,
						TechnologyObject.class, 0);
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(taClass);
			}*/
			return (TA) context.getServiceManager().getTechnologyAdapterService().getTechnologyAdapters().get(0);
		}
		return technologyAdapter;
	}

	public void setTechnologyAdapter(TA technologyAdapter) {
		if ((technologyAdapter == null && this.technologyAdapter != null)
				|| (technologyAdapter != null && !technologyAdapter.equals(this.technologyAdapter))) {
			TechnologyAdapter<?> oldValue = this.technologyAdapter;
			this.technologyAdapter = technologyAdapter;
			getPropertyChangeSupport().firePropertyChange("technologyAdapter", oldValue, technologyAdapter);
			getPropertyChangeSupport().firePropertyChange("availableFlexoRoleTypes", null, getAvailableFlexoRoleTypes());
			getPropertyChangeSupport().firePropertyChange("availableModelSlotTypes", null, getAvailableModelSlotTypes());
			getPropertyChangeSupport().firePropertyChange("modelSlotClass", null, getModelSlotClass());
			getPropertyChangeSupport().firePropertyChange("flexoRoleClass", null, getFlexoRoleClass());
			getPropertyChangeSupport().firePropertyChange("isTypeAwareModelSlot", !isTypeAwareModelSlot(), isTypeAwareModelSlot());
			getPropertyChangeSupport().firePropertyChange("isVirtualModelModelSlot", !isVirtualModelModelSlot(), isVirtualModelModelSlot());
			getPropertyChangeSupport().firePropertyChange("type", null, getType());
		}
	}

	public Class<? extends ModelSlot<?>> getModelSlotClass() {
		/*if (getTechnologyAdapter() != null) {
			for (Class<? extends ModelSlot<?>> msClass : getTechnologyAdapter().getAvailableModelSlotTypes()) {
				Type rdType = TypeUtils.getTypeArgument(msClass, ModelSlot.class, 0);
				if (TypeUtils.isTypeAssignableFrom(getType(), rdType)) {
					return msClass;
				}
			}
		}*/
		if (modelSlotClass == null && getAvailableModelSlotTypes() != null && getAvailableModelSlotTypes().size() > 0) {
			return getAvailableModelSlotTypes().get(0);
		}
		return modelSlotClass;
	}

	public void setModelSlotClass(Class<? extends ModelSlot<?>> modelSlotClass) {
		if ((modelSlotClass == null && this.modelSlotClass != null)
				|| (modelSlotClass != null && !modelSlotClass.equals(this.modelSlotClass))) {
			Class<? extends ModelSlot<?>> oldValue = this.modelSlotClass;
			this.modelSlotClass = modelSlotClass;
			getPropertyChangeSupport().firePropertyChange("modelSlotClass", oldValue, modelSlotClass);
			getPropertyChangeSupport().firePropertyChange("isTypeAwareModelSlot", !isTypeAwareModelSlot(), isTypeAwareModelSlot());
			getPropertyChangeSupport().firePropertyChange("isVirtualModelModelSlot", !isVirtualModelModelSlot(), isVirtualModelModelSlot());
			getPropertyChangeSupport().firePropertyChange("type", null, getType());
		}
	}

	public Class<? extends FlexoRole<?>> getFlexoRoleClass() {
		/*if (getTechnologyAdapter() != null) {
			for (Class<? extends ModelSlot<?>> msClass : getTechnologyAdapter().getAvailableModelSlotTypes()) {
				for (Class<? extends FlexoRole<?>> roleClass : getTechnologyAdapter().getTechnologyAdapterService()
						.getAvailableFlexoRoleTypes(msClass)) {
					Type rdType = TypeUtils.getTypeArgument(roleClass, FlexoRole.class, 0);
					if (TypeUtils.isTypeAssignableFrom(getType(), rdType)) {
						return roleClass;
					}
				}
			}
		}*/
		if (flexoRoleClass == null && getAvailableFlexoRoleTypes() != null && getAvailableFlexoRoleTypes().size() > 0) {
			return getAvailableFlexoRoleTypes().get(0);
		}
		return flexoRoleClass;
	}

	public void setFlexoRoleClass(Class<? extends FlexoRole<?>> flexoRoleClass) {
		if ((flexoRoleClass == null && this.flexoRoleClass != null)
				|| (flexoRoleClass != null && !flexoRoleClass.equals(this.flexoRoleClass))) {
			Class<? extends FlexoRole<?>> oldValue = this.flexoRoleClass;
			this.flexoRoleClass = flexoRoleClass;
			getPropertyChangeSupport().firePropertyChange("flexoRoleClass", oldValue, flexoRoleClass);
			getPropertyChangeSupport().firePropertyChange("type", null, getType());
		}
	}

	@Override
	public BindingModel getBindingModel() {
		if (context != null) {
			return context.getBindingModel();
		}
		return null;
	}

	@Override
	public BindingFactory getBindingFactory() {
		return ((Bindable) context).getBindingFactory();
	}

	@Override
	public void notifiedBindingChanged(DataBinding<?> dataBinding) {
	}

	@Override
	public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
	}

	public List<Class<? extends FlexoRole<?>>> getAvailableFlexoRoleTypes() {
		if (getTechnologyAdapter() == null) {
			return null;
		}
		List<Class<? extends FlexoRole<?>>> returned = availableFlexoRoleTypes.get(getTechnologyAdapter());
		if (returned == null) {
			returned = buildAvailableFlexoRoleTypes(getTechnologyAdapter());
			availableFlexoRoleTypes.put(getTechnologyAdapter(), returned);
		}
		return returned;
	}

	private static List<Class<? extends FlexoRole<?>>> buildAvailableFlexoRoleTypes(TechnologyAdapter<?> ta) {
		List<Class<? extends FlexoRole<?>>> returned = new ArrayList<>();
		for (Class<? extends ModelSlot<?>> modelSlotClass : ta.getAvailableModelSlotTypes()) {
			for (Class<? extends FlexoRole<?>> flexoRoleClass : ta.getTechnologyAdapterService()
					.getAvailableFlexoRoleTypes(modelSlotClass)) {
				if (!returned.contains(flexoRoleClass)) {
					returned.add(flexoRoleClass);
				}
			}
		}
		return returned;
	}

	public List<Class<? extends ModelSlot<?>>> getAvailableModelSlotTypes() {
		if (getTechnologyAdapter() != null) {
			return getTechnologyAdapter().getAvailableModelSlotTypes();
		}
		return null;
	}

	public boolean isTypeAwareModelSlot() {
		return getModelSlotClass() != null && !isVirtualModelModelSlot() && TypeAwareModelSlot.class.isAssignableFrom(getModelSlotClass());
	}

	public boolean isVirtualModelModelSlot() {
		return getModelSlotClass() != null && FMLRTModelSlot.class.isAssignableFrom(getModelSlotClass());
	}

	public FlexoConcept getFlexoConcept() {
		return flexoConcept;
	}

	public void setFlexoConcept(FlexoConcept flexoConcept) {
		if ((flexoConcept == null && this.flexoConcept != null) || (flexoConcept != null && !flexoConcept.equals(this.flexoConcept))) {
			FlexoConcept oldValue = this.flexoConcept;
			this.flexoConcept = flexoConcept;
			getPropertyChangeSupport().firePropertyChange("flexoConcept", oldValue, flexoConcept);
			getPropertyChangeSupport().firePropertyChange("type", null, getType());
		}
	}

	public VirtualModelResource getVirtualModelResource() {
		return virtualModelResource;
	}

	public void setVirtualModelResource(VirtualModelResource virtualModelResource) {
		if ((virtualModelResource == null && this.virtualModelResource != null)
				|| (virtualModelResource != null && !virtualModelResource.equals(this.virtualModelResource))) {
			VirtualModelResource oldValue = this.virtualModelResource;
			this.virtualModelResource = virtualModelResource;
			getPropertyChangeSupport().firePropertyChange("virtualModelResource", oldValue, virtualModelResource);
			getPropertyChangeSupport().firePropertyChange("type", null, getType());
		}
	}

	public FlexoMetaModelResource<?, ?, ?> getMetaModelResource() {
		return metaModelResource;
	}

	public void setMetaModelResource(FlexoMetaModelResource<?, ?, ?> metaModelResource) {
		if ((metaModelResource == null && this.metaModelResource != null)
				|| (metaModelResource != null && !metaModelResource.equals(this.metaModelResource))) {
			FlexoMetaModelResource<?, ?, ?> oldValue = this.metaModelResource;
			this.metaModelResource = metaModelResource;
			getPropertyChangeSupport().firePropertyChange("metaModelResource", oldValue, metaModelResource);
			getPropertyChangeSupport().firePropertyChange("type", null, getType());
		}
	}

	/**
	 * Called to create a new {@link FlexoProperty} conform to this specification, in the supplied FlexoConcept and as a embedded action of
	 * supplied container action
	 * 
	 * 
	 * @param destinationConcept
	 * @param containerAction
	 * @return
	 */
	public FlexoProperty<?> performCreateProperty(FlexoConcept destinationConcept, FlexoAction<?, ?, ?> containerAction) {
		Progress.progress(getLocales().localizedForKey("create_property") + " " + getName());

		AbstractCreateFlexoProperty<?> action = null;

		switch (getPropertyType()) {
			case PRIMITIVE:
				CreatePrimitiveRole createPrimitive = CreatePrimitiveRole.actionType.makeNewEmbeddedAction(destinationConcept, null,
						containerAction);
				action = createPrimitive;
				createPrimitive.setRoleName(getName());
				createPrimitive.setCardinality(getCardinality());
				if (TypeUtils.isString(getType())) {
					createPrimitive.setPrimitiveType(PrimitiveType.String);
				}
				if (TypeUtils.isDate(getType())) {
					createPrimitive.setPrimitiveType(PrimitiveType.Date);
				}
				if (TypeUtils.isBoolean(getType())) {
					createPrimitive.setPrimitiveType(PrimitiveType.Boolean);
				}
				if (TypeUtils.isInteger(getType()) || TypeUtils.isLong(getType()) || TypeUtils.isShort(getType())
						|| TypeUtils.isByte(getType())) {
					createPrimitive.setPrimitiveType(PrimitiveType.Integer);
				}
				if (TypeUtils.isFloat(getType())) {
					createPrimitive.setPrimitiveType(PrimitiveType.Float);
				}
				if (TypeUtils.isDouble(getType())) {
					createPrimitive.setPrimitiveType(PrimitiveType.Double);
				}
				break;
			case ABSTRACT_PROPERTY:
				CreateAbstractProperty createAbstractProperty = CreateAbstractProperty.actionType.makeNewEmbeddedAction(destinationConcept,
						null, containerAction);
				action = createAbstractProperty;
				createAbstractProperty.setPropertyName(getName());
				createAbstractProperty.setPropertyType(getType());
				break;
			case EXPRESSION_PROPERTY:
				CreateExpressionProperty createExpressionProperty = CreateExpressionProperty.actionType
						.makeNewEmbeddedAction(destinationConcept, null, containerAction);
				action = createExpressionProperty;
				createExpressionProperty.setPropertyName(getName());
				break;
			case GET_PROPERTY:
				CreateGetSetProperty createGetProperty = CreateGetSetProperty.actionType.makeNewEmbeddedAction(destinationConcept, null,
						containerAction);
				action = createGetProperty;
				createGetProperty.setPropertyName(getName());
				break;
			case GET_SET_PROPERTY:
				CreateGetSetProperty createGetSetProperty = CreateGetSetProperty.actionType.makeNewEmbeddedAction(destinationConcept, null,
						containerAction);
				action = createGetSetProperty;
				createGetSetProperty.setPropertyName(getName());
				break;
			case FLEXO_CONCEPT_INSTANCE:
				CreateFlexoConceptInstanceRole createFCIRole = CreateFlexoConceptInstanceRole.actionType
						.makeNewEmbeddedAction(destinationConcept, null, containerAction);
				action = createFCIRole;
				createFCIRole.setPropertyName(getName());
				/*if (getType() instanceof FlexoConceptInstanceType) {
					createFCIRole.setFlexoConceptInstanceType(((FlexoConceptInstanceType) getType()).getFlexoConcept());
				}*/
				createFCIRole.setFlexoConceptInstanceType(getFlexoConcept());
				createFCIRole.setVirtualModelInstance(new DataBinding<VirtualModelInstance<?, ?>>(getContainer().toString()));
				break;
			case MODEL_SLOT:
				CreateModelSlot createModelSlot = CreateModelSlot.actionType.makeNewEmbeddedAction(destinationConcept, null,
						containerAction);
				action = createModelSlot;
				createModelSlot.setModelSlotName(getName());
				if (getTechnologyAdapter() != null) {
					createModelSlot.setTechnologyAdapter(getTechnologyAdapter());
					createModelSlot.setModelSlotClass(getModelSlotClass());
					if (isVirtualModelModelSlot()) {
						createModelSlot.setVmRes(getVirtualModelResource());
					}
					if (isTypeAwareModelSlot()) {
						createModelSlot.setMmRes(getMetaModelResource());
					}
					// System.out.println("ModelSlotClass=" + getModelSlotClass());
				}
				break;
			case TECHNOLOGY_ROLE:
				CreateTechnologyRole createTechnologyRole = CreateTechnologyRole.actionType.makeNewEmbeddedAction(destinationConcept, null,
						containerAction);
				action = createTechnologyRole;
				createTechnologyRole.setRoleName(getName());
				if (getTechnologyAdapter() != null && getFlexoRoleClass() != null) {
					// System.out.println("FlexoRoleClass= " + getFlexoRoleClass());
					// System.out.println("container= " + getContainer());
					// System.out.println("defaultValue= " + getDefaultValue());
					createTechnologyRole.setFlexoRoleClass(getFlexoRoleClass());
					createTechnologyRole.setIsRequired(isRequired());
					createTechnologyRole.setContainer(new DataBinding<>(getContainer().toString()));
					createTechnologyRole.setDefaultValue(new DataBinding<>(getDefaultValue().toString()));
				}
				break;
		}

		if (action != null) {
			action.setDescription(getDescription());
			System.out.println("Executing action " + action + " valid=" + action.isValid());
			action.doAction();
			return action.getNewFlexoProperty();
		}
		System.out.println("Create property " + getName() + " not implemented yet");
		return null;
	}
}
