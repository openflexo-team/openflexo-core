package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitParser;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * A binding is written back as it was written ({@code DataBinding#getPrettyPrint()}, given by its p2pp nodes), and keeps its normalized text
 * in {@code DataBinding#toString()}: when typed (an inspector, {@code setUnparsedBinding}), when parsed from an FML unit, and in the FML text
 * of the unit (CORE-D-33).
 */
@RunWith(OrderedRunner.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TestDataBindingPrettyPrint extends OpenflexoTestCase {

	private static final String FIXTURE_URI = "http://openflexo.org/test/TestResourceCenter/TestDerivedInspector.fml";

	private static CompilationUnitResource resource;
	private static FlexoConcept concept;
	private static PrimitiveRole<?> role;

	@Test
	@TestOrder(1)
	public void test0ATypedBindingKeepsItsSyntax() throws Exception {
		instanciateTestServiceManager();
		resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(FIXTURE_URI);
		assertNotNull(resource);
		concept = resource.getCompilationUnit().getVirtualModel().getFlexoConcept("ConceptB");
		role = concept.getFMLModelFactory().newPrimitiveRole();
		role.setName("aPrettyPrintedInt");
		role.setPrimitiveType(PrimitiveType.Integer);
		concept.addToFlexoProperties(role);
		((PrimitiveRole) role).setDefaultValue(new DataBinding<>("1+2", role, Integer.class, BindingDefinitionType.GET));

		DataBinding<?> value = role.getDefaultValue();
		assertEquals("1 + 2", value.toString());
		assertEquals("1+2", value.getPrettyPrint());
		String text = resource.getCompilationUnit().getFMLPrettyPrint();
		assertTrue(text, text.contains("aPrettyPrintedInt = 1+2;"));
		assertTrue(resource.getCompilationUnit().getNormalizedFML().contains("aPrettyPrintedInt = 1 + 2;"));
	}

	@Test
	@TestOrder(2)
	public void test1ABindingEditedInPlaceKeepsTheNewSyntax() throws Exception {
		DataBinding<?> value = role.getDefaultValue();
		value.setUnparsedBinding("(1+2)*3");
		assertEquals("(1 + 2) * 3", value.toString());
		assertEquals("(1+2)*3", value.getPrettyPrint());
		// Equality stays on the normalized text
		assertEquals(new DataBinding<>("(1 + 2) * 3", role, Integer.class, BindingDefinitionType.GET), value);
		String text = resource.getCompilationUnit().getFMLPrettyPrint();
		assertTrue(text, text.contains("aPrettyPrintedInt = (1+2)*3;"));
	}

	@Test
	@TestOrder(3)
	public void test2ABindingParsedFromTheUnitKeepsItsSyntax() throws Exception {
		String text = resource.getCompilationUnit().getFMLPrettyPrint();
		FMLCompilationUnit parsed = new FMLCompilationUnitParser().parse(text, resource.getCompilationUnit().getFMLModelFactory(),
				modelSlotClasses -> null, true);
		FlexoProperty<?> property = parsed.getVirtualModel().getFlexoConcept("ConceptB").getAccessibleProperty("aPrettyPrintedInt");
		DataBinding<?> value = ((PrimitiveRole<?>) property).getDefaultValue();
		assertEquals("(1 + 2) * 3", value.toString());
		assertEquals("delegate=" + value.getPrettyPrintDelegate(), "(1+2)*3", value.getPrettyPrint());
	}

	@Test
	@TestOrder(4)
	public void test3ABindingSetFromTheModelIsNormalized() throws Exception {
		// An expression set from the model, not from a text: it has no syntax of its own
		DataBinding<?> value = role.getDefaultValue();
		DataBinding<?> other = new DataBinding<>("4+5", role, Integer.class, BindingDefinitionType.GET);
		((DataBinding) value).setExpression(other.getExpression());
		assertEquals("4 + 5", value.getPrettyPrint());
		concept.removeFromFlexoProperties(role);
	}
}
