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
import java.util.List;
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
public class ResourceManager extends FlexoServiceImpl implements FlexoService {

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
		resources = new ArrayList<FlexoResource<?>>();
		filesToDelete = new ArrayList<File>();
	}

	@Override
	public void initialize() {
		logger.info("Initialized ResourceManager...");
	}

	// It should be synchronized has the same resource could be registered several times in different threads
	public synchronized void registerResource(FlexoResource<?> resource) {

		if (resource.getResourceCenter() == null) {
			logger.warning("Resource belonging to no ResourceCenter: " + resource);
			// Thread.dumpStack();
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
		List<R> returned = new ArrayList<R>();
		for (FlexoResource<?> r : getRegisteredResources()) {
			if (resourceClass.isAssignableFrom(r.getClass())) {
				returned.add((R) r);
			}
		}
		return returned;
	}

	// TODO: optimize this
	public List<FlexoResource<?>> getLoadedResources() {
		List<FlexoResource<?>> returned = new ArrayList<FlexoResource<?>>();
		for (FlexoResource<?> r : resources) {
			if (r.isLoaded()) {
				returned.add(r);
			}
		}
		return returned;
	}

	// TODO: optimize this
	public List<FlexoResource<?>> getUnsavedResources() {
		List<FlexoResource<?>> returned = new ArrayList<FlexoResource<?>>();
		for (FlexoResource<?> r : new ArrayList<FlexoResource<?>>(resources)) {
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
		for (FlexoResource r : new ArrayList<>(resources)) {
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
			notifyObservers(new DataModification(null, ((ResourceCenterAdded) notification).getAddedResourceCenter()));
		}
		if (notification instanceof ResourceCenterRemoved) {
			// In this case, we MUST unregister all resources contained in removed ResourceCenter
			FlexoResourceCenter<?> removedRC = ((ResourceCenterRemoved) notification).getRemovedResourceCenter();

			for (FlexoResource<?> r : removedRC.getAllResources(null)) {
				unregisterResource(r);
			}

			// Because we cannot be confident of #getAllResources() we proceed to iterate on all registered resources
			for (FlexoResource<?> r : new ArrayList<FlexoResource<?>>(resources)) {
				if (r.getResourceCenter() == removedRC) {
					unregisterResource(r);
				}
			}

			setChanged();
			notifyObservers(new DataModification(((ResourceCenterRemoved) notification).getRemovedResourceCenter(), null));

		}
	}

	public List<TechnologyAdapter> getTechnologyAdapters() {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapters();
		}
		return null;
	}

	/**
	 * Return the list of all non-empty {@link ResourceRepository} discovered in this {@link InformationSpace}, related to technology as
	 * supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public List<ResourceRepository<?, ?>> getAllRepositories(TechnologyAdapter technologyAdapter) {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getAllRepositories(technologyAdapter);
		}
		return null;
	}

	/**
	 * Return the list of all global {@link ResourceRepository} discovered in this {@link InformationSpace}, related to technology as
	 * supplied by {@link TechnologyAdapter} parameter.<br>
	 * One global repository for each {@link FlexoResourceCenter} is returned
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public <TA extends TechnologyAdapter> List<TechnologyAdapterResourceRepository<?, TA, ?, ?>> getGlobalRepositories(
			TA technologyAdapter) {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getGlobalRepositories(technologyAdapter);
		}
		return null;
	}

	/**
	 * Return the list of all non-empty {@link ResourceRepository} discovered in the scope of {@link FlexoServiceManager} which may give
	 * access to some instance of supplied resource data class, related to technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public <RD extends ResourceData<RD>> List<ResourceRepository<? extends FlexoResource<RD>, ?>> getAllRepositories(
			TechnologyAdapter technologyAdapter, Class<RD> resourceDataClass) {
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
				FlexoResource<?> res = rc.retrieveResource(uri, null);
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
		for (TechnologyAdapter ta : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
			FlexoMetaModelResource<?, ?, ?> returned = getMetaModelWithURI(uri, ta);
			if (returned != null) {
				return returned;
			}
		}
		return null;
	}

	// TODO: also handle version parameter
	public FlexoMetaModelResource<?, ?, ?> getMetaModelWithURI(String uri, TechnologyAdapter technologyAdapter) {
		if (technologyAdapter != null && technologyAdapter.getTechnologyContextManager() != null) {
			return (FlexoMetaModelResource<?, ?, ?>) technologyAdapter.getTechnologyContextManager().getResourceWithURI(uri);
		}
		return null;
	}

	// TODO: also handle version parameter
	public FlexoModelResource<?, ?, ?, ?> getModelWithURI(String uri) {
		for (TechnologyAdapter ta : getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
			FlexoModelResource<?, ?, ?, ?> returned = getModelWithURI(uri, ta);
			if (returned != null) {
				return returned;
			}
		}
		return null;
	}

	// TODO: also handle version parameter
	public FlexoModelResource<?, ?, ?, ?> getModelWithURI(String uri, TechnologyAdapter technologyAdapter) {
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
	public List<ModelRepository<?, ?, ?, ?, ?, ?>> getAllModelRepositories(TechnologyAdapter technologyAdapter) {
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
	public List<MetaModelRepository<?, ?, ?, ?, ?>> getAllMetaModelRepositories(TechnologyAdapter technologyAdapter) {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getAllMetaModelRepositories(technologyAdapter);
		}
		return null;
	}

}
