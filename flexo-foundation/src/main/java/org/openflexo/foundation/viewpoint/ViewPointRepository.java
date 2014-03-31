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
package org.openflexo.foundation.viewpoint;

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.resource.FileResourceRepository;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.ModelRepository;
import org.openflexo.foundation.view.ViewRepository;
import org.openflexo.foundation.viewpoint.rm.ViewPointResource;

/**
 * A {@link ViewRepository} contains some resources storing viewpoint, and contained in a given {@link FlexoResourceCenter}
 * 
 * @author sylvain
 * 
 */
public class ViewPointRepository extends FileResourceRepository<ViewPointResource> {

	private static final Logger logger = Logger.getLogger(ModelRepository.class.getPackage().getName());

	private static final String DEFAULT_BASE_URI = "http://www.openflexo.org/ViewPoints";

	private FlexoResourceCenter resourceCenter;
	private final FlexoServiceManager serviceManager;

	public ViewPointRepository(FlexoResourceCenter resourceCenter, FlexoServiceManager serviceManager) {
		super(resourceCenter, resourceCenter instanceof FileSystemBasedResourceCenter ? ((FileSystemBasedResourceCenter) resourceCenter)
				.getRootDirectory() : null);
		this.resourceCenter = resourceCenter;
		this.serviceManager = serviceManager;
		getRootFolder().setName(resourceCenter.getName());
	}

	public FlexoResourceCenter getResourceCenter() {
		return resourceCenter;
	}

	public void setResourceCenter(FlexoResourceCenter resourceCenter) {
		this.resourceCenter = resourceCenter;
	}

	public ViewPointLibrary getViewPointLibrary() {
		return serviceManager.getViewPointLibrary();
	}

	@Override
	public String getDefaultBaseURI() {
		return DEFAULT_BASE_URI;
	}
}
