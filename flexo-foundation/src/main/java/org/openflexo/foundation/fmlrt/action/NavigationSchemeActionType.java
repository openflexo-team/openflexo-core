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
package org.openflexo.foundation.fmlrt.action;

import java.util.Vector;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.NavigationScheme;
import org.openflexo.foundation.fmlrt.FlexoConceptInstance;
import org.openflexo.foundation.fmlrt.VirtualModelInstanceObject;
import org.openflexo.localization.LocalizedDelegate;

public class NavigationSchemeActionType extends FlexoActionType<NavigationSchemeAction, FlexoConceptInstance, VirtualModelInstanceObject> {

	private final NavigationScheme navigationScheme;
	private final FlexoConceptInstance flexoConceptInstance;

	public NavigationSchemeActionType(NavigationScheme navigationScheme, FlexoConceptInstance flexoConceptInstance) {
		super(navigationScheme.getName(), FlexoActionType.defaultGroup, FlexoActionType.NORMAL_ACTION_TYPE);
		this.navigationScheme = navigationScheme;
		this.flexoConceptInstance = flexoConceptInstance;
	}

	@Override
	public LocalizedDelegate getLocalizer() {
		return navigationScheme.getLocalizedDictionary();
	}

	@Override
	public boolean isEnabled(FlexoConceptInstance object, Vector<VirtualModelInstanceObject> globalSelection) {
		return isEnabledForSelection(object, globalSelection);
	}

	@Override
	public boolean isEnabledForSelection(FlexoConceptInstance object, Vector<VirtualModelInstanceObject> globalSelection) {
		return navigationScheme.evaluateCondition(flexoConceptInstance);
	}

	@Override
	public boolean isVisibleForSelection(FlexoConceptInstance object, Vector<VirtualModelInstanceObject> globalSelection) {
		return true;
	}

	@Override
	public NavigationSchemeAction makeNewAction(FlexoConceptInstance focusedObject, Vector<VirtualModelInstanceObject> globalSelection,
			FlexoEditor editor) {
		return new NavigationSchemeAction(this, focusedObject, globalSelection, editor);
	}

	public NavigationScheme getNavigationScheme() {
		return navigationScheme;
	}

	public FlexoConceptInstance getFlexoConceptInstance() {
		return flexoConceptInstance;
	}

}
