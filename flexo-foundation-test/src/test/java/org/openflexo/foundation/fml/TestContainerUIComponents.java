/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.rm.FIBComponentResource;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.rm.Resource;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test the container-based lookup of the user interfaces of a {@link VirtualModel} and of its {@link FlexoConcept}s.
 *
 * <p>
 * The components live in the <code>Xxx.fml/</code> container itself, beside the FML core file, and are resolved by naming convention, by an
 * <code>@UI</code> / <code>@Inspector</code> annotation, or inherited from a parent concept. See
 * {@link FlexoConcept#getUIComponentResource()} and the <code>TestContainerUI.fml</code> fixture, whose header lists what its container
 * ships.
 *
 * <p>
 * This layer returns a {@link Resource} and never loads the component: <code>flexo-foundation</code> has no dependency on GINA.
 */
@RunWith(OrderedRunner.class)
public class TestContainerUIComponents extends OpenflexoTestCase {

	private static final String FIXTURE_URI = "http://openflexo.org/test/TestResourceCenter/TestContainerUI.fml";

	private static VirtualModel virtualModel;

	@Test
	@TestOrder(1)
	public void test0LoadFixture() {

		instanciateTestServiceManager();
		assertNotNull(serviceManager);

		CompilationUnitResource resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(FIXTURE_URI);
		assertNotNull("No compilation unit for " + FIXTURE_URI, resource);

		virtualModel = resource.getCompilationUnit().getVirtualModel();
		assertNotNull(virtualModel);

		// A failed parse leaves an EMPTY compilation unit behind, which then validates with zero errors:
		// assert the concepts were actually parsed before asserting anything about them.
		assertEquals("The fixture did not parse", 21, virtualModel.getFlexoConcepts().size());
	}

	/** A VirtualModel is a FlexoConcept, so the convention gives it its own view as Xxx.fml/Xxx.fib. */
	@Test
	@TestOrder(2)
	public void test1VirtualModelResolvesItsOwnComponents() {

		assertResolvesTo("TestContainerUI.fib", virtualModel.getUIComponentResource());
		assertResolvesTo("TestContainerUI.inspector", virtualModel.getInspectorComponentResource());
	}

	/**
	 * A component at the root of the container is a registered resource, LINKED INTO the contents of its compilation unit resource - which
	 * is what makes it show up under its VirtualModel in the browsers, and what gets it deleted with it.
	 */
	@Test
	@TestOrder(12)
	public void test11ComponentsAreContentsOfTheCompilationUnitResource() {

		CompilationUnitResource resource = (CompilationUnitResource) virtualModel.getDeclaringCompilationUnit().getResource();
		List<FIBComponentResource> components = resource.getContents(FIBComponentResource.class);

		// Computed rather than hard-coded: EVERY component at the root of the container must be linked, and the fixture
		// grows whenever the create-inspector action is exercised in the running application, which writes into
		// src/main/resources. UI/NestedScreen.fib sits one level deeper and is deliberately not a content.
		TreeSet<String> atContainerRoot = new TreeSet<>();
		for (Resource artefact : virtualModel.getDeclaringCompilationUnit().getContainerDirectoryResource().getContents(false)) {
			String path = artefact.getRelativePath();
			if (path != null && (path.endsWith(".fib") || path.endsWith(".inspector"))) {
				atContainerRoot.add(path.substring(path.lastIndexOf('/') + 1));
			}
		}

		TreeSet<String> linked = new TreeSet<>();
		for (FIBComponentResource component : components) {
			String path = component.getIODelegate().getSerializationArtefactAsResource().getRelativePath();
			linked.add(path.substring(path.lastIndexOf('/') + 1));
		}

		assertEquals("Every component of the container must be linked into " + resource.getURI(), atContainerRoot, linked);
		assertTrue("The fixture lost its components", linked.size() >= 8);

		assertNotNull(virtualModel.getUIComponentFlexoResource());
		assertNotNull(virtualModel.getInspectorComponentFlexoResource());
		assertEquals(virtualModel.getUIComponentResource(),
				virtualModel.getUIComponentFlexoResource().getIODelegate().getSerializationArtefactAsResource());
	}

	/** &lt;ConceptName&gt;.fib and &lt;ConceptName&gt;.inspector, at the root of the container. */
	@Test
	@TestOrder(3)
	public void test2ConventionResolvesFromConceptName() {

		FlexoConcept simple = concept("Simple");
		assertResolvesTo("Simple.fib", simple.getUIComponentResource());
		assertResolvesTo("Simple.inspector", simple.getInspectorComponentResource());
	}

	/** A concept declaring nothing inherits the components of its parent. */
	@Test
	@TestOrder(4)
	public void test3ComponentIsInheritedFromParentConcept() {

		FlexoConcept inheriting = concept("InheritingFromSimple");
		assertResolvesTo("Simple.fib", inheriting.getUIComponentResource());
		assertResolvesTo("Simple.inspector", inheriting.getInspectorComponentResource());
	}

	/** An @UI annotation wins over both the convention and what the concept would inherit. */
	@Test
	@TestOrder(5)
	public void test4AnnotationOverridesConventionAndInheritance() {

		FlexoConcept annotated = concept("Annotated");
		assertTrue("The fixture lost its @UI annotation", annotated.hasMetaData(FlexoConcept.UI_METADATA));

		assertResolvesTo("CustomScreen.fib", annotated.getUIComponentResource());

		// Annotated declares no inspector of its own, so it still inherits Simple's
		assertResolvesTo("Simple.inspector", annotated.getInspectorComponentResource());
	}

	/** An annotation may name a nested artefact, which the flat convention deliberately does not reach. */
	@Test
	@TestOrder(6)
	public void test5AnnotationMayNameANestedArtefact() {

		assertResolvesTo("NestedScreen.fib", concept("Nested").getUIComponentResource());

		// Registered as a resource all the same, though it is not a content of the compilation unit: the contents of a
		// compilation unit hold what its own directory carries, and this one sits one level deeper.
		assertNotNull(concept("Nested").getUIComponentFlexoResource());
	}

	/** A declaration naming a component that does not exist resolves to null rather than to something else. */
	@Test
	@TestOrder(7)
	public void test6BrokenDeclarationResolvesToNull() {

		assertNull(concept("BrokenDeclaration").getUIComponentResource());
	}

	/** Neither convention, nor annotation, nor parent. */
	@Test
	@TestOrder(8)
	public void test7ConceptWithoutAnyComponentResolvesToNull() {

		FlexoConcept without = concept("WithoutAnyComponent");
		assertNull(without.getUIComponentResource());
		assertNull(without.getInspectorComponentResource());
	}

	/** The container is free space: an arbitrary artefact of it is reachable, and an absent one yields null. */
	@Test
	@TestOrder(9)
	public void test8ContainedArtefactIsReachableByName() {

		FMLCompilationUnit compilationUnit = virtualModel.getDeclaringCompilationUnit();

		assertNotNull(compilationUnit.getContainerDirectoryResource());
		assertResolvesTo("TestContainerUI.fml", compilationUnit.getContainedArtefact("TestContainerUI.fml"));
		assertResolvesTo("NestedScreen.fib", compilationUnit.getContainedArtefact("UI/NestedScreen.fib"));
		assertNull(compilationUnit.getContainedArtefact("NoSuchArtefact.fib"));
		assertNull(compilationUnit.getContainedArtefact(null));
		assertNull(compilationUnit.getContainedArtefact(""));
	}

	/** A renderer is STORED as @Renderer metadata; the deprecated {@link FlexoConceptInspector} only reads it back. */
	@Test
	@TestOrder(10)
	public void test9RendererComesFromAnnotation() {

		FlexoConcept presented = concept("Presented");
		assertTrue("The fixture lost its @Renderer annotation", presented.hasMetaData(FlexoConcept.RENDERER_METADATA));

		DataBinding<String> renderer = presented.getApplicableRenderer();
		assertNotNull("No applicable renderer for " + presented, renderer);
		assertEquals("name", renderer.toString());

		// A concept declaring none gets none, rather than an empty binding carried by a lazily created inspector
		assertNull(concept("Simple").getApplicableRenderer());
	}

	/**
	 * {@link FlexoConcept#freezeConventionalUIComponentNames()} - called before a rename, to preserve a component still resolved by the
	 * naming convention alone (CORE-F-4, see the rename actions in flexo-foundation) - only ever has something to do for a concept with
	 * no <code>@UI</code>/<code>@Inspector</code> of its own, and only for the components it resolves ON ITSELF.
	 */
	@Test
	@TestOrder(11)
	public void test10FreezeWritesAnAnnotationOnlyWhereTheConventionResolves() {

		FlexoConcept simple = concept("Simple");
		assertFalse(simple.hasMetaData(FlexoConcept.UI_METADATA));
		assertFalse(simple.hasMetaData(FlexoConcept.INSPECTOR_METADATA));

		simple.freezeConventionalUIComponentNames();

		assertEquals("Simple.fib", simple.getSingleMetaData(FlexoConcept.UI_METADATA, String.class));
		assertEquals("Simple.inspector", simple.getSingleMetaData(FlexoConcept.INSPECTOR_METADATA, String.class));
		// Resolution is unchanged: the new annotation names exactly what the convention already resolved
		assertResolvesTo("Simple.fib", simple.getUIComponentResource());
		assertResolvesTo("Simple.inspector", simple.getInspectorComponentResource());

		// A concept that already declares an annotation of its own is left untouched
		FlexoConcept annotated = concept("Annotated");
		annotated.freezeConventionalUIComponentNames();
		assertEquals("CustomScreen.fib", annotated.getSingleMetaData(FlexoConcept.UI_METADATA, String.class));

		// Nothing to freeze: no component resolves on this concept at all
		FlexoConcept without = concept("WithoutAnyComponent");
		without.freezeConventionalUIComponentNames();
		assertFalse(without.hasMetaData(FlexoConcept.UI_METADATA));
		assertFalse(without.hasMetaData(FlexoConcept.INSPECTOR_METADATA));

		// A component resolved only by INHERITANCE must not be frozen onto the child: InheritingFromSimple has no
		// Xxx.fib/.inspector of its OWN in the container
		FlexoConcept inheriting = concept("InheritingFromSimple");
		inheriting.freezeConventionalUIComponentNames();
		assertFalse(inheriting.hasMetaData(FlexoConcept.UI_METADATA));
		assertFalse(inheriting.hasMetaData(FlexoConcept.INSPECTOR_METADATA));
		assertResolvesTo("Simple.fib", inheriting.getUIComponentResource());

		// A concept resolving only ONE of the two (an inspector but no .fib) freezes only that one
		FlexoConcept plain = concept("Plain");
		assertFalse(plain.hasMetaData(FlexoConcept.INSPECTOR_METADATA));
		plain.freezeConventionalUIComponentNames();
		assertEquals("Plain.inspector", plain.getSingleMetaData(FlexoConcept.INSPECTOR_METADATA, String.class));
		assertFalse("No Plain.fib in the container: nothing to freeze", plain.hasMetaData(FlexoConcept.UI_METADATA));
	}

	/**
	 * A multi-valued <code>@UI</code> declares named variants; its <code>default</code> key is what an unqualified lookup returns.
	 */
	@Test
	@TestOrder(13)
	public void test12NamedVariantsAreResolved() {

		FlexoConcept withVariants = concept("WithVariants");

		assertResolvesTo("Screen.fib", withVariants.getUIComponentResource());
		assertResolvesTo("Screen.fib", withVariants.getUIComponentResource(FlexoConcept.DEFAULT_VARIANT));
		assertResolvesTo("Compact.fib", withVariants.getUIComponentResource("compact"));

		assertNull(withVariants.getUIComponentResource("noSuchVariant"));

		assertEquals(Arrays.asList(FlexoConcept.DEFAULT_VARIANT, "compact"), withVariants.getUIComponentVariants());
	}

	/** Variants are inherited one by one: a concept may override just one and take the rest from its parent. */
	@Test
	@TestOrder(14)
	public void test13VariantsAreInheritedOneByOne() {

		FlexoConcept inheriting = concept("InheritingVariants");

		assertResolvesTo("OtherCompact.fib", inheriting.getUIComponentResource("compact"));
		// not redeclared here, so taken from WithVariants
		assertResolvesTo("Screen.fib", inheriting.getUIComponentResource());

		assertTrue(inheriting.getUIComponentVariants().containsAll(Arrays.asList(FlexoConcept.DEFAULT_VARIANT, "compact")));
	}

	/** Declaring only named variants still leaves a default: the first one declared. */
	@Test
	@TestOrder(15)
	public void test14FirstVariantStandsInForAMissingDefault() {

		FlexoConcept withoutDefault = concept("WithoutDefaultVariant");

		assertResolvesTo("Screen.fib", withoutDefault.getUIComponentResource());
		assertResolvesTo("Screen.fib", withoutDefault.getUIComponentResource("large"));
	}

	/** The single-valued form keeps its meaning, and declares the default variant alone. */
	@Test
	@TestOrder(16)
	public void test15SingleValuedFormDeclaresOnlyTheDefault() {

		FlexoConcept annotated = concept("Annotated");

		assertResolvesTo("CustomScreen.fib", annotated.getUIComponentResource());
		assertEquals(Arrays.asList(FlexoConcept.DEFAULT_VARIANT), annotated.getUIComponentVariants());
		assertNull(annotated.getUIComponentResource("compact"));
	}

	/** Every variant is a component of the container, and each knows the concept driving it. */
	@Test
	@TestOrder(17)
	public void test16EveryVariantKnowsItsDrivingConcept() {

		FlexoConcept withVariants = concept("WithVariants");

		assertEquals(withVariants, withVariants.getUIComponentFlexoResource("compact").getDrivingConcept());
	}

	/** The inspector of an instance is ADDITIVE: the components of the whole hierarchy, the most general first. */
	@Test
	@TestOrder(18)
	public void test17InspectorComponentsOfTheHierarchyAncestorsFirst() {

		assertEquals(Arrays.asList("InspParent.inspector"), inspectorNames(concept("InspParent")));
		assertEquals(Arrays.asList("InspParent.inspector", "InspChild.inspector"), inspectorNames(concept("InspChild")));
		assertEquals(Arrays.asList("InspParent.inspector", "InspChild.inspector", "InspGrandChild.inspector"),
				inspectorNames(concept("InspGrandChild")));

		// getInspectorComponentResource() is still "the" component of the concept: the grand child's own, hiding the others
		assertResolvesTo("InspGrandChild.inspector", concept("InspGrandChild").getInspectorComponentResource());
		assertResolvesTo("InspGrandChild.inspector", concept("InspGrandChild").getOwnInspectorComponentResource());
	}

	/** A concept contributing nothing is skipped, but its ancestors' contributions remain. */
	@Test
	@TestOrder(19)
	public void test18ConceptWithoutInspectorContributesNothingButKeepsAncestors() {

		FlexoConcept noInspector = concept("InspNoInspector");

		assertNull(noInspector.getOwnInspectorComponentResource());
		assertEquals(Arrays.asList("InspParent.inspector", "InspChild.inspector"), inspectorNames(noInspector));
		// The single-component accessor takes the most specialized parent's
		assertResolvesTo("InspChild.inspector", noInspector.getInspectorComponentResource());
		assertEquals(Arrays.asList(concept("InspParent"), concept("InspChild")), noInspector.getInspectorContributingConcepts());
	}

	/** Several parents are all considered, and a common ancestor is contributed once, before the concepts specializing it. */
	@Test
	@TestOrder(20)
	public void test19DiamondContributesTheCommonAncestorOnce() {

		assertEquals(Arrays.asList("InspParent.inspector", "InspLeft.inspector", "InspRight.inspector", "InspDiamond.inspector"),
				inspectorNames(concept("InspDiamond")));
	}

	/** One FIBComponentResource per resource, resolved through the concept declaring it. */
	@Test
	@TestOrder(21)
	public void test20FlexoResourcesFollowTheResources() {

		FlexoConcept grandChild = concept("InspGrandChild");
		List<FIBComponentResource> resources = grandChild.getInspectorComponentFlexoResources();
		assertEquals(grandChild.getInspectorComponentResources().size(), resources.size());
		assertEquals(grandChild.getInspectorComponentResource(), resources.get(2).getIODelegate().getSerializationArtefactAsResource());
	}

	/** Nothing in the hierarchy: nothing to compose. */
	@Test
	@TestOrder(22)
	public void test21NoInspectorAnywhere() {
		assertTrue(concept("WithoutAnyComponent").getInspectorComponentResources().isEmpty());
	}

	private static List<String> inspectorNames(FlexoConcept concept) {
		List<String> returned = new ArrayList<>();
		for (Resource resource : concept.getInspectorComponentResources()) {
			String path = resource.getRelativePath();
			returned.add(path.substring(path.lastIndexOf('/') + 1));
		}
		return returned;
	}

	private static FlexoConcept concept(String name) {
		FlexoConcept returned = virtualModel.getFlexoConcept(name);
		assertNotNull("No concept " + name + " in the fixture", returned);
		return returned;
	}

	/**
	 * Assert supplied resource is the artefact simply named <code>expectedName</code>. Only the last element of the relative path may be
	 * compared: its base differs between a file-based and a jar-based resource center.
	 */
	private static void assertResolvesTo(String expectedName, Resource resolved) {
		assertNotNull("Resolved to nothing, expected " + expectedName, resolved);
		String path = resolved.getRelativePath();
		assertTrue("Expected an artefact named " + expectedName + ", got " + path, path.endsWith("/" + expectedName));
	}
}
