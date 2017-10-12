package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;

import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.GetSetProperty;

/**
 * BindingVariable used to give set value while control graph is declared as a SET control graph for a {@link GetSetProperty}
 * 
 * @author sylvain
 * 
 */
public class SetValueBindingVariable<T> extends BindingVariable implements PropertyChangeListener {

	private final GetSetProperty<T> property;
	private Type lastKnownType = null;

	// private boolean debug = false;

	public SetValueBindingVariable(GetSetProperty<T> property) {
		super(property.getValueVariableName(), property.getType(), true);
		this.property = property;
		if (property != null) {
			lastKnownType = property.getType();
		}
		if (property != null && property.getPropertyChangeSupport() != null) {
			property.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public void delete() {
		if (property != null && property.getPropertyChangeSupport() != null) {
			property.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public String getVariableName() {
		return property.getValueVariableName();
	}

	@Override
	public Type getType() {
		if (getProperty() != null) {
			return getProperty().getType();
		}
		return null;
	}

	public GetSetProperty<T> getProperty() {
		return property;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// if (debug) {
		// System.out.println("****** propertyChange " + evt.getPropertyName() + " source=" + evt.getSource());
		// }
		if (evt.getSource() == getProperty()) {
			if (evt.getPropertyName().equals(GetSetProperty.VALUE_VARIABLE_NAME_KEY)) {
				// System.out.println("Notify name changing for " + getFlexoRole() + " new=" + getVariableName());
				getPropertyChangeSupport().firePropertyChange(VARIABLE_NAME_PROPERTY, evt.getOldValue(), getVariableName());
			}
			valueVariableTypeMightHaveChanged();
		}
	}

	private void valueVariableTypeMightHaveChanged() {
		if (lastKnownType != getType()) {
			// if (debug) {
			// System.out.println("Iterator type changed for " + getType());
			// }
			getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, getType());
			lastKnownType = getType();
		}
	}

}
