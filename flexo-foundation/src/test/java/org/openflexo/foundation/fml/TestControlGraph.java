package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.OpenflexoTestCase;
import org.openflexo.foundation.fml.PrimitiveRole.PrimitiveType;
import org.openflexo.foundation.fml.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.fml.VirtualModel.VirtualModelImpl;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateEditionAction.CreateEditionActionChoice;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviourParameter;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoRole;
import org.openflexo.foundation.fml.controlgraph.EmptyControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.Sequence;
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
public class TestControlGraph extends OpenflexoTestCase {

	static FlexoEditor editor;
	static ViewPoint newViewPoint;
	static VirtualModel newVirtualModel;

	static FlexoConcept flexoConcept;

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
		// assertTrue(((ViewPointResource) newViewPoint.getResource()).getDirectory().exists());
		// assertTrue(((ViewPointResource) newViewPoint.getResource()).getFile().exists());
		assertTrue(((ViewPointResource) newViewPoint.getResource()).getDirectory() != null);
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
	public void testCreateFlexoConcept() throws SaveResourceException {

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptA");
		addEP.doAction();

		flexoConcept = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept A = " + flexoConcept);
		assertNotNull(flexoConcept);

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		// System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFile());
		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFlexoIODelegate().toString());
	}

	@Test
	@TestOrder(9)
	public void testCreateSomePatternRolesToConcept() throws SaveResourceException {

		CreateFlexoRole createPR1 = CreateFlexoRole.actionType.makeNewAction(flexoConcept, null, editor);
		createPR1.setRoleName("aString");
		createPR1.setFlexoRoleClass(PrimitiveRole.class);
		createPR1.setPrimitiveType(PrimitiveType.String);
		createPR1.doAction();

		CreateFlexoRole createPR2 = CreateFlexoRole.actionType.makeNewAction(flexoConcept, null, editor);
		createPR2.setRoleName("aBoolean");
		createPR2.setFlexoRoleClass(PrimitiveRole.class);
		createPR2.setPrimitiveType(PrimitiveType.Boolean);
		createPR2.doAction();

		CreateFlexoRole createPR3 = CreateFlexoRole.actionType.makeNewAction(flexoConcept, null, editor);
		createPR3.setRoleName("anInteger");
		createPR3.setFlexoRoleClass(PrimitiveRole.class);
		createPR3.setPrimitiveType(PrimitiveType.Integer);
		createPR3.doAction();

		assertEquals(3, flexoConcept.getFlexoRoles().size());
		assertTrue(flexoConcept.getFlexoRoles().contains(createPR1.getNewFlexoRole()));
		assertTrue(flexoConcept.getFlexoRoles().contains(createPR2.getNewFlexoRole()));
		assertTrue(flexoConcept.getFlexoRoles().contains(createPR3.getNewFlexoRole()));

	}

	@Test
	@TestOrder(10)
	public void testCreateACreationSchemeInConcept() throws SaveResourceException {

		log("testCreateACreationSchemeInConcept");

		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConcept, null, editor);
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme.doAction();
		CreationScheme creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();

		assertTrue(creationScheme.getControlGraph() instanceof EmptyControlGraph);
		assertTrue(creationScheme.getControlGraph().getBindingModel().getBaseBindingModel() == creationScheme.getBindingModel());

		CreateEditionAction createEditionAction1 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		createEditionAction1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction1.setBuiltInActionClass(DeclareFlexoRole.class);
		createEditionAction1.doAction();
		DeclareFlexoRole action1 = (DeclareFlexoRole) createEditionAction1.getNewEditionAction();
		action1.setAssignation(new DataBinding<Object>("aString"));
		action1.setObject(new DataBinding<Object>("'foo'"));
		action1.setName("action1");

		assertEquals(action1, creationScheme.getControlGraph());
		assertEquals(creationScheme.getBindingModel(), action1.getBindingModel().getBaseBindingModel());

		CreateEditionAction createEditionAction2 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		createEditionAction2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction2.setBuiltInActionClass(DeclareFlexoRole.class);
		createEditionAction2.doAction();
		DeclareFlexoRole action2 = (DeclareFlexoRole) createEditionAction2.getNewEditionAction();
		action2.setAssignation(new DataBinding<Object>("aBoolean"));
		action2.setObject(new DataBinding<Object>("true"));
		action2.setName("action2");

		assertTrue(creationScheme.getControlGraph() instanceof Sequence);
		Sequence seq1 = (Sequence) creationScheme.getControlGraph();
		assertEquals(action1, seq1.getControlGraph1());
		assertEquals(action2, seq1.getControlGraph2());

		assertTrue(seq1.getBindingModel().getBaseBindingModel() == creationScheme.getBindingModel());
		assertEquals(seq1.getBindingModel(), action1.getBindingModel().getBaseBindingModel());
		assertEquals(seq1.getControlGraph1().getInferedBindingModel(), action2.getBindingModel().getBaseBindingModel());

		CreateEditionAction createEditionAction3 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		createEditionAction3.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction3.setBuiltInActionClass(AssignationAction.class);
		createEditionAction3.doAction();
		AssignationAction action3 = (AssignationAction) createEditionAction3.getNewEditionAction();
		action3.setAssignation(new DataBinding<Object>("anInteger"));
		action3.setValue(new DataBinding<Object>("8"));

		assertTrue(flexoConcept.getFlexoBehaviours().contains(creationScheme));
		assertTrue(flexoConcept.getCreationSchemes().contains(creationScheme));

		VirtualModelModelFactory factory = ((VirtualModelResource) creationScheme.getVirtualModel().getResource()).getFactory();
		FMLControlGraph controlGraph = creationScheme.getControlGraph();

		// String cg = factory.stringRepresentation(creationScheme);
		// System.out.println("Control graph:\n" + cg);

		assertTrue(controlGraph instanceof Sequence);
		assertTrue(((Sequence) controlGraph).getControlGraph1() instanceof DeclareFlexoRole);
		assertTrue(((Sequence) controlGraph).getControlGraph2() instanceof Sequence);
		assertTrue(((Sequence) ((Sequence) controlGraph).getControlGraph2()).getControlGraph1() instanceof DeclareFlexoRole);
		assertTrue(((Sequence) ((Sequence) controlGraph).getControlGraph2()).getControlGraph2() instanceof AssignationAction);

		assertTrue(creationScheme.getControlGraph() instanceof Sequence);
		assertTrue(creationScheme.getControlGraph().getBindingModel().getBaseBindingModel() == creationScheme.getBindingModel());

		Sequence seq2 = (Sequence) seq1.getControlGraph2();
		assertTrue(seq1.getBindingModel().getBaseBindingModel() == creationScheme.getBindingModel());
		assertEquals(seq1.getBindingModel(), action1.getBindingModel().getBaseBindingModel());
		assertEquals(seq2.getBindingModel(), action2.getBindingModel().getBaseBindingModel());
		assertEquals(action2.getInferedBindingModel(), action3.getBindingModel().getBaseBindingModel());

		assertEquals(creationScheme, action1.getRootOwner());
		assertEquals(creationScheme, action2.getRootOwner());
		assertEquals(creationScheme, action3.getRootOwner());
		assertEquals(creationScheme, seq1.getRootOwner());
		assertEquals(creationScheme, seq2.getRootOwner());
	}

	@Test
	@TestOrder(11)
	public void testCreateAnActionSchemeInConcept() throws SaveResourceException {

		log("testCreateAnActionSchemeInConcept");

		CreateFlexoBehaviour createActionScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConcept, null, editor);
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
		createConditionAction1.actionChoice = CreateEditionActionChoice.ControlAction;
		createConditionAction1.setControlActionClass(ConditionalAction.class);
		createConditionAction1.doAction();
		ConditionalAction conditional1 = (ConditionalAction) createConditionAction1.getNewEditionAction();
		conditional1.setCondition(new DataBinding<Boolean>("parameters.aFlag = true"));

		VirtualModelModelFactory factory = ((VirtualModelResource) actionScheme.getVirtualModel().getResource()).getFactory();

		// String cg = factory.stringRepresentation(actionScheme);
		// System.out.println("1 - Control graph:\n" + cg);

		assertNotNull(conditional1);
		assertTrue(conditional1.getCondition().isValid());

		CreateEditionAction createDeclarePatternRoleInCondition1 = CreateEditionAction.actionType.makeNewAction(
				conditional1.getThenControlGraph(), null, editor);
		createDeclarePatternRoleInCondition1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclarePatternRoleInCondition1.setBuiltInActionClass(DeclareFlexoRole.class);
		createDeclarePatternRoleInCondition1.doAction();

		assertTrue(createDeclarePatternRoleInCondition1.hasActionExecutionSucceeded());

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

		CreateEditionAction createDeclarePatternRoleInCondition2 = CreateEditionAction.actionType.makeNewAction(
				conditional2.getThenControlGraph(), null, editor);
		createDeclarePatternRoleInCondition2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclarePatternRoleInCondition2.setBuiltInActionClass(DeclareFlexoRole.class);
		createDeclarePatternRoleInCondition2.doAction();
		DeclareFlexoRole declarePatternRoleInCondition2 = (DeclareFlexoRole) createDeclarePatternRoleInCondition2.getNewEditionAction();
		declarePatternRoleInCondition2.setAssignation(new DataBinding<Object>("anInteger"));
		declarePatternRoleInCondition2.setObject(new DataBinding<Object>("12"));

		conditional2.setElseControlGraph(factory.newEmptyControlGraph());
		CreateEditionAction createDeclarePatternRole2InCondition2 = CreateEditionAction.actionType.makeNewAction(
				conditional2.getElseControlGraph(), null, editor);
		createDeclarePatternRole2InCondition2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclarePatternRole2InCondition2.setBuiltInActionClass(DeclareFlexoRole.class);
		createDeclarePatternRole2InCondition2.doAction();
		DeclareFlexoRole declarePatternRole2InCondition2 = (DeclareFlexoRole) createDeclarePatternRole2InCondition2.getNewEditionAction();
		declarePatternRole2InCondition2.setAssignation(new DataBinding<Object>("anInteger"));
		declarePatternRole2InCondition2.setObject(new DataBinding<Object>("3"));

		String debug = factory.stringRepresentation(actionScheme);
		System.out.println("ActionScheme:\n" + debug);

		FMLControlGraph controlGraph = actionScheme.getControlGraph();

		assertTrue(controlGraph instanceof Sequence);
		assertEquals(conditional1, ((Sequence) controlGraph).getControlGraph1());
		assertEquals(conditional2, ((Sequence) controlGraph).getControlGraph2());
		assertEquals(declarePatternRoleInCondition1, conditional1.getThenControlGraph());
		assertEquals(declarePatternRoleInCondition2, conditional2.getThenControlGraph());
		assertEquals(declarePatternRole2InCondition2, conditional2.getElseControlGraph());

		assertEquals(actionScheme, declarePatternRoleInCondition1.getRootOwner());
		assertEquals(actionScheme, declarePatternRoleInCondition2.getRootOwner());
		assertEquals(actionScheme, declarePatternRole2InCondition2.getRootOwner());
		assertEquals(actionScheme, conditional1.getRootOwner());
		assertEquals(actionScheme, conditional2.getRootOwner());

	}
}
