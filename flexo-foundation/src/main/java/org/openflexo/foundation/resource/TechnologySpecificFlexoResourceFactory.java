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

import java.util.logging.Logger;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.model.exceptions.ModelDefinitionException;

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
public abstract class TechnologySpecificFlexoResourceFactory<R extends TechnologyAdapterResource<RD, TA>, RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter>
		extends FlexoResourceFactory<R, RD> implements ITechnologySpecificFlexoResourceFactory<R, RD, TA> {

	private static final Logger logger = Logger.getLogger(TechnologySpecificFlexoResourceFactory.class.getPackage().getName());

	private final FlexoResourceType resourceType;

	/**
	 * Generic constructor
	 * 
	 * @param resourceClass
	 * @throws ModelDefinitionException
	 */
	protected TechnologySpecificFlexoResourceFactory(Class<R> resourceClass) throws ModelDefinitionException {
		super(resourceClass);
		resourceType = new FlexoResourceType(this);
	}

	/**
	 * Generic constructor
	 * 
	 * @param resourceClass
	 * @throws ModelDefinitionException
	 */
	protected TechnologySpecificFlexoResourceFactory(Class<R> resourceClass, Class<?>... requiredClasses) throws ModelDefinitionException {
		super(resourceClass);
		resourceType = new FlexoResourceType(this);
	}

	@Override
	public FlexoResourceType getResourceType() {
		return resourceType;
	}

	@Override
	public Class<TA> getTechnologyAdapterClass() {
		return (Class<TA>) (TypeUtils.getBaseClass(TypeUtils.getTypeArgument(getResourceClass(), TechnologyAdapterResource.class, 1)));
	}

	@Override
	public TA getTechnologyAdapter(FlexoServiceManager sm) {
		return sm.getTechnologyAdapterService().getTechnologyAdapter(getTechnologyAdapterClass());
	}

	@Override
	public TechnologyContextManager<TA> getTechnologyContextManager(FlexoServiceManager sm) {
		TA technologyAdapter = sm.getTechnologyAdapterService().getTechnologyAdapter(getTechnologyAdapterClass());
		return (TechnologyContextManager<TA>) technologyAdapter.getTechnologyContextManager();
	}

	/**
	 * Called to register a resource in a given {@link FlexoResourceCenter} and a given technology
	 * 
	 * @param resource
	 * @param resourceCenter
	 * @param technologyContextManager
	 * @return
	 */
	@Override
	protected <I> R registerResource(R resource, FlexoResourceCenter<I> resourceCenter) {

		R returned = super.registerResource(resource, resourceCenter);

		TechnologyContextManager<TA> technologyContextManager = getTechnologyContextManager(resourceCenter.getServiceManager());

		// Register the resource in the global repository of technology adapter
		if (resourceCenter != null) {
			registerResourceInResourceRepository(resource,
					technologyContextManager.getTechnologyAdapter().getGlobalRepository(resourceCenter));
		}
		resource.setTechnologyAdapter(technologyContextManager.getTechnologyAdapter());
		resource.setTechnologyContextManager(technologyContextManager);
		technologyContextManager.registerResource(resource);

		return returned;
	}

}
