package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.editionaction.FetchRequestIterationAction;

/**
 * BindingVariable associated to an {@link FetchRequestIterationAction} iterator
 * 
 * @author sylvain
 * 
 */
public class FetchRequestConditionSelectedBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(FetchRequestConditionSelectedBindingVariable.class.getPackage().getName());

	private final FetchRequestCondition condition;
	private Type lastKnownType = null;

	public FetchRequestConditionSelectedBindingVariable(FetchRequestCondition condition) {
		super(FetchRequestCondition.SELECTED, condition.getAction() != null ? condition.getAction().getFetchedType() : Object.class, false);
		this.condition = condition;
		lastKnownType = condition.getAction() != null ? condition.getAction().getFetchedType() : Object.class;
		if (condition != null && condition.getPropertyChangeSupport() != null) {
			condition.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		if (condition.getAction() != null && condition.getAction().getPropertyChangeSupport() != null) {
			condition.getAction().getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public void delete() {
		if (condition != null && condition.getPropertyChangeSupport() != null) {
			condition.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		if (condition.getAction() != null && condition.getAction().getPropertyChangeSupport() != null) {
			condition.getAction().getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public Type getType() {
		if (condition.getAction() != null) {
			return condition.getAction().getFetchedType();
		}
		return null;
	}

	public FetchRequestCondition getCondition() {
		return condition;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == condition || evt.getSource() == condition.getAction()) {
			if (lastKnownType != getType()) {
				// System.out.println("Notify type changing");
				getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, getType());
				lastKnownType = getType();
			}
		}
	}
}