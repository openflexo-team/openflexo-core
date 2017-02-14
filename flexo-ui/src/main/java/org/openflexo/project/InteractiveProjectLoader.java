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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.ProgressWindow;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.ProjectLoader;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.resource.SaveResourceExceptionList;
import org.openflexo.foundation.resource.SaveResourcePermissionDeniedException;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.InteractiveFlexoEditor;

/**
 * Extends {@link ProjectLoader} service by providing interactive features in interactive context<br>
 * This service provides auto-save features.<br>
 * This service provides asynchronous calls to new/laod/close projects.
 * 
 * @author sylvain
 *
 */
public class InteractiveProjectLoader extends ProjectLoader {

	private static final Logger logger = Logger.getLogger(InteractiveProjectLoader.class.getPackage().getName());

	protected final Map<FlexoProject, AutoSaveService> autoSaveServices;

	public InteractiveProjectLoader() {
		super();
		autoSaveServices = new HashMap<FlexoProject, AutoSaveService>();
	}

	@Override
	public ApplicationContext getServiceManager() {
		return (ApplicationContext) super.getServiceManager();
	}

	/**
	 * Loads the project located withing <code> projectDirectory </code>. The following method is the default methode to call when opening a
	 * project from a GUI (Interactive mode) so that resource update handling is properly initialized. Additional small stuffs can be
	 * performed in that call so that projects are always opened the same way.
	 * 
	 * @param projectDirectory
	 *            the project directory
	 * @return the {@link InteractiveFlexoEditor} editor if the opening succeeded else <code>null</code>
	 * @throws org.openflexo.foundation.utils.ProjectLoadingCancelledException
	 *             whenever the load procedure is interrupted by the user or by Flexo.
	 * @throws ProjectInitializerException
	 */
	@Override
	public LoadProjectTask loadProject(File projectDirectory, boolean asImportedProject, FlexoTask... tasksToBeExecutedBefore) {

		LoadProjectTask loadProject = new LoadProjectTask(this, projectDirectory, asImportedProject);
		for (FlexoTask task : tasksToBeExecutedBefore) {
			loadProject.addToDependantTasks(task);
		}
		getServiceManager().getTaskManager().scheduleExecution(loadProject);
		return loadProject;

	}

	@Override
	public LoadProjectTask loadProject(File projectDirectory, FlexoTask... tasksToBeExecutedBefore) {
		return loadProject(projectDirectory, false, tasksToBeExecutedBefore);
	}

	@Override
	public LoadProjectTask reloadProject(FlexoProject project) {
		File projectDirectory = project.getProjectDirectory();
		closeProject(project);
		return loadProject(projectDirectory);
	}

	@Override
	public NewProjectTask newProject(File projectDirectory, FlexoTask... tasksToBeExecutedBefore) {
		return newProject(projectDirectory, null, tasksToBeExecutedBefore);
	}

	@Override
	public NewProjectTask newProject(File projectDirectory, ProjectNature<?, ?> projectNature, FlexoTask... tasksToBeExecutedBefore) {

		NewProjectTask returned = new NewProjectTask(this, projectDirectory, projectNature);
		for (FlexoTask task : tasksToBeExecutedBefore) {
			returned.addToDependantTasks(task);
		}
		getServiceManager().getTaskManager().scheduleExecution(returned);
		return returned;

	}

	@Override
	protected void newEditor(FlexoEditor editor) {
		if (getServiceManager().isAutoSaveServiceEnabled()) {
			autoSaveServices.put(editor.getProject(), new AutoSaveService(this, editor.getProject()));
		}
		super.newEditor(editor);
	}

	@Override
	public void closeProject(FlexoProject project) {
		AutoSaveService autoSaveService = getAutoSaveService(project);
		if (autoSaveService != null) {
			autoSaveService.close();
			autoSaveServices.remove(project);
		}
		super.closeProject(project);
	}

	public AutoSaveService getAutoSaveService(FlexoProject project) {
		return autoSaveServices.get(project);
	}

	@Override
	public void saveProjects(List<FlexoProject> projects) throws SaveResourceExceptionList {
		List<SaveResourceException> exceptions = new ArrayList<SaveResourceException>();
		Collections.sort(projects, new Comparator<FlexoProject>() {
			@Override
			public int compare(FlexoProject o1, FlexoProject o2) {
				if (o1.importsProject(o2)) {
					return 1;
				}
				else if (o2.importsProject(o1)) {
					return -1;
				}
				return 0;
			}
		});
		try {
			ProgressWindow.showProgressWindow(FlexoLocalization.getMainLocalizer().localizedForKey("saving"), projects.size());
			for (FlexoProject project : projects) {
				try {
					ProgressWindow.setProgressInstance(
							FlexoLocalization.getMainLocalizer().localizedForKey("saving") + " " + project.getDisplayName());
					project.save(ProgressWindow.instance());
				} catch (SaveResourceException e) {
					e.printStackTrace();
					exceptions.add(e);
				}
			}
			if (exceptions.size() > 0) {
				throw new SaveResourceExceptionList(exceptions);
			}
		} finally {
			ProgressWindow.hideProgressWindow();
		}
	}

	static void informUserAboutSaveResourceException(SaveResourceException e) {
		if (e instanceof SaveResourcePermissionDeniedException) {
			informUserAboutPermissionDeniedException((SaveResourcePermissionDeniedException) e);
		}
		else {
			FlexoController.showError(FlexoLocalization.getMainLocalizer().localizedForKey("error_during_saving"));
		}
		logger.warning("Exception raised: " + e.getClass().getName() + ". See console for details.");
		logger.warning(e.getMessage());
		e.printStackTrace();
	}

	private static void informUserAboutPermissionDeniedException(SaveResourcePermissionDeniedException e) {
		if (e instanceof FileIODelegate && ((FileIODelegate) e).getFile().isDirectory()) {
			FlexoController.showError(FlexoLocalization.getMainLocalizer().localizedForKey("permission_denied"),
					FlexoLocalization.getMainLocalizer().localizedForKey("project_was_not_properly_saved_permission_denied_directory")
							+ "\n" + e.toString());
		}
		else if (e instanceof FileIODelegate) {
			FlexoController.showError(FlexoLocalization.getMainLocalizer().localizedForKey("permission_denied"),
					FlexoLocalization.getMainLocalizer().localizedForKey("project_was_not_properly_saved_permission_denied_file") + "\n"
							+ e.toString());
		}
		else {
			FlexoController.showError(FlexoLocalization.getMainLocalizer().localizedForKey("permission_denied"),
					FlexoLocalization.getMainLocalizer().localizedForKey("project_was_not_properly_saved_permission_denied") + "\n"
							+ e.toString());
		}
	}

	@Override
	public void preInitialization(File projectDirectory) {
		getServiceManager().getGeneralPreferences().addToLastOpenedProjects(projectDirectory);
		getServiceManager().getPreferencesService().savePreferences();
	}

	@Override
	public void initialize() {
		super.initialize();
	}

}
