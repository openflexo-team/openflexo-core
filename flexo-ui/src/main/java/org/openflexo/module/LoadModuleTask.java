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
package org.openflexo.module;

import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.Progress;
import org.openflexo.localization.FlexoLocalization;

/**
 * A task used to load a Flexo module
 * 
 * @author sylvain
 *
 */
public class LoadModuleTask extends FlexoTask {
	/**
	 * 
	 */
	private final ModuleLoader moduleLoader;
	private final Module module;

	LoadModuleTask(ModuleLoader moduleLoader, Module module) {
		super(FlexoLocalization.localizedForKey("loading_module") + " " + module.getLocalizedName());
		this.moduleLoader = moduleLoader;
		this.module = module;
	}

	@Override
	public void performTask() {
		try {
			Progress.setExpectedProgressSteps(100);
			this.moduleLoader.performSwitchToModule(module);
		} catch (ModuleLoadingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean isCancellable() {
		return false;
	}
}