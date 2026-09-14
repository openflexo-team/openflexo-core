/**
 *
 * Copyright (c) 2019, Openflexo
 *
 * This file is part of FML-parser, a component of the software infrastructure
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
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.SemanticAnalysisWarning;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.test.parser.FMLParserTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Checks that property cardinalities survive a pretty-print round trip (parse, pretty-print, re-parse), both through the normalized
 * representation and through the syntax-preserving one
 *
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestFMLPrettyPrint11 extends FMLParserTestCase {

	private static FMLCompilationUnit compilationUnit;

	@Test
	@TestOrder(1)
	public void loadInitialVersion() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager();

		final Resource fmlFile = ResourceLocator.locateResource("TestFMLPrettyPrint11/InitialModel.fml");
		compilationUnit = parseFile(fmlFile);
		checkCardinalities(compilationUnit);

		// [2,8] is accepted, with a warning and no error
		assertTrue(compilationUnit.getPrettyPrintDelegate().getSemanticAnalysisIssues().isEmpty());
		List<SemanticAnalysisWarning> warnings = compilationUnit.getPrettyPrintDelegate().getSemanticAnalysisWarnings();
		assertEquals(1, warnings.size());
		assertEquals("Cardinality [2,8] is not supported, read as [0,*]", warnings.get(0).getMessage());
		assertEquals(19, warnings.get(0).getLine());
	}

	@Test
	@TestOrder(2)
	public void reparseNormalizedFML() throws ParseException, ModelDefinitionException, IOException {
		String normalizedFML = compilationUnit.getNormalizedFML();
		System.out.println("Normalized=\n" + normalizedFML);
		assertTrue(normalizedFML.contains("String[1,1] mandatory;"));
		assertTrue(normalizedFML.contains("String[0,*] ellipsis;"));
		assertTrue(normalizedFML.contains("String[0,*] bounded;"));
		checkCardinalities(reparse(normalizedFML));
	}

	@Test
	@TestOrder(3)
	public void reparseFMLPrettyPrint() throws ParseException, ModelDefinitionException, IOException {
		String prettyPrint = compilationUnit.getFMLPrettyPrint();
		System.out.println("FML=\n" + prettyPrint);
		// Syntax-preserving pretty-print keeps the parsed cardinality text
		assertTrue(prettyPrint.contains("String[1,1] mandatory;"));
		assertTrue(prettyPrint.contains("String[0,1] explicitlyOptional;"));
		assertTrue(prettyPrint.contains("String... ellipsis;"));
		assertTrue(prettyPrint.contains("String[2,8] bounded;"));
		checkCardinalities(reparse(prettyPrint));
	}

	private static FMLCompilationUnit reparse(String fml) throws ParseException, ModelDefinitionException, IOException {
		FMLCompilationUnitParser parser = new FMLCompilationUnitParser();
		return parser.parse(fml, new FMLModelFactory(null, serviceManager), (modelSlotClasses) -> {
			return null;
		}, true);
	}

	private static void checkCardinalities(FMLCompilationUnit unit) {
		VirtualModel virtualModel = unit.getVirtualModel();
		assertNotNull(virtualModel);
		FlexoConcept holder = virtualModel.getFlexoConcept("Holder");
		assertNotNull(holder);

		// Primitive roles
		assertCardinality(PropertyCardinality.ZeroOne, holder, "optional");
		assertCardinality(PropertyCardinality.ZeroOne, holder, "explicitlyOptional");
		assertCardinality(PropertyCardinality.One, holder, "mandatory");
		assertCardinality(PropertyCardinality.OneMany, holder, "atLeastOne");
		assertCardinality(PropertyCardinality.ZeroMany, holder, "many");
		assertCardinality(PropertyCardinality.ZeroMany, holder, "ellipsis");
		assertCardinality(PropertyCardinality.ZeroMany, holder, "bounded");

		// FlexoConceptInstance roles
		assertCardinality(PropertyCardinality.ZeroOne, holder, "optionalTarget");
		assertCardinality(PropertyCardinality.One, holder, "mandatoryTarget");
		assertCardinality(PropertyCardinality.ZeroMany, holder, "targets");
	}

	private static void assertCardinality(PropertyCardinality expected, FlexoConcept concept, String propertyName) {
		FlexoProperty<?> property = concept.getDeclaredProperty(propertyName);
		assertNotNull("Property not found: " + propertyName, property);
		assertTrue("Not a role: " + propertyName, property instanceof FlexoRole);
		assertEquals("Cardinality of " + propertyName, expected, ((FlexoRole<?>) property).getCardinality());
	}
}
