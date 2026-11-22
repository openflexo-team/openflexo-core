package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.VirtualModel;

/**
 * Characterisation of the containment of FlexoConcepts in a VirtualModel, written before CORE-F-19 (a single container/contents
 * association). It pins what must stay true through that refactoring: containers, URIs, lookup, the pretty-print
 * round trip, adding, deleting and cloning, and the notifications the user interface listens to.
 * It also covers what CORE-F-19 fixed on the way: a root concept extending a nested one stays at the root and is reported by the
 * validation (CORE-D-91, CORE-D-95), so is a concept extending a root one outside the root of its VirtualModel or of a
 * VirtualModel extending it (CORE-D-96), and an event or an enum declared inside a concept is kept (CORE-D-86).
 * <p>
 * Some tests replay, on purpose, the call sequences of callers not migrated yet (the deprecated writes {@code addToFlexoConcepts},
 * {@code setContainerFlexoConcept}, {@code addToEmbeddedFlexoConcepts}...): they are rewritten when those aliases are removed, in the
 * last phase of CORE-F-19. {@link #allConcepts(VirtualModel)} is the one place that reads the list of all concepts.
 */
public class TestConceptContainment {

	private static final String URI = "http://openflexo.org/test/Containment.fml";

	/** Nested concepts only: printed back as it is parsed */
	static final String NESTED = String.join("\n", //
			"@URI(\"" + URI + "\")", //
			"public model Containment {", //
			"	public concept Root1 {", //
			"		String r;", //
			"		public concept Alpha {", //
			"			String a;", //
			"			public concept Leaf {", //
			"				String l;", //
			"			}", //
			"		}", //
			"		public concept Beta extends Alpha {", //
			"		}", //
			"	}", //
			"	public concept Root2 {", //
			"		public concept Inner {", //
			"		}", //
			"	}", //
			"	event Top {", //
			"	}", //
			"	enum Color {", //
			"		RED,", //
			"		GREEN", //
			"	}", //
			"}", "");

	/** Same, plus Gamma: a root concept extending a nested one (an invalid model) */
	static final String WITH_INHERITED_CONTAINER = NESTED.replace("	public concept Root2 {",
			"	public concept Gamma extends Alpha {\n	}\n	public concept Root2 {");

	@BeforeClass
	public static void initServiceManager() {
		TestFMLReindent.initServiceManager();
	}

	/** Every concept of the VirtualModel, nested ones included (the flat list of before CORE-F-19) */
	static List<FlexoConcept> allConcepts(VirtualModel vm) {
		return vm.getAllFlexoConcepts();
	}

	static Set<String> names(List<? extends FlexoConcept> concepts) {
		Set<String> returned = new HashSet<>();
		for (FlexoConcept concept : concepts) {
			returned.add(concept.getName());
		}
		return returned;
	}

	static Set<String> set(String... names) {
		return new HashSet<>(List.of(names));
	}

	static FlexoConcept concept(VirtualModel vm, String name) {
		FlexoConcept returned = vm.getFlexoConcept(name);
		assertNotNull(name, returned);
		return returned;
	}

	/** The container and the nesting of each concept of {@link #NESTED} */
	private static void assertNestedStructure(VirtualModel vm) {
		assertEquals(set("Root1", "Alpha", "Leaf", "Beta", "Root2", "Inner", "Top", "Color"), names(allConcepts(vm)));
		FlexoConcept root1 = concept(vm, "Root1");
		FlexoConcept alpha = concept(vm, "Alpha");
		FlexoConcept leaf = concept(vm, "Leaf");
		FlexoConcept beta = concept(vm, "Beta");
		FlexoConcept root2 = concept(vm, "Root2");
		FlexoConcept inner = concept(vm, "Inner");
		assertTrue(concept(vm, "Top") instanceof FlexoEvent);
		assertTrue(concept(vm, "Color") instanceof FlexoEnum);

		for (FlexoConcept concept : allConcepts(vm)) {
			assertSame(concept.getName(), vm, concept.getOwningVirtualModel());
			assertSame(concept.getName(), vm, concept.getOwner());
		}
		for (String root : List.of("Root1", "Root2", "Top", "Color")) {
			FlexoConcept concept = concept(vm, root);
			assertNull(root, concept.getContainerFlexoConcept());
			assertSame(root, vm, concept.getContainer());
			assertTrue(root, concept.isRoot());
		}
		assertSame(root1, alpha.getContainerFlexoConcept());
		assertSame(alpha, leaf.getContainerFlexoConcept());
		assertSame(root1, beta.getContainerFlexoConcept());
		assertSame(root2, inner.getContainerFlexoConcept());
		for (FlexoConcept nested : List.of(alpha, leaf, beta, inner)) {
			assertFalse(nested.getName(), nested.isRoot());
			assertSame(nested.getName(), nested.getContainerFlexoConcept(), nested.getContainer());
		}

		// direct contents only
		assertEquals(List.of(alpha, beta), root1.getEmbeddedFlexoConcepts());
		assertEquals(List.of(leaf), alpha.getEmbeddedFlexoConcepts());
		assertEquals(List.of(), leaf.getEmbeddedFlexoConcepts());
		assertEquals(List.of(inner), root2.getEmbeddedFlexoConcepts());
		// inherited contents: Beta extends Alpha, so Leaf is accessible from Beta
		assertEquals(List.of(leaf), beta.getAccessibleEmbeddedFlexoConcepts());

		assertEquals(set("Root1", "Root2", "Top", "Color"), names(vm.getAllRootFlexoConcepts()));
	}

