/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.rt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.InconsistentFlexoConceptHierarchyException;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.controlgraph.EmptyControlGraph;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.ViewPointResourceFactory;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateViewInFolder;
import org.openflexo.foundation.fml.rt.action.CreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeAction;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeActionType;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test the instantiation of a VirtualModel whose instances have {@link FMLControlledDiagramVirtualModelNature}
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestPopulateVirtualModelInstance extends OpenflexoProjectAtRunTimeTestCase {

	static FlexoEditor editor;
	static ViewPoint viewPoint;
	static VirtualModel virtualModel;

	static FlexoConcept flexoConceptA;
	static FlexoConcept flexoConceptB;
	static FlexoConcept flexoConceptC;

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestViewPoint";
	public static final String VIRTUAL_MODEL_NAME = "TestVirtualModel";

	private static DirectoryResourceCenter resourceCenter;

	private static FlexoProject project;
	private static View newView;
	private static VirtualModelInstance newVirtualModelInstance;

	/**
	 * Test the VP creation
	 * 
	 * @throws ModelDefinitionException
	 * @throws SaveResourceException
	 * @throws IOException
	 */
	@Test
	@TestOrder(1)
	public void testCreateViewPoint() throws SaveResourceException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		resourceCenter = makeNewDirectoryResourceCenter();
		assertNotNull(resourceCenter);
		System.out.println("ResourceCenter= " + resourceCenter);

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		ViewPointResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();

		ViewPointResource newViewPointResource = factory.makeViewPointResource(VIEWPOINT_NAME, VIEWPOINT_URI,
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(),
				fmlTechnologyAdapter.getTechnologyContextManager(), true);
		viewPoint = newViewPointResource.getLoadedResourceData();

		// assertTrue(((ViewPointResource)
		// newViewPoint.getResource()).getDirectory().exists());
		// assertTrue(((ViewPointResource)
		// newViewPoint.getResource()).getFile().exists());
		assertTrue(((ViewPointResource) viewPoint.getResource()).getDirectory() != null);
		assertTrue(((ViewPointResource) viewPoint.getResource()).getIODelegate().exists());

		assertEquals(viewPoint, viewPoint.getViewPoint());
		assertEquals(viewPoint, viewPoint.getVirtualModel());
		assertEquals(null, viewPoint.getOwningVirtualModel());
		assertEquals(viewPoint, viewPoint.getFlexoConcept());
	}

	/**
	 * Test the VirtualModel creation
	 * 
	 * @throws ModelDefinitionException
	 */
	@Test
	@TestOrder(2)
	public void testCreateVirtualModel() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory().getVirtualModelResourceFactory();
		VirtualModelResource newVMResource = factory.makeVirtualModelResource(VIRTUAL_MODEL_NAME, viewPoint.getViewPointResource(),
				fmlTechnologyAdapter.getTechnologyContextManager(), true);
		virtualModel = newVMResource.getLoadedResourceData();

		assertTrue(ResourceLocator.retrieveResourceAsFile(((VirtualModelResource) virtualModel.getResource()).getDirectory()).exists());
		assertTrue(((VirtualModelResource) virtualModel.getResource()).getIODelegate().exists());

		assertEquals(viewPoint, virtualModel.getViewPoint());
		assertEquals(virtualModel, virtualModel.getVirtualModel());
		// assertEquals(null, newVirtualModel.getOwningVirtualModel());

		assertSame(viewPoint, virtualModel.getOwningVirtualModel());

		assertEquals(virtualModel, virtualModel.getFlexoConcept());
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
	public void testCreateFlexoConceptA() throws SaveResourceException {

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptA");

		addEP.doAction();

		flexoConceptA = addEP.getNewFlexoConcept();

		assertNotNull(flexoConceptA);

		assertEquals(viewPoint, flexoConceptA.getViewPoint());
		assertEquals(virtualModel, flexoConceptA.getVirtualModel());
		assertEquals(virtualModel, flexoConceptA.getOwningVirtualModel());
		assertEquals(flexoConceptA, flexoConceptA.getFlexoConcept());
		assertEquals(virtualModel, flexoConceptA.getResourceData());

		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme.doAction();
		CreationScheme creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();
		assertTrue(creationScheme.getControlGraph() instanceof EmptyControlGraph);

		CreateFlexoBehaviour createDeletionScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptA, null, editor);
		createDeletionScheme.setFlexoBehaviourClass(DeletionScheme.class);
		createDeletionScheme.doAction();
		DeletionScheme deletionScheme = (DeletionScheme) createDeletionScheme.getNewFlexoBehaviour();
		assertTrue(deletionScheme.getControlGraph() instanceof EmptyControlGraph);

		((VirtualModelResource) virtualModel.getResource()).save(null);

		// System.out.println("Saved: " + ((VirtualModelResource)
		// newVirtualModel.getResource()).getFile());
		System.out.println("Saved: " + ((VirtualModelResource) virtualModel.getResource()).getIODelegate().toString());

	}

	/**
	 * Test the FlexoConcept creation
	 * 
	 * @throws InconsistentFlexoConceptHierarchyException
	 */
	@Test
	@TestOrder(5)
	public void testCreateFlexoConceptB() throws SaveResourceException, InconsistentFlexoConceptHierarchyException {

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptB");
		addEP.doAction();

		flexoConceptB = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept B = " + flexoConceptB);
		assertNotNull(flexoConceptB);

		flexoConceptB.addToParentFlexoConcepts(flexoConceptA);

		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptB, null, editor);
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme.doAction();
		CreationScheme creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();
		assertTrue(creationScheme.getControlGraph() instanceof EmptyControlGraph);

		CreateFlexoBehaviour createDeletionScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptB, null, editor);
		createDeletionScheme.setFlexoBehaviourClass(DeletionScheme.class);
		createDeletionScheme.doAction();
		DeletionScheme deletionScheme = (DeletionScheme) createDeletionScheme.getNewFlexoBehaviour();
		assertTrue(deletionScheme.getControlGraph() instanceof EmptyControlGraph);

		((VirtualModelResource) virtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) virtualModel.getResource()).getIODelegate().toString());

	}

	/**
	 * Test the FlexoConcept creation
	 * 
	 * @throws InconsistentFlexoConceptHierarchyException
	 */
	@Test
	@TestOrder(6)
	public void testCreateFlexoConceptC() throws SaveResourceException, InconsistentFlexoConceptHierarchyException {

		CreateFlexoConcept addEP = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptC");
		addEP.doAction();

		flexoConceptC = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept C = " + flexoConceptC);
		assertNotNull(flexoConceptC);

		flexoConceptC.addToParentFlexoConcepts(flexoConceptB);
		flexoConceptC.addToParentFlexoConcepts(flexoConceptA);

		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptC, null, editor);
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme.doAction();
		CreationScheme creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();
		assertTrue(creationScheme.getControlGraph() instanceof EmptyControlGraph);

		CreateFlexoBehaviour createDeletionScheme = CreateFlexoBehaviour.actionType.makeNewAction(flexoConceptC, null, editor);
		createDeletionScheme.setFlexoBehaviourClass(DeletionScheme.class);
		createDeletionScheme.doAction();
		DeletionScheme deletionScheme = (DeletionScheme) createDeletionScheme.getNewFlexoBehaviour();
		assertTrue(deletionScheme.getControlGraph() instanceof EmptyControlGraph);

		System.out.println("Saved: " + ((VirtualModelResource) virtualModel.getResource()).getIODelegate().toString());

		System.out.println("FML=\n" + virtualModel.getFMLRepresentation());

		((VirtualModelResource) virtualModel.getResource()).save(null);

		// System.out.println("les unsaved: " + serviceManager.getResourceManager().getUnsavedResources());
		/*virtualModel.getPropertyChangeSupport().addPropertyChangeListener(new PropertyChangeListener() {
		
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("event " + evt.getPropertyName() + " from " + evt.getOldValue() + " to " + evt.getNewValue());
			}
		});*/
		// System.out.println("Tiens on ecoute les modifs de " + virtualModel);
		// ((VirtualModelImpl) virtualModel).notifyModified = true;
	}

	@Test
	@TestOrder(6)
	public void testCreateProject() {
		editor = createProject("TestProject");
		project = editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
		assertTrue(project.getProjectDataResource().getIODelegate().exists());
	}

	/**
	 * Instantiate in project a View conform to the ViewPoint
	 */
	@Test
	@TestOrder(7)
	public void testCreateView() {
		CreateViewInFolder action = CreateViewInFolder.actionType.makeNewAction(project.getViewLibrary().getRootFolder(), null, editor);
		action.setNewViewName("MyView");
		action.setNewViewTitle("Test creation of a new view");
		action.setViewpointResource((ViewPointResource) viewPoint.getResource());
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		newView = action.getNewView();
		assertNotNull(newView);
		assertNotNull(newView.getResource());
		assertTrue(ResourceLocator.retrieveResourceAsFile(((ViewResource) newView.getResource()).getDirectory()).exists());
		assertTrue(((ViewResource) newView.getResource()).getIODelegate().exists());
	}

	/**
	 * Instantiate in project a VirtualModelInstance conform to the VirtualModel
	 * 
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(8)
	public void testCreateVirtualModelInstance() throws SaveResourceException {

		log("testCreateVirtualModelInstance()");

		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType.makeNewAction(newView, null, editor);
		action.setNewVirtualModelInstanceName("MyVirtualModelInstance");
		action.setNewVirtualModelInstanceTitle("Test creation of a new VirtualModelInstance");
		action.setVirtualModel(virtualModel);

		action.doAction();

		if (!action.hasActionExecutionSucceeded()) {
			fail(action.getThrownException().getMessage());
		}

		assertTrue(action.hasActionExecutionSucceeded());
		newVirtualModelInstance = action.getNewVirtualModelInstance();
		assertNotNull(newVirtualModelInstance);
		assertNotNull(newVirtualModelInstance.getResource());
		assertTrue(((ViewResource) newView.getResource()).getIODelegate().exists());

		assertFalse(newVirtualModelInstance.isModified());

		// virtualModel.getResource().save(null);
	}

	/**
	 * Try to populate VirtualModelInstance
	 * 
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(9)
	public void testPopulateVirtualModelInstance() throws SaveResourceException {

		log("testPopulateVirtualModelInstance()");
		assertEquals(0, serviceManager.getResourceManager().getUnsavedResources().size());

		VirtualModelInstanceResource vmiRes = (VirtualModelInstanceResource) newVirtualModelInstance.getResource();
		assertFalse(newVirtualModelInstance.isModified());

		FlexoConceptInstance a1 = createInstance(flexoConceptA);
		FlexoConceptInstance a2 = createInstance(flexoConceptA);
		FlexoConceptInstance a3 = createInstance(flexoConceptA);

		FlexoConceptInstance b1 = createInstance(flexoConceptB);
		FlexoConceptInstance b2 = createInstance(flexoConceptB);
		FlexoConceptInstance b3 = createInstance(flexoConceptB);

		FlexoConceptInstance c1 = createInstance(flexoConceptC);
		FlexoConceptInstance c2 = createInstance(flexoConceptC);
		FlexoConceptInstance c3 = createInstance(flexoConceptC);

		System.out.println(vmiRes.getFactory().stringRepresentation(vmiRes.getLoadedResourceData()));

		assertTrue(serviceManager.getResourceManager().getUnsavedResources().contains(newVirtualModelInstance.getResource()));

		newVirtualModelInstance.getResource().save(null);
		assertTrue(((VirtualModelInstanceResource) newVirtualModelInstance.getResource()).getIODelegate().exists());
		assertFalse(newVirtualModelInstance.isModified());

		System.out.println("A: " + newVirtualModelInstance.getFlexoConceptInstances(flexoConceptA));
		System.out.println("B: " + newVirtualModelInstance.getFlexoConceptInstances(flexoConceptB));
		System.out.println("C: " + newVirtualModelInstance.getFlexoConceptInstances(flexoConceptC));

		assertEquals(9, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptA).size());
		assertEquals(6, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptB).size());
		assertEquals(3, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptC).size());

		assertEquals(9, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptA.getURI()).size());
		assertEquals(6, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptB.getURI()).size());
		assertEquals(3, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptC.getURI()).size());

		deleteFlexoConceptInstance(a1);
		deleteFlexoConceptInstance(b1);
		deleteFlexoConceptInstance(c1);

		assertEquals(6, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptA).size());
		assertEquals(4, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptB).size());
		assertEquals(2, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptC).size());

		a2.delete();
		b2.delete();
		c2.delete();

		assertEquals(3, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptA).size());
		assertEquals(2, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptB).size());
		assertEquals(1, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptC).size());

		FlexoConceptInstance a4 = createInstance(flexoConceptA);
		FlexoConceptInstance b4 = createInstance(flexoConceptB);
		FlexoConceptInstance c4 = createInstance(flexoConceptC);

		assertEquals(6, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptA).size());
		assertEquals(4, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptB).size());
		assertEquals(2, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptC).size());
	}

	private FlexoConceptInstance createInstance(FlexoConcept concept) {
		CreationSchemeAction action = CreationSchemeAction.actionType.makeNewAction(newVirtualModelInstance, null, editor);
		action.setCreationScheme(concept.getCreationSchemes().get(0));
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		return action.getFlexoConceptInstance();
	}

	private void deleteFlexoConceptInstance(FlexoConceptInstance fci) {
		DeletionSchemeActionType actionType = new DeletionSchemeActionType(fci.getFlexoConcept().getDefaultDeletionScheme(), fci);
		DeletionSchemeAction action = actionType.makeNewAction(fci, null, editor);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
	}

	/**
	 * Instantiate in project a VirtualModelInstance conform to the VirtualModel
	 * 
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	@Test
	@TestOrder(10)
	public void testReloadProject() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		log("testReloadProject()");

		instanciateTestServiceManager();

		serviceManager.getResourceCenterService().addToResourceCenters(
				resourceCenter = new DirectoryResourceCenter(resourceCenter.getDirectory(), serviceManager.getResourceCenterService()));

		editor = reloadProject(project.getDirectory());
		project = editor.getProject();
		assertNotNull(editor);
		assertNotNull(project);

		System.out.println("All resources=" + project.getAllResources());
		assertEquals(2, project.getAllResources().size());
		assertNotNull(project.getResource(newView.getURI()));

		ViewResource newViewResource = project.getViewLibrary().getView(newView.getURI());
		assertNotNull(newViewResource);
		assertNull(newViewResource.getLoadedResourceData());
		newViewResource.loadResourceData(null);
		assertNotNull(newView = newViewResource.getView());

		assertEquals(1, newViewResource.getVirtualModelInstanceResources().size());
		VirtualModelInstanceResource vmiResource = newViewResource.getVirtualModelInstanceResources().get(0);
		assertNotNull(vmiResource);
		assertNull(vmiResource.getLoadedResourceData());
		vmiResource.loadResourceData(null);
		assertNotNull(newVirtualModelInstance = vmiResource.getVirtualModelInstance());

		assertEquals(9, newVirtualModelInstance.getFlexoConceptInstances().size());
		// FlexoConceptInstance fci = newVirtualModelInstance.getFlexoConceptInstances().get(0);
		// assertNotNull(fci);

		assertNotNull(flexoConceptA = virtualModel.getFlexoConcept(flexoConceptA.getURI()));
		assertNotNull(flexoConceptB = virtualModel.getFlexoConcept(flexoConceptB.getURI()));
		assertNotNull(flexoConceptC = virtualModel.getFlexoConcept(flexoConceptC.getURI()));

		assertEquals(9, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptA.getURI()).size());
		assertEquals(6, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptB.getURI()).size());
		assertEquals(3, newVirtualModelInstance.getFlexoConceptInstances(flexoConceptC.getURI()).size());

	}
}
