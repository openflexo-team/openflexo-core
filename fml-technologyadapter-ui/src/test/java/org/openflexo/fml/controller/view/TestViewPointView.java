/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.view;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.gina.swing.utils.FIBJPanel;
import org.openflexo.gina.test.OpenflexoFIBTestCase;
import org.openflexo.gina.test.SwingGraphicalContextDelegate;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test FlexoConceptPanel fib
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestViewPointView extends OpenflexoFIBTestCase {

	private static SwingGraphicalContextDelegate gcDelegate;

	private static String fibFileName;

	static ViewPoint viewPoint;

	static FlexoEditor editor;

	@BeforeClass
	public static void setupClass() {
		instanciateTestServiceManager();
		initGUI();
	}

	@Test
	@TestOrder(1)
	public void testLoadWidget() {

		fibFileName = "Fib/FML/ViewPointView.fib";
		assertTrue(ResourceLocator.locateResource(fibFileName) != null);
	}

	@Test
	@TestOrder(2)
	public void testValidateWidget() throws InterruptedException {

		validateFIB(ResourceLocator.locateResource(fibFileName));
	}

	@Test
	@TestOrder(3)
	public void loadConcepts() {

		// instanciateTestServiceManager();

		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);
		viewPoint = vpLib.getViewPoint("http://openflexo.org/test/TestViewPointA");
		assertNotNull(viewPoint);
		VirtualModel virtualModel = viewPoint.getVirtualModelNamed("TestVirtualModel");
		assertNotNull(virtualModel);

	}

	@Test
	@TestOrder(4)
	public void testInstanciateWidget() {

		FIBJPanel<ViewPoint> widget = instanciateFIB(ResourceLocator.locateResource(fibFileName), viewPoint, ViewPoint.class);

		gcDelegate.addTab("TestViewPointA", widget.getController());
	}

	public static void initGUI() {
		gcDelegate = new SwingGraphicalContextDelegate(TestViewPointView.class.getSimpleName());
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
