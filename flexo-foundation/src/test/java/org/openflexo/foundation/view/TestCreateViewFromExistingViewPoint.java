package org.openflexo.foundation.view;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.action.CreateView;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test View creation facilities with a ViewPoint created on the fly
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestCreateViewFromExistingViewPoint extends OpenflexoProjectAtRunTimeTestCase {

	private static ViewPoint viewPoint;
	private static FlexoEditor editor;
	private static FlexoProject project;
	private static View newView;

	/**
	 * Retrieve the ViewPoint
	 */
	@Test
	@TestOrder(1)
	public void testLoadViewPoint() {
		instanciateTestServiceManager();
		ViewPointLibrary vpLib = serviceManager.getViewPointLibrary();
		assertNotNull(vpLib);
		viewPoint = vpLib.getViewPoint("http://openflexo.org/test/TestViewPoint1");
		assertNotNull(viewPoint);
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
	 * Instantiate in project a View conform to the ViewPoint
	 */
	@Test
	@TestOrder(3)
	public void testCreateView() {
		CreateView action = CreateView.actionType.makeNewAction(project.getViewLibrary().getRootFolder(), null, editor);
		action.setNewViewName("MyView");
		action.setNewViewTitle("Test creation of a new view");
		action.setViewpointResource((ViewPointResource) viewPoint.getResource());
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		newView = action.getNewView();
		assertNotNull(newView);
		assertNotNull(newView.getResource());
		//assertTrue(((ViewResource) newView.getResource()).getDirectory().exists());
		//assertTrue(((ViewResource) newView.getResource()).getFile().exists());
		assertTrue(((ViewResource) newView.getResource()).getDirectory()!=null);
		assertTrue(((ViewResource) newView.getResource()).getFlexoIODelegate().exists());
	}
}
