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

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.SimpleInvariant;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.editionaction.LogAction;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoConceptNode;
import org.openflexo.foundation.fml.parser.fmlnodes.SimpleAssertNode;
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
public class TestFMLPrettyPrint10 extends FMLParserTestCase {

	private static FMLCompilationUnit compilationUnit;
	private static VirtualModel virtualModel;

	static FlexoEditor editor;

	@Test
	@TestOrder(1)
	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

	}

	private static SimpleInvariant invariant1;
	private static SimpleInvariant invariant2;
	private static SimpleInvariant invariant3;
	private static SimpleInvariant invariant4;

	@Test
	@TestOrder(2)
	public void loadInitialVersion() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		log("Initial version");

		final Resource fmlFile = ResourceLocator.locateResource("TestFMLPrettyPrint10/InitialModel.fml");
		compilationUnit = parseFile(fmlFile);
		assertNotNull(virtualModel = compilationUnit.getVirtualModel());
		assertEquals("TestSimpleInvariants", virtualModel.getName());

		assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
		assertNotNull(rootNode.getObjectNode(virtualModel));

		RawSource rawSource = rootNode.getRawSource();
		System.out.println(rawSource.debug());
		debug(rootNode, 0);

		FlexoConcept concept = virtualModel.getFlexoConcept("AConcept");
		FlexoConceptNode conceptNode = (FlexoConceptNode) rootNode.getObjectNode(concept);

		invariant1 = (SimpleInvariant) concept.getInvariants().get(0);
		assertNotNull(invariant1);
		invariant2 = (SimpleInvariant) concept.getInvariants().get(1);
		assertNotNull(invariant2);
		invariant3 = (SimpleInvariant) concept.getInvariants().get(2);
		assertNotNull(invariant3);
		invariant4 = (SimpleInvariant) concept.getInvariants().get(2);
		assertNotNull(invariant4);

		SimpleAssertNode invariantNode = (SimpleAssertNode) rootNode.getObjectNode(invariant1);
		assertNotNull(invariantNode);

		System.out.println("InvariantNode: " + invariantNode.debug());

		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint10/Step1Normalized.fml");

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint10/Step1PrettyPrint.fml");

	}

	@Test
	@TestOrder(3)
	public void addFailureClauseOnInvariant1() throws ParseException, ModelDefinitionException, IOException {
		log("addFailureClauseOnInvariant1()");
		LogAction logAction = invariant1.getFMLModelFactory().newLogAction();
		logAction.setLogString(new DataBinding<String>("\"a is false!\""));
		invariant1.setViolationControlGraph(logAction);

		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint10/Step2Normalized.fml");

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint10/Step2PrettyPrint.fml");

	}

	@Test
	@TestOrder(4)
	public void completeFailureClauseOnInvariant1() throws ParseException, ModelDefinitionException, IOException {
		log("completeFailureClauseOnInvariant1()");
		LogAction logAction = invariant1.getFMLModelFactory().newLogAction();
		logAction.setLogString(new DataBinding<String>("\"we must do something\""));
		invariant1.getViolationControlGraph().sequentiallyAppend(logAction);

		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint10/Step3Normalized.fml");

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint10/Step3PrettyPrint.fml");

	}

	@Test
	@TestOrder(5)
	public void removeFailureClauseOnInvariant2() throws ParseException, ModelDefinitionException, IOException {
		log("removeFailureClauseOnInvariant2()");
		invariant2.setViolationControlGraph(null);

		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint10/Step4Normalized.fml");

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint10/Step4PrettyPrint.fml");

	}

	@Test
	@TestOrder(6)
	public void removeFailureClauseOnInvariant3() throws ParseException, ModelDefinitionException, IOException {
		log("removeFailureClauseOnInvariant3()");
		invariant3.setViolationControlGraph(null);

		System.out.println("Normalized=\n" + compilationUnit.getNormalizedFML());
		testNormalizedFMLRepresentationEquals(compilationUnit, "TestFMLPrettyPrint10/Step5Normalized.fml");

		System.out.println("FML=\n" + compilationUnit.getFMLPrettyPrint());
		testFMLPrettyPrintEquals(compilationUnit, "TestFMLPrettyPrint10/Step5PrettyPrint.fml");

	}
}
