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

import org.openflexo.foundation.fml.editionaction.DeclarationAction;

/**
 * BindingVariable associated to an {@link DeclarationAction}
 * 
 * @author sylvain
 * 
 */
public class DeclarationBindingVariable extends AbstractFMLBindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(DeclarationBindingVariable.class.getPackage().getName());

	private final DeclarationAction<?> action;

	public DeclarationBindingVariable(DeclarationAction<?> action) {
		super(action.getVariableName(), true);
		this.action = action;
		setCacheable(false);
		typeMightHaveChanged();
		if (action.getPropertyChangeSupport() != null) {
			action.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		if (action.getAssignableAction() != null && action.getAssignableAction().getPropertyChangeSupport() != null) {
			action.getAssignableAction().getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public void delete() {
		if (action.getAssignableAction() != null && action.getAssignableAction().getPropertyChangeSupport() != null) {
			action.getAssignableAction().getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		if (action != null && action.getPropertyChangeSupport() != null) {
			action.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public String getVariableName() {
		return getAction().getVariableName();
	}

	@Override
	public Type getType() {
		return getAction().getAssignableType();
	}

	public DeclarationAction<?> getAction() {
		return action;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == getAction()) {
			if (evt.getPropertyName().equals(DeclarationAction.VARIABLE_NAME_KEY)) {
				getPropertyChangeSupport().firePropertyChange(VARIABLE_NAME_PROPERTY, evt.getOldValue(), getVariableName());
			}
			else {
				typeMightHaveChanged();
			}
		}
		if (evt.getSource() == getAction().getAssignableAction()) {
			typeMightHaveChanged();
		}
	}

}
