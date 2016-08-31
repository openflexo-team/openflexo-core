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
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

/**
 * Abstract implementation a factory that manages the creation of a given type of {@link FlexoResource} and a given
 * {@link TechnologyAdapter}, in PAMELA context
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
public abstract class PamelaResourceFactory<R extends TechnologyAdapterResource<RD, TA> & PamelaResource<RD, F>, RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter, F extends ModelFactory & PamelaResourceModelFactory>
		extends FlexoResourceFactory<R, RD, TA> {

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

	@Override
	protected <I> R initResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<TA> technologyContextManager, String uri) throws ModelDefinitionException {
		R returned = super.initResource(serializationArtefact, resourceCenter, technologyContextManager, uri);
		returned.setFactory(makeResourceDataFactory(returned, technologyContextManager));
		return returned;
	}

	/**
	 * Build a new factory managing contents of a {@link PamelaResource}
	 * 
	 * @param resource
	 * @param technologyContextManager
	 * @return
	 */
	public abstract F makeResourceDataFactory(R resource, TechnologyContextManager<TA> technologyContextManager)
			throws ModelDefinitionException;

}
