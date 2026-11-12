package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openflexo.foundation.fml.parser.TestFMLReindent.parse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.rt.editionaction.FinalizeMatching;

/**
 * CORE-D-57: the normalized pretty-print (the text built from the model alone) must parse again.
 * <ul>
 * <li>{@code end match ... unmatched: delete()} kept its clause only in the parsed text; the clause is now optional in the grammar, so
 * that a matching with no behaviour for the unmatched instances is printed, and parsed, without it;</li>
 * <li>a statement in a {@code for} loop whose collection could not be built lost its {@code ;};</li>
 * <li>{@code use ...ModelSlot as X} threw a NullPointerException while parsing.</li>
 * </ul>
 */
public class TestCoreD57 {

	@BeforeClass
	public static void initServiceManager() {
		TestFMLReindent.initServiceManager();
	}

	private static String unit(String body) {
		return String.join("\n", //
				"use org.openflexo.foundation.fml.rt.FMLRTModelSlot as FMLRT;", //
				"@URI(\"http://openflexo.org/test/CoreD57.fml\")", //
				"public model CoreDefectModel {", //
				"\tconcept Item {", //
				"\t\tString p;", //
				"\t}", //
				"\tconcept Mirror {", //
				"\t\tItem source;", //
				"\t\tcreate::_create(Item source) {", //
				"\t\t}", //
				"\t\tdelete() {", //
				"\t\t}", //
				"\t\tpublic void refresh() {", //
				"\t\t}", //
				"\t}", //
				"\tpublic void synchronize() {", //
				"\t\tMatchingSet matchingSet = begin match Mirror from (this);", //
				"\t\tfor (Item item : select Item from (this)) {", //
				"\t\t\tmatch Mirror in (matchingSet) from (this) where (source=item) unmatched: new Mirror::_create(item);", //
				"\t\t}", //
				body, //
				"\t}", //
				"}", "");
	}

	/** The FinalizeMatching of the unit: the last statement of 'synchronize' */
	private static FinalizeMatching endMatch(FMLCompilationUnit unit) {
		FMLControlGraph cg = unit.getVirtualModel().getFlexoBehaviour("synchronize").getControlGraph();
		for (FMLControlGraph statement : cg.getFlattenedSequence()) {
			if (statement instanceof FinalizeMatching) {
				return (FinalizeMatching) statement;
			}
		}
		return null;
	}

	@Test
	public void endMatchDeletingTheUnmatchedInstances() throws Exception {
		FMLCompilationUnit unit = parse(unit("\t\tend match Mirror in (matchingSet) unmatched: delete();"));
		assertTrue(endMatch(unit).getFlexoBehaviour() instanceof DeletionScheme);
		String normalized = unit.getNormalizedFML();
		assertTrue(normalized, normalized.contains("end match Mirror in matchingSet unmatched: delete();"));
		assertTrue(endMatch(parse(normalized)).getFlexoBehaviour() instanceof DeletionScheme);
	}

	@Test
	public void endMatchLeavingTheUnmatchedInstances() throws Exception {
		FMLCompilationUnit unit = parse(unit("\t\tend match Mirror in (matchingSet);"));
		assertNotNull(endMatch(unit));
		assertNull(endMatch(unit).getBehaviourReference());
		String normalized = unit.getNormalizedFML();
		assertTrue(normalized, normalized.contains("end match Mirror in matchingSet;"));
		assertNull(endMatch(parse(normalized)).getBehaviourReference());
		// the text printed from the source gains no clause ('in (matchingSet)' loses its parentheses: CORE-D-33)
		String printed = unit.getFMLPrettyPrint();
		assertTrue(printed, printed.contains("end match Mirror in matchingSet;") || printed.contains("end match Mirror in (matchingSet);"));
	}

	@Test
	public void endMatchRunningANamedBehaviour() throws Exception {
		// 'unmatched: <behaviour>(...)' runs that behaviour of the concept (CORE-D-2), no longer the first action scheme
		FMLCompilationUnit unit = parse(unit("\t\tend match Mirror in (matchingSet) unmatched: refresh();"));
		assertTrue(endMatch(unit).getFlexoBehaviour() != null);
		assertTrue("refresh".equals(endMatch(unit).getFlexoBehaviour().getName()));
		String normalized = unit.getNormalizedFML();
		assertTrue(normalized, normalized.contains("end match Mirror in matchingSet unmatched: refresh();"));
		assertTrue("refresh".equals(endMatch(parse(normalized)).getFlexoBehaviour().getName()));
	}

	@Test
	public void aDeletionSchemeOtherThanTheDefaultOneIsCalledByItsName() throws Exception {
		String source = unit("\t\tend match Mirror in (matchingSet) unmatched: purge();").replace("\t\tdelete() {\n\t\t}",
				"\t\tdelete() {\n\t\t}\n\t\tdelete::purge() {\n\t\t}");
		FMLCompilationUnit unit = parse(source);
		assertTrue(endMatch(unit).getFlexoBehaviour() instanceof DeletionScheme);
		assertTrue("purge".equals(endMatch(unit).getFlexoBehaviour().getName()));
		String normalized = unit.getNormalizedFML();
		assertTrue(normalized, normalized.contains("unmatched: purge();"));
	}

	@Test
	public void anUnknownBehaviourRunsNothing() throws Exception {
		FMLCompilationUnit unit = parse(unit("\t\tend match Mirror in (matchingSet) unmatched: nowhere();"));
		assertNull(endMatch(unit).getBehaviourReference());
		assertNull(endMatch(unit).getFlexoBehaviour());
		// its text is kept
		assertTrue(unit.getNormalizedFML().contains("unmatched: nowhere();"));
	}

	@Test
	public void aStatementInALoopWhoseCollectionIsNotBuiltKeepsItsSemi() throws Exception {
		// 'Unknown' does not resolve: the collection of the loop is not built, the match is the first child of the loop
		String source = unit("\t\tend match Mirror in (matchingSet) unmatched: delete();").replace("select Item from (this)",
				"select Unknown from (this)");
		String normalized = parse(source).getNormalizedFML();
		assertTrue(normalized, normalized.contains("unmatched:new Mirror::_create(item);"));
	}

	@Test
	public void useOfModelSlotItselfDoesNotThrow() throws Exception {
		String source = String.join("\n", //
				"use org.openflexo.foundation.technologyadapter.ModelSlot as MS;", //
				"import [\"http://openflexo.org/test/Nowhere.fml\"];", //
				"import SomeObject someObject from [\"http://openflexo.org/test/Nowhere\":\"Toto#13\"];", //
				"@URI(\"http://openflexo.org/test/CoreD57b.fml\")", //
				"model CoreDefectUse {", //
				"}", "");
		FMLCompilationUnit unit = parse(source);
		assertFalse(unit.getUseDeclarations().isEmpty());
	}
}
