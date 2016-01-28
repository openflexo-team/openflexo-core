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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

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
import org.openflexo.foundation.resource.DirectoryBasedFlexoIODelegate;
import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceRepository;

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
	private List<Class<? extends ModelSlot<?>>> availableModelSlotTypes;
	private List<Class<? extends VirtualModelInstanceNature>> availableVirtualModelInstanceNatures;
	private List<Class<? extends TechnologyAdapterResource<?, ?>>> availableResourceTypes;

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
	public abstract TechnologyContextManager createTechnologyContextManager(FlexoResourceCenterService service);

	/**
	 * Return the {@link TechnologyContextManager} for this technology shared by all {@link FlexoResourceCenter} declared in the scope of
	 * {@link FlexoResourceCenterService}
	 * 
	 * @return
	 */
	public TechnologyContextManager getTechnologyContextManager() {
		return getTechnologyAdapterService().getTechnologyContextManager(this);
	}

	/**
	 * Return the technology-specific binding factory
	 * 
	 * @return
	 */
	public abstract TechnologyAdapterBindingFactory getTechnologyAdapterBindingFactory();

	/**
	 * Provides a hook to finalize initialization of a TechnologyAdapter.<br>
	 * This method is called:
	 * <ul>
	 * <li>after all TechnologyAdapter have been loaded</li>
	 * <li>after all {@link FlexoResourceCenter} have been initialized</li>
	 * </ul>
	 */
	public void initialize() {
		initTechnologySpecificTypes(getTechnologyAdapterService());
	}

	/**
	 * Initialize the supplied resource center with the technology<br>
	 * ResourceCenter is scanned, ResourceRepositories are created and new technology-specific resources are build and registered.
	 * 
	 * @param resourceCenter
	 */
	public abstract <I> void initializeResourceCenter(FlexoResourceCenter<I> resourceCenter);

	public abstract <I> boolean isIgnorable(FlexoResourceCenter<I> resourceCenter, I contents);

	public abstract <I> void contentsAdded(FlexoResourceCenter<I> resourceCenter, I contents);

	public abstract <I> void contentsDeleted(FlexoResourceCenter<I> resourceCenter, I contents);

	/**
	 * Provides a hook to detect when a new resource center was added or discovered
	 * 
	 * @param newResourceCenter
	 */
	public void resourceCenterAdded(FlexoResourceCenter<?> newResourceCenter) {
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

	public List<Class<? extends TechnologyAdapterResource<?, ?>>> getAvailableResourceTypes() {
		if (availableResourceTypes == null) {
			availableResourceTypes = computeAvailableResourceTypes();
		}
		return availableResourceTypes;
	}

	private List<Class<? extends TechnologyAdapterResource<?, ?>>> computeAvailableResourceTypes() {
		availableResourceTypes = new ArrayList<Class<? extends TechnologyAdapterResource<?, ?>>>();
		Class<?> cl = getClass();
		if (cl.isAnnotationPresent(DeclareResourceTypes.class)) {
			DeclareResourceTypes allResourceTypes = cl.getAnnotation(DeclareResourceTypes.class);
			for (Class<? extends TechnologyAdapterResource> resourceClass : allResourceTypes.value()) {
				availableResourceTypes.add((Class<? extends TechnologyAdapterResource<?, ?>>) resourceClass);
			}
		}
		return availableResourceTypes;
	}

	/*public List<Class<? extends TechnologySpecificType<?>>> getAvailableTechnologySpecificTypes() {
		if (availableTechnologySpecificTypes == null) {
			availableTechnologySpecificTypes = computeAvailableTechnologySpecificTypes();
		}
		return availableTechnologySpecificTypes;
	}
	
	protected List<Class<? extends TechnologySpecificType<?>>> computeAvailableTechnologySpecificTypes() {
		availableTechnologySpecificTypes = new ArrayList<Class<? extends TechnologySpecificType<?>>>();
		appendDeclareTechnologySpecificTypes(availableTechnologySpecificTypes, getClass());
		if (!availableTechnologySpecificTypes.contains(FlexoConceptInstanceType.class)) {
			availableTechnologySpecificTypes.add(FlexoConceptInstanceType.class);
		}
		if (!availableTechnologySpecificTypes.contains(VirtualModelInstanceType.class)) {
			availableTechnologySpecificTypes.add(VirtualModelInstanceType.class);
		}
		if (!availableTechnologySpecificTypes.contains(ViewType.class)) {
			availableTechnologySpecificTypes.add(ViewType.class);
		}
	
		if (hasTypeAwareModelSlot()) {
			if (!availableTechnologySpecificTypes.contains(IndividualOfClass.class)) {
				availableTechnologySpecificTypes.add((Class) IndividualOfClass.class);
			}
			if (!availableTechnologySpecificTypes.contains(SubClassOfClass.class)) {
				availableTechnologySpecificTypes.add((Class) SubClassOfClass.class);
			}
			if (!availableTechnologySpecificTypes.contains(SubPropertyOfProperty.class)) {
				availableTechnologySpecificTypes.add((Class) SubPropertyOfProperty.class);
			}
		}
		return availableTechnologySpecificTypes;
	}
	
	private void appendDeclareTechnologySpecificTypes(List<Class<? extends TechnologySpecificType<?>>> aList, Class<?> cl) {
		if (cl.isAnnotationPresent(DeclareTechnologySpecificTypes.class)) {
			DeclareTechnologySpecificTypes allTypes = cl.getAnnotation(DeclareTechnologySpecificTypes.class);
			for (Class<? extends TechnologySpecificType<?>> typeClass : allTypes.value()) {
				if (!availableTechnologySpecificTypes.contains(typeClass)) {
					availableTechnologySpecificTypes.add(typeClass);
				}
			}
		}
		if (cl.getSuperclass() != null) {
			appendDeclareTechnologySpecificTypes(aList, cl.getSuperclass());
		}
	
		for (Class superInterface : cl.getInterfaces()) {
			appendDeclareTechnologySpecificTypes(aList, superInterface);
		}
	
	}*/

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
	protected <R extends FlexoResource<?>> RepositoryFolder<R> retrieveRepositoryFolder(ResourceRepository<R> repository, File aFile) {
		try {
			return repository.getRepositoryFolder(aFile, true);
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
	public void referenceResource(FlexoResource<?> resource, FlexoResourceCenter<?> resourceCenter) {
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

				/*if (resource instanceof DirectoryContainerResource) {
					candidateFile = ResourceLocator.retrieveResourceAsFile(((DirectoryContainerResource<?>) resource).getDirectory());
				} else {
					candidateFile = ((FileFlexoIODelegate) resource.getFlexoIODelegate()).getFile();
				}*/
				((ResourceRepository) resourceCenter).registerResource(resource,
						((ResourceRepository<?>) resourceCenter).getRepositoryFolder(candidateFile, true));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Called when a resource has been dereferenced by the {@link TechnologyAdapter}
	 * 
	 * @param resource
	 * @param resourceCenter
	 */
	public void dereferenceResource(FlexoResource<?> resource, FlexoResourceCenter<?> resourceCenter) {
		// TODO
		logger.warning("dereferenceResource() not implemented yet");
	}

	// Override when required
	public void initFMLModelFactory(FMLModelFactory fMLModelFactory) {
	}

	/**
	 * Return the list of all non-empty {@link ResourceRepository} discovered in the scope of {@link FlexoServiceManager}, related to
	 * technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public List<ResourceRepository<?>> getAllRepositories() {
		List<ResourceRepository<?>> returned = new ArrayList<ResourceRepository<?>>();
		for (FlexoResourceCenter<?> rc : getTechnologyAdapterService().getServiceManager().getResourceCenterService()
				.getResourceCenters()) {
			Collection<ResourceRepository<?>> repCollection = rc.getRegistedRepositories(this);
			if (repCollection != null) {
				returned.addAll(repCollection);
			}
		}
		return returned;
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
	 * Returned the resource matched for the resource center
	 * @param rc
	 * @return
	 */
	public abstract <T> FlexoResource<?> tryToLookUp(FlexoResourceCenter<?> rc, T toMatch );
}
