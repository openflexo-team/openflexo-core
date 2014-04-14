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
package org.openflexo.action;

import java.awt.event.KeyEvent;
import java.util.EventObject;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.openflexo.FlexoCst;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.CutAction;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;

public class CutActionInitializer extends ActionInitializer<CutAction, FlexoObject, FlexoObject> {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public static KeyStroke ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_X, FlexoCst.META_MASK);

	public CutActionInitializer(ControllerActionInitializer actionInitializer) {
		super(actionInitializer.getEditingContext().getCutActionType(), actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<CutAction> getDefaultInitializer() {
		return new FlexoActionInitializer<CutAction>() {
			@Override
			public boolean run(EventObject e, CutAction action) {
				return FlexoController.confirm(FlexoLocalization.localizedForKey("would_you_like_to_cut_those_objects"));
			}
		};
	}

	@Override
	protected FlexoActionFinalizer<CutAction> getDefaultFinalizer() {
		return new FlexoActionFinalizer<CutAction>() {
			@Override
			public boolean run(EventObject e, CutAction action) {
				// getControllerActionInitializer().getController().getSelectionManager().setSelectedObjects(null);
				return true;
			}
		};
	}

	@Override
	protected Icon getEnabledIcon() {
		return IconLibrary.CUT_ICON;
	}

	@Override
	protected KeyStroke getShortcut() {
		return ACCELERATOR;
	}

}
