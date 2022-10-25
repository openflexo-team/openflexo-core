/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Cartoeditor, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.foundation.fml.controlgraph.EmptyControlGraph;
import org.openflexo.foundation.fml.parser.fmlnodes.ActionSchemeNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoConceptNode;
import org.openflexo.foundation.fml.parser.fmlnodes.VirtualModelNode;
import org.openflexo.foundation.test.parser.FMLParserTestCase;
import org.openflexo.p2pp.RawSource;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Parse a FML file, perform some edits and checks that pretty-print is correct
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestFMLPrettyPrint5 extends FMLParserTestCase {

	private static FMLCompilationUnit compilationUnit;
	private static VirtualModel virtualModel;
	private static FlexoConcept conceptA;
	private static FlexoConcept conceptB;
	private static FlexoConcept conceptC;
	private static FlexoConcept conceptD;
	private static FlexoConcept conceptE;

	private static ActionScheme behaviour1;
	private static ActionScheme behaviour2;
	private static ActionScheme behaviour3;

	static FlexoEditor editor;

	@Test
	@TestOrder(1)
	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

	}

	private static VirtualModelNode vmNode;
	private static FlexoConceptNode conceptANode;
	private static FlexoConceptNode conceptBNode;
	private static FlexoConceptNode conceptCNode;
	private static FlexoConceptNode conceptDNode;
	private static FlexoConceptNode conceptENode;
	private static ActionSchemeNode behaviour1Node;
	private static ActionSchemeNode behaviour2Node;
	private static ActionSchemeNode behaviour3Node;

	@Test
	@TestOrder(2)
	public void loadInitialVersion() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		log("Initial version");

		final Resource fmlFile = ResourceLocator.locateResource("TestFMLPrettyPrint5/InitialModel.fml");
		compilationUnit = parseFile(fmlFile);
		assertNotNull(virtualModel = compilationUnit.getVirtualModel());
		assertEquals("TestViewPointA", virtualModel.getName());

		assertEquals(5, virtualModel.getFlexoConcepts().size());
		assertNotNull(conceptA = virtualModel.getFlexoConcept("ConceptA"));
		assertNotNull(conceptB = virtualModel.getFlexoConcept("ConceptB"));
		assertNotNull(conceptC = virtualModel.getFlexoConcept("ConceptC"));
		assertNotNull(conceptD = virtualModel.getFlexoConcept("ConceptD"));
		assertNotNull(conceptE = virtualModel.getFlexoConcept("ConceptE"));

		assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
		assertNotNull(vmNode = (VirtualModelNode) rootNode.getObjectNode(virtualModel));
		assertNotNull(conceptANode = (FlexoConceptNode) rootNode.getObjectNode(conceptA));
		assertNotNull(conceptBNode = (FlexoConceptNode) rootNode.getObjectNode(conceptB));
		assertNotNull(conceptCNode = (FlexoConceptNode) rootNode.getObjectNode(conceptC));

		assertNotNull(behaviour1 = (ActionScheme) conceptE.getFlexoBehaviour("firstBehaviour"));
		assertNotNull(behaviour2 = (ActionScheme) conceptE.getFlexoBehaviour("secondBehaviour", String.class));
		assertNotNull(behaviour3 = (ActionScheme) conceptE.getFlexoBehaviour("thirdBehaviour"));

		assertTrue(behaviour1.getControlGraph() instanceof EmptyControlGraph);
		assertNull(behaviour2.getControlGraph());
		assertNull(behaviour3.getControlGraph());

		assertNotNull(behaviour1Node = (ActionSchemeNode) rootNode.getObjectNode(behaviour1));
		assertNotNull(behaviour2Node = (ActionSchemeNode) rootNode.getObjectNode(behaviour2));
		assertNotNull(behaviour3Node = (ActionSchemeNode) rootNode.getObjectNode(behaviour3));

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint5/Step1Normalized.fml");

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint5/Step1PrettyPrint.fml");

		RawSource rawSource = rootNode.getRawSource();
		System.out.println(rawSource.debug());
		debug(rootNode, 0);

		assertEquals("(1:0)-(32:1)", rootNode.getLastParsedFragment().toString());
		assertEquals(null, rootNode.getPrelude());
		assertEquals(null, rootNode.getPostlude());

		conceptANode = checkNodeForObject("(9:1)-(11:2)", "(8:1)-(9:0)", "(11:2)-(12:0)", conceptA);

		/*assertEquals("(1:0)-(1:80)", useDeclNode.getLastParsedFragment().toString());
		assertEquals(null, useDeclNode.getPrelude());
		assertEquals("(1:80)-(3:0)", useDeclNode.getPostlude().toString());
		
		assertEquals("(3:0)-(3:73)", elementImportNode.getLastParsedFragment().toString());
		assertEquals(null, elementImportNode.getPrelude());
		assertEquals("(3:73)-(5:0)", elementImportNode.getPostlude().toString());
		
		assertEquals("(5:0)-(13:1)", vmNode.getLastParsedFragment().toString());
		assertEquals(null, vmNode.getPrelude());
		assertEquals(null, vmNode.getPostlude());
		
		assertEquals("(8:1)-(8:67)", modelSlotNode.getLastParsedFragment().toString());
		assertEquals("(8:0)-(8:1)", modelSlotNode.getPrelude().toString());
		assertEquals("(8:67)-(9:0)", modelSlotNode.getPostlude().toString());
		
		assertEquals("(8:33)-(8:48)", modelSlotP1Node.getLastParsedFragment().toString());
		assertEquals(null, modelSlotP1Node.getPrelude());
		assertEquals("(8:48)-(8:49)", modelSlotP1Node.getPostlude().toString());
		
		assertEquals("(8:49)-(8:65)", modelSlotP2Node.getLastParsedFragment().toString());
		assertEquals(null, modelSlotP2Node.getPrelude());
		assertEquals(null, modelSlotP2Node.getPostlude());
		*/
	}

	@Test
	@TestOrder(3)
	public void changeAbstractConceptA() throws ParseException, IOException {

		log("changeAbstractConceptA()");

		conceptA.setAbstract(true);
		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());

		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint5/Step2Normalized.fml");
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint5/Step2PrettyPrint.fml");

		conceptA.setAbstract(false);

	}

	@Test
	@TestOrder(4)
	public void changeConceptAVisibility() throws ParseException, IOException {

		log("changeConceptAVisibility()");

		conceptA.setVisibility(Visibility.Public);
		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());

		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint5/Step3Normalized.fml");
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint5/Step3PrettyPrint.fml");

	}

	@Test
	@TestOrder(5)
	public void changeConceptBVisibility() throws ParseException, IOException {

		log("changeConceptBVisibility()");

		conceptB.setVisibility(Visibility.Default);
		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());

		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint5/Step4Normalized.fml");
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint5/Step4PrettyPrint.fml");

	}

	@Test
	@TestOrder(6)
	public void changeBothConceptCAbstractAndVisibility() throws ParseException, IOException {

		log("changeBothConceptCAbstractAndVisibility()");

		conceptC.setAbstract(false);
		conceptC.setVisibility(Visibility.Public);
		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint5/Step5Normalized.fml");

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint5/Step5PrettyPrint.fml");

	}

	@Test
	@TestOrder(7)
	public void changeSomeConceptVisibilityAndAbstract() throws ParseException, IOException {

		log("changeSomeConceptVisibilityAndAbstract()");

		conceptA.setVisibility(Visibility.Private);
		conceptD.setAbstract(false);
		conceptD.setVisibility(Visibility.Protected);
		conceptE.setVisibility(Visibility.Default);

		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint5/Step6Normalized.fml");

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint5/Step6PrettyPrint.fml");

	}

	@Test
	@TestOrder(8)
	public void changeSomeMethodsVisibility() throws ParseException, IOException {

		log("changeSomeConceptVisibilityAndAbstract()");

		behaviour1.setVisibility(Visibility.Default);
		behaviour2.setVisibility(Visibility.Public);
		behaviour3.setVisibility(Visibility.Public);

		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint5/Step7Normalized.fml");

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint5/Step7PrettyPrint.fml");

	}

	@Test
	@TestOrder(9)
	public void changeFirstBehaviourImplementation() throws ParseException, IOException {

		log("changeFirstBehaviourImplementation()");

		behaviour1.setAbstract(true);
		behaviour1.setControlGraph(null);

		// behaviour2.setVisibility(Visibility.Public);
		// behaviour3.setVisibility(Visibility.Public);

		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint5/Step8Normalized.fml");

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint5/Step8PrettyPrint.fml");

	}

	@Test
	@TestOrder(10)
	public void changeOthersBehaviourImplementation() throws ParseException, IOException {

		log("changeFirstBehaviourImplementation()");

		behaviour2.setAbstract(false);
		behaviour2.setControlGraph(behaviour2.getFMLModelFactory().newEmptyControlGraph());

		behaviour3.setAbstract(true);

		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint5/Step9Normalized.fml");

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint5/Step9PrettyPrint.fml");

	}

}
