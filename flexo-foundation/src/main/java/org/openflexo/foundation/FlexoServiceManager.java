/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoService.ServiceNotification;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.localization.LocalizationService;
import org.openflexo.foundation.nature.ProjectNatureService;
import org.openflexo.foundation.nature.ScreenshotService;
import org.openflexo.foundation.project.FlexoProjectImpl.FlexoProjectReferenceLoader;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ProjectClosed;
import org.openflexo.foundation.resource.ProjectLoaded;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.FlexoTaskManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;

/**
 * The {@link FlexoServiceManager} provides Run-Time environment for executing Openflexo infrastructure.
 * 
 * Implementation of a manager of {@link FlexoService}<br>
 * All {@link FlexoService} are registered in the {@link FlexoServiceManager} which broadcast all service events to all services<br>
 * 
 * @author sylvain
 * 
 */
public abstract class FlexoServiceManager {

	protected static final Logger logger = Logger.getLogger(FlexoServiceManager.class.getPackage().getName());

	private final ArrayList<FlexoService> registeredServices;

	public FlexoServiceManager() {
		registeredServices = new ArrayList<>();
	}

	/**
	 * Register the supplied service, by adding it in the list of all services managed by this {@link FlexoServiceManager} instance<br>
	 * Notify all already registered services that a new service has been registered, then initialize the service itself.
	 * 
	 * @param service
	 */
	public void registerService(FlexoService service) {
		if (service != null) {
			registeredServices.add(service);
			service.register(this);

			notify(service, new ServiceRegistered());

			service.initialize();
		}
		else {
			logger.warning("Trying to register null FlexoService");
		}
	}

	public void notify(FlexoService caller, ServiceNotification notification) {
		for (FlexoService s : new ArrayList<>(registeredServices)) {
			if (s != caller) {
				s.receiveNotification(caller, notification);
			}
		}
		if (notification instanceof ProjectLoaded) {
			resourceCenterAdded(((ProjectLoaded) notification).getProject());
			for (TechnologyAdapter ta : ((ProjectLoaded) notification).getProject().getRequiredTechnologyAdapters()) {
				activateTechnologyAdapter(ta, false);
			}
		}
		if (notification instanceof ProjectClosed) {
			resourceCenterRemoved(((ProjectClosed<?>) notification).getProject());
		}
	}

	public FlexoTask resourceCenterAdded(FlexoResourceCenter<?> resourceCenter) {
		getResourceCenterService().addToResourceCenters(resourceCenter);
		return null;
	}

	public FlexoTask resourceCenterRemoved(FlexoResourceCenter<?> resourceCenter) {
		getResourceCenterService().removeFromResourceCenters(resourceCenter);
		return null;
	}

	/**
	 * Enable a {@link TechnologyAdapter}<br>
	 * All resources centers are notified to scan the resources that they may interpret
	 * 
	 * @param technologyAdapter
	 */
	public <TA extends TechnologyAdapter<TA>> FlexoTask activateTechnologyAdapter(TA technologyAdapter, boolean performNowInThisThread) {
		if (technologyAdapter.isActivated()) {
			return null;
		}
		technologyAdapter.activate();
		notify(getTechnologyAdapterService(), new TechnologyAdapterHasBeenActivated<>(technologyAdapter));
		return null;
	}

	/**
	 * Disable a {@link TechnologyAdapter}<br>
	 * All resources centers are notified to free the resources that they are managing, if possible
	 * 
	 * @param technologyAdapter
	 */
	public <TA extends TechnologyAdapter<TA>> FlexoTask disactivateTechnologyAdapter(TA technologyAdapter) {
		if (!technologyAdapter.isActivated()) {
			return null;
		}
		technologyAdapter.disactivate();
		notify(getTechnologyAdapterService(), new TechnologyAdapterHasBeenDisactivated<>(technologyAdapter));
		return null;
	}

	/**
	 * Callback when a {@link TechnologyAdapter} has finished activating
	 * 
	 * @param technologyAdapter
	 */
	public void hasActivated(TechnologyAdapter<?> technologyAdapter) {
	}

