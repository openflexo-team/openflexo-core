package org.openflexo;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openflexo.br.BugReportService;
import org.openflexo.drm.DocResourceManager;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.resource.DefaultResourceCenterService;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.DefaultTechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.InformationSpace;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.utils.ProjectLoadingHandler;
import org.openflexo.prefs.PreferencesService;
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
		super();
		this.generateCompoundTestResourceCenter = generateCompoundTestResourceCenter;
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
			rcService.addToResourceCenters(resourceCenter = new DirectoryResourceCenter(testResourceCenterDirectory));
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
	protected InformationSpace createInformationSpace() {
		return new InformationSpace();
	}

	@Override
	protected PreferencesService createPreferencesService() {
		// needed for testing the VEModule
		return new PreferencesService();
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
}
