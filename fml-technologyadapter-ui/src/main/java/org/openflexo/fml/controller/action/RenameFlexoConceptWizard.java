/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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
import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.action.RenameFlexoConcept;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class RenameFlexoConceptWizard extends FlexoActionWizard<RenameFlexoConcept> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(RenameFlexoConceptWizard.class.getPackage().getName());

	private static final String NO_NAME_DEFINED = "please_provide_a_valid_name";
	private static final String UNCHANGED_NAME = "unchanged_name";
	private static final String ALREADY_EXISTING_NAME = "already_existing_concept";
	private static final Dimension DIMENSIONS = new Dimension(700, 400);

	private final RenameFlexoConceptInfo renameFlexoConceptInfo;

	public RenameFlexoConceptWizard(RenameFlexoConcept action, FlexoController controller) {
		super(action, controller);
		addStep(renameFlexoConceptInfo = new RenameFlexoConceptInfo());
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("rename_flexo_concept");
	}

	@Override
	public Image getDefaultPageImage() {
		return FMLIconLibrary.FLEXO_CONCEPT_BIG_ICON.getImage();
	}

	public RenameFlexoConceptInfo getMoveFlexoConceptInfo() {
		return renameFlexoConceptInfo;
	}

	@FIBPanel("Fib/Wizard/Refactor/RenameFlexoConcept.fib")
	public class RenameFlexoConceptInfo extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public RenameFlexoConcept getAction() {
			return RenameFlexoConceptWizard.this.getAction();
		}

		public FlexoConcept getFlexoConcept() {
			return getAction().getFocusedObject();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("choose_a_valid_new_name");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getNewName())) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_NAME_DEFINED), IssueMessageType.ERROR);
				return false;
			}
			if (getAction().getFocusedObject().getName().equals(getNewName())) {
				setIssueMessage(getAction().getLocales().localizedForKey(UNCHANGED_NAME), IssueMessageType.ERROR);
				return false;
			}
			if (getFlexoConcept().getDeclaringCompilationUnit().getFlexoConcept(getNewName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey(ALREADY_EXISTING_NAME), IssueMessageType.ERROR);
				return false;
			}

			return true;
		}

		public String getNewName() {
			return getAction().getNewFlexoConceptName();
		}

		public void setNewName(String newName) {
			if ((newName == null && getNewName() != null) || (newName != null && !newName.equals(getNewName()))) {
				String oldValue = getNewName();
				getAction().setNewFlexoConceptName(newName);
				getPropertyChangeSupport().firePropertyChange("newName", oldValue, newName);
				checkValidity();
			}
		}

		public String getNewDescription() {
			return getAction().getNewFlexoConceptDescription();
		}

		public void setNewDescription(String newDescription) {
			if ((newDescription == null && getNewDescription() != null)
					|| (newDescription != null && !newDescription.equals(getNewDescription()))) {
				String oldValue = getNewDescription();
				getAction().setNewFlexoConceptDescription(newDescription);
				getPropertyChangeSupport().firePropertyChange("newDescription", oldValue, newDescription);
				checkValidity();
			}
		}

	}

}
