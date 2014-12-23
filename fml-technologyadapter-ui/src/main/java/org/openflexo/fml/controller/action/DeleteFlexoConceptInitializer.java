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
package org.openflexo.fml.controller.action;

import java.util.EventObject;
import java.util.logging.Logger;

import javax.swing.Icon;

import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.ViewPointObject;
import org.openflexo.foundation.fml.action.DeleteFlexoConcept;
import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;

public class DeleteFlexoConceptInitializer extends ActionInitializer<DeleteFlexoConcept, FlexoConcept, ViewPointObject> {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public DeleteFlexoConceptInitializer(ControllerActionInitializer actionInitializer) {
		super(DeleteFlexoConcept.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<DeleteFlexoConcept> getDefaultInitializer() {
		return new FlexoActionInitializer<DeleteFlexoConcept>() {
			@Override
			public boolean run(EventObject e, DeleteFlexoConcept action) {
				return FlexoController.confirm(FlexoLocalization.localizedForKey("would_you_really_like_to_delete_this_flexo_concept"));
			}
		};
	}

	@Override
	protected Icon getEnabledIcon() {
		return IconLibrary.DELETE_ICON;
	}

}
