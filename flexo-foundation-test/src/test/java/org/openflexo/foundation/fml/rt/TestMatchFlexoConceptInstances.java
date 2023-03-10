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
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rt.action.ActionSchemeAction;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionFactory;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test FlexoConceptInstance matching features
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestMatchFlexoConceptInstances extends OpenflexoProjectAtRunTimeTestCase {

	private static VirtualModel viewPoint;
	private static VirtualModel vm;
	private static VirtualModel matchingVM;
	private static FlexoConcept concept;
	private static FlexoConcept matchedConcept;
	private static VirtualModel vm2;
	private static VirtualModel matchingVM2;
	private static FlexoConcept conceptA;
	private static FlexoConcept conceptB;
	private static FlexoConcept matchedConceptA;
	private static FlexoConcept matchedConceptB;

	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FMLRTVirtualModelInstance newView;
	private static FMLRTVirtualModelInstance model;
	private static FMLRTVirtualModelInstance matchingModel;
	private static FMLRTVirtualModelInstance model2;
	private static FMLRTVirtualModelInstance matchingModel2;

	private static FlexoConceptInstance c1, c2, c3, c4;
	private static FlexoConceptInstance a1, a2, a3, b1, b2, b3, b4, b5, b6, b7;

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
		viewPoint = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestMatchFlexoConceptInstance.fml");
		assertNotNull(viewPoint);
		assertNotNull(vm = viewPoint.getVirtualModelNamed("MyVM"));
		assertNotNull(concept = vm.getFlexoConcept("Concept"));
		assertNotNull(matchingVM = viewPoint.getVirtualModelNamed("MatchingVM"));
		assertNotNull(matchedConcept = matchingVM.getFlexoConcept("MatchedConcept"));
		assertNotNull(vm2 = viewPoint.getVirtualModelNamed("MyVM2"));
		assertNotNull(conceptA = vm2.getFlexoConcept("ConceptA"));
		assertNotNull(conceptB = vm2.getFlexoConcept("ConceptB"));
		assertNotNull(matchingVM2 = viewPoint.getVirtualModelNamed("MatchingVM2"));
		assertNotNull(matchedConceptA = matchingVM2.getFlexoConcept("MatchedConceptA"));
		assertNotNull(matchedConceptB = matchingVM2.getFlexoConcept("MatchedConceptB"));

		System.out.println("VM: " + vm.getCompilationUnit().getFMLPrettyPrint());
		assertVirtualModelIsValid(vm);

		System.out.println("VM2: " + vm2.getCompilationUnit().getFMLPrettyPrint());
		assertVirtualModelIsValid(vm2);

		System.out.println("matchingVM: " + matchingVM.getCompilationUnit().getFMLPrettyPrint());
		assertVirtualModelIsValid(matchingVM);

		System.out.println("matchingVM2: " + matchingVM2.getCompilationUnit().getFMLPrettyPrint());
		assertVirtualModelIsValid(matchingVM2);

		System.out.println(viewPoint.getFMLModelFactory().stringRepresentation(viewPoint));

		System.out.println("VP: " + viewPoint.getCompilationUnit().getFMLPrettyPrint());
		assertVirtualModelIsValid(viewPoint);

	}

	@Test
	@TestOrder(2)
	public void testCreateProject() {
		editor = createStandaloneProject("TestProject");
		project = (FlexoProject<File>) editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
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
		action.setCreationScheme(viewPoint.getCreationSchemes().get(0));
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		assertNotNull(newView = action.getNewVirtualModelInstance());
		assertNotNull(model = (FMLRTVirtualModelInstance) newView.getVirtualModelInstance("model"));
		assertNotNull(matchingModel = (FMLRTVirtualModelInstance) newView.getVirtualModelInstance("matchingModel"));
		assertEquals(model, matchingModel.getFlexoPropertyValue("_model"));
		assertNotNull(model2 = (FMLRTVirtualModelInstance) newView.getVirtualModelInstance("model2"));
		assertNotNull(matchingModel2 = (FMLRTVirtualModelInstance) newView.getVirtualModelInstance("matchingModel2"));
		assertEquals(model2, matchingModel2.getFlexoPropertyValue("_model"));

	}

	/**
	 * Populate virtual model instance
	 */
	@Test
	@TestOrder(5)
	public void testPopulateModel() {

		assertNotNull(c1 = createInstance(concept, model, "c1"));
		assertNotNull(c2 = createInstance(concept, model, "c2"));
		assertNotNull(c3 = createInstance(concept, model, "c3"));

		System.out.println("c1=" + c1.getStringRepresentation());
		System.out.println("c2=" + c2.getStringRepresentation());
		System.out.println("c3=" + c3.getStringRepresentation());

		assertEquals("c1", c1.getFlexoPropertyValue("p"));
		assertEquals("c2", c2.getFlexoPropertyValue("p"));
		assertEquals("c3", c3.getFlexoPropertyValue("p"));
	}

	/**
	 * Test synchronization using MatchingSet schemes
	 * 
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(6)
	public void testSynchronizeMatchingModelUsingMatchingSet() throws SaveResourceException {

		assertEquals(0, matchingModel.getFlexoConceptInstances().size());

		synchronizeMatchingModelUsingMatchingSet();
		assertEquals(3, matchingModel.getFlexoConceptInstances().size());

		FlexoConceptInstance matchedC1 = matchingModel.getFlexoConceptInstances().get(0);
		FlexoConceptInstance matchedC2 = matchingModel.getFlexoConceptInstances().get(1);
		FlexoConceptInstance matchedC3 = matchingModel.getFlexoConceptInstances().get(2);

		assertEquals(c1, matchedC1.getFlexoPropertyValue("c"));
		assertEquals(c2, matchedC2.getFlexoPropertyValue("c"));
		assertEquals(c3, matchedC3.getFlexoPropertyValue("c"));
		assertEquals("c1-matched", matchedC1.getFlexoPropertyValue("p2"));
		assertEquals("c2-matched", matchedC2.getFlexoPropertyValue("p2"));
		assertEquals("c3-matched", matchedC3.getFlexoPropertyValue("p2"));

		synchronizeMatchingModelUsingMatchingSet();
		assertEquals(3, matchingModel.getFlexoConceptInstances().size());
		assertEquals(matchedC1, matchingModel.getFlexoConceptInstances().get(0));
		assertEquals(matchedC2, matchingModel.getFlexoConceptInstances().get(1));
		assertEquals(matchedC3, matchingModel.getFlexoConceptInstances().get(2));

		matchedC1.delete();
		assertEquals(2, matchingModel.getFlexoConceptInstances().size());

		synchronizeMatchingModelUsingMatchingSet();
		assertEquals(3, matchingModel.getFlexoConceptInstances().size());
		matchedC1 = matchingModel.getFlexoConceptInstances().get(2);
		assertEquals(c1, matchedC1.getFlexoPropertyValue("c"));
		assertEquals("c1-matched", matchedC1.getFlexoPropertyValue("p2"));

		FlexoConceptInstance matchedC4 = createInstance(matchedConcept, matchingModel, "c4-matched", null);
		FlexoConceptInstance matchedC5 = createInstance(matchedConcept, matchingModel, "c5-matched", null);

		assertEquals(5, matchingModel.getFlexoConceptInstances().size());

		synchronizeMatchingModelUsingMatchingSet();

		// for (FlexoConceptInstance fci : matchingModel.getFlexoConceptInstances()) {
		// System.out.println(" > " + fci.getStringRepresentation());
		// }

		assertEquals(3, matchingModel.getFlexoConceptInstances().size());
		assertEquals(matchedC1, matchingModel.getFlexoConceptInstances().get(2));
		assertEquals(matchedC2, matchingModel.getFlexoConceptInstances().get(0));
		assertEquals(matchedC3, matchingModel.getFlexoConceptInstances().get(1));
		assertTrue(matchedC4.isDeleted());
		assertTrue(matchedC5.isDeleted());

		assertNotNull(c4 = createInstance(concept, model, "c4"));
		synchronizeMatchingModelUsingMatchingSet();
		assertEquals(4, matchingModel.getFlexoConceptInstances().size());
		matchedC4 = matchingModel.getFlexoConceptInstances().get(3);
		assertEquals(c4, matchedC4.getFlexoPropertyValue("c"));
		assertEquals("c4-matched", matchedC4.getFlexoPropertyValue("p2"));

		c4.delete();
		synchronizeMatchingModelUsingMatchingSet();
		assertEquals(3, matchingModel.getFlexoConceptInstances().size());
		assertEquals(matchedC1, matchingModel.getFlexoConceptInstances().get(2));
		assertEquals(matchedC2, matchingModel.getFlexoConceptInstances().get(0));
		assertEquals(matchedC3, matchingModel.getFlexoConceptInstances().get(1));
		assertTrue(matchedC4.isDeleted());
	}

	/**
	 * Test synchronization using default scheme (no matching set management)
	 * 
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(7)
	public void testSynchronizeMatchingModelUsingDefaultScheme() throws SaveResourceException {

		matchingModel.clear();

		assertEquals(0, matchingModel.getFlexoConceptInstances().size());

		synchronizeMatchingModelUsingDefaultScheme();
		assertEquals(3, matchingModel.getFlexoConceptInstances().size());

		FlexoConceptInstance matchedC1 = matchingModel.getFlexoConceptInstances().get(0);
		FlexoConceptInstance matchedC2 = matchingModel.getFlexoConceptInstances().get(1);
		FlexoConceptInstance matchedC3 = matchingModel.getFlexoConceptInstances().get(2);

		assertEquals(c1, matchedC1.getFlexoPropertyValue("c"));
		assertEquals(c2, matchedC2.getFlexoPropertyValue("c"));
		assertEquals(c3, matchedC3.getFlexoPropertyValue("c"));
		assertEquals("c1-default", matchedC1.getFlexoPropertyValue("p2"));
		assertEquals("c2-default", matchedC2.getFlexoPropertyValue("p2"));
		assertEquals("c3-default", matchedC3.getFlexoPropertyValue("p2"));

		synchronizeMatchingModelUsingDefaultScheme();
		assertEquals(3, matchingModel.getFlexoConceptInstances().size());
		assertEquals(matchedC1, matchingModel.getFlexoConceptInstances().get(0));
		assertEquals(matchedC2, matchingModel.getFlexoConceptInstances().get(1));
		assertEquals(matchedC3, matchingModel.getFlexoConceptInstances().get(2));

		matchedC1.delete();
		assertEquals(2, matchingModel.getFlexoConceptInstances().size());

		synchronizeMatchingModelUsingDefaultScheme();
		assertEquals(3, matchingModel.getFlexoConceptInstances().size());
		matchedC1 = matchingModel.getFlexoConceptInstances().get(2);
		assertEquals(c1, matchedC1.getFlexoPropertyValue("c"));
		assertEquals("c1-default", matchedC1.getFlexoPropertyValue("p2"));

		FlexoConceptInstance matchedC4 = createInstance(matchedConcept, matchingModel, "c4-default", null);
		FlexoConceptInstance matchedC5 = createInstance(matchedConcept, matchingModel, "c5-default", null);

		assertEquals(5, matchingModel.getFlexoConceptInstances().size());

		synchronizeMatchingModelUsingDefaultScheme();

		// for (FlexoConceptInstance fci : matchingModel.getFlexoConceptInstances()) {
		// System.out.println(" > " + fci.getStringRepresentation());
		// }

		assertEquals(3, matchingModel.getFlexoConceptInstances().size());
		assertEquals(matchedC1, matchingModel.getFlexoConceptInstances().get(2));
		assertEquals(matchedC2, matchingModel.getFlexoConceptInstances().get(0));
		assertEquals(matchedC3, matchingModel.getFlexoConceptInstances().get(1));
		assertTrue(matchedC4.isDeleted());
		assertTrue(matchedC5.isDeleted());

		assertNotNull(c4 = createInstance(concept, model, "c4"));
		synchronizeMatchingModelUsingDefaultScheme();
		assertEquals(4, matchingModel.getFlexoConceptInstances().size());
		matchedC4 = matchingModel.getFlexoConceptInstances().get(3);
		assertEquals(c4, matchedC4.getFlexoPropertyValue("c"));
		assertEquals("c4-default", matchedC4.getFlexoPropertyValue("p2"));

		c4.delete();
		synchronizeMatchingModelUsingDefaultScheme();
		assertEquals(3, matchingModel.getFlexoConceptInstances().size());
		assertEquals(matchedC1, matchingModel.getFlexoConceptInstances().get(2));
		assertEquals(matchedC2, matchingModel.getFlexoConceptInstances().get(0));
		assertEquals(matchedC3, matchingModel.getFlexoConceptInstances().get(1));
		assertTrue(matchedC4.isDeleted());
	}

	/**
	 * Populate virtual model instance
	 */
	@Test
	@TestOrder(8)
	public void testPopulateModel2() {
		assertNotNull(a1 = createInstance(conceptA, model2, "a1"));
		assertNotNull(b1 = createInstance(conceptB, a1, "b1"));
		assertNotNull(b2 = createInstance(conceptB, a1, "b2"));
		assertNotNull(a2 = createInstance(conceptA, model2, "a2"));
		assertNotNull(b3 = createInstance(conceptB, a2, "b3"));
		assertNotNull(b4 = createInstance(conceptB, a2, "b4"));
		assertNotNull(b5 = createInstance(conceptB, a2, "b5"));

		System.out.println("a1=" + a1.getStringRepresentation());
		System.out.println("b1=" + b1.getStringRepresentation());
		System.out.println("b2=" + b2.getStringRepresentation());
		System.out.println("a2=" + a2.getStringRepresentation());
		System.out.println("b3=" + b3.getStringRepresentation());
		System.out.println("b4=" + b4.getStringRepresentation());
		System.out.println("b5=" + b5.getStringRepresentation());

		// for (FlexoConceptInstance fci : matchingModel.getFlexoConceptInstances()) {
		// System.out.println(" > " + fci.getStringRepresentation());
		// }

	}

	/**
	 * Test synchronization using MatchingSet schemes
	 * 
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(9)
	public void testSynchronizeMatchingModelEmbeddingContext() throws SaveResourceException {

		assertEquals(0, matchingModel2.getFlexoConceptInstances().size());

		synchronizeMatchingModel2UsingMatchingSet();

		assertEquals(2, matchingModel2.getFlexoConceptInstances(matchedConceptA).size());
		FlexoConceptInstance matchedA1 = matchingModel2.getFlexoConceptInstances(matchedConceptA).get(0);
		FlexoConceptInstance matchedA2 = matchingModel2.getFlexoConceptInstances(matchedConceptA).get(1);

		assertEquals(2, matchedA1.getEmbeddedFlexoConceptInstances(matchedConceptB).size());
		FlexoConceptInstance matchedB1 = matchedA1.getEmbeddedFlexoConceptInstances(matchedConceptB).get(0);
		FlexoConceptInstance matchedB2 = matchedA1.getEmbeddedFlexoConceptInstances(matchedConceptB).get(1);

		assertEquals(3, matchedA2.getEmbeddedFlexoConceptInstances(matchedConceptB).size());
		FlexoConceptInstance matchedB3 = matchedA2.getEmbeddedFlexoConceptInstances(matchedConceptB).get(0);
		FlexoConceptInstance matchedB4 = matchedA2.getEmbeddedFlexoConceptInstances(matchedConceptB).get(1);
		FlexoConceptInstance matchedB5 = matchedA2.getEmbeddedFlexoConceptInstances(matchedConceptB).get(2);

		assertEquals(a1, matchedA1.getFlexoPropertyValue("conceptA"));
		assertEquals(a2, matchedA2.getFlexoPropertyValue("conceptA"));
		assertEquals("a1-matched", matchedA1.getFlexoPropertyValue("name"));
		assertEquals("a2-matched", matchedA2.getFlexoPropertyValue("name"));

		assertEquals(b1, matchedB1.getFlexoPropertyValue("conceptB"));
		assertEquals(b2, matchedB2.getFlexoPropertyValue("conceptB"));
		assertEquals("b1-matched", matchedB1.getFlexoPropertyValue("name"));
		assertEquals("b2-matched", matchedB2.getFlexoPropertyValue("name"));

		assertEquals(b3, matchedB3.getFlexoPropertyValue("conceptB"));
		assertEquals(b4, matchedB4.getFlexoPropertyValue("conceptB"));
		assertEquals(b5, matchedB5.getFlexoPropertyValue("conceptB"));
		assertEquals("b3-matched", matchedB3.getFlexoPropertyValue("name"));
		assertEquals("b4-matched", matchedB4.getFlexoPropertyValue("name"));
		assertEquals("b5-matched", matchedB5.getFlexoPropertyValue("name"));

		// We perform now some modifications on model
		assertNotNull(a3 = createInstance(conceptA, model2, "a3"));
		assertNotNull(b6 = createInstance(conceptB, a1, "b6"));
		b5.delete();
		assertNotNull(b7 = createInstance(conceptB, a3, "b7"));

		// Now synchronize !
		synchronizeMatchingModel2UsingMatchingSet();

		assertEquals(3, matchingModel2.getFlexoConceptInstances(matchedConceptA).size());
		FlexoConceptInstance matchedA3 = matchingModel2.getFlexoConceptInstances(matchedConceptA).get(2);

		assertEquals(1, matchedA3.getEmbeddedFlexoConceptInstances(matchedConceptB).size());
		FlexoConceptInstance matchedB7 = matchedA3.getEmbeddedFlexoConceptInstances(matchedConceptB).get(0);

		assertEquals(3, matchedA1.getEmbeddedFlexoConceptInstances(matchedConceptB).size());
		FlexoConceptInstance matchedB6 = matchedA1.getEmbeddedFlexoConceptInstances(matchedConceptB).get(2);

		assertEquals(2, matchedA2.getEmbeddedFlexoConceptInstances().size());
		assertEquals(2, matchedA2.getEmbeddedFlexoConceptInstances(matchedConceptB).size());

		assertEquals(a3, matchedA3.getFlexoPropertyValue("conceptA"));
		assertEquals("a3-matched", matchedA3.getFlexoPropertyValue("name"));

		assertEquals(b6, matchedB6.getFlexoPropertyValue("conceptB"));
		assertEquals("b6-matched", matchedB6.getFlexoPropertyValue("name"));

		assertEquals(b7, matchedB7.getFlexoPropertyValue("conceptB"));
		assertEquals("b7-matched", matchedB7.getFlexoPropertyValue("name"));

	}

	private static FlexoConceptInstance createInstance(FlexoConcept concept, FlexoConceptInstance container, Object... parameters) {

		CreateFlexoConceptInstance action = CreateFlexoConceptInstance.actionType.makeNewAction(container, null, editor);
		action.setFlexoConcept(concept);
		if (concept.getCreationSchemes().size() > 0) {
			CreationScheme cs = concept.getCreationSchemes().get(0);
			action.setCreationScheme(cs);
			for (int i = 0; i < parameters.length; i++) {
				action.setParameterValue(cs.getParameters().get(i), parameters[i]);
			}
		}
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		return action.getNewFlexoConceptInstance();
	}

	private static void synchronizeMatchingModelUsingMatchingSet() {

		ActionScheme actionScheme = matchingVM.getActionSchemes().get(0);

		ActionSchemeActionFactory actionType = new ActionSchemeActionFactory(actionScheme, matchingModel);
		ActionSchemeAction actionSchemeCreationAction = actionType.makeNewAction(matchingModel, null, editor);
		assertNotNull(actionSchemeCreationAction);
		actionSchemeCreationAction.doAction();

		assertTrue(actionSchemeCreationAction.hasActionExecutionSucceeded());

	}

	private static void synchronizeMatchingModelUsingDefaultScheme() {

		ActionScheme actionScheme = matchingVM.getActionSchemes().get(1);

		ActionSchemeActionFactory actionType = new ActionSchemeActionFactory(actionScheme, matchingModel);
		ActionSchemeAction actionSchemeCreationAction = actionType.makeNewAction(matchingModel, null, editor);
		assertNotNull(actionSchemeCreationAction);
		actionSchemeCreationAction.doAction();

		assertTrue(actionSchemeCreationAction.hasActionExecutionSucceeded());

	}

	private static void synchronizeMatchingModel2UsingMatchingSet() {

		ActionScheme actionScheme = matchingVM2.getActionSchemes().get(0);

		ActionSchemeActionFactory actionType = new ActionSchemeActionFactory(actionScheme, matchingModel2);
		ActionSchemeAction actionSchemeCreationAction = actionType.makeNewAction(matchingModel2, null, editor);
		assertNotNull(actionSchemeCreationAction);
		actionSchemeCreationAction.doAction();

		assertTrue(actionSchemeCreationAction.hasActionExecutionSucceeded());

	}

}