	@Test
	public void containersAndContents() throws Exception {
		assertNestedStructure(TestFMLReindent.parse(NESTED).getVirtualModel());
	}

	@Test
	public void urisFollowTheContainers() throws Exception {
		VirtualModel vm = TestFMLReindent.parse(NESTED).getVirtualModel();
		assertEquals(URI + "#Root1", concept(vm, "Root1").getURI());
		assertEquals(URI + "#Root1#Alpha", concept(vm, "Alpha").getURI());
		assertEquals(URI + "#Root1#Alpha#Leaf", concept(vm, "Leaf").getURI());
		assertEquals(URI + "#Root1#Beta", concept(vm, "Beta").getURI());
		assertEquals(URI + "#Root2#Inner", concept(vm, "Inner").getURI());
		assertEquals(URI + "#Top", concept(vm, "Top").getURI());
		assertEquals("Root1", concept(vm, "Root1").getLocalURI());
		assertEquals("Root1#Alpha#Leaf", concept(vm, "Leaf").getLocalURI());
	}

	@Test
	public void lookupByNameAndByUriFindsNestedConcepts() throws Exception {
		VirtualModel vm = TestFMLReindent.parse(NESTED).getVirtualModel();
		for (FlexoConcept concept : allConcepts(vm)) {
			assertSame(concept.getName(), concept, vm.getFlexoConcept(concept.getName()));
			assertSame(concept.getURI(), concept, vm.getFlexoConcept(concept.getURI()));
		}
		assertSame(concept(vm, "Leaf"), concept(vm, "Alpha").getEmbeddedFlexoConcept("Leaf"));
	}

	/**
	 * Step 4b of CORE-F-19: the container is never inherited. Gamma, declared at the root and extending a concept nested in Root1, is an
	 * invalid model: it is a root concept as declared, and the validation reports it (CORE-D-95).
	 */
	@Test
	public void rootConceptExtendingANestedOneIsARootConcept() throws Exception {
		VirtualModel vm = TestFMLReindent.parse(WITH_INHERITED_CONTAINER).getVirtualModel();
		FlexoConcept gamma = concept(vm, "Gamma");
		assertSame(vm, gamma.getContainer());
		assertNull(gamma.getContainerFlexoConcept());
		assertTrue(gamma.isRoot());
		assertEquals(URI + "#Gamma", gamma.getURI());
		assertEquals(List.of(), gamma.getEmbeddedFlexoConcepts());
		assertTrue(names(vm.getAllRootFlexoConcepts()).contains("Gamma"));
		assertTrue(isReported(vm, "Gamma"));
		// the concepts nested in its parent stay accessible through inheritance
		assertEquals(List.of(concept(vm, "Leaf")), gamma.getAccessibleEmbeddedFlexoConcepts());
	}

	@Test
	public void prettyPrintRoundTripKeepsTheNesting() throws Exception {
		FMLCompilationUnit unit = TestFMLReindent.parse(NESTED);
		String normalized = unit.getNormalizedFML();
		FMLCompilationUnit reparsed = TestFMLReindent.parse(normalized);
		assertNestedStructure(reparsed.getVirtualModel());
		assertEquals(normalized, reparsed.getNormalizedFML());
		// nested concepts are printed inside their container, once
		int root1 = normalized.indexOf("concept Root1");
		int alpha = normalized.indexOf("concept Alpha");
		int leaf = normalized.indexOf("concept Leaf");
		int root2 = normalized.indexOf("concept Root2");
		assertTrue(normalized, root1 < alpha && alpha < leaf && leaf < root2);
		assertEquals(normalized, alpha, normalized.lastIndexOf("concept Alpha"));
		assertEquals(normalized, leaf, normalized.lastIndexOf("concept Leaf"));
	}

