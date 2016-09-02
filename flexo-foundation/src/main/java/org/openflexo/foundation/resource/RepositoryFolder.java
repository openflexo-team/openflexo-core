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
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.DefaultFlexoObject;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents a folder, as an organization item inside a {@link ResourceRepository}
 * 
 * @param <R>
 *            type of resources being stored in this {@link ResourceRepository}
 * @param <I>
 *            serialization artefact type
 *
 * @author sylvain
 * 
 */
public class RepositoryFolder<R extends FlexoResource<?>, I> extends DefaultFlexoObject {

	private static final Logger logger = Logger.getLogger(RepositoryFolder.class.getPackage().getName());

	private I serializationArtefact;
	private final ResourceRepository<R, I> resourceRepository;
	private String name;
	private String fullQualifiedPath;
	private String repositoryContext = null;
	private RepositoryFolder<R, I> parent;
	private final ArrayList<RepositoryFolder<R, I>> children;
	private final ArrayList<R> resources;

	public static final String NAME_KEY = "name";
	public static final String FULL_QUALIFIED_PATH_KEY = "fullQualifiedPath";
	public static final String PARENT_KEY = "parent";
	public static final String CHILDREN_KEY = "children";
	public static final String RESOURCES_KEY = "resources";

	public RepositoryFolder(I serializationArtefact, RepositoryFolder<R, I> parentFolder, ResourceRepository<R, I> resourceRepository) {
		this.serializationArtefact = serializationArtefact;
		this.resourceRepository = resourceRepository;
		this.name = resourceRepository.getResourceCenter().retrieveName(serializationArtefact);
		this.parent = parentFolder;
		children = new ArrayList<RepositoryFolder<R, I>>();
		resources = new ArrayList<R>();
		if (parentFolder != null) {
			parentFolder.addToChildren(this);
		}
	}

	public I getSerializationArtefact() {
		return serializationArtefact;
	}

	public String getName() {
		if (getFile() != null) {
			return getFile().getName();
		}
		return name;
	}

