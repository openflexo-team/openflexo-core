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
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rt.action.ActionSchemeAction;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionType;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test FlexoConceptInstance embedding features
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestEmbeddedFlexoConceptInstances extends OpenflexoProjectAtRunTimeTestCase {

	private static VirtualModel viewPoint;
	private static VirtualModel vm1;
	private static FlexoConcept conceptA;
	private static FlexoConcept conceptB;
	private static FlexoConcept conceptC;

	private static FlexoEditor editor;
	private static FlexoProject project;
	private static VirtualModelInstance newView;
	private static VirtualModelInstance vmi1;
	private static VirtualModelInstance vmi2;

	/**
	 * Retrieve the ViewPoint
	 * 
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	@Test
	@TestOrder(1)
	public void testLoadViewPoint() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		instanciateTestServiceManager();
		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);
		viewPoint = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestViewPointB.fml");
		assertNotNull(viewPoint);
		assertNotNull(vm1 = viewPoint.getVirtualModelNamed("VM1"));
		assertNotNull(conceptA = vm1.getFlexoConcept("ConceptA"));
		assertNotNull(conceptB = vm1.getFlexoConcept("ConceptB"));
		assertNotNull(conceptC = vm1.getFlexoConcept("ConceptC"));

		assertVirtualModelIsValid(viewPoint);

	}

	@Test
	@TestOrder(2)
	public void testCreateProject() {
		editor = createProject("TestProject");
		project = editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
		assertTrue(project.getProjectDataResource().getIODelegate().exists());
	}

	/**
	 * Instantiate in project a View conform to the ViewPoint
	 */
	@Test
	@TestOrder(3)
	public void testCreateView() {
		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType
				.makeNewAction(project.getVirtualModelInstanceRepository().getRootFolder(), null, editor);
		action.setNewVirtualModelInstanceName("MyView");
		action.setNewVirtualModelInstanceTitle("Test creation of a new view");
		action.setVirtualModel(viewPoint);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		newView = action.getNewVirtualModelInstance();
		assertNotNull(newView);
	}

	/**
	 * Instantiate in View a VirtualModelInstance
	 */
	@Test
	@TestOrder(4)
	public void testCreateVirtualModelInstance() {
		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		action.setNewVirtualModelInstanceName("MyVMI1");
		action.setNewVirtualModelInstanceTitle("MyVMI1");
		action.setVirtualModel(vm1);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		vmi1 = action.getNewVirtualModelInstance();
		assertNotNull(vmi1);
	}

	private static FlexoConceptInstance conceptInstanceA1;
	private static FlexoConceptInstance conceptInstanceA2;
	private static FlexoConceptInstance conceptInstanceB1;
	private static FlexoConceptInstance conceptInstanceB2;
	private static FlexoConceptInstance conceptInstanceB3;
	private static FlexoConceptInstance conceptInstanceC1;
	private static FlexoConceptInstance conceptInstanceC2;
	private static FlexoConceptInstance conceptInstanceC3;

	/**
	 * Populate virtual model instance
	 */
	@Test
	@TestOrder(5)
	public void testPopulateVMI1() {
		assertNotNull(conceptInstanceA1 = createInstance(conceptA, vmi1, "ConceptInstanceA1"));
		assertNotNull(conceptInstanceA2 = createInstance(conceptA, vmi1, "ConceptInstanceA2"));
		assertNotNull(conceptInstanceB1 = createInstance(conceptB, conceptInstanceA1, "ConceptInstanceB1"));
		assertNotNull(conceptInstanceB2 = createInstance(conceptB, conceptInstanceA1, "ConceptInstanceB2"));
		assertNotNull(conceptInstanceB3 = createInstance(conceptB, conceptInstanceA2, "ConceptInstanceB3"));
		assertNotNull(conceptInstanceC1 = createInstance(conceptC, conceptInstanceB1, "ConceptInstanceC1"));
		assertNotNull(conceptInstanceC2 = createInstance(conceptC, conceptInstanceB1, "ConceptInstanceC2"));
		assertNotNull(conceptInstanceC3 = createInstance(conceptC, conceptInstanceB2, "ConceptInstanceC2"));
		System.out.println("conceptInstanceA1=" + conceptInstanceA1.getStringRepresentation());
		System.out.println("conceptInstanceA2=" + conceptInstanceA2.getStringRepresentation());
		System.out.println("conceptInstanceB1=" + conceptInstanceB1.getStringRepresentation());
		System.out.println("conceptInstanceB2=" + conceptInstanceB2.getStringRepresentation());
		System.out.println("conceptInstanceB3=" + conceptInstanceB3.getStringRepresentation());
		System.out.println("conceptInstanceC1=" + conceptInstanceC1.getStringRepresentation());
		System.out.println("conceptInstanceC2=" + conceptInstanceC2.getStringRepresentation());
		System.out.println("conceptInstanceC3=" + conceptInstanceC3.getStringRepresentation());

		assertEquals("ConceptInstanceA1", conceptInstanceA1.getFlexoPropertyValue("a1"));
		assertEquals("ConceptInstanceA1", conceptInstanceA1.getFlexoActor("a1"));
		assertEquals(7, (long) conceptInstanceA1.getFlexoPropertyValue("a2"));
		assertEquals(7, (long) conceptInstanceA1.getFlexoActor("a2"));

		assertEquals("ConceptInstanceB1", conceptInstanceB1.getFlexoPropertyValue("b1"));
		assertEquals("ConceptInstanceB1", conceptInstanceB1.getFlexoActor("b1"));
		assertEquals("ConceptInstanceA1", conceptInstanceB1.getFlexoPropertyValue("a1"));
		assertEquals("ConceptInstanceA1", conceptInstanceB1.getFlexoActor("a1"));

		assertEquals("ConceptInstanceC1", conceptInstanceC1.getFlexoPropertyValue("c1"));
		assertEquals("ConceptInstanceC1", conceptInstanceC1.getFlexoActor("c1"));
		assertEquals("ConceptInstanceB1", conceptInstanceC1.getFlexoPropertyValue("b1"));
		assertEquals("ConceptInstanceB1", conceptInstanceC1.getFlexoActor("b1"));
		assertEquals("ConceptInstanceA1", conceptInstanceC1.getFlexoPropertyValue("a1"));
		assertEquals("ConceptInstanceA1", conceptInstanceC1.getFlexoActor("a1"));
	}

	/**
	 * Populate virtual model instance
	 * 
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(6)
	public void testPropertiesSettings() throws SaveResourceException {

		conceptInstanceA1.setFlexoPropertyValue((FlexoProperty<String>) conceptA.getAccessibleProperty("a1"),
				"NewNameForConceptInstanceA1");

		assertEquals("NewNameForConceptInstanceA1", conceptInstanceA1.getFlexoPropertyValue("a1"));
		assertEquals("NewNameForConceptInstanceA1", conceptInstanceA1.getFlexoActor("a1"));

		assertEquals("ConceptInstanceB1", conceptInstanceB1.getFlexoPropertyValue("b1"));
		assertEquals("ConceptInstanceB1", conceptInstanceB1.getFlexoActor("b1"));
		assertEquals("NewNameForConceptInstanceA1", conceptInstanceB1.getFlexoPropertyValue("a1"));
		assertEquals("NewNameForConceptInstanceA1", conceptInstanceB1.getFlexoActor("a1"));

		assertEquals("ConceptInstanceC1", conceptInstanceC1.getFlexoPropertyValue("c1"));
		assertEquals("ConceptInstanceC1", conceptInstanceC1.getFlexoActor("c1"));
		assertEquals("ConceptInstanceB1", conceptInstanceC1.getFlexoPropertyValue("b1"));
		assertEquals("ConceptInstanceB1", conceptInstanceC1.getFlexoActor("b1"));
		assertEquals("NewNameForConceptInstanceA1", conceptInstanceC1.getFlexoPropertyValue("a1"));
		assertEquals("NewNameForConceptInstanceA1", conceptInstanceC1.getFlexoActor("a1"));

		vmi1.getResource().save(null);

	}

	private FlexoConceptInstance createInstance(FlexoConcept concept, FlexoConceptInstance container, String name) {

		CreateFlexoConceptInstance action = CreateFlexoConceptInstance.actionType.makeNewAction(container, null, editor);
		action.setFlexoConcept(concept);
		if (concept.getCreationSchemes().size() > 0) {
			CreationScheme cs = concept.getCreationSchemes().get(0);
			action.setCreationScheme(cs);
			action.setParameterValue(cs.getParameters().get(0), name);
		}
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		return action.getNewFlexoConceptInstance();
	}

	/**
	 * Populate virtual model instance
	 * 
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(7)
	public void testCreateAndPopulateVMI2() throws SaveResourceException {
		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		action.setNewVirtualModelInstanceName("MyVMI2");
		action.setNewVirtualModelInstanceTitle("MyVMI2");
		action.setVirtualModel(vm1);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		vmi2 = action.getNewVirtualModelInstance();
		assertNotNull(vmi2);

		ActionScheme actionScheme = vm1.getActionSchemes().get(0);

		ActionSchemeActionType actionType = new ActionSchemeActionType(actionScheme, vmi2);

		ActionSchemeAction actionSchemeCreationAction = actionType.makeNewAction(vmi2, null, editor);
		assertNotNull(actionSchemeCreationAction);
		FlexoBehaviourParameter p = actionScheme.getParameter("instanceName");
		actionSchemeCreationAction.setParameterValue(p, "TestInstance");
		actionSchemeCreationAction.doAction();

		assertTrue(actionSchemeCreationAction.hasActionExecutionSucceeded());

		vmi2.getResource().save(null);

		System.out.println("FML=" + actionScheme.getFMLRepresentation());

		FlexoConceptInstance a, b1, b2, c1, c2;

		assertEquals(1, vmi2.getAllRootFlexoConceptInstances().size());
		assertNotNull(a = vmi2.getAllRootFlexoConceptInstances().get(0));

		assertEquals(2, a.getEmbeddedFlexoConceptInstances().size());
		assertNotNull(b1 = a.getEmbeddedFlexoConceptInstances().get(0));
		assertNotNull(b2 = a.getEmbeddedFlexoConceptInstances().get(1));

		assertEquals(1, b1.getEmbeddedFlexoConceptInstances().size());
		assertNotNull(c1 = b1.getEmbeddedFlexoConceptInstances().get(0));

		assertEquals(1, b2.getEmbeddedFlexoConceptInstances().size());
		assertNotNull(c2 = b2.getEmbeddedFlexoConceptInstances().get(0));

	}

}
