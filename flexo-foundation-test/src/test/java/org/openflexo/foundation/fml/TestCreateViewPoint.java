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
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test ViewPoint creation facilities
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestCreateViewPoint extends OpenflexoTestCase {

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestResourceCenter/TestViewPoint.fml";

	static VirtualModel newViewPoint;
	static CompilationUnitResource newVirtualModelResource;

	private static DirectoryResourceCenter resourceCenter;

	/**
	 * Test the creation
	 * 
	 * @throws ModelDefinitionException
	 * @throws SaveResourceException
	 * @throws IOException
	 */
	@Test
	@TestOrder(1)
	public void testCreateViewPoint() throws SaveResourceException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();
		resourceCenter = makeNewDirectoryResourceCenter();
		assertNotNull(resourceCenter);
		System.out.println("ResourceCenter= " + resourceCenter);

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		CompilationUnitResourceFactory factory = fmlTechnologyAdapter.getCompilationUnitResourceFactory();

		newVirtualModelResource = factory.makeTopLevelCompilationUnitResource(VIEWPOINT_NAME, VIEWPOINT_URI,
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(), true);
		newViewPoint = newVirtualModelResource.getLoadedResourceData().getVirtualModel();

		assertNotNull(newViewPoint);
		newVirtualModelResource = newViewPoint.getResource();
		assertNotNull(newVirtualModelResource);
		assertTrue(newViewPoint.getResource().getDirectory() != null);
		assertTrue(newViewPoint.getResource().getIODelegate().exists());

		assertNotNull(newViewPoint.getCompilationUnit().getLocalizedDictionary());

		assertEquals(newViewPoint, newViewPoint.getDeclaringCompilationUnit());
		assertEquals(null, newViewPoint.getContainerVirtualModel());
		assertEquals(newViewPoint, newViewPoint.getFlexoConcept());
		assertEquals(newViewPoint, newViewPoint.getResourceData());

		assertEquals(VIEWPOINT_URI, newViewPoint.getURI());

		System.out.println("URI=" + newViewPoint.getURI());
		System.out.println("File:" + newViewPoint.getResource().getIODelegate().getSerializationArtefact());

	}

	/**
	 * Reload the ViewPoint<br>
	 * We first re-init a full ServiceManager, and copy the just created ViewPoint<br>
	 * The goal is to let the FileSystem monitoring system detects the new directory and instantiate ViewPoint
	 * 
	 * @throws IOException
	 */
	@Test
	@TestOrder(2)
	public void testReloadViewPoint() throws IOException {

		log("testReloadViewPoint()");

		instanciateTestServiceManager();

		serviceManager.getResourceCenterService().addToResourceCenters(resourceCenter = DirectoryResourceCenter
				.instanciateNewDirectoryResourceCenter(resourceCenter.getRootDirectory(), serviceManager.getResourceCenterService()));

		CompilationUnitResource retrievedVPResource1 = (CompilationUnitResource) serviceManager.getResourceManager()
				.getResource(VIEWPOINT_URI);
		assertNotNull(retrievedVPResource1);

		CompilationUnitResource retrievedVPResource2 = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(VIEWPOINT_URI);
		assertNotNull(retrievedVPResource2);

		assertSame(retrievedVPResource1, retrievedVPResource2);

		VirtualModel reloadedViewPoint = retrievedVPResource1.getCompilationUnit().getVirtualModel();
		assertEquals(reloadedViewPoint, reloadedViewPoint.getDeclaringCompilationUnit());
		assertEquals(null, reloadedViewPoint.getContainerVirtualModel());
		assertEquals(reloadedViewPoint, reloadedViewPoint.getFlexoConcept());
		assertEquals(reloadedViewPoint, reloadedViewPoint.getResourceData());

	}

}
