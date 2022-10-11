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

package org.openflexo.foundation.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * Default implementation for {@link FlexoResource}<br>
 * Note that this default implementation extends {@link FlexoObject}: all resources in Openflexo model are instances of {@link FlexoObject}
 * 
 * Very first draft for implementation, only implements get/load scheme
 * 
 * @param <RD>
 *            the type of the resource data reference by this resource
 * @author Sylvain
 * 
 */
public abstract class FlexoResourceImpl<RD extends ResourceData<RD>> extends FlexoObjectImpl implements FlexoResource<RD> {

	static final Logger logger = Logger.getLogger(FlexoResourceImpl.class.getPackage().getName());

	private FlexoServiceManager serviceManager = null;
	private boolean isLoading = false;
	protected boolean isUnloading = false;
	protected RD resourceData = null;

	/**
	 * Return flag indicating if this resource is loaded
	 * 
	 * @return
	 */
	@Override
	public boolean isLoaded() {
		return resourceData != null;
	}

	/**
	 * Return flag indicating if this resource is loadable<br>
	 * By default, a resource is always loadable, then this method always returns true if IO delegate exists
	 * 
	 * @return
	 */
	@Override
	public boolean isLoadable() {
		// By default, a resource is always loadable, then this method always
		// returns true if IO delegate exists
		return getIODelegate() != null && getIODelegate().exists();
	}

	protected List<ResourceWithPotentialCrossReferences<?>> getCrossReferenceDependencies() {
		List<ResourceWithPotentialCrossReferences<?>> returned = new ArrayList<>();
		for (FlexoResource<?> dependency : getDependencies()) {
			appendCrossReferenceDependencies(dependency, returned);
		}
		return returned;
	}

	protected void appendCrossReferenceDependencies(FlexoResource<?> dependency, List<ResourceWithPotentialCrossReferences<?>> l) {
		if (!l.contains(dependency) && dependency instanceof ResourceWithPotentialCrossReferences && dependency != this) {
			l.add((ResourceWithPotentialCrossReferences) dependency);
			for (FlexoResource<?> d2 : dependency.getDependencies()) {
				appendCrossReferenceDependencies(d2, l);
			}
		}
	}

	protected List<FlexoResource<?>> getNonCrossReferenceDependencies() {
		List<FlexoResource<?>> returned = new ArrayList<>();
		for (FlexoResource<?> dependency : getDependencies()) {
			if (!(dependency instanceof ResourceWithPotentialCrossReferences)) {
				returned.add(dependency);
			}
		}
		return returned;
	}

	/**
	 * Returns the &quot;real&quot; resource data of this resource. This may cause the loading of the resource data.
	 * 
	 * @return the resource data.
	 * @throws ResourceLoadingCancelledException
	 */
	@Override
	public RD getResourceData()
			throws ResourceLoadingCancelledException, ResourceLoadingCancelledException, FileNotFoundException, FlexoException {

		if (isDeleted()) {
			return null;
		}
		if (isUnloading) {
			return null;
		}
		if (isLoading()) {
			// Avoid stack overflow, but this should never happen
			logger.warning("Preventing StackOverflow while loading resource " + this);
			Thread.dumpStack();
			return resourceData;
		}

		if (resourceData == null && isLoadable() && !isLoading()) {
			// The resourceData is null, we try to load it

			// Now load the non cross-reference dependencies
			for (FlexoResource<?> dependency : getNonCrossReferenceDependencies()) {
				dependency.loadResourceData();
			}

			// Then really load
			setLoading(true);
			resourceData = loadResourceData();
			setLoading(false);
		}
		return resourceData;
	}

	/**
	 * Return flag indicating if this resource is being loaded
	 * 
	 * @return
	 */
	@Override
	public boolean isLoading() {
		return isLoading;
	}

	/**
	 * Set a flag indicating if this resource is being loaded
	 * 
	 * @return
	 */
	public void setLoading(boolean isLoading) {
		this.isLoading = isLoading;
	}

