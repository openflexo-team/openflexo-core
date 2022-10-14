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
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
import org.openflexo.foundation.fml.binding.EditionActionBindingModel;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.controlgraph.Sequence.SequenceImpl;
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
public class TestReplaceWith2 extends OpenflexoTestCase {

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

		behaviour = (ActionScheme) vm.getFlexoBehaviour("testBehaviour2");
		assertNotNull(behaviour);
		
		System.out.println("FML: " + vm.getFMLPrettyPrint());

	}
	
	@Test
	@TestOrder(2)
	public void testReplaceWith1() {
		log("testReplaceWith1()");
		
		FMLControlGraph thenControlGraph = ((ConditionalAction)behaviour.getControlGraph()).getThenControlGraph();
		System.out.println("Owner: "+thenControlGraph.getOwner());
		
		
		FMLControlGraph controlGraph1 = ((Sequence)thenControlGraph).getControlGraph1();
		AssignationAction<?> assignationAction = (AssignationAction<?>)controlGraph1;
		System.out.println("controlGraph1="+controlGraph1+" of "+controlGraph1.getClass());
		
		EditionActionBindingModel bm = (EditionActionBindingModel)controlGraph1.getBindingModel();
		
		System.out.println("BM="+bm);
		System.out.println("CG="+bm.getControlGraph());
		System.out.println("Owner="+bm.getControlGraph().getOwner());
		System.out.println("Base="+bm.getBaseBindingModel());
	
		ControlGraphBindingModel bbm = (ControlGraphBindingModel)bm.getBaseBindingModel();
		System.out.println("BCG="+bbm.getControlGraph());
		
		SequenceImpl s1 = (SequenceImpl)bbm.getControlGraph();
		SequenceImpl s2 = (SequenceImpl)thenControlGraph;
		
		System.out.println("PP="+s1.getFMLPrettyPrint());
		System.out.println("PP="+s2.getFMLPrettyPrint());

		assertSame(s1,s2);
	
		System.out.println("BOwner="+bbm.getControlGraph().getOwner());
		System.out.println("BBase="+bbm.getBaseBindingModel());

		
		//System.exit(-1);
		
		System.out.println("BM-1=" + behaviour.getControlGraph().getBindingModel());
		System.out.println("BM-2=" + thenControlGraph.getBindingModel());
		System.out.println("BM-3=" + ((Sequence)thenControlGraph).getControlGraph1().getBindingModel());
		System.out.println("BM-4=" + ((Sequence)thenControlGraph).getControlGraph2().getBindingModel());

		
		List<? extends FMLControlGraph> flattenedSequence = thenControlGraph.getFlattenedSequence();
		assertEquals(3, flattenedSequence.size());

		System.out.println(thenControlGraph.getFMLPrettyPrint());
		
		AssignationAction<?> a1 = (AssignationAction<?>) flattenedSequence.get(0);
		AssignationAction<?> a2 = (AssignationAction<?>) flattenedSequence.get(1);
		AssignationAction<?> a3 = (AssignationAction<?>) flattenedSequence.get(2);

		System.out.println("BM1=" + a1.getBindingModel());
		System.out.println("BM2=" + a2.getBindingModel());
		System.out.println("BM3=" + a3.getBindingModel());

		assertEquals(3,a1.getBindingModel().getBindingVariablesCount()); // parameters, this, a
		assertEquals(3,a2.getBindingModel().getBindingVariablesCount()); // parameters, this, a
		assertEquals(3,a3.getBindingModel().getBindingVariablesCount()); // parameters, this, a

		ExpressionAction<?> newExpression1 = vm.getFMLModelFactory().newExpressionAction();
		newExpression1.setExpression(new DataBinding<>("this.a"));
		a1.replaceWith(newExpression1);

		System.out.println("FML: " + vm.getFMLPrettyPrint());
		
		flattenedSequence = thenControlGraph.getFlattenedSequence();
		assertSame(newExpression1,flattenedSequence.get(0));
		assertSame(a2,flattenedSequence.get(1));
		assertSame(a3,flattenedSequence.get(2));

		System.out.println("BM1=" + newExpression1.getBindingModel());
		System.out.println("BM2=" + a2.getBindingModel());
		System.out.println("BM3=" + a3.getBindingModel());

		assertEquals(3,newExpression1.getBindingModel().getBindingVariablesCount()); // parameters, this, a
		assertEquals(3,a2.getBindingModel().getBindingVariablesCount()); // parameters, this, a
		assertEquals(3,a3.getBindingModel().getBindingVariablesCount()); // parameters, this, a

	}

}
