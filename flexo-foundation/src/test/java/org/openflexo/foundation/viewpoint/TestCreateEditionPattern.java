package org.openflexo.foundation.viewpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.viewpoint.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.viewpoint.VirtualModel.VirtualModelImpl;
import org.openflexo.foundation.viewpoint.action.AddEditionPattern;
import org.openflexo.foundation.viewpoint.rm.ViewPointResource;
import org.openflexo.foundation.viewpoint.rm.VirtualModelResource;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test EditionPattern creation facilities
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestCreateEditionPattern extends ViewPointTestCase {

	static FlexoEditor editor;
	static ViewPoint newViewPoint;
	static VirtualModel newVirtualModel;

	static EditionPattern flexoConceptA;
	static EditionPattern flexoConceptB;
	static EditionPattern flexoConceptC;
	static EditionPattern flexoConceptD;
	static EditionPattern flexoConceptE;

	/**
	 * Test the VP creation
	 */
	@Test
	@TestOrder(1)
	public void testCreateViewPoint() {
		instanciateTestServiceManager();
		System.out.println("ResourceCenter= " + resourceCenter);
		newViewPoint = ViewPointImpl.newViewPoint("TestViewPoint", "http://openflexo.org/test/TestViewPoint",
				resourceCenter.getDirectory(), serviceManager.getViewPointLibrary());
		assertTrue(((ViewPointResource) newViewPoint.getResource()).getDirectory().exists());
		assertTrue(((ViewPointResource) newViewPoint.getResource()).getFile().exists());
	}

	/**
	 * Test the VirtualModel creation
	 */
	@Test
	@TestOrder(2)
	public void testCreateVirtualModel() throws SaveResourceException {
		newVirtualModel = VirtualModelImpl.newVirtualModel("TestVirtualModel", newViewPoint);
		assertTrue(((VirtualModelResource) newVirtualModel.getResource()).getDirectory().exists());
		assertTrue(((VirtualModelResource) newVirtualModel.getResource()).getFile().exists());
	}

	/**
	 * Test the EditionPattern creation
	 */
	@Test
	@TestOrder(3)
	public void testCreateEditor() {
		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);
	}

	/**
	 * Test the EditionPattern creation
	 */
	@Test
	@TestOrder(4)
	public void testCreateFlexoConceptA() throws SaveResourceException {

		AddEditionPattern addEP = AddEditionPattern.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptA");
		addEP.doAction();

		flexoConceptA = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept A = " + flexoConceptA);
		assertNotNull(flexoConceptA);

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFile());

	}

	/**
	 * Test the EditionPattern creation
	 */
	@Test
	@TestOrder(5)
	public void testCreateFlexoConceptB() throws SaveResourceException {

		AddEditionPattern addEP = AddEditionPattern.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptB");
		addEP.doAction();

		flexoConceptB = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept B = " + flexoConceptB);
		assertNotNull(flexoConceptB);

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFile());

	}

	/**
	 * Test the EditionPattern creation
	 */
	@Test
	@TestOrder(5)
	public void testCreateFlexoConceptC() throws SaveResourceException {

		AddEditionPattern addEP = AddEditionPattern.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptC");
		addEP.addToParentConcepts(flexoConceptB);
		addEP.doAction();

		flexoConceptC = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept C = " + flexoConceptC);
		assertNotNull(flexoConceptC);
		assertEquals(1, flexoConceptC.getParentEditionPatterns().size());
		assertEquals(flexoConceptB, flexoConceptC.getParentEditionPatterns().get(0));

		assertEquals(1, flexoConceptB.getChildEditionPatterns().size());
		assertEquals(flexoConceptC, flexoConceptB.getChildEditionPatterns().get(0));

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFile());

	}

	/**
	 * Test the EditionPattern creation
	 */
	@Test
	@TestOrder(5)
	public void testCreateFlexoConceptD() throws SaveResourceException {

		AddEditionPattern addEP = AddEditionPattern.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptD");
		addEP.addToParentConcepts(flexoConceptB);
		addEP.doAction();

		flexoConceptD = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept D = " + flexoConceptD);
		assertNotNull(flexoConceptD);
		assertEquals(1, flexoConceptD.getParentEditionPatterns().size());
		assertEquals(flexoConceptB, flexoConceptD.getParentEditionPatterns().get(0));

		assertEquals(2, flexoConceptB.getChildEditionPatterns().size());
		assertEquals(flexoConceptC, flexoConceptB.getChildEditionPatterns().get(0));
		assertEquals(flexoConceptD, flexoConceptB.getChildEditionPatterns().get(1));

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFile());

	}

	/**
	 * Test the EditionPattern creation
	 */
	@Test
	@TestOrder(6)
	public void testCreateFlexoConceptE() throws SaveResourceException {

		AddEditionPattern addEP = AddEditionPattern.actionType.makeNewAction(newVirtualModel, null, editor);
		addEP.setNewFlexoConceptName("FlexoConceptE");
		addEP.addToParentConcepts(flexoConceptA);
		addEP.addToParentConcepts(flexoConceptB);
		addEP.addToParentConcepts(flexoConceptC);
		addEP.doAction();

		flexoConceptE = addEP.getNewFlexoConcept();

		System.out.println("FlexoConcept E = " + flexoConceptE);
		assertNotNull(flexoConceptE);
		assertEquals(3, flexoConceptE.getParentEditionPatterns().size());
		assertEquals(flexoConceptA, flexoConceptE.getParentEditionPatterns().get(0));
		assertEquals(flexoConceptB, flexoConceptE.getParentEditionPatterns().get(1));
		assertEquals(flexoConceptC, flexoConceptE.getParentEditionPatterns().get(2));

		assertEquals(1, flexoConceptA.getChildEditionPatterns().size());
		assertEquals(flexoConceptE, flexoConceptA.getChildEditionPatterns().get(0));
		assertEquals(3, flexoConceptB.getChildEditionPatterns().size());
		assertEquals(flexoConceptC, flexoConceptB.getChildEditionPatterns().get(0));
		assertEquals(flexoConceptD, flexoConceptB.getChildEditionPatterns().get(1));
		assertEquals(flexoConceptE, flexoConceptB.getChildEditionPatterns().get(2));
		assertEquals(1, flexoConceptC.getChildEditionPatterns().size());
		assertEquals(flexoConceptE, flexoConceptC.getChildEditionPatterns().get(0));

		((VirtualModelResource) newVirtualModel.getResource()).save(null);

		System.out.println("Saved: " + ((VirtualModelResource) newVirtualModel.getResource()).getFile());

	}

}
