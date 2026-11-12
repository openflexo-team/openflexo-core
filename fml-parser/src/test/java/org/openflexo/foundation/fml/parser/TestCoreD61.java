package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openflexo.foundation.fml.parser.TestFMLReindent.parse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;

/**
 * CORE-D-61: the declared type {@code Object} of a local variable with an initializer was not stored (PAMELA compared it with the
 * fallback of the getter, the analyzed type, {@code Object} before the right-hand side is set), and the variable was printed with the
 * type of its value: {@code Object o = 1;} printed {@code long o = 1;}.
 */
public class TestCoreD61 {

	@BeforeClass
	public static void initServiceManager() {
		TestFMLReindent.initServiceManager();
	}

	private static FMLCompilationUnit unit(String statement) throws Exception {
		return parse(String.join("\n", //
				"@URI(\"http://openflexo.org/test/CoreD61.fml\")", //
				"public model CoreDefectDeclaredType {", //
				"\tconcept Item {", //
				"\t}", //
				"\tpublic void f() {", //
				"\t\t" + statement, //
				"\t}", //
				"}", ""));
	}

	private static DeclarationAction<?> declaration(FMLCompilationUnit unit) {
		for (FMLControlGraph statement : unit.getVirtualModel().getFlexoBehaviour("f").getControlGraph().getFlattenedSequence()) {
			if (statement instanceof DeclarationAction) {
				return (DeclarationAction<?>) statement;
			}
		}
		return null;
	}

	private static void check(String statement, Class<?> declaredType) throws Exception {
		FMLCompilationUnit unit = unit(statement);
		assertEquals(statement, declaredType, declaration(unit).getDeclaredType());
		String normalized = unit.getNormalizedFML();
		assertTrue(normalized, normalized.contains(statement));
	}

	@Test
	public void objectWithAnInitializer() throws Exception {
		check("Object o = 1;", Object.class);
	}

	@Test
	public void objectWithASelect() throws Exception {
		FMLCompilationUnit unit = unit("Object o = select Item from (this);");
		assertEquals(Object.class, declaration(unit).getDeclaredType());
		assertTrue(unit.getNormalizedFML().contains("Object o = select Item from this;"));
	}

	@Test
	public void otherTypesAreUnchanged() throws Exception {
		check("Object o;", Object.class);
		check("String o = \"a\";", String.class);
		check("Number o = 1;", Number.class);
		check("Integer o = 1;", Integer.class);
	}

	@Test
	public void theReturnTypeObjectOfABehaviour() throws Exception {
		// FlexoBehaviour.getDeclaredType() falls back on the analyzed return type the same way
		FMLCompilationUnit unit = parse(String.join("\n", //
				"@URI(\"http://openflexo.org/test/CoreD61b.fml\")", //
				"public model CoreDefectReturnType {", //
				"\tpublic Object g() {", //
				"\t\treturn 1;", //
				"\t}", //
				"}", ""));
		assertEquals(Object.class, unit.getVirtualModel().getFlexoBehaviour("g").getDeclaredType());
		assertTrue(unit.getNormalizedFML(), unit.getNormalizedFML().contains("public Object g()"));
	}
}
