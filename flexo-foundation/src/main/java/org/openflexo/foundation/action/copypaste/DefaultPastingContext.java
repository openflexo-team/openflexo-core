package org.openflexo.foundation.action.copypaste;

import java.util.HashMap;
import java.util.Map;

import org.openflexo.foundation.FlexoObject;

/**
 * This is the default implementation of {@link PastingContext} <br>
 * Contains all informations to manage a pasting operation
 * 
 * @author sylvain
 * 
 */
public class DefaultPastingContext<T extends FlexoObject> implements PastingContext<T> {

	private final T pastingPointHolder;
	private final Map<String, String> pasteProperties = new HashMap<>();

	public DefaultPastingContext(T holder) {
		this.pastingPointHolder = holder;
	}

	/**
	 * Return the object that will hold pasting point for this {@link PastingContext}
	 * 
	 * @return
	 */
	@Override
	public T getPastingPointHolder() {
		return pastingPointHolder;
	}

	@Override
	public String getPasteProperty(String key) {
		return pasteProperties.get(key);
	}

	@Override
	public void setPasteProperty(String key, String value) {
		pasteProperties.put(key, value);
	}

	@Override
	public String toString() {
		return "DefaultPastingContext/" + getPastingPointHolder() + "/" + pasteProperties;
	}

}
