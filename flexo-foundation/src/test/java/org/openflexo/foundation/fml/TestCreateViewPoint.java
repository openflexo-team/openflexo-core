package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.OpenflexoTestCase;
import org.openflexo.foundation.fml.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.FileUtils;

/**
 * This unit test is intented to test ViewPoint creation facilities
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestCreateViewPoint extends OpenflexoTestCase {

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestViewPoint";

	static ViewPoint newViewPoint;
	static ViewPointResource newViewPointResource;

	/**
	 * Test the creation
	 */
	@Test
	@TestOrder(1)
	public void testCreateViewPoint() {
		instanciateTestServiceManager();
		System.out.println("ResourceCenter= " + resourceCenter);
		newViewPoint = ViewPointImpl.newViewPoint(VIEWPOINT_NAME, VIEWPOINT_URI, resourceCenter.getDirectory(),
				serviceManager.getViewPointLibrary());
		assertNotNull(newViewPoint);
		newViewPointResource = (ViewPointResource) newViewPoint.getResource();
		assertNotNull(newViewPointResource);
		// assertTrue(((ViewPointResource) newViewPoint.getResource()).getDirectory().exists());
		// assertTrue(((ViewPointResource) newViewPoint.getResource()).getFile().exists());
		assertTrue(((ViewPointResource) newViewPoint.getResource()).getDirectory() != null);
		assertTrue(((ViewPointResource) newViewPoint.getResource()).getFlexoIODelegate().exists());

		assertNotNull(newViewPoint.getLocalizedDictionary());

		assertEquals(newViewPoint, newViewPoint.getViewPoint());
		assertEquals(newViewPoint, newViewPoint.getVirtualModel());
		assertEquals(null, newViewPoint.getOwningVirtualModel());
		assertEquals(newViewPoint, newViewPoint.getFlexoConcept());
		assertEquals(newViewPoint, newViewPoint.getResourceData());
	}

	/**
	 * Reload the ViewPoint<br>
	 * We first re-init a full ServiceManager, and copy the just created ViewPoint<br>
	 * The goal is to let the FileSystem monitoring system detects the new directory and instantiate ViewPoint
	 */
	@Test
	@TestOrder(2)
	public void testReloadViewPoint() {

		log("testReloadViewPoint()");

		instanciateTestServiceManager();
		File directory = ResourceLocator.retrieveResourceAsFile(newViewPointResource.getDirectory());
		File newDirectory = new File(((FileSystemBasedResourceCenter) resourceCenter).getDirectory(), directory.getName());
		newDirectory.mkdirs();

		try {
			FileUtils.copyContentDirToDir(directory, newDirectory);
			// We wait here for the thread monitoring ResourceCenters to detect new files
			Thread.sleep(3000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ViewPointResource retrievedVPResource = serviceManager.getViewPointLibrary().getViewPointResource(VIEWPOINT_URI);
		assertNotNull(retrievedVPResource);

		ViewPoint reloadedViewPoint = retrievedVPResource.getViewPoint();
		assertEquals(reloadedViewPoint, reloadedViewPoint.getViewPoint());
		assertEquals(reloadedViewPoint, reloadedViewPoint.getVirtualModel());
		assertEquals(null, reloadedViewPoint.getOwningVirtualModel());
		assertEquals(reloadedViewPoint, reloadedViewPoint.getFlexoConcept());
		assertEquals(reloadedViewPoint, reloadedViewPoint.getResourceData());

	}

}
