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
import java.util.logging.Logger;

import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;

/**
 * A {@link BindingVariable} representing access to a {@link FlexoConceptInstanceRole} in a {@link FlexoConcept} instance context.
 * 
 * @author sylvain
 *
 */
public class FlexoConceptInstanceRoleBindingVariable extends FlexoRoleBindingVariable {
	static final Logger logger = Logger.getLogger(FlexoConceptInstanceRoleBindingVariable.class.getPackage().getName());

	private FlexoConcept conceptBeeingListened = null;

	public FlexoConceptInstanceRoleBindingVariable(FlexoConceptInstanceRole role) {
		super(role);
		if (getFlexoRole().getFlexoConceptType() != null) {
			conceptBeeingListened = getFlexoRole().getFlexoConceptType();
			conceptBeeingListened.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public void delete() {
		if (conceptBeeingListened != null) {
			conceptBeeingListened.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public FlexoConceptInstanceRole getFlexoRole() {
		return (FlexoConceptInstanceRole) super.getFlexoRole();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == getFlexoRole() && evt.getPropertyName().equals(FlexoConceptInstanceRole.FLEXO_CONCEPT_TYPE_KEY)) {
			if (conceptBeeingListened != null) {
				conceptBeeingListened.getPropertyChangeSupport().removePropertyChangeListener(this);
			}
			if (getFlexoRole().getFlexoConceptType() != null) {
				conceptBeeingListened = getFlexoRole().getFlexoConceptType();
				conceptBeeingListened.getPropertyChangeSupport().addPropertyChangeListener(this);
			}
		}
		if (evt.getSource() instanceof FlexoConcept) {
			if (evt.getPropertyName().equals(FlexoConcept.FLEXO_PROPERTIES_KEY)
					|| evt.getPropertyName().equals(FlexoConcept.FLEXO_BEHAVIOURS_KEY)) {
				isNotifyingBindingPathChanged = true;
				getPropertyChangeSupport().firePropertyChange(BINDING_PATH_CHANGED, false, true);
				isNotifyingBindingPathChanged = false;
			}
		}
	}

	private boolean isNotifyingBindingPathChanged = false;

	@Override
	public boolean isNotifyingBindingPathChanged() {
		return isNotifyingBindingPathChanged;
	}

}
