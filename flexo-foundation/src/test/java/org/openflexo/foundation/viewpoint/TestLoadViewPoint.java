package org.openflexo.foundation.viewpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.OpenflexoTestCase;
import org.openflexo.foundation.resource.JarResourceCenter;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intended to test ViewPoint loading
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestLoadViewPoint extends OpenflexoTestCase {

	/**
	 * Instanciate compound test resource center
	 */
	@Test
	@TestOrder(1)
	public void testinstanciateTestServiceManager() {
		instanciateTestServiceManager();
		JarResourceCenter.addNamedJarFromClassPathResourceCenters(getFlexoServiceManager().getResourceCenterService(), "testViewpoint2-1.0");
		
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

		FlexoConcept flexoConceptA = virtualModel.getFlexoConcept("FlexoConceptA");
		System.out.println("flexoConceptA=" + flexoConceptA);
		assertNotNull(flexoConceptA);

		FlexoConcept flexoConceptB = virtualModel.getFlexoConcept("FlexoConceptB");
		System.out.println("flexoConceptB=" + flexoConceptB);
		assertNotNull(flexoConceptB);

		FlexoConcept flexoConceptC = virtualModel.getFlexoConcept("FlexoConceptC");
		System.out.println("flexoConceptC=" + flexoConceptC);
		assertNotNull(flexoConceptC);

		FlexoConcept flexoConceptD = virtualModel.getFlexoConcept("FlexoConceptD");
		System.out.println("flexoConceptD=" + flexoConceptD);
		assertNotNull(flexoConceptD);

		FlexoConcept flexoConceptE = virtualModel.getFlexoConcept("FlexoConceptE");
		System.out.println("flexoConceptE=" + flexoConceptE);
		assertNotNull(flexoConceptE);

		assertEquals(3, flexoConceptE.getParentFlexoConcepts().size());
		assertEquals(flexoConceptA, flexoConceptE.getParentFlexoConcepts().get(0));
		assertEquals(flexoConceptB, flexoConceptE.getParentFlexoConcepts().get(1));
		assertEquals(flexoConceptC, flexoConceptE.getParentFlexoConcepts().get(2));

		assertEquals(1, flexoConceptA.getChildFlexoConcepts().size());
		assertEquals(flexoConceptE, flexoConceptA.getChildFlexoConcepts().get(0));
		assertEquals(3, flexoConceptB.getChildFlexoConcepts().size());
		assertEquals(flexoConceptC, flexoConceptB.getChildFlexoConcepts().get(0));
		assertEquals(flexoConceptD, flexoConceptB.getChildFlexoConcepts().get(1));
		assertEquals(flexoConceptE, flexoConceptB.getChildFlexoConcepts().get(2));
		assertEquals(1, flexoConceptC.getChildFlexoConcepts().size());
		assertEquals(flexoConceptE, flexoConceptC.getChildFlexoConcepts().get(0));

	}
	
	/**
	 * Test the loading
	 */
	@Test
	@TestOrder(3)
	public void testLoadViewPointFromAClassPathJar() {

		
		//JarResourceCenter.addNamedJarFromClassPathResourceCenters(getFlexoServiceManager().getResourceCenterService(), "org\\openflexo\\testViewpoint2\\1.0\\testViewpoint2-1.0");
				
		
		ViewPointLibrary vpLib = serviceManager.getViewPointLibrary();

		System.out.println("VPLibrary=" + vpLib);
		assertNotNull(vpLib);

		System.out.println("All vp= " + vpLib.getViewPoints());

		assertEquals(1, vpLib.getLoadedViewPoints().size());

		ViewPoint viewPoint = vpLib.getViewPoint("http://openflexo.org/test/TestViewPoint2");

		System.out.println("ViewPoint=" + viewPoint);

		assertNotNull(viewPoint);

		VirtualModel virtualModel = viewPoint.getVirtualModelNamed("TestVirtualModel");
		System.out.println("virtualModel=" + virtualModel);

		assertNotNull(virtualModel);

		FlexoConcept flexoConceptA = virtualModel.getFlexoConcept("FlexoConceptA");
		System.out.println("flexoConceptA=" + flexoConceptA);
		assertNotNull(flexoConceptA);

		FlexoConcept flexoConceptB = virtualModel.getFlexoConcept("FlexoConceptB");
		System.out.println("flexoConceptB=" + flexoConceptB);
		assertNotNull(flexoConceptB);

		FlexoConcept flexoConceptC = virtualModel.getFlexoConcept("FlexoConceptC");
		System.out.println("flexoConceptC=" + flexoConceptC);
		assertNotNull(flexoConceptC);

		FlexoConcept flexoConceptD = virtualModel.getFlexoConcept("FlexoConceptD");
		System.out.println("flexoConceptD=" + flexoConceptD);
		assertNotNull(flexoConceptD);

		FlexoConcept flexoConceptE = virtualModel.getFlexoConcept("FlexoConceptE");
		System.out.println("flexoConceptE=" + flexoConceptE);
		assertNotNull(flexoConceptE);

		assertEquals(3, flexoConceptE.getParentFlexoConcepts().size());
		assertEquals(flexoConceptA, flexoConceptE.getParentFlexoConcepts().get(0));
		assertEquals(flexoConceptB, flexoConceptE.getParentFlexoConcepts().get(1));
		assertEquals(flexoConceptC, flexoConceptE.getParentFlexoConcepts().get(2));

		assertEquals(1, flexoConceptA.getChildFlexoConcepts().size());
		assertEquals(flexoConceptE, flexoConceptA.getChildFlexoConcepts().get(0));
		assertEquals(3, flexoConceptB.getChildFlexoConcepts().size());
		assertEquals(flexoConceptC, flexoConceptB.getChildFlexoConcepts().get(0));
		assertEquals(flexoConceptD, flexoConceptB.getChildFlexoConcepts().get(1));
		assertEquals(flexoConceptE, flexoConceptB.getChildFlexoConcepts().get(2));
		assertEquals(1, flexoConceptC.getChildFlexoConcepts().size());
		assertEquals(flexoConceptE, flexoConceptC.getChildFlexoConcepts().get(0));

	}

}