	/**
	 * Returns the &quot;real&quot; resource data of this resource, asserting resource data is already loaded. If the resource is not
	 * loaded, do not load the data, and return null
	 * 
	 * @return the resource data.
	 */
	@Override
	public RD getLoadedResourceData() {
		return resourceData;
	}

	/**
	 * Programmatically sets {@link ResourceData} for this resource<br>
	 * The resource is then notified that it has been loaded
	 * 
	 * @param resourceData
	 */
	@Override
	public void setResourceData(RD resourceData) {
		this.resourceData = resourceData;
		// notifyResourceLoaded();
	}

	/**
	 * Rename resource
	 * 
	 * @param aName
	 */
	@Override
	public void setName(String aName) throws CannotRenameException {
		if (StringUtils.isEmpty(aName)) {
			// logger.warning("Trying to rename a FlexoResource with a null or empty name. Please investigate.");
			return;
		}
		if (getIODelegate() != null && getIODelegate().hasWritePermission()) {
			if (computeDefaultURI() != null && computeDefaultURI().equals(getURI())) {
				// This was the default URI, which should then be recomputed
				setURI(null);
			}
			performSuperSetter(NAME, aName);
			if (getIODelegate() != null) {
				getIODelegate().rename(aName);
			}
			// System.out.println("Renamed URI: " + getURI());
		}
		else if (!isDeleting()) {
			System.out.println("Trying to rename " + this + " from " + getName() + " to " + aName);
			System.out.println("IOdelegate=" + getIODelegate());
			if (getIODelegate() != null) {
				System.out.println("Write permission: " + getIODelegate().hasWritePermission());
			}
			throw new CannotRenameException(this);
		}
	}

	/**
	 * Called to init name of the resource<br>
	 * No renaming is performed here: use this method at the very beginning of life-cyle of FlexoResource
	 * 
	 * @param aName
	 */
	@Override
	public void initName(String aName) {
		performSuperSetter(NAME, aName);
	}

	@Override
	public void notifyResourceWillLoad() {
		ResourceWillLoad notification = new ResourceWillLoad(this);
		setChanged();
		notifyObservers(notification);
		if (getServiceManager() != null) {
			getServiceManager().getResourceManager().resourceWillLoad(this, notification);
		}
		else {
			logger.warning("Resource " + this + " does not refer to any ServiceManager. Please investigate...");
		}
	}

	/**
	 * Called to notify that a resource has successfully been loaded
	 */
	@Override
	public void notifyResourceLoaded() {
		logger.fine("notifyResourceLoaded(), resource=" + this);

		ResourceLoaded<?> notification = new ResourceLoaded<>(this, resourceData);
		setChanged();
		notifyObservers(notification);
		// Also notify that the contents of the resource may also have changed
		setChanged();
		notifyObservers(new DataModification<>("contents", null, getContents()));
		if (getServiceManager() != null) {
			getServiceManager().getResourceManager().resourceLoaded(this, notification);
		}
		else {
			logger.warning("Resource " + this + " does not refer to any ServiceManager. Please investigate...");
		}
	}

	/**
	 * Called to notify that a resource has successfully been unloaded
	 */
	@Override
	public void notifyResourceUnloaded() {
		logger.fine("notifyResourceUnloaded(), resource=" + this);

		ResourceUnloaded<?> notification = new ResourceUnloaded<>(this, resourceData);
		setChanged();
		notifyObservers(notification);
		// Also notify that the contents of the resource may also have changed
		setChanged();
		notifyObservers(new DataModification<>("contents", null, null));
		if (getServiceManager() != null) {
			getServiceManager().getResourceManager().resourceUnloaded(this, notification);
		}
		else {
			logger.warning("Resource " + this + " does not refer to any ServiceManager. Please investigate...");
		}

	}

