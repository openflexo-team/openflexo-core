package org.openflexo.foundation.viewpoint;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.foundation.viewpoint.ViewPoint.ViewPointImpl;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test ViewPoint creation facilities
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestCreateViewPointInProject extends OpenflexoProjectAtRunTimeTestCase {

	static FlexoEditor editor;
	static FlexoProject project;
	
	@Test
	@TestOrder(1)
	public void testInstanciateServiceManager() {
		instanciateTestServiceManager();
		System.out.println("ResourceCenter= " + resourceCenter);
	}

	@Test
	@TestOrder(2)
	public void testCreateProject() {
		editor = createProject("TestProject");
		project = editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
		assertTrue(project.getProjectDataResource().getFlexoIODelegate().exists());
	}

	/**
	 * Test the creation of the viewpoint
	 */
	@Test
	@TestOrder(3)
	public void testCreateViewPoint() {
		ViewPoint newViewPoint = ViewPointImpl.newViewPoint("TestViewPoint", "http://openflexo.org/test/TestViewPoint",
				project.getExpectedViewPointDirectory(), serviceManager.getViewPointLibrary());
		assertNotNull(newViewPoint);
		assertNotNull(newViewPoint.getResource());
	}


}
