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
import java.util.logging.Logger;

import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;

/**
 * A {@link BindingVariable} representing access to a {@link FlexoProperty} in a {@link FlexoConcept} instance context.
 * 
 * @author sylvain
 *
 */
public class SuperBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(SuperBindingVariable.class.getPackage().getName());

	private final FlexoConcept superConcept;
	private final boolean isUnique;

	public SuperBindingVariable(FlexoConcept superConcept, boolean isUnique) {
		super(isUnique ? FlexoConceptBindingModel.SUPER_PROPERTY_NAME : FlexoConceptBindingModel.SUPER_PROPERTY_NAME + "_" + superConcept.getName(),
				superConcept.getInstanceType());
		this.superConcept = superConcept;
		this.isUnique = isUnique;
		if (superConcept.getPropertyChangeSupport() != null) {
			superConcept.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public void delete() {
		// System.out.println("################# Desactivate " + this + " " + Integer.toHexString(hashCode()) + " on ecoute " +
		// flexoProperty
		// + " cs=" + flexoProperty.getPropertyChangeSupport());

		if (superConcept != null && superConcept.getPropertyChangeSupport() != null) {
			superConcept.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public String getVariableName() {
		return isUnique ? FlexoConceptBindingModel.SUPER_PROPERTY_NAME : FlexoConceptBindingModel.SUPER_PROPERTY_NAME + "_" + superConcept.getName();
	}

	public FlexoConcept getSuperConcept() {
		return superConcept;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (evt.getSource() == getSuperConcept()) {
			if (evt.getPropertyName().equals(FlexoProperty.NAME_KEY) || evt.getPropertyName().equals(FlexoProperty.PROPERTY_NAME_KEY)) {
				// System.out.println("Notify name changing for " + getFlexoProperty() + " new=" + getVariableName());
				if (getPropertyChangeSupport() != null) {
					getPropertyChangeSupport().firePropertyChange(VARIABLE_NAME_PROPERTY, evt.getOldValue(), getVariableName());
				}
			}
		}
	}
}