	/**
	 * Called to notify that a resource has successfully been saved
	 */
	@Override
	public void notifyResourceSaved() {
		logger.fine("notifyResourceSaved(), resource=" + this);

		ResourceSaved<RD> notification = new ResourceSaved<>(this, resourceData);
		setChanged();
		notifyObservers(notification);
		getServiceManager().notify(getServiceManager().getResourceManager(), notification);
	}

	/**
	 * Called to notify that a resource has been modified
	 */
	@Override
	public void notifyResourceModified() {
		// logger.info("notifyResourceModified(), resource=" + this);

		ResourceModified<RD> notification = new ResourceModified<>(this, resourceData);
		setChanged();
		notifyObservers(notification);
		getServiceManager().notify(getServiceManager().getResourceManager(), notification);

	}

	/**
	 * Called to notify that a resource has been added to contents<br>
	 * TODO: integrate this in setContents() when this interface will extends {@link AccessibleProxyObject}
	 * 
	 * @param resource
	 *            : resource being added
	 */
	@Override
	public void notifyContentsAdded(FlexoResource<?> resource) {
		logger.fine("notifyContentsAdded(), resource=" + this);

		ContentsAdded notification = new ContentsAdded(this, resource);
		setChanged();
		notifyObservers(notification);
		getServiceManager().notify(getServiceManager().getResourceManager(), notification);
	}

	/**
	 * Called to notify that a resource has been remove to contents<br>
	 * TODO: integrate this in setContents() when this interface will extends {@link AccessibleProxyObject}
	 * 
	 * @param resource
	 *            : resource being removed
	 */
	@Override
	public void notifyContentsRemoved(FlexoResource<?> resource) {
		logger.fine("notifyContentsRemoved(), resource=" + this);

		ContentsRemoved notification = new ContentsRemoved(this, resource);
		setChanged();
		notifyObservers(notification);
		getServiceManager().notify(getServiceManager().getResourceManager(), notification);
	}

	@Override
	public void notifyResourceStatusChanged() {
	}

	@Override
	public String toString() {
		if (getLoadedResourceData() instanceof FlexoObject) {
			return ((FlexoObject) getLoadedResourceData()).getImplementedInterface().getSimpleName() + "." + getURI() + "." + getVersion()
					+ "." + getRevision();
		}
		return getClass().getSimpleName() + "." + getURI() + "." + getVersion() + "." + getRevision();
	}

	/*public List<? extends Resource> getContents(boolean deep) {
		// TODO: manage depth
		return getContents();
	}*/

	/*@Override
	public List<FlexoResource<?>> getContents() {
		return null;
	}*/

	/**
	 * Returns a list of resources of supplied type contained by this resource.
	 * 
	 * @return the list of contained resources.
	 */
	@Override
	public <R extends FlexoResource<?>> List<R> getContents(Class<R> resourceClass) {
		ArrayList<R> returned = new ArrayList<>();
		for (FlexoResource<?> r : getContents()) {
			if (resourceClass.isAssignableFrom(r.getClass())) {
				returned.add((R) r);
			}
		}
		return returned;
	}

	/**
	 * Returns the resource identified in supplied URI, asserting that is resource is of supplied type
	 * 
	 * @return
	 */
	@Override
	public <R extends FlexoResource<?>> R getContentWithURI(Class<R> resourceClass, String resourceURI) {
		for (R r : getContents(resourceClass)) {
			if (r != null && r.getURI().equals(resourceURI)) {
				return r;
			}
		}
		return null;
	}

	@Override
	public FlexoServiceManager getServiceManager() {
		return serviceManager;
	}

