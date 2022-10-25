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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.controlgraph.EmptyControlGraph;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoBehaviourNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.AssignationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ExpressionActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.SequenceNode;
import org.openflexo.foundation.test.parser.FMLParserTestCase;
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test sequences parsing
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestSequences extends FMLParserTestCase {

	static FlexoEditor editor;

	static FMLCompilationUnit compilationUnit;
	static FMLCompilationUnitNode rootNode;

	@Test
	@TestOrder(1)
	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

	}

	@Test
	@TestOrder(2)
	public void loadCompilationUnit() throws ParseException, ModelDefinitionException, IOException {
		log("Initial version");

		final Resource fmlFile = ResourceLocator.locateResource("FMLExamples/TestSequences.fml");

		compilationUnit = testFMLCompilationUnit(fmlFile);
		assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
	}

	@Test
	@TestOrder(3)
	public void testNullControlGraph() throws ParseException, ModelDefinitionException, IOException {
		log("testNullControlGraph()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testAbstract");
		assertNotNull(actionScheme);

		assertNull(actionScheme.getControlGraph());

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		assertEquals("(12:1)-(12:16)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(11:0)-(12:0)", behaviourNode.getPrelude().toString());
		assertEquals("(12:16)-(13:0)", behaviourNode.getPostlude().toString());

	}

	@Test
	@TestOrder(4)
	public void testEmptyControlGraph() throws ParseException, ModelDefinitionException, IOException {
		log("testEmptyControlGraph()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testNoStatement");
		assertNotNull(actionScheme);

		assertTrue(actionScheme.getControlGraph() instanceof EmptyControlGraph);

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		ControlGraphNode<?, ?> controlGraphNode = (ControlGraphNode<?, ?>) rootNode.getObjectNode(actionScheme.getControlGraph());

		assertEquals("(14:1)-(15:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(13:2)-(14:0)", behaviourNode.getPrelude().toString());
		assertEquals("(15:2)-(16:0)", behaviourNode.getPostlude().toString());

		assertEquals("(14:20)-(15:1)", controlGraphNode.getLastParsedFragment().toString());
		assertEquals(null, controlGraphNode.getPrelude());
		assertEquals(null, controlGraphNode.getPostlude());

	}

	@Test
	@TestOrder(5)
	public void testOneStatement() throws ParseException, ModelDefinitionException, IOException {
		log("testOneStatement()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testOneStatement");
		assertNotNull(actionScheme);

		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		ControlGraphNode<?, ?> controlGraphNode = (ControlGraphNode<?, ?>) rootNode.getObjectNode(actionScheme.getControlGraph());

		assertEquals("(17:1)-(19:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(16:2)-(17:0)", behaviourNode.getPrelude().toString());
		assertEquals("(19:2)-(20:0)", behaviourNode.getPostlude().toString());

		assertEquals("(18:2)-(18:6)", controlGraphNode.getLastParsedFragment().toString());
		assertEquals(null, controlGraphNode.getPrelude());
		assertEquals(null, controlGraphNode.getPostlude());

	}

	@Test
	@TestOrder(6)
	public void testTwoStatements() throws ParseException, ModelDefinitionException, IOException {
		log("testTwoStatements()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testTwoStatements");
		assertNotNull(actionScheme);

		assertTrue(actionScheme.getControlGraph() instanceof Sequence);
		Sequence sequence = (Sequence) actionScheme.getControlGraph();
		assertTrue(sequence.getControlGraph1() instanceof AssignationAction);
		assertTrue(sequence.getControlGraph2() instanceof AssignationAction);
		AssignationAction<?> assignation1 = (AssignationAction<?>) sequence.getControlGraph1();
		AssignationAction<?> assignation2 = (AssignationAction<?>) sequence.getControlGraph2();
		assertTrue(assignation1.getAssignableAction() instanceof ExpressionAction);
		assertTrue(assignation2.getAssignableAction() instanceof ExpressionAction);
		ExpressionAction<?> expression1 = (ExpressionAction<?>) assignation1.getAssignableAction();
		ExpressionAction<?> expression2 = (ExpressionAction<?>) assignation2.getAssignableAction();

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		SequenceNode sequenceNode = (SequenceNode) (P2PPNode) rootNode.getObjectNode(actionScheme.getControlGraph());
		AssignationActionNode assignation1Node = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignation1);
		AssignationActionNode assignation2Node = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignation2);
		ExpressionActionNode expression1Node = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expression1);
		ExpressionActionNode expression2Node = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expression2);

		assertEquals("(21:1)-(24:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(20:1)-(21:0)", behaviourNode.getPrelude().toString());
		assertEquals("(24:2)-(25:0)", behaviourNode.getPostlude().toString());

		assertEquals("(22:2)-(23:6)", sequenceNode.getLastParsedFragment().toString());
		assertEquals(null, sequenceNode.getPrelude());
		assertEquals(null, sequenceNode.getPostlude());

		assertEquals("(22:2)-(22:6)", assignation1Node.getLastParsedFragment().toString());
		assertEquals(null, assignation1Node.getPrelude());
		assertEquals(null, assignation1Node.getPostlude());

		assertEquals("(22:4)-(22:5)", expression1Node.getLastParsedFragment().toString());
		assertEquals(null, expression1Node.getPrelude());
		assertEquals(null, expression1Node.getPostlude());

		assertEquals("(23:2)-(23:6)", assignation2Node.getLastParsedFragment().toString());
		assertEquals(null, assignation2Node.getPrelude());
		assertEquals(null, assignation2Node.getPostlude());

		assertEquals("(23:4)-(23:5)", expression2Node.getLastParsedFragment().toString());
		assertEquals(null, expression2Node.getPrelude());
		assertEquals(null, expression2Node.getPostlude());

	}

	@Test
	@TestOrder(7)
	public void testThreeStatements() throws ParseException, ModelDefinitionException, IOException {
		log("testThreeStatements()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testThreeStatements");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		assertTrue(actionScheme.getControlGraph() instanceof Sequence);
		Sequence sequence1 = (Sequence) actionScheme.getControlGraph();
		assertTrue(sequence1.getControlGraph1() instanceof AssignationAction);
		assertTrue(sequence1.getControlGraph2() instanceof Sequence);
		Sequence sequence2 = (Sequence) sequence1.getControlGraph2();
		assertTrue(sequence2.getControlGraph1() instanceof AssignationAction);
		assertTrue(sequence2.getControlGraph2() instanceof AssignationAction);

		AssignationAction<?> assignation1 = (AssignationAction<?>) sequence1.getControlGraph1();
		AssignationAction<?> assignation2 = (AssignationAction<?>) sequence2.getControlGraph1();
		AssignationAction<?> assignation3 = (AssignationAction<?>) sequence2.getControlGraph2();
		assertTrue(assignation1.getAssignableAction() instanceof ExpressionAction);
		assertTrue(assignation2.getAssignableAction() instanceof ExpressionAction);
		assertTrue(assignation3.getAssignableAction() instanceof ExpressionAction);
		ExpressionAction<?> expression1 = (ExpressionAction<?>) assignation1.getAssignableAction();
		ExpressionAction<?> expression2 = (ExpressionAction<?>) assignation2.getAssignableAction();
		ExpressionAction<?> expression3 = (ExpressionAction<?>) assignation3.getAssignableAction();

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		SequenceNode sequence1Node = (SequenceNode) (P2PPNode) rootNode.getObjectNode(sequence1);
		SequenceNode sequence2Node = (SequenceNode) (P2PPNode) rootNode.getObjectNode(sequence2);
		AssignationActionNode assignation1Node = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignation1);
		AssignationActionNode assignation2Node = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignation2);
		AssignationActionNode assignation3Node = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignation3);
		ExpressionActionNode expression1Node = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expression1);
		ExpressionActionNode expression2Node = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expression2);
		ExpressionActionNode expression3Node = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expression3);

		assertEquals("(26:1)-(30:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(25:1)-(26:0)", behaviourNode.getPrelude().toString());
		assertEquals("(30:2)-(31:0)", behaviourNode.getPostlude().toString());

		assertEquals("(27:2)-(29:6)", sequence1Node.getLastParsedFragment().toString());
		assertEquals(null, sequence1Node.getPrelude());
		assertEquals(null, sequence1Node.getPostlude());

		assertEquals("(27:2)-(27:6)", assignation1Node.getLastParsedFragment().toString());
		assertEquals(null, assignation1Node.getPrelude());
		assertEquals(null, assignation1Node.getPostlude());

		assertEquals("(27:4)-(27:5)", expression1Node.getLastParsedFragment().toString());
		assertEquals(null, expression1Node.getPrelude());
		assertEquals(null, expression1Node.getPostlude());

		assertEquals("(28:2)-(29:6)", sequence2Node.getLastParsedFragment().toString());
		assertEquals(null, sequence2Node.getPrelude());
		assertEquals(null, sequence2Node.getPostlude());

		assertEquals("(28:2)-(28:6)", assignation2Node.getLastParsedFragment().toString());
		assertEquals(null, assignation2Node.getPrelude());
		assertEquals(null, assignation2Node.getPostlude());

		assertEquals("(28:4)-(28:5)", expression2Node.getLastParsedFragment().toString());
		assertEquals(null, expression2Node.getPrelude());
		assertEquals(null, expression2Node.getPostlude());

		assertEquals("(29:2)-(29:6)", assignation3Node.getLastParsedFragment().toString());
		assertEquals(null, assignation3Node.getPrelude());
		assertEquals(null, assignation3Node.getPostlude());

		assertEquals("(29:4)-(29:5)", expression3Node.getLastParsedFragment().toString());
		assertEquals(null, expression3Node.getPrelude());
		assertEquals(null, expression3Node.getPostlude());

	}

	@Test
	@TestOrder(8)
	public void testFourStatements() throws ParseException, ModelDefinitionException, IOException {
		log("testFourStatements()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testFourStatements");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		assertTrue(actionScheme.getControlGraph() instanceof Sequence);
		Sequence sequence1 = (Sequence) actionScheme.getControlGraph();
		assertTrue(sequence1.getControlGraph1() instanceof AssignationAction);
		assertTrue(sequence1.getControlGraph2() instanceof Sequence);
		Sequence sequence2 = (Sequence) sequence1.getControlGraph2();
		assertTrue(sequence2.getControlGraph1() instanceof AssignationAction);
		assertTrue(sequence2.getControlGraph2() instanceof Sequence);
		Sequence sequence3 = (Sequence) sequence2.getControlGraph2();
		assertTrue(sequence3.getControlGraph1() instanceof AssignationAction);
		assertTrue(sequence3.getControlGraph2() instanceof AssignationAction);

		AssignationAction<?> assignation1 = (AssignationAction<?>) sequence1.getControlGraph1();
		AssignationAction<?> assignation2 = (AssignationAction<?>) sequence2.getControlGraph1();
		AssignationAction<?> assignation3 = (AssignationAction<?>) sequence3.getControlGraph1();
		AssignationAction<?> assignation4 = (AssignationAction<?>) sequence3.getControlGraph2();
		assertTrue(assignation1.getAssignableAction() instanceof ExpressionAction);
		assertTrue(assignation2.getAssignableAction() instanceof ExpressionAction);
		assertTrue(assignation3.getAssignableAction() instanceof ExpressionAction);
		assertTrue(assignation4.getAssignableAction() instanceof ExpressionAction);
		ExpressionAction<?> expression1 = (ExpressionAction<?>) assignation1.getAssignableAction();
		ExpressionAction<?> expression2 = (ExpressionAction<?>) assignation2.getAssignableAction();
		ExpressionAction<?> expression3 = (ExpressionAction<?>) assignation3.getAssignableAction();
		ExpressionAction<?> expression4 = (ExpressionAction<?>) assignation4.getAssignableAction();

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		SequenceNode sequence1Node = (SequenceNode) (P2PPNode) rootNode.getObjectNode(sequence1);
		SequenceNode sequence2Node = (SequenceNode) (P2PPNode) rootNode.getObjectNode(sequence2);
		SequenceNode sequence3Node = (SequenceNode) (P2PPNode) rootNode.getObjectNode(sequence3);
		AssignationActionNode assignation1Node = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignation1);
		AssignationActionNode assignation2Node = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignation2);
		AssignationActionNode assignation3Node = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignation3);
		AssignationActionNode assignation4Node = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignation4);
		ExpressionActionNode expression1Node = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expression1);
		ExpressionActionNode expression2Node = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expression2);
		ExpressionActionNode expression3Node = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expression3);
		ExpressionActionNode expression4Node = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expression4);

		assertEquals("(32:1)-(37:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(31:1)-(32:0)", behaviourNode.getPrelude().toString());
		assertEquals("(37:2)-(38:0)", behaviourNode.getPostlude().toString());

		assertEquals("(33:2)-(36:6)", sequence1Node.getLastParsedFragment().toString());
		assertEquals(null, sequence1Node.getPrelude());
		assertEquals(null, sequence1Node.getPostlude());

		assertEquals("(33:2)-(33:6)", assignation1Node.getLastParsedFragment().toString());
		assertEquals(null, assignation1Node.getPrelude());
		assertEquals(null, assignation1Node.getPostlude());

		assertEquals("(33:4)-(33:5)", expression1Node.getLastParsedFragment().toString());
		assertEquals(null, expression1Node.getPrelude());
		assertEquals(null, expression1Node.getPostlude());

		assertEquals("(34:2)-(36:6)", sequence2Node.getLastParsedFragment().toString());
		assertEquals(null, sequence2Node.getPrelude());
		assertEquals(null, sequence2Node.getPostlude());

		assertEquals("(34:2)-(34:6)", assignation2Node.getLastParsedFragment().toString());
		assertEquals(null, assignation2Node.getPrelude());
		assertEquals(null, assignation2Node.getPostlude());

		assertEquals("(34:4)-(34:5)", expression2Node.getLastParsedFragment().toString());
		assertEquals(null, expression2Node.getPrelude());
		assertEquals(null, expression2Node.getPostlude());

		assertEquals("(35:2)-(36:6)", sequence3Node.getLastParsedFragment().toString());
		assertEquals(null, sequence3Node.getPrelude());
		assertEquals(null, sequence3Node.getPostlude());

		assertEquals("(35:2)-(35:6)", assignation3Node.getLastParsedFragment().toString());
		assertEquals(null, assignation3Node.getPrelude());
		assertEquals(null, assignation3Node.getPostlude());

		assertEquals("(35:4)-(35:5)", expression3Node.getLastParsedFragment().toString());
		assertEquals(null, expression3Node.getPrelude());
		assertEquals(null, expression3Node.getPostlude());

		assertEquals("(36:2)-(36:6)", assignation4Node.getLastParsedFragment().toString());
		assertEquals(null, assignation4Node.getPrelude());
		assertEquals(null, assignation4Node.getPostlude());

		assertEquals("(36:4)-(36:5)", expression4Node.getLastParsedFragment().toString());
		assertEquals(null, expression4Node.getPrelude());
		assertEquals(null, expression4Node.getPostlude());

	}

	/*testOneStatement() {
		i=1;
	}
	
	testTwoStatements() {
		i=1;
		i=2;
	}
	
	testThreeStatements() {
		i=1;
		i=2;
		i=3;
	}
	
	testFourStatements() {
		i=1;
		i=2;
		i=3;
		i=4;
	}*/

}
