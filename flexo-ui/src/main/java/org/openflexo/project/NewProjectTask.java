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

package org.openflexo.project;

import java.io.File;
import java.io.IOException;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.task.FlexoApplicationTask;

/**
 * A task used to create a new Flexo project
 * 
 * @author sylvain
 *
 */
public class NewProjectTask extends FlexoApplicationTask {
	/**
	 * 
	 */
	private final InteractiveProjectLoader projectLoader;
	private final File projectDirectory;
	private final Class<? extends ProjectNature> projectNatureClass;
	private FlexoEditor flexoEditor;

	public NewProjectTask(InteractiveProjectLoader projectLoader, File projectDirectory) {
		this(projectLoader, projectDirectory, null);
	}

	public NewProjectTask(InteractiveProjectLoader projectLoader, File projectDirectory,
			Class<? extends ProjectNature> projectNatureClass) {
		super("NewProject", FlexoLocalization.getMainLocalizer().localizedForKey("new_project") + " " + projectDirectory.getName(),
				projectLoader.getServiceManager());
		this.projectLoader = projectLoader;
		this.projectDirectory = projectDirectory;
		this.projectNatureClass = projectNatureClass;
	}

	@Override
	public void performTask() {

		Progress.setExpectedProgressSteps(10);

		try {
			projectLoader.newStandaloneProject(projectDirectory, projectNatureClass);
		} catch (ProjectInitializerException e) {
			throwException(e);
		} catch (IOException e) {
			throwException(e);
		}

		/*Progress.setExpectedProgressSteps(100);
		
		// This will just create the .version in the project
		FlexoProjectUtil.currentFlexoVersionIsSmallerThanLastVersion(projectDirectory);
		
		projectLoader.preInitialization(projectDirectory);
		
		if (projectDirectory.exists()) {
			// We should have already asked the user if the new project has to override the old one
			// so we really delete the old project
		
			File backupProject = new File(projectDirectory.getParentFile(), projectDirectory.getName() + "~");
			if (backupProject.exists()) {
				FileUtils.recursiveDeleteFile(backupProject);
			}
		
			try {
				FileUtils.rename(projectDirectory, backupProject);
			} catch (IOException e) {
				throwException(e);
			}
		}
		
		try {
			flexoEditor = FlexoProject.newProject(projectDirectory, projectLoader.getServiceManager(), projectLoader.getServiceManager(),
					null);
		} catch (ProjectInitializerException e) {
			throwException(e);
		}
		
		projectLoader.newEditor(flexoEditor);
		
		projectLoader.addToRootProjects(flexoEditor.getProject());
		
		// Now, if a nature has been supplied, gives this nature to the project
		if (projectNature != null) {
			projectNature.givesNature(flexoEditor.getProject(), flexoEditor);
		}
		
		// Notify project just loaded
		projectLoader.getServiceManager().notify(projectLoader, new ProjectLoaded(flexoEditor.getProject()));
		*/
	}

	public FlexoEditor getFlexoEditor() {
		return flexoEditor;
	}

	@Override
	public boolean isCancellable() {
		return true;
	}

	@Override
	protected void notifyThrownException(Exception e) {

		showException(FlexoLocalization.getMainLocalizer().localizedForKey("could_not_create_project"),
				FlexoLocalization.getMainLocalizer().localizedForKey("could_not_create_project_located_at")
						+ projectDirectory.getAbsolutePath(),
				e);
		e.printStackTrace();
	}
}
