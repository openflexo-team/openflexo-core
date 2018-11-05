/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.view.controller;

import java.io.File;
import java.io.IOException;

import org.openflexo.ApplicationData;
import org.openflexo.components.NewProjectComponent;
import org.openflexo.components.OpenProjectComponent;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.utils.OperationCancelledException;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.module.LoadModuleTask;
import org.openflexo.module.Module;
import org.openflexo.module.ModuleLoader;
import org.openflexo.module.ModuleLoadingException;
import org.openflexo.module.NatureSpecificModule;
import org.openflexo.project.InteractiveProjectLoader;

public class WelcomePanelController extends FlexoFIBController {

	public WelcomePanelController(FIBComponent component, GinaViewFactory<?> viewFactory) {
		super(component, viewFactory);
	}

	@Override
	public ApplicationData getDataObject() {
		return (ApplicationData) super.getDataObject();
	}

	private ModuleLoader getModuleLoader() {
		return getDataObject().getApplicationContext().getModuleLoader();
	}

	private InteractiveProjectLoader getProjectLoader() {
		return (InteractiveProjectLoader) getDataObject().getApplicationContext().getProjectLoader();
	}

	/* Unused
	private FlexoTaskManager getTaskManager() {
		return getDataObject().getApplicationContext().getTaskManager();
	}
	
	private ProjectNatureService getProjectNatureService() {
		return getDataObject().getApplicationContext().getProjectNatureService();
	}
	*/

	public void exit() {
		try {
			getModuleLoader().quit(false);
		} catch (OperationCancelledException e) {}
	}

	public void openModule(Module<?> module) {
		hide();
		try {
			getModuleLoader().switchToModule(module);
			validateAndDispose();
		} catch (ModuleLoadingException e) {
			e.printStackTrace();
			FlexoController.notify(FlexoLocalization.getMainLocalizer().localizedForKey("could_not_load_module") + " " + e.getModule());
			show();
		}
	}

	public void openProject(Module<?> module) throws ProjectLoadingCancelledException, ProjectInitializerException {
		File projectDirectory = OpenProjectComponent.getProjectDirectory(getDataObject().getApplicationContext());
		if (projectDirectory != null) {
			openProject(projectDirectory, module);
		}
	}

	public void openProject(File projectDirectory, Module<?> module) throws ProjectLoadingCancelledException, ProjectInitializerException {
		if (projectDirectory == null)
			return;

		hide();
		LoadModuleTask loadModuleTask = null;
		try {
			loadModuleTask = getModuleLoader().switchToModule(module);
		} catch (ModuleLoadingException e) {
			e.printStackTrace();
			FlexoController.notify(FlexoLocalization.getMainLocalizer().localizedForKey("could_not_load_module") + " " + e.getModule());
			show();
		}

		getProjectLoader().makeLoadProjectTask(projectDirectory, loadModuleTask);

		validateAndDispose();

	}

	public void newProject(Module<?> module) throws IOException, ProjectInitializerException {
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
			FlexoController.notify(FlexoLocalization.getMainLocalizer().localizedForKey("could_not_load_module") + " " + e.getModule());
			show();
		}

		if (module instanceof NatureSpecificModule) {
			Class<? extends ProjectNature> projectNatureClass = ((NatureSpecificModule<?, ?>) module).getProjectNatureClass();
			getProjectLoader().makeNewProjectTask(projectDirectory, projectNatureClass, loadModuleTask);
		}
		else {
			getProjectLoader().makeNewProjectTask(projectDirectory, loadModuleTask);
		}
		validateAndDispose();
	}

}
