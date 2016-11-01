/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.fml.fib.widget;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateViewPoint;
import org.openflexo.foundation.fml.action.CreateVirtualModel;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.gina.swing.utils.TypeSelector;
import org.openflexo.gina.swing.utils.TypeSelector.TypeSelectorDetailsPanel;
import org.openflexo.gina.test.OpenflexoTestCaseWithGUI;
import org.openflexo.gina.test.SwingGraphicalContextDelegate;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test the structural and behavioural features of {@link TypeSelector}
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestTypeSelector extends OpenflexoTestCaseWithGUI {

	private static SwingGraphicalContextDelegate gcDelegate;

	public static final String VIEWPOINT_NAME = "TestViewPoint";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestViewPoint";
	public static final String VIRTUAL_MODEL_NAME = "TestVirtualModel";

	static ViewPoint newViewPoint;
	static VirtualModel newVirtualModel;
	// static ViewPointResource newViewPointResource;
	static FlexoConcept flexoConceptA, flexoConceptB, flexoConceptC;

	static FlexoEditor editor;

	@BeforeClass
	public static void setupClass() {
		instanciateTestServiceManager();
		editor = new DefaultFlexoEditor(null, serviceManager);
		initGUI();
	}

	/**
	 * Test the VP creation
	 * 
	 * @throws SaveResourceException
	 */
	@Test
	@TestOrder(1)
	public void testSomeConcepts() throws SaveResourceException {

		CreateViewPoint addViewPointAction = CreateViewPoint.actionType
				.makeNewAction(resourceCenter.getViewPointRepository().getRootFolder(), null, editor);
		addViewPointAction.setNewViewPointName(VIEWPOINT_NAME);
		addViewPointAction.setNewViewPointURI(VIEWPOINT_URI);
		addViewPointAction.doAction();
		assertTrue(addViewPointAction.hasActionExecutionSucceeded());
		ViewPoint newViewPoint = addViewPointAction.getNewViewPoint();

		// newViewPoint = ViewPointImpl.newViewPoint(VIEWPOINT_NAME, VIEWPOINT_URI, resourceCenter.getDirectory(),
		// serviceManager.getViewPointLibrary());
		// newViewPointResource = (ViewPointResource) newViewPoint.getResource();

		CreateVirtualModel addVirtualModelAction = CreateVirtualModel.actionType.makeNewAction(newViewPoint, null, editor);
		addVirtualModelAction.setNewVirtualModelName("TestVirtualModel");
		addVirtualModelAction.doAction();
		assertTrue(addVirtualModelAction.hasActionExecutionSucceeded());
		VirtualModel newVirtualModel = addVirtualModelAction.getNewVirtualModel();

		CreateFlexoConcept addFlexoConceptAAction = CreateFlexoConcept.actionType.makeNewAction(newVirtualModel, null, editor);
		addFlexoConceptAAction.setNewFlexoConceptName("FlexoConceptA");
		addFlexoConceptAAction.doAction();
		assertTrue(addFlexoConceptAAction.hasActionExecutionSucceeded());
		flexoConceptA = addFlexoConceptAAction.getNewFlexoConcept();

	}

	private static TypeSelector typeSelector;

	@Test
	@TestOrder(2)
	public void testInstanciateWidgetWithString() {

		typeSelector = new TypeSelector(String.class);
		typeSelector.setCustomTypeManager(serviceManager.getTechnologyAdapterService());
		typeSelector.setCustomTypeEditorProvider(serviceManager.getTechnologyAdapterControllerService());
		gcDelegate.addTab("TypeSelector", ((TypeSelectorDetailsPanel) typeSelector.getCustomPanel()).getController());
		assertSame(typeSelector.getChoice(), PrimitiveType.String);
	}

	@Test
	@TestOrder(3)
	public void checkCustomTypes() {
		typeSelector.setCustomTypeManager(serviceManager.getTechnologyAdapterService());
		typeSelector.setCustomTypeEditorProvider(serviceManager.getTechnologyAdapterControllerService());
		System.out.println("customTypeFactories=" + serviceManager.getTechnologyAdapterService().getCustomTypeFactories());
		assertTrue(typeSelector.getChoices().contains(FlexoConceptInstanceType.class));
	}

	@Test
	@TestOrder(4)
	public void testInstanciateWidgetWithFlexoConceptInstanceType() {

		FlexoConceptInstanceType type = FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConceptA);
		typeSelector.setEditedObject(type);
	}

	public static void initGUI() {
		gcDelegate = new SwingGraphicalContextDelegate(TestTypeSelector.class.getSimpleName());
	}

	@AfterClass
	public static void waitGUI() {
		gcDelegate.waitGUI();
	}

	@Before
	public void setUp() {
		gcDelegate.setUp();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		gcDelegate.tearDown();
	}

}
