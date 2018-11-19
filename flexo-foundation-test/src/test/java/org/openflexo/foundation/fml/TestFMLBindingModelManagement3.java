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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.InvalidBindingException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateModelSlot;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreationSchemeAction;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.FileUtils;

/**
 * This unit test is intented to test {@link BindingModel} management along FML model
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestFMLBindingModelManagement3 extends OpenflexoProjectAtRunTimeTestCase {

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestResourceCenter/TestViewPoint.fml";
	public static final String VIRTUAL_MODEL_NAME = "TestVirtualModel";

	static FlexoEditor editor;
	static VirtualModel viewPoint;
	static VirtualModel virtualModel1;
	static VirtualModel virtualModel2;
	static VirtualModel virtualModel3;

	static FlexoConcept flexoConceptA;
	static FlexoConcept flexoConceptB;
	static FlexoConcept flexoConceptC;
	static FlexoConcept flexoConceptD;

	static FlexoProject<File> project;
	static FMLRTVirtualModelInstance newView;
	static FMLRTVirtualModelInstance vmi1;
	static FMLRTVirtualModelInstance vmi2;
	static FMLRTVirtualModelInstance vmi3;
	static FlexoConceptInstance fci;

	private static DirectoryResourceCenter resourceCenter;

	/**
	 * Init
	 * 
	 * @throws IOException
	 */
	@Test
	@TestOrder(1)
	public void init() throws IOException {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		resourceCenter = makeNewDirectoryResourceCenter();
		assertNotNull(resourceCenter);
		System.out.println("ResourceCenter= " + resourceCenter);
	}

	/**
	 * Test {@link VirtualModel} creation, check {@link BindingModel}
	 * 
	 * @throws ModelDefinitionException
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(2)
	public void testCreateViewPoint() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();

		VirtualModelResource newVirtualModelResource = factory.makeTopLevelVirtualModelResource(VIEWPOINT_NAME, VIEWPOINT_URI,
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(), true);
		viewPoint = newVirtualModelResource.getLoadedResourceData();

		assertTrue(((VirtualModelResource) viewPoint.getResource()).getDirectory() != null);
		assertTrue(((VirtualModelResource) viewPoint.getResource()).getIODelegate().exists());

		assertNotNull(viewPoint.getBindingModel());
		assertEquals(1, viewPoint.getBindingModel().getBindingVariablesCount());
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));

	}

	/**
	 * Test {@link VirtualModel} creation, check {@link BindingModel}
	 * 
	 * @throws ModelDefinitionException
	 */
	@Test
	@TestOrder(3)
	public void testCreateVirtualModels() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();
		VirtualModelResource newVMResource = factory.makeContainedVirtualModelResource("VM1", viewPoint.getVirtualModelResource(), true);
		virtualModel1 = newVMResource.getLoadedResourceData();

		assertTrue(((VirtualModelResource) virtualModel1.getResource()).getIODelegate().exists());

		assertNotNull(virtualModel1.getBindingModel());

		System.out.println("BM=" + virtualModel1.getBindingModel());

		assertEquals(2, virtualModel1.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

		VirtualModelResource newVMResource2 = factory.makeContainedVirtualModelResource("VM2",
				(VirtualModelResource) virtualModel1.getResource(), true);
		virtualModel2 = newVMResource2.getLoadedResourceData();

		assertTrue(((VirtualModelResource) virtualModel2.getResource()).getIODelegate().exists());

		assertNotNull(virtualModel2.getBindingModel());
		assertEquals(2, virtualModel2.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel2),
				virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

		VirtualModelResource newVMResource3 = factory.makeContainedVirtualModelResource("VM3",
				(VirtualModelResource) virtualModel2.getResource(), true);
		virtualModel3 = newVMResource3.getLoadedResourceData();
		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) virtualModel3.getResource()).getDirectory()).exists());
		assertTrue(((VirtualModelResource) virtualModel3.getResource()).getIODelegate().exists());

		assertNotNull(virtualModel3.getBindingModel());
		assertEquals(2, virtualModel3.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel2),
				virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel3),
				virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

		// Now we create the vm1 model slot
		CreateModelSlot createMS1 = CreateModelSlot.actionType.makeNewAction(virtualModel3, null, editor);
		createMS1.setTechnologyAdapter(serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class));
		createMS1.setModelSlotClass(FMLRTVirtualModelInstanceModelSlot.class);
		createMS1.setModelSlotName("vm1");
		createMS1.setVmRes((VirtualModelResource) virtualModel1.getResource());
		createMS1.doAction();
		assertTrue(createMS1.hasActionExecutionSucceeded());

		FMLRTModelSlot<?, ?> ms1 = (FMLRTModelSlot<?, ?>) virtualModel3.getModelSlot("vm1");
		assertNotNull(ms1);
		assertSame(createMS1.getNewModelSlot(), ms1);

		// Now we create the vm2 model slot
		CreateModelSlot createMS2 = CreateModelSlot.actionType.makeNewAction(virtualModel3, null, editor);
		createMS2.setTechnologyAdapter(serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class));
		createMS2.setModelSlotClass(FMLRTVirtualModelInstanceModelSlot.class);
		createMS2.setModelSlotName("vm2");
		createMS2.setVmRes((VirtualModelResource) virtualModel2.getResource());
		createMS2.doAction();
		assertTrue(createMS2.hasActionExecutionSucceeded());

		// VirtualModel should have two FMLRTModelSlot
		assertEquals(2, virtualModel3.getModelSlots(FMLRTModelSlot.class).size());

		CreatePrimitiveRole createRoleInVM3 = CreatePrimitiveRole.actionType.makeNewAction(virtualModel3, null, editor);
		createRoleInVM3.setRoleName("aStringInVM3");
		createRoleInVM3.setPrimitiveType(PrimitiveType.String);
		createRoleInVM3.doAction();

		System.out.println("BM=" + virtualModel3.getBindingModel());

		assertNotNull(virtualModel3.getBindingModel());
		assertEquals(5, virtualModel3.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("vm1"));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				virtualModel3.getBindingModel().bindingVariableNamed("vm1").getType());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("vm2"));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel2),
				virtualModel3.getBindingModel().bindingVariableNamed("vm2").getType());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("aStringInVM3"));
		assertEquals(String.class, virtualModel3.getBindingModel().bindingVariableNamed("aStringInVM3").getType());

	}

	/**
	 * Test FlexoConcept creation, check BindingModel
	 */
	@Test
	@TestOrder(4)
	public void testCreateFlexoConceptA() throws SaveResourceException {

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(virtualModel1, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptA");
		addEP.doAction();

		flexoConceptA = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept A = " + flexoConceptA);
		assertNotNull(flexoConceptA);

		((VirtualModelResource) virtualModel1.getResource()).save();

		System.out.println("Saved: " + ((VirtualModelResource) virtualModel1.getResource()).getIODelegate());

		assertEquals(2, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

		CreatePrimitiveRole createPR1 = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR1.setRoleName("aStringInA");
		createPR1.setPrimitiveType(PrimitiveType.String);
		createPR1.doAction();

		CreatePrimitiveRole createPR2 = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR2.setRoleName("aBooleanInA");
		createPR2.setPrimitiveType(PrimitiveType.Boolean);
		createPR2.doAction();

		CreatePrimitiveRole createPR3 = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR3.setRoleName("anIntegerInA");
		createPR3.setPrimitiveType(PrimitiveType.Integer);
		createPR3.doAction();

		CreatePrimitiveRole createPR4 = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createPR4.setRoleName("anOtherBooleanInA");
		createPR4.setPrimitiveType(PrimitiveType.Boolean);
		createPR4.doAction();

		assertEquals(4, flexoConceptA.getFlexoProperties().size());

		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme.setFlexoBehaviourName("creationScheme");
		createCreationScheme.doAction();
		CreationScheme creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();

		CreateEditionAction createEditionAction1 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction1.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createEditionAction1.setEditionActionClass(ExpressionAction.class);
		createEditionAction1.setAssignation(new DataBinding<>("aStringInA"));
		createEditionAction1.doAction();
		AssignationAction<?> action1 = (AssignationAction<?>) createEditionAction1.getNewEditionAction();
		((ExpressionAction<?>) action1.getAssignableAction()).setExpression(new DataBinding<>("'foo'"));
		action1.setName("action1");

		assertTrue(action1.getAssignation().isValid());
		assertTrue(((ExpressionAction<?>) action1.getAssignableAction()).getExpression().isValid());

		CreateEditionAction createEditionAction2 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction2.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createEditionAction2.setEditionActionClass(ExpressionAction.class);
		createEditionAction2.setAssignation(new DataBinding<>("aBooleanInA"));
		createEditionAction2.doAction();
		AssignationAction<?> action2 = (AssignationAction<?>) createEditionAction2.getNewEditionAction();
		((ExpressionAction<?>) action2.getAssignableAction()).setExpression(new DataBinding<>("true"));
		action2.setName("action2");

		assertTrue(action2.getAssignation().isValid());
		assertTrue(((ExpressionAction<?>) action2.getAssignableAction()).getExpression().isValid());

		CreateEditionAction createEditionAction3 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction3.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createEditionAction3.setEditionActionClass(ExpressionAction.class);
		createEditionAction3.setAssignation(new DataBinding<>("anIntegerInA"));
		createEditionAction3.doAction();
		AssignationAction<?> action3 = (AssignationAction<?>) createEditionAction3.getNewEditionAction();
		((ExpressionAction<?>) action3.getAssignableAction()).setExpression(new DataBinding<>("8"));
		action3.setName("action3");

		assertTrue(action3.getAssignation().isValid());
		assertTrue(((ExpressionAction<?>) action3.getAssignableAction()).getExpression().isValid());

		assertTrue(flexoConceptA.getFlexoBehaviours().contains(creationScheme));
		assertTrue(flexoConceptA.getCreationSchemes().contains(creationScheme));

		// assertEquals(3, creationScheme.getActions().size());

		System.out.println("FML=\n" + creationScheme.getFlexoConcept().getFMLRepresentation());

	}

	@Test
	@TestOrder(5)
	public void testViewPointIsValid() {

		System.out.println("FML=" + virtualModel1.getFMLRepresentation());

		assertVirtualModelIsValid(viewPoint);

	}

	@Test
	@TestOrder(6)
	public void testInstanciateVirtualModelInstances()
			throws TypeMismatchException, NullReferenceException, InvocationTargetException, InvalidBindingException {

		log("testInstanciateVirtualModelInstances()");

		editor = createStandaloneProject("TestProject");
		project = (FlexoProject<File>) editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
		assertTrue(project.getResource().getIODelegate().exists());

		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType
				.makeNewAction(project.getVirtualModelInstanceRepository().getRootFolder(), null, editor);
		action.setNewVirtualModelInstanceName("MyView");
		action.setNewVirtualModelInstanceTitle("Test creation of a new view");
		action.setVirtualModel(viewPoint);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		newView = action.getNewVirtualModelInstance();
		assertNotNull(newView);
		assertNotNull(newView.getResource());
		try {
			newView.getResource().save();
		} catch (SaveResourceException e) {
			e.printStackTrace();
		}
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getDirectory() != null);
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getIODelegate().exists());

		assertNotNull(project.getResource(newView.getURI()));
		assertNotNull(project.getVirtualModelInstanceRepository().getResource(newView.getURI()));

		CreateBasicVirtualModelInstance createVMI1 = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		createVMI1.setNewVirtualModelInstanceName("MyVirtualModelInstance1");
		createVMI1.setNewVirtualModelInstanceTitle("Test creation of a new FMLRTVirtualModelInstance 1");
		createVMI1.setVirtualModel(virtualModel1);
		createVMI1.doAction();
		assertTrue(createVMI1.hasActionExecutionSucceeded());
		vmi1 = createVMI1.getNewVirtualModelInstance();
		assertSame(vmi1.getContainerVirtualModelInstance(), newView);
		assertNotNull(vmi1);
		assertNotNull(vmi1.getResource());
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getDirectory() != null);
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getIODelegate().exists());
		assertEquals(virtualModel1, vmi1.getFlexoConcept());
		assertEquals(virtualModel1, vmi1.getVirtualModel());

		CreateBasicVirtualModelInstance createVMI2 = CreateBasicVirtualModelInstance.actionType.makeNewAction(vmi1, null, editor);
		createVMI2.setNewVirtualModelInstanceName("MyVirtualModelInstance2");
		createVMI2.setNewVirtualModelInstanceTitle("Test creation of a new FMLRTVirtualModelInstance 2");
		createVMI2.setVirtualModel(virtualModel2);
		createVMI2.doAction();
		assertTrue(createVMI2.hasActionExecutionSucceeded());
		vmi2 = createVMI2.getNewVirtualModelInstance();
		assertSame(vmi2.getContainerVirtualModelInstance(), vmi1);
		assertNotNull(vmi2);
		assertNotNull(vmi2.getResource());
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getDirectory() != null);
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getIODelegate().exists());
		assertEquals(virtualModel2, vmi2.getFlexoConcept());
		assertEquals(virtualModel2, vmi2.getVirtualModel());

		CreateBasicVirtualModelInstance createVMI3 = CreateBasicVirtualModelInstance.actionType.makeNewAction(vmi2, null, editor);
		createVMI3.setNewVirtualModelInstanceName("MyVirtualModelInstance3");
		createVMI3.setNewVirtualModelInstanceTitle("Test creation of a new FMLRTVirtualModelInstance 3");
		createVMI3.setVirtualModel(virtualModel3);

		createVMI3.doAction();
		assertTrue(createVMI3.hasActionExecutionSucceeded());
		vmi3 = createVMI3.getNewVirtualModelInstance();
		assertSame(vmi3.getContainerVirtualModelInstance(), vmi2);
		assertNotNull(vmi3);
		assertNotNull(vmi3.getResource());
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getDirectory() != null);
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getIODelegate().exists());
		assertEquals(virtualModel3, vmi3.getFlexoConcept());
		assertEquals(virtualModel3, vmi3.getVirtualModel());

		FMLRTVirtualModelInstanceModelSlot ms1 = (FMLRTVirtualModelInstanceModelSlot) virtualModel3.getModelSlot("vm1");
		FMLRTVirtualModelInstanceModelSlot ms2 = (FMLRTVirtualModelInstanceModelSlot) virtualModel3.getModelSlot("vm2");

		vmi3.setFlexoPropertyValue(ms1, vmi1);
		vmi3.setFlexoPropertyValue(ms2, vmi2);

		assertEquals(vmi1, vmi3.getFlexoPropertyValue(ms1));
		assertEquals(vmi2, vmi3.getFlexoPropertyValue(ms2));

		assertNotNull(virtualModel3.getBindingModel());
		assertEquals(5, virtualModel3.getBindingModel().getBindingVariablesCount());

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("vm1"));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				virtualModel3.getBindingModel().bindingVariableNamed("vm1").getType());
		assertEquals(vmi1, vmi3.execute("vm1"));

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("vm2"));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel2),
				virtualModel3.getBindingModel().bindingVariableNamed("vm2").getType());
		assertEquals(vmi2, vmi3.execute("vm2"));

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("aStringInVM3"));
		assertEquals(String.class, virtualModel3.getBindingModel().bindingVariableNamed("aStringInVM3").getType());
		assertEquals((String) null, vmi3.execute("aStringInVM3"));

		System.out.println("FML=" + virtualModel3.getFMLRepresentation());

		vmi3.setFlexoActor("toto", (FlexoRole) vmi3.getVirtualModel().getAccessibleProperty("aStringInVM3"));
		assertEquals("toto", vmi3.execute("aStringInVM3"));
	}

	@Test
	@TestOrder(7)
	public void checkContainerAccess()
			throws TypeMismatchException, NullReferenceException, InvocationTargetException, InvalidBindingException {
		assertEquals(vmi3, vmi3.execute("this"));
		assertEquals(vmi2, vmi3.execute("this.container"));
		assertEquals(vmi2, vmi3.execute("container"));
		assertEquals(vmi1, vmi3.execute("this.container.container"));
		assertEquals(vmi1, vmi3.execute("container.container"));
		assertEquals(newView, vmi3.execute("this.container.container.container"));
		assertEquals(newView, vmi3.execute("container.container.container"));
	}

	@Test
	@TestOrder(8)
	public void checkIntrospectiveAccess()
			throws TypeMismatchException, NullReferenceException, InvocationTargetException, InvalidBindingException {
		assertEquals(virtualModel3, vmi3.execute("this.concept"));
		assertEquals(virtualModel3, vmi3.execute("this.virtualModel"));
		assertEquals(virtualModel2, vmi3.execute("this.container.concept"));
		assertEquals(virtualModel2, vmi3.execute("container.concept"));
		assertEquals(virtualModel2, vmi3.execute("this.container.virtualModel"));
		assertEquals(virtualModel2, vmi3.execute("container.virtualModel"));
		assertEquals(virtualModel1, vmi3.execute("this.container.container.concept"));
		assertEquals(virtualModel1, vmi3.execute("container.container.concept"));
		assertEquals(virtualModel1, vmi3.execute("this.container.container.virtualModel"));
		assertEquals(virtualModel1, vmi3.execute("container.container.virtualModel"));
		assertEquals(viewPoint, vmi3.execute("this.container.container.container.concept"));
		assertEquals(viewPoint, vmi3.execute("container.container.container.concept"));
		assertEquals(viewPoint, vmi3.execute("this.container.container.container.virtualModel"));
		assertEquals(viewPoint, vmi3.execute("container.container.container.virtualModel"));

	}

	@Test
	@TestOrder(10)
	public void testInstanciateFlexoConceptInstance()
			throws TypeMismatchException, NullReferenceException, InvocationTargetException, InvalidBindingException {

		log("testInstanciateFlexoConceptInstance()");

		CreationScheme creationScheme = flexoConceptA.getFlexoBehaviours(CreationScheme.class).get(0);
		assertNotNull(creationScheme);

		System.out.println("Instanciate using: ");
		System.out.println(creationScheme.getFMLRepresentation());

		CreationSchemeAction creationSchemeCreationAction = new CreationSchemeAction(creationScheme, vmi1, null, editor);
		assertNotNull(creationSchemeCreationAction);
		creationSchemeCreationAction.doAction();
		assertTrue(creationSchemeCreationAction.hasActionExecutionSucceeded());

		fci = creationSchemeCreationAction.getFlexoConceptInstance();

		assertNotNull(fci);
		assertEquals(flexoConceptA, fci.getFlexoConcept());

		assertEquals("foo", fci.getFlexoActor("aStringInA"));
		assertEquals(true, fci.getFlexoActor("aBooleanInA"));
		assertEquals(8, (long) fci.getFlexoActor("anIntegerInA"));

		fci.setFlexoActor(false, (FlexoRole<Boolean>) flexoConceptA.getAccessibleProperty("anOtherBooleanInA"));

		assertEquals(6, flexoConceptA.getBindingModel().getBindingVariablesCount());

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY).getType());

		assertEquals(fci, fci.execute("this"));
		assertEquals(vmi1, fci.execute("this.container"));
		assertEquals(vmi1, fci.execute("container"));
		assertEquals(flexoConceptA, fci.execute("this.concept"));

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConceptA.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertEquals("foo", fci.execute("aStringInA"));

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, flexoConceptA.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertEquals(true, fci.execute("aBooleanInA"));

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, flexoConceptA.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertEquals(8, (long) fci.execute("anIntegerInA"));

		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class, flexoConceptA.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertEquals(false, fci.execute("anOtherBooleanInA"));

		assertEquals(vmi3, vmi3.execute("this"));
		assertEquals(vmi1, vmi3.execute("vm1"));
		assertEquals(vmi2, vmi3.execute("vm2"));
		assertEquals(vmi1, vmi3.execute("this.vm1"));
		assertEquals(vmi2, vmi3.execute("this.vm2"));
		assertEquals(1, (long) vmi3.execute("this.vm1.flexoConceptInstances.size"));
		assertSame(fci, vmi3.execute("this.vm1.flexoConceptInstances.get(0)"));
	}

	/**
	 * Reload the ViewPoint<br>
	 * We first re-init a full ServiceManager, and copy the just created ViewPoint<br>
	 * The goal is to let the FileSystem monitoring system detects the new directory and instantiate ViewPoint
	 * 
	 * @throws IOException
	 * @throws InvalidBindingException
	 */
	/*@Test
	@TestOrder(11)
	public void testReloadViewPoint() throws IOException {
	
		log("testReloadViewPoint()");
	
		instanciateTestServiceManager();
		resourceCenter = makeNewDirectoryResourceCenter();
	
		File directory = ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) viewPoint.getResource()).getDirectory());
		File newDirectory = new File(((FileSystemBasedResourceCenter) resourceCenter).getDirectory(), directory.getName());
		newDirectory.mkdirs();
	
		try {
			FileUtils.copyContentDirToDir(directory, newDirectory);
			// We wait here for the thread monitoring ResourceCenters to detect
			// new files
			((FileSystemBasedResourceCenter) resourceCenter).performDirectoryWatchingNow();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		VirtualModelResource retrievedVPResource = serviceManager.getVirtualModelLibrary().getVirtualModelResource(VIEWPOINT_URI);
		assertNotNull(retrievedVPResource);
	
		VirtualModel reloadedViewPoint = retrievedVPResource.getVirtualModel();
		VirtualModel reloadedVM1 = reloadedViewPoint.getVirtualModelNamed("VM1");
		assertNotNull(reloadedVM1);
		VirtualModel reloadedVM2 = reloadedVM1.getVirtualModelNamed("VM2");
		assertNotNull(reloadedVM2);
		VirtualModel reloadedVM3 = reloadedVM2.getVirtualModelNamed("VM3");
		assertNotNull(reloadedVM3);
	
	}*/

	@Test
	@TestOrder(12)
	public void testReloadProject() throws ResourceLoadingCancelledException, FlexoException, TypeMismatchException, NullReferenceException,
			InvocationTargetException, IOException, InvalidBindingException {

		log("testReloadProject()");

		instanciateTestServiceManager();

		resourceCenter = makeNewDirectoryResourceCenter();

		File directory = ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) viewPoint.getResource()).getDirectory());
		File newDirectory = new File(((FileSystemBasedResourceCenter) resourceCenter).getRootDirectory(), directory.getName());
		newDirectory.mkdirs();

		try {
			FileUtils.copyContentDirToDir(directory, newDirectory);
			// We wait here for the thread monitoring ResourceCenters to detect
			// new files
			((FileSystemBasedResourceCenter) resourceCenter).performDirectoryWatchingNow();
		} catch (IOException e) {
			e.printStackTrace();
		}

		VirtualModelResource retrievedVPResource = serviceManager.getVirtualModelLibrary().getVirtualModelResource(VIEWPOINT_URI);
		assertNotNull(retrievedVPResource);

		VirtualModel reloadedViewPoint = retrievedVPResource.getVirtualModel();
		VirtualModel reloadedVM1 = reloadedViewPoint.getVirtualModelNamed("VM1");
		assertNotNull(reloadedVM1);
		VirtualModel reloadedVM2 = reloadedVM1.getVirtualModelNamed("VM2");
		assertNotNull(reloadedVM2);
		VirtualModel reloadedVM3 = reloadedVM2.getVirtualModelNamed("VM3");
		assertNotNull(reloadedVM3);

		System.out.println("Found resource " + reloadedViewPoint.getURI());
		System.out.println("Found resource " + reloadedVM1.getURI());
		System.out.println("Found resource " + reloadedVM2.getURI());
		System.out.println("Found resource " + reloadedVM3.getURI());

		String searchedViewURI = newView.getURI();

		editor = loadProject(project.getProjectDirectory());
		project = (FlexoProject<File>) editor.getProject();
		assertNotNull(editor);
		assertNotNull(project);

		System.out.println("All resources=" + project.getAllResources());
		System.out.println("searching: " + searchedViewURI);
		assertEquals(5, project.getAllResources().size());
		assertNotNull(project.getResource(searchedViewURI));

		FMLRTVirtualModelInstanceResource newViewResource = project.getVirtualModelInstanceRepository()
				.getVirtualModelInstance(searchedViewURI);
		assertNotNull(newViewResource);
		assertNull(newViewResource.getLoadedResourceData());
		newViewResource.loadResourceData();
		assertNotNull(newView = newViewResource.getVirtualModelInstance());

		assertEquals(1, newViewResource.getVirtualModelInstanceResources().size());
		FMLRTVirtualModelInstanceResource newVMI1Resource = newViewResource.getVirtualModelInstanceResources().get(0);
		assertNotNull(newVMI1Resource);
		assertNull(newVMI1Resource.getLoadedResourceData());
		newVMI1Resource.loadResourceData();
		assertNotNull(vmi1 = newVMI1Resource.getVirtualModelInstance());

		assertEquals(1, newVMI1Resource.getVirtualModelInstanceResources().size());
		FMLRTVirtualModelInstanceResource newVMI2Resource = newVMI1Resource.getVirtualModelInstanceResources().get(0);
		assertNotNull(newVMI2Resource);
		assertNull(newVMI2Resource.getLoadedResourceData());
		newVMI2Resource.loadResourceData();
		assertNotNull(vmi2 = newVMI2Resource.getVirtualModelInstance());

		assertEquals(1, newVMI2Resource.getVirtualModelInstanceResources().size());
		FMLRTVirtualModelInstanceResource newVMI3Resource = newVMI2Resource.getVirtualModelInstanceResources().get(0);
		assertNotNull(newVMI3Resource);
		assertNull(newVMI3Resource.getLoadedResourceData());
		newVMI3Resource.loadResourceData();
		assertNotNull(vmi3 = newVMI3Resource.getVirtualModelInstance());

		assertEquals(vmi3, vmi3.execute("this"));
		assertEquals(vmi2, vmi3.execute("this.container"));
		assertEquals(vmi2, vmi3.execute("container"));
		assertEquals(vmi1, vmi3.execute("this.container.container"));
		assertEquals(vmi1, vmi3.execute("container.container"));
		assertEquals(newView, vmi3.execute("this.container.container.container"));
		assertEquals(newView, vmi3.execute("container.container.container"));

		assertEquals(reloadedVM3, vmi3.execute("this.concept"));
		assertEquals(reloadedVM3, vmi3.execute("this.virtualModel"));
		assertEquals(reloadedVM2, vmi3.execute("this.container.concept"));
		assertEquals(reloadedVM2, vmi3.execute("container.concept"));
		assertEquals(reloadedVM2, vmi3.execute("this.container.virtualModel"));
		assertEquals(reloadedVM2, vmi3.execute("container.virtualModel"));
		assertEquals(reloadedVM1, vmi3.execute("this.container.container.concept"));
		assertEquals(reloadedVM1, vmi3.execute("container.container.concept"));
		assertEquals(reloadedVM1, vmi3.execute("this.container.container.virtualModel"));
		assertEquals(reloadedVM1, vmi3.execute("container.container.virtualModel"));
		assertEquals(reloadedViewPoint, vmi3.execute("this.container.container.container.concept"));
		assertEquals(reloadedViewPoint, vmi3.execute("container.container.container.concept"));
		assertEquals(reloadedViewPoint, vmi3.execute("this.container.container.container.virtualModel"));
		assertEquals(reloadedViewPoint, vmi3.execute("container.container.container.virtualModel"));

	}

}
