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
package org.openflexo.fml.controller.action;

import java.util.EventObject;
import java.util.logging.Logger;

import javax.swing.Icon;

import org.openflexo.components.wizard.Wizard;
import org.openflexo.components.wizard.WizardDialog;
import org.openflexo.fib.controller.FIBController.Status;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.icon.VPMIconLibrary;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;

public class CreateFlexoConceptInitializer extends ActionInitializer {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public CreateFlexoConceptInitializer(ControllerActionInitializer actionInitializer) {
		super(CreateFlexoConcept.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<CreateFlexoConcept> getDefaultInitializer() {
		return new FlexoActionInitializer<CreateFlexoConcept>() {
			@Override
			public boolean run(EventObject e, CreateFlexoConcept action) {
				Wizard wizard = new CreateFlexoConceptWizard(action, getController());
				WizardDialog dialog = new WizardDialog(wizard);
				dialog.showDialog();
				if (dialog.getStatus() != Status.VALIDATED) {
					// Operation cancelled
					return false;
				}
				return true;
				// return instanciateAndShowDialog(action, VPMCst.CREATE_FLEXO_CONCEPT_DIALOG_FIB);
			}
		};
	}

	@Override
	protected FlexoActionFinalizer<CreateFlexoConcept> getDefaultFinalizer() {
		return new FlexoActionFinalizer<CreateFlexoConcept>() {
			@Override
			public boolean run(EventObject e, CreateFlexoConcept action) {
				if (action.switchNewlyCreatedFlexoConcept) {
					getController().setCurrentEditedObjectAsModuleView(action.getNewFlexoConcept());
				}
				return true;
			}
		};
	}

	@Override
	protected Icon getEnabledIcon() {
		return VPMIconLibrary.FLEXO_CONCEPT_ICON;
	}

}
