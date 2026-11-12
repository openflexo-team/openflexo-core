package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.openflexo.foundation.fml.parser.TestFMLReindent.parse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.rt.editionaction.AbstractSelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.FinalizeMatching;
import org.openflexo.foundation.fml.rt.editionaction.InitiateMatching;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;

/**
 * CORE-D-58: what refers to a concept that does not resolve (an import of a unit that is not there) was dropped from the model. It is
 * now kept, typed by an unresolved concept type keeping the name written: the text is printed back as written, and the semantic issue is
 * still raised.
 */
public class TestCoreD58 {

	@BeforeClass
	public static void initServiceManager() {
		TestFMLReindent.initServiceManager();
	}

	private static FMLCompilationUnit parseBody(String body) throws Exception {
		return parse(String.join("\n", //
				"use org.openflexo.foundation.fml.rt.FMLRTModelSlot as FMLRT;", //
				"import [\"http://openflexo.org/test/Nowhere.fml\"];", //
				"@URI(\"http://openflexo.org/test/CoreD58.fml\")", //
				"public model CoreDefectUnresolved {", //
				"\tpublic void f() {", //
				body, //
				"\t}", //
				"}", ""));
	}

	private static void check(String body, String expectedInNormalizedText, String expectedIssue) throws Exception {
		FMLCompilationUnit unit = parseBody(body);
		String normalized = unit.getNormalizedFML();
		assertTrue(normalized, normalized.contains(expectedInNormalizedText));
		String issues = ((FMLCompilationUnitNode) unit.getPrettyPrintDelegate()).getSemanticAnalysisIssues().toString();
		assertTrue(issues, issues.contains(expectedIssue));
		// the normalized text parses again, and gives the same text
		assertTrue(parse(normalized).getNormalizedFML().contains(expectedInNormalizedText));
	}

	@Test
	public void aSelectOfAnUnresolvedConceptIsKept() throws Exception {
		// the whole body of the behaviour was lost
		check("\t\tselect Unknown from (this);", "select Unknown from this;", "Unresolved type Unknown");
	}

	@Test
	public void aMatchingOnAnUnresolvedConceptIsKept() throws Exception {
		check(String.join("\n", //
				"\t\tMatchingSet ms = begin match Unknown from (this);", //
				"\t\tmatch Unknown in (ms) from (this) where (key=1) unmatched: new Unknown::ctor(1);", //
				"\t\tend match Unknown in (ms) unmatched: delete();"), //
				"MatchingSet ms = begin match Unknown from this;", "Unexpected matched type Unknown");
		// the property of a criterion is not resolved either: its name is kept ('where (?=1)' before)
		assertTrue(parseBody("\t\tMatchingSet ms = begin match Unknown from (this);\n\t\tmatch Unknown in (ms) from (this) where (key=1) "
				+ "unmatched: new Unknown::ctor(1);").getNormalizedFML().contains("where (key=1)"));
		String normalized = parseBody(String.join("\n", //
				"\t\tMatchingSet ms = begin match Unknown from (this);", //
				"\t\tend match Unknown in (ms);")).getNormalizedFML();
		assertTrue(normalized, normalized.contains("end match Unknown in ms;"));
	}

	@Test
	public void aNewOfAnUnresolvedConceptIsKept() throws Exception {
		// the expression was printed empty ('o = ;'), with no issue at all
		check("\t\tUnknown o = new Unknown::ctor(1,\"a\");", "= new Unknown::ctor(1,\"a\");", "Unresolved concept Unknown");
	}

	private static <T> T statement(FMLCompilationUnit unit, Class<T> type) {
		for (FMLControlGraph s : unit.getVirtualModel().getFlexoBehaviour("f").getControlGraph().getFlattenedSequence()) {
			if (type.isInstance(s)) {
				return type.cast(s);
			}
			if (s instanceof DeclarationAction && type.isInstance(((DeclarationAction<?>) s).getAssignableAction())) {
				return type.cast(((DeclarationAction<?>) s).getAssignableAction());
			}
		}
		return null;
	}

	/** The merge of the FML editor (updateWith): an unresolved type of the parsed unit is translated into the unit merged into */
	private static void checkMerged(FlexoConceptInstanceType merged, FlexoConceptInstanceType ofParsedUnit, String name) {
		assertEquals(name, merged.getConceptURI());
		assertNotSame(ofParsedUnit, merged);
	}

	@Test
	public void theEditorMergeTranslatesUnresolvedTypes() throws Exception {
		String before = String.join("\n", //
				"\t\tselect Unknown from (this);", //
				"\t\tMatchingSet ms = begin match Unknown from (this);", //
				"\t\tend match Unknown in (ms);");
		FMLCompilationUnit existing = parseBody(before);
		FMLCompilationUnit parsed = parseBody(before.replace("Unknown", "Other"));
		existing.updateWith(parsed);
		checkMerged(statement(existing, AbstractSelectFlexoConceptInstance.class).getType(),
				statement(parsed, AbstractSelectFlexoConceptInstance.class).getType(), "Other");
		checkMerged(statement(existing, InitiateMatching.class).getMatchedType(), statement(parsed, InitiateMatching.class).getMatchedType(),
				"Other");
		checkMerged(statement(existing, FinalizeMatching.class).getMatchedType(), statement(parsed, FinalizeMatching.class).getMatchedType(),
				"Other");
		String normalized = existing.getNormalizedFML();
		assertTrue(normalized, normalized.contains("select Other from this;"));
		assertTrue(normalized, normalized.contains("begin match Other from this;"));
		assertTrue(normalized, normalized.contains("end match Other in ms;"));
	}

	@Test
	public void theEditorMergeKeepsANewOfAnUnresolvedConcept() throws Exception {
		FMLCompilationUnit existing = parseBody("\t\tObject o = new Unknown::ctor(1);");
		existing.updateWith(parseBody("\t\tObject o = new Other::ctor(2);"));
		String normalized = existing.getNormalizedFML();
		assertTrue(normalized, normalized.contains("Object o = new Other::ctor(2);"));
	}
}
