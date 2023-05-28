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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.br.ActivateBugReportServiceTask;
import org.openflexo.br.BugReportService;
import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoService.ServiceNotification;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.resource.DefaultResourceCenterService;
import org.openflexo.foundation.resource.DefaultResourceCenterService.DefaultPackageResourceCenterIsNotInstalled;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterListShouldBeStored;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.JarResourceCenter;
import org.openflexo.foundation.task.FlexoTaskManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.utils.ProjectLoadingHandler;
import org.openflexo.module.ModuleLoader;
import org.openflexo.prefs.AdvancedPrefs;
import org.openflexo.prefs.ApplicationFIBLibraryService;
import org.openflexo.prefs.BugReportPreferences;
import org.openflexo.prefs.GeneralPreferences;
import org.openflexo.prefs.LoggingPreferences;
import org.openflexo.prefs.PreferencesService;
import org.openflexo.prefs.PresentationPreferences;
import org.openflexo.rm.ActivateTechnologyAdapterTask;
import org.openflexo.rm.AddResourceCenterTask;
import org.openflexo.rm.DisactivateTechnologyAdapterTask;
import org.openflexo.rm.RemoveResourceCenterTask;
import org.openflexo.rm.ResourceConsistencyService;
import org.openflexo.task.TaskManagerPanel;
import org.openflexo.view.controller.FlexoServerInstanceManager;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * The {@link ApplicationContext} is an implementation of {@link FlexoServiceManager} at desktop application level.<br>
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
public abstract class ApplicationContext extends DefaultFlexoServiceManager {

	private final ApplicationData applicationData;

	/**
	 * Initialize a new {@link ApplicationContext}
	 * 
	 * @param localizationRelativePath
	 *            a String identifying a relative path to use for main localization (such as "FlexoLocalization/MyLocales") of the
	 *            application
	 * @param devMode
	 *            true when 'developer' mode set to true (enable more services)
	 */
	public ApplicationContext(String localizationRelativePath, boolean enableDirectoryWatching, boolean devMode) {
		super(localizationRelativePath, enableDirectoryWatching, devMode);

		applicationData = new ApplicationData(this);

		registerApplicationFIBLibraryService();
		registerModuleLoaderService();

		TechnologyAdapterControllerService technologyAdapterControllerService = createTechnologyAdapterControllerService();
		registerService(technologyAdapterControllerService);

		registerPreferencesService();

		// BugReportService bugReportService = createBugReportService();
		// registerService(bugReportService);
		// DocResourceManager docResourceManager = createDocResourceManager();
		// registerService(docResourceManager);
		FlexoServerInstanceManager flexoServerInstanceManager = createFlexoServerInstanceManager();
		registerService(flexoServerInstanceManager);
		ResourceConsistencyService resourceConsistencyService = createResourceConsistencyService();
		registerService(resourceConsistencyService);

	}

	// Unused private JFIBEditor applicationFIBEditor;

	public ApplicationData getApplicationData() {
		return applicationData;
	}

	private void registerApplicationFIBLibraryService() {
		if (getApplicationFIBLibraryService() == null) {
			ApplicationFIBLibraryService applicationFIBLibraryService = createApplicationFIBLibraryService();
			registerService(applicationFIBLibraryService);
		}
	}

	protected void registerPreferencesService() {
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

	public ApplicationFIBLibraryService getApplicationFIBLibraryService() {
		return getService(ApplicationFIBLibraryService.class);
	}

	public ModuleLoader getModuleLoader() {
		return getService(ModuleLoader.class);
	}

	public BugReportService getBugReportService() {
		if (getService(BugReportService.class) == null) {

			ActivateBugReportServiceTask activateBRTask = new ActivateBugReportServiceTask(this);

			// BugReportService bugReportService = createBugReportService();
			// registerService(bugReportService);

			getTaskManager().scheduleExecution(activateBRTask);
			// getTaskManager().waitTask(activateBRTask);

			return activateBRTask.getBugReportService();
		}
		return getService(BugReportService.class);
	}

	/*

	 	@Override
	public synchronized ActivateTechnologyAdapterTask activateTechnologyAdapter(TechnologyAdapter technologyAdapter) {
	
		// We try here to prevent activate all TA concurrently
	
		if (technologyAdapter.isActivated()) {
			// Already activated
			return null;
		}
		if (activatingTechnologyAdapters == null) {
			activatingTechnologyAdapters = new HashMap<>();
		}
		if (activatingTechnologyAdapters.get(technologyAdapter) != null) {
			// About to be activated. No need to go further
			return null;
		}
		ActivateTechnologyAdapterTask activateTATask = new ActivateTechnologyAdapterTask(getTechnologyAdapterService(), technologyAdapter);
		for (TechnologyAdapter ta : activatingTechnologyAdapters.keySet()) {
			activateTATask.addToDependantTasks(activatingTechnologyAdapters.get(ta));
			// System.out.println("> Waiting " + ta);
		}
	
		activatingTechnologyAdapters.put(technologyAdapter, activateTATask);
	
		getTaskManager().scheduleExecution(activateTATask);
		return activateTATask;
	}
	
	*/

	public ProjectLoader getProjectLoader() {
		return getService(ProjectLoader.class);
	}

	public FlexoServerInstanceManager getFlexoServerInstanceManager() {
		return getService(FlexoServerInstanceManager.class);
	}

	public ResourceConsistencyService getResourceConsistencyService() {
		return getService(ResourceConsistencyService.class);
	}

	public final TechnologyAdapterControllerService getTechnologyAdapterControllerService() {
		return getService(TechnologyAdapterControllerService.class);
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

	protected ApplicationFIBLibraryService createApplicationFIBLibraryService() {
		return new ApplicationFIBLibraryService();
	}

	protected abstract PreferencesService createPreferencesService();

	public abstract BugReportService createBugReportService();

	protected abstract FlexoServerInstanceManager createFlexoServerInstanceManager();

	protected abstract ResourceConsistencyService createResourceConsistencyService();

	protected ModuleLoader createModuleLoader() {
		return new ModuleLoader(this);
	}

	@Override
	protected FlexoResourceCenterService createResourceCenterService(boolean enableDirectoryWatching) {
		FlexoResourceCenterService returned = DefaultResourceCenterService.getNewInstance(enableDirectoryWatching, Flexo.isDev);
		return returned;
	}

	@Override
	public void notify(FlexoService caller, ServiceNotification notification) {
		super.notify(caller, notification);
		// Little hack to handle rc location saving
		// TODO: Should be removed when preferences will be a service see OPENFLEXO-651
		if (notification instanceof ResourceCenterListShouldBeStored && caller instanceof FlexoResourceCenterService) {
			List<File> rcList = new ArrayList<>();
			for (FlexoResourceCenter<?> rc : ((FlexoResourceCenterService) caller).getResourceCenters()) {
				if (rc instanceof DirectoryResourceCenter) {
					rcList.add(((DirectoryResourceCenter) rc).getRootDirectory());
				}
				else if (rc instanceof JarResourceCenter) {
					rcList.add(new File(((JarResourceCenter) rc).getJarResourceImpl().getRelativePath()));
				}
			}
			// TODO: We should be able to initialize preference service if it does not already exist
			// but that's not possible calling registerPreferencesService cause a null pointer that seems
			// to be connected to TA not completely initialized
			if (getGeneralPreferences() != null) {
				getGeneralPreferences().setDirectoryResourceCenterList(rcList);
			}
		}
		else if (notification instanceof DefaultPackageResourceCenterIsNotInstalled && caller instanceof FlexoResourceCenterService) {
			defaultPackagedResourceCenterIsNotInstalled = true;
		}
	}

	@Override
	public AddResourceCenterTask resourceCenterAdded(FlexoResourceCenter<?> resourceCenter) {
		logger.info("Instantiate and execute new AddResourceCenterTask with " + resourceCenter);
		AddResourceCenterTask addRCTask = new AddResourceCenterTask(getResourceCenterService(), resourceCenter);
		getTaskManager().scheduleExecution(addRCTask);
		return addRCTask;
	}

	@Override
	public RemoveResourceCenterTask resourceCenterRemoved(FlexoResourceCenter<?> resourceCenter) {
		RemoveResourceCenterTask removeRCTask = new RemoveResourceCenterTask(getResourceCenterService(), resourceCenter);
		getTaskManager().scheduleExecution(removeRCTask);
		return removeRCTask;
	}

	private Map<TechnologyAdapter<?>, ActivateTechnologyAdapterTask<?>> activatingTechnologyAdapters;

	@Override
	public <TA extends TechnologyAdapter<TA>> ActivateTechnologyAdapterTask<TA> activateTechnologyAdapter(TA technologyAdapter,
			boolean performNowInThisThread) {

		if (technologyAdapter.isActivated()) {
			// Already activated
			return null;
		}

		if (technologyAdapter.isActivating()) {
			// Already activating
			return null;
		}

		logger.fine("********** Activating technology adapter " + technologyAdapter);

		if (performNowInThisThread) {
			super.activateTechnologyAdapter(technologyAdapter, true);
			return null;
		}

		// We try here to prevent activate all TA concurrently

		if (activatingTechnologyAdapters == null) {
			activatingTechnologyAdapters = new HashMap<>();
		}
		if (activatingTechnologyAdapters.get(technologyAdapter) != null) {
			// About to be activated. No need to go further
			return null;
		}
		ActivateTechnologyAdapterTask<TA> activateTATask = new ActivateTechnologyAdapterTask<>(getTechnologyAdapterService(),
				technologyAdapter);
		for (TechnologyAdapter<?> ta : activatingTechnologyAdapters.keySet()) {
			activateTATask.addToDependantTasks(activatingTechnologyAdapters.get(ta));
			// System.out.println("> Waiting " + ta);
		}

		activatingTechnologyAdapters.put(technologyAdapter, activateTATask);

		getTaskManager().scheduleExecution(activateTATask);
		return activateTATask;
	}

	@Override
	public void hasActivated(TechnologyAdapter<?> technologyAdapter) {
		super.hasActivated(technologyAdapter);
		activatingTechnologyAdapters.remove(technologyAdapter);
	}

	@Override
	public <TA extends TechnologyAdapter<TA>> DisactivateTechnologyAdapterTask<TA> disactivateTechnologyAdapter(TA technologyAdapter) {
		if (!technologyAdapter.isActivated()) {
			return null;
		}
		DisactivateTechnologyAdapterTask<TA> disactivateTATask = new DisactivateTechnologyAdapterTask<>(getTechnologyAdapterService(),
				technologyAdapter);
		getTaskManager().scheduleExecution(disactivateTATask);
		return disactivateTATask;
	}

	private boolean defaultPackagedResourceCenterIsNotInstalled;

	public boolean defaultPackagedResourceCenterIsToBeInstalled() {
		return defaultPackagedResourceCenterIsNotInstalled;
	}

	public GeneralPreferences getGeneralPreferences() {
		if (getPreferencesService() != null) {
			return getPreferencesService().getGeneralPreferences();
		}
		return null;
	}

	public PresentationPreferences getPresentationPreferences() {
		if (getPreferencesService() != null) {
			return getPreferencesService().getPresentationPreferences();
		}
		return null;
	}

	public AdvancedPrefs getAdvancedPrefs() {
		if (getPreferencesService() != null) {
			return getPreferencesService().getAdvancedPrefs();
		}
		return null;
	}

	public LoggingPreferences getLoggingPreferences() {
		if (getPreferencesService() != null) {
			return getPreferencesService().getLoggingPreferences();
		}
		return null;
	}

	public BugReportPreferences getBugReportPreferences() {
		if (getPreferencesService() != null) {
			return getPreferencesService().getBugReportPreferences();
		}
		return null;
	}

	@Override
	protected FlexoTaskManager createTaskManager() {
		FlexoTaskManager returned = super.createTaskManager();
		// Unused TaskManagerPanel taskManagerPanel =
		new TaskManagerPanel(returned);
		return returned;
	}

}
