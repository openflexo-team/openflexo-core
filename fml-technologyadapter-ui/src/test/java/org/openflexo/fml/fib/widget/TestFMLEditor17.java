/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.fml.fib.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.fml.controller.FMLFIBController;
import org.openflexo.fml.controller.widget.fmleditor.FMLEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.swing.utils.FIBJPanel;
import org.openflexo.gina.test.OpenflexoFIBTestCase;
import org.openflexo.gina.test.SwingGraphicalContextDelegate;
import org.openflexo.gina.utils.InspectorGroup;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * Test {@link FMLEditor} component
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestFMLEditor17 extends OpenflexoFIBTestCase {

	private static SwingGraphicalContextDelegate gcDelegate;

	private static Resource fibResource;

	static FlexoEditor editor;

	@BeforeClass
	public static void setupClass() {
		instanciateTestServiceManager();
		initGUI();
	}

	private static CompilationUnitResource fmlResource;

	private static FMLCompilationUnit compilationUnit;
	private static VirtualModel virtualModel;

	private static ActionScheme as1;
	private static ActionScheme as2;

	@Test
	@TestOrder(3)
	@Category(UITest.class)
	public void loadFMLResource() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);
		VirtualModel vm = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/Bug17.fml");
		assertNotNull(vm);

		fmlResource = vm.getResource();
		assertNotNull(fmlResource);

		compilationUnit = fmlResource.getCompilationUnit();
		virtualModel = compilationUnit.getVirtualModel();

	}

	private static FMLEditor fmlEditor;

	@Test
	@TestOrder(4)
	@Category(UITest.class)
	public void testInstanciateFMLEditor() {

		fmlEditor = new FMLEditor(fmlResource, null);
		gcDelegate.addTab("FML Editor", fmlEditor);
		FMLCompilationUnit cu = fmlEditor.getFMLResource().getCompilationUnit();
		assertNotNull(cu);
		assertEquals(0, cu.getVirtualModel().getFlexoProperties().size());
		assertEquals(3, cu.getVirtualModel().getFlexoBehaviours().size());

		ValidationReport validation = validate(cu);
		assertEquals(1, validation.getErrorsCount());

	}

	@Test
	@TestOrder(5)
	@Category(UITest.class)
	public void testInstanciateWidget() {

		fibResource = ResourceLocator.locateResource("Fib/FML/CompilationUnitView.fib");
		assertTrue(fibResource != null);
		FIBJPanel<FMLCompilationUnit> widget = instanciateFIB(fibResource, fmlResource.getCompilationUnit(), FMLCompilationUnit.class);
		FMLFIBController fibController = (FMLFIBController) widget.getController();
		InspectorGroup fmlInspectorGroup = new InspectorGroup(ResourceLocator.locateResource("Inspectors/FML"),
				ApplicationFIBLibraryImpl.instance(), null);
		fibController.setDefaultInspectorGroup(fmlInspectorGroup);

		// ModuleInspectorController inspectorController = new ModuleInspectorController(null);
		// fibController.setInspectorController
		gcDelegate.addTab("Standard GUI", widget.getController());
	}

	public static void initGUI() {
		gcDelegate = new SwingGraphicalContextDelegate(TestFMLEditor17.class.getSimpleName());
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

	@Test
	@TestOrder(6)
	@Category(UITest.class)
	public void updateWrongType() {

		log("updateWrongType");

		// @formatter:off
		String fml = "@URI(\"http://openflexo.org/test/TestResourceCenter/Bug17.fml\")\n"
				+ "public model Bug17 {\n"
				+ "    \n"
				+ "    itWorks() {\n"
				+ "        List<String> aList = new ArrayList<String>();\n"
				+ "    }\n"
				+ "    \n"
				+ "    itWorksAsWell() {\n"
				+ "        List<String> myList = new ArrayList<>();\n"
				+ "    }\n"
				+ "    \n"
				+ "    itDoesnotWork() {\n"
				+ "        List<Integer> anOtherList = new ArrayList<Integer>();\n"
				+ "    }\n"
				+ "}\n";

		
		// @formatter:on

		fmlEditor.getTextArea().setText(fml);
		fmlEditor.parseImmediately();

		FMLCompilationUnit cu = fmlEditor.getFMLResource().getCompilationUnit();
		assertNotNull(cu);
		assertEquals(0, cu.getVirtualModel().getFlexoProperties().size());
		assertEquals(3, cu.getVirtualModel().getFlexoBehaviours().size());
		assertEquals(0, cu.getVirtualModel().getFlexoConcepts().size());

		assertSame(cu, compilationUnit);
		assertSame(cu.getVirtualModel(), virtualModel);

		as1 = (ActionScheme) cu.getVirtualModel().getFlexoBehaviour("itWorks");
		System.out.println("as1: " + as1);
		as2 = (ActionScheme) cu.getVirtualModel().getFlexoBehaviour("itDoesnotWork");
		System.out.println("as2: " + as2);

		ValidationReport validation = validate(cu);
		assertEquals(0, validation.getErrorsCount());

	}

}