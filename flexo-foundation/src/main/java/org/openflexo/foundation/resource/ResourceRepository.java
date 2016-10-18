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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.openflexo.foundation.DataFlexoObserver;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.DefaultFlexoObject;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * A {@link ResourceRepository} stores all resources of a particular type.<br>
 * Resources are organized with a folder hierarchy inside a repository<br>
 * A {@link ResourceRepository} lives in a {@link FlexoResourceCenter}.
 * 
 * @author sylvain
 * 
 * @param <R>
 *            type of resources being stored in this {@link ResourceRepository}
 * @param <I>
 *            serialization artefact type
 */
public abstract class ResourceRepository<R extends FlexoResource<?>, I> extends DefaultFlexoObject implements DataFlexoObserver {

	private static final Logger logger = Logger.getLogger(ResourceRepository.class.getPackage().getName());

	private I baseArtefact;

	/**
	 * Hashtable where resources are stored, used key is the URI of the resource
	 */
	protected HashMap<String, R> resources;

	private final RepositoryFolder<R, I> rootFolder;

	/** Stores the resource center which is the "owner" of this repository */
	private FlexoResourceCenter<I> resourceCenter;

	/**
	 * Creates a new {@link ResourceRepository}
	 */
	public ResourceRepository(FlexoResourceCenter<I> resourceCenter, I baseArtefact) {
		this.resourceCenter = resourceCenter;
		resources = new HashMap<String, R>();
		rootFolder = new RepositoryFolder<R, I>(baseArtefact, null, this);
		this.baseArtefact = baseArtefact;
	}

	public I getBaseArtefact() {
		return baseArtefact;
	}

	protected void setBaseArtefact(I baseArtefact) {
		if ((baseArtefact == null && this.baseArtefact != null) || (baseArtefact != null && !baseArtefact.equals(this.baseArtefact))) {
			I oldValue = this.baseArtefact;
			this.baseArtefact = baseArtefact;
			getPropertyChangeSupport().firePropertyChange("baseArtefact", oldValue, baseArtefact);
			rootFolder.setSerializationArtefact(baseArtefact);
		}
	}

	public RepositoryFolder<R, I> getRootFolder() {
		return rootFolder;
	}

	/**
	 * Return the default base URI associated with the {@link ResourceRepository}.<br>
	 * 
	 * This URI might be used as default base URI for any resource stored in this repository, if no explicit URI was given to related
	 * resource. Resulting URI will be given by concatenation of this base URI with base name for related resource
	 * 
	 * @return
	 */
	public abstract String getDefaultBaseURI();

	/**
	 * Generate and return an URI of supplied resource, if this resource has no explicit URI, asserting the resource is not yet contained in
	 * repository (otherwise, an new URI will be generated)<br>
	 * 
	 * Returned URI will be given by concatenation of this base URI with base name for related resource
	 * 
	 * @return
	 */
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

	/**
	 * Return the object which is the "owner" of this repository.<br>
	 * The {@link FlexoResourceCenter} as owner has the responsability of this repository.
	 */
	public FlexoResourceCenter<I> getResourceCenter() {
		return resourceCenter;
	}

	/**
	 * Sets the "owner" of this repository
	 * 
	 * @param owner
	 */
	public void setResourceCenter(FlexoResourceCenter<I> resourceCenter) {
		if ((resourceCenter == null && this.resourceCenter != null)
				|| (resourceCenter != null && !resourceCenter.equals(this.resourceCenter))) {
			FlexoResourceCenter<I> oldValue = this.resourceCenter;
			this.resourceCenter = resourceCenter;
			getPropertyChangeSupport().firePropertyChange("resourceCenter", oldValue, resourceCenter);
		}
	}

	/**
	 * Return resource with the supplied URI, if this resource was already declared<br>
	 * Also implement a scheme to resolve resources from URI whose value has changed since registration
	 * 
	 * @param resourceURI
	 * @return
	 */
	public R getResource(String resourceURI) {
		R returned = resources.get(resourceURI);

		// TODO: perf issue : implement a scheme to avoid another search for an URI that could not be resolved once (unless some other
		// resources are registered or unregistered)

		// scheme to resolve resources from URI whose value has changed since registration
		for (String oldURI : new ArrayList<String>(resources.keySet())) {
			R resource = resources.get(oldURI);
			if (!oldURI.equals(resource.getURI())) {
				resources.remove(oldURI);
				resources.put(resource.getURI(), resource);
			}
			if (resource.getURI().equals(resourceURI)) {
				return resource;
			}
		}
		return returned;
	}

