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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.annotations.DeclareModelSlots;
import org.openflexo.foundation.fml.annotations.DeclareResourceTypes;
import org.openflexo.foundation.fml.annotations.DeclareVirtualModelInstanceNatures;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceNature;
import org.openflexo.foundation.nature.ProjectNatureService;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.FlexoResourceFactory;
import org.openflexo.foundation.resource.JarResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceRepository;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;

/**
 * This class represents a technology adapter<br>
 * A {@link TechnologyAdapter} is plugin loaded at run-time which defines and implements the required A.P.I used to connect Flexo Modelling
 * Language Virtual Machine to a technology.<br>
 * 
 * Note: this code was partially adapted from Nicolas Daniels (Blue Pimento team)
 * 
 * @author sylvain
 * 
 */
public abstract class TechnologyAdapter extends FlexoObservable {

	private static final Logger logger = Logger.getLogger(TechnologyAdapter.class.getPackage().getName());

	private TechnologyAdapterService technologyAdapterService;
	private TechnologyContextManager<?> technologyContextManager;

	private final List<FlexoResourceFactory<?, ?, ?>> resourceFactories;

	private List<Class<? extends ModelSlot<?>>> availableModelSlotTypes;
	private List<Class<? extends VirtualModelInstanceNature>> availableVirtualModelInstanceNatures;
	private final List<Class<? extends TechnologyAdapterResource<?, ?>>> availableResourceTypes;

	public TechnologyAdapter() {
		resourceFactories = new ArrayList<>();
		availableResourceTypes = new ArrayList<Class<? extends TechnologyAdapterResource<?, ?>>>();
	}

	private LocalizedDelegate locales = null;

	// private List<Class<? extends TechnologySpecificType<?>>> availableTechnologySpecificTypes;

	/**
	 * Return human-understandable name for this technology adapter<br>
	 * Unique id to consider must be the class name
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * Returns applicable {@link ProjectNatureService}
	 * 
	 * @return
	 */
	public TechnologyAdapterService getTechnologyAdapterService() {
		return technologyAdapterService;
	}

	/**
	 * Sets applicable {@link ProjectNatureService}
	 * 
	 * @param technologyAdapterService
	 */
	public void setTechnologyAdapterService(TechnologyAdapterService technologyAdapterService) {
		this.technologyAdapterService = technologyAdapterService;
	}

	/**
	 * Creates and return the {@link TechnologyContextManager} for this technology and for all {@link FlexoResourceCenter} declared in the
	 * scope of {@link FlexoResourceCenterService}
	 * 
	 * @return
	 */
	public abstract TechnologyContextManager<?> createTechnologyContextManager(FlexoResourceCenterService service);

	/**
	 * Return the {@link TechnologyContextManager} for this technology shared by all {@link FlexoResourceCenter} declared in the scope of
	 * {@link FlexoResourceCenterService}
	 * 
	 * @return
	 */
	public TechnologyContextManager<?> getTechnologyContextManager() {
		return technologyContextManager;
	}

	/**
	 * Return the technology-specific binding factory
	 * 
	 * @return
	 */
	public abstract TechnologyAdapterBindingFactory getTechnologyAdapterBindingFactory();

	/**
	 * Called to activate the {@link TechnologyAdapter}
	 */
	public void activate() {
		if (!isActivated()) {
			technologyContextManager = createTechnologyContextManager(getTechnologyAdapterService().getFlexoResourceCenterService());
			initResourceFactories();
			initTechnologySpecificTypes(getTechnologyAdapterService());
			locales = new LocalizedDelegateImpl(ResourceLocator.locateResource(getLocalizationDirectory()),
					getTechnologyAdapterService().getServiceManager().getLocalizationService().getFlexoLocalizer(), true, true);
			loadPrivateResourceCenters();
			isActivated = true;
			getPropertyChangeSupport().firePropertyChange("activated", false, true);
		}
	}

	/**
	 * Called to activate the {@link TechnologyAdapter}
	 */
	public void disactivate() {
		isActivated = false;
	}

	private boolean isActivated = false;

	public boolean isActivated() {
		return isActivated;
	}

	public List<FlexoResourceFactory<?, ?, ?>> getResourceFactories() {
		return resourceFactories;
	}

	public <R extends FlexoResourceFactory<?, ?, ?>> R getResourceFactory(Class<R> resourceFactory) {
		if (!isActivated()) {
			activate();
		}
		for (FlexoResourceFactory<?, ?, ?> frf : getResourceFactories()) {
			if (resourceFactory.isAssignableFrom(frf.getClass())) {
				return (R) frf;
			}
		}
		return null;
	}

	/**
	 * Initialize the supplied resource center with the technology, if not already done
	 * 
	 * @param resourceCenter
	 */
	public final <I> void initializeResourceCenter(FlexoResourceCenter<I> resourceCenter) {
		if (!resourceCentersManagingThisTechnology.contains(resourceCenter)) {
			performInitializeResourceCenter(resourceCenter);
			resourceCentersManagingThisTechnology.add(resourceCenter);
		}

	}

	/**
	 * Initialize the supplied resource center with the technology<br>
	 * ResourceCenter is scanned, ResourceRepositories are created and new technology-specific resources are build and registered.
	 * 
	 * @param resourceCenter
	 */
	public final <I> void performInitializeResourceCenter(FlexoResourceCenter<I> resourceCenter) {

		logger.info("--------> performInitializeResourceCenter " + getName() + " for " + resourceCenter);

		Iterator<I> it = resourceCenter.iterator();

		while (it.hasNext()) {
			I serializationArtefact = it.next();

			/*if (resourceCenter instanceof JarResourceCenter) {
				System.out.println("pour " + serializationArtefact);
			}*/

			if (!isIgnorable(resourceCenter, serializationArtefact)) {
				/*if (resourceCenter instanceof JarResourceCenter) {
					System.out.println("on ignore pas " + serializationArtefact);
				}*/
				for (FlexoResourceFactory<?, ?, ?> resourceFactory : getResourceFactories()) {
					FlexoResource r = tryToLookupResource(resourceFactory, resourceCenter, serializationArtefact);
					if (r != null) {
						System.out.println("Look-up resource " + r.getImplementedInterface().getSimpleName() + " " + r.getURI());
					}
				}
			}
		}

		resourceCenterHasBeenInitialized(resourceCenter);

	}

	protected void resourceCenterHasBeenInitialized(FlexoResourceCenter<?> rc) {
		// Call it to update the current repositories
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// Call it to update the current repositories
					notifyRepositoryStructureChanged();
				}
			});
		}
		else {
			// Call it to update the current repositories
			notifyRepositoryStructureChanged();
		}
	}

	/**
	 * Internally called to lookup resources from serialization artefacts
	 * 
	 * @param resourceFactory
	 * @param resourceCenter
	 * @param serializationArtefact
	 * @return
	 */
	private <RF extends FlexoResourceFactory<R, RD, TA>, R extends TechnologyAdapterResource<RD, TA>, RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter, I> R tryToLookupResource(
			RF resourceFactory, FlexoResourceCenter<I> resourceCenter, I serializationArtefact) {

		TechnologyContextManager<TA> technologyContextManager = (TechnologyContextManager<TA>) getTechnologyContextManager();
		if (resourceFactory.isValidArtefact(serializationArtefact, resourceCenter)) {
			try {
				return resourceFactory.retrieveResource(serializationArtefact, resourceCenter, technologyContextManager);
			} catch (ModelDefinitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public abstract <I> boolean isIgnorable(FlexoResourceCenter<I> resourceCenter, I contents);

	/**
	 * Called when a new serialization artefact has been discovered
	 * 
	 * @param resourceCenter
	 * @param serializationArtefact
	 * @return a boolean indicating if this file has been handled by the technology, when false ResourceCenter might resend notification
	 */
	public final <I> boolean contentsAdded(FlexoResourceCenter<I> resourceCenter, I serializationArtefact) {
		boolean hasBeenLookedUp = false;
		if (!isIgnorable(resourceCenter, serializationArtefact)) {
			for (FlexoResourceFactory<?, ?, ?> resourceFactory : getResourceFactories()) {
				FlexoResource<?> resource = tryToLookupResource(resourceFactory, resourceCenter, serializationArtefact);
				if (resource != null) {
					hasBeenLookedUp = true;
				}
			}
		}
		return hasBeenLookedUp;
	}

	/**
	 * Called when an existing serialization artefact has been removed or deleted<br>
	 * The matching resources are looked-up and referenced from infrastructure
	 * 
	 * @param resourceCenter
	 * @param serializationArtefact
	 * @return a boolean indicating if this file removing has been handled by the technology, when false ResourceCenter might resend
	 *         notification
	 */
	public final <I> boolean contentsDeleted(FlexoResourceCenter<I> resourceCenter, I serializationArtefact) {
		// TODO
		return false;
	}

	/**
	 * Called when an existing serialization artefact has been modified in directory representing this ResourceCenter
	 * 
	 * @param resourceCenter
	 * @param serializationArtefact
	 * @return a boolean indicating if this file removing has been handled by the technology, when false ResourceCenter might resend
	 *         notification
	 */
	public final <I> boolean contentsModified(FlexoResourceCenter<I> resourceCenter, I serializationArtefact) {
		// TODO
		return false;
	}

	/**
	 * Called when an existing serialization artefact has been renamed in directory representing this ResourceCenter
	 * 
	 * @param resourceCenter
	 * @param serializationArtefact
	 * @return a boolean indicating if this file removing has been handled by the technology, when false ResourceCenter might resend
	 *         notification
	 */
	public final <I> boolean contentsRenamed(FlexoResourceCenter<I> resourceCenter, I serializationArtefact, String oldName,
			String newName) {
		// TODO
		return false;
	}

	private final List<FlexoResourceCenter<?>> resourceCentersManagingThisTechnology = new ArrayList<>();

	/**
	 * Provides a hook to detect when a new resource center was added or discovered
	 * 
	 * @param newResourceCenter
	 */
	public void resourceCenterAdded(FlexoResourceCenter<?> newResourceCenter) {
		initializeResourceCenter(newResourceCenter);
		setChanged();
		notifyObservers(new DataModification(null, newResourceCenter));
	}

	/**
	 * Provides a hook to detect when a new resource center was removed
	 * 
	 * @param newResourceCenter
	 */
	public void resourceCenterRemoved(FlexoResourceCenter<?> removedResourceCenter) {
		setChanged();
		notifyObservers(new DataModification(removedResourceCenter, null));
	}

	public boolean hasTypeAwareModelSlot() {
		for (Class<? extends ModelSlot<?>> modelSlotType : getAvailableModelSlotTypes()) {
			if (TypeAwareModelSlot.class.isAssignableFrom(modelSlotType)) {
				return true;
			}
		}
		return false;
	}

	public List<Class<? extends ModelSlot<?>>> getAvailableModelSlotTypes() {
		if (availableModelSlotTypes == null) {
			availableModelSlotTypes = computeAvailableModelSlotTypes();
		}
		return availableModelSlotTypes;
	}

	private List<Class<? extends ModelSlot<?>>> computeAvailableModelSlotTypes() {
		availableModelSlotTypes = new ArrayList<Class<? extends ModelSlot<?>>>();
		Class<?> cl = getClass();
		if (cl.isAnnotationPresent(DeclareModelSlots.class)) {
			DeclareModelSlots allModelSlots = cl.getAnnotation(DeclareModelSlots.class);
			for (Class<? extends ModelSlot> msClass : allModelSlots.value()) {
				availableModelSlotTypes.add((Class<? extends ModelSlot<?>>) msClass);
			}
		}
		return availableModelSlotTypes;
	}

	public List<Class<? extends VirtualModelInstanceNature>> getAvailableVirtualModelInstanceNatures() {
		if (availableVirtualModelInstanceNatures == null) {
			availableVirtualModelInstanceNatures = computeAvailableVirtualModelInstanceNatures();
		}
		return availableVirtualModelInstanceNatures;
	}

	private List<Class<? extends VirtualModelInstanceNature>> computeAvailableVirtualModelInstanceNatures() {
		availableVirtualModelInstanceNatures = new ArrayList<Class<? extends VirtualModelInstanceNature>>();
		Class<?> cl = getClass();
		if (cl.isAnnotationPresent(DeclareVirtualModelInstanceNatures.class)) {
			DeclareVirtualModelInstanceNatures allVirtualModelInstanceNatures = cl.getAnnotation(DeclareVirtualModelInstanceNatures.class);
			for (Class<? extends VirtualModelInstanceNature> natureClass : allVirtualModelInstanceNatures.value()) {
				availableVirtualModelInstanceNatures.add(natureClass);
			}
		}
		return availableVirtualModelInstanceNatures;
	}

	protected void initResourceFactories() {
		resourceFactories.clear();
		availableResourceTypes.clear();
		Class<?> cl = getClass();
		if (cl.isAnnotationPresent(DeclareResourceTypes.class)) {
			DeclareResourceTypes allResourceTypes = cl.getAnnotation(DeclareResourceTypes.class);
			for (Class<? extends FlexoResourceFactory<?, ?, ?>> resourceFactoryClass : allResourceTypes.value()) {
				Constructor<? extends FlexoResourceFactory<?, ?, ?>> constructor;
				try {
					constructor = resourceFactoryClass.getConstructor();
					logger.info("Loading resource factory " + resourceFactoryClass + " using " + constructor);
					FlexoResourceFactory<?, ?, ?> newFactory = constructor.newInstance();
					resourceFactories.add(newFactory);
					availableResourceTypes.add(newFactory.getResourceClass());
					logger.info("Initialized ResourceFactory for " + newFactory.getResourceClass().getSimpleName());
				} catch (InstantiationException e) {
					logger.warning(
							"Unexpected InstantiationException while initializing ResourceFactory " + resourceFactoryClass.getSimpleName());
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					logger.warning(
							"Unexpected NoSuchMethodException while initializing ResourceFactory " + resourceFactoryClass.getSimpleName());
					e.printStackTrace();
				} catch (SecurityException e) {
					logger.warning(
							"Unexpected SecurityException while initializing ResourceFactory " + resourceFactoryClass.getSimpleName());
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					logger.warning(
							"Unexpected IllegalAccessException while initializing ResourceFactory " + resourceFactoryClass.getSimpleName());
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					logger.warning("Unexpected IllegalArgumentException while initializing ResourceFactory "
							+ resourceFactoryClass.getSimpleName());
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					logger.warning("Unexpected InvocationTargetException while initializing ResourceFactory "
							+ resourceFactoryClass.getSimpleName());
					e.printStackTrace();
				}
			}
		}
	}

	public List<Class<? extends TechnologyAdapterResource<?, ?>>> getAvailableResourceTypes() {
		return availableResourceTypes;
	}

	/**
	 * Creates and return a new {@link ModelSlot} of supplied class.<br>
	 * This responsability is delegated to the {@link TechnologyAdapter} which manages with introspection its own {@link ModelSlot} types
	 * 
	 * @param modelSlotClass
	 * @param containerVirtualModel
	 *            the virtual model in which model slot should be created
	 * @return
	 */
	public final <MS extends ModelSlot<?>> MS makeModelSlot(Class<MS> modelSlotClass, AbstractVirtualModel<?> containerVirtualModel) {
		// NPE Protection
		if (containerVirtualModel != null) {
			FMLModelFactory factory = containerVirtualModel.getFMLModelFactory();
			MS returned = factory.newInstance(modelSlotClass);
			// containerVirtualModel.addToModelSlots(returned);
			returned.setModelSlotTechnologyAdapter(this);
			return returned;
		}
		else {
			logger.warning("INVESTIGATE: VirtualModel is null, unable to create a new ModelSlot!");
			return null;
		}

	}

	/**
	 * Retrieve (creates it when not existant) folder containing supplied file
	 * 
	 * @param repository
	 * @param aFile
	 * @return
	 */
	protected <R extends FlexoResource<?>, I> RepositoryFolder<R, I> retrieveRepositoryFolder(ResourceRepository<R, I> repository,
			I serializationArtefact) {
		try {
			return repository.getRepositoryFolder(serializationArtefact, true);
		} catch (IOException e) {
			e.printStackTrace();
			return repository.getRootFolder();
		}
	}

	/**
	 * Called when a resource has been looked-up by the {@link TechnologyAdapter}
	 * 
	 * @param resource
	 * @param resourceCenter
	 */
	/*public void referenceResource(FlexoResource<?> resource, FlexoResourceCenter<?> resourceCenter) {
		resource.setResourceCenter(resourceCenter);
		if (resourceCenter instanceof ResourceRepository && resource != null
				&& resource.getFlexoIODelegate() instanceof FileFlexoIODelegate) {
			// Also register the resource in the ResourceCenter seen as a ResourceRepository
			try {
				File candidateFile = null;
				if (resource.getFlexoIODelegate() instanceof DirectoryBasedFlexoIODelegate) {
					candidateFile = ((DirectoryBasedFlexoIODelegate) resource.getFlexoIODelegate()).getDirectory();
				}
				else if (resource.getFlexoIODelegate() instanceof FileFlexoIODelegate) {
					candidateFile = ((FileFlexoIODelegate) resource.getFlexoIODelegate()).getFile();
				}
	
				((ResourceRepository) resourceCenter).registerResource(resource,
						((ResourceRepository<?>) resourceCenter).getRepositoryFolder(candidateFile, true));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		registerResourceInGlobalRepository(resource, resourceCenter);
	
	}*/

	/*protected void registerResourceInGlobalRepository(FlexoResource<?> resource, FlexoResourceCenter<?> resourceCenter) {
		TechnologyAdapterGlobalRepository globalRepository = getGlobalRepository(resourceCenter);
		if (globalRepository != null) {
			RepositoryFolder<?> folderInGlobalRepository;
			try {
				globalRepository.registerResource(resource, resource.getFlexoIODelegate().getRepositoryFolder(globalRepository, true));
			} catch (IOException e) {
				logger.warning("Unexpected I/O exception: " + e);
				e.printStackTrace();
			}
		}
	}*/

	/**
	 * Called when a resource has been dereferenced by the {@link TechnologyAdapter}
	 * 
	 * @param resource
	 * @param resourceCenter
	 */
	/*public void dereferenceResource(FlexoResource<?> resource, FlexoResourceCenter<?> resourceCenter) {
		// TODO
		logger.warning("dereferenceResource() not implemented yet");
	}*/

	// Override when required
	public void initFMLModelFactory(FMLModelFactory fMLModelFactory) {
	}

	/**
	 * Return the list of all non-empty global repository for this technology adapter<br>
	 * It is stated that the global repository contains all resources which supplied technology adapter has discovered and may interpret,
	 * for a given resource center<br>
	 * Global repositories are resource repositories which are generally given in GUIs (such as browsers) to display the contents of a
	 * resource center for a given technology
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	/**
	 * Return the list of all non-empty {@link ResourceRepository} discovered in the scope of {@link FlexoServiceManager}, related to
	 * technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public List<ResourceRepository<?, ?>> getGlobalRepositories() {
		List<ResourceRepository<?, ?>> returned = new ArrayList<ResourceRepository<?, ?>>();
		for (FlexoResourceCenter<?> rc : getTechnologyAdapterService().getServiceManager().getResourceCenterService()
				.getResourceCenters()) {
			// System.out.println("Pour le RC " + rc);
			ResourceRepository<?, ?> globalRepository = getGlobalRepository(rc);// rc.getGlobalRepository(this);
			// System.out.println("global repo = " + globalRepository);
			if (globalRepository != null) {
				returned.add(globalRepository);
			}
		}
		return returned;
	}

	private final Map<FlexoResourceCenter<?>, TechnologyAdapterGlobalRepository> globalRepositories = new HashMap<>();

	public TechnologyAdapterGlobalRepository getGlobalRepository(FlexoResourceCenter<?> rc) {
		TechnologyAdapterGlobalRepository returned = globalRepositories.get(rc);
		if (returned == null) {
			returned = new TechnologyAdapterGlobalRepository(this, rc);
			globalRepositories.put(rc, returned);
		}
		return returned;
	}

	/**
	 * Return the list of all non-empty {@link ResourceRepository} discovered in the scope of {@link FlexoServiceManager}, related to
	 * technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public List<ResourceRepository<?, ?>> getAllRepositories() {
		List<ResourceRepository<?, ?>> returned = new ArrayList<>();
		for (FlexoResourceCenter<?> rc : getTechnologyAdapterService().getServiceManager().getResourceCenterService()
				.getResourceCenters()) {
			Collection<? extends ResourceRepository<?, ?>> repCollection = rc.getRegistedRepositories(this);
			if (repCollection != null) {
				returned.addAll(repCollection);
			}
		}
		return returned;
	}

	/**
	 * Called to notify that the structure of registered and/or global repositories has changed
	 */
	public void notifyRepositoryStructureChanged() {

		getPropertyChangeSupport().firePropertyChange("getAllRepositories()", null, getAllRepositories());
		getPropertyChangeSupport().firePropertyChange("getGlobalRepositories()", null, getGlobalRepositories());

	}

	@Override
	public String getDeletedProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Hook allowing to register technology-specific types
	 * 
	 * @param converter
	 */
	public void initTechnologySpecificTypes(TechnologyAdapterService taService) {
	}

	/**
	 * Return identifier as it is used in FML language
	 * 
	 * @return
	 */
	public abstract String getIdentifier();

	/**
	 * Return the locales relative to this technology<br>
	 * If the technology is not activated, locales are not loaded, and this method will return null
	 * 
	 * @return
	 */
	public LocalizedDelegate getLocales() {
		// XTOF: TA must be activated for Locales to be accessible.
		// SYL: we dont' want to load the TA just for that
		if (!isActivated) {
			return FlexoLocalization.getMainLocalizer();
		}

		return locales;
	}

	public abstract String getLocalizationDirectory();

	/**
	 * Add all the RCs that contain an identification of a FlexoResourceCenter in META-INF<br>
	 * (identified by META-INF/PrivateRC/org.openflexo.foundation.resource.FlexoResourceCenter) Those ResourceCenters are private resource
	 * center and will be used as system resource centers.
	 * 
	 * WARNING: should only be called once
	 * 
	 */
	protected void loadPrivateResourceCenters() {

		logger.info("Loading available private ResourceCenters from classpath");

		FlexoServiceManager serviceManager = getTechnologyAdapterService().getServiceManager();
		Enumeration<URL> urlList;
		ArrayList<FlexoResourceCenter> rcList = new ArrayList<FlexoResourceCenter>(
				serviceManager.getResourceCenterService().getResourceCenters());

		try {
			urlList = ClassLoader.getSystemClassLoader().getResources("META-INF/PrivateRC/" + FlexoResourceCenter.class.getCanonicalName());

			if (urlList != null && urlList.hasMoreElements()) {
				FlexoResourceCenter rc = null;
				boolean rcExists = false;
				while (urlList.hasMoreElements()) {
					URL url = urlList.nextElement();

					StringWriter writer = new StringWriter();
					IOUtils.copy(url.openStream(), writer, "UTF-8");
					String rcBaseUri = writer.toString();

					System.out.println("Protocol " + url.getProtocol() + ": Attempt to loading RC " + rcBaseUri + " from " + url);

					rcExists = false;
					for (FlexoResourceCenter r : rcList) {
						rcExists = r.getDefaultBaseURI().equals(rcBaseUri) || rcExists;
					}
					if (!rcExists) {
						if (url.getProtocol().equals("file")) {
							// When it is a file and it is contained in target/classes directory then we
							// replace with directory from source code (development mode)
							String dirPath = URLDecoder.decode(url.getPath().substring(0, url.getPath().indexOf("META-INF")), "UTF-8")
									.replace("target/classes", "src/main/resources");
							File rcDir = new File(dirPath);
							if (rcDir.exists()) {
								rc = new DirectoryResourceCenter(rcDir, serviceManager.getResourceCenterService());
							}
						}
						else if (url.getProtocol().equals("jar")) {

							String jarPath = URLDecoder.decode(url.getPath().substring(0, url.getPath().indexOf("!")).replace("+", "%2B"),
									"UTF-8");

							URL jarURL = new URL(jarPath);
							URI jarURI = new URI(jarURL.getProtocol(), jarURL.getUserInfo(), jarURL.getHost(), jarURL.getPort(),
									jarURL.getPath(), jarURL.getQuery(), jarURL.getRef());

							rc = JarResourceCenter.addJarFile(new JarFile(new File(jarURI)), serviceManager.getResourceCenterService());

						}
						else {
							logger.warning("INVESTIGATE: don't know how to deal with RC accessed through " + url.getProtocol());
						}
					}
					else {
						logger.warning("an RC already exists with DefaultBaseURI: " + rcBaseUri);
					}

					if (rc != null) {
						rc.setDefaultBaseURI(rcBaseUri);
						rc.getResourceCenterEntry().setIsSystemEntry(true);
						serviceManager.getResourceCenterService().addToResourceCenters(rc);
						rc = null;
					}

				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
