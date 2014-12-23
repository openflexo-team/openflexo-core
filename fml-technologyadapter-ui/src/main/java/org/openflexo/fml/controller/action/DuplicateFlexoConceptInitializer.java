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

import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.action.DuplicateFlexoConcept;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;

public class DuplicateFlexoConceptInitializer extends ActionInitializer<DuplicateFlexoConcept, FlexoConcept, FMLObject> {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public DuplicateFlexoConceptInitializer(ControllerActionInitializer actionInitializer) {
		super(DuplicateFlexoConcept.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<DuplicateFlexoConcept> getDefaultInitializer() {
		return new FlexoActionInitializer<DuplicateFlexoConcept>() {
			@Override
			public boolean run(EventObject e, DuplicateFlexoConcept action) {
				String s = FlexoController.askForString(FlexoLocalization.localizedForKey("please_provide_new_name"));
				if (s == null) {
					return false;
				}
				action.newName = s;
				return true;
			}
		};
	}

	@Override
	protected FlexoActionFinalizer<DuplicateFlexoConcept> getDefaultFinalizer() {
		return new FlexoActionFinalizer<DuplicateFlexoConcept>() {
			@Override
			public boolean run(EventObject e, DuplicateFlexoConcept action) {
				getController().selectAndFocusObject(action.getNewFlexoConcept());
				return true;
			}
		};
	}

	@Override
	protected Icon getEnabledIcon() {
		return FMLIconLibrary.FLEXO_CONCEPT_ICON;
	}

}
