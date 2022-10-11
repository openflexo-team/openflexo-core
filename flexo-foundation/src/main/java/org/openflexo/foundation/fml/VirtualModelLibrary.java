/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.fml;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.DefaultFlexoObject;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.fml.rt.FMLRTValidationModel;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterAdded;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterRemoved;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.validation.Validable;
import org.openflexo.toolbox.StringUtils;

/**
 * The {@link VirtualModelLibrary} manages all references to all {@link VirtualModel} known in a deployed Openflexo infrastructure.<br>
 * The {@link VirtualModelLibrary} is a {@link FlexoService} working in conjunction with a {@link FlexoResourceCenterService}, with
 * synchronization performed through a {@link FlexoServiceManager} (generally this is the ApplicationContext)
 * 
 * @author sylvain
 * 
 */
public class VirtualModelLibrary extends DefaultFlexoObject implements FlexoService {

	private static final Logger logger = Logger.getLogger(VirtualModelLibrary.class.getPackage().getName());

	public FMLValidationModel fmlValidationModel;
	public FMLRTValidationModel fmlRTValidationModel;

	private final Map<String, CompilationUnitResource> map;

	private FlexoServiceManager serviceManager;

	protected Status status = Status.Registered;

	public VirtualModelLibrary() {
		super();
		map = new Hashtable<>();
	}

	@Override
	public String getServiceName() {
		return "VirtualModelLibrary";
	}

