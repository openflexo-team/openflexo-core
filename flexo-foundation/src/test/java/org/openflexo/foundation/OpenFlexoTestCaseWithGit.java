package org.openflexo.foundation;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.resource.DefaultResourceCenterService;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.GitResourceCenter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.rm.ClasspathResourceLocatorImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FileUtils;

/**
 * Provide a git environment for tests cases
 * 
 * @author Arkantea
 *
 */
public class OpenFlexoTestCaseWithGit extends OpenflexoProjectAtRunTimeTestCase {

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
	protected static FlexoServiceManager instanciateTestServiceManager(final boolean generateCompoundTestResourceCenter,
			Class<? extends TechnologyAdapter>... taClasses) {
		serviceManager = instanciateTestServiceManager(generateCompoundTestResourceCenter);
		for (Class<? extends TechnologyAdapter> technologyAdapterClass : taClasses) {
			serviceManager
					.activateTechnologyAdapter(serviceManager.getTechnologyAdapterService().getTechnologyAdapter(technologyAdapterClass));
		}
		return serviceManager;
	}

	protected static FlexoServiceManager instanciateTestServiceManager(final boolean generateCompoundTestResourceCenter) {
		File previousResourceCenterDirectoryToRemove = null;
		if (testResourceCenterDirectory != null && testResourceCenterDirectory.exists()) {
			previousResourceCenterDirectoryToRemove = testResourceCenterDirectory;
		}
		serviceManager = new DefaultFlexoServiceManager(null, true) {

			@Override
			protected FlexoEditingContext createEditingContext() {
				// In unit tests, we do NOT want to be warned against unexpected edits
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

					System.out.println("Creating TestResourceCenter [compound: " + generateCompoundTestResourceCenter + "] "
							+ testResourceCenterDirectory);
					System.out.println("***************** WARNING WARNING WARNING ************************");

					if (generateCompoundTestResourceCenter) {

						ClasspathResourceLocatorImpl locator = new ClasspathResourceLocatorImpl();

						List<Resource> toto = locator.locateAllResources("TestResourceCenter");
						for (Resource tstRC : toto) {
							System.out.println(tstRC.toString());
							FileUtils.copyResourceToDir(tstRC, testResourceCenterDirectory);
						}
					}
					else {

						Resource tstRC = ResourceLocator.locateResource("TestResourceCenter");
						System.out.println("Ressource Container Uri " + tstRC.getURI());
						for (Resource resource : tstRC.getContents()) {
							System.out.println("Resource URI : " + resource.getURI());
						}
						System.out.println("Copied from " + tstRC);
						FileUtils.copyResourceToDir(tstRC, testResourceCenterDirectory);
					}

					FlexoResourceCenterService rcService = DefaultResourceCenterService.getNewInstance();
					// rcService.addToResourceCenters(
					// resourceCenter = new DirectoryResourceCenter(testResourceCenterDirectory, TEST_RESOURCE_CENTER_URI));
					// System.out.println("Copied TestResourceCenter to " + testResourceCenterDirectory);

					System.out.println("OK, on cree un nouveau GitResourceCenter ");
					System.out.println("testResourceCenterDirectory=" + testResourceCenterDirectory);

					try {
						rcService.addToResourceCenters(gitResourceCenter = new GitResourceCenter(testResourceCenterDirectory,
								testResourceCenterDirectory, rcService));
						gitResourceCenter.setDefaultBaseURI(TEST_RESOURCE_CENTER_URI);
					} catch (IllegalStateException | GitAPIException e) {
						e.printStackTrace();
					}
					System.out.println("Hop, c'est bon pour le GitResourceCenter");

					// ici il y a des truc a voir

					return rcService;
				} catch (IOException e) {
					e.printStackTrace();
					fail();
					return null;
				}

			}

		};

		if (previousResourceCenterDirectoryToRemove != null) {
			if (testResourceCenterDirectoriesToRemove == null) {
				testResourceCenterDirectoriesToRemove = new ArrayList<File>();
			}
			testResourceCenterDirectoriesToRemove.add(previousResourceCenterDirectoryToRemove);
		}

		// Activate both FML and FML@RT technology adapters
		TechnologyAdapterService taService = serviceManager.getTechnologyAdapterService();
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLTechnologyAdapter.class));
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class));

		return serviceManager;
	}

}
