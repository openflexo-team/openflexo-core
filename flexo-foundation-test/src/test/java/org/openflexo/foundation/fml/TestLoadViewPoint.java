/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.resource.JarResourceCenter;
import org.openflexo.foundation.test.OpenflexoTestCase;
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
		JarResourceCenter.addNamedJarFromClassPath(getFlexoServiceManager().getResourceCenterService(), "testViewpoint2-1.0");

		System.out.println("ServiceManager= " + serviceManager);
		assertNotNull(serviceManager);

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

		ViewPoint viewPoint = vpLib.getViewPoint("http://openflexo.org/test/TestViewPointA");

		System.out.println("ViewPoint=" + viewPoint);

		assertNotNull(viewPoint);

		VirtualModel virtualModel = viewPoint.getVirtualModelNamed("TestVirtualModel");
		System.out.println("virtualModel=" + virtualModel);

		assertNotNull(virtualModel);

		FlexoConcept flexoConceptA = virtualModel.getFlexoConcept("FlexoConceptA");
		System.out.println("flexoConcept=" + flexoConceptA);
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
