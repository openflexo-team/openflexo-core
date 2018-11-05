package org.openflexo.foundation.action.copypaste;

import java.awt.Event;

import org.openflexo.foundation.FlexoObject;

/**
 * A {@link PastingContext} contains all informations to manage a pasting operation<br>
 * {@link PastingContext} instances should be retrieved from {@link PasteHandler} instances.
 * 
 * @author sylvain
 * 
 */
public interface PastingContext<T extends FlexoObject> {

	/**
	 * Return the object that will hold pasting point for this {@link PastingContext}
	 * 
	 * @return
	 */
	public T getPastingPointHolder();

	/**
	 * Return the (not required) {@link java.awt.Event} from which originate the paste operation<br>
	 * (might be null)
	 * 
	 * @return
	 */
	public Event getEvent();

	public String getPasteProperty(String key);

	public void setPasteProperty(String key, String value);

}
