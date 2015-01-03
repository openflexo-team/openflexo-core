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
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.antar.binding.BindingEvaluationContext;
import org.openflexo.antar.binding.BindingModel;
import org.openflexo.foundation.fml.ActionContainer;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
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
@Deprecated
public abstract class ActionContainerBindingModel extends BindingModel implements PropertyChangeListener {

	private final ActionContainer actionContainer;

	protected ActionContainerBindingModel(ActionContainer actionContainer, BindingModel baseBindingModel) {
		super(baseBindingModel);

		this.actionContainer = actionContainer;

		assignationBindingVariables = new HashMap<AssignableAction<?, ?>, AssignationBindingVariable>();

		if (actionContainer != null && actionContainer.getPropertyChangeSupport() != null) {
			actionContainer.getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		updateAssignationVariables();

	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (actionContainer != null && actionContainer.getPropertyChangeSupport() != null) {
			actionContainer.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		for (AssignableAction<?, ?> a : assignationBindingVariables.keySet()) {
			a.getPropertyChangeSupport().removePropertyChangeListener(this);
		}

		assignationBindingVariables.clear();

		super.delete();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == actionContainer) {
			if (evt.getPropertyName().equals(ActionContainer.ACTIONS_KEY)) {
				// Actions were touched
				updateAssignationVariables();
			}
		} else if (evt.getSource() instanceof AssignableAction<?, ?>) {
			// Something has changed in any of contained actions
			updateAssignationVariables();
		}
	}

	public ActionContainer getActionContainer() {
		return actionContainer;
	}

	private final Map<AssignableAction<?, ?>, AssignationBindingVariable> assignationBindingVariables;

	private void updateAssignationVariables() {

		List<AssignableAction<?, ?>> assignationToBeDeleted = new ArrayList<AssignableAction<?, ?>>(assignationBindingVariables.keySet());

		for (final EditionAction<?, ?> a : actionContainer.getActions()) {
			if (a instanceof AssignableAction && ((AssignableAction) a).getIsVariableDeclaration()) {
				if (assignationToBeDeleted.contains(a)) {
					assignationToBeDeleted.remove(a);
				} else {
					AssignationBindingVariable bv = new AssignationBindingVariable((AssignableAction<?, ?>) a);
					addToBindingVariables(bv);
					assignationBindingVariables.put((AssignableAction<?, ?>) a, bv);
					a.getPropertyChangeSupport().addPropertyChangeListener(this);
				}
			}
		}

		for (AssignableAction<?, ?> a : assignationToBeDeleted) {
			AssignationBindingVariable bvToRemove = assignationBindingVariables.get(a);
			removeFromBindingVariables(bvToRemove);
			assignationBindingVariables.remove(a);
			bvToRemove.delete();
			a.getPropertyChangeSupport().removePropertyChangeListener(this);
		}

	}

}
