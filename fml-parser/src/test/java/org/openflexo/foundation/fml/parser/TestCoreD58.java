package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertTrue;
import static org.openflexo.foundation.fml.parser.TestFMLReindent.parse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FMLCompilationUnit;
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
}