	/**
	 * Retrieve, load and return ViewPoint/VirtualModel identified by supplied URI<br>
	 * If the flag loadWhenRequired is set to true, load required viewpoint when unloaded<br>
	 * Use of this method triggers required virtual models to be loaded
	 * 
	 * @param viewpointURI
	 * @return
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	public VirtualModel getVirtualModel(String virtualModelURI)
			throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		return getVirtualModel(virtualModelURI, true);
	}

	/**
	 * Retrieve, load and return ViewPoint/VirtualModel identified by supplied URI<br>
	 * If the flag loadWhenRequired is set to true, load required viewpoint when unloaded
	 * 
	 * @param viewpointURI
	 * @return
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	public VirtualModel getVirtualModel(String virtualModelURI, boolean loadWhenRequired)
			throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		CompilationUnitResource returned = getCompilationUnitResource(virtualModelURI);
		if (returned == null && !virtualModelURI.endsWith(CompilationUnitResourceFactory.FML_SUFFIX)) {
			returned = getCompilationUnitResource(virtualModelURI + CompilationUnitResourceFactory.FML_SUFFIX);
		}
		if (returned != null) {
			if (loadWhenRequired) {
				if (returned.getResourceData() != null) {
					return returned.getResourceData().getVirtualModel();
				}
				else {
					logger.warning(
							"Cannot load resource: " + returned.getURI() + " at " + returned.getIODelegate().getSerializationArtefact());
					return null;
				}
			}
			return returned.getLoadedResourceData().getVirtualModel();
		}
		/*if (returned == null) {
			logger.warning("Cannot find virtual model:" + virtualModelURI);
		}*/
		return null;
	}

	/**
	 * Retrieve and return {@link CompilationUnitResource} identified by supplied URI, without loading it
	 * 
	 * @param virtualModelURI
	 * @return
	 */
	public CompilationUnitResource getCompilationUnitResource(String virtualModelURI) {
		if (virtualModelURI.contains("/")) {
			String containerVirtualModelURI = virtualModelURI.substring(0, virtualModelURI.lastIndexOf("/"));
			CompilationUnitResource vpres = getCompilationUnitResource(containerVirtualModelURI);
			if (vpres != null) {
				CompilationUnitResource returned = vpres.getCompilationUnitResource(virtualModelURI);
				if (returned != null) {
					return returned;
				}
			}
		}
		return map.get(virtualModelURI);
	}

	/**
	 * Return all {@link CompilationUnitResource} contained in this library<br>
	 * No consideration is performed on underlying organization structure
	 * 
	 * @return
	 */
	public Collection<CompilationUnitResource> getCompilationUnitResources() {
		return map.values();
	}

	/**
	 * Return all loaded virtual models in the current library
	 */
	public Collection<FMLCompilationUnit> getLoadedCompilationUnits() {
		Vector<FMLCompilationUnit> returned = new Vector<>();
		for (CompilationUnitResource vpRes : getCompilationUnitResources()) {
			if (vpRes.isLoaded()) {
				returned.add(vpRes.getCompilationUnit());
			}
		}
		return returned;
	}

	/**
	 * Register supplied {@link CompilationUnitResource} in this library
	 * 
	 * @param resource
	 * @return
	 */
	public CompilationUnitResource registerCompilationUnit(CompilationUnitResource resource) {
		// clearNotFoundObjects();
		String uri = resource.getURI();
		if (StringUtils.isNotEmpty(uri)) {
			map.put(uri, resource);
			setChanged();
			notifyObservers(new CompilationUnitRegistered(resource));
			return resource;
		}
		return null;
	}

	/**
	 * UnRegister supplied {@link CompilationUnitResource} in this library
	 * 
	 * @param resource
	 * @return
	 */
	public CompilationUnitResource unregisterCompilationUnit(CompilationUnitResource resource) {

		// clearNotFoundObjects();
		// Unregister the viewpoint resource from the viewpoint library
		for (Iterator<Map.Entry<String, CompilationUnitResource>> i = map.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, CompilationUnitResource> entry = i.next();
			if ((entry.getValue().equals(resource))) {
				i.remove();
			}
		}

		// Unregister the viewpoint resource from the viewpoint repository
		FMLTechnologyAdapter vmTA = getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		List<FlexoResourceCenter<?>> resourceCenters = getResourceCenterService().getResourceCenters();
		for (FlexoResourceCenter<?> rc : resourceCenters) {
			CompilationUnitRepository<?> vprfb = vmTA.getVirtualModelRepository(rc);
			if ((vprfb != null) && (vprfb.getAllResources().contains(resource))) {
				vprfb.unregisterResource(resource);
			}
		}
		setChanged();
		return resource;
	}

	/**
	 * Lookup and return {@link FMLObject} identified by supplied objectURI<br>
	 * Use of this method triggers required virtual models to be loaded when loadWhenRequired set to true
	 * 
	 * @param objectURI
	 * @param loadWhenRequired
	 * @return
	 */
	public <O extends FMLObject> O getFMLObject(String objectURI, boolean loadWhenRequired) {

		if (objectURI == null) {
			return null;
		}

		CompilationUnitResource compilationUnitResource = getCompilationUnitResource(objectURI);

		// System.out.println("objectURI=" + objectURI);
		// System.out.println("compilationUnitResource=" + compilationUnitResource);

		if (compilationUnitResource != null /*&& compilationUnitResource.getCompilationUnit() != null*/) {
			// System.out.println("compilationUnitResource.getCompilationUnit()=" + compilationUnitResource.getCompilationUnit());
			if (loadWhenRequired) {
				if (compilationUnitResource.getCompilationUnit() != null) {
					return (O) compilationUnitResource.getCompilationUnit().getVirtualModel();
				}
				else {
					// Cannot retrieve the resource data
					return null;
				}
			}
			if (compilationUnitResource.getLoadedCompilationUnit() != null) {
				return (O) compilationUnitResource.getLoadedCompilationUnit().getVirtualModel();
			}
			return null;
			/*return (loadWhenRequired ? (O) compilationUnitResource.getCompilationUnit().getVirtualModel()
					: (compilationUnitResource.getLoadedCompilationUnit() != null
							? (O) compilationUnitResource.getLoadedCompilationUnit().getVirtualModel()
							: null));*/
		}

		if (objectURI.indexOf("#") > -1) {
			String virtualModelURI = objectURI.substring(0, objectURI.indexOf("#"));
			String uriRemains = objectURI.substring(objectURI.indexOf("#") + 1);
			compilationUnitResource = getCompilationUnitResource(virtualModelURI);
			if (compilationUnitResource == null) {
				compilationUnitResource = getCompilationUnitResource(virtualModelURI + ".fml");
				if (compilationUnitResource == null && virtualModelURI.contains("/")) {
					String vpURI = virtualModelURI.substring(0, virtualModelURI.lastIndexOf("/"));
					if (vpURI.endsWith(".viewpoint")) {
						vpURI = vpURI.substring(0, vpURI.length() - 10);
					}
					if (!vpURI.endsWith(".fml")) {
						vpURI = vpURI + ".fml";
					}
					String vmName = virtualModelURI.substring(virtualModelURI.lastIndexOf("/") + 1);
					if (!vmName.endsWith(".fml")) {
						vmName = vmName + ".fml";
					}
					compilationUnitResource = getCompilationUnitResource(vpURI + "/" + vmName);
				}
				logger.info("Attempt to retrieve VirtualModel from former URI form. Searched " + virtualModelURI + " Found: "
						+ compilationUnitResource);
			}
			if (compilationUnitResource != null) {
				FMLCompilationUnit compilationUnit;
				if (loadWhenRequired) {
					compilationUnit = compilationUnitResource.getCompilationUnit();
				}
				else {
					compilationUnit = compilationUnitResource.getLoadedResourceData();
				}
				if (compilationUnit == null) {
					// VirtualModel is not loaded, return null
					return null;
				}

				if (uriRemains.lastIndexOf(".") > -1) {
					String flexoConceptName = uriRemains.substring(0, uriRemains.lastIndexOf("."));
					FlexoConcept concept = compilationUnit.getFlexoConcept(flexoConceptName);
					uriRemains = uriRemains.substring(uriRemains.lastIndexOf(".") + 1);
					if (concept != null) {
						return getFMLObject(uriRemains, concept);
					}
					else {
						logger.warning("Cannot find concept " + flexoConceptName + " in " + compilationUnit);
						return null;
					}
				}
				else {
					return (O) compilationUnit.getFlexoConcept(uriRemains);
				}
			}
		}
		else {
			if (objectURI.lastIndexOf(".") > -1) {
				String flexoConceptURI = objectURI.substring(0, objectURI.lastIndexOf("."));
				FlexoConcept concept = getFlexoConcept(flexoConceptURI, loadWhenRequired);
				String uriRemains = objectURI.substring(objectURI.lastIndexOf(".") + 1);
				if (concept != null) {
					return getFMLObject(uriRemains, concept);
				}
				else {
					logger.warning("Cannot find concept " + flexoConceptURI);
					return null;
				}
			}

		}
		return null;
	}

	private <O extends FMLObject> O getFMLObject(String uriRemains, FlexoConcept expectedConcept) {
		FlexoProperty<?> property = expectedConcept.getDeclaredProperty(uriRemains);
		if (property != null) {
			return (O) property;
		}

		if (uriRemains.equals("create")) {
			uriRemains = "_create";
			// System.out.println("On cherche plutot " + uriRemains);
		}
		if (uriRemains.equals("delete")) {
			uriRemains = "_delete";
			// System.out.println("On cherche plutot " + uriRemains);
		}
		if (uriRemains.startsWith("create(")) {
			uriRemains = "_" + uriRemains;
			// System.out.println("On cherche plutot " + uriRemains);
		}
		if (uriRemains.equals("delete(")) {
			uriRemains = "_" + uriRemains;
			// System.out.println("On cherche plutot " + uriRemains);
		}

		if (uriRemains.contains("(")) {
			FlexoBehaviour behaviour = expectedConcept.getDeclaredFlexoBehaviour(uriRemains);
			if (behaviour != null) {
				return (O) behaviour;
			}
		}
		else {
			FlexoBehaviour behaviour = expectedConcept.getFlexoBehaviour(uriRemains);
			if (behaviour != null) {
				return (O) behaviour;
			}
		}
		logger.warning("Cannot find property or behaviour " + uriRemains + " in " + expectedConcept);
		return null;
	}

	/**
	 * Lookup and return {@link FlexoConcept} identified by supplied flexoConceptURI<br>
	 * Return concept might be a {@link VirtualModel}, a {@link VirtualModel} or a simple {@link FlexoConcept}<br>
	 * Use of this method triggers required virtual models to be loaded
	 * 
	 * @param flexoConceptURI
	 * @return
	 */
	public FlexoConcept getFlexoConcept(String flexoConceptURI) {
		return getFlexoConcept(flexoConceptURI, true);
	}

	/**
	 * Lookup and return {@link FlexoConcept} identified by supplied simple name<br>
	 * This method is more permissive than {@link #getFlexoConcept(String, boolean)} which work on URI<br>
	 * Look up first all FMLCompilationUnitResource, even those which are not loaded, but only for root VirtualModel<br>
	 * Then look into all loaded {@link VirtualModel} to find all matching concepts<br>
	 * Then look into all unloaded {@link VirtualModel} to find all matching concepts, if flag <tt>loadWhenRequired</tt> set to true<br>
	 * 
	 * @param flexoConceptName
	 * @return
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	public List<FlexoConcept> getAllFlexoConceptWithSimpleName(String flexoConceptName, boolean loadWhenRequired)
			throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		List<FlexoConcept> returned = new ArrayList<>();
		for (CompilationUnitResource compilationUnitResource : getCompilationUnitResources()) {
			if (compilationUnitResource.getName().equals(flexoConceptName)) {
				returned.add(compilationUnitResource.getCompilationUnit().getVirtualModel());
			}
			if (compilationUnitResource.isLoaded()) {
				for (FlexoConcept flexoConcept : compilationUnitResource.getLoadedResourceData().getVirtualModel().getFlexoConcepts()) {
					if (flexoConcept.getName().equals(flexoConceptName)) {
						returned.add(flexoConcept);
					}
				}
			}
		}
		if (loadWhenRequired) {
			for (CompilationUnitResource compilationUnitResource : getCompilationUnitResources()) {
				for (FlexoConcept flexoConcept : compilationUnitResource.getResourceData().getVirtualModel().getFlexoConcepts()) {
					if (flexoConcept.getName().equals(flexoConceptName)) {
						returned.add(flexoConcept);
					}
				}
			}
		}
		return returned;
	}

	/**
	 * Lookup and return first {@link FlexoConcept} identified by supplied simple name<br>
	 * Look up first all FMLCompilationUnitResource, even those which are not loaded, but only for root VirtualModel<br>
	 * Then look into all loaded {@link VirtualModel} to find all matching concepts<br>
	 * 
	 * @param flexoConceptName
	 * @return
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	public FlexoConcept getFirstConceptWithSimpleName(String flexoConceptName)
			throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		List<FlexoConcept> returned = getAllFlexoConceptWithSimpleName(flexoConceptName, false);
		if (returned.size() > 0) {
			return returned.get(0);
		}
		return null;
	}

	/**
	 * Lookup and return {@link FlexoConcept} identified by supplied flexoConceptURI<br>
	 * Return concept might be a {@link VirtualModel}, a {@link VirtualModel} or a simple {@link FlexoConcept}<br>
	 * If the flag loadWhenRequired is set to true, load required virtual models
	 * 
	 * @param flexoConceptURI
	 * @return
	 */
	public FlexoConcept getFlexoConcept(String flexoConceptURI, boolean loadWhenRequired) {
		return (FlexoConcept) getFMLObject(flexoConceptURI, loadWhenRequired);
	}

	public FlexoProperty<?> getFlexoProperty(String propertyURI, boolean loadWhenRequired) {
		return (FlexoProperty<?>) getFMLObject(propertyURI, loadWhenRequired);
	}

	public FlexoBehaviour getFlexoBehaviour(String behaviourURI, boolean loadWhenRequired) {
		return (FlexoBehaviour) getFMLObject(behaviourURI, loadWhenRequired);
	}

	@Override
	public Collection<Validable> getEmbeddedValidableObjects() {
		Collection<Validable> returned = new ArrayList<>();
		returned.addAll(getLoadedCompilationUnits());
		return returned;
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		if (caller instanceof FlexoResourceCenterService) {
			if (notification instanceof ResourceCenterAdded) {
				FlexoResourceCenter<?> newRC = ((ResourceCenterAdded) notification).getAddedResourceCenter();
				// A new resource center has just been referenced, initialize it related to viewpoint exploring
				// newRC.initialize(this);

				getPropertyChangeSupport().firePropertyChange("getResourceCenters()", null, newRC);
			}
			if (notification instanceof ResourceCenterRemoved) {
				FlexoResourceCenter<?> newRC = ((ResourceCenterRemoved) notification).getRemovedResourceCenter();
				// A new resource center has just been referenced, initialize it related to viewpoint exploring
				// newRC.initialize(this);

				getPropertyChangeSupport().firePropertyChange("getResourceCenters()", null, newRC);
			}
			/*if (notification instanceof ResourceCenterRemoved) {
				FileSystemBasedResourceCenter newRC = (FileSystemBasedResourceCenter) ((ResourceCenterRemoved) notification)
						.getRemovedResourceCenter();
			
				// A resource center must be been dereferenced
				VirtualModelRepository vpr = newRC.getViewPointRepository();
				for (ViewPointResource vpR : vpr.getAllResources()) {
					if (((FileSystemBasedResourceCenter) vpr.getResourceCenter()).getResource(vpR.getURI()) != null) {
						vpR.unloadResourceData();
						unregisterViewPoint(vpR);
						vpr.unregisterResource(vpR);
					}
				}
				vpr.delete();
			
			}*/
		}
	}

	@Override
	public void register(FlexoServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	@Override
	public FlexoServiceManager getServiceManager() {
		return serviceManager;
	}

	public FlexoResourceCenterService getResourceCenterService() {
		return getServiceManager().getService(FlexoResourceCenterService.class);
	}

	public TechnologyAdapterService getTechnologyAdapterService() {
		return getServiceManager().getService(TechnologyAdapterService.class);
	}

	public List<FlexoResourceCenter<?>> getResourceCenters() {
		return getResourceCenterService().getResourceCenters();
	}

	@Override
	public void initialize() {
		if (getResourceCenterService() != null) {

			FMLTechnologyAdapter fmlTA = getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);

			getTechnologyAdapterService().activateTechnologyAdapter(fmlTA, true);

			for (FlexoResourceCenter<?> rc : getResourceCenters()) {
				// Register Viewpoint viewpoint resources
				CompilationUnitRepository<?> vprfb = fmlTA.getVirtualModelRepository(rc);
				// System.out.println("vprfb=" + vprfb);
				if (vprfb == null) {
					logger.warning("Could not retrieve VirtualModelRepository from RC: " + rc);
				}
				else {
					for (CompilationUnitResource vpRes : vprfb.getAllResources()) {
						vpRes.setVirtualModelLibrary(this);
						registerCompilationUnit(vpRes);
					}
				}
			}
			status = Status.Started;
		}
	}

	public FMLValidationModel getFMLValidationModel() {
		if (fmlValidationModel == null && serviceManager != null) {
			try {
				fmlValidationModel = new FMLValidationModel(serviceManager.getTechnologyAdapterService());
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
		}
		return fmlValidationModel;
	}

	public FMLRTValidationModel getFMLRTValidationModel() {
		if (fmlRTValidationModel == null && serviceManager != null) {
			try {
				fmlRTValidationModel = new FMLRTValidationModel(serviceManager.getTechnologyAdapterService());
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
		}
		return fmlRTValidationModel;
	}

	@Override
	public void stop() {
		logger.warning("STOP Method for service should be overriden in each service [" + this.getClass().getCanonicalName() + "]");
		status = Status.Stopped;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	/**
	 * Return indicating general status of this FlexoService<br>
	 * This is the display value of 'service <service> status' as given in FML command-line interpreter
	 * 
	 * @return
	 */
	@Override
	public String getDisplayableStatus() {
		return getServiceName() + StringUtils.buildWhiteSpaceIndentation(30 - getServiceName().length()) + getStatus();
	}

	private List<ServiceOperation<?>> availableServiceOperations = null;

	/**
	 * Return collection of all available {@link ServiceOperation} available for this {@link FlexoService}
	 * 
	 * @return
	 */
	@Override
	public Collection<ServiceOperation<?>> getAvailableServiceOperations() {
		if (availableServiceOperations == null) {
			availableServiceOperations = new ArrayList<>();
			availableServiceOperations.add(HELP_ON_SERVICE);
			availableServiceOperations.add(DISPLAY_SERVICE_STATUS);
			availableServiceOperations.add(START_SERVICE);
			availableServiceOperations.add(STOP_SERVICE);
		}
		return availableServiceOperations;
	}

	@Override
	public void addToAvailableServiceOperations(ServiceOperation<?> serviceOperation) {
		getAvailableServiceOperations().add(serviceOperation);
	}

}
