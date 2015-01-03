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
import org.openflexo.foundation.fml.action.CreateEditionAction.CreateEditionActionChoice;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviourParameter;
import org.openflexo.foundation.fml.action.CreateFlexoRole;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ConditionalAction;
import org.openflexo.foundation.fml.editionaction.DeclareFlexoRole;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test StandardFlexoConceptView fib
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestStandardFlexoConceptView extends OpenflexoFIBTestCase {

	private static GraphicalContextDelegate gcDelegate;

	private static Resource fibResource;

	static FlexoEditor editor;

	static FlexoConcept flexoConceptA;
	static FlexoConcept flexoConceptB;
	static FlexoConcept flexoConceptC;
	static FlexoConcept flexoConceptD;
	static FlexoConcept flexoConceptE;

	static CreationScheme creationScheme;
	static DeletionScheme deletionScheme;
	static NavigationScheme navigationScheme;
	static ActionScheme actionScheme;

	@BeforeClass
	public static void setupClass() {
		instanciateTestServiceManager();
		initGUI();
	}

	@Test
	@TestOrder(1)
	public void testLoadWidget() {

		fibResource = ResourceLocator.locateResource("Fib/FML/StandardFlexoConceptView.fib");
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
		ViewPoint viewPoint = vpLib.getViewPoint("http://openflexo.org/test/TestViewPoint1");
		assertNotNull(viewPoint);
		VirtualModel virtualModel = viewPoint.getVirtualModelNamed("TestVirtualModel");
		assertNotNull(virtualModel);

		flexoConceptA = virtualModel.getFlexoConcept("FlexoConceptA");
		System.out.println("flexoConcept=" + flexoConceptA);
		assertNotNull(flexoConceptA);

		flexoConceptB = virtualModel.getFlexoConcept("FlexoConceptB");
		System.out.println("flexoConceptB=" + flexoConceptB);
		assertNotNull(flexoConceptB);

		flexoConceptC = virtualModel.getFlexoConcept("FlexoConceptC");
		System.out.println("flexoConceptC=" + flexoConceptC);
		assertNotNull(flexoConceptC);

		flexoConceptD = virtualModel.getFlexoConcept("FlexoConceptD");
		System.out.println("flexoConceptD=" + flexoConceptD);
		assertNotNull(flexoConceptD);

		flexoConceptE = virtualModel.getFlexoConcept("FlexoConceptE");
		System.out.println("flexoConceptE=" + flexoConceptE);
		assertNotNull(flexoConceptE);

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

		CreateEditionAction createEditionAction1 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		createEditionAction1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction1.setBuiltInActionClass(DeclareFlexoRole.class);
		createEditionAction1.doAction();
		DeclareFlexoRole action1 = (DeclareFlexoRole) createEditionAction1.getNewEditionAction();
		action1.setAssignation(new DataBinding<Object>("aString"));
		action1.setObject(new DataBinding<Object>("'foo'"));

		CreateEditionAction createEditionAction2 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		createEditionAction2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction2.setBuiltInActionClass(DeclareFlexoRole.class);
		createEditionAction2.doAction();
		DeclareFlexoRole action2 = (DeclareFlexoRole) createEditionAction2.getNewEditionAction();
		action2.setAssignation(new DataBinding<Object>("aBoolean"));
		action2.setObject(new DataBinding<Object>("true"));

		CreateEditionAction createEditionAction3 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		createEditionAction3.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction3.setBuiltInActionClass(AssignationAction.class);
		createEditionAction3.doAction();
		AssignationAction action3 = (AssignationAction) createEditionAction3.getNewEditionAction();
		action3.setAssignation(new DataBinding<Object>("anInteger"));
		action3.setValue(new DataBinding<Object>("8"));

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
		createConditionAction1.actionChoice = CreateEditionActionChoice.ControlAction;
		createConditionAction1.setControlActionClass(ConditionalAction.class);
		createConditionAction1.doAction();
		ConditionalAction conditional1 = (ConditionalAction) createConditionAction1.getNewEditionAction();
		conditional1.setCondition(new DataBinding<Boolean>("parameters.aFlag = true"));

		assertNotNull(conditional1);
		assertTrue(conditional1.getCondition().isValid());

		CreateEditionAction createDeclarePatternRoleInCondition1 = CreateEditionAction.actionType.makeNewAction(conditional1, null, editor);
		createDeclarePatternRoleInCondition1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclarePatternRoleInCondition1.setBuiltInActionClass(DeclareFlexoRole.class);
		createDeclarePatternRoleInCondition1.doAction();
		DeclareFlexoRole declarePatternRoleInCondition1 = (DeclareFlexoRole) createDeclarePatternRoleInCondition1.getNewEditionAction();
		declarePatternRoleInCondition1.setAssignation(new DataBinding<Object>("anInteger"));
		declarePatternRoleInCondition1.setObject(new DataBinding<Object>("8"));

		CreateEditionAction createConditionAction2 = CreateEditionAction.actionType.makeNewAction(actionScheme.getControlGraph(), null,
				editor);
		createConditionAction2.actionChoice = CreateEditionActionChoice.ControlAction;
		createConditionAction2.setControlActionClass(ConditionalAction.class);
		createConditionAction2.doAction();
		ConditionalAction conditional2 = (ConditionalAction) createConditionAction2.getNewEditionAction();
		conditional2.setCondition(new DataBinding<Boolean>("parameters.aFlag = false"));

		assertNotNull(conditional2);
		assertTrue(conditional2.getCondition().isValid());

		CreateEditionAction createDeclarePatternRoleInCondition2 = CreateEditionAction.actionType.makeNewAction(conditional2, null, editor);
		createDeclarePatternRoleInCondition2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclarePatternRoleInCondition2.setBuiltInActionClass(DeclareFlexoRole.class);
		createDeclarePatternRoleInCondition2.doAction();
		DeclareFlexoRole declarePatternRoleInCondition2 = (DeclareFlexoRole) createDeclarePatternRoleInCondition2.getNewEditionAction();
		declarePatternRoleInCondition2.setAssignation(new DataBinding<Object>("anInteger"));
		declarePatternRoleInCondition2.setObject(new DataBinding<Object>("12"));

		assertEquals(2, actionScheme.getActions().size());

	}

	@Test
	@TestOrder(4)
	public void testInstanciateWidgetForConceptA() {

		FIBJPanel<FlexoConcept> widget = instanciateFIB(fibResource, flexoConceptA, FlexoConcept.class);

		gcDelegate.addTab("FlexoConceptA", widget.getController());
	}

	@Test
	@TestOrder(5)
	public void testInstanciateWidgetForConceptB() {

		FIBJPanel<FlexoConcept> widget = instanciateFIB(fibResource, flexoConceptB, FlexoConcept.class);

		gcDelegate.addTab("FlexoConceptB", widget.getController());
	}

	@Test
	@TestOrder(6)
	public void testInstanciateWidgetForConceptC() {

		FIBJPanel<FlexoConcept> widget = instanciateFIB(fibResource, flexoConceptC, FlexoConcept.class);

		gcDelegate.addTab("FlexoConceptC", widget.getController());
	}

	@Test
	@TestOrder(7)
	public void testInstanciateWidgetForConceptD() {

		FIBJPanel<FlexoConcept> widget = instanciateFIB(fibResource, flexoConceptD, FlexoConcept.class);

		gcDelegate.addTab("FlexoConceptD", widget.getController());
	}

	@Test
	@TestOrder(8)
	public void testInstanciateWidgetForConceptE() {

		FIBJPanel<FlexoConcept> widget = instanciateFIB(fibResource, flexoConceptE, FlexoConcept.class);

		gcDelegate.addTab("FlexoConceptE", widget.getController());
	}

	public static void initGUI() {
		gcDelegate = new GraphicalContextDelegate(TestStandardFlexoConceptView.class.getSimpleName());
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
