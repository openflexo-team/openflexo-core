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

package org.openflexo.fml.rt.controller.action;

import java.util.EventObject;
import java.util.logging.Logger;

import javax.swing.Icon;

import org.openflexo.components.wizard.Wizard;
import org.openflexo.components.wizard.WizardDialog;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.action.FlexoExceptionHandler;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.rt.action.CreateViewInFolder;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;

public class CreateViewInitializer extends ActionInitializer<CreateViewInFolder, RepositoryFolder<ViewResource, ?>, FlexoObject> {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public CreateViewInitializer(ControllerActionInitializer actionInitializer) {
		super(CreateViewInFolder.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<CreateViewInFolder> getDefaultInitializer() {
		return new FlexoActionInitializer<CreateViewInFolder>() {
			@Override
			public boolean run(EventObject e, CreateViewInFolder action) {
				// ((FlexoProject)action.getFocusedObject().getResourceRepository()).getViewPointRepository().getViewPointLibrary().getViewPoints();
				if (action.skipChoosePopup()) {
					return true;
				}
				else {
					Wizard wizard = new CreateViewWizard(action, getController());
					WizardDialog dialog = new WizardDialog(wizard, getController());
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
	protected FlexoActionFinalizer<CreateViewInFolder> getDefaultFinalizer() {
		return new FlexoActionFinalizer<CreateViewInFolder>() {
			@Override
			public boolean run(EventObject e, CreateViewInFolder action) {
				System.out.println("Hop, on notifie pour la nouvelle resource");
				action.getNewView().getResource().getPropertyChangeSupport().firePropertyChange("contents", null,
						action.getNewView().getResource().getContents());
				action.getFocusedObject().getPropertyChangeSupport().firePropertyChange("resources", null,
						action.getFocusedObject().getResources());

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
	protected FlexoExceptionHandler<CreateViewInFolder> getDefaultExceptionHandler() {
		return new FlexoExceptionHandler<CreateViewInFolder>() {
			@Override
			public boolean handleException(FlexoException exception, CreateViewInFolder action) {
				if (exception instanceof NotImplementedException) {
					FlexoController.notify(action.getLocales().localizedForKey("not_implemented_yet"));
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
