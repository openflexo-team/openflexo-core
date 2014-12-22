package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.fml.editionaction.IterationAction;

/**
 * BindingVariable associated to a {@link IterationAction} iterator
 * 
 * @author sylvain
 * 
 */
public class IterationActionBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(IterationActionBindingVariable.class.getPackage().getName());

	private final IterationAction action;
	private Type lastKnownType = null;

	public IterationActionBindingVariable(IterationAction action) {
		super(action.getIteratorName(), action.getItemType(), true);
		this.action = action;
		if (action != null) {
			lastKnownType = action.getItemType();
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
		return getAction().getIteratorName();
	}

	@Override
	public Type getType() {
		return getAction().getItemType();
	}

	public IterationAction getAction() {
		return action;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getAction()) {
			if (evt.getPropertyName().equals(IterationAction.ITERATOR_NAME_KEY)) {
				System.out.println("Hop, l'iterator change de " + evt.getOldValue() + " a " + getVariableName());
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