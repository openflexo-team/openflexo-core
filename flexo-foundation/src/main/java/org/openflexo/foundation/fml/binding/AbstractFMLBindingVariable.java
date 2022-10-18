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
import org.openflexo.foundation.fml.TechnologySpecificType;

/**
 * An abstract implementation of a {@link BindingVariable} infered in FML language
 * 
 * We provide here support for type listening.
 * 
 * Note: final classes using this abstract class MUST call {@link #typeMightHaveChanged()} after their own initialization
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractFMLBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(AbstractFMLBindingVariable.class.getPackage().getName());

	private Type lastKnownType = null;

	/**
	 * Constructor for an {@link AbstractFMLBindingVariable}
	 * 
	 * Type is not given here, as it is obtained from {@link #getType()} method
	 * 
	 * Note: final classes using this abstract class MUST call {@link #typeMightHaveChanged()} after their own initialization
	 * 
	 * @param variableName
	 * @param settable
	 */
	public AbstractFMLBindingVariable(String variableName, boolean settable) {
		super(variableName, Object.class, true);
	}

	@Override
	public void delete() {
		if (lastKnownType instanceof TechnologySpecificType) {
			((TechnologySpecificType)lastKnownType).getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		lastKnownType = null;
		super.delete();
	}

	/**
	 * Return accessed type for this {@link AbstractFMLBindingVariable}
	 * 
	 * @return
	 */
	@Override
	public abstract Type getType();

	/**
	 * Return type beeing last notified
	 * @return
	 */
	protected Type getLastKnownType() {
		return lastKnownType;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getType() && evt.getPropertyName().equals(TechnologySpecificType.TYPE_CHANGED)) {
			getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, null, getType());
		}
	}

	/**
	 * Internally called to notify that type of this {@link BindingVariable} might have changed. 
	 * Compare the new type to the last notified one, and notify when required.
	 * Also manage observation registering
	 */
	protected void typeMightHaveChanged() {
		if (lastKnownType != getType()) {
			if (lastKnownType instanceof TechnologySpecificType) {
				((TechnologySpecificType)lastKnownType).getPropertyChangeSupport().removePropertyChangeListener(this);
			}
			if (getType() instanceof TechnologySpecificType) {
				((TechnologySpecificType)getType()).getPropertyChangeSupport().addPropertyChangeListener(this);
			}
			getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, getType());
			lastKnownType = getType();
		}
	}
	

}
