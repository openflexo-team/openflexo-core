package org.openflexo.foundation;

import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.model.factory.ModelFactory;

/**
 * Interface implemented by any {@link ModelFactory} managed by a {@link PamelaResource}<br>
 * 
 * This interface gives access to the {@link PamelaResource} and provides hooks for deserialization starting and stopping
 * 
 * @author sylvain
 * 
 */
public interface PamelaResourceModelFactory<R extends PamelaResource<?, ?>> {

	/**
	 * Return the {@link PamelaResource} which manages this {@link PamelaResourceModelFactory}
	 * 
	 * @return
	 */
	public R getResource();

	public void startDeserializing();

	public void stopDeserializing();

}
