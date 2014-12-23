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

import java.util.HashMap;
import java.util.Map;

import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;

public class FMLTechnologyContextManager extends TechnologyContextManager<FMLTechnologyAdapter> {

	protected Map<String, ViewPointResource> viewPoints = new HashMap<String, ViewPointResource>();

	public FMLTechnologyContextManager(FMLTechnologyAdapter adapter, FlexoResourceCenterService resourceCenterService) {
		super(adapter, resourceCenterService);
	}

	public ViewPointResource getViewPointResource(String uri) {
		return viewPoints.get(uri);
	}

	public void registerViewPoint(ViewPointResource viewPointResource) {
		registerResource(viewPointResource);
		viewPoints.put(viewPointResource.getURI(), viewPointResource);
	}

}
