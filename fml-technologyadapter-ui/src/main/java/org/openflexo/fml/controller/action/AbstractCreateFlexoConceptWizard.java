/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.action;

import java.awt.Dimension;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.AbstractCreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateViewPoint;
import org.openflexo.foundation.fml.action.CreateVirtualModel;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
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
	@FIBPanel("Fib/Wizard/CreateFMLElement/ConfigureAdditionalStepsForNewFlexoConcept.fib")
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
