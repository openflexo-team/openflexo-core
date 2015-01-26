/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexodocresourcemanager, a component of the software infrastructure 
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

package org.openflexo.drm.action;

import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.drm.DocItemFolder;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;

/**
 * @author gpolet
 * 
 */
public class DeleteDocItemFolder extends FlexoAction {

	private static final Logger logger = Logger.getLogger(DeleteDocItemFolder.class.getPackage().getName());

	public static FlexoActionType actionType = new FlexoActionType("delete", FlexoActionType.editGroup, FlexoActionType.DELETE_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public FlexoAction makeNewAction(FlexoObject focusedObject, Vector globalSelection, FlexoEditor editor) {
			return new DeleteDocItemFolder(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector globalSelection) {
			return object != null || globalSelection != null && globalSelection.size() > 0;
		}

	};

	DeleteDocItemFolder(FlexoObject focusedObject, Vector globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	private Vector folders;

	/**
	 * Overrides doAction
	 * 
	 * @see org.openflexo.foundation.action.FlexoAction#doAction(java.lang.Object)
	 */
	@Override
	protected void doAction(Object context) throws FlexoException {
		if (folders != null) {
			Enumeration en = folders.elements();
			while (en.hasMoreElements()) {
				DocItemFolder folder = (DocItemFolder) en.nextElement();
				folder.delete();
			}
		} else if (logger.isLoggable(Level.WARNING)) {
			logger.warning("Tried to delete doc folders, but folders was not set.");
		}
	}

	public Vector getFolders() {
		return folders;
	}

	public void setFolders(Vector folders) {
		this.folders = folders;
	}

}
