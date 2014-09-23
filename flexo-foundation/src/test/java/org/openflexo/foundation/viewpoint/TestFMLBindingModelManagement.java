package org.openflexo.foundation.viewpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.antar.binding.Bindable;
import org.openflexo.antar.binding.BindingEvaluationContext;
import org.openflexo.antar.binding.BindingModel;
import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.binding.DataBinding.BindingDefinitionType;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.View;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.view.action.ActionSchemeAction;
import org.openflexo.foundation.view.action.ActionSchemeActionType;
import org.openflexo.foundation.view.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.view.action.CreateView;
import org.openflexo.foundation.view.action.CreationSchemeAction;
import org.openflexo.foundation.view.action.ModelSlotInstanceConfiguration.DefaultModelSlotInstanceConfigurationOption;
import org.openflexo.foundation.view.rm.ViewResource;
import org.openflexo.foundation.view.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.viewpoint.PrimitiveRole.PrimitiveType;
import org.openflexo.foundation.viewpoint.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.viewpoint.VirtualModel.VirtualModelImpl;
import org.openflexo.foundation.viewpoint.action.AddFlexoConcept;
import org.openflexo.foundation.viewpoint.action.CreateEditionAction;
import org.openflexo.foundation.viewpoint.action.CreateEditionAction.CreateEditionActionChoice;
import org.openflexo.foundation.viewpoint.action.CreateFlexoBehaviour;
import org.openflexo.foundation.viewpoint.action.CreateFlexoBehaviourParameter;
import org.openflexo.foundation.viewpoint.action.CreateFlexoRole;
import org.openflexo.foundation.viewpoint.action.CreateModelSlot;
import org.openflexo.foundation.viewpoint.binding.FlexoBehaviourBindingModel;
import org.openflexo.foundation.viewpoint.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.viewpoint.binding.FlexoRoleBindingVariable;
import org.openflexo.foundation.viewpoint.binding.ViewPointBindingModel;
import org.openflexo.foundation.viewpoint.binding.VirtualModelBindingModel;
import org.openflexo.foundation.viewpoint.editionaction.AssignationAction;
import org.openflexo.foundation.viewpoint.editionaction.ConditionalAction;
import org.openflexo.foundation.viewpoint.editionaction.CreateFlexoConceptInstanceParameter;
import org.openflexo.foundation.viewpoint.editionaction.DeclareFlexoRole;
import org.openflexo.foundation.viewpoint.editionaction.FetchRequestCondition;
import org.openflexo.foundation.viewpoint.editionaction.FetchRequestIterationAction;
import org.openflexo.foundation.viewpoint.editionaction.IterationAction;
import org.openflexo.foundation.viewpoint.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.viewpoint.editionaction.MatchingCriteria;
import org.openflexo.foundation.viewpoint.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.viewpoint.inspector.FlexoConceptInspector;
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
public class TestFMLBindingModelManagement extends OpenflexoProjectAtRunTimeTestCase {

	static FlexoEditor editor;
	static ViewPoint viewPoint;
	static VirtualModel virtualModel1;
	static VirtualModel virtualModel2;
	static VirtualModel virtualModel3;

	static FlexoConcept flexoConceptA;
	static FlexoConcept flexoConceptB;
	static FlexoConcept flexoConceptC;
	static FlexoConcept flexoConceptD;

	static FlexoProject project;
	static View newView;
	static VirtualModelInstance vmi1;
	static VirtualModelInstance vmi2;
	static VirtualModelInstance vmi3;
	static FlexoConceptInstance fci;

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
	 * Test {@link ViewPoint} creation, check {@link BindingModel}
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
	 * Test {@link VirtualModel} creation, check {@link BindingModel}
	 */
	@Test
	@TestOrder(3)
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
	 * Test FlexoConcept creation, check BindingModel
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

