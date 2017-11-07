/**
 * 
 * Copyright (c) 2013-2015, Openflexo
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

package org.openflexo.foundation.technologyadapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.annotations.DeclareEditionActions;
import org.openflexo.foundation.fml.annotations.DeclareFetchRequests;
import org.openflexo.foundation.fml.annotations.DeclareFlexoBehaviours;
import org.openflexo.foundation.fml.annotations.DeclareFlexoRoles;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.nature.ProjectNatureService;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterAdded;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterRemoved;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceRepository;
import org.openflexo.foundation.resource.ResourceRepositoryImpl;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.Progress;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

/**
 * Default implementation for {@link ProjectNatureService}
 * 
 * @author sylvain
 * 
 */
public abstract class DefaultTechnologyAdapterService extends FlexoServiceImpl implements TechnologyAdapterService {

	private static final Logger logger = Logger.getLogger(DefaultTechnologyAdapterService.class.getPackage().getName());

	private Map<Class<?>, TechnologyAdapter> loadedAdapters;

	private Map<Class<? extends ModelSlot<?>>, List<Class<? extends FlexoRole<?>>>> availableFlexoRoleTypes;
	private Map<Class<? extends ModelSlot<?>>, List<Class<? extends FlexoBehaviour>>> availableFlexoBehaviourTypes;
	private Map<Class<? extends ModelSlot<?>>, List<Class<? extends TechnologySpecificAction<?, ?, ?>>>> availableEditionActionTypes;
	private Map<Class<? extends ModelSlot<?>>, List<Class<? extends FetchRequest<?, ?, ?>>>> availableFetchRequestActionTypes;

	// private Map<Class<? extends ModelSlot<?>>, List<Class<? extends FlexoBehaviourParameter>>> availableFlexoBehaviourParameterTypes;
	// private Map<Class<? extends ModelSlot<?>>, List<Class<? extends InspectorEntry>>> availableInspectorEntryTypes;

	public static TechnologyAdapterService getNewInstance(FlexoResourceCenterService resourceCenterService) {
		try {
			ModelFactory factory = new ModelFactory(TechnologyAdapterService.class);
			factory.setImplementingClassForInterface(DefaultTechnologyAdapterService.class, TechnologyAdapterService.class);
			TechnologyAdapterService returned = factory.newInstance(TechnologyAdapterService.class);
			returned.setFlexoResourceCenterService(resourceCenterService);
			// returned.loadAvailableTechnologyAdapters();
			return returned;
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
	public void loadAvailableTechnologyAdapters() {

		// Load all other technology adapters found in the classpath (using java ServiceLoader)
		// (Those TA are found using META-INF informations collected in classpath)
		ServiceLoader<TechnologyAdapter> loader = ServiceLoader.load(TechnologyAdapter.class);

		if (loadedAdapters == null) {

			loadedAdapters = new Hashtable<>();

			logger.info("Loading available technology adapters...");

			// First load the FML technology adapter
			FMLTechnologyAdapter fmlTechnologyAdapter = new FMLTechnologyAdapter();
			registerTechnologyAdapter(fmlTechnologyAdapter);

			// First load the FML@runtime technology adapter
			FMLRTTechnologyAdapter fmlRTTechnologyAdapter = new FMLRTTechnologyAdapter();
			registerTechnologyAdapter(fmlRTTechnologyAdapter);

			// Then the other TA
			Iterator<TechnologyAdapter> iterator = loader.iterator();
			while (iterator.hasNext()) {
				TechnologyAdapter technologyAdapter = iterator.next();
				registerTechnologyAdapter(technologyAdapter);
			}
			logger.info("Loading available technology adapters. Done.");
		}
		else {
			Iterator<TechnologyAdapter> iterator = loader.iterator();
			while (iterator.hasNext()) {
				TechnologyAdapter technologyAdapter = iterator.next();
				if (!loadedAdapters.containsKey(technologyAdapter.getClass())) {
					registerTechnologyAdapter(technologyAdapter);
				}
				logger.info("Loading available technology adapters. Done.");
			}
		}

	}

	private void registerTechnologyAdapter(TechnologyAdapter technologyAdapter) {
		logger.fine("Found " + technologyAdapter);
		technologyAdapter.setTechnologyAdapterService(this);
		addToTechnologyAdapters(technologyAdapter);

		logger.info("Load " + technologyAdapter.getName() + " as " + technologyAdapter.getClass());

		if (loadedAdapters.containsKey(technologyAdapter.getClass())) {
			logger.severe("Cannot include TechnologyAdapter with classname '" + technologyAdapter.getClass().getName()
					+ "' because it already exists !!!! A TechnologyAdapter name MUST be unique !");
		}
		else {
			loadedAdapters.put(technologyAdapter.getClass(), technologyAdapter);
		}
	}

	/**
	 * Return loaded technology adapter mapping supplied class<br>
	 * If adapter is not loaded, return null
	 * 
	 * @param technologyAdapterClass
	 * @return
	 */
	@Override
	public <TA extends TechnologyAdapter> TA getTechnologyAdapter(Class<TA> technologyAdapterClass) {
		return (TA) loadedAdapters.get(technologyAdapterClass);
	}

	/**
	 * Iterates over loaded technology adapters
	 * 
	 * @return
	 */
	public Collection<TechnologyAdapter> getLoadedAdapters() {
		return loadedAdapters.values();
	}

	/**
	 * Return the {@link TechnologyContextManager} for this technology for this technology shared by all {@link FlexoResourceCenter}
	 * declared in the scope of {@link FlexoResourceCenterService}
	 * 
	 * @return
	 */
	@Override
	public TechnologyContextManager<?> getTechnologyContextManager(TechnologyAdapter technologyAdapter) {
		if (technologyAdapter == null) {
			return null;
		}
		return technologyAdapter.getTechnologyContextManager();
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		if (caller instanceof FlexoResourceCenterService) {
			if (notification instanceof ResourceCenterAdded) {
				FlexoResourceCenter<?> rc = ((ResourceCenterAdded) notification).getAddedResourceCenter();
				Progress.progress(getLocales().localizedForKey("initializing") + " " + rc);
				for (TechnologyAdapter ta : getTechnologyAdapters()) {
					if (ta.isActivated()) {
						Progress.progress(getLocales().localizedForKey("scan_resources_for_technology_adapters") + " " + ta.getName());
						ta.resourceCenterAdded(rc);
					}
				}
			}
			if (notification instanceof ResourceCenterRemoved) {
				FlexoResourceCenter<?> rc = ((ResourceCenterRemoved) notification).getRemovedResourceCenter();
				for (TechnologyAdapter ta : getTechnologyAdapters()) {
					ta.resourceCenterRemoved(rc);
				}
			}
		}
	}

	@Override
	public void initialize() {
		availableFlexoRoleTypes = new HashMap<>();
		availableEditionActionTypes = new HashMap<>();
		availableFetchRequestActionTypes = new HashMap<>();
		availableFlexoBehaviourTypes = new HashMap<>();
		loadAvailableTechnologyAdapters();
	}

	/**
	 * Return the list of all non-empty {@link ResourceRepositoryImpl} discovered in the scope of {@link FlexoServiceManager}, related to
	 * technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	@Override
	public List<ResourceRepository<?, ?>> getAllRepositories(TechnologyAdapter technologyAdapter) {
		List<ResourceRepository<?, ?>> returned = new ArrayList<>();
		for (FlexoResourceCenter<?> rc : getFlexoResourceCenterService().getResourceCenters()) {
			Collection<? extends ResourceRepository<?, ?>> repCollection = rc.getRegistedRepositories(technologyAdapter, true);
			if (repCollection != null) {
				returned.addAll(repCollection);
			}
		}
		return returned;
	}

	/**
	 * Return the list of all global {@link ResourceRepositoryImpl} discovered in this {@link InformationSpace}, related to technology as
	 * supplied by {@link TechnologyAdapter} parameter.<br>
	 * One global repository for each {@link FlexoResourceCenter} is returned
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	@Override
	public <TA extends TechnologyAdapter> List<TechnologyAdapterResourceRepository<?, TA, ?, ?>> getGlobalRepositories(
			TA technologyAdapter) {
		List<TechnologyAdapterResourceRepository<?, TA, ?, ?>> returned = new ArrayList<>();
		for (FlexoResourceCenter<?> rc : getFlexoResourceCenterService().getResourceCenters()) {
			returned.add(technologyAdapter.getGlobalRepository(rc));
		}
		return returned;
	}

	/**
	 * Return the list of all non-empty {@link ResourceRepositoryImpl} discovered in the scope of {@link FlexoServiceManager} which may give
	 * access to some instance of supplied resource data class, related to technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	@Override
	public <RD extends ResourceData<RD>> List<ResourceRepository<? extends FlexoResource<RD>, ?>> getAllRepositories(
			TechnologyAdapter technologyAdapter, Class<RD> resourceDataClass) {
		List<ResourceRepository<? extends FlexoResource<RD>, ?>> returned = new ArrayList<>();
		for (FlexoResourceCenter<?> rc : getFlexoResourceCenterService().getResourceCenters()) {
			Collection<? extends ResourceRepository<?, ?>> repCollection = rc.getRegistedRepositories(technologyAdapter, true);
			if (repCollection != null) {
				for (ResourceRepository<?, ?> rep : repCollection) {
					if (resourceDataClass.isAssignableFrom(rep.getResourceDataClass())) {
						returned.add((ResourceRepositoryImpl<? extends FlexoResource<RD>, ?>) rep);
					}
				}
			}
		}
		return returned;
	}

	/**
	 * Return the list of all non-empty {@link ModelRepository} discovered in the scope of {@link FlexoServiceManager}, related to
	 * technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	@Override
	public List<ModelRepository<?, ?, ?, ?, ?, ?>> getAllModelRepositories(TechnologyAdapter technologyAdapter) {
		List<ModelRepository<?, ?, ?, ?, ?, ?>> returned = new ArrayList<>();
		for (FlexoResourceCenter<?> rc : getFlexoResourceCenterService().getResourceCenters()) {
			Collection<? extends ResourceRepository<?, ?>> repCollection = rc.getRegistedRepositories(technologyAdapter, true);
			if (repCollection != null) {
				for (ResourceRepository<?, ?> rep : repCollection) {
					if (rep instanceof ModelRepository) {
						returned.add((ModelRepository<?, ?, ?, ?, ?, ?>) rep);
					}
				}
			}
		}
		return returned;
	}

	/**
	 * Return the list of all non-empty {@link MetaModelRepository} discovered in the scope of {@link FlexoServiceManager}, related to
	 * technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	@Override
	public List<MetaModelRepository<?, ?, ?, ?, ?>> getAllMetaModelRepositories(TechnologyAdapter technologyAdapter) {
		List<MetaModelRepository<?, ?, ?, ?, ?>> returned = new ArrayList<>();
		for (FlexoResourceCenter<?> rc : getFlexoResourceCenterService().getResourceCenters()) {
			Collection<? extends ResourceRepository<?, ?>> repCollection = rc.getRegistedRepositories(technologyAdapter, true);
			if (repCollection != null) {
				for (ResourceRepository<?, ?> rep : repCollection) {
					if (rep instanceof MetaModelRepository) {
						returned.add((MetaModelRepository<?, ?, ?, ?, ?>) rep);
					}
				}
			}
		}
		return returned;
	}

	private final Map<Class<? extends CustomType>, CustomTypeFactory<?>> customTypeFactories = new LinkedHashMap<>();

	/**
	 * Return all {@link CustomType} factories defined for all known technologies
	 * 
	 * @return
	 */
	@Override
	public Map<Class<? extends CustomType>, CustomTypeFactory<?>> getCustomTypeFactories() {
		return customTypeFactories;
	}

	/**
	 * Register CustomTypeFactory
	 * 
	 * @param typeClass
	 * @param factory
	 */
	@Override
	public <T extends CustomType> void registerTypeClass(Class<T> typeClass, CustomTypeFactory<T> factory) {
		// System.out.println("registering " + typeClass + " with " + factory);
		customTypeFactories.put(typeClass, factory);
	}

	/**
	 * Enable a {@link TechnologyAdapter}<br>
	 * All resources centers are notified to scan the resources that they may interpret
	 * 
	 * @param technologyAdapter
	 */
	@Override
	public FlexoTask activateTechnologyAdapter(TechnologyAdapter technologyAdapter) {

		return getServiceManager().activateTechnologyAdapter(technologyAdapter);
	}

	/**
	 * Disable a {@link TechnologyAdapter}<br>
	 * All resources centers are notified to free the resources that they are managing, if possible
	 * 
	 * @param technologyAdapter
	 */
	@Override
	public FlexoTask disactivateTechnologyAdapter(TechnologyAdapter technologyAdapter) {

		return getServiceManager().disactivateTechnologyAdapter(technologyAdapter);
	}

	/**
	 * Return {@link TechnologyAdapter} where supplied modelSlotClass has been declared
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	@Override
	public <MS extends ModelSlot<?>> TechnologyAdapter getTechnologyAdapterForModelSlot(Class<MS> modelSlotClass) {
		if (modelSlotClass == null) {
			return null;
		}
		for (TechnologyAdapter ta : getTechnologyAdapters()) {
			for (Class<? extends ModelSlot<?>> msType : ta.getAvailableModelSlotTypes()) {
				if (modelSlotClass.isAssignableFrom(msType)) {
					return ta;
				}
			}
		}
		return null;
	}

	/**
	 * Return the list of {@link FlexoRole} class available for supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	@Override
	public <MS extends ModelSlot<?>> List<Class<? extends FlexoRole<?>>> getAvailableFlexoRoleTypes(Class<MS> modelSlotClass) {
		List<Class<? extends FlexoRole<?>>> returned = availableFlexoRoleTypes.get(modelSlotClass);
		if (returned == null) {
			returned = new ArrayList<>();
			appendDeclareFlexoRoles(returned, modelSlotClass);
			availableFlexoRoleTypes.put(modelSlotClass, returned);
		}
		return returned;
	}

	/**
	 * Return the list of {@link TechnologySpecificAction} class available for supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	@Override
	public <MS extends ModelSlot<?>> List<Class<? extends TechnologySpecificAction<?, ?, ?>>> getAvailableEditionActionTypes(
			Class<MS> modelSlotClass) {
		List<Class<? extends TechnologySpecificAction<?, ?, ?>>> returned = availableEditionActionTypes.get(modelSlotClass);
		if (returned == null) {
			returned = new ArrayList<>();
			appendEditionActionTypes(returned, modelSlotClass);
			availableEditionActionTypes.put(modelSlotClass, returned);
		}
		return returned;

	}

	/**
	 * Return the list of {@link FetchRequest} class available for supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	@Override
	public <MS extends ModelSlot<?>> List<Class<? extends FetchRequest<?, ?, ?>>> getAvailableFetchRequestActionTypes(
			Class<MS> modelSlotClass) {
		List<Class<? extends FetchRequest<?, ?, ?>>> returned = availableFetchRequestActionTypes.get(modelSlotClass);
		if (returned == null) {
			returned = new ArrayList<>();
			appendFetchRequestActionTypes(returned, modelSlotClass);
			availableFetchRequestActionTypes.put(modelSlotClass, returned);
		}
		return returned;
	}

	/**
	 * Return the list of {@link FlexoBehaviour} class available for supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	@Override
	public <MS extends ModelSlot<?>> List<Class<? extends FlexoBehaviour>> getAvailableFlexoBehaviourTypes(Class<MS> modelSlotClass) {
		List<Class<? extends FlexoBehaviour>> returned = availableFlexoBehaviourTypes.get(modelSlotClass);
		if (returned == null) {
			returned = new ArrayList<>();
			appendFlexoBehaviourTypes(returned, modelSlotClass);
			availableFlexoBehaviourTypes.put(modelSlotClass, returned);
		}
		return returned;
	}

	private static void appendDeclareFlexoRoles(List<Class<? extends FlexoRole<?>>> aList, Class<?> cl) {
		if (cl.isAnnotationPresent(DeclareFlexoRoles.class)) {
			DeclareFlexoRoles allFlexoRoles = cl.getAnnotation(DeclareFlexoRoles.class);
			for (Class<? extends FlexoRole> roleClass : allFlexoRoles.value()) {
				if (!aList.contains(roleClass)) {
					aList.add((Class<FlexoRole<?>>) roleClass);
				}
			}
		}
		if (cl.getSuperclass() != null) {
			appendDeclareFlexoRoles(aList, cl.getSuperclass());
		}

		for (Class<?> superInterface : cl.getInterfaces()) {
			appendDeclareFlexoRoles(aList, superInterface);
		}
	}

	private static void appendEditionActionTypes(List<Class<? extends TechnologySpecificAction<?, ?, ?>>> aList, Class<?> cl) {
		if (cl.isAnnotationPresent(DeclareEditionActions.class)) {
			DeclareEditionActions allEditionActions = cl.getAnnotation(DeclareEditionActions.class);
			for (Class<? extends TechnologySpecificAction> editionActionClass : allEditionActions.value()) {
				if (!aList.contains(editionActionClass)) {
					aList.add((Class<? extends TechnologySpecificAction<?, ?, ?>>) editionActionClass);
				}
			}
		}
		if (cl.getSuperclass() != null) {
			appendEditionActionTypes(aList, cl.getSuperclass());
		}
		for (Class<?> superInterface : cl.getInterfaces()) {
			appendEditionActionTypes(aList, superInterface);
		}
	}

	private static void appendFetchRequestActionTypes(List<Class<? extends FetchRequest<?, ?, ?>>> aList, Class<?> cl) {
		if (cl.isAnnotationPresent(DeclareFetchRequests.class)) {
			DeclareFetchRequests allFetchRequestActions = cl.getAnnotation(DeclareFetchRequests.class);
			for (Class<? extends FetchRequest> fetchRequestClass : allFetchRequestActions.value()) {
				if (!aList.contains(fetchRequestClass)) {
					aList.add((Class) fetchRequestClass);
				}
			}
		}
		if (cl.getSuperclass() != null) {
			appendFetchRequestActionTypes(aList, cl.getSuperclass());
		}
		for (Class<?> superInterface : cl.getInterfaces()) {
			appendFetchRequestActionTypes(aList, superInterface);
		}
	}

	private static void appendFlexoBehaviourTypes(List<Class<? extends FlexoBehaviour>> aList, Class<?> cl) {
		if (cl.isAnnotationPresent(DeclareFlexoBehaviours.class)) {
			DeclareFlexoBehaviours allFlexoBehaviours = cl.getAnnotation(DeclareFlexoBehaviours.class);
			for (Class<? extends FlexoBehaviour> flexoBehaviourClass : allFlexoBehaviours.value()) {
				if (!aList.contains(flexoBehaviourClass)) {
					aList.add(flexoBehaviourClass);
				}
			}
		}
		if (cl.getSuperclass() != null) {
			appendFlexoBehaviourTypes(aList, cl.getSuperclass());
		}
		for (Class<?> superInterface : cl.getInterfaces()) {
			appendFlexoBehaviourTypes(aList, superInterface);
		}
	}

	@Override
	public Class<? extends ModelSlot<?>> getModelSlotClass(Class<? extends FlexoRole<?>> roleClass) {
		for (TechnologyAdapter ta : getTechnologyAdapters()) {
			for (Class<? extends ModelSlot<?>> modelSlotClass : ta.getAvailableModelSlotTypes()) {
				if (getAvailableFlexoRoleTypes(modelSlotClass).contains(roleClass)) {
					return modelSlotClass;
				}
			}
		}
		return null;
	}

}
