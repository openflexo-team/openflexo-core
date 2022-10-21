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

package org.openflexo.gina.test;

import java.io.File;

import org.openflexo.ApplicationContext;
import org.openflexo.br.BugReportService;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.localization.LocalizationService;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.DefaultTechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.test.OpenflexoTestCase.TestProjectLoader;
import org.openflexo.foundation.utils.ProjectLoadingHandler;
import org.openflexo.prefs.PreferencesService;
import org.openflexo.rm.ActivateTechnologyAdapterTask;
import org.openflexo.rm.DisactivateTechnologyAdapterTask;
import org.openflexo.rm.ResourceConsistencyService;
import org.openflexo.view.controller.DefaultTechnologyAdapterControllerService;
import org.openflexo.view.controller.FlexoServerInstanceManager;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Test purposes: implements an ApplicationContext
 * 
 * @author sylvain
 * 
 */
public class TestApplicationContext extends ApplicationContext {

	// protected static DirectoryResourceCenter resourceCenter;

	// Unused private static final String TEST_RESOURCE_CENTER_URI = "http://openflexo.org/test/TestResourceCenter";

	public static class FlexoTestEditor extends DefaultFlexoEditor {
		public FlexoTestEditor(FlexoProject<?> project, FlexoServiceManager sm) {
			super(project, sm);
		}

	}

	public TestApplicationContext() {
		super(null, false, true);

		getLocalizationService().setAutomaticSaving(false);

		// Activate both FML and FML@RT technology adapters
		TechnologyAdapterService taService = getTechnologyAdapterService();
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLTechnologyAdapter.class), true);
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class), true);

	}

	@Override
	protected LocalizationService createLocalizationService(String relativePath) {
		LocalizationService returned = super.createLocalizationService(relativePath);
		returned.setAutomaticSaving(false);
		return returned;
	}

	@Override
	protected ProjectLoader createProjectLoaderService() {
		return new TestProjectLoader();
	}

	@Override
	protected FlexoEditor createApplicationEditor() {
		return new FlexoTestEditor(null, this);
	}

	@Override
	protected void registerPreferencesService() {
		// PreferencesService is not activated in test context
	}
	
	@Override
	public ProjectLoadingHandler getProjectLoadingHandler(File projectDirectory) {
		/*
		 * if (UserType.isCustomerRelease() || UserType.isAnalystRelease()) {
		 * return new BasicInteractiveProjectLoadingHandler(projectDirectory); }
		 * else { return new
		 * FullInteractiveProjectLoadingHandler(projectDirectory); }
		 */
		return null;
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
	protected VirtualModelLibrary createViewPointLibraryService() {
		return new VirtualModelLibrary();
	}

	@Override
	protected PreferencesService createPreferencesService() {
		// needed for testing the VEModule
		return new PreferencesService() {
			@Override
			public boolean readOnly() {
				// We force the PreferencesService to be read-only in test scope
				return true;
			}
		};
	}

	@Override
	public BugReportService createBugReportService() {
		// not necessary
		return null;
	}

	@Override
	protected FlexoServerInstanceManager createFlexoServerInstanceManager() {
		// not necessary
		return null;
	}

	@Override
	protected ResourceConsistencyService createResourceConsistencyService() {
		return null;
	}

	/**
	 * Enable a {@link TechnologyAdapter}<br>
	 * All resources centers are notified to scan the resources that they may interpret
	 * 
	 * @param technologyAdapter
	 */
	@Override
	public <TA extends TechnologyAdapter<TA>> ActivateTechnologyAdapterTask<TA> activateTechnologyAdapter(TA technologyAdapter,
			boolean performNowInThisThread) {
		if (technologyAdapter.isActivated())
			return null;
		technologyAdapter.activate();
		notify(getTechnologyAdapterService(), new TechnologyAdapterHasBeenActivated<>(technologyAdapter));
		return null;
	}

	/**
	 * Disable a {@link TechnologyAdapter}<br>
	 * All resources centers are notified to free the resources that they are managing, if possible
	 * 
	 * @param technologyAdapter
	 */
	@Override
	public <TA extends TechnologyAdapter<TA>> DisactivateTechnologyAdapterTask<TA> disactivateTechnologyAdapter(TA technologyAdapter) {
		if (!technologyAdapter.isActivated())
			return null;
		technologyAdapter.disactivate();
		notify(getTechnologyAdapterService(), new TechnologyAdapterHasBeenDisactivated<>(technologyAdapter));
		return null;
	}
}
