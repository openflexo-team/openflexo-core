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

import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.fml.AbstractInvariant;
import org.openflexo.foundation.fml.IterationInvariant;

/**
 * This is the {@link BindingModel} exposed by a {@link IterationInvariant}<br>
 * 
 * @author sylvain
 * 
 */
public class IterationInvariantBindingModel extends BindingModel {

	private IterationInvariantBindingVariable iteratorBindingVariable;

	private final IterationInvariant iterationInvariant;

	public IterationInvariantBindingModel(IterationInvariant invariant) {
		super(invariant.getFlexoConcept() != null ? invariant.getFlexoConcept().getBindingModel() : null);

		this.iterationInvariant = invariant;

		if (invariant.getPropertyChangeSupport() != null) {
			invariant.getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		updateIterator();
	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (iterationInvariant != null && iterationInvariant.getPropertyChangeSupport() != null) {
			iterationInvariant.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == iterationInvariant) {
			if (evt.getPropertyName().equals(AbstractInvariant.FLEXO_CONCEPT_KEY)) {
				setBaseBindingModel(
						iterationInvariant.getFlexoConcept() != null ? iterationInvariant.getFlexoConcept().getBindingModel() : null);
			}
			else if (evt.getPropertyName().equals(IterationInvariant.ITERATOR_NAME_KEY)
					|| evt.getPropertyName().equals(IterationInvariant.ITERATION_KEY)) {
				updateIterator();
			}
		}
	}

	public AbstractInvariant getConstraint() {
		return iterationInvariant;
	}

	public IterationInvariantBindingVariable getIteratorBindingVariable() {
		return iteratorBindingVariable;
	}

	private void updateIterator() {
		if (iteratorBindingVariable == null) {
			iteratorBindingVariable = new IterationInvariantBindingVariable(iterationInvariant);
			iteratorBindingVariable.setCacheable(false);
			addToBindingVariables(iteratorBindingVariable);
		}
		else {
			iteratorBindingVariable.setVariableName(iterationInvariant.getIteratorName());
			iteratorBindingVariable.setType(iterationInvariant.getIteratorType());
		}
	}
}
