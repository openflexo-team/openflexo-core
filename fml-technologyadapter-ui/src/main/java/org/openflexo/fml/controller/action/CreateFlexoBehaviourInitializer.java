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

import javax.swing.Icon;

import org.openflexo.components.wizard.Wizard;
import org.openflexo.components.wizard.WizardDialog;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionRunnable;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;

public class CreateFlexoBehaviourInitializer extends ActionInitializer<CreateFlexoBehaviour, FlexoConceptObject, FMLObject> {
	public CreateFlexoBehaviourInitializer(ControllerActionInitializer actionInitializer) {
		super(CreateFlexoBehaviour.actionType, actionInitializer);
		actionInitializer.registerInitializer(CreateFlexoBehaviour.createCreationSchemeType, this);
		actionInitializer.registerInitializer(CreateFlexoBehaviour.createActionSchemeType, this);
		actionInitializer.registerInitializer(CreateFlexoBehaviour.createDeletionSchemeType, this);
		actionInitializer.registerInitializer(CreateFlexoBehaviour.createEventListenerType, this);
		actionInitializer.registerInitializer(CreateFlexoBehaviour.createSynchronizationSchemeType, this);
		actionInitializer.registerInitializer(CreateFlexoBehaviour.createCloningSchemeType, this);
		actionInitializer.registerInitializer(CreateFlexoBehaviour.createNavigationSchemeType, this);
	}

	@Override
	protected FlexoActionRunnable<CreateFlexoBehaviour, FlexoConceptObject, FMLObject> getDefaultInitializer() {
		return (e, action) -> {
			Wizard wizard = new CreateFlexoBehaviourWizard(action, getController());
			WizardDialog dialog = new WizardDialog(wizard, getController());
			dialog.showDialog();
			if (dialog.getStatus() != Status.VALIDATED) {
				// Operation cancelled
				return false;
			}
			return true;
			// return instanciateAndShowDialog(action, VPMCst.CREATE_FLEXO_BEHAVIOUR_DIALOG_FIB);
		};
	}

	@Override
	protected FlexoActionRunnable<CreateFlexoBehaviour, FlexoConceptObject, FMLObject> getDefaultFinalizer() {
		return (e, action) -> {
			// getController().setCurrentEditedObjectAsModuleView(action.getNewModelSlot(), getController().VIEW_POINT_PERSPECTIVE);
			return true;
		};
	}

	@Override
	protected Icon getEnabledIcon(FlexoActionFactory<CreateFlexoBehaviour, FlexoConceptObject, FMLObject> actionType) {
		if (actionType == CreateFlexoBehaviour.createActionSchemeType) {
			return FMLIconLibrary.ACTION_SCHEME_ICON;
		}
		if (actionType == CreateFlexoBehaviour.createCreationSchemeType) {
			return FMLIconLibrary.CREATION_SCHEME_ICON;
		}
		if (actionType == CreateFlexoBehaviour.createDeletionSchemeType) {
			return FMLIconLibrary.DELETION_SCHEME_ICON;
		}
		if (actionType == CreateFlexoBehaviour.createEventListenerType) {
			return FMLIconLibrary.EVENT_LISTENER_ICON;
		}
		if (actionType == CreateFlexoBehaviour.createSynchronizationSchemeType) {
			return FMLIconLibrary.SYNCHRONIZATION_SCHEME_ICON;
		}
		if (actionType == CreateFlexoBehaviour.createCloningSchemeType) {
			return FMLIconLibrary.CLONING_SCHEME_ICON;
		}
		return FMLIconLibrary.FLEXO_CONCEPT_ACTION_ICON;
	}

}
