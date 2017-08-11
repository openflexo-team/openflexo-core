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
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterAdded;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterRemoved;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.validation.Validable;
import org.openflexo.toolbox.StringUtils;

/**
 * The {@link ViewPointLibrary} manages all references to all {@link ViewPoint} known in a JVM instance.<br>
 * The {@link ViewPointLibrary} is a {@link FlexoService} working in conjunction with a {@link FlexoResourceCenterService}, with
 * synchronization performed through a {@link FlexoServiceManager} (generally this is the ApplicationContext)
 * 
 * @author sylvain
 * 
 */
public class ViewPointLibrary extends DefaultFlexoObject implements FlexoService, Validable {

	private static final Logger logger = Logger.getLogger(ViewPointLibrary.class.getPackage().getName());

	public FMLValidationModel viewPointValidationModel;

	private final Map<String, ViewPointResource> map;

	private FlexoServiceManager serviceManager;

	// private XMLMapping viewPointModel_0_1;
	// private XMLMapping viewPointModel_1_0;

	public ViewPointLibrary() {
		super();

		map = new Hashtable<String, ViewPointResource>();

	}

	/**
	 * Retrieve, and return ViewPointResource identified by supplied URI
	 * 
	 * @param viewpointURI
	 * @return
	 */
	public ViewPointResource getViewPointResource(String viewpointURI) {
		/*System.out.println("On recherche " + viewpointURI);
		System.out.println("On retourne " + map.get(viewpointURI));
		System.out.println("Mais: " + getServiceManager().getResourceManager().getResource(viewpointURI, ViewPoint.class));
		for (FlexoResource<?> r : getServiceManager().getResourceManager().getRegisteredResources()) {
			System.out.println("   - " + r.getURI() + " " + r);
		}*/
		return map.get(viewpointURI);
	}

	/**
	 * Retrieve and return ViewPoint identified by supplied URI<br>
	 * If the flag loadWhenRequired is set to true, load required viewpoint when unloaded
	 * 
	 * @param viewpointURI
	 * @return
	 */
	public ViewPoint getViewPoint(String viewpointURI) {
		return getViewPoint(viewpointURI, true);
	}

	/**
	 * Retrieve and return ViewPoint identified by supplied URI<br>
	 * If the flag loadWhenRequired is set to true, load required viewpoint when unloaded<br>
	 * Use of this method triggers required virtual models to be loaded
	 * 
	 * @param viewpointURI
	 * @return
	 */
	public ViewPoint getViewPoint(String viewpointURI, boolean loadWhenRequired) {
		if (getViewPointResource(viewpointURI) != null) {
			if (loadWhenRequired) {
				return getViewPointResource(viewpointURI).getViewPoint();
			}
			else {
				return getViewPointResource(viewpointURI).getLoadedResourceData();
			}
		}
		return null;
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
		VirtualModelResource returned = getVirtualModelResource(virtualModelURI);
		if (returned != null) {
			if (loadWhenRequired) {
				return returned.getResourceData(null);
			}
			else {
				return returned.getLoadedResourceData();
			}
		}
		/*if (returned == null) {
			logger.warning("Cannot find virtual model:" + virtualModelURI);
		}*/
		return null;
	}

	/**
	 * Retrieve and return ViewPoint/VirtualModel resource identified by supplied URI, without loading it
	 * 
	 * @param viewpointURI
	 * @return
	 */
	public VirtualModelResource getVirtualModelResource(String virtualModelURI) {
		if (virtualModelURI.contains("/")) {
			String viewPointURI = virtualModelURI.substring(0, virtualModelURI.lastIndexOf("/"));
			ViewPointResource vpres = getViewPointResource(viewPointURI);
			if (vpres != null) {
				VirtualModelResource returned = vpres.getVirtualModelResource(virtualModelURI);
				if (returned != null) {
					return returned;
				}
			}
		}
		return null;
	}

