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
import org.openflexo.foundation.fml.controlgraph.AbstractIterationAction;
import org.openflexo.foundation.fml.controlgraph.IterationAction;

/**
 * BindingVariable associated to a {@link IterationAction} iterator
 * 
 * @author sylvain
 * 
 */
public class IterationActionBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(IterationActionBindingVariable.class.getPackage().getName());

	private final AbstractIterationAction action;
	private Type lastKnownType = null;

	public IterationActionBindingVariable(AbstractIterationAction action) {
		super(action.getIteratorName(), action.getItemType(), true);
		this.action = action;
		lastKnownType = action.getItemType();
		if (action.getPropertyChangeSupport() != null) {
			action.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		if (action instanceof IterationAction && ((IterationAction) action).getIterationAction() != null
				&& ((IterationAction) action).getIterationAction().getPropertyChangeSupport() != null) {
			((IterationAction) action).getIterationAction().getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		/*if (action.getIteratorName().equals("taskType")) {
			if (action instanceof IterationAction) {
				ExpressionActionImpl expression = (ExpressionActionImpl) (((IterationAction) action).getIterationAction());
				System.out.println("expression=" + expression.getExpression());
				if (expression.getExpression().toString().equals("taskTypes")) {
					System.out.println("OK j'ai ma variable " + this + " iteration=" + action);
				}
			}
		}*/
	}

	/*@Override
	public String toString() {
		return "IterationBindingVariable " + getVariableName() + "/" + TypeUtils.simpleRepresentation(getType()) + " ["
				+ Integer.toHexString(hashCode()) + "]";
	}*/

	@Override
	public void delete() {
		if (action instanceof IterationAction && ((IterationAction) action).getIterationAction().getPropertyChangeSupport() != null) {
			((IterationAction) action).getIterationAction().getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		if (action != null && action.getPropertyChangeSupport() != null) {
			action.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public String getVariableName() {
		return getAction().getIteratorName();
	}

	@Override
	public Type getType() {
		if (getAction() != null) {
			return getAction().getItemType();
		}
		return null;
	}

	public AbstractIterationAction getAction() {
		return action;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// if (debug) {
		// System.out.println("****** propertyChange " + evt.getPropertyName() + " source=" + evt.getSource());
		// }
		if (evt.getSource() == getAction()) {
			if (evt.getPropertyName().equals(IterationAction.ITERATOR_NAME_KEY)) {
				// System.out.println("Notify name changing for " + getFlexoRole() + " new=" + getVariableName());
				getPropertyChangeSupport().firePropertyChange(VARIABLE_NAME_PROPERTY, evt.getOldValue(), getVariableName());
			}
			iteratorTypeMightHaveChanged();
			if (evt.getPropertyName().equals(IterationAction.ITERATION_CONTROL_GRAPH_KEY)) {
				if (action instanceof IterationAction && ((IterationAction) action).getIterationAction() != null
						&& ((IterationAction) action).getIterationAction().getPropertyChangeSupport() != null) {
					// if (debug)
					// System.out
					// .println("****** Also listening " + ((IterationAction) action).getIterationAction().getFMLRepresentation());
					((IterationAction) action).getIterationAction().getPropertyChangeSupport().addPropertyChangeListener(this);
				}
				iteratorTypeMightHaveChanged();
			}
		}
		if ((getAction() instanceof IterationAction) && (evt.getSource() == ((IterationAction) getAction()).getIterationAction())) {
			iteratorTypeMightHaveChanged();
		}
	}

	private void iteratorTypeMightHaveChanged() {
		if (lastKnownType != getType()) {
			// if (debug) {
			// System.out.println("Iterator type changed for " + getType());
			// }
			getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, getType());
			lastKnownType = getType();
		}
	}

}
