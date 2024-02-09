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
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;

public class DeleteCompilationUnit extends FlexoAction<DeleteCompilationUnit, FMLCompilationUnit, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(DeleteCompilationUnit.class.getPackage().getName());

	public static FlexoActionFactory<DeleteCompilationUnit, FMLCompilationUnit, FMLObject> actionType = new FlexoActionFactory<DeleteCompilationUnit, FMLCompilationUnit, FMLObject>(
			"delete_compilation_unit", FlexoActionFactory.editGroup, FlexoActionFactory.DELETE_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DeleteCompilationUnit makeNewAction(FMLCompilationUnit focusedObject, Vector<FMLObject> globalSelection,
				FlexoEditor editor) {
			return new DeleteCompilationUnit(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLCompilationUnit object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FMLCompilationUnit object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(DeleteCompilationUnit.actionType, FMLCompilationUnit.class);
	}

	private DeleteCompilationUnit(FMLCompilationUnit focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		logger.info("Delete CompilationUnit " + getFocusedObject());

		CompilationUnitResource cuResource = (CompilationUnitResource) getFocusedObject().getResource();

		// First recursively delete contained VirtualModel
		for (CompilationUnitResource compilationUnitResource : cuResource.getContainedCompilationUnitResources()) {
			DeleteCompilationUnit deleteCU = DeleteCompilationUnit.actionType
					.makeNewEmbeddedAction(compilationUnitResource.getCompilationUnit(), null, this);
			deleteCU.doAction();
		}

		// Then handle the resource
		if (cuResource != null) {
			CompilationUnitResource containerResource = cuResource.getContainer();
			/*if (containerResource != null) {
				containerResource.getCompilationUnit().getVirtualModel().removeFromVirtualModels(getFocusedObject());
			}*/

			// Delete the VirtualModel itself
			getFocusedObject().delete();

			// Delete the resource and notify container
			cuResource.delete();
			if (containerResource != null) {
				containerResource.notifyContentsRemoved(cuResource);
			}
		}

	}

}
