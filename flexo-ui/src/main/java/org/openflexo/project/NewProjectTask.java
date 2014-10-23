package org.openflexo.project;

import java.io.File;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.resource.ProjectLoaded;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.utils.FlexoProjectUtil;
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
	private final ProjectLoader projectLoader;
	private final File projectDirectory;
	private final ProjectNature<?, ?> projectNature;
	private FlexoEditor flexoEditor;

	public NewProjectTask(ProjectLoader projectLoader, File projectDirectory) {
		this(projectLoader, projectDirectory, null);
	}

	public NewProjectTask(ProjectLoader projectLoader, File projectDirectory, ProjectNature<?, ?> projectNature) {
		super(FlexoLocalization.localizedForKey("new_project") + " " + projectDirectory.getName(), projectLoader);
		this.projectLoader = projectLoader;
		this.projectDirectory = projectDirectory;
		this.projectNature = projectNature;
	}

	@Override
	public void performTask() {
		Progress.setExpectedProgressSteps(100);

		// This will just create the .version in the project
		FlexoProjectUtil.currentFlexoVersionIsSmallerThanLastVersion(projectDirectory);

		projectLoader.preInitialization(projectDirectory);
		try {
			flexoEditor = FlexoProject.newProject(projectDirectory, projectNature, projectLoader.getServiceManager(),
					projectLoader.getServiceManager(), null);
		} catch (ProjectInitializerException e) {
			throwException(e);
		}
		projectLoader.newEditor(flexoEditor);
		projectLoader.addToRootProjects(flexoEditor.getProject());

		// Notify project just loaded
		projectLoader.getServiceManager().notify(projectLoader, new ProjectLoaded(flexoEditor.getProject()));
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

		showException(FlexoLocalization.localizedForKey("could_not_create_project"),
				FlexoLocalization.localizedForKey("could_not_create_project_located_at") + projectDirectory.getAbsolutePath(), e);
		e.printStackTrace();
	}
}