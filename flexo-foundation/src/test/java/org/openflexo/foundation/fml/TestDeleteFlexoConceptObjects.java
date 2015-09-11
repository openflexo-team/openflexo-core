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

import java.util.List;
import java.util.Vector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.foundation.action.AbstractCopyAction.InvalidSelectionException;
import org.openflexo.foundation.fml.PrimitiveRole.PrimitiveType;
import org.openflexo.foundation.fml.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.fml.VirtualModel.VirtualModelImpl;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.action.DeleteFlexoConceptObjects;
import org.openflexo.foundation.fml.binding.ViewPointBindingModel;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test {@link DeleteFlexoConceptObjects}
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestDeleteFlexoConceptObjects extends OpenflexoProjectAtRunTimeTestCase {

	static FlexoEditor editor;
	static ViewPoint viewPoint;
	static VirtualModel virtualModel;

	static FlexoConcept flexoConcept1;
	static FlexoConcept flexoConcept2;

	public static PrimitiveRole<String> aStringInA;
	public static PrimitiveRole<Boolean> someBooleanInA;
	public static PrimitiveRole<Integer> someIntegerInA;
	public static FlexoConceptInstanceRole someFlexoConcept2;

	public static CreationScheme creationScheme;

	public static AssignationAction<?> action1;

	/**
	 * Init
	 */
	@Test
	@TestOrder(1)
	public void init() {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		System.out.println("ResourceCenter= " + resourceCenter);
	}

	/**
	 * Test {@link ViewPoint} creation, check {@link BindingModel}
	 */
	@Test
	@TestOrder(2)
	public void testCreateViewPoint() {
		viewPoint = ViewPointImpl.newViewPoint("TestViewPoint", "http://openflexo.org/test/TestViewPoint", resourceCenter.getDirectory(),
				serviceManager.getViewPointLibrary(), resourceCenter);
		// assertTrue(((ViewPointResource) viewPoint.getResource()).getDirectory().exists());
		// assertTrue(((ViewPointResource) viewPoint.getResource()).getFile().exists());
		assertTrue(((ViewPointResource) viewPoint.getResource()).getDirectory() != null);
		assertTrue(((ViewPointResource) viewPoint.getResource()).getFlexoIODelegate().exists());

		System.out.println("ViewPoint BindingModel = " + viewPoint.getBindingModel());
		assertNotNull(viewPoint.getBindingModel());
		assertEquals(2, viewPoint.getBindingModel().getBindingVariablesCount());
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));

	}

	/**
	 * Test {@link VirtualModel} creation, check {@link BindingModel}
	 */
	@Test
	@TestOrder(3)
	public void testCreateVirtualModel() throws SaveResourceException {

		virtualModel = VirtualModelImpl.newVirtualModel("VM1", viewPoint);
		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) virtualModel.getResource()).getDirectory()).exists());
		assertTrue(((VirtualModelResource) virtualModel.getResource()).getFlexoIODelegate().exists());

		assertNotNull(virtualModel.getBindingModel());
		assertEquals(4, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel),
				virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

		// We disconnect VirtualModel from ViewPoint, and we check BindingModel evolution
		viewPoint.removeFromVirtualModels(virtualModel);
		System.out.println("VirtualModel BindingModel = " + virtualModel.getBindingModel());
		assertNotNull(virtualModel.getBindingModel());
		assertEquals(2, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		// assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1), virtualModel1.getBindingModel()
		// .bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

		// We reconnect VirtualModel again, and we check BindingModel evolution
		viewPoint.addToVirtualModels(virtualModel);
		System.out.println("VirtualModel BindingModel = " + virtualModel.getBindingModel());
		assertEquals(4, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel),
				virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

	}

	/**
	 * Create some objects
	 */
	@Test
	@TestOrder(4)
	public void testCreateFMLModel() throws SaveResourceException {

		CreateFlexoConcept addEP1 = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addEP1.setNewFlexoConceptName("FlexoConcept1");
		addEP1.doAction();

		flexoConcept1 = addEP1.getNewFlexoConcept();

		System.out.println("flexoConcept1 = " + flexoConcept1);
		assertNotNull(flexoConcept1);

		CreateFlexoConcept addEP2 = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addEP2.setNewFlexoConceptName("FlexoConcept2");
		addEP2.doAction();

		flexoConcept2 = addEP2.getNewFlexoConcept();

		System.out.println("flexoConcept2 = " + flexoConcept2);
		assertNotNull(flexoConcept2);

		((VirtualModelResource) virtualModel.getResource()).save(null);

		CreatePrimitiveRole createPR1 = CreatePrimitiveRole.actionType.makeNewAction(flexoConcept1, null, editor);
		createPR1.setRoleName("aStringInA");
		createPR1.setPrimitiveType(PrimitiveType.String);
		createPR1.setCardinality(PropertyCardinality.One);
		createPR1.doAction();

		CreatePrimitiveRole createPR2 = CreatePrimitiveRole.actionType.makeNewAction(flexoConcept1, null, editor);
		createPR2.setRoleName("someBooleanInA");
		createPR2.setPrimitiveType(PrimitiveType.Boolean);
		createPR2.setCardinality(PropertyCardinality.ZeroMany);
		createPR2.doAction();

		CreatePrimitiveRole createPR3 = CreatePrimitiveRole.actionType.makeNewAction(flexoConcept1, null, editor);
		createPR3.setRoleName("someIntegerInA");
		createPR3.setPrimitiveType(PrimitiveType.Integer);
		createPR3.setCardinality(PropertyCardinality.OneMany);
		createPR3.doAction();

		CreateFlexoConceptInstanceRole createPR4 = CreateFlexoConceptInstanceRole.actionType.makeNewAction(flexoConcept1, null, editor);
		createPR4.setRoleName("someFlexoConcept2");
		createPR4.setFlexoConceptInstanceType(flexoConcept2);
		createPR4.setCardinality(PropertyCardinality.ZeroMany);
		createPR4.doAction();

		assertEquals(4, flexoConcept1.getFlexoProperties().size());
		assertTrue(flexoConcept1.getFlexoProperties().contains(createPR1.getNewFlexoRole()));
		assertTrue(flexoConcept1.getFlexoProperties().contains(createPR2.getNewFlexoRole()));
		assertTrue(flexoConcept1.getFlexoProperties().contains(createPR3.getNewFlexoRole()));
		assertTrue(flexoConcept1.getFlexoProperties().contains(createPR4.getNewFlexoRole()));

		aStringInA = (PrimitiveRole<String>) flexoConcept1.getAccessibleProperty("aStringInA");
		assertNotNull(aStringInA);
		assertEquals(String.class, aStringInA.getType());
		assertEquals(String.class, aStringInA.getResultingType());
		someBooleanInA = (PrimitiveRole<Boolean>) flexoConcept1.getAccessibleProperty("someBooleanInA");
		assertNotNull(someBooleanInA);
		assertEquals(Boolean.class, someBooleanInA.getType());
		assertEquals(new ParameterizedTypeImpl(List.class, Boolean.class), someBooleanInA.getResultingType());
		someIntegerInA = (PrimitiveRole<Integer>) flexoConcept1.getAccessibleProperty("someIntegerInA");
		assertNotNull(someIntegerInA);
		assertEquals(Integer.class, someIntegerInA.getType());
		assertEquals(new ParameterizedTypeImpl(List.class, Integer.class), someIntegerInA.getResultingType());
		someFlexoConcept2 = (FlexoConceptInstanceRole) flexoConcept1.getAccessibleProperty("someFlexoConcept2");
		assertNotNull(someFlexoConcept2);

		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept2), someFlexoConcept2.getType());
		assertEquals(new ParameterizedTypeImpl(List.class, FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept2)),
				someFlexoConcept2.getResultingType());

		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConcept1, null, editor);
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme.setFlexoBehaviourName("creationScheme");
		createCreationScheme.doAction();
		creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();

		CreateEditionAction createEditionAction1 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction1.setEditionActionClass(ExpressionAction.class);
		createEditionAction1.setAssignation(new DataBinding<Object>("aStringInA"));
		createEditionAction1.doAction();
		action1 = (AssignationAction<?>) createEditionAction1.getNewEditionAction();
		((ExpressionAction) action1.getAssignableAction()).setExpression(new DataBinding<Object>("'foo'"));
		action1.setName("action1");

		assertTrue(action1.getAssignation().isValid());
		assertTrue(((ExpressionAction) action1.getAssignableAction()).getExpression().isValid());

		assertTrue(flexoConcept1.getFlexoBehaviours().contains(creationScheme));
		assertTrue(flexoConcept1.getCreationSchemes().contains(creationScheme));

		// We create now an empty creation scheme for FlexoConcept 2
		CreateFlexoBehaviour createCreationScheme2 = CreateFlexoBehaviour.actionType.makeNewAction(flexoConcept2, null, editor);
		createCreationScheme2.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme2.setFlexoBehaviourName("creationScheme");
		createCreationScheme2.doAction();
		CreationScheme creationScheme2 = (CreationScheme) createCreationScheme2.getNewFlexoBehaviour();
		assertTrue(flexoConcept2.getFlexoBehaviours().contains(creationScheme2));
		assertTrue(flexoConcept2.getCreationSchemes().contains(creationScheme2));

		assertViewPointIsValid(viewPoint);

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

	}

	@Test
	@TestOrder(5)
	public void testDelete1() throws InvalidSelectionException {

		log("testDelete1");

		Vector<FlexoConceptObject> sel = new Vector<FlexoConceptObject>();
		DeleteFlexoConceptObjects deleteAction = DeleteFlexoConceptObjects.actionType.makeNewAction(flexoConcept1, sel, editor);
		assertSameList(deleteAction.getObjectsToDelete(), flexoConcept1);
	}

	@Test
	@TestOrder(6)
	public void testDelete2() throws InvalidSelectionException {

		log("testDelete2");

		Vector<FlexoConceptObject> sel = new Vector<FlexoConceptObject>();
		sel.add(flexoConcept1);
		sel.add(flexoConcept2);
		DeleteFlexoConceptObjects deleteAction = DeleteFlexoConceptObjects.actionType.makeNewAction(flexoConcept1, sel, editor);
		assertSameList(deleteAction.getObjectsToDelete(), flexoConcept1, flexoConcept2);
	}

	@Test
	@TestOrder(7)
	public void testDelete3() throws InvalidSelectionException {

		log("testDelete3");

		Vector<FlexoConceptObject> sel = new Vector<FlexoConceptObject>();
		sel.add(aStringInA);
		sel.add(someBooleanInA);
		sel.add(someIntegerInA);
		sel.add(someFlexoConcept2);
		DeleteFlexoConceptObjects deleteAction = DeleteFlexoConceptObjects.actionType.makeNewAction(null, sel, editor);
		assertSameList(deleteAction.getObjectsToDelete(), aStringInA, someBooleanInA, someIntegerInA, someFlexoConcept2);
	}

	@Test
	@TestOrder(8)
	public void testDelete4() throws InvalidSelectionException {

		log("testDelete4");

		Vector<FlexoConceptObject> sel = new Vector<FlexoConceptObject>();
		sel.add(flexoConcept1);
		sel.add(aStringInA);
		sel.add(someBooleanInA);
		DeleteFlexoConceptObjects deleteAction = DeleteFlexoConceptObjects.actionType.makeNewAction(flexoConcept1, sel, editor);
		assertSameList(deleteAction.getObjectsToDelete(), flexoConcept1);
	}

	@Test
	@TestOrder(9)
	public void testDelete5() throws InvalidSelectionException {

		log("testDelete5");

		Vector<FlexoConceptObject> sel = new Vector<FlexoConceptObject>();
		sel.add(aStringInA);
		sel.add(someBooleanInA);
		DeleteFlexoConceptObjects deleteAction = DeleteFlexoConceptObjects.actionType.makeNewAction(null, sel, editor);
		assertSameList(deleteAction.getObjectsToDelete(), aStringInA, someBooleanInA);
		deleteAction.doAction();
		assertTrue(deleteAction.hasActionExecutionSucceeded());

		assertEquals(2, flexoConcept1.getFlexoProperties().size());
		assertTrue(flexoConcept1.getFlexoProperties().contains(someIntegerInA));
		assertTrue(flexoConcept1.getFlexoProperties().contains(someFlexoConcept2));

		assertEquals(1, validate(viewPoint).getErrorsCount());
	}

	@Test
	@TestOrder(10)
	public void testDelete6() throws InvalidSelectionException {

		log("testDelete6");

		Vector<FlexoConceptObject> sel = new Vector<FlexoConceptObject>();
		sel.add(action1);
		DeleteFlexoConceptObjects deleteAction = DeleteFlexoConceptObjects.actionType.makeNewAction(null, sel, editor);
		assertSameList(deleteAction.getObjectsToDelete(), action1);
		deleteAction.doAction();
		assertTrue(deleteAction.hasActionExecutionSucceeded());

		System.out.println("creationScheme cg = " + creationScheme.getControlGraph());

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

		assertViewPointIsValid(viewPoint);

	}

	@Test
	@TestOrder(11)
	public void testDelete7() throws InvalidSelectionException {

		log("testDelete7");

		Vector<FlexoConceptObject> sel = new Vector<FlexoConceptObject>();
		sel.add(creationScheme);
		DeleteFlexoConceptObjects deleteAction = DeleteFlexoConceptObjects.actionType.makeNewAction(null, sel, editor);
		assertSameList(deleteAction.getObjectsToDelete(), creationScheme);

		assertEquals(1, flexoConcept1.getFlexoBehaviours().size());

		deleteAction.doAction();
		assertTrue(deleteAction.hasActionExecutionSucceeded());
		assertTrue(creationScheme.isDeleted());

		assertEquals(0, flexoConcept1.getFlexoBehaviours().size());

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

		assertViewPointIsValid(viewPoint);

	}

	@Test
	@TestOrder(12)
	public void testDelete8() throws InvalidSelectionException {

		log("testDelete8");

		Vector<FlexoConceptObject> sel = new Vector<FlexoConceptObject>();
		sel.add(flexoConcept1);
		sel.add(flexoConcept2);
		DeleteFlexoConceptObjects deleteAction = DeleteFlexoConceptObjects.actionType.makeNewAction(null, sel, editor);
		assertSameList(deleteAction.getObjectsToDelete(), flexoConcept1, flexoConcept2);

		assertEquals(2, virtualModel.getFlexoConcepts().size());

		deleteAction.doAction();
		assertTrue(deleteAction.hasActionExecutionSucceeded());
		assertTrue(flexoConcept1.isDeleted());
		assertTrue(flexoConcept2.isDeleted());

		assertEquals(0, virtualModel.getFlexoConcepts().size());

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

		assertViewPointIsValid(viewPoint);

	}

}
