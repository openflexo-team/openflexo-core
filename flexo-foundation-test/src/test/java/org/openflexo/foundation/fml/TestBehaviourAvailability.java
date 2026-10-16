package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Proxy;
import java.util.Vector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitParser;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionFactory;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Tests the <code>@Visible</code> and <code>@Enable</code> metadata of an {@link AbstractActionScheme}: stored as metadata, serialized in
 * FML, read again when the text is edited, and taken into account by the action factory.
 */
@RunWith(OrderedRunner.class)
public class TestBehaviourAvailability extends OpenflexoTestCase {

	private static final String FIXTURE_URI = "http://openflexo.org/test/TestResourceCenter/TestDerivedInspector.fml";

	private static CompilationUnitResource resource;
	private static ActionScheme scheme;

	/**
	 * A stand-in for an instance: only the expression evaluator is needed to evaluate an expression which reads nothing from it
	 */
	private static FlexoConceptInstance instance() {
		return (FlexoConceptInstance) Proxy.newProxyInstance(TestBehaviourAvailability.class.getClassLoader(),
				new Class<?>[] { FlexoConceptInstance.class }, (proxy, method, args) -> {
					if (method.getName().equals("getEvaluator")) {
						return new FMLExpressionEvaluator(null);
					}
					switch (method.getName()) {
						case "hashCode":
							return System.identityHashCode(proxy);
						case "equals":
							return proxy == args[0];
						case "toString":
							return "instance";
						default:
							return null;
					}
				});
	}

	@Test
	@TestOrder(1)
	public void test0DefaultIsVisibleAndEnabled() throws Exception {
		instanciateTestServiceManager();
		resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(FIXTURE_URI);
		assertNotNull(resource);
		FlexoConcept concept = resource.getCompilationUnit().getVirtualModel().getFlexoConcept("ConceptB");
		scheme = concept.getFMLModelFactory().newActionScheme();
		scheme.setName("myBehaviour");
		concept.addToFlexoBehaviours(scheme);

		assertTrue(scheme.evaluateVisible(instance()));
		assertTrue(scheme.evaluateCondition(instance()));
		assertFalse(resource.getCompilationUnit().getFMLPrettyPrint().contains("@Visible"));
		assertFalse(resource.getCompilationUnit().getFMLPrettyPrint().contains("@Enable"));
	}

	@Test
	@TestOrder(2)
	public void test1DeclareAndSerialize() throws Exception {
		scheme.setVisible(new DataBinding<>("false"));
		scheme.setEnable(new DataBinding<>("false"));
		assertEquals("false", scheme.getVisible().toString());
		assertFalse(scheme.evaluateVisible(instance()));
		assertFalse(scheme.evaluateCondition(instance()));

		String text = resource.getCompilationUnit().getFMLPrettyPrint();
		assertTrue(text, text.contains("@Visible(false)"));
		assertTrue(text, text.contains("@Enable(false)"));

		ActionSchemeActionFactory factory = new ActionSchemeActionFactory(scheme, instance());
		assertFalse(factory.isVisibleForSelection(instance(), new Vector<>()));
		assertFalse(factory.isEnabledForSelection(instance(), new Vector<>()));
	}

	@Test
	@TestOrder(3)
	public void test2EditInText() throws Exception {
		FMLCompilationUnit existing = resource.getCompilationUnit();
		String text = existing.getFMLPrettyPrint().replace("@Visible(false)", "@Visible(true)");
		FMLCompilationUnit parsed = new FMLCompilationUnitParser().parse(text, existing.getFMLModelFactory(), modelSlotClasses -> null,
				true);
		parsed.setResource(resource);
		existing.updateWith(parsed);
		existing.setResource(resource);
		existing.revalidateAllBindings();

		ActionScheme updated = (ActionScheme) existing.getVirtualModel().getFlexoConcept("ConceptB").getFlexoBehaviours().stream()
				.filter(b -> "myBehaviour".equals(b.getName())).findFirst().orElse(null);
		assertNotNull(updated);
		assertTrue(updated.evaluateVisible(instance()));
		assertFalse(updated.evaluateCondition(instance()));
		String after = existing.getFMLPrettyPrint();
		assertTrue(after, after.contains("@Visible(true)"));
		assertTrue(after, after.contains("@Enable(false)"));
	}

	private static ActionScheme currentScheme() {
		return (ActionScheme) resource.getCompilationUnit().getVirtualModel().getFlexoConcept("ConceptB").getFlexoBehaviours().stream()
				.filter(b -> "myBehaviour".equals(b.getName())).findFirst().orElse(null);
	}

	/** What the inspector does: edit the expression in place, then the FML text must follow, and the inspector be notified */
	@Test
	@TestOrder(4)
	public void test3EditFromInspector() throws Exception {
		ActionScheme current = currentScheme();
		java.util.List<String> events = new java.util.ArrayList<>();
		current.getPropertyChangeSupport().addPropertyChangeListener(e -> events.add(e.getPropertyName()));

		current.getEnable().setUnparsedBinding("true");
		String text = resource.getCompilationUnit().getFMLPrettyPrint();
		assertTrue("First edit not in FML:\n" + text, text.contains("@Enable(true)"));

		current.getEnable().setUnparsedBinding("false");
		text = resource.getCompilationUnit().getFMLPrettyPrint();
		assertTrue("Second edit not in FML:\n" + text, text.contains("@Enable(false)"));

		assertTrue("FML editor not notified " + events, events.contains("FMLPrettyPrint"));
	}

