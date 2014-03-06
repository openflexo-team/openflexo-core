package org.openflexo.fib.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.fib.testutils.GraphicalContextDelegate;
import org.openflexo.fib.utils.OpenflexoFIBTestCase;
import org.openflexo.fib.view.widget.DefaultFIBCustomComponent;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.viewpoint.ActionScheme;
import org.openflexo.foundation.viewpoint.CheckboxParameter;
import org.openflexo.foundation.viewpoint.CreationScheme;
import org.openflexo.foundation.viewpoint.DeletionScheme;
import org.openflexo.foundation.viewpoint.FlexoBehaviourParameter;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.NavigationScheme;
import org.openflexo.foundation.viewpoint.PrimitiveRole;
import org.openflexo.foundation.viewpoint.PrimitiveRole.PrimitiveType;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.ViewPointLibrary;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.action.CreateEditionAction;
import org.openflexo.foundation.viewpoint.action.CreateEditionAction.CreateEditionActionChoice;
import org.openflexo.foundation.viewpoint.action.CreateEditionScheme;
import org.openflexo.foundation.viewpoint.action.CreateEditionSchemeParameter;
import org.openflexo.foundation.viewpoint.action.CreateFlexoRole;
import org.openflexo.foundation.viewpoint.editionaction.AssignationAction;
import org.openflexo.foundation.viewpoint.editionaction.ConditionalAction;
import org.openflexo.foundation.viewpoint.editionaction.DeclarePatternRole;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.ResourceLocator;

/**
 * Test StandardFlexoConceptView fib
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestStandardFlexoConceptView extends OpenflexoFIBTestCase {

	private static GraphicalContextDelegate gcDelegate;

	private static File fibFile;

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

		fibFile = ResourceLocator.locateFile("Fib/VPM/StandardFlexoConceptView.fib");
		assertTrue(fibFile.exists());
	}

	@Test
	@TestOrder(2)
	public void testValidateWidget() {

		validateFIB(fibFile);
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
		System.out.println("flexoConceptA=" + flexoConceptA);
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

		CreateEditionScheme createCreationScheme = CreateEditionScheme.actionType.makeNewAction(flexoConceptA, null, editor);
		createCreationScheme.flexoBehaviourClass = CreationScheme.class;
		createCreationScheme.doAction();
		creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();

		CreateEditionAction createEditionAction1 = CreateEditionAction.actionType.makeNewAction(creationScheme, null, editor);
		createEditionAction1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction1.builtInActionClass = DeclarePatternRole.class;
		createEditionAction1.doAction();
		DeclarePatternRole action1 = (DeclarePatternRole) createEditionAction1.getNewEditionAction();
		action1.setAssignation(new DataBinding<Object>("aString"));
		action1.setObject(new DataBinding<Object>("'foo'"));

		CreateEditionAction createEditionAction2 = CreateEditionAction.actionType.makeNewAction(creationScheme, null, editor);
		createEditionAction2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction2.builtInActionClass = DeclarePatternRole.class;
		createEditionAction2.doAction();
		DeclarePatternRole action2 = (DeclarePatternRole) createEditionAction2.getNewEditionAction();
		action2.setAssignation(new DataBinding<Object>("aBoolean"));
		action2.setObject(new DataBinding<Object>("true"));

		CreateEditionAction createEditionAction3 = CreateEditionAction.actionType.makeNewAction(creationScheme, null, editor);
		createEditionAction3.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction3.builtInActionClass = AssignationAction.class;
		createEditionAction3.doAction();
		AssignationAction action3 = (AssignationAction) createEditionAction3.getNewEditionAction();
		action3.setAssignation(new DataBinding<Object>("anInteger"));
		action3.setValue(new DataBinding<Object>("8"));

		CreateEditionScheme createActionScheme = CreateEditionScheme.actionType.makeNewAction(flexoConceptA, null, editor);
		createActionScheme.flexoBehaviourClass = ActionScheme.class;
		createActionScheme.doAction();
		actionScheme = (ActionScheme) createActionScheme.getNewFlexoBehaviour();
		assertNotNull(actionScheme);

		CreateEditionSchemeParameter createParameter = CreateEditionSchemeParameter.actionType.makeNewAction(actionScheme, null, editor);
		createParameter.flexoBehaviourParameterClass = CheckboxParameter.class;
		createParameter.setParameterName("aFlag");
		createParameter.doAction();
		FlexoBehaviourParameter param = createParameter.getNewParameter();
		assertNotNull(param);
		assertTrue(actionScheme.getParameters().contains(param));

		CreateEditionAction createConditionAction1 = CreateEditionAction.actionType.makeNewAction(actionScheme, null, editor);
		createConditionAction1.actionChoice = CreateEditionActionChoice.ControlAction;
		createConditionAction1.controlActionClass = ConditionalAction.class;
		createConditionAction1.doAction();
		ConditionalAction conditional1 = (ConditionalAction) createConditionAction1.getNewEditionAction();
		conditional1.setCondition(new DataBinding<Boolean>("parameters.aFlag = true"));

		assertNotNull(conditional1);
		assertTrue(conditional1.getCondition().isValid());

		CreateEditionAction createDeclarePatternRoleInCondition1 = CreateEditionAction.actionType.makeNewAction(conditional1, null, editor);
		createDeclarePatternRoleInCondition1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclarePatternRoleInCondition1.builtInActionClass = DeclarePatternRole.class;
		createDeclarePatternRoleInCondition1.doAction();
		DeclarePatternRole declarePatternRoleInCondition1 = (DeclarePatternRole) createDeclarePatternRoleInCondition1.getNewEditionAction();
		declarePatternRoleInCondition1.setAssignation(new DataBinding<Object>("anInteger"));
		declarePatternRoleInCondition1.setObject(new DataBinding<Object>("8"));

		CreateEditionAction createConditionAction2 = CreateEditionAction.actionType.makeNewAction(actionScheme, null, editor);
		createConditionAction2.actionChoice = CreateEditionActionChoice.ControlAction;
		createConditionAction2.controlActionClass = ConditionalAction.class;
		createConditionAction2.doAction();
		ConditionalAction conditional2 = (ConditionalAction) createConditionAction2.getNewEditionAction();
		conditional2.setCondition(new DataBinding<Boolean>("parameters.aFlag = false"));

		assertNotNull(conditional2);
		assertTrue(conditional2.getCondition().isValid());

		CreateEditionAction createDeclarePatternRoleInCondition2 = CreateEditionAction.actionType.makeNewAction(conditional2, null, editor);
		createDeclarePatternRoleInCondition2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclarePatternRoleInCondition2.builtInActionClass = DeclarePatternRole.class;
		createDeclarePatternRoleInCondition2.doAction();
		DeclarePatternRole declarePatternRoleInCondition2 = (DeclarePatternRole) createDeclarePatternRoleInCondition2.getNewEditionAction();
		declarePatternRoleInCondition2.setAssignation(new DataBinding<Object>("anInteger"));
		declarePatternRoleInCondition2.setObject(new DataBinding<Object>("12"));

		assertEquals(2, actionScheme.getActions().size());

	}

	@Test
	@TestOrder(4)
	public void testInstanciateWidgetForConceptA() {

		DefaultFIBCustomComponent<FlexoConcept> widget = instanciateFIB(fibFile, flexoConceptA, FlexoConcept.class);

		gcDelegate.addTab("FlexoConceptA", widget.getController());
	}

	@Test
	@TestOrder(5)
	public void testInstanciateWidgetForConceptB() {

		DefaultFIBCustomComponent<FlexoConcept> widget = instanciateFIB(fibFile, flexoConceptB, FlexoConcept.class);

		gcDelegate.addTab("FlexoConceptB", widget.getController());
	}

	@Test
	@TestOrder(6)
	public void testInstanciateWidgetForConceptC() {

		DefaultFIBCustomComponent<FlexoConcept> widget = instanciateFIB(fibFile, flexoConceptC, FlexoConcept.class);

		gcDelegate.addTab("FlexoConceptC", widget.getController());
	}

	@Test
	@TestOrder(7)
	public void testInstanciateWidgetForConceptD() {

		DefaultFIBCustomComponent<FlexoConcept> widget = instanciateFIB(fibFile, flexoConceptD, FlexoConcept.class);

		gcDelegate.addTab("FlexoConceptD", widget.getController());
	}

	@Test
	@TestOrder(8)
	public void testInstanciateWidgetForConceptE() {

		DefaultFIBCustomComponent<FlexoConcept> widget = instanciateFIB(fibFile, flexoConceptE, FlexoConcept.class);

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
