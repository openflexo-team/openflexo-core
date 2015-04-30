/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo;

import java.io.File;

import org.openflexo.br.BugReportService;
import org.openflexo.drm.DocResourceManager;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.DefaultTechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.InformationSpace;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.utils.ProjectLoadingHandler;
import org.openflexo.prefs.PreferencesService;
import org.openflexo.rm.ResourceConsistencyService;
import org.openflexo.view.controller.DefaultTechnologyAdapterControllerService;
import org.openflexo.view.controller.FlexoServerInstanceManager;
import org.openflexo.view.controller.FullInteractiveProjectLoadingHandler;
import org.openflexo.view.controller.InteractiveFlexoEditor;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

public class InteractiveApplicationContext extends ApplicationContext {
	@Override
	public FlexoEditor makeFlexoEditor(FlexoProject project, FlexoServiceManager sm) {
		return new InteractiveFlexoEditor(this, project);
	}

	@Override
	protected FlexoEditor createApplicationEditor() {
		return new InteractiveFlexoEditor(this, null);
	}

	@Override
	public ProjectLoadingHandler getProjectLoadingHandler(File projectDirectory) {
		/*if (UserType.isCustomerRelease() || UserType.isAnalystRelease()) {
			return new BasicInteractiveProjectLoadingHandler(projectDirectory);
		} else {*/
		return new FullInteractiveProjectLoadingHandler(projectDirectory);
		// }
	}

	@Override
	protected TechnologyAdapterService createTechnologyAdapterService(FlexoResourceCenterService resourceCenterService) {
		return DefaultTechnologyAdapterService.getNewInstance(resourceCenterService);
	}

	@Override
	protected TechnologyAdapterControllerService createTechnologyAdapterControllerService() {
		return DefaultTechnologyAdapterControllerService.getNewInstance();
	}

	@Override
	protected ViewPointLibrary createViewPointLibraryService() {
		return new ViewPointLibrary();
	}

	@Override
	protected InformationSpace createInformationSpace() {
		return new InformationSpace();
	}

	@Override
	protected BugReportService createBugReportService() {
		return new BugReportService();
	}

	@Override
	protected PreferencesService createPreferencesService() {
		return new PreferencesService();
	}

	@Override
	protected DocResourceManager createDocResourceManager() {
		return new DocResourceManager();
	}

	@Override
	protected FlexoServerInstanceManager createFlexoServerInstanceManager() {
		return new FlexoServerInstanceManager();
	}

	@Override
	protected ResourceConsistencyService createResourceConsistencyService() {
		return new ResourceConsistencyService();
	}
}
