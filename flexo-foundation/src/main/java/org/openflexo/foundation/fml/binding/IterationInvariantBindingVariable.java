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
import org.openflexo.foundation.fml.AbstractInvariant;
import org.openflexo.foundation.fml.IterationInvariant;

/**
 * {@link BindingVariable} associated to the iterator variable of a {@link IterationInvariant}
 * 
 * @author sylvain
 * 
 */
public class IterationInvariantBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(IterationInvariantBindingVariable.class.getPackage().getName());

	private final IterationInvariant invariant;
	private Type lastKnownType = null;

	public IterationInvariantBindingVariable(IterationInvariant invariant) {
		super(invariant.getIteratorName(), invariant.getIteratorType() != null ? invariant.getIteratorType() : Object.class, false);
		this.invariant = invariant;
		if (invariant.getPropertyChangeSupport() != null) {
			invariant.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		lastKnownType = ((invariant != null && invariant.getIteratorType() != null) ? invariant.getIteratorType() : Object.class);
		setCacheable(false);
	}

	@Override
	public void delete() {
		if (invariant != null) {
			if (invariant.getPropertyChangeSupport() != null) {
				invariant.getPropertyChangeSupport().addPropertyChangeListener(this);
			}
		}
		super.delete();
	}

	@Override
	public Type getType() {
		if (invariant != null) {
			return invariant.getIteratorType();
		}
		return null;
	}

	public AbstractInvariant getConstraint() {
		return invariant;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == invariant) {
			if (lastKnownType != getType()) {
				// System.out.println("Notify type changing");
				getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, getType());
				lastKnownType = getType();
			}
		}
	}
}
