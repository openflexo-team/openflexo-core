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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.PrimitiveRole.PrimitiveType;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviourParameter;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.ViewPointResourceFactory;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test FlexoConcept creation facilities
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestCreateFlexoConcept extends OpenflexoTestCase {

	static FlexoEditor editor;
	static ViewPoint newViewPoint;
	static AbstractVirtualModel<?> newVirtualModel;

	static FlexoConcept flexoConceptA;
	static FlexoConcept flexoConceptB;
	static FlexoConcept flexoConceptC;
	static FlexoConcept flexoConceptD;
	static FlexoConcept flexoConceptE;

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestViewPoint";
	public static final String VIRTUAL_MODEL_NAME = "TestVirtualModel";

	/**
	 * Test the VP creation
	 * 
	 * @throws ModelDefinitionException
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(1)
	public void testCreateViewPoint() throws SaveResourceException, ModelDefinitionException {
		instanciateTestServiceManager();
		System.out.println("ResourceCenter= " + resourceCenter);

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		ViewPointResourceFactory factory = fmlTechnologyAdapter.getViewPointResourceFactory();

		ViewPointResource newViewPointResource = factory.makeViewPointResource(VIEWPOINT_NAME, VIEWPOINT_URI,
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(),
				fmlTechnologyAdapter.getTechnologyContextManager(), true);
		newViewPoint = newViewPointResource.getLoadedResourceData();

		// assertTrue(((ViewPointResource) newViewPoint.getResource()).getDirectory().exists());
		// assertTrue(((ViewPointResource) newViewPoint.getResource()).getFile().exists());
		assertTrue(((ViewPointResource) newViewPoint.getResource()).getDirectory() != null);
		assertTrue(((ViewPointResource) newViewPoint.getResource()).getFlexoIODelegate().exists());

		assertEquals(newViewPoint, newViewPoint.getViewPoint());
		assertEquals(newViewPoint, newViewPoint.getVirtualModel());
		assertEquals(null, newViewPoint.getOwningVirtualModel());
		assertEquals(newViewPoint, newViewPoint.getFlexoConcept());
	}

	/**
	 * Test the VirtualModel creation
	 * 
	 * @throws ModelDefinitionException
	 */
	@Test
	@TestOrder(2)
	public void testCreateVirtualModel() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getViewPointResourceFactory().getVirtualModelResourceFactory();
		VirtualModelResource newVMResource = factory.makeVirtualModelResource(VIRTUAL_MODEL_NAME, newViewPoint.getViewPointResource(),
				fmlTechnologyAdapter.getTechnologyContextManager(), true);
		newVirtualModel = newVMResource.getLoadedResourceData();

		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) newVirtualModel.getResource()).getDirectory()).exists());
		assertTrue(((VirtualModelResource) newVirtualModel.getResource()).getFlexoIODelegate().exists());

		assertEquals(newViewPoint, newVirtualModel.getViewPoint());
		assertEquals(newVirtualModel, newVirtualModel.getVirtualModel());
		// assertEquals(null, newVirtualModel.getOwningVirtualModel());

		assertSame(newViewPoint, newVirtualModel.getOwningVirtualModel());

		assertEquals(newVirtualModel, newVirtualModel.getFlexoConcept());
	}

	/**
	 * Test the FlexoConcept creation
	 */
	@Test
	@TestOrder(3)
	public void testCreateEditor() {
		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);
	}

	/**
	 * Test the FlexoConcept creation
	 */
	@Test
	@TestOrder(4)
	public void testCreateFlexoConceptA() throws SaveResourceException {

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptA");

		addEP.doAction();

		flexoConceptA = addEP.getNewFlexoConcept();

		assertNotNull(flexoConceptA);

		assertEquals(newViewPoint, flexoConceptA.getViewPoint());
		assertEquals(newVirtualModel, flexoConceptA.getVirtualModel());
		assertEquals(newVirtualModel, flexoConceptA.getOwningVirtualModel());
		assertEquals(flexoConceptA, flexoConceptA.getFlexoConcept());
		assertEquals(newVirtualModel, flexoConceptA.getResourceData());

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		// System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFile());
		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFlexoIODelegate().toString());

	}

	/**
	 * Test the FlexoConcept creation
	 */
	@Test
	@TestOrder(5)
	public void testCreateFlexoConceptB() throws SaveResourceException {

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptB");
		addEP.doAction();

		flexoConceptB = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept B = " + flexoConceptB);
		assertNotNull(flexoConceptB);

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFlexoIODelegate().toString());

	}

	/**
	 * Test the FlexoConcept creation
	 */
	@Test
	@TestOrder(6)
	public void testCreateFlexoConceptC() throws SaveResourceException {

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptC");
		addEP.addToParentConcepts(flexoConceptB);
		addEP.doAction();

		flexoConceptC = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept C = " + flexoConceptC);
		assertNotNull(flexoConceptC);
		assertEquals(1, flexoConceptC.getParentFlexoConcepts().size());
		assertEquals(flexoConceptB, flexoConceptC.getParentFlexoConcepts().get(0));

		assertEquals(1, flexoConceptB.getChildFlexoConcepts().size());
		assertEquals(flexoConceptC, flexoConceptB.getChildFlexoConcepts().get(0));

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFlexoIODelegate().toString());

	}

	/**
	 * Test the FlexoConcept creation
	 */
	@Test
	@TestOrder(7)
	public void testCreateFlexoConceptD() throws SaveResourceException {

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptD");
		addEP.addToParentConcepts(flexoConceptB);
		addEP.doAction();

		flexoConceptD = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept D = " + flexoConceptD);
		assertNotNull(flexoConceptD);
		assertEquals(1, flexoConceptD.getParentFlexoConcepts().size());
		assertEquals(flexoConceptB, flexoConceptD.getParentFlexoConcepts().get(0));

		assertEquals(2, flexoConceptB.getChildFlexoConcepts().size());
		assertEquals(flexoConceptC, flexoConceptB.getChildFlexoConcepts().get(0));
		assertEquals(flexoConceptD, flexoConceptB.getChildFlexoConcepts().get(1));

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFlexoIODelegate().toString());

	}

	/**
	 * Test the FlexoConcept creation
	 */
	@Test
	@TestOrder(8)
	public void testCreateFlexoConceptE() throws SaveResourceException {

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptE");
		addEP.addToParentConcepts(flexoConceptA);
		addEP.addToParentConcepts(flexoConceptB);
		addEP.addToParentConcepts(flexoConceptC);
		addEP.doAction();

		flexoConceptE = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept E = " + flexoConceptE);
		assertNotNull(flexoConceptE);
		assertEquals(3, flexoConceptE.getParentFlexoConcepts().size());
		assertEquals(flexoConceptA, flexoConceptE.getParentFlexoConcepts().get(0));
		assertEquals(flexoConceptB, flexoConceptE.getParentFlexoConcepts().get(1));
		assertEquals(flexoConceptC, flexoConceptE.getParentFlexoConcepts().get(2));

		assertEquals(1, flexoConceptA.getChildFlexoConcepts().size());
		assertEquals(flexoConceptE, flexoConceptA.getChildFlexoConcepts().get(0));
		assertEquals(3, flexoConceptB.getChildFlexoConcepts().size());
		assertEquals(flexoConceptC, flexoConceptB.getChildFlexoConcepts().get(0));
		assertEquals(flexoConceptD, flexoConceptB.getChildFlexoConcepts().get(1));
		assertEquals(flexoConceptE, flexoConceptB.getChildFlexoConcepts().get(2));
		assertEquals(1, flexoConceptC.getChildFlexoConcepts().size());
		assertEquals(flexoConceptE, flexoConceptC.getChildFlexoConcepts().get(0));

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFlexoIODelegate().toString());

	}

	@Test
	@TestOrder(9)
	public void testCreateSomePatternRolesToConceptA() throws SaveResourceException {

		CreatePrimitiveRole createPR1 = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR1.setRoleName("aString");
		createPR1.setPrimitiveType(PrimitiveType.String);
		createPR1.doAction();
		PrimitiveRole<String> role1 = (PrimitiveRole<String>) createPR1.getNewFlexoRole();
		assertNotNull(role1);

		CreatePrimitiveRole createPR2 = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR2.setRoleName("aBoolean");
		createPR2.setPrimitiveType(PrimitiveType.Boolean);
		createPR2.doAction();
		PrimitiveRole<Boolean> role2 = (PrimitiveRole<Boolean>) createPR2.getNewFlexoRole();
		assertNotNull(role2);

		CreatePrimitiveRole createPR3 = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR3.setRoleName("anInteger");
		createPR3.setPrimitiveType(PrimitiveType.Integer);
		createPR3.doAction();
		PrimitiveRole<Integer> role3 = (PrimitiveRole<Integer>) createPR3.getNewFlexoRole();
		assertNotNull(role3);

		assertEquals(3, flexoConceptA.getFlexoProperties().size());
		assertTrue(flexoConceptA.getFlexoProperties().contains(role1));
		assertTrue(flexoConceptA.getFlexoProperties().contains(role2));
		assertTrue(flexoConceptA.getFlexoProperties().contains(role3));

		assertEquals(newViewPoint, role1.getViewPoint());
		assertEquals(newVirtualModel, role1.getOwningVirtualModel());
		assertEquals(flexoConceptA, role1.getFlexoConcept());
		assertEquals(newVirtualModel, role1.getResourceData());

		assertEquals(newViewPoint, role2.getViewPoint());
		assertEquals(newVirtualModel, role2.getOwningVirtualModel());
		assertEquals(flexoConceptA, role2.getFlexoConcept());
		assertEquals(newVirtualModel, role2.getResourceData());

		assertEquals(newViewPoint, role3.getViewPoint());
		assertEquals(newVirtualModel, role3.getOwningVirtualModel());
		assertEquals(flexoConceptA, role3.getFlexoConcept());
		assertEquals(newVirtualModel, role3.getResourceData());

	}

	@Test
	@TestOrder(10)
	public void testCreateACreationSchemeInConceptA() throws SaveResourceException {

		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme.doAction();
		CreationScheme creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();

		CreateEditionAction createEditionAction1 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction1.setEditionActionClass(ExpressionAction.class);
		createEditionAction1.setAssignation(new DataBinding<Object>("aString"));
		createEditionAction1.doAction();
		AssignationAction<?> action1 = (AssignationAction<?>) createEditionAction1.getNewEditionAction();
		((ExpressionAction) action1.getAssignableAction()).setExpression(new DataBinding<Object>("'foo'"));
		action1.setName("action1");

		CreateEditionAction createEditionAction2 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction2.setEditionActionClass(ExpressionAction.class);
		createEditionAction2.setAssignation(new DataBinding<Object>("aBoolean"));
		createEditionAction2.doAction();
		AssignationAction<?> action2 = (AssignationAction<?>) createEditionAction2.getNewEditionAction();
		((ExpressionAction) action2.getAssignableAction()).setExpression(new DataBinding<Object>("true"));
		action2.setName("action2");

		CreateEditionAction createEditionAction3 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction3.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction3.setEditionActionClass(ExpressionAction.class);
		createEditionAction3.setAssignation(new DataBinding<Object>("anInteger"));
		createEditionAction3.doAction();
		AssignationAction<?> action3 = (AssignationAction<?>) createEditionAction3.getNewEditionAction();
		((ExpressionAction) action3.getAssignableAction()).setExpression(new DataBinding<Object>("8"));
		action3.setName("action3");

		assertTrue(flexoConceptA.getFlexoBehaviours().contains(creationScheme));
		assertTrue(flexoConceptA.getCreationSchemes().contains(creationScheme));

		assertEquals(newViewPoint, creationScheme.getViewPoint());
		assertEquals(newVirtualModel, creationScheme.getOwningVirtualModel());
		assertEquals(flexoConceptA, creationScheme.getFlexoConcept());

		assertEquals(newViewPoint, action1.getViewPoint());
		assertEquals(newVirtualModel, action1.getOwningVirtualModel());
		assertEquals(flexoConceptA, action1.getFlexoConcept());

	}

	@Test
	@TestOrder(11)
	public void testCreateAnActionSchemeInConceptA() throws SaveResourceException {

		CreateFlexoBehaviour createActionScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createActionScheme.setFlexoBehaviourClass(ActionScheme.class);
		createActionScheme.doAction();
		ActionScheme actionScheme = (ActionScheme) createActionScheme.getNewFlexoBehaviour();
		assertNotNull(actionScheme);

		CreateFlexoBehaviourParameter createParameter = CreateFlexoBehaviourParameter.actionType.makeNewAction(actionScheme, null, editor);
		createParameter.setFlexoBehaviourParameterClass(CheckboxParameter.class);
		createParameter.setParameterName("aFlag");
		createParameter.doAction();
		FlexoBehaviourParameter param = createParameter.getNewParameter();
		assertNotNull(param);
		assertTrue(actionScheme.getParameters().contains(param));

		CreateEditionAction createConditionAction1 = CreateEditionAction.actionType.makeNewAction(actionScheme.getControlGraph(), null,
				editor);
		// createConditionAction1.actionChoice = CreateEditionActionChoice.ControlAction;
		createConditionAction1.setEditionActionClass(ConditionalAction.class);
		createConditionAction1.doAction();
		ConditionalAction conditional1 = (ConditionalAction) createConditionAction1.getNewEditionAction();
		conditional1.setCondition(new DataBinding<Boolean>("parameters.aFlag = true"));

		assertNotNull(conditional1);
		assertTrue(conditional1.getCondition().isValid());

		CreateEditionAction createDeclarePatternRoleInCondition1 = CreateEditionAction.actionType
				.makeNewAction(conditional1.getThenControlGraph(), null, editor);
		// createDeclarePatternRoleInCondition1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclarePatternRoleInCondition1.setEditionActionClass(ExpressionAction.class);
		createDeclarePatternRoleInCondition1.setAssignation(new DataBinding<Object>("anInteger"));
		createDeclarePatternRoleInCondition1.doAction();
		AssignationAction<?> declarePatternRoleInCondition1 = (AssignationAction<?>) createDeclarePatternRoleInCondition1
				.getNewEditionAction();
		((ExpressionAction) declarePatternRoleInCondition1.getAssignableAction()).setExpression(new DataBinding<Object>("8"));

		CreateEditionAction createConditionAction2 = CreateEditionAction.actionType.makeNewAction(actionScheme.getControlGraph(), null,
				editor);
		// createConditionAction2.actionChoice = CreateEditionActionChoice.ControlAction;
		createConditionAction2.setEditionActionClass(ConditionalAction.class);
		createConditionAction2.doAction();
		ConditionalAction conditional2 = (ConditionalAction) createConditionAction2.getNewEditionAction();
		conditional2.setCondition(new DataBinding<Boolean>("parameters.aFlag = false"));

		assertNotNull(conditional2);
		assertTrue(conditional2.getCondition().isValid());

		CreateEditionAction createDeclarePatternRoleInCondition2 = CreateEditionAction.actionType
				.makeNewAction(conditional2.getThenControlGraph(), null, editor);
		// createDeclarePatternRoleInCondition2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclarePatternRoleInCondition2.setEditionActionClass(ExpressionAction.class);
		createDeclarePatternRoleInCondition2.setAssignation(new DataBinding<Object>("anInteger"));
		createDeclarePatternRoleInCondition2.doAction();
		AssignationAction<?> declarePatternRoleInCondition2 = (AssignationAction<?>) createDeclarePatternRoleInCondition2
				.getNewEditionAction();
		((ExpressionAction) declarePatternRoleInCondition2.getAssignableAction()).setExpression(new DataBinding<Object>("12"));

	}
}
