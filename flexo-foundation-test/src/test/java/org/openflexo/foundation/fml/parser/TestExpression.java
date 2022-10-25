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
import org.openflexo.foundation.fml.parser.fmlnodes.expr.PlusExpressionNode;
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
public class TestExpression extends FMLParserTestCase {

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

		final Resource fmlFile = ResourceLocator.locateResource("FMLParsingExamples/TestExpression.fml");

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
		AssignationActionNode assignationNode = checkNodeForObject("(16:2)-(16:11)", null, assignationAction);

		DataBindingNode assignNode = checkNode("(16:2)-(16:3)", "a", (DataBindingNode) assignationNode.getChildren().get(0));
		BindingPathNode assignBPNode = checkNode("(16:2)-(16:3)", "a", (BindingPathNode) assignNode.getChildren().get(0));
		BindingVariableNode assignPathElementNode1 = checkNode("(16:2)-(16:3)", "a",
				(BindingVariableNode) assignBPNode.getChildren().get(0));

		ExpressionActionNode expressionNode = checkNodeForObject("(16:6)-(16:10)", null, assignationAction.getAssignableAction());
		DataBindingNode expressionValueNode = checkNode("(16:6)-(16:10)", "42 + 1", (DataBindingNode) expressionNode.getChildren().get(0));

		PlusExpressionNode plusExpressionNode = checkNode("(16:6)-(16:10)", "42 + 1",
				(PlusExpressionNode) expressionValueNode.getChildren().get(0));
		ConstantNode value1Node = checkNode("(16:6)-(16:8)", "42", (ConstantNode) plusExpressionNode.getChildren().get(0));
		ConstantNode value2Node = checkNode("(16:9)-(16:10)", "1", (ConstantNode) plusExpressionNode.getChildren().get(1));

	}
}
