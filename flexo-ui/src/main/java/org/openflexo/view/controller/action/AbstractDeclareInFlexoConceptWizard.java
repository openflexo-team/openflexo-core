/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-rt-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.view.controller.action;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.action.transformation.AbstractDeclareInFlexoConcept;
import org.openflexo.foundation.action.transformation.AbstractDeclareInFlexoConcept.DeclareInFlexoConceptChoices;
import org.openflexo.foundation.action.transformation.FlexoConceptCreationStrategy;
import org.openflexo.foundation.action.transformation.FlexoRoleCreationStrategy;
import org.openflexo.foundation.action.transformation.FlexoRoleSettingStrategy;
import org.openflexo.foundation.action.transformation.TransformationStrategy;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.FlexoController;

public abstract class AbstractDeclareInFlexoConceptWizard<A extends AbstractDeclareInFlexoConcept<A, ?, ?>> extends
		AbstractTransformationWizard<A> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractDeclareInFlexoConceptWizard.class.getPackage().getName());

	private final ChooseOption chooseOption;
	private TransformationConfigurationStep<? extends TransformationStrategy<A>> detailedStep;

	public AbstractDeclareInFlexoConceptWizard(A action, FlexoController controller) {
		super(action, controller);
		addStep(chooseOption = new ChooseOption());
	}

	/**
	 * This step is used to select option
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/DeclareDiagramElementInFlexoConcept/ChooseOption.fib")
	public class ChooseOption extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public A getAction() {
			return AbstractDeclareInFlexoConceptWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("choose_an_option");
		}

		@Override
		public boolean isTransitionalStep() {
			return true;
		}

		@Override
		public boolean isValid() {

			if (getPrimaryChoice() == null) {
				setIssueMessage(FlexoLocalization.localizedForKey("please_choose_an_option"), IssueMessageType.ERROR);
				return false;
			} else if (getVirtualModelResource() == null) {
				setIssueMessage(FlexoLocalization.localizedForKey("please_select_a_virtual_model"), IssueMessageType.ERROR);
				return false;
			}
			return true;
		}

		public DeclareInFlexoConceptChoices getPrimaryChoice() {
			return getAction().getPrimaryChoice();
		}

		public void setPrimaryChoice(DeclareInFlexoConceptChoices choice) {
			if (choice != getPrimaryChoice()) {
				DeclareInFlexoConceptChoices oldValue = getPrimaryChoice();
				getAction().setPrimaryChoice(choice);
				getPropertyChangeSupport().firePropertyChange("primaryChoice", oldValue, choice);
				getPropertyChangeSupport().firePropertyChange("availableStrategies", oldValue, choice);
				checkValidity();
			}
		}

		public VirtualModelResource getVirtualModelResource() {
			return getAction().getVirtualModelResource();
		}

		public void setVirtualModelResource(VirtualModelResource virtualModelResource) {
			if (virtualModelResource != getVirtualModelResource()) {
				VirtualModelResource oldValue = getVirtualModelResource();
				getAction().setVirtualModelResource(virtualModelResource);
				getPropertyChangeSupport().firePropertyChange("virtualModelResource", oldValue, virtualModelResource);
				checkValidity();
			}
		}

		public List<? extends TransformationStrategy<A>> getAvailableStrategies() {
			switch (getPrimaryChoice()) {
			case REPLACE_ELEMENT_IN_EXISTING_FLEXO_CONCEPT:
				return getAction().getAvailableFlexoRoleSettingStrategies();
			case CREATE_ELEMENT_IN_EXISTING_FLEXO_CONCEPT:
				return getAction().getAvailableFlexoRoleCreationStrategies();
			case CREATES_FLEXO_CONCEPT:
				return getAction().getAvailableFlexoConceptCreationStrategies();
			}
			return null;
		}

		@Override
		public void performTransition() {

			switch (chooseOption.getPrimaryChoice()) {
			case REPLACE_ELEMENT_IN_EXISTING_FLEXO_CONCEPT:
				detailedStep = replaceElementInExistingFlexoConcept();
				break;
			case CREATE_ELEMENT_IN_EXISTING_FLEXO_CONCEPT:
				detailedStep = createsElementInExistingFlexoConcept();
				break;
			case CREATES_FLEXO_CONCEPT:
				detailedStep = chooseNewFlexoConcept();
				break;
			}

			addStep(detailedStep);

		}

		@Override
		public void discardTransition() {

			removeStep(detailedStep);
			detailedStep = null;
		}

	}

	public abstract TransformationConfigurationStep<? extends FlexoRoleSettingStrategy<A, ?, ?, ?>> replaceElementInExistingFlexoConcept();

	public abstract TransformationConfigurationStep<? extends FlexoRoleCreationStrategy<A, ?, ?, ?>> createsElementInExistingFlexoConcept();

	public abstract TransformationConfigurationStep<? extends FlexoConceptCreationStrategy<A>> chooseNewFlexoConcept();

}
