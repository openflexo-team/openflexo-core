package org.openflexo.foundation.resource;

import java.io.IOException;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

public interface IFlexoResourceFactory<R extends FlexoResource<RD>, RD extends ResourceData<RD>> {

	/**
	 * Return class of {@link FlexoResource} beeing managed by this factory
	 * 
	 * @return
	 */
	Class<R> getResourceClass();

	/**
	 * Return type of {@link ResourceData} beeing managed by this factory
	 * 
	 * @return
	 */
	Class<RD> getResourceDataClass();

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
	<I> R makeResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException;

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
	<I> R makeResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter, String name, String uri, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException;

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
	<I> R makeResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter, String name, String uri,
			Class<? extends RD> specializedResourceDataClass, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException;

	/**
	 * Retrieve resource for a given artefact, a resource center and a technology context manager.<br>
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @param technologyContextManager
	 * @return
	 */
	<I> R retrieveResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) throws ModelDefinitionException, IOException;

	/**
	 * Build and return an empty content for a resource
	 * 
	 * @return
	 */
	RD makeEmptyResourceData(R resource);

	/**
	 * Return boolean indicating is supplied serialization artefact seems to be a good candidate to be wrapped in considered
	 * {@link FlexoResource}
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @return
	 */
	<I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter);

	/**
	 * If supplied serialization artefact is interpretable as a former format and might be read as a valid resource
	 * 
	 * Return converted serialization artefact when adequate, null if this serialization artefact is not to be converted.
	 * 
	 * This method is a hook to convert former resources when serialization change (backward compatibility)
	 * 
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @return
	 */
	default <I> I getConvertableArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		return null;
	}
}
