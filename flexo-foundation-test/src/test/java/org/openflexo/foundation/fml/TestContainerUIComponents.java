/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;

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
		assertEquals("The fixture did not parse", 7, virtualModel.getFlexoConcepts().size());
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

		// The five artefacts at the root of the container; UI/NestedScreen.fib sits one level deeper
		assertEquals("Unexpected components linked into " + resource.getURI() + ": " + components, 5, components.size());

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
