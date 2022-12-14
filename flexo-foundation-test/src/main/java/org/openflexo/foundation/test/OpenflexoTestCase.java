/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.openflexo.connie.binding.javareflect.KeyValueLibrary;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.CompilationUnitRepository;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.localization.LocalizationService;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.resource.DefaultResourceCenterService;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter.FSBasedResourceCenterEntry;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenter.ResourceCenterEntry;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.logging.FlexoLoggingManager;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FileUtils.CopyStrategy;

import junit.framework.AssertionFailedError;

/**
 * Provides a JUnit 4 generic environment of Openflexo-core for testing purposes<br>
 * 
 * Note that there is a static variable called {@link #serviceManager} which is set while using
 * {@link #instanciateTestServiceManager(Class...)}.<br>
 * The purpose of that variable is to share the same {@link FlexoServiceManager} while chaining some tests using @TestOrder annotation For
 * same reasons, a static {@link #resourceCenter} variable is also managed in this class
 * 
 */
public abstract class OpenflexoTestCase {

	/**
	 * !!!!! IMPORTANT !!!!!<br>
	 * Do not forget to set back this flag to true when committing into a production environment
	 */
	public static final boolean DELETE_TEST_RESOURCE_CENTER_AFTER_TEST_EXECUTION = true;

	private static final Logger logger = FlexoLogger.getLogger(OpenflexoTestCase.class.getPackage().getName());

	protected static final String RESOURCE_CENTER_URI = "http://openflexo.org/test/TestResourceCenter";

	/**
	 * ResourceCenter being statically referenced while using makeNewDirectoryResourceCenter() methods
	 */
	private static DirectoryResourceCenter resourceCenter;

	protected static FlexoServiceManager serviceManager;

	protected static File testResourceCenterDirectory;
	protected static List<File> testResourceCenterDirectoriesToRemove;

	// We should have it unchanged to chain tests from right HOME dir
	protected static File HOME_DIR;

