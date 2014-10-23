/*
 * (c) Copyright 2014-2015 Openflexo
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
package org.openflexo.project;

import java.io.File;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.components.ProgressWindow;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.resource.ProjectLoaded;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.utils.FlexoProjectUtil;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.foundation.utils.UnreadableProjectException;
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

	private static final Logger logger = Logger.getLogger(ProjectLoader.class.getPackage().getName());

	private final ProjectLoader projectLoader;
	private final File projectDirectory;
	private FlexoEditor flexoEditor;
	private final boolean asImportedProject;

	public LoadProjectTask(ProjectLoader projectLoader, File projectDirectory, boolean asImportedProject) {
		super(FlexoLocalization.localizedForKey("loading_project") + " " + projectDirectory.getName(), projectLoader);
		this.projectLoader = projectLoader;
		this.projectDirectory = projectDirectory;
		this.asImportedProject = asImportedProject;
	}

	@Override
	public void performTask() {
		Progress.setExpectedProgressSteps(10);
		if (projectDirectory == null) {
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

		Progress.progress(FlexoLocalization.localizedForKey("opening_project") + projectDirectory.getAbsolutePath());

		FlexoEditor editor = null;

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Opening " + projectDirectory.getAbsolutePath());
		}
		if (!asImportedProject) {
			// Adds to recent project
			Progress.progress(FlexoLocalization.localizedForKey("preinitialize_project") + projectDirectory.getAbsolutePath());
			projectLoader.preInitialization(projectDirectory);
		}
		for (Entry<FlexoProject, FlexoEditor> e : projectLoader.editors.entrySet()) {
			if (e.getKey().getProjectDirectory().equals(projectDirectory)) {
				editor = e.getValue();
			}
		}
		if (editor == null) {
			try {
				editor = FlexoProject.openProject(projectDirectory, projectLoader.getServiceManager(), projectLoader.getServiceManager(),
						ProgressWindow.instance());
			} catch (ProjectLoadingCancelledException e1) {
				throwException(e1);
			} catch (ProjectInitializerException e1) {
				throwException(e1);
			}
			Progress.progress(FlexoLocalization.localizedForKey("create_and_open_editor"));
			projectLoader.newEditor(editor);
		}
		if (!asImportedProject) {
			projectLoader.addToRootProjects(editor.getProject());
		}

		// Notify project just loaded
		Progress.progress(FlexoLocalization.localizedForKey("notify_editors"));
		projectLoader.getServiceManager().notify(projectLoader, new ProjectLoaded(editor.getProject()));
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
		// TODO Auto-generated method stub
		super.throwException(e);

		FlexoController.notify(FlexoLocalization.localizedForKey("could_not_open_project_located_at") + projectDirectory.getAbsolutePath());
	}
}