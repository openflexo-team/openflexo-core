package org.openflexo.foundation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intended to test project creation and reloading facilities<br>
 * We should retrieve project metadata after project creation and saving
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestReloadProject extends OpenflexoProjectAtRunTimeTestCase {

	private static FlexoEditor editor;
	private static FlexoProject project;

	/**
	 * Create an empty project
	 * 
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(1)
	public void testCreateProject() throws SaveResourceException {
		editor = createProject("TestProject");
		project = editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
		assertTrue(project.getProjectDataResource().getFlexoIODelegate().exists());
		project.setDescription("This is a test project");
		project.save();
	}

	/**
	 * Reload the project, tests that uri, name and description are persistent
	 */
	@Test
	@TestOrder(2)
	public void testReloadProject() {

		String oldURI = project.getURI();
		System.out.println("Old URI: " + oldURI);
		instanciateTestServiceManager();
		editor = reloadProject(project.getDirectory());
		project = editor.getProject();
		String newURI = project.getURI();
		System.out.println("New URI: " + newURI);
		assertNotNull(editor);
		assertNotNull(project);
		assertEquals(newURI, oldURI);
		assertEquals("This is a test project", project.getDescription());
	}

}
