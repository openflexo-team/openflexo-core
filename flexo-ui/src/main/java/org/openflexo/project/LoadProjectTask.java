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
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.task.FlexoApplicationTask;
import org.openflexo.view.controller.FlexoController;

/**
 * A task used to load a Flexo project
 * 
 * @author sylvain
 *
 */
public class LoadProjectTask extends FlexoApplicationTask {

	private static final Logger logger = Logger.getLogger(InteractiveProjectLoader.class.getPackage().getName());

	protected final InteractiveProjectLoader projectLoader;
	protected final File projectDirectory;
	private FlexoEditor flexoEditor;
	protected final boolean asImportedProject;

	public LoadProjectTask(InteractiveProjectLoader projectLoader, File projectDirectory, boolean asImportedProject) {
		super("LoadProject", FlexoLocalization.getMainLocalizer().localizedForKey("loading_project") + " " + projectDirectory.getName(),
				projectLoader.getServiceManager());
		this.projectLoader = projectLoader;
		this.projectDirectory = projectDirectory;
		this.asImportedProject = asImportedProject;
	}

	@Override
	public void performTask() {

		// System.out.println("Loading project " + projectDirectory);

		Progress.setExpectedProgressSteps(10);

		try {
			projectLoader.loadProject(projectDirectory);
		} catch (ProjectLoadingCancelledException e) {
			throwException(e);
		} catch (ProjectInitializerException e) {
			throwException(e);
		}

		/*if (projectDirectory == null) {
			throwException(new IllegalArgumentException("Project directory cannot be null"));
		}
		if (!projectDirectory.exists()) {
			throwException(new ProjectInitializerException("project directory does not exist", projectDirectory));
		}
		try {
			FlexoProjectUtil.isProjectOpenable(projectDirectory);
		} catch (UnreadableProjectException e) {
			throwException(new ProjectLoadingCancelledException(e.getMessage()));
		}
		
		Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("opening_project") + projectDirectory.getAbsolutePath());
		
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Opening " + projectDirectory.getAbsolutePath());
		}
		if (!asImportedProject) {
			// Adds to recent project
			Progress.progress(
					FlexoLocalization.getMainLocalizer().localizedForKey("preinitialize_project") + projectDirectory.getAbsolutePath());
			projectLoader.preInitialization(projectDirectory);
		}
		for (Entry<FlexoProject, FlexoEditor> e : projectLoader.getEditors().entrySet()) {
			if (e.getKey().getProjectDirectory().equals(projectDirectory)) {
				flexoEditor = e.getValue();
			}
		}
		if (flexoEditor == null) {
			try {
				flexoEditor = FlexoProject.openProject(projectDirectory, projectLoader.getServiceManager(),
						projectLoader.getServiceManager(), ProgressWindow.instance());
			} catch (ProjectLoadingCancelledException e1) {
				throwException(e1);
			} catch (ProjectInitializerException e1) {
				throwException(e1);
			}
			Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("create_and_open_editor"));
			projectLoader.newEditor(flexoEditor);
		}
		if (!asImportedProject) {
			projectLoader.addToRootProjects(flexoEditor.getProject());
		}
		
		// Notify project just loaded
		Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("notify_editors"));
		projectLoader.getServiceManager().notify(projectLoader, new ProjectLoaded(flexoEditor.getProject()));*/
	}

	public FlexoEditor getFlexoEditor() {
		return flexoEditor;
	}

	@Override
	public boolean isCancellable() {
		return true;
	}

	@Override
	public void throwException(Exception e) {
		super.throwException(e);

		FlexoController.notify(FlexoLocalization.getMainLocalizer().localizedForKey("could_not_open_project_located_at")
				+ projectDirectory.getAbsolutePath());
	}
}
