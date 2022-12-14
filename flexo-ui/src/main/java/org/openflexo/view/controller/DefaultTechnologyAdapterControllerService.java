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

package org.openflexo.view.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.FlexoServiceManager.ServiceRegistered;
import org.openflexo.foundation.FlexoServiceManager.TechnologyAdapterHasBeenActivated;
import org.openflexo.foundation.FlexoServiceManager.TechnologyAdapterHasBeenDisactivated;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.nature.ProjectNatureService;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceLoaded;
import org.openflexo.foundation.resource.ResourceUnloaded;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.gina.controller.CustomTypeEditor;
import org.openflexo.module.ModuleLoader.ModuleLoaded;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * Default implementation for {@link ProjectNatureService}
 * 
 * @author sylvain
 * 
 */
public abstract class DefaultTechnologyAdapterControllerService extends FlexoServiceImpl implements TechnologyAdapterControllerService {

	private static final Logger logger = Logger.getLogger(DefaultTechnologyAdapterControllerService.class.getPackage().getName());

	private Map<Class<?>, TechnologyAdapterController<?>> loadedAdapters;

	public static TechnologyAdapterControllerService getNewInstance() {
		try {
			PamelaModelFactory factory = new PamelaModelFactory(TechnologyAdapterControllerService.class);
			factory.setImplementingClassForInterface(DefaultTechnologyAdapterControllerService.class,
					TechnologyAdapterControllerService.class);
			return factory.newInstance(TechnologyAdapterControllerService.class);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Load all available technology adapters<br>
	 * Retrieve all {@link TechnologyAdapter} available from classpath. <br>
	 * Map contains the TechnologyAdapter class name as key and the TechnologyAdapter itself as value.
	 */
	private void loadAvailableTechnologyAdapterControllers() {
		if (loadedAdapters == null) {
			loadedAdapters = new Hashtable<>();
			logger.info("Loading available technology adapter controllers...");
			System.getProperty("java.class.path");
			for (TechnologyAdapterController<?> technologyAdapterController : ServiceLoader.load(TechnologyAdapterController.class))
				registerTechnologyAdapterController(technologyAdapterController);
			logger.info("Loading available technology adapters. Done.");
		}

	}

	private void registerTechnologyAdapterController(TechnologyAdapterController<?> technologyAdapterController) {
		logger.fine("Loading " + technologyAdapterController.getClass());
		technologyAdapterController.setTechnologyAdapterService(this);

		if (loadedAdapters.containsKey(technologyAdapterController.getClass())) {
			logger.severe("Cannot include TechnologyAdapter with classname '" + technologyAdapterController.getClass().getName()
					+ "' because it already exists !!!! A TechnologyAdapter name MUST be unique !");
		}
		else {
			loadedAdapters.put(technologyAdapterController.getClass(), technologyAdapterController);
		}
		logger.info("Loaded " + technologyAdapterController.getClass());
	}

	/**
	 * Load all available technology adapters plugins<br>
	 * Retrieve all {@link TechnologyAdapterPluginController} available from classpath. <br>
	 * 
	 */
	private void loadAvailableTechnologyAdapterPlugins() {
		logger.info("Loading available technology adapter plugins...");
		for (TechnologyAdapterPluginController<?> plugin : ServiceLoader.load(TechnologyAdapterPluginController.class)) {
			registerTechnologyAdapterPlugin(plugin);
		}
		logger.info("Loading available technology adapter plugins. Done.");
	}

	private void registerTechnologyAdapterPlugin(TechnologyAdapterPluginController<?> technologyAdapterPlugin) {
		logger.fine("Loading plugin " + technologyAdapterPlugin.getClass());
		technologyAdapterPlugin.setServiceManager(getServiceManager());
		technologyAdapterPlugin.getTargetTechnologyAdapterController().addToTechnologyAdapterPlugins(technologyAdapterPlugin);
	}

	/**
	 * Return loaded technology adapter controller mapping supplied class<br>
	 * If adapter is not loaded, return null
	 * 
	 * @param technologyAdapterClass
	 * @return
	 */
	@Override
	public <TAC extends TechnologyAdapterController<TA>, TA extends TechnologyAdapter<TA>> TAC getTechnologyAdapterController(
			Class<TAC> technologyAdapterControllerClass) {
		return (TAC) loadedAdapters.get(technologyAdapterControllerClass);
	}

	/**
	 * Return loaded technology adapter controller mapping supplied technology adapter<br>
	 * If adapter is not loaded, return null
	 * 
	 * @param technologyAdapterClass
	 * @return
	 */
	@Override
	public <TAC extends TechnologyAdapterController<TA>, TA extends TechnologyAdapter<TA>> TAC getTechnologyAdapterController(
			TA technologyAdapter) {
		for (TechnologyAdapterController<?> tac : loadedAdapters.values()) {
			if (tac.getTechnologyAdapter() == technologyAdapter) {
				return (TAC) tac;
			}
		}
		/*ApplicationContext app = (ApplicationContext)getServiceManager();
		ServiceLoader<TechnologyAdapterController> loader = (ServiceLoader<TechnologyAdapterController>) app.getFlexoUpdateService().load(TechnologyAdapterController.class);
		Iterator<TechnologyAdapterController> iterator = loader.iterator();
		while (iterator.hasNext()) {
			TechnologyAdapterController technologyAdapterController = iterator.next();
			if(!loadedAdapters.containsKey(technologyAdapterController.getClass())){
				registerTechnologyAdapterController(technologyAdapterController);
				return (TAC) technologyAdapterController;
			}
		
		}*/

		return null;
	}

	/**
	 * Iterates over loaded technology adapters
	 * 
	 * @return
	 */
	@Override
	public Collection<TechnologyAdapterController<?>> getLoadedAdapterControllers() {
		return loadedAdapters.values();
	}

	private <RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter<TA>> void resourceLoaded(
			TechnologyAdapterResource<RD, TA> r) {
		TA ta = r.getTechnologyAdapter();
		// System.out.println("Loaded resource " + r + " for TA " + ta);
		TechnologyAdapterController<TA> tac = getTechnologyAdapterController(ta);
		if (tac != null) {
			tac.resourceLoading(r);
		}
	}

	private <RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter<TA>> void resourceUnloaded(
			TechnologyAdapterResource<RD, TA> r) {
		TA ta = r.getTechnologyAdapter();
		// System.out.println("Unloaded resource " + r + " for TA " + ta);
		TechnologyAdapterController<TA> tac = getTechnologyAdapterController(ta);
		tac.resourceUnloaded(r);
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("*************** TechnologyAdapterController service received notification " + notification + " from " + caller);
		}

		if (notification instanceof ResourceLoaded) {
			FlexoResource<?> r = ((ResourceLoaded) notification).getLoadedResource();
			if (r instanceof TechnologyAdapterResource) {
				resourceLoaded((TechnologyAdapterResource<?, ?>) r);
			}
		}
		if (notification instanceof ResourceUnloaded) {
			FlexoResource<?> r = ((ResourceUnloaded) notification).getUnloadedResource();
			if (r instanceof TechnologyAdapterResource) {
				resourceUnloaded((TechnologyAdapterResource<?, ?>) r);
			}
		}
		if (notification instanceof ModuleLoaded) {

			// When a module is loaded, register all loaded technology adapter controllers with new new loaded module action initializer
			// The newly loaded module will be able to provide all tooling provided by the technology adapter

			// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!! On vient de charger le module " + notification);

			// We have to start with the FMLTechnologyAdapter, if it exists
			TechnologyAdapter ta = getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
			if (ta != null) {
				TechnologyAdapterController<?> adapterController = this.getTechnologyAdapterController(ta);
				// System.out.println("Activated " + adapterController.getTechnologyAdapter() + " ? " +
				// adapterController.isActivated());
				if (ta.isActivated()) {
					Progress.progress(getLocales().localizedForKey("initialize_actions_for_technology_adapter") + " "
							+ adapterController.getTechnologyAdapter().getName());
					adapterController.activate(((ModuleLoaded) notification).getLoadedModule());
				}
			}

			for (TechnologyAdapterController<?> adapterController : getLoadedAdapterControllers()) {
				ta = adapterController.getTechnologyAdapter();
				if (!(ta instanceof FMLTechnologyAdapter)) {
					// System.out.println("Activated " + adapterController.getTechnologyAdapter() + " ? " +
					// adapterController.isActivated());
					if (ta.isActivated()) {
						Progress.progress(getLocales().localizedForKey("initialize_actions_for_technology_adapter") + " "
								+ adapterController.getTechnologyAdapter().getName());
						adapterController.activate(((ModuleLoaded) notification).getLoadedModule());
					}
				}
			}
		}

		if (caller instanceof TechnologyAdapterService) {
			if (notification instanceof ServiceRegistered) {
				/*for (FlexoResourceCenter rc : getResourceCenters()) {
					rc.initialize((TechnologyAdapterService) caller);
				}*/
			}
			else if (notification instanceof TechnologyAdapterHasBeenActivated) {
				activateTechnology(((TechnologyAdapterHasBeenActivated<?>) notification).getTechnologyAdapter());
			}
			else if (notification instanceof TechnologyAdapterHasBeenDisactivated) {
				disactivateTechnology(((TechnologyAdapterHasBeenActivated<?>) notification).getTechnologyAdapter());
			}
		}

	}

	@Override
	public String getServiceName() {
		return "TechnologyAdapterControllerService";
	}

	@Override
	public void initialize() {
		loadAvailableTechnologyAdapterControllers();
		for (TechnologyAdapter ta : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
			if (ta.isActivated()) {
				TechnologyAdapterController<?> tac = getTechnologyAdapterController(ta);
				if (tac != null) {
					tac.activate();
				}
			}
		}
		loadAvailableTechnologyAdapterPlugins();
		status = Status.Started;
	}

	/**
	 * Enable a {@link TechnologyAdapter}<br>
	 * The {@link FlexoResourceCenter} should scan the resources that it may interpret
	 * 
	 * @param technologyAdapter
	 */
	@Override
	public void activateTechnology(TechnologyAdapter<?> technologyAdapter) {
		TechnologyAdapterController<?> tac = getTechnologyAdapterController((TechnologyAdapter) technologyAdapter);
		if (tac != null) {
			tac.activate();
		}

		for (TechnologyAdapterController<?> technologyAdapterController : loadedAdapters.values()) {
			technologyAdapterController.activateActivablePlugins();
		}

		getServiceManager().notify(this, getServiceManager().new TechnologyAdapterHasBeenActivated<>(technologyAdapter));
	}

	/**
	 * Disable a {@link TechnologyAdapter}<br>
	 * The {@link FlexoResourceCenter} is notified to free the resources that it is managing, if possible
	 * 
	 * @param technologyAdapter
	 */
	@Override
	public void disactivateTechnology(TechnologyAdapter<?> technologyAdapter) {
		getTechnologyAdapterController((TechnologyAdapter) technologyAdapter).disactivate();
		getServiceManager().notify(this, getServiceManager().new TechnologyAdapterHasBeenDisactivated<>(technologyAdapter));
	}

	/**
	 * Return boolean indicating if this TechnologyAdapter controller service support ModuleView rendering for supplied technology object
	 * 
	 * @param object
	 * @return
	 */
	/*@Override
	public <TA extends TechnologyAdapter<TA>> boolean hasModuleViewForObject(TechnologyObject<TA> object, FlexoController controller,
			FlexoPerspective perspective) {
		TA technologyAdapter = object.getTechnologyAdapter();
		TechnologyAdapterController<TA> taController = getTechnologyAdapterController(technologyAdapter);
		return taController.hasModuleViewForObject(object, controller,perspective);
	}*/
	
	@Override
	public <TA extends TechnologyAdapter<TA>> boolean isRepresentableInModuleView(TechnologyObject<TA> object) {
		TA technologyAdapter = object.getTechnologyAdapter();
		TechnologyAdapterController<TA> taController = getTechnologyAdapterController(technologyAdapter);
		return taController.isRepresentableInModuleView(object);
	}
	
	@Override
	public <TA extends TechnologyAdapter<TA>> FlexoObject getRepresentableMasterObject(TechnologyObject<TA> object) {
		TA technologyAdapter = object.getTechnologyAdapter();
		TechnologyAdapterController<TA> taController = getTechnologyAdapterController(technologyAdapter);
		return taController.getRepresentableMasterObject(object);
	}

	@Override
	public <TA extends TechnologyAdapter<TA>> ModuleView<?> createModuleViewForMasterObject(TechnologyObject<TA> object,
			FlexoController controller, FlexoPerspective perspective) {
		TA technologyAdapter = object.getTechnologyAdapter();
		TechnologyAdapterController<TA> taController = getTechnologyAdapterController(technologyAdapter);
		return taController.createModuleViewForMasterObject(object, controller, perspective);
	}

	public <TA extends TechnologyAdapter<TA>> String getWindowTitleforObject(TechnologyObject<TA> object, FlexoController controller) {
		TA technologyAdapter = object.getTechnologyAdapter();
		TechnologyAdapterController<TA> taController = getTechnologyAdapterController(technologyAdapter);
		return taController.getWindowTitleforObject(object, controller);
	}

	@Override
	public <T extends CustomType> CustomTypeEditor<T> getCustomTypeEditor(Class<T> typeClass) {

		for (TechnologyAdapter<?> ta : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
			if (ta.isActivated()) {
				TechnologyAdapterController<?> tac = getTechnologyAdapterController((TechnologyAdapter) ta);
				CustomTypeEditor<T> returned = tac.getCustomTypeEditor(typeClass);
				if (returned != null) {
					return returned;
				}
			}
		}
		return null;
	}

	/**
	 * Return singleton instance of supplied Plugin class
	 * 
	 * @param <P>
	 * @param pluginClass
	 * @return
	 */
	@Override
	public <P extends TechnologyAdapterPluginController<?>> P getPlugin(Class<P> pluginClass) {
		Class<? extends TechnologyAdapter> taClass = (Class<? extends TechnologyAdapter>) TypeUtils
				.getBaseClass(TypeUtils.getTypeArgument(pluginClass, TechnologyAdapterPluginController.class, 0));
		if (taClass != null) {
			TechnologyAdapter ta = getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(taClass);
			return (P) getTechnologyAdapterController(ta).getPlugin(pluginClass);
		}
		return null;
	}

	/**
	 * Return the list of all activated {@link TechnologyAdapterPluginController}
	 * 
	 * @return
	 */
	@Override
	public List<TechnologyAdapterPluginController<?>> getActivatedPlugins() {
		List<TechnologyAdapterPluginController<?>> returned = new ArrayList<>();
		for (TechnologyAdapterController<?> technologyAdapterController : loadedAdapters.values()) {
			returned.addAll(technologyAdapterController.getPlugins());
		}
		return returned;
	}

}
