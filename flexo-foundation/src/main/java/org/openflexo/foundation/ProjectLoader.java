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

package org.openflexo.foundation;

import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.resource.FlexoProjectReference;
import org.openflexo.foundation.resource.ProjectClosed;
import org.openflexo.foundation.resource.ProjectLoaded;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.resource.SaveResourceExceptionList;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.utils.FlexoProjectUtil;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.foundation.utils.UnreadableProjectException;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.HasPropertyChangeSupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This {@link FlexoService} allows to instanciate {@link FlexoProject} through a {@link FlexoEditor} for a given
 * {@link FlexoServiceManager}
 * 
 * @author sylvain
 *
 */
public class ProjectLoader extends FlexoServiceImpl implements HasPropertyChangeSupport, FlexoService {

	private static final Logger logger = Logger.getLogger(ProjectLoader.class.getPackage().getName());

	public static final String PROJECT_OPENED = "projectOpened";
	public static final String PROJECT_CLOSED = "projectClosed";

	public static final String EDITOR_ADDED = "editorAdded";
	public static final String EDITOR_REMOVED = "editorRemoved";
	public static final String ROOT_PROJECTS = "rootProjects";

	private final Map<FlexoProject, FlexoEditor> editors;

	private final List<FlexoProject> rootProjects;
	private ModelFactory modelFactory;

