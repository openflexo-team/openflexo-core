/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.resource;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.ViewPointRepository;
import org.openflexo.foundation.resource.DirectoryResourceCenter.DirectoryResourceCenterEntry;
import org.openflexo.foundation.resource.JarResourceCenter.JarResourceCenterEntry;
import org.openflexo.foundation.resource.RemoteResourceCenter.RemoteResourceCenterEntry;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;

/**
 * A {@link FlexoResourceCenter} is a symbolic repository storing {@link FlexoResource} from artefacts of type I
 * 
 * @param <I>
 *            I is the type of iterable artefacts this resource center stores
 * 
 * @author sylvain
 * 
 */
@ModelEntity
public interface FlexoResourceCenter<I> extends Iterable<I>, FlexoObject {

	/**
	 * Provides a persistent entry allowing to instantiate a FlexoResourceCenter
	 * 
	 * @author sylvain
	 * 
	 */
	@ModelEntity
	@XMLElement
	@Imports({ @Import(DirectoryResourceCenterEntry.class), @Import(RemoteResourceCenterEntry.class),
			@Import(JarResourceCenterEntry.class) })
	public static interface ResourceCenterEntry<RC extends FlexoResourceCenter<?>> {

		public RC makeResourceCenter();
	}

	/**
	 * Return a user-friendly named identifier for this resource center
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Return the default base URI associated with this {@link FlexoResourceCenter}.<br>
	 * 
	 * This URI might be used as default base URI for any resource stored in this repository, if no explicit URI was given to related
	 * resource. Resulting URI will be given by concatenation of this base URI with base name for related resource
	 * 
	 * @return
	 */
	public abstract String getDefaultBaseURI();

	/**
	 * Initialize the FlexoResourceCenter by retrieving viewpoints defined in this {@link FlexoResourceCenter}<br>
	 * 
	 * @param technologyAdapterService
	 */
	// public void initialize(ViewPointLibrary viewPointLibrary);

	/**
	 * Initialize the FlexoResourceCenter by matching contents of this {@link FlexoResourceCenter} with all available technologies.<br>
	 * 
	 * @param technologyAdapterService
	 */
	public void initialize(TechnologyAdapterService technologyAdapterService);

	/**
	 * Finalize the FlexoResourceCenter<br>
	 * 
	 * @param technologyAdapterService
	 */
	public void finalize(TechnologyAdapterService technologyAdapterService);

	/**
	 * Returns all resources available in this resource center
	 * 
	 * @param progress
	 *            a progress monitor that will be notified of the progress of this task. This parameter can be <code>null</code>
	 * @return a list of all resources available in this resource center.
	 */
	public @Nonnull Collection<? extends FlexoResource<?>> getAllResources(@Nullable IProgress progress);

	/**
	 * Returns the resource identified by the given <code>uri</code> and the provided <code>version</code>.
	 * 
	 * @param uri
	 *            the URI of the resource
	 * @param version
	 *            the version of the resource
	 * @param type
	 *            the type of the resource data reference by the resource to retrieve. The implementation is responsible to make the
	 *            appropriate type verifications.
	 * @param progress
	 *            a progress monitor that will be notified of the progress of this task. This parameter can be <code>null</code>
	 * @return the resource with the given <code>uri</code> and the provided <code>version</code>, or null if it cannot be found.
	 */
	public @Nullable <T extends ResourceData<T>> FlexoResource<T> retrieveResource(@Nonnull String uri, @Nonnull FlexoVersion version,
			@Nonnull Class<T> type, @Nullable IProgress progress);

	/**
	 * Returns the resource identified by the given <code>uri</code>.<br>
	 * Returns resource with last version if more than one version is registered
	 * 
	 * @param uri
	 *            the URI of the resource
	 * @param progress
	 *            a progress monitor that will be notified of the progress of this task. This parameter can be <code>null</code>
	 * @return the resource with the given <code>uri</code>, or null if it cannot be found.
	 */
	public @Nullable FlexoResource<?> retrieveResource(@Nonnull String uri, @Nullable IProgress progress);

	/**
	 * Returns all available versions of the resource identified by the given <code>uri</code>
	 * 
	 * @param uri
	 *            the URI of the resource
	 * @param type
	 *            the type of the resource data reference by the resource to retrieve. The implementation is responsible to make the
	 *            appropriate type verifications.
	 * @param progress
	 *            a progress monitor that will be notified of the progress of this task. This parameter can be <code>null</code>
	 * @return all available versions of the resource identified by the given <code>uri</code>. An empty list is returned if no match were
	 *         found
	 */
	public @Nonnull <T extends ResourceData<T>> List<FlexoResource<T>> retrieveResource(@Nonnull String uri, @Nonnull Class<T> type,
			@Nullable IProgress progress);

	/**
	 * Publishes the resource in this resource center.
	 * 
	 * @param resource
	 *            the resource to publish
	 * @param newVersion
	 *            the new version of this resource. If this value is null, the implementation is responsible to set the version
	 *            appropriately (can be left unchanged or updated)
	 * @param progress
	 *            a progress monitor that will be notified of the progress of this task. This parameter can be <code>null</code>
	 * @throws Exception
	 *             in case the publication of this resource failed.
	 */
	public void publishResource(@Nonnull FlexoResource<?> resource, @Nullable FlexoVersion newVersion, @Nullable IProgress progress)
			throws Exception;

	/**
	 * Refreshes this resource center. This can be particularly useful for caching implementations.
	 */
	public void update() throws IOException;

	/**
	 * Retrieve ViewPoint repository (containing all resources storing a ViewPoint) for this {@link FlexoResourceCenter}
	 * 
	 * @return
	 */
	public ViewPointRepository getViewPointRepository();

	/**
	 * Returns an iterator over a set of elements of type I, which are iterables artefacts this resource center stores
	 * 
	 * @return an Iterator.
	 */
	@Override
	public Iterator<I> iterator();

	/**
	 * Return flag indicating whether supplied artefact might be ignored
	 * 
	 * @param artefact
	 * @return
	 */
	public boolean isIgnorable(I artefact);

	/**
	 * Retrieve repository matching supplied type and technology
	 * 
	 * @param repositoryType
	 * @param technologyAdapter
	 * @return the registered repository
	 */
	public <R extends ResourceRepository<?>> R getRepository(Class<? extends R> repositoryType, TechnologyAdapter technologyAdapter);

	/**
	 * Register supplied repository for a given type and technology
	 * 
	 * @param repository
	 *            the non-null repository to register
	 * @param repositoryType
	 * @param technologyAdapter
	 */
	public <R extends ResourceRepository<?>> void registerRepository(R repository, Class<? extends R> repositoryType,
			TechnologyAdapter technologyAdapter);

	/**
	 * Return the list of all {@link ResourceRepository} registered in this ResourceCenter for a given technology
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public Collection<ResourceRepository<?>> getRegistedRepositories(TechnologyAdapter technologyAdapter);

	public ResourceCenterEntry<?> getResourceCenterEntry();

	/**
	 * Stops the Resource Center (When needed)
	 */
	public void stop();

	/**
	 * Compute and return a default URI for supplied resource<br>
	 * If resource does not provide URI support, this might be delegated to the {@link FlexoResourceCenter} through this method
	 * 
	 * @param resource
	 * @return
	 */
	public String getDefaultResourceURI(FlexoResource<?> resource);
}
