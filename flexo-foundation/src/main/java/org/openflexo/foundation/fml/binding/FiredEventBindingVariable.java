package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;

import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.EventListener;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;

/**
 * BindingVariable used to handle fired event in a {@link EventListener} behaviour
 * 
 * @author sylvain
 * 
 */
public class FiredEventBindingVariable extends BindingVariable implements PropertyChangeListener {

	public static final String EVENT_NAME = "event";
	private final EventListener eventListener;
	private Type lastKnownType = null;

	public FiredEventBindingVariable(EventListener eventListener) {
		super(EVENT_NAME, FlexoConceptInstanceType.getFlexoConceptInstanceType(eventListener.getEventType()), true);
		this.eventListener = eventListener;
		lastKnownType = getType();
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
		if (evt.getSource() == getEventListener()) {
			/*if (evt.getPropertyName().equals(GetSetProperty.VALUE_VARIABLE_NAME_KEY)) {
				// System.out.println("Notify name changing for " + getFlexoRole() + " new=" + getVariableName());
				getPropertyChangeSupport().firePropertyChange(VARIABLE_NAME_PROPERTY, evt.getOldValue(), getVariableName());
			}*/
			valueVariableTypeMightHaveChanged();
		}
	}

	private void valueVariableTypeMightHaveChanged() {
		if (lastKnownType != getType()) {
			getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, getType());
			lastKnownType = getType();
		}
	}

}