	public ProjectLoader() {
		this.rootProjects = new ArrayList<FlexoProject>();
		this.editors = new LinkedHashMap<FlexoProject, FlexoEditor>();
		try {
			modelFactory = new ModelFactory(FlexoProjectReference.class);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
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
		return getEditorForProjectDirectory(projectDirectory) != null;
	}

	public FlexoEditor getEditorForProjectDirectory(File projectDirectory) {
		if (projectDirectory == null) {
			return null;
		}
		for (Entry<FlexoProject, FlexoEditor> e : editors.entrySet()) {
			if (e.getKey().getProjectDirectory().equals(projectDirectory)) {
				return e.getValue();
			}
		}
		return null;
	}

	public Map<FlexoProject, FlexoEditor> getEditors() {
		return editors;
	}

	public FlexoTask loadProject(File projectDirectory, FlexoTask... tasksToBeExecutedBefore)
			throws ProjectLoadingCancelledException, ProjectInitializerException {
		return loadProject(projectDirectory, false, tasksToBeExecutedBefore);
	}

	/**
	 * Loads the project located withing <code> projectDirectory </code>. The following method is the default method to call when opening a
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
	public FlexoTask loadProject(File projectDirectory, boolean asImportedProject, FlexoTask... tasksToBeExecutedBefore)
			throws ProjectInitializerException, ProjectLoadingCancelledException
	{

		internalLoadProject(projectDirectory, asImportedProject);
		return null;
	}

	private FlexoEditor internalLoadProject(File projectDirectory, boolean asImportedProject)
			throws ProjectInitializerException, ProjectLoadingCancelledException {
		LocalizedDelegate locales = getServiceManager().getLocalizationService().getFlexoLocalizer();

		if (projectDirectory == null) {
			throw new IllegalArgumentException("Project directory cannot be null");
		}
		if (!projectDirectory.exists()) {
			throw new ProjectInitializerException("project directory does not exist", projectDirectory);
		}
		try {
			FlexoProjectUtil.isProjectOpenable(projectDirectory);
		} catch (UnreadableProjectException e) {
			throw new ProjectLoadingCancelledException(e.getMessage());
		}

		Progress.progress(locales.localizedForKey("opening_project") + projectDirectory.getAbsolutePath());

		FlexoEditor editor = null;

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Opening " + projectDirectory.getAbsolutePath());
		}
		if (!asImportedProject) {
			// Adds to recent project
			Progress.progress(locales.localizedForKey("preinitialize_project") + projectDirectory.getAbsolutePath());
			preInitialization(projectDirectory);
		}
		for (Entry<FlexoProject, FlexoEditor> e : editors.entrySet()) {
			if (e.getKey().getProjectDirectory().equals(projectDirectory)) {
				editor = e.getValue();
			}
		}
		if (editor == null) {
			try {
				editor = FlexoProject.openProject(projectDirectory, new FlexoEditor.FlexoEditorFactory() {
					@Override
					public FlexoEditor makeFlexoEditor(FlexoProject project, FlexoServiceManager serviceManager) {
						return new DefaultFlexoEditor(project, serviceManager);
					}
				}, getServiceManager(), null);
			} catch (ProjectLoadingCancelledException e1) {
				throw e1;
			} catch (ProjectInitializerException e1) {
				throw e1;
			}
			Progress.progress(locales.localizedForKey("create_and_open_editor"));
			newEditor(editor);
		}
		if (!asImportedProject) {
			addToRootProjects(editor.getProject());
		}

		// Notify project just loaded
		Progress.progress(locales.localizedForKey("notify_editors"));
		getServiceManager().notify(this, new ProjectLoaded(editor.getProject()));

		return editor;
	}

	public FlexoTask reloadProject(FlexoProject project) throws ProjectLoadingCancelledException, ProjectInitializerException {
		File projectDirectory = project.getProjectDirectory();
		closeProject(project);
		return loadProject(projectDirectory);
	}

	public FlexoTask newProject(File projectDirectory, FlexoTask... tasksToBeExecutedBefore)
			throws IOException, ProjectInitializerException {
		return newProject(projectDirectory, null, tasksToBeExecutedBefore);
	}

	public FlexoTask newProject(File projectDirectory, ProjectNature<?, ?> projectNature, FlexoTask... tasksToBeExecutedBefore)
			throws IOException, ProjectInitializerException {

		// This will just create the .version in the project
		FlexoProjectUtil.currentFlexoVersionIsSmallerThanLastVersion(projectDirectory);

		preInitialization(projectDirectory);

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
				throw e;
			}
		}

		FlexoEditor editor = null;

		try {
			editor = FlexoProject.newProject(projectDirectory, new FlexoEditor.FlexoEditorFactory() {
				@Override
				public FlexoEditor makeFlexoEditor(FlexoProject project, FlexoServiceManager serviceManager) {
					return new DefaultFlexoEditor(project, serviceManager);
				}
			}, getServiceManager(), null);
		} catch (ProjectInitializerException e) {
			throw e;
		}
		newEditor(editor);
		addToRootProjects(editor.getProject());

		// Notify project just loaded
		getServiceManager().notify(this, new ProjectLoaded(editor.getProject()));

		// Now, if a nature has been supplied, gives this nature to the project
		if (projectNature != null) {
			projectNature.givesNature(editor.getProject(), editor);
		}

		return null;
	}

	protected void newEditor(FlexoEditor editor) {
		editors.put(editor.getProject(), editor);
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
		FlexoEditor editor = editors.remove(project);
		if (project != null) {
			project.close();
			removeFromRootProjects(project);
			getPropertyChangeSupport().firePropertyChange(EDITOR_REMOVED, editor, null);
		}
		getServiceManager().notify(this, new ProjectClosed(project));
	}

	public void saveAsProject(File projectDirectory, FlexoProject project) throws Exception {
		// closes the selected project
		closeProject(project);

		// prepare target if exists
		if (projectDirectory.exists()) {
			// We should have already asked the user if the new project has to override the old one
			// so we really delete the old project

			File backupProject = new File(projectDirectory.getParentFile(), projectDirectory.getName() + "~");
			if (backupProject.exists()) {
				FileUtils.recursiveDeleteFile(backupProject);
			}
			FileUtils.rename(projectDirectory, backupProject);
		}

		// copy project files
		project.copyTo(projectDirectory);

		// reload project
		loadProject(projectDirectory);
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

	public void addToRootProjects(FlexoProject project) {
		if (!rootProjects.contains(project)) {
			rootProjects.add(project);
			getPropertyChangeSupport().firePropertyChange(PROJECT_OPENED, null, project);
			getPropertyChangeSupport().firePropertyChange(ROOT_PROJECTS, null, project);
		}
	}

	public void removeFromRootProjects(FlexoProject project) {
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
				}
				else if (o2.importsProject(o1)) {
					return -1;
				}
				return 0;
			}
		});
		for (FlexoProject project : projects) {
			try {
				project.save(null);
			} catch (SaveResourceException e) {
				e.printStackTrace();
				exceptions.add(e);
			}
		}
		if (exceptions.size() > 0) {
			throw new SaveResourceExceptionList(exceptions);
		}
	}

	protected void preInitialization(File projectDirectory) {
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

	public ModelFactory getModelFactory() {
		return modelFactory;
	}

}
