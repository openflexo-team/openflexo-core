/*
 * (c) Copyright 2013 Openflexo
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

package org.openflexo.foundation.resource;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.foundation.technologyadapter.TechnologyObject;

/**
 * Abstract implementation a factory that manages the life-cycle of a given type of {@link TechnologyAdapterResource} managed by a given
 * technology
 * 
 * @author sylvain
 *
 * @param <R>
 *            type of FlexoResource being handled by this factory, implementing both {@link TechnologyAdapterResource} and
 *            {@link PamelaResource}
 * @param <RD>
 *            type of {@link ResourceData} managed by resources (contents of resources)
 * @param <TA>
 *            type of {@link TechnologyAdapter}
 */
public interface ITechnologySpecificFlexoResourceFactory<R extends TechnologyAdapterResource<RD, TA>, RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter<TA>>
		extends IFlexoResourceFactory<R, RD> {

	public FlexoResourceType getResourceType();

	public Class<TA> getTechnologyAdapterClass();

	public TA getTechnologyAdapter(FlexoServiceManager sm);

	public TechnologyContextManager<TA> getTechnologyContextManager(FlexoServiceManager sm);

	@Override
	public Class<R> getResourceClass();

	@Override
	public Class<RD> getResourceDataClass();

}
