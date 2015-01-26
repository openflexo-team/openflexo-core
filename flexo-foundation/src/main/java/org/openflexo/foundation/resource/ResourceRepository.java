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
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.openflexo.foundation.DataFlexoObserver;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.DefaultFlexoObject;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.rm.ClasspathResourceLocatorImpl;
import org.openflexo.rm.InJarResourceImpl;
import org.openflexo.toolbox.FileUtils;

/**
 * A {@link ResourceRepository} stores all resources of a particular type.<br>
 * Resources are organized with a folder hierarchy inside a repository
 * 
 * @author sylvain
 * 
 * @param <R>
 * @param <TA>
 */
public abstract class ResourceRepository<R extends FlexoResource<?>> extends DefaultFlexoObject implements DataFlexoObserver {

	private static final Logger logger = Logger.getLogger(ResourceRepository.class.getPackage().getName());

	/**
	 * Hashtable where resources are stored, used key is the URI of the resource
	 */
	protected HashMap<String, R> resources;

	private final RepositoryFolder<R> rootFolder;

	/** Stores the object which is the "owner" of this repository */
	private Object owner;

	public RepositoryFolder<R> getRootFolder() {
		return rootFolder;
	}

	/**
	 * Creates a new {@link ResourceRepository}
	 */
	public ResourceRepository(Object owner) {
		this.owner = owner;
		resources = new HashMap<String, R>();
		rootFolder = new RepositoryFolder<R>("root", null, this);
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
	 * Stores the object which is the "owner" of this repository. The owner has the responsability of this repository.
	 */
	public Object getOwner() {
		return owner;
	}

	/**
	 * Sets the "owner" of this repository
	 * 
	 * @param owner
	 */
	public void setOwner(Object owner) {
		this.owner = owner;
	}

	/**
	 * Return resource with the supplied URI, if this resource was already declared
	 * 
	 * @param resourceURI
	 * @return
	 */
	public R getResource(String resourceURI) {
		return resources.get(resourceURI);
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
		RepositoryFolder<R> parentFolder = getParentFolder(flexoResource);
		parentFolder.removeFromResources(flexoResource);
		resources.remove(flexoResource.getURI());

	}

	/**
	 * Register supplied resource in supplied folder
	 * 
	 * @param resource
	 * @param parentFolder
	 */
	public void registerResource(R resource, RepositoryFolder<R> parentFolder) {
		if (resource == null) {
			logger.warning("Trying to register a null resource");
			return;
		}
		resources.put(resource.getURI(), resource);
		parentFolder.addToResources(resource);
	}

	/**
	 * Creates new folder with supplied name in supplied parent folder
	 * 
	 * @param folderName
	 * @param parentFolder
	 * @return the newly created folder
	 */
	public RepositoryFolder<R> createNewFolder(String folderName, RepositoryFolder<R> parentFolder) {
		// System.out.println("Create folder " + folderName + " parent=" + parentFolder);
		// System.out.println("parent file = " + parentFolder.getFile());
		RepositoryFolder<R> newFolder = new RepositoryFolder<R>(folderName, parentFolder, this);
		newFolder.getFile().mkdirs();

		return newFolder;
	}

	/**
	 * Creates new folder with supplied name in default root folder
	 * 
	 * @param folderName
	 * @return the newly created folder
	 */
	public RepositoryFolder<R> createNewFolder(String folderName) {
		return createNewFolder(folderName, getRootFolder());
	}

	/**
	 * Delete supplied folder, asserting supplied folder is empty
	 * 
	 * @param folder
	 */
	public void deleteFolder(RepositoryFolder<R> folder) {
		RepositoryFolder<R> parentFolder = getParentFolder(folder);
		if (parentFolder != null && folder.getResources().size() == 0) {
			if (folder.getFile().exists()) {
				folder.getFile().delete();
			}
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
	public void moveResource(R resource, RepositoryFolder<R> fromFolder, RepositoryFolder<R> toFolder) {
		if (getParentFolder(resource) == fromFolder) {
			fromFolder.removeFromResources(resource);
			toFolder.addToResources(resource);
			if (resource.getFlexoIODelegate() instanceof FileFlexoIODelegate) {
				File fromFile = ((FileFlexoIODelegate) resource.getFlexoIODelegate()).getFile();
				File toFile = new File(toFolder.getFile(), fromFile.getName());
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
	public Enumeration<RepositoryFolder<R>> allFolders() {
		Vector<RepositoryFolder<R>> temp = new Vector<RepositoryFolder<R>>();
		addFolders(temp, getRootFolder());
		return temp.elements();
	}

	/**
	 * Return number of folders
	 */
	public int allFoldersCount() {
		Vector<RepositoryFolder<R>> temp = new Vector<RepositoryFolder<R>>();
		addFolders(temp, getRootFolder());
		return temp.size();
	}

	private void addFolders(List<RepositoryFolder<R>> temp, RepositoryFolder<R> folder) {
		temp.add(folder);
		for (RepositoryFolder<R> currentFolder : folder.getChildren()) {
			addFolders(temp, currentFolder);
		}
	}

	public RepositoryFolder<R> getFolderWithName(String folderName) {
		for (Enumeration<RepositoryFolder<R>> e = allFolders(); e.hasMoreElements();) {
			RepositoryFolder<R> folder = e.nextElement();

			if (folder.getName().equals(folderName)) {
				return folder;
			}

		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Could not find folder named " + folderName);
		}
		return null;
	}

	public RepositoryFolder<R> getParentFolder(R resource) {
		for (Enumeration<RepositoryFolder<R>> e = allFolders(); e.hasMoreElements();) {
			RepositoryFolder<R> folder = e.nextElement();
			if (folder.getResources().contains(resource)) {
				return folder;
			}

		}
		return null;
	}

	public RepositoryFolder<R> getParentFolder(RepositoryFolder<R> aFolder) {
		for (Enumeration<RepositoryFolder<R>> e = allFolders(); e.hasMoreElements();) {
			RepositoryFolder<R> folder = e.nextElement();
			if (folder.getChildren().contains(aFolder)) {
				return folder;
			}

		}
		return null;
	}

	/**
	 * Get the respository folder. The object can be accessed from different ways, for instance it can be a file
	 * or an InJarResource, so the path must be computed for each kind of access.
	 * @param element
	 * @param createWhenNonExistent
	 * @return
	 * @throws IOException
	 */
	public RepositoryFolder<R> getRepositoryFolder(Object element, boolean createWhenNonExistent) throws IOException {
		List<String> pathTo = null;
		if(element instanceof File){
			pathTo = getPathTo((File)element);
		}else if(element instanceof InJarResourceImpl){
			pathTo = getPathTo((InJarResourceImpl) element);
		}
		return getRepositoryFolder(pathTo, createWhenNonExistent);
	}
	
	/**
	 * Get the set of path in the case of File
	 * @param aFile
	 * @return
	 * @throws IOException
	 */
	private List<String> getPathTo(File aFile) throws IOException{
		if (FileUtils.directoryContainsFile(getRootFolder().getFile(), aFile,true)) {
			List<String> pathTo = new ArrayList<String>();
			File f = aFile.getParentFile().getCanonicalFile();
			while (f != null && !f.equals(getRootFolder().getFile().getCanonicalFile())) {
				pathTo.add(0, f.getName());
				f = f.getParentFile();
			}
			return pathTo;
		}else{
			return null;
		}
	}
	
	/**
	 * Get the set of path in the case of InJarResource
	 * @param resource
	 * @return
	 */
	private List<String> getPathTo(InJarResourceImpl resource){
		if(!getRootFolder().getChildren().contains(resource)){
			List<String> pathTo = new ArrayList<String>();
			StringTokenizer string = new StringTokenizer(resource.getURI().toString(), 
					Character.toString(ClasspathResourceLocatorImpl.PATH_SEP.toCharArray()[0]));
			while(string.hasMoreTokens()){
				pathTo.add(string.nextToken());
			}
			return pathTo;
		}else{
			return null;
		}
	}
	
	/**
	 * Get the repository folder from a set of path
	 * @param pathTo
	 * @param createWhenNonExistent
	 * @return
	 * @throws IOException
	 */
	public RepositoryFolder<R> getRepositoryFolder(List<String> pathTo, boolean createWhenNonExistent) throws IOException {
		RepositoryFolder<R> returned = getRootFolder();
		if(pathTo!=null){
			for (String pathElement : pathTo) {
				RepositoryFolder<R> currentFolder = returned.getFolderNamed(pathElement);
				if (currentFolder == null) {
					if (createWhenNonExistent) {
						RepositoryFolder<R> newFolder = new RepositoryFolder<R>(pathElement, returned, this);
						currentFolder = newFolder;
					} else {
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
	public final Class<R> getResourceClass() {
		return (Class<R>) TypeUtils.getTypeArguments(getClass(), ResourceRepository.class).get(
				ResourceRepository.class.getTypeParameters()[0]);
	}

	/**
	 * Return class of resource this repository contains
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final Class<? extends ResourceData<?>> getResourceDataClass() {
		return (Class<? extends ResourceData<?>>) TypeUtils.getTypeArguments(getResourceClass(), FlexoResource.class).get(
				FlexoResource.class.getTypeParameters()[0]);
	}

}
