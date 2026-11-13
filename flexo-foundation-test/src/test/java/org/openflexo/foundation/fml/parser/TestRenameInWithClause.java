package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;

/**
 * A property named in the 'with (...)' clause of an instantiation is written with its new name by the preserving pretty-print (what is
 * saved), as by the normalized one; the clause keeps its text while nothing in it changes (CORE-D-33, CORE-D-67).
 */
public class TestRenameInWithClause {

	private static FlexoServiceManager serviceManager;

	@BeforeClass
	public static void initServiceManager() {
		serviceManager = FMLTestServiceManager.create();
	}

	private static final String SOURCE = "use org.openflexo.foundation.fml.rt.FMLRTModelSlot as FMLRT;\n\nmodel MyModel {\n\n"
			+ "\tString vmName;\n\n\tcreate(String aString, int anInt) {\n\t}\n\n"
			+ "\ttestInWithClause() {\n\t\tmyModel = new MyModel(\"test\",3) with ( name = vmName );\n\t}\n\n}\n";

	private static FMLCompilationUnit parse() throws Exception {
		return new FMLCompilationUnitParser().parse(SOURCE, new FMLModelFactory(null, serviceManager), m -> null, true);
	}

	@Test
	public void anUntouchedClauseKeepsItsText() throws Exception {
		String derived = parse().getFMLPrettyPrint();
		assertTrue(derived, derived.contains("new MyModel(\"test\",3) with ( name = vmName );"));
	}

	@Test
	public void aRenameInTheClauseIsFollowed() throws Exception {
		FMLCompilationUnit unit = parse();
		unit.getVirtualModel().getAccessibleProperty("vmName").setName("renamed");
		assertTrue(unit.getNormalizedFML(), unit.getNormalizedFML().contains("with (name=renamed)"));
		String derived = unit.getFMLPrettyPrint();
		assertTrue(derived, derived.contains("with (name=renamed)"));
		unit.getVirtualModel().getAccessibleProperty("renamed").setName("vmName");
		derived = unit.getFMLPrettyPrint();
		assertTrue(derived, derived.contains("with ( name = vmName );"));
	}
}