	/**
	 * Register supplied resource in default root folder
	 * 
	 * @param flexoResource
	 */
	public void registerResource(R flexoResource) {
		registerResource(flexoResource, getRootFolder());
	}

	public void unregisterResource(R flexoResource) {
		RepositoryFolder<R, I> parentFolder = getParentFolder(flexoResource);
		parentFolder.removeFromResources(flexoResource);
		resources.remove(flexoResource.getURI());

	}

	/**
	 * Register supplied resource in supplied folder
	 * 
	 * @param resource
	 * @param parentFolder
	 */
	public void registerResource(R resource, RepositoryFolder<R, I> parentFolder) {
		if (resource == null) {
			logger.warning("Trying to register a null resource");
			return;
		}
		resource.setResourceCenter(getResourceCenter());
		parentFolder.addToResources(resource);
		resources.put(resource.getURI(), resource);
	}

	/**
	 * Register supplied resource in parent resource
	 * 
	 * @param resource
	 * @param parentFolder
	 */
	public void registerResource(R resource, R parentResource) {
		if (resource == null) {
			logger.warning("Trying to register a null resource");
			return;
		}
		resource.setResourceCenter(getResourceCenter());
		resources.put(resource.getURI(), resource);
		parentResource.addToContents(resource);
	}

	/**
	 * Creates new folder with supplied name in supplied parent folder
	 * 
	 * @param folderName
	 * @param parentFolder
	 * @return the newly created folder
	 */
	public RepositoryFolder<R, I> createNewFolder(String folderName, RepositoryFolder<R, I> parentFolder) {
		// System.out.println("Create folder " + folderName + " parent=" + parentFolder);
		// System.out.println("parent file = " + parentFolder.getFile());
		I serializationArtefact = getResourceCenter().createDirectory(folderName, parentFolder.getSerializationArtefact());
		RepositoryFolder<R, I> newFolder = new RepositoryFolder<R, I>(serializationArtefact, parentFolder, this);

		return newFolder;
	}

	/**
	 * Creates new folder with supplied name in default root folder
	 * 
	 * @param folderName
	 * @return the newly created folder
	 */
	public RepositoryFolder<R, I> createNewFolder(String folderName) {
		return createNewFolder(folderName, getRootFolder());
	}

	/**
	 * Delete supplied folder, asserting supplied folder is empty
	 * 
	 * @param folder
	 */
	public void deleteFolder(RepositoryFolder<R, I> folder) {
		RepositoryFolder<R, I> parentFolder = getParentFolder(folder);
		if (parentFolder != null && folder.getResources().size() == 0) {
			parentFolder.removeFromChildren(folder);
			folder.delete();
		}
	}

