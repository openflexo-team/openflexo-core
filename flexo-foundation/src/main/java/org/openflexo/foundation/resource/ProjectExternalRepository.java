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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.KVCFlexoObject;

/**
 * Represents a directory outside a project logically bound to some data inside the project<br>
 * 
 * This allows to easyly "carry" projects and to connect them to local repository (the most common use stands for code/doc generation, when
 * generated data is not likely in the project directory)
 * 
 * @author sguerin
 * 
 */
public class ProjectExternalRepository<I> extends KVCFlexoObject {
	private final FlexoProject<I> _project;
	private String _identifier;
	private File _directory;
	private Map<String, String> directoriesForUser = new TreeMap<>();

	private static final String getUserName() {
		return System.getProperty("user.name");
	}

	public ProjectExternalRepository(FlexoProject<I> aProject, String identifier) {
		this(aProject);
		setIdentifier(identifier);
	}

	public ProjectExternalRepository(FlexoProject<I> aProject, String identifier, File directory) {
		this(aProject, identifier);
		setDirectory(directory);
	}

	public ProjectExternalRepository(FlexoProject<I> aProject) {
		_project = aProject;
	}

	public String getName() {
		return getIdentifier();
	}

	public String getIdentifier() {
		return _identifier;
	}

	public void setIdentifier(String identifier) {
		_identifier = identifier;
	}

	public File getDirectory() {
		if (_directory == null) {
			String s = directoriesForUser.get(getUserName());
			if (s != null) {
				_directory = new File(s);
			}
			_isConnected = _directory != null && _directory.exists();
		}
		return _directory;
	}

	public void setDirectory(File directory) {
		_directory = directory;
		if (_directory != null) {
			directoriesForUser.put(getUserName(), _directory.getAbsolutePath());
		}
		else {
			directoriesForUser.remove(getUserName());
		}
		_isConnected = _directory != null && _directory.exists();
		// getProject().clearCachedFiles();
		// getProject().notifyResourceChanged(null);
	}

	public FlexoProject<I> getProject() {
		return _project;
	}

	public String getSerializationIdentifier() {
		return getProject().getUserIdentifier() + "_" + getIdentifier();
	}

	@Override
	public String toString() {
		return "ProjectExternalRepository:" + getName() + "[" + (getDirectory() != null ? getDirectory().getAbsolutePath() : "null") + "]";
	}

	private boolean _isConnected = false;
	private boolean _isNormallyConnected = false;

	public boolean isConnected() {
		return _isConnected;
	}

	public boolean shouldBeConnected() {
		return _isNormallyConnected;
	}

	public boolean _getIsConnected() {
		return isConnected();
	}

	public void _setIsConnected(boolean aBoolean) {
		_isNormallyConnected = aBoolean;
	}

	// TODO: reimplement this
	public List<FlexoResource<?>> getRelatedResources() {
		/*List<FlexoFileResource<?>> returned = new Vector<FlexoFileResource<?>>();
		for (FlexoResource<?> resource : getProject().getServiceManager().getResourceManager().getRegisteredResources()) {
			if (resource instanceof FlexoFileResource) {
				FlexoFileResource fileResource = (FlexoFileResource)resource;
				if (FlexoProjectFile.isContainedInProjectDeclaredExternalRepositories(fileResource.getFile(), getProject())) {
					
				}
				
				FlexoProjectFile pFile = ((FlexoFileResource<?>) resource).getResourceFile();
				if (pFile.getExternalRepository() == this) {
					returned.add((FlexoFileResource<? extends FlexoResourceData>) resource);
				}
			}
		}
		return returned;*/
		return null;
	}

	// TODO: reimplement this
	public List<FlexoResource<?>> getRelatedActiveResources() {
		/*List<FlexoFileResource<? extends FlexoResourceData>> returned = new Vector<FlexoFileResource<? extends FlexoResourceData>>();
		for (FlexoFileResource<? extends FlexoResourceData> resource : getRelatedResources()) {
			if (resource.isActive()) {
				returned.add(resource);
			}
		}
		return returned;*/
		return null;
	}

	public Map<String, String> getDirectoriesForUser() {
		return directoriesForUser;
	}

	public void setDirectoriesForUser(Map<String, String> directoriesForUser) {
		this.directoriesForUser = new TreeMap<>(directoriesForUser);
	}

	public void setDirectoriesForUserForKey(String directory, String user) {
		directoriesForUser.put(user, directory);
	}

	public void removeDirectoriesForUserWithKey(String key) {
		this.directoriesForUser.remove(key);
	}

}
