/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-rt-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.rt.controller.widget;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.components.widget.FIBFlexoObjectSelector;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resource;

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

	public static Resource FIB_FILE = ResourceLocator.locateResource("Fib/ViewFolderSelector.fib");

	private FlexoProject project;

	public FIBViewFolderSelector(RepositoryFolder editedObject) {
		super(editedObject);
	}


	@Override
	public Resource getFIBResource() {
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
