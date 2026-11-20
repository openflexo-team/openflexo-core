package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.VirtualModel;

/**
 * CORE-D-97: when the last root concept of a VirtualModel is removed from it (deleted, or moved into another concept), the text
 * printed back keeping the parsed text loses the line break before the closing brace of the model ({@code }}}), and a moved concept
 * keeps the indentation of its former place. Asserts the intended behaviour and fails until the defect is fixed. Skipped unless the
 * environment variable {@code OPENFLEXO_KNOWN_DEFECTS} is set:
 *
 * <pre>
 * OPENFLEXO_KNOWN_DEFECTS=1 ./gradlew :fml-parser:cleanTest :fml-parser:test --tests '*KnownDefectsPrintAfterLastRootRemoval*'
 * </pre>
 */
public class KnownDefectsPrintAfterLastRootRemovalTest {

	static final String SOURCE = String.join("\n", //
			"@URI(\"http://openflexo.org/test/LastRoot.fml\")", //
			"public model LastRoot {", //
			"	public concept Root1 {", //
			"		String r;", //
			"	}", //
			"	public concept Root2 {", //
			"		public concept Inner {", //
			"		}", //
			"	}", //
			"}", "");

	@BeforeClass
	public static void initServiceManager() {
		Assume.assumeTrue("Known defects are run only when OPENFLEXO_KNOWN_DEFECTS is set", System.getenv("OPENFLEXO_KNOWN_DEFECTS") != null);
		TestFMLReindent.initServiceManager();
	}

	@Test
	public void deletingTheLastRootConceptKeepsTheLineBreak() throws Exception {
		FMLCompilationUnit unit = TestFMLReindent.parse(SOURCE);
		unit.getVirtualModel().getFlexoConcept("Root2").delete();
		String printed = unit.getFMLPrettyPrint();
		assertFalse(printed, printed.contains("}}"));
		assertTrue(printed, printed.contains("\t}\n}"));
	}

	@Test
	public void movingTheLastRootConceptIndentsItAtItsNewPlace() throws Exception {
		FMLCompilationUnit unit = TestFMLReindent.parse(SOURCE);
		VirtualModel vm = unit.getVirtualModel();
		vm.getFlexoConcept("Root2").setContainer(vm.getFlexoConcept("Root1"));
		String printed = unit.getFMLPrettyPrint();
		assertFalse(printed, printed.contains("}}"));
		assertTrue(printed, printed.contains("\n\t\tpublic concept Root2 {\n\t\t\tpublic concept Inner {"));
	}
}
