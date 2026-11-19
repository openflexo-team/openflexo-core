package org.openflexo.foundation.fml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitParser;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Non-regression guard for CORE-D-94 (not reproduced): a unit edited as the FML editor does (parse + <code>updateWith</code>) must be
 * marked modified, written by the save of the project with its concepts (a nested one included), and found again after a reload.
 */
@RunWith(OrderedRunner.class)
public class TestCoreD94 extends OpenflexoProjectAtRunTimeTestCase {

	private static final String URI = "http://openflexo.org/test/TestContainment";

	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static CompilationUnitResource resource;

	@Test
	@TestOrder(1)
	public void test1CreateUnit() throws Exception {
		instanciateTestServiceManager();
		editor = createStandaloneProject("TestProjectD94");
		project = (FlexoProject<File>) editor.getProject();
		FMLTechnologyAdapter ta = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		CompilationUnitResourceFactory factory = ta.getCompilationUnitResourceFactory();
		resource = factory.makeTopLevelCompilationUnitResource("TestContainment", URI, ta.getGlobalRepository(project).getRootFolder(),
				true);
		assertNotNull(resource.getLoadedResourceData().getVirtualModel());
		project.saveModifiedResources();
		System.out.println("D94 file after creation:\n" + read());
	}

	@Test
	@TestOrder(2)
	public void test2EditAsEditorDoes() throws Exception {
		FMLCompilationUnit existing = resource.getCompilationUnit();
		String text = existing.getFMLPrettyPrint();
		System.out.println("D94 pretty print before:\n" + text);
		int i = text.lastIndexOf('}');
		String edited = text.substring(0, i) + "\tpublic concept Alpha {\n\t\tpublic concept Leaf {\n\t\t}\n\t}\n\tpublic concept Gamma {\n\t}\n}\n";
		FMLCompilationUnit parsed = new FMLCompilationUnitParser().parse(edited, existing.getFMLModelFactory(), m -> null, true);
		parsed.setResource(resource);
		existing.updateWith(parsed);
		existing.getTypingSpace().resolveUnresolvedTypes();
		existing.setResource(resource);
		existing.manageImports();

		System.out.println("D94 concepts in model: " + existing.getVirtualModel().getAllFlexoConcepts());
		System.out.println("D94 resource.isModified()=" + resource.isModified() + " unit.isModified()=" + existing.isModified()
				+ " vm.isModified()=" + existing.getVirtualModel().isModified());
		assertTrue("Edited unit not marked modified", resource.isModified());
	}

	@Test
	@TestOrder(3)
	public void test3Save() throws Exception {
		project.saveModifiedResources();
		String onDisk = read();
		System.out.println("D94 file after save:\n" + onDisk);
		assertTrue("Concepts missing on disk:\n" + onDisk, onDisk.contains("concept Alpha") && onDisk.contains("concept Gamma"));
	}

	@Test
	@TestOrder(4)
	public void test4Reload() throws Exception {
		editor = reloadProject(project);
		project = (FlexoProject<File>) editor.getProject();
		CompilationUnitResource reloaded = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(URI);
		assertNotNull(reloaded);
		assertTrue("Reloaded unit is empty", reloaded.getCompilationUnit().getVirtualModel().getAllFlexoConcepts().size() >= 3);
	}

	private static String read() throws Exception {
		File dir = project.getProjectDirectory();
		try (var s = Files.walk(dir.toPath())) {
			File f = s.map(java.nio.file.Path::toFile)
					.filter(x -> x.isFile() && x.getName().equals("TestContainment.fml") && x.getParentFile().getName().equals("TestContainment.fml"))
					.findFirst().orElseThrow(() -> new AssertionError("TestContainment.fml not found under " + dir));
			return new String(Files.readAllBytes(f.toPath()));
		}
	}
}
