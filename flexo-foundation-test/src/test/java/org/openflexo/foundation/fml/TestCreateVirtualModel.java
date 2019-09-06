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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.FileUtils;

/**
 * This unit test is intented to test VirtualModel creation facilities
 * 
 * TODO: write test with FlexoAction primitives only
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestCreateVirtualModel extends OpenflexoTestCase {

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestResourceCenter/TestViewPoint.fml";
	public static final String VIRTUAL_MODEL_NAME = "TestVirtualModel";

	static VirtualModel newViewPoint;
	static CompilationUnitResource newVirtualModelResource;

	private static DirectoryResourceCenter resourceCenter;

	/**
	 * Test the VP creation
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

		// assertTrue(newVirtualModelResource.getDirectory().exists());
		// assertTrue(newVirtualModelResource.getFile().exists());
		assertTrue(newVirtualModelResource.getDirectory() != null);
		assertTrue(newVirtualModelResource.getIODelegate().exists());

		assertEquals(newViewPoint, newViewPoint.getDeclaringCompilationUnit());
		assertEquals(null, newViewPoint.getContainerVirtualModel());
		assertEquals(newViewPoint, newViewPoint.getFlexoConcept());
		assertEquals(newViewPoint, newViewPoint.getResourceData());

	}

	/**
	 * Test the VirtualModel creation
	 * 
	 * @throws ModelDefinitionException
	 */
	@Test
	@TestOrder(2)
	public void testCreateVirtualModel() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		CompilationUnitResourceFactory factory = fmlTechnologyAdapter.getCompilationUnitResourceFactory();
		CompilationUnitResource newVMResource = factory.makeContainedCompilationUnitResource(VIRTUAL_MODEL_NAME, newViewPoint.getResource(),
				true);
		VirtualModel newVirtualModel = newVMResource.getLoadedResourceData().getVirtualModel();

		// VirtualModel newVirtualModel =
		// VirtualModelImpl.newVirtualModel(VIRTUAL_MODEL_NAME, newViewPoint);
		assertTrue(ResourceLocator.retrieveResourceAsFile(newVirtualModel.getResource().getDirectory()).exists());
		assertTrue(newVirtualModel.getResource().getIODelegate().exists());

		assertEquals(newViewPoint, newVirtualModel.getContainerVirtualModel());
		assertEquals(newVirtualModel, newVirtualModel.getFlexoConcept());
		assertEquals(newVirtualModel, newVirtualModel.getResourceData());

	}

	/**
	 * Reload the ViewPoint<br>
	 * We first re-init a full ServiceManager, and copy the just created ViewPoint<br>
	 * The goal is to let the FileSystem monitoring system detects the new directory and instantiate ViewPoint
	 * 
	 * @throws IOException
	 */
	@Test
	@TestOrder(3)
	public void testReloadViewPoint() throws IOException {

		log("testReloadViewPoint()");

		instanciateTestServiceManager();
		resourceCenter = makeNewDirectoryResourceCenter();

		File directory = ResourceLocator.retrieveResourceAsFile(newVirtualModelResource.getDirectory());
		File newDirectory = new File(((FileSystemBasedResourceCenter) resourceCenter).getRootDirectory(), directory.getName());
		newDirectory.mkdirs();

		try {
			FileUtils.copyContentDirToDir(directory, newDirectory);
			// We wait here for the thread monitoring ResourceCenters to detect
			// new files
			((FileSystemBasedResourceCenter) resourceCenter).performDirectoryWatchingNow();
		} catch (IOException e) {
			e.printStackTrace();
		}

		CompilationUnitResource retrievedVPResource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(VIEWPOINT_URI);
		assertNotNull(retrievedVPResource);

		VirtualModel reloadedViewPoint = retrievedVPResource.getCompilationUnit().getVirtualModel();
		assertEquals(reloadedViewPoint, reloadedViewPoint.getDeclaringCompilationUnit());
		assertEquals(null, reloadedViewPoint.getContainerVirtualModel());
		assertEquals(reloadedViewPoint, reloadedViewPoint.getFlexoConcept());
		assertEquals(reloadedViewPoint, reloadedViewPoint.getResourceData());

		VirtualModel reloadedVirtualModel = reloadedViewPoint.getVirtualModelNamed(VIRTUAL_MODEL_NAME);
		assertNotNull(reloadedVirtualModel);

		assertEquals(reloadedViewPoint, reloadedVirtualModel.getContainerVirtualModel());
		assertEquals(reloadedVirtualModel, reloadedVirtualModel.getDeclaringCompilationUnit());
		assertEquals(reloadedVirtualModel, reloadedVirtualModel.getFlexoConcept());
		assertEquals(reloadedVirtualModel, reloadedVirtualModel.getResourceData());

	}

}
