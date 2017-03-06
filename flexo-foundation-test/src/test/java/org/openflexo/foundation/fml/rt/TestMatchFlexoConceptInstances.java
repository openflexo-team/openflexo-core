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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.action.ActionSchemeAction;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionType;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.CreateViewInFolder;
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

	private static ViewPoint viewPoint;
	private static VirtualModel vm;
	private static VirtualModel matchingVM;
	private static FlexoConcept concept;
	private static FlexoConcept matchedConcept;

	private static FlexoEditor editor;
	private static FlexoProject project;
	private static View newView;
	private static VirtualModelInstance model;
	private static VirtualModelInstance matchingModel;

	/**
	 * Retrieve the ViewPoint
	 */
	@Test
	@TestOrder(1)
	public void testLoadViewPoint() {
		instanciateTestServiceManager();
		ViewPointLibrary vpLib = serviceManager.getViewPointLibrary();
		assertNotNull(vpLib);
		viewPoint = vpLib.getViewPoint("http://openflexo.org/test/TestMatchFlexoConceptInstance");
		assertNotNull(viewPoint);
		assertNotNull(vm = viewPoint.getVirtualModelNamed("VM"));
		assertNotNull(concept = vm.getFlexoConcept("Concept"));
		assertNotNull(matchingVM = viewPoint.getVirtualModelNamed("MatchingVM"));

		System.out.println("" + matchingVM.getFMLRepresentation());

		assertNotNull(matchedConcept = matchingVM.getFlexoConcept("MatchedConcept"));
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
		CreateViewInFolder action = CreateViewInFolder.actionType.makeNewAction(project.getViewLibrary().getRootFolder(), null, editor);
		action.setNewViewName("MyView");
		action.setNewViewTitle("Test creation of a new view");
		action.setViewpointResource((ViewPointResource) viewPoint.getResource());
		action.setCreationScheme(viewPoint.getCreationSchemes().get(0));
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		assertNotNull(newView = action.getNewView());
		assertNotNull(model = (VirtualModelInstance) newView.getVirtualModelInstance("model"));
		assertNotNull(matchingModel = (VirtualModelInstance) newView.getVirtualModelInstance("matchingModel"));
		assertEquals(model, matchingModel.getFlexoPropertyValue("model"));

	}

	private static FlexoConceptInstance c1;
	private static FlexoConceptInstance c2;
	private static FlexoConceptInstance c3;
	private static FlexoConceptInstance c4;

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

		assertEquals(c1, matchedC1.getFlexoPropertyValue("concept"));
		assertEquals(c2, matchedC2.getFlexoPropertyValue("concept"));
		assertEquals(c3, matchedC3.getFlexoPropertyValue("concept"));
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
		assertEquals(c1, matchedC1.getFlexoPropertyValue("concept"));
		assertEquals("c1-matched", matchedC1.getFlexoPropertyValue("p2"));

		FlexoConceptInstance matchedC4 = createInstance(matchedConcept, matchingModel, null, "c4-matched");
		FlexoConceptInstance matchedC5 = createInstance(matchedConcept, matchingModel, null, "c5-matched");

		assertEquals(5, matchingModel.getFlexoConceptInstances().size());

		synchronizeMatchingModelUsingMatchingSet();

		/*for (FlexoConceptInstance fci : matchingModel.getFlexoConceptInstances()) {
			System.out.println(" > " + fci.getStringRepresentation());
		}*/

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
		assertEquals(c4, matchedC4.getFlexoPropertyValue("concept"));
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
	@TestOrder(6)
	public void testSynchronizeMatchingModelUsingDefaultScheme() throws SaveResourceException {

		matchingModel.clear();

		assertEquals(0, matchingModel.getFlexoConceptInstances().size());

		synchronizeMatchingModelUsingDefaultScheme();
		assertEquals(3, matchingModel.getFlexoConceptInstances().size());

		FlexoConceptInstance matchedC1 = matchingModel.getFlexoConceptInstances().get(0);
		FlexoConceptInstance matchedC2 = matchingModel.getFlexoConceptInstances().get(1);
		FlexoConceptInstance matchedC3 = matchingModel.getFlexoConceptInstances().get(2);

		assertEquals(c1, matchedC1.getFlexoPropertyValue("concept"));
		assertEquals(c2, matchedC2.getFlexoPropertyValue("concept"));
		assertEquals(c3, matchedC3.getFlexoPropertyValue("concept"));
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
		assertEquals(c1, matchedC1.getFlexoPropertyValue("concept"));
		assertEquals("c1-default", matchedC1.getFlexoPropertyValue("p2"));

		FlexoConceptInstance matchedC4 = createInstance(matchedConcept, matchingModel, null, "c4-default");
		FlexoConceptInstance matchedC5 = createInstance(matchedConcept, matchingModel, null, "c5-default");

		assertEquals(5, matchingModel.getFlexoConceptInstances().size());

		synchronizeMatchingModelUsingDefaultScheme();

		/*for (FlexoConceptInstance fci : matchingModel.getFlexoConceptInstances()) {
			System.out.println(" > " + fci.getStringRepresentation());
		}*/

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
		assertEquals(c4, matchedC4.getFlexoPropertyValue("concept"));
		assertEquals("c4-default", matchedC4.getFlexoPropertyValue("p2"));

		c4.delete();
		synchronizeMatchingModelUsingDefaultScheme();
		assertEquals(3, matchingModel.getFlexoConceptInstances().size());
		assertEquals(matchedC1, matchingModel.getFlexoConceptInstances().get(2));
		assertEquals(matchedC2, matchingModel.getFlexoConceptInstances().get(0));
		assertEquals(matchedC3, matchingModel.getFlexoConceptInstances().get(1));
		assertTrue(matchedC4.isDeleted());
	}

	private FlexoConceptInstance createInstance(FlexoConcept concept, FlexoConceptInstance container, Object... parameters) {

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

	private void synchronizeMatchingModelUsingMatchingSet() {

		ActionScheme actionScheme = matchingVM.getActionSchemes().get(0);

		ActionSchemeActionType actionType = new ActionSchemeActionType(actionScheme, matchingModel);
		ActionSchemeAction actionSchemeCreationAction = actionType.makeNewAction(matchingModel, null, editor);
		assertNotNull(actionSchemeCreationAction);
		actionSchemeCreationAction.doAction();

		assertTrue(actionSchemeCreationAction.hasActionExecutionSucceeded());

	}

	private void synchronizeMatchingModelUsingDefaultScheme() {

		ActionScheme actionScheme = matchingVM.getActionSchemes().get(1);

		ActionSchemeActionType actionType = new ActionSchemeActionType(actionScheme, matchingModel);
		ActionSchemeAction actionSchemeCreationAction = actionType.makeNewAction(matchingModel, null, editor);
		assertNotNull(actionSchemeCreationAction);
		actionSchemeCreationAction.doAction();

		assertTrue(actionSchemeCreationAction.hasActionExecutionSucceeded());

	}

}
