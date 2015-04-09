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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.foundation.fml.editionaction.AddToListAction;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.DeclareFlexoRole;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.ExecutionAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;

/**
 * Convert FML behaviour model from 1.7.0 to 1.7.1<br>
 * 
 * Note: will be removed from 1.7.2 version
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

		if (anAction instanceof AssignationAction && StringUtils.isNotEmpty(((AssignationAction) anAction).getDeprecatedVariableName())
				&& ((AssignationAction) anAction).getDeprecatedValue() != null
				&& ((AssignationAction) anAction).getDeprecatedValue().isSet()) {
			AssignationAction assignationAction = (AssignationAction) anAction;
			DeclarationAction action = owner.getFMLModelFactory().newDeclarationAction(assignationAction.getDeprecatedVariableName(),
					assignationAction.getDeprecatedValue());
			action.initializeDeserialization(owner.getFMLModelFactory());
			action.getAssignableAction().initializeDeserialization(owner.getFMLModelFactory());
			anAction = action;
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

		if (anAction instanceof IterationAction && ((IterationAction) anAction).getIteration() != null
				&& ((IterationAction) anAction).getIteration().isSet()) {

			IterationAction iterationAction = (IterationAction) anAction;
			ExpressionAction<? extends List<?>> iterationAsExpression = owner.getFMLModelFactory().newExpressionAction(
					iterationAction.getIteration());
			iterationAsExpression.initializeDeserialization(owner.getFMLModelFactory());
			iterationAction.setIterationAction(iterationAsExpression);
			iterationAction.setIteration(null);
		}

		if (anAction instanceof AddToListAction && ((AddToListAction<?>) anAction).getValue() != null
				&& ((AddToListAction<?>) anAction).getValue().isSet()) {

			AddToListAction<?> addToListAction = (AddToListAction<?>) anAction;
			ExpressionAction<?> valueAsExpression = owner.getFMLModelFactory().newExpressionAction(addToListAction.getValue());
			valueAsExpression.initializeDeserialization(owner.getFMLModelFactory());
			addToListAction.setAssignableAction((ExpressionAction) valueAsExpression);
			addToListAction.setValue(null);
		}

		if (anAction instanceof ExecutionAction && ((ExecutionAction) anAction).getExecution() != null
				&& ((ExecutionAction) anAction).getExecution().isSet()) {

			ExecutionAction executionAction = (ExecutionAction) anAction;
			ExpressionAction<? extends List<?>> executionAsExpression = owner.getFMLModelFactory().newExpressionAction(
					executionAction.getExecution());
			executionAsExpression.initializeDeserialization(owner.getFMLModelFactory());
			anAction = executionAsExpression;
		}

		if (anAction instanceof EditionAction && anAction.getConditional() != null && anAction.getConditional().isSet()) {
			ConditionalAction action = owner.getFMLModelFactory().newConditionalAction();
			action.initializeDeserialization(owner.getFMLModelFactory());
			action.setCondition(anAction.getConditional());
			action.setThenControlGraph(anAction);
			anAction = action;
		}

		// Convert any reference to deprecated FetchRequestIterationAction to IterationAction
		if (anAction instanceof FetchRequestIterationAction) {

			FetchRequestIterationAction fetchRequestIteration = (FetchRequestIterationAction) anAction;
			FetchRequest<?, ?> fetchRequest = fetchRequestIteration.getFetchRequest();
			IterationAction action = owner.getFMLModelFactory().newIterationAction();
			action.initializeDeserialization(owner.getFMLModelFactory());
			action.setIteratorName(fetchRequestIteration.getIteratorName());
			action.setIterationAction(fetchRequest);

			if (fetchRequestIteration.getControlGraph() != null) {

				FMLControlGraph contained = fetchRequestIteration.getControlGraph();
				fetchRequestIteration.setControlGraph(null);
				action.setControlGraph(contained);
			}

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
