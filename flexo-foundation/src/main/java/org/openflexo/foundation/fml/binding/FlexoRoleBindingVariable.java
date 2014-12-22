package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.fml.FlexoRole;

public class FlexoRoleBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(FlexoRoleBindingVariable.class.getPackage().getName());

	private final FlexoRole<?> flexoRole;
	private Type lastKnownType = null;

	public FlexoRoleBindingVariable(FlexoRole<?> flexoRole) {
		super(flexoRole.getName(), flexoRole.getType(), true);
		this.flexoRole = flexoRole;
		if (flexoRole != null) {
			lastKnownType = flexoRole.getType();
		}
		if (flexoRole != null && flexoRole.getPropertyChangeSupport() != null) {
			flexoRole.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public void delete() {
		if (flexoRole != null && flexoRole.getPropertyChangeSupport() != null) {
			flexoRole.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public String getVariableName() {
		return getFlexoRole().getName();
	}

	@Override
	public Type getType() {
		return getFlexoRole().getType();
	}

	public FlexoRole getFlexoRole() {
		return flexoRole;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getFlexoRole()) {
			if (evt.getPropertyName().equals(FlexoRole.NAME_KEY)) {
				// System.out.println("Notify name changing for " + getFlexoRole() + " new=" + getVariableName());
				getPropertyChangeSupport().firePropertyChange(VARIABLE_NAME_PROPERTY, evt.getOldValue(), getVariableName());
			}
			if (evt.getPropertyName().equals(TYPE_PROPERTY)) {
				Type newType = (Type) evt.getNewValue();
				if (lastKnownType == null || !lastKnownType.equals(newType)) {
					getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, newType);
					lastKnownType = newType;
				}
			}
			if (lastKnownType != getType()) {
				// We might arrive here only in the case of a FlexoRole does not correctely notify
				// its type change. We warn it to 'tell' the developper that such notification should be done
				// in FlexoRole (see IndividualRole for example)
				logger.warning("Detecting un-notified type changing for FlexoRole " + flexoRole + " from " + lastKnownType + " to "
						+ getType() + ". Trying to handle case.");
				getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, getType());
				lastKnownType = getType();
			}
		}
	}
}