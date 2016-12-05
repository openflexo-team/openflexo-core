/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.rm;

import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.task.FlexoApplicationTask;

/**
 * A task used to activate a {@link BugReportService}
 * 
 * @author sylvain
 *
 */
public class ActivateTechnologyAdapterTask extends FlexoApplicationTask {

	private final TechnologyAdapterService taService;
	private final TechnologyAdapter technologyAdapter;

	public ActivateTechnologyAdapterTask(TechnologyAdapterService taService, TechnologyAdapter technologyAdapter) {
		super(FlexoLocalization.getMainLocalizer().localizedForKey("activate_technology") + " " + technologyAdapter.getName(),
				taService.getServiceManager());
		this.taService = taService;
		this.technologyAdapter = technologyAdapter;

		for (FlexoTask task : getServiceManager().getTaskManager().getScheduledTasks()) {
			if (task instanceof AddResourceCenterTask) {
				addToDependantTasks(task);
			}
		}
	}

	@Override
	public void performTask() {

		Progress.setExpectedProgressSteps(getServiceManager().getResourceCenterService().getResourceCenters().size() + 2);

		technologyAdapter.activate();
		taService.getServiceManager().notify(taService,
				taService.getServiceManager().new TechnologyAdapterHasBeenActivated(technologyAdapter));

	}

	public TechnologyAdapter getTechnologyAdapter() {
		return technologyAdapter;
	}

	@Override
	public boolean isCancellable() {
		return true;
	}

	@Override
	protected synchronized void finishedExecution() {
		// TODO Auto-generated method stub
		super.finishedExecution();
	}
}