	/**
	 * Return all viewpoints contained in this library<br>
	 * No consideration is performed on underlying organization structure
	 * 
	 * @return
	 */
	public Collection<ViewPointResource> getViewPoints() {
		return map.values();
	}

	/**
	 * Return all loaded viewpoint in the current library
	 */
	public Collection<ViewPoint> getLoadedViewPoints() {
		Vector<ViewPoint> returned = new Vector<ViewPoint>();
		for (ViewPointResource vpRes : getViewPoints()) {
			if (vpRes.isLoaded()) {
				returned.add(vpRes.getViewPoint());
			}
		}
		return returned;
	}

	/**
	 * Register supplied ViewPointResource in this library
	 * 
	 * @param vpRes
	 * @return
	 */
	public ViewPointResource registerViewPoint(ViewPointResource vpRes) {
		String uri = vpRes.getURI();
		if (StringUtils.isNotEmpty(uri)) {
			map.put(uri, vpRes);
			setChanged();
			notifyObservers(new ViewPointRegistered(vpRes));
			return vpRes;
		}
		return null;
	}

	/**
	 * UnRegister supplied ViewPointResource in this library
	 * 
	 * @param vpRes
	 * @return
	 */
	public ViewPointResource unregisterViewPoint(ViewPointResource vpRes) {

		// Unregister the viewpoint resource from the viewpoint library
		for (Iterator<Map.Entry<String, ViewPointResource>> i = map.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, ViewPointResource> entry = i.next();
			if ((entry.getValue().equals(vpRes))) {
				i.remove();
			}
		}

		// Unregister the viewpoint resource from the viewpoint repository
		FMLTechnologyAdapter vmTA = getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		List<FlexoResourceCenter<?>> resourceCenters = getResourceCenterService().getResourceCenters();
		for (FlexoResourceCenter<?> rc : resourceCenters) {
			ViewPointRepository<?> vprfb = vmTA.getViewPointRepository(rc);
			if ((vprfb != null) && (vprfb.getAllResources().contains(vpRes))) {
				vprfb.unregisterResource(vpRes);
			}
		}
		setChanged();
		return vpRes;
	}

	/**
	 * Lookup and return {@link FlexoConcept} identified by supplied flexoConceptURI<br>
	 * Return concept might be a {@link ViewPoint}, a {@link VirtualModel} or a simple {@link FlexoConcept}<br>
	 * Use of this method triggers required virtual models to be loaded
	 * 
	 * @param flexoConceptURI
	 * @return
	 */
	public FlexoConcept getFlexoConcept(String flexoConceptURI) {
		return getFlexoConcept(flexoConceptURI, true);
	}

	/**
	 * Lookup and return {@link FlexoConcept} identified by supplied flexoConceptURI<br>
	 * Return concept might be a {@link ViewPoint}, a {@link VirtualModel} or a simple {@link FlexoConcept}<br>
	 * If the flag loadWhenRequired is set to true, load required virtual models
	 * 
	 * @param flexoConceptURI
	 * @return
	 */
	public FlexoConcept getFlexoConcept(String flexoConceptURI, boolean loadWhenRequired) {
		FlexoConcept returned = null;

		// Is that a viewpoint ?
		returned = getViewPoint(flexoConceptURI, loadWhenRequired);
		if (returned != null) {
			return returned;
		}

		// Is that a virtual model ?
		VirtualModelResource vmRes = getVirtualModelResource(flexoConceptURI);
		if (vmRes != null) {
			if (loadWhenRequired) {
				return vmRes.getVirtualModel();
			}
			else {
				return vmRes.getLoadedResourceData();
			}
		}

		// May be a simple concept ?
		if (flexoConceptURI.indexOf("#") > -1) {
			String virtualModelURI = flexoConceptURI.substring(0, flexoConceptURI.indexOf("#"));
			String flexoConceptName = flexoConceptURI.substring(flexoConceptURI.indexOf("#") + 1);
			vmRes = getVirtualModelResource(virtualModelURI);
			if (vmRes != null) {
				VirtualModel vm;
				if (loadWhenRequired) {
					vm = vmRes.getVirtualModel();
				}
				else {
					vm = vmRes.getLoadedResourceData();
				}
				if (vm != null) {
					return vm.getFlexoConcept(flexoConceptName);
				}
				else {
					// It is possible to come here, because this can be called during deserialization of VirtualModel itself
					// In this case, the resource cannot be loaded yet (because already loading)
					// Concept will be looked up later
				}
			}
			// logger.warning("Cannot find virtual model " + virtualModelURI + " while searching flexo concept:" + flexoConceptURI + " ("
			// + flexoConceptName + ")");
		}
		// logger.warning("Cannot find flexo concept:" + flexoConceptURI);
		/*String viewPointURI = flexoConceptURI.substring(0, flexoConceptURI.lastIndexOf("/"));
		for (VirtualModelResource r : getViewPointResource(viewPointURI).getVirtualModelResources()) {
			System.out.println("> VM " + r.getURI());
		}*/
		return null;
	}

