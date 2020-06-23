/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.action;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.openflexo.FlexoCst;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionRunnable;
import org.openflexo.foundation.action.SelectAllAction;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;

public class SelectAllActionInitializer extends ActionInitializer<SelectAllAction, FlexoObject, FlexoObject> {
	public SelectAllActionInitializer(ControllerActionInitializer actionInitializer) {
		super(actionInitializer.getEditingContext().getSelectAllActionType(), actionInitializer);
	}

	@Override
	protected FlexoActionRunnable<SelectAllAction, FlexoObject, FlexoObject> getDefaultFinalizer() {
		return (e, action) -> {
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
		};
	}

	@Override
	protected KeyStroke getShortcut() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_A, FlexoCst.META_MASK);
	}

}
