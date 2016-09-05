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
import static org.junit.Assert.assertSame;

import java.io.FileNotFoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.OpenflexoTestCase;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.JarResourceCenter;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.rm.InJarResourceImpl;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intended to test JarResourceCenter
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestJarResourceCenter extends OpenflexoTestCase {

	private static JarResourceCenter jarResourceCenter;

	/**
	 * Instanciate compound test resource center
	 */
	@Test
	@TestOrder(1)
	public void testinstanciateTestServiceManager() {
		instanciateTestServiceManager();
		jarResourceCenter = JarResourceCenter.addNamedJarFromClassPath(getFlexoServiceManager().getResourceCenterService(),
				"testViewpoint2-1.0");

		System.out.println("ServiceManager= " + serviceManager);
		assertNotNull(serviceManager);

		System.out.println("jarResourceCenter= " + jarResourceCenter);
		assertNotNull(jarResourceCenter);

	}

	@Test
	@TestOrder(2)
	public void analyzeSerializationArtefactsForJarResourCenter() {

		InJarResourceImpl rootEntry = jarResourceCenter.getBaseArtefact();
		assertNotNull(rootEntry);
		assertEquals(1, rootEntry.getContents().size());

		InJarResourceImpl resourceEntry = rootEntry.getContents().get(0);
		assertEquals(1, resourceEntry.getContents().size());

		InJarResourceImpl testResourceCenterEntry = resourceEntry.getContents().get(0);
		assertEquals(1, testResourceCenterEntry.getContents().size());

		InJarResourceImpl viewpointsEntry = testResourceCenterEntry.getContents().get(0);
		assertEquals(1, viewpointsEntry.getContents().size());

		InJarResourceImpl vp2Entry = viewpointsEntry.getContents().get(0);
		assertEquals(3, vp2Entry.getContents().size());

	}

	@Test
	@TestOrder(3)
	public void analyzeResourcesForJarResourCenter() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		System.out.println("all resources = " + jarResourceCenter.getAllResources());

		assertEquals(2, jarResourceCenter.getAllResources().size());

		FlexoResource<?> viewPointFromRM = serviceManager.getResourceManager().getResource("http://openflexo.org/test/TestViewPoint2");
		System.out.println("viewPointFromRM=" + viewPointFromRM);
		assertNotNull(viewPointFromRM);

		ViewPoint viewPoint = serviceManager.getViewPointLibrary().getViewPoint("http://openflexo.org/test/TestViewPoint2");
		System.out.println("ViewPoint=" + viewPoint);
		assertNotNull(viewPoint);

		assertSame(viewPointFromRM.getResourceData(null), viewPoint);

		System.out.println("Contents=" + viewPoint.getResource().getContents());

		VirtualModel virtualModel = viewPoint.getVirtualModelNamed("TestVirtualModel");
		System.out.println("virtualModel=" + virtualModel);

		assertNotNull(virtualModel);
	}

}
