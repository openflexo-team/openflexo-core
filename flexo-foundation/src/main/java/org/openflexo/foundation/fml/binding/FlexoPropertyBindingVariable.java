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
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.FlexoProperty;

public class FlexoPropertyBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(FlexoPropertyBindingVariable.class.getPackage().getName());

	private final FlexoProperty<?> flexoProperty;
	private Type lastKnownType = null;

	public FlexoPropertyBindingVariable(FlexoProperty<?> flexoProperty) {
		super(flexoProperty.getName(), flexoProperty.getResultingType(), !flexoProperty.isReadOnly());
		this.flexoProperty = flexoProperty;
		if (flexoProperty != null) {
			lastKnownType = flexoProperty.getResultingType();
		}
		if (flexoProperty != null && flexoProperty.getPropertyChangeSupport() != null) {
			flexoProperty.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public void delete() {
		// Thread.dumpStack();
		if (flexoProperty != null && flexoProperty.getPropertyChangeSupport() != null) {
			PropertyChangeSupport pcSupport = flexoProperty.getPropertyChangeSupport();
			// System.out.println("Je lui dit d'arreter d'observer " + flexoProperty);
			flexoProperty.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public String getVariableName() {
		return getFlexoProperty().getName();
	}

	@Override
	public Type getType() {
		return getFlexoProperty().getResultingType();
	}

	public FlexoProperty<?> getFlexoProperty() {
		return flexoProperty;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (evt.getSource() == getFlexoProperty()) {
			if (evt.getPropertyName().equals(FlexoProperty.NAME_KEY) || evt.getPropertyName().equals(FlexoProperty.PROPERTY_NAME_KEY)) {
				// System.out.println("Notify name changing for " + getFlexoRole() + " new=" + getVariableName());
				getPropertyChangeSupport().firePropertyChange(VARIABLE_NAME_PROPERTY, evt.getOldValue(), getVariableName());
			}
			if (evt.getPropertyName().equals(TYPE_PROPERTY) || evt.getPropertyName().equals(FlexoProperty.RESULTING_TYPE_PROPERTY)) {
				Type newType = getFlexoProperty().getResultingType();
				if (lastKnownType == null || !lastKnownType.equals(newType)) {
					/*System.out.println("pcSupport=" + getPropertyChangeSupport());
					if (getPropertyChangeSupport() == null) {
						System.out.println("trop con lui");
						System.out.println("ca vient de " + evt.getSource());
						Thread.dumpStack();
					}*/
					if (getPropertyChangeSupport() != null) {
						getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, newType);
					}
					lastKnownType = newType;
				}
			}
			if (lastKnownType != getType()) {
				// We might arrive here only in the case of a FlexoRole does not correctely notify
				// its type change. We warn it to 'tell' the developper that such notification should be done
				// in FlexoRole (see IndividualRole for example)
				logger.warning("Detecting un-notified type changing for FlexoProperty " + flexoProperty + " from " + lastKnownType + " to "
						+ getType() + ". Trying to handle case.");
				getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, getType());
				lastKnownType = getType();
			}
		}
	}
}
