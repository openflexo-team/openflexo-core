/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.br.BugReportService;
import org.openflexo.drm.DocResourceManager;
import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoEditor.FlexoEditorFactory;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoService.ServiceNotification;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.remoteresources.FlexoUpdateService;
import org.openflexo.foundation.resource.DefaultResourceCenterService;
import org.openflexo.foundation.resource.DefaultResourceCenterService.DefaultPackageResourceCenterIsNotInstalled;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterListShouldBeStored;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.JarResourceCenter;
import org.openflexo.foundation.task.FlexoTaskManager;
import org.openflexo.foundation.utils.ProjectLoadingHandler;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.module.ModuleLoader;
import org.openflexo.prefs.PreferencesService;
import org.openflexo.project.ProjectLoader;
import org.openflexo.rm.AddResourceCenterTask;
import org.openflexo.rm.RemoveResourceCenterTask;
import org.openflexo.rm.ResourceConsistencyService;
import org.openflexo.task.TaskManagerPanel;
import org.openflexo.view.controller.FlexoServerInstanceManager;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * The {@link ApplicationContext} is the {@link FlexoServiceManager} at desktop application level.<br>
 * 
 * It basically inherits from {@link FlexoServiceManager} by extending service manager with desktop-level services:<br>
 * <ul>
 * <li>{@link ModuleLoader}</li>
 * <li>{@link PreferencesService}</li>
 * <li>{@link TechnologyAdapterControllerService}</li>
 * <li>{@link BugReportService}</li>
 * <li>{@link DocResourceManager}</li>
 * </ul>
 * 
 * @author sylvain
 * 
 */
public abstract class ApplicationContext extends DefaultFlexoServiceManager implements FlexoEditorFactory {

	private final FlexoEditor applicationEditor;

	// private ServerRestService serverRestService;

