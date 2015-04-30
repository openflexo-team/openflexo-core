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

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.ProgressWindow;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.FlexoProjectReference;
import org.openflexo.foundation.resource.ProjectClosed;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.resource.SaveResourceExceptionList;
import org.openflexo.foundation.resource.SaveResourcePermissionDeniedException;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.InteractiveFlexoEditor;

public class ProjectLoader extends FlexoServiceImpl implements HasPropertyChangeSupport, FlexoService {

	private static final Logger logger = Logger.getLogger(ProjectLoader.class.getPackage().getName());

	public static final String PROJECT_OPENED = "projectOpened";
	public static final String PROJECT_CLOSED = "projectClosed";

	private static final String FOR_FLEXO_SERVER = "_forFlexoServer_";
	public static final String EDITOR_ADDED = "editorAdded";
	public static final String EDITOR_REMOVED = "editorRemoved";
	public static final String ROOT_PROJECTS = "rootProjects";

	protected final Map<FlexoProject, FlexoEditor> editors;

	private final Map<FlexoProject, AutoSaveService> autoSaveServices;

	private final PropertyChangeSupport propertyChangeSupport;
	private final List<FlexoProject> rootProjects;
	private final ModelFactory modelFactory;

	public ProjectLoader() throws ModelDefinitionException {
		this.rootProjects = new ArrayList<FlexoProject>();
		this.editors = new LinkedHashMap<FlexoProject, FlexoEditor>();
		this.propertyChangeSupport = new PropertyChangeSupport(this);
		autoSaveServices = new HashMap<FlexoProject, AutoSaveService>();
		modelFactory = new ModelFactory(FlexoProjectReference.class);
	}

	@Override
	public ApplicationContext getServiceManager() {
		return (ApplicationContext) super.getServiceManager();
	}

	public FlexoEditor getEditorForProject(FlexoProject project) {
		return editors.get(project);
	}

	public FlexoEditor editorForProjectURIAndRevision(String projectURI, long revision) {
		for (Entry<FlexoProject, FlexoEditor> e : editors.entrySet()) {
			if (e.getKey().getProjectURI().equals(projectURI) && e.getKey().getRevision() == revision) {
				return e.getValue();
			}
		}
		return null;
	}

	public boolean hasEditorForProjectDirectory(File projectDirectory) {
		if (projectDirectory == null) {
			return false;
		}
		for (Entry<FlexoProject, FlexoEditor> e : editors.entrySet()) {
			if (e.getKey().getProjectDirectory().equals(projectDirectory)) {
				return true;
			}
		}
		return false;
	}

	public LoadProjectTask loadProject(File projectDirectory, FlexoTask... tasksToBeExecutedBefore) {
		return loadProject(projectDirectory, false, tasksToBeExecutedBefore);
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
	public LoadProjectTask loadProject(File projectDirectory, boolean asImportedProject, FlexoTask... tasksToBeExecutedBefore) {

		LoadProjectTask loadProject = new LoadProjectTask(this, projectDirectory, asImportedProject);
		for (FlexoTask task : tasksToBeExecutedBefore) {
			loadProject.addToDependantTasks(task);
		}
		getServiceManager().getTaskManager().scheduleExecution(loadProject);
		return loadProject;

	}

	public LoadProjectTask reloadProject(FlexoProject project) {
		File projectDirectory = project.getProjectDirectory();
		closeProject(project);
		return loadProject(projectDirectory);
	}

	public NewProjectTask newProject(File projectDirectory, FlexoTask... tasksToBeExecutedBefore) {
		return newProject(projectDirectory, null, tasksToBeExecutedBefore);
	}

	public NewProjectTask newProject(File projectDirectory, ProjectNature<?, ?> projectNature, FlexoTask... tasksToBeExecutedBefore) {

		NewProjectTask returned = new NewProjectTask(this, projectDirectory, projectNature);
		for (FlexoTask task : tasksToBeExecutedBefore) {
			returned.addToDependantTasks(task);
		}
		getServiceManager().getTaskManager().scheduleExecution(returned);
		return returned;

	}

	protected void newEditor(FlexoEditor editor) {
		editors.put(editor.getProject(), editor);
		if (getServiceManager().isAutoSaveServiceEnabled()) {
			autoSaveServices.put(editor.getProject(), new AutoSaveService(this, editor.getProject()));
		}
		getPropertyChangeSupport().firePropertyChange(EDITOR_ADDED, null, editor);
		try {
			FlexoProjectReference ref = modelFactory.newInstance(FlexoProjectReference.class);
			ref.init(editor.getProject());
			// applicationContext.getResourceCenterService().getUserResourceCenter()
			// .publishResource(ref, editor.getProject().getVersion(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (FlexoProject project : new ArrayList<FlexoProject>(editors.keySet())) {
			if (project.getProjectData() != null) {
				for (FlexoProjectReference reference : project.getProjectData().getImportedProjects()) {
					reference.getReferredProject(false);
				}
			}
		}
	}

	public void closeProject(FlexoProject project) {
		AutoSaveService autoSaveService = getAutoSaveService(project);
		if (autoSaveService != null) {
			autoSaveService.close();
			autoSaveServices.remove(project);
		}
		FlexoEditor editor = editors.remove(project);
		if (project != null) {
			project.close();
			removeFromRootProjects(project);
			getPropertyChangeSupport().firePropertyChange(EDITOR_REMOVED, editor, null);
		}
		getServiceManager().notify(this, new ProjectClosed(project));
	}

	public AutoSaveService getAutoSaveService(FlexoProject project) {
		return autoSaveServices.get(project);
	}

	public void saveProjectForServer(FlexoProject project) {
		/*final String zipFilename = project.getProjectDirectory().getName()
				.substring(0, project.getProjectDirectory().getName().length() - 4);
		String zipFileNameProposal = zipFilename + FOR_FLEXO_SERVER + new FlexoVersion(1, 0, 0, -1, false, false);
		File[] zips = project.getProjectDirectory().getParentFile().listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().toLowerCase().endsWith(".zip") && pathname.getName().startsWith(zipFilename);
			}
		});
		File previousVersion = null;
		if (zips != null) {
			for (File file : zips) {
				if (previousVersion == null || previousVersion.lastModified() < file.lastModified()) {
					previousVersion = file;
				}
			}
		}
		if (previousVersion != null && previousVersion.getName().indexOf(FOR_FLEXO_SERVER) > -1) {
			String version = previousVersion.getName()
					.substring(previousVersion.getName().indexOf(FOR_FLEXO_SERVER) + FOR_FLEXO_SERVER.length(),
							previousVersion.getName().length() - 4);
			if (FlexoVersion.isValidVersionString(version)) {
				FlexoVersion v = new FlexoVersion(version);
				v.minor++;
				zipFileNameProposal = zipFilename + FOR_FLEXO_SERVER + v;
			}
		}
		final FileParameter targetZippedProject = new FileParameter("targetZippedProject", "new_zip_file", new File(project
				.getProjectDirectory().getParentFile(), zipFileNameProposal + ".zip")) {
			@Override
			public void setValue(File value) {
				if (!value.getName().endsWith(".zip")) {
					value = new File(value.getParentFile(), value.getName() + ".zip");
				}
				super.setValue(value);
			}
		};
		targetZippedProject.setDepends("targetZippedProject");
		targetZippedProject.addParameter(FileEditWidget.TITLE, "select_a_zip_file");
		targetZippedProject.addParameter(FileEditWidget.FILTER, "*.zip");
		targetZippedProject.addParameter(FileEditWidget.MODE, FileEditWidget.SAVE);
		CheckboxParameter removeScreenshotsAndLibraries = new CheckboxParameter("lighten", "remove_screenshots_and_libs", true);

		AskParametersDialog dialog = AskParametersDialog.createAskParametersDialog(project, null,
				FlexoLocalization.localizedForKey("save_project_as"), FlexoLocalization.localizedForKey("select_a_zip_file_for_project"),
				new AskParametersDialog.ValidationCondition() {
					@Override
					public boolean isValid(ParametersModel model) {
						if (targetZippedProject.getValue() == null) {
							errorMessage = FlexoLocalization.localizedForKey("please_submit_a_zip");
							return false;
						}
						return true;
					}
				}, targetZippedProject, removeScreenshotsAndLibraries);

		System.setProperty("apple.awt.fileDialogForDirectories", "false");
		if (dialog.getStatus() == AskParametersDialog.VALIDATE) {
			File zipFile = targetZippedProject.getValue();
			if (zipFile == null) {
				return;
			}
			if (!zipFile.exists()) {
				try {
					FileUtils.createNewFile(zipFile);
				} catch (IOException e1) {
					e1.printStackTrace();
					FlexoController.notify(FlexoLocalization.localizedForKey("could_not_save_permission_denied"));
					return;
				}
			} else {
				if (!FlexoController.confirm(FlexoLocalization.localizedForKey("file_already_exists.replace_it?"))) {
					return;
				}
			}
			if (!zipFile.canWrite()) {
				FlexoController.notify(FlexoLocalization.localizedForKey("could_not_save_permission_denied"));
				return;
			}
			try {
				ProgressWindow.showProgressWindow(FlexoLocalization.localizedForKey("saving"), removeScreenshotsAndLibraries.getValue() ? 5
						: 2);
				project.saveAsZipFile(zipFile, ProgressWindow.instance(), removeScreenshotsAndLibraries.getValue(), true);
				ProgressWindow.hideProgressWindow();
			} catch (SaveResourceException e) {
				e.printStackTrace();
				ProgressWindow.hideProgressWindow();
				FlexoController.notify(FlexoLocalization.localizedForKey("save_as_operation_failed"));
			}
		}*/
	}

	public void saveAsProject(FlexoProject project) {
		logger.warning("Not implemented yet");
		// TODO
		/*project.getXmlMappings();
		List<FlexoVersion> availableVersions = new ArrayList<FlexoVersion>(XMLSerializationService.getReleaseVersions());
		Collections.sort(availableVersions, Collections.reverseOrder(FlexoVersion.comparator));

		final DirectoryParameter targetPrjDirectory = new DirectoryParameter("targetPrjDirectory", "new_project_file", project
				.getProjectDirectory().getParentFile()) {
			@Override
			public void setValue(File value) {
				if (!value.getName().endsWith(".prj")) {
					value = new File(value.getParentFile(), value.getName() + ".prj");
				}
				super.setValue(value);
			}
		};
		targetPrjDirectory.setDepends("targetPrjDirectory");
		targetPrjDirectory.addParameter(FileEditWidget.TITLE, "select_a_prj_directory");
		targetPrjDirectory.addParameter(FileEditWidget.FILTER, "*.prj");
		targetPrjDirectory.addParameter(FileEditWidget.MODE, FileEditWidget.SAVE);
		final DynamicDropDownParameter<FlexoVersion> versionParam = new DynamicDropDownParameter<FlexoVersion>("version", "version",
				availableVersions, availableVersions.get(0));
		versionParam.setShowReset(false);

		AskParametersDialog dialog = AskParametersDialog.createAskParametersDialog(project, null,
				FlexoLocalization.localizedForKey("save_project_as"),
				FlexoLocalization.localizedForKey("enter_parameters_for_project_saving"), new AskParametersDialog.ValidationCondition() {
					@Override
					public boolean isValid(ParametersModel model) {
						if (versionParam.getValue() == null) {
							errorMessage = FlexoLocalization.localizedForKey("please_submit_a_version");
							return false;
						}
						if (targetPrjDirectory.getValue() == null) {
							errorMessage = FlexoLocalization.localizedForKey("please_submit_a_prj_directory");
							return false;
						}
						if (!(targetPrjDirectory.getValue().getName().endsWith(".prj") && !targetPrjDirectory.getValue().exists())) {
							errorMessage = FlexoLocalization.localizedForKey("please_submit_a_valid_prj_directory");
							return false;
						}
						return true;
					}
				}, targetPrjDirectory, versionParam);

		System.setProperty("apple.awt.fileDialogForDirectories", "false");
		if (dialog.getStatus() == AskParametersDialog.VALIDATE) {
			File projectDirectory = targetPrjDirectory.getValue();
			if (projectDirectory == null) {
				return;
			} else if (!projectDirectory.exists()) {
				if (!projectDirectory.mkdirs()) {
					FlexoController.notify(FlexoLocalization.localizedForKey("could_not_create_prj_directory"));
					return;
				}
			}
			if (!projectDirectory.canWrite()) {
				FlexoController.notify(FlexoLocalization.localizedForKey("could_not_save_permission_denied"));
				return;
			}
			try {
				ProgressWindow.showProgressWindow(FlexoLocalization.localizedForKey("saving"), 1);
				project.saveAs(projectDirectory, ProgressWindow.instance(),
						FlexoCst.BUSINESS_APPLICATION_VERSION.equals(versionParam.getValue()) ? null : versionParam.getValue(), true, true);
				GeneralPreferences.addToLastOpenedProjects(projectDirectory);
				ProgressWindow.hideProgressWindow();
			} catch (SaveResourceException e) {
				e.printStackTrace();
				ProgressWindow.hideProgressWindow();
				FlexoController.notify(FlexoLocalization.localizedForKey("save_as_operation_failed"));
			}
		}*/
	}

	public List<FlexoProject> getModifiedProjects() {
		List<FlexoProject> projects = new ArrayList<FlexoProject>(editors.size());
		// 1. compute all modified projects
		for (FlexoProject project : getRootProjects()) {
			if (project.hasUnsavedResources()) {
				projects.add(project);
			}
		}
		// 2. we now add all the projects that depend on a modified project
		// to the list of modified projects (so that they also get saved
		for (FlexoProject modifiedProject : new ArrayList<FlexoProject>(projects)) {
			for (FlexoProject project : getRootProjects()) {
				if (project.importsProject(modifiedProject)) {
					if (!projects.contains(project)) {
						projects.add(project);
					}
				}
			}
		}
		// 3. We restore the order of projects according to the one in rootProjects
		Collections.sort(projects, new Comparator<FlexoProject>() {

			@Override
			public int compare(FlexoProject o1, FlexoProject o2) {
				return rootProjects.indexOf(o1) - rootProjects.indexOf(o2);
			}
		});
		return projects;
	}

	public List<FlexoProject> getRootProjects() {
		return rootProjects;
	}

	protected void addToRootProjects(FlexoProject project) {
		if (!rootProjects.contains(project)) {
			rootProjects.add(project);
			getPropertyChangeSupport().firePropertyChange(PROJECT_OPENED, null, project);
			getPropertyChangeSupport().firePropertyChange(ROOT_PROJECTS, null, project);
		}
	}

	private void removeFromRootProjects(FlexoProject project) {
		rootProjects.remove(project);
		getPropertyChangeSupport().firePropertyChange(PROJECT_CLOSED, project, null);
		getPropertyChangeSupport().firePropertyChange(ROOT_PROJECTS, project, null);
	}

	public void saveAllProjects() throws SaveResourceExceptionList {
		// Saves all projects. It is necessary to save all projects because during serialization, a project may increment its revision which
		// in turn can modify project that import the former one.
		List<FlexoProject> projects = new ArrayList<FlexoProject>(rootProjects);
		saveProjects(projects);

	}

	public void saveProjects(List<FlexoProject> projects) throws SaveResourceExceptionList {
		List<SaveResourceException> exceptions = new ArrayList<SaveResourceException>();
		Collections.sort(projects, new Comparator<FlexoProject>() {
			@Override
			public int compare(FlexoProject o1, FlexoProject o2) {
				if (o1.importsProject(o2)) {
					return 1;
				} else if (o2.importsProject(o1)) {
					return -1;
				}
				return 0;
			}
		});
		try {
			ProgressWindow.showProgressWindow(FlexoLocalization.localizedForKey("saving"), projects.size());
			for (FlexoProject project : projects) {
				try {
					ProgressWindow.setProgressInstance(FlexoLocalization.localizedForKey("saving") + " " + project.getDisplayName());
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
		} else {
			FlexoController.showError(FlexoLocalization.localizedForKey("error_during_saving"));
		}
		logger.warning("Exception raised: " + e.getClass().getName() + ". See console for details.");
		logger.warning(e.getMessage());
		e.printStackTrace();
	}

	private static void informUserAboutPermissionDeniedException(SaveResourcePermissionDeniedException e) {
		if (e instanceof FileFlexoIODelegate && ((FileFlexoIODelegate) e).getFile().isDirectory()) {
			FlexoController.showError(FlexoLocalization.localizedForKey("permission_denied"),
					FlexoLocalization.localizedForKey("project_was_not_properly_saved_permission_denied_directory") + "\n" + e.toString());
		} else if (e instanceof FileFlexoIODelegate) {
			FlexoController.showError(FlexoLocalization.localizedForKey("permission_denied"),
					FlexoLocalization.localizedForKey("project_was_not_properly_saved_permission_denied_file") + "\n" + e.toString());
		} else {
			FlexoController.showError(FlexoLocalization.localizedForKey("permission_denied"),
					FlexoLocalization.localizedForKey("project_was_not_properly_saved_permission_denied") + "\n" + e.toString());
		}
	}

	protected void preInitialization(File projectDirectory) {
		getServiceManager().getGeneralPreferences().addToLastOpenedProjects(projectDirectory);
		getServiceManager().getPreferencesService().savePreferences();
	}

	public boolean someProjectsAreModified() {
		for (FlexoProject project : getRootProjects()) {
			if (project.hasUnsavedResources()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		logger.fine("ProjectLoader service received notification " + notification + " from " + caller);
	}

	@Override
	public void initialize() {
	}

}
