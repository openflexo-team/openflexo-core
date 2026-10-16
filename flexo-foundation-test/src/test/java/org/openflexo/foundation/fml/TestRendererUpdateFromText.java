package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitParser;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Editing a <code>@Renderer</code> in the FML text, as the FML editor does (parse the text, then <code>updateWith</code> on the
 * existing compilation unit), must reach the model, hence the next serialization - even when the renderer was already read once.
 */
@RunWith(OrderedRunner.class)
public class TestRendererUpdateFromText extends OpenflexoTestCase {

	private static final String FIXTURE_URI = "http://openflexo.org/test/TestResourceCenter/TestDerivedInspector.fml";

	private static CompilationUnitResource resource;
	private static FlexoConcept concept;

	@Test
	@TestOrder(1)
	public void test0DeclareRenderer() {
		instanciateTestServiceManager();
		resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(FIXTURE_URI);
		assertNotNull(resource);
		concept = resource.getCompilationUnit().getVirtualModel().getFlexoConcept("ConceptB");
		concept.setRenderer(new DataBinding<>("instance.name"));
		// Reading it once is what makes the stored value the DataBinding itself
		assertEquals("instance.name", concept.getRenderer().toString());
		assertTrue(resource.getCompilationUnit().getFMLPrettyPrint().contains("@Renderer(instance.name)"));
	}

	@Test
	@TestOrder(2)
	public void test1EditRendererInText() throws Exception {
		FMLCompilationUnit existing = resource.getCompilationUnit();
		String text = existing.getFMLPrettyPrint().replace("@Renderer(instance.name)", "@Renderer(\"X-\" + instance.name)");
		assertTrue(text.contains("@Renderer(\"X-\" + instance.name)"));

		FMLCompilationUnit parsed = new FMLCompilationUnitParser().parse(text, existing.getFMLModelFactory(), modelSlotClasses -> null,
				true);
		parsed.setResource(resource);
		existing.updateWith(parsed);
		existing.setResource(resource);
		existing.revalidateAllBindings();

		FlexoConcept updated = existing.getVirtualModel().getFlexoConcept("ConceptB");
		assertEquals("The model did not take the edited renderer", "\"X-\" + instance.name", updated.getRenderer().toString());
		String after = existing.getFMLPrettyPrint();
		assertTrue("The serialization lost the edited renderer:\n" + after, after.contains("@Renderer(\"X-\" + instance.name)"));
	}

	/** A second edit, once the renderer has been merged from the text and read again. */
	@Test
	@TestOrder(3)
	public void test2EditAgain() throws Exception {
		FMLCompilationUnit existing = resource.getCompilationUnit();
		String text = existing.getFMLPrettyPrint().replace("@Renderer(\"X-\" + instance.name)", "@Renderer(\"Y-\" + instance.name)");
		FMLCompilationUnit parsed = new FMLCompilationUnitParser().parse(text, existing.getFMLModelFactory(), modelSlotClasses -> null,
				true);
		parsed.setResource(resource);
		existing.updateWith(parsed);
		existing.setResource(resource);
		existing.revalidateAllBindings();

		assertEquals("\"Y-\" + instance.name", existing.getVirtualModel().getFlexoConcept("ConceptB").getRenderer().toString());
		assertTrue(existing.getFMLPrettyPrint().contains("@Renderer(\"Y-\" + instance.name)"));
	}
}
