/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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
import org.openflexo.foundation.fml.FlexoConceptInstanceType;

/**
 * A {@link BindingVariable} implementation with a given {@link FlexoConcept} as type
 * 
 * @author sylvain
 *
 */
public class FlexoConceptBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(FlexoConceptBindingVariable.class.getPackage().getName());

	private final FlexoConcept flexoConcept;

	public FlexoConceptBindingVariable(String variableName, FlexoConcept anFlexoConcept) {
		super(variableName, FlexoConceptInstanceType.getFlexoConceptInstanceType(anFlexoConcept));
		this.flexoConcept = anFlexoConcept;
		if (flexoConcept != null && flexoConcept.getPropertyChangeSupport() != null) {
			flexoConcept.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public void delete() {
		if (flexoConcept != null && flexoConcept.getPropertyChangeSupport() != null) {
			flexoConcept.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public Type getType() {
		return FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept);
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return flexoConcept.getDescription();
	}

	public FlexoConcept getFlexoConcept() {
		return flexoConcept;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
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
