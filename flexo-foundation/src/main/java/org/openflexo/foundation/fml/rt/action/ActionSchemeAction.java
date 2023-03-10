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
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.InvalidParametersException;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;

/**
 * Provides execution environment of a {@link ActionScheme} on a given {@link FlexoConceptInstance} as a {@link FlexoAction}
 *
 * An {@link ActionSchemeAction} represents the execution (in the "instances" world) of an {@link ActionScheme}.<br>
 * To be used and executed on Openflexo platform, it is wrapped in a {@link FlexoAction}.<br>
 * 
 * @author sylvain
 */
public class ActionSchemeAction extends AbstractActionSchemeAction<ActionSchemeAction, ActionScheme, FlexoConceptInstance> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ActionSchemeAction.class.getPackage().getName());

	/**
	 * Constructor to be used with a factory
	 * 
	 * @param actionFactory
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	public ActionSchemeAction(ActionSchemeActionFactory actionFactory, FlexoConceptInstance focusedObject,
			List<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	/**
	 * Constructor to be used for creating a new action without factory
	 * 
	 * @param flexoBehaviour
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	public ActionSchemeAction(ActionScheme actionScheme, FlexoConceptInstance focusedObject,
			List<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(actionScheme, focusedObject, globalSelection, editor);
	}

	/**
	 * Constructor to be used for creating a new action as an action embedded in another one
	 * 
	 * @param flexoBehaviour
	 * @param focusedObject
	 * @param globalSelection
	 * @param ownerAction
	 *            Action in which action to be created will be embedded
	 */
	public ActionSchemeAction(ActionScheme actionScheme, FlexoConceptInstance focusedObject,
			List<VirtualModelInstanceObject> globalSelection, FlexoAction<?, ?, ?> ownerAction) {
		super(actionScheme, focusedObject, globalSelection, ownerAction);
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		if (getFlexoConceptInstance() == null) {
			throw new InvalidParametersException("Cannot execute a null FlexoConceptInstance");
		}
		if (getFlexoConceptInstance().getFlexoConcept() == null) {
			throw new InvalidParametersException("Cannot execute a FlexoConceptInstance with null concept: " + getFlexoConceptInstance());
		}

		ActionScheme applicableActionScheme = getApplicableActionScheme();

		if (applicableActionScheme != null) {

			if (applicableActionScheme.getFlexoConcept() == null) {
				throw new InvalidParametersException(
						"Inconsistent data: ActionScheme is not defined in any FlexoConcept: " + applicableActionScheme);
			}

			executeControlGraph();

			/*if (applicableActionScheme.getFlexoConcept().isAssignableFrom(getFlexoConceptInstance().getFlexoConcept())) {
				if (applicableActionScheme != null && applicableActionScheme.evaluateCondition(getFlexoConceptInstance())) {
					executeControlGraph();
				}
			}
			else if (applicableActionScheme.getFlexoConcept().isAssignableFrom(getVirtualModelInstance().getVirtualModel())) {
				System.out.println("On tente de rattrapper le coup pour executer " + applicableActionScheme.getSignature());
				System.out.println("On a besoin d'acceder a " + applicableActionScheme.getFlexoConcept());
				System.out.println("Mais on a FCI=" + getFlexoConceptInstance());
				System.out.println("Mais on a VMI=" + getVirtualModelInstance());
				if (applicableActionScheme != null && applicableActionScheme.evaluateCondition(getVirtualModelInstance())) {
					System.out.println("On execute bien le truc: ");
					prout = getVirtualModelInstance();
					System.out.println(applicableActionScheme.getFMLPrettyPrint());
					executeControlGraph();
					System.out.println("Et on retourne: " + returnedValue);
				}
			}
			else {
				System.out.println("On a un probleme ici pour executer " + applicableActionScheme.getSignature());
				System.out.println("On a besoin d'acceder a " + applicableActionScheme.getFlexoConcept());
				System.out.println("Mais on a FCI=" + getFlexoConceptInstance());
				System.out.println("Mais on a VMI=" + getVirtualModelInstance());
				throw new InvalidParametersException("ActionScheme " + applicableActionScheme + " is not a behaviour defined for "
						+ getFlexoConceptInstance().getFlexoConcept());
			}*/
		}
		else {

			// System.out.println("Pour le FCI: " + getFlexoConceptInstance());
			// System.out.println("Et le behaviour " + getFlexoBehaviour());
			// System.out.println("J'obtiens: " + applicableActionScheme);

			throw new InvalidParametersException("Cannot execute a null ActionScheme for " + getFlexoConceptInstance());
		}

	}
}
