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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
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
public class TestReplaceWith extends OpenflexoTestCase {

	private static VirtualModel vm;
	private static ActionScheme behaviour;

	/**
	 * Instanciate service manager
	 * 
	 * @throws IOException
	 */
	@Test
	@TestOrder(1)
	public void testinstanciateTestServiceManager() throws IOException {
		instanciateTestServiceManager();
		assertNotNull(serviceManager);

	}

	/**
	 * Test the loading
	 * 
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	@Test
	@TestOrder(2)
	public void testLoadViewPoint() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		vm = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestReplaceWith.fml");
		assertNotNull(vm);
		System.out.println("FML: " + vm.getFMLPrettyPrint());

		behaviour = (ActionScheme) vm.getFlexoBehaviour("testBehaviour");
		assertNotNull(behaviour);

		List<? extends FMLControlGraph> flattenedSequence = behaviour.getControlGraph().getFlattenedSequence();
		assertEquals(4, flattenedSequence.size());

		AssignationAction<?> a1 = (AssignationAction<?>) flattenedSequence.get(0);
		AssignationAction<?> a2 = (AssignationAction<?>) flattenedSequence.get(1);
		AssignationAction<?> a3 = (AssignationAction<?>) flattenedSequence.get(2);
		AssignationAction<?> a4 = (AssignationAction<?>) flattenedSequence.get(3);

		System.out.println("BM1=" + a4.getBindingModel());

		ExpressionAction<?> newExpression1 = vm.getFMLModelFactory().newExpressionAction();
		newExpression1.setExpression(new DataBinding<>("this.a"));
		a3.replaceWith(newExpression1);

		ExpressionAction<?> newExpression2 = vm.getFMLModelFactory().newExpressionAction();
		newExpression2.setExpression(new DataBinding<>("this.a+1"));
		a4.replaceWith(newExpression2);

		System.out.println("FML: " + vm.getFMLPrettyPrint());
		System.out.println("BM2=" + newExpression1.getBindingModel());
		System.out.println("BM3=" + newExpression2.getBindingModel());

	}
}
