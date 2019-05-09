/**
 * 
 * Copyright (c) 2013-2014, Openflexo
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.openflexo.connie.annotations.NotificationUnsafe;
import org.openflexo.foundation.DataFlexoObserver;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * Default implementation for {@link ResourceRepository}
 * 
 * @author sylvain
 * 
 * @param <R>
 *            type of resources being stored in this {@link ResourceRepositoryImpl}
 * @param <I>
 *            serialization artefact type
 */
public abstract class ResourceRepositoryImpl<R extends FlexoResource<?>, I> extends FlexoObjectImpl
		implements DataFlexoObserver, ResourceRepository<R, I> {

	private static final Logger logger = Logger.getLogger(ResourceRepositoryImpl.class.getPackage().getName());

	private I baseArtefact;

	/**
	 * Hashtable where resources are stored, used key is the URI of the resource
	 */
	protected HashMap<String, R> resources;

	private RepositoryFolder<R, I> rootFolder;

	/** Stores the resource center which is the "owner" of this repository */
	private FlexoResourceCenter<I> resourceCenter;

	/**
	 * Creates a new {@link ResourceRepositoryImpl}
	 */
	public ResourceRepositoryImpl() {
		resources = new HashMap<>();
	}

	/**
	 * Creates a new {@link ResourceRepositoryImpl}
	 */
	public ResourceRepositoryImpl(FlexoResourceCenter<I> resourceCenter, I baseArtefact) {
		this();
		this.resourceCenter = resourceCenter;
		this.baseArtefact = baseArtefact;
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getBaseArtefact()
	 */
	@Override
	public I getBaseArtefact() {
		return baseArtefact;
	}

	@Override
	public void setBaseArtefact(I baseArtefact) {
		if ((baseArtefact == null && this.baseArtefact != null) || (baseArtefact != null && !baseArtefact.equals(this.baseArtefact))) {
			I oldValue = this.baseArtefact;
			this.baseArtefact = baseArtefact;
			getPropertyChangeSupport().firePropertyChange("baseArtefact", oldValue, baseArtefact);
			rootFolder = new RepositoryFolder<>(baseArtefact, null, this);
		}
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getRootFolder()
	 */
	@Override
	@NotificationUnsafe
	public RepositoryFolder<R, I> getRootFolder() {
		return rootFolder;
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getDefaultBaseURI()
	 */
	@Override
	public abstract String getDefaultBaseURI();

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#generateURI(java.lang.String)
	 */
	@Override
	public String generateURI(String baseName) {
		String baseURI = getDefaultBaseURI() + "/" + baseName;
		String returnedURI = baseURI;
		int i = 1;
		while (getResource(returnedURI) != null) {
			i++;
			returnedURI = baseURI + i;
		}
		return returnedURI;
	}

	@Override
	public FlexoServiceManager getServiceManager() {
		if (getResourceCenter() != null) {
			return getResourceCenter().getServiceManager();
		}
		return super.getServiceManager();
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getResourceCenter()
	 */
	@Override
	public FlexoResourceCenter<I> getResourceCenter() {
		return resourceCenter;
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#setResourceCenter(org.openflexo.foundation.resource.FlexoResourceCenter)
	 */
	@Override
	public void setResourceCenter(FlexoResourceCenter<I> resourceCenter) {
		if ((resourceCenter == null && this.resourceCenter != null)
				|| (resourceCenter != null && !resourceCenter.equals(this.resourceCenter))) {
			FlexoResourceCenter<I> oldValue = this.resourceCenter;
			this.resourceCenter = resourceCenter;
			getPropertyChangeSupport().firePropertyChange("resourceCenter", oldValue, resourceCenter);
		}
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getResource(java.lang.String)
	 */
	@Override
	public R getResource(String resourceURI) {
		R returned = resources.get(resourceURI);

		// TODO: perf issue : implement a scheme to avoid another search for an URI that could not be resolved once (unless some other
		// resources are registered or unregistered)

		// scheme to resolve resources from URI whose value has changed since registration
		for (String oldURI : new ArrayList<>(resources.keySet())) {
			R resource = resources.get(oldURI);
			if (!Objects.equals(oldURI, resource.getURI())) {
				resources.remove(oldURI);
				resources.put(resource.getURI(), resource);
			}
			if (Objects.equals(resource.getURI(), resourceURI)) {
				return resource;
			}
		}
		return returned;
	}

	/**
	 * Return first found resource matching supplied serialization artefact
	 * 
	 * @param serializationArtefact
	 * @return
	 */
	public R getResource(I serializationArtefact) {

		// TODO: perf issue ?

		for (R r : getAllResources()) {
			if (serializationArtefact.equals(r.getIODelegate().getSerializationArtefact())) {
				return r;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#registerResource(R)
	 */
	@Override
	public void registerResource(R flexoResource) {
		registerResource(flexoResource, getRootFolder());
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#unregisterResource(R)
	 */
	@Override
	public void unregisterResource(R flexoResource) {
		if (flexoResource.getContainer() != null) {
			flexoResource.getContainer().removeFromContents(flexoResource);
		}
		RepositoryFolder<R, I> parentFolder = getParentFolder(flexoResource);
		if (parentFolder != null) {
			parentFolder.removeFromResources(flexoResource);
		}
		resources.remove(flexoResource.getURI());

	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#registerResource(R, org.openflexo.foundation.resource.RepositoryFolder)
	 */
	@Override
	public void registerResource(R resource, RepositoryFolder<R, I> parentFolder) {
		if (resource == null) {
			logger.warning("Trying to register a null resource");
			return;
		}
		resource.setResourceCenter(getResourceCenter());
		parentFolder.addToResources(resource);
		resources.put(resource.getURI(), resource);
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#registerResource(R, R)
	 */
	@Override
	public void registerResource(R resource, R parentResource) {
		if (resource == null) {
			logger.warning("Trying to register a null resource");
			return;
		}
		resource.setResourceCenter(getResourceCenter());
		resources.put(resource.getURI(), resource);
		if (parentResource.getContents().contains(resource)) {
			parentResource.addToContents(resource);
		}
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#createNewFolder(java.lang.String, org.openflexo.foundation.resource.RepositoryFolder)
	 */
	@Override
	public RepositoryFolder<R, I> createNewFolder(String folderName, RepositoryFolder<R, I> parentFolder) {
		// System.out.println("Create folder " + folderName + " parent=" + parentFolder);
		// System.out.println("parent file = " + parentFolder.getFile());
		I serializationArtefact = getResourceCenter().createDirectory(folderName, parentFolder.getSerializationArtefact());
		RepositoryFolder<R, I> newFolder = new RepositoryFolder<>(serializationArtefact, parentFolder, this);

		return newFolder;
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#createNewFolder(java.lang.String)
	 */
	@Override
	public RepositoryFolder<R, I> createNewFolder(String folderName) {
		return createNewFolder(folderName, getRootFolder());
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#deleteFolder(org.openflexo.foundation.resource.RepositoryFolder)
	 */
	@Override
	public void deleteFolder(RepositoryFolder<R, I> folder) {
		RepositoryFolder<R, I> parentFolder = getParentFolder(folder);
		if (parentFolder != null && folder.getResources().size() == 0) {
			parentFolder.removeFromChildren(folder);
			folder.delete();
		}
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#moveResource(R, org.openflexo.foundation.resource.RepositoryFolder, org.openflexo.foundation.resource.RepositoryFolder)
	 */
	@Override
	public void moveResource(R resource, RepositoryFolder<R, I> fromFolder, RepositoryFolder<R, I> toFolder) {
		if (getParentFolder(resource) == fromFolder) {
			fromFolder.removeFromResources(resource);
			toFolder.addToResources(resource);
			// TODO: reimplement this with more genericity (delegate to RC)
			if (resource.getIODelegate() instanceof FileIODelegate) {
				File fromFile = ((FileIODelegate) resource.getIODelegate()).getFile();
				File toFile = new File((File) toFolder.getSerializationArtefact(), fromFile.getName());
				try {
					FileUtils.rename(fromFile, toFile);
					((FileIODelegate) resource.getIODelegate()).setFile(toFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getAllResources()
	 */
	@Override
	public Collection<R> getAllResources() {
		return resources.values();
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#containsResource(R)
	 */
	// TODO: perf issue
	@Override
	public boolean containsResource(R resource) {
		return getAllResources().contains(resource);
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getRepositoryFolder(R)
	 */
	@Override
	public RepositoryFolder<R, I> getRepositoryFolder(R resource) {
		return getRootFolder().getRepositoryFolder(resource);
	}

	@Override
	public void update(FlexoObservable observable, DataModification<?> dataModification) {
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getSize()
	 */
	@Override
	public int getSize() {
		return resources.size();
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#allFolders()
	 */
	@Override
	public Enumeration<RepositoryFolder<R, I>> allFolders() {
		Vector<RepositoryFolder<R, I>> temp = new Vector<>();
		addFolders(temp, getRootFolder());
		return temp.elements();
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#allFoldersCount()
	 */
	@Override
	public int allFoldersCount() {
		Vector<RepositoryFolder<R, I>> temp = new Vector<>();
		addFolders(temp, getRootFolder());
		return temp.size();
	}

	private void addFolders(List<RepositoryFolder<R, I>> temp, RepositoryFolder<R, I> folder) {
		temp.add(folder);
		for (RepositoryFolder<R, I> currentFolder : folder.getChildren()) {
			addFolders(temp, currentFolder);
		}
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getFolderWithName(java.lang.String)
	 */
	@Override
	public RepositoryFolder<R, I> getFolderWithName(String folderName) {
		for (Enumeration<RepositoryFolder<R, I>> e = allFolders(); e.hasMoreElements();) {
			RepositoryFolder<R, I> folder = e.nextElement();

			if (folder.getName().equals(folderName)) {
				return folder;
			}

		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Could not find folder named " + folderName);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getParentFolder(R)
	 */
	@Override
	public RepositoryFolder<R, I> getParentFolder(R resource) {
		for (Enumeration<RepositoryFolder<R, I>> e = allFolders(); e.hasMoreElements();) {
			RepositoryFolder<R, I> folder = e.nextElement();
			if (folder.getResources().contains(resource)) {
				return folder;
			}

		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getParentFolder(org.openflexo.foundation.resource.RepositoryFolder)
	 */
	@Override
	public RepositoryFolder<R, I> getParentFolder(RepositoryFolder<R, I> aFolder) {
		for (Enumeration<RepositoryFolder<R, I>> e = allFolders(); e.hasMoreElements();) {
			RepositoryFolder<R, I> folder = e.nextElement();
			if (folder.getChildren().contains(aFolder)) {
				return folder;
			}

		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getParentRepositoryFolder(I, boolean)
	 */
	@Override
	public RepositoryFolder<R, I> getParentRepositoryFolder(I serializationArtefact, boolean createWhenNonExistent) throws IOException {
		List<String> pathTo = getResourceCenter().getPathTo(serializationArtefact);
		return getRepositoryFolder(pathTo, createWhenNonExistent);
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getRepositoryFolder(I, boolean)
	 */
	@Override
	public RepositoryFolder<R, I> getRepositoryFolder(I serializationArtefact, boolean createWhenNonExistent) throws IOException {
		List<String> pathTo = getResourceCenter().getPathTo(serializationArtefact);
		// NPE Protection when serializationArtefact is not in the given resourceCenter
		if (pathTo != null) {
			pathTo.add(getResourceCenter().retrieveName(serializationArtefact));
		}
		return getRepositoryFolder(pathTo, createWhenNonExistent);
	}

	/**
	 * Get the repository folder from a set of path
	 * 
	 * @param pathTo
	 * @param createWhenNonExistent
	 * @return
	 * @throws IOException
	 */
	private RepositoryFolder<R, I> getRepositoryFolder(List<String> pathTo, boolean createWhenNonExistent) throws IOException {
		RepositoryFolder<R, I> returned = getRootFolder();
		if (pathTo != null) {
			for (String pathElement : pathTo) {
				RepositoryFolder<R, I> currentFolder = returned.getFolderNamed(pathElement);
				if (currentFolder == null) {
					if (createWhenNonExistent) {
						I serializationArtefact = getResourceCenter().getDirectory(pathElement, returned.getSerializationArtefact());
						RepositoryFolder<R, I> newFolder = new RepositoryFolder<>(serializationArtefact, returned, this);
						// System.out.println("On notifie " + newFolder + " pour " + returned);
						returned.getPropertyChangeSupport().firePropertyChange("children", null, newFolder);
						currentFolder = newFolder;
					}
					else {
						return null;
					}
				}
				returned = currentFolder;
			}
		}
		return returned;
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getResourceClass()
	 */
	@Override
	public final Class<?> getResourceClass() {
		return org.openflexo.connie.type.TypeUtils.getBaseClass(TypeUtils.getTypeArguments(getClass(), ResourceRepositoryImpl.class)
				.get(ResourceRepositoryImpl.class.getTypeParameters()[0]));
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getResourceDataClass()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public final Class<? extends ResourceData<?>> getResourceDataClass() {
		return (Class<? extends ResourceData<?>>) TypeUtils.getTypeArguments(getResourceClass(), FlexoResource.class)
				.get(FlexoResource.class.getTypeParameters()[0]);
	}

	@Override
	public String toString() {
		return getImplementedInterface().getSimpleName() + ":" + baseArtefact + "[" + getAllResources().size() + "]";
	}

	public String debug() {
		StringBuffer sb = new StringBuffer();
		return debug(rootFolder, sb, 0);
	}

	protected String debug(RepositoryFolder<R, I> f, StringBuffer sb, int indentLevel) {
		for (RepositoryFolder<R, I> f2 : f.getChildren()) {
			sb.append(StringUtils.buildWhiteSpaceIndentation(indentLevel * 2) + "> " + f2.getName() + "\n");
			debug(f2, sb, indentLevel + 1);
		}
		for (R resource : f.getResources()) {
			sb.append(StringUtils.buildWhiteSpaceIndentation(indentLevel * 2) + "- " + resource.getName() + "\n");
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getDisplayableName()
	 */
	@Override
	public abstract String getDisplayableName();

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#isResourceContainedIn(org.openflexo.foundation.resource.FlexoResource, org.openflexo.foundation.resource.FlexoResource)
	 */
	@Override
	public boolean isResourceContainedIn(FlexoResource<?> resource, FlexoResource<?> container) {
		if (resource == container) {
			return true;
		}
		if (resource.getContainer() == null) {
			return false;
		}
		return isResourceContainedIn(resource.getContainer(), container);
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getMostSpecializedContainer(R, R)
	 */
	// TODO: we should write unit tests for that
	@Override
	public FlexoObject getMostSpecializedContainer(R resource1, R resource2) {

		if (resource1 == null || resource2 == null) {
			return null;
		}

		if (!containsResource(resource1) || !containsResource(resource2)) {
			return null;
		}

		if (resource1 == resource2) {
			return resource2;
		}

		if (isResourceContainedIn(resource1, resource2)) {
			return resource2;
		}

		if (isResourceContainedIn(resource2, resource1)) {
			return resource1;
		}

		RepositoryFolder<R, I> folder1 = getRepositoryFolder(resource1);
		RepositoryFolder<R, I> folder2 = getRepositoryFolder(resource2);

		RepositoryFolder<R, I> commonFolder = getMostSpecializedRepositoryFolder(folder1, folder2);
		if (commonFolder != null) {
			return commonFolder;
		}

		// Otherwise, parent ancestor is the repository itself
		return this;
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.ResourceRepository#getMostSpecializedRepositoryFolder(org.openflexo.foundation.resource.RepositoryFolder, org.openflexo.foundation.resource.RepositoryFolder)
	 */
	// TODO: we should write unit tests for that
	@Override
	public RepositoryFolder<R, I> getMostSpecializedRepositoryFolder(RepositoryFolder<R, I> folder1, RepositoryFolder<R, I> folder2) {

		if (folder1 == null || folder2 == null) {
			return null;
		}
		if (folder1 == folder2) {
			return folder2;
		}

		if (folder1.getResourceRepository() != this && folder2.getResourceRepository() != this) {
			return null;
		}

		if (folder1.getParentFolder() == null && folder2.getParentFolder() == null) {
			// nothing in common
			return null;
		}

		if (folder2.isFatherOf(folder1)) {
			return folder2;
		}

		if (folder1.isFatherOf(folder2)) {
			return folder1;
		}

		RepositoryFolder<R, I> pivot = null;
		RepositoryFolder<R, I> iterated = null;
		if (folder1.getParentFolder() != null) {
			pivot = folder1;
			iterated = folder2;
		}
		else {
			pivot = folder2;
			iterated = folder1;
		}

		if (pivot.getParentFolder().isFatherOf(iterated)) {
			return pivot.getParentFolder();
		}

		RepositoryFolder<R, I> returned = getMostSpecializedRepositoryFolder(pivot.getParentFolder(), iterated);
		if (returned != null) {
			return returned;
		}

		return null;

	}

}