	static {
		try {
			FlexoLoggingManager.initialize(-1, true, null, Level.WARNING, null);
			HOME_DIR = new File(System.getProperty("user.dir"));
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		deleteTestResourceCenters();
		unloadServiceManager();
	}

	protected static void unloadServiceManager() {
		if (serviceManager != null) {
			serviceManager.stopAllServices();

			if (resourceCenter != null) {
				deleteTestResourceCenters();
			}
		}
		serviceManager = null;
	}

	protected static void deleteTestResourceCenters() {
		FlexoResourceCenterService RCService = null;

		if (serviceManager != null) {
			RCService = serviceManager.getResourceCenterService();
		}
		// Must stop ResourceCenterService before deleting resourceCenters
		if (RCService != null) {
			if (resourceCenter != null) {
				RCService.removeFromResourceCenters(resourceCenter);
			}
			RCService.stop();
		}

		if (DELETE_TEST_RESOURCE_CENTER_AFTER_TEST_EXECUTION) {
			if (testResourceCenterDirectory != null && testResourceCenterDirectory.exists()) {
				FileUtils.deleteDir(testResourceCenterDirectory);
			}
			if (testResourceCenterDirectoriesToRemove != null) {
				for (File testResourceCenterDirectoryToRemove : testResourceCenterDirectoriesToRemove) {
					if (testResourceCenterDirectoryToRemove != null && testResourceCenterDirectoryToRemove.exists())
						FileUtils.deleteDir(testResourceCenterDirectoryToRemove);
				}
			}
		}
		resourceCenter = null;
	}

	public static class FlexoTestEditor extends DefaultFlexoEditor {
		public FlexoTestEditor(FlexoProject<?> project, FlexoServiceManager sm) {
			super(project, sm);
		}

	}

	public static class TestProjectLoader extends ProjectLoader {
		/**
		 * Create and return a new {@link FlexoEditor} for supplied {@link FlexoProject}
		 * 
		 * @param project
		 * @return
		 */
		@Override
		public FlexoEditor makeFlexoEditor(FlexoProject<?> project) {
			FlexoEditor returned = new FlexoTestEditor(project, getServiceManager());
			getPropertyChangeSupport().firePropertyChange(EDITOR_ADDED, null, returned);
			return returned;
		}
	}

	public File getResource(String resourceRelativeName) {
		File retval = new File("src/test/resources", resourceRelativeName);
		if (retval.exists()) {
			return retval;
		}
		retval = new File("../flexofoundation/src/test/resources", resourceRelativeName);
		if (retval.exists()) {
			return retval;
		}
		retval = new File("tmp/tests/FlexoResources/", resourceRelativeName);
		if (retval.exists()) {
			return retval;
		}
		else if (logger.isLoggable(Level.WARNING)) {
			logger.warning("Could not find resource " + resourceRelativeName);
		}
		return null;
	}

	/**
	 * Instantiate a default {@link FlexoServiceManager} well suited for test purpose<br>
	 * FML and FML@RT technology adapters are activated in returned {@link FlexoServiceManager}<br>
	 * Supplied technology adapters are also activated
	 * 
	 * @return a newly created {@link FlexoServiceManager}
	 */
	@SafeVarargs
	protected static FlexoServiceManager instanciateTestServiceManager(Class<? extends TechnologyAdapter>... taClasses) {
		File previousResourceCenterDirectoryToRemove = null;
		if (testResourceCenterDirectory != null && testResourceCenterDirectory.exists()) {
			previousResourceCenterDirectoryToRemove = testResourceCenterDirectory;
		}
		serviceManager = new DefaultFlexoServiceManager(null, false, true) {

			@Override
			protected LocalizationService createLocalizationService(String relativePath) {
				LocalizationService returned = super.createLocalizationService(relativePath);
				returned.setAutomaticSaving(false);
				return returned;
			}

			@Override
			protected FlexoEditingContext createEditingContext() {
				// In unit tests, we do NOT want to be warned against unexpected
				// edits
				return FlexoEditingContext.createInstance(false);
			}

			@Override
			protected FlexoTestEditor createApplicationEditor() {
				return new FlexoTestEditor(null, this);
			}

			@Override
			protected TestProjectLoader createProjectLoaderService() {
				return new TestProjectLoader();
			}

		};

		serviceManager.getLocalizationService().setAutomaticSaving(false);

		// Activate both FML and FML@RT technology adapters
		TechnologyAdapterService taService = serviceManager.getTechnologyAdapterService();
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLTechnologyAdapter.class), true);
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class), true);

		if (previousResourceCenterDirectoryToRemove != null) {
			if (testResourceCenterDirectoriesToRemove == null) {
				testResourceCenterDirectoriesToRemove = new ArrayList<>();
			}
			testResourceCenterDirectoriesToRemove.add(previousResourceCenterDirectoryToRemove);
		}

		for (Class<? extends TechnologyAdapter> technologyAdapterClass : taClasses) {
			TechnologyAdapter ta = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(technologyAdapterClass);
			serviceManager.activateTechnologyAdapter(ta, true);
		}

		return serviceManager;
	}

	public static DirectoryResourceCenter getResourceCenter() {
		return resourceCenter;
	}

	protected static FlexoResourceCenterService getNewResourceCenter(String name) {
		try {
			PamelaModelFactory factory = new PamelaModelFactory(FSBasedResourceCenterEntry.class);
			FSBasedResourceCenterEntry<?> entry = factory.newInstance(FSBasedResourceCenterEntry.class);
			entry.setDirectory(FileUtils.createTempDirectory(name, "ResourceCenter"));
			List<ResourceCenterEntry<?>> rcList = new ArrayList<>();
			rcList.add(entry);
			return DefaultResourceCenterService.getNewInstance(rcList, false, true);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail();
		}
		return null;
	}

	/**
	 * Create a new empty DirectoryResourceCenter for service manager referenced in static variable
	 * 
	 * @return
	 * @throws IOException
	 */
	public static DirectoryResourceCenter makeNewDirectoryResourceCenter() throws IOException {
		return makeNewDirectoryResourceCenter(serviceManager);
	}

	/**
	 * Create a new empty {@link DirectoryResourceCenter} for supplied {@link FlexoServiceManager}
	 * 
	 * @return
	 * @throws IOException
	 */
	public static DirectoryResourceCenter makeNewDirectoryResourceCenter(FlexoServiceManager serviceManager) throws IOException {
		File tempFile = File.createTempFile("Temp", "");
		testResourceCenterDirectory = new File(tempFile.getParentFile(), tempFile.getName() + "TestResourceCenter");
		tempFile.delete();
		testResourceCenterDirectory.mkdirs();
		FlexoResourceCenterService rcService = serviceManager.getResourceCenterService();
		resourceCenter = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(testResourceCenterDirectory, rcService);
		resourceCenter.setDefaultBaseURI(RESOURCE_CENTER_URI);
		rcService.addToResourceCenters(resourceCenter);
		return resourceCenter;
	}

	/**
	 * Create a new empty {@link DirectoryResourceCenter} for supplied {@link FlexoServiceManager} while copying the contents of supplied
	 * {@link FlexoResourceCenter}
	 * 
	 * Gives the same URI, remove supplied {@link FlexoResourceCenter} from the list of registered resource centers
	 * 
	 * @return
	 * @throws IOException
	 */
	public static DirectoryResourceCenter makeNewDirectoryResourceCenterFromExistingResourceCenter(FlexoServiceManager serviceManager,
			FlexoResourceCenter<?> existingResourcesRC) throws IOException {
		File tempFile = File.createTempFile("Temp", "");
		testResourceCenterDirectory = new File(tempFile.getParentFile(), tempFile.getName() + "TestResourceCenter");
		tempFile.delete();
		testResourceCenterDirectory.mkdirs();

		logger.info("Copying " + existingResourcesRC.getBaseArtefactAsResource() + " to " + testResourceCenterDirectory);
		logger.info("Resource: " + existingResourcesRC.getBaseArtefactAsResource().getClass());

		FileUtils.copyResourceToDir(existingResourcesRC.getBaseArtefactAsResource(), testResourceCenterDirectory, CopyStrategy.REPLACE);

		FlexoResourceCenterService rcService = serviceManager.getResourceCenterService();
		DirectoryResourceCenter resourceCenter = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(testResourceCenterDirectory,
				rcService);
		resourceCenter.setDefaultBaseURI(existingResourcesRC.getDefaultBaseURI());
		rcService.removeFromResourceCenters(existingResourcesRC);
		rcService.addToResourceCenters(resourceCenter);
		return resourceCenter;
	}

	protected void reloadResourceCenter(Resource oldRCDirectory) {
		if (oldRCDirectory instanceof FileResourceImpl) {
			File directory = ((FileResourceImpl) oldRCDirectory).getFile();
			File newDirectory = new File(((FileSystemBasedResourceCenter) resourceCenter).getRootDirectory(), directory.getName());
			newDirectory.mkdirs();
			try {
				FileUtils.copyContentDirToDir(directory, newDirectory);
				// We wait here for the thread monitoring ResourceCenters to
				// detect new files
				((FileSystemBasedResourceCenter) resourceCenter).performDirectoryWatchingNow();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected static FlexoServiceManager getFlexoServiceManager() {
		return serviceManager;
	}

	protected void assertNotModified(FlexoResource<?> resource) {
		try {
			if (resource.isLoaded()) {
				assertFalse("Resource " + resource.getURI() + " should not be modfied", resource.getLoadedResourceData().isModified());
			}
			else {
				fail("Resource " + resource.getURI() + " should not be modified but is not even loaded");
			}
		} catch (AssertionFailedError e) {
			logger.warning("RESOURCE status problem: " + resource + " MUST be NOT modified");
			throw e;
		}
	}

	protected void assertModified(FlexoResource<?> resource) {
		try {
			if (resource.isLoaded()) {
				assertTrue("Resource " + resource.getURI() + " should be modified", resource.getLoadedResourceData().isModified());
			}
			else {
				fail("Resource " + resource.getURI() + " should be modified but is not even loaded");
			}
		} catch (AssertionFailedError e) {
			logger.warning("RESOURCE status problem: " + resource + " MUST be modified");
			throw e;
		}
	}

	protected void assertNotLoaded(FlexoResource<?> resource) {
		try {
			assertFalse("Resource " + resource.getURI() + " should not be loaded", resource.isLoaded());
		} catch (AssertionFailedError e) {
			logger.warning("RESOURCE status problem: " + resource + " MUST be NOT loaded");
			throw e;
		}
	}

	protected void assertLoaded(FlexoResource<?> resource) {
		try {
			assertTrue("Resource " + resource.getURI() + " should be loaded", resource.isLoaded());
		} catch (AssertionFailedError e) {
			logger.warning("RESOURCE status problem: " + resource + " MUST be loaded");
			throw e;
		}
	}

	protected void log(String step) {
		logger.info("\n******************************************************************************\n" + step
				+ "\n******************************************************************************\n");
	}

	protected void debugMemory() {

		int mb = 1024 * 1024;

		// Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();

		log("##### Heap utilization statistics [MB] #####");

		// Print used memory
		logger.info("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);

		// Print free memory
		logger.info("Free Memory:" + runtime.freeMemory() / mb);

		// Print total available memory
		logger.info("Total Memory:" + runtime.totalMemory() / mb);

		// Print Maximum available memory
		logger.info("Max Memory:" + runtime.maxMemory() / mb);

	}

	/**
	 * Assert this is the same list, doesn't care about order
	 * 
	 * @param aList
	 * @param objects
	 * @throws AssertionFailedError
	 */
	@SafeVarargs
	public static <T> void assertSameList(Collection<? extends T> aList, T... objects) throws AssertionFailedError {
		Set<T> set1 = new HashSet<>(aList);
		Set<T> set2 = new HashSet<>();
		for (T o : objects) {
			set2.add(o);
		}
		if (!set1.equals(set2)) {
			StringBuffer message = new StringBuffer();
			for (T o : set1) {
				if (!set2.contains(o)) {
					message.append(" Extra: " + o);
				}
			}
			for (T o : set2) {
				if (!set1.contains(o)) {
					message.append(" Missing: " + o);
				}
			}
			throw new AssertionFailedError(
					"AssertionFailedError when comparing lists, expected: " + set1 + " but was " + set2 + " Details = " + message);
		}
	}

	@After
	public void tearDown() throws Exception {
		KeyValueLibrary.clearCache();
	}

	protected void assertVirtualModelIsValid(VirtualModel vm) {
		assertObjectIsValid(vm);
	}

	protected void assertObjectIsValid(FMLObject object) {
		assertEquals(0, validate(object).getErrorsCount());
	}

	protected ValidationReport validate(FMLObject object) {

		try {
			ValidationModel validationModel = object.getVirtualModelLibrary().getFMLValidationModel();
			ValidationReport report = object.getVirtualModelLibrary().getFMLValidationModel().validate(object);

			for (ValidationError<?, ?> error : report.getAllErrors()) {
				System.out.println("Found error: " + validationModel.localizedIssueMessage(error) + " details="
						+ validationModel.localizedIssueDetailedInformations(error) + " Object: " + error.getValidable());
			}

			return report;

		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Interrupted");
			return null;
		}
	}

	protected <T extends TechnologyAdapter<T>> T getTA(Class<T> type) {
		return serviceManager.getTechnologyAdapterService().getTechnologyAdapter(type);
	}

	protected VirtualModel createTopLevelVirtualModel(FlexoResourceCenter<File> rc, String vmName, String vmURI)
			throws org.openflexo.foundation.resource.SaveResourceException, ModelDefinitionException {
		FMLTechnologyAdapter fmlTechnologyAdapter = getTA(FMLTechnologyAdapter.class);
		CompilationUnitResourceFactory factory = fmlTechnologyAdapter.getCompilationUnitResourceFactory();
		CompilationUnitRepository<File> viewPointRepository = rc.getVirtualModelRepository();
		CompilationUnitResource viewPointResource = factory.makeTopLevelCompilationUnitResource(vmName, vmURI,
				viewPointRepository.getRootFolder(), true);

		viewPointResource.save();
		return viewPointResource.getLoadedResourceData().getVirtualModel();
	}

}