	/**
	 * Move resource from a folder to an other one
	 * 
	 * @param resource
	 * @param fromFolder
	 * @param toFolder
	 */
	public void moveResource(R resource, RepositoryFolder<R, I> fromFolder, RepositoryFolder<R, I> toFolder) {
		if (getParentFolder(resource) == fromFolder) {
			fromFolder.removeFromResources(resource);
			toFolder.addToResources(resource);
			// TODO: reimplement this with more genericity (delegate to RC)
			if (resource.getFlexoIODelegate() instanceof FileFlexoIODelegate) {
				File fromFile = ((FileFlexoIODelegate) resource.getFlexoIODelegate()).getFile();
				File toFile = new File((File) toFolder.getSerializationArtefact(), fromFile.getName());
				try {
					FileUtils.rename(fromFile, toFile);
					((FileFlexoIODelegate) resource.getFlexoIODelegate()).setFile(toFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Return a collection storing all resources contained in this repository
	 * 
	 * @return
	 */
	public Collection<R> getAllResources() {
		return resources.values();
	}

	/**
	 * Return flag indicating if supplied resource is contained in this repository
	 * 
	 * @param resource
	 * @return
	 */
	// TODO: perf issue
	public boolean containsResource(R resource) {
		return getAllResources().contains(resource);
	}

	/**
	 * Return the repository folder where the resource is registered
	 * 
	 * @param resource
	 * @return
	 */
	public RepositoryFolder<R, I> getRepositoryFolder(R resource) {
		return getRootFolder().getRepositoryFolder(resource);
	}

	@Override
	public void update(FlexoObservable observable, DataModification dataModification) {
	}

	/**
	 * Returns the number of resources registed in this repository
	 * 
	 * @return
	 */
	public int getSize() {
		return resources.size();
	}

	/**
	 * Return an enumeration of all folders, by recursively explore the tree
	 * 
	 * @return an Enumeration of FlexoComponentFolder elements
	 */
	public Enumeration<RepositoryFolder<R, I>> allFolders() {
		Vector<RepositoryFolder<R, I>> temp = new Vector<RepositoryFolder<R, I>>();
		addFolders(temp, getRootFolder());
		return temp.elements();
	}

	/**
	 * Return number of folders
	 */
	public int allFoldersCount() {
		Vector<RepositoryFolder<R, I>> temp = new Vector<RepositoryFolder<R, I>>();
		addFolders(temp, getRootFolder());
		return temp.size();
	}

	private void addFolders(List<RepositoryFolder<R, I>> temp, RepositoryFolder<R, I> folder) {
		temp.add(folder);
		for (RepositoryFolder<R, I> currentFolder : folder.getChildren()) {
			addFolders(temp, currentFolder);
		}
	}

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

	public RepositoryFolder<R, I> getParentFolder(R resource) {
		for (Enumeration<RepositoryFolder<R, I>> e = allFolders(); e.hasMoreElements();) {
			RepositoryFolder<R, I> folder = e.nextElement();
			if (folder.getResources().contains(resource)) {
				return folder;
			}

		}
		return null;
	}

	public RepositoryFolder<R, I> getParentFolder(RepositoryFolder<R, I> aFolder) {
		for (Enumeration<RepositoryFolder<R, I>> e = allFolders(); e.hasMoreElements();) {
			RepositoryFolder<R, I> folder = e.nextElement();
			if (folder.getChildren().contains(aFolder)) {
				return folder;
			}

		}
		return null;
	}

	/**
	 * Get the parent repository folder. The object can be accessed from different ways, for instance it can be a file or an InJarResource,
	 * so the path must be computed for each kind of access.
	 * 
	 * @param element
	 * @param createWhenNonExistent
	 * @return
	 * @throws IOException
	 */
	public RepositoryFolder<R, I> getParentRepositoryFolder(I serializationArtefact, boolean createWhenNonExistent) throws IOException {
		List<String> pathTo = getResourceCenter().getPathTo(serializationArtefact);
		return getRepositoryFolder(pathTo, createWhenNonExistent);
	}

	/**
	 * Get the repository folder. The object can be accessed from different ways, for instance it can be a file or an InJarResource, so the
	 * path must be computed for each kind of access.
	 * 
	 * @param element
	 * @param createWhenNonExistent
	 * @return
	 * @throws IOException
	 */
	public RepositoryFolder<R, I> getRepositoryFolder(I serializationArtefact, boolean createWhenNonExistent) throws IOException {
		List<String> pathTo = getResourceCenter().getPathTo(serializationArtefact);
		pathTo.add(getResourceCenter().retrieveName(serializationArtefact));
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
						RepositoryFolder<R, I> newFolder = new RepositoryFolder<R, I>(serializationArtefact, returned, this);
						// returned.getPropertyChangeSupport().firePropertyChange("children", null, newFolder);
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

	/**
	 * Return class of resource this repository contains
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final Class<?> getResourceClass() {
		return org.openflexo.connie.type.TypeUtils.getBaseClass(
				TypeUtils.getTypeArguments(getClass(), ResourceRepository.class).get(ResourceRepository.class.getTypeParameters()[0]));
	}

	/**
	 * Return class of resource this repository contains
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final Class<? extends ResourceData<?>> getResourceDataClass() {
		return (Class<? extends ResourceData<?>>) TypeUtils.getTypeArguments(getResourceClass(), FlexoResource.class)
				.get(FlexoResource.class.getTypeParameters()[0]);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " with " + getAllResources().size() + " resources";
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

	public abstract String getDisplayableName();
}
