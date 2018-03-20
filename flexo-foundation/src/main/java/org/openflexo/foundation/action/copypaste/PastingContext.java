package org.openflexo.foundation.action.copypaste;

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

	public String getPasteProperty(String key);

	public void setPasteProperty(String key, String value);

}
