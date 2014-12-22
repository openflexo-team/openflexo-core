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

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.JarResourceCenter;
import org.openflexo.foundation.resource.ResourceRepository;

public class ViewPointJarBasedRepository extends ResourceRepository<ViewPointResource> implements ViewPointRepository<ViewPointResource>{
	private static final String DEFAULT_BASE_URI = "http://www.openflexo.org/ViewPoints";

	private FlexoResourceCenter resourceCenter;
	private final FlexoServiceManager serviceManager;
	
	public ViewPointJarBasedRepository(FlexoResourceCenter resourceCenter, FlexoServiceManager serviceManager) {
		super(resourceCenter);
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
