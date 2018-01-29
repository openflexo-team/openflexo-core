/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.action;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;

public class DeleteVirtualModel extends FlexoAction<DeleteVirtualModel, VirtualModel, FMLObject> {

	private static final Logger logger = Logger.getLogger(DeleteVirtualModel.class.getPackage().getName());

	public static FlexoActionFactory<DeleteVirtualModel, VirtualModel, FMLObject> actionType = new FlexoActionFactory<DeleteVirtualModel, VirtualModel, FMLObject>(
			"delete_virtual_model", FlexoActionFactory.editGroup, FlexoActionFactory.DELETE_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DeleteVirtualModel makeNewAction(VirtualModel focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new DeleteVirtualModel(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(VirtualModel object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(VirtualModel object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(DeleteVirtualModel.actionType, VirtualModel.class);
	}

	DeleteVirtualModel(VirtualModel focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		logger.info("Delete VirtualModel");

		// First recursively delete contained VirtualModel
		for (VirtualModel vm : new ArrayList<>(getFocusedObject().getVirtualModels())) {
			DeleteVirtualModel deleteVM = DeleteVirtualModel.actionType.makeNewEmbeddedAction(vm, null, this);
			deleteVM.doAction();
		}

		// Then handle the resource
		VirtualModelResource virtualModelResource = (VirtualModelResource) getFocusedObject().getResource();
		VirtualModelResource containerResource = virtualModelResource.getContainer();
		if (containerResource != null) {
			containerResource.getVirtualModel().removeFromVirtualModels(getFocusedObject());
		}

		// Delete the VirtualModel itself
		getFocusedObject().delete();

		// Delete the resource and notify container
		if (virtualModelResource != null) {
			virtualModelResource.delete();
			if (containerResource != null) {
				containerResource.notifyContentsRemoved(virtualModelResource);
			}
		}

	}

}
