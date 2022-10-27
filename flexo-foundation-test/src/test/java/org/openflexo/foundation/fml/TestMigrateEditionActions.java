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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.binding.CreationSchemePathElement;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
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
public class TestMigrateEditionActions extends OpenflexoTestCase {

	private static VirtualModel migrationVM;
	private static VirtualModel insideVM;
	private static FlexoConcept foo;
	private static FlexoConcept foo2;
	private static ActionScheme testNewInstance;
	private static ActionScheme testNewEmbeddedInstance;
	private static ActionScheme testNewEmbeddedVirtualModelInstance;

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
		assertNotNull(vpLib);
		System.out.println("All vp= " + vpLib.getCompilationUnitResources());

		for (CompilationUnitResource compilationUnitResource : vpLib.getCompilationUnitResources()) {
			System.out.println(" > " + compilationUnitResource + " URI=" + compilationUnitResource.getURI());
		}

		assertTrue(serviceManager.getResourceManager().getResource("http://openflexo.org/test/TestResourceCenter/MigrationVM.fml") != null);
		assertTrue(serviceManager.getResourceManager().getResource("http://openflexo.org/test/TestResourceCenter/MigrationVM.fml/InsideVM.fml") != null);
		
		assertEquals(0, vpLib.getLoadedCompilationUnits().size());

		CompilationUnitResource compilationUnitResource = vpLib
				.getCompilationUnitResource("http://openflexo.org/test/TestResourceCenter/MigrationVM.fml");
		System.out.println("compilationUnitResource=" + compilationUnitResource);

		compilationUnitResource.loadResourceData();

		migrationVM = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/MigrationVM.fml");
		assertNotNull(migrationVM);
		System.out.println("migrationVM: " + migrationVM);
		System.out.println("FML: " + migrationVM.getFMLPrettyPrint());

		insideVM = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/MigrationVM.fml/InsideVM.fml");
		assertNotNull(insideVM);
		System.out.println("insideVM: " + insideVM);
		System.out.println("FML: " + insideVM.getFMLPrettyPrint());

		foo = migrationVM.getFlexoConcept("Foo");
		assertNotNull(foo);

		foo2 = migrationVM.getFlexoConcept("Foo2");
		assertNotNull(foo2);
	}

	@Test
	@TestOrder(3)
	public void testNewInstanceBehaviour() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		testNewInstance = (ActionScheme) migrationVM.getFlexoBehaviour("testNewInstance");
		assertNotNull(testNewInstance);

		List<? extends FMLControlGraph> flattenedSequence = testNewInstance.getControlGraph().getFlattenedSequence();
		assertEquals(4, flattenedSequence.size());

		DeclarationAction<?> a1 = (DeclarationAction<?>) flattenedSequence.get(0);
		ExpressionAction<?> e1 = (ExpressionAction<?>) a1.getAssignableAction();
		BindingPath bv1 = (BindingPath) e1.getExpression().getExpression();
		assertEquals(null, bv1.getBindingVariable());
		assertEquals(1, bv1.getBindingPath().size());
		assertTrue(bv1.getBindingPath().get(0) instanceof CreationSchemePathElement);
		assertTrue(e1.getExpression().isValid());
		assertEquals(foo.getInstanceType(), a1.getAnalyzedType());

		ExpressionAction<?> a2 = (ExpressionAction<?>) flattenedSequence.get(1);
		BindingPath bv2 = (BindingPath) a2.getExpression().getExpression();
		assertEquals(null, bv2.getBindingVariable());
		assertEquals(1, bv2.getBindingPath().size());
		assertTrue(bv2.getBindingPath().get(0) instanceof CreationSchemePathElement);
		assertTrue(a2.getExpression().isValid());
		assertEquals(foo.getInstanceType(), a2.getAssignableType());

		AssignationAction<?> a3 = (AssignationAction<?>) flattenedSequence.get(2);
		ExpressionAction<?> e3 = (ExpressionAction<?>) a3.getAssignableAction();
		BindingPath bv3 = (BindingPath) e3.getExpression().getExpression();
		assertEquals(null, bv3.getBindingVariable());
		assertEquals(1, bv3.getBindingPath().size());
		assertTrue(bv3.getBindingPath().get(0) instanceof CreationSchemePathElement);
		assertTrue(e3.getExpression().isValid());
		assertEquals(foo.getInstanceType(), a3.getAssignableType());

		/*DeclarationAction<?> a4 = (DeclarationAction<?>) flattenedSequence.get(3);
		ExpressionAction<?> e4 = (ExpressionAction<?>) a4.getAssignableAction();
		BindingPath bv4 = (BindingPath) e4.getExpression().getExpression();
		assertEquals(migrationVM.getBindingModel().getBindingVariableNamed("aFoo"), bv4.getBindingVariable());
		assertEquals(1, bv4.getBindingPath().size());
		assertTrue(bv4.getBindingPath().get(0) instanceof CreationSchemePathElement);
		//System.out.println("Invalid:" + e4.getExpression().invalidBindingReason());
		//assertSame(e4, e4.getExpression().getOwner());
		
		//System.out.println("o1=" + e4.getExpression().getOwner());
		//System.out.println("BM=" + e4.getExpression().getOwner().getBindingModel());
		//System.out.println("bv=" + e4.getExpression().getOwner().getBindingModel().getBindingVariableNamed("aFoo"));
		
		assertTrue(e4.getExpression().isValid());
		assertEquals(foo2.getInstanceType(), a4.getAssignableType());*/

	}

	@Test
	@TestOrder(4)
	public void testNewEmbeddedInstanceBehaviour() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		testNewEmbeddedInstance = (ActionScheme) migrationVM.getFlexoBehaviour("testNewEmbeddedInstance");
		assertNotNull(testNewInstance);

		DeclarationAction<?> a4 = (DeclarationAction<?>) testNewEmbeddedInstance.getControlGraph();
		ExpressionAction<?> e4 = (ExpressionAction<?>) a4.getAssignableAction();
		BindingPath bv4 = (BindingPath) e4.getExpression().getExpression();
		assertEquals(migrationVM.getBindingModel().getBindingVariableNamed("aFoo"), bv4.getBindingVariable());
		assertEquals(1, bv4.getBindingPath().size());
		assertTrue(bv4.getBindingPath().get(0) instanceof CreationSchemePathElement);
		assertTrue(e4.getExpression().isValid());
		assertEquals(foo2.getInstanceType(), a4.getAssignableType());
	}

	@Test
	@TestOrder(5)
	public void testNewEmbeddedVirtualModelInstanceBehaviour()
			throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		testNewEmbeddedVirtualModelInstance = (ActionScheme) migrationVM.getFlexoBehaviour("testNewEmbeddedVirtualModelInstance");
		assertNotNull(testNewEmbeddedVirtualModelInstance);

		DeclarationAction<?> a4 = (DeclarationAction<?>) testNewEmbeddedVirtualModelInstance.getControlGraph();
		ExpressionAction<?> e4 = (ExpressionAction<?>) a4.getAssignableAction();
		BindingPath bv4 = (BindingPath) e4.getExpression().getExpression();
		assertEquals(migrationVM.getBindingModel().getBindingVariableNamed("this"), bv4.getBindingVariable());
		assertEquals(1, bv4.getBindingPath().size());
		assertTrue(bv4.getBindingPath().get(0) instanceof CreationSchemePathElement);
		assertTrue(e4.getExpression().isValid());
		assertEquals(insideVM.getInstanceType(), a4.getAssignableType());
	}

}
