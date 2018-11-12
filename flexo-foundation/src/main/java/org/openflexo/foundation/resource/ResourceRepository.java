package org.openflexo.foundation.resource;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * A {@link ResourceRepository} stores all resources of a particular type.<br>
 * Resources are organized with a logical folder hierarchy inside the repository<br>
 * A {@link ResourceRepository} lives in a {@link FlexoResourceCenter}.
 * 
 * @author sylvain
 * 
 * @param <R>
 *            type of resources being stored in this {@link ResourceRepositoryImpl}
 * @param <I>
 *            serialization artefact type
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(ResourceRepositoryImpl.class)
public interface ResourceRepository<R extends FlexoResource<?>, I> {

	/**
	 * Return serialization artefact which is the base of this {@link ResourceRepository}
	 * 
	 * @return
	 */
	I getBaseArtefact();

	/**
	 * Sets serialization artefact which is the base of this {@link ResourceRepository}
	 * 
	 * @param baseArtefact
	 */
	public void setBaseArtefact(I baseArtefact);

	/**
	 * Return the logical root folder for this {@link ResourceRepository}
	 * 
	 * @return
	 */
	RepositoryFolder<R, I> getRootFolder();

	/**
	 * Return the default base URI associated with the {@link ResourceRepositoryImpl}.<br>
	 * 
	 * This URI might be used as default base URI for any resource stored in this repository, if no explicit URI was given to related
	 * resource. Resulting URI will be given by concatenation of this base URI with base name for related resource
	 * 
	 * @return
	 */
	String getDefaultBaseURI();

	/**
	 * Generate and return an URI of supplied resource, if this resource has no explicit URI, asserting the resource is not yet contained in
	 * repository (otherwise, an new URI will be generated)<br>
	 * 
	 * Returned URI will be given by concatenation of this base URI with base name for related resource
	 * 
	 * @return
	 */
	String generateURI(String baseName);

	/**
	 * Return the object which is the "owner" of this repository.<br>
	 * The {@link FlexoResourceCenter} as owner has the responsability of this repository.
	 */
	FlexoResourceCenter<I> getResourceCenter();

	/**
	 * Sets the "owner" of this repository
	 * 
	 * @param owner
	 */
	void setResourceCenter(FlexoResourceCenter<I> resourceCenter);

	/**
	 * Return resource with the supplied URI, if this resource was already declared<br>
	 * Also implement a scheme to resolve resources from URI whose value has changed since registration
	 * 
	 * @param resourceURI
	 * @return
	 */
	R getResource(String resourceURI);

	/**
	 * Register supplied resource in default root folder
	 * 
	 * @param flexoResource
	 */
	void registerResource(R flexoResource);

	void unregisterResource(R flexoResource);

	/**
	 * Register supplied resource in supplied folder
	 * 
	 * @param resource
	 * @param parentFolder
	 */
	void registerResource(R resource, RepositoryFolder<R, I> parentFolder);

	/**
	 * Register supplied resource in parent resource
	 * 
	 * @param resource
	 * @param parentFolder
	 */
	void registerResource(R resource, R parentResource);

	/**
	 * Creates new folder with supplied name in supplied parent folder
	 * 
	 * @param folderName
	 * @param parentFolder
	 * @return the newly created folder
	 */
	RepositoryFolder<R, I> createNewFolder(String folderName, RepositoryFolder<R, I> parentFolder);

	/**
	 * Creates new folder with supplied name in default root folder
	 * 
	 * @param folderName
	 * @return the newly created folder
	 */
	RepositoryFolder<R, I> createNewFolder(String folderName);

	/**
	 * Delete supplied folder, asserting supplied folder is empty
	 * 
	 * @param folder
	 */
	void deleteFolder(RepositoryFolder<R, I> folder);

	/**
	 * Move resource from a folder to an other one
	 * 
	 * @param resource
	 * @param fromFolder
	 * @param toFolder
	 */
	void moveResource(R resource, RepositoryFolder<R, I> fromFolder, RepositoryFolder<R, I> toFolder);

	/**
	 * Return a collection storing all resources contained in this repository
	 * 
	 * @return
	 */
	Collection<R> getAllResources();

	/**
	 * Return flag indicating if supplied resource is contained in this repository
	 * 
	 * @param resource
	 * @return
	 */
	// TODO: perf issue
	boolean containsResource(R resource);

	/**
	 * Return the repository folder where the resource is registered
	 * 
	 * @param resource
	 * @return
	 */
	RepositoryFolder<R, I> getRepositoryFolder(R resource);

	/**
	 * Returns the number of resources registed in this repository
	 * 
	 * @return
	 */
	int getSize();

	/**
	 * Return an enumeration of all folders, by recursively explore the tree
	 * 
	 * @return an Enumeration of FlexoComponentFolder elements
	 */
	Enumeration<RepositoryFolder<R, I>> allFolders();

	/**
	 * Return number of folders
	 */
	int allFoldersCount();

	RepositoryFolder<R, I> getFolderWithName(String folderName);

	RepositoryFolder<R, I> getParentFolder(R resource);

	RepositoryFolder<R, I> getParentFolder(RepositoryFolder<R, I> aFolder);

	/**
	 * Get the parent repository folder. The object can be accessed from different ways, for instance it can be a file or an InJarResource,
	 * so the path must be computed for each kind of access.
	 * 
	 * @param element
	 * @param createWhenNonExistent
	 * @return
	 * @throws IOException
	 */
	RepositoryFolder<R, I> getParentRepositoryFolder(I serializationArtefact, boolean createWhenNonExistent) throws IOException;

	/**
	 * Get the repository folder. The object can be accessed from different ways, for instance it can be a file or an InJarResource, so the
	 * path must be computed for each kind of access.
	 * 
	 * @param element
	 * @param createWhenNonExistent
	 * @return
	 * @throws IOException
	 */
	RepositoryFolder<R, I> getRepositoryFolder(I serializationArtefact, boolean createWhenNonExistent) throws IOException;

	/**
	 * Return class of resource this repository contains
	 * 
	 * @return
	 */
	Class<?> getResourceClass();

	/**
	 * Return class of resource this repository contains
	 * 
	 * @return
	 */
	Class<? extends ResourceData<?>> getResourceDataClass();

	String getDisplayableName();

	/**
	 * Return boolean indicating is supplied resource is contained (recursive semantics) in supplied container
	 * 
	 * @param resource
	 * @param container
	 * @return
	 */
	boolean isResourceContainedIn(FlexoResource<?> resource, FlexoResource<?> container);

	/**
	 * Return the most specialized container for the two supplied {@link FlexoResource}
	 * 
	 * Returned value could be:
	 * <ul>
	 * <li>a {@link FlexoResource} containing both resources</li>
	 * <li>a {@link RepositoryFolder} containing both resources</li>
	 * <li>a {@link ResourceRepositoryImpl} (a ResourceCenter) containing both resources</li>
	 * </ul>
	 * 
	 * @param resource1
	 * @param resource2
	 * @return
	 */
	// TODO: we should write unit tests for that
	FlexoObject getMostSpecializedContainer(R resource1, R resource2);

	/**
	 * Return the most specialized {@link RepositoryFolder} for the two supplied {@link FlexoResource}
	 * 
	 * @param folder1
	 * @param folder2
	 * @return
	 */
	// TODO: we should write unit tests for that
	RepositoryFolder<R, I> getMostSpecializedRepositoryFolder(RepositoryFolder<R, I> folder1, RepositoryFolder<R, I> folder2);

}
