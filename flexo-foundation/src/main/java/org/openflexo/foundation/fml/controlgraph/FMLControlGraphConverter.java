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

		FMLControlGraph controlGraph = owner.getControlGraph(ownerContext);
		if (controlGraph == null) {
			// If control graph is null, action will be new new control graph
			owner.setControlGraph(anAction, ownerContext);
		} else {
			// Otherwise, sequentially append action
			controlGraph.sequentiallyAppend(anAction);
		}

	}

	public static void removeFromActions(FMLControlGraphOwner owner, String ownerContext, EditionAction anAction) {
		anAction.delete();
	}
}
