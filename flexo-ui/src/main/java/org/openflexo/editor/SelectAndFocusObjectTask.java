/*
 * (c) Copyright 2014-2015 Openflexo
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
package org.openflexo.editor;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.task.Progress;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.task.FlexoApplicationTask;
import org.openflexo.view.controller.FlexoController;

/**
 * A task used to select and focus an object
 * 
 * @author sylvain
 *
 */
public class SelectAndFocusObjectTask extends FlexoApplicationTask {

	private final FlexoController controller;
	private final FlexoObject objectToFocusOn;

	public SelectAndFocusObjectTask(FlexoController controller, FlexoObject objectToFocusOn) {
		super(FlexoLocalization.localizedForKey("opening_module_view_for_object") + " " + objectToFocusOn, controller
				.getApplicationContext().getModuleLoader());
		this.controller = controller;
		this.objectToFocusOn = objectToFocusOn;
	}

	@Override
	public void performTask() {
		Progress.setExpectedProgressSteps(10);
		controller.selectAndFocusObject(objectToFocusOn);
	}

	@Override
	public boolean isCancellable() {
		return true;
	}
}