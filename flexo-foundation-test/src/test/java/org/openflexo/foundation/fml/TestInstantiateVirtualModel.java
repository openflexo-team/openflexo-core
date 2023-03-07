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

package org.openflexo.foundation.fml;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test View creation facilities with a ViewPoint created on the fly
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestInstantiateVirtualModel extends OpenflexoProjectAtRunTimeTestCase {

	private static VirtualModel virtualModel;
	private static FlexoConcept conceptA;
	private static FlexoConcept conceptB;
	private static FlexoEditor editor;
	private static FlexoProject<File> project;

	@Test
	@TestOrder(1)
	public void testLoadVirtualModel() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		instanciateTestServiceManager();
		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);
		virtualModel = vpLib
				.getVirtualModel("http://openflexo.org/test/TestResourceCenter/FML/TestInstantiateVirtualModels/TestInstantiate.fml");
		assertNotNull(virtualModel);

		System.out.println(virtualModel.getCompilationUnit().getFMLPrettyPrint());
		System.out.println(virtualModel.getCompilationUnit().getNormalizedFML());

	}

	// This should fail because no name provided
	@Test
	@TestOrder(2)
	public void testInstantiation1IsNotValid() {
		ActionScheme instantiation1 = (ActionScheme) virtualModel.getFlexoBehaviour("instantiation1", new Type[0]);
		assertObjectIsNotValid(instantiation1);
	}

	// Success
	@Test
	@TestOrder(3)
	public void testInstantiation2IsValid() {
		ActionScheme instantiation2 = (ActionScheme) virtualModel.getFlexoBehaviour("instantiation2", String.class);
		assertObjectIsValid(instantiation2);
	}

	// This should fail because default anonymous constructor does not define String parameter
	@Test
	@TestOrder(4)
	public void testInstantiation3IsNotValid() {
		ActionScheme instantiation3 = (ActionScheme) virtualModel.getFlexoBehaviour("instantiation3", new Type[0]);
		assertObjectIsNotValid(instantiation3);

	}

	// This should fail because no name provided
	@Test
	@TestOrder(5)
	public void testInstantiation4IsNotValid() {
		ActionScheme instantiation4 = (ActionScheme) virtualModel.getFlexoBehaviour("instantiation4", new Type[0]);
		assertObjectIsNotValid(instantiation4);

	}

	// Success
	@Test
	@TestOrder(6)
	public void testInstantiation5IsValid() {
		ActionScheme instantiation5 = (ActionScheme) virtualModel.getFlexoBehaviour("instantiation5", String.class);
		assertObjectIsValid(instantiation5);

	}

	// This should fail because foo is not known
	@Test
	@TestOrder(7)
	public void testInstantiation6IsNotValid() {
		ActionScheme instantiation6 = (ActionScheme) virtualModel.getFlexoBehaviour("instantiation6", new Type[0]);
		assertObjectIsNotValid(instantiation6);

	}

	// Success
	@Test
	@TestOrder(8)
	public void testInstantiation7IsValid() {
		ActionScheme instantiation7 = (ActionScheme) virtualModel.getFlexoBehaviour("instantiation7", String.class);
		assertObjectIsValid(instantiation7);

	}

	// This should fail because folder is not of right type
	@Test
	@TestOrder(9)
	public void testInstantiation8IsNotValid() {
		ActionScheme instantiation8 = (ActionScheme) virtualModel.getFlexoBehaviour("instantiation8", String.class);
		assertObjectIsNotValid(instantiation8);

	}

	// Success
	@Test
	@TestOrder(10)
	public void testInstantiation9IsValid() {
		ActionScheme instantiation9 = (ActionScheme) virtualModel.getFlexoBehaviour("instantiation9", String.class);
		assertObjectIsValid(instantiation9);

	}

}
