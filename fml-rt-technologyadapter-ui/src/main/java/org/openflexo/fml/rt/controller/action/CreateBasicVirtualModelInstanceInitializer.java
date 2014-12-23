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
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;

public class CreateBasicVirtualModelInstanceInitializer extends ActionInitializer<CreateBasicVirtualModelInstance, View, FlexoObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public CreateBasicVirtualModelInstanceInitializer(ControllerActionInitializer actionInitializer) {
		super(CreateBasicVirtualModelInstance.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<CreateBasicVirtualModelInstance> getDefaultInitializer() {
		return new FlexoActionInitializer<CreateBasicVirtualModelInstance>() {
			@Override
			public boolean run(EventObject e, CreateBasicVirtualModelInstance action) {
				if (action.skipChoosePopup) {
					return true;
				} else {
					Wizard wizard = new CreateBasicVirtualModelInstanceWizard(action, getController());
					WizardDialog dialog = new WizardDialog(wizard);
					dialog.showDialog();
					if (dialog.getStatus() != Status.VALIDATED) {
						// Operation cancelled
						return false;
					}
					return true;
				}

			}
		};
	}

	@Override
	protected FlexoActionFinalizer<CreateBasicVirtualModelInstance> getDefaultFinalizer() {
		return new FlexoActionFinalizer<CreateBasicVirtualModelInstance>() {
			@Override
			public boolean run(EventObject e, CreateBasicVirtualModelInstance action) {
				// getController().setCurrentEditedObjectAsModuleView(action.getNewVirtualModelInstance());
				getController().selectAndFocusObject(action.getNewVirtualModelInstance());
				return true;
			}
		};
	}

	@Override
	protected FlexoExceptionHandler<CreateBasicVirtualModelInstance> getDefaultExceptionHandler() {
		return new FlexoExceptionHandler<CreateBasicVirtualModelInstance>() {
			@Override
			public boolean handleException(FlexoException exception, CreateBasicVirtualModelInstance action) {
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
		return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
	}

	/**
	 * @author Vincent This method has to be removed as soon as we will have a real Wizard Management. Its purpose is to handle the
	 *         separation of FIBs for Model Slot Configurations.
	 * @return File that correspond to the FIB
	 */
	/*private Resource getModelSlotInstanceConfigurationFIB(Class<? extends ModelSlot> modelSlotClass) {
		if (TypeAwareModelSlot.class.isAssignableFrom(modelSlotClass)) {
			return CommonFIB.CONFIGURE_TYPE_AWARE_MODEL_SLOT_INSTANCE_DIALOG_FIB;
		}
		if (FreeModelSlot.class.isAssignableFrom(modelSlotClass)) {
			return CommonFIB.CONFIGURE_FREE_MODEL_SLOT_INSTANCE_DIALOG_FIB;
		}
		if (FMLRTModelSlot.class.isAssignableFrom(modelSlotClass)) {
			return CommonFIB.CONFIGURE_VIRTUAL_MODEL_SLOT_INSTANCE_DIALOG_FIB;
		}
		return null;
	}*/
}
