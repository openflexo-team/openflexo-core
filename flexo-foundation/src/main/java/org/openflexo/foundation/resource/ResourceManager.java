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

package org.openflexo.foundation.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterAdded;
import org.openflexo.foundation.resource.DefaultResourceCenterService.ResourceCenterRemoved;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.FlexoModelResource;
import org.openflexo.foundation.technologyadapter.MetaModelRepository;
import org.openflexo.foundation.technologyadapter.ModelRepository;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResourceRepository;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.foundation.utils.FlexoObjectReference.ReferenceOwner;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FlexoVersion;

/**
 * The ResourceManager has the responsability of managing resources.<br>
 * 
 * This is the entry point to retrieve a {@link FlexoResource} existing in the context of a {@link FlexoServiceManager} (the "session")
 * 
 * The ResourceManager mainly reflect {@link InformationSpace} as {@link FlexoService} providing access to modelling elements from their
 * original technological context.<br>
 * The information space is obtained through two services from the {@link FlexoServiceManager}, and results from the merging of the
 * {@link FlexoResourceCenterService} and the {@link TechnologyAdapterService}.<br>
 * For each {@link FlexoResourceCenter} and for each {@link TechnologyAdapter}, some repositories are managed.
 *
 * Each {@link FlexoResource} is defined in a {@link FlexoResourceCenter} (except some internal resources that may have no resource center,
 * as their life-cycle does not include serialization). The {@link ResourceManager} then delegate some responsabilities (such as uri-naming,
 * and storing features) to the related {@link FlexoResourceCenter}.
 * 
 * {@link FlexoResource} are stored, identified and retrieved by the combination of their uri and a version. Version might be null, but
 * access to such resource is not guaranteed in a multiple version mode.
 * 
 * The ResourceManager has also the responsability - with a certain delegation to the concerned FlexoResourceCenter - to dispatch
 * resource-based event, such as resource discovering, deleting, updating (for example if a file on disk has changed, the ResourceManager
 * will handle that)
 * 
 * If underlying technology handle type-aware modelling (@see {@link FlexoMetaModel} and {@link FlexoModel}), also provide access to Model,
 * MetaModels, and their respective repositories
 * 
 * 
 * @author sylvain
 * 
 */
public class ResourceManager extends FlexoServiceImpl implements ReferenceOwner {

	protected static final Logger logger = Logger.getLogger(ResourceManager.class.getPackage().getName());

	// TODO: implement this as a map !!!
	private final List<FlexoResource<?>> resources;

	private final List<File> filesToDelete;

	public static ResourceManager createInstance() {
		return new ResourceManager();
	}

	private ResourceManager() {
		// Not now: will be performed by the ServiceManager
		// initialize();
		resources = new ArrayList<>();
		filesToDelete = new ArrayList<>();
	}

	@Override
	public String getServiceName() {
		return "ResourceManager";
	}

	@Override
	public void initialize() {
		logger.info("Initialized ResourceManager...");
		status = Status.Started;
	}

	private Map<String, List<PendingResourceDependency>> pendingResourceDependencies = new HashMap<>();

	class PendingResourceDependency {
		FlexoResource<?> resource;
		String dependencyURI;

		PendingResourceDependency(FlexoResource<?> resource, String dependencyURI) {
			super();
			this.resource = resource;
			this.dependencyURI = dependencyURI;
		}
	}

	public void registerPendingDependencyResource(FlexoResource<?> resource, String dependencyURI) {
		// System.out.println("registerPendingDependencyResource " + dependencyURI + " for " + resource);
		List<PendingResourceDependency> l = pendingResourceDependencies.get(dependencyURI);
		if (l == null) {
			l = new ArrayList<PendingResourceDependency>();
			pendingResourceDependencies.put(dependencyURI, l);
		}
		l.add(new PendingResourceDependency(resource, dependencyURI));
	}

