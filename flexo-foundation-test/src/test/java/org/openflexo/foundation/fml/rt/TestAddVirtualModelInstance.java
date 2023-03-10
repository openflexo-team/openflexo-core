/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.fml.rt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.editionaction.AddVirtualModelInstance;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to {@link AddVirtualModelInstance} edition action
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestAddVirtualModelInstance extends OpenflexoProjectAtRunTimeTestCase {

	private static VirtualModel virtualModel;
	private static VirtualModel containedVM;
	private static CreationScheme creationScheme;

	@Test
	@TestOrder(1)
	public void testLoadVirtualModel() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		instanciateTestServiceManager();
		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);
		virtualModel = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestAddVirtualModelInstance.fml");
		assertNotNull(virtualModel);
		assertNotNull(containedVM = virtualModel.getVirtualModelNamed("MyVM"));

		CompilationUnitResource virtualModelResource = virtualModel.getResource();
		CompilationUnitResource containedVMResource = containedVM.getResource();

		assertTrue(virtualModelResource.getDependencies().contains(containedVMResource));

		assertVirtualModelIsValid(virtualModel);
		assertVirtualModelIsValid(containedVM);

		System.out.println("virtualModel: " + virtualModel.getCompilationUnit().getFMLPrettyPrint());
		assertEquals(1, virtualModel.getCompilationUnit().getElementImports().size());
	}

	@Test
	@TestOrder(2)
	public void testPrettyPrint() {
		System.out.println("containedVM: " + containedVM.getCompilationUnit().getFMLPrettyPrint());

		assertNotNull(creationScheme = virtualModel.getCreationSchemes().get(0));
		assertEquals("vm = new MyVM::createInstance(\"foo\") with (name=\"myVMInstance\");",
				creationScheme.getControlGraph().getFMLPrettyPrint());

	}

}