	/**
	 * Sets and register the service manager<br>
	 * Also (VERY IMPORTANT) register the resource in the FlexoEditingContext !!!
	 */
	@Override
	public void setServiceManager(FlexoServiceManager serviceManager) {
		this.serviceManager = serviceManager;
		if (getServiceManager() != null) {
			try {
				getServiceManager().getResourceManager().registerResource(this);
			} catch (DuplicateURIException e) {
				logger.warning(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Indicates whether this resource can be edited or not. Returns <code>true</code> if the resource cannot be edited, else returns
	 * <code>false</code>.<br>
	 * This is here the default implementation, always returned false;
	 * 
	 * @return <code>true</code> if the resource cannot be edited, else returns <code>false</code>.
	 */
	@Override
	public boolean isReadOnly() {
		if (getIODelegate() != null && !getIODelegate().hasWritePermission()) {
			return true;
		}
		return false;
	}

	private boolean isDeleting = false;

	@Override
	public boolean isDeleting() {
		return isDeleting;
	}

	/**
	 * Delete this resource<br>
	 * Contents of this resource are deleted, and resource data is unloaded
	 */
	@Override
	public boolean delete(Object... context) {
		if (isReadOnly()) {
			logger.warning("Delete requested for READ-ONLY resource " + this);
			return false;
		}
		isDeleting = true;
		logger.info("Deleting resource " + this);

		if (getResourceCenter() instanceof ResourceRepositoryImpl) {
			((ResourceRepository) getResourceCenter()).unregisterResource(this);
		}

		if (getContainer() != null) {
			FlexoResource<?> container = getContainer();
			container.removeFromContents(this);
			container.notifyContentsRemoved(this);
		}
		for (org.openflexo.foundation.resource.FlexoResource<?> r : new ArrayList<>(getContents())) {
			r.delete();
		}

		if (isLoaded()) {
			unloadResourceData(true);
		}

		// Handle Flexo IO delegate deletion
		getIODelegate().delete();

		performSuperDelete(context);

		isDeleting = false;

		return true;
	}

	/**
	 * Delete (dereference) resource data if resource data is loaded<br>
	 * Also delete the resource data
	 */
	@Override
	public void unloadResourceData(boolean deleteResourceData) {
		if (isLoaded()) {
			isUnloading = true;
			if (deleteResourceData) {
				resourceData.delete();
			}
			resourceData = null;
			// That's fine, resource is loaded, now let's notify the loading of
			// the resources
			notifyResourceUnloaded();
			isUnloading = false;
		}
	}

	/**
	 * Callback called when a cycle was detected in Resource Loading Scheme, and when the resource beeing requested has finally been loaded.
	 * 
	 * @param requestedResource
	 */
	@Override
	public void resolvedCrossReferenceDependency(FlexoResource<?> requestedResource) {
		// Override when required
	}

	/**
	 * Return flag indicating if this resource support external update<br>
	 * Default implementation return false
	 * 
	 * @return
	 */
	@Override
	public boolean isUpdatable() {
		return false;
	}

	/**
	 * If this resource support external update (reloading), perform it now<br>
	 * Default implementation does nothing
	 * 
	 * @see #isUpdatable()
	 */
	@Override
	public void updateResourceData() {
	}

	@Override
	public final boolean isModified() {
		return isLoaded() && getLoadedResourceData().isModified();
	}

	@Override
	public void notifyObjectLoaded(FlexoObjectReference<?> reference) {
		logger.warning("TODO: implement this");
	}

	@Override
	public void objectCantBeFound(FlexoObjectReference<?> reference) {
		logger.warning("TODO: implement this");
	}

	@Override
	public void objectSerializationIdChanged(FlexoObjectReference<?> reference) {
		setChanged();
	}

	@Override
	public void objectDeleted(FlexoObjectReference<?> reference) {
		logger.warning("TODO: implement this");
	}

	// TODO: check this
	/*public void setContainer(Resource resource) {
		if (resource instanceof FlexoResource) {
			setContainer((FlexoResource<?>) resource);
		}
	}*/

	// TODO: check this
	/*@Override
	public List<? extends Resource> getContents(Pattern pattern, boolean deep) {
		return null;
	}*/

	// TODO: check this
	/*@Override
	public ResourceLocatorDelegate getLocator() {
		return null;
	}*/

	// TODO: check this
	/*@Override
	public InputStream openInputStream() {
		if (getFlexoIODelegate() instanceof FlexoIOStreamDelegate) {
			return ((FlexoIOStreamDelegate) getFlexoIODelegate()).getInputStream();
		}
		return null;
	}*/

	// TODO: check this
	/*@Override
	public OutputStream openOutputStream() {
		if (getFlexoIODelegate() instanceof FlexoIOStreamDelegate) {
			return ((FlexoIOStreamDelegate) getFlexoIODelegate()).getOutputStream();
		}
		return null;
	}*/

	// TODO: check this
	/*@Override
	public final String getRelativePath() {
		return null;
	}*/

	/*@Override
	public String computeRelativePath(Resource resource) {
		return null;
	}*/

	/*@Override
	public Resource locateResource(String relativePathName) {
		return null;
	}*/

	// TODO: check this
	/*@Override
	public boolean isContainer() {
		return true;
	}*/

	/**
	 * Return URI of this resource<br>
	 * If URI was set for this resource, return that URI, otherwise delegate default URI computation to resource center in which this
	 * resource exists
	 */
	@Override
	public final String getURI() {
		String returned = (String) performSuperGetter(URI);

		if (returned == null && getResourceCenter() != null) {
			returned = computeDefaultURI();
		}

		if (returned == null && (getIODelegate() instanceof FileIODelegate) && (((FileIODelegate) getIODelegate()).getFile() != null)) {
			if (((FileIODelegate) getIODelegate()).getFile() != null) {
				return ((FileIODelegate) getIODelegate()).getFile().toURI().toString();
			}
		}
		return returned;
	}

	@Override
	public String computeDefaultURI() {
		return getResourceCenter().getDefaultResourceURI(this);
	}

	@Override
	public LocalizedDelegate getLocales() {
		if (this instanceof TechnologyAdapterResource) {
			return ((TechnologyAdapterResource<?, ?>) this).getTechnologyAdapter().getLocales();
		}
		return super.getLocales();
	}

	/*
	 * public void willWrite(File file) { getServiceManager().notify(null, new
	 * WillWriteFileOnDiskNotification(file)); }
	 * 
	 * public void hasWritten(File file) { getServiceManager().notify(null, new
	 * FileHasBeenWrittenOnDiskNotification(file)); }
	 * 
	 * public void willRename(File fromFile, File toFile) {
	 * getServiceManager().notify(null, new
	 * WillRenameFileOnDiskNotification(fromFile, toFile)); }
	 * 
	 * public void willDelete(File file) { getServiceManager().notify(null, new
	 * WillDeleteFileOnDiskNotification(file)); }
	 */

	private boolean needsConversion = false;

	@Override
	public void setNeedsConversion() {
		needsConversion = true;
	}

	@Override
	public boolean needsConversion() {
		return needsConversion;
	}

	/**
	 * Return displayable name for this FlexoResource
	 * 
	 * @return
	 */
	@Override
	public String getDisplayName() {
		if (getIODelegate() != null) {
			return getIODelegate().getDisplayName();
		}
		return getName();
	}

	protected static void makeLocalCopy(File file) throws IOException {
		if (file != null && file.exists()) {
			String localCopyName = file.getName() + "~";
			File localCopy = new File(file.getParentFile(), localCopyName);
			FileUtils.copyFileToFile(file, localCopy);
		}
	}

	@Override
	public <I> String pathRelativeToResourceCenter(FlexoResourceCenter<I> rc) {
		return rc.relativePath((I) getIODelegate().getSerializationArtefact());
	}

	@Override
	public <I> String parentPathRelativeToResourceCenter(FlexoResourceCenter<I> rc) {
		I parent = rc.getContainer((I) getIODelegate().getSerializationArtefact());
		return rc.relativePath(parent);
	}

	@Override
	public <I> String parentParentPathRelativeToResourceCenter(FlexoResourceCenter<I> rc) {
		I parent = rc.getContainer((I) getIODelegate().getSerializationArtefact());
		I parentParent = rc.getContainer(parent);
		return rc.relativePath(parentParent);
	}

}
