package org.openflexo.vpm.controller.action;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.FreeModelSlot;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.VirtualModelModelSlot;
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
	private final List<ConfigureModelSlot<?>> modelSlotConfigurationSteps;

	public AbstractCreateVirtualModelWizard(A action, FlexoController controller) {
		super(action, controller);
		modelSlotConfigurationSteps = new ArrayList<AbstractCreateVirtualModelWizard<A>.ConfigureModelSlot<?>>();
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

		private final List<AbstractCreateVirtualModelWizard<A>.ConfigureModelSlots.ModelSlotEntry> modelSlotEntries;

		public ConfigureModelSlots() {
			modelSlotEntries = new ArrayList<AbstractCreateVirtualModelWizard<A>.ConfigureModelSlots.ModelSlotEntry>();
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
		public List<AbstractCreateVirtualModelWizard<A>.ConfigureModelSlots.ModelSlotEntry> getModelSlotEntries() {
			return modelSlotEntries;
		}

		// Required full qualified class name, otherwise JVM throw a ParseException while introspecting
		public AbstractCreateVirtualModelWizard<A>.ConfigureModelSlots.ModelSlotEntry newModelSlotEntry() {
			ModelSlotEntry returned = new ModelSlotEntry();
			modelSlotEntries.add(returned);
			getPropertyChangeSupport().firePropertyChange("modelSlotEntries", null, returned);
			checkValidity();
			return returned;
		}

		// Required full qualified class name, otherwise JVM throw a ParseException while introspecting
		public void deleteModelSlotEntry(AbstractCreateVirtualModelWizard<A>.ConfigureModelSlots.ModelSlotEntry modelSlotEntryToDelete) {
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
					if (!entry.isValidIgnoreConfiguration()) {
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

		@Override
		public boolean isTransitionalStep() {
			return true;
		}

		private ConfigureModelSlot<?> makeConfigureModelSlotStep(ModelSlotEntry msEntry) {
			if (TypeAwareModelSlot.class.isAssignableFrom(msEntry.getModelSlotClass())) {
				return new ConfigureTypeAwareModelSlot(msEntry);
			} else if (FreeModelSlot.class.isAssignableFrom(msEntry.getModelSlotClass())) {
				return new ConfigureFreeModelSlot(msEntry);
			} else if (VirtualModelModelSlot.class.isAssignableFrom(msEntry.getModelSlotClass())) {
				return new ConfigureVirtualModelModelSlot(msEntry);
			} else {
				logger.warning("Could not instantiate ConfigureModelSlot for " + msEntry);
				return null;
			}
		}

		@Override
		public void performTransition() {
			// We have now to update all steps according to chosen model slots
			for (ModelSlotEntry msEntry : getConfigureModelSlots().getModelSlotEntries()) {
				ConfigureModelSlot<?> step = makeConfigureModelSlotStep(msEntry);
				if (step != null) {
					modelSlotConfigurationSteps.add(step);
					addStep(step);
				}
			}
		}

		@Override
		public void discardTransition() {
			for (ConfigureModelSlot<?> step : modelSlotConfigurationSteps) {
				removeStep(step);
			}
			modelSlotConfigurationSteps.clear();
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

			public boolean isValidIgnoreConfiguration() {

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

	/**
	 * This abstract generic step is used to configure a model slot
	 * 
	 * @author sylvain
	 *
	 */
	public abstract class ConfigureModelSlot<MS extends ModelSlot<?>> extends WizardStep {

		private final AbstractCreateVirtualModelWizard<A>.ConfigureModelSlots.ModelSlotEntry modelSlotEntry;

		public ConfigureModelSlot(AbstractCreateVirtualModelWizard<A>.ConfigureModelSlots.ModelSlotEntry modelSlotEntry) {
			this.modelSlotEntry = modelSlotEntry;
		}

		public AbstractCreateVirtualModelWizard<A>.ConfigureModelSlots.ModelSlotEntry getModelSlotEntry() {
			return modelSlotEntry;
		}

		public A getAction() {
			return AbstractCreateVirtualModelWizard.this.getAction();
		}

		@Override
		public boolean isValid() {
			boolean isValid = modelSlotEntry.isValidIgnoreConfiguration();
			if (isValid) {
				setIssueMessage(FlexoLocalization.localizedForKey("valid_model_slot_configuration"), IssueMessageType.INFO);
			}
			return isValid;
		}

		public ImageIcon getTechnologyIcon() {
			return getController().getTechnologyAdapterController(modelSlotEntry.getTechnologyAdapter()).getTechnologyBigIcon();
		}
	}

	/**
	 * This step is used to configure a type-aware model slot
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFlexoConcept/ConfigureTypeAwareModelSlot.fib")
	public class ConfigureTypeAwareModelSlot<M extends FlexoModel<M, MM> & TechnologyObject<?>, MM extends FlexoMetaModel<MM> & TechnologyObject<?>>
			extends ConfigureModelSlot<TypeAwareModelSlot<M, MM>> {

		public ConfigureTypeAwareModelSlot(AbstractCreateVirtualModelWizard<A>.ConfigureModelSlots.ModelSlotEntry entry) {
			super(entry);
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("configure_type_aware_model_slot") + " : " + getModelSlotEntry().getModelSlotName();
		}

	}

	/**
	 * This step is used to configure a type-aware model slot
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFlexoConcept/ConfigureFreeModelSlot.fib")
	public class ConfigureFreeModelSlot extends ConfigureModelSlot<FreeModelSlot<?>> {

		public ConfigureFreeModelSlot(AbstractCreateVirtualModelWizard<A>.ConfigureModelSlots.ModelSlotEntry entry) {
			super(entry);
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("configure_free_model_slot") + " : " + getModelSlotEntry().getModelSlotName();
		}

	}

	/**
	 * This step is used to configure a type-aware model slot
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFlexoConcept/ConfigureVirtualModelModelSlot.fib")
	public class ConfigureVirtualModelModelSlot extends ConfigureModelSlot<VirtualModelModelSlot> {

		public ConfigureVirtualModelModelSlot(AbstractCreateVirtualModelWizard<A>.ConfigureModelSlots.ModelSlotEntry entry) {
			super(entry);
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("configure_virtual_model_slot") + " : " + getModelSlotEntry().getModelSlotName();
		}

	}

}