	/** What CreateFlexoConcept does: add to the VirtualModel, then set the container */
	@Test
	public void addingANestedConcept() throws Exception {
		FMLCompilationUnit unit = TestFMLReindent.parse(NESTED);
		VirtualModel vm = unit.getVirtualModel();
		FlexoConcept alpha = concept(vm, "Alpha");
		List<String> fired = new ArrayList<>();
		PropertyChangeListener vmListener = e -> fired.add("vm:" + e.getPropertyName());
		PropertyChangeListener alphaListener = e -> fired.add("alpha:" + e.getPropertyName());
		vm.getPropertyChangeSupport().addPropertyChangeListener(vmListener);
		alpha.getPropertyChangeSupport().addPropertyChangeListener(alphaListener);

		FMLModelFactory factory = unit.getFMLModelFactory();
		FlexoConcept added = factory.newFlexoConcept();
		added.setName("Added");
		vm.addToFlexoConcepts(added);
		added.setContainerFlexoConcept(alpha);

		vm.getPropertyChangeSupport().removePropertyChangeListener(vmListener);
		alpha.getPropertyChangeSupport().removePropertyChangeListener(alphaListener);

		assertSame(vm, added.getOwningVirtualModel());
		assertSame(alpha, added.getContainerFlexoConcept());
		assertEquals(List.of(concept(vm, "Leaf"), added), alpha.getEmbeddedFlexoConcepts());
		assertTrue(names(allConcepts(vm)).contains("Added"));
		assertFalse(names(vm.getAllRootFlexoConcepts()).contains("Added"));
		assertEquals(URI + "#Root1#Alpha#Added", added.getURI());
		// what the browsers of the FML editor listen to
		assertTrue(fired.toString(), fired.contains("vm:allRootFlexoConcepts"));
		assertTrue(fired.toString(), fired.contains("vm:" + VirtualModel.ALL_FLEXO_CONCEPTS_KEY));
		assertTrue(fired.toString(), fired.contains("alpha:embeddedFlexoConcepts"));

		String normalized = unit.getNormalizedFML();
		VirtualModel reparsed = TestFMLReindent.parse(normalized).getVirtualModel();
		assertSame(concept(reparsed, "Alpha"), concept(reparsed, "Added").getContainerFlexoConcept());
	}

	/**
	 * Moving a concept inside its VirtualModel never announces another owner, and keeps the binding model of the VirtualModel behind
	 * the moved concept and the concepts nested in it (found by the review of CORE-F-19 phase 2: a transient null owner was notified)
	 */
	@Test
	public void movingAConceptInsideItsVirtualModel() throws Exception {
		VirtualModel vm = TestFMLReindent.parse(NESTED).getVirtualModel();
		FlexoConcept root1 = concept(vm, "Root1");
		FlexoConcept root2 = concept(vm, "Root2");
		FlexoConcept inner = concept(vm, "Inner");
		assertSame(vm.getBindingModel(), root2.getBindingModel().getBaseBindingModel());
		List<Object> ownerEvents = new ArrayList<>();
		PropertyChangeListener listener = e -> {
			if (e.getPropertyName().equals(FlexoConcept.OWNER_KEY)) {
				ownerEvents.add(e.getNewValue());
			}
		};
		root2.getPropertyChangeSupport().addPropertyChangeListener(listener);
		inner.getPropertyChangeSupport().addPropertyChangeListener(listener);

		// root -> nested (the inspector, CreateFlexoConcept with a container)
		root2.setContainerFlexoConcept(root1);
		assertSame(root1, root2.getContainer());
		assertEquals(List.of(concept(vm, "Alpha"), concept(vm, "Beta"), root2), root1.getEmbeddedFlexoConcepts());
		assertFalse(vm.getContents().contains(root2));
		assertEquals(URI + "#Root1#Root2#Inner", inner.getURI());
		assertSame(vm, root2.getOwner());
		assertSame(vm, inner.getOwner());
		assertSame(vm.getBindingModel(), root2.getBindingModel().getBaseBindingModel());
		assertSame(vm.getBindingModel(), inner.getBindingModel().getBaseBindingModel());

		// nested -> nested (drag and drop: FMLFIBController.moveFlexoConcept)
		FlexoConcept alpha = concept(vm, "Alpha");
		vm.removeFromFlexoConcepts(root2);
		vm.addToFlexoConcepts(root2);
		alpha.addToEmbeddedFlexoConcepts(root2);
		assertSame(alpha, root2.getContainer());
		assertEquals(List.of(alpha, concept(vm, "Beta")), root1.getEmbeddedFlexoConcepts());
		assertSame(vm.getBindingModel(), root2.getBindingModel().getBaseBindingModel());

		// nested -> root
		root2.setContainerFlexoConcept(null);
		assertSame(vm, root2.getContainer());
		assertTrue(vm.getContents().contains(root2));
		assertEquals(URI + "#Root2#Inner", inner.getURI());
		assertSame(vm.getBindingModel(), root2.getBindingModel().getBaseBindingModel());
		assertSame(vm.getBindingModel(), inner.getBindingModel().getBaseBindingModel());

		root2.getPropertyChangeSupport().removePropertyChangeListener(listener);
		inner.getPropertyChangeSupport().removePropertyChangeListener(listener);
		assertFalse("owner events: " + ownerEvents, ownerEvents.contains(null));
		assertEquals(set("Root1", "Alpha", "Leaf", "Beta", "Root2", "Inner", "Top", "Color"), names(allConcepts(vm)));
	}

