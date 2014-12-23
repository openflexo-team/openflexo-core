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
package org.openflexo.fml.rt.controller.action;

import java.util.EventObject;
import java.util.logging.Logger;

import javax.swing.Icon;

import org.openflexo.components.wizard.Wizard;
import org.openflexo.components.wizard.WizardDialog;
import org.openflexo.fib.controller.FIBController.Status;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.action.FlexoExceptionHandler;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.rt.action.CreateView;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;

public class CreateViewInitializer extends ActionInitializer<CreateView, RepositoryFolder, FlexoObject> {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public CreateViewInitializer(ControllerActionInitializer actionInitializer) {
		super(CreateView.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<CreateView> getDefaultInitializer() {
		return new FlexoActionInitializer<CreateView>() {
			@Override
			public boolean run(EventObject e, CreateView action) {
				// ((FlexoProject)action.getFocusedObject().getResourceRepository()).getViewPointRepository().getViewPointLibrary().getViewPoints();
				if (action.skipChoosePopup) {
					return true;
				} else {
					Wizard wizard = new CreateViewWizard(action, getController());
					WizardDialog dialog = new WizardDialog(wizard);
					dialog.showDialog();
					if (dialog.getStatus() != Status.VALIDATED) {
						// Operation cancelled
						return false;
					}
					return true;
					// return instanciateAndShowDialog(action, CommonFIB.CREATE_VIEW_DIALOG_FIB);
				}

			}
		};
	}

	@Override
	protected FlexoActionFinalizer<CreateView> getDefaultFinalizer() {
		return new FlexoActionFinalizer<CreateView>() {
			@Override
			public boolean run(EventObject e, CreateView action) {
				// getController().setCurrentEditedObjectAsModuleView(action.getNewView());
				getController().selectAndFocusObject(action.getNewView());
				/*CreateConcreteVirtualModelInstance actionVMi = CreateConcreteVirtualModelInstance.actionType.makeNewAction(
						action.getNewView(), null, getEditor());
				actionVMi.doAction();*/
				return true;
			}
		};
	}

	@Override
	protected FlexoExceptionHandler<CreateView> getDefaultExceptionHandler() {
		return new FlexoExceptionHandler<CreateView>() {
			@Override
			public boolean handleException(FlexoException exception, CreateView action) {
				if (exception instanceof NotImplementedException) {
					FlexoController.notify(FlexoLocalization.localizedForKey("not_implemented_yet"));
					return true;
				}
				return false;
			}
		};
	}

	@Override
	protected Icon getEnabledIcon() {
		return FMLRTIconLibrary.VIEW_ICON;
	}

}
