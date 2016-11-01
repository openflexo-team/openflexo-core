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
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.LongRunningAction;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.InconsistentFlexoConceptHierarchyException;
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour.BehaviourParameterEntry;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.ModelSlot.ModelSlotImpl;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.toolbox.StringUtils;

/**
 * Abstract action creating a {@link FlexoConcept} or any of its subclass
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractCreateFlexoConcept<A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FMLObject>
		extends FlexoAction<A, T1, T2>implements LongRunningAction {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCreateFlexoConcept.class.getPackage().getName());

	private final List<ParentFlexoConceptEntry> parentFlexoConceptEntries;

	private List<PropertyEntry> propertiesEntries;
	private List<PropertyEntry> propertiesUsedForCreationScheme;
	private List<PropertyEntry> propertiesUsedForInspector;

	public static final String PARENT_FLEXO_CONCEPT_ENTRIES = "parentFlexoConceptEntries";
	public static final String PROPERTIES_ENTRIES = "propertiesEntries";

	AbstractCreateFlexoConcept(FlexoActionType<A, T1, T2> actionType, T1 focusedObject, Vector<T2> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		parentFlexoConceptEntries = new ArrayList<>();
		propertiesEntries = new ArrayList<>();
	}

	public abstract FlexoConcept getNewFlexoConcept();

	public List<ParentFlexoConceptEntry> getParentFlexoConceptEntries() {
		return parentFlexoConceptEntries;
	}

	public ParentFlexoConceptEntry newParentFlexoConceptEntry() {
		ParentFlexoConceptEntry returned = new ParentFlexoConceptEntry();
		parentFlexoConceptEntries.add(returned);
		getPropertyChangeSupport().firePropertyChange(PARENT_FLEXO_CONCEPT_ENTRIES, null, returned);
		return returned;
	}

	public void deleteParentFlexoConceptEntry(ParentFlexoConceptEntry parentFlexoConceptEntryToDelete) {
		parentFlexoConceptEntries.remove(parentFlexoConceptEntryToDelete);
		parentFlexoConceptEntryToDelete.delete();
		getPropertyChangeSupport().firePropertyChange(PARENT_FLEXO_CONCEPT_ENTRIES, parentFlexoConceptEntryToDelete, null);
	}

	public ParentFlexoConceptEntry addToParentConcepts(FlexoConcept parentFlexoConcept) {
		ParentFlexoConceptEntry newParentFlexoConceptEntry = new ParentFlexoConceptEntry(parentFlexoConcept);
		parentFlexoConceptEntries.add(newParentFlexoConceptEntry);
		getPropertyChangeSupport().firePropertyChange(PARENT_FLEXO_CONCEPT_ENTRIES, null, newParentFlexoConceptEntry);
		return newParentFlexoConceptEntry;
	}

	protected void performSetParentConcepts() throws InconsistentFlexoConceptHierarchyException {
		for (ParentFlexoConceptEntry entry : getParentFlexoConceptEntries()) {
			getNewFlexoConcept().addToParentFlexoConcepts(entry.getParentConcept());
		}
	}

	public List<PropertyEntry> getPropertiesEntries() {
		return propertiesEntries;
	}

	public List<PropertyEntry> getPropertiesUsedForCreationScheme() {
		if (propertiesUsedForCreationScheme == null) {
			propertiesUsedForCreationScheme = new ArrayList<>();
			propertiesUsedForCreationScheme.addAll(getPropertiesEntries());
		}
		return propertiesUsedForCreationScheme;
	}

	public List<PropertyEntry> getPropertiesUsedForInspector() {
		if (propertiesUsedForInspector == null) {
			propertiesUsedForInspector = new ArrayList<>();
			propertiesUsedForInspector.addAll(getPropertiesEntries());
		}
		return propertiesUsedForInspector;
	}

	public PropertyEntry newPropertyEntry() {
		PropertyEntry returned = new PropertyEntry("property" + (getPropertiesEntries().size() + 1), getLocales());
		returned.setType(String.class);
		propertiesEntries.add(returned);
		getPropertyChangeSupport().firePropertyChange(PROPERTIES_ENTRIES, null, returned);
		return returned;
	}

	public void deletePropertyEntry(PropertyEntry propertyEntryToDelete) {
		propertiesEntries.remove(propertyEntryToDelete);
		propertyEntryToDelete.delete();
		getPropertyChangeSupport().firePropertyChange(PROPERTIES_ENTRIES, propertyEntryToDelete, null);
	}

	public void propertyFirst(PropertyEntry p) {
		getPropertiesEntries().remove(p);
		getPropertiesEntries().add(0, p);
		getPropertyChangeSupport().firePropertyChange(PROPERTIES_ENTRIES, null, getPropertiesEntries());
	}

	public void propertyUp(PropertyEntry p) {
		int index = getPropertiesEntries().indexOf(p);
		if (index > 0) {
			getPropertiesEntries().remove(p);
			getPropertiesEntries().add(index - 1, p);
			getPropertyChangeSupport().firePropertyChange(PROPERTIES_ENTRIES, null, getPropertiesEntries());
		}
	}

	public void propertyDown(PropertyEntry p) {
		int index = getPropertiesEntries().indexOf(p);
		if (index > -1) {
			getPropertiesEntries().remove(p);
			getPropertiesEntries().add(index + 1, p);
			getPropertyChangeSupport().firePropertyChange(PROPERTIES_ENTRIES, null, getPropertiesEntries());
		}
	}

	public void propertyLast(PropertyEntry p) {
		getPropertiesEntries().remove(p);
		getPropertiesEntries().add(p);
		getPropertyChangeSupport().firePropertyChange(PROPERTIES_ENTRIES, null, getPropertiesEntries());
	}

	protected void performCreateProperties() {
		for (PropertyEntry entry : getPropertiesEntries()) {
			performCreateProperty(entry);
		}
	}

	private String paramNameForEntry(PropertyEntry entry) {
		String capitalizedName = entry.getName().substring(0, 1).toUpperCase() + entry.getName().substring(1);
		if (capitalizedName.startsWith("A") || capitalizedName.startsWith("E") || capitalizedName.startsWith("I")
				|| capitalizedName.startsWith("O") || capitalizedName.startsWith("U")) {
			return "an" + capitalizedName;
		}
		return "a" + capitalizedName;
	}

	protected void performCreateBehaviours() {
		if (getDefineSomeBehaviours()) {
			if (getDefineDefaultCreationScheme()) {
				CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewEmbeddedAction(getNewFlexoConcept(),
						null, this);
				createCreationScheme.setFlexoBehaviourName("create");
				createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
				for (PropertyEntry entry : getPropertiesUsedForCreationScheme()) {
					BehaviourParameterEntry newEntry = createCreationScheme.newParameterEntry();
					newEntry.setParameterName(paramNameForEntry(entry));
					newEntry.setParameterType(entry.getType());
					newEntry.setContainer(entry.getContainer());
					newEntry.setDefaultValue(entry.getDefaultValue());
					newEntry.setParameterDescription(entry.getDescription());
				}
				System.out.println("action valide = " + createCreationScheme.isValid());
				createCreationScheme.doAction();
				CreationScheme creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();
				for (PropertyEntry entry : getPropertiesUsedForCreationScheme()) {
					CreateEditionAction assignAction = CreateEditionAction.actionType
							.makeNewEmbeddedAction(creationScheme.getControlGraph(), null, this);
					assignAction.setEditionActionClass(ExpressionAction.class);
					assignAction.setAssignation(new DataBinding<Object>(entry.getName()));
					assignAction.doAction();
					AssignationAction<?> createRightMember = (AssignationAction<?>) assignAction.getNewEditionAction();
					((ExpressionAction) createRightMember.getAssignableAction())
							.setExpression(new DataBinding<Object>("parameters." + paramNameForEntry(entry)));
				}

			}

			if (getDefineDefaultDeletionScheme()) {
				getNewFlexoConcept().generateDefaultDeletionScheme();
			}

			if (getDefineSynchronizationScheme()) {
				CreateFlexoBehaviour createSynchronizationScheme = CreateFlexoBehaviour.actionType
						.makeNewEmbeddedAction(getNewFlexoConcept(), null, this);
				createSynchronizationScheme.setFlexoBehaviourName("synchronize");
				createSynchronizationScheme.setFlexoBehaviourClass(SynchronizationScheme.class);
				createSynchronizationScheme.doAction();
			}

			if (getDefineCloningScheme()) {
				CreateFlexoBehaviour createCloningScheme = CreateFlexoBehaviour.actionType.makeNewEmbeddedAction(getNewFlexoConcept(), null,
						this);
				createCloningScheme.setFlexoBehaviourName("clone");
				createCloningScheme.setFlexoBehaviourClass(CloningScheme.class);
				createCloningScheme.doAction();
			}
		}
	}

	protected void performCreateInspectors() {
		/*if (getDefineSomeBehaviours()) {
			if (getDefineDefaultCreationScheme()) {
				CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewEmbeddedAction(getNewFlexoConcept(),
						null, this);
				createCreationScheme.setFlexoBehaviourName("create");
				createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
				for (PropertyEntry entry : propertiesUsedForCreationScheme) {
					BehaviourParameterEntry newEntry = createCreationScheme.newParameterEntry();
					newEntry.setParameterName(entry.getName());
					newEntry.setParameterType(entry.getType());
					newEntry.setContainer(entry.getContainer());
					newEntry.setDefaultValue(entry.getDefaultValue());
					newEntry.setParameterDescription(entry.getDescription());
					newEntry.setParameterName(entry.getName());
				}
				System.out.println("action valide = " + createCreationScheme.isValid());
				createCreationScheme.doAction();
			}
		}*/
	}

	private void performCreateProperty(PropertyEntry entry) {
		Progress.progress(getLocales().localizedForKey("create_property") + " " + entry.getName());

		AbstractCreateFlexoProperty action = null;

		switch (entry.getPropertyType()) {
			case PRIMITIVE:
				CreatePrimitiveRole createPrimitive = CreatePrimitiveRole.actionType.makeNewEmbeddedAction(getNewFlexoConcept(), null,
						this);
				action = createPrimitive;
				createPrimitive.setRoleName(entry.getName());
				createPrimitive.setCardinality(entry.getCardinality());
				if (TypeUtils.isString(entry.getType())) {
					createPrimitive.setPrimitiveType(PrimitiveType.String);
				}
				if (TypeUtils.isBoolean(entry.getType())) {
					createPrimitive.setPrimitiveType(PrimitiveType.Boolean);
				}
				if (TypeUtils.isInteger(entry.getType()) || TypeUtils.isLong(entry.getType()) || TypeUtils.isShort(entry.getType())
						|| TypeUtils.isByte(entry.getType())) {
					createPrimitive.setPrimitiveType(PrimitiveType.Integer);
				}
				if (TypeUtils.isFloat(entry.getType())) {
					createPrimitive.setPrimitiveType(PrimitiveType.Float);
				}
				if (TypeUtils.isDouble(entry.getType())) {
					createPrimitive.setPrimitiveType(PrimitiveType.Double);
				}
				break;
			case ABSTRACT_PROPERTY:
				CreateAbstractProperty createAbstractProperty = CreateAbstractProperty.actionType
						.makeNewEmbeddedAction(getNewFlexoConcept(), null, this);
				action = createAbstractProperty;
				createAbstractProperty.setPropertyName(entry.getName());
				createAbstractProperty.setPropertyType(entry.getType());
				break;
			case EXPRESSION_PROPERTY:
				CreateExpressionProperty createExpressionProperty = CreateExpressionProperty.actionType
						.makeNewEmbeddedAction(getNewFlexoConcept(), null, this);
				action = createExpressionProperty;
				createExpressionProperty.setPropertyName(entry.getName());
				break;
			case GET_PROPERTY:
				CreateGetSetProperty createGetProperty = CreateGetSetProperty.actionType.makeNewEmbeddedAction(getNewFlexoConcept(), null,
						this);
				action = createGetProperty;
				createGetProperty.setPropertyName(entry.getName());
				break;
			case GET_SET_PROPERTY:
				CreateGetSetProperty createGetSetProperty = CreateGetSetProperty.actionType.makeNewEmbeddedAction(getNewFlexoConcept(),
						null, this);
				action = createGetSetProperty;
				createGetSetProperty.setPropertyName(entry.getName());
				break;
			case FLEXO_CONCEPT_INSTANCE:
				CreateFlexoConceptInstanceRole createFCIRole = CreateFlexoConceptInstanceRole.actionType
						.makeNewEmbeddedAction(getNewFlexoConcept(), null, this);
				action = createFCIRole;
				createFCIRole.setPropertyName(entry.getName());
				if (entry.getType() instanceof FlexoConceptInstanceType) {
					createFCIRole.setFlexoConceptInstanceType(((FlexoConceptInstanceType) entry.getType()).getFlexoConcept());
				}
				createFCIRole.setVirtualModelInstance(new DataBinding<AbstractVirtualModelInstance<?, ?>>(entry.getContainer().toString()));
				break;
			case MODEL_SLOT:
				CreateModelSlot createModelSlot = CreateModelSlot.actionType.makeNewEmbeddedAction(getNewFlexoConcept(), null, this);
				action = createModelSlot;
				createModelSlot.setModelSlotName(entry.getName());
				if (entry.getTechnologyAdapter() != null) {
					createModelSlot.setTechnologyAdapter(entry.getTechnologyAdapter());
					createModelSlot.setModelSlotClass(entry.getModelSlotClass());
					System.out.println("Trouve le ms: c'est " + entry.getModelSlotClass());
				}
				break;
			case TECHNOLOGY_ROLE:
				CreateTechnologyRole createTechnologyRole = CreateTechnologyRole.actionType.makeNewEmbeddedAction(getNewFlexoConcept(),
						null, this);
				action = createTechnologyRole;
				createTechnologyRole.setRoleName(entry.getName());
				if (entry.getTechnologyAdapter() != null && entry.getFlexoRoleClass() != null) {
					System.out.println("Trouve le role: c'est " + entry.getFlexoRoleClass());
					createTechnologyRole.setFlexoRoleClass(entry.getFlexoRoleClass());
				}
				break;
		}

		if (action != null) {
			action.setDescription(entry.getDescription());
			System.out.println("Executing action " + action + " valid=" + action.isValid());
			action.doAction();
		}
		else {
			System.out.println("Create property " + entry.getName() + " not implemented yet");

		}

	}

	private boolean defineSomeBehaviours = true;
	private boolean defineDefaultCreationScheme = true;
	private boolean defineDefaultDeletionScheme = true;
	private boolean defineSynchronizationScheme = false;
	private boolean defineCloningScheme = false;

	public boolean getDefineSomeBehaviours() {
		return defineSomeBehaviours;
	}

	public boolean getDefineDefaultCreationScheme() {
		return defineDefaultCreationScheme;
	}

	public boolean getDefineDefaultDeletionScheme() {
		return defineDefaultDeletionScheme;
	}

	public boolean getDefineSynchronizationScheme() {
		return defineSynchronizationScheme;
	}

	public boolean getDefineCloningScheme() {
		return defineCloningScheme;
	}

	public void setDefineSomeBehaviours(boolean defineSomeBehaviours) {
		if (defineSomeBehaviours != this.defineSomeBehaviours) {
			this.defineSomeBehaviours = defineSomeBehaviours;
			getPropertyChangeSupport().firePropertyChange("defineSomeBehaviours", !defineSomeBehaviours, defineSomeBehaviours);
		}
	}

	public void setDefineDefaultCreationScheme(boolean defineDefaultCreationScheme) {
		if (defineDefaultCreationScheme != this.defineDefaultCreationScheme) {
			this.defineDefaultCreationScheme = defineDefaultCreationScheme;
			getPropertyChangeSupport().firePropertyChange("defineDefaultCreationScheme", !defineDefaultCreationScheme,
					defineDefaultCreationScheme);
		}
	}

	public void setDefineDefaultDeletionScheme(boolean defineDefaultDeletionScheme) {
		if (defineDefaultDeletionScheme != this.defineDefaultDeletionScheme) {
			this.defineDefaultDeletionScheme = defineDefaultDeletionScheme;
			getPropertyChangeSupport().firePropertyChange("defineDefaultDeletionScheme", !defineDefaultDeletionScheme,
					defineDefaultDeletionScheme);
		}
	}

	public void setDefineSynchronizationScheme(boolean defineSynchronizationScheme) {
		if (defineSynchronizationScheme != this.defineSynchronizationScheme) {
			this.defineSynchronizationScheme = defineSynchronizationScheme;
			getPropertyChangeSupport().firePropertyChange("defineSynchronizationScheme", !defineSynchronizationScheme,
					defineSynchronizationScheme);
		}
	}

	public void setDefineCloningScheme(boolean defineCloningScheme) {
		if (defineCloningScheme != this.defineCloningScheme) {
			this.defineCloningScheme = defineCloningScheme;
			getPropertyChangeSupport().firePropertyChange("defineCloningScheme", !defineCloningScheme, defineCloningScheme);
		}
	}

	public static class ParentFlexoConceptEntry extends PropertyChangedSupportDefaultImplementation {

		private FlexoConcept parentConcept;

		public ParentFlexoConceptEntry() {
			super();
		}

		public ParentFlexoConceptEntry(FlexoConcept parentConcept) {
			super();
			this.parentConcept = parentConcept;
		}

		public void delete() {
			parentConcept = null;
		}

		public FlexoConcept getParentConcept() {
			return parentConcept;
		}

		public void setParentConcept(FlexoConcept parentConcept) {
			if ((parentConcept == null && this.parentConcept != null)
					|| (parentConcept != null && !parentConcept.equals(this.parentConcept))) {
				FlexoConcept oldValue = this.parentConcept;
				this.parentConcept = parentConcept;
				getPropertyChangeSupport().firePropertyChange("parentConcept", oldValue, parentConcept);
			}
		}
	}

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

	public class PropertyEntry extends PropertyChangedSupportDefaultImplementation implements Bindable {

		private String name;
		private Type type;
		private PropertyCardinality cardinality = PropertyCardinality.ZeroOne;

		private boolean required = true;
		private String description;
		private PropertyType propertyType;

		private DataBinding<?> defaultValue;
		private DataBinding<?> container;

		private LocalizedDelegate locales;

		public PropertyEntry(String paramName, LocalizedDelegate locales) {
			super();
			this.name = paramName;
			this.locales = locales;
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
			}
		}

		public List<PropertyType> getAvailablePropertyTypes() {
			return AbstractCreateFlexoConcept.getAvailablePropertyTypes(getType());
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

		public int getIndex() {
			if (getPropertiesEntries() != null) {
				return getPropertiesEntries().indexOf(this);
			}
			return -1;
		}

		public DataBinding<?> getDefaultValue() {
			if (defaultValue == null) {
				defaultValue = new DataBinding<Object>(this, getType(), BindingDefinitionType.GET);
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
				container = new DataBinding<Object>(this, Object.class, BindingDefinitionType.GET);
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
		 * Return technology adapter infered from type, when possible
		 * 
		 * @return
		 */
		public TechnologyAdapter getTechnologyAdapter() {
			Class<?> baseClass = TypeUtils.getBaseClass(getType());
			if (TechnologyObject.class.isAssignableFrom(baseClass)) {
				Class<? extends TechnologyAdapter> taClass = (Class<? extends TechnologyAdapter>) TypeUtils.getTypeArgument(baseClass,
						TechnologyObject.class, 0);
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(taClass);
			}
			return null;
		}

		public Class<? extends ModelSlot<?>> getModelSlotClass() {
			if (getTechnologyAdapter() != null) {
				for (Class<? extends ModelSlot<?>> msClass : getTechnologyAdapter().getAvailableModelSlotTypes()) {
					Type rdType = TypeUtils.getTypeArgument(msClass, ModelSlot.class, 0);
					if (TypeUtils.isTypeAssignableFrom(getType(), rdType)) {
						return msClass;
					}
				}
			}
			return null;
		}

		public Class<? extends FlexoRole<?>> getFlexoRoleClass() {
			if (getTechnologyAdapter() != null) {
				for (Class<? extends ModelSlot<?>> msClass : getTechnologyAdapter().getAvailableModelSlotTypes()) {
					for (Class<? extends FlexoRole<?>> roleClass : ModelSlotImpl.getAvailableFlexoRoleTypes(msClass)) {
						Type rdType = TypeUtils.getTypeArgument(roleClass, FlexoRole.class, 0);
						if (TypeUtils.isTypeAssignableFrom(getType(), rdType)) {
							return roleClass;
						}
					}
				}
			}
			return null;
		}

		@Override
		public BindingModel getBindingModel() {
			if (getFocusedObject() instanceof Bindable) {
				return ((Bindable) getFocusedObject()).getBindingModel();
			}
			return null;
		}

		@Override
		public BindingFactory getBindingFactory() {
			return ((Bindable) getFocusedObject()).getBindingFactory();
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
		}

		@Override
		public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
		}

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
	private static List<PropertyType> ALL_TYPES = Arrays.asList(PropertyType.values());

	public static List<PropertyType> getAvailablePropertyTypes(Type type) {
		Class<?> baseClass = TypeUtils.getBaseClass(type);
		if (TypeUtils.isPrimitive(type) || (type instanceof Class && TypeUtils.isWrapperClass((Class<?>) type))
				|| TypeUtils.isString(type)) {
			return PRIMITIVE_TYPES;
		}
		else if (type instanceof VirtualModelInstanceType) {
			return VMI_TYPES;
		}
		else if (type instanceof FlexoConceptInstanceType) {
			return FCI_TYPES;
		}
		else if (TechnologyObject.class.isAssignableFrom(baseClass)) {
			if (ResourceData.class.isAssignableFrom(baseClass)) {
				return RESOURCE_DATA_TYPES;
			}
			else {
				return ROLE_TYPES;
			}
		}
		return ALL_TYPES;
	}

}
