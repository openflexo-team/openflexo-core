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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * Abstract implementation a factory that manages the life-cycle of a given type of {@link FlexoResource}
 * 
 * @author sylvain
 *
 * @param <R>
 *            type of FlexoResource being handled by this factory, implementing both {@link TechnologyAdapterResource} and
 *            {@link PamelaResource}
 * @param <RD>
 *            type of {@link ResourceData} managed by resources (contents of resources)
 */
public abstract class FlexoResourceFactory<R extends FlexoResource<RD>, RD extends ResourceData<RD>> extends PamelaModelFactory
		implements IFlexoResourceFactory<R, RD> {

	private static final Logger logger = Logger.getLogger(FlexoResourceFactory.class.getPackage().getName());

	private final Class<R> resourceClass;

	/**
	 * Generic constructor
	 * 
	 * @param resourceClass
	 * @throws ModelDefinitionException
	 */
	protected FlexoResourceFactory(Class<R> resourceClass) throws ModelDefinitionException {
		super(PamelaMetaModelLibrary.retrieveMetaModel(resourceClass, FlexoIODelegate.class));
		this.resourceClass = resourceClass;
	}

	/**
	 * Generic constructor
	 * 
	 * @param resourceClass
	 * @throws ModelDefinitionException
	 */
	protected FlexoResourceFactory(Class<R> resourceClass, Class<?>... requiredClasses) throws ModelDefinitionException {
		super(PamelaMetaModelLibrary.retrieveMetaModel(resourceClass, requiredClasses));
		this.resourceClass = resourceClass;
	}

	/**
	 * Return type of {@link FlexoResource} beeing managed by this factory
	 * 
	 * @return
	 */
	@Override
	public Class<R> getResourceClass() {
		return resourceClass;
	}

	/**
	 * Return type of {@link ResourceData} beeing managed by this factory
	 * 
	 * @return
	 */
	@Override
	public Class<RD> getResourceDataClass() {
		return (Class<RD>) (TypeUtils.getBaseClass(TypeUtils.getTypeArgument(getResourceClass(), TechnologyAdapterResource.class, 0)));
	}

	/**
	 * Make a new empty resource for a given artefact, a resource center and a technology context manager.<br>
	 * The newly created resource is set with empty contents as it is computed from {@link #makeEmptyResourceData()}<br>
	 * Name of resource is retrieved from the name of serialization artefact, and uri is set to default (given by the resource center)
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @param createEmptyContents
	 *            when set to true, initiate contents of resource with technology specific empty contents
	 * @return
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 */
	@Override
	public <I> R makeResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException {
		return makeResource(serializationArtefact, resourceCenter, resourceCenter.retrieveName(serializationArtefact), null,
				createEmptyContents);
	}

	/**
	 * Make a new empty resource for a given artefact, a resource center and a technology context manager.<br>
	 * The newly created resource is set with empty contents as it is computed from {@link #makeEmptyResourceData()}<br>
	 * Name and URI are explicitely given to the new resource
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @param name
	 * @param uri
	 * @param createEmptyContents
	 *            when set to true, initiate contents of resource with technology specific empty contents
	 * @return
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 */
	/*@Override
	public <I> R makeResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter, String name, String uri,
			boolean createEmptyContents) throws SaveResourceException, ModelDefinitionException {
		return makeResource(serializationArtefact, resourceCenter, name, uri, createEmptyContents);
	}*/

	/**
	 * Make a new empty resource for a given artefact, a resource center and a technology context manager.<br>
	 * The newly created resource is set with empty contents as it is computed from {@link #makeEmptyResourceData()}<br>
	 * Name and URI are explicitely given to the new resource
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @param name
	 * @param uri
	 * @param specializedResourceDataClass
	 *            might be null if default, or specialized resource data class
	 * @param createEmptyContents
	 *            when set to true, initiate contents of resource with technology specific empty contents
	 * @return
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 */
	@Override
	public <I> R makeResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter, String name, String uri,
			/*Class<? extends RD> specializedResourceDataClass,*/ boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException {
		R returned = initResourceForCreation(serializationArtefact, resourceCenter, name, uri);
		registerResource(returned, resourceCenter);
		// returned.setSpecializedResourceDataClass(specializedResourceDataClass);

		if (createEmptyContents) {
			createEmptyContents(returned);
			returned.save();
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

	/**
	 * @throws ModelDefinitionException
	 *             raised by children
	 */
	protected <I> R initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter, String name, String uri)
			throws ModelDefinitionException {
		R returned = newInstance(resourceClass);
		returned.setResourceCenter(resourceCenter);
		returned.initName(name);
		returned.setURI(uri);
		returned.setIODelegate(makeFlexoIODelegate(serializationArtefact, resourceCenter));
		return returned;
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.IFlexoResourceFactory#retrieveResource(I, org.openflexo.foundation.resource.FlexoResourceCenter)
	 */
	@Override
	public <I> R retrieveResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter)
			throws ModelDefinitionException, IOException {
		R returned = initResourceForRetrieving(serializationArtefact, resourceCenter);
		registerResource(returned, resourceCenter);
		return returned;
	}

	/**
	 * @throws ModelDefinitionException
	 *             raised by children
	 * @throws IOException
	 *             raised by children
	 */
	protected <I> R initResourceForRetrieving(I serializationArtefact, FlexoResourceCenter<I> resourceCenter)
			throws ModelDefinitionException, IOException {
		R returned = newInstance(resourceClass);
		returned.setResourceCenter(resourceCenter);
		returned.initName(resourceCenter.retrieveName(serializationArtefact));

		returned.setIODelegate(makeFlexoIODelegate(serializationArtefact, resourceCenter));

		return returned;
	}

	protected <I> FlexoIODelegate<I> makeFlexoIODelegate(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		try {
			return resourceCenter.makeFlexoIODelegate(serializationArtefact, this);
		} catch (IOException e) {
			// TODO: find a better way to handle this
			e.printStackTrace();
			return null;
		}
	}

	private Map<Object, R> resourcesForSerializationArtefact = new HashMap<>();

	/**
	 * Called to register a resource in a given {@link FlexoResourceCenter} and a given technology
	 * 
	 * @param resource
	 * @param resourceCenter
	 * @param technologyContextManager
	 * @return
	 */
	public <I> R registerResource(R resource, FlexoResourceCenter<I> resourceCenter) {

		resource.setResourceCenter(resourceCenter);

		if (resourceCenter != null && resource.getIODelegate() != null) {
			resourceCenter.registerResource(resource, (I) resource.getIODelegate().getSerializationArtefact());
		}

		if (resourceCenter != null) {
			resource.setServiceManager(resourceCenter.getServiceManager());
		}

		// Also register the resource in the ResourceCenter seen as a ResourceRepositoryImpl
		if (resourceCenter instanceof ResourceRepositoryImpl) {
			registerResourceInResourceRepository(resource, (ResourceRepository) resourceCenter);
		}

		resourcesForSerializationArtefact.put(resource.getIODelegate().getSerializationArtefact(), resource);

		return resource;
	}

	public <I> R getRegisteredResource(I serializationArtefact) {
		return resourcesForSerializationArtefact.get(serializationArtefact);
	}

	/**
	 * Called to unregister a resource from a given {@link FlexoResourceCenter} and a given technology
	 * 
	 * @param resource
	 * @param resourceCenter
	 * @param technologyContextManager
	 * @return
	 */
	public <I> R unregisterResource(R resource, FlexoResourceCenter<I> resourceCenter) {

		if (resourceCenter.getServiceManager() != null && resourceCenter.getServiceManager().getResourceManager() != null) {
			resourceCenter.getServiceManager().getResourceManager().unregisterResource(resource);
		}

		if (resourceCenter != null && resource != null && resource.getIODelegate() != null) {
			resourceCenter.unregisterResource(resource, (I) resource.getIODelegate().getSerializationArtefact());
		}

		// TODO
		logger.warning("unregisterResource() not fully implemented yet");
		return resource;
	}

	/**
	 * Internally called to register a {@link FlexoResource} in a {@link ResourceRepositoryImpl}<br>
	 * Folder in computed according to I/O delegate type
	 * 
	 * @param resource
	 * @param resourceRepository
	 */
	protected <I> void registerResourceInResourceRepository(R resource, ResourceRepository<R, I> resourceRepository) {
		FlexoResourceCenter<I> resourceCenter = resourceRepository.getResourceCenter();
		FlexoIODelegate<I> ioDelegate = (FlexoIODelegate<I>) resource.getIODelegate();

		if (resource.getContainer() == null) {
			RepositoryFolder<R, I> folder = resourceCenter.getRepositoryFolder(ioDelegate, resourceRepository);
			if (folder != null) {
				resourceRepository.registerResource(resource, folder);
			}
			else {
				logger.warning("Could not lookup folder for " + resourceCenter + " while registering " + resource);
			}
		}
		else {
			resourceRepository.registerResource(resource, (R) resource.getContainer());
		}
	}

	protected <I> void unregisterResourceInResourceRepository(R resource, ResourceRepository<R, I> resourceRepository) {
		resourceRepository.unregisterResource(resource);
	}

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.IFlexoResourceFactory#makeEmptyResourceData(R)
	 */
	@Override
	public abstract RD makeEmptyResourceData(R resource);

	/* (non-Javadoc)
	 * @see org.openflexo.foundation.resource.IFlexoResourceFactory#isValidArtefact(I, org.openflexo.foundation.resource.FlexoResourceCenter)
	 */
	@Override
	public abstract <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter);
}
