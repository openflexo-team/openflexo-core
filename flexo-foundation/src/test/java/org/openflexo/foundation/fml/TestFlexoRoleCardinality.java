/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.foundation.fml.PrimitiveRole.PrimitiveType;
import org.openflexo.foundation.fml.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.fml.VirtualModel.VirtualModelImpl;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoRole;
import org.openflexo.foundation.fml.binding.ViewPointBindingModel;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateView;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test {@link BindingModel} management along FML model<br>
 * 
 * In this test we try to move a complete control graph from an iteration to another iteration, and we check that binding model management
 * is consistent
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestFlexoRoleCardinality extends OpenflexoProjectAtRunTimeTestCase {

	static FlexoEditor editor;
	static ViewPoint viewPoint;
	static VirtualModel virtualModel;

	static FlexoConcept flexoConcept1;
	static FlexoConcept flexoConcept2;

	static FlexoProject project;
	static View newView;
	static VirtualModelInstance vmi;
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
		// assertTrue(((ViewPointResource) viewPoint.getResource()).getDirectory().exists());
		// assertTrue(((ViewPointResource) viewPoint.getResource()).getFile().exists());
		assertTrue(((ViewPointResource) viewPoint.getResource()).getDirectory() != null);
		assertTrue(((ViewPointResource) viewPoint.getResource()).getFlexoIODelegate().exists());

		System.out.println("ViewPoint BindingModel = " + viewPoint.getBindingModel());
		assertNotNull(viewPoint.getBindingModel());
		assertEquals(2, viewPoint.getBindingModel().getBindingVariablesCount());
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));

	}

	/**
	 * Test {@link VirtualModel} creation, check {@link BindingModel}
	 */
	@Test
	@TestOrder(3)
	public void testCreateVirtualModel() throws SaveResourceException {

		virtualModel = VirtualModelImpl.newVirtualModel("VM1", viewPoint);
		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) virtualModel.getResource()).getDirectory()).exists());
		assertTrue(((VirtualModelResource) virtualModel.getResource()).getFlexoIODelegate().exists());

		assertNotNull(virtualModel.getBindingModel());
		assertEquals(4, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel), virtualModel.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

		// We disconnect VirtualModel from ViewPoint, and we check BindingModel evolution
		viewPoint.removeFromVirtualModels(virtualModel);
		System.out.println("VirtualModel BindingModel = " + virtualModel.getBindingModel());
		assertNotNull(virtualModel.getBindingModel());
		assertEquals(2, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		// assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1), virtualModel1.getBindingModel()
		// .bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

		// We reconnect VirtualModel again, and we check BindingModel evolution
		viewPoint.addToVirtualModels(virtualModel);
		System.out.println("VirtualModel BindingModel = " + virtualModel.getBindingModel());
		assertEquals(4, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel), virtualModel.getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

	}

	/**
	 * Test FlexoConcept creation, check BindingModel
	 */
	@Test
	@TestOrder(4)
	public void testCreateFlexoConcept() throws SaveResourceException {

		CreateFlexoConcept addEP1 = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addEP1.setNewFlexoConceptName("FlexoConcept1");
		addEP1.doAction();

		flexoConcept1 = addEP1.getNewFlexoConcept();

		System.out.println("flexoConcept1 = " + flexoConcept1);
		assertNotNull(flexoConcept1);

		CreateFlexoConcept addEP2 = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addEP2.setNewFlexoConceptName("FlexoConcept2");
		addEP2.doAction();

		flexoConcept2 = addEP2.getNewFlexoConcept();

		System.out.println("flexoConcept2 = " + flexoConcept2);
		assertNotNull(flexoConcept2);

		((VirtualModelResource) virtualModel.getResource()).save(null);

		CreateFlexoRole createPR1 = CreateFlexoRole.actionType.makeNewAction(flexoConcept1, null, editor);
		createPR1.setRoleName("aStringInA");
		createPR1.setFlexoRoleClass(PrimitiveRole.class);
		createPR1.setPrimitiveType(PrimitiveType.String);
		createPR1.setRoleCardinality(RoleCardinality.One);
		createPR1.doAction();

		CreateFlexoRole createPR2 = CreateFlexoRole.actionType.makeNewAction(flexoConcept1, null, editor);
		createPR2.setRoleName("someBooleanInA");
		createPR2.setFlexoRoleClass(PrimitiveRole.class);
		createPR2.setPrimitiveType(PrimitiveType.Boolean);
		createPR2.setRoleCardinality(RoleCardinality.ZeroMany);
		createPR2.doAction();

		CreateFlexoRole createPR3 = CreateFlexoRole.actionType.makeNewAction(flexoConcept1, null, editor);
		createPR3.setRoleName("someIntegerInA");
		createPR3.setFlexoRoleClass(PrimitiveRole.class);
		createPR3.setPrimitiveType(PrimitiveType.Integer);
		createPR3.setRoleCardinality(RoleCardinality.OneMany);
		createPR3.doAction();

		CreateFlexoRole createPR4 = CreateFlexoRole.actionType.makeNewAction(flexoConcept1, null, editor);
		createPR4.setRoleName("someFlexoConcept2");
		createPR4.setFlexoRoleClass(FlexoConceptInstanceRole.class);
		createPR4.setFlexoConceptInstanceType(flexoConcept2);
		createPR4.setRoleCardinality(RoleCardinality.ZeroMany);
		createPR4.doAction();

		assertEquals(4, flexoConcept1.getFlexoRoles().size());
		assertTrue(flexoConcept1.getFlexoRoles().contains(createPR1.getNewFlexoRole()));
		assertTrue(flexoConcept1.getFlexoRoles().contains(createPR2.getNewFlexoRole()));
		assertTrue(flexoConcept1.getFlexoRoles().contains(createPR3.getNewFlexoRole()));
		assertTrue(flexoConcept1.getFlexoRoles().contains(createPR4.getNewFlexoRole()));

		PrimitiveRole<String> aStringInA = (PrimitiveRole<String>) flexoConcept1.getFlexoRole("aStringInA");
		assertNotNull(aStringInA);
		assertEquals(String.class, aStringInA.getType());
		assertEquals(String.class, aStringInA.getResultingType());
		PrimitiveRole<Boolean> aBooleanInA = (PrimitiveRole<Boolean>) flexoConcept1.getFlexoRole("someBooleanInA");
		assertNotNull(aBooleanInA);
		assertEquals(Boolean.class, aBooleanInA.getType());
		assertEquals(new ParameterizedTypeImpl(List.class, Boolean.class), aBooleanInA.getResultingType());
		PrimitiveRole<Integer> anIntegerInA = (PrimitiveRole<Integer>) flexoConcept1.getFlexoRole("someIntegerInA");
		assertNotNull(anIntegerInA);
		assertEquals(Integer.class, anIntegerInA.getType());
		assertEquals(new ParameterizedTypeImpl(List.class, Integer.class), anIntegerInA.getResultingType());

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

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
		assertTrue(project.getProjectDataResource().getFlexoIODelegate().exists());

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
		// assertTrue(((ViewResource) newView.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource) newView.getResource()).getFile().exists());
		assertTrue(((ViewResource) newView.getResource()).getDirectory() != null);
		assertTrue(((ViewResource) newView.getResource()).getFlexoIODelegate().exists());

		assertNotNull(project.getResource(newView.getURI()));
		assertNotNull(project.getViewLibrary().getResource(newView.getURI()));

		CreateBasicVirtualModelInstance createVMI = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		createVMI.setNewVirtualModelInstanceName("MyVirtualModelInstance");
		createVMI.setNewVirtualModelInstanceTitle("Test creation of a new VirtualModelInstance");
		createVMI.setVirtualModel(virtualModel);
		createVMI.doAction();
		assertTrue(createVMI.hasActionExecutionSucceeded());
		vmi = createVMI.getNewVirtualModelInstance();
		assertNotNull(vmi);
		assertNotNull(vmi.getResource());
		// assertTrue(((ViewResource) newView.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource) newView.getResource()).getFile().exists());
		assertTrue(((ViewResource) newView.getResource()).getDirectory() != null);
		assertTrue(((ViewResource) newView.getResource()).getFlexoIODelegate().exists());
		assertEquals(virtualModel, vmi.getFlexoConcept());
		assertEquals(virtualModel, vmi.getVirtualModel());

		/*
		CreateBasicVirtualModelInstance createVMI2 = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		createVMI2.setNewVirtualModelInstanceName("MyVirtualModelInstance2");
		createVMI2.setNewVirtualModelInstanceTitle("Test creation of a new VirtualModelInstance 2");
		createVMI2.setVirtualModel(virtualModel2);
		createVMI2.doAction();
		assertTrue(createVMI2.hasActionExecutionSucceeded());
		vmi2 = createVMI2.getNewVirtualModelInstance();
		assertNotNull(vmi2);
		assertNotNull(vmi2.getResource());
		// assertTrue(((ViewResource) newView.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource) newView.getResource()).getFile().exists());
		assertTrue(((ViewResource) newView.getResource()).getDirectory() != null);
		assertTrue(((ViewResource) newView.getResource()).getFlexoIODelegate().exists());
		assertEquals(virtualModel2, vmi2.getFlexoConcept());
		assertEquals(virtualModel2, vmi2.getVirtualModel());

		CreateBasicVirtualModelInstance createVMI3 = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		createVMI3.setNewVirtualModelInstanceName("MyVirtualModelInstance3");
		createVMI3.setNewVirtualModelInstanceTitle("Test creation of a new VirtualModelInstance 3");
		createVMI3.setVirtualModel(virtualModel3);

		FMLRTModelSlot ms1 = (FMLRTModelSlot) virtualModel3.getModelSlot("vm1");
		FMLRTModelSlotInstanceConfiguration ms1Configuration = (FMLRTModelSlotInstanceConfiguration) createVMI3
				.getModelSlotInstanceConfiguration(ms1);
		ms1Configuration.setOption(DefaultModelSlotInstanceConfigurationOption.SelectExistingVirtualModel);
		ms1Configuration.setAddressedVirtualModelInstanceResource((VirtualModelInstanceResource) vmi1.getResource());
		assertTrue(ms1Configuration.isValidConfiguration());

		FMLRTModelSlot ms2 = (FMLRTModelSlot) virtualModel3.getModelSlot("vm2");
		FMLRTModelSlotInstanceConfiguration ms2Configuration = (FMLRTModelSlotInstanceConfiguration) createVMI3
				.getModelSlotInstanceConfiguration(ms2);
		ms2Configuration.setOption(DefaultModelSlotInstanceConfigurationOption.SelectExistingVirtualModel);
		ms2Configuration.setAddressedVirtualModelInstanceResource((VirtualModelInstanceResource) vmi2.getResource());
		assertTrue(ms2Configuration.isValidConfiguration());

		createVMI3.doAction();
		assertTrue(createVMI3.hasActionExecutionSucceeded());
		vmi3 = createVMI3.getNewVirtualModelInstance();
		assertNotNull(vmi3);
		assertNotNull(vmi3.getResource());
		// assertTrue(((ViewResource) newView.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource) newView.getResource()).getFile().exists());
		assertTrue(((ViewResource) newView.getResource()).getDirectory() != null);
		assertTrue(((ViewResource) newView.getResource()).getFlexoIODelegate().exists());
		assertEquals(virtualModel3, vmi3.getFlexoConcept());
		assertEquals(virtualModel3, vmi3.getVirtualModel());

		assertNotNull(virtualModel3.getBindingModel());
		assertEquals(7, virtualModel3.getBindingModel().getBindingVariablesCount());

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY, virtualModel3, vmi3, viewPoint);

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY, virtualModel3, vmi3, virtualModel3);

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel3.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		checkBindingVariableAccess(ViewPointBindingModel.VIEW_PROPERTY, virtualModel3, vmi3, newView);

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
		*/
	}

	/*@Test
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

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				flexoConceptA.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		checkBindingVariableAccess(ViewPointBindingModel.VIEW_PROPERTY, flexoConceptA, fci, newView);

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
		assertEquals(Boolean.class, flexoConceptA.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		checkBindingVariableAccess("aBooleanInA", flexoConceptA, fci, true);

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, flexoConceptA.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		checkBindingVariableAccess("anIntegerInA", flexoConceptA, fci, 8);

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class, flexoConceptA.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
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

	}*/

	/*@Test
	@TestOrder(22)
	public void testFlexoBehaviourAtRunTime() {

		log("testFlexoBehaviourAtRunTime()");

		fci.setFlexoActor("newValue", (FlexoRole<String>) flexoConceptA.getFlexoRole("aStringInA"));
		assertEquals("newValue", fci.getFlexoActor("aStringInA"));

		ActionScheme actionScheme = flexoConceptA.getFlexoBehaviours(ActionScheme.class).get(0);
		assertNotNull(actionScheme);

		System.out.println("Applying " + actionScheme.getFMLModelFactory().stringRepresentation(actionScheme));

		System.out.println("Soit en FML:\n" + actionScheme.getFMLRepresentation());

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

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				actionScheme.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		checkBindingVariableAccess(ViewPointBindingModel.VIEW_PROPERTY, actionScheme, actionSchemeCreationAction, newView);

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
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		checkBindingVariableAccess("aBooleanInA", actionScheme, actionSchemeCreationAction, true);

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		checkBindingVariableAccess("anIntegerInA", actionScheme, actionSchemeCreationAction, 12);

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		checkBindingVariableAccess("anOtherBooleanInA", actionScheme, actionSchemeCreationAction, true);

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		checkBindingVariableAccess(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY, actionScheme, actionSchemeCreationAction,
				actionSchemeCreationAction.getParametersValues());

		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));
		checkBindingVariableAccess(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY, actionScheme, actionSchemeCreationAction,
				actionScheme.getParameters());
	}*/

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
