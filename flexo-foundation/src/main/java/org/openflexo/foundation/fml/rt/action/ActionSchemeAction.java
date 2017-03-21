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

package org.openflexo.foundation.fml.rt.action;

import java.util.List;
import java.util.logging.Logger;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.AbstractActionScheme;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;

public class ActionSchemeAction extends FlexoBehaviourAction<ActionSchemeAction, AbstractActionScheme, FlexoConceptInstance> {

	private static final Logger logger = Logger.getLogger(ActionSchemeAction.class.getPackage().getName());

	private final ActionSchemeActionType actionType;

	public ActionSchemeAction(ActionSchemeActionType actionType, FlexoConceptInstance focusedObject,
			List<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		this.actionType = actionType;
	}

	public AbstractActionScheme getActionScheme() {
		if (actionType != null) {
			return actionType.getActionScheme();
		}
		return null;
	}

	/**
	 * Return the {@link FlexoConceptInstance} on which this {@link FlexoBehaviour} is applied.<br>
	 * 
	 * @return
	 */
	@Override
	public FlexoConceptInstance getFlexoConceptInstance() {
		if (actionType != null) {
			return actionType.getFlexoConceptInstance();
		}
		return null;
	}

	@Override
	public AbstractActionScheme getFlexoBehaviour() {
		return getActionScheme();
	}

	@Override
	protected void doAction(Object context) throws FlexoException {
		logger.fine("Perform action " + actionType);

		if (getActionScheme() != null && getActionScheme().evaluateCondition(actionType.getFlexoConceptInstance())) {
			executeControlGraph();
		}
	}

	@Override
	public AbstractVirtualModelInstance<?, ?> retrieveVirtualModelInstance() {
		if (getFlexoConceptInstance() instanceof VirtualModelInstance) {
			return (VirtualModelInstance) getFlexoConceptInstance();
		}
		if (getFlexoConceptInstance() != null) {
			return getFlexoConceptInstance().getVirtualModelInstance();
		}
		return null;
	}

	/*@Override
	public Object getValue(BindingVariable variable) {
		return super.getValue(variable);
	}*/

	/*@Override
	public void setValue(Object value, BindingVariable variable) {
		// Why not upper ?
		if (variable instanceof FlexoPropertyBindingVariable) {
			getFlexoConceptInstance().setFlexoActor(value, (FlexoProperty) ((FlexoPropertyBindingVariable) variable).getFlexoProperty());
			return;
		}
		super.setValue(value, variable);
	}*/

}
