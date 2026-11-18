package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openflexo.foundation.fml.parser.TestFMLReindent.parse;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * CORE-D-88: the criteria of a <code>match ... where (...)</code> are <code>property = expression</code>; anything else is reported
 * with a message that says so.
 */
public class TestCoreD88 {

	@BeforeClass
	public static void initServiceManager() {
		TestFMLReindent.initServiceManager();
	}

	private static String model(String criterion) {
		return String.join("\n", //
				"@URI(\"http://openflexo.org/test/CoreD88.fml\")", //
				"public model CoreD88 {", //
				"\tconcept Wrapper {", //
				"\t}", //
				"\tconcept Holder {", //
				"\t\tWrapper w;", //
				"\t}", //
				"\tpublic synchronize() {", //
				"\t\tMatchingSet<Holder> ms = begin match Holder from this;", //
				"\t\tfor (Wrapper wr : select Wrapper from this) {", //
				"\t\t\tmatch Holder in ms from this where (" + criterion + ") unmatched: new Holder();", //
				"\t\t}", //
				"\t\tend match Holder in ms;", //
				"\t}", //
				"}", "");
	}

	@Test
	public void aPathOnTheLeftOfACriterionSaysWhatIsExpected() throws Exception {
		try {
			parse(model("selected.w = wr"));
			fail("A path is not a valid left-hand side");
		} catch (ParseException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("write 'name = expression'"));
		}
	}

	@Test
	public void aPropertyCriterionParses() throws Exception {
		assertTrue(parse(model("w = wr")).getNormalizedFML().contains("where (w=wr)"));
	}
}
