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
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.openflexo.FlexoCst;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.FlexoActionVisibleCondition;
import org.openflexo.foundation.action.PasteAction;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;

public class PasteActionInitializer extends ActionInitializer<PasteAction, FlexoObject, FlexoObject> {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public PasteActionInitializer(ControllerActionInitializer actionInitializer) {
		super(actionInitializer.getEditingContext().getPasteActionType(), actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<PasteAction> getDefaultInitializer() {
		return new FlexoActionInitializer<PasteAction>() {
			@Override
			public boolean run(EventObject e, PasteAction action) {
				logger.info("Paste initializer");
				// getControllerActionInitializer().getController().getSelectionManager().setSelectedObjects(null);
				return true;
			}
		};
	}

	@Override
	protected FlexoActionFinalizer<PasteAction> getDefaultFinalizer() {
		return new FlexoActionFinalizer<PasteAction>() {
			@Override
			public boolean run(EventObject e, PasteAction action) {
				logger.info("Paste finalizer");
				getControllerActionInitializer().getController().getSelectionManager().setSelectedObjects(action.getPastedObjects());
				return true;
			}
		};
	}

	@Override
	protected Icon getEnabledIcon() {
		return IconLibrary.PASTE_ICON;
	}

	@Override
	protected KeyStroke getShortcut() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_V, FlexoCst.META_MASK);
	}

	@Override
	protected FlexoActionVisibleCondition<PasteAction, FlexoObject, FlexoObject> getVisibleCondition() {
		return new FlexoActionVisibleCondition<PasteAction, FlexoObject, FlexoObject>() {

			@Override
			public boolean isVisible(FlexoActionType<PasteAction, FlexoObject, FlexoObject> actionType, FlexoObject object,
					Vector<FlexoObject> globalSelection, FlexoEditor editor) {
				return true;
			}

		};
	}
}
