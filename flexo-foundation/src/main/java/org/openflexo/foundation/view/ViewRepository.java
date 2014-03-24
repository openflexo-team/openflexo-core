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
package org.openflexo.foundation.view;

import java.io.File;

import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.ModelRepository;
import org.openflexo.foundation.view.rm.ViewResource;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.VirtualModelTechnologyAdapter;

/**
 * A repository storing {@link ViewResource}
 * 
 * @author sylvain
 * 
 */
public class ViewRepository extends ModelRepository<ViewResource, View, ViewPoint, VirtualModelTechnologyAdapter> {

	public ViewRepository(VirtualModelTechnologyAdapter adapter, FlexoResourceCenter<?> resourceCenter) {
		super(adapter, resourceCenter);
	}

	public ViewRepository(VirtualModelTechnologyAdapter adapter, FlexoResourceCenter<?> resourceCenter, File directory) {
		super(adapter, resourceCenter, directory);
	}

	private static final String DEFAULT_BASE_URI = "http://www.openflexo.org/FML/Views";

	@Override
	public String getDefaultBaseURI() {
		return DEFAULT_BASE_URI;
	}

}
