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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.foundation.fml.action.CreateAbstractProperty;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole;
import org.openflexo.foundation.fml.binding.ViewPointBindingModel;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.ViewPointResourceFactory;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.FileUtils;

/**
 * This unit test is intented to test {@link FlexoConceptInstanceType} management
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestFlexoConceptInstanceType extends OpenflexoProjectAtRunTimeTestCase {

	public static final String VIEWPOINT_NAME = "TestFlexoConceptInstanceViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestFlexoConceptInstanceViewPoint";
	public static final String VIRTUAL_MODEL_NAME = "TestVirtualModel";

	static FlexoEditor editor;
	static ViewPoint viewPoint;
	static VirtualModel virtualModel;

	static FlexoConcept flexoConceptA;
	static FlexoConcept flexoConceptB;
	static FlexoConcept flexoConceptC;

	public static AbstractProperty<FlexoConceptInstanceType> property4InA;
	public static AbstractProperty<FlexoConceptInstanceType> property4InB;
	public static FlexoConceptInstanceRole property4InC;

	static FlexoProject project;
	static View newView;
	static VirtualModelInstance vmi;
	static FlexoConceptInstance a;

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
	 * 
	 * @throws ModelDefinitionException
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(2)
	public void testCreateViewPoint() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		ViewPointResourceFactory factory = fmlTechnologyAdapter.getViewPointResourceFactory();

		ViewPointResource newViewPointResource = factory.makeViewPointResource(VIEWPOINT_NAME, VIEWPOINT_URI,
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(),
				fmlTechnologyAdapter.getTechnologyContextManager(), true);
		viewPoint = newViewPointResource.getLoadedResourceData();

		// viewPoint = ViewPointImpl.newViewPoint(VIEWPOINT_NAME, VIEWPOINT_URI, resourceCenter.getDirectory(),
		// serviceManager.getViewPointLibrary(), resourceCenter);
		// assertTrue(((ViewPointResource) viewPoint.getResource()).getDirectory().exists());
		// assertTrue(((ViewPointResource) viewPoint.getResource()).getFile().exists());
		assertTrue(((ViewPointResource) viewPoint.getResource()).getDirectory() != null);
		assertTrue(((ViewPointResource) viewPoint.getResource()).getFlexoIODelegate().exists());

		System.out.println("ViewPoint BindingModel = " + viewPoint.getBindingModel());
		assertNotNull(viewPoint.getBindingModel());
		assertEquals(6, viewPoint.getBindingModel().getBindingVariablesCount());
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(ViewPointBindingModel.PROJECT_PROPERTY));
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(ViewPointBindingModel.RC_PROPERTY));
		assertNotNull(viewPoint.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));

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
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getViewPointResourceFactory().getVirtualModelResourceFactory();
		VirtualModelResource newVMResource = factory.makeVirtualModelResource(VIRTUAL_MODEL_NAME, viewPoint.getViewPointResource(),
				fmlTechnologyAdapter.getTechnologyContextManager(), true);
		virtualModel = newVMResource.getLoadedResourceData();

		// virtualModel = VirtualModelImpl.newVirtualModel(VIRTUAL_MODEL_NAME, viewPoint);
		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) virtualModel.getResource()).getDirectory()).exists());
		assertTrue(((VirtualModelResource) virtualModel.getResource()).getFlexoIODelegate().exists());

		assertNotNull(virtualModel.getBindingModel());
		assertEquals(10, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.PROJECT_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.RC_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel),
				virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

	}

	/**
	 * Test FlexoConceptA creation, with 6 properties
	 */
	@Test
	@TestOrder(4)
	public void testCreateFlexoConceptA() throws SaveResourceException {

		log("testCreateFlexoConceptA()");

		CreateFlexoConcept addConceptA = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addConceptA.setNewFlexoConceptName("FlexoConceptA");
		addConceptA.doAction();

		flexoConceptA = addConceptA.getNewFlexoConcept();

		assertNotNull(flexoConceptA);

		CreateAbstractProperty createProperty4inA = CreateAbstractProperty.actionType.makeNewAction(flexoConceptA, null, editor);
		createProperty4inA.setPropertyName("property4");
		createProperty4inA.setPropertyType(FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE);
		createProperty4inA.doAction();
		assertTrue(createProperty4inA.hasActionExecutionSucceeded());
		assertNotNull(property4InA = (AbstractProperty<FlexoConceptInstanceType>) createProperty4inA.getNewFlexoProperty());

		assertEquals(1, flexoConceptA.getFlexoProperties().size());
		assertEquals(1, flexoConceptA.getDeclaredProperties().size());
		assertEquals(1, flexoConceptA.getAccessibleProperties().size());
		assertTrue(flexoConceptA.getDeclaredProperties().contains(createProperty4inA.getNewFlexoProperty()));
		assertEquals(flexoConceptA.getDeclaredProperties(), flexoConceptA.getAccessibleProperties());

		assertSame(property4InA, flexoConceptA.getAccessibleProperty("property4"));
		assertEquals(FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE, property4InA.getType());
		assertEquals(FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE, property4InA.getResultingType());

		// Because concept define some abstract properties, it is abstract
		assertTrue(flexoConceptA.isAbstract());

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

		((VirtualModelResource) virtualModel.getResource()).save(null);

	}

	/**
	 * Test FlexoConceptB creation, define some overriden properties
	 */
	@Test
	@TestOrder(5)
	public void testCreateFlexoConceptB() throws SaveResourceException, InconsistentFlexoConceptHierarchyException {

		log("testCreateFlexoConceptB()");

		CreateFlexoConcept addConceptB = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addConceptB.setNewFlexoConceptName("FlexoConceptB");
		addConceptB.doAction();

		flexoConceptB = addConceptB.getNewFlexoConcept();

		flexoConceptB.addToParentFlexoConcepts(flexoConceptA);

		System.out.println("flexoConceptB = " + flexoConceptB);
		assertNotNull(flexoConceptB);

		CreateAbstractProperty createProperty4inB = CreateAbstractProperty.actionType.makeNewAction(flexoConceptB, null, editor);
		createProperty4inB.setPropertyName("property4");
		createProperty4inB.setPropertyType(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA));
		createProperty4inB.doAction();
		assertTrue(createProperty4inB.hasActionExecutionSucceeded());
		assertNotNull(property4InB = (AbstractProperty<FlexoConceptInstanceType>) createProperty4inB.getNewFlexoProperty());

		assertEquals(1, flexoConceptB.getFlexoProperties().size());
		assertEquals(1, flexoConceptB.getDeclaredProperties().size());
		assertEquals(1, flexoConceptB.getAccessibleProperties().size());

		assertTrue(flexoConceptB.getDeclaredProperties().contains(createProperty4inB.getNewFlexoProperty()));

		assertSame(property4InB, flexoConceptB.getAccessibleProperty("property4"));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), property4InB.getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), property4InB.getResultingType());
		assertSameList(property4InB.getSuperProperties(), property4InA);
		assertSameList(property4InB.getAllSuperProperties(), property4InA);

		// Because concept define some abstract properties, it is abstract
		assertTrue(flexoConceptB.isAbstract());

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

		((VirtualModelResource) virtualModel.getResource()).save(null);

	}

	/**
	 * Test FlexoConceptC creation, define some overriden properties
	 */
	@Test
	@TestOrder(6)
	public void testCreateFlexoConceptC() throws SaveResourceException, InconsistentFlexoConceptHierarchyException {

		log("testCreateFlexoConceptC()");

		CreateFlexoConcept addConceptC = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addConceptC.setNewFlexoConceptName("FlexoConceptC");
		addConceptC.doAction();

		flexoConceptC = addConceptC.getNewFlexoConcept();

		flexoConceptC.addToParentFlexoConcepts(flexoConceptB);

		System.out.println("flexoConceptC = " + flexoConceptC);
		assertNotNull(flexoConceptC);

		CreateFlexoConceptInstanceRole createProperty4InC = CreateFlexoConceptInstanceRole.actionType.makeNewAction(flexoConceptC, null,
				editor);
		createProperty4InC.setRoleName("property4");
		createProperty4InC.setFlexoConceptInstanceType(flexoConceptB);
		createProperty4InC.setCardinality(PropertyCardinality.ZeroOne);
		createProperty4InC.doAction();
		assertTrue(createProperty4InC.hasActionExecutionSucceeded());
		assertNotNull(property4InC = createProperty4InC.getNewFlexoRole());

		assertEquals(1, flexoConceptC.getFlexoProperties().size());
		assertEquals(1, flexoConceptC.getDeclaredProperties().size());
		assertEquals(1, flexoConceptC.getAccessibleProperties().size());

		assertTrue(flexoConceptC.getDeclaredProperties().contains(createProperty4InC.getNewFlexoProperty()));

		assertSame(property4InC, flexoConceptC.getAccessibleProperty("property4"));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptB), property4InC.getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptB), property4InC.getResultingType());
		assertSameList(property4InC.getSuperProperties(), property4InB);
		assertSameList(property4InC.getAllSuperProperties(), property4InA, property4InB);

		// Because concept define no abstract properties, it is not abstract
		assertFalse(flexoConceptC.isAbstract());

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

		((VirtualModelResource) virtualModel.getResource()).save(null);

	}

	@Test
	@TestOrder(19)
	public void testViewPointIsValid() {

		assertViewPointIsValid(viewPoint);

	}

	/**
	 * Reload the ViewPoint<br>
	 * We first re-init a full ServiceManager, and copy the just created ViewPoint<br>
	 * The goal is to let the FileSystem monitoring system detects the new directory and instantiate ViewPoint
	 */
	@Test
	@TestOrder(20)
	public void testReloadViewPoint() {

		log("testReloadViewPoint()");

		ViewPointResource viewPointResource = (ViewPointResource) viewPoint.getResource();

		instanciateTestServiceManager();

		File directory = ResourceLocator.retrieveResourceAsFile(viewPointResource.getDirectory());
		File newDirectory = new File(((FileSystemBasedResourceCenter) resourceCenter).getDirectory(), directory.getName());
		newDirectory.mkdirs();

		try {
			FileUtils.copyContentDirToDir(directory, newDirectory);
			// We wait here for the thread monitoring ResourceCenters to detect new files
			((FileSystemBasedResourceCenter) resourceCenter).performDirectoryWatchingNow();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ViewPointResource retrievedVPResource = serviceManager.getViewPointLibrary().getViewPointResource(VIEWPOINT_URI);
		assertNotNull(retrievedVPResource);

		ViewPoint reloadedViewPoint = retrievedVPResource.getViewPoint();
		assertEquals(reloadedViewPoint, reloadedViewPoint.getViewPoint());
		assertEquals(reloadedViewPoint, reloadedViewPoint.getVirtualModel());
		assertEquals(null, reloadedViewPoint.getOwningVirtualModel());
		assertEquals(reloadedViewPoint, reloadedViewPoint.getFlexoConcept());
		assertEquals(reloadedViewPoint, reloadedViewPoint.getResourceData());

		AbstractVirtualModel<?> reloadedVirtualModel = reloadedViewPoint.getVirtualModelNamed(VIRTUAL_MODEL_NAME);
		assertNotNull(reloadedVirtualModel);

		assertNotNull(flexoConceptA = reloadedVirtualModel.getFlexoConcept("FlexoConceptA"));
		assertNotNull(flexoConceptB = reloadedVirtualModel.getFlexoConcept("FlexoConceptB"));
		assertNotNull(flexoConceptC = reloadedVirtualModel.getFlexoConcept("FlexoConceptC"));

		assertEquals(1, flexoConceptA.getFlexoProperties().size());
		assertEquals(1, flexoConceptA.getDeclaredProperties().size());
		assertEquals(1, flexoConceptA.getAccessibleProperties().size());

		assertNotNull(property4InA = (AbstractProperty<FlexoConceptInstanceType>) flexoConceptA.getAccessibleProperty("property4"));
		assertEquals(FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE, property4InA.getType());
		assertEquals(FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE, property4InA.getResultingType());

		assertTrue(flexoConceptA.getDeclaredProperties().contains(property4InA));
		assertEquals(flexoConceptA.getDeclaredProperties(), flexoConceptA.getAccessibleProperties());

		// Because concept define some abstract properties, it is abstract
		assertTrue(flexoConceptA.isAbstract());

		assertNotNull(property4InB = (AbstractProperty<FlexoConceptInstanceType>) flexoConceptB.getAccessibleProperty("property4"));

		assertEquals(1, flexoConceptB.getFlexoProperties().size());
		assertEquals(1, flexoConceptB.getDeclaredProperties().size());
		assertEquals(1, flexoConceptB.getAccessibleProperties().size());

		assertTrue(flexoConceptB.getDeclaredProperties().contains(property4InB));
		assertSame(property4InB, flexoConceptB.getAccessibleProperty("property4"));

		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), property4InB.getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA), property4InB.getResultingType());
		assertSameList(property4InB.getSuperProperties(), property4InA);
		assertSameList(property4InB.getAllSuperProperties(), property4InA);

		// Because concept define some abstract properties, it is abstract
		assertTrue(flexoConceptB.isAbstract());

		assertNotNull(property4InC = (FlexoConceptInstanceRole) flexoConceptC.getAccessibleProperty("property4"));

		assertEquals(1, flexoConceptC.getFlexoProperties().size());
		assertEquals(1, flexoConceptC.getDeclaredProperties().size());
		assertEquals(1, flexoConceptC.getAccessibleProperties().size());

		assertTrue(flexoConceptC.getDeclaredProperties().contains(property4InC));

		assertSame(property4InC, flexoConceptC.getAccessibleProperty("property4"));

		assertTrue(property4InC instanceof FlexoConceptInstanceRole);
		assertSame(property4InC.getViewPoint(), reloadedViewPoint);

		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptB), property4InC.getType());
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptB), property4InC.getResultingType());
		assertSameList(property4InC.getSuperProperties(), property4InB);
		assertSameList(property4InC.getAllSuperProperties(), property4InA, property4InB);

		// Because concept define no abstract properties, it is not abstract
		assertFalse(flexoConceptC.isAbstract());

	}
}