	public void setName(String name) {
		File oldFile = getFile();
		String oldName = this.name;
		if (oldName != null && !oldName.equals(name)) {
			this.name = name;
			if (getFile() != null) {
				// This is a File, i try to rename it
				// TODO: test this (quick and dirty done for Wei)
				try {
					FileUtils.rename(oldFile, getFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			getPropertyChangeSupport().firePropertyChange(NAME_KEY, oldName, name);
		}

	}

	public String getPathRelativeToRepository() {
		if (getParentFolder() != null) {
			return getParentFolder().getPathRelativeToRepository() + "/" + getName();
		}
		return getName();
	}

	public String getFullQualifiedPath() {
		if (getFile() != null) {
			return getFile().getAbsolutePath();
		}
		return fullQualifiedPath;
	}

	public void setFullQualifiedPath(String fullQualifiedPath) {
		String old = this.fullQualifiedPath;
		this.fullQualifiedPath = fullQualifiedPath;
		getPropertyChangeSupport().firePropertyChange(FULL_QUALIFIED_PATH_KEY, old, fullQualifiedPath);
		if (name == null) {
			if (fullQualifiedPath.lastIndexOf(File.separator) > -1) {
				name = fullQualifiedPath.substring(fullQualifiedPath.lastIndexOf(File.separator));
			}
			else {
				name = fullQualifiedPath;
			}
		}
	}

	public List<RepositoryFolder<R, I>> getChildren() {
		return children;
	}

	public void addToChildren(RepositoryFolder<R, I> aFolder) {
		children.add(aFolder);
		aFolder.parent = this;
		setChanged();
		notifyObservers(new RepositoryFolderAdded(this, aFolder));
		getPropertyChangeSupport().firePropertyChange(CHILDREN_KEY, null, aFolder);
	}

	public void removeFromChildren(RepositoryFolder<R, I> aFolder) {
		children.remove(aFolder);
		aFolder.parent = null;
		setChanged();
		notifyObservers(new RepositoryFolderRemoved(this, aFolder));
		getPropertyChangeSupport().firePropertyChange(CHILDREN_KEY, aFolder, null);
	}

	public RepositoryFolder<R, I> getParentFolder() {
		return parent;
	}

	public boolean isRootFolder() {
		return getParentFolder() == null;
	}

	public RepositoryFolder<R, I> getFolderNamed(String newFolderName) {
		for (RepositoryFolder<R, I> f : children) {
			if (f.getName().equals(newFolderName)) {
				return f;
			}
		}
		return null;
	}

	public List<R> getResources() {
		return resources;
	}

	public void addToResources(R resource) {
		/*if (getResourceRepository() instanceof FlexoProject) {
			System.out.println("Je tiens mon truc !!!!!!");
			Thread.dumpStack();
			System.exit(-1);
		}*/
		if (resources.contains(resource)) {
			// logger.warning("Resource already present in " + this + " : " + resource + ". Ignore it.");
			// Thread.dumpStack();
			return;
		}
		resources.add(resource);
		setChanged();
		notifyObservers(new ResourceRegistered(resource, this));
		getPropertyChangeSupport().firePropertyChange(RESOURCES_KEY, null, resource);
	}

	public void removeFromResources(R resource) {
		resources.remove(resource);
		setChanged();
		notifyObservers(new ResourceUnregistered(resource, this));
		deleteObservers();
		getPropertyChangeSupport().firePropertyChange(RESOURCES_KEY, resource, null);
	}

	public ResourceRepository<R, I> getResourceRepository() {
		return resourceRepository;
	}

	// TODO : might be an issue here, while create a File systematically when it is not a FileResourceRepository?
	// Remove this method
	@Deprecated
	public File getFile() {
		if (isRootFolder()) {
			if (getResourceRepository() instanceof FileResourceRepository) {
				return ((FileResourceRepository) getResourceRepository()).getDirectory();
			}
			return null;
		}
		else {
			return new File(getParentFolder().getFile(), name);
		}
	}

	public R getResourceWithName(String resourceName) {
		for (R resource : getResources()) {
			if (resource.getName().equals(resourceName)) {
				return resource;
			}
		}
		return null;
	}

	public boolean isValidResourceName(String resourceName) {
		return getResourceWithName(resourceName) == null;
	}

	public boolean isFatherOf(RepositoryFolder<R, I> folder) {
		RepositoryFolder<R, I> f = folder.getParentFolder();
		while (f != null) {
			if (f.equals(this)) {
				return true;
			}
			f = f.getParentFolder();
		}
		return false;
	}

	@Override
	public boolean delete(Object... context) {
		if (getFile().exists()) {
			getFile().delete();
		}
		super.delete(context);
		return true;
	}

	@Override
	public String toString() {
		return "RepositoryFolder " + getName() + (!isRootFolder() ? " in " + getParentFolder().getName() : " root") + " of "
				+ getResourceRepository();
	}

	public int getIndex() {
		if (getParentFolder() != null) {
			return getParentFolder().getChildren().indexOf(this);
		}
		return -1;
	}

	public String getRepositoryContext() {
		return repositoryContext;
	}

	public void setRepositoryContext(String repositoryContext) {
		this.repositoryContext = repositoryContext;
	}

	public String getDisplayableName() {
		return (StringUtils.isNotEmpty(getRepositoryContext()) ? getRepositoryContext() + " " : "") + getName();
	}

	/**
	 * Return the repository folder where the resource is registered
	 * 
	 * @param resource
	 * @return
	 */
	public RepositoryFolder<R, I> getRepositoryFolder(R resource) {
		if (getResources().contains(resource)) {
			return this;
		}
		if (getChildren() != null && getChildren().size() > 0) {
			for (RepositoryFolder<R, I> child : getChildren()) {
				RepositoryFolder<R, I> returned = child.getRepositoryFolder(resource);
				if (returned != null) {
					return returned;
				}
			}
		}
		return null;
	}

}
