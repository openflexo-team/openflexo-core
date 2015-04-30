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

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;

/**
 * This is the {@link BindingModel} exposed by a {@link FlexoBehaviour}<br>
 * This {@link BindingModel} is based on FlexoConcepts's (owner of this FlexoConcept) {@link BindingModel} if this owner is not null
 * 
 * Provides access to the parameters and parameterDefinitions<br>
 * 
 * Note that default {@link BindingEvaluationContext} corresponding to this {@link BindingModel} is a {@link FlexoBehaviourAction}
 * 
 * 
 * @author sylvain
 * 
 */
public class FlexoBehaviourBindingModel extends BindingModel {

	private final FlexoBehaviour flexoBehaviour;

	private final FlexoBehaviourParametersBindingVariable parametersBindingVariable;
	private final FlexoBehaviourParametersDefinitionBindingVariable parametersDefinitionBindingVariable;

	public static final String PARAMETERS_PROPERTY = "parameters";
	public static final String PARAMETERS_DEFINITION_PROPERTY = "parametersDefinitions";

	public FlexoBehaviourBindingModel(FlexoBehaviour flexoBehaviour) {
		super(flexoBehaviour.getFlexoConcept() != null ? flexoBehaviour.getFlexoConcept().getBindingModel() : null);

		this.flexoBehaviour = flexoBehaviour;

		parametersBindingVariable = new FlexoBehaviourParametersBindingVariable(flexoBehaviour);
		addToBindingVariables(parametersBindingVariable);

		parametersDefinitionBindingVariable = new FlexoBehaviourParametersDefinitionBindingVariable(flexoBehaviour);
		addToBindingVariables(parametersDefinitionBindingVariable);

		if (flexoBehaviour != null && flexoBehaviour.getPropertyChangeSupport() != null) {
			flexoBehaviour.getPropertyChangeSupport().addPropertyChangeListener(this);
		}

	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {

		if (flexoBehaviour != null && flexoBehaviour.getPropertyChangeSupport() != null) {
			flexoBehaviour.getPropertyChangeSupport().removePropertyChangeListener(this);
		}

		super.delete();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == flexoBehaviour) {
			if (evt.getPropertyName().equals(FlexoBehaviour.FLEXO_CONCEPT_KEY)) {
				// The FlexoBehaviour changes it's FlexoConcept
				setBaseBindingModel(flexoBehaviour.getFlexoConcept() != null ? flexoBehaviour.getFlexoConcept().getBindingModel() : null);
			}
		}
	}

	public FlexoBehaviour getFlexoBehaviour() {
		return flexoBehaviour;
	}

	public FlexoBehaviourParametersBindingVariable getParametersBindingVariable() {
		return parametersBindingVariable;
	}

	public FlexoBehaviourParametersDefinitionBindingVariable getParametersDefinitionBindingVariable() {
		return parametersDefinitionBindingVariable;
	}
}
