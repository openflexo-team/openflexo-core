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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.foundation.fml.AbstractActionScheme;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.action.ActionSchemeAction;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionType;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateView;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeAction;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeActionType;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test some virtual model instance actions. The viewpoint is already existing
 * 
 * @author vincent
 * 
 */
@RunWith(OrderedRunner.class)
public class TestFlexoConceptInstanceEditionAction extends OpenflexoProjectAtRunTimeTestCase {

	private static ViewPoint viewPoint;
	private static FlexoEditor editor;
	private static FlexoProject project;
	private static View newView;
	private static VirtualModelInstance newVmi;
	private static VirtualModel virtualModel;

	private static FlexoConcept flexoConcept1;
	private static FlexoConcept flexoConcept2;
	private static FlexoConcept flexoConcept3;

	private static FlexoConceptInstance fci1;
	private static FlexoConceptInstance fci2a;
	private static FlexoConceptInstance fci2b;
	private static FlexoConceptInstance fci3;

	/**
	 * Retrieve the ViewPoint
	 */
	@Test
	@TestOrder(1)
	public void testLoadViewPoint() {
		instanciateTestServiceManager();
		ViewPointLibrary vpLib = serviceManager.getViewPointLibrary();
		assertNotNull(vpLib);
		viewPoint = vpLib.getViewPoint("http://openflexo.org/test/TestViewPoint2");
		assertNotNull(viewPoint);
	}

	@Test
	@TestOrder(2)
	public void testCreateProject() {
		editor = createProject("TestProject");
		project = editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
		assertTrue(project.getProjectDataResource().getFlexoIODelegate().exists());
	}

	/**
	 * Instantiate in project a View conform to the ViewPoint
	 */
	@Test
	@TestOrder(3)
	public void testCreateView() {
		CreateView action = CreateView.actionType.makeNewAction(project.getViewLibrary().getRootFolder(), null, editor);
		action.setNewViewName("MyView");
		action.setNewViewTitle("Test creation of a new view");
		action.setViewpointResource((ViewPointResource) viewPoint.getResource());
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		newView = action.getNewView();
		virtualModel = viewPoint.getVirtualModelNamed("VirtualModel");
		flexoConcept1 = virtualModel.getFlexoConcept("Concept1");
		flexoConcept2 = virtualModel.getFlexoConcept("Concept2");
		flexoConcept3 = virtualModel.getFlexoConcept("Concept3");

		assertNotNull(newView);
		assertNotNull(newView.getResource());
		// assertTrue(((ViewResource) newView.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource) newView.getResource()).getFile().exists());
		assertTrue(((ViewResource) newView.getResource()).getDirectory() != null);
		assertTrue(((ViewResource) newView.getResource()).getFlexoIODelegate().exists());
	}

	/**
	 * Instantiate in project a VirtualModelInstance conform to the VirtualModel
	 */
	@Test
	@TestOrder(4)
	public void testCreateVirtualModelInstance() {

		log("testCreateVirtualModelInstance()");

		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		action.setNewVirtualModelInstanceName("MyVirtualModelInstance");
		action.setNewVirtualModelInstanceTitle("Test creation of a new VirtualModelInstance");
		action.setVirtualModel(virtualModel);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		newVmi = action.getNewVirtualModelInstance();
		assertNotNull(newVmi);
		assertNotNull(newVmi.getResource());
		assertEquals(virtualModel, newVmi.getFlexoConcept());
		assertEquals(virtualModel, newVmi.getVirtualModel());
	}

	/**
	 * Test action that create flexo concept instance using some predefined parameters
	 * 
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	@Test
	@TestOrder(5)
	public void testCreationScheme() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		ActionScheme createConcept1 = null;
		ActionScheme createConcept2 = null;
		ActionScheme createConcept3 = null;
		for (AbstractActionScheme action : newVmi.getVirtualModel().getAbstractActionSchemes()) {
			if (action.getName().equals("addConcept1")) {
				createConcept1 = (ActionScheme) action;
			}
			if (action.getName().equals("addConcept2")) {
				createConcept2 = (ActionScheme) action;
			}
			if (action.getName().equals("addConcept3")) {
				createConcept3 = (ActionScheme) action;
			}
		}

		// FlexoBehaviourParameter nameParam = dropScheme.getParameters().size() > 0 ? dropScheme.getParameters().get(0) : null;
		ActionSchemeAction createConcept1Action = new ActionSchemeActionType(createConcept1, newVmi).makeNewAction(newVmi, null, editor);
		createConcept1Action.doAction();
		assertTrue(createConcept1Action.hasActionExecutionSucceeded());
		assertTrue(newVmi.getFlexoConceptInstances(flexoConcept1).size() == 1);
		fci1 = (newVmi.getFlexoConceptInstances(flexoConcept1).get(0));

		ActionSchemeAction createConcept2Action = new ActionSchemeActionType(createConcept2, newVmi).makeNewAction(newVmi, null, editor);
		createConcept2Action.doAction();
		assertTrue(createConcept2Action.hasActionExecutionSucceeded());
		assertTrue(newVmi.getFlexoConceptInstances(flexoConcept2).size() == 2);
		fci2a = (newVmi.getFlexoConceptInstances(flexoConcept2).get(0));
		fci2b = (newVmi.getFlexoConceptInstances(flexoConcept2).get(1));

		ActionSchemeAction createConcept3Action = new ActionSchemeActionType(createConcept3, newVmi).makeNewAction(newVmi, null, editor);
		createConcept3Action.doAction();
		assertTrue(createConcept3Action.hasActionExecutionSucceeded());
		assertTrue(newVmi.getFlexoConceptInstances(flexoConcept3).size() == 1);
		fci3 = (newVmi.getFlexoConceptInstances(flexoConcept3).get(0));
	}

	/**
	 * Test an action that assigns a flexo concept instance to a role without multiple cardinality
	 * 
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	@Test
	@TestOrder(6)
	public void testAssignToRole() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		ActionScheme setConcept3 = null;
		for (AbstractActionScheme action : flexoConcept1.getAbstractActionSchemes()) {
			if (action.getName().equals("setConcept3")) {
				setConcept3 = (ActionScheme) action;
			}
		}

		ActionSchemeAction createConcept1Action = new ActionSchemeActionType(setConcept3, fci1).makeNewAction(fci1, null, editor);
		createConcept1Action.setParameterValue(setConcept3.getParameter("concept3"), fci3);
		createConcept1Action.doAction();

		assertTrue(createConcept1Action.hasActionExecutionSucceeded());
		assertTrue(fci1.getFlexoActor("concept3").equals(fci3));
	}

	/**
	 * Test an action that add some flexo concept instances to a role with multiple cardinality
	 * 
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	@Test
	@TestOrder(7)
	public void testAddToList() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		ActionScheme addConcept2 = null;
		for (AbstractActionScheme action : flexoConcept1.getAbstractActionSchemes()) {
			if (action.getName().equals("addConcept2")) {
				addConcept2 = (ActionScheme) action;
			}
		}

		ActionSchemeAction addConcept2Action = new ActionSchemeActionType(addConcept2, fci1).makeNewAction(fci1, null, editor);
		addConcept2Action.setParameterValue(addConcept2.getParameter("concept2"), fci2a);
		addConcept2Action.doAction();
		assertTrue(addConcept2Action.hasActionExecutionSucceeded());

		addConcept2Action.setParameterValue(addConcept2.getParameter("concept2"), fci2b);
		addConcept2Action.doAction();
		assertTrue(addConcept2Action.hasActionExecutionSucceeded());

		assertTrue(fci1.getFlexoActorList("concept2").contains(fci2a));
		assertTrue(fci1.getFlexoActorList("concept2").contains(fci2b));
	}

	/**
	 * Test an action that remove a flexo concept instances from a role with multiple cardinality
	 * 
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	@Test
	@TestOrder(8)
	public void testRemoveFromList() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		ActionScheme removeConcept2 = null;
		for (AbstractActionScheme action : flexoConcept1.getAbstractActionSchemes()) {
			if (action.getName().equals("removeConcept2")) {
				removeConcept2 = (ActionScheme) action;
			}
		}

		ActionSchemeAction removeConcept2Action = new ActionSchemeActionType(removeConcept2, fci1).makeNewAction(fci1, null, editor);
		removeConcept2Action.setParameterValue(removeConcept2.getParameter("concept2"), fci2a);
		removeConcept2Action.doAction();
		assertTrue(removeConcept2Action.hasActionExecutionSucceeded());

		assertFalse(fci1.getFlexoActorList("concept2").contains(fci2a));
		assertTrue(fci1.getFlexoActorList("concept2").contains(fci2b));
	}

	/**
	 * Test an action that deletes a flexo concept instance
	 * 
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	@Test
	@TestOrder(9)
	public void testDeletionAction() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		DeletionScheme delete = flexoConcept2.getDeletionSchemes().get(0);
		DeletionSchemeAction deleteAction = new DeletionSchemeActionType(delete, fci2b).makeNewAction(newVmi, null, editor);
		deleteAction.doAction();
		assertTrue(deleteAction.hasActionExecutionSucceeded());
		assertTrue(fci2b.isDeleted());
		assertFalse(newVmi.getFlexoConceptInstances(flexoConcept2).contains(fci2b));
	}

}
