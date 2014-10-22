package org.openflexo.view.controller;

import java.io.File;

import org.openflexo.ApplicationData;
import org.openflexo.components.NewProjectComponent;
import org.openflexo.components.OpenProjectComponent;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.nature.ProjectNatureService;
import org.openflexo.foundation.task.FlexoTaskManager;
import org.openflexo.foundation.utils.OperationCancelledException;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.module.LoadModuleTask;
import org.openflexo.module.Module;
import org.openflexo.module.ModuleLoader;
import org.openflexo.module.ModuleLoadingException;
import org.openflexo.module.NatureSpecificModule;
import org.openflexo.project.ProjectLoader;

public class WelcomePanelController extends FlexoFIBController {

	public WelcomePanelController(FIBComponent component) {
		super(component);
	}

	@Override
	public ApplicationData getDataObject() {
		return (ApplicationData) super.getDataObject();
	}

	private ModuleLoader getModuleLoader() {
		return getDataObject().getApplicationContext().getModuleLoader();
	}

	private ProjectLoader getProjectLoader() {
		return getDataObject().getApplicationContext().getProjectLoader();
	}

	private FlexoTaskManager getTaskManager() {
		return getDataObject().getApplicationContext().getTaskManager();
	}

	private ProjectNatureService getProjectNatureService() {
		return getDataObject().getApplicationContext().getProjectNatureService();
	}

	public void exit() {
		try {
			getModuleLoader().quit(false);
		} catch (OperationCancelledException e) {
		}
	}

	public void openModule(Module module) {
		hide();
		try {
			getModuleLoader().switchToModule(module);
			validateAndDispose();
		} catch (ModuleLoadingException e) {
			e.printStackTrace();
			FlexoController.notify(FlexoLocalization.localizedForKey("could_not_load_module") + " " + e.getModule());
			show();
		}
	}

	public void openProject(File projectDirectory, Module module) {

		if (projectDirectory == null) {
			projectDirectory = OpenProjectComponent.getProjectDirectory(getDataObject().getApplicationContext());
			if (projectDirectory == null) {
				return;
			}
		}
		hide();
		LoadModuleTask loadModuleTask = null;
		try {
			loadModuleTask = getModuleLoader().switchToModule(module);
		} catch (ModuleLoadingException e) {
			e.printStackTrace();
			FlexoController.notify(FlexoLocalization.localizedForKey("could_not_load_module") + " " + e.getModule());
			show();
		}

		// try {

		getProjectLoader().loadProject(projectDirectory, loadModuleTask);

		// LoadProjectTask loadProjectTask = new LoadProjectTask(getProjectLoader(), projectDirectory);
		// loadProjectTask.addToDependantTasks(loadModuleTask);
		// getTaskManager().scheduleExecution(loadProjectTask);

		/*} catch (ProjectLoadingCancelledException e) {
		} catch (ProjectInitializerException e) {
			e.printStackTrace();
			FlexoController.notify(FlexoLocalization.localizedForKey("could_not_open_project_located_at")
					+ e.getProjectDirectory().getAbsolutePath());
		}*/

		validateAndDispose();

	}

	public void newProject(Module module) {
		File projectDirectory;
		projectDirectory = NewProjectComponent.getProjectDirectory(getDataObject().getApplicationContext());
		if (projectDirectory == null) {
			return;
		}

		hide();

		LoadModuleTask loadModuleTask = null;
		try {
			loadModuleTask = getModuleLoader().switchToModule(module);
		} catch (ModuleLoadingException e) {
			e.printStackTrace();
			FlexoController.notify(FlexoLocalization.localizedForKey("could_not_load_module") + " " + e.getModule());
			show();
		}

		if (module instanceof NatureSpecificModule) {
			ProjectNature<?, ?> nature = getProjectNatureService().getProjectNature(((NatureSpecificModule) module).getNatureClass());
			getProjectLoader().newProject(projectDirectory, nature, loadModuleTask);
		} else {
			getProjectLoader().newProject(projectDirectory, loadModuleTask);
		}
		validateAndDispose();
	}

}
