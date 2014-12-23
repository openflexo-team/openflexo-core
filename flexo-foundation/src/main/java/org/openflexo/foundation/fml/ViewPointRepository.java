/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.fml;

import java.io.IOException;
import java.util.Collection;

import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.ViewRepository;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;

/**
 * A {@link ViewRepository} contains some resources storing viewpoint, and contained in a given {@link FlexoResourceCenter}
 * 
 * @author sylvain
 * 
 */
public interface ViewPointRepository<VP extends ViewPointResource> {

	public FlexoResourceCenter getResourceCenter(); 

	public void setResourceCenter(FlexoResourceCenter resourceCenter);
	
	public Collection<ViewPointResource> getAllResources();
	
	public void registerResource(ViewPointResource flexoResource);
	
	public void registerResource(ViewPointResource resource, RepositoryFolder<ViewPointResource> parentFolder);

	public void unregisterResource(ViewPointResource flexoResource);
	
	public ViewPointLibrary getViewPointLibrary();
	
	public RepositoryFolder<VP> getRootFolder();
	
	public RepositoryFolder<VP> getRepositoryFolder(Object element, boolean createWhenNonExistent) throws IOException;

}
