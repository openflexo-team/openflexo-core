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

import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.fml.ActionContainer;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;

/**
 * This is the {@link BindingModel} exposed by a {@link FlexoBehaviour}<br>
 * This {@link BindingModel} is based on FlexoConcepts's (owner of this FlexoConcept) {@link BindingModel} if this owner is not null
 * 
 * Provides access to the parameters and parameterDefinitions<br>
 * 
 * Note that default {@link RunTimeEvaluationContext} corresponding to this {@link BindingModel} is a {@link FlexoBehaviourAction}
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

		// declarationBindingVariables = new HashMap<AssignableAction<?, ?>, DeclarationBindingVariable>();

		if (actionContainer != null && actionContainer.getPropertyChangeSupport() != null) {
			actionContainer.getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		// updateAssignationVariables();

	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (actionContainer != null && actionContainer.getPropertyChangeSupport() != null) {
			actionContainer.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		/*for (AssignableAction<?, ?> a : declarationBindingVariables.keySet()) {
			a.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		
		declarationBindingVariables.clear();*/

		super.delete();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == actionContainer) {
			if (evt.getPropertyName().equals(ActionContainer.ACTIONS_KEY)) {
				// Actions were touched
				// updateAssignationVariables();
			}
		}
		else if (evt.getSource() instanceof AssignableAction<?>) {
			// Something has changed in any of contained actions
			// updateAssignationVariables();
		}
	}

	public ActionContainer getActionContainer() {
		return actionContainer;
	}

	// private final Map<AssignableAction<?>, DeclarationBindingVariable> declarationBindingVariables;

	/*private void updateAssignationVariables() {
	
		List<AssignableAction<?, ?>> assignationToBeDeleted = new ArrayList<AssignableAction<?, ?>>(declarationBindingVariables.keySet());
	
		for (final EditionAction a : actionContainer.getActions()) {
			if (a instanceof AssignableAction && ((AssignableAction) a).getIsVariableDeclaration()) {
				if (assignationToBeDeleted.contains(a)) {
					assignationToBeDeleted.remove(a);
				} else {
					DeclarationBindingVariable bv = new DeclarationBindingVariable((AssignableAction<?, ?>) a);
					addToBindingVariables(bv);
					declarationBindingVariables.put((AssignableAction<?, ?>) a, bv);
					a.getPropertyChangeSupport().addPropertyChangeListener(this);
				}
			}
		}
	
		for (AssignableAction<?, ?> a : assignationToBeDeleted) {
			DeclarationBindingVariable bvToRemove = declarationBindingVariables.get(a);
			removeFromBindingVariables(bvToRemove);
			declarationBindingVariables.remove(a);
			bvToRemove.delete();
			a.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
	
	}*/

}
