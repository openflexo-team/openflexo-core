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

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateGenericBehaviourParameter;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.binding.FlexoBehaviourBindingModel;
import org.openflexo.foundation.fml.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.fml.binding.FlexoPropertyBindingVariable;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.model.exceptions.ModelDefinitionException;
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

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestViewPoint";
	public static final String VIRTUAL_MODEL_NAME = "TestVirtualModel";

	static FlexoEditor editor;
	static VirtualModel viewPoint;
	static VirtualModel virtualModel;

	static FlexoConcept flexoConcept;

	static IterationAction iteration1;
	static IterationAction iteration2;

	static ActionScheme actionScheme;

	static DeclarationAction<String> assignation1;
	static AssignationAction<String> assignation2;

	private static DirectoryResourceCenter resourceCenter;

	/**
	 * Init
	 * 
	 * @throws IOException
	 */
	@Test
	@TestOrder(1)
	public void init() throws IOException {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		resourceCenter = makeNewDirectoryResourceCenter();
		assertNotNull(resourceCenter);
		System.out.println("ResourceCenter= " + resourceCenter);
	}

	/**
	 * Test {@link ViewPoint} creation, check {@link BindingModel}
	 * 
	 * @throws ModelDefinitionException
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(2)
	public void testCreateViewPoint() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();

		VirtualModelResource newVirtualModelResource = factory.makeTopLevelVirtualModelResource(VIEWPOINT_NAME, VIEWPOINT_URI,
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(), true);
		viewPoint = newVirtualModelResource.getLoadedResourceData();

		// viewPoint = ViewPointImpl.newViewPoint("TestViewPoint",
		// "http://openflexo.org/test/TestViewPoint",
		// resourceCenter.getDirectory(),
		// serviceManager.getViewPointLibrary(), resourceCenter);
		// assertTrue(((VirtualModelResource)
		// viewPoint.getResource()).getDirectory().exists());
		// assertTrue(((VirtualModelResource)
		// viewPoint.getResource()).getFile().exists());
		assertTrue(((VirtualModelResource) viewPoint.getResource()).getDirectory() != null);
		assertTrue(((VirtualModelResource) viewPoint.getResource()).getIODelegate().exists());

		System.out.println("ViewPoint BindingModel = " + viewPoint.getBindingModel());
		assertNotNull(viewPoint.getBindingModel());
		assertEquals(1, viewPoint.getBindingModel().getBindingVariablesCount());
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));

	}

	/**
	 * Test {@link VirtualModel} creation, check {@link BindingModel}
	 * 
	 * @throws ModelDefinitionException
	 */
	@Test
	@TestOrder(3)
	public void testCreateVirtualModel() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();
		VirtualModelResource newVMResource = factory.makeContainedVirtualModelResource("VM1", viewPoint.getVirtualModelResource(), true);
		virtualModel = newVMResource.getLoadedResourceData();

		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) virtualModel.getResource()).getDirectory()).exists());
		assertTrue(((VirtualModelResource) virtualModel.getResource()).getIODelegate().exists());

		assertNotNull(virtualModel.getBindingModel());
		assertEquals(2, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel),
				virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

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

		System.out.println("Saved: " + ((VirtualModelResource) virtualModel.getResource()).getIODelegate());

		System.out.println("FlexoConcept BindingModel = " + flexoConcept.getBindingModel());

		assertEquals(2, flexoConcept.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel),
				flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept),
				flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

		CreatePrimitiveRole createPR1 = CreatePrimitiveRole.actionType.makeNewAction(flexoConcept, null, editor);
		createPR1.setRoleName("aStringInA");
		createPR1.setPrimitiveType(PrimitiveType.String);
		createPR1.doAction();

		CreatePrimitiveRole createPR2 = CreatePrimitiveRole.actionType.makeNewAction(flexoConcept, null, editor);
		createPR2.setRoleName("aBooleanInA");
		createPR2.setPrimitiveType(PrimitiveType.Boolean);
		createPR2.doAction();

		CreatePrimitiveRole createPR3 = CreatePrimitiveRole.actionType.makeNewAction(flexoConcept, null, editor);
		createPR3.setRoleName("anIntegerInA");
		createPR3.setPrimitiveType(PrimitiveType.Integer);
		createPR3.doAction();

		assertEquals(3, flexoConcept.getFlexoProperties().size());
		assertTrue(flexoConcept.getFlexoProperties().contains(createPR1.getNewFlexoRole()));
		assertTrue(flexoConcept.getFlexoProperties().contains(createPR2.getNewFlexoRole()));
		assertTrue(flexoConcept.getFlexoProperties().contains(createPR3.getNewFlexoRole()));

		System.out.println("FlexoConcept BindingModel = " + flexoConcept.getBindingModel());

		assertEquals(5, flexoConcept.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel),
				flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept),
				flexoConcept.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConcept.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, flexoConcept.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(flexoConcept.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, flexoConcept.getBindingModel().bindingVariableNamed("anIntegerInA").getType());

		PrimitiveRole<?> aStringInA = (PrimitiveRole<?>) flexoConcept.getAccessibleProperty("aStringInA");
		assertNotNull(aStringInA);

		FlexoPropertyBindingVariable bv = (FlexoPropertyBindingVariable) flexoConcept.getBindingModel().bindingVariableNamed("aStringInA");
		assertNotNull(bv);

		// add property in FlexoConceptA
		CreatePrimitiveRole createOtherBooleanInA = CreatePrimitiveRole.actionType.makeNewAction(flexoConcept, null, editor);
		createOtherBooleanInA.setRoleName("anOtherBooleanInA");
		createOtherBooleanInA.setPrimitiveType(PrimitiveType.Boolean);
		createOtherBooleanInA.doAction();

	}

	@Test
	@TestOrder(5)
	public void testCreateFirstIteration() {

		// We programmatically implement this code:
		// ActionScheme testFetchRequestIteration(String aString, Boolean
		// aBoolean) {
		// ... for (item in SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = parameters.aBoolean;
		// selected.aStringInA = parameters.aString)) {
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

		CreateGenericBehaviourParameter createStringParameter = CreateGenericBehaviourParameter.actionType.makeNewAction(actionScheme, null,
				editor);
		createStringParameter.setParameterName("aString");
		createStringParameter.setParameterType(String.class);
		createStringParameter.doAction();
		FlexoBehaviourParameter param1 = createStringParameter.getNewParameter();
		assertNotNull(param1);
		assertTrue(actionScheme.getParameters().contains(param1));

		CreateGenericBehaviourParameter createBooleanParameter = CreateGenericBehaviourParameter.actionType.makeNewAction(actionScheme,
				null, editor);
		createBooleanParameter.setParameterName("aBoolean");
		createBooleanParameter.setParameterType(Boolean.class);
		createBooleanParameter.doAction();
		FlexoBehaviourParameter param2 = createBooleanParameter.getNewParameter();
		assertNotNull(param2);
		assertTrue(actionScheme.getParameters().contains(param2));

		for (int i = 0; i < actionScheme.getBindingModel().getBindingVariablesCount(); i++) {
			System.out.println("Variable at " + i + " = " + actionScheme.getBindingModel().getBindingVariableAt(i));
		}

		assertEquals(7, actionScheme.getBindingModel().getBindingVariablesCount());

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept),
				actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, actionScheme.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));

		CreateEditionAction createIterationAction = CreateEditionAction.actionType.makeNewAction(actionScheme.getControlGraph(), null,
				editor);
		// createIterationAction.actionChoice =
		// CreateEditionActionChoice.ControlAction;
		createIterationAction.setEditionActionClass(IterationAction.class);

		// createSelectFetchRequestIterationAction.setRequestActionClass(SelectFlexoConceptInstance.class);
		createIterationAction.doAction();
		iteration1 = (IterationAction) createIterationAction.getNewEditionAction();

		SelectFlexoConceptInstance selectFlexoConceptInstance = iteration1.getFMLModelFactory().newSelectFlexoConceptInstance();
		selectFlexoConceptInstance.setFlexoConceptType(flexoConcept);
		selectFlexoConceptInstance.setReceiver(new DataBinding<>("container"));
		iteration1.setIterationAction(selectFlexoConceptInstance);

		FetchRequestCondition condition1 = selectFlexoConceptInstance.createCondition();
		condition1.setCondition(new DataBinding<Boolean>("selected.aBooleanInA = parameters.aBoolean"));

		FetchRequestCondition condition2 = selectFlexoConceptInstance.createCondition();
		condition2.setCondition(new DataBinding<Boolean>("selected.aStringInA = parameters.aString"));

		assertEquals(7, iteration1.getBindingModel().getBindingVariablesCount());
		assertEquals(8, condition1.getBindingModel().getBindingVariablesCount());
		assertNotNull(condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept),
				condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED).getType());

		CreateEditionAction createAssignationAction = CreateEditionAction.actionType.makeNewAction(iteration1.getControlGraph(), null,
				editor);
		createAssignationAction.setEditionActionClass(ExpressionAction.class);
		createAssignationAction.setDeclarationVariableName("name");
		createAssignationAction.doAction();
		assignation1 = (DeclarationAction<String>) createAssignationAction.getNewEditionAction();
		((ExpressionAction) assignation1.getAssignableAction()).setExpression(new DataBinding<>("item.aStringInA"));

		CreateEditionAction createAssignationAction2 = CreateEditionAction.actionType.makeNewAction(iteration1.getControlGraph(), null,
				editor);
		createAssignationAction2.setEditionActionClass(ExpressionAction.class);
		createAssignationAction2.setAssignation(new DataBinding<>("item.aStringInA"));
		createAssignationAction2.doAction();
		assignation2 = (AssignationAction<String>) createAssignationAction2.getNewEditionAction();
		((ExpressionAction) assignation2.getAssignableAction()).setExpression(new DataBinding<>("name+\"foo\""));

		assertTrue(((ExpressionAction) assignation2.getAssignableAction()).getExpression().isValid());
		assertTrue(assignation2.getAssignation().isValid());

		assertTrue(((ExpressionAction) assignation2.getAssignableAction()).getExpression().isValid());

		assertEquals(7, iteration1.getBindingModel().getBindingVariablesCount());

		assertEquals(8, condition1.getBindingModel().getBindingVariablesCount());
		assertNotNull(condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept),
				condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED).getType());

		assertEquals(9, assignation1.getInferedBindingModel().getBindingVariablesCount());
		assertNull(assignation1.getInferedBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertNotNull(assignation1.getBindingModel().bindingVariableNamed(iteration1.getIteratorName()));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept),
				assignation1.getBindingModel().bindingVariableNamed(iteration1.getIteratorName()).getType());
		assertNotNull(assignation1.getInferedBindingModel().bindingVariableNamed("name"));
		assertEquals(String.class, assignation1.getInferedBindingModel().bindingVariableNamed("name").getType());

		System.out.println("FML: " + actionScheme.getFMLRepresentation());
	}

	@Test
	@TestOrder(6)
	public void testCreateSecondIteration() {

		// We add a second iteration with no control graph
		//
		// ActionScheme testFetchRequestIteration(String aString, Boolean
		// aBoolean) {
		// ... for (item in SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = parameters.aBoolean;
		// selected.aStringInA = parameters.aString)) {
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
		createIterationAction2.setEditionActionClass(IterationAction.class);

		// createSelectFetchRequestIterationAction.setRequestActionClass(SelectFlexoConceptInstance.class);
		createIterationAction2.doAction();
		iteration2 = (IterationAction) createIterationAction2.getNewEditionAction();

		SelectFlexoConceptInstance selectFlexoConceptInstance = iteration2.getFMLModelFactory().newSelectFlexoConceptInstance();
		selectFlexoConceptInstance.setFlexoConceptType(flexoConcept);
		selectFlexoConceptInstance.setReceiver(new DataBinding<VirtualModelInstance<?, ?>>("container"));
		iteration2.setIterationAction(selectFlexoConceptInstance);
		iteration2.setIteratorName("item2");

		FetchRequestCondition condition1 = selectFlexoConceptInstance.createCondition();
		condition1.setCondition(new DataBinding<Boolean>("selected.aBooleanInA = !(parameters.aBoolean)"));

		System.out.println("FML: " + actionScheme.getFMLRepresentation());

		assertEquals(7, iteration2.getBindingModel().getBindingVariablesCount());

		assertEquals(8, iteration2.getInferedBindingModel().getBindingVariablesCount());

		assertEquals(iteration2.getInferedBindingModel(), iteration2.getControlGraph().getBindingModel());

		assertNotNull(iteration2.getInferedBindingModel().bindingVariableNamed("item2"));

		assertEquals(8, condition1.getBindingModel().getBindingVariablesCount());
		assertNotNull(condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept),
				condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED).getType());

		System.out.println("FML: " + actionScheme.getFMLRepresentation());

	}

	@Test
	@TestOrder(7)
	public void moveControlGraph() {

		// We move the control graph from one iteration to another one
		//
		// ActionScheme testFetchRequestIteration(String aString, Boolean
		// aBoolean) {
		// ... for (item in SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = parameters.aBoolean;
		// selected.aStringInA = parameters.aString)) {
		// ....}
		// ....for (item2 in SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = !parameters.aBoolean)) {
		// .........name = item2.aStringInA;
		// .........item2.aStringInA = (name + "foo");
		// ....}
		// ...}
		// }

		System.out.println("FML WAS : " + actionScheme.getFMLRepresentation());

		System.out.println("Iteration1: " + iteration1.getFMLRepresentation());
		System.out.println("Iteration2: " + iteration2.getFMLRepresentation());

		FMLControlGraph cg = iteration1.getControlGraph();
		iteration1.setControlGraph(null);
		iteration2.setControlGraph(cg);

		System.out.println("CG is a " + cg.getClass());

		System.out.println("FML NOW : " + actionScheme.getFMLRepresentation());

		assertEquals(iteration2, cg.getOwner());

		System.out.println("Assignation1: " + assignation1 + " FML=" + assignation1.getFMLRepresentation());
		System.out.println("Assignation2: " + assignation2 + " FML=" + assignation2.getFMLRepresentation());

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
	@TestOrder(8)
	public void testViewPointIsValid() {

		assertVirtualModelIsValid(viewPoint);

	}

}
