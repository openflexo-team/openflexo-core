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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateModelSlot;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTModelSlotInstanceConfiguration;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.ModelSlotInstanceConfiguration.DefaultModelSlotInstanceConfigurationOption;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test {@link BindingModel} management along FML model
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestFMLBindingModelManagement3 extends OpenflexoProjectAtRunTimeTestCase {

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestViewPoint";
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

	static FlexoProject project;
	static VirtualModelInstance newView;
	static VirtualModelInstance vmi1;
	static VirtualModelInstance vmi2;
	static VirtualModelInstance vmi3;
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
	 * Test {@link ViewPoint} creation, check {@link BindingModel}
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
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(),
				fmlTechnologyAdapter.getTechnologyContextManager(), true);
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
		VirtualModelResource newVMResource = factory.makeContainedVirtualModelResource("VM1", viewPoint.getVirtualModelResource(),
				fmlTechnologyAdapter.getTechnologyContextManager(), true);
		virtualModel1 = newVMResource.getLoadedResourceData();

		// virtualModel1 = VirtualModelImpl.newVirtualModel("VM1", viewPoint);
		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) virtualModel1.getResource()).getDirectory()).exists());
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
				(VirtualModelResource) virtualModel1.getResource(), fmlTechnologyAdapter.getTechnologyContextManager(), true);
		virtualModel2 = newVMResource2.getLoadedResourceData();

		// virtualModel2 = VirtualModelImpl.newVirtualModel("VM2", viewPoint);
		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) virtualModel2.getResource()).getDirectory()).exists());
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
				(VirtualModelResource) virtualModel2.getResource(), fmlTechnologyAdapter.getTechnologyContextManager(), true);
		virtualModel3 = newVMResource3.getLoadedResourceData();
		// virtualModel3 = VirtualModelImpl.newVirtualModel("VM3", viewPoint);
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

		FMLRTModelSlot ms1 = (FMLRTModelSlot) virtualModel3.getModelSlot("vm1");
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

		((VirtualModelResource) virtualModel1.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) virtualModel1.getResource()).getIODelegate());

		assertEquals(2, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

	}

	@Test
	@TestOrder(19)
	public void testViewPointIsValid() {

		System.out.println("FML=" + virtualModel1.getFMLRepresentation());

		assertVirtualModelIsValid(viewPoint);

	}

	@Test
	@TestOrder(20)
	public void testInstanciateVirtualModelInstances() throws TypeMismatchException, NullReferenceException, InvocationTargetException {

		log("testInstanciateVirtualModelInstances()");

		editor = createProject("TestProject");
		project = editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
		assertTrue(project.getProjectDataResource().getIODelegate().exists());

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
			newView.getResource().save(null);
		} catch (SaveResourceException e) {
			e.printStackTrace();
		}
		// assertTrue(((ViewResource)
		// newView.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource)
		// newView.getResource()).getFile().exists());
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getDirectory() != null);
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getIODelegate().exists());

		assertNotNull(project.getResource(newView.getURI()));
		assertNotNull(project.getVirtualModelInstanceRepository().getResource(newView.getURI()));

		CreateBasicVirtualModelInstance createVMI1 = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		createVMI1.setNewVirtualModelInstanceName("MyVirtualModelInstance1");
		createVMI1.setNewVirtualModelInstanceTitle("Test creation of a new VirtualModelInstance 1");
		createVMI1.setVirtualModel(virtualModel1);
		createVMI1.doAction();
		assertTrue(createVMI1.hasActionExecutionSucceeded());
		vmi1 = createVMI1.getNewVirtualModelInstance();
		assertSame(vmi1.getContainerVirtualModelInstance(), newView);
		assertNotNull(vmi1);
		assertNotNull(vmi1.getResource());
		// assertTrue(((ViewResource)
		// newView.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource)
		// newView.getResource()).getFile().exists());
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getDirectory() != null);
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getIODelegate().exists());
		assertEquals(virtualModel1, vmi1.getFlexoConcept());
		assertEquals(virtualModel1, vmi1.getVirtualModel());

		CreateBasicVirtualModelInstance createVMI2 = CreateBasicVirtualModelInstance.actionType.makeNewAction(vmi1, null, editor);
		createVMI2.setNewVirtualModelInstanceName("MyVirtualModelInstance2");
		createVMI2.setNewVirtualModelInstanceTitle("Test creation of a new VirtualModelInstance 2");
		createVMI2.setVirtualModel(virtualModel2);
		createVMI2.doAction();
		assertTrue(createVMI2.hasActionExecutionSucceeded());
		vmi2 = createVMI2.getNewVirtualModelInstance();
		assertSame(vmi2.getContainerVirtualModelInstance(), vmi1);
		assertNotNull(vmi2);
		assertNotNull(vmi2.getResource());
		// assertTrue(((ViewResource)
		// newView.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource)
		// newView.getResource()).getFile().exists());
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getDirectory() != null);
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getIODelegate().exists());
		assertEquals(virtualModel2, vmi2.getFlexoConcept());
		assertEquals(virtualModel2, vmi2.getVirtualModel());

		CreateBasicVirtualModelInstance createVMI3 = CreateBasicVirtualModelInstance.actionType.makeNewAction(vmi2, null, editor);
		createVMI3.setNewVirtualModelInstanceName("MyVirtualModelInstance3");
		createVMI3.setNewVirtualModelInstanceTitle("Test creation of a new VirtualModelInstance 3");
		createVMI3.setVirtualModel(virtualModel3);

		FMLRTModelSlot ms1 = (FMLRTModelSlot) virtualModel3.getModelSlot("vm1");
		FMLRTModelSlotInstanceConfiguration ms1Configuration = (FMLRTModelSlotInstanceConfiguration) createVMI3
				.getModelSlotInstanceConfiguration(ms1);
		ms1Configuration.setOption(DefaultModelSlotInstanceConfigurationOption.SelectExistingVirtualModel);
		ms1Configuration.setAddressedVirtualModelInstanceResource((FMLRTVirtualModelInstanceResource) vmi1.getResource());
		assertTrue(ms1Configuration.isValidConfiguration());

		FMLRTModelSlot ms2 = (FMLRTModelSlot) virtualModel3.getModelSlot("vm2");
		FMLRTModelSlotInstanceConfiguration ms2Configuration = (FMLRTModelSlotInstanceConfiguration) createVMI3
				.getModelSlotInstanceConfiguration(ms2);
		ms2Configuration.setOption(DefaultModelSlotInstanceConfigurationOption.SelectExistingVirtualModel);
		ms2Configuration.setAddressedVirtualModelInstanceResource((FMLRTVirtualModelInstanceResource) vmi2.getResource());
		assertTrue(ms2Configuration.isValidConfiguration());

		createVMI3.doAction();
		assertTrue(createVMI3.hasActionExecutionSucceeded());
		vmi3 = createVMI3.getNewVirtualModelInstance();
		assertSame(vmi3.getContainerVirtualModelInstance(), vmi2);
		assertNotNull(vmi3);
		assertNotNull(vmi3.getResource());
		// assertTrue(((ViewResource)
		// newView.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource)
		// newView.getResource()).getFile().exists());
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getDirectory() != null);
		assertTrue(((FMLRTVirtualModelInstanceResource) newView.getResource()).getIODelegate().exists());
		assertEquals(virtualModel3, vmi3.getFlexoConcept());
		assertEquals(virtualModel3, vmi3.getVirtualModel());

		assertNotNull(virtualModel3.getBindingModel());
		assertEquals(5, virtualModel3.getBindingModel().getBindingVariablesCount());

		/*assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY, virtualModel3, vmi3, viewPoint);
		
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY, virtualModel3, vmi3, virtualModel3);
		
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		checkBindingVariableAccess(VirtualModelBindingModel.VIEW_PROPERTY, virtualModel3, vmi3, newView);
		
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel3),
				virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		checkBindingVariableAccess(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY, virtualModel3, vmi3, vmi3);*/

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("vm1"));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				virtualModel3.getBindingModel().bindingVariableNamed("vm1").getType());
		checkBindingVariableAccess("vm1", virtualModel3, vmi3, vmi1);

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("vm2"));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel2),
				virtualModel3.getBindingModel().bindingVariableNamed("vm2").getType());
		checkBindingVariableAccess("vm2", virtualModel3, vmi3, vmi2);

		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed("aStringInVM3"));
		assertEquals(String.class, virtualModel3.getBindingModel().bindingVariableNamed("aStringInVM3").getType());
		checkBindingVariableAccess("aStringInVM3", virtualModel3, vmi3, null);
		assertEquals((String) null, vmi3.execute("aStringInVM3"));

		System.out.println("FML=" + virtualModel3.getFMLRepresentation());

		vmi3.setFlexoActor("toto", (FlexoRole) vmi3.getVirtualModel().getAccessibleProperty("aStringInVM3"));
		checkBindingVariableAccess("aStringInVM3", virtualModel3, vmi3, "toto");
		assertEquals("toto", vmi3.execute("aStringInVM3"));

		assertEquals("toto", vmi3.execute("aStringInVM3"));

		assertEquals(vmi3, vmi3.execute("this"));
		assertEquals(vmi2, vmi3.execute("this.container"));
		assertEquals(vmi2, vmi3.execute("container"));
		assertEquals(vmi1, vmi3.execute("this.container.container"));
		assertEquals(vmi1, vmi3.execute("container.container"));
		assertEquals(virtualModel3, vmi3.execute("this.concept"));

	}

	/*@Test
	@TestOrder(21)
	public void testInstanciateFlexoConceptInstance() {
	
		log("testInstanciateFlexoConceptInstance()");
	
		CreationScheme creationScheme = flexoConceptA.getFlexoBehaviours(CreationScheme.class).get(0);
		assertNotNull(creationScheme);
	
		System.out.println("Instanciate using: ");
		System.out.println(creationScheme.getFMLRepresentation());
	
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
		assertEquals(8, (long) fci.getFlexoActor("anIntegerInA"));
	
		fci.setFlexoActor(false, (FlexoRole<Boolean>) flexoConceptA.getAccessibleProperty("anOtherBooleanInA"));
	
		assertEquals(12, flexoConceptA.getBindingModel().getBindingVariablesCount());
	
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY, flexoConceptA, fci, viewPoint);
	
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY, flexoConceptA, fci, virtualModel1);
	
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		checkBindingVariableAccess(VirtualModelBindingModel.VIEW_PROPERTY, flexoConceptA, fci, newView);
	
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY, flexoConceptA, fci, flexoConceptA);
	
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		checkBindingVariableAccess(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY, flexoConceptA, fci, vmi1);
	
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY).getType());
		checkBindingVariableAccess(FlexoConceptBindingModel.THIS_PROPERTY, flexoConceptA, fci, fci);
	
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
	
		fci.setFlexoActor("newValue", (FlexoRole<String>) flexoConceptA.getAccessibleProperty("aStringInA"));
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
		assertEquals(12, (long) fci.getFlexoActor("anIntegerInA"));
	
		assertEquals(14, actionScheme.getBindingModel().getBindingVariablesCount());
	
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY, actionScheme, actionSchemeCreationAction, viewPoint);
	
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY, actionScheme, actionSchemeCreationAction,
				virtualModel1);
	
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		checkBindingVariableAccess(VirtualModelBindingModel.VIEW_PROPERTY, actionScheme, actionSchemeCreationAction, newView);
	
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		checkBindingVariableAccess(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY, actionScheme, actionSchemeCreationAction,
				flexoConceptA);
	
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		checkBindingVariableAccess(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY, actionScheme, actionSchemeCreationAction,
				vmi1);
	
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY).getType());
		checkBindingVariableAccess(FlexoConceptBindingModel.THIS_PROPERTY, actionScheme, actionSchemeCreationAction, fci);
	
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
		DataBinding<Object> db = new DataBinding<>(bv.getVariableName(), owner, bv.getType(), BindingDefinitionType.GET);
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
		DataBinding<Object> db = new DataBinding<>(binding, owner, Object.class, BindingDefinitionType.GET);
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
