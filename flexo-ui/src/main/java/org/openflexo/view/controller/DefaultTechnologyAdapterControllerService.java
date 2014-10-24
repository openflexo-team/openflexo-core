package org.openflexo.view.controller;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.nature.ProjectNatureService;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.module.ModuleLoader.ModuleLoaded;
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

	private Map<Class, TechnologyAdapterController<?>> loadedAdapters;

	public static TechnologyAdapterControllerService getNewInstance() {
		try {
			ModelFactory factory = new ModelFactory(TechnologyAdapterControllerService.class);
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
	 * 
	 * @return the retrieved TechnologyModuleDefinition map.
	 */
	private void loadAvailableTechnologyAdapterControllers() {
		if (loadedAdapters == null) {
			loadedAdapters = new Hashtable<Class, TechnologyAdapterController<?>>();
			logger.info("Loading available technology adapter controllers...");
			ServiceLoader<TechnologyAdapterController> loader = ServiceLoader.load(TechnologyAdapterController.class);
			Iterator<TechnologyAdapterController> iterator = loader.iterator();
			System.getProperty("java.class.path");
			while (iterator.hasNext()) {
				TechnologyAdapterController technologyAdapterController = iterator.next();
				registerTechnologyAdapterController(technologyAdapterController);
			}
			logger.info("Loading available technology adapters. Done.");
		}

	}

	private void registerTechnologyAdapterController(TechnologyAdapterController<?> technologyAdapterController) {
		logger.info("Loading " + technologyAdapterController.getClass());
		technologyAdapterController.setTechnologyAdapterService(this);

		if (loadedAdapters.containsKey(technologyAdapterController.getClass())) {
			logger.severe("Cannot include TechnologyAdapter with classname '" + technologyAdapterController.getClass().getName()
					+ "' because it already exists !!!! A TechnologyAdapter name MUST be unique !");
		} else {
			loadedAdapters.put(technologyAdapterController.getClass(), technologyAdapterController);
		}
		logger.info("Loaded " + technologyAdapterController.getClass());
	}

	/**
	 * Return loaded technology adapter controller mapping supplied class<br>
	 * If adapter is not loaded, return null
	 * 
	 * @param technologyAdapterClass
	 * @return
	 */
	@Override
	public <TAC extends TechnologyAdapterController<TA>, TA extends TechnologyAdapter> TAC getTechnologyAdapterController(
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
	public <TAC extends TechnologyAdapterController<TA>, TA extends TechnologyAdapter> TAC getTechnologyAdapterController(
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
	public Collection<TechnologyAdapterController<?>> getLoadedAdapterControllers() {
		return loadedAdapters.values();
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		logger.fine("TechnologyAdapterController service received notification " + notification + " from " + caller);
		if (notification instanceof ModuleLoaded) {
			// When a module is loaded, register all loaded technology adapter controllers with new new loaded module action initializer
			// The newly loaded module will be able to provide all tooling provided by the technology adapter
			for (TechnologyAdapterController<?> adapterController : getLoadedAdapterControllers()) {
				Progress.progress(FlexoLocalization.localizedForKey("initialize_actions_for_technology_adapter") + " "
						+ adapterController.getTechnologyAdapter().getName());
				adapterController.initializeActions(((ModuleLoaded) notification).getLoadedModule().getController()
						.getControllerActionInitializer());
			}
		}
	}

	@Override
	public void initialize() {
		loadAvailableTechnologyAdapterControllers();
		for (TechnologyAdapterController<?> ta : getLoadedAdapterControllers()) {
			ta.initialize();
		}
	}

	/**
	 * Return boolean indicating if this TechnologyAdapter controller service support ModuleView rendering for supplied technology object
	 * 
	 * @param object
	 * @return
	 */
	@Override
	public <TA extends TechnologyAdapter> boolean hasModuleViewForObject(TechnologyObject<TA> object, FlexoController controller) {
		TA technologyAdapter = object.getTechnologyAdapter();
		TechnologyAdapterController<TA> taController = getTechnologyAdapterController(technologyAdapter);
		return taController.hasModuleViewForObject(object, controller);
	}

	@Override
	public <TA extends TechnologyAdapter> ModuleView<?> createModuleViewForObject(TechnologyObject<TA> object, FlexoController controller,
			FlexoPerspective perspective) {
		TA technologyAdapter = object.getTechnologyAdapter();
		TechnologyAdapterController<TA> taController = getTechnologyAdapterController(technologyAdapter);
		return taController.createModuleViewForObject(object, controller, perspective);
	}

	public <TA extends TechnologyAdapter> String getWindowTitleforObject(TechnologyObject<TA> object, FlexoController controller) {
		TA technologyAdapter = object.getTechnologyAdapter();
		TechnologyAdapterController<TA> taController = getTechnologyAdapterController(technologyAdapter);
		return taController.getWindowTitleforObject(object, controller);
	}

}
