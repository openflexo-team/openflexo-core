package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.p2pp.testing.PrettyPrintTarget.Document;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * A reference to a concept or a constructor inside an expression follows every rename of its target, also when the expression was printed
 * between two renames (CORE-D-67). Both pretty-prints are checked: the normalized one writes the bindings from the model
 * ({@code DataBinding#toString()}), the derived one keeps the parsed text of what did not change.
 */
public class TestRenameInExpressions {

	private static FlexoServiceManager serviceManager;

	@BeforeClass
	public static void initServiceManager() {
		serviceManager = FMLTestServiceManager.create();
	}

	private static String source() throws IOException {
		Resource resource = ResourceLocator.locateResource("FMLParsingExamples/TestAddFlexoConceptInstance.fml");
		try (InputStream in = resource.openInputStream()) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static void check(Document document, String present, String absent) {
		for (String text : new String[] { document.normalized(), document.derived() }) {
			assertTrue("missing " + present + " in\n" + text, text.contains(present));
			assertFalse("still " + absent + " in\n" + text, text.contains(absent));
		}
	}

	@Test
	public void aConstructorRenamedTwiceIsFollowed() throws Exception {
		Document document = new FMLTarget(serviceManager).parse(source());
		check(document, "new ConceptB::constructor1()", "ConceptB::q(");
		document.apply("rename ConceptB.constructor1 to q");
		check(document, "new ConceptB::q()", "ConceptB::constructor1(");
		document.apply("rename ConceptB.q to constructor1");
		check(document, "new ConceptB::constructor1()", "ConceptB::q(");
	}

	@Test
	public void aConceptRenamedTwiceIsFollowed() throws Exception {
		Document document = new FMLTarget(serviceManager).parse(source());
		check(document, "new ConceptA(name)", "new Qx(");
		document.apply("rename ConceptA to Qx");
		check(document, "new Qx(name)", "new ConceptA(");
		document.apply("rename Qx to ConceptA");
		check(document, "new ConceptA(name)", "new Qx(");
	}
	/** The right-hand side of the single assignment of a behaviour of the model */
	private static DataBinding<?> assignedExpression(FMLCompilationUnit unit, String behaviour) {
		AssignationAction<?> assignation = (AssignationAction<?>) unit.getVirtualModel().getFlexoBehaviour(behaviour).getControlGraph();
		return ((ExpressionAction<?>) assignation.getAssignableAction()).getExpression();
	}

	/** The text of the binding itself (what an inspector shows), not only the pretty-print of the unit */
	@Test
	public void theTextOfTheBindingFollowsTwoRenames() throws Exception {
		FMLCompilationUnit unit = new FMLCompilationUnitParser().parse(source(), new FMLModelFactory(null, serviceManager), m -> null, true);
		DefaultFlexoEditor editor = new DefaultFlexoEditor(null, serviceManager);
		DataBinding<?> binding = assignedExpression(unit, "testNewFullQualifiedInstanceNoArg1");
		assertEquals("new ConceptB::constructor1()", binding.toString());
		FMLEdits.apply(unit, editor, "rename ConceptB.constructor1 to q");
		assertEquals("new ConceptB::q()", binding.toString());
		FMLEdits.apply(unit, editor, "rename ConceptB.q to constructor1");
		assertEquals("new ConceptB::constructor1()", binding.toString());
	}
	@Test
	public void theTextOfTheBindingFollowsTwoRenamesOfAConcept() throws Exception {
		FMLCompilationUnit unit = new FMLCompilationUnitParser().parse(source(), new FMLModelFactory(null, serviceManager), m -> null, true);
		DefaultFlexoEditor editor = new DefaultFlexoEditor(null, serviceManager);
		DataBinding<?> binding = assignedExpression(unit, "testNewSimpleInstance");
		assertEquals("new ConceptA(name)", binding.toString());
		FMLEdits.apply(unit, editor, "rename ConceptA to Qx");
		assertEquals("new Qx(name)", binding.toString());
		FMLEdits.apply(unit, editor, "rename Qx to ConceptA");
		assertEquals("new ConceptA(name)", binding.toString());
	}
	/**
	 * A behaviour renamed and renamed back, without a print in between: the source is given back (the edit matrix, "undo"). The call is
	 * invalidated by the first rename and not resolved again before the second one (CORE-D-71)
	 */
	@Test
	public void aBehaviourRenamedAndBackGivesTheSourceBack() throws Exception {
		Resource resource = ResourceLocator.locateResource("FMLParsingExamples/TestInvariants.fml");
		String source;
		try (InputStream in = resource.openInputStream()) {
			source = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		Document document = new FMLTarget(serviceManager).parse(source);
		document.apply("rename AConcept.incrementA to q");
		document.apply("rename AConcept.q to incrementA");
		String derived = document.derived();
		assertTrue(derived + "\n----- normalized:\n" + document.normalized(), derived.contains("this.incrementA();"));
	}
}
