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
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CheckboxParameter;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.PrimitiveRole.PrimitiveType;
import org.openflexo.foundation.fml.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.fml.VirtualModel.VirtualModelImpl;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviourParameter;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoRole;
import org.openflexo.foundation.fml.action.CreateEditionAction.CreateEditionActionChoice;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ConditionalAction;
import org.openflexo.foundation.fml.editionaction.DeclareFlexoRole;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.resource.SaveResourceException;
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
	static VirtualModel newVirtualModel;

	static FlexoConcept flexoConceptA;
	static FlexoConcept flexoConceptB;
	static FlexoConcept flexoConceptC;
	static FlexoConcept flexoConceptD;
	static FlexoConcept flexoConceptE;

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
		//assertTrue(((ViewPointResource) newViewPoint.getResource()).getDirectory().exists());
		//assertTrue(((ViewPointResource) newViewPoint.getResource()).getFile().exists());
		assertTrue(((ViewPointResource) newViewPoint.getResource()).getDirectory()!=null);
		assertTrue(((ViewPointResource) newViewPoint.getResource()).getFlexoIODelegate().exists());
	}

	/**
	 * Test the VirtualModel creation
	 */
	@Test
	@TestOrder(2)
	public void testCreateVirtualModel() throws SaveResourceException {
		newVirtualModel = VirtualModelImpl.newVirtualModel("TestVirtualModel", newViewPoint);
		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) newVirtualModel.getResource()).getDirectory()).exists());
		assertTrue(((VirtualModelResource) newVirtualModel.getResource()).getFlexoIODelegate().exists());
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

		System.out.println("FlexoConcept A = " + flexoConceptA);
		assertNotNull(flexoConceptA);

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		//System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFile());
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

		assertEquals(3, flexoConceptA.getFlexoRoles().size());
		assertTrue(flexoConceptA.getFlexoRoles().contains(createPR1.getNewFlexoRole()));
		assertTrue(flexoConceptA.getFlexoRoles().contains(createPR2.getNewFlexoRole()));
		assertTrue(flexoConceptA.getFlexoRoles().contains(createPR3.getNewFlexoRole()));

	}

	@Test
	@TestOrder(10)
	public void testCreateACreationSchemeInConceptA() throws SaveResourceException {

		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme.doAction();
		CreationScheme creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();

		CreateEditionAction createEditionAction1 = CreateEditionAction.actionType.makeNewAction(creationScheme, null, editor);
		createEditionAction1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction1.setBuiltInActionClass(DeclareFlexoRole.class);
		createEditionAction1.doAction();
		DeclareFlexoRole action1 = (DeclareFlexoRole) createEditionAction1.getNewEditionAction();
		action1.setAssignation(new DataBinding<Object>("aString"));
		action1.setObject(new DataBinding<Object>("'foo'"));

		CreateEditionAction createEditionAction2 = CreateEditionAction.actionType.makeNewAction(creationScheme, null, editor);
		createEditionAction2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction2.setBuiltInActionClass(DeclareFlexoRole.class);
		createEditionAction2.doAction();
		DeclareFlexoRole action2 = (DeclareFlexoRole) createEditionAction2.getNewEditionAction();
		action2.setAssignation(new DataBinding<Object>("aBoolean"));
		action2.setObject(new DataBinding<Object>("true"));

		CreateEditionAction createEditionAction3 = CreateEditionAction.actionType.makeNewAction(creationScheme, null, editor);
		createEditionAction3.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction3.setBuiltInActionClass(AssignationAction.class);
		createEditionAction3.doAction();
		AssignationAction action3 = (AssignationAction) createEditionAction3.getNewEditionAction();
		action3.setAssignation(new DataBinding<Object>("anInteger"));
		action3.setValue(new DataBinding<Object>("8"));

		assertTrue(flexoConceptA.getFlexoBehaviours().contains(creationScheme));
		assertTrue(flexoConceptA.getCreationSchemes().contains(creationScheme));

		assertEquals(3, creationScheme.getActions().size());
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

		CreateEditionAction createConditionAction1 = CreateEditionAction.actionType.makeNewAction(actionScheme, null, editor);
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

		CreateEditionAction createConditionAction2 = CreateEditionAction.actionType.makeNewAction(actionScheme, null, editor);
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
}
