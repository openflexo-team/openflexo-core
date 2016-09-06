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

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

/**
 * Abstract implementation a factory that manages the creation of a given type of {@link FlexoResource} and a given
 * {@link TechnologyAdapter}
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
public abstract class FlexoResourceFactory<R extends TechnologyAdapterResource<RD, TA>, RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter>
		extends ModelFactory {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FlexoResourceFactory.class.getPackage().getName());

	private final Class<R> resourceClass;

	/**
	 * Generic constructor
	 * 
	 * @param resourceClass
	 * @throws ModelDefinitionException
	 */
	protected FlexoResourceFactory(Class<R> resourceClass) throws ModelDefinitionException {
		super(ModelContextLibrary.getCompoundModelContext(resourceClass, FlexoIODelegate.class));
		this.resourceClass = resourceClass;
	}

	/**
	 * Return class of {@link FlexoResource} beeing managed by this factory
	 * 
	 * @return
	 */
	public Class<R> getResourceClass() {
		return resourceClass;
	}

	/**
	 * Make a new empty resource for a given artefact, a resource center and a technology context manager.<br>
	 * The newly created resource is set with empty contents as it is computed from {@link #makeEmptyResourceData()}
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @param technologyContextManager
	 * @param createEmptyContents
	 *            when set to true, initiate contents of resource with technology specific empty contents
	 * @return
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 */
	public <I> R makeResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<TA> technologyContextManager, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException {
		return makeResource(serializationArtefact, resourceCenter, technologyContextManager, null, createEmptyContents);
	}

	/**
	 * Make a new empty resource for a given artefact, a resource center and a technology context manager.<br>
	 * The newly created resource is set with empty contents as it is computed from {@link #makeEmptyResourceData()}<br>
	 * URI is explicitely given to the new resource
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @param technologyContextManager
	 * @param uri
	 * @return
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 */
	public <I> R makeResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<TA> technologyContextManager, String uri, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException {
		R returned = initResourceForCreation(serializationArtefact, resourceCenter, technologyContextManager, uri);
		registerResource(returned, resourceCenter, technologyContextManager);

		if (createEmptyContents) {
			createEmptyContents(returned);
			returned.save(null);
		}

		return returned;
	}

	protected RD createEmptyContents(R resource) {
		RD resourceData = makeEmptyResourceData(resource);
		resourceData.setResource(resource);
		resource.setResourceData(resourceData);
		resource.setModified(true);
		return resourceData;
	}

	protected <I> R initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<TA> technologyContextManager, String uri) throws ModelDefinitionException {
		R returned = newInstance(resourceClass);
		returned.setResourceCenter(resourceCenter);
		returned.initName(resourceCenter.retrieveName(serializationArtefact));
		returned.setURI(uri);
		returned.setFlexoIODelegate(makeFlexoIODelegate(serializationArtefact, resourceCenter));
		return returned;
	}

	/**
	 * Retrieve resource for a given artefact, a resource center and a technology context manager.<br>
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @param technologyContextManager
	 * @return
	 */
	public <I> R retrieveResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<TA> technologyContextManager) throws ModelDefinitionException {
		R returned = initResourceForRetrieving(serializationArtefact, resourceCenter, technologyContextManager);
		registerResource(returned, resourceCenter, technologyContextManager);
		return returned;
	}

	protected <I> R initResourceForRetrieving(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<TA> technologyContextManager) throws ModelDefinitionException {
		R returned = newInstance(resourceClass);
		returned.setResourceCenter(resourceCenter);
		returned.initName(resourceCenter.retrieveName(serializationArtefact));

		returned.setFlexoIODelegate(makeFlexoIODelegate(serializationArtefact, resourceCenter));

		return returned;
	}

	protected <I> FlexoIODelegate<I> makeFlexoIODelegate(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		return resourceCenter.makeFlexoIODelegate(serializationArtefact, this);
	}

	/**
	 * Called to register a resource in a given {@link FlexoResourceCenter} and a given technology
	 * 
	 * @param resource
	 * @param resourceCenter
	 * @param technologyContextManager
	 * @return
	 */
	protected <I> R registerResource(R resource, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<TA> technologyContextManager) {
		resource.setResourceCenter(resourceCenter);
		resource.setServiceManager(technologyContextManager.getServiceManager());
		resource.setTechnologyAdapter(technologyContextManager.getTechnologyAdapter());
		resource.setTechnologyContextManager(technologyContextManager);
		technologyContextManager.registerResource(resource);

		// Register the resource in the global repository of technology adapter
		registerResourceInResourceRepository(resource, technologyContextManager.getTechnologyAdapter().getGlobalRepository(resourceCenter));

		// Also register the resource in the ResourceCenter seen as a ResourceRepository
		if (resourceCenter instanceof ResourceRepository) {
			registerResourceInResourceRepository(resource, (ResourceRepository) resourceCenter);
		}

		return resource;
	}

	/**
	 * Called to unregister a resource from a given {@link FlexoResourceCenter} and a given technology
	 * 
	 * @param resource
	 * @param resourceCenter
	 * @param technologyContextManager
	 * @return
	 */
	protected <I> R unregisterResource(R resource, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<TA> technologyContextManager) {
		// TODO
		logger.warning("unregisterResource() not implemented yet");
		return resource;
	}

	/**
	 * Internally called to register a {@link FlexoResource} in a {@link ResourceRepository}<br>
	 * Folder in computed according to I/O delegate type
	 * 
	 * @param resource
	 * @param resourceRepository
	 */
	protected <I> void registerResourceInResourceRepository(R resource, ResourceRepository<R, I> resourceRepository) {
		FlexoResourceCenter<I> resourceCenter = resourceRepository.getResourceCenter();
		FlexoIODelegate<I> ioDelegate = (FlexoIODelegate<I>) resource.getFlexoIODelegate();
		RepositoryFolder<R, I> folder = resourceCenter.getRepositoryFolder(ioDelegate, resourceRepository);
		if (folder != null) {
			resourceRepository.registerResource(resource, folder);
		}
		else {
			logger.warning("Could not lookup folder for " + resourceCenter + " while registering " + resource);
		}
	}

	/**
	 * Build and return an empty content for a resource
	 * 
	 * @return
	 */
	public abstract RD makeEmptyResourceData(R resource);

	/**
	 * Return boolean indicating is supplied serialization artefact seems to be a good candidate to be wrapped in considered
	 * {@link FlexoResource}
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @return
	 */
	public abstract <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter);

}
