package org.openflexo.vpm.controller.action;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Icon;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.annotations.FIBPanel;
import org.openflexo.icon.VPMIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public abstract class AbstractCreateVirtualModelWizard<A extends FlexoAction<?, ?, ?>> extends AbstractCreateFlexoConceptWizard<A> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCreateVirtualModelWizard.class.getPackage().getName());

	private ConfigureModelSlots configureModelSlots;

	public AbstractCreateVirtualModelWizard(A action, FlexoController controller) {
		super(action, controller);
	}

	protected void appendConfigureModelSlots() {
		addStep(configureModelSlots = new ConfigureModelSlots());
	}

	public ConfigureModelSlots getConfigureModelSlots() {
		return configureModelSlots;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFlexoConcept/ConfigureModelSlots.fib")
	public class ConfigureModelSlots extends WizardStep {

		private final List<AbstractCreateVirtualModelWizard.ConfigureModelSlots.ModelSlotEntry> modelSlotEntries;

		public ConfigureModelSlots() {
			modelSlotEntries = new ArrayList<AbstractCreateVirtualModelWizard.ConfigureModelSlots.ModelSlotEntry>();
		}

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public A getAction() {
			return AbstractCreateVirtualModelWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("configure_model_slots");
		}

		// Required full qualified class name, otherwise JVM throw a ParseException while introspecting
		public List<AbstractCreateVirtualModelWizard.ConfigureModelSlots.ModelSlotEntry> getModelSlotEntries() {
			return modelSlotEntries;
		}

		// Required full qualified class name, otherwise JVM throw a ParseException while introspecting
		public AbstractCreateVirtualModelWizard.ConfigureModelSlots.ModelSlotEntry newModelSlotEntry() {
			ModelSlotEntry returned = new ModelSlotEntry();
			modelSlotEntries.add(returned);
			getPropertyChangeSupport().firePropertyChange("modelSlotEntries", null, returned);
			checkValidity();
			return returned;
		}

		// Required full qualified class name, otherwise JVM throw a ParseException while introspecting
		public void deleteModelSlotEntry(AbstractCreateVirtualModelWizard.ConfigureModelSlots.ModelSlotEntry modelSlotEntryToDelete) {
			modelSlotEntries.remove(modelSlotEntryToDelete);
			modelSlotEntryToDelete.delete();
			getPropertyChangeSupport().firePropertyChange("modelSlotEntries", modelSlotEntryToDelete, null);
			checkValidity();
			// return modelSlotEntryToDelete;
		}

		@Override
		public boolean isValid() {

			if (getModelSlotEntries().size() == 0) {
				setIssueMessage(FlexoLocalization.localizedForKey("no_model_slots_defined"), IssueMessageType.WARNING);
				return true;
			} else {

				// We try to detect duplicated names
				for (ModelSlotEntry entry : getModelSlotEntries()) {
					String modelSlotName = entry.getModelSlotName();
					for (ModelSlotEntry entry2 : getModelSlotEntries()) {
						if ((entry != entry2) && (entry.getModelSlotName().equals(entry2.getModelSlotName()))) {
							setIssueMessage(FlexoLocalization.localizedForKey("duplicated_model_slot_name") + " : " + modelSlotName,
									IssueMessageType.ERROR);
							return false;
						}
					}
				}

				// Then, we valid each ModelSLotEntry
				boolean hasWarnings = false;
				for (ModelSlotEntry entry : getModelSlotEntries()) {
					if (!entry.isValid()) {
						return false;
					}
					if (entry.hasWarnings()) {
						hasWarnings = true;
					}
				}
				if (!hasWarnings) {
					setIssueMessage(FlexoLocalization.localizedForKey("all_model_slots_are_valid"), IssueMessageType.INFO);
				}
				return true;
			}

		}

		public class ModelSlotEntry extends PropertyChangedSupportDefaultImplementation {

			private final String defaultModelSlotName;
			private String modelSlotName;
			private String description;
			private TechnologyAdapter technologyAdapter;
			private boolean required = true;
			private boolean readOnly = false;
			private Class<? extends ModelSlot<?>> modelSlotClass;

			public ModelSlotEntry() {
				super();
				defaultModelSlotName = "modelSlot" + (getModelSlotEntries().size() + 1);
				checkValidity();
			}

			public void delete() {
				modelSlotName = null;
				description = null;
				technologyAdapter = null;
				modelSlotClass = null;
			}

			public Icon getIcon() {
				return VPMIconLibrary.iconForModelSlot(getTechnologyAdapter());
			}

			public Class<? extends ModelSlot<?>> getModelSlotClass() {
				if (modelSlotClass == null && technologyAdapter != null && technologyAdapter.getAvailableModelSlotTypes().size() > 0) {
					return technologyAdapter.getAvailableModelSlotTypes().get(0);
				}
				return modelSlotClass;
			}

			public void setModelSlotClass(Class<? extends ModelSlot<?>> modelSlotClass) {
				this.modelSlotClass = modelSlotClass;
				getPropertyChangeSupport().firePropertyChange("modelSlotClass", modelSlotClass != null ? null : false, modelSlotClass);
				checkValidity();
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
				checkValidity();
			}

			public String getModelSlotDescription() {
				return description;
			}

			public void setModelSlotDescription(String description) {
				this.description = description;
				getPropertyChangeSupport().firePropertyChange("modelSlotDescription", null, description);
				checkValidity();
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
				checkValidity();
			}

			public boolean isRequired() {
				return required;
			}

			public void setRequired(boolean required) {
				this.required = required;
				getPropertyChangeSupport().firePropertyChange("required", null, required);
				checkValidity();
			}

			public boolean isReadOnly() {
				return readOnly;
			}

			public void setReadOnly(boolean readOnly) {
				this.readOnly = readOnly;
				getPropertyChangeSupport().firePropertyChange("readOnly", null, readOnly);
				checkValidity();
			}

			public boolean isValid() {

				if (StringUtils.isEmpty(getModelSlotName())) {
					setIssueMessage(FlexoLocalization.localizedForKey("please_supply_valid_model_slot_name"), IssueMessageType.ERROR);
					return false;
				}
				if (getTechnologyAdapter() == null) {
					setIssueMessage(FlexoLocalization.localizedForKey("no_technology_adapter_defined_for") + " " + getModelSlotName(),
							IssueMessageType.ERROR);
					return false;
				}
				if (getModelSlotClass() == null) {
					setIssueMessage(FlexoLocalization.localizedForKey("no_model_slot_type_defined_for") + " " + getModelSlotName(),
							IssueMessageType.ERROR);
					return false;
				}

				return true;
			}

			public boolean hasWarnings() {
				if (StringUtils.isEmpty(getModelSlotDescription())) {
					setIssueMessage(FlexoLocalization.localizedForKey("it_is_recommanded_to_describe_model_slot") + " "
							+ getModelSlotName(), IssueMessageType.WARNING);
					return true;
				}
				return false;

			}

		}
	}
}
