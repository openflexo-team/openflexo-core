package org.openflexo.foundation.viewpoint.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.viewpoint.FlexoRole;

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
			if (lastKnownType != getType()) {
				// System.out.println("Notify type changing");
				getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, getType());
				lastKnownType = getType();
			}
		}
	}
}