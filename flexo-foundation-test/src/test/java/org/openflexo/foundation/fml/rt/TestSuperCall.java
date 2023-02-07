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
import java.util.logging.Logger;

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
import org.openflexo.logging.FlexoLogger;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test FML with super() call
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestSuperCall extends OpenflexoProjectAtRunTimeTestCase {

	private static final Logger logger = FlexoLogger.getLogger(TestSuperCall.class.getPackage().getName());

	private static VirtualModel virtualModel;
	private static FlexoConcept parentConcept;
	private static FlexoConcept childConcept;
	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FMLRTVirtualModelInstance vmi;
	private static FlexoConceptInstance fci;

	@Test
	@TestOrder(1)
	public void testLoadVirtualModel() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		instanciateTestServiceManager();
		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);
		virtualModel = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestSuperCall.fml");
		assertNotNull(virtualModel);

		parentConcept = virtualModel.getFlexoConcept("ParentConcept");
		assertNotNull(parentConcept);
		childConcept = virtualModel.getFlexoConcept("ChildConcept");
		assertNotNull(childConcept);

		assertTrue(parentConcept.isSuperConceptOf(childConcept));

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
	public void testInstantiateConcept()
			throws TypeMismatchException, NullReferenceException, InvalidBindingException, ReflectiveOperationException {

		CreateFlexoConceptInstance action = CreateFlexoConceptInstance.actionType.makeNewAction(vmi, null, editor);
		action.setFlexoConcept(childConcept);

		CreationScheme cs = childConcept.getCreationSchemes().get(0);
		action.setCreationScheme(cs);
		action.setParameterValue(cs.getParameter("arg"), "foo");
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		fci = action.getNewFlexoConceptInstance();
		assertNotNull(fci);

		assertEquals("foo", fci.execute("this.foo"));
	}

}
