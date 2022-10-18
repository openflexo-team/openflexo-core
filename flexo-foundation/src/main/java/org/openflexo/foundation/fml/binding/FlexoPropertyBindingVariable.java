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
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;

/**
 * A {@link BindingVariable} representing access to a {@link FlexoProperty} in a {@link FlexoConcept} instance context.
 * 
 * @author sylvain
 *
 */
public class FlexoPropertyBindingVariable extends AbstractFMLBindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(FlexoPropertyBindingVariable.class.getPackage().getName());

	private final FlexoProperty<?> flexoProperty;

	public FlexoPropertyBindingVariable(FlexoProperty<?> flexoProperty) {
		super(flexoProperty.getName(), !flexoProperty.isReadOnly());
		this.flexoProperty = flexoProperty;
		typeMightHaveChanged();
		if (flexoProperty.getPropertyChangeSupport() != null) {
			flexoProperty.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public void delete() {
		if (flexoProperty != null && flexoProperty.getPropertyChangeSupport() != null) {
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

		super.propertyChange(evt);
		
		if (evt.getSource() == getFlexoProperty()) {
			if (evt.getPropertyName().equals(FlexoProperty.NAME_KEY) || evt.getPropertyName().equals(FlexoProperty.PROPERTY_NAME_KEY)) {
				// System.out.println("Notify name changing for " + getFlexoProperty() + " new=" + getVariableName());
				if (getPropertyChangeSupport() != null) {
					getPropertyChangeSupport().firePropertyChange(VARIABLE_NAME_PROPERTY, evt.getOldValue(), getVariableName());
				}
			}
			typeMightHaveChanged();
		}
	}

	public Object getValue(FlexoConceptInstance flexoConceptInstance) {

		if (flexoProperty instanceof FlexoRole) {
			if (flexoProperty.getCardinality().isMultipleCardinality())
				return flexoConceptInstance.getFlexoActorList((FlexoRole<?>) flexoProperty);
			return flexoConceptInstance.getFlexoActor((FlexoRole<?>) flexoProperty);
		}
		return flexoConceptInstance.getFlexoPropertyValue(flexoProperty);
	}

	@Override
	public boolean isSettable() {
		if (flexoProperty != null) {
			return !flexoProperty.isReadOnly();
		}
		return super.isSettable();
	}

}
