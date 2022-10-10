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

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.exception.InvalidBindingException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
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
public class TestSingleInheritance2 extends OpenflexoProjectAtRunTimeTestCase {

	private static VirtualModel virtualModel;
	private static FlexoConcept conceptA;
	private static FlexoConcept conceptB;
	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FMLRTVirtualModelInstance vmi;
	private static FlexoConceptInstance a, b;

	@Test
	@TestOrder(1)
	public void testLoadVirtualModel() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		instanciateTestServiceManager();
		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);
		virtualModel = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestSingleInheritance2.fml");
		assertNotNull(virtualModel);

		conceptA = virtualModel.getFlexoConcept("ConceptA");
		assertNotNull(conceptA);
		conceptB = virtualModel.getFlexoConcept("ConceptB");
		assertNotNull(conceptB);

		assertTrue(conceptA.isSuperConceptOf(conceptB));

		/*System.out.println(virtualModel.getCompilationUnit().getFMLPrettyPrint());
		
		System.out.println("ConceptA BindingModel = " + conceptA.getBindingModel());
		System.out.println("ConceptB BindingModel = " + conceptB.getBindingModel());
		
		System.out.println("parents=" + conceptB.getParentFlexoConcepts());
		assertSameList(conceptB.getParentFlexoConcepts(), conceptA);
		
		CreationScheme createB = conceptB.getCreationSchemes().get(0);
		Sequence s1 = (Sequence) createB.getControlGraph();
		Sequence s2 = (Sequence) s1.getControlGraph2();
		AssignationAction<?> a = (AssignationAction<?>) s2.getControlGraph2();
		
		System.out.println(a.getFMLPrettyPrint());
		System.out.println("a BindingModel = " + a.getBindingModel());*/

		assertVirtualModelIsValid(virtualModel);
	}

	@Test
	@TestOrder(2)
	public void testCreateProject() {
		editor = createStandaloneProject("TestProject");
		project = (FlexoProject<File>) editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
	}

	@Test
	@TestOrder(3)
	public void testCreateVMI() {
		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType
				.makeNewAction(project.getVirtualModelInstanceRepository().getRootFolder(), null, editor);
		action.setNewVirtualModelInstanceName("MyVMI");
		action.setNewVirtualModelInstanceTitle("Test creation of a new VMI");
		action.setVirtualModel(virtualModel);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		vmi = action.getNewVirtualModelInstance();
		assertNotNull(vmi);
		assertNotNull(vmi.getResource());
		// assertTrue(((ViewResource) vmi.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource) vmi.getResource()).getFile().exists());
		assertTrue(((FMLRTVirtualModelInstanceResource) vmi.getResource()).getDirectory() != null);
		assertTrue(((FMLRTVirtualModelInstanceResource) vmi.getResource()).getIODelegate().exists());
	}

	@Test
	@TestOrder(4)
	public void testCreateA() throws TypeMismatchException, NullReferenceException, InvalidBindingException, ReflectiveOperationException {

		CreateFlexoConceptInstance action = CreateFlexoConceptInstance.actionType.makeNewAction(vmi, null, editor);
		action.setFlexoConcept(conceptA);

		CreationScheme cs = conceptA.getCreationSchemes().get(0);
		action.setCreationScheme(cs);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		a = action.getNewFlexoConceptInstance();
		assertNotNull(a);

		assertEquals("ConceptA", a.execute("this.foo"));
		assertEquals(42, (int) a.execute("this.doSomething()"));
	}

	@Test
	@TestOrder(5)
	public void testCreateB() throws TypeMismatchException, NullReferenceException, InvalidBindingException, ReflectiveOperationException {

		CreateFlexoConceptInstance action = CreateFlexoConceptInstance.actionType.makeNewAction(vmi, null, editor);
		action.setFlexoConcept(conceptB);
		CreationScheme cs = conceptB.getCreationSchemes().get(0);
		action.setCreationScheme(cs);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		b = action.getNewFlexoConceptInstance();
		assertNotNull(b);

		assertEquals("ConceptAConceptB", b.execute("this.foo"));
		assertEquals(84, (long) b.execute("this.doSomething()"));
	}
}
