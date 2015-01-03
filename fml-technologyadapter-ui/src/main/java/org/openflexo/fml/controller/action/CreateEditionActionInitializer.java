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

import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;

public class CreateEditionActionInitializer extends ActionInitializer<CreateEditionAction, FMLControlGraph, FMLObject> {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public static Resource CREATE_EDITION_ACTION_DIALOG_FIB = ResourceLocator.locateResource("Fib/Dialog/CreateEditionActionDialog.fib");

	public CreateEditionActionInitializer(ControllerActionInitializer actionInitializer) {
		super(CreateEditionAction.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<CreateEditionAction> getDefaultInitializer() {
		return new FlexoActionInitializer<CreateEditionAction>() {
			@Override
			public boolean run(EventObject e, CreateEditionAction action) {
				return instanciateAndShowDialog(action, CREATE_EDITION_ACTION_DIALOG_FIB);
			}
		};
	}

	@Override
	protected FlexoActionFinalizer<CreateEditionAction> getDefaultFinalizer() {
		return new FlexoActionFinalizer<CreateEditionAction>() {
			@Override
			public boolean run(EventObject e, CreateEditionAction action) {
				// getController().setCurrentEditedObjectAsModuleView(action.getNewModelSlot(), getController().VIEW_POINT_PERSPECTIVE);
				return true;
			}
		};
	}

	@Override
	protected Icon getEnabledIcon() {
		return FMLIconLibrary.FLEXO_CONCEPT_ACTION_ICON;
	}

}