	@Test
	public void deletingANestedConcept() throws Exception {
		VirtualModel vm = TestFMLReindent.parse(NESTED).getVirtualModel();
		FlexoConcept alpha = concept(vm, "Alpha");
		FlexoConcept leaf = concept(vm, "Leaf");
		List<String> fired = new ArrayList<>();
		PropertyChangeListener alphaListener = e -> fired.add("alpha:" + e.getPropertyName());
		alpha.getPropertyChangeSupport().addPropertyChangeListener(alphaListener);
		leaf.delete();
		alpha.getPropertyChangeSupport().removePropertyChangeListener(alphaListener);

		assertTrue(leaf.isDeleted());
		assertEquals(List.of(), alpha.getEmbeddedFlexoConcepts());
		assertFalse(names(allConcepts(vm)).contains("Leaf"));
		assertNull(vm.getFlexoConcept("Leaf"));
		assertTrue(fired.toString(), fired.contains("alpha:embeddedFlexoConcepts"));
	}

	@Test
	public void deletingAContainerDeletesItsContents() throws Exception {
		VirtualModel vm = TestFMLReindent.parse(NESTED).getVirtualModel();
		FlexoConcept root2 = concept(vm, "Root2");
		FlexoConcept inner = concept(vm, "Inner");
		root2.delete();
		assertTrue(root2.isDeleted());
		assertTrue(inner.isDeleted());
		assertEquals(set("Root1", "Alpha", "Leaf", "Beta", "Top", "Color"), names(allConcepts(vm)));
		assertEquals(set("Root1", "Top", "Color"), names(vm.getAllRootFlexoConcepts()));
	}

	@Test
	public void cloningAConceptClonesItsContents() throws Exception {
		VirtualModel vm = TestFMLReindent.parse(NESTED).getVirtualModel();
		FlexoConcept alpha = concept(vm, "Alpha");
		FlexoConcept leaf = concept(vm, "Leaf");
		FlexoConcept clone = (FlexoConcept) alpha.cloneObject();

		assertEquals("Alpha", clone.getName());
		// the clone is detached, until pasted somewhere
		assertNull(clone.getOwningVirtualModel());
		assertNull(clone.getContainerFlexoConcept());
		assertEquals(1, clone.getEmbeddedFlexoConcepts().size());
		FlexoConcept clonedLeaf = clone.getEmbeddedFlexoConcepts().get(0);
		assertEquals("Leaf", clonedLeaf.getName());
		assertTrue(clonedLeaf != leaf);
		assertSame(clone, clonedLeaf.getContainerFlexoConcept());
		assertNull(clonedLeaf.getOwningVirtualModel());
		// the original is untouched
		assertNestedStructure(vm);
	}

