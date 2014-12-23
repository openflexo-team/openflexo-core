/*
 * (c) Copyright 2010-2011 AgileBirds
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
package org.openflexo.foundation.fml.rt.action;

import java.util.Vector;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.action.ActionGroup;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;
import org.openflexo.localization.LocalizedDelegate;

public class DeletionSchemeActionType extends FlexoActionType<DeletionSchemeAction, FlexoConceptInstance, VirtualModelInstanceObject> {

	private final DeletionScheme deletionScheme;
	private final FlexoConceptInstance flexoConceptInstance;

	public DeletionSchemeActionType(DeletionScheme deletionScheme, FlexoConceptInstance flexoConceptInstance) {
		super(deletionScheme.getLabel(), FlexoActionType.defaultGroup, FlexoActionType.NORMAL_ACTION_TYPE);
		this.deletionScheme = deletionScheme;
		this.flexoConceptInstance = flexoConceptInstance;
	}

	@Override
	public LocalizedDelegate getLocalizer() {
		return deletionScheme.getLocalizedDictionary();
	}

	@Override
	public boolean isEnabled(FlexoConceptInstance object, Vector<VirtualModelInstanceObject> globalSelection) {
		return isEnabledForSelection(object, globalSelection);
	}

	@Override
	public boolean isEnabledForSelection(FlexoConceptInstance object, Vector<VirtualModelInstanceObject> globalSelection) {
		return deletionScheme.evaluateCondition(flexoConceptInstance);
	}

	@Override
	public boolean isVisibleForSelection(FlexoConceptInstance object, Vector<VirtualModelInstanceObject> globalSelection) {
		return true;
	}

	@Override
	public DeletionSchemeAction makeNewAction(FlexoConceptInstance focusedObject, Vector<VirtualModelInstanceObject> globalSelection,
			FlexoEditor editor) {
		return new DeletionSchemeAction(this, focusedObject, globalSelection, editor);
	}

	public DeletionScheme getDeletionScheme() {
		return deletionScheme;
	}

	public FlexoConceptInstance getFlexoConceptInstance() {
		return flexoConceptInstance;
	}

	@Override
	public ActionGroup getActionGroup() {
		return FlexoActionType.editGroup;
	}

	@Override
	public int getActionCategory() {
		return DELETE_ACTION_TYPE;
	}
}
