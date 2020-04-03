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
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;

/**
 * Provides execution environment of the call of a super {@link CreationScheme} on a given {@link FlexoConceptInstance}
 * 
 * Note that this action can only be executed in the context of a more specialized {@link CreationScheme} execution
 * 
 * @author sylvain
 * 
 */
public class SuperCreationSchemeAction extends FlexoBehaviourAction<SuperCreationSchemeAction, CreationScheme, FlexoConceptInstance> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SuperCreationSchemeAction.class.getPackage().getName());

	/**
	 * Constructor to be used with a factory
	 * 
	 * @param actionFactory
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	public SuperCreationSchemeAction(SuperCreationSchemeActionFactory actionFactory, FlexoConceptInstance focusedObject,
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
	public SuperCreationSchemeAction(CreationScheme creationScheme, FlexoConceptInstance focusedObject,
			List<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(creationScheme, focusedObject, globalSelection, editor);
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
	public SuperCreationSchemeAction(CreationScheme creationScheme, FlexoConceptInstance focusedObject,
			List<VirtualModelInstanceObject> globalSelection, FlexoAction<?, ?, ?> ownerAction) {
		super(creationScheme, focusedObject, globalSelection, ownerAction);
	}

	@Override
	public SuperCreationSchemeActionFactory getActionFactory() {
		return (SuperCreationSchemeActionFactory) super.getActionFactory();
	}

	public final CreationScheme getApplicableCreationScheme() {
		return getApplicableFlexoBehaviour();
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		if (getFlexoConceptInstance() == null) {
			throw new InvalidParametersException("Cannot execute a null FlexoConceptInstance");
		}
		if (getFlexoConceptInstance().getFlexoConcept() == null) {
			throw new InvalidParametersException("Cannot execute a FlexoConceptInstance with null concept: " + getFlexoConceptInstance());
		}

		CreationScheme applicableCreationScheme = getApplicableCreationScheme();

		if (applicableCreationScheme != null) {

			if (applicableCreationScheme.getFlexoConcept() == null) {
				throw new InvalidParametersException(
						"Inconsistent data: ActionScheme is not defined in any FlexoConcept: " + applicableCreationScheme);
			}
			if (applicableCreationScheme.getFlexoConcept().isAssignableFrom(getFlexoConceptInstance().getFlexoConcept())) {
				if (applicableCreationScheme != null) {
					executeControlGraph();
				}
			}
			else {
				throw new InvalidParametersException("ActionScheme " + applicableCreationScheme + " is not a behaviour defined for "
						+ getFlexoConceptInstance().getFlexoConcept());
			}
		}
		else {
			throw new InvalidParametersException("Cannot execute a null CreationScheme for " + getFlexoConceptInstance());
		}

	}
}
