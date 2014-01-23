package org.openflexo.foundation.viewpoint;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.viewpoint.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.viewpoint.VirtualModel.VirtualModelImpl;
import org.openflexo.foundation.viewpoint.rm.ViewPointResource;
import org.openflexo.foundation.viewpoint.rm.VirtualModelResource;

/**
 * This unit test is intented to test VirtualModel creation facilities
 * 
 * @author sylvain
 * 
 */
public class TestCreateVirtualModel extends ViewPointTestCase {

	static ViewPoint newViewPoint;

	/**
	 * Test the VP creation
	 */
	@Test
	public void testCreateViewPoint() {
		instanciateTestServiceManager();
		System.out.println("ResourceCenter= " + resourceCenter);
		newViewPoint = ViewPointImpl.newViewPoint("TestViewPoint",
				"http://openflexo.org/test/TestViewPoint",
				resourceCenter.getDirectory(),
				serviceManager.getViewPointLibrary());
		assertTrue(((ViewPointResource) newViewPoint.getResource())
				.getDirectory().exists());
		assertTrue(((ViewPointResource) newViewPoint.getResource()).getFile()
				.exists());
	}

	/**
	 * Test the VirtualModel creation
	 */
	@Test
	public void testCreateVirtualModel() throws SaveResourceException {
		VirtualModel newVirtualModel = VirtualModelImpl.newVirtualModel(
				"TestVirtualModel", newViewPoint);
		assertTrue(((VirtualModelResource) newVirtualModel.getResource())
				.getDirectory().exists());
		assertTrue(((VirtualModelResource) newVirtualModel.getResource())
				.getFile().exists());
	}

}