	/**
	 * CORE-D-91: the text is printed where each concept is declared, not where the container of its parent is. Gamma, declared at the
	 * root and extending a nested concept, is an invalid model (see the rule tests below), but saving it must not move it, nor change
	 * its URI.
	 */
	@Test
	public void rootConceptExtendingANestedOneStaysAtTheRoot() throws Exception {
		FMLCompilationUnit unit = TestFMLReindent.parse(WITH_INHERITED_CONTAINER);
		VirtualModel vm = unit.getVirtualModel();
		assertTrue(names(vm.getContents()).contains("Gamma"));
		String normalized = unit.getNormalizedFML();
		VirtualModel reparsed = TestFMLReindent.parse(normalized).getVirtualModel();
		FlexoConcept gamma = reparsed.getFlexoConcept("Gamma");
		assertNull(normalized, gamma.getContainerFlexoConcept());
		assertEquals(URI + "#Gamma", gamma.getURI());
		assertEquals(normalized, reparsed.getNormalizedFML());
	}

	/** Containment: B nested in A has its instances in an instance of A, so a concept extending B lives in A (or a subtype of A) too */
	private static final String ROOTS = String.join("\n", //
			"@URI(\"" + URI + "\")", //
			"public model Containment {", //
			"	public concept Root1 {", //
			"		public concept Alpha {", //
			"		}", //
			"%s", //
			"	}", //
			"	public concept Root1Sub extends Root1 {", //
			"%s", //
			"	}", //
			"	public concept Root2 {", //
			"		public concept Beta {", //
			"		}", //
			"	}", //
			"%s", //
			"}", "");

	private static VirtualModel withGamma(String inRoot1, String inRoot1Sub, String atRoot) throws Exception {
		return TestFMLReindent.parse(String.format(ROOTS, inRoot1, inRoot1Sub, atRoot)).getVirtualModel();
	}

	private static boolean isReported(VirtualModel vm, String conceptName) {
		return new FlexoConcept.ContainerFlexoConceptMustBeConsistentWithParents().applyValidation(concept(vm, conceptName)) != null;
	}

	@Test
	public void conceptExtendingANestedOneDeclaredAtTheRootIsReported() throws Exception {
		VirtualModel vm = withGamma("", "", "	public concept Gamma extends Alpha {\n	}");
		assertTrue(isReported(vm, "Gamma"));
		assertFalse(isReported(vm, "Alpha"));
		assertFalse(isReported(vm, "Root1Sub"));
	}

	@Test
	public void conceptExtendingANestedOneIsValidInTheContainerOfItsParent() throws Exception {
		VirtualModel vm = withGamma("		public concept Gamma extends Alpha {\n		}", "", "");
		assertSame(concept(vm, "Root1"), concept(vm, "Gamma").getContainerFlexoConcept());
		assertFalse(isReported(vm, "Gamma"));
	}

	@Test
	public void conceptExtendingANestedOneIsValidInASubtypeOfTheContainerOfItsParent() throws Exception {
		VirtualModel vm = withGamma("", "		public concept Gamma extends Alpha {\n		}", "");
		assertSame(concept(vm, "Root1Sub"), concept(vm, "Gamma").getContainerFlexoConcept());
		assertFalse(isReported(vm, "Gamma"));
	}

	@Test
	public void conceptExtendingANestedOneIsReportedInAnUnrelatedContainer() throws Exception {
		VirtualModel vm = withGamma("", "", "	public concept Other {\n		public concept Gamma extends Alpha {\n		}\n	}");
		assertTrue(isReported(vm, "Gamma"));
	}

	@Test
	public void everyParentContainerIsRequired() throws Exception {
		// Delta extends Alpha (in Root1) and Beta (in Root2): no container is a subtype of both
		VirtualModel root = withGamma("", "", "	public concept Delta extends Alpha, Beta {\n	}");
		assertTrue(isReported(root, "Delta"));
		VirtualModel inRoot1 = withGamma("		public concept Delta extends Alpha, Beta {\n		}", "", "");
		assertTrue(isReported(inRoot1, "Delta"));
	}

	/**
	 * CORE-D-96: every ancestor is checked, not only the direct parents. Gamma is reported (root, extends the nested Alpha); Delta extends
	 * Gamma, declared at the root like Delta, but still inherits from Alpha, whose instances live in a Root1: Delta is reported too
	 */
	@Test
	public void everyAncestorIsChecked() throws Exception {
		VirtualModel vm = withGamma("", "", "	public concept Gamma extends Alpha {\n	}\n	public concept Delta extends Gamma {\n	}");
		assertTrue(isReported(vm, "Gamma"));
		assertTrue(isReported(vm, "Delta"));
	}

