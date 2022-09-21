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
public class TestAddFlexoConceptInstance extends FMLParserTestCase {

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

		final Resource fmlFile = ResourceLocator.locateResource("FMLExamples/TestAddFlexoConceptInstance.fml");

		// System.out.println(FileUtils.fileContents(((FileResourceImpl) fmlFile).getFile()));

		compilationUnit = testFMLCompilationUnit(fmlFile);
		assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
	}

	@SuppressWarnings("rawtypes")
	@Test
	@TestOrder(3)
	public void testNewSimpleInstance() throws ParseException, ModelDefinitionException, IOException {
		log("testNewSimpleInstance()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("testNewSimpleInstance");
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

		assertEquals("(9:1)-(11:2)", behaviourNode.getLastParsedFragment().toString());
		assertEquals("(8:0)-(9:0)", behaviourNode.getPrelude().toString());
		assertEquals("(11:2)-(12:0)", behaviourNode.getPostlude().toString());

		assertEquals("(10:2)-(10:32)", assignationNode.getLastParsedFragment().toString());
		assertEquals(null, assignationNode.getPrelude());
		assertEquals(null, assignationNode.getPostlude());

		assertEquals("(10:13)-(10:31)", expressionActionNode.getLastParsedFragment().toString());
		assertEquals(null, expressionActionNode.getPrelude());
		assertEquals(null, expressionActionNode.getPostlude());

	}

}
