package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertTrue;
import static org.openflexo.foundation.fml.parser.TestFMLReindent.parse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FMLCompilationUnit;

/**
 * CORE-D-60: a role declared without 'with' was printed back 'myConceptBwith ConceptInstance();', which does not parse. The 'with'
 * clause is inserted right after the name, whose postlude (a space) the source wrote empty: the insertion must start with it.
 */
public class TestCoreD60 {

	@BeforeClass
	public static void initServiceManager() {
		TestFMLReindent.initServiceManager();
	}

	private static String unit(String role) {
		return String.join("\n", //
				"use org.openflexo.foundation.fml.rt.FMLRTModelSlot as FMLRT;", //
				"@URI(\"http://openflexo.org/test/CoreD60.fml\")", //
				"public model CoreDefectRoles {", //
				"\tpublic concept Target {", //
				"\t}", //
				"\tpublic concept Holder {", //
				"\t\t" + role, //
				"\t}", //
				"}", "");
	}

	private static void checkPrintedAndParsed(String role, String expected) throws Exception {
		FMLCompilationUnit unit = parse(unit(role));
		String printed = unit.getFMLPrettyPrint();
		assertTrue(printed, printed.contains(expected));
		parse(printed);
	}

	@Test
	public void aRoleWithoutWith() throws Exception {
		checkPrintedAndParsed("public Target target;", "public Target target with ConceptInstance();");
	}

	@Test
	public void aRoleWithoutWithNorVisibility() throws Exception {
		checkPrintedAndParsed("Target target;", "Target target with ConceptInstance();");
	}

	@Test
	public void aSpaceInTheSourceIsNotDoubled() throws Exception {
		checkPrintedAndParsed("public Target target ;", "public Target target with ConceptInstance();");
	}

	@Test
	public void aRoleWithWithIsUntouched() throws Exception {
		checkPrintedAndParsed("public Target target with ConceptInstance();", "public Target target with ConceptInstance();");
	}
}
