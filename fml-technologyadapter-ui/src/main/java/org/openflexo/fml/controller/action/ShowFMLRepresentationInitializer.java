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

import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.action.ShowFMLRepresentation;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;

public class ShowFMLRepresentationInitializer extends ActionInitializer<ShowFMLRepresentation, FMLObject, FMLObject> {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public static Resource SHOW_FML_REPRESENTATION_DIALOG_FIB = ResourceLocator
			.locateResource("Fib/Dialog/ShowFMLRepresentationDialog.fib");

	public ShowFMLRepresentationInitializer(ControllerActionInitializer actionInitializer) {
		super(ShowFMLRepresentation.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<ShowFMLRepresentation> getDefaultInitializer() {
		return new FlexoActionInitializer<ShowFMLRepresentation>() {
			@Override
			public boolean run(EventObject e, ShowFMLRepresentation action) {
				return instanciateAndShowDialog(action, SHOW_FML_REPRESENTATION_DIALOG_FIB);
			}

		};
	}

}
