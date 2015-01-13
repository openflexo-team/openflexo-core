package org.openflexo.fml.controller.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.fib.swing.FIBJPanel;
import org.openflexo.fib.testutils.GraphicalContextDelegate;
import org.openflexo.fib.utils.OpenflexoFIBTestCase;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CheckboxParameter;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.NavigationScheme;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.PrimitiveRole.PrimitiveType;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviourParameter;
import org.openflexo.foundation.fml.action.CreateFlexoRole;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test FlexoConceptPanel fib
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestVirtualModelView extends OpenflexoFIBTestCase {

	private static GraphicalContextDelegate gcDelegate;

	private static Resource fibResource;

	static ViewPoint viewPoint;
	static VirtualModel virtualModel;

	static FlexoConcept flexoConceptA;

	static CreationScheme creationScheme;
	static DeletionScheme deletionScheme;
	static NavigationScheme navigationScheme;
	static ActionScheme actionScheme;

	static FlexoEditor editor;

	@BeforeClass
	public static void setupClass() {
		instanciateTestServiceManager();
		initGUI();
	}

	@Test
	@TestOrder(1)
	public void testLoadWidget() {

		fibResource = ResourceLocator.locateResource("Fib/FML/VirtualModelView.fib");
		assertTrue(fibResource != null);
	}

	@Test
	@TestOrder(2)
	public void testValidateWidget() throws InterruptedException {

		validateFIB(fibResource);
	}

	@Test
	@TestOrder(3)
	public void loadConcepts() {

		ViewPointLibrary vpLib = serviceManager.getViewPointLibrary();
		assertNotNull(vpLib);
		viewPoint = vpLib.getViewPoint("http://openflexo.org/test/TestViewPoint1");
		assertNotNull(viewPoint);
		virtualModel = viewPoint.getVirtualModelNamed("TestVirtualModel");
		assertNotNull(virtualModel);

		flexoConceptA = virtualModel.getFlexoConcept("FlexoConceptA");
		System.out.println("flexoConcept=" + flexoConceptA);
		assertNotNull(flexoConceptA);

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		CreateFlexoRole createPR1 = CreateFlexoRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR1.setRoleName("aString");
		createPR1.setFlexoRoleClass(PrimitiveRole.class);
		createPR1.setPrimitiveType(PrimitiveType.String);
		createPR1.doAction();

		CreateFlexoRole createPR2 = CreateFlexoRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR2.setRoleName("aBoolean");
		createPR2.setFlexoRoleClass(PrimitiveRole.class);
		createPR2.setPrimitiveType(PrimitiveType.Boolean);
		createPR2.doAction();

		CreateFlexoRole createPR3 = CreateFlexoRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR3.setRoleName("anInteger");
		createPR3.setFlexoRoleClass(PrimitiveRole.class);
		createPR3.setPrimitiveType(PrimitiveType.Integer);
		createPR3.doAction();

		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme.doAction();
		creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();
		assertNotNull(creationScheme);

		CreateEditionAction createEditionAction1 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction1.setEditionActionClass(ExpressionAction.class);
		createEditionAction1.setAssignation(new DataBinding<Object>("aString"));
		createEditionAction1.doAction();
		AssignationAction<?> action1 = (AssignationAction<?>) createEditionAction1.getNewEditionAction();
		((ExpressionAction) action1.getAssignableAction()).setExpression(new DataBinding<Object>("'foo'"));
		action1.setName("action1");

		assertEquals(action1, creationScheme.getControlGraph());
		assertEquals(creationScheme.getBindingModel(), action1.getBindingModel().getBaseBindingModel());

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

		CreateFlexoBehaviour createActionScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createActionScheme.setFlexoBehaviourClass(ActionScheme.class);
		createActionScheme.doAction();
		actionScheme = (ActionScheme) createActionScheme.getNewFlexoBehaviour();
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

		CreateEditionAction createDeclarePatternRoleInCondition1 = CreateEditionAction.actionType.makeNewAction(
				conditional1.getThenControlGraph(), null, editor);
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

		CreateEditionAction createDeclarePatternRoleInCondition2 = CreateEditionAction.actionType.makeNewAction(conditional2, null, editor);
		// createDeclarePatternRoleInCondition2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclarePatternRoleInCondition2.setEditionActionClass(ExpressionAction.class);
		createDeclarePatternRoleInCondition2.setAssignation(new DataBinding<Object>("anInteger"));
		createDeclarePatternRoleInCondition2.doAction();
		AssignationAction<?> declarePatternRoleInCondition2 = (AssignationAction<?>) createDeclarePatternRoleInCondition2
				.getNewEditionAction();
		((ExpressionAction) declarePatternRoleInCondition2.getAssignableAction()).setExpression(new DataBinding<Object>("12"));

	}

	@Test
	@TestOrder(4)
	public void testInstanciateWidget() {

		FIBJPanel<VirtualModel> widget = instanciateFIB(fibResource, virtualModel, VirtualModel.class);

		gcDelegate.addTab("TestVirtualModel", widget.getController());
	}

	public static void initGUI() {
		gcDelegate = new GraphicalContextDelegate(TestVirtualModelView.class.getSimpleName());
	}

	@AfterClass
	public static void waitGUI() {
		gcDelegate.waitGUI();
	}

	@Before
	public void setUp() {
		gcDelegate.setUp();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		gcDelegate.tearDown();
	}

}
