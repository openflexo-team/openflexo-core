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

import org.openflexo.foundation.PamelaResourceModelFactory;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * Abstract implementation a factory that manages the creation of a given type of {@link FlexoResource} in PAMELA context
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
 * @param <F>
 *            type of {@link PamelaResourceModelFactory} managing contents of resources
 */
public abstract class PamelaResourceFactory<R extends FlexoResource<RD>, RD extends ResourceData<RD>, F extends PamelaModelFactory & PamelaResourceModelFactory>
		extends FlexoResourceFactory<R, RD> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PamelaResourceFactory.class.getPackage().getName());

	/**
	 * Generic constructor
	 * 
	 * @param resourceClass
	 * @throws ModelDefinitionException
	 */
	protected PamelaResourceFactory(Class<R> resourceClass) throws ModelDefinitionException {
		super(resourceClass);
	}

	/*@Override
	protected <I> R initResourceForRetrieving(I serializationArtefact, FlexoResourceCenter<I> resourceCenter)
			throws ModelDefinitionException, IOException {
		R returned = super.initResourceForRetrieving(serializationArtefact, resourceCenter);
		// TechnologyContextManager<TA> technologyContextManager = getTechnologyContextManager(resourceCenter.getServiceManager());
		// returned.setFactory(makeResourceDataFactory(returned, technologyContextManager));
		return returned;
	}
	
	@Override
	protected <I> R initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter, String name, String uri)
			throws ModelDefinitionException {
		R returned = super.initResourceForCreation(serializationArtefact, resourceCenter, name, uri);
		// returned.setFactory(makeResourceDataFactory(returned, technologyContextManager));
		return returned;
	}*/

}
