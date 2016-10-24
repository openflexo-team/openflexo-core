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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openflexo.ApplicationContext;
import org.openflexo.br.BugReportService;
import org.openflexo.drm.DocResourceManager;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.resource.DefaultResourceCenterService;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.DefaultTechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.utils.ProjectLoadingHandler;
import org.openflexo.prefs.PreferencesService;
import org.openflexo.rm.ActivateTechnologyAdapterTask;
import org.openflexo.rm.DisactivateTechnologyAdapterTask;
import org.openflexo.rm.FileSystemResourceLocatorImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceConsistencyService;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.view.controller.DefaultTechnologyAdapterControllerService;
import org.openflexo.view.controller.FlexoServerInstanceManager;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Test purposes: implements an ApplicationContext with a unique ResourceCenter
 * 
 * @author sylvain
 * 
 */
public class TestApplicationContext extends ApplicationContext {

	protected static DirectoryResourceCenter resourceCenter;

	private static final String TEST_RESOURCE_CENTER_URI = "http://openflexo.org/test/TestResourceCenter";

	public static class FlexoTestEditor extends DefaultFlexoEditor {
		public FlexoTestEditor(FlexoProject project, FlexoServiceManager sm) {
			super(project, sm);
		}

	}

	private boolean generateCompoundTestResourceCenter = false;

	public TestApplicationContext() {
		this(false);
	}

	public TestApplicationContext(boolean generateCompoundTestResourceCenter) {
		super(null, true);
		this.generateCompoundTestResourceCenter = generateCompoundTestResourceCenter;

		// Activate both FML and FML@RT technology adapters
		TechnologyAdapterService taService = getTechnologyAdapterService();
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLTechnologyAdapter.class));
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class));

	}

	@Override
	public FlexoEditor makeFlexoEditor(FlexoProject project, FlexoServiceManager serviceManager) {
		return new FlexoTestEditor(project, serviceManager);
	}

	@Override
	protected FlexoEditor createApplicationEditor() {
		return new FlexoTestEditor(null, this);
	}

	@Override
	protected FlexoResourceCenterService createResourceCenterService() {
		try {
			File tempFile = File.createTempFile("Temp", "");
			File testResourceCenterDirectory = new File(tempFile.getParentFile(), tempFile.getName() + "TestResourceCenter");
			testResourceCenterDirectory.mkdirs();

			System.out.println("Creating TestResourceCenter " + testResourceCenterDirectory);

			if (generateCompoundTestResourceCenter) {
				System.out.println("Generating CompoundTestResourceCenter");
				// TODO : FIX this, it does not work & , this is crappy!
				List<File> testRCList = ((FileSystemResourceLocatorImpl) ResourceLocator
						.getInstanceForLocatorClass(FileSystemResourceLocatorImpl.class)).locateAllFiles("TestResourceCenter");
				for (File f : testRCList) {
					System.out.println("Found TestResourceCenter " + f);
					FileUtils.copyContentDirToDir(f, testResourceCenterDirectory);
				}
			}
			else {
				Resource sourceTestResourceCenter = ResourceLocator.locateResource("TestResourceCenter");
				System.out.println("Found TestResourceCenter " + sourceTestResourceCenter);
				FileUtils.copyResourceToDir(sourceTestResourceCenter, testResourceCenterDirectory);
			}

			FlexoResourceCenterService rcService = DefaultResourceCenterService.getNewInstance();
			rcService.addToResourceCenters(
					resourceCenter = new DirectoryResourceCenter(testResourceCenterDirectory, TEST_RESOURCE_CENTER_URI, rcService));
			System.out.println("Copied TestResourceCenter to " + testResourceCenterDirectory);

			// ici il y a des truc a voir

			return rcService;
		} catch (IOException e) {
			e.printStackTrace();
			fail();
			return null;
		}

	}

	@Override
	public ProjectLoadingHandler getProjectLoadingHandler(File projectDirectory) {
		/*if (UserType.isCustomerRelease() || UserType.isAnalystRelease()) {
			return new BasicInteractiveProjectLoadingHandler(projectDirectory);
		} else {
			return new FullInteractiveProjectLoadingHandler(projectDirectory);
		}*/
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
	protected ViewPointLibrary createViewPointLibraryService() {
		return new ViewPointLibrary();
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
	protected BugReportService createBugReportService() {
		// not necessary
		return null;
	}

	@Override
	protected DocResourceManager createDocResourceManager() {
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
	public ActivateTechnologyAdapterTask activateTechnologyAdapter(TechnologyAdapter technologyAdapter) {

		if (technologyAdapter.isActivated()) {
			return null;
		}

		technologyAdapter.activate();

		notify(getTechnologyAdapterService(), new TechnologyAdapterHasBeenActivated(technologyAdapter));

		return null;
	}

	/**
	 * Disable a {@link TechnologyAdapter}<br>
	 * All resources centers are notified to free the resources that they are managing, if possible
	 * 
	 * @param technologyAdapter
	 */
	@Override
	public DisactivateTechnologyAdapterTask disactivateTechnologyAdapter(TechnologyAdapter technologyAdapter) {

		if (!technologyAdapter.isActivated()) {
			return null;
		}

		technologyAdapter.disactivate();
		notify(getTechnologyAdapterService(), new TechnologyAdapterHasBeenDisactivated(technologyAdapter));

		return null;
	}

}
