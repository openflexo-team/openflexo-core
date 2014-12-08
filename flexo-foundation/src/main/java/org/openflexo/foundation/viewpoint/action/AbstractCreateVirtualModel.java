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
package org.openflexo.foundation.viewpoint.action;

// org.openflexo.foundation.viewpoint.action.AbstractCreateVirtualModel$ModelSlotEntry

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.ViewPointObject;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.rm.VirtualModelResource;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.toolbox.StringUtils;

/**
 * Abstract action creating a {@link FlexoConcept} or any of its subclass
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractCreateVirtualModel<A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends ViewPointObject>
		extends AbstractCreateFlexoConcept<A, T1, T2> {

	private static final Logger logger = Logger.getLogger(AbstractCreateVirtualModel.class.getPackage().getName());

	private final List<ModelSlotEntry> modelSlotEntries;

	AbstractCreateVirtualModel(FlexoActionType<A, T1, T2> actionType, T1 focusedObject, Vector<T2> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		modelSlotEntries = new ArrayList<AbstractCreateVirtualModel.ModelSlotEntry>();
	}

	public List<ModelSlotEntry> getModelSlotEntries() {
		return modelSlotEntries;
	}

	public ModelSlotEntry newModelSlotEntry() {
		ModelSlotEntry returned = new ModelSlotEntry("modelSlot" + (getModelSlotEntries().size() + 1));
		modelSlotEntries.add(returned);
		getPropertyChangeSupport().firePropertyChange("modelSlotEntries", null, returned);
		return returned;
	}

	public void deleteModelSlotEntry(ModelSlotEntry modelSlotEntryToDelete) {
		modelSlotEntries.remove(modelSlotEntryToDelete);
		modelSlotEntryToDelete.delete();
		getPropertyChangeSupport().firePropertyChange("modelSlotEntries", modelSlotEntryToDelete, null);
	}

	public abstract VirtualModel getNewVirtualModel();

	@Override
	public FlexoConcept getNewFlexoConcept() {
		return getNewVirtualModel();
	}

	protected void performCreateModelSlots() {
		for (ModelSlotEntry entry : getModelSlotEntries()) {
			performCreateModelSlot(entry);
		}
	}

	protected void performCreateModelSlot(ModelSlotEntry entry) {
		Progress.progress(FlexoLocalization.localizedForKey("create_model_slot") + " " + entry.getModelSlotName());
		CreateModelSlot action = CreateModelSlot.actionType.makeNewEmbeddedAction(getNewVirtualModel(), null, this);
		action.setModelSlotName(entry.getModelSlotName());
		action.setDescription(entry.getModelSlotDescription());
		action.setTechnologyAdapter(entry.getTechnologyAdapter());
		action.setModelSlotClass(entry.getModelSlotClass());
		action.setMmRes(entry.getMetaModelResource());
		action.setVmRes(entry.getVirtualModelResource());
		action.doAction();
	}

	public static class ModelSlotEntry extends PropertyChangedSupportDefaultImplementation {

		private final String defaultModelSlotName;
		private String modelSlotName;
		private String description;
		private TechnologyAdapter technologyAdapter;
		private boolean required = true;
		private boolean readOnly = false;
		private Class<? extends ModelSlot<?>> modelSlotClass;

		private VirtualModelResource virtualModelResource;
		private FlexoMetaModelResource<?, ?, ?> metaModelResource;

		public ModelSlotEntry(String defaultName) {
			super();
			defaultModelSlotName = defaultName;
		}

		public void delete() {
			modelSlotName = null;
			description = null;
			technologyAdapter = null;
			modelSlotClass = null;
		}

		/*public Icon getIcon() {
			return VPMIconLibrary.iconForModelSlot(getTechnologyAdapter());
		}*/

		public Class<? extends ModelSlot<?>> getModelSlotClass() {
			if (modelSlotClass == null && technologyAdapter != null && technologyAdapter.getAvailableModelSlotTypes().size() > 0) {
				return technologyAdapter.getAvailableModelSlotTypes().get(0);
			}
			return modelSlotClass;
		}

		public void setModelSlotClass(Class<? extends ModelSlot<?>> modelSlotClass) {
			this.modelSlotClass = modelSlotClass;
			getPropertyChangeSupport().firePropertyChange("modelSlotClass", modelSlotClass != null ? null : false, modelSlotClass);
		}

		public String getModelSlotName() {
			if (modelSlotName == null) {
				return defaultModelSlotName;
			}
			return modelSlotName;
		}

		public void setModelSlotName(String modelSlotName) {
			this.modelSlotName = modelSlotName;
			getPropertyChangeSupport().firePropertyChange("modelSlotName", null, modelSlotName);
		}

		public String getModelSlotDescription() {
			return description;
		}

		public void setModelSlotDescription(String description) {
			this.description = description;
			getPropertyChangeSupport().firePropertyChange("modelSlotDescription", null, description);
		}

		public TechnologyAdapter getTechnologyAdapter() {
			return technologyAdapter;
		}

		public void setTechnologyAdapter(TechnologyAdapter technologyAdapter) {
			this.technologyAdapter = technologyAdapter;
			getPropertyChangeSupport().firePropertyChange("technologyAdapter", null, technologyAdapter);
			if (getModelSlotClass() != null && !technologyAdapter.getAvailableModelSlotTypes().contains(getModelSlotClass())) {
				// The ModelSlot class is not consistent anymore
				if (technologyAdapter.getAvailableModelSlotTypes().size() > 0) {
					setModelSlotClass(technologyAdapter.getAvailableModelSlotTypes().get(0));
				} else {
					setModelSlotClass(null);
				}
			}
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
			getPropertyChangeSupport().firePropertyChange("required", null, required);
		}

		public boolean isReadOnly() {
			return readOnly;
		}

		public void setReadOnly(boolean readOnly) {
			this.readOnly = readOnly;
			getPropertyChangeSupport().firePropertyChange("readOnly", null, readOnly);
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
			}
		}

		public String getConfigurationErrorMessage() {

			if (StringUtils.isEmpty(getModelSlotName())) {
				return FlexoLocalization.localizedForKey("please_supply_valid_model_slot_name");
			}
			if (getTechnologyAdapter() == null) {
				return FlexoLocalization.localizedForKey("no_technology_adapter_defined_for") + " " + getModelSlotName();
			}
			if (getModelSlotClass() == null) {
				return FlexoLocalization.localizedForKey("no_model_slot_type_defined_for") + " " + getModelSlotName();
			}

			return null;
		}

		public String getConfigurationWarningMessage() {
			if (StringUtils.isEmpty(getModelSlotDescription())) {
				return FlexoLocalization.localizedForKey("it_is_recommanded_to_describe_model_slot") + " " + getModelSlotName();
			}
			return null;

		}

	}

}
