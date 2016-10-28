/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.FlexoBehaviourParameterImpl;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.WidgetType;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptBehaviouralFacet;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.NavigationScheme;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.toolbox.StringUtils;

public class CreateFlexoBehaviour extends FlexoAction<CreateFlexoBehaviour, FlexoConceptObject, FMLObject> {

	private static final Logger logger = Logger.getLogger(CreateFlexoBehaviour.class.getPackage().getName());

	public static FlexoActionType<CreateFlexoBehaviour, FlexoConceptObject, FMLObject> actionType = new FlexoActionType<CreateFlexoBehaviour, FlexoConceptObject, FMLObject>(
			"flexo_behaviour", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateFlexoBehaviour makeNewAction(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateFlexoBehaviour(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateFlexoBehaviour.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(CreateFlexoBehaviour.actionType, FlexoConceptBehaviouralFacet.class);
	}

	/*public static enum CreateEditionSchemeChoice {
		BuiltInAction, ModelSlotSpecificBehaviour
	}*/

	private String flexoBehaviourName;
	private String description;
	private Class<? extends FlexoBehaviour> flexoBehaviourClass;
	private final HashMap<Class<? extends FlexoBehaviour>, TechnologyAdapter> behaviourClassMap;

	private List<Class<? extends FlexoBehaviour>> behaviours;

	private final List<BehaviourParameterEntry> parameterEntries;

	private FlexoBehaviour newFlexoBehaviour;

	CreateFlexoBehaviour(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);

		behaviourClassMap = new HashMap<Class<? extends FlexoBehaviour>, TechnologyAdapter>();

		if (focusedObject instanceof AbstractVirtualModel<?>) {
			addVirtualModelFlexoBehaviours((AbstractVirtualModel<?>) focusedObject);
		}
		else if (focusedObject instanceof FlexoConcept) {
			addFlexoConceptFlexoBehaviours((FlexoConcept) focusedObject);
		}
		else if (focusedObject instanceof FlexoConceptBehaviouralFacet) {
			FlexoConcept facetConcept = ((FlexoConceptBehaviouralFacet) focusedObject).getFlexoConcept();
			if (facetConcept instanceof AbstractVirtualModel<?>) {
				addVirtualModelFlexoBehaviours((AbstractVirtualModel<?>) facetConcept);
			}
			else {
				addFlexoConceptFlexoBehaviours(facetConcept);
			}
		}

		parameterEntries = new ArrayList<CreateFlexoBehaviour.BehaviourParameterEntry>();
	}

	public List<BehaviourParameterEntry> getParameterEntries() {
		return parameterEntries;
	}

	public BehaviourParameterEntry newParameterEntry() {
		BehaviourParameterEntry returned = new BehaviourParameterEntry("param" + (getParameterEntries().size() + 1), getLocales());
		returned.setParameterType(String.class);
		parameterEntries.add(returned);
		getPropertyChangeSupport().firePropertyChange("parameterEntries", null, returned);
		return returned;
	}

	public void deleteParameterEntry(BehaviourParameterEntry parameterEntryToDelete) {
		parameterEntries.remove(parameterEntryToDelete);
		parameterEntryToDelete.delete();
		getPropertyChangeSupport().firePropertyChange("parameterEntries", parameterEntryToDelete, null);
	}

	public void parameterFirst(BehaviourParameterEntry p) {
		getParameterEntries().remove(p);
		getParameterEntries().add(0, p);
		getPropertyChangeSupport().firePropertyChange("parameterEntries", null, getParameterEntries());
	}

	public void parameterUp(BehaviourParameterEntry p) {
		int index = getParameterEntries().indexOf(p);
		if (index > 0) {
			getParameterEntries().remove(p);
			getParameterEntries().add(index - 1, p);
			getPropertyChangeSupport().firePropertyChange("parameterEntries", null, getParameterEntries());
		}
	}

	public void parameterDown(BehaviourParameterEntry p) {
		int index = getParameterEntries().indexOf(p);
		if (index > -1) {
			getParameterEntries().remove(p);
			getParameterEntries().add(index + 1, p);
			getPropertyChangeSupport().firePropertyChange("parameterEntries", null, getParameterEntries());
		}
	}

	public void parameterLast(BehaviourParameterEntry p) {
		getParameterEntries().remove(p);
		getParameterEntries().add(p);
		getPropertyChangeSupport().firePropertyChange("parameterEntries", null, getParameterEntries());
	}

	private void addVirtualModelFlexoBehaviours(AbstractVirtualModel<?> virtualModel) {
		behaviourClassMap.put(ActionScheme.class, virtualModel.getTechnologyAdapter());
		behaviourClassMap.put(CloningScheme.class, virtualModel.getTechnologyAdapter());
		behaviourClassMap.put(CreationScheme.class, virtualModel.getTechnologyAdapter());
		behaviourClassMap.put(DeletionScheme.class, virtualModel.getTechnologyAdapter());
		behaviourClassMap.put(SynchronizationScheme.class, virtualModel.getTechnologyAdapter());
		for (ModelSlot<?> ms : virtualModel.getModelSlots()) {
			List<Class<? extends FlexoBehaviour>> msBehaviours = ms.getAvailableFlexoBehaviourTypes();
			for (Class<? extends FlexoBehaviour> behaviour : msBehaviours) {
				if (!behaviourClassMap.containsKey(behaviour)) {
					behaviourClassMap.put(behaviour, ms.getModelSlotTechnologyAdapter());
				}
			}
		}
	}

	private void addFlexoConceptFlexoBehaviours(FlexoConcept flexoConcept) {
		if (flexoConcept.getOwningVirtualModel() != null) {
			behaviourClassMap.put(ActionScheme.class, flexoConcept.getOwningVirtualModel().getTechnologyAdapter());
			behaviourClassMap.put(CloningScheme.class, flexoConcept.getOwningVirtualModel().getTechnologyAdapter());
			behaviourClassMap.put(CreationScheme.class, flexoConcept.getOwningVirtualModel().getTechnologyAdapter());
			behaviourClassMap.put(DeletionScheme.class, flexoConcept.getOwningVirtualModel().getTechnologyAdapter());
			for (ModelSlot<?> ms : flexoConcept.getOwningVirtualModel().getModelSlots()) {
				List<Class<? extends FlexoBehaviour>> msBehaviours = ms.getAvailableFlexoBehaviourTypes();
				for (Class<? extends FlexoBehaviour> behaviour : msBehaviours) {
					if (!behaviourClassMap.containsKey(behaviour)) {
						behaviourClassMap.put(behaviour, ms.getModelSlotTechnologyAdapter());
					}
				}
			}
		}
	}

	public TechnologyAdapter getBehaviourTechnologyAdapter(Class<? extends FlexoBehaviour> behaviourClass) {
		return behaviourClassMap.get(behaviourClass);
	}

	public List<Class<? extends FlexoBehaviour>> getBehaviours() {
		if (behaviours == null) {
			behaviours = new ArrayList<Class<? extends FlexoBehaviour>>();
			for (Class<? extends FlexoBehaviour> mapKey : behaviourClassMap.keySet()) {
				behaviours.add(mapKey);
			}
		}
		return behaviours;
	}

	/*public List<Class<? extends FlexoBehaviour>> getModelSlotSpecificBehaviours() {
		if (modelSlot != null && !(modelSlot instanceof FMLRTModelSlot)) {
			return modelSlot.getAvailableFlexoBehaviourTypes();
		}
		return null;
	}*/

	public FlexoConcept getFlexoConcept() {
		if (getFocusedObject() != null) {
			return getFocusedObject().getFlexoConcept();
		}
		return null;
	}

	private String defaultFlexoBehaviourBaseName() {
		if (flexoBehaviourClass != null) {
			if (CreationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
				return "create";
			}
			else if (DeletionScheme.class.isAssignableFrom(flexoBehaviourClass)) {
				return "delete";
			}
			else if (ActionScheme.class.isAssignableFrom(flexoBehaviourClass)) {
				return "action";
			}
			else if (CloningScheme.class.isAssignableFrom(flexoBehaviourClass)) {
				return "clone";
			}
			else if (NavigationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
				return "navigate";
			}
			String baseName = flexoBehaviourClass.getSimpleName();
			return baseName.substring(0, 1).toLowerCase() + baseName.substring(1);
		}
		return null;
	}

	public String getFlexoBehaviourName() {
		if (StringUtils.isEmpty(flexoBehaviourName) && flexoBehaviourClass != null) {
			return getFlexoConcept().getAvailableFlexoBehaviourName(defaultFlexoBehaviourBaseName());
		}
		return flexoBehaviourName;
	}

	public void setFlexoBehaviourName(String flexoBehaviourName) {
		this.flexoBehaviourName = flexoBehaviourName;
	}

	private void performCreateParameters() {
		for (BehaviourParameterEntry entry : getParameterEntries()) {
			performCreateParameter(entry);
		}
	}

	private void performCreateParameter(BehaviourParameterEntry entry) {
		Progress.progress(getLocales().localizedForKey("create_parameter") + " " + entry.getParameterName());
		CreateGenericBehaviourParameter action = CreateGenericBehaviourParameter.actionType.makeNewEmbeddedAction(getNewFlexoBehaviour(),
				null, this);
		action.setParameterName(entry.getParameterName());
		action.setParameterType(entry.getParameterType());
		action.setWidgetType(entry.getWidgetType());
		action.setContainer(entry.getContainer());
		action.setDefaultValue(entry.getDefaultValue());
		action.setList(entry.getList());
		action.setIsRequired(entry.isRequired());
		action.setDescription(entry.getParameterDescription());
		action.doAction();

	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		logger.info("Add flexo behaviour, name=" + getFlexoBehaviourName() + " type=" + flexoBehaviourClass);

		if (flexoBehaviourClass != null) {

			FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
			newFlexoBehaviour = factory.newInstance(flexoBehaviourClass);
			newFlexoBehaviour.setName(getFlexoBehaviourName());
			newFlexoBehaviour.setDescription(getDescription());
			newFlexoBehaviour.setFlexoConcept(getFlexoConcept());
			performCreateParameters();
			newFlexoBehaviour.setControlGraph(factory.newEmptyControlGraph());
			getFlexoConcept().addToFlexoBehaviours(newFlexoBehaviour);
		}
		else {
			throw new InvalidParameterException("flexoBehaviourClass is null");
		}

	}

	public FlexoBehaviour getNewFlexoBehaviour() {
		return newFlexoBehaviour;
	}

	@Override
	public boolean isValid() {
		if (getFlexoBehaviourName() == null) {
			return false;
		}
		else if (getFlexoConcept().getFlexoBehaviour(getFlexoBehaviourName()) != null) {
			return false;
		}
		else if (flexoBehaviourClass == null) {
			return false;
		}
		else {
			return true;
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		boolean wasValid = isValid();
		this.description = description;
		getPropertyChangeSupport().firePropertyChange("description", null, description);
		getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
	}

	public Class<? extends FlexoBehaviour> getFlexoBehaviourClass() {
		return flexoBehaviourClass;
	}

	public void setFlexoBehaviourClass(Class<? extends FlexoBehaviour> flexoBehaviourClass) {
		boolean wasValid = isValid();
		this.flexoBehaviourClass = flexoBehaviourClass;
		getPropertyChangeSupport().firePropertyChange("flexoBehaviourClass", null, flexoBehaviourClass);
		getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
	}

	public class BehaviourParameterEntry extends PropertyChangedSupportDefaultImplementation implements Bindable {

		private String parameterName;
		private Type parameterType;
		private boolean required = true;
		private String description;
		private WidgetType widgetType;

		private DataBinding<?> defaultValue;
		private DataBinding<?> container;
		private DataBinding<List<?>> list;

		private LocalizedDelegate locales;

		public BehaviourParameterEntry(String paramName, LocalizedDelegate locales) {
			super();
			this.parameterName = paramName;
			this.locales = locales;
		}

		public void delete() {
			parameterName = null;
			description = null;
			parameterType = null;
		}

		public LocalizedDelegate getLocales() {
			return locales;
		}

		public String getParameterName() {
			if (parameterName == null) {
				return "param";
			}
			return parameterName;
		}

		public void setParameterName(String parameterName) {
			this.parameterName = parameterName;
			getPropertyChangeSupport().firePropertyChange("parameterName", null, parameterName);
		}

		public Type getParameterType() {
			return parameterType;
		}

		public void setParameterType(Type parameterType) {
			if ((parameterType == null && this.parameterType != null)
					|| (parameterType != null && !parameterType.equals(this.parameterType))) {
				Type oldValue = this.parameterType;
				this.parameterType = parameterType;
				getPropertyChangeSupport().firePropertyChange("parameterType", oldValue, parameterType);
				getPropertyChangeSupport().firePropertyChange("availableWidgetTypes", null, getAvailableWidgetTypes());
				listType = null;
				if (list != null) {
					list.setDeclaredType(getListType());
				}
				if (defaultValue != null) {
					defaultValue.setDeclaredType(getParameterType());
				}
				getPropertyChangeSupport().firePropertyChange("isListType", !isListType(), isListType());
				if (!getAvailableWidgetTypes().contains(getWidgetType()) && getAvailableWidgetTypes().size() > 0) {
					setWidgetType(getAvailableWidgetTypes().get(0));
				}
			}
		}

		public WidgetType getWidgetType() {
			return widgetType;
		}

		public void setWidgetType(WidgetType widgetType) {
			if ((widgetType == null && this.widgetType != null) || (widgetType != null && !widgetType.equals(this.widgetType))) {
				WidgetType oldValue = this.widgetType;
				this.widgetType = widgetType;
				getPropertyChangeSupport().firePropertyChange("widgetType", oldValue, widgetType);
			}
		}

		public List<WidgetType> getAvailableWidgetTypes() {
			return FlexoBehaviourParameterImpl.getAvailableWidgetTypes(getParameterType());
		}

		public String getParameterDescription() {
			return description;
		}

		public void setParameterDescription(String description) {
			this.description = description;
			getPropertyChangeSupport().firePropertyChange("parameterDescription", null, description);
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
			getPropertyChangeSupport().firePropertyChange("required", null, required);
		}

		public String getConfigurationErrorMessage() {

			if (StringUtils.isEmpty(getParameterName())) {
				return getLocales().localizedForKey("please_supply_valid_parameter_name");
			}
			if (getParameterType() == null) {
				return getLocales().localizedForKey("no_parameter_type_defined_for") + " " + getParameterName();
			}
			if (getWidgetType() == null) {
				return getLocales().localizedForKey("no_widget_type_defined_for") + " " + getParameterName();
			}

			return null;
		}

		public String getConfigurationWarningMessage() {
			if (StringUtils.isEmpty(getParameterDescription())) {
				return getLocales().localizedForKey("it_is_recommanded_to_describe_parameter") + " " + getParameterName();
			}
			return null;

		}

		public int getIndex() {
			if (getParameterEntries() != null) {
				return getParameterEntries().indexOf(this);
			}
			return -1;
		}

		public DataBinding<?> getDefaultValue() {
			if (defaultValue == null) {
				defaultValue = new DataBinding<Object>(this, getParameterType(), BindingDefinitionType.GET);
				defaultValue.setBindingName("defaultValue");
			}
			return defaultValue;
		}

		public void setDefaultValue(DataBinding<?> defaultValue) {
			if (defaultValue != null) {
				defaultValue.setOwner(this);
				defaultValue.setBindingName("defaultValue");
				defaultValue.setDeclaredType(getParameterType());
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

		public DataBinding<List<?>> getList() {
			if (list == null) {
				list = new DataBinding<List<?>>(this, getListType(), BindingDefinitionType.GET);
			}
			return list;
		}

		public void setList(DataBinding<List<?>> list) {
			if (list != null) {
				list.setOwner(this);
				list.setBindingName("list");
				list.setDeclaredType(getListType());
				list.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.list = list;
		}

		private ParameterizedTypeImpl listType = null;

		private Type getListType() {
			if (listType == null) {
				listType = new ParameterizedTypeImpl(List.class, getParameterType());
			}
			return listType;
		}

		public boolean isListType() {
			return TypeUtils.isList(getParameterType());
		}

		@Override
		public BindingModel getBindingModel() {
			return getFocusedObject().getBindingModel();
		}

		@Override
		public BindingFactory getBindingFactory() {
			return getFocusedObject().getBindingFactory();
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
		}

		@Override
		public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
		}

	}

}
