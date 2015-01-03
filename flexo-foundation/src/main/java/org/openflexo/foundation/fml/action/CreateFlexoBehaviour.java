/*
 * (c) Copyright 2010-2011 AgileBirds
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
package org.openflexo.foundation.fml.action;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptBehaviouralFacet;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.NavigationScheme;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelModelFactory;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.toolbox.StringUtils;

// TODO: rename as CreateFlexoBehaviour
public class CreateFlexoBehaviour extends FlexoAction<CreateFlexoBehaviour, FlexoConceptObject, FMLObject> {

	private static final Logger logger = Logger.getLogger(CreateFlexoBehaviour.class.getPackage().getName());

	public static FlexoActionType<CreateFlexoBehaviour, FlexoConceptObject, FMLObject> actionType = new FlexoActionType<CreateFlexoBehaviour, FlexoConceptObject, FMLObject>(
			"create_flexo_behaviour", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

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

		if (focusedObject instanceof VirtualModel) {
			addVirtualModelFlexoBehaviours((VirtualModel) focusedObject);
		} else if (focusedObject instanceof FlexoConcept) {
			addFlexoConceptFlexoBehaviours((FlexoConcept) focusedObject);
		}

		parameterEntries = new ArrayList<CreateFlexoBehaviour.BehaviourParameterEntry>();
	}

	public List<BehaviourParameterEntry> getParameterEntries() {
		return parameterEntries;
	}

	public BehaviourParameterEntry newParameterEntry() {
		BehaviourParameterEntry returned = new BehaviourParameterEntry("param" + (getParameterEntries().size() + 1));
		parameterEntries.add(returned);
		getPropertyChangeSupport().firePropertyChange("parameterEntries", null, returned);
		return returned;
	}

	public void deleteParameterEntry(BehaviourParameterEntry parameterEntryToDelete) {
		parameterEntries.remove(parameterEntryToDelete);
		parameterEntryToDelete.delete();
		getPropertyChangeSupport().firePropertyChange("parameterEntries", parameterEntryToDelete, null);
	}

	private void addVirtualModelFlexoBehaviours(VirtualModel virtualModel) {
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
		behaviourClassMap.put(ActionScheme.class, flexoConcept.getVirtualModel().getTechnologyAdapter());
		behaviourClassMap.put(CloningScheme.class, flexoConcept.getVirtualModel().getTechnologyAdapter());
		behaviourClassMap.put(CreationScheme.class, flexoConcept.getVirtualModel().getTechnologyAdapter());
		behaviourClassMap.put(DeletionScheme.class, flexoConcept.getVirtualModel().getTechnologyAdapter());
		for (ModelSlot<?> ms : flexoConcept.getVirtualModel().getModelSlots()) {
			List<Class<? extends FlexoBehaviour>> msBehaviours = ms.getAvailableFlexoBehaviourTypes();
			for (Class<? extends FlexoBehaviour> behaviour : msBehaviours) {
				if (!behaviourClassMap.containsKey(behaviour)) {
					behaviourClassMap.put(behaviour, ms.getModelSlotTechnologyAdapter());
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
			} else if (DeletionScheme.class.isAssignableFrom(flexoBehaviourClass)) {
				return "delete";
			} else if (ActionScheme.class.isAssignableFrom(flexoBehaviourClass)) {
				return "action";
			} else if (CloningScheme.class.isAssignableFrom(flexoBehaviourClass)) {
				return "clone";
			} else if (NavigationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
				return "navigate";
			}
			String baseName = flexoBehaviourClass.getSimpleName();
			return baseName.substring(0, 1).toLowerCase() + baseName.substring(1);
		}
		return null;
	}

	public String getFlexoBehaviourName() {
		if (StringUtils.isEmpty(flexoBehaviourName) && flexoBehaviourClass != null) {
			return getFlexoConcept().getAvailableEditionSchemeName(defaultFlexoBehaviourBaseName());
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
		Progress.progress(FlexoLocalization.localizedForKey("create_parameter") + " " + entry.getParameterName());
		CreateFlexoBehaviourParameter action = CreateFlexoBehaviourParameter.actionType.makeNewEmbeddedAction(getNewFlexoBehaviour(), null,
				this);
		action.setParameterName(entry.getParameterName());
		action.setFlexoBehaviourParameterClass(entry.getParameterClass());
		action.setDescription(entry.getParameterDescription());
		action.doAction();
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		logger.info("Add flexo behaviour, name=" + getFlexoBehaviourName() + " type=" + flexoBehaviourClass);

		if (flexoBehaviourClass != null) {

			VirtualModelModelFactory factory = getFocusedObject().getVirtualModelFactory();
			newFlexoBehaviour = factory.newInstance(flexoBehaviourClass);
			newFlexoBehaviour.setName(getFlexoBehaviourName());
			newFlexoBehaviour.setFlexoConcept(getFlexoConcept());
			performCreateParameters();
			newFlexoBehaviour.setControlGraph(factory.newEmptyControlGraph());
			getFlexoConcept().addToFlexoBehaviours(newFlexoBehaviour);
		} else {
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
		} else if (getFlexoConcept().getFlexoBehaviour(getFlexoBehaviourName()) != null) {
			return false;
		} else if (flexoBehaviourClass == null) {
			return false;
		} else {
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

	public static class BehaviourParameterEntry extends PropertyChangedSupportDefaultImplementation {

		private String parameterName;
		private Class<? extends FlexoBehaviourParameter> parameterClass;
		private boolean required = true;
		private String description;

		public BehaviourParameterEntry(String paramName) {
			super();
			this.parameterName = paramName;
		}

		public void delete() {
			parameterName = null;
			description = null;
			parameterClass = null;
		}

		public Class<? extends FlexoBehaviourParameter> getParameterClass() {
			return parameterClass;
		}

		public void setParameterClass(Class<? extends FlexoBehaviourParameter> parameterClass) {
			this.parameterClass = parameterClass;
			getPropertyChangeSupport().firePropertyChange("parameterClass", parameterClass != null ? null : false, parameterClass);
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
				return FlexoLocalization.localizedForKey("please_supply_valid_parameter_name");
			}
			if (getParameterClass() == null) {
				return FlexoLocalization.localizedForKey("no_parameter_type_defined_for") + " " + getParameterName();
			}

			return null;
		}

		public String getConfigurationWarningMessage() {
			if (StringUtils.isEmpty(getParameterDescription())) {
				return FlexoLocalization.localizedForKey("it_is_recommanded_to_describe_parameter") + " " + getParameterName();
			}
			return null;

		}

	}

}