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

import java.util.EventObject;
import java.util.logging.Logger;

import javax.swing.Icon;

import org.openflexo.components.wizard.Wizard;
import org.openflexo.components.wizard.WizardDialog;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
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
				action.setDefineSomeBehaviours(true);
				Wizard wizard = new CreateFlexoConceptWizard(action, getController());
				WizardDialog dialog = new WizardDialog(wizard, getController());
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
	protected Icon getEnabledIcon(FlexoActionType actionType) {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_CONCEPT_ICON, IconLibrary.NEW_MARKER);
	}

}
