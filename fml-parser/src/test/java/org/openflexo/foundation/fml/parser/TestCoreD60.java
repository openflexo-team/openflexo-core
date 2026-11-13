package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertTrue;
import static org.openflexo.foundation.fml.parser.TestFMLReindent.parse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;

/**
 * CORE-D-60: a role declared without 'with' was printed back 'myConceptBwith ConceptInstance();', which does not parse. The 'with'
 * clause is inserted right after the name, whose postlude (a space) the source wrote empty: the insertion must start with it. Since
 * CORE-D-33 the clause is only inserted once the role is no longer the default one (here: a property is given a value).
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

	/**
	 * The 'with' clause is inserted once the role is given a property value: the insertion follows the name with one space, whatever the
	 * source wrote after it
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void checkInsertedAndParsed(String role, String expected) throws Exception {
		FMLCompilationUnit unit = parse(unit(role));
		FlexoConceptInstanceRole target = (FlexoConceptInstanceRole) unit.getVirtualModel().getFlexoConcept("Holder")
				.getAccessibleProperty("target");
		target.setVirtualModelInstance(new DataBinding("this"));
		String printed = unit.getFMLPrettyPrint();
		assertTrue(printed, printed.contains(expected));
		parse(printed);
	}

	/** A role declared without 'with' is printed as written while its role is the default one (CORE-D-33) */
	@Test
	public void aRoleWithoutWithIsUntouched() throws Exception {
		checkPrintedAndParsed("public Target target;", "public Target target;");
		checkPrintedAndParsed("Target target;", "Target target;");
		checkPrintedAndParsed("public Target target ;", "public Target target ;");
	}

	@Test
	public void aRoleWithoutWith() throws Exception {
		checkInsertedAndParsed("public Target target;", "public Target target with ConceptInstance(");
	}

	@Test
	public void aRoleWithoutWithNorVisibility() throws Exception {
		checkInsertedAndParsed("Target target;", "Target target with ConceptInstance(");
	}

	@Test
	public void aSpaceInTheSourceIsNotDoubled() throws Exception {
		checkInsertedAndParsed("public Target target ;", "public Target target with ConceptInstance(");
	}

	@Test
	public void aRoleWithWithIsUntouched() throws Exception {
		checkPrintedAndParsed("public Target target with ConceptInstance();", "public Target target with ConceptInstance();");
	}
}
