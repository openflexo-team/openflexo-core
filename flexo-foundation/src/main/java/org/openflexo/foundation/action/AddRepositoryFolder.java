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
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceRepository;

public class AddRepositoryFolder extends FlexoAction<AddRepositoryFolder, RepositoryFolder, RepositoryFolder> {

	private static final Logger logger = Logger.getLogger(AddRepositoryFolder.class.getPackage().getName());

	public static FlexoActionFactory<AddRepositoryFolder, RepositoryFolder, RepositoryFolder> actionType = new FlexoActionFactory<AddRepositoryFolder, RepositoryFolder, RepositoryFolder>(
			"create_folder", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public AddRepositoryFolder makeNewAction(RepositoryFolder focusedObject, Vector<RepositoryFolder> globalSelection,
				FlexoEditor editor) {
			return new AddRepositoryFolder(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(RepositoryFolder object, Vector<RepositoryFolder> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(RepositoryFolder object, Vector<RepositoryFolder> globalSelection) {
			return true;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(AddRepositoryFolder.actionType, RepositoryFolder.class);
	}

	private RepositoryFolder<?, ?> newFolder;
	private String newFolderName;

	private AddRepositoryFolder(RepositoryFolder<?, ?> focusedObject, Vector<RepositoryFolder> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws InvalidParametersException {
		logger.info("Add view folder");
		if (getFocusedObject() != null) {
			ResourceRepository<?, ?> rr = getFocusedObject().getResourceRepository();
			newFolder = rr.createNewFolder(getNewFolderName(), getFocusedObject());
		}
		else {
			throw new InvalidParametersException("unable to create view folder: no focused object supplied");
		}
	}

	public String getNewFolderName() {
		return newFolderName;
	}

	public void setNewFolderName(String newFolderName) {
		this.newFolderName = newFolderName;
	}

	public RepositoryFolder getNewFolder() {
		return newFolder;
	}

}
