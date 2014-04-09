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

import javax.swing.KeyStroke;

import org.openflexo.FlexoCst;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.action.SelectAllAction;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;

public class SelectAllActionInitializer extends ActionInitializer<SelectAllAction, FlexoObject, FlexoObject> {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public SelectAllActionInitializer(ControllerActionInitializer actionInitializer) {
		super(actionInitializer.getEditingContext().getSelectAllActionType(), actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<SelectAllAction> getDefaultInitializer() {
		return new FlexoActionInitializer<SelectAllAction>() {
			@Override
			public boolean run(EventObject e, SelectAllAction action) {
				return true;
			}
		};
	}

	@Override
	protected FlexoActionFinalizer<SelectAllAction> getDefaultFinalizer() {
		return new FlexoActionFinalizer<SelectAllAction>() {
			@Override
			public boolean run(EventObject e, SelectAllAction action) {
				System.out.println("Select all with " + action.getFocusedObject());
				/*DiagramElement<?> container = action.getFocusedObject();
				if (action.getFocusedObject() instanceof DiagramConnector) {
					container = ((DiagramConnector) action.getFocusedObject()).getParent();
				} else if (action.getFocusedObject() instanceof DiagramShape
						&& ((DiagramShape) action.getFocusedObject()).getChilds().size() == 0) {
					container = ((DiagramShape) action.getFocusedObject()).getParent();
				}
				if (container != null) {
					getControllerActionInitializer().getVESelectionManager().setSelectedObjects(container.getChilds());
					return true;
				} else {
					return false;
				}*/
				return false;
			}
		};
	}

	@Override
	protected KeyStroke getShortcut() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_A, FlexoCst.META_MASK);
	}

}
