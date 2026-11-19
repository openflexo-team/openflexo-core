package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.VirtualModel;

/**
 * Known defects of the containment of FlexoConcepts, found while characterising it for CORE-F-19. Each test asserts the intended
 * behaviour and fails until its defect is fixed. Skipped unless the environment variable {@code OPENFLEXO_KNOWN_DEFECTS} is set:
 *
 * <pre>
 * OPENFLEXO_KNOWN_DEFECTS=1 ./gradlew :fml-parser:cleanTest :fml-parser:test --tests '*KnownDefectsConceptContainment*'
 * </pre>
 */
public class KnownDefectsConceptContainmentTest {

	private static final String URI = "http://openflexo.org/test/Containment.fml";

	@BeforeClass
	public static void initServiceManager() {
		Assume.assumeTrue("Known defects are run only when OPENFLEXO_KNOWN_DEFECTS is set", System.getenv("OPENFLEXO_KNOWN_DEFECTS") != null);
		TestFMLReindent.initServiceManager();
	}

	/**
	 * CORE-D-86: an event or an enum declared inside a concept is dropped. FlexoEventNode
	 * and FlexoEnumNode never attach the new object to a concept parent, so it is in no list, it cannot be found, and it disappears from
	 * the printed text.
	 */
	@Test
	public void eventAndEnumNestedInAConceptAreKept() throws Exception {
		FMLCompilationUnit unit = TestFMLReindent.parse(String.join("\n", //
				"@URI(\"" + URI + "\")", //
				"public model Containment {", //
				"	public concept Root1 {", //
				"		event Changed {", //
				"			String what;", //
				"		}", //
				"		enum Kind {", //
				"			SMALL,", //
				"			LARGE", //
				"		}", //
				"	}", //
				"}", ""));
		VirtualModel vm = unit.getVirtualModel();
		FlexoConcept root1 = vm.getFlexoConcept("Root1");
		FlexoConcept changed = vm.getFlexoConcept("Changed");
		FlexoConcept kind = vm.getFlexoConcept("Kind");
		assertNotNull("event Changed lost", changed);
		assertNotNull("enum Kind lost", kind);
		assertTrue(changed instanceof FlexoEvent);
		assertTrue(kind instanceof FlexoEnum);
		assertSame(root1, changed.getContainerFlexoConcept());
		assertSame(root1, kind.getContainerFlexoConcept());
		String normalized = unit.getNormalizedFML();
		assertTrue(normalized, normalized.contains("event Changed"));
		assertTrue(normalized, normalized.contains("enum Kind"));
	}

	/**
	 * CORE-D-91: a root concept whose container is inherited (it extends a nested concept) is printed inside the container of its parent.
	 * The VirtualModel prints getAllRootFlexoConcepts(), which leaves out every concept with an applicable container, and the container
	 * prints getAllEmbeddedFlexoConceptsDeclaringThisConceptAsContainer(), which takes the applicable container. Saving the unit then
	 * moves the concept, and changes its URI from #Gamma to #Root1#Gamma.
	 */
	@Test
	public void rootConceptExtendingANestedOneStaysAtTheRoot() throws Exception {
		FMLCompilationUnit unit = TestFMLReindent.parse(TestConceptContainment.WITH_INHERITED_CONTAINER);
		VirtualModel vm = unit.getVirtualModel();
		assertTrue(TestConceptContainment.names(vm.getAllRootFlexoConcepts()).contains("Gamma"));
		VirtualModel reparsed = TestFMLReindent.parse(unit.getNormalizedFML()).getVirtualModel();
		FlexoConcept gamma = reparsed.getFlexoConcept("Gamma");
		assertNull(unit.getNormalizedFML(), gamma.getContainerFlexoConcept());
		assertEquals(URI + "#Gamma", gamma.getURI());
	}
}
