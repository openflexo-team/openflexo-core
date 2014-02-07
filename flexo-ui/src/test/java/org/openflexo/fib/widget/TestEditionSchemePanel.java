package org.openflexo.fib.widget;

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
import org.openflexo.fib.testutils.GraphicalContextDelegate;
import org.openflexo.fib.utils.OpenflexoFIBTestCase;
import org.openflexo.fib.view.widget.DefaultFIBCustomComponent;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.viewpoint.ActionScheme;
import org.openflexo.foundation.viewpoint.CheckboxParameter;
import org.openflexo.foundation.viewpoint.CreationScheme;
import org.openflexo.foundation.viewpoint.DeletionScheme;
import org.openflexo.foundation.viewpoint.EditionPattern;
import org.openflexo.foundation.viewpoint.EditionScheme;
import org.openflexo.foundation.viewpoint.EditionSchemeParameter;
import org.openflexo.foundation.viewpoint.NavigationScheme;
import org.openflexo.foundation.viewpoint.PrimitivePatternRole;
import org.openflexo.foundation.viewpoint.PrimitivePatternRole.PrimitiveType;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.ViewPointLibrary;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.action.CreateEditionAction;
import org.openflexo.foundation.viewpoint.action.CreateEditionAction.CreateEditionActionChoice;
import org.openflexo.foundation.viewpoint.action.CreateEditionScheme;
import org.openflexo.foundation.viewpoint.action.CreateEditionSchemeParameter;
import org.openflexo.foundation.viewpoint.action.CreatePatternRole;
import org.openflexo.foundation.viewpoint.editionaction.AssignationAction;
import org.openflexo.foundation.viewpoint.editionaction.ConditionalAction;
import org.openflexo.foundation.viewpoint.editionaction.DeclarePatternRole;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.FileResource;

/**
 * Test EditionPatternPanel fib
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestEditionSchemePanel extends OpenflexoFIBTestCase {

	private static GraphicalContextDelegate gcDelegate;

	private static FileResource fibFile;

	static EditionPattern flexoConceptA;

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

		fibFile = new FileResource("Fib/VPM/EditionSchemePanel.fib");
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

		flexoConceptA = virtualModel.getEditionPattern("FlexoConceptA");
		System.out.println("flexoConceptA=" + flexoConceptA);
		assertNotNull(flexoConceptA);

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		CreatePatternRole createPR1 = CreatePatternRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR1.setPatternRoleName("aString");
		createPR1.patternRoleClass = PrimitivePatternRole.class;
		createPR1.primitiveType = PrimitiveType.String;
		createPR1.doAction();

		CreatePatternRole createPR2 = CreatePatternRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR2.setPatternRoleName("aBoolean");
		createPR2.patternRoleClass = PrimitivePatternRole.class;
		createPR2.primitiveType = PrimitiveType.Boolean;
		createPR2.doAction();

		CreatePatternRole createPR3 = CreatePatternRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR3.setPatternRoleName("anInteger");
		createPR3.patternRoleClass = PrimitivePatternRole.class;
		createPR3.primitiveType = PrimitiveType.Integer;
		createPR3.doAction();

		CreateEditionScheme createCreationScheme = CreateEditionScheme.actionType.makeNewAction(flexoConceptA, null, editor);
		createCreationScheme.editionSchemeClass = CreationScheme.class;
		createCreationScheme.doAction();
		creationScheme = (CreationScheme) createCreationScheme.getNewEditionScheme();

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
		createActionScheme.editionSchemeClass = ActionScheme.class;
		createActionScheme.doAction();
		actionScheme = (ActionScheme) createActionScheme.getNewEditionScheme();
		assertNotNull(actionScheme);

		CreateEditionSchemeParameter createParameter = CreateEditionSchemeParameter.actionType.makeNewAction(actionScheme, null, editor);
		createParameter.editionSchemeParameterClass = CheckboxParameter.class;
		createParameter.setParameterName("aFlag");
		createParameter.doAction();
		EditionSchemeParameter param = createParameter.getNewParameter();
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
	public void testInstanciateWidgetForCreationScheme() {

		DefaultFIBCustomComponent<EditionScheme> widget = instanciateFIB(fibFile, creationScheme, EditionScheme.class);

		gcDelegate.addTab("CreationScheme", widget.getController());
	}

	@Test
	@TestOrder(5)
	public void testInstanciateWidgetForActionScheme() {

		DefaultFIBCustomComponent<EditionScheme> widget = instanciateFIB(fibFile, actionScheme, EditionScheme.class);

		gcDelegate.addTab("ActionScheme", widget.getController());
	}

	/*@Test
	@TestOrder(5)
	public void testInstanciateWidgetForDeletionScheme() {

		DefaultFIBCustomComponent<EditionScheme> widget = instanciateFIB(fibFile, flexoConceptA.getDefaultDeletionScheme(),
				EditionScheme.class);

		gcDelegate.addTab("DeletionScheme", widget.getController());
	}*/

	@Test
	@TestOrder(10)
	public void testInstanciateWidgetForDeclarePatternRole() {

		DefaultFIBCustomComponent<DeclarePatternRole> widget = instanciateFIB(new FileResource("FIB/VPM/DeclarePatternRolePanel.fib"),
				(DeclarePatternRole) creationScheme.getActions().get(0), DeclarePatternRole.class);

		gcDelegate.addTab("DeclarePatternRole", widget.getController());
	}

	public static void initGUI() {
		gcDelegate = new GraphicalContextDelegate(TestEditionSchemePanel.class.getSimpleName());
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
