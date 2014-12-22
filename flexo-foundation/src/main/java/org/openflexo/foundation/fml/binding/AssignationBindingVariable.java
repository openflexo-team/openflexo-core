package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.fml.editionaction.AssignableAction;

/**
 * BindingVariable associated to an {@link AssignableAction}
 * 
 * @author sylvain
 * 
 */
public class AssignationBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(AssignationBindingVariable.class.getPackage().getName());

	private final AssignableAction<?, ?> action;
	private Type lastKnownType = null;

	public AssignationBindingVariable(AssignableAction<?, ?> action) {
		super(action.getVariableName(), action.getAssignableType(), true);
		this.action = action;
		if (action != null) {
			lastKnownType = action.getAssignableType();
		}
		if (action != null && action.getPropertyChangeSupport() != null) {
			action.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public void delete() {
		if (action != null && action.getPropertyChangeSupport() != null) {
			action.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public String getVariableName() {
		return getAction().getVariableName();
	}

	@Override
	public Type getType() {
		return getAction().getAssignableType();
	}

	public AssignableAction<?, ?> getAction() {
		return action;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getAction()) {
			if (evt.getPropertyName().equals(AssignableAction.VARIABLE_NAME_KEY)) {
				// System.out.println("Notify name changing for " + getFlexoRole() + " new=" + getVariableName());
				getPropertyChangeSupport().firePropertyChange(VARIABLE_NAME_PROPERTY, evt.getOldValue(), getVariableName());
			}
			if (lastKnownType != getType()) {
				// System.out.println("Notify type changing");
				getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, getType());
				lastKnownType = getType();
			}
		}
	}
}