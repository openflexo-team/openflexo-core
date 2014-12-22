package org.openflexo.vpm.controller.action;

import java.awt.Dimension;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.AbstractCreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateViewPoint;
import org.openflexo.foundation.fml.action.CreateVirtualModel;
import org.openflexo.foundation.fml.annotations.FIBPanel;
import org.openflexo.foundation.fmlrt.VirtualModelInstance;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.FlexoController;

/**
 * Common stuff for wizards of {@link AbstractCreateFlexoConcept} action
 * 
 * @author sylvain
 *
 * @param <A>
 * @see CreateFlexoConcept
 * @see CreateVirtualModel
 * @see CreateViewPoint
 */
public abstract class AbstractCreateFlexoConceptWizard<A extends AbstractCreateFlexoConcept<?, ?, ?>> extends FlexoWizard {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCreateFlexoConceptWizard.class.getPackage().getName());

	private final A action;

	private static final Dimension DIMENSIONS = new Dimension(900, 600);

	public AbstractCreateFlexoConceptWizard(A action, FlexoController controller) {
		super(controller);
		this.action = action;
	}

	public A getAction() {
		return action;
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFlexoConcept/ConfigureAdditionalStepsForNewFlexoConcept.fib")
	public class ConfigureAdditionalStepsForNewFlexoConcept extends WizardStep {

		private boolean defineSomeRoles = false;
		private boolean defineSomeBehaviours = false;
		private boolean defineDefaultCreationScheme = false;
		private boolean defineDefaultDeletionScheme = false;
		private boolean defineInspector = false;

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public AbstractCreateFlexoConcept getAction() {
			return AbstractCreateFlexoConceptWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("configure_new_flexo_concept");
		}

		@Override
		public boolean isValid() {

			return true;
		}

		public boolean getDefineSomeRoles() {
			return defineSomeRoles;
		}

		public boolean getDefineSomeBehaviours() {
			return defineSomeBehaviours;
		}

		public boolean getDefineDefaultCreationScheme() {
			return defineDefaultCreationScheme;
		}

		public boolean getDefineDefaultDeletionScheme() {
			return defineDefaultDeletionScheme;
		}

		public boolean getDefineInspector() {
			return defineInspector;
		}

		public void setDefineSomeRoles(boolean defineSomeRoles) {
			if (defineSomeRoles != this.defineSomeRoles) {
				this.defineSomeRoles = defineSomeRoles;
				getPropertyChangeSupport().firePropertyChange("defineSomeRoles", !defineSomeRoles, defineSomeRoles);
				checkValidity();
			}
		}

		public void setDefineSomeBehaviours(boolean defineSomeBehaviours) {
			if (defineSomeBehaviours != this.defineSomeBehaviours) {
				this.defineSomeBehaviours = defineSomeBehaviours;
				getPropertyChangeSupport().firePropertyChange("defineSomeBehaviours", !defineSomeBehaviours, defineSomeBehaviours);
				checkValidity();
			}
		}

		public void setDefineDefaultCreationScheme(boolean defineDefaultCreationScheme) {
			if (defineDefaultCreationScheme != this.defineDefaultCreationScheme) {
				this.defineDefaultCreationScheme = defineDefaultCreationScheme;
				getPropertyChangeSupport().firePropertyChange("defineDefaultCreationScheme", !defineDefaultCreationScheme,
						defineDefaultCreationScheme);
				checkValidity();
			}
		}

		public void setDefineDefaultDeletionScheme(boolean defineDefaultDeletionScheme) {
			if (defineDefaultDeletionScheme != this.defineDefaultDeletionScheme) {
				this.defineDefaultDeletionScheme = defineDefaultDeletionScheme;
				getPropertyChangeSupport().firePropertyChange("defineDefaultDeletionScheme", !defineDefaultDeletionScheme,
						defineDefaultDeletionScheme);
				checkValidity();
			}
		}

		public void setDefineInspector(boolean defineInspector) {
			if (defineInspector != this.defineInspector) {
				this.defineInspector = defineInspector;
				getPropertyChangeSupport().firePropertyChange("defineInspector", !defineInspector, defineInspector);
				checkValidity();
			}
		}

		@Override
		public boolean isTransitionalStep() {
			return getDefineSomeRoles() || getDefineSomeBehaviours() || getDefineDefaultCreationScheme()
					|| getDefineDefaultDeletionScheme() || getDefineInspector();
		}

		@Override
		public void performTransition() {
			// We have now to update all steps according to chosen VirtualModel
			/*for (ModelSlot<?> ms : chooseVirtualModel.getVirtualModel().getModelSlots()) {
				ConfigureModelSlot<?, ?> step = makeConfigureModelSlotStep(ms);
				if (step != null) {
					modelSlotConfigurationSteps.add(step);
					addStep(step);
				}
			}
			if (chooseVirtualModel.getVirtualModel().hasCreationScheme()) {
				chooseAndConfigureCreationScheme = new ChooseAndConfigureCreationScheme();
				addStep(chooseAndConfigureCreationScheme);
			}*/
		}

		@Override
		public void discardTransition() {
			/*	for (ConfigureModelSlot<?, ?> step : modelSlotConfigurationSteps) {
					removeStep(step);
				}
				modelSlotConfigurationSteps.clear();
				if (chooseAndConfigureCreationScheme != null) {
					removeStep(chooseAndConfigureCreationScheme);
					chooseAndConfigureCreationScheme = null;
				}*/
		}

	}

}