	// It should be synchronized has the same resource could be registered several times in different threads
	public synchronized void registerResource(FlexoResource<?> resource) throws DuplicateURIException {

		if (resource.getResourceCenter() == null) {
			logger.warning("Resource belonging to no ResourceCenter: " + resource);
			// Thread.dumpStack();
		}

		if (getResource(resource.getURI()) != null) {
			// This URI seems to be already registered
			if (getResource(resource.getURI()) == resource) {
				// That's fine, but this resource is already registered
				return;
			}
			throw new DuplicateURIException(resource);
		}

		if (!resources.contains(resource)) {
			resources.add(resource);
			getServiceManager().notify(this, new ResourceRegistered(resource, null));
		}
		else {
			logger.info("Resource already registered: " + resource);
		}
		if (resource.getURI() == null) {
			logger.info("Resource with null URI: " + resource);
			resource.getURI();
			Thread.dumpStack();
		}
		else {
			List<PendingResourceDependency> prdList = pendingResourceDependencies.get(resource.getURI());
			if (prdList != null && prdList.size() > 0) {
				for (PendingResourceDependency prd : prdList) {
					// System.out.println("Resolved pending dependency " + prd.dependencyURI);
					prd.resource.addToDependencies(resource);
				}
				pendingResourceDependencies.remove(resource.getURI());
			}
		}
	}

	public void unregisterResource(FlexoResource<?> resource) {
		resources.remove(resource);
		getServiceManager().notify(this, new ResourceUnregistered(resource, null));
	}

	public List<FlexoResource<?>> getRegisteredResources() {
		return resources;
	}

	public List<FlexoResource<?>> getRegisteredFileResources() {
		return (List<FlexoResource<?>>) getRegisteredResources(FlexoResource.class);
	}

	public <R extends FlexoResource<?>> List<? extends R> getRegisteredResources(Class<R> resourceClass) {
		List<R> returned = new ArrayList<>();
		for (FlexoResource<?> r : getRegisteredResources()) {
			if (resourceClass.isAssignableFrom(r.getClass())) {
				returned.add((R) r);
			}
		}
		return returned;
	}

	// TODO: optimize this
	public List<FlexoResource<?>> getLoadedResources() {
		List<FlexoResource<?>> returned = new ArrayList<>();
		for (FlexoResource<?> r : resources) {
			if (r.isLoaded()) {
				returned.add(r);
			}
		}
		return returned;
	}

	// TODO: optimize this
	public List<FlexoResource<?>> getUnsavedResources() {
		List<FlexoResource<?>> returned = new ArrayList<>();
		for (FlexoResource<?> r : new ArrayList<>(resources)) {
			if (r.isLoaded() && r.getLoadedResourceData().isModified()) {
				returned.add(r);
			}
			if (r.isDeleted()) {
				returned.add(r);
			}
		}
		return returned;
	}

	/**
	 * Returns a resource identified by the given <code>uri</code><br>
	 * No specific version is searched, return the last found version<br>
	 * From a methodological point of view, this method is generally used to retrieve a snapshot version
	 * 
	 * @param uri
	 *            the URI of the resource
	 * @return the resource with the given <code>uri</code>, or null if it cannot be found.
	 */
	public FlexoResource<?> getResource(String resourceURI) {

		if (StringUtils.isEmpty(resourceURI)) {
			return null;
		}
		for (FlexoResource<?> r : new ArrayList<>(resources)) {
			if (resourceURI.equals(r.getURI())) {
				return r;
			}
		}
		return null;
	}

	/**
	 * Returns a typed resource identified by the given <code>uri</code><br>
	 * No specific version is searched, return the last found version<br>
	 * From a methodological point of view, this method is generally used to retrieve a snapshot version
	 * 
	 * @param uri
	 *            the URI of the resource
	 * @param type
	 *            the type of the resource data reference by the resource to retrieve. The implementation is responsible to make the
	 *            appropriate type verifications.
	 * @return the resource with the given <code>uri</code>, or null if it cannot be found.
	 */
	public @Nullable <T extends ResourceData<T>> FlexoResource<T> getResource(@Nonnull String uri, @Nonnull Class<T> type) {
		// TODO: a better type checking would be better
		return (FlexoResource<T>) getResource(uri);
	}