	@SuppressWarnings("unchecked")
	public <S extends FlexoService> S getService(Class<S> serviceClass) {
		for (FlexoService s : registeredServices) {
			if (serviceClass.isAssignableFrom(s.getClass())) {
				return (S) s;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <S extends FlexoService> S getService(String serviceName) {
		for (FlexoService s : registeredServices) {
			if (s.getServiceName().equals(serviceName)) {
				return (S) s;
			}
		}
		return null;
	}

	public List<FlexoService> getRegisteredServices() {
		return registeredServices;
	}

	public void stopAllServices() {
		for (FlexoService r : registeredServices) {
			r.stop();
		}
	}

	public LocalizationService getLocalizationService() {
		return getService(LocalizationService.class);
	}

	public FlexoEditingContext getEditingContext() {
		return getService(FlexoEditingContext.class);
	}

	public TechnologyAdapterService getTechnologyAdapterService() {
		return getService(TechnologyAdapterService.class);
	}

	public FlexoResourceCenterService getResourceCenterService() {
		return getService(FlexoResourceCenterService.class);
	}

	public ProjectNatureService getProjectNatureService() {
		return getService(ProjectNatureService.class);
	}

	public VirtualModelLibrary getVirtualModelLibrary() {
		return getService(VirtualModelLibrary.class);
	}

	public FlexoProjectReferenceLoader getProjectReferenceLoader() {
		return getService(FlexoProjectReferenceLoader.class);
	}

	public ProjectLoader getProjectLoaderService() {
		return getService(ProjectLoader.class);
	}

	public ResourceManager getResourceManager() {
		return getService(ResourceManager.class);
	}

	public FlexoTaskManager getTaskManager() {
		return getService(FlexoTaskManager.class);
	}

	public ScreenshotService getScreenshotService() {
		return getService(ScreenshotService.class);
	}

	public class ServiceRegistered implements ServiceNotification {
	}

	/**
	 * Notification of a TechnologyAdapter that has been activated
	 * 
	 * @author sylvain
	 * 
	 */
	public class TechnologyAdapterHasBeenActivated<TA extends TechnologyAdapter<TA>> implements ServiceNotification {
		private final TechnologyAdapter<TA> technologyAdapter;

		public TechnologyAdapterHasBeenActivated(TechnologyAdapter<TA> technologyAdapter) {
			this.technologyAdapter = technologyAdapter;
		}

		public TechnologyAdapter<TA> getTechnologyAdapter() {
			return technologyAdapter;
		}
	}

	/**
	 * Notification of a TechnologyAdapter that has been disactivated
	 * 
	 * @author sylvain
	 * 
	 */
	public class TechnologyAdapterHasBeenDisactivated<TA extends TechnologyAdapter<TA>> implements ServiceNotification {
		private final TechnologyAdapter<TA> technologyAdapter;

		public TechnologyAdapterHasBeenDisactivated(TechnologyAdapter<TA> technologyAdapter) {
			this.technologyAdapter = technologyAdapter;
		}

		public TechnologyAdapter<TA> getTechnologyAdapter() {
			return technologyAdapter;
		}
	}

	protected abstract FlexoEditingContext createEditingContext();

	protected abstract FlexoEditor createApplicationEditor();

	protected abstract FlexoProjectReferenceLoader createProjectReferenceLoader();

	protected abstract FlexoResourceCenterService createResourceCenterService(boolean enableDirectoryWatching);

	protected abstract TechnologyAdapterService createTechnologyAdapterService(FlexoResourceCenterService flexoResourceCenterService);

	protected abstract ProjectNatureService createProjectNatureService();

	protected abstract VirtualModelLibrary createViewPointLibraryService();

	protected abstract LocalizationService createLocalizationService(String relativePath);

	protected abstract ResourceManager createResourceManager();

	protected abstract FlexoTaskManager createTaskManager();

	protected abstract ScreenshotService createScreenshotService();

	protected abstract ProjectLoader createProjectLoaderService();

	public FlexoEditor getDefaultEditor() {
		return null;
	}

}
