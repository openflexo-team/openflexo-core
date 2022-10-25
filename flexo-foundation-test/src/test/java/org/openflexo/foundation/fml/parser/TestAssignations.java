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
import org.openflexo.foundation.test.parser.FMLParserTestCase;
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test assignations parsing
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestAssignations extends FMLParserTestCase {

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

		final Resource fmlFile = ResourceLocator.locateResource("FMLExamples/TestAssignations.fml");

		compilationUnit = testFMLCompilationUnit(fmlFile);
		assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
	}

	@Test
	@TestOrder(4)
	public void testAssignConstant() throws ParseException, ModelDefinitionException, IOException {
		log("testAssignConstant()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testAssignConstant");
		assertNotNull(actionScheme);

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("i", assignationAction.getAssignation().toString());
		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		ExpressionAction<?> expAction = (ExpressionAction) assignationAction.getAssignableAction();
		assertEquals("1", expAction.getExpression().toString());

		AssignationActionNode assignationNode = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignationAction);
		ExpressionActionNode expressionNode = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expAction);

		assertEquals("(11:1)-(13:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(10:0)-(11:0)", behaviourNode.getPrelude().toString());
		assertEquals("(13:2)-(14:0)", behaviourNode.getPostlude().toString());

		assertEquals("(12:2)-(12:8)", assignationNode.getLastParsedFragment().toString());
		assertEquals(null, assignationNode.getPrelude());
		assertEquals(null, assignationNode.getPostlude());

		assertEquals("(12:6)-(12:7)", expressionNode.getLastParsedFragment().toString());
		assertEquals(null, expressionNode.getPrelude());
		assertEquals(null, expressionNode.getPostlude());

	}

	@Test
	@TestOrder(5)
	public void testAssignNumericExpression() throws ParseException, ModelDefinitionException, IOException {
		log("testAssignNumericExpression()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testAssignNumericExpression");
		assertNotNull(actionScheme);

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("i", assignationAction.getAssignation().toString());
		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		ExpressionAction<?> expAction = (ExpressionAction) assignationAction.getAssignableAction();
		assertEquals("1 + 2", expAction.getExpression().toString());

		AssignationActionNode assignationNode = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignationAction);
		ExpressionActionNode expressionNode = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expAction);

		assertEquals("(15:1)-(17:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(14:1)-(15:0)", behaviourNode.getPrelude().toString());
		assertEquals("(17:2)-(18:0)", behaviourNode.getPostlude().toString());

		assertEquals("(16:2)-(16:10)", assignationNode.getLastParsedFragment().toString());
		assertEquals(null, assignationNode.getPrelude());
		assertEquals(null, assignationNode.getPostlude());

		assertEquals("(16:6)-(16:9)", expressionNode.getLastParsedFragment().toString());
		assertEquals(null, expressionNode.getPrelude());
		assertEquals(null, expressionNode.getPostlude());

	}

	@Test
	@TestOrder(6)
	public void testAssignAssignation() throws ParseException, ModelDefinitionException, IOException {
		log("testAssignAssignation()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testAssignAssignation");
		assertNotNull(actionScheme);

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("i", assignationAction.getAssignation().toString());
		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		ExpressionAction<?> expAction = (ExpressionAction) assignationAction.getAssignableAction();
		assertEquals("j = 1", expAction.getExpression().toString());

		AssignationActionNode assignationNode = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignationAction);
		ExpressionActionNode expressionNode = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expAction);

		assertEquals("(19:1)-(21:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(18:1)-(19:0)", behaviourNode.getPrelude().toString());
		assertEquals("(21:2)-(22:0)", behaviourNode.getPostlude().toString());

		assertEquals("(20:2)-(20:14)", assignationNode.getLastParsedFragment().toString());
		assertEquals(null, assignationNode.getPrelude());
		assertEquals(null, assignationNode.getPostlude());

		assertEquals("(20:6)-(20:13)", expressionNode.getLastParsedFragment().toString());
		assertEquals(null, expressionNode.getPrelude());
		assertEquals(null, expressionNode.getPostlude());

	}

	@Test
	@TestOrder(7)
	public void testAssignConditional() throws ParseException, ModelDefinitionException, IOException {
		log("testAssignConditional()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testAssignConditional");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("i", assignationAction.getAssignation().toString());
		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		ExpressionAction<?> expAction = (ExpressionAction) assignationAction.getAssignableAction();
		assertEquals("(j > 0 ? 1 : 2)", expAction.getExpression().toString());

		AssignationActionNode assignationNode = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignationAction);
		ExpressionActionNode expressionNode = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expAction);

		assertEquals("(23:1)-(25:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(22:1)-(23:0)", behaviourNode.getPrelude().toString());
		assertEquals("(25:2)-(26:0)", behaviourNode.getPostlude().toString());

		assertEquals("(24:2)-(24:16)", assignationNode.getLastParsedFragment().toString());
		assertEquals(null, assignationNode.getPrelude());
		assertEquals(null, assignationNode.getPostlude());

		assertEquals("(24:6)-(24:15)", expressionNode.getLastParsedFragment().toString());
		assertEquals(null, expressionNode.getPrelude());
		assertEquals(null, expressionNode.getPostlude());

	}

	@Test
	@TestOrder(8)
	public void testAssignBinding1() throws ParseException, ModelDefinitionException, IOException {
		log("testAssignBinding1()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testAssignBinding1");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("i", assignationAction.getAssignation().toString());
		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		ExpressionAction<?> expAction = (ExpressionAction) assignationAction.getAssignableAction();
		assertEquals("j", expAction.getExpression().toString());

		AssignationActionNode assignationNode = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignationAction);
		ExpressionActionNode expressionNode = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expAction);

		assertEquals("(27:1)-(29:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(26:1)-(27:0)", behaviourNode.getPrelude().toString());
		assertEquals("(29:2)-(30:0)", behaviourNode.getPostlude().toString());

		assertEquals("(28:2)-(28:8)", assignationNode.getLastParsedFragment().toString());
		assertEquals(null, assignationNode.getPrelude());
		assertEquals(null, assignationNode.getPostlude());

		assertEquals("(28:6)-(28:7)", expressionNode.getLastParsedFragment().toString());
		assertEquals(null, expressionNode.getPrelude());
		assertEquals(null, expressionNode.getPostlude());

	}

	@Test
	@TestOrder(9)
	public void testAssignBinding2() throws ParseException, ModelDefinitionException, IOException {
		log("testAssignBinding2()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testAssignBinding2");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("i", assignationAction.getAssignation().toString());
		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		ExpressionAction<?> expAction = (ExpressionAction) assignationAction.getAssignableAction();
		assertEquals("this.isa.binding", expAction.getExpression().toString());

		AssignationActionNode assignationNode = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignationAction);
		ExpressionActionNode expressionNode = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expAction);

		assertEquals("(31:1)-(33:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(30:0)-(31:0)", behaviourNode.getPrelude().toString());
		assertEquals("(33:2)-(34:0)", behaviourNode.getPostlude().toString());

		assertEquals("(32:2)-(32:23)", assignationNode.getLastParsedFragment().toString());
		assertEquals(null, assignationNode.getPrelude());
		assertEquals(null, assignationNode.getPostlude());

		assertEquals("(32:6)-(32:22)", expressionNode.getLastParsedFragment().toString());
		assertEquals(null, expressionNode.getPrelude());
		assertEquals(null, expressionNode.getPostlude());

	}

	@Test
	@TestOrder(10)
	public void testAssignBinding3() throws ParseException, ModelDefinitionException, IOException {
		log("testAssignBinding3()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testAssignBinding3");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("i", assignationAction.getAssignation().toString());
		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		ExpressionAction<?> expAction = (ExpressionAction) assignationAction.getAssignableAction();
		assertEquals("this.isa.another.binding(1,2)", expAction.getExpression().toString());

		AssignationActionNode assignationNode = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignationAction);
		ExpressionActionNode expressionNode = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expAction);

		assertEquals("(35:1)-(37:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(34:0)-(35:0)", behaviourNode.getPrelude().toString());
		assertEquals("(37:2)-(38:0)", behaviourNode.getPostlude().toString());

		assertEquals("(36:2)-(36:36)", assignationNode.getLastParsedFragment().toString());
		assertEquals(null, assignationNode.getPrelude());
		assertEquals(null, assignationNode.getPostlude());

		assertEquals("(36:6)-(36:35)", expressionNode.getLastParsedFragment().toString());
		assertEquals(null, expressionNode.getPrelude());
		assertEquals(null, expressionNode.getPostlude());

	}

	@Test
	@TestOrder(11)
	public void testAssignBinding4() throws ParseException, ModelDefinitionException, IOException {
		log("testAssignBinding4()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testAssignBinding4");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("i", assignationAction.getAssignation().toString());
		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		ExpressionAction<?> expAction = (ExpressionAction) assignationAction.getAssignableAction();
		assertEquals("this.isa.another.binding(1,2) * 8 / 4 - 2 * foo(2)", expAction.getExpression().toString());

		AssignationActionNode assignationNode = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignationAction);
		ExpressionActionNode expressionNode = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expAction);

		assertEquals("(39:1)-(41:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(38:1)-(39:0)", behaviourNode.getPrelude().toString());
		assertEquals("(41:2)-(42:0)", behaviourNode.getPostlude().toString());

		assertEquals("(40:2)-(40:51)", assignationNode.getLastParsedFragment().toString());
		assertEquals(null, assignationNode.getPrelude());
		assertEquals(null, assignationNode.getPostlude());

		assertEquals("(40:6)-(40:50)", expressionNode.getLastParsedFragment().toString());
		assertEquals(null, expressionNode.getPrelude());
		assertEquals(null, expressionNode.getPostlude());

	}

	@Test
	@TestOrder(12)
	public void testAssignNewInstance() throws ParseException, ModelDefinitionException, IOException {
		log("testAssignNewInstance()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testAssignNewInstance");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("aConcept", assignationAction.getAssignation().toString());
		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		ExpressionAction expressionAction = (ExpressionAction) assignationAction.getAssignableAction();

		AssignationActionNode assignationNode = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignationAction);
		ExpressionActionNode expressionActionNode = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expressionAction);

		assertEquals("(43:1)-(45:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(42:1)-(43:0)", behaviourNode.getPrelude().toString());
		assertEquals("(45:2)-(46:0)", behaviourNode.getPostlude().toString());

		assertEquals("(44:2)-(44:29)", assignationNode.getLastParsedFragment().toString());
		assertEquals(null, assignationNode.getPrelude());
		assertEquals(null, assignationNode.getPostlude());

		assertEquals("(44:13)-(44:28)", expressionActionNode.getLastParsedFragment().toString());
		assertEquals(null, expressionActionNode.getPrelude());
		assertEquals(null, expressionActionNode.getPostlude());

	}

}
