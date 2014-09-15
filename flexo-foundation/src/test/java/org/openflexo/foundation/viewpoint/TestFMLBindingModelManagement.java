package org.openflexo.foundation.viewpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.antar.binding.BindingModel;
import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.OpenflexoTestCase;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.view.View;
import org.openflexo.foundation.viewpoint.PrimitiveRole.PrimitiveType;
import org.openflexo.foundation.viewpoint.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.viewpoint.VirtualModel.VirtualModelImpl;
import org.openflexo.foundation.viewpoint.action.AddFlexoConcept;
import org.openflexo.foundation.viewpoint.action.CreateEditionAction;
import org.openflexo.foundation.viewpoint.action.CreateEditionAction.CreateEditionActionChoice;
import org.openflexo.foundation.viewpoint.action.CreateFlexoBehaviour;
import org.openflexo.foundation.viewpoint.action.CreateFlexoRole;
import org.openflexo.foundation.viewpoint.action.CreateModelSlot;
import org.openflexo.foundation.viewpoint.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.viewpoint.binding.FlexoRoleBindingVariable;
import org.openflexo.foundation.viewpoint.binding.ViewPointBindingModel;
import org.openflexo.foundation.viewpoint.binding.VirtualModelBindingModel;
import org.openflexo.foundation.viewpoint.editionaction.AssignationAction;
import org.openflexo.foundation.viewpoint.editionaction.DeclareFlexoRole;
import org.openflexo.foundation.viewpoint.rm.ViewPointResource;
import org.openflexo.foundation.viewpoint.rm.VirtualModelResource;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test {@link BindingModel} management along FML model
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestFMLBindingModelManagement extends OpenflexoTestCase {

	static FlexoEditor editor;
	static ViewPoint viewPoint;
	static VirtualModel virtualModel1;
	static VirtualModel virtualModel2;
	static VirtualModel virtualModel3;

	static FlexoConcept flexoConceptA;
	static FlexoConcept flexoConceptB;
	static FlexoConcept flexoConceptC;
	static FlexoConcept flexoConceptD;

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
	 * Test the VP creation
	 */
	@Test
	@TestOrder(2)
	public void testCreateViewPoint() {
		viewPoint = ViewPointImpl.newViewPoint("TestViewPoint", "http://openflexo.org/test/TestViewPoint", resourceCenter.getDirectory(),
				serviceManager.getViewPointLibrary());
		assertTrue(((ViewPointResource) viewPoint.getResource()).getDirectory().exists());
		assertTrue(((ViewPointResource) viewPoint.getResource()).getFile().exists());

		System.out.println("ViewPoint BindingModel = " + viewPoint.getBindingModel());
		assertNotNull(viewPoint.getBindingModel());
		assertEquals(1, viewPoint.getBindingModel().getBindingVariablesCount());
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));

	}

	/**
	 * Test the VirtualModel creation
	 */
	@Test
	@TestOrder(2)
	public void testCreateVirtualModel() throws SaveResourceException {

		virtualModel1 = VirtualModelImpl.newVirtualModel("VM1", viewPoint);
		assertTrue(((VirtualModelResource) virtualModel1.getResource()).getDirectory().exists());
		assertTrue(((VirtualModelResource) virtualModel1.getResource()).getFile().exists());

		assertNotNull(virtualModel1.getBindingModel());
		assertEquals(4, virtualModel1.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1), virtualModel1.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

		// We disconnect VirtualModel from ViewPoint, and we check BindingModel evolution
		viewPoint.removeFromVirtualModels(virtualModel1);
		System.out.println("VirtualModel BindingModel = " + virtualModel1.getBindingModel());
		assertNotNull(virtualModel1.getBindingModel());
		assertEquals(3, virtualModel1.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(View.class, virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		// assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1), virtualModel1.getBindingModel()
		// .bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

		// We reconnect VirtualModel again, and we check BindingModel evolution
		viewPoint.addToVirtualModels(virtualModel1);
		System.out.println("VirtualModel BindingModel = " + virtualModel1.getBindingModel());
		assertEquals(4, virtualModel1.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1), virtualModel1.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

	}

	/**
	 * Test the FlexoConcept creation
	 */
	@Test
	@TestOrder(4)
	public void testCreateFlexoConceptA() throws SaveResourceException {

		AddFlexoConcept addEP = AddFlexoConcept.actionType.makeNewAction(virtualModel1, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptA");
		addEP.doAction();

		flexoConceptA = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept A = " + flexoConceptA);
		assertNotNull(flexoConceptA);

		((VirtualModelResource) virtualModel1.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) virtualModel1.getResource()).getFile());

		System.out.println("FlexoConcept BindingModel = " + flexoConceptA.getBindingModel());

		assertEquals(5, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1), flexoConceptA.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

		// Disconnect FlexoConcept
		virtualModel1.removeFromFlexoConcepts(flexoConceptA);

		assertEquals(1, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		/*assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstance.class,
				flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());*/

		// Reconnect FlexoConcept
		virtualModel1.addToFlexoConcepts(flexoConceptA);

		assertEquals(5, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1), flexoConceptA.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

	}

	@Test
	@TestOrder(5)
	public void testCreateSomeFlexoRolesToConceptA() throws SaveResourceException {

		CreateFlexoRole createPR1 = CreateFlexoRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR1.setRoleName("aStringInA");
		createPR1.setFlexoRoleClass(PrimitiveRole.class);
		createPR1.setPrimitiveType(PrimitiveType.String);
		createPR1.doAction();

		CreateFlexoRole createPR2 = CreateFlexoRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR2.setRoleName("aBooleanInA");
		createPR2.setFlexoRoleClass(PrimitiveRole.class);
		createPR2.setPrimitiveType(PrimitiveType.Boolean);
		createPR2.doAction();

		CreateFlexoRole createPR3 = CreateFlexoRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR3.setRoleName("anIntegerInA");
		createPR3.setFlexoRoleClass(PrimitiveRole.class);
		createPR3.setPrimitiveType(PrimitiveType.Integer);
		createPR3.doAction();

		assertEquals(3, flexoConceptA.getFlexoRoles().size());
		assertTrue(flexoConceptA.getFlexoRoles().contains(createPR1.getNewFlexoRole()));
		assertTrue(flexoConceptA.getFlexoRoles().contains(createPR2.getNewFlexoRole()));
		assertTrue(flexoConceptA.getFlexoRoles().contains(createPR3.getNewFlexoRole()));

		System.out.println("FlexoConcept BindingModel = " + flexoConceptA.getBindingModel());

		assertEquals(8, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), flexoConceptA.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConceptA.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.TYPE, flexoConceptA.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, flexoConceptA.getBindingModel().bindingVariableNamed("anIntegerInA").getType());

		PrimitiveRole aStringInA = (PrimitiveRole) flexoConceptA.getFlexoRole("aStringInA");
		assertNotNull(aStringInA);

		FlexoRoleBindingVariable bv = (FlexoRoleBindingVariable) flexoConceptA.getBindingModel().bindingVariableNamed("aStringInA");
		assertNotNull(bv);

		// Attempt to change name
		renameWasNotified = false;
		bv.getPropertyChangeSupport().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(BindingVariable.VARIABLE_NAME_PROPERTY)) {
					renameWasNotified = true;
				}
			}
		});

		// Attempt to change type
		aStringInA.setName("aRenamedStringInA");
		if (!renameWasNotified) {
			fail("FlexoRole renaming was not notified");
		}

		typeChangedWasNotified = false;
		bv.getPropertyChangeSupport().addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(BindingVariable.TYPE_PROPERTY)) {
					typeChangedWasNotified = true;
				}
			}
		});

		aStringInA.setPrimitiveType(PrimitiveType.Float);
		if (!typeChangedWasNotified) {
			fail("FlexoRole type changing was not notified");
		}

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("aRenamedStringInA"));
		assertEquals(Float.TYPE, flexoConceptA.getBindingModel().bindingVariableNamed("aRenamedStringInA").getType());

		// Back to initial values
		aStringInA.setName("aStringInA");
		aStringInA.setPrimitiveType(PrimitiveType.String);
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConceptA.getBindingModel().bindingVariableNamed("aStringInA").getType());

		System.out.println("FlexoConcept BindingModel = " + flexoConceptA.getBindingModel());
	}

	private boolean renameWasNotified = false;
	private boolean rename2WasNotified = false;
	private boolean typeChangedWasNotified = false;

	@Test
	@TestOrder(6)
	public void testSomeMoreFlexoConcept() throws SaveResourceException {

		AddFlexoConcept addFlexoConceptB = AddFlexoConcept.actionType.makeNewAction(virtualModel1, null, editor);
		addFlexoConceptB.setNewFlexoConceptName("FlexoConceptB");
		addFlexoConceptB.doAction();
		flexoConceptB = addFlexoConceptB.getNewFlexoConcept();
		assertNotNull(flexoConceptB);

		AddFlexoConcept addFlexoConceptC = AddFlexoConcept.actionType.makeNewAction(virtualModel1, null, editor);
		addFlexoConceptC.setNewFlexoConceptName("FlexoConceptC");
		addFlexoConceptC.addToParentConcepts(flexoConceptB);
		addFlexoConceptC.doAction();
		flexoConceptC = addFlexoConceptC.getNewFlexoConcept();

		CreateFlexoRole createRoleInFlexoConceptB = CreateFlexoRole.actionType.makeNewAction(flexoConceptB, null, editor);
		createRoleInFlexoConceptB.setRoleName("aStringInB");
		createRoleInFlexoConceptB.setFlexoRoleClass(PrimitiveRole.class);
		createRoleInFlexoConceptB.setPrimitiveType(PrimitiveType.String);
		createRoleInFlexoConceptB.doAction();

		assertEquals(1, flexoConceptB.getFlexoRoles().size());
		assertTrue(flexoConceptB.getFlexoRoles().contains(createRoleInFlexoConceptB.getNewFlexoRole()));

		CreateFlexoRole createRoleInFlexoConceptC = CreateFlexoRole.actionType.makeNewAction(flexoConceptC, null, editor);
		createRoleInFlexoConceptC.setRoleName("aStringInC");
		createRoleInFlexoConceptC.setFlexoRoleClass(PrimitiveRole.class);
		createRoleInFlexoConceptC.setPrimitiveType(PrimitiveType.String);
		createRoleInFlexoConceptC.doAction();

		assertEquals(1, flexoConceptC.getFlexoRoles().size());
		assertTrue(flexoConceptC.getFlexoRoles().contains(createRoleInFlexoConceptC.getNewFlexoRole()));

		((VirtualModelResource) virtualModel1.getResource()).save(null);

		assertEquals(6, flexoConceptB.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), flexoConceptB.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed("aStringInB"));
		assertEquals(String.class, flexoConceptB.getBindingModel().bindingVariableNamed("aStringInB").getType());

		assertEquals(7, flexoConceptC.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), flexoConceptC.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aStringInB"));
		assertEquals(String.class, flexoConceptC.getBindingModel().bindingVariableNamed("aStringInB").getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aStringInC"));
		assertEquals(String.class, flexoConceptC.getBindingModel().bindingVariableNamed("aStringInC").getType());

		PrimitiveRole aStringInB = (PrimitiveRole) flexoConceptB.getFlexoRole("aStringInB");
		assertNotNull(aStringInB);

		// We now try to rename the FlexoRole in FlexoConceptB

		FlexoRoleBindingVariable bvForB = (FlexoRoleBindingVariable) flexoConceptB.getBindingModel().bindingVariableNamed("aStringInB");
		assertNotNull(bvForB);

		renameWasNotified = false;
		bvForB.getPropertyChangeSupport().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(BindingVariable.VARIABLE_NAME_PROPERTY)) {
					renameWasNotified = true;
				}
			}
		});

		FlexoRoleBindingVariable bvForC = (FlexoRoleBindingVariable) flexoConceptC.getBindingModel().bindingVariableNamed("aStringInB");
		assertNotNull(bvForC);

		rename2WasNotified = false;
		bvForC.getPropertyChangeSupport().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(BindingVariable.VARIABLE_NAME_PROPERTY)) {
					rename2WasNotified = true;
				}
			}
		});

		aStringInB.setName("aRenamedStringInB");

		if (!renameWasNotified || !rename2WasNotified) {
			fail("FlexoRole renaming was not notified");
		}

		assertEquals(6, flexoConceptB.getBindingModel().getBindingVariablesCount());
		assertEquals(7, flexoConceptC.getBindingModel().getBindingVariablesCount());

		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed("aRenamedStringInB"));
		assertEquals(String.class, flexoConceptB.getBindingModel().bindingVariableNamed("aRenamedStringInB").getType());

		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aRenamedStringInB"));
		assertEquals(String.class, flexoConceptC.getBindingModel().bindingVariableNamed("aRenamedStringInB").getType());

		// We now try to add a FlexoRole in FlexoConceptB
		CreateFlexoRole createRoleInFlexoConceptB2 = CreateFlexoRole.actionType.makeNewAction(flexoConceptB, null, editor);
		createRoleInFlexoConceptB2.setRoleName("anOtherStringInB");
		createRoleInFlexoConceptB2.setFlexoRoleClass(PrimitiveRole.class);
		createRoleInFlexoConceptB2.setPrimitiveType(PrimitiveType.String);
		createRoleInFlexoConceptB2.doAction();

		assertEquals(7, flexoConceptB.getBindingModel().getBindingVariablesCount());
		assertEquals(8, flexoConceptC.getBindingModel().getBindingVariablesCount());

		flexoConceptC.removeFromParentFlexoConcepts(flexoConceptB);

		assertEquals(6, flexoConceptC.getBindingModel().getBindingVariablesCount());

		flexoConceptC.addToParentFlexoConcepts(flexoConceptA);
		flexoConceptC.addToParentFlexoConcepts(flexoConceptB);

		assertEquals(11, flexoConceptC.getBindingModel().getBindingVariablesCount());
	}

	@Test
	@TestOrder(7)
	public void testSomeMoreFlexoConcept2() throws SaveResourceException {

		AddFlexoConcept addFlexoConceptD = AddFlexoConcept.actionType.makeNewAction(virtualModel1, null, editor);
		addFlexoConceptD.setNewFlexoConceptName("FlexoConceptD");
		addFlexoConceptD.doAction();
		flexoConceptD = addFlexoConceptD.getNewFlexoConcept();
		assertNotNull(flexoConceptD);

		assertEquals(5, flexoConceptD.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), flexoConceptD.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

		flexoConceptD.addToParentFlexoConcepts(flexoConceptC);
		flexoConceptD.addToParentFlexoConcepts(flexoConceptB);

		assertEquals(11, flexoConceptD.getBindingModel().getBindingVariablesCount());

		// add role in FlexoConceptA
		CreateFlexoRole createOtherBooleanInA = CreateFlexoRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createOtherBooleanInA.setRoleName("anOtherBooleanInA");
		createOtherBooleanInA.setFlexoRoleClass(PrimitiveRole.class);
		createOtherBooleanInA.setPrimitiveType(PrimitiveType.Boolean);
		createOtherBooleanInA.doAction();

		// Check that this role is visible in FlexoConceptD
		assertEquals(12, flexoConceptD.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.TYPE, flexoConceptD.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());

		((VirtualModelResource) virtualModel1.getResource()).save(null);
		System.out.println("Saved: " + ((VirtualModelResource) virtualModel1.getResource()).getFile());

	}

	/**
	 * Test the VirtualModel creation
	 */
	@Test
	@TestOrder(8)
	public void testCreateOtherVirtualModels() throws SaveResourceException {

		virtualModel2 = VirtualModelImpl.newVirtualModel("VM2", viewPoint);
		assertTrue(((VirtualModelResource) virtualModel2.getResource()).getDirectory().exists());
		assertTrue(((VirtualModelResource) virtualModel2.getResource()).getFile().exists());

		assertNotNull(virtualModel2.getBindingModel());
		assertEquals(4, virtualModel2.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel2), virtualModel2.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

		virtualModel3 = VirtualModelImpl.newVirtualModel("VM3", viewPoint);
		assertTrue(((VirtualModelResource) virtualModel3.getResource()).getDirectory().exists());
		assertTrue(((VirtualModelResource) virtualModel3.getResource()).getFile().exists());

		assertNotNull(virtualModel3.getBindingModel());
		assertEquals(4, virtualModel3.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel3), virtualModel3.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

		// Now we create the vm1 model slot
		CreateModelSlot createMS1 = CreateModelSlot.actionType.makeNewAction(virtualModel3, null, editor);
		createMS1.setTechnologyAdapter(serviceManager.getTechnologyAdapterService().getTechnologyAdapter(
				VirtualModelTechnologyAdapter.class));
		createMS1.setModelSlotClass(VirtualModelModelSlot.class);
		createMS1.setModelSlotName("vm1");
		createMS1.setVmRes((VirtualModelResource) virtualModel1.getResource());
		createMS1.doAction();
		assertTrue(createMS1.hasActionExecutionSucceeded());

		// Now we create the vm2 model slot
		CreateModelSlot createMS2 = CreateModelSlot.actionType.makeNewAction(virtualModel3, null, editor);
		createMS2.setTechnologyAdapter(serviceManager.getTechnologyAdapterService().getTechnologyAdapter(
				VirtualModelTechnologyAdapter.class));
		createMS2.setModelSlotClass(VirtualModelModelSlot.class);
		createMS2.setModelSlotName("vm2");
		createMS2.setVmRes((VirtualModelResource) virtualModel2.getResource());
		createMS2.doAction();
		assertTrue(createMS2.hasActionExecutionSucceeded());

		// VirtualModel should have two VirtualModelModelSlot
		assertEquals(2, virtualModel3.getModelSlots(VirtualModelModelSlot.class).size());

		CreateFlexoRole createRoleInVM3 = CreateFlexoRole.actionType.makeNewAction(virtualModel3, null, editor);
		createRoleInVM3.setRoleName("aStringInVM3");
		createRoleInVM3.setFlexoRoleClass(PrimitiveRole.class);
		createRoleInVM3.setPrimitiveType(PrimitiveType.String);
		createRoleInVM3.doAction();

		System.out.println("BM=" + virtualModel3.getBindingModel());

		assertNotNull(virtualModel3.getBindingModel());
		assertEquals(7, virtualModel3.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel3), virtualModel3.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("vm1"));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1), virtualModel3.getBindingModel()
				.bindingVariableNamed("vm1").getType());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("vm2"));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel2), virtualModel3.getBindingModel()
				.bindingVariableNamed("vm2").getType());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("aStringInVM3"));
		assertEquals(String.class, virtualModel3.getBindingModel().bindingVariableNamed("aStringInVM3").getType());

		// Attempt to remove a model slot
		ModelSlot<?> ms1 = virtualModel3.getModelSlot("vm1");
		assertNotNull(ms1);
		virtualModel3.removeFromModelSlots(ms1);
		assertEquals(6, virtualModel3.getBindingModel().getBindingVariablesCount());

		// Add it again
		virtualModel3.addToModelSlots(ms1);
		assertEquals(7, virtualModel3.getBindingModel().getBindingVariablesCount());

	}

	@Test
	@TestOrder(9)
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

		System.out.println("BM=" + creationScheme.getBindingModel());
	}

	/*@Test
	@TestOrder(13)
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

		System.out.println("FlexoConcept BindingModel = " + flexoConceptA.getBindingModel());

	}*/

}