	public void addToFilesToDelete(File f) {
		filesToDelete.add(f);
	}

	public void removeFromFilesToDelete(File f) {
		filesToDelete.remove(f);
	}

	public void deleteFilesToBeDeleted() {
		for (File f : filesToDelete) {
			try {
				if (FileUtils.recursiveDeleteFile(f)) {
					if (logger.isLoggable(Level.INFO)) {
						logger.info("Successfully deleted " + f.getAbsolutePath());
						// filesToDelete.remove(f);
					}
				}
				else if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Could not delete " + f.getAbsolutePath());
				}
			} catch (RuntimeException e) {
				e.printStackTrace();
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Could not delete " + f.getAbsolutePath());
				}
			}
		}
		filesToDelete.clear();
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		super.receiveNotification(caller, notification);
		if (notification instanceof ResourceCenterAdded) {
			setChanged();
			notifyObservers(new DataModification<>(null, ((ResourceCenterAdded) notification).getAddedResourceCenter()));
		}
		if (notification instanceof ResourceCenterRemoved) {
			// In this case, we MUST unregister all resources contained in removed ResourceCenter
			FlexoResourceCenter<?> removedRC = ((ResourceCenterRemoved) notification).getRemovedResourceCenter();

			for (FlexoResource<?> r : removedRC.getAllResources()) {
				unregisterResource(r);
			}

			// Because we cannot be confident of #getAllResources() we proceed to iterate on all registered resources
			for (FlexoResource<?> r : new ArrayList<>(resources)) {
				if (r.getResourceCenter() == removedRC) {
					unregisterResource(r);
				}
			}

			setChanged();
			notifyObservers(new DataModification<>(((ResourceCenterRemoved) notification).getRemovedResourceCenter(), null));
		}
	}

	/*private Map<Thread, Stack<FlexoResource<?>>> resourceLoadingCausality = new HashMap<>();
	private List<CrossReferenceDependency> crossReferenceDependencies = new ArrayList<>();
	
	public void crossReferencesLoadingSchemeDetected(FlexoResource<?> resource) {
	
		System.out.println("Hopala, cross references detected.... for " + resource);
		Thread.dumpStack();
	
		Stack<FlexoResource<?>> stack = resourceLoadingCausality.get(Thread.currentThread());
		if (stack != null && !stack.isEmpty()) {
			CrossReferenceDependency crossReferenceDependency = new CrossReferenceDependency(resource, stack.peek());
			if (!crossReferenceDependencies.contains(crossReferenceDependency)) {
				crossReferenceDependencies.add(crossReferenceDependency);
			}
		}
	}
	
	public class CrossReferenceDependency {
		private final FlexoResource<?> requestedResource;
		private final FlexoResource<?> requestingResource;
	
		public CrossReferenceDependency(FlexoResource<?> requestedResource, FlexoResource<?> requestingResource) {
			super();
			this.requestedResource = requestedResource;
			this.requestingResource = requestingResource;
		}
	
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + ((requestedResource == null) ? 0 : requestedResource.hashCode());
			result = prime * result + ((requestingResource == null) ? 0 : requestingResource.hashCode());
			return result;
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CrossReferenceDependency other = (CrossReferenceDependency) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (requestedResource == null) {
				if (other.requestedResource != null)
					return false;
			}
			else if (!requestedResource.equals(other.requestedResource))
				return false;
			if (requestingResource == null) {
				if (other.requestingResource != null)
					return false;
			}
			else if (!requestingResource.equals(other.requestingResource))
				return false;
			return true;
		}
	
		private ResourceManager getEnclosingInstance() {
			return ResourceManager.this;
		}
	}*/

	public void resourceWillLoad(FlexoResource<?> resource, ResourceWillLoad notification) {
		/*Stack<FlexoResource<?>> stack = resourceLoadingCausality.get(Thread.currentThread());
		if (stack == null) {
			stack = new Stack<>();
			resourceLoadingCausality.put(Thread.currentThread(), stack);
		}
		stack.push(resource);*/

		if (getServiceManager() != null) {
			getServiceManager().notify(this, notification);
		}
	}

	public void resourceLoaded(FlexoResource<?> resource, ResourceLoaded<?> notification) {
		/*Stack<FlexoResource<?>> stack = resourceLoadingCausality.get(Thread.currentThread());
		if (stack != null) {
			// System.out.println("################### Finished loading " + resource.getName());
			stack.pop();
		}
		for (CrossReferenceDependency crossReferenceDependency : new ArrayList<>(crossReferenceDependencies)) {
			if (crossReferenceDependency.requestedResource == resource) {
				crossReferenceDependency.requestingResource.resolvedCrossReferenceDependency(crossReferenceDependency.requestedResource);
				if (getServiceManager() != null) {
					// Notify ResourceLoaded again
					// The validation will be performed later, causing chance for resource to be fully validated
					getServiceManager().notify(this, new ResourceLoaded<>(crossReferenceDependency.requestingResource));
				}
			}
		}*/
		if (getServiceManager() != null) {
			getServiceManager().notify(this, notification);
		}
	}

	public void resourceUnloaded(FlexoResource<?> resource, ResourceUnloaded<?> notification) {
		if (getServiceManager() != null) {
			getServiceManager().notify(this, notification);
		}
	}

	public List<TechnologyAdapter> getTechnologyAdapters() {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapters();
		}
		return null;
	}

	/**
	 * Return the list of all non-empty {@link ResourceRepositoryImpl} discovered in this {@link InformationSpace}, related to technology as
	 * supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public List<ResourceRepository<?, ?>> getAllRepositories(TechnologyAdapter<?> technologyAdapter) {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getAllRepositories(technologyAdapter);
		}
		return null;
	}

	/**
	 * Return the list of all global {@link ResourceRepositoryImpl} discovered in this {@link InformationSpace}, related to technology as
	 * supplied by {@link TechnologyAdapter} parameter.<br>
	 * One global repository for each {@link FlexoResourceCenter} is returned
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public <TA extends TechnologyAdapter<TA>> List<TechnologyAdapterResourceRepository<?, TA, ?, ?>> getGlobalRepositories(
			TA technologyAdapter) {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getGlobalRepositories(technologyAdapter);
		}
		return null;
	}

	/**
	 * Return the list of all non-empty {@link ResourceRepositoryImpl} discovered in the scope of {@link FlexoServiceManager} which may give
	 * access to some instance of supplied resource data class, related to technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public <RD extends ResourceData<RD>> List<ResourceRepository<? extends FlexoResource<RD>, ?>> getAllRepositories(
			TechnologyAdapter<?> technologyAdapter, Class<RD> resourceDataClass) {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getAllRepositories(technologyAdapter, resourceDataClass);
		}
		return null;
	}

	/**
	 * Returns the resource identified by the given <code>uri</code>
	 * 
	 * @param uri
	 *            the URI of the resource
	 * @return the resource with the given <code>uri</code>, or null if it cannot be found.
	 */
	public @Nullable FlexoResource<?> getResource(@Nonnull String uri, FlexoVersion version) {
		if (getServiceManager() != null) {
			for (FlexoResourceCenter<?> rc : getServiceManager().getResourceCenterService().getResourceCenters()) {
				FlexoResource<?> res = rc.retrieveResource(uri);
				if (res != null) {
					return res;
				}
			}

		}
		return null;
	}

	/**
	 * Returns a typed resource identified by the given <code>uri</code>
	 * 
	 * @param uri
	 *            the URI of the resource
	 * @param type
	 *            the type of the resource data reference by the resource to retrieve. The implementation is responsible to make the
	 *            appropriate type verifications.
	 * @return the resource with the given <code>uri</code>, or null if it cannot be found.
	 */
	public @Nullable <T extends ResourceData<T>> FlexoResource<T> getResource(@Nonnull String uri, FlexoVersion version,
			@Nonnull Class<T> type) {
		// TODO: a better type checking would be better
		return (FlexoResource<T>) getResource(uri, version);
	}

	// TODO: also handle version parameter
	public FlexoMetaModelResource<?, ?, ?> getMetaModelWithURI(String uri) {
		for (TechnologyAdapter<?> ta : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
			FlexoMetaModelResource<?, ?, ?> returned = getMetaModelWithURI(uri, ta);
			if (returned != null) {
				return returned;
			}
		}
		return null;
	}

	// TODO: also handle version parameter
	public FlexoMetaModelResource<?, ?, ?> getMetaModelWithURI(String uri, TechnologyAdapter<?> technologyAdapter) {
		if (technologyAdapter != null && technologyAdapter.getTechnologyContextManager() != null) {
			return (FlexoMetaModelResource<?, ?, ?>) technologyAdapter.getTechnologyContextManager().getResourceWithURI(uri);
		}
		return null;
	}

	// TODO: also handle version parameter
	public FlexoModelResource<?, ?, ?, ?> getModelWithURI(String uri) {
		for (TechnologyAdapter<?> ta : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
			FlexoModelResource<?, ?, ?, ?> returned = getModelWithURI(uri, ta);
			if (returned != null) {
				return returned;
			}
		}
		return null;
	}

	// TODO: also handle version parameter
	public FlexoModelResource<?, ?, ?, ?> getModelWithURI(String uri, TechnologyAdapter<?> technologyAdapter) {
		if (technologyAdapter == null) {
			logger.warning("Unexpected null " + technologyAdapter);
			return null;
		}
		else if (technologyAdapter.getTechnologyContextManager() == null) {
			// logger.warning("Unexpected null technologyContextManager for " + technologyAdapter);
			return null;
		}
		return (FlexoModelResource<?, ?, ?, ?>) technologyAdapter.getTechnologyContextManager().getResourceWithURI(uri);
	}

	/**
	 * Return the list of all non-empty {@link ModelRepository} discoverable in the scope of {@link FlexoServiceManager}, related to
	 * technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public List<ModelRepository<?, ?, ?, ?, ?, ?>> getAllModelRepositories(TechnologyAdapter<?> technologyAdapter) {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getAllModelRepositories(technologyAdapter);
		}
		return null;
	}

	/**
	 * Return the list of all non-empty {@link MetaModelRepository} discoverable in the scope of {@link FlexoServiceManager}, related to
	 * technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public List<MetaModelRepository<?, ?, ?, ?, ?>> getAllMetaModelRepositories(TechnologyAdapter<?> technologyAdapter) {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getAllMetaModelRepositories(technologyAdapter);
		}
		return null;
	}

	@Override
	public void notifyObjectLoaded(FlexoObjectReference<?> reference) {

	}

	@Override
	public void objectCantBeFound(FlexoObjectReference<?> reference) {

	}

	@Override
	public void objectDeleted(FlexoObjectReference<?> reference) {

	}

	@Override
	public void objectSerializationIdChanged(FlexoObjectReference<?> reference) {

	}

	public List<FlexoResource<?>> getResources(Object serializationArtefact) {
		List<FlexoResource<?>> returned = new ArrayList<>();
		for (FlexoResource<?> flexoResource : resources) {
			// System.out.println(" > " + flexoResource + " io=" + flexoResource.getIODelegate().getSerializationArtefact());
			if (flexoResource.getIODelegate().getSerializationArtefact().equals(serializationArtefact)) {
				returned.add(flexoResource);
			}
			else if (flexoResource.getIODelegate() instanceof DirectoryBasedIODelegate) {
				if (((DirectoryBasedIODelegate) flexoResource.getIODelegate()).getDirectory().equals(serializationArtefact)) {
					returned.add(flexoResource);
				}
			}
			else if (flexoResource.getIODelegate() instanceof DirectoryBasedJarIODelegate) {
				if (((DirectoryBasedJarIODelegate) flexoResource.getIODelegate()).getDirectory().equals(serializationArtefact)) {
					returned.add(flexoResource);
				}
			}
		}
		return returned;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
	}

}
