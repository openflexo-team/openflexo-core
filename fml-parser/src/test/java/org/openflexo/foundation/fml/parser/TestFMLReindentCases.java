package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.openflexo.foundation.fml.parser.TestFMLReindent.parse;
import static org.openflexo.foundation.fml.parser.TestFMLReindent.reindent;
import static org.openflexo.foundation.fml.parser.TestFMLReindent.show;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Targeted cases of the re-indentation of FML (P2PP-F-1): rows that the corpus does not hold outside comments
 */
public class TestFMLReindentCases {

	@BeforeClass
	public static void initServiceManager() {
		TestFMLReindent.initServiceManager();
	}

	@Test
	public void elseOnItsOwnRowAWrappedStatementAndAnAnnotation() throws Exception {
		String source = String.join("\n", //
				"@URI(\"http://openflexo.org/test/Reindent.fml\")", //
				"    public model TestReindent {", //
				"int n;", //
				"public void a(Boolean c) {", //
				"     if (parameters.c) {", //
				"n = 1;", //
				"  }", //
				"  else {", //
				"n = 2;", //
				"}", //
				"n", //
				"= 3;", //
				"}", //
				"}", "");
		String expected = String.join("\n", //
				"@URI(\"http://openflexo.org/test/Reindent.fml\")", //
				"public model TestReindent {", //
				"\tint n;", //
				"\tpublic void a(Boolean c) {", //
				"\t\tif (parameters.c) {", //
				"\t\t\tn = 1;", //
				"\t\t}", //
				"\t\telse {", //
				"\t\t\tn = 2;", //
				"\t\t}", //
				"\t\tn", //
				"\t\t\t= 3;", //
				"\t}", //
				"}", "");
		assertEquals(show(expected), show(reindent(parse(source))));
	}
}