	/** A first declaration from the inspector (no metadata yet) */
	@Test
	@TestOrder(5)
	public void test4FirstDeclarationFromInspector() throws Exception {
		FlexoConcept concept = resource.getCompilationUnit().getVirtualModel().getFlexoConcept("ConceptB");
		ActionScheme other = concept.getFMLModelFactory().newActionScheme();
		other.setName("other");
		concept.addToFlexoBehaviours(other);
		other.getVisible().setUnparsedBinding("false");
		String text = resource.getCompilationUnit().getFMLPrettyPrint();
		assertTrue(text, text.contains("@Visible(false)"));
	}

	private static void mergeText(String text) throws Exception {
		FMLCompilationUnit existing = resource.getCompilationUnit();
		FMLCompilationUnit parsed = new FMLCompilationUnitParser().parse(text, existing.getFMLModelFactory(), modelSlotClasses -> null,
				true);
		parsed.setResource(resource);
		existing.updateWith(parsed);
		existing.setResource(resource);
		existing.revalidateAllBindings();
	}

	/** The annotation is deleted from, then typed again in, the FML text: the expression held by the inspector must follow */
	@Test
	@TestOrder(6)
	public void test5AnnotationAppearsAndDisappearsInText() throws Exception {
		ActionScheme held = currentScheme();
		DataBinding<Boolean> readBefore = held.getVisible();
		assertEquals("true", readBefore.toString());
		java.util.List<String> events = new java.util.ArrayList<>();
		held.getPropertyChangeSupport().addPropertyChangeListener(e -> events.add(e.getPropertyName()));

		mergeText(resource.getCompilationUnit().getFMLPrettyPrint().replace("@Visible(true)", ""));
		assertEquals("The merge replaced the behaviour", held, currentScheme());
		assertFalse("Deleted annotation still shown: " + held.getVisible(), held.getVisible().isSet());
		assertTrue(events.toString(), events.contains("visible"));

		events.clear();
		mergeText(resource.getCompilationUnit().getFMLPrettyPrint().replace("@Enable(", "@Visible(false) @Enable("));
		assertEquals("The merge replaced the behaviour", held, currentScheme());
		assertEquals("false", held.getVisible().toString());
		assertTrue(events.toString(), events.contains("visible"));
	}

	/** An expression which is not a literal goes through the FML text and comes back unchanged */
	@Test
	@TestOrder(7)
	public void test6ExpressionRoundTrip() throws Exception {
		ActionScheme current = currentScheme();
		current.getVisible().setUnparsedBinding("name != null");
		String text = resource.getCompilationUnit().getFMLPrettyPrint();
		assertTrue(text, text.contains("@Visible(name != null)"));
		mergeText(text);
		assertEquals("name != null", currentScheme().getVisible().toString());
	}

	/**
	 * What the binding selector of the inspector does once the typed text is valid: it builds a NEW binding and hands it over to the
	 * setter. The FML editor (which listens to the compilation unit) must be told.
	 */
	@Test
	@TestOrder(8)
	public void test7NewBindingHandedOver() throws Exception {
		ActionScheme current = currentScheme();
		java.util.List<String> events = new java.util.ArrayList<>();
		resource.getCompilationUnit().getPropertyChangeSupport().addPropertyChangeListener(e -> events.add(e.getPropertyName()));

		DataBinding<Boolean> typed = new DataBinding<>("name != \"null\"", current.getAvailabilityContext(), Boolean.class,
				DataBinding.BindingDefinitionType.GET);
		current.setVisible(typed);

		String text = resource.getCompilationUnit().getFMLPrettyPrint();
		assertTrue(text, text.contains("@Visible(name != \"null\")"));
		assertTrue("FML editor not notified " + events, events.contains("FMLPrettyPrint"));
		// The selector then tells its owner the binding changed
		typed.notifyBindingChanged(null, typed.getExpression());
		assertEquals("name != \"null\"", current.getVisible().toString());
	}

	/** An expression calling another behaviour of the concept, as in <code>@Enable(this.isFireable())</code> */
	@Test
	@TestOrder(9)
	public void test8ExpressionCallingABehaviour() throws Exception {
		CompilationUnitResource petri = serviceManager.getVirtualModelLibrary()
				.getCompilationUnitResource("http://openflexo.org/test/TestResourceCenter/TestConceptualModel.fml");
		assertNotNull(petri);
		FlexoConcept transition = petri.getCompilationUnit().getVirtualModel().getFlexoConcept("Transition");
		assertNotNull(transition);
		ActionScheme fire = transition.getFMLModelFactory().newActionScheme();
		fire.setName("fireIt");
		transition.addToFlexoBehaviours(fire);
		fire.getEnable().setUnparsedBinding("this.isFireable()");
		assertTrue("Invalid: " + fire.getEnable().invalidBindingReason(), fire.getEnable().isValid());
	}
}