	public ApplicationContext() {
		super();

		registerModuleLoaderService();
		registerPreferencesService();

		applicationEditor = createApplicationEditor();
		try {
			ProjectLoader projectLoader = new ProjectLoader();
			registerService(projectLoader);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		TechnologyAdapterControllerService technologyAdapterControllerService = createTechnologyAdapterControllerService();
		registerService(technologyAdapterControllerService);
		// BugReportService bugReportService = createBugReportService();
		// registerService(bugReportService);
		DocResourceManager docResourceManager = createDocResourceManager();
		registerService(docResourceManager);
		FlexoServerInstanceManager flexoServerInstanceManager = createFlexoServerInstanceManager();
		registerService(flexoServerInstanceManager);
		ResourceConsistencyService resourceConsistencyService = createResourceConsistencyService();
		registerService(resourceConsistencyService);
	}

	private void registerPreferencesService() {
		registerModuleLoaderService();
		if (getPreferencesService() == null) {
			PreferencesService preferencesService = createPreferencesService();
			registerService(preferencesService);
		}
	}

	private void registerModuleLoaderService() {
		if (getModuleLoader() == null) {
			ModuleLoader moduleLoader = createModuleLoader();
			registerService(moduleLoader);
		}
	}

	public PreferencesService getPreferencesService() {
		return getService(PreferencesService.class);
	}

	public ModuleLoader getModuleLoader() {
		return getService(ModuleLoader.class);
	}

	public BugReportService getBugReportService() {
		if (getService(BugReportService.class) == null) {
			BugReportService bugReportService = createBugReportService();
			registerService(bugReportService);
		}
		return getService(BugReportService.class);
	}

	public DocResourceManager getDocResourceManager() {
		return getService(DocResourceManager.class);
	}

	public ProjectLoader getProjectLoader() {
		return getService(ProjectLoader.class);
	}

	public FlexoServerInstanceManager getFlexoServerInstanceManager() {
		return getService(FlexoServerInstanceManager.class);
	}

	public FlexoUpdateService getFlexoUpdateService() {
		return getService(FlexoUpdateService.class);
	}

	public ResourceConsistencyService getResourceConsistencyService() {
		return getService(ResourceConsistencyService.class);
	}

	public final TechnologyAdapterControllerService getTechnologyAdapterControllerService() {
		return getService(TechnologyAdapterControllerService.class);
	}

	public final FlexoEditor getApplicationEditor() {
		return applicationEditor;
	}

	/*
	public ServerRestService getServerRestService() {
		if (serverRestService == null) {
			serverRestService = new ServerRestService(getProjectLoader());
		}
		return serverRestService;
	}
	 */

	public boolean isAutoSaveServiceEnabled() {
		return false;
	}

	public abstract ProjectLoadingHandler getProjectLoadingHandler(File projectDirectory);

	@Override
	protected abstract FlexoEditor createApplicationEditor();

	protected abstract TechnologyAdapterControllerService createTechnologyAdapterControllerService();

	protected abstract PreferencesService createPreferencesService();

	protected abstract BugReportService createBugReportService();

	protected abstract DocResourceManager createDocResourceManager();

	protected abstract FlexoServerInstanceManager createFlexoServerInstanceManager();

	protected abstract ResourceConsistencyService createResourceConsistencyService();

	protected ModuleLoader createModuleLoader() {
		return new ModuleLoader(this);
	}

	@Override
	protected FlexoResourceCenterService createResourceCenterService() {
		registerPreferencesService();
		return DefaultResourceCenterService
				.getNewInstance(getPreferencesService().getResourceCenterPreferences().getResourceCenterEntries());
	}

	@Override
	public void notify(FlexoService caller, ServiceNotification notification) {
		super.notify(caller, notification);
		// Little hack to handle rc location saving
		// TODO: Should be removed when preferences will be a service see OPENFLEXO-651
		if (notification instanceof ResourceCenterListShouldBeStored && caller instanceof FlexoResourceCenterService) {
			List<File> rcList = new ArrayList<File>();
			for (FlexoResourceCenter<?> rc : ((FlexoResourceCenterService) caller).getResourceCenters()) {
				if (rc instanceof DirectoryResourceCenter) {
					rcList.add(((DirectoryResourceCenter) rc).getDirectory());
				}
				else if (rc instanceof JarResourceCenter) {
					rcList.add(new File(((JarResourceCenter) rc).getJarResourceImpl().getRelativePath()));
				}
			}
			getGeneralPreferences().setDirectoryResourceCenterList(rcList);
		}
		else if (notification instanceof DefaultPackageResourceCenterIsNotInstalled && caller instanceof FlexoResourceCenterService) {
			defaultPackagedResourceCenterIsNotInstalled = true;
		}
	}

	@Override
	protected AddResourceCenterTask resourceCenterAdded(FlexoResourceCenter<?> resourceCenter) {
		AddResourceCenterTask addRCTask = new AddResourceCenterTask(getResourceCenterService(), resourceCenter);
		getTaskManager().scheduleExecution(addRCTask);
		return addRCTask;
	}

	@Override
	protected void resourceCenterRemoved(FlexoResourceCenter<?> resourceCenter) {
		RemoveResourceCenterTask removeRCTask = new RemoveResourceCenterTask(getResourceCenterService(), resourceCenter);
		getTaskManager().scheduleExecution(removeRCTask);
	}

	private boolean defaultPackagedResourceCenterIsNotInstalled;

	public boolean defaultPackagedResourceCenterIsToBeInstalled() {
		return defaultPackagedResourceCenterIsNotInstalled;
	}

	public GeneralPreferences getGeneralPreferences() {
		if (getPreferencesService() != null) {
			return getPreferencesService().getPreferences(GeneralPreferences.class);
		}
		return null;
	}

	public AdvancedPrefs getAdvancedPrefs() {
		if (getPreferencesService() != null) {
			return getPreferencesService().getPreferences(AdvancedPrefs.class);
		}
		return null;
	}

	@Override
	protected FlexoTaskManager createTaskManager() {
		FlexoTaskManager returned = super.createTaskManager();
		TaskManagerPanel taskManagerPanel = new TaskManagerPanel(returned);
		return returned;
	}
}
