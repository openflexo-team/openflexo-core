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
package org.openflexo.rm;

import java.io.IOException;

import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.task.Progress;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.task.FlexoApplicationTask;

/**
 * A task used to load a Flexo module
 * 
 * @author sylvain
 *
 */
public class RefreshResourceCenterTask extends FlexoApplicationTask {
	/**
	 * 
	 */
	private final FlexoResourceCenterService rcService;
	private final FlexoResourceCenter<?> resourceCenter;

	public RefreshResourceCenterTask(FlexoResourceCenterService rcService, FlexoResourceCenter<?> resourceCenter) {
		super(FlexoLocalization.localizedForKey("refreshing_resource_center") + " " + resourceCenter.toString(), rcService);
		this.rcService = rcService;
		this.resourceCenter = resourceCenter;
	}

	@Override
	public void performTask() {

		Progress.setExpectedProgressSteps(getServiceManager().getTechnologyAdapterService().getTechnologyAdapters().size() + 2);
		try {
			resourceCenter.update();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean isCancellable() {
		return true;
	}
}