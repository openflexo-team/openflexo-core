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

package org.openflexo.foundation.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.action.CreateProject;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.nature.ProjectNatureFactory;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ProjectClosed;
import org.openflexo.foundation.resource.ProjectLoaded;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.resource.SaveResourceExceptionList;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.FileUtils;

/**
 * This {@link FlexoService} is responsible to provide {@link FlexoEditor}s on {@link FlexoProject}
 * 
 * @author sylvain
 *
 */
public class ProjectLoader extends FlexoServiceImpl {

	private static final Logger logger = Logger.getLogger(ProjectLoader.class.getPackage().getName());

	public static final String PROJECT_OPENED = "projectOpened";
	public static final String PROJECT_CLOSED = "projectClosed";

	public static final String EDITOR_ADDED = "editorAdded";
	public static final String EDITOR_REMOVED = "editorRemoved";
	public static final String ROOT_PROJECTS = "rootProjects";

	private final Map<FlexoProject<?>, FlexoEditor> editors;
	private Map<Object, FlexoProjectResource<?>> projectResourcesForSerializationArtefacts = new HashMap<>();

	private final List<FlexoProject<?>> rootProjects;
	// private PamelaModelFactory modelFactory;

	/**
	 * Build new {@link ProjectLoader}
	 */
	public ProjectLoader() {
		this.rootProjects = new ArrayList<>();
		this.editors = new LinkedHashMap<>();
		/*try {
			modelFactory = new PamelaModelFactory(FlexoProjectReference.class);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}*/
	}

	public <I> FlexoProjectResource<I> retrieveFlexoProjectResource(I projectDirectory)
			throws ProjectInitializerException, ModelDefinitionException, IOException {
		if (projectDirectory == null) {
			throw new IllegalArgumentException("Project directory cannot be null");
		}
		if (projectDirectory instanceof File && !((File) projectDirectory).exists()) {
			throw new ProjectInitializerException("project directory does not exist", projectDirectory);
		}

		FlexoProjectResource returned = projectResourcesForSerializationArtefacts.get(projectDirectory);

		if (returned == null) {
			for (FlexoResourceCenter rc : getServiceManager().getResourceCenterService().getResourceCenters()) {
				if (rc.getSerializationArtefactClass().isAssignableFrom(projectDirectory.getClass())) {
					Object serializationArtefact = rc.getEntry(FlexoProjectResourceFactory.PROJECT_DATA_FILENAME, projectDirectory);
					returned = (FlexoProjectResource) rc.getResource(serializationArtefact, FlexoProjectResource.class);
					if (returned != null) {
						return returned;
					}
				}
			}
		}

		// Not found nowhere in all RCs, create a new FlexoProjectResource
		if (returned == null) {
			FlexoResourceCenter<I> resourceCenter = getServiceManager().getResourceCenterService()
					.getResourceCenterContaining(projectDirectory);
			returned = getServiceManager().getResourceCenterService().getFlexoProjectResourceFactory().retrieveResource(projectDirectory,
					resourceCenter);
			projectResourcesForSerializationArtefacts.put(projectDirectory, returned);
		}
		return returned;
	}

	public Map<FlexoProject<?>, FlexoEditor> getEditors() {
		return editors;
	}

	/**
	 * Create a new {@link FlexoProject} with supplied projectDirectory and instantiate and return a new {@link FlexoEditor} on it<br>
	 * 
	 * @param projectDirectory
	 * @return a new FlexoEditor allowing to edit new created {@link FlexoProject}
	 * @throws IOException
	 * @throws ProjectInitializerException
	 */
	public <I> FlexoEditor newStandaloneProject(I projectDirectory) throws IOException, ProjectInitializerException {
		return newStandaloneProject(projectDirectory, null);
	}

	/**
	 * Create a new {@link FlexoProject} with supplied name and repository folder and instantiate and return a new {@link FlexoEditor} on
	 * it<br>
	 * 
	 * @param projectName
	 * @param folder
	 * @return a new FlexoEditor allowing to edit new created {@link FlexoProject}
	 * @throws IOException
	 * @throws ProjectInitializerException
	 */
	public <I> FlexoEditor newProjectInResourceCenter(String projectName, RepositoryFolder<FlexoProjectResource<?>, I> folder)
			throws IOException, ProjectInitializerException {
		return newProjectInResourceCenter(projectName, folder, null);
	}

	/**
	 * Create a new {@link FlexoProject} with supplied projectDirectory and instantiate and return a new {@link FlexoEditor} on it<br>
	 * Also gives supplied nature to the project when not null
	 * 
	 * @param projectDirectory
	 * @param projectNature
	 * @return a new FlexoEditor allowing to edit new created {@link FlexoProject}
	 * @throws IOException
	 * @throws ProjectInitializerException
	 */
	public <I> FlexoEditor newStandaloneProject(I projectDirectory, Class<? extends ProjectNature> projectNatureClass)
			throws IOException, ProjectInitializerException {

		Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("new_project") + projectDirectory);

		// This will just create the .version in the project
		// FlexoProjectUtil.currentFlexoVersionIsSmallerThanLastVersion(projectDirectory);

		preInitialization(projectDirectory);

		if (projectDirectory instanceof File && ((File) projectDirectory).exists()) {
			// We should have already asked the user if the new project has to override the old one
			// so we really delete the old project

			File backupProject = new File(((File) projectDirectory).getParentFile(), ((File) projectDirectory).getName() + "~");
			if (backupProject.exists()) {
				FileUtils.recursiveDeleteFile(backupProject);
			}

			try {
				FileUtils.rename(((File) projectDirectory), backupProject);
			} catch (IOException e) {
				throw e;
			}
		}

		FlexoProject<?> newProject = null;

		// TODO: attempt to lookup an eventual FlexoResourceCenter, repository and folder for supplied project directory;

		Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("create_new_project") + " " + projectDirectory);

		// Create the project
		CreateProject action = CreateProject.actionType.makeNewAction(null, null, getServiceManager().getDefaultEditor());
		action.setSerializationArtefact(projectDirectory);
		action.setProjectNatureClass(projectNatureClass);

		action.doAction();
		newProject = action.getNewProject();

		if (newProject == null)
			return null;

		projectResourcesForSerializationArtefacts.put(projectDirectory,
				(FlexoProjectResource<?>) (FlexoResource<?>) newProject.getResource());
		addToRootProjects(newProject);

		// Notify project just loaded
		getServiceManager().notify(this, new ProjectLoaded(newProject));

		FlexoEditor newEditor = getEditorForProject(newProject);

		System.out.println("Hop, on vient de creer le projet " + newProject);
		System.out.println("newEditor=" + newEditor);
		System.out.println("projectNatureClass=" + projectNatureClass);

		// Now, if a nature has been supplied, gives this nature to the project
		if (projectNatureClass != null) {
			Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("gives_nature") + projectDirectory);
			ProjectNatureFactory<?> natureFactory = getServiceManager().getProjectNatureService()
					.getProjectNatureFactory(projectNatureClass);
			natureFactory.givesNature(newProject, newEditor);
		}

		// Create and return a FlexoEditor for the new FlexoProject
		Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("make_editor_for_project") + " " + newProject.getName());
		return getEditorForProject(newProject);
	}

	/**
	 * Create a new {@link FlexoProject} with supplied projectDirectory and instantiate and return a new {@link FlexoEditor} on it<br>
	 * Also gives supplied nature to the project when not null
	 * 
	 * @param projectName
	 * @param folder
	 * @return a new FlexoEditor allowing to edit new created {@link FlexoProject}
	 * @throws IOException
	 * @throws ProjectInitializerException
	 */
	public <I> FlexoEditor newProjectInResourceCenter(String projectName, RepositoryFolder<FlexoProjectResource<?>, I> folder,
			Class<? extends ProjectNature> projectNatureClass) throws IOException, ProjectInitializerException {

		FlexoProject<?> newProject = null;
		FlexoEditor newEditor = null;

		// TODO: attempt to lookup an eventual FlexoResourceCenter, repository and folder for supplied project directory;

		// Create the project
		CreateProject action = CreateProject.actionType.makeNewAction(folder, null, getServiceManager().getDefaultEditor());
		action.setNewProjectName(projectName);
		action.doAction();
		newProject = action.getNewProject();

		preInitialization(newProject.getProjectDirectory());

		projectResourcesForSerializationArtefacts.put(newProject.getProjectDirectory(),
				(FlexoProjectResource) (FlexoResource) newProject.getResource());

		// newEditor(editor);
		addToRootProjects(newProject);

		// Notify project just loaded
		getServiceManager().notify(this, new ProjectLoaded(newProject));

		// Now, if a nature has been supplied, gives this nature to the project
		if (projectNatureClass != null) {
			Progress.progress(
					FlexoLocalization.getMainLocalizer().localizedForKey("gives_nature") + " " + newProject.getProjectDirectory());
			ProjectNatureFactory<?> natureFactory = getServiceManager().getProjectNatureService()
					.getProjectNatureFactory(projectNatureClass);
			natureFactory.givesNature(newProject, newEditor);
		}

		// Create and return a FlexoEditor for the new FlexoProject
		return getEditorForProject(newProject);
	}

	/**
	 * Loads the project located in supplied directory
	 * 
	 * @param projectDirectory
	 * @return
	 * @throws ProjectLoadingCancelledException
	 *             whenever the load procedure is interrupted by the user or by Flexo.
	 * @throws ProjectInitializerException
	 *             a an exception occurs during project initialization
	 */
	public <I> FlexoEditor loadProject(I projectDirectory) throws ProjectLoadingCancelledException, ProjectInitializerException {
		return loadProject(projectDirectory, false);
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
	 * @throws IOException
	 * @throws ModelDefinitionException
	 */
	public <I> FlexoEditor loadProject(I projectDirectory, boolean asImportedProject)
			throws ProjectInitializerException, ProjectLoadingCancelledException {

		Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("opening_project") + projectDirectory);

		try {
			// Retrieve project resource
			FlexoProjectResource<?> projectResource = retrieveFlexoProjectResource(projectDirectory);

			// Load project
			Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("loading_project") + " " + projectResource.getName());
			FlexoProject<?> loadedProject = internalLoadProject(projectResource, asImportedProject);

			// Create and return a FlexoEditor for the new FlexoProject
			Progress.progress(
					FlexoLocalization.getMainLocalizer().localizedForKey("make_editor_for_project") + " " + projectResource.getName());
			return getEditorForProject(loadedProject);

		} catch (ResourceLoadingCancelledException e) {
			throw new ProjectLoadingCancelledException();
		} catch (ModelDefinitionException e) {
			throw new ProjectInitializerException(e, projectDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FlexoException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Return default {@link FlexoEditor} for supplied project
	 * 
	 * @param project
	 * @return
	 */
	public final FlexoEditor getEditorForProject(FlexoProject<?> project) {
		if (project == null) {
			return null;
		}
		FlexoEditor returned = editors.get(project);
		if (returned == null) {
			returned = makeFlexoEditor(project);
			editors.put(project, returned);
			getPropertyChangeSupport().firePropertyChange(EDITOR_ADDED, null, returned);
		}
		return returned;
	}

	public FlexoEditor makeFlexoEditor(FlexoProject<?> project) {
		return new DefaultFlexoEditor(project, getServiceManager());
	}

	/*public FlexoEditor editorForProjectURIAndRevision(String projectURI, long revision) {
		for (Entry<FlexoProject<?>, FlexoEditor> e : editors.entrySet()) {
			if (e.getKey().getProjectURI().equals(projectURI) && e.getKey().getProjectRevision() == revision) {
				return e.getValue();
			}
		}
		return null;
	}*/

	/*public boolean hasEditorForProjectDirectory(File projectDirectory) {
		return getEditorForProjectDirectory(projectDirectory) != null;
	}
	
	public FlexoEditor getEditorForProjectDirectory(File projectDirectory) {
		if (projectDirectory == null) {
			return null;
		}
		for (Entry<FlexoProject<?>, FlexoEditor> e : editors.entrySet()) {
			if (e.getKey().getProjectDirectory().equals(projectDirectory)) {
				return e.getValue();
			}
		}
		return null;
	}*/

	/**
	 * Internally load supplied project resource
	 * 
	 * @param projectResource
	 * @param asImportedProject
	 * @return
	 * @throws ProjectInitializerException
	 * @throws FileNotFoundException
	 * @throws ResourceLoadingCancelledException
	 * @throws FlexoException
	 */
	private <I> FlexoProject<I> internalLoadProject(FlexoProjectResource<I> projectResource, boolean asImportedProject)
			throws ProjectInitializerException, FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		LocalizedDelegate locales = getServiceManager().getLocalizationService().getFlexoLocalizer();

		/*try {
			FlexoProjectUtil.isProjectOpenable(projectDirectory);
		} catch (UnreadableProjectException e) {
			throw new ProjectLoadingCancelledException(e.getMessage());
		}*/

		Progress.progress(locales.localizedForKey("opening_project") + projectResource.getName());

		// FlexoEditor editor = null;

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Opening " + projectResource);
		}
		if (!asImportedProject) {
			// Adds to recent project
			Progress.progress(locales.localizedForKey("initialize_project") + projectResource.getName());
			preInitialization(projectResource.getDelegateResourceCenter()
					.getContainer((I) projectResource.getIODelegate().getSerializationArtefact()));
		}

		/*for (Entry<FlexoProject<?>, FlexoEditor> e : editors.entrySet()) {
			if (e.getKey().getProjectDirectory().equals(projectDirectory)) {
				editor = e.getValue();
			}
		}*/

		FlexoProject<I> loadedProject = projectResource.getResourceData();

		/*if (editor == null) {
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
		}*/

		if (!asImportedProject) {
			addToRootProjects(loadedProject);
		}

		// Notify project just loaded
		Progress.progress(locales.localizedForKey("notify_editors"));
		getServiceManager().notify(this, new ProjectLoaded(loadedProject));

		return loadedProject;
	}

	public <I> FlexoEditor reloadProject(FlexoProject<I> project) throws ProjectLoadingCancelledException, ProjectInitializerException {
		I projectDirectory = (I) project.getResource().getIODelegate().getSerializationArtefact();
		closeProject(project);
		return loadProject(projectDirectory);
	}

	public void closeProject(FlexoProject<?> project) {
		FlexoEditor editor = editors.remove(project);
		if (project != null) {
			project.close();
			removeFromRootProjects(project);
			getPropertyChangeSupport().firePropertyChange(EDITOR_REMOVED, editor, null);
		}
		getServiceManager().notify(this, new ProjectClosed<>(project));
	}

	public <I> FlexoEditor saveAsProject(I projectDirectory, FlexoProject<I> project) throws Exception {
		// closes the selected project
		closeProject(project);

		// prepare target if exists
		if (projectDirectory instanceof File && ((File) projectDirectory).exists()) {
			// We should have already asked the user if the new project has to override the old one
			// so we really delete the old project

			File backupProject = new File(((File) projectDirectory).getParentFile(), ((File) projectDirectory).getName() + "~");
			if (backupProject.exists()) {
				FileUtils.recursiveDeleteFile(backupProject);
			}
			FileUtils.rename(((File) projectDirectory), backupProject);
		}

		// copy project files
		project.copyTo(projectDirectory);

		// reload project
		return loadProject(projectDirectory);
	}

	public List<FlexoProject<?>> getModifiedProjects() {
		List<FlexoProject<?>> projects = new ArrayList<>(editors.size());
		// 1. compute all modified projects
		for (FlexoProject<?> project : getRootProjects()) {
			if (project.hasUnsavedResources()) {
				projects.add(project);
			}
		}
		// 2. we now add all the projects that depend on a modified project
		// to the list of modified projects (so that they also get saved
		for (FlexoProject<?> modifiedProject : new ArrayList<>(projects)) {
			for (FlexoProject<?> project : getRootProjects()) {
				if (project.importsProject(modifiedProject)) {
					if (!projects.contains(project)) {
						projects.add(project);
					}
				}
			}
		}
		// 3. We restore the order of projects according to the one in rootProjects
		Collections.sort(projects, new Comparator<FlexoProject<?>>() {

			@Override
			public int compare(FlexoProject<?> o1, FlexoProject<?> o2) {
				return rootProjects.indexOf(o1) - rootProjects.indexOf(o2);
			}
		});
		return projects;
	}

	public List<FlexoProject<?>> getRootProjects() {
		return rootProjects;
	}

	public void addToRootProjects(FlexoProject<?> project) {
		if (!rootProjects.contains(project)) {
			rootProjects.add(project);
			getPropertyChangeSupport().firePropertyChange(PROJECT_OPENED, null, project);
			getPropertyChangeSupport().firePropertyChange(ROOT_PROJECTS, null, project);
		}
	}

	public void removeFromRootProjects(FlexoProject<?> project) {
		rootProjects.remove(project);
		getPropertyChangeSupport().firePropertyChange(PROJECT_CLOSED, project, null);
		getPropertyChangeSupport().firePropertyChange(ROOT_PROJECTS, project, null);
	}

	public void saveAllProjects() throws SaveResourceExceptionList {
		// Saves all projects. It is necessary to save all projects because during serialization, a project may increment its revision which
		// in turn can modify project that import the former one.
		List<FlexoProject<?>> projects = new ArrayList<>(rootProjects);
		saveProjects(projects);

	}

	public void saveProjects(List<FlexoProject<?>> projects) throws SaveResourceExceptionList {
		List<SaveResourceException> exceptions = new ArrayList<>();
		Collections.sort(projects, new Comparator<FlexoProject<?>>() {
			@Override
			public int compare(FlexoProject<?> o1, FlexoProject<?> o2) {
				if (o1.importsProject(o2)) {
					return 1;
				}
				else if (o2.importsProject(o1)) {
					return -1;
				}
				return 0;
			}
		});
		for (FlexoProject<?> project : projects) {
			try {
				project.getResource().save();
			} catch (SaveResourceException e) {
				e.printStackTrace();
				exceptions.add(e);
			}
		}
		if (exceptions.size() > 0) {
			throw new SaveResourceExceptionList(exceptions);
		}
	}

	protected <I> void preInitialization(I projectDirectory) {
	}

	public boolean someProjectsAreModified() {
		for (FlexoProject<?> project : getRootProjects()) {
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
	public String getServiceName() {
		return "ProjectLoader";
	}

	@Override
	public void initialize() {
		status = Status.Started;
	}

	/*public PamelaModelFactory getModelFactory() {
		return modelFactory;
	}*/

}
