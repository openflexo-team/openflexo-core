package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.fml.controlgraph.FetchRequestIterationAction;

/**
 * BindingVariable associated to an {@link FetchRequestIterationAction} iterator
 * 
 * @author sylvain
 * 
 */
public class FetchRequestIterationActionBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(FetchRequestIterationActionBindingVariable.class.getPackage().getName());

	private final FetchRequestIterationAction action;
	private Type lastKnownType = null;

	public FetchRequestIterationActionBindingVariable(FetchRequestIterationAction action) {
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

	public FetchRequestIterationAction getAction() {
		return action;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getAction()) {
			if (evt.getPropertyName().equals(FetchRequestIterationAction.ITERATOR_NAME_KEY)) {
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