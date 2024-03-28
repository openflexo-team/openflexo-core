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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.fml.controller.FMLFIBController;
import org.openflexo.fml.controller.widget.fmleditor.FMLEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoConceptNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoRolePropertyNode;
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
@Ignore // CI/CD issue : job does not end on Jenkins / Investigate
public class TestFMLEditor9 extends OpenflexoFIBTestCase {

	private static SwingGraphicalContextDelegate gcDelegate;

	private static Resource fibResource;

	static FlexoEditor editor;

	private static CompilationUnitResource fmlResource;
	private static FMLCompilationUnit compilationUnit;

	static VirtualModel conceptualModel;
	static FlexoConcept place;
	static FlexoConcept transition;
	static FlexoConcept placeToTransitionEdge;
	static FlexoConcept transitionToPlaceEdge;
	static FlexoConcept edge;

	@BeforeClass
	public static void setupClass() {
		instanciateTestServiceManager();
		initGUI();
	}

	@Test
	@TestOrder(3)
	@Category(UITest.class)
	public void loadFMLResource() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);
		VirtualModel vm = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestConceptualModel.fml");
		assertNotNull(vm);

		fmlResource = vm.getResource();
		assertNotNull(fmlResource);

		compilationUnit = fmlResource.getCompilationUnit();
		conceptualModel = compilationUnit.getVirtualModel();

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
		gcDelegate = new SwingGraphicalContextDelegate(TestFMLEditor9.class.getSimpleName());
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
	public void performSomeChecks() {

		log("performSomeChecks");

		assertEquals(0, compilationUnit.getVirtualModel().getFlexoProperties().size());
		assertEquals(0, compilationUnit.getVirtualModel().getFlexoBehaviours().size());
		assertEquals(5, compilationUnit.getVirtualModel().getFlexoConcepts().size());

		assertNotNull(place = compilationUnit.getVirtualModel().getFlexoConcept("Place"));
		assertNotNull(transition = compilationUnit.getVirtualModel().getFlexoConcept("Transition"));
		assertNotNull(placeToTransitionEdge = compilationUnit.getVirtualModel().getFlexoConcept("PlaceToTransitionEdge"));
		assertNotNull(transitionToPlaceEdge = compilationUnit.getVirtualModel().getFlexoConcept("TransitionToPlaceEdge"));
		assertNotNull(edge = compilationUnit.getVirtualModel().getFlexoConcept("Edge"));

		assertTrue(edge.isAssignableFrom(placeToTransitionEdge));
		assertTrue(edge.isAssignableFrom(transitionToPlaceEdge));

		ActionScheme stepBehaviour = (ActionScheme) transition.getFlexoBehaviour("step");
		assertNotNull(stepBehaviour);

		ConditionalAction conditional = (ConditionalAction) stepBehaviour.getControlGraph();
		Sequence sequence = (Sequence) conditional.getThenControlGraph();
		IterationAction it1 = (IterationAction) sequence.getControlGraph1();
		IterationAction it2 = (IterationAction) sequence.getControlGraph2();

		/*System.out.println("Normalized : " + placeToTransitionEdge.getNormalizedFML());
		
		FlexoConceptNode fcNode = (FlexoConceptNode) placeToTransitionEdge.getPrettyPrintDelegate();
		System.out.println(fcNode.debug());
		System.out.println("FML : " + placeToTransitionEdge.getFMLPrettyPrint());*/

		System.out.println("FML: " + compilationUnit.getFMLPrettyPrint());

		ValidationReport validation = validate(compilationUnit);
		assertEquals(0, validation.getErrorsCount());

		FlexoConceptNode fcNode = (FlexoConceptNode) transition.getPrettyPrintDelegate();
		System.out.println(fcNode.debug());

	}

	@Test
	@TestOrder(7)
	@Category(UITest.class)
	public void reparse() {

		log("reparse");

		String fml = compilationUnit.getFMLPrettyPrint();
		fmlEditor.getTextArea().setText(fml);
		fmlEditor.parseImmediately();

		FMLCompilationUnit cu = fmlEditor.getFMLResource().getCompilationUnit();
		assertNotNull(cu);
		assertEquals(0, cu.getVirtualModel().getFlexoProperties().size());
		assertEquals(0, cu.getVirtualModel().getFlexoBehaviours().size());
		assertEquals(5, cu.getVirtualModel().getFlexoConcepts().size());

		assertSame(cu, compilationUnit);
		assertSame(cu.getVirtualModel(), conceptualModel);

		assertNotNull(place = compilationUnit.getVirtualModel().getFlexoConcept("Place"));
		assertNotNull(transition = compilationUnit.getVirtualModel().getFlexoConcept("Transition"));
		assertNotNull(placeToTransitionEdge = compilationUnit.getVirtualModel().getFlexoConcept("PlaceToTransitionEdge"));
		assertNotNull(transitionToPlaceEdge = compilationUnit.getVirtualModel().getFlexoConcept("TransitionToPlaceEdge"));
		assertNotNull(edge = compilationUnit.getVirtualModel().getFlexoConcept("Edge"));

		assertTrue(edge.isAssignableFrom(placeToTransitionEdge));
		assertTrue(edge.isAssignableFrom(transitionToPlaceEdge));

		ValidationReport validation = validate(cu);
		assertEquals(0, validation.getErrorsCount());

		System.out.println("FML: " + transition.getFMLPrettyPrint());

		FlexoConceptNode fcNode = (FlexoConceptNode) transition.getPrettyPrintDelegate();
		System.out.println("FC node");
		System.out.println(fcNode.debug());
		// System.out.println("lastParsed: " + fcNode.getLastParsedFragment());
		// System.out.println("text: " + fcNode.getLastParsedFragment().getRawText());

		FlexoConceptInstanceRole incomingsRole = (FlexoConceptInstanceRole) transition.getDeclaredProperty("incomings");
		FlexoConceptInstanceRole outgoingsRole = (FlexoConceptInstanceRole) transition.getDeclaredProperty("outgoings");

		System.out.println("Incoming node");
		FlexoRolePropertyNode<?, ?> incomingsRoleNode = (FlexoRolePropertyNode) incomingsRole.getPrettyPrintDelegate();
		System.out.println(incomingsRoleNode.debug());

		System.out.println("Outgoing node");
		FlexoRolePropertyNode<?, ?> outgoingsRoleNode = (FlexoRolePropertyNode) outgoingsRole.getPrettyPrintDelegate();
		System.out.println(outgoingsRoleNode.debug());

		// DerivedRawSource derivedRawSource = fcNode.computeTextualRepresentation(fcNode.makePrettyPrintContext());
		// derivedRawSource.debugStringRepresentation();

	}

}