		assertEquals(6, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1), flexoConceptA.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), flexoConceptA.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());

		// Disconnect FlexoConcept
		virtualModel1.removeFromFlexoConcepts(flexoConceptA);

		assertEquals(2, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));

		// Reconnect FlexoConcept
		virtualModel1.addToFlexoConcepts(flexoConceptA);

		assertEquals(6, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1), flexoConceptA.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), flexoConceptA.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());

	}

	@Test
	@TestOrder(5)
	public void testFlexoRoleBindingModelManagement() throws SaveResourceException {

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

		assertEquals(9, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), flexoConceptA.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), flexoConceptA.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
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
	public void testFlexoConceptBindingModelManagement() throws SaveResourceException {

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

		assertEquals(7, flexoConceptB.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), flexoConceptB.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptB), flexoConceptB.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed("aStringInB"));
		assertEquals(String.class, flexoConceptB.getBindingModel().bindingVariableNamed("aStringInB").getType());

		assertEquals(8, flexoConceptC.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), flexoConceptC.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptC), flexoConceptC.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
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

		assertEquals(7, flexoConceptB.getBindingModel().getBindingVariablesCount());
		assertEquals(8, flexoConceptC.getBindingModel().getBindingVariablesCount());

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

		assertEquals(8, flexoConceptB.getBindingModel().getBindingVariablesCount());
		assertEquals(9, flexoConceptC.getBindingModel().getBindingVariablesCount());

		flexoConceptC.removeFromParentFlexoConcepts(flexoConceptB);

		assertEquals(7, flexoConceptC.getBindingModel().getBindingVariablesCount());

		flexoConceptC.addToParentFlexoConcepts(flexoConceptA);
		flexoConceptC.addToParentFlexoConcepts(flexoConceptB);

		assertEquals(12, flexoConceptC.getBindingModel().getBindingVariablesCount());
	}

	@Test
	@TestOrder(7)
	public void testFlexoConceptBindingModelManagement2() throws SaveResourceException {

		AddFlexoConcept addFlexoConceptD = AddFlexoConcept.actionType.makeNewAction(virtualModel1, null, editor);
		addFlexoConceptD.setNewFlexoConceptName("FlexoConceptD");
		addFlexoConceptD.doAction();
		flexoConceptD = addFlexoConceptD.getNewFlexoConcept();
		assertNotNull(flexoConceptD);

		assertEquals(6, flexoConceptD.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), flexoConceptD.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptD), flexoConceptD.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());

		flexoConceptD.addToParentFlexoConcepts(flexoConceptC);
		flexoConceptD.addToParentFlexoConcepts(flexoConceptB);

		assertEquals(12, flexoConceptD.getBindingModel().getBindingVariablesCount());

		// add role in FlexoConceptA
		CreateFlexoRole createOtherBooleanInA = CreateFlexoRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createOtherBooleanInA.setRoleName("anOtherBooleanInA");
		createOtherBooleanInA.setFlexoRoleClass(PrimitiveRole.class);
		createOtherBooleanInA.setPrimitiveType(PrimitiveType.Boolean);
		createOtherBooleanInA.doAction();

		// Check that this role is visible in FlexoConceptD
		assertEquals(13, flexoConceptD.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.TYPE, flexoConceptD.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());

		((VirtualModelResource) virtualModel1.getResource()).save(null);
		System.out.println("Saved: " + ((VirtualModelResource) virtualModel1.getResource()).getFile());

	}

	/**
	 * Test management of VirtualModel's BindingModel
	 */
	@Test
	@TestOrder(8)
	public void testVirtualModelBindingModelManagement() throws SaveResourceException {

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
	public void testFlexoBehaviourBindingModelManagement() throws SaveResourceException {

		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme.setFlexoBehaviourName("creationScheme");
		createCreationScheme.doAction();
		CreationScheme creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();

		CreateEditionAction createEditionAction1 = CreateEditionAction.actionType.makeNewAction(creationScheme, null, editor);
		createEditionAction1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction1.setBuiltInActionClass(DeclareFlexoRole.class);
		createEditionAction1.doAction();
		DeclareFlexoRole action1 = (DeclareFlexoRole) createEditionAction1.getNewEditionAction();
		action1.setAssignation(new DataBinding<Object>("aStringInA"));
		action1.setObject(new DataBinding<Object>("'foo'"));

		assertTrue(action1.getAssignation().isValid());
		assertTrue(action1.getObject().isValid());

		CreateEditionAction createEditionAction2 = CreateEditionAction.actionType.makeNewAction(creationScheme, null, editor);
		createEditionAction2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction2.setBuiltInActionClass(DeclareFlexoRole.class);
		createEditionAction2.doAction();
		DeclareFlexoRole action2 = (DeclareFlexoRole) createEditionAction2.getNewEditionAction();
		action2.setAssignation(new DataBinding<Object>("aBooleanInA"));
		action2.setObject(new DataBinding<Object>("true"));

		assertTrue(action2.getAssignation().isValid());
		assertTrue(action2.getObject().isValid());

		CreateEditionAction createEditionAction3 = CreateEditionAction.actionType.makeNewAction(creationScheme, null, editor);
		createEditionAction3.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction3.setBuiltInActionClass(AssignationAction.class);
		createEditionAction3.doAction();
		AssignationAction action3 = (AssignationAction) createEditionAction3.getNewEditionAction();
		action3.setAssignation(new DataBinding<Object>("anIntegerInA"));
		action3.setValue(new DataBinding<Object>("8"));

		assertTrue(action3.getAssignation().isValid());
		assertTrue(action3.getValue().isValid());

		assertTrue(flexoConceptA.getFlexoBehaviours().contains(creationScheme));
		assertTrue(flexoConceptA.getCreationSchemes().contains(creationScheme));

		assertEquals(3, creationScheme.getActions().size());

		System.out.println("FML=\n" + creationScheme.getFMLRepresentation());

		System.out.println("BM=" + creationScheme.getBindingModel());

		assertEquals(12, creationScheme.getBindingModel().getBindingVariablesCount());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				creationScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), creationScheme.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), creationScheme.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, creationScheme.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.TYPE, creationScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, creationScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.TYPE, creationScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));

	}

	@Test
	@TestOrder(13)
	public void testEditionActionBindingModelManagement() throws SaveResourceException {

		// We programmatically implement this code:
		// CreateFlexoBehaviour createActionScheme(boolean aFlag) {
		// ..if (parameters.aFlag == true) {
		// .... anIntegerInA = 8;
		// ..}
		// ..if (parameters.aFlag == false) {
		// .... anIntegerInA = 12;
		// .... for (FlexoConceptInstance fci : virtualModelInstance.flexoConceptInstances) {
		// ...... aStringInA = "foo";
		// ...... anOtherBooleanInA = (fci.toString.substring(2,3) != aStringInA)
		// .... }
		// ..}
		// }

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

		assertEquals(12, actionScheme.getBindingModel().getBindingVariablesCount());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), actionScheme.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), actionScheme.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, actionScheme.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.TYPE, actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.TYPE, actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));

		CreateEditionAction createConditionAction1 = CreateEditionAction.actionType.makeNewAction(actionScheme, null, editor);
		createConditionAction1.actionChoice = CreateEditionActionChoice.ControlAction;
		createConditionAction1.setControlActionClass(ConditionalAction.class);
		createConditionAction1.doAction();
		ConditionalAction conditional1 = (ConditionalAction) createConditionAction1.getNewEditionAction();
		conditional1.setCondition(new DataBinding<Boolean>("parameters.aFlag = true"));

		assertEquals(12, conditional1.getBindingModel().getBindingVariablesCount());

		assertNotNull(conditional1);
		assertTrue(conditional1.getCondition().isValid());

		CreateEditionAction createDeclareFlexoRoleInCondition1 = CreateEditionAction.actionType.makeNewAction(conditional1, null, editor);
		createDeclareFlexoRoleInCondition1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclareFlexoRoleInCondition1.setBuiltInActionClass(DeclareFlexoRole.class);
		createDeclareFlexoRoleInCondition1.doAction();
		DeclareFlexoRole declarePatternRoleInCondition1 = (DeclareFlexoRole) createDeclareFlexoRoleInCondition1.getNewEditionAction();
		declarePatternRoleInCondition1.setAssignation(new DataBinding<Object>("anIntegerInA"));
		declarePatternRoleInCondition1.setObject(new DataBinding<Object>("8"));

		CreateEditionAction createConditionAction2 = CreateEditionAction.actionType.makeNewAction(actionScheme, null, editor);
		createConditionAction2.actionChoice = CreateEditionActionChoice.ControlAction;
		createConditionAction2.setControlActionClass(ConditionalAction.class);
		createConditionAction2.doAction();
		ConditionalAction conditional2 = (ConditionalAction) createConditionAction2.getNewEditionAction();
		conditional2.setCondition(new DataBinding<Boolean>("parameters.aFlag = false"));

		assertNotNull(conditional2);
		assertTrue(conditional2.getCondition().isValid());

		assertEquals(12, conditional2.getBindingModel().getBindingVariablesCount());

		CreateEditionAction createDeclareFlexoRoleInCondition2 = CreateEditionAction.actionType.makeNewAction(conditional2, null, editor);
		createDeclareFlexoRoleInCondition2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclareFlexoRoleInCondition2.setBuiltInActionClass(DeclareFlexoRole.class);
		createDeclareFlexoRoleInCondition2.doAction();
		DeclareFlexoRole declareFlexoRoleInCondition2 = (DeclareFlexoRole) createDeclareFlexoRoleInCondition2.getNewEditionAction();
		declareFlexoRoleInCondition2.setAssignation(new DataBinding<Object>("anIntegerInA"));
		declareFlexoRoleInCondition2.setObject(new DataBinding<Object>("12"));

		assertEquals(12, declareFlexoRoleInCondition2.getBindingModel().getBindingVariablesCount());

		CreateEditionAction createIterationInCondition2 = CreateEditionAction.actionType.makeNewAction(conditional2, null, editor);
		createIterationInCondition2.actionChoice = CreateEditionActionChoice.ControlAction;
		createIterationInCondition2.setControlActionClass(IterationAction.class);
		createIterationInCondition2.doAction();
		IterationAction iteration = (IterationAction) createIterationInCondition2.getNewEditionAction();
		assertNotNull(iteration);
		iteration.setIteration(new DataBinding<List<?>>("virtualModelInstance.flexoConceptInstances"));
		iteration.setIteratorName("fci");

		assertTrue(iteration.getIteration().isValid());

		assertEquals(12, iteration.getBindingModel().getBindingVariablesCount());
		assertEquals(13, iteration.getControlGraphBindingModel().getBindingVariablesCount());
		assertNotNull(iteration.getControlGraphBindingModel().bindingVariableNamed("fci"));

		CreateEditionAction createDeclareFlexoRoleInIteration1 = CreateEditionAction.actionType.makeNewAction(iteration, null, editor);
		createDeclareFlexoRoleInIteration1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclareFlexoRoleInIteration1.setBuiltInActionClass(DeclareFlexoRole.class);
		createDeclareFlexoRoleInIteration1.doAction();
		DeclareFlexoRole declareFlexoRoleInIteration1 = (DeclareFlexoRole) createDeclareFlexoRoleInIteration1.getNewEditionAction();
		assertNotNull(declareFlexoRoleInIteration1);
		declareFlexoRoleInIteration1.setAssignation(new DataBinding<Object>("aStringInA"));
		declareFlexoRoleInIteration1.setObject(new DataBinding<Object>("\"foo\""));

		assertEquals(13, declareFlexoRoleInIteration1.getBindingModel().getBindingVariablesCount());
		assertNotNull(declareFlexoRoleInIteration1.getBindingModel().bindingVariableNamed("fci"));

		assertTrue(declareFlexoRoleInIteration1.getAssignation().isValid());
		assertTrue(declareFlexoRoleInIteration1.getObject().isValid());

		CreateEditionAction createDeclareFlexoRoleInIteration2 = CreateEditionAction.actionType.makeNewAction(iteration, null, editor);
		createDeclareFlexoRoleInIteration2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createDeclareFlexoRoleInIteration2.setBuiltInActionClass(DeclareFlexoRole.class);
		createDeclareFlexoRoleInIteration2.doAction();
		DeclareFlexoRole declareFlexoRoleInIteration2 = (DeclareFlexoRole) createDeclareFlexoRoleInIteration2.getNewEditionAction();
		assertNotNull(declareFlexoRoleInIteration2);
		declareFlexoRoleInIteration2.setAssignation(new DataBinding<Object>("anOtherBooleanInA"));
		declareFlexoRoleInIteration2.setObject(new DataBinding<Object>("fci.toString.substring(2,3) != aStringInA"));

		assertEquals(13, declareFlexoRoleInIteration2.getBindingModel().getBindingVariablesCount());
		assertNotNull(declareFlexoRoleInIteration2.getBindingModel().bindingVariableNamed("fci"));

		assertTrue(declareFlexoRoleInIteration2.getAssignation().isValid());
		assertTrue(declareFlexoRoleInIteration2.getObject().isValid());

		assertEquals(2, actionScheme.getActions().size());
		assertEquals(1, conditional1.getActions().size());
		assertEquals(2, conditional2.getActions().size());

		System.out.println("FML=\n" + actionScheme.getFMLRepresentation());

		// Test renaming iteratorName
		iteration.setIteratorName("iteratorHasChanged");
		assertNull(declareFlexoRoleInIteration2.getBindingModel().bindingVariableNamed("fci"));
		assertNotNull(declareFlexoRoleInIteration2.getBindingModel().bindingVariableNamed("iteratorHasChanged"));

		assertEquals("(iteratorHasChanged.toString.substring(2,3) != aStringInA)", declareFlexoRoleInIteration2.getObject().toString());
		assertTrue(declareFlexoRoleInIteration2.getObject().isValid());

		iteration.removeFromActions(declareFlexoRoleInIteration2);

		assertEquals(0, declareFlexoRoleInIteration2.getBindingModel().getBindingVariablesCount());

		conditional2.addToActions(declareFlexoRoleInIteration2);

		// Check the BindingModel has been set again
		assertEquals(12, conditional2.getBindingModel().getBindingVariablesCount());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				conditional2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), conditional2.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), conditional2.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, conditional2.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.TYPE, conditional2.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, conditional2.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.TYPE, conditional2.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));

		conditional2.removeFromActions(declareFlexoRoleInIteration2);
		iteration.addToActions(declareFlexoRoleInIteration2);

	}

	@Test
	@TestOrder(14)
	public void testFetchRequestBindingModelManagement() throws SaveResourceException {

		// We programmatically implement this code:
		// ActionScheme testFetchRequest(String aString, Boolean aBoolean) {
		// ... SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = parameters.aBoolean; selected.aStringInA = parameters.aString) {
		// ......}
		// ...}
		// }

		CreateFlexoBehaviour createActionScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createActionScheme.setFlexoBehaviourName("testFetchRequest");
		createActionScheme.setFlexoBehaviourClass(ActionScheme.class);
		createActionScheme.doAction();
		ActionScheme actionScheme = (ActionScheme) createActionScheme.getNewFlexoBehaviour();
		assertNotNull(actionScheme);

		CreateFlexoBehaviourParameter createStringParameter = CreateFlexoBehaviourParameter.actionType.makeNewAction(actionScheme, null,
				editor);
		createStringParameter.setFlexoBehaviourParameterClass(TextFieldParameter.class);
		createStringParameter.setParameterName("aString");
		createStringParameter.doAction();
		FlexoBehaviourParameter param1 = createStringParameter.getNewParameter();
		assertNotNull(param1);
		assertTrue(actionScheme.getParameters().contains(param1));

		CreateFlexoBehaviourParameter createBooleanParameter = CreateFlexoBehaviourParameter.actionType.makeNewAction(actionScheme, null,
				editor);
		createBooleanParameter.setFlexoBehaviourParameterClass(CheckboxParameter.class);
		createBooleanParameter.setParameterName("aBoolean");
		createBooleanParameter.doAction();
		FlexoBehaviourParameter param2 = createBooleanParameter.getNewParameter();
		assertNotNull(param2);
		assertTrue(actionScheme.getParameters().contains(param2));

		assertEquals(12, actionScheme.getBindingModel().getBindingVariablesCount());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), actionScheme.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), actionScheme.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, actionScheme.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.TYPE, actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.TYPE, actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));

		CreateEditionAction createSelectFlexoConceptInstanceAction = CreateEditionAction.actionType.makeNewAction(actionScheme, null,
				editor);
		createSelectFlexoConceptInstanceAction.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createSelectFlexoConceptInstanceAction.setBuiltInActionClass(SelectFlexoConceptInstance.class);
		createSelectFlexoConceptInstanceAction.doAction();
		SelectFlexoConceptInstance selectFlexoConceptInstance = (SelectFlexoConceptInstance) createSelectFlexoConceptInstanceAction
				.getNewEditionAction();
		selectFlexoConceptInstance.setFlexoConceptType(flexoConceptA);

		FetchRequestCondition condition1 = selectFlexoConceptInstance.createCondition();
		condition1.setCondition(new DataBinding<Boolean>("selected.aBooleanInA = parameters.aBoolean"));

		FetchRequestCondition condition2 = selectFlexoConceptInstance.createCondition();
		condition2.setCondition(new DataBinding<Boolean>("selected.aStringInA = parameters.aString"));

		System.out.println("FML: " + actionScheme.getFMLRepresentation());
		System.out.println("BM: " + selectFlexoConceptInstance.getBindingModel());
		System.out.println("BM2: " + condition1.getBindingModel());

		assertEquals(12, selectFlexoConceptInstance.getBindingModel().getBindingVariablesCount());
		assertEquals(13, condition1.getBindingModel().getBindingVariablesCount());
		assertNotNull(condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), condition1.getBindingModel()
				.bindingVariableNamed(FetchRequestCondition.SELECTED).getType());

	}

	@Test
	@TestOrder(15)
	public void testFetchRequestIterationBindingModelManagement() throws SaveResourceException {

		// We programmatically implement this code:
		// ActionScheme testFetchRequestIteration(String aString, Boolean aBoolean) {
		// ... for (item in SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = parameters.aBoolean; selected.aStringInA = parameters.aString)) {
		// .........name = item.aStringInA;
		// .........item.aStringInA = (name + "foo");
		// ......}
		// ...}
		// }

		CreateFlexoBehaviour createActionScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createActionScheme.setFlexoBehaviourName("testFetchRequestIteration");
		createActionScheme.setFlexoBehaviourClass(ActionScheme.class);
		createActionScheme.doAction();
		ActionScheme actionScheme = (ActionScheme) createActionScheme.getNewFlexoBehaviour();
		assertNotNull(actionScheme);

		CreateFlexoBehaviourParameter createStringParameter = CreateFlexoBehaviourParameter.actionType.makeNewAction(actionScheme, null,
				editor);
		createStringParameter.setFlexoBehaviourParameterClass(TextFieldParameter.class);
		createStringParameter.setParameterName("aString");
		createStringParameter.doAction();
		FlexoBehaviourParameter param1 = createStringParameter.getNewParameter();
		assertNotNull(param1);
		assertTrue(actionScheme.getParameters().contains(param1));

		CreateFlexoBehaviourParameter createBooleanParameter = CreateFlexoBehaviourParameter.actionType.makeNewAction(actionScheme, null,
				editor);
		createBooleanParameter.setFlexoBehaviourParameterClass(CheckboxParameter.class);
		createBooleanParameter.setParameterName("aBoolean");
		createBooleanParameter.doAction();
		FlexoBehaviourParameter param2 = createBooleanParameter.getNewParameter();
		assertNotNull(param2);
		assertTrue(actionScheme.getParameters().contains(param2));

		assertEquals(12, actionScheme.getBindingModel().getBindingVariablesCount());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), actionScheme.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), actionScheme.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, actionScheme.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.TYPE, actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.TYPE, actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));

		CreateEditionAction createSelectFetchRequestIterationAction = CreateEditionAction.actionType.makeNewAction(actionScheme, null,
				editor);
		createSelectFetchRequestIterationAction.actionChoice = CreateEditionActionChoice.ControlAction;
		createSelectFetchRequestIterationAction.setControlActionClass(FetchRequestIterationAction.class);
		createSelectFetchRequestIterationAction.setRequestActionClass(SelectFlexoConceptInstance.class);
		createSelectFetchRequestIterationAction.doAction();
		FetchRequestIterationAction fetchRequestIteration = (FetchRequestIterationAction) createSelectFetchRequestIterationAction
				.getNewEditionAction();

		SelectFlexoConceptInstance selectFlexoConceptInstance = (SelectFlexoConceptInstance) fetchRequestIteration.getFetchRequest();
		selectFlexoConceptInstance.setFlexoConceptType(flexoConceptA);

		FetchRequestCondition condition1 = selectFlexoConceptInstance.createCondition();
		condition1.setCondition(new DataBinding<Boolean>("selected.aBooleanInA = parameters.aBoolean"));

		FetchRequestCondition condition2 = selectFlexoConceptInstance.createCondition();
		condition2.setCondition(new DataBinding<Boolean>("selected.aStringInA = parameters.aString"));

		assertEquals(12, fetchRequestIteration.getBindingModel().getBindingVariablesCount());
		assertEquals(13, condition1.getBindingModel().getBindingVariablesCount());
		assertNotNull(condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), condition1.getBindingModel()
				.bindingVariableNamed(FetchRequestCondition.SELECTED).getType());

		CreateEditionAction createAssignationAction = CreateEditionAction.actionType.makeNewAction(fetchRequestIteration, null, editor);
		createAssignationAction.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createAssignationAction.setBuiltInActionClass(AssignationAction.class);
		createAssignationAction.doAction();
		AssignationAction assignation1 = (AssignationAction) createAssignationAction.getNewEditionAction();
		assignation1.setIsVariableDeclaration(true);
		assignation1.setVariableName("name");
		assignation1.setValue(new DataBinding<Object>("item.aStringInA"));

		CreateEditionAction createAssignationAction2 = CreateEditionAction.actionType.makeNewAction(fetchRequestIteration, null, editor);
		createAssignationAction2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createAssignationAction2.setBuiltInActionClass(AssignationAction.class);
		createAssignationAction2.doAction();
		AssignationAction assignation2 = (AssignationAction) createAssignationAction2.getNewEditionAction();
		assignation2.setAssignation(new DataBinding<Object>("item.aStringInA"));
		assignation2.setValue(new DataBinding<Object>("name+\"foo\""));

		assertTrue(assignation1.getValue().isValid());
		assertTrue(assignation2.getAssignation().isValid());
		assertTrue(assignation2.getValue().isValid());

		assertEquals(12, fetchRequestIteration.getBindingModel().getBindingVariablesCount());

		assertEquals(13, condition1.getBindingModel().getBindingVariablesCount());
		assertNotNull(condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), condition1.getBindingModel()
				.bindingVariableNamed(FetchRequestCondition.SELECTED).getType());

		assertEquals(14, assignation1.getBindingModel().getBindingVariablesCount());
		assertNull(assignation1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertNotNull(assignation1.getBindingModel().bindingVariableNamed(fetchRequestIteration.getIteratorName()));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), assignation1.getBindingModel()
				.bindingVariableNamed(fetchRequestIteration.getIteratorName()).getType());
		assertNotNull(assignation1.getBindingModel().bindingVariableNamed("name"));
		assertEquals(String.class, assignation1.getBindingModel().bindingVariableNamed("name").getType());

		// System.out.println("BM1: " + assignation1.getBindingModel());
		// System.out.println("BM2: " + assignation2.getBindingModel());

		fetchRequestIteration.setIteratorName("myConceptInstance");
		assertNotNull(assignation1.getBindingModel().bindingVariableNamed("myConceptInstance"));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), assignation1.getBindingModel()
				.bindingVariableNamed("myConceptInstance").getType());

		assertTrue(assignation1.getValue().isValid());
		assertTrue(assignation2.getAssignation().isValid());
		assertTrue(assignation2.getValue().isValid());

		// System.out.println("FML: " + actionScheme.getFMLRepresentation());

		assertEquals("myConceptInstance.aStringInA", assignation2.getAssignation().toString());

	}

	@Test
	@TestOrder(16)
	public void testMatchFlexoConceptInstanceBindingModelManagement() throws SaveResourceException {

		// We programmatically implement this code:
		// ActionScheme testFetchRequestIteration(String aString, Boolean aBoolean) {
		// ... for (item in SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = parameters.aBoolean; selected.aStringInA = parameters.aString)) {
		// .........name = item.aStringInA;
		// .........item.aStringInA = (name + "foo");
		// ......}
		// ...}
		// }

		CreateFlexoBehaviour createActionScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createActionScheme.setFlexoBehaviourName("testFetchRequestIteration");
		createActionScheme.setFlexoBehaviourClass(ActionScheme.class);
		createActionScheme.doAction();
		ActionScheme actionScheme = (ActionScheme) createActionScheme.getNewFlexoBehaviour();
		assertNotNull(actionScheme);

		CreateFlexoBehaviourParameter createStringParameter = CreateFlexoBehaviourParameter.actionType.makeNewAction(actionScheme, null,
				editor);
		createStringParameter.setFlexoBehaviourParameterClass(TextFieldParameter.class);
		createStringParameter.setParameterName("aString");
		createStringParameter.doAction();
		FlexoBehaviourParameter param1 = createStringParameter.getNewParameter();
		assertNotNull(param1);
		assertTrue(actionScheme.getParameters().contains(param1));

		CreateFlexoBehaviourParameter createBooleanParameter = CreateFlexoBehaviourParameter.actionType.makeNewAction(actionScheme, null,
				editor);
		createBooleanParameter.setFlexoBehaviourParameterClass(CheckboxParameter.class);
		createBooleanParameter.setParameterName("aBoolean");
		createBooleanParameter.doAction();
		FlexoBehaviourParameter param2 = createBooleanParameter.getNewParameter();
		assertNotNull(param2);
		assertTrue(actionScheme.getParameters().contains(param2));

		assertEquals(12, actionScheme.getBindingModel().getBindingVariablesCount());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), actionScheme.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), actionScheme.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, actionScheme.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.TYPE, actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.TYPE, actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));

		CreateEditionAction createSelectFetchRequestIterationAction = CreateEditionAction.actionType.makeNewAction(actionScheme, null,
				editor);
		createSelectFetchRequestIterationAction.actionChoice = CreateEditionActionChoice.ControlAction;
		createSelectFetchRequestIterationAction.setControlActionClass(FetchRequestIterationAction.class);
		createSelectFetchRequestIterationAction.setRequestActionClass(SelectFlexoConceptInstance.class);
		createSelectFetchRequestIterationAction.doAction();
		FetchRequestIterationAction fetchRequestIteration = (FetchRequestIterationAction) createSelectFetchRequestIterationAction
				.getNewEditionAction();

		SelectFlexoConceptInstance selectFlexoConceptInstance = (SelectFlexoConceptInstance) fetchRequestIteration.getFetchRequest();
		selectFlexoConceptInstance.setFlexoConceptType(flexoConceptA);

		FetchRequestCondition condition1 = selectFlexoConceptInstance.createCondition();
		condition1.setCondition(new DataBinding<Boolean>("selected.aBooleanInA = parameters.aBoolean"));

		FetchRequestCondition condition2 = selectFlexoConceptInstance.createCondition();
		condition2.setCondition(new DataBinding<Boolean>("selected.aStringInA = parameters.aString"));

		CreateEditionAction createMatchFlexoConceptInstanceAction = CreateEditionAction.actionType.makeNewAction(fetchRequestIteration,
				null, editor);
		createMatchFlexoConceptInstanceAction.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createMatchFlexoConceptInstanceAction.setBuiltInActionClass(MatchFlexoConceptInstance.class);
		createMatchFlexoConceptInstanceAction.doAction();
		MatchFlexoConceptInstance matchFlexoConceptInstance = (MatchFlexoConceptInstance) createMatchFlexoConceptInstanceAction
				.getNewEditionAction();
		matchFlexoConceptInstance.setFlexoConceptType(flexoConceptA);

		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme.setFlexoBehaviourName("creationScheme2");
		createCreationScheme.doAction();
		CreationScheme creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();

		CreateFlexoBehaviourParameter createStringParameter2 = CreateFlexoBehaviourParameter.actionType.makeNewAction(creationScheme, null,
				editor);
		createStringParameter2.setFlexoBehaviourParameterClass(TextFieldParameter.class);
		createStringParameter2.setParameterName("aStringParameter");
		createStringParameter2.doAction();
		FlexoBehaviourParameter creationSchemeParam1 = createStringParameter2.getNewParameter();
		assertNotNull(creationSchemeParam1);
		assertTrue(creationScheme.getParameters().contains(creationSchemeParam1));

		CreateFlexoBehaviourParameter createBooleanParameter2 = CreateFlexoBehaviourParameter.actionType.makeNewAction(creationScheme,
				null, editor);
		createBooleanParameter2.setFlexoBehaviourParameterClass(CheckboxParameter.class);
		createBooleanParameter2.setParameterName("aBooleanParameter");
		createBooleanParameter2.doAction();
		FlexoBehaviourParameter creationSchemeParam2 = createBooleanParameter2.getNewParameter();
		assertNotNull(creationSchemeParam2);
		assertTrue(creationScheme.getParameters().contains(creationSchemeParam2));

		CreateEditionAction createEditionAction1 = CreateEditionAction.actionType.makeNewAction(creationScheme, null, editor);
		createEditionAction1.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction1.setBuiltInActionClass(DeclareFlexoRole.class);
		createEditionAction1.doAction();
		DeclareFlexoRole action1 = (DeclareFlexoRole) createEditionAction1.getNewEditionAction();
		action1.setAssignation(new DataBinding<Object>("aStringInA"));
		action1.setObject(new DataBinding<Object>("parameters.aStringParameter"));

		assertTrue(action1.getAssignation().isValid());
		assertTrue(action1.getObject().isValid());

		CreateEditionAction createEditionAction2 = CreateEditionAction.actionType.makeNewAction(creationScheme, null, editor);
		createEditionAction2.actionChoice = CreateEditionActionChoice.BuiltInAction;
		createEditionAction2.setBuiltInActionClass(DeclareFlexoRole.class);
		createEditionAction2.doAction();
		DeclareFlexoRole action2 = (DeclareFlexoRole) createEditionAction2.getNewEditionAction();
		action2.setAssignation(new DataBinding<Object>("aBooleanInA"));
		action2.setObject(new DataBinding<Object>("parameters.aBooleanParameter"));

		assertTrue(action2.getAssignation().isValid());
		assertTrue(action2.getObject().isValid());

		assertNotNull(creationScheme);
		System.out.println("FML=" + creationScheme.getFMLRepresentation());

		matchFlexoConceptInstance.setCreationScheme(creationScheme);

		// We check here that matching criterias were updated
		assertEquals(4, matchFlexoConceptInstance.getMatchingCriterias().size());

		MatchingCriteria criteria1 = matchFlexoConceptInstance.getMatchingCriteria(flexoConceptA.getFlexoRole("aStringInA"));
		MatchingCriteria criteria2 = matchFlexoConceptInstance.getMatchingCriteria(flexoConceptA.getFlexoRole("aBooleanInA"));
		MatchingCriteria criteria3 = matchFlexoConceptInstance.getMatchingCriteria(flexoConceptA.getFlexoRole("anIntegerInA"));
		MatchingCriteria criteria4 = matchFlexoConceptInstance.getMatchingCriteria(flexoConceptA.getFlexoRole("anOtherBooleanInA"));

		assertNotNull(criteria1);
		assertNotNull(criteria2);
		assertNotNull(criteria3);
		assertNotNull(criteria4);

		criteria1.setValue(new DataBinding<Object>("item.aStringInA"));
		assertTrue(criteria1.getValue().isValid());

		MatchingCriteria criteria1bis = matchFlexoConceptInstance.getMatchingCriteria(flexoConceptA.getFlexoRole("aStringInA"));
		assertSame(criteria1, criteria1bis);

		// We add a role
		// We check here that matching criterias were updated: an other criteria should appear

		CreateFlexoRole createRole = CreateFlexoRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createRole.setRoleName("anOtherIntegerInA");
		createRole.setFlexoRoleClass(PrimitiveRole.class);
		createRole.setPrimitiveType(PrimitiveType.Integer);
		createRole.doAction();
		FlexoRole newRole = createRole.getNewFlexoRole();

		assertEquals(5, matchFlexoConceptInstance.getMatchingCriterias().size());
		assertNotNull(matchFlexoConceptInstance.getMatchingCriteria(newRole));

		// We remove the role
		// We check here that matching criterias were updated: the criteria should disappear
		flexoConceptA.removeFromFlexoRoles(newRole);

		assertEquals(4, matchFlexoConceptInstance.getMatchingCriterias().size());

		// We check here that create parameters were updated

		assertEquals(2, matchFlexoConceptInstance.getParameters().size());

		CreateFlexoConceptInstanceParameter createFCIParam1 = matchFlexoConceptInstance.getParameter(creationSchemeParam1);
		CreateFlexoConceptInstanceParameter createFCIParam2 = matchFlexoConceptInstance.getParameter(creationSchemeParam2);
		assertNotNull(createFCIParam1);
		assertNotNull(createFCIParam2);

		createFCIParam1.setValue(new DataBinding<Object>("item.aStringInA"));
		createFCIParam2.setValue(new DataBinding<Object>("true"));
		assertTrue(createFCIParam1.getValue().isValid());
		assertTrue(createFCIParam2.getValue().isValid());

		// WE change creation scheme, parameters should disappear
		matchFlexoConceptInstance.setCreationScheme(null);

		assertEquals(0, matchFlexoConceptInstance.getParameters().size());

		// We set again the creation scheme, parameters should come back
		matchFlexoConceptInstance.setCreationScheme(creationScheme);
		assertEquals(2, matchFlexoConceptInstance.getParameters().size());
		createFCIParam1 = matchFlexoConceptInstance.getParameter(creationSchemeParam1);
		createFCIParam2 = matchFlexoConceptInstance.getParameter(creationSchemeParam2);
		createFCIParam1.setValue(new DataBinding<Object>("item.aStringInA"));
		createFCIParam2.setValue(new DataBinding<Object>("true"));
		assertTrue(createFCIParam1.getValue().isValid());
		assertTrue(createFCIParam2.getValue().isValid());

		// We try to add a parameter
		CreateFlexoBehaviourParameter createBooleanParameter3 = CreateFlexoBehaviourParameter.actionType.makeNewAction(creationScheme,
				null, editor);
		createBooleanParameter3.setFlexoBehaviourParameterClass(CheckboxParameter.class);
		createBooleanParameter3.setParameterName("anOtherBooleanParameter");
		createBooleanParameter3.doAction();
		FlexoBehaviourParameter creationSchemeParam3 = createBooleanParameter3.getNewParameter();
		assertNotNull(creationSchemeParam3);
		assertTrue(creationScheme.getParameters().contains(creationSchemeParam3));
		assertEquals(3, matchFlexoConceptInstance.getParameters().size());

		// We remove it
		creationScheme.removeFromParameters(creationSchemeParam3);
		assertEquals(2, matchFlexoConceptInstance.getParameters().size());

		assertEquals(12, fetchRequestIteration.getBindingModel().getBindingVariablesCount());

		assertEquals(13, condition1.getBindingModel().getBindingVariablesCount());

		assertEquals(13, createFCIParam1.getBindingModel().getBindingVariablesCount());

		System.out.println("FML: " + actionScheme.getFMLRepresentation());

	}

	@Test
	@TestOrder(17)
	public void testFlexoConceptInstanceInspector() {

		assertSame(flexoConceptA.getBindingModel(), flexoConceptA.getInspector().getBindingModel().getBaseBindingModel());

		assertEquals(10, flexoConceptA.getInspector().getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getInspector().getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getInspector().getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getInspector().getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptA.getInspector().getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptA.getInspector().getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getInspector().getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), flexoConceptA.getInspector().getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptA.getInspector().getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), flexoConceptA.getInspector().getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.TYPE, flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.TYPE, flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
	}

	@Test
	@TestOrder(18)
	public void testFlexoConceptInstanceRenderer() {

		flexoConceptA.getInspector().setRenderer(new DataBinding<String>("\"FlexoConceptA:\"+instance.aStringInA"));
		assertTrue(flexoConceptA.getInspector().getRenderer().isValid());

		assertEquals(11, flexoConceptA.getInspector().getFormatter().getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel()
				.bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY)
						.getType());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), flexoConceptA.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), flexoConceptA.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("aStringInA")
				.getType());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.TYPE, flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("aBooleanInA")
				.getType());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("anIntegerInA")
				.getType());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.TYPE, flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("anOtherBooleanInA")
				.getType());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel()
				.bindingVariableNamed(FlexoConceptInspector.FORMATTER_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), flexoConceptA.getInspector().getFormatter()
				.getBindingModel().bindingVariableNamed(FlexoConceptInspector.FORMATTER_INSTANCE_PROPERTY).getType());

	}

	@Test
	@TestOrder(19)
	public void testViewPointIsValid() {

		assertViewPointIsValid(viewPoint);

	}

	@Test
	@TestOrder(20)
	public void testInstanciateVirtualModelInstances() {

		log("testInstanciateVirtualModelInstances()");

		editor = createProject("TestProject");
		project = editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
		assertTrue(project.getProjectDataResource().getFile().exists());

		CreateView action = CreateView.actionType.makeNewAction(project.getViewLibrary().getRootFolder(), null, editor);
		action.setNewViewName("MyView");
		action.setNewViewTitle("Test creation of a new view");
		action.setViewpointResource((ViewPointResource) viewPoint.getResource());
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		newView = action.getNewView();
		assertNotNull(newView);
		assertNotNull(newView.getResource());
		try {
			newView.getResource().save(null);
		} catch (SaveResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(((ViewResource) newView.getResource()).getDirectory().exists());
		assertTrue(((ViewResource) newView.getResource()).getFile().exists());

		assertNotNull(project.getResource(newView.getURI()));
		assertNotNull(project.getViewLibrary().getResource(newView.getURI()));

		CreateBasicVirtualModelInstance createVMI1 = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		createVMI1.setNewVirtualModelInstanceName("MyVirtualModelInstance1");
		createVMI1.setNewVirtualModelInstanceTitle("Test creation of a new VirtualModelInstance 1");
		createVMI1.setVirtualModel(virtualModel1);
		createVMI1.doAction();
		assertTrue(createVMI1.hasActionExecutionSucceeded());
		vmi1 = createVMI1.getNewVirtualModelInstance();
		assertNotNull(vmi1);
		assertNotNull(vmi1.getResource());
		assertTrue(((ViewResource) newView.getResource()).getDirectory().exists());
		assertTrue(((ViewResource) newView.getResource()).getFile().exists());
		assertEquals(virtualModel1, vmi1.getFlexoConcept());
		assertEquals(virtualModel1, vmi1.getVirtualModel());

		CreateBasicVirtualModelInstance createVMI2 = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		createVMI2.setNewVirtualModelInstanceName("MyVirtualModelInstance2");
		createVMI2.setNewVirtualModelInstanceTitle("Test creation of a new VirtualModelInstance 2");
		createVMI2.setVirtualModel(virtualModel2);
		createVMI2.doAction();
		assertTrue(createVMI2.hasActionExecutionSucceeded());
		vmi2 = createVMI2.getNewVirtualModelInstance();
		assertNotNull(vmi2);
		assertNotNull(vmi2.getResource());
		assertTrue(((ViewResource) newView.getResource()).getDirectory().exists());
		assertTrue(((ViewResource) newView.getResource()).getFile().exists());
		assertEquals(virtualModel2, vmi2.getFlexoConcept());
		assertEquals(virtualModel2, vmi2.getVirtualModel());

		CreateBasicVirtualModelInstance createVMI3 = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		createVMI3.setNewVirtualModelInstanceName("MyVirtualModelInstance3");
		createVMI3.setNewVirtualModelInstanceTitle("Test creation of a new VirtualModelInstance 3");
		createVMI3.setVirtualModel(virtualModel3);

		VirtualModelModelSlot ms1 = (VirtualModelModelSlot) virtualModel3.getModelSlot("vm1");
		VirtualModelModelSlotInstanceConfiguration ms1Configuration = (VirtualModelModelSlotInstanceConfiguration) createVMI3
				.getModelSlotInstanceConfiguration(ms1);
		ms1Configuration.setOption(DefaultModelSlotInstanceConfigurationOption.SelectExistingVirtualModel);
		ms1Configuration.setAddressedVirtualModelInstanceResource((VirtualModelInstanceResource) vmi1.getResource());
		assertTrue(ms1Configuration.isValidConfiguration());

		VirtualModelModelSlot ms2 = (VirtualModelModelSlot) virtualModel3.getModelSlot("vm2");
		VirtualModelModelSlotInstanceConfiguration ms2Configuration = (VirtualModelModelSlotInstanceConfiguration) createVMI3
				.getModelSlotInstanceConfiguration(ms2);
		ms2Configuration.setOption(DefaultModelSlotInstanceConfigurationOption.SelectExistingVirtualModel);
		ms2Configuration.setAddressedVirtualModelInstanceResource((VirtualModelInstanceResource) vmi2.getResource());
		assertTrue(ms2Configuration.isValidConfiguration());

		createVMI3.doAction();
		assertTrue(createVMI3.hasActionExecutionSucceeded());
		vmi3 = createVMI3.getNewVirtualModelInstance();
		assertNotNull(vmi3);
		assertNotNull(vmi3.getResource());
		assertTrue(((ViewResource) newView.getResource()).getDirectory().exists());
		assertTrue(((ViewResource) newView.getResource()).getFile().exists());
		assertEquals(virtualModel3, vmi3.getFlexoConcept());
		assertEquals(virtualModel3, vmi3.getVirtualModel());

		assertNotNull(virtualModel3.getBindingModel());
		assertEquals(7, virtualModel3.getBindingModel().getBindingVariablesCount());

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY, virtualModel3, vmi3, viewPoint);

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY, virtualModel3, vmi3, virtualModel3);

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		checkBindingVariableAccess(VirtualModelBindingModel.VIEW_PROPERTY, virtualModel3, vmi3, newView);

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel3), virtualModel3.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		checkBindingVariableAccess(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY, virtualModel3, vmi3, vmi3);

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("vm1"));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1), virtualModel3.getBindingModel()
				.bindingVariableNamed("vm1").getType());
		checkBindingVariableAccess("vm1", virtualModel3, vmi3, vmi1);

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("vm2"));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel2), virtualModel3.getBindingModel()
				.bindingVariableNamed("vm2").getType());
		checkBindingVariableAccess("vm2", virtualModel3, vmi3, vmi2);

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("aStringInVM3"));
		assertEquals(String.class, virtualModel3.getBindingModel().bindingVariableNamed("aStringInVM3").getType());
		checkBindingVariableAccess("aStringInVM3", virtualModel3, vmi3, null);

		vmi3.setFlexoActor("toto", (FlexoRole) vmi3.getVirtualModel().getFlexoRole("aStringInVM3"));
		checkBindingVariableAccess("aStringInVM3", virtualModel3, vmi3, "toto");

	}

	@Test
	@TestOrder(21)
	public void testInstanciateFlexoConceptInstance() {

		log("testInstanciateFlexoConceptInstance()");

		CreationScheme creationScheme = flexoConceptA.getFlexoBehaviours(CreationScheme.class).get(0);
		assertNotNull(creationScheme);

		CreationSchemeAction creationSchemeCreationAction = CreationSchemeAction.actionType.makeNewAction(vmi1, null, editor);
		creationSchemeCreationAction.setCreationScheme(creationScheme);
		assertNotNull(creationSchemeCreationAction);
		creationSchemeCreationAction.doAction();
		assertTrue(creationSchemeCreationAction.hasActionExecutionSucceeded());

		fci = creationSchemeCreationAction.getFlexoConceptInstance();
		assertNotNull(fci);
		assertEquals(flexoConceptA, fci.getFlexoConcept());
		assertEquals("foo", fci.getFlexoActor("aStringInA"));
		assertEquals(true, fci.getFlexoActor("aBooleanInA"));
		assertEquals((long) 8, fci.getFlexoActor("anIntegerInA"));

		fci.setFlexoActor(false, (FlexoRole<Boolean>) flexoConceptA.getFlexoRole("anOtherBooleanInA"));

		assertEquals(10, flexoConceptA.getBindingModel().getBindingVariablesCount());

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY, flexoConceptA, fci, viewPoint);

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY, flexoConceptA, fci, virtualModel1);

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		checkBindingVariableAccess(VirtualModelBindingModel.VIEW_PROPERTY, flexoConceptA, fci, newView);

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY, flexoConceptA, fci, flexoConceptA);

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), flexoConceptA.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		checkBindingVariableAccess(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY, flexoConceptA, fci, vmi1);

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), flexoConceptA.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
		checkBindingVariableAccess(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY, flexoConceptA, fci, fci);

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConceptA.getBindingModel().bindingVariableNamed("aStringInA").getType());
		checkBindingVariableAccess("aStringInA", flexoConceptA, fci, "foo");

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.TYPE, flexoConceptA.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		checkBindingVariableAccess("aBooleanInA", flexoConceptA, fci, true);

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, flexoConceptA.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		checkBindingVariableAccess("anIntegerInA", flexoConceptA, fci, (long) 8);

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.TYPE, flexoConceptA.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		checkBindingVariableAccess("anOtherBooleanInA", flexoConceptA, fci, false);

		checkBinding("flexoConceptInstance", flexoConceptA, fci, fci);
		checkBinding("virtualModelInstance", flexoConceptA, fci, vmi1);

		checkBinding("virtualModelInstance", virtualModel3, vmi3, vmi3);
		checkBinding("virtualModelInstance.vm1", virtualModel3, vmi3, vmi1);
		checkBinding("virtualModelInstance.vm2", virtualModel3, vmi3, vmi2);
		checkBinding("virtualModelInstance.vm1.flexoConceptInstances.size", virtualModel3, vmi3, (long) 1);
		checkBinding("virtualModelInstance.vm1.flexoConceptInstances.get(0)", virtualModel3, vmi3, fci);

		assertTrue(fci.hasValidRenderer());
		assertEquals("FlexoConceptA:foo", fci.getStringRepresentation());

	}

	@Test
	@TestOrder(22)
	public void testFlexoBehaviourAtRunTime() {

		log("testFlexoBehaviourAtRunTime()");

		fci.setFlexoActor("newValue", (FlexoRole<String>) flexoConceptA.getFlexoRole("aStringInA"));
		assertEquals("newValue", fci.getFlexoActor("aStringInA"));

		ActionScheme actionScheme = flexoConceptA.getFlexoBehaviours(ActionScheme.class).get(0);
		assertNotNull(actionScheme);

		ActionSchemeActionType actionType = new ActionSchemeActionType(actionScheme, fci);

		ActionSchemeAction actionSchemeCreationAction = actionType.makeNewAction(fci, null, editor);
		assertNotNull(actionSchemeCreationAction);
		FlexoBehaviourParameter p = actionScheme.getParameter("aFlag");
		actionSchemeCreationAction.setParameterValue(p, false);
		actionSchemeCreationAction.doAction();
		assertTrue(actionSchemeCreationAction.hasActionExecutionSucceeded());

		assertEquals("foo", fci.getFlexoActor("aStringInA"));
		assertEquals((long) 12, fci.getFlexoActor("anIntegerInA"));

		assertEquals(12, actionScheme.getBindingModel().getBindingVariablesCount());

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY, actionScheme, actionSchemeCreationAction, viewPoint);

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY, actionScheme, actionSchemeCreationAction,
				virtualModel1);

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		checkBindingVariableAccess(VirtualModelBindingModel.VIEW_PROPERTY, actionScheme, actionSchemeCreationAction, newView);

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY, actionScheme, actionSchemeCreationAction,
				flexoConceptA);

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), actionScheme.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		checkBindingVariableAccess(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY, actionScheme, actionSchemeCreationAction, vmi1);

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), actionScheme.getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY).getType());
		checkBindingVariableAccess(FlexoConceptBindingModel.FLEXO_CONCEPT_INSTANCE_PROPERTY, actionScheme, actionSchemeCreationAction, fci);

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, actionScheme.getBindingModel().bindingVariableNamed("aStringInA").getType());
		checkBindingVariableAccess("aStringInA", actionScheme, actionSchemeCreationAction, "foo");

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.TYPE, actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		checkBindingVariableAccess("aBooleanInA", actionScheme, actionSchemeCreationAction, true);

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		checkBindingVariableAccess("anIntegerInA", actionScheme, actionSchemeCreationAction, (long) 12);

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.TYPE, actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		checkBindingVariableAccess("anOtherBooleanInA", actionScheme, actionSchemeCreationAction, true);

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		checkBindingVariableAccess(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY, actionScheme, actionSchemeCreationAction,
				actionSchemeCreationAction.getParametersValues());

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));
		checkBindingVariableAccess(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY, actionScheme, actionSchemeCreationAction,
				actionScheme.getParameters());
	}

	private void checkBindingVariableAccess(String variableName, Bindable owner, BindingEvaluationContext beContext, Object expectedValue) {
		BindingVariable bv = owner.getBindingModel().bindingVariableNamed(variableName);
		assertNotNull(bv);
		DataBinding<Object> db = new DataBinding<Object>(bv.getVariableName(), owner, bv.getType(), BindingDefinitionType.GET);
		assertTrue(db.isValid());
		try {
			assertEquals(expectedValue, db.getBindingValue(beContext));
		} catch (TypeMismatchException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NullReferenceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void checkBinding(String binding, Bindable owner, BindingEvaluationContext beContext, Object expectedValue) {
		DataBinding<Object> db = new DataBinding<Object>(binding, owner, Object.class, BindingDefinitionType.GET);
		assertTrue(db.isValid());
		try {
			assertEquals(expectedValue, db.getBindingValue(beContext));
		} catch (TypeMismatchException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NullReferenceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
