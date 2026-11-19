/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.resource.RepositoryFolder;

/**
 * Closes a {@link FlexoProject}: a GUI action, available on the project and on its root folder (contextual menu) as well as from the File menu. Nothing is saved.
 * The initializer in the user interface closes the project through the project loader, which owns the editors and the root projects.
 * 
 * @author sylvain
 */
public class CloseProject extends FlexoGUIAction<CloseProject, FlexoObject, FlexoObject> {

	public static final FlexoActionFactory<CloseProject, FlexoObject, FlexoObject> actionType = new FlexoActionFactory<CloseProject, FlexoObject, FlexoObject>(
			"close_project", FlexoActionFactory.defaultGroup) {

		@Override
		public CloseProject makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new CloseProject(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return isClosable(ProjectActionSupport.projectOf(object));
		}

		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return isClosable(ProjectActionSupport.projectOf(object));
		}

		/**
		 * Only a root project of the project loader can be closed: a project imported by another one is closed with it
		 */
		private boolean isClosable(FlexoProject<?> project) {
			return project != null && project.getServiceManager() != null && project.getServiceManager().getProjectLoaderService() != null
					&& project.getServiceManager().getProjectLoaderService().getRootProjects().contains(project);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(actionType, FlexoProject.class);
		FlexoObjectImpl.addActionForClass(actionType, RepositoryFolder.class);
	}

	private CloseProject(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	public FlexoProject<?> getProject() {
		return ProjectActionSupport.projectOf(getFocusedObject());
	}

}
