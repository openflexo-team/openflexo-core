/**
 * 
 * Copyright (c) 2015, Openflexo
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

package org.openflexo.foundation.fml.controlgraph;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.DeclareFlexoRole;
import org.openflexo.foundation.fml.editionaction.EditionAction;

/**
 * Convert FML behaviour model from 1.7.0 to 1.7.1
 * 
 * @author sylvain
 * 
 */
@Deprecated
public class FMLControlGraphConverter {

	public static void addToActions(FMLControlGraphOwner owner, String ownerContext, EditionAction anAction) {

		if (anAction instanceof DeclareFlexoRole) {
			DeclareFlexoRole declareFlexoRole = (DeclareFlexoRole) anAction;
			AssignationAction action = owner.getFMLModelFactory().newAssignationAction(declareFlexoRole.getObject());
			action.initializeDeserialization(owner.getFMLModelFactory());
			action.getAssignableAction().initializeDeserialization(owner.getFMLModelFactory());
			action.setAssignation(declareFlexoRole.getDeprecatedAssignation());
			anAction = action;
		}
		if (anAction instanceof AssignableAction && !(anAction instanceof AssignationAction)) {
			AssignableAction assignableAction = (AssignableAction) anAction;
			if (StringUtils.isNotEmpty(assignableAction.getDeprecatedVariableName())) {
				DeclarationAction action = owner.getFMLModelFactory().newDeclarationAction(assignableAction.getDeprecatedVariableName(),
						assignableAction);
				action.initializeDeserialization(owner.getFMLModelFactory());
				action.getAssignableAction().initializeDeserialization(owner.getFMLModelFactory());
				anAction = action;
			} else if (assignableAction.getDeprecatedAssignation() != null && assignableAction.getDeprecatedAssignation().isSet()) {
				AssignationAction action = owner.getFMLModelFactory().newAssignationAction(assignableAction);
				action.initializeDeserialization(owner.getFMLModelFactory());
				action.getAssignableAction().initializeDeserialization(owner.getFMLModelFactory());
				action.setAssignation(assignableAction.getDeprecatedAssignation());
				anAction = action;
			}
		}
		if (anAction instanceof AssignationAction && ((AssignationAction) anAction).getDeprecatedAssignation() != null
				&& ((AssignationAction) anAction).getDeprecatedAssignation().isSet()) {
			AssignableAction assignationAction = (AssignableAction) anAction;
			AssignationAction action = owner.getFMLModelFactory().newAssignationAction(((AssignationAction) anAction).getDeprecatedValue());
			action.initializeDeserialization(owner.getFMLModelFactory());
			action.getAssignableAction().initializeDeserialization(owner.getFMLModelFactory());
			action.setAssignation(assignationAction.getDeprecatedAssignation());
			anAction = action;
		}

		if (anAction instanceof EditionAction && anAction.getConditional() != null && anAction.getConditional().isSet()) {
			ConditionalAction action = owner.getFMLModelFactory().newConditionalAction();
			action.setCondition(anAction.getConditional());
			action.setThenControlGraph(anAction);
			anAction = action;
		}

		FMLControlGraph controlGraph = owner.getControlGraph(ownerContext);
		if (controlGraph == null) {
			// If control graph is null, action will be new new control graph
			owner.setControlGraph(anAction, ownerContext);
		} else {
			// Otherwise, sequentially append action
			controlGraph.initializeDeserialization(owner.getFMLModelFactory());
			controlGraph.sequentiallyAppend(anAction);
		}

	}

	public static void removeFromActions(FMLControlGraphOwner owner, String ownerContext, EditionAction anAction) {
		anAction.delete();
	}
}
