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

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.binding.FlexoPropertyBindingVariable;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test {@link BindingModel} management with single inheritance
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestSingleInheritanceBindingModelManagement extends OpenflexoProjectAtRunTimeTestCase {

	public static final String VIEWPOINT_NAME = "TestSingleInheritanceViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestSingleInheritanceViewPoint";
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
		CompilationUnitResourceFactory factory = fmlTechnologyAdapter.getCompilationUnitResourceFactory();

		CompilationUnitResource newVirtualModelResource = factory.makeTopLevelCompilationUnitResource(VIEWPOINT_NAME, VIEWPOINT_URI,
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(), true);
		viewPoint = newVirtualModelResource.getLoadedResourceData().getVirtualModel();

		assertTrue(viewPoint.getResource().getDirectory() != null);
		assertTrue(viewPoint.getResource().getIODelegate().exists());

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
		CompilationUnitResourceFactory factory = fmlTechnologyAdapter.getCompilationUnitResourceFactory();
		CompilationUnitResource newVMResource = factory.makeContainedCompilationUnitResource("VM1", viewPoint.getCompilationUnitResource(),
				true);
		virtualModel1 = newVMResource.getLoadedResourceData().getVirtualModel();

		// virtualModel1 = VirtualModelImpl.newVirtualModel("VM1", viewPoint);
		assertTrue(ResourceLocator.retrieveResourceAsFile(virtualModel1.getResource().getDirectory()).exists());
		assertTrue(virtualModel1.getResource().getIODelegate().exists());

		assertNotNull(virtualModel1.getBindingModel());

		System.out.println("BM=" + virtualModel1.getBindingModel());

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

		virtualModel1.getResource().save();

		System.out.println("Saved: " + virtualModel1.getResource().getIODelegate());

		System.out.println("FlexoConcept BindingModel = " + flexoConceptA.getBindingModel());

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
	public void testFlexoRoleBindingModelManagement() {

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
		assertEquals(Boolean.TYPE, flexoConceptA.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(flexoConceptA.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, flexoConceptA.getBindingModel().bindingVariableNamed("anIntegerInA").getType());

		PrimitiveRole<?> aStringInA = (PrimitiveRole<?>) flexoConceptA.getAccessibleProperty("aStringInA");
		assertNotNull(aStringInA);

		FlexoPropertyBindingVariable bv = (FlexoPropertyBindingVariable) flexoConceptA.getBindingModel().bindingVariableNamed("aStringInA");
		assertNotNull(bv);

		System.out.println("FlexoConcept BindingModel = " + flexoConceptA.getBindingModel());
	}

	@Test
	@TestOrder(6)
	public void testFlexoConceptBindingModelManagement() throws SaveResourceException, InconsistentFlexoConceptHierarchyException {

		CreateFlexoConcept addFlexoConceptB = CreateFlexoConcept.actionType.makeNewAction(virtualModel1, null, editor);
		addFlexoConceptB.setNewFlexoConceptName("FlexoConceptB");
		addFlexoConceptB.addToParentConcepts(flexoConceptA);
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

		virtualModel1.getResource().save();

		assertEquals(7, flexoConceptB.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.SUPER_PROPERTY));
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptB),
				flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA),
				flexoConceptB.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.SUPER_PROPERTY).getType());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed("aStringInB"));
		assertEquals(String.class, flexoConceptB.getBindingModel().bindingVariableNamed("aStringInB").getType());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConceptB.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.TYPE, flexoConceptB.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(flexoConceptB.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, flexoConceptB.getBindingModel().bindingVariableNamed("anIntegerInA").getType());

		System.out.println("FlexoConcept BindingModel = " + flexoConceptC.getBindingModel());

		assertEquals(8, flexoConceptC.getBindingModel().getBindingVariablesCount());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY));
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.SUPER_PROPERTY));
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel1),
				flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.CONTAINER_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptC),
				flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.THIS_PROPERTY).getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptB),
				flexoConceptC.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.SUPER_PROPERTY).getType());

		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aStringInA"));
		assertEquals(String.class, flexoConceptC.getBindingModel().bindingVariableNamed("aStringInA").getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aBooleanInA"));
		assertEquals(Boolean.TYPE, flexoConceptC.getBindingModel().bindingVariableNamed("aBooleanInA").getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("anIntegerInA"));
		assertEquals(Integer.TYPE, flexoConceptC.getBindingModel().bindingVariableNamed("anIntegerInA").getType());

		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aStringInB"));
		assertEquals(String.class, flexoConceptC.getBindingModel().bindingVariableNamed("aStringInB").getType());
		assertNotNull(flexoConceptC.getBindingModel().bindingVariableNamed("aStringInC"));
		assertEquals(String.class, flexoConceptC.getBindingModel().bindingVariableNamed("aStringInC").getType());

	}

}
