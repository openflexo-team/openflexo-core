/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.factory.AccessibleProxyObject;
import org.openflexo.rm.Resource;

/**
 * Flexo IO Delegate makes a link between a flexo resource and a serialization
 * artefact.
 * 
 * @author Vincent, Sylvain
 *
 * @param <I>
 */
@ModelEntity(isAbstract = true)
@Imports({ @Import(FlexoIOStreamDelegate.class), @Import(ClassLoaderIODelegate.class) })
public interface FlexoIODelegate<I> extends AccessibleProxyObject {

	@PropertyIdentifier(type = String.class)
	public static final String NAME = "name";

	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_RESOURCE = "flexo_resource";

	/**
	 * A Serialization Artefact represents the flexo resource according to a
	 * given serialization type
	 */
	public static final String SERIALIZATION_ARTEFACT = "serialization_artefact";

	@Getter(value = FLEXO_RESOURCE)
	public FlexoResource<?> getFlexoResource();

	@Setter(FLEXO_RESOURCE)
	public void setFlexoResource(FlexoResource<?> resource);

	@Getter(value = SERIALIZATION_ARTEFACT, ignoreType = true)
	public I getSerializationArtefact();

	@Setter(SERIALIZATION_ARTEFACT)
	public void setSerializationArtefact(I artefact);

	public String getSerializationArtefactName();

	public Resource getSerializationArtefactAsResource();

	/**
	 * Indicates whether this resource can be edited or not. Returns
	 * <code>true</code> if the resource cannot be edited, else returns
	 * <code>false</code>.
	 * 
	 * @return <code>true</code> if the resource cannot be edited, else returns
	 *         <code>false</code>.
	 */
	public boolean isReadOnly();

	public boolean delete();

	/**
	 * Return true if the serialization artefact exists
	 * 
	 * @return
	 */
	public boolean exists();

	public String stringRepresentation();

	/**
	 * Return true if the serialization artefact can be overrided
	 * 
	 * @return
	 */
	public boolean hasWritePermission();

	@Override
	public abstract String toString();

	public FileWritingLock willWriteOnDisk();

	public void hasWrittenOnDisk(FileWritingLock lock);

	public void notifyHasBeenWrittenOnDisk();

	public String getParentPath();

	/**
	 * Called when the {@link FlexoResource} this delegate handle I/O has been
	 * renamed.
	 */
	public void rename() throws CannotRenameException;

	public void save(FlexoResource<?> resource) throws NotImplementedException;

	public RepositoryFolder<?, I> getRepositoryFolder(ResourceRepository<?, I> resourceRepository,
			boolean createWhenNonExistent) throws IOException;

	/**
	 * Used to retrieve a ClassLoader exposing code embedded in serialization
	 * artefact
	 * 
	 * @return
	 */
	public ClassLoader retrieveClassLoader();

	/**
	 * Used to retrieve a resource stored in parent serialization artefact, and
	 * identified by a relativePathName
	 * 
	 * @param relativePathName
	 * @return
	 */
	// public Resource locateResourceRelativeToParentPath(String
	// relativePathName);
}