	/** CORE-D-96: the instances of a root concept live in the VirtualModel instance, so a concept extending it is declared at the root */
	@Test
	public void conceptNestedAndExtendingARootOneIsReported() throws Exception {
		VirtualModel vm = withGamma("		public concept Gamma extends Root2 {\n		}", "", "");
		assertTrue(isReported(vm, "Gamma"));
		// Root1Sub, at the root, extends the root concept Root1
		assertFalse(isReported(vm, "Root1Sub"));
	}

	/** CORE-D-96: a specialisation nested in the concept it extends (a recursive containment) is reported */
	@Test
	public void conceptNestedInTheRootConceptItExtendsIsReported() throws Exception {
		VirtualModel vm = withGamma("", "", "	public concept Other {\n		public concept SubOther extends Other {\n		}\n	}");
		assertTrue(isReported(vm, "SubOther"));
	}

	/** The example of §5.1.5 of the FML specification (openflexo-core/spec): keep both in line */
	@Test
	public void specificationExampleOfContainment() throws Exception {
		VirtualModel vm = TestFMLReindent.parse(String.join("\n", //
				"@URI(\"http://openflexo.org/test/Shop.fml\")", //
				"public model Shop {", //
				"	public abstract concept Item {", //
				"	}", //
				"	public concept Tool extends Item {", //
				"	}", //
				"	public concept Box {", //
				"		public abstract concept Part {", //
				"		}", //
				"		public concept Screw extends Part {", //
				"		}", //
				"		public concept Nail extends Item {", //
				"		}", //
				"		public concept SubBox extends Box {", //
				"		}", //
				"	}", //
				"}", "")).getVirtualModel();
		assertFalse(isReported(vm, "Tool"));
		assertFalse(isReported(vm, "Screw"));
		assertTrue(isReported(vm, "Nail"));
		assertTrue(isReported(vm, "SubBox"));
	}

	private static final String M1 = "@URI(\"http://openflexo.org/test/ModelOne.fml\")\nmodel ModelOne {\n	concept First {\n	}\n}\n";
	private static final String M2 = "@URI(\"http://openflexo.org/test/ModelTwo.fml\")\nmodel ModelTwo {\n	concept Second {\n	}\n}\n";

	/**
	 * CORE-D-96: a root concept of M2 extending a root concept of M1 is valid only when M2 extends M1 (the parents are set by hand: the
	 * two units are parsed apart)
	 */
	@Test
	public void rootConceptExtendingARootConceptOfAnotherVirtualModel() throws Exception {
		VirtualModel m1 = TestFMLReindent.parse(M1).getVirtualModel();
		VirtualModel m2 = TestFMLReindent.parse(M2).getVirtualModel();
		concept(m2, "Second").addToParentFlexoConcepts(concept(m1, "First"));
		assertTrue(isReported(m2, "Second"));
		m2.addToParentFlexoConcepts(m1);
		assertFalse(isReported(m2, "Second"));
	}

	/**
	 * An event or an enum declared inside a concept is in its contents, can be found, and is printed back (CORE-D-86: FlexoEventNode and
	 * FlexoEnumNode used to attach it to nothing)
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
	 * Containment is declared by nesting only: the {@code inside X} clause is removed from FML, and {@code inside} is an ordinary
	 * identifier again (CORE-F-19)
	 */
	@Test
	public void insideIsAnOrdinaryIdentifier() throws Exception {
		FMLCompilationUnit unit = TestFMLReindent.parse(String.join("\n", //
				"@URI(\"" + URI + "\")", //
				"public model Containment {", //
				"	public concept Root1 {", //
				"		String inside;", //
				"	}", //
				"}", ""));
		FlexoConcept root1 = concept(unit.getVirtualModel(), "Root1");
		assertNotNull(root1.getDeclaredProperty("inside"));
		String normalized = unit.getNormalizedFML();
		assertTrue(normalized, normalized.contains("String inside;"));
		assertNotNull(concept(TestFMLReindent.parse(normalized).getVirtualModel(), "Root1").getDeclaredProperty("inside"));
	}

	@Test
	public void theInsideClauseIsNoLongerAccepted() throws Exception {
		try {
			TestFMLReindent.parse(String.join("\n", //
					"@URI(\"" + URI + "\")", //
					"public model Containment {", //
					"	public concept Root1 {", //
					"	}", //
					"	public concept Alpha inside Root1 {", //
					"	}", //
					"}", ""));
			org.junit.Assert.fail("the inside clause was parsed");
		} catch (ParseException e) {
			// expected: a syntax error
		}
	}
}
