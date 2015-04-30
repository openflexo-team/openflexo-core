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

package org.openflexo.foundation.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.resource.RepositoryFolder;

public class MoveRepositoryFolder extends FlexoAction<MoveRepositoryFolder, RepositoryFolder, RepositoryFolder> {

	private static final Logger logger = Logger.getLogger(MoveRepositoryFolder.class.getPackage().getName());

	public static final FlexoActionType<MoveRepositoryFolder, RepositoryFolder, RepositoryFolder> actionType = new FlexoActionType<MoveRepositoryFolder, RepositoryFolder, RepositoryFolder>(
			"move_folder") {

		@Override
		public boolean isEnabledForSelection(RepositoryFolder object, Vector<RepositoryFolder> globalSelection) {
			return true;
		}

		@Override
		public boolean isVisibleForSelection(RepositoryFolder object, Vector<RepositoryFolder> globalSelection) {
			return false;
		}

		@Override
		public MoveRepositoryFolder makeNewAction(RepositoryFolder focusedObject, Vector<RepositoryFolder> globalSelection,
				FlexoEditor editor) {
			return new MoveRepositoryFolder(focusedObject, globalSelection, editor);
		}

	};

	private RepositoryFolder folder;

	protected MoveRepositoryFolder(RepositoryFolder focusedObject, Vector<RepositoryFolder> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws FlexoException {
		if (getFolder() == null) {
			logger.warning("Cannot move: null folder");
			return;
		}
		for (RepositoryFolder v : getGlobalSelection()) {
			if (isFolderMovableTo(v, folder)) {
				moveToFolder(v, folder);
			}
		}
	}

	private void moveToFolder(RepositoryFolder folderToMove, RepositoryFolder newFatherFolder) {
		if (isFolderMovableTo(folderToMove, newFatherFolder)) {
			RepositoryFolder oldFolder = folderToMove.getParentFolder();
			// Hack: we have first to load the view, to prevent a null value returned by FlexoViewResource.getSchemaDefinition()
			oldFolder.removeFromChildren(folderToMove);
			newFatherFolder.addToChildren(folderToMove);
		}
	}

	public RepositoryFolder getFolder() {
		return folder;
	};

	public void setFolder(RepositoryFolder folder) {
		this.folder = folder;
	}

	public static boolean isFolderMovableTo(RepositoryFolder folderToMove, RepositoryFolder newLocation) {
		return folderToMove != newLocation && !folderToMove.isFatherOf(newLocation);
	}
}
