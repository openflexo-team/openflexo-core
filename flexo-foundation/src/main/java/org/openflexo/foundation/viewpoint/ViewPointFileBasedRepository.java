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
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.ModelRepository;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterFileResourceRepository;
import org.openflexo.foundation.viewpoint.rm.ViewPointResource;


public class ViewPointFileBasedRepository extends TechnologyAdapterFileResourceRepository<ViewPointResource, VirtualModelTechnologyAdapter, ViewPoint> implements ViewPointRepository<ViewPointResource>{

	private static final Logger logger = Logger.getLogger(ModelRepository.class.getPackage().getName());

	private static final String DEFAULT_BASE_URI = "http://www.openflexo.org/ViewPoints";
	
	private FlexoResourceCenter resourceCenter;
	private FlexoServiceManager serviceManager;

	public ViewPointFileBasedRepository(VirtualModelTechnologyAdapter adapter, FlexoResourceCenter<?> resourceCenter) {
		super(adapter, resourceCenter);
		this.serviceManager = adapter.getServiceManager();
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