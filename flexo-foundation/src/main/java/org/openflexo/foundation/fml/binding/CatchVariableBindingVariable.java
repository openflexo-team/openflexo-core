/**
 * 
 * Copyright (c) 2026, Openflexo
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

import org.openflexo.foundation.fml.controlgraph.CatchClause;

/**
 * The variable of a {@link CatchClause}, typed by the type it catches, visible in its block only (CORE-F-11 phase 7)
 */
public class CatchVariableBindingVariable extends AbstractFMLBindingVariable implements PropertyChangeListener {

	private final CatchClause clause;

	public CatchVariableBindingVariable(CatchClause clause) {
		super(clause.getVariableName(), true);
		this.clause = clause;
		typeMightHaveChanged();
		if (clause.getPropertyChangeSupport() != null) {
			clause.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public void delete() {
		if (clause.getPropertyChangeSupport() != null) {
			clause.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	public CatchClause getClause() {
		return clause;
	}

	@Override
	public String getVariableName() {
		return clause.getVariableName();
	}

	@Override
	public Type getType() {
		return clause.getCatchType();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == clause) {
			if (CatchClause.VARIABLE_NAME_KEY.equals(evt.getPropertyName())) {
				getPropertyChangeSupport().firePropertyChange(VARIABLE_NAME_PROPERTY, evt.getOldValue(), getVariableName());
			}
			if (CatchClause.CATCH_TYPE_KEY.equals(evt.getPropertyName())) {
				typeMightHaveChanged();
			}
		}
	}
}
