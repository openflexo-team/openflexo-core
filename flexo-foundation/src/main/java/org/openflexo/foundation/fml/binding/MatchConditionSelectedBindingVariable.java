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
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.InitiateMatching;
import org.openflexo.foundation.fml.rt.editionaction.MatchCondition;

/**
 * BindingVariable associated to an {@link FetchRequestIterationAction} iterator
 * 
 * @author sylvain
 * 
 */
public class MatchConditionSelectedBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(MatchConditionSelectedBindingVariable.class.getPackage().getName());

	private final MatchCondition condition;
	private Type lastKnownType = null;

	public MatchConditionSelectedBindingVariable(MatchCondition condition) {
		super(FetchRequestCondition.SELECTED, condition.getAction() != null ? condition.getAction().getMatchedType() : Object.class, false);
		this.condition = condition;
		if (condition != null) {
			if (condition.getPropertyChangeSupport() != null) {
				condition.getPropertyChangeSupport().addPropertyChangeListener(this);
			}
			InitiateMatching action = condition.getAction();
			lastKnownType = action != null ? condition.getAction().getMatchedType() : FlexoConceptInstance.class;
			if (action != null && action.getPropertyChangeSupport() != null) {
				action.getPropertyChangeSupport().addPropertyChangeListener(this);
			}
		}
		setCacheable(false);
	}

	@Override
	public void delete() {
		if (condition != null) {
			if (condition.getPropertyChangeSupport() != null) {
				condition.getPropertyChangeSupport().addPropertyChangeListener(this);
			}
			InitiateMatching action = condition.getAction();
			if (action != null && action.getPropertyChangeSupport() != null) {
				action.getPropertyChangeSupport().addPropertyChangeListener(this);
			}
		}
		super.delete();
	}

	@Override
	public Type getType() {
		if (condition.getAction() != null) {
			return condition.getAction().getMatchedType();
		}
		return null;
	}

	public MatchCondition getCondition() {
		return condition;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == condition || evt.getSource() == condition.getAction()) {
			if (lastKnownType != getType()) {
				// System.out.println("Notify type changing");
				getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, lastKnownType, getType());
				lastKnownType = getType();
			}
		}
	}
}
