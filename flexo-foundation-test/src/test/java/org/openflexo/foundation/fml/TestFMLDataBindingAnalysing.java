/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Type;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateGenericBehaviourParameter;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test {@link DataBinding} analysis in the context of FML {@link VirtualModel} on which we perform changes
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestFMLDataBindingAnalysing extends OpenflexoTestCase {

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestViewPoint";
	public static final String VIRTUAL_MODEL_NAME = "TestVirtualModel";

	private static DirectoryResourceCenter resourceCenter;

	static FlexoEditor editor;
	static VirtualModel topVirtualModel;
	static VirtualModel virtualModel;

	static FlexoConcept flexoConceptA;
	static FlexoConcept flexoConceptB;

	private static PrimitiveRole<String> stringProperty1;
	private static PrimitiveRole<String> stringProperty2;
	private static PrimitiveRole<Boolean> booleanProperty2;
	private static PrimitiveRole<Integer> intProperty2;
	private static CreationScheme creationScheme;
	private static ActionScheme actionScheme;
	private static ActionScheme actionScheme2;

	public void genericTest(Bindable bindable, String bindingPath, boolean expectedValidity, Type expectedType) {

		DataBinding<?> db = makeBinding(bindable, bindingPath, expectedValidity, expectedType);
		db.delete();
	}

	public DataBinding<?> makeBinding(Bindable bindable, String bindingPath, boolean expectedValidity, Type expectedType) {

		System.out.println("Evaluate " + bindingPath);

		DataBinding<?> dataBinding = new DataBinding<>(bindingPath, bindable, expectedType, DataBinding.BindingDefinitionType.GET);

		if (dataBinding.getExpression() != null) {
			System.out.println(
					"Parsed " + dataBinding + " as " + dataBinding.getExpression() + " of " + dataBinding.getExpression().getClass());

			if (expectedValidity && !dataBinding.isValid()) {
				fail("Binding is not valid: " + dataBinding + " reason: " + dataBinding.invalidBindingReason());
			}

			assertEquals(expectedValidity, dataBinding.isValid());

			if (dataBinding.isValid()) {
				assertEquals(expectedType, dataBinding.getAnalyzedType());
			}

			return dataBinding;

		}
		System.out.println("Could not Parse " + dataBinding + " defined as " + dataBinding);
		fail("Unparseable binding");
		return null;
	}

	/**
	 * Test the VP creation
	 * 
	 * @throws ModelDefinitionException
	 * @throws SaveResourceException
	 * @throws IOException
	 */
	@Test
	@TestOrder(1)
	public void testCreateViewPoint() throws SaveResourceException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		resourceCenter = makeNewDirectoryResourceCenter();
		assertNotNull(resourceCenter);
		System.out.println("ResourceCenter= " + resourceCenter);

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		CompilationUnitResourceFactory factory = fmlTechnologyAdapter.getCompilationUnitResourceFactory();

		CompilationUnitResource newVirtualModelResource = factory.makeTopLevelCompilationUnitResource(VIEWPOINT_NAME, VIEWPOINT_URI,
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(), true);
		topVirtualModel = newVirtualModelResource.getLoadedResourceData().getVirtualModel();

		assertTrue(topVirtualModel.getResource().getDirectory() != null);
		assertTrue(topVirtualModel.getResource().getIODelegate().exists());

		assertEquals(topVirtualModel, topVirtualModel.getDeclaringCompilationUnit().getVirtualModel());
		assertEquals(null, topVirtualModel.getContainerVirtualModel());
		assertEquals(topVirtualModel, topVirtualModel.getFlexoConcept());
		assertEquals(topVirtualModel, topVirtualModel.getResourceData().getVirtualModel());
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
		CompilationUnitResourceFactory factory = fmlTechnologyAdapter.getCompilationUnitResourceFactory();
		CompilationUnitResource newVMResource = factory.makeContainedCompilationUnitResource(VIRTUAL_MODEL_NAME,
				topVirtualModel.getResource(), true);
		virtualModel = newVMResource.getLoadedResourceData().getVirtualModel();

		assertTrue(ResourceLocator.retrieveResourceAsFile(virtualModel.getResource().getDirectory()).exists());
		assertTrue(virtualModel.getResource().getIODelegate().exists());

		assertSame(topVirtualModel, virtualModel.getContainerVirtualModel());

		assertEquals(virtualModel, virtualModel.getFlexoConcept());

		CreatePrimitiveRole createPR1 = CreatePrimitiveRole.actionType.makeNewAction(virtualModel, null, editor);
		createPR1.setRoleName("aStringInVirtualModel");
		createPR1.setPrimitiveType(PrimitiveType.String);
		createPR1.doAction();
		stringProperty1 = (PrimitiveRole<String>) createPR1.getNewFlexoRole();
		assertNotNull(stringProperty1);

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

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptA");

		addEP.doAction();

		flexoConceptA = addEP.getNewFlexoConcept();

		assertNotNull(flexoConceptA);

		assertEquals(topVirtualModel, flexoConceptA.getOwner().getContainerVirtualModel());
		assertEquals(virtualModel, flexoConceptA.getOwner());
		assertEquals(virtualModel, flexoConceptA.getOwningVirtualModel());
		assertEquals(flexoConceptA, flexoConceptA.getFlexoConcept());
		assertEquals(virtualModel, flexoConceptA.getResourceData().getVirtualModel());

		virtualModel.getResource().save();

		// System.out.println("Saved: " + ((VirtualModelResource)
		// virtualModel.getResource()).getFile());
		System.out.println("Saved: " + virtualModel.getResource().getIODelegate().toString());

	}

	/**
	 * Test the FlexoConcept creation
	 */
	@Test
	@TestOrder(5)
	public void testCreateFlexoConceptB() throws SaveResourceException {

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptB");
		addEP.doAction();

		flexoConceptB = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept B = " + flexoConceptB);
		assertNotNull(flexoConceptB);

		virtualModel.getResource().save();

		System.out.println("Saved: " + virtualModel.getResource().getIODelegate().toString());

	}

	@Test
	@TestOrder(9)
	public void testCreateSomePropertiesToConceptA() {

		CreatePrimitiveRole createPR1 = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR1.setRoleName("aString");
		createPR1.setPrimitiveType(PrimitiveType.String);
		createPR1.doAction();
		stringProperty2 = (PrimitiveRole<String>) createPR1.getNewFlexoRole();
		assertNotNull(stringProperty2);

		CreatePrimitiveRole createPR2 = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR2.setRoleName("aBoolean");
		createPR2.setPrimitiveType(PrimitiveType.Boolean);
		createPR2.doAction();
		booleanProperty2 = (PrimitiveRole<Boolean>) createPR2.getNewFlexoRole();
		assertNotNull(booleanProperty2);

		CreatePrimitiveRole createPR3 = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR3.setRoleName("anInteger");
		createPR3.setPrimitiveType(PrimitiveType.Integer);
		createPR3.doAction();
		intProperty2 = (PrimitiveRole<Integer>) createPR3.getNewFlexoRole();
		assertNotNull(intProperty2);

		assertEquals(3, flexoConceptA.getFlexoProperties().size());
		assertTrue(flexoConceptA.getFlexoProperties().contains(stringProperty2));
		assertTrue(flexoConceptA.getFlexoProperties().contains(booleanProperty2));
		assertTrue(flexoConceptA.getFlexoProperties().contains(intProperty2));

		assertEquals(virtualModel, stringProperty2.getOwningVirtualModel());
		assertEquals(flexoConceptA, stringProperty2.getFlexoConcept());
		assertEquals(virtualModel, stringProperty2.getResourceData().getVirtualModel());

		assertEquals(virtualModel, booleanProperty2.getOwningVirtualModel());
		assertEquals(flexoConceptA, booleanProperty2.getFlexoConcept());
		assertEquals(virtualModel, booleanProperty2.getResourceData().getVirtualModel());

		assertEquals(virtualModel, intProperty2.getOwningVirtualModel());
		assertEquals(flexoConceptA, intProperty2.getFlexoConcept());
		assertEquals(virtualModel, intProperty2.getResourceData().getVirtualModel());

	}

	@Test
	@TestOrder(10)
	public void testCreateACreationSchemeInConceptA() throws InvalidNameException {

		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme.doAction();
		creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();

		CreateEditionAction createEditionAction1 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction1.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createEditionAction1.setEditionActionClass(ExpressionAction.class);
		createEditionAction1.setAssignation(new DataBinding<>("aString"));
		createEditionAction1.doAction();
		AssignationAction<?> action1 = (AssignationAction<?>) createEditionAction1.getNewEditionAction();
		((ExpressionAction<?>) action1.getAssignableAction()).setExpression(new DataBinding<>("'foo'"));
		action1.setName("action1");

		CreateEditionAction createEditionAction2 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction2.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createEditionAction2.setEditionActionClass(ExpressionAction.class);
		createEditionAction2.setAssignation(new DataBinding<>("aBoolean"));
		createEditionAction2.doAction();
		AssignationAction<?> action2 = (AssignationAction<?>) createEditionAction2.getNewEditionAction();
		((ExpressionAction<?>) action2.getAssignableAction()).setExpression(new DataBinding<>("true"));
		action2.setName("action2");

		CreateEditionAction createEditionAction3 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction3.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createEditionAction3.setEditionActionClass(ExpressionAction.class);
		createEditionAction3.setAssignation(new DataBinding<>("anInteger"));
		createEditionAction3.doAction();
		AssignationAction<?> action3 = (AssignationAction<?>) createEditionAction3.getNewEditionAction();
		((ExpressionAction<?>) action3.getAssignableAction()).setExpression(new DataBinding<>("8"));
		action3.setName("action3");

		assertTrue(flexoConceptA.getFlexoBehaviours().contains(creationScheme));
		assertTrue(flexoConceptA.getCreationSchemes().contains(creationScheme));

		assertEquals(virtualModel, creationScheme.getOwningVirtualModel());
		assertEquals(flexoConceptA, creationScheme.getFlexoConcept());

		assertEquals(virtualModel, action1.getOwningVirtualModel());
		assertEquals(flexoConceptA, action1.getFlexoConcept());

	}

	@Test
	@TestOrder(11)
	public void testCreateAnActionSchemeInConceptA() {

		CreateFlexoBehaviour createActionScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createActionScheme.setFlexoBehaviourClass(ActionScheme.class);
		createActionScheme.doAction();
		actionScheme = (ActionScheme) createActionScheme.getNewFlexoBehaviour();
		assertNotNull(actionScheme);

		CreateGenericBehaviourParameter createParameter = CreateGenericBehaviourParameter.actionType.makeNewAction(actionScheme, null,
				editor);
		createParameter.setParameterName("aFlag");
		createParameter.setParameterType(Boolean.class);
		createParameter.doAction();
		FlexoBehaviourParameter param = createParameter.getNewParameter();
		assertNotNull(param);
		assertTrue(actionScheme.getParameters().contains(param));

		CreateEditionAction createConditionAction1 = CreateEditionAction.actionType.makeNewAction(actionScheme.getControlGraph(), null,
				editor);
		// createConditionAction1.actionChoice =
		// CreateEditionActionChoice.ControlAction;
		createConditionAction1.setEditionActionClass(ConditionalAction.class);
		createConditionAction1.doAction();
		ConditionalAction conditional1 = (ConditionalAction) createConditionAction1.getNewEditionAction();
		conditional1.setCondition(new DataBinding<Boolean>("parameters.aFlag = true"));

		assertNotNull(conditional1);
		assertTrue(conditional1.getCondition().isValid());

		CreateEditionAction createDeclareFlexoRoleInCondition1 = CreateEditionAction.actionType
				.makeNewAction(conditional1.getThenControlGraph(), null, editor);
		// createDeclareFlexoRoleInCondition1.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createDeclareFlexoRoleInCondition1.setEditionActionClass(ExpressionAction.class);
		createDeclareFlexoRoleInCondition1.setAssignation(new DataBinding<>("anInteger"));
		createDeclareFlexoRoleInCondition1.doAction();
		AssignationAction<?> declareFlexoRoleInCondition1 = (AssignationAction<?>) createDeclareFlexoRoleInCondition1.getNewEditionAction();
		((ExpressionAction<?>) declareFlexoRoleInCondition1.getAssignableAction()).setExpression(new DataBinding<>("8"));

		CreateEditionAction createConditionAction2 = CreateEditionAction.actionType.makeNewAction(actionScheme.getControlGraph(), null,
				editor);
		// createConditionAction2.actionChoice =
		// CreateEditionActionChoice.ControlAction;
		createConditionAction2.setEditionActionClass(ConditionalAction.class);
		createConditionAction2.doAction();
		ConditionalAction conditional2 = (ConditionalAction) createConditionAction2.getNewEditionAction();
		conditional2.setCondition(new DataBinding<Boolean>("parameters.aFlag = false"));

		assertNotNull(conditional2);
		assertTrue(conditional2.getCondition().isValid());

		CreateEditionAction createDeclareFlexoRoleInCondition2 = CreateEditionAction.actionType
				.makeNewAction(conditional2.getThenControlGraph(), null, editor);
		// createDeclareFlexoRoleInCondition2.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createDeclareFlexoRoleInCondition2.setEditionActionClass(ExpressionAction.class);
		createDeclareFlexoRoleInCondition2.setAssignation(new DataBinding<>("anInteger"));
		createDeclareFlexoRoleInCondition2.doAction();
		AssignationAction<?> declareFlexoRoleInCondition2 = (AssignationAction<?>) createDeclareFlexoRoleInCondition2.getNewEditionAction();
		((ExpressionAction<?>) declareFlexoRoleInCondition2.getAssignableAction()).setExpression(new DataBinding<>("12"));

	}

	@Test
	@TestOrder(12)
	public void testCreateAnotherActionSchemeInConceptA() {

		CreateFlexoBehaviour createActionScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createActionScheme.setFlexoBehaviourClass(ActionScheme.class);
		createActionScheme.doAction();
		actionScheme2 = (ActionScheme) createActionScheme.getNewFlexoBehaviour();
		assertNotNull(actionScheme2);

		CreateGenericBehaviourParameter createParameter = CreateGenericBehaviourParameter.actionType.makeNewAction(actionScheme2, null,
				editor);
		createParameter.setParameterName("anotherFlag");
		createParameter.setParameterType(Boolean.class);
		createParameter.doAction();
		FlexoBehaviourParameter param = createParameter.getNewParameter();
		assertNotNull(param);
		assertTrue(actionScheme2.getParameters().contains(param));

	}

	@Test
	@TestOrder(20)
	public void testTrivialCase() {

		// System.out.println(virtualModel.getFMLPrettyPrint());

		System.out.println("*********** testTrivialCase");

		genericTest(virtualModel, "aString", false, null);

		genericTest(flexoConceptA, "aString", true, String.class);

		genericTest(virtualModel, "aStringInVirtualModel", true, String.class);

		genericTest(flexoConceptA, "aStringInVirtualModel", true, String.class);

		genericTest(flexoConceptA, "aString+aStringInVirtualModel", true, String.class);

		genericTest(flexoConceptA, "aString+aStringInVirtualModel.substring(1,5)", true, String.class);

		genericTest(creationScheme, "aString+aStringInVirtualModel", true, String.class);

		genericTest(actionScheme, "aString+aStringInVirtualModel+parameters.aFlag", true, String.class);

		genericTest(creationScheme, "aString+aStringInVirtualModel+parameters.aFlag", false, null);
	}

	@Test
	@TestOrder(21)
	public void testChangePropertyName() throws InvalidNameException {

		// System.out.println(virtualModel.getFMLPrettyPrint());

		System.out.println("*********** testChangePropertyName");

		DataBinding<?> db = makeBinding(virtualModel, "aStringInVirtualModel", true, String.class);

		assertTrue(db.isValid());

		stringProperty1.setName("nameHasChanged");

		assertEquals("nameHasChanged", db.toString());
		assertTrue(db.isValid());

		stringProperty1.setName("aStringInVirtualModel");

		assertEquals("aStringInVirtualModel", db.toString());
		assertTrue(db.isValid());

	}

	@Test
	@TestOrder(22)
	public void testChangePropertyName2() throws InvalidNameException {

		// System.out.println(virtualModel.getFMLPrettyPrint());

		System.out.println("*********** testChangePropertyName2");

		DataBinding<?> db = makeBinding(flexoConceptA, "aStringInVirtualModel+aString", true, String.class);

		assertTrue(db.isValid());

		stringProperty1.setName("nameHasChanged");

		System.out.println("Et maintenant le db vaut: " + db);

		assertEquals("nameHasChanged + aString", db.toString());
		assertTrue(db.isValid());

		stringProperty1.setName("aStringInVirtualModel");

		assertEquals("aStringInVirtualModel + aString", db.toString());
		assertTrue(db.isValid());

	}

	@Test
	@TestOrder(23)
	public void testChangeParameterName() throws InvalidNameException {

		// System.out.println(virtualModel.getFMLPrettyPrint());

		System.out.println("*********** testChangeParameterName");

		DataBinding<?> db = makeBinding(actionScheme, "parameters.aFlag", true, Boolean.TYPE);

		/*BindingValue bv = (BindingValue) db.getExpression();
		System.out.println("variable: " + bv.getBindingVariable());
		for (BindingPathElement bindingPathElement : bv.getBindingPath()) {
			System.out.println(" > " + bindingPathElement + " activated=" + bindingPathElement.isActivated());
		}*/

		assertTrue(db.isValid());

		actionScheme.getParameters().get(0).setName("aRenamedParameter");

		assertEquals("parameters.aRenamedParameter", db.toString());
		assertTrue(db.isValid());

		actionScheme.getParameters().get(0).setName("aFlag");

		assertEquals("parameters.aFlag", db.toString());
		assertTrue(db.isValid());

	}

	@Test
	@TestOrder(24)
	public void testChangeParameterName2() throws InvalidNameException {

		// System.out.println(virtualModel.getFMLPrettyPrint());

		System.out.println("*********** testChangeParameterName2");

		DataBinding<?> db = makeBinding(actionScheme, "aStringInVirtualModel+aString+parameters.aFlag", true, String.class);

		assertTrue(db.isValid());

		actionScheme.getParameters().get(0).setName("aRenamedParameter");

		assertEquals("aStringInVirtualModel + aString + parameters.aRenamedParameter", db.toString());
		assertTrue(db.isValid());

		actionScheme.getParameters().get(0).setName("aFlag");
		assertEquals("aStringInVirtualModel + aString + parameters.aFlag", db.toString());
		assertTrue(db.isValid());

		db.delete();

	}

	@Test
	@TestOrder(25)
	public void testChangeBehaviourName() throws InvalidNameException {

		// System.out.println(virtualModel.getFMLPrettyPrint());

		System.out.println("*********** testChangeBehaviourName");

		DataBinding<?> db = makeBinding(actionScheme, "this.doSomething2(false)", true, Void.TYPE);

		assertTrue(db.isValid());

		actionScheme2.setName("actionWasRenamed");

		assertEquals("this.actionWasRenamed(false)", db.toString());
		assertTrue(db.isValid());

		actionScheme2.setName("action2");
		assertEquals("this.action2(false)", db.toString());
		assertTrue(db.isValid());

		db.delete();
	}

	@Test
	@TestOrder(26)
	public void testChangeBehaviourAndParametersName() throws InvalidNameException {

		// System.out.println(virtualModel.getFMLPrettyPrint());

		System.out.println("*********** testChangeBehaviourAndParametersName");

		DataBinding<?> db = makeBinding(actionScheme, "this.action2(parameters.aFlag)", true, Void.TYPE);

		// Rename ActionScheme, check that DataBinding was accordingly modified
		actionScheme2.setName("actionWasRenamed");
		assertEquals("this.actionWasRenamed(parameters.aFlag)", db.toString());
		assertTrue(db.isValid());

		// Rename parameter, check that DataBinding was accordingly modified
		actionScheme.getParameters().get(0).setName("aRenamedParameter");
		assertEquals("this.actionWasRenamed(parameters.aRenamedParameter)", db.toString());
		assertTrue(db.isValid());

		// Revert to old values
		actionScheme.getParameters().get(0).setName("aFlag");
		assertTrue(db.isValid());
		assertEquals("this.actionWasRenamed(parameters.aFlag)", db.toString());

		actionScheme2.setName("action2");
		assertTrue(db.isValid());
		assertEquals("this.action2(parameters.aFlag)", db.toString());

		db.delete();

	}
}
