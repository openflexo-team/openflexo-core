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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import org.openflexo.foundation.fml.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.fml.binding.FlexoPropertyBindingVariable;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
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
public class TestFMLBindingModelManagement extends OpenflexoProjectAtRunTimeTestCase {

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

		// viewPoint = ViewPointImpl.newViewPoint("TestViewPoint",
		// "http://openflexo.org/test/TestViewPoint",
		// resourceCenter.getDirectory(),
		// serviceManager.getViewPointLibrary(), resourceCenter);
		// assertTrue(((VirtualModelResource)
		// viewPoint.getResource()).getDirectory().exists());
		// assertTrue(((VirtualModelResource)
		// viewPoint.getResource()).getFile().exists());
		assertTrue(((VirtualModelResource) viewPoint.getResource()).getDirectory() != null);
		assertTrue(((VirtualModelResource) viewPoint.getResource()).getIODelegate().exists());

		System.out.println("ViewPoint BindingModel = " + viewPoint.getBindingModel());
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
	public void testCreateVirtualModel() throws SaveResourceException, ModelDefinitionException {

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

		// We disconnect VirtualModel from ViewPoint, and we check BindingModel
		// evolution
		viewPoint.removeFromVirtualModels(virtualModel1);
		assertNotNull(virtualModel1.getBindingModel());
		assertEquals(1, virtualModel1.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

		// We reconnect VirtualModel again, and we check BindingModel evolution
		viewPoint.addToVirtualModels(virtualModel1);
		System.out.println("BM = " + virtualModel1.getBindingModel());
		assertEquals(2, virtualModel1.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				virtualModel1.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

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

		System.out.println("FlexoConcept BindingModel = " + flexoConceptA.getBindingModel());

		assertEquals(2, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

		// Disconnect FlexoConcept
		virtualModel1.removeFromFlexoConcepts(flexoConceptA);

		assertEquals(1, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));

		// Reconnect FlexoConcept
		virtualModel1.addToFlexoConcepts(flexoConceptA);

		assertEquals(2, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

	}

	@Test
	@TestOrder(5)
	public void testFlexoRoleBindingModelManagement() throws SaveResourceException {

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

		assertEquals(3, flexoConceptA.getFlexoProperties().size());
		assertTrue(flexoConceptA.getFlexoProperties().contains(createPR1.getNewFlexoRole()));
		assertTrue(flexoConceptA.getFlexoProperties().contains(createPR2.getNewFlexoRole()));
		assertTrue(flexoConceptA.getFlexoProperties().contains(createPR3.getNewFlexoRole()));

		System.out.println("FlexoConcept BindingModel = " + flexoConceptA.getBindingModel());

		assertEquals(5, flexoConceptA.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConceptA.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, flexoConceptA.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, flexoConceptA.getBindingModel().bindingVariableNamed("anIntegerInA").getType());

		PrimitiveRole aStringInA = (PrimitiveRole) flexoConceptA.getAccessibleProperty("aStringInA");
		assertNotNull(aStringInA);

		FlexoPropertyBindingVariable bv = (FlexoPropertyBindingVariable) flexoConceptA.getBindingModel().bindingVariableNamed("aStringInA");
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
		assertEquals(Float.class, flexoConceptA.getBindingModel().bindingVariableNamed("aRenamedStringInA").getType());

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
	public void testFlexoConceptBindingModelManagement() throws SaveResourceException, InconsistentFlexoConceptHierarchyException {

		CreateFlexoConcept addFlexoConceptB = CreateFlexoConcept.actionType.makeNewAction(virtualModel1, null, editor);
		addFlexoConceptB.setNewFlexoConceptName("FlexoConceptB");
		addFlexoConceptB.doAction();
		flexoConceptB = addFlexoConceptB.getNewFlexoConcept();
		assertNotNull(flexoConceptB);

		CreateFlexoConcept addFlexoConceptC = CreateFlexoConcept.actionType.makeNewAction(virtualModel1, null, editor);
		addFlexoConceptC.setNewFlexoConceptName("FlexoConceptC");
		addFlexoConceptC.addToParentConcepts(flexoConceptB);
		addFlexoConceptC.doAction();
		flexoConceptC = addFlexoConceptC.getNewFlexoConcept();

		CreatePrimitiveRole createRoleInFlexoConceptB = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptB, null, editor);
		createRoleInFlexoConceptB.setRoleName("aStringInB");
		createRoleInFlexoConceptB.setPrimitiveType(PrimitiveType.String);
		createRoleInFlexoConceptB.doAction();

		assertEquals(1, flexoConceptB.getFlexoProperties().size());
		assertTrue(flexoConceptB.getFlexoProperties().contains(createRoleInFlexoConceptB.getNewFlexoRole()));

		CreatePrimitiveRole createRoleInFlexoConceptC = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptC, null, editor);
		createRoleInFlexoConceptC.setRoleName("aStringInC");
		createRoleInFlexoConceptC.setPrimitiveType(PrimitiveType.String);
		createRoleInFlexoConceptC.doAction();

		assertEquals(1, flexoConceptC.getFlexoProperties().size());
		assertTrue(flexoConceptC.getFlexoProperties().contains(createRoleInFlexoConceptC.getNewFlexoRole()));

		((VirtualModelResource) virtualModel1.getResource()).save(null);

		assertEquals(3, flexoConceptB.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptB),
				flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed("aStringInB"));
		assertEquals(String.class, flexoConceptB.getBindingModel().bindingVariableNamed("aStringInB").getType());

		assertEquals(4, flexoConceptC.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptC),
				flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aStringInB"));
		assertEquals(String.class, flexoConceptC.getBindingModel().bindingVariableNamed("aStringInB").getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aStringInC"));
		assertEquals(String.class, flexoConceptC.getBindingModel().bindingVariableNamed("aStringInC").getType());

		PrimitiveRole aStringInB = (PrimitiveRole) flexoConceptB.getAccessibleProperty("aStringInB");
		assertNotNull(aStringInB);

		// We now try to rename the FlexoRole in FlexoConceptB

		FlexoPropertyBindingVariable bvForB = (FlexoPropertyBindingVariable) flexoConceptB.getBindingModel()
				.bindingVariableNamed("aStringInB");
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

		FlexoPropertyBindingVariable bvForC = (FlexoPropertyBindingVariable) flexoConceptC.getBindingModel()
				.bindingVariableNamed("aStringInB");
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

		assertEquals(3, flexoConceptB.getBindingModel().getBindingVariablesCount());
		assertEquals(4, flexoConceptC.getBindingModel().getBindingVariablesCount());

		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed("aRenamedStringInB"));
		assertEquals(String.class, flexoConceptB.getBindingModel().bindingVariableNamed("aRenamedStringInB").getType());

		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aRenamedStringInB"));
		assertEquals(String.class, flexoConceptC.getBindingModel().bindingVariableNamed("aRenamedStringInB").getType());

		// We now try to add a FlexoRole in FlexoConceptB
		CreatePrimitiveRole createRoleInFlexoConceptB2 = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptB, null, editor);
		createRoleInFlexoConceptB2.setRoleName("anOtherStringInB");
		createRoleInFlexoConceptB2.setPrimitiveType(PrimitiveType.String);
		createRoleInFlexoConceptB2.doAction();

		assertEquals(4, flexoConceptB.getBindingModel().getBindingVariablesCount());
		assertEquals(5, flexoConceptC.getBindingModel().getBindingVariablesCount());

		flexoConceptC.removeFromParentFlexoConcepts(flexoConceptB);

		assertEquals(3, flexoConceptC.getBindingModel().getBindingVariablesCount());

		flexoConceptC.addToParentFlexoConcepts(flexoConceptA);
		flexoConceptC.addToParentFlexoConcepts(flexoConceptB);

		assertEquals(8, flexoConceptC.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptC),
				flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConceptC.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, flexoConceptC.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, flexoConceptC.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aRenamedStringInB"));
		assertEquals(String.class, flexoConceptC.getBindingModel().bindingVariableNamed("aRenamedStringInB").getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("anOtherStringInB"));
		assertEquals(String.class, flexoConceptC.getBindingModel().bindingVariableNamed("anOtherStringInB").getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aStringInC"));
		assertEquals(String.class, flexoConceptC.getBindingModel().bindingVariableNamed("aStringInC").getType());
	}

	@Test
	@TestOrder(7)
	public void testFlexoConceptBindingModelManagement2() throws SaveResourceException, InconsistentFlexoConceptHierarchyException {

		CreateFlexoConcept addFlexoConceptD = CreateFlexoConcept.actionType.makeNewAction(virtualModel1, null, editor);
		addFlexoConceptD.setNewFlexoConceptName("FlexoConceptD");
		addFlexoConceptD.doAction();
		flexoConceptD = addFlexoConceptD.getNewFlexoConcept();
		assertNotNull(flexoConceptD);

		assertEquals(2, flexoConceptD.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptD),
				flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

		/*assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.PROJECT_PROPERTY));
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.RC_PROPERTY));
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1),
				flexoConceptD.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptD),
				flexoConceptD.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY).getType());*/

		flexoConceptD.addToParentFlexoConcepts(flexoConceptC);
		flexoConceptD.addToParentFlexoConcepts(flexoConceptB);

		System.out.println("FML: " + virtualModel1.getFMLRepresentation());
		System.out.println("BM=" + flexoConceptD.getBindingModel());

		assertEquals(8, flexoConceptD.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConceptD.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, flexoConceptD.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, flexoConceptD.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed("aRenamedStringInB"));
		assertEquals(String.class, flexoConceptD.getBindingModel().bindingVariableNamed("aRenamedStringInB").getType());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed("anOtherStringInB"));
		assertEquals(String.class, flexoConceptD.getBindingModel().bindingVariableNamed("anOtherStringInB").getType());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed("aStringInC"));
		assertEquals(String.class, flexoConceptD.getBindingModel().bindingVariableNamed("aStringInC").getType());

		// add property in FlexoConceptA
		CreatePrimitiveRole createOtherBooleanInA = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createOtherBooleanInA.setRoleName("anOtherBooleanInA");
		createOtherBooleanInA.setPrimitiveType(PrimitiveType.Boolean);
		createOtherBooleanInA.doAction();

		// Check that this property is visible in FlexoConceptD
		assertEquals(9, flexoConceptD.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptD.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class, flexoConceptD.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());

		((VirtualModelResource) virtualModel1.getResource()).save(null);
		System.out.println("Saved: " + ((VirtualModelResource) virtualModel1.getResource()).getIODelegate().toString());

	}

	/**
	 * Test management of VirtualModel's BindingModel
	 * 
	 * @throws ModelDefinitionException
	 */
	@Test
	@TestOrder(8)
	public void testVirtualModelBindingModelManagement() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();

		VirtualModelResource newVMResource2 = factory.makeContainedVirtualModelResource("VM2", viewPoint.getVirtualModelResource(),
				fmlTechnologyAdapter.getTechnologyContextManager(), true);
		virtualModel2 = newVMResource2.getLoadedResourceData();

		// virtualModel2 = VirtualModelImpl.newVirtualModel("VM2", viewPoint);
		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) virtualModel2.getResource()).getDirectory()).exists());
		assertTrue(((VirtualModelResource) virtualModel2.getResource()).getIODelegate().exists());

		assertNotNull(virtualModel2.getBindingModel());
		assertEquals(2, virtualModel2.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel2),
				virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

		/*assertEquals(6, virtualModel2.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.PROJECT_PROPERTY));
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.RC_PROPERTY));
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel2),
				virtualModel2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());*/

		VirtualModelResource newVMResource3 = factory.makeContainedVirtualModelResource("VM3", viewPoint.getVirtualModelResource(),
				fmlTechnologyAdapter.getTechnologyContextManager(), true);
		virtualModel3 = newVMResource3.getLoadedResourceData();
		// virtualModel3 = VirtualModelImpl.newVirtualModel("VM3", viewPoint);
		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) virtualModel3.getResource()).getDirectory()).exists());
		assertTrue(((VirtualModelResource) virtualModel3.getResource()).getIODelegate().exists());

		assertNotNull(virtualModel3.getBindingModel());
		assertEquals(2, virtualModel3.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel3),
				virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());

		/*assertEquals(6, virtualModel3.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.PROJECT_PROPERTY));
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.RC_PROPERTY));
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel3),
				virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());*/

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

		// Attempt to remove a model slot
		virtualModel3.removeFromModelSlots(ms1);
		assertEquals(4, virtualModel3.getBindingModel().getBindingVariablesCount());

		// Add it again
		virtualModel3.addToModelSlots(ms1);
		assertEquals(5, virtualModel3.getBindingModel().getBindingVariablesCount());

	}

	/*@Test
	@TestOrder(9)
	public void testFlexoBehaviourBindingModelManagement() throws SaveResourceException {
	
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
		((ExpressionAction) action1.getAssignableAction()).setExpression(new DataBinding<>("'foo'"));
		action1.setName("action1");
	
		assertTrue(action1.getAssignation().isValid());
		assertTrue(((ExpressionAction) action1.getAssignableAction()).getExpression().isValid());
	
		CreateEditionAction createEditionAction2 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction2.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createEditionAction2.setEditionActionClass(ExpressionAction.class);
		createEditionAction2.setAssignation(new DataBinding<>("aBooleanInA"));
		createEditionAction2.doAction();
		AssignationAction<?> action2 = (AssignationAction<?>) createEditionAction2.getNewEditionAction();
		((ExpressionAction) action2.getAssignableAction()).setExpression(new DataBinding<>("true"));
		action2.setName("action2");
	
		assertTrue(action2.getAssignation().isValid());
		assertTrue(((ExpressionAction) action2.getAssignableAction()).getExpression().isValid());
	
		CreateEditionAction createEditionAction3 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction3.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createEditionAction3.setEditionActionClass(ExpressionAction.class);
		createEditionAction3.setAssignation(new DataBinding<>("anIntegerInA"));
		createEditionAction3.doAction();
		AssignationAction<?> action3 = (AssignationAction<?>) createEditionAction3.getNewEditionAction();
		((ExpressionAction) action3.getAssignableAction()).setExpression(new DataBinding<>("8"));
		action3.setName("action3");
	
		assertTrue(action3.getAssignation().isValid());
		assertTrue(((ExpressionAction) action3.getAssignableAction()).getExpression().isValid());
	
		assertTrue(flexoConceptA.getFlexoBehaviours().contains(creationScheme));
		assertTrue(flexoConceptA.getCreationSchemes().contains(creationScheme));
	
		// assertEquals(3, creationScheme.getActions().size());
	
		System.out.println("FML=\n" + creationScheme.getFMLRepresentation());
	
		System.out.println("BM=" + creationScheme.getBindingModel());
	
		assertEquals(14, creationScheme.getBindingModel().getBindingVariablesCount());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.PROJECT_PROPERTY));
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.RC_PROPERTY));
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				creationScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1),
				creationScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				creationScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY).getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, creationScheme.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, creationScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, creationScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class, creationScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		assertNotNull(creationScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));
	
	}*/

	/*@Test
	@TestOrder(13)
	public void testEditionActionBindingModelManagement() throws SaveResourceException {
	
		// We programmatically implement this code:
		// CreateFlexoBehaviour createActionScheme(boolean aFlag) {
		// ..if (parameters.aFlag == true) {
		// .... anIntegerInA = 8;
		// ..}
		// ..if (parameters.aFlag == false) {
		// .... anIntegerInA = 12;
		// .... for (FlexoConceptInstance fci :
		// virtualModelInstance.flexoConceptInstances) {
		// ...... aStringInA = "foo";
		// ...... anOtherBooleanInA = (fci.toString.substring(4,3) !=
		// aStringInA)
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
	
		assertEquals(14, actionScheme.getBindingModel().getBindingVariablesCount());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.PROJECT_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.RC_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, actionScheme.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));
	
		CreateEditionAction createConditionAction1 = CreateEditionAction.actionType.makeNewAction(actionScheme.getControlGraph(), null,
				editor);
		// createConditionAction1.actionChoice =
		// CreateEditionActionChoice.ControlAction;
		createConditionAction1.setEditionActionClass(ConditionalAction.class);
		createConditionAction1.doAction();
		ConditionalAction conditional1 = (ConditionalAction) createConditionAction1.getNewEditionAction();
		conditional1.setCondition(new DataBinding<>("parameters.aFlag == true"));
	
		assertEquals(14, conditional1.getBindingModel().getBindingVariablesCount());
	
		assertNotNull(conditional1);
		assertTrue(conditional1.getCondition().isValid());
	
		CreateEditionAction createDeclareFlexoRoleInCondition1 = CreateEditionAction.actionType
				.makeNewAction(conditional1.getThenControlGraph(), null, editor);
		// createDeclareFlexoRoleInCondition1.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createDeclareFlexoRoleInCondition1.setEditionActionClass(ExpressionAction.class);
		createDeclareFlexoRoleInCondition1.setAssignation(new DataBinding<>("anIntegerInA"));
		createDeclareFlexoRoleInCondition1.doAction();
		AssignationAction<?> declarePatternRoleInCondition1 = (AssignationAction<?>) createDeclareFlexoRoleInCondition1
				.getNewEditionAction();
		((ExpressionAction) declarePatternRoleInCondition1.getAssignableAction()).setExpression(new DataBinding<>("8"));
	
		CreateEditionAction createConditionAction2 = CreateEditionAction.actionType.makeNewAction(actionScheme.getControlGraph(), null,
				editor);
		// createConditionAction2.actionChoice =
		// CreateEditionActionChoice.ControlAction;
		createConditionAction2.setEditionActionClass(ConditionalAction.class);
		createConditionAction2.doAction();
		ConditionalAction conditional2 = (ConditionalAction) createConditionAction2.getNewEditionAction();
		conditional2.setCondition(new DataBinding<>("parameters.aFlag == false"));
	
		assertNotNull(conditional2);
		assertTrue(conditional2.getCondition().isValid());
	
		assertEquals(14, conditional2.getBindingModel().getBindingVariablesCount());
	
		CreateEditionAction createDeclareFlexoRoleInCondition2 = CreateEditionAction.actionType
				.makeNewAction(conditional2.getThenControlGraph(), null, editor);
		// createDeclareFlexoRoleInCondition2.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createDeclareFlexoRoleInCondition2.setEditionActionClass(ExpressionAction.class);
		createDeclareFlexoRoleInCondition2.setAssignation(new DataBinding<>("anIntegerInA"));
		createDeclareFlexoRoleInCondition2.doAction();
		AssignationAction<?> declareFlexoRoleInCondition2 = (AssignationAction<?>) createDeclareFlexoRoleInCondition2.getNewEditionAction();
		((ExpressionAction) declareFlexoRoleInCondition2.getAssignableAction()).setExpression(new DataBinding<>("12"));
	
		assertEquals(14, declareFlexoRoleInCondition2.getBindingModel().getBindingVariablesCount());
	
		CreateEditionAction createIterationInCondition2 = CreateEditionAction.actionType.makeNewAction(conditional2.getThenControlGraph(),
				null, editor);
		// createIterationInCondition2.actionChoice =
		// CreateEditionActionChoice.ControlAction;
		createIterationInCondition2.setEditionActionClass(IterationAction.class);
		createIterationInCondition2.doAction();
		IterationAction iteration = (IterationAction) createIterationInCondition2.getNewEditionAction();
		assertNotNull(iteration);
		iteration.setIterationAction(
				iteration.getFMLModelFactory().newExpressionAction(new DataBinding<>("virtualModelInstance.flexoConceptInstances")));
		iteration.setIteratorName("fci");
	
		assertTrue(((ExpressionAction) iteration.getIterationAction()).getExpression().isValid());
	
		assertEquals(14, iteration.getBindingModel().getBindingVariablesCount());
		// assertEquals(13,
		// iteration.getControlGraphBindingModel().getBindingVariablesCount());
		assertEquals(15, iteration.getInferedBindingModel().getBindingVariablesCount());
		assertNotNull(iteration.getInferedBindingModel().bindingVariableNamed("fci"));
	
		CreateEditionAction createDeclareFlexoRoleInIteration1 = CreateEditionAction.actionType.makeNewAction(iteration.getControlGraph(),
				null, editor);
		// createDeclareFlexoRoleInIteration1.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createDeclareFlexoRoleInIteration1.setEditionActionClass(ExpressionAction.class);
		createDeclareFlexoRoleInIteration1.setAssignation(new DataBinding<>("aStringInA"));
		createDeclareFlexoRoleInIteration1.doAction();
		AssignationAction<?> declareFlexoRoleInIteration1 = (AssignationAction<?>) createDeclareFlexoRoleInIteration1.getNewEditionAction();
		assertNotNull(declareFlexoRoleInIteration1);
		((ExpressionAction) declareFlexoRoleInIteration1.getAssignableAction()).setExpression(new DataBinding<>("\"foo\""));
	
		FMLModelFactory factory = actionScheme.getFMLModelFactory();
		System.out.println("actionScheme =\n" + factory.stringRepresentation(actionScheme));
		System.out.println("FML=\n" + actionScheme.getFMLRepresentation());
		System.out.println("bm=" + declareFlexoRoleInIteration1.getInferedBindingModel());
		System.out.println("iteration.getControlGraph()=" + iteration.getControlGraph());
		System.out.println("declareFlexoRoleInIteration1=" + declareFlexoRoleInIteration1);
		System.out.println("declareFlexoRoleInIteration1.getOwner()=" + declareFlexoRoleInIteration1.getOwner());
	
		assertEquals(15, declareFlexoRoleInIteration1.getInferedBindingModel().getBindingVariablesCount());
		assertNotNull(declareFlexoRoleInIteration1.getInferedBindingModel().bindingVariableNamed("fci"));
	
		assertTrue(declareFlexoRoleInIteration1.getAssignation().isValid());
		assertTrue(((ExpressionAction) declareFlexoRoleInIteration1.getAssignableAction()).getExpression().isValid());
	
		CreateEditionAction createDeclareFlexoRoleInIteration2 = CreateEditionAction.actionType.makeNewAction(iteration.getControlGraph(),
				null, editor);
		// createDeclareFlexoRoleInIteration2.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createDeclareFlexoRoleInIteration2.setEditionActionClass(ExpressionAction.class);
		createDeclareFlexoRoleInIteration2.setAssignation(new DataBinding<>("anOtherBooleanInA"));
		createDeclareFlexoRoleInIteration2.doAction();
		AssignationAction<?> declareFlexoRoleInIteration2 = (AssignationAction<?>) createDeclareFlexoRoleInIteration2.getNewEditionAction();
		assertNotNull(declareFlexoRoleInIteration2);
		((ExpressionAction) declareFlexoRoleInIteration2.getAssignableAction())
				.setExpression(new DataBinding<>("fci.toString.substring(3,4) != aStringInA"));
	
		System.out.println("FML=\n" + actionScheme.getFMLRepresentation());
	
		assertEquals(15, declareFlexoRoleInIteration2.getBindingModel().getBindingVariablesCount());
		assertNotNull(declareFlexoRoleInIteration2.getBindingModel().bindingVariableNamed("fci"));
	
		assertTrue(declareFlexoRoleInIteration2.getAssignation().isValid());
	
		System.out.println("expression=" + ((ExpressionAction) declareFlexoRoleInIteration2.getAssignableAction()).getExpression());
		System.out.println("valid=" + ((ExpressionAction) declareFlexoRoleInIteration2.getAssignableAction()).getExpression().isValid());
		System.out.println(
				"reason=" + ((ExpressionAction) declareFlexoRoleInIteration2.getAssignableAction()).getExpression().invalidBindingReason());
	
		IterationActionBindingVariable fciBindingVariable = (IterationActionBindingVariable) declareFlexoRoleInIteration2.getBindingModel()
				.bindingVariableNamed("fci");
		System.out.println("fciBindingVariable=" + fciBindingVariable + " of " + fciBindingVariable.getClass());
		System.out.println("action=" + fciBindingVariable.getAction());
		System.out.println("type=" + fciBindingVariable.getType());
		ExpressionAction expressionAction = (ExpressionAction) ((IterationAction) fciBindingVariable.getAction()).getIterationAction();
		System.out.println("expressionAction=" + expressionAction.getExpression());
		System.out.println("valid=" + expressionAction.getExpression().isValid());
		System.out.println("reason=" + expressionAction.getExpression().invalidBindingReason());
		System.out.println("expressionAction.type=" + expressionAction.getAssignableType());
		System.out.println("expressionAction.iterator=" + expressionAction.getIteratorType());
	
		assertTrue(((ExpressionAction) declareFlexoRoleInIteration2.getAssignableAction()).getExpression().isValid());
	
		System.out.println(factory.stringRepresentation(actionScheme));
	
		System.out.println("FML=\n" + actionScheme.getFMLRepresentation());
	
		assertTrue(actionScheme.getControlGraph() instanceof Sequence);
		assertEquals(2, ((Sequence) actionScheme.getControlGraph()).getFlattenedSequence().size());
		assertTrue(conditional1.getThenControlGraph() instanceof AssignationAction);
		assertTrue(conditional2.getThenControlGraph() instanceof Sequence);
		assertEquals(2, ((Sequence) conditional2.getThenControlGraph()).getFlattenedSequence().size());
	
		// Test renaming iteratorName
		iteration.setIteratorName("iteratorHasChanged");
		assertNull(declareFlexoRoleInIteration2.getBindingModel().bindingVariableNamed("fci"));
		assertNotNull(declareFlexoRoleInIteration2.getBindingModel().bindingVariableNamed("iteratorHasChanged"));
	
		assertEquals("(iteratorHasChanged.toString.substring(3,4) != aStringInA)",
				((ExpressionAction) declareFlexoRoleInIteration2.getAssignableAction()).getExpression().toString());
		assertTrue(((ExpressionAction) declareFlexoRoleInIteration2.getAssignableAction()).getExpression().isValid());
	
		System.out.println("FML=\n" + actionScheme.getFMLRepresentation());
	
		FMLControlGraphOwner owner = declareFlexoRoleInIteration2.getOwner();
		String ownerContext = declareFlexoRoleInIteration2.getOwnerContext();
	
		// We replace the declareFlexoRoleInIteration2 by an empty cg
		EmptyControlGraph emptyControlGraph = declareFlexoRoleInIteration2.getFMLModelFactory().newEmptyControlGraph();
		owner.setControlGraph(emptyControlGraph, ownerContext);
	
		// We check that the binding model is now empty
		assertEquals(0, declareFlexoRoleInIteration2.getBindingModel().getBindingVariablesCount());
	
		// We put the removed cg back
		owner.setControlGraph(declareFlexoRoleInIteration2, ownerContext);
	
		// Check the BindingModel has been set again
		assertEquals(14, conditional2.getBindingModel().getBindingVariablesCount());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.PROJECT_PROPERTY));
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				conditional2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1),
				conditional2.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				conditional2.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY).getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, conditional2.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, conditional2.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, conditional2.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class, conditional2.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		assertNotNull(conditional2.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));
	
		// conditional2.removeFromActions(declareFlexoRoleInIteration2);
		// iteration.addToActions(declareFlexoRoleInIteration2);
	
	}*/

	/*@Test
	@TestOrder(14)
	public void testFetchRequestBindingModelManagement() throws SaveResourceException {
	
		// We programmatically implement this code:
		// ActionScheme testFetchRequest(String aString, Boolean aBoolean) {
		// ... SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = parameters.aBoolean;
		// selected.aStringInA = parameters.aString) {
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
	
		assertEquals(14, actionScheme.getBindingModel().getBindingVariablesCount());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, actionScheme.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));
	
		CreateEditionAction createSelectFlexoConceptInstanceAction = CreateEditionAction.actionType
				.makeNewAction(actionScheme.getControlGraph(), null, editor);
		// createSelectFlexoConceptInstanceAction.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createSelectFlexoConceptInstanceAction.setEditionActionClass(SelectFlexoConceptInstance.class);
		createSelectFlexoConceptInstanceAction.doAction();
	
		SelectFlexoConceptInstance selectFlexoConceptInstance = (SelectFlexoConceptInstance) createSelectFlexoConceptInstanceAction
				.getNewEditionAction();
		selectFlexoConceptInstance.setFlexoConceptType(flexoConceptA);
		selectFlexoConceptInstance.setReceiver(new DataBinding<>("virtualModelInstance"));
	
		FetchRequestCondition condition1 = selectFlexoConceptInstance.createCondition();
		condition1.setCondition(new DataBinding<>("selected.aBooleanInA = parameters.aBoolean"));
	
		FetchRequestCondition condition2 = selectFlexoConceptInstance.createCondition();
		condition2.setCondition(new DataBinding<>("selected.aStringInA = parameters.aString"));
	
		System.out.println("FML: " + actionScheme.getFMLRepresentation());
		System.out.println("BM: " + selectFlexoConceptInstance.getBindingModel());
		System.out.println("BM2: " + condition1.getBindingModel());
	
		assertEquals(14, selectFlexoConceptInstance.getBindingModel().getBindingVariablesCount());
		assertEquals(15, condition1.getBindingModel().getBindingVariablesCount());
		assertNotNull(condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED).getType());
	
	}*/

	/*@Test
	@TestOrder(15)
	public void testFetchRequestIterationBindingModelManagement() throws SaveResourceException {
	
		// We programmatically implement this code:
		// ActionScheme testFetchRequestIteration(String aString, Boolean
		// aBoolean) {
		// ... for (item in SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = parameters.aBoolean;
		// selected.aStringInA = parameters.aString)) {
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
	
		assertEquals(14, actionScheme.getBindingModel().getBindingVariablesCount());
	
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, actionScheme.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));
	
		CreateEditionAction createIterationAction = CreateEditionAction.actionType.makeNewAction(actionScheme.getControlGraph(), null,
				editor);
		// createIterationAction.actionChoice =
		// CreateEditionActionChoice.ControlAction;
		createIterationAction.setEditionActionClass(IterationAction.class);
	
		// createSelectFetchRequestIterationAction.setRequestActionClass(SelectFlexoConceptInstance.class);
		createIterationAction.doAction();
		IterationAction fetchRequestIteration = (IterationAction) createIterationAction.getNewEditionAction();
	
		SelectFlexoConceptInstance selectFlexoConceptInstance = fetchRequestIteration.getFMLModelFactory().newSelectFlexoConceptInstance();
		selectFlexoConceptInstance.setFlexoConceptType(flexoConceptA);
		selectFlexoConceptInstance.setReceiver(new DataBinding<>("virtualModelInstance"));
		fetchRequestIteration.setIterationAction(selectFlexoConceptInstance);
	
		FetchRequestCondition condition1 = selectFlexoConceptInstance.createCondition();
		condition1.setCondition(new DataBinding<>("selected.aBooleanInA = parameters.aBoolean"));
	
		FetchRequestCondition condition2 = selectFlexoConceptInstance.createCondition();
		condition2.setCondition(new DataBinding<>("selected.aStringInA = parameters.aString"));
	
		assertEquals(14, fetchRequestIteration.getBindingModel().getBindingVariablesCount());
		assertEquals(15, condition1.getBindingModel().getBindingVariablesCount());
		assertNotNull(condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED).getType());
	
		CreateEditionAction createAssignationAction = CreateEditionAction.actionType.makeNewAction(fetchRequestIteration.getControlGraph(),
				null, editor);
		// createAssignationAction.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createAssignationAction.setEditionActionClass(ExpressionAction.class);
		createAssignationAction.setDeclarationVariableName("name");
		createAssignationAction.doAction();
		DeclarationAction<?> assignation1 = (DeclarationAction<?>) createAssignationAction.getNewEditionAction();
		((ExpressionAction) assignation1.getAssignableAction()).setExpression(new DataBinding<>("item.aStringInA"));
	
		CreateEditionAction createAssignationAction2 = CreateEditionAction.actionType.makeNewAction(fetchRequestIteration.getControlGraph(),
				null, editor);
		// createAssignationAction2.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createAssignationAction2.setEditionActionClass(ExpressionAction.class);
		createAssignationAction2.setAssignation(new DataBinding<>("item.aStringInA"));
		createAssignationAction2.doAction();
		AssignationAction<?> assignation2 = (AssignationAction<?>) createAssignationAction2.getNewEditionAction();
		((ExpressionAction) assignation2.getAssignableAction()).setExpression(new DataBinding<>("name+\"foo\""));
	
		assertTrue(((ExpressionAction) assignation1.getAssignableAction()).getExpression().isValid());
		assertTrue(assignation2.getAssignation().isValid());
	
		assertTrue(((ExpressionAction) assignation2.getAssignableAction()).getExpression().isValid());
	
		assertEquals(14, fetchRequestIteration.getBindingModel().getBindingVariablesCount());
	
		assertEquals(15, condition1.getBindingModel().getBindingVariablesCount());
		assertNotNull(condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				condition1.getBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED).getType());
	
		assertEquals(16, assignation1.getInferedBindingModel().getBindingVariablesCount());
		assertNull(assignation1.getInferedBindingModel().bindingVariableNamed(FetchRequestCondition.SELECTED));
		assertNotNull(assignation1.getBindingModel().bindingVariableNamed(fetchRequestIteration.getIteratorName()));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				assignation1.getBindingModel().bindingVariableNamed(fetchRequestIteration.getIteratorName()).getType());
		assertNotNull(assignation1.getInferedBindingModel().bindingVariableNamed("name"));
		assertEquals(String.class, assignation1.getInferedBindingModel().bindingVariableNamed("name").getType());
	
		// System.out.println("BM1: " + assignation1.getBindingModel());
		// System.out.println("BM2: " + assignation2.getBindingModel());
	
		fetchRequestIteration.setIteratorName("myConceptInstance");
		assertNotNull(assignation1.getBindingModel().bindingVariableNamed("myConceptInstance"));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				assignation1.getBindingModel().bindingVariableNamed("myConceptInstance").getType());
	
		assertTrue(((ExpressionAction) assignation1.getAssignableAction()).getExpression().isValid());
		assertTrue(assignation2.getAssignation().isValid());
		assertTrue(((ExpressionAction) assignation2.getAssignableAction()).getExpression().isValid());
	
		// System.out.println("FML: " + actionScheme.getFMLRepresentation());
	
		assertEquals("myConceptInstance.aStringInA", assignation2.getAssignation().toString());
	
	}*/

	/*@Test
	@TestOrder(16)
	public void testMatchFlexoConceptInstanceBindingModelManagement() throws SaveResourceException {
	
		// We programmatically implement this code:
		// ActionScheme testFetchRequestIteration(String aString, Boolean
		// aBoolean) {
		// ... for (item in SelectFlexoConceptInstance as FlexoConceptA where
		// ......(selected.aBooleanInA = parameters.aBoolean;
		// selected.aStringInA = parameters.aString)) {
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
	
		assertEquals(14, actionScheme.getBindingModel().getBindingVariablesCount());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.PROJECT_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1),
				actionScheme.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				actionScheme.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY).getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, actionScheme.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, actionScheme.getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class, actionScheme.getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY));
		assertNotNull(actionScheme.getBindingModel().bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY));
	
		CreateEditionAction createSelectFetchRequestIterationAction = CreateEditionAction.actionType
				.makeNewAction(actionScheme.getControlGraph(), null, editor);
		// createSelectFetchRequestIterationAction.actionChoice =
		// CreateEditionActionChoice.ControlAction;
		createSelectFetchRequestIterationAction.setEditionActionClass(IterationAction.class);
		createSelectFetchRequestIterationAction.doAction();
		IterationAction fetchRequestIteration = (IterationAction) createSelectFetchRequestIterationAction.getNewEditionAction();
	
		SelectFlexoConceptInstance selectFlexoConceptInstance = fetchRequestIteration.getFMLModelFactory().newSelectFlexoConceptInstance();
		selectFlexoConceptInstance.setFlexoConceptType(flexoConceptA);
		selectFlexoConceptInstance.setReceiver(new DataBinding<>("virtualModelInstance"));
		fetchRequestIteration.setIterationAction(selectFlexoConceptInstance);
	
		FetchRequestCondition condition1 = selectFlexoConceptInstance.createCondition();
		condition1.setCondition(new DataBinding<>("selected.aBooleanInA = parameters.aBoolean"));
	
		FetchRequestCondition condition2 = selectFlexoConceptInstance.createCondition();
		condition2.setCondition(new DataBinding<>("selected.aStringInA = parameters.aString"));
	
		CreateEditionAction createMatchFlexoConceptInstanceAction = CreateEditionAction.actionType
				.makeNewAction(fetchRequestIteration.getControlGraph(), null, editor);
		// createMatchFlexoConceptInstanceAction.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createMatchFlexoConceptInstanceAction.setEditionActionClass(MatchFlexoConceptInstance.class);
		createMatchFlexoConceptInstanceAction.doAction();
		MatchFlexoConceptInstance matchFlexoConceptInstance = (MatchFlexoConceptInstance) createMatchFlexoConceptInstanceAction
				.getNewEditionAction();
		matchFlexoConceptInstance.setFlexoConceptType(flexoConceptA);
		matchFlexoConceptInstance.setReceiver(new DataBinding<>("virtualModelInstance"));
	
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
	
		CreateFlexoBehaviourParameter createBooleanParameter2 = CreateFlexoBehaviourParameter.actionType.makeNewAction(creationScheme, null,
				editor);
		createBooleanParameter2.setFlexoBehaviourParameterClass(CheckboxParameter.class);
		createBooleanParameter2.setParameterName("aBooleanParameter");
		createBooleanParameter2.doAction();
		FlexoBehaviourParameter creationSchemeParam2 = createBooleanParameter2.getNewParameter();
		assertNotNull(creationSchemeParam2);
		assertTrue(creationScheme.getParameters().contains(creationSchemeParam2));
	
		CreateEditionAction createEditionAction1 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction1.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createEditionAction1.setEditionActionClass(ExpressionAction.class);
		createEditionAction1.setAssignation(new DataBinding<>("aStringInA"));
		createEditionAction1.doAction();
		AssignationAction<?> action1 = (AssignationAction<?>) createEditionAction1.getNewEditionAction();
		((ExpressionAction) action1.getAssignableAction()).setExpression(new DataBinding<>("parameters.aStringParameter"));
	
		assertTrue(action1.getAssignation().isValid());
		assertTrue(((ExpressionAction) action1.getAssignableAction()).getExpression().isValid());
	
		CreateEditionAction createEditionAction2 = CreateEditionAction.actionType.makeNewAction(creationScheme.getControlGraph(), null,
				editor);
		// createEditionAction2.actionChoice =
		// CreateEditionActionChoice.BuiltInAction;
		createEditionAction2.setEditionActionClass(ExpressionAction.class);
		createEditionAction2.setAssignation(new DataBinding<>("aBooleanInA"));
		createEditionAction2.doAction();
		AssignationAction<?> action2 = (AssignationAction<?>) createEditionAction2.getNewEditionAction();
		((ExpressionAction) action2.getAssignableAction()).setExpression(new DataBinding<>("parameters.aBooleanParameter"));
	
		assertTrue(action2.getAssignation().isValid());
		assertTrue(((ExpressionAction) action2.getAssignableAction()).getExpression().isValid());
	
		assertNotNull(actionScheme);
		System.out.println("FML=" + actionScheme.getFMLRepresentation());
	
		matchFlexoConceptInstance.setCreationScheme(creationScheme);
	
		// We check here that matching criterias were updated
		assertEquals(4, matchFlexoConceptInstance.getMatchingCriterias().size());
	
		MatchingCriteria criteria1 = matchFlexoConceptInstance.getMatchingCriteria(flexoConceptA.getAccessibleProperty("aStringInA"));
		MatchingCriteria criteria2 = matchFlexoConceptInstance.getMatchingCriteria(flexoConceptA.getAccessibleProperty("aBooleanInA"));
		MatchingCriteria criteria3 = matchFlexoConceptInstance.getMatchingCriteria(flexoConceptA.getAccessibleProperty("anIntegerInA"));
		MatchingCriteria criteria4 = matchFlexoConceptInstance
				.getMatchingCriteria(flexoConceptA.getAccessibleProperty("anOtherBooleanInA"));
	
		assertNotNull(criteria1);
		assertNotNull(criteria2);
		assertNotNull(criteria3);
		assertNotNull(criteria4);
	
		criteria1.setValue(new DataBinding<>("item.aStringInA"));
	
		System.out.println("FML=" + actionScheme.getFMLRepresentation());
	
		assertTrue(criteria1.getValue().isValid());
	
		MatchingCriteria criteria1bis = matchFlexoConceptInstance.getMatchingCriteria(flexoConceptA.getAccessibleProperty("aStringInA"));
		assertSame(criteria1, criteria1bis);
	
		// We add a property
		// We check here that matching criterias were updated: an other criteria
		// should appear
	
		CreatePrimitiveRole createRole = CreatePrimitiveRole.actionType.makeNewAction(flexoConceptA, null, editor);
		createRole.setRoleName("anOtherIntegerInA");
		createRole.setPrimitiveType(PrimitiveType.Integer);
		createRole.doAction();
		FlexoRole newRole = createRole.getNewFlexoRole();
	
		assertEquals(5, matchFlexoConceptInstance.getMatchingCriterias().size());
		assertNotNull(matchFlexoConceptInstance.getMatchingCriteria(newRole));
	
		// We remove the property
		// We check here that matching criterias were updated: the criteria
		// should disappear
		flexoConceptA.removeFromFlexoProperties(newRole);
	
		assertEquals(4, matchFlexoConceptInstance.getMatchingCriterias().size());
	
		// We check here that create parameters were updated
	
		assertEquals(2, matchFlexoConceptInstance.getParameters().size());
	
		CreateFlexoConceptInstanceParameter createFCIParam1 = matchFlexoConceptInstance.getParameter(creationSchemeParam1);
		CreateFlexoConceptInstanceParameter createFCIParam2 = matchFlexoConceptInstance.getParameter(creationSchemeParam2);
		assertNotNull(createFCIParam1);
		assertNotNull(createFCIParam2);
	
		createFCIParam1.setValue(new DataBinding<>("item.aStringInA"));
		createFCIParam2.setValue(new DataBinding<>("true"));
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
		createFCIParam1.setValue(new DataBinding<>("item.aStringInA"));
		createFCIParam2.setValue(new DataBinding<>("true"));
		assertTrue(createFCIParam1.getValue().isValid());
		assertTrue(createFCIParam2.getValue().isValid());
	
		// We try to add a parameter
		CreateFlexoBehaviourParameter createBooleanParameter3 = CreateFlexoBehaviourParameter.actionType.makeNewAction(creationScheme, null,
				editor);
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
	
		assertEquals(14, fetchRequestIteration.getBindingModel().getBindingVariablesCount());
	
		assertEquals(15, condition1.getBindingModel().getBindingVariablesCount());
	
		assertEquals(15, createFCIParam1.getBindingModel().getBindingVariablesCount());
	
		System.out.println("FML: " + actionScheme.getFMLRepresentation());
	
	}*/

	/*@Test
	@TestOrder(17)
	public void testFlexoConceptInstanceInspector() {
	
		assertSame(flexoConceptA.getBindingModel(), flexoConceptA.getInspector().getBindingModel().getBaseBindingModel());
	
		assertEquals(12, flexoConceptA.getInspector().getBindingModel().getBindingVariablesCount());
		assertNotNull(
				flexoConceptA.getInspector().getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(
				flexoConceptA.getInspector().getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getInspector().getBindingModel().bindingVariableNamed(VirtualModelBindingModel.PROJECT_PROPERTY));
		assertNotNull(flexoConceptA.getInspector().getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint),
				flexoConceptA.getInspector().getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(
				flexoConceptA.getInspector().getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getInspector().getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1), flexoConceptA.getInspector().getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(flexoConceptA.getInspector().getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				flexoConceptA.getInspector().getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY).getType());
		assertNotNull(flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class, flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class, flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class, flexoConceptA.getInspector().getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
	}*/

	/*@Test
	@TestOrder(18)
	public void testFlexoConceptInstanceRenderer() {
	
		flexoConceptA.getInspector().setRenderer(new DataBinding<>("\"FlexoConceptA:\"+instance.aStringInA"));
		assertTrue(flexoConceptA.getInspector().getRenderer().isValid());
	
		assertEquals(13, flexoConceptA.getInspector().getFormatter().getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.PROJECT_PROPERTY));
		assertNotNull(
				flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(viewPoint), flexoConceptA.getInspector().getFormatter()
				.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel()
				.bindingVariableNamed(FlexoConceptBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel()
				.bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getFlexoConceptInstanceType(virtualModel1),
				flexoConceptA.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());
		assertNotNull(
				flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				flexoConceptA.getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY).getType());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class,
				flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.class,
				flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.class,
				flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("anIntegerInA").getType());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("anOtherBooleanInA"));
		assertEquals(Boolean.class,
				flexoConceptA.getInspector().getFormatter().getBindingModel().bindingVariableNamed("anOtherBooleanInA").getType());
		assertNotNull(flexoConceptA.getInspector().getFormatter().getBindingModel()
				.bindingVariableNamed(FlexoConceptInspector.FORMATTER_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), flexoConceptA.getInspector().getFormatter()
				.getBindingModel().bindingVariableNamed(FlexoConceptInspector.FORMATTER_INSTANCE_PROPERTY).getType());
	
	}*/

	/*@Test
	@TestOrder(19)
	public void testViewPointIsValid() {
	
		assertVirtualModelIsValid(viewPoint);
	
	}*/

	/*@Test
	@TestOrder(20)
	public void testInstanciateVirtualModelInstances() {
	
		log("testInstanciateVirtualModelInstances()");
	
		editor = createProject("TestProject");
		project = editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
		assertTrue(project.getProjectDataResource().getIODelegate().exists());
	
		CreateViewInFolder action = CreateViewInFolder.actionType.makeNewAction(project.getViewLibrary().getRootFolder(), null, editor);
		action.setNewViewName("MyView");
		action.setNewViewTitle("Test creation of a new view");
		action.setViewpointResource((VirtualModelResource) viewPoint.getResource());
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		newView = action.getNewView();
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
		assertTrue(((ViewResource) newView.getResource()).getDirectory() != null);
		assertTrue(((ViewResource) newView.getResource()).getIODelegate().exists());
	
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
		// assertTrue(((ViewResource)
		// newView.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource)
		// newView.getResource()).getFile().exists());
		assertTrue(((ViewResource) newView.getResource()).getDirectory() != null);
		assertTrue(((ViewResource) newView.getResource()).getIODelegate().exists());
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
		// assertTrue(((ViewResource)
		// newView.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource)
		// newView.getResource()).getFile().exists());
		assertTrue(((ViewResource) newView.getResource()).getDirectory() != null);
		assertTrue(((ViewResource) newView.getResource()).getIODelegate().exists());
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
		// assertTrue(((ViewResource)
		// newView.getResource()).getDirectory().exists());
		// assertTrue(((ViewResource)
		// newView.getResource()).getFile().exists());
		assertTrue(((ViewResource) newView.getResource()).getDirectory() != null);
		assertTrue(((ViewResource) newView.getResource()).getIODelegate().exists());
		assertEquals(virtualModel3, vmi3.getFlexoConcept());
		assertEquals(virtualModel3, vmi3.getVirtualModel());
	
		assertNotNull(virtualModel3.getBindingModel());
		assertEquals(9, virtualModel3.getBindingModel().getBindingVariablesCount());
	
		assertNotNull(virtualModel3.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
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
		checkBindingVariableAccess(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY, virtualModel3, vmi3, vmi3);
	
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
	
		vmi3.setFlexoActor("toto", (FlexoRole) vmi3.getVirtualModel().getAccessibleProperty("aStringInVM3"));
		checkBindingVariableAccess("aStringInVM3", virtualModel3, vmi3, "toto");
	
	}*/

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
