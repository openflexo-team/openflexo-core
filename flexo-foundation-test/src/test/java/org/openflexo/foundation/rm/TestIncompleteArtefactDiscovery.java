/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.foundation.rm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceFactory;
import org.openflexo.foundation.project.FlexoProjectResourceFactory;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.FileUtils;

/**
 * A directory artefact found while it is being copied gives its resource once complete, and a container explores again what is copied
 * into it afterwards (CORE-D-39).
 *
 * <p>
 * The directory watcher of a resource center runs in its own thread, and may pass at any moment of a copy. Each test here copies a unit
 * in steps, and makes the resource center watch its directory between two of them, where the watcher thread once passed.
 */
@RunWith(OrderedRunner.class)
public class TestIncompleteArtefactDiscovery extends OpenflexoTestCase {

	private static final String OUTER_URI = "http://openflexo.org/test/TestIncompleteArtefactDiscovery/Outer.fml";

	private static File outerDirectory;

	/** Builds the unit Outer, holding a unit Inner that declares a concept ConceptA, in a first resource center */
	@Test
	@TestOrder(1)
	public void testCreateUnits() throws Exception {
		instanciateTestServiceManager();
		FlexoEditor editor = new DefaultFlexoEditor(null, serviceManager);
		DirectoryResourceCenter resourceCenter = makeNewDirectoryResourceCenter();
		FMLTechnologyAdapter fmlTA = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		CompilationUnitResourceFactory factory = fmlTA.getCompilationUnitResourceFactory();

		CompilationUnitResource outerResource = factory.makeTopLevelCompilationUnitResource("Outer", OUTER_URI,
				fmlTA.getGlobalRepository(resourceCenter).getRootFolder(), true);
		VirtualModel outer = outerResource.getLoadedResourceData().getVirtualModel();
		CompilationUnitResource innerResource = factory.makeContainedCompilationUnitResource("Inner", outerResource, true);
		VirtualModel inner = innerResource.getLoadedResourceData().getVirtualModel();
		CreateFlexoConcept createA = CreateFlexoConcept.actionType.makeNewAction(inner, null, editor);
		createA.setNewFlexoConceptName("ConceptA");
		createA.doAction();
		assertTrue(createA.hasActionExecutionSucceeded());
		innerResource.save();
		outerResource.save();

		outerDirectory = ResourceLocator.retrieveResourceAsFile(outer.getResource().getDirectory());
		assertTrue(new File(outerDirectory, "Outer.fml").exists());
		assertTrue(new File(new File(outerDirectory, "Inner.fml"), "Inner.fml").exists());
	}

	/** The watcher passes after the directory of the unit was made, before its files were copied */
	@Test
	@TestOrder(2)
	public void testUnitFoundBeforeItsMainFile() throws IOException {
		instanciateTestServiceManager();
		DirectoryResourceCenter resourceCenter = makeNewDirectoryResourceCenter();
		File copy = new File(resourceCenter.getRootDirectory(), outerDirectory.getName());

		copy.mkdirs();
		resourceCenter.performDirectoryWatchingNow();
		assertNull("A directory without its unit gave a resource", outerResource());
		FMLTechnologyAdapter fmlTA = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		assertFalse("A directory without its unit was taken for a folder",
				hasFolderNamed(fmlTA.getGlobalRepository(resourceCenter).getRootFolder(), outerDirectory.getName()));

		FileUtils.copyContentDirToDir(outerDirectory, copy);
		resourceCenter.performDirectoryWatchingNow();

		assertUnitIsComplete();
	}

