package org.openflexo.foundation.resource;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;

/**
 * Flexo IO Delegate makes a link between a flexo resource and a serialization artefact.
 * 
 * @author Vincent, Sylvain
 *
 * @param <I>
 */
@ModelEntity(isAbstract = true)
@Imports({ @Import(FlexoIOStreamDelegate.class) })
public interface FlexoIODelegate<I> {

	@PropertyIdentifier(type = String.class)
	public static final String NAME = "name";

	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_RESOURCE = "flexo_resource";

	/**
	 * A Serialization Artefact represents the flexo resource according to a given serialization type
	 */
	public static final String SERIALIZATION_ARTEFACT = "serialization_artefact";

	@Getter(value = FLEXO_RESOURCE, inverse = FlexoResource.FLEXO_IO_DELEGATE)
	public FlexoResource<?> getFlexoResource();

	@Setter(FLEXO_RESOURCE)
	public void setFlexoResource(FlexoResource<?> resource);

	@Getter(value = SERIALIZATION_ARTEFACT, ignoreType = true)
	public I getSerializationArtefact();

	@Setter(SERIALIZATION_ARTEFACT)
	public void setSerializationArtefact(I artefact);

	/**
	 * Indicates whether this resource can be edited or not. Returns <code>true</code> if the resource cannot be edited, else returns
	 * <code>false</code>.
	 * 
	 * @return <code>true</code> if the resource cannot be edited, else returns <code>false</code>.
	 */
	public boolean isReadOnly();

	public boolean delete();

	/**
	 * Return true if the serialization artefact exists
	 * 
	 * @return
	 */
	public boolean exists();

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
}
