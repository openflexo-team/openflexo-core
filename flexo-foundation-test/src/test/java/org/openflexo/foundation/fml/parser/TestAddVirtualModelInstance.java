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
import org.openflexo.foundation.fml.parser.fmlnodes.expr.DataBindingNode;
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
public class TestAddVirtualModelInstance extends FMLParserTestCase {

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

		final Resource fmlFile = ResourceLocator.locateResource("FMLExamples/TestAddVirtualModelInstance2.fml");

		// System.out.println("fmlFile=" + fmlFile);
		// System.out.println(FileUtils.fileContents(((FileResourceImpl) fmlFile).getFile()));

		compilationUnit = testFMLCompilationUnit(fmlFile);
		assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
	}

	@Test
	@TestOrder(3)
	public void testNewSimpleInstance() throws ParseException, ModelDefinitionException, IOException {
		log("testNewSimpleInstance()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testNewInstance");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate();
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		assertTrue(actionScheme.getControlGraph() instanceof AssignationAction);
		AssignationAction assignationAction = (AssignationAction) actionScheme.getControlGraph();
		assertEquals("myModel", assignationAction.getAssignation().toString());
		assertTrue(assignationAction.getAssignableAction() instanceof ExpressionAction);
		ExpressionAction expressionAction = (ExpressionAction) assignationAction.getAssignableAction();

		AssignationActionNode assignationNode = (AssignationActionNode) (P2PPNode) rootNode.getObjectNode(assignationAction);
		ExpressionActionNode expressionActionNode = (ExpressionActionNode) (P2PPNode) rootNode.getObjectNode(expressionAction);
		DataBindingNode dbNode = (DataBindingNode) (P2PPNode) rootNode.getObjectNode(expressionAction.getExpression());

		/*BehaviourCallArgumentNode arg1Node = (BehaviourCallArgumentNode) (P2PPNode) rootNode
				.getObjectNode(expressionAction.getParameters().get(0));
		BehaviourCallArgumentNode arg2Node = (BehaviourCallArgumentNode) (P2PPNode) rootNode
				.getObjectNode(expressionAction.getParameters().get(1));*/

		assertEquals("new MyModel(\"test\",3) with (virtualModelInstanceName=\"foo\")", expressionAction.getFMLPrettyPrint());
		assertEquals("new MyModel(\"test\",3) with (virtualModelInstanceName=\"foo\")", expressionAction.getNormalizedFML());

		assertEquals("(13:1)-(15:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(12:0)-(13:0)", behaviourNode.getPrelude().toString());
		assertEquals("(15:2)-(16:0)", behaviourNode.getPostlude().toString());

		assertEquals("(14:2)-(14:72)", assignationNode.getLastParsedFragment().toString());
		assertEquals(null, assignationNode.getPrelude());
		assertEquals(null, assignationNode.getPostlude());

		/*assertEquals("(14:12)-(14:71)", addActionNode.getLastParsedFragment().toString());
		assertEquals(null, addActionNode.getPrelude());
		assertEquals(null, addActionNode.getPostlude());
		
		assertEquals("(14:24)-(14:30)", arg1Node.getLastParsedFragment().toString());
		assertEquals(null, arg1Node.getPrelude());
		assertEquals("(14:30)-(14:31)", arg1Node.getPostlude().toString());
		
		assertEquals("(14:31)-(14:32)", arg2Node.getLastParsedFragment().toString());
		assertEquals(null, arg2Node.getPrelude());
		assertEquals(null, arg2Node.getPostlude());*/

		System.out.println("FML:" + actionScheme.getFMLPrettyPrint());

	}

}
