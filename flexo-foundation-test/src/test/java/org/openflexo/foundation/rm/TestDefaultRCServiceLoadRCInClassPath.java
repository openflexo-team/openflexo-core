/**
 * 
 * Copyright (c) 2016, Openflexo
 * 
 * This file is part of Integration-tests, a component of the software infrastructure 
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

package org.openflexo.foundation.rm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.resource.DefaultResourceCenterService;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * We test here CartoEditor viewpoint
 * 
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestDefaultRCServiceLoadRCInClassPath extends OpenflexoTestCase {

	public static FlexoProject project;
	private static DefaultResourceCenterService rcService;
	private static VirtualModel testVP;

	private static FlexoResourceCenter testRC, testRCfromCP;

	/**
	 * Instantiate test resource center
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked" })
	@Test
	@TestOrder(1)
	public void instantiateResourceCenter() throws IOException {

		log("test0InstantiateResourceCenter()");

		instanciateTestServiceManager(FMLTechnologyAdapter.class, FMLRTTechnologyAdapter.class);

		rcService = (DefaultResourceCenterService) serviceManager.getResourceCenterService();
		assertNotNull(rcService);

		testRC = makeNewDirectoryResourceCenter();

		for (FlexoResourceCenter rc : rcService.getResourceCenters()) {
			log("FOUND: RC name " + rc.getName() + "  [" + rc.getDefaultBaseURI() + "]");
			if (rc.getDefaultBaseURI().equals("http://openflexo.org/test/TestResourceCenter")) {
				testRC = rc;
			}
			if (rc.getDefaultBaseURI().equals("http://openflexo.org/test/flexo-test-resources")) {
				testRCfromCP = rc;
			}
		}

		assertNotNull(testRC);

		assertFalse(testRC.getResourceCenterEntry().isSystemEntry());
		assertTrue(testRCfromCP.getResourceCenterEntry().isSystemEntry());
	}

	@Test
	@TestOrder(3)
	public void loadViewPoint() {

		log("loadViewPoint");

		String viewPointURI = "http://openflexo.org/test/TestResourceCenter/TestViewPointA.fml";
		log("Testing ViewPoint loading: " + viewPointURI);

		VirtualModelResource vpRes = serviceManager.getVirtualModelLibrary().getVirtualModelResource(viewPointURI);

		assertNotNull(vpRes);
		assertFalse(vpRes.isLoaded());

		testVP = vpRes.getVirtualModel();
		assertTrue(vpRes.isLoaded());

	}

	@Test
	@TestOrder(4)
	public void testViewPoint() {

		log("testViewPoint");

		assertNotNull(testVP);
		System.out.println("Found view point in " + ((VirtualModelResource) testVP.getResource()).getIODelegate().toString());
		assertVirtualModelIsValid(testVP);

	}

}
