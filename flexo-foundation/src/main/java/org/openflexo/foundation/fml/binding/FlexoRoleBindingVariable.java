/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.FlexoRole;

public class FlexoRoleBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(FlexoRoleBindingVariable.class.getPackage().getName());

	private final FlexoRole<?> flexoRole;
	private Type lastKnownType = null;

	public FlexoRoleBindingVariable(FlexoRole<?> flexoRole) {
		super(flexoRole.getName(), flexoRole.getResultingType(), true);
		this.flexoRole = flexoRole;
		if (flexoRole != null) {
			lastKnownType = flexoRole.getResultingType();
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
		return getFlexoRole().getResultingType();
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
			if (evt.getPropertyName().equals(TYPE_PROPERTY) || evt.getPropertyName().equals(FlexoRole.RESULTING_TYPE_PROPERTY)) {
				Type newType = getFlexoRole().getResultingType();
				System.out.println("ici, newType=" + newType);
				if (lastKnownType == null || !lastKnownType.equals(newType)) {
					System.out.println("on notifie " + TYPE_PROPERTY + " de " + lastKnownType + " a " + newType);
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
