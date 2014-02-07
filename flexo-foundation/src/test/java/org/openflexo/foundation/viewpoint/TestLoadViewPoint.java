package org.openflexo.foundation.viewpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intended to test ViewPoint loading
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestLoadViewPoint extends ViewPointTestCase {

	/**
	 * Instanciate compound test resource center
	 */
	@Test
	@TestOrder(1)
	public void testinstanciateTestServiceManager() {
		instanciateTestServiceManager();

		System.out.println("ServiceManager= " + serviceManager);
		assertNotNull(serviceManager);

		System.out.println("ResourceCenter= " + resourceCenter);
		assertNotNull(resourceCenter);

	}

	/**
	 * Test the loading
	 */
	@Test
	@TestOrder(2)
	public void testLoadViewPoint() {

		ViewPointLibrary vpLib = serviceManager.getViewPointLibrary();

		System.out.println("VPLibrary=" + vpLib);
		assertNotNull(vpLib);

		System.out.println("All vp= " + vpLib.getViewPoints());

		assertEquals(0, vpLib.getLoadedViewPoints().size());

		ViewPoint viewPoint = vpLib.getViewPoint("http://openflexo.org/test/TestViewPoint1");

		System.out.println("ViewPoint=" + viewPoint);

		assertNotNull(viewPoint);

		VirtualModel virtualModel = viewPoint.getVirtualModelNamed("TestVirtualModel");
		System.out.println("virtualModel=" + virtualModel);

		assertNotNull(virtualModel);

		EditionPattern flexoConceptA = virtualModel.getEditionPattern("FlexoConceptA");
		System.out.println("flexoConceptA=" + flexoConceptA);
		assertNotNull(flexoConceptA);

		EditionPattern flexoConceptB = virtualModel.getEditionPattern("FlexoConceptB");
		System.out.println("flexoConceptB=" + flexoConceptB);
		assertNotNull(flexoConceptB);

		EditionPattern flexoConceptC = virtualModel.getEditionPattern("FlexoConceptC");
		System.out.println("flexoConceptC=" + flexoConceptC);
		assertNotNull(flexoConceptC);

		EditionPattern flexoConceptD = virtualModel.getEditionPattern("FlexoConceptD");
		System.out.println("flexoConceptD=" + flexoConceptD);
		assertNotNull(flexoConceptD);

		EditionPattern flexoConceptE = virtualModel.getEditionPattern("FlexoConceptE");
		System.out.println("flexoConceptE=" + flexoConceptE);
		assertNotNull(flexoConceptE);

		assertEquals(3, flexoConceptE.getParentEditionPatterns().size());
		assertEquals(flexoConceptA, flexoConceptE.getParentEditionPatterns().get(0));
		assertEquals(flexoConceptB, flexoConceptE.getParentEditionPatterns().get(1));
		assertEquals(flexoConceptC, flexoConceptE.getParentEditionPatterns().get(2));

		assertEquals(1, flexoConceptA.getChildEditionPatterns().size());
		assertEquals(flexoConceptE, flexoConceptA.getChildEditionPatterns().get(0));
		assertEquals(3, flexoConceptB.getChildEditionPatterns().size());
		assertEquals(flexoConceptC, flexoConceptB.getChildEditionPatterns().get(0));
		assertEquals(flexoConceptD, flexoConceptB.getChildEditionPatterns().get(1));
		assertEquals(flexoConceptE, flexoConceptB.getChildEditionPatterns().get(2));
		assertEquals(1, flexoConceptC.getChildEditionPatterns().size());
		assertEquals(flexoConceptE, flexoConceptC.getChildEditionPatterns().get(0));

	}

}
