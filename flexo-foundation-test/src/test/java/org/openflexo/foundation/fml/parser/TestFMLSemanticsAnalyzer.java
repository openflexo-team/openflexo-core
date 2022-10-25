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

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.test.parser.FMLParserTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Parse a FML file, and test semantics analyzing
 * 
 * @author sylvain
 *
 */
public class TestFMLSemanticsAnalyzer extends FMLParserTestCase {

	@BeforeClass
	public static void initServiceManager() {
		instanciateTestServiceManager();
	}

	@Test
	public void testBasicTypes() throws ParseException, ModelDefinitionException, IOException {
		final Resource fmlFile = ResourceLocator.locateResource("FMLExamples/TestBasicTypes.fml");
		FMLCompilationUnit compilationUnit = parseFile(fmlFile);
		VirtualModel virtualModel;
		FlexoConcept conceptA;
		assertNotNull(virtualModel = compilationUnit.getVirtualModel());
		assertEquals("MyModel", virtualModel.getName());
		assertEquals(2, virtualModel.getFlexoConcepts().size());
		conceptA = virtualModel.getFlexoConcepts().get(0);
		assertEquals("ConceptA", conceptA.getName());

		System.out.println("Normalized:");
		System.out.println(compilationUnit.getPrettyPrintDelegate()
				.getNormalizedRepresentation(compilationUnit.getPrettyPrintDelegate().makePrettyPrintContext()));

		System.out.println("Current FML");
		System.out.println(">>>>>>>>>>>>>>>>" + compilationUnit.getPrettyPrintDelegate()
				.getRepresentation(compilationUnit.getPrettyPrintDelegate().makePrettyPrintContext()) + "<<<<<<<<<<<<<<");
	}

	/*@Test
	public void testTestProperties() throws ParseException, ModelDefinitionException {
		final Resource fmlFile = ResourceLocator.locateResource("FMLExamples/TestProperties.fml");
		FMLCompilationUnit compilationUnit = parseFile(fmlFile);
		VirtualModel virtualModel;
		FlexoConcept conceptA, conceptB, conceptC, conceptD, conceptE;
		assertNotNull(virtualModel = compilationUnit.getVirtualModel());
		assertEquals("MyModel", virtualModel.getName());
		assertEquals(5, virtualModel.getFlexoConcepts().size());
		conceptA = virtualModel.getFlexoConcepts().get(0);
		assertEquals("ConceptA", conceptA.getName());
		conceptB = virtualModel.getFlexoConcepts().get(1);
		assertEquals("ConceptB", conceptB.getName());
		conceptC = virtualModel.getFlexoConcepts().get(2);
		assertEquals("ConceptC", conceptC.getName());
		conceptD = virtualModel.getFlexoConcepts().get(3);
		assertEquals("ConceptD", conceptD.getName());
		conceptE = virtualModel.getFlexoConcepts().get(4);
		assertEquals("ConceptE", conceptE.getName());
	}*/

	/*@Test
	public void testTestFMLDiagram() throws ParseException, ModelDefinitionException {
		final Resource fmlFile = ResourceLocator.locateResource("FMLExamples/TestFMLDiagram.fml");
		FMLCompilationUnit compilationUnit = parseFile(fmlFile);
		VirtualModel virtualModel;
		FlexoConcept myConceptGR;
		FlexoConcept linkGR;
		assertNotNull(virtualModel = compilationUnit.getVirtualModel());
		assertEquals("MyModel", virtualModel.getName());
		assertEquals(2, virtualModel.getFlexoConcepts().size());
		myConceptGR = virtualModel.getFlexoConcepts().get(0);
		assertEquals("MyConceptGR", myConceptGR.getName());
		linkGR = virtualModel.getFlexoConcepts().get(1);
		assertEquals("LinkGR", linkGR.getName());
	}*/

}
