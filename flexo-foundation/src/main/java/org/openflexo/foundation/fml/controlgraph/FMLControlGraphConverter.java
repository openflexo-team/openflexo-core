package org.openflexo.foundation.fml.controlgraph;

import org.openflexo.foundation.fml.editionaction.EditionAction;

/**
 * Convert FML behaviour model from 1.7.0 to 1.7.1
 * 
 * @author sylvain
 *
 */
@Deprecated
public class FMLControlGraphConverter {

	public static void addToActions(FMLControlGraphOwner owner, String ownerContext, EditionAction<?, ?> anAction) {
		FMLControlGraph controlGraph = owner.getControlGraph(ownerContext);
		if (controlGraph == null) {
			// If control graph is null, action will be new new control graph
			owner.setControlGraph(anAction, ownerContext);
		} else {
			// Otherwise, sequentially append action
			controlGraph.sequentiallyAppend(anAction);
		}

	}

	public static void removeFromActions(FMLControlGraphOwner owner, String ownerContext, EditionAction<?, ?> anAction) {
		anAction.delete();
	}
}