	/** The watcher passes after the main file of the container was copied, before its contained unit */
	@Test
	@TestOrder(3)
	public void testContainedUnitCopiedAfterItsContainer() throws IOException {
		instanciateTestServiceManager();
		DirectoryResourceCenter resourceCenter = makeNewDirectoryResourceCenter();
		File copy = new File(resourceCenter.getRootDirectory(), outerDirectory.getName());
		copy.mkdirs();
		FileUtils.copyFileToFile(new File(outerDirectory, "Outer.fml"), new File(copy, "Outer.fml"));
		resourceCenter.performDirectoryWatchingNow();
		assertNotNull(outerResource());
		assertNull(serviceManager.getVirtualModelLibrary().getCompilationUnitResource(OUTER_URI + "/Inner.fml"));

		File innerCopy = new File(copy, "Inner.fml");
		innerCopy.mkdirs();
		resourceCenter.performDirectoryWatchingNow();
		FileUtils.copyContentDirToDir(new File(outerDirectory, "Inner.fml"), innerCopy);
		resourceCenter.performDirectoryWatchingNow();

		assertUnitIsComplete();
	}

	/** The rule holds for the other directory artefacts: an instance of a virtual model, a project */
	@Test
	@TestOrder(4)
	public void testOtherDirectoryArtefactsNeedTheirMainFile() throws IOException {
		instanciateTestServiceManager();
		DirectoryResourceCenter resourceCenter = makeNewDirectoryResourceCenter();
		FMLRTVirtualModelInstanceResourceFactory vmiFactory = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLRTTechnologyAdapter.class).getFMLRTVirtualModelInstanceResourceFactory();
		FlexoProjectResourceFactory<File> projectFactory = (FlexoProjectResourceFactory<File>) serviceManager.getResourceCenterService()
				.getFlexoProjectResourceFactory();

		File instance = new File(resourceCenter.getRootDirectory(), "Instance.fml.rt");
		instance.mkdirs();
		assertFalse(vmiFactory.isValidArtefact(instance, resourceCenter));
		assertTrue(vmiFactory.isIncompleteArtefact(instance, resourceCenter));
		FileUtils.saveToFile(new File(instance, "Instance.fml.rt.xml"), "<FMLRTVirtualModelInstance/>");
		assertTrue(vmiFactory.isValidArtefact(instance, resourceCenter));
		assertFalse(vmiFactory.isIncompleteArtefact(instance, resourceCenter));

		File project = new File(resourceCenter.getRootDirectory(), "Project.prj");
		project.mkdirs();
		assertFalse(projectFactory.isValidArtefact(project, resourceCenter));
		assertTrue(projectFactory.isIncompleteArtefact(project, resourceCenter));
		FileUtils.saveToFile(new File(project, FlexoProjectResourceFactory.PROJECT_DATA_FILENAME), "<FlexoProject/>");
		assertTrue(projectFactory.isValidArtefact(project, resourceCenter));
		assertFalse(projectFactory.isIncompleteArtefact(project, resourceCenter));
	}

	private static CompilationUnitResource outerResource() {
		return serviceManager.getVirtualModelLibrary().getCompilationUnitResource(OUTER_URI);
	}

	/** The copied unit is found by its URI, and holds Inner and its concept ConceptA */
	private static void assertUnitIsComplete() {
		CompilationUnitResource outerResource = outerResource();
		assertNotNull("The copied unit is not found by its URI", outerResource);
		assertNotNull("The contained unit is not registered",
				serviceManager.getVirtualModelLibrary().getCompilationUnitResource(OUTER_URI + "/Inner.fml"));
		VirtualModel outer = outerResource.getCompilationUnit().getVirtualModel();
		VirtualModel inner = outer.getVirtualModelNamed("Inner");
		assertNotNull("The contained virtual model is missing", inner);
		assertNotNull("The concept of the contained virtual model is missing", inner.getFlexoConcept("ConceptA"));
		assertEquals(1, outerResource.getContents(CompilationUnitResource.class).size());
	}

	private static boolean hasFolderNamed(RepositoryFolder<?, ?> folder, String name) {
		for (RepositoryFolder<?, ?> child : folder.getChildren()) {
			if (name.equals(child.getName())) {
				return true;
			}
		}
		return false;
	}
}
