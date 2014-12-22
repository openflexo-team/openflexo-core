/*
  * (c) Copyright 2014-2015 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;

import org.openflexo.antar.binding.BindingEvaluationContext;
import org.openflexo.antar.binding.BindingModel;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fmlrt.action.FlexoBehaviourAction;

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
public class FlexoBehaviourBindingModel extends ActionContainerBindingModel {

	private final FlexoBehaviour flexoBehaviour;

	private final FlexoBehaviourParametersBindingVariable parametersBindingVariable;
	private final FlexoBehaviourParametersDefinitionBindingVariable parametersDefinitionBindingVariable;

	public static final String PARAMETERS_PROPERTY = "parameters";
	public static final String PARAMETERS_DEFINITION_PROPERTY = "parametersDefinitions";

	public FlexoBehaviourBindingModel(FlexoBehaviour flexoBehaviour) {
		super(flexoBehaviour, flexoBehaviour.getFlexoConcept() != null ? flexoBehaviour.getFlexoConcept().getBindingModel() : null);

		this.flexoBehaviour = flexoBehaviour;

		parametersBindingVariable = new FlexoBehaviourParametersBindingVariable(flexoBehaviour);
		addToBindingVariables(parametersBindingVariable);

		parametersDefinitionBindingVariable = new FlexoBehaviourParametersDefinitionBindingVariable(flexoBehaviour);
		addToBindingVariables(parametersDefinitionBindingVariable);

	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
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
