package org.openflexo.vpm.controller.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.FreeModelSlot;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.VirtualModelModelSlot;
import org.openflexo.foundation.viewpoint.annotations.FIBPanel;
import org.openflexo.foundation.viewpoint.rm.VirtualModelResource;
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
		modelSlotConfigurationSteps = new ArrayList<ConfigureModelSlot<?>>();
	}

	protected void appendConfigureModelSlots() {
		addStep(configureModelSlots = new ConfigureModelSlots());
	}

	public ConfigureModelSlots getConfigureModelSlots() {
		return configureModelSlots;
	}

	public abstract ViewPoint getViewPoint();

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFlexoConcept/ConfigureModelSlots.fib")
	public class ConfigureModelSlots extends WizardStep implements PropertyChangeListener {

		private final List<AbstractCreateVirtualModelWizard.ModelSlotEntry> modelSlotEntries;

		public ConfigureModelSlots() {
			modelSlotEntries = new ArrayList<AbstractCreateVirtualModelWizard.ModelSlotEntry>();
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
		public List<AbstractCreateVirtualModelWizard.ModelSlotEntry> getModelSlotEntries() {
			return modelSlotEntries;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (getModelSlotEntries().contains(evt.getSource())) {
				checkValidity();
			}
		}

		// Required full qualified class name, otherwise JVM throw a ParseException while introspecting
		public AbstractCreateVirtualModelWizard.ModelSlotEntry newModelSlotEntry() {
			ModelSlotEntry returned = new ModelSlotEntry();
			modelSlotEntries.add(returned);
			returned.getPropertyChangeSupport().addPropertyChangeListener(this);
			getPropertyChangeSupport().firePropertyChange("modelSlotEntries", null, returned);
			checkValidity();
			return returned;
		}

		// Required full qualified class name, otherwise JVM throw a ParseException while introspecting
		public void deleteModelSlotEntry(AbstractCreateVirtualModelWizard.ModelSlotEntry modelSlotEntryToDelete) {
			modelSlotEntryToDelete.getPropertyChangeSupport().removePropertyChangeListener(this);
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
					String errorMessage = entry.getConfigurationErrorMessage();
					String warningMessage = entry.getConfigurationWarningMessage();
					if (StringUtils.isNotEmpty(errorMessage)) {
						setIssueMessage(errorMessage, IssueMessageType.ERROR);
						return false;
					}
					if (StringUtils.isNotEmpty(warningMessage)) {
						setIssueMessage(warningMessage, IssueMessageType.WARNING);
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
			return getModelSlotEntries().size() > 0;
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
				msEntry.getPropertyChangeSupport().removePropertyChangeListener(this);
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
				step.getModelSlotEntry().getPropertyChangeSupport().addPropertyChangeListener(this);
				removeStep(step);
			}
			modelSlotConfigurationSteps.clear();
		}

	}

	/**
	 * This abstract generic step is used to configure a model slot
	 * 
	 * @author sylvain
	 *
	 */
	public abstract class ConfigureModelSlot<MS extends ModelSlot<?>> extends WizardStep {

		private final AbstractCreateVirtualModelWizard.ModelSlotEntry modelSlotEntry;

		public ConfigureModelSlot(AbstractCreateVirtualModelWizard.ModelSlotEntry modelSlotEntry) {
			this.modelSlotEntry = modelSlotEntry;
		}

		public AbstractCreateVirtualModelWizard.ModelSlotEntry getModelSlotEntry() {
			return modelSlotEntry;
		}

		public A getAction() {
			return AbstractCreateVirtualModelWizard.this.getAction();
		}

		public ViewPoint getViewPoint() {
			return AbstractCreateVirtualModelWizard.this.getViewPoint();
		}

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		@Override
		public boolean isValid() {
			String configurationErrorMessage = modelSlotEntry.getConfigurationErrorMessage();
			if (StringUtils.isNotEmpty(configurationErrorMessage)) {
				setIssueMessage(FlexoLocalization.localizedForKey("valid_model_slot_configuration"), IssueMessageType.INFO);
				return false;
			}
			return true;
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

		private FlexoMetaModelResource<M, MM, ?> metaModelResource;

		public ConfigureTypeAwareModelSlot(AbstractCreateVirtualModelWizard.ModelSlotEntry entry) {
			super(entry);
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("configure_type_aware_model_slot") + " : " + getModelSlotEntry().getModelSlotName();
		}

		public FlexoMetaModelResource<M, MM, ?> getMetaModelResource() {
			return metaModelResource;
		}

		public void setMetaModelResource(FlexoMetaModelResource<M, MM, ?> metaModelResource) {
			if ((metaModelResource == null && this.metaModelResource != null)
					|| (metaModelResource != null && !metaModelResource.equals(this.metaModelResource))) {
				FlexoMetaModelResource<M, MM, ?> oldValue = this.metaModelResource;
				this.metaModelResource = metaModelResource;
				getPropertyChangeSupport().firePropertyChange("metaModelResource", oldValue, metaModelResource);
				checkValidity();
			}
		}

		@Override
		public boolean isValid() {
			if (!super.isValid()) {
				return false;
			}
			if (getMetaModelResource() == null) {
				setIssueMessage(FlexoLocalization.localizedForKey("please_provide_a_valid_meta_model"), IssueMessageType.ERROR);
				return false;
			}
			return true;
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

		public ConfigureFreeModelSlot(AbstractCreateVirtualModelWizard.ModelSlotEntry entry) {
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

		private VirtualModelResource virtualModelResource;

		public ConfigureVirtualModelModelSlot(AbstractCreateVirtualModelWizard.ModelSlotEntry entry) {
			super(entry);
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("configure_virtual_model_slot") + " : " + getModelSlotEntry().getModelSlotName();
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
				checkValidity();
			}
		}

		@Override
		public boolean isValid() {
			if (!super.isValid()) {
				return false;
			}
			if (getVirtualModelResource() == null) {
				setIssueMessage(FlexoLocalization.localizedForKey("please_provide_a_valid_virtual_model"), IssueMessageType.ERROR);
				return false;
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
			defaultModelSlotName = "modelSlot" + (configureModelSlots.getModelSlotEntries().size() + 1);
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
