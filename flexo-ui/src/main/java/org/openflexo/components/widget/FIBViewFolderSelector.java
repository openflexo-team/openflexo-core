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
package org.openflexo.components.widget;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.toolbox.FileResource;

/**
 * Widget allowing to select a View folder
 * 
 * @author sguerin
 * 
 *         DEPRECATED BECAUSE NOT IMPLEMENTED YET!
 */
@Deprecated
@SuppressWarnings("serial")
public class FIBViewFolderSelector extends FIBFlexoObjectSelector<RepositoryFolder> {

	static final Logger logger = Logger.getLogger(FIBViewFolderSelector.class.getPackage().getName());

	public static FileResource FIB_FILE = new FileResource("Fib/ViewFolderSelector.fib");

	private FlexoProject project;

	public FIBViewFolderSelector(RepositoryFolder editedObject) {
		super(editedObject);
	}

	@Override
	public File getFIBFile() {
		return FIB_FILE;
	}

	@Override
	public Class<RepositoryFolder> getRepresentedType() {
		return RepositoryFolder.class;
	}

	@Override
	public String renderedString(RepositoryFolder editedObject) {
		if (editedObject != null) {
			return editedObject.getName();
		}
		return "";
	}

	public FlexoProject getProject() {
		return project;
	}

	@CustomComponentParameter(name = "project", type = CustomComponentParameter.Type.MANDATORY)
	public void setProject(FlexoProject project) {
		if (this.project != project) {
			FlexoProject oldProject = this.project;
			if (project == null) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Passing null project. If you rely on project this is unlikely to work");
				}
			}
			System.out.println(">>>>>>>>> Sets project with " + project);
			this.project = project;
			getPropertyChangeSupport().firePropertyChange("project", oldProject, project);
		}
	}

	// Please uncomment this for a live test
	// Never commit this uncommented since it will not compile on continuous build
	// To have icon, you need to choose "Test interface" in the editor (otherwise, flexo controller is not insanciated in EDIT mode)
	/*public static void main(String[] args) {
		FIBAbstractEditor editor = new FIBAbstractEditor() {
			@Override
			public Object[] getData() {
				FlexoEditor editor = ProjectDialogEDITOR.loadProject(new FileResource("Prj/TestVE.prj"));
				FlexoProject project = editor.getProject();
				FIBViewSelector selector = new FIBViewSelector(null);
				selector.setProject(project);
				return makeArray(selector);
			}

			@Override
			public File getFIBFile() {
				return FIB_FILE;
			}

			@Override
			public FIBController makeNewController(FIBComponent component) {
				return new FlexoFIBController<FIBViewSelector>(component);
			}
		};
		editor.launch();
	}*/

}