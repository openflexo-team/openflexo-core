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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.foundation.fml.PrimitiveRole.PrimitiveType;
import org.openflexo.foundation.fml.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.fml.VirtualModel.VirtualModelImpl;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviourParameter;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoRole;
import org.openflexo.foundation.fml.binding.FlexoBehaviourBindingModel;
import org.openflexo.foundation.fml.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.fml.binding.FlexoPropertyBindingVariable;
import org.openflexo.foundation.fml.binding.ViewPointBindingModel;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test {@link BindingModel} management along FML model<br>
 * 
 * In this test we try to move a complete control graph from an iteration to another iteration, and we check that binding model management
 * is consistent
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestFMLBindingModelManagement2 extends OpenflexoProjectAtRunTimeTestCase {

	static FlexoEditor editor;
	static ViewPoint viewPoint;
	static VirtualModel virtualModel;

	static FlexoConcept flexoConcept;

	static IterationAction iteration1;
	static IterationAction iteration2;

	static ActionScheme actionScheme;

	static DeclarationAction<String> assignation1;
	static AssignationAction<String> assignation2;

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
				serviceManager.getViewPointLibrary());
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
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel), virtualModel.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

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
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel), virtualModel.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

	}

	/**
	 * Test FlexoConcept creation, check BindingModel
	 */
	@Test
	@TestOrder(4)
	public void testCreateFlexoConceptA() throws SaveResourceException {

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptA");
		addEP.doAction();

		flexoConcept = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept A = " + flexoConcept);
		assertNotNull(flexoConcept);

		((VirtualModelResource) virtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) virtualModel.getResource()).getFlexoIODelegate());

		System.out.println("FlexoConcept BindingModel = " + flexoConcept.getBindingModel());

		assertEquals(6, flexoConcept.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConcept.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel), flexoConcept.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept), flexoConcept.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());

		// Disconnect FlexoConcept
		virtualModel.removeFromFlexoConcepts(flexoConcept);

		assertEquals(2, flexoConcept.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));

		// Reconnect FlexoConcept
		virtualModel.addToFlexoConcepts(flexoConcept);

		assertEquals(6, flexoConcept.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConcept.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel), flexoConcept.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept), flexoConcept.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());

		CreateFlexoRole createPR1 = CreateFlexoRole.actionType.makeNewAction(flexoConcept, null, editor);
		createPR1.setRoleName("aStringInA");
		createPR1.setFlexoRoleClass(PrimitiveRole.class);
		createPR1.setPrimitiveType(PrimitiveType.String);
		createPR1.doAction();

		CreateFlexoRole createPR2 = CreateFlexoRole.actionType.makeNewAction(flexoConcept, null, editor);
		createPR2.setRoleName("aBooleanInA");
		createPR2.setFlexoRoleClass(PrimitiveRole.class);
		createPR2.setPrimitiveType(PrimitiveType.Boolean);
		createPR2.doAction();

		CreateFlexoRole createPR3 = CreateFlexoRole.actionType.makeNewAction(flexoConcept, null, editor);
		createPR3.setRoleName("anIntegerInA");
		createPR3.setFlexoRoleClass(PrimitiveRole.class);
		createPR3.setPrimitiveType(PrimitiveType.Integer);
		createPR3.doAction();

		assertEquals(3, flexoConcept.getFlexoProperties().size());
		assertTrue(flexoConcept.getFlexoProperties().contains(createPR1.getNewFlexoRole()));
		assertTrue(flexoConcept.getFlexoProperties().contains(createPR2.getNewFlexoRole()));
		assertTrue(flexoConcept.getFlexoProperties().contains(createPR3.getNewFlexoRole()));

		System.out.println("FlexoConcept BindingModel = " + flexoConcept.getBindingModel());

		assertEquals(9, flexoConcept.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConcept.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel), flexoConcept.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept), flexoConcept.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConcept.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, flexoConcept.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, flexoConcept.getBindingModel().bindingVariableNamed("anIntegerInA").getType());

		PrimitiveRole aStringInA = (PrimitiveRole) flexoConcept.getFlexoProperty("aStringInA");
		assertNotNull(aStringInA);

		FlexoPropertyBindingVariable bv = (FlexoPropertyBindingVariable) flexoConcept.getBindingModel().bindingVariableNamed("aStringInA");
		assertNotNull(bv);

		// add property in FlexoConceptA
		CreateFlexoRole createOtherBooleanInA = CreateFlexoRole.actionType.makeNewAction(flexoConcept, null, editor);
		createOtherBooleanInA.setRoleName("anOtherBooleanInA");
		createOtherBooleanInA.setFlexoRoleClass(PrimitiveRole.class);
		createOtherBooleanInA.setPrimitiveType(PrimitiveType.Boolean);
		createOtherBooleanInA.doAction();

	}

	@Test
	@TestOrder(5)
	public void testCreateFirstIteration() throws SaveResourceException {

		// We programmatically implement this code:
		// ActionScheme testFetchRequestIteration(String aString, Boolean aBoolean) {
		// ... for (item in SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = parameters.aBoolean; selected.aStringInA = parameters.aString)) {
		// .........name = item.aStringInA;
		// .........item.aStringInA = (name + "foo");
		// ......}
		// ...}
		// }

		CreateFlexoBehaviour createActionScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConcept, null, editor);
		createActionScheme.setFlexoBehaviourName("testActionScheme");
		createActionScheme.setFlexoBehaviourClass(ActionScheme.class);
		createActionScheme.doAction();
		actionScheme = (ActionScheme) createActionScheme.getNewFlexoBehaviour();
		assertNotNull(actionScheme);

		CreateFlexoBehaviourParameter createStringParameter = CreateFlexoBehaviourParameter.actionType.makeNewAction(actionScheme, null,
				editor);
		createStringParameter.setFlexoBehaviourParameterClass(TextFieldParameter.class);
		createStringParameter.setParameterName("aString");
		createStringParameter.doAction();
		FlexoBehaviourParameter param1 = createStringParameter.getNewParameter();
		assertNotNull(param1);
		assertTrue(actionScheme.getParameters().contains(param1));

		CreateFlexoBehaviourParameter createBooleanParameter = CreateFlexoBehaviourParameter.actionType.makeNewAction(actionScheme, null,
				editor);
		createBooleanParameter.setFlexoBehaviourParameterClass(CheckboxParameter.class);
		createBooleanParameter.setParameterName("aBoolean");
		createBooleanParameter.doAction();
		FlexoBehaviourParameter param2 = createBooleanParameter.getNewParameter();
		assertNotNull(param2);
		assertTrue(actionScheme.getParameters().contains(param2));

		for (int i = 0; i < actionScheme.getBindingModel().getBindingVariablesCount(); i++) {
			System.out.println("Variable at " + i + " = " + actionScheme.getBindingModel().getBindingVariableAt(i));
		}

		assertEquals(12, actionScheme.getBindingModel().getBindingVariablesCount());

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				actionScheme.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel), actionScheme.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept), actionScheme.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, actionScheme.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));

		CreateEditionAction createIterationAction = CreateEditionAction.actionType.makeNewAction(actionScheme.getControlGraph(), null,
				editor);
		// createIterationAction.actionChoice = CreateEditionActionChoice.ControlAction;
		createIterationAction.setEditionActionClass(IterationAction.class);

		// createSelectFetchRequestIterationAction.setRequestActionClass(SelectFlexoConceptInstance.class);
		createIterationAction.doAction();
		iteration1 = (IterationAction) createIterationAction.getNewEditionAction();

		SelectFlexoConceptInstance selectFlexoConceptInstance = iteration1.getFMLModelFactory().newSelectFlexoConceptInstance();
		selectFlexoConceptInstance.setFlexoConceptType(flexoConcept);
		iteration1.setIterationAction(selectFlexoConceptInstance);

		/*CreateEditionAction createFetchRequest = CreateEditionAction.actionType.makeNewAction(fetchRequestIteration.getControlGraph(),
				null, editor);
		createFetchRequest.actionChoice = CreateEditionActionChoice.RequestAction;
		createFetchRequest.setRequestActionClass(SelectFlexoConceptInstance.class);
		createFetchRequest.doAction();
		SelectFlexoConceptInstance selectFlexoConceptInstance = (SelectFlexoConceptInstance) createFetchRequest.getNewEditionAction();
		selectFlexoConceptInstance.setFlexoConceptType(flexoConceptA);

		fetchRequestIteration.setIterationAction(selectFlexoConceptInstance);*/

		FetchRequestCondition condition1 = selectFlexoConceptInstance.createCondition();
		condition1.setCondition(new DataBinding<Boolean>("selected.aBooleanInA = parameters.aBoolean"));

		FetchRequestCondition condition2 = selectFlexoConceptInstance.createCondition();
		condition2.setCondition(new DataBinding<Boolean>("selected.aStringInA = parameters.aString"));

		assertEquals(12, iteration1.getBindingModel().getBindingVariablesCount());
		assertEquals(13, condition1.getBindingModel().getBindingVariablesCount());
		assertNotNull(condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept),
				condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED).getType());

		CreateEditionAction createAssignationAction = CreateEditionAction.actionType.makeNewAction(iteration1.getControlGraph(), null,
				editor);
		// createAssignationAction.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createAssignationAction.setEditionActionClass(ExpressionAction.class);
		createAssignationAction.setDeclarationVariableName("name");
		createAssignationAction.doAction();
		assignation1 = (DeclarationAction<String>) createAssignationAction.getNewEditionAction();
		((ExpressionAction) assignation1.getAssignableAction()).setExpression(new DataBinding<Object>("item.aStringInA"));

		CreateEditionAction createAssignationAction2 = CreateEditionAction.actionType.makeNewAction(iteration1.getControlGraph(), null,
				editor);
		// createAssignationAction2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createAssignationAction2.setEditionActionClass(ExpressionAction.class);
		createAssignationAction2.setAssignation(new DataBinding<Object>("item.aStringInA"));
		createAssignationAction2.doAction();
		assignation2 = (AssignationAction<String>) createAssignationAction2.getNewEditionAction();
		((ExpressionAction) assignation2.getAssignableAction()).setExpression(new DataBinding<Object>("name+\"foo\""));

		assertTrue(((ExpressionAction) assignation2.getAssignableAction()).getExpression().isValid());
		assertTrue(assignation2.getAssignation().isValid());

		assertTrue(((ExpressionAction) assignation2.getAssignableAction()).getExpression().isValid());

		assertEquals(12, iteration1.getBindingModel().getBindingVariablesCount());

		assertEquals(13, condition1.getBindingModel().getBindingVariablesCount());
		assertNotNull(condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept),
				condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED).getType());

		assertEquals(14, assignation1.getInferedBindingModel().getBindingVariablesCount());
		assertNull(assignation1.getInferedBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertNotNull(assignation1.getBindingModel().bindingVariableNamed(iteration1.getIteratorName()));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept), assignation1.getBindingModel()
				.bindingVariableNamed(iteration1.getIteratorName()).getType());
		assertNotNull(assignation1.getInferedBindingModel().bindingVariableNamed("name"));
		assertEquals(String.class, assignation1.getInferedBindingModel().bindingVariableNamed("name").getType());

		System.out.println("FML: " + actionScheme.getFMLRepresentation());
	}

	@Test
	@TestOrder(5)
	public void testCreateSecondIteration() throws SaveResourceException {

		// We add a second iteration with no control graph
		//
		// ActionScheme testFetchRequestIteration(String aString, Boolean aBoolean) {
		// ... for (item in SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = parameters.aBoolean; selected.aStringInA = parameters.aString)) {
		// .........name = item.aStringInA;
		// .........item.aStringInA = (name + "foo");
		// ....}
		// ....for (item2 in SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = !parameters.aBoolean)) {
		// ....}
		// ...}
		// }

		CreateEditionAction createIterationAction2 = CreateEditionAction.actionType.makeNewAction(actionScheme.getControlGraph(), null,
				editor);
		// createIterationAction.actionChoice = CreateEditionActionChoice.ControlAction;
		createIterationAction2.setEditionActionClass(IterationAction.class);

		// createSelectFetchRequestIterationAction.setRequestActionClass(SelectFlexoConceptInstance.class);
		createIterationAction2.doAction();
		iteration2 = (IterationAction) createIterationAction2.getNewEditionAction();

		SelectFlexoConceptInstance selectFlexoConceptInstance = iteration2.getFMLModelFactory().newSelectFlexoConceptInstance();
		selectFlexoConceptInstance.setFlexoConceptType(flexoConcept);
		iteration2.setIterationAction(selectFlexoConceptInstance);
		iteration2.setIteratorName("item2");

		FetchRequestCondition condition1 = selectFlexoConceptInstance.createCondition();
		condition1.setCondition(new DataBinding<Boolean>("selected.aBooleanInA = !(parameters.aBoolean)"));

		System.out.println("FML: " + actionScheme.getFMLRepresentation());

		assertEquals(12, iteration2.getBindingModel().getBindingVariablesCount());

		assertEquals(13, iteration2.getInferedBindingModel().getBindingVariablesCount());

		/*for (int i = 0; i < iteration2.getInferedBindingModel().getBindingVariablesCount(); i++) {
			System.out.println("Variable at " + i + " = " + iteration2.getInferedBindingModel().getBindingVariableAt(i));
		}*/

		assertEquals(iteration2.getInferedBindingModel(), iteration2.getControlGraph().getBindingModel());

		assertNotNull(iteration2.getInferedBindingModel().bindingVariableNamed("item2"));

		assertEquals(13, condition1.getBindingModel().getBindingVariablesCount());
		assertNotNull(condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept),
				condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED).getType());

		System.out.println("FML: " + actionScheme.getFMLRepresentation());

	}

	@Test
	@TestOrder(6)
	public void moveControlGraph() throws SaveResourceException {

		// We move the control graph from one iteration to another one
		//
		// ActionScheme testFetchRequestIteration(String aString, Boolean aBoolean) {
		// ... for (item in SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = parameters.aBoolean; selected.aStringInA = parameters.aString)) {
		// ....}
		// ....for (item2 in SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = !parameters.aBoolean)) {
		// .........name = item2.aStringInA;
		// .........item2.aStringInA = (name + "foo");
		// ....}
		// ...}
		// }

		System.out.println("Iteration1: " + iteration1.getFMLRepresentation());
		System.out.println("Iteration2: " + iteration2.getFMLRepresentation());

		FMLControlGraph cg = iteration1.getControlGraph();
		iteration1.setControlGraph(null);
		iteration2.setControlGraph(cg);

		System.out.println("CG is a " + cg.getClass());

		System.out.println("FML: " + actionScheme.getFMLRepresentation());

		assertEquals(iteration2, cg.getOwner());

		System.out.println("Assignation1: " + assignation1 + " FML=" + assignation1.getFMLRepresentation());
		System.out.println("Assignation2: " + assignation2 + " FML=" + assignation2.getFMLRepresentation());

		/*for (int i = 0; i < assignation1.getBindingModel().getBindingVariablesCount(); i++) {
			System.out.println("Variable at " + i + " = " + assignation1.getBindingModel().getBindingVariableAt(i));
		}*/

		/*for (int i = 0; i < assignation1.getBindingModel().getBaseBindingModel().getBindingVariablesCount(); i++) {
			System.out.println("1 / Variable at " + i + " = "
					+ assignation1.getBindingModel().getBaseBindingModel().getBindingVariableAt(i));
		}

		for (int i = 0; i < assignation2.getBindingModel().getBaseBindingModel().getBindingVariablesCount(); i++) {
			System.out.println("2 / Variable at " + i + " = "
					+ assignation2.getBindingModel().getBaseBindingModel().getBindingVariableAt(i));
		}

		for (int i = 0; i < iteration2.getInferedBindingModel().getBindingVariablesCount(); i++) {
			System.out.println("3 / Variable at " + i + " = " + iteration2.getInferedBindingModel().getBindingVariableAt(i));
		}
		*/

		// System.out.println("Le BM c'est un " + assignation2.getBindingModel().getClass());
		// System.out.println("Le BM c'est un " + assignation2.getBindingModel().getBaseBindingModel().getClass());

		assertEquals(iteration2.getInferedBindingModel(), assignation1.getBindingModel().getBaseBindingModel());
		assertEquals(iteration2.getInferedBindingModel(), assignation2.getBindingModel().getBaseBindingModel().getBaseBindingModel());

		// Type is undefined (Object) because
		System.out.println("Type=" + assignation1.getAssignableType());
		assertEquals(Object.class, assignation1.getAssignableType());

		ExpressionAction<String> expression1 = (ExpressionAction<String>) assignation1.getAssignableAction();
		assertFalse(expression1.getExpression().isValid());

		expression1.setExpression(new DataBinding<String>("item2.aStringInA"));
		assertTrue(expression1.getExpression().isValid());
		System.out.println("Type=" + assignation1.getAssignableType());

		assertFalse(assignation2.getAssignation().isValid());

		assignation2.setAssignation(new DataBinding<String>("item2.aStringInA"));
		assertTrue(assignation2.getAssignation().isValid());

		System.out.println("FML: " + actionScheme.getFMLRepresentation());
	}

	@Test
	@TestOrder(19)
	public void testViewPointIsValid() {

		assertViewPointIsValid(viewPoint);

	}

}
