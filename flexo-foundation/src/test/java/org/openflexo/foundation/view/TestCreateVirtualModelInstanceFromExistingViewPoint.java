package org.openflexo.foundation.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateView;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test View creation facilities with a ViewPoint created on the fly
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestCreateVirtualModelInstanceFromExistingViewPoint extends OpenflexoProjectAtRunTimeTestCase {

	private static ViewPoint viewPoint;
	private static VirtualModel virtualModel;
	private static FlexoEditor editor;
	private static FlexoProject project;
	private static View newView;
	private static VirtualModelInstance newVirtualModelInstance;

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
		assertEquals(1, viewPoint.getVirtualModels().size());
		virtualModel = viewPoint.getVirtualModels().get(0);
		assertNotNull(virtualModel);
	}

	@Test
	@TestOrder(2)
	public void testCreateProject() {

		log("testCreateProject()");

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

		log("testCreateView()");

		CreateView action = CreateView.actionType.makeNewAction(project.getViewLibrary().getRootFolder(), null, editor);
		action.setNewViewName("MyView");
		action.setNewViewTitle("Test creation of a new view");
		action.setViewpointResource((ViewPointResource) viewPoint.getResource());
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		newView = action.getNewView();
		assertNotNull(newView);
		assertNotNull(newView.getResource());
		try {
			newView.getResource().save(null);
		} catch (SaveResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//assertTrue(((ViewResource) newView.getResource()).getDirectory().exists());
		//assertTrue(((ViewResource) newView.getResource()).getFile().exists());
		assertTrue(((ViewResource) newView.getResource()).getDirectory()!=null);
		assertTrue(((ViewResource) newView.getResource()).getFlexoIODelegate().exists());

		assertNotNull(project.getResource(newView.getURI()));
		assertNotNull(project.getViewLibrary().getResource(newView.getURI()));

	}

	/**
	 * Instantiate in project a VirtualModelInstance conform to the VirtualModel
	 */
	@Test
	@TestOrder(4)
	public void testCreateVirtualModelInstance() {

		log("testCreateVirtualModelInstance()");

		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		action.setNewVirtualModelInstanceName("MyVirtualModelInstance");
		action.setNewVirtualModelInstanceTitle("Test creation of a new VirtualModelInstance");
		action.setVirtualModel(virtualModel);
		action.doAction();
		System.out.println(action.getThrownException());
		assertTrue(action.hasActionExecutionSucceeded());
		newVirtualModelInstance = action.getNewVirtualModelInstance();
		assertNotNull(newVirtualModelInstance);
		assertNotNull(newVirtualModelInstance.getResource());
		//assertTrue(((ViewResource) newView.getResource()).getDirectory().exists());
		//assertTrue(((ViewResource) newView.getResource()).getFile().exists());
		assertTrue(((ViewResource) newView.getResource()).getDirectory()!=null);
		assertTrue(((ViewResource) newView.getResource()).getFlexoIODelegate().exists());

		// Not relevant anymore since reflexive model slot has disappeared from 1.7.0-beta to 1.7.0 version
		// assertEquals(1, newVirtualModelInstance.getModelSlotInstances().size());
		// VirtualModelModelSlotInstance reflexiveMSInstance = (VirtualModelModelSlotInstance)
		// newVirtualModelInstance.getModelSlotInstances()
		// .get(0);
		// assertNotNull(reflexiveMSInstance);
		// assertEquals(newVirtualModelInstance, reflexiveMSInstance.getAccessedResourceData());

		assertEquals(virtualModel, newVirtualModelInstance.getFlexoConcept());
		assertEquals(virtualModel, newVirtualModelInstance.getVirtualModel());
	}

	/**
	 * Instantiate in project a VirtualModelInstance conform to the VirtualModel
	 * 
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	@Test
	@TestOrder(5)
	public void testReloadProject() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		log("testReloadProject()");

		FlexoProject oldProject = project;
		instanciateTestServiceManager();
		editor = reloadProject(project.getDirectory());
		project = editor.getProject();
		assertNotSame(oldProject, project);
		assertNotNull(editor);
		assertNotNull(project);
		ViewResource newViewResource = project.getViewLibrary().getView(newView.getURI());
		assertNotNull(newViewResource);
		assertNull(newViewResource.getLoadedResourceData());
		newViewResource.loadResourceData(null);
		assertNotNull(newViewResource.getLoadedResourceData());
		newView = newViewResource.getLoadedResourceData();

		System.out.println("All resources=" + project.getAllResources());
		assertNotNull(project.getResource(newView.getURI()));

		ViewPointResource vpRes = newViewResource.getViewPointResource();
		viewPoint = vpRes.getResourceData(null);
		assertNotNull(viewPoint);
		assertEquals(1, viewPoint.getVirtualModels().size());
		virtualModel = viewPoint.getVirtualModels().get(0);

		assertEquals(1, newViewResource.getVirtualModelInstanceResources().size());
		VirtualModelInstanceResource vmiRes = (VirtualModelInstanceResource) newViewResource.getVirtualModelInstanceResources().get(0);
		newVirtualModelInstance = vmiRes.getVirtualModelInstance();

		assertEquals(virtualModel, newVirtualModelInstance.getFlexoConcept());
		assertEquals(virtualModel, newVirtualModelInstance.getVirtualModel());
	}

}
