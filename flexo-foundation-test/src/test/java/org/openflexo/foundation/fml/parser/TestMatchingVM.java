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
import org.openflexo.foundation.fml.controlgraph.EmptyControlGraph;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoBehaviourNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.BeginMatchActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.DeclarationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.EmptyControlGraphNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.FetchRequestNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.IterationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.DataBindingNode;
import org.openflexo.foundation.fml.rt.editionaction.InitiateMatching;
import org.openflexo.foundation.test.parser.FMLParserTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test some special constructs (declaration with edition action, and enhanced loops with edition action)
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestMatchingVM extends FMLParserTestCase {

	static FlexoEditor editor;

	static FMLCompilationUnit compilationUnit;
	// static FMLCompilationUnitNode rootNode;

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

		final Resource fmlFile = ResourceLocator.locateResource("FMLExamples/TestMatchingVM.fml");

		// System.out.println(FileUtils.fileContents(((FileResourceImpl) fmlFile).getFile()));

		compilationUnit = testFMLCompilationUnit(fmlFile);
		assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
	}

	@SuppressWarnings("rawtypes")
	@Test
	@TestOrder(3)
	public void testSynchronizeMethod() throws ParseException, ModelDefinitionException, IOException {
		log("synchronizeUsingMatchingSet()");

		ActionScheme actionScheme = (ActionScheme) compilationUnit.getVirtualModel().getFlexoBehaviour("synchronizeUsingMatchingSet");
		assertNotNull(actionScheme);

		System.out.println("PP:" + actionScheme.getFMLPrettyPrint());
		System.out.println("Norm:" + actionScheme.getNormalizedFML());

		FlexoBehaviourNode<?, ?> behaviourNode = checkNodeForObject("(7:1)-(11:2)", "(6:0)-(7:0)", "(11:2)-(12:0)", null, actionScheme);
		assertSame(behaviourNode, rootNode.getObjectNode(actionScheme));
		debug(behaviourNode, 0);

		assertTrue(actionScheme.getControlGraph() instanceof Sequence);

		DeclarationAction declarationAction = (DeclarationAction) ((Sequence) actionScheme.getControlGraph()).getControlGraph1();
		DeclarationActionNode declarationActionNode = checkNodeForObject("(8:2)-(8:65)", null, declarationAction);

		InitiateMatching initiateMatching = (InitiateMatching) declarationAction.getAssignableAction();
		BeginMatchActionNode beginMatchActionNode = checkNodeForObject("(8:28)-(8:64)", null, initiateMatching);
		DataBindingNode db1Node = checkNode("(8:60)-(8:64)", "this", (DataBindingNode) beginMatchActionNode.getChildren().get(0));

		IterationAction iterationAction = (IterationAction) ((Sequence) actionScheme.getControlGraph()).getControlGraph2();
		IterationActionNode iterationActionNode = checkNodeForObject("(9:2)-(10:3)", null, iterationAction);

		FetchRequest fetchRequest = (FetchRequest) iterationAction.getIterationAction();
		FetchRequestNode fetchRequestNode = checkNodeForObject("(9:26)-(9:57)", null, fetchRequest);
		DataBindingNode db2Node = checkNode("(9:53)-(9:57)", "this", (DataBindingNode) fetchRequestNode.getChildren().get(0));

		EmptyControlGraph emptyCG = (EmptyControlGraph) iterationAction.getControlGraph();
		EmptyControlGraphNode emptyCGNode = checkNodeForObject("(9:59)-(10:2)", null, emptyCG);

	}

}