	public FlexoProperty<?> getFlexoProperty(String propertyURI, boolean loadWhenRequired) {
		if (propertyURI == null)
			return null;
		if (propertyURI.lastIndexOf(".") > -1) {
			String flexoConceptURI = propertyURI.substring(0, propertyURI.lastIndexOf("."));
			FlexoConcept ep = getFlexoConcept(flexoConceptURI, loadWhenRequired);
			if (ep != null) {
				return ep.getAccessibleProperty(propertyURI.substring(propertyURI.lastIndexOf(".") + 1));
			}
		}
		logger.warning("Cannot find property:" + propertyURI);
		return null;
	}

	public FlexoBehaviour getFlexoBehaviour(String behaviourURI, boolean loadWhenRequired) {
		if (behaviourURI == null)
			return null;
		if (behaviourURI.lastIndexOf(".") > -1) {
			String flexoConceptURI = behaviourURI.substring(0, behaviourURI.lastIndexOf("."));
			FlexoConcept ep = getFlexoConcept(flexoConceptURI, loadWhenRequired);
			if (ep != null) {
				return ep.getFlexoBehaviour(behaviourURI.substring(behaviourURI.lastIndexOf(".") + 1));
			}
		}
		logger.warning("Cannot find behaviour:" + behaviourURI);
		return null;
	}

	@Override
	public Collection<ViewPoint> getEmbeddedValidableObjects() {
		return getLoadedViewPoints();
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
				ViewPointRepository vpr = newRC.getViewPointRepository();
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
			FlexoTask activateFMLTATask = getTechnologyAdapterService().activateTechnologyAdapter(fmlTA);

			if (activateFMLTATask != null) {
				// We have to wait the task to be finished
				getServiceManager().getTaskManager().waitTask(activateFMLTATask);
			}

			for (FlexoResourceCenter<?> rc : getResourceCenters()) {
				// Register Viewpoint viewpoint resources
				ViewPointRepository<?> vprfb = fmlTA.getViewPointRepository(rc);
				// System.out.println("vprfb=" + vprfb);
				if (vprfb == null) {
					logger.warning("Could not retrieve ViewPointRepository from RC: " + rc);
				}
				else {
					for (ViewPointResource vpRes : vprfb.getAllResources()) {
						vpRes.setViewPointLibrary(this);
						registerViewPoint(vpRes);
					}
				}
			}
		}
	}

	public FMLValidationModel getViewPointValidationModel() {
		if (viewPointValidationModel == null && serviceManager != null) {
			try {
				viewPointValidationModel = new FMLValidationModel(serviceManager.getTechnologyAdapterService());
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
		}
		return viewPointValidationModel;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		logger.warning("STOP Method for service should be overriden in each service [" + this.getClass().getCanonicalName() + "]");

	}

	/*public void delete(ViewPoint viewPoint) {
		logger.info("Remove viewpoint " + viewPoint);
		unregisterViewPoint(viewPoint.getResource());
	}*/

}