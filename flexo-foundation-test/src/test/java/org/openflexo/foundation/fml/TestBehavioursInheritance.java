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
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour.BehaviourParameterEntry;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.binding.ViewPointBindingModel;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.ViewPointResourceFactory;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.FileUtils;

/**
 * This unit test is intented to test {@link FlexoConcept} inheritance features, as well as "isAbstract" management and
 * {@link FlexoProperty} inheritance and shadowing
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestBehavioursInheritance extends OpenflexoProjectAtRunTimeTestCase {

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestViewPoint";
	public static final String VIRTUAL_MODEL_NAME = "TestVirtualModel";

	static FlexoEditor editor;
	static ViewPoint viewPoint;
	static VirtualModel virtualModel;

	static FlexoConcept flexoConceptA;
	static FlexoConcept flexoConceptB;
	static FlexoConcept flexoConceptC;

	public static FlexoBehaviour m1InA; // m1()
	public static FlexoBehaviour m2InA; // m2(String)
	public static FlexoBehaviour m5InA; // m5(Boolean)
	public static FlexoBehaviour m6InA; // m6(String)

	public static FlexoBehaviour m2InB; // m2(String)
	public static FlexoBehaviour m4InB; // m4(Integer)
	public static FlexoBehaviour m6InB; // m6(String)

	public static FlexoBehaviour m1InC; // m1()
	public static FlexoBehaviour m3InC; // m3(Double)
	public static FlexoBehaviour m4InC; // m4(Boolean)
	public static FlexoBehaviour m6InC; // m6(String)

	// static FlexoProject project;
	// static View newView;
	// static VirtualModelInstance vmi;
	// static FlexoConceptInstance a;

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
		assertEquals(4, viewPoint.getBindingModel().getBindingVariablesCount());
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
		assertEquals(6, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.PROJECT_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.RC_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
		assertEquals(ViewType.getViewType(viewPoint),
				virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY).getType());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY));
		assertEquals(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel),
				virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.VIRTUAL_MODEL_INSTANCE_PROPERTY).getType());

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
		assertEquals(6, virtualModel.getBindingModel().getBindingVariablesCount());
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(VirtualModelBindingModel.REFLEXIVE_ACCESS_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.PROJECT_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.RC_PROPERTY));
		assertNotNull(virtualModel.getBindingModel().bindingVariableNamed(ViewPointBindingModel.VIEW_PROPERTY));
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

		System.out.println("flexoConceptA = " + flexoConceptA);
		assertNotNull(flexoConceptA);

		CreateFlexoBehaviour createM1InA = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createM1InA.setFlexoBehaviourName("m1");
		createM1InA.setFlexoBehaviourClass(ActionScheme.class);
		createM1InA.doAction();
		assertTrue(createM1InA.hasActionExecutionSucceeded());
		assertNotNull(m1InA = createM1InA.getNewFlexoBehaviour());

		CreateFlexoBehaviour createM2InA = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createM2InA.setFlexoBehaviourName("m2");
		createM2InA.setFlexoBehaviourClass(ActionScheme.class);
		BehaviourParameterEntry m2Arg = createM2InA.newParameterEntry("arg", String.class);
		createM2InA.doAction();
		assertTrue(createM2InA.hasActionExecutionSucceeded());
		assertNotNull(m2InA = createM2InA.getNewFlexoBehaviour());

		CreateFlexoBehaviour createM5InA = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createM5InA.setFlexoBehaviourName("m5");
		createM5InA.setFlexoBehaviourClass(ActionScheme.class);
		BehaviourParameterEntry m5Arg = createM5InA.newParameterEntry("arg", Boolean.class);
		createM5InA.doAction();
		assertTrue(createM5InA.hasActionExecutionSucceeded());
		assertNotNull(m5InA = createM5InA.getNewFlexoBehaviour());

		CreateFlexoBehaviour createM6InA = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createM6InA.setFlexoBehaviourName("m6");
		createM6InA.setFlexoBehaviourClass(ActionScheme.class);
		BehaviourParameterEntry m6Arg = createM6InA.newParameterEntry("arg", Boolean.class);
		createM6InA.doAction();
		assertTrue(createM6InA.hasActionExecutionSucceeded());
		assertNotNull(m6InA = createM6InA.getNewFlexoBehaviour());

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

		assertEquals(4, flexoConceptA.getFlexoBehaviours().size());
		assertEquals(4, flexoConceptA.getDeclaredFlexoBehaviours().size());
		assertEquals(4, flexoConceptA.getAccessibleFlexoBehaviours().size());

		((VirtualModelResource) virtualModel.getResource()).save(null);

	}

	/**
	 * Test FlexoConceptB creation, define some overriden properties
	 * 
	 * @throws InconsistentFlexoConceptHierarchyException
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

		CreateFlexoBehaviour createM2InB = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptB, null, editor);
		createM2InB.setFlexoBehaviourName("m2");
		createM2InB.setFlexoBehaviourClass(ActionScheme.class);
		createM2InB.newParameterEntry("arg", String.class);
		createM2InB.doAction();
		assertTrue(createM2InB.hasActionExecutionSucceeded());
		assertNotNull(m2InB = createM2InB.getNewFlexoBehaviour());

		CreateFlexoBehaviour createM4InB = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptB, null, editor);
		createM4InB.setFlexoBehaviourName("m4");
		createM4InB.setFlexoBehaviourClass(ActionScheme.class);
		createM4InB.newParameterEntry("arg", Integer.class);
		createM4InB.doAction();
		assertTrue(createM4InB.hasActionExecutionSucceeded());
		assertNotNull(m4InB = createM4InB.getNewFlexoBehaviour());

		CreateFlexoBehaviour createM6InB = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptB, null, editor);
		createM6InB.setFlexoBehaviourName("m6");
		createM6InB.setFlexoBehaviourClass(ActionScheme.class);
		BehaviourParameterEntry m6Arg = createM6InB.newParameterEntry("arg", Boolean.class);
		createM6InB.doAction();
		assertTrue(createM6InB.hasActionExecutionSucceeded());
		assertNotNull(m6InB = createM6InB.getNewFlexoBehaviour());

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

		assertEquals(3, flexoConceptB.getFlexoBehaviours().size());
		assertEquals(3, flexoConceptB.getDeclaredFlexoBehaviours().size());
		assertEquals(5, flexoConceptB.getAccessibleFlexoBehaviours().size());

		assertTrue(m2InB.overrides(m2InA));
		assertTrue(m2InA.isOverridenInContext(flexoConceptB));
		assertSame(m2InB, m2InA.getMostSpecializedBehaviour(flexoConceptB));
		assertSame(m2InA, m2InA.getMostSpecializedBehaviour(flexoConceptA));
		assertTrue(m6InB.overrides(m6InA));

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

		CreateFlexoBehaviour createM1InC = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptC, null, editor);
		createM1InC.setFlexoBehaviourName("m1");
		createM1InC.setFlexoBehaviourClass(ActionScheme.class);
		createM1InC.doAction();
		assertTrue(createM1InC.hasActionExecutionSucceeded());
		assertNotNull(m1InC = createM1InC.getNewFlexoBehaviour());

		CreateFlexoBehaviour createM3InC = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptC, null, editor);
		createM3InC.setFlexoBehaviourName("m3");
		createM3InC.setFlexoBehaviourClass(ActionScheme.class);
		createM3InC.newParameterEntry("arg", Double.class);
		createM3InC.doAction();
		assertTrue(createM3InC.hasActionExecutionSucceeded());
		assertNotNull(m3InC = createM3InC.getNewFlexoBehaviour());

		CreateFlexoBehaviour createM4InC = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptC, null, editor);
		createM4InC.setFlexoBehaviourName("m4");
		createM4InC.setFlexoBehaviourClass(ActionScheme.class);
		createM4InC.newParameterEntry("arg", String.class);
		createM4InC.doAction();
		assertTrue(createM4InC.hasActionExecutionSucceeded());
		assertNotNull(m4InC = createM4InC.getNewFlexoBehaviour());

		CreateFlexoBehaviour createM6InC = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptC, null, editor);
		createM6InC.setFlexoBehaviourName("m6");
		createM6InC.setFlexoBehaviourClass(ActionScheme.class);
		BehaviourParameterEntry m6Arg = createM6InC.newParameterEntry("arg", Boolean.class);
		createM6InC.doAction();
		assertTrue(createM6InC.hasActionExecutionSucceeded());
		assertNotNull(m6InC = createM6InC.getNewFlexoBehaviour());

		System.out.println("FML=" + virtualModel.getFMLRepresentation());

		assertEquals(4, flexoConceptC.getFlexoBehaviours().size());
		assertEquals(4, flexoConceptC.getDeclaredFlexoBehaviours().size());
		assertEquals(7, flexoConceptC.getAccessibleFlexoBehaviours().size());

		assertTrue(m2InB.overrides(m2InA));
		assertTrue(m2InA.isOverridenInContext(flexoConceptC));

		assertTrue(m1InC.overrides(m1InA));
		assertTrue(m1InA.isOverridenInContext(flexoConceptC));

		assertSame(m2InB, m2InA.getMostSpecializedBehaviour(flexoConceptC));
		assertSame(m2InB, m2InB.getMostSpecializedBehaviour(flexoConceptC));

		assertSame(m1InC, m1InA.getMostSpecializedBehaviour(flexoConceptC));
		assertSame(m1InA, m1InA.getMostSpecializedBehaviour(flexoConceptA));

		assertTrue(m6InC.overrides(m6InB));

		assertFalse(m4InC.overrides(m4InB));

		((VirtualModelResource) virtualModel.getResource()).save(null);
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
		assertNotNull(m1InA = flexoConceptA.getFlexoBehaviour("m1"));
		assertNotNull(m2InA = flexoConceptA.getFlexoBehaviour("m2"));
		assertNotNull(m5InA = flexoConceptA.getFlexoBehaviour("m5"));
		assertNotNull(m2InB = flexoConceptB.getFlexoBehaviour("m2"));
		assertNotNull(m4InB = flexoConceptB.getFlexoBehaviour("m4"));
		assertNotNull(m1InC = flexoConceptC.getFlexoBehaviour("m1"));
		assertNotNull(m3InC = flexoConceptC.getFlexoBehaviour("m3"));
		assertNotNull(m4InC = flexoConceptC.getFlexoBehaviour("m4"));

		assertEquals(4, flexoConceptA.getFlexoBehaviours().size());
		assertEquals(4, flexoConceptA.getDeclaredFlexoBehaviours().size());
		assertEquals(4, flexoConceptA.getAccessibleFlexoBehaviours().size());

		assertEquals(3, flexoConceptB.getFlexoBehaviours().size());
		assertEquals(3, flexoConceptB.getDeclaredFlexoBehaviours().size());
		assertEquals(5, flexoConceptB.getAccessibleFlexoBehaviours().size());

		assertTrue(m2InB.overrides(m2InA));
		assertTrue(m2InA.isOverridenInContext(flexoConceptB));
		assertSame(m2InB, m2InA.getMostSpecializedBehaviour(flexoConceptB));
		assertSame(m2InA, m2InA.getMostSpecializedBehaviour(flexoConceptA));
		assertTrue(m6InB.overrides(m6InA));

		assertEquals(4, flexoConceptC.getFlexoBehaviours().size());
		assertEquals(4, flexoConceptC.getDeclaredFlexoBehaviours().size());
		assertEquals(7, flexoConceptC.getAccessibleFlexoBehaviours().size());

		assertTrue(m2InB.overrides(m2InA));
		assertTrue(m2InA.isOverridenInContext(flexoConceptC));

		assertTrue(m1InC.overrides(m1InA));
		assertTrue(m1InA.isOverridenInContext(flexoConceptC));

		assertSame(m2InB, m2InA.getMostSpecializedBehaviour(flexoConceptC));
		assertSame(m2InB, m2InB.getMostSpecializedBehaviour(flexoConceptC));

		assertSame(m1InC, m1InA.getMostSpecializedBehaviour(flexoConceptC));
		assertSame(m1InA, m1InA.getMostSpecializedBehaviour(flexoConceptA));

		assertTrue(m6InC.overrides(m6InB));

		assertFalse(m4InC.overrides(m4InB));

	}

}
