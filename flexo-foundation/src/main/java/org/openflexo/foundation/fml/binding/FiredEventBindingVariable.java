package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;

import org.openflexo.foundation.fml.EventListener;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;

/**
 * BindingVariable used to handle fired event in a {@link EventListener} behaviour
 * 
 * @author sylvain
 * 
 */
public class FiredEventBindingVariable extends AbstractFMLBindingVariable implements PropertyChangeListener {

	public static final String EVENT_NAME = "event";
	private final EventListener eventListener;

	public FiredEventBindingVariable(EventListener eventListener) {
		super(EVENT_NAME, true);
		this.eventListener = eventListener;
		typeMightHaveChanged();
		if (eventListener.getPropertyChangeSupport() != null) {
			eventListener.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public void delete() {
		if (eventListener != null && eventListener.getPropertyChangeSupport() != null) {
			eventListener.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public String getVariableName() {
		return EVENT_NAME;
	}

	@Override
	public Type getType() {
		if (getEventListener() != null) {
			return FlexoConceptInstanceType.getFlexoConceptInstanceType(getEventListener().getEventType());
		}
		return null;
	}

	public EventListener getEventListener() {
		return eventListener;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		super.propertyChange(evt);
		
		if (evt.getSource() == getEventListener()) {
			typeMightHaveChanged();
		}
	}

}
