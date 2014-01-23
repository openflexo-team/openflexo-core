package org.openflexo.foundation.viewpoint;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.viewpoint.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.viewpoint.rm.ViewPointResource;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test ViewPoint creation facilities
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestCreateViewPoint extends ViewPointTestCase {

	/**
	 * Test the creation
	 */
	@Test
	@TestOrder(1)
	public void testCreateViewPoint() {
		instanciateTestServiceManager();
		System.out.println("ResourceCenter= " + resourceCenter);
		ViewPoint newViewPoint = ViewPointImpl.newViewPoint("TestViewPoint",
				"http://openflexo.org/test/TestViewPoint",
				resourceCenter.getDirectory(),
				serviceManager.getViewPointLibrary());
		assertNotNull(newViewPoint);
		assertNotNull(newViewPoint.getResource());
		assertTrue(((ViewPointResource) newViewPoint.getResource())
				.getDirectory().exists());
		assertTrue(((ViewPointResource) newViewPoint.getResource()).getFile()
				.exists());

		assertNotNull(newViewPoint.getLocalizedDictionary());
	}

}
