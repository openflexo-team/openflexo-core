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

import java.awt.HeadlessException;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionRunnable;
import org.openflexo.foundation.action.copypaste.AbstractCopyAction.InvalidSelectionException;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.action.DeleteFlexoConceptObjects;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;

public class DeleteFlexoConceptObjectsInitializer
		extends ActionInitializer<DeleteFlexoConceptObjects, FlexoConceptObject, FlexoConceptObject> {
	public DeleteFlexoConceptObjectsInitializer(ControllerActionInitializer actionInitializer) {
		super(DeleteFlexoConceptObjects.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionRunnable<DeleteFlexoConceptObjects, FlexoConceptObject, FlexoConceptObject> getDefaultInitializer() {
		return (e, action) -> {
			try {
				if (action.getObjectsToDelete().size() > 1) {
					return FlexoController.confirm(action.getLocales().localizedForKey("would_you_really_like_to_delete_those_objects_?"));
				}
				return FlexoController.confirm(action.getLocales().localizedForKey("would_you_really_like_to_delete_this_object_?"));
			} catch (HeadlessException e1) {
				e1.printStackTrace();
				return false;
			} catch (InvalidSelectionException e1) {
				e1.printStackTrace();
				return false;
			}
		};
	}

	@Override
	protected Icon getEnabledIcon(FlexoActionFactory<DeleteFlexoConceptObjects, FlexoConceptObject, FlexoConceptObject> actionType) {
		return IconLibrary.DELETE_ICON;
	}

	@Override
	protected KeyStroke getShortcut() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
	}
}
