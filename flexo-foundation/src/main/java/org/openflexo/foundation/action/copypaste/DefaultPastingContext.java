package org.openflexo.foundation.action.copypaste;

import java.awt.Event;
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
	private final Event event;
	private final Map<String, String> pasteProperties = new HashMap<String, String>();

	public DefaultPastingContext(T holder, Event event) {
		this.pastingPointHolder = holder;
		this.event = event;
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

	/**
	 * Return the (not required) {@link java.awt.Event} from which originate the paste operation<br>
	 * (might be null)
	 * 
	 * @return
	 */
	@Override
	public Event getEvent() {
		return event;
	}

	@Override
	public String getPasteProperty(String key) {
		return pasteProperties.get(key);
	}

	@Override
	public void setPasteProperty(String key, String value) {
		pasteProperties.put(key, value);
	}

}