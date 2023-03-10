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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoBehaviourNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.AssignationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ExpressionActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.BindingPathNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.BindingVariableNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.ConstantNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.DataBindingNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.MethodCallBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.PlusExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.SimplePathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.SuperMethodCallBindingPathElementNode;
import org.openflexo.foundation.test.parser.FMLParserTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test BindingPath parsing
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestBindingPath extends FMLParserTestCase {

	static FlexoEditor editor;

	static FMLCompilationUnit compilationUnit;

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

		final Resource fmlFile = ResourceLocator.locateResource("FMLParsingExamples/TestBindingPath.fml");

		// System.out.println(FileUtils.fileContents(((FileResourceImpl) fmlFile).getFile()));

		compilationUnit = testFMLCompilationUnit(fmlFile);
		assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
	}

	@Test
	@TestOrder(3)
	public void testSimpleValue() throws ParseException, ModelDefinitionException, IOException {
		log("testSimpleValue()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testSimpleValue");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = checkNodeForObject("(11:1)-(13:2)", "(10:0)-(11:0)", "(13:2)-(14:0)", null, actionScheme);
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);
		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("a", assignationAction.getAssignation().toString());

		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		AssignationActionNode assignationNode = checkNodeForObject("(12:2)-(12:9)", null, assignationAction);

		DataBindingNode assignNode = checkNode("(12:2)-(12:3)", "a", (DataBindingNode) assignationNode.getChildren().get(0));
		BindingPathNode assignBPNode = checkNode("(12:2)-(12:3)", "a", (BindingPathNode) assignNode.getChildren().get(0));
		BindingVariableNode assignPathElementNode = checkNode("(12:2)-(12:3)", "a",
				(BindingVariableNode) assignBPNode.getChildren().get(0));

		ExpressionActionNode expressionNode = checkNodeForObject("(12:6)-(12:8)", null, assignationAction.getAssignableAction());
		DataBindingNode expressionValueNode = checkNode("(12:6)-(12:8)", "42", (DataBindingNode) expressionNode.getChildren().get(0));
		ConstantNode valueNode = checkNode("(12:6)-(12:8)", "42", (ConstantNode) expressionValueNode.getChildren().get(0));

	}

	@Test
	@TestOrder(4)
	public void testSimpleExpression() throws ParseException, ModelDefinitionException, IOException {

		log("testSimpleExpression()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testSimpleExpression");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = checkNodeForObject("(15:1)-(17:2)", "(14:0)-(15:0)", "(17:2)-(18:0)", null, actionScheme);
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);
		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();

		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		AssignationActionNode assignationNode = checkNodeForObject("(16:2)-(16:13)", null, assignationAction);

		DataBindingNode assignNode = checkNode("(16:2)-(16:5)", "a.b", (DataBindingNode) assignationNode.getChildren().get(0));
		BindingPathNode assignBPNode = checkNode("(16:2)-(16:5)", "a.b", (BindingPathNode) assignNode.getChildren().get(0));
		BindingVariableNode assignPathElementNode1 = checkNode("(16:2)-(16:3)", "a",
				(BindingVariableNode) assignBPNode.getChildren().get(0));
		SimplePathElementNode assignPathElementNode2 = checkNode("(16:4)-(16:5)", "UnresolvedSimplePathElement:b",
				(SimplePathElementNode) assignBPNode.getChildren().get(1));

		ExpressionActionNode expressionNode = checkNodeForObject("(16:8)-(16:12)", null, assignationAction.getAssignableAction());
		DataBindingNode expressionValueNode = checkNode("(16:8)-(16:12)", "c + 42", (DataBindingNode) expressionNode.getChildren().get(0));

		PlusExpressionNode plusExpressionNode = checkNode("(16:8)-(16:12)", "c + 42",
				(PlusExpressionNode) expressionValueNode.getChildren().get(0));
		BindingPathNode value1Node = checkNode("(16:8)-(16:9)", "c", (BindingPathNode) plusExpressionNode.getChildren().get(0));
		BindingVariableNode value1PathElementNode = checkNode("(16:8)-(16:9)", "c", (BindingVariableNode) value1Node.getChildren().get(0));
		ConstantNode value2Node = checkNode("(16:10)-(16:12)", "42", (ConstantNode) plusExpressionNode.getChildren().get(1));
	}

	@Test
	@TestOrder(5)
	public void testSimpleBindingPath() throws ParseException, ModelDefinitionException, IOException {
		log("testSimpleBindingPath()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testSimpleBindingPath");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = checkNodeForObject("(19:1)-(21:2)", "(18:0)-(19:0)", "(21:2)-(22:0)", null, actionScheme);
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);
		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("a", assignationAction.getAssignation().toString());

		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		AssignationActionNode assignationNode = checkNodeForObject("(20:2)-(20:10)", null, assignationAction);

		DataBindingNode assignNode = checkNode("(20:2)-(20:3)", "a", (DataBindingNode) assignationNode.getChildren().get(0));
		BindingPathNode assignBPNode = checkNode("(20:2)-(20:3)", "a", (BindingPathNode) assignNode.getChildren().get(0));
		BindingVariableNode assignPathElementNode1 = checkNode("(20:2)-(20:3)", "a",
				(BindingVariableNode) assignBPNode.getChildren().get(0));

		ExpressionActionNode expressionNode = checkNodeForObject("(20:6)-(20:9)", null, assignationAction.getAssignableAction());
		DataBindingNode expressionValueNode = checkNode("(20:6)-(20:9)", "b.c", (DataBindingNode) expressionNode.getChildren().get(0));
		BindingPathNode valueNode = checkNode("(20:6)-(20:9)", "b.c", (BindingPathNode) expressionValueNode.getChildren().get(0));
		BindingVariableNode value1PathElementNode = checkNode("(20:6)-(20:7)", "b", (BindingVariableNode) valueNode.getChildren().get(0));
		SimplePathElementNode value2PathElementNode = checkNode("(20:8)-(20:9)", "UnresolvedSimplePathElement:c",
				(SimplePathElementNode) valueNode.getChildren().get(1));
	}

	@Test
	@TestOrder(6)
	public void testMethodCall() throws ParseException, ModelDefinitionException, IOException {
		log("testMethodCall()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testMethodCall");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = checkNodeForObject("(23:1)-(25:2)", "(22:0)-(23:0)", "(25:2)-(26:0)", null, actionScheme);
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);
		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("a", assignationAction.getAssignation().toString());

		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		AssignationActionNode assignationNode = checkNodeForObject("(24:2)-(24:10)", null, assignationAction);

		ExpressionActionNode expressionNode = checkNodeForObject("(24:6)-(24:9)", null, assignationAction.getAssignableAction());
		DataBindingNode expressionValueNode = checkNode("(24:6)-(24:9)", "b()", (DataBindingNode) expressionNode.getChildren().get(0));
		BindingPathNode valueNode = checkNode("(24:6)-(24:9)", "b()", (BindingPathNode) expressionValueNode.getChildren().get(0));
		MethodCallBindingPathElementNode methodPathElementNode = checkNode("(24:6)-(24:9)", "JavaInstanceMethodPathElement:b()",
				(MethodCallBindingPathElementNode) valueNode.getChildren().get(0));
	}

	@Test
	@TestOrder(7)
	public void testMethodCall2() throws ParseException, ModelDefinitionException, IOException {
		log("testMethodCall2()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testMethodCall2");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = checkNodeForObject("(27:1)-(29:2)", "(26:0)-(27:0)", "(29:2)-(30:0)", null, actionScheme);
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);
		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("a", assignationAction.getAssignation().toString());

		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		AssignationActionNode assignationNode = checkNodeForObject("(28:2)-(28:14)", null, assignationAction);

		ExpressionActionNode expressionNode = checkNodeForObject("(28:6)-(28:13)", null, assignationAction.getAssignableAction());
		DataBindingNode expressionValueNode = checkNode("(28:6)-(28:13)", "b.c.d()", (DataBindingNode) expressionNode.getChildren().get(0));
		BindingPathNode valueNode = checkNode("(28:6)-(28:13)", "b.c.d()", (BindingPathNode) expressionValueNode.getChildren().get(0));
		BindingVariableNode value1PathElementNode = checkNode("(28:6)-(28:7)", "b", (BindingVariableNode) valueNode.getChildren().get(0));
		SimplePathElementNode value2PathElementNode = checkNode("(28:8)-(28:9)", "UnresolvedSimplePathElement:c",
				(SimplePathElementNode) valueNode.getChildren().get(1));
		MethodCallBindingPathElementNode methodPathElementNode = checkNode("(28:10)-(28:13)", "JavaInstanceMethodPathElement:d()",
				(MethodCallBindingPathElementNode) valueNode.getChildren().get(2));
	}

	@Test
	@TestOrder(8)
	public void testMethodCall3() throws ParseException, ModelDefinitionException, IOException {
		log("testMethodCall3()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testMethodCall3");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = checkNodeForObject("(31:1)-(33:2)", "(30:0)-(31:0)", "(33:2)-(34:0)", null, actionScheme);
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);
		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("a", assignationAction.getAssignation().toString());

		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		AssignationActionNode assignationNode = checkNodeForObject("(32:2)-(32:18)", null, assignationAction);

		ExpressionActionNode expressionNode = checkNodeForObject("(32:6)-(32:17)", null, assignationAction.getAssignableAction());
		DataBindingNode expressionValueNode = checkNode("(32:6)-(32:17)", "b.c().d().e",
				(DataBindingNode) expressionNode.getChildren().get(0));
		BindingPathNode valueNode = checkNode("(32:6)-(32:17)", "b.c().d().e", (BindingPathNode) expressionValueNode.getChildren().get(0));

		BindingVariableNode pathElementNode1 = checkNode("(32:6)-(32:7)", "b", (BindingVariableNode) valueNode.getChildren().get(0));
		MethodCallBindingPathElementNode pathElementNode2 = checkNode("(32:8)-(32:11)", "JavaInstanceMethodPathElement:c()",
				(MethodCallBindingPathElementNode) valueNode.getChildren().get(1));
		MethodCallBindingPathElementNode pathElementNode3 = checkNode("(32:12)-(32:15)", "JavaInstanceMethodPathElement:d()",
				(MethodCallBindingPathElementNode) valueNode.getChildren().get(2));
		SimplePathElementNode pathElementNode4 = checkNode("(32:16)-(32:17)", "UnresolvedSimplePathElement:e",
				(SimplePathElementNode) valueNode.getChildren().get(3));
	}

	@Test
	@TestOrder(9)
	public void testMethodCall4() throws ParseException, ModelDefinitionException, IOException {
		log("testMethodCall4()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testMethodCall4");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = checkNodeForObject("(35:1)-(37:2)", "(34:0)-(35:0)", "(37:2)-(38:0)", null, actionScheme);
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);
		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("a", assignationAction.getAssignation().toString());

		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		AssignationActionNode assignationNode = checkNodeForObject("(36:2)-(36:14)", null, assignationAction);

		ExpressionActionNode expressionNode = checkNodeForObject("(36:6)-(36:13)", null, assignationAction.getAssignableAction());
		DataBindingNode expressionValueNode = checkNode("(36:6)-(36:13)", "super()", (DataBindingNode) expressionNode.getChildren().get(0));
		BindingPathNode valueNode = checkNode("(36:6)-(36:13)", "super()", (BindingPathNode) expressionValueNode.getChildren().get(0));

		SuperMethodCallBindingPathElementNode pathElementNode1 = checkNode("(36:6)-(36:13)", "FlexoBehaviourPathElement:super()",
				(SuperMethodCallBindingPathElementNode) valueNode.getChildren().get(0));
	}

	@Test
	@TestOrder(10)
	public void testMethodCall5() throws ParseException, ModelDefinitionException, IOException {
		log("testMethodCall5()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testMethodCall5");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = checkNodeForObject("(39:1)-(41:2)", "(38:0)-(39:0)", "(41:2)-(42:0)", null, actionScheme);
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);
		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("a", assignationAction.getAssignation().toString());

		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		AssignationActionNode assignationNode = checkNodeForObject("(40:2)-(40:19)", null, assignationAction);

		ExpressionActionNode expressionNode = checkNodeForObject("(40:6)-(40:18)", null, assignationAction.getAssignableAction());
		DataBindingNode expressionValueNode = checkNode("(40:6)-(40:18)", "super.init()",
				(DataBindingNode) expressionNode.getChildren().get(0));
		BindingPathNode valueNode = checkNode("(40:6)-(40:18)", "super.init()", (BindingPathNode) expressionValueNode.getChildren().get(0));

		BindingVariableNode pathElementNode1 = checkNode("(40:6)-(40:11)", "super", (BindingVariableNode) valueNode.getChildren().get(0));
		MethodCallBindingPathElementNode pathElementNode2 = checkNode("(40:12)-(40:18)", "FlexoBehaviourPathElement:init()",
				(MethodCallBindingPathElementNode) valueNode.getChildren().get(1));
	}

	@Test
	@TestOrder(11)
	public void testMethodCallWithArguments() throws ParseException, ModelDefinitionException, IOException {
		log("testMethodCallWithArguments()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testMethodCallWithArguments");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = checkNodeForObject("(43:1)-(45:2)", "(42:0)-(43:0)", "(45:2)-(46:0)", null, actionScheme);
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);
		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("a", assignationAction.getAssignation().toString());

		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		AssignationActionNode assignationNode = checkNodeForObject("(44:2)-(44:13)", null, assignationAction);

		ExpressionActionNode expressionNode = checkNodeForObject("(44:6)-(44:12)", null, assignationAction.getAssignableAction());
		DataBindingNode expressionValueNode = checkNode("(44:6)-(44:12)", "b.c(1)", (DataBindingNode) expressionNode.getChildren().get(0));
		BindingPathNode valueNode = checkNode("(44:6)-(44:12)", "b.c(1)", (BindingPathNode) expressionValueNode.getChildren().get(0));

		assertEquals(2, valueNode.getChildren().size());
		BindingVariableNode pathElementNode1 = checkNode("(44:6)-(44:7)", "b", (BindingVariableNode) valueNode.getChildren().get(0));
		MethodCallBindingPathElementNode pathElementNode2 = checkNode("(44:8)-(44:12)", "JavaInstanceMethodPathElement:c(1)",
				(MethodCallBindingPathElementNode) valueNode.getChildren().get(1));

		// SuperMethodCallBindingPathElementNode pathElementNode1 = checkNode("(44:6)-(44:13)", "Call[b()]",
		// (SuperMethodCallBindingPathElementNode) valueNode.getChildren().get(0));
	}

}
