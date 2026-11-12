package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openflexo.foundation.fml.parser.TestFMLReindent.parse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;

/**
 * CORE-D-62: a parent that does not resolve (an import of a unit that is not there) was dropped from the model, and 'extends' with it.
 * The concept now keeps it, with the name written, beside the parents that resolve; the issue is still raised.
 */
public class TestCoreD62 {

	@BeforeClass
	public static void initServiceManager() {
		TestFMLReindent.initServiceManager();
	}

	private static FMLCompilationUnit unit(String modelDecl, String members) throws Exception {
		return parse(String.join("\n", //
				"import [\"http://openflexo.org/test/Nowhere.fml\"];", //
				"@URI(\"http://openflexo.org/test/CoreD62.fml\")", //
				modelDecl + " {", //
				members, //
				"}", ""));
	}

	private static String issues(FMLCompilationUnit unit) {
		return ((FMLCompilationUnitNode) unit.getPrettyPrintDelegate()).getSemanticAnalysisIssues().toString();
	}

	@Test
	public void aModelExtendingAnUnresolvedConcept() throws Exception {
		FMLCompilationUnit unit = unit("public model CoreDefectExtends extends Unknown", "");
		assertTrue(issues(unit), issues(unit).contains("Unknown not found"));
		assertTrue(unit.getVirtualModel().getParentFlexoConcepts().isEmpty());
		String normalized = unit.getNormalizedFML();
		assertTrue(normalized, normalized.contains("model CoreDefectExtends extends Unknown {"));
		assertTrue(parse(normalized).getNormalizedFML().contains("model CoreDefectExtends extends Unknown {"));
	}

	@Test
	public void theParentsThatResolveAreAttached() throws Exception {
		FMLCompilationUnit unit = unit("public model CoreDefectExtends", "\tconcept Base {\n\t}\n\tconcept Child extends Base, Unknown {\n\t}");
		FlexoConcept child = unit.getVirtualModel().getFlexoConcept("Child");
		assertEquals(1, child.getParentFlexoConcepts().size());
		assertEquals("Base", child.getParentFlexoConcepts().get(0).getName());
		assertTrue(issues(unit), issues(unit).contains("Unknown not found"));
		String normalized = unit.getNormalizedFML();
		assertTrue(normalized, normalized.contains("concept Child extends Base,Unknown {"));
	}

	@Test
	public void resolvedParentsAreUnchanged() throws Exception {
		FMLCompilationUnit unit = unit("public model CoreDefectExtends", "\tconcept Base {\n\t}\n\tconcept Child extends Base {\n\t}");
		FlexoConcept child = unit.getVirtualModel().getFlexoConcept("Child");
		assertEquals(1, child.getParentFlexoConcepts().size());
		assertTrue(unit.getNormalizedFML().contains("concept Child extends Base {"));
		assertTrue(issues(unit), !issues(unit).contains("not found"));
	}

	@Test
	public void anUnresolvedParentSurvivesTheMergeOfTheEditor() throws Exception {
		// the FML editor parses the text again and merges it into the unit it holds (updateWith)
		FMLCompilationUnit existing = unit("public model CoreDefectExtends", "\tconcept Base {\n\t}\n\tconcept Child extends Base {\n\t}");
		FMLCompilationUnit parsed = unit("public model CoreDefectExtends",
				"\tconcept Base {\n\t}\n\tconcept Child extends Base, Unknown {\n\t}");
		existing.updateWith(parsed);
		FlexoConcept child = existing.getVirtualModel().getFlexoConcept("Child");
		assertEquals(1, child.getParentFlexoConcepts().size());
		assertTrue(existing.getVirtualModel() == child.getParentFlexoConcepts().get(0).getOwningVirtualModel());
		assertTrue(existing.getNormalizedFML(), existing.getNormalizedFML().contains("concept Child extends Base,Unknown {"));
	}
}
