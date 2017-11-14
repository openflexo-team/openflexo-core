package org.openflexo.foundation.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.localization.LocalizationService;
import org.openflexo.foundation.resource.DefaultResourceCenterService;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.GitResourceCenter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.rm.ClasspathResourceLocatorImpl;
import org.openflexo.rm.Resource;
import org.openflexo.toolbox.FileUtils;

/**
 * Provide a git environment for tests cases
 * 
 * @author Arkantea
 *
 */
public class OpenFlexoTestCaseWithGit extends OpenflexoProjectAtRunTimeTestCase {

	// private static DirectoryResourceCenter resourceCenter;
	private static GitResourceCenter gitResourceCenter;

	protected static final String TEST_RESOURCE_CENTER_URI = "http://openflexo.org/test/TestGitResourceCenter";

	/**
	 * Instantiate a default {@link FlexoServiceManager} well suited for test purpose<br>
	 * FML and FML@RT technology adapters are activated in returned {@link FlexoServiceManager}, as well as technology adapters whose
	 * classes are supplied as varargs arguments
	 * 
	 * @param taClasses
	 * @return a newly created {@link FlexoServiceManager}
	 */
	@SafeVarargs
	protected static FlexoServiceManager instanciateTestServiceManager(Class<? extends TechnologyAdapter>... taClasses) {
		File previousResourceCenterDirectoryToRemove = null;
		if (testResourceCenterDirectory != null && testResourceCenterDirectory.exists()) {
			previousResourceCenterDirectoryToRemove = testResourceCenterDirectory;
		}
		serviceManager = new DefaultFlexoServiceManager(null, true) {

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
			protected FlexoEditor createApplicationEditor() {
				return new FlexoTestEditor(null, this);
			}

			@Override
			protected FlexoResourceCenterService createResourceCenterService() {
				try {
					File tempFile = File.createTempFile("Temp", "");
					testResourceCenterDirectory = new File(tempFile.getParentFile(), tempFile.getName() + "TestResourceCenter");
					tempFile.delete();
					testResourceCenterDirectory.mkdirs();

					System.out.println("Creating TestResourceCenter [compound:true] " + testResourceCenterDirectory);
					System.out.println("***************** WARNING WARNING WARNING ************************");

					// if (generateCompoundTestResourceCenter) {

					ClasspathResourceLocatorImpl locator = new ClasspathResourceLocatorImpl();

					List<Resource> toto = locator.locateAllResources("TestResourceCenter");
					System.out.println("on cherche tous les TestResourceCenter");
					System.out.println("list=" + toto);

					for (Resource tstRC : toto) {
						System.out.println(tstRC.toString());
						FileUtils.copyResourceToDir(tstRC, testResourceCenterDirectory);
					}
					/*
					 * } else {
					 * 
					 * Resource tstRC =
					 * ResourceLocator.locateResource("TestResourceCenter");
					 * System.out.println("Ressource Container Uri " +
					 * tstRC.getURI()); for (Resource resource :
					 * tstRC.getContents()) {
					 * System.out.println("Resource URI : " +
					 * resource.getURI()); } System.out.println("Copied from " +
					 * tstRC); FileUtils.copyResourceToDir(tstRC,
					 * testResourceCenterDirectory); }
					 */

					FlexoResourceCenterService rcService = DefaultResourceCenterService.getNewInstance(true); //
					// rcService.addToResourceCenters(resourceCenter = new
					// DirectoryResourceCenter(
					// testResourceCenterDirectory, TEST_RESOURCE_CENTER_URI,
					// rcService)); //
					// System.out.println("Copied TestResourceCenter to " +
					// testResourceCenterDirectory);

					System.out.println("OK, on cree un nouveau GitResourceCenter ");
					System.out.println("testResourceCenterDirectory=" + testResourceCenterDirectory);

					try {
						gitResourceCenter = GitResourceCenter.instanciateNewGitResourceCenter(testResourceCenterDirectory,
								testResourceCenterDirectory, rcService);
						gitResourceCenter.setDefaultBaseURI(TEST_RESOURCE_CENTER_URI);
						rcService.addToResourceCenters(gitResourceCenter);
					} catch (IllegalStateException | GitAPIException e) {
						e.printStackTrace();
					}
					System.out.println("Hop, c'est bon pour le GitResourceCenter");

					// ici il y a des truc a voir

					return rcService;
				} catch (IOException e) {
					e.printStackTrace();
					// fail();
					return null;
				}

			}

		};

		serviceManager.getLocalizationService().setAutomaticSaving(false);

		// Activate both FML and FML@RT technology adapters
		TechnologyAdapterService taService = serviceManager.getTechnologyAdapterService();
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLTechnologyAdapter.class));
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class));

		for (Class<? extends TechnologyAdapter> technologyAdapterClass : taClasses) {
			serviceManager
					.activateTechnologyAdapter(serviceManager.getTechnologyAdapterService().getTechnologyAdapter(technologyAdapterClass));
		}

		if (previousResourceCenterDirectoryToRemove != null) {
			if (testResourceCenterDirectoriesToRemove == null) {
				testResourceCenterDirectoriesToRemove = new ArrayList<>();
			}
			testResourceCenterDirectoriesToRemove.add(previousResourceCenterDirectoryToRemove);
		}

		return serviceManager;
	}

}
