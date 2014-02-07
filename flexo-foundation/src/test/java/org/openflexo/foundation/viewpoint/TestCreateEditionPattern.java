package org.openflexo.foundation.viewpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.OpenflexoTestCase;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.viewpoint.PrimitivePatternRole.PrimitiveType;
import org.openflexo.foundation.viewpoint.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.viewpoint.VirtualModel.VirtualModelImpl;
import org.openflexo.foundation.viewpoint.action.AddEditionPattern;
import org.openflexo.foundation.viewpoint.action.CreateEditionAction;
import org.openflexo.foundation.viewpoint.action.CreateEditionAction.CreateEditionActionChoice;
import org.openflexo.foundation.viewpoint.action.CreateEditionScheme;
import org.openflexo.foundation.viewpoint.action.CreateEditionSchemeParameter;
import org.openflexo.foundation.viewpoint.action.CreatePatternRole;
import org.openflexo.foundation.viewpoint.editionaction.AssignationAction;
import org.openflexo.foundation.viewpoint.editionaction.ConditionalAction;
import org.openflexo.foundation.viewpoint.editionaction.DeclarePatternRole;
import org.openflexo.foundation.viewpoint.rm.ViewPointResource;
import org.openflexo.foundation.viewpoint.rm.VirtualModelResource;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test EditionPattern creation facilities
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestCreateEditionPattern extends OpenflexoTestCase {

	static FlexoEditor editor;
	static ViewPoint newViewPoint;
	static VirtualModel newVirtualModel;

	static EditionPattern flexoConceptA;
	static EditionPattern flexoConceptB;
	static EditionPattern flexoConceptC;
	static EditionPattern flexoConceptD;
	static EditionPattern flexoConceptE;

	/**
	 * Test the VP creation
	 */
	@Test
	@TestOrder(1)
	public void testCreateViewPoint() {
		instanciateTestServiceManager();
		System.out.println("ResourceCenter= " + resourceCenter);
		newViewPoint = ViewPointImpl.newViewPoint("TestViewPoint", "http://openflexo.org/test/TestViewPoint",
				resourceCenter.getDirectory(), serviceManager.getViewPointLibrary());
		assertTrue(((ViewPointResource) newViewPoint.getResource()).getDirectory().exists());
		assertTrue(((ViewPointResource) newViewPoint.getResource()).getFile().exists());
	}

	/**
	 * Test the VirtualModel creation
	 */
	@Test
	@TestOrder(2)
	public void testCreateVirtualModel() throws SaveResourceException {
		newVirtualModel = VirtualModelImpl.newVirtualModel("TestVirtualModel", newViewPoint);
		assertTrue(((VirtualModelResource) newVirtualModel.getResource()).getDirectory().exists());
		assertTrue(((VirtualModelResource) newVirtualModel.getResource()).getFile().exists());
	}

	/**
	 * Test the EditionPattern creation
	 */
	@Test
	@TestOrder(3)
	public void testCreateEditor() {
		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);
	}

	/**
	 * Test the EditionPattern creation
	 */
	@Test
	@TestOrder(4)
	public void testCreateFlexoConceptA() throws SaveResourceException {

		AddEditionPattern addEP = AddEditionPattern.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptA");
		addEP.doAction();

		flexoConceptA = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept A = " + flexoConceptA);
		assertNotNull(flexoConceptA);

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFile());

	}

	/**
	 * Test the EditionPattern creation
	 */
	@Test
	@TestOrder(5)
	public void testCreateFlexoConceptB() throws SaveResourceException {

		AddEditionPattern addEP = AddEditionPattern.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptB");
		addEP.doAction();

		flexoConceptB = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept B = " + flexoConceptB);
		assertNotNull(flexoConceptB);

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFile());

	}

	/**
	 * Test the EditionPattern creation
	 */
	@Test
	@TestOrder(6)
	public void testCreateFlexoConceptC() throws SaveResourceException {

		AddEditionPattern addEP = AddEditionPattern.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptC");
		addEP.addToParentConcepts(flexoConceptB);
		addEP.doAction();

		flexoConceptC = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept C = " + flexoConceptC);
		assertNotNull(flexoConceptC);
		assertEquals(1, flexoConceptC.getParentEditionPatterns().size());
		assertEquals(flexoConceptB, flexoConceptC.getParentEditionPatterns().get(0));

		assertEquals(1, flexoConceptB.getChildEditionPatterns().size());
		assertEquals(flexoConceptC, flexoConceptB.getChildEditionPatterns().get(0));

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFile());

	}

	/**
	 * Test the EditionPattern creation
	 */
	@Test
	@TestOrder(7)
	public void testCreateFlexoConceptD() throws SaveResourceException {

		AddEditionPattern addEP = AddEditionPattern.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptD");
		addEP.addToParentConcepts(flexoConceptB);
		addEP.doAction();

		flexoConceptD = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept D = " + flexoConceptD);
		assertNotNull(flexoConceptD);
		assertEquals(1, flexoConceptD.getParentEditionPatterns().size());
		assertEquals(flexoConceptB, flexoConceptD.getParentEditionPatterns().get(0));

		assertEquals(2, flexoConceptB.getChildEditionPatterns().size());
		assertEquals(flexoConceptC, flexoConceptB.getChildEditionPatterns().get(0));
		assertEquals(flexoConceptD, flexoConceptB.getChildEditionPatterns().get(1));

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFile());

	}

	/**
	 * Test the EditionPattern creation
	 */
	@Test
	@TestOrder(8)
	public void testCreateFlexoConceptE() throws SaveResourceException {

		AddEditionPattern addEP = AddEditionPattern.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptE");
		addEP.addToParentConcepts(flexoConceptA);
		addEP.addToParentConcepts(flexoConceptB);
		addEP.addToParentConcepts(flexoConceptC);
		addEP.doAction();

		flexoConceptE = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept E = " + flexoConceptE);
		assertNotNull(flexoConceptE);
		assertEquals(3, flexoConceptE.getParentEditionPatterns().size());
		assertEquals(flexoConceptA, flexoConceptE.getParentEditionPatterns().get(0));
		assertEquals(flexoConceptB, flexoConceptE.getParentEditionPatterns().get(1));
		assertEquals(flexoConceptC, flexoConceptE.getParentEditionPatterns().get(2));

		assertEquals(1, flexoConceptA.getChildEditionPatterns().size());
		assertEquals(flexoConceptE, flexoConceptA.getChildEditionPatterns().get(0));
		assertEquals(3, flexoConceptB.getChildEditionPatterns().size());
		assertEquals(flexoConceptC, flexoConceptB.getChildEditionPatterns().get(0));
		assertEquals(flexoConceptD, flexoConceptB.getChildEditionPatterns().get(1));
		assertEquals(flexoConceptE, flexoConceptB.getChildEditionPatterns().get(2));
		assertEquals(1, flexoConceptC.getChildEditionPatterns().size());
		assertEquals(flexoConceptE, flexoConceptC.getChildEditionPatterns().get(0));

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFile());

	}

	@Test
	@TestOrder(9)
	public void testCreateSomePatternRolesToConceptA() throws SaveResourceException {

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

		assertEquals(3, flexoConceptA.getPatternRoles().size());
		assertTrue(flexoConceptA.getPatternRoles().contains(createPR1.getNewPatternRole()));
		assertTrue(flexoConceptA.getPatternRoles().contains(createPR2.getNewPatternRole()));
		assertTrue(flexoConceptA.getPatternRoles().contains(createPR3.getNewPatternRole()));

	}

	@Test
	@TestOrder(10)
	public void testCreateACreationSchemeInConceptA() throws SaveResourceException {

		CreateEditionScheme createCreationScheme = CreateEditionScheme.actionType.makeNewAction(flexoConceptA, null, editor);
		createCreationScheme.editionSchemeClass = CreationScheme.class;
		createCreationScheme.doAction();
		CreationScheme creationScheme = (CreationScheme) createCreationScheme.getNewEditionScheme();

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

		assertTrue(flexoConceptA.getEditionSchemes().contains(creationScheme));
		assertTrue(flexoConceptA.getCreationSchemes().contains(creationScheme));

		assertEquals(3, creationScheme.getActions().size());
	}

	@Test
	@TestOrder(11)
	public void testCreateAnActionSchemeInConceptA() throws SaveResourceException {

		CreateEditionScheme createActionScheme = CreateEditionScheme.actionType.makeNewAction(flexoConceptA, null, editor);
		createActionScheme.editionSchemeClass = ActionScheme.class;
		createActionScheme.doAction();
		ActionScheme actionScheme = (ActionScheme) createActionScheme.getNewEditionScheme();
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
}
