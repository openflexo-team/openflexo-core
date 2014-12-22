package org.openflexo.vpm.controller.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.FMLModelSlot;
import org.openflexo.foundation.fml.action.AbstractCreateVirtualModel;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateViewPoint;
import org.openflexo.foundation.fml.action.CreateVirtualModel;
import org.openflexo.foundation.fml.action.AbstractCreateVirtualModel.ModelSlotEntry;
import org.openflexo.foundation.fml.annotations.FIBPanel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fmlrt.VirtualModelInstance;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.FreeModelSlot;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

/**
 * Common stuff for wizards of {@link AbstractCreateVirtualModel} action
 * 
 * @author sylvain
 *
 * @param <A>
 * @see CreateFlexoConcept
 * @see CreateVirtualModel
 * @see CreateViewPoint
 */
public abstract class AbstractCreateVirtualModelWizard<A extends AbstractCreateVirtualModel<?, ?, ?>> extends
		AbstractCreateFlexoConceptWizard<A> {

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

		public ConfigureModelSlots() {
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

		public List<ModelSlotEntry> getModelSlotEntries() {
			return getAction().getModelSlotEntries();
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (getModelSlotEntries().contains(evt.getSource())) {
				checkValidity();
			}
		}

		public ModelSlotEntry newModelSlotEntry() {
			ModelSlotEntry newEntry = getAction().newModelSlotEntry();
			newEntry.getPropertyChangeSupport().addPropertyChangeListener(this);
			checkValidity();
			return newEntry;
		}

		public void deleteModelSlotEntry(ModelSlotEntry modelSlotEntryToDelete) {
			modelSlotEntryToDelete.getPropertyChangeSupport().removePropertyChangeListener(this);
			getAction().deleteModelSlotEntry(modelSlotEntryToDelete);
			checkValidity();
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
			} else if (FMLModelSlot.class.isAssignableFrom(msEntry.getModelSlotClass())) {
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

		private final ModelSlotEntry modelSlotEntry;

		public ConfigureModelSlot(ModelSlotEntry modelSlotEntry) {
			this.modelSlotEntry = modelSlotEntry;
		}

		public ModelSlotEntry getModelSlotEntry() {
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

		public ConfigureTypeAwareModelSlot(ModelSlotEntry entry) {
			super(entry);
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("configure_type_aware_model_slot") + " : " + getModelSlotEntry().getModelSlotName();
		}

		public FlexoMetaModelResource<M, MM, ?> getMetaModelResource() {
			return (FlexoMetaModelResource<M, MM, ?>) getModelSlotEntry().getMetaModelResource();
		}

		public void setMetaModelResource(FlexoMetaModelResource<M, MM, ?> metaModelResource) {
			if (getMetaModelResource() != metaModelResource) {
				FlexoMetaModelResource<M, MM, ?> oldValue = getMetaModelResource();
				getModelSlotEntry().setMetaModelResource(metaModelResource);
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

		public ConfigureFreeModelSlot(ModelSlotEntry entry) {
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
	public class ConfigureVirtualModelModelSlot extends ConfigureModelSlot<FMLModelSlot> {

		public ConfigureVirtualModelModelSlot(ModelSlotEntry entry) {
			super(entry);
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("configure_virtual_model_slot") + " : " + getModelSlotEntry().getModelSlotName();
		}

		public VirtualModelResource getVirtualModelResource() {
			return getModelSlotEntry().getVirtualModelResource();
		}

		public void setVirtualModelResource(VirtualModelResource virtualModelResource) {
			if (getVirtualModelResource() != virtualModelResource) {
				VirtualModelResource oldValue = getVirtualModelResource();
				getModelSlotEntry().setVirtualModelResource(virtualModelResource);
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

}
