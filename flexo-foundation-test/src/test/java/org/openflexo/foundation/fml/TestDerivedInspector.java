/**
 *
 * Copyright (c) 2014-2026, Openflexo
 *
 * This file is part of Flexo-foundation, a component of the software infrastructure
 * developed at Openflexo.
 *
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either
 * version 1.1 of the License, or any later version ), which is available at
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 *
 * You can redistribute it and/or modify under the terms of either of these licenses
 *
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or
 *          combining it with software containing parts covered by the terms
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. *
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.openflexo.org/license.html for details.
 *
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 *
 */

package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.binding.FlexoConceptBindingModel;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitParser;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * <code>@Inspector(derived=…)</code>: hands inspection of an instance of a concept entirely to another
 * {@link org.openflexo.foundation.fml.rt.FlexoConceptInstance}, found by evaluating the expression against the instance being
 * inspected - see {@link FlexoConcept#getDerivedInspector()} and
 * {@link org.openflexo.foundation.fml.rt.FlexoConceptInstance#getInspectedObject()}, which follows it.
 *
 * <p>
 * Everything here is at the {@link FlexoConcept} / annotation level: what a validation rule or {@code getInspectedObject()} does with
 * an actual {@link org.openflexo.foundation.fml.rt.FlexoConceptInstance} at runtime is NOT exercised here (it would need a full
 * VirtualModelInstance) - only that the annotation parses, resolves, round-trips through the FML source, and is flagged by the
 * validation rules when misused.
 *
 * <p>
 * Loads the real <code>TestDerivedInspector.fml</code> fixture (flexo-test-resources) rather than a detached
 * {@code FMLCompilationUnitParser} parse: a role such as <code>myConceptB</code> is "too early to parse" on the first pass (Connie
 * defers it, {@code DataBinding} stays {@code needsParsing}) and only resolves once the SECOND analysis pass a real
 * {@code CompilationUnitResource} load runs actually retries it (see {@code CompilationUnitResourceImpl.finalizeLoadResourceData()},
 * CORE-D-26's "second analysis pass" in KNOWN_DEFECTS.md) - a bare parse of an in-memory string never gets that second pass, and the
 * binding stays permanently invalid.
 */
@RunWith(OrderedRunner.class)
public class TestDerivedInspector extends OpenflexoTestCase {

	private static final String FIXTURE_URI = "http://openflexo.org/test/TestResourceCenter/TestDerivedInspector.fml";

	private static FlexoEditor editor;
	private static VirtualModel virtualModel;

	@Test
	@TestOrder(1)
	public void test0LoadFixture() {

		instanciateTestServiceManager();
		editor = new DefaultFlexoEditor(null, serviceManager);

		CompilationUnitResource resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(FIXTURE_URI);
		assertNotNull("No compilation unit for " + FIXTURE_URI, resource);

		virtualModel = resource.getCompilationUnit().getVirtualModel();
		assertNotNull(virtualModel);
		assertEquals("The fixture did not parse", 4, virtualModel.getFlexoConcepts().size());
	}

	/** Storage: a single key-value pair under the SAME <code>Inspector</code> metadata key as a filename would use. */
	@Test
	@TestOrder(2)
	public void test1DerivedIsDeclared() {

		FlexoConcept conceptA = concept("ConceptA");

		assertTrue(conceptA.hasDerivedInspector());
		assertTrue(conceptA.hasMetaData(FlexoConcept.INSPECTOR_METADATA));

		assertNotNull(conceptA.getDerivedInspector());
		assertTrue(conceptA.getDerivedInspector().isSet());
		assertTrue("Invalid: " + conceptA.getDerivedInspector().invalidBindingReason(), conceptA.getDerivedInspector().isValid());
		assertEquals("myConceptB", conceptA.getDerivedInspector().toString());

		// No file resolves for a concept that only derives: getInspectorComponentResource() stays orthogonal (it never even
		// gets consulted at runtime, since FlexoConceptInstance.getInspectedObject() redirects before it would be)
		assertNull(conceptA.getInspectorComponentResource());
	}

	/** A concept declaring neither convention, file, nor derived key has none of it. */
	@Test
	@TestOrder(3)
	public void test2ConceptWithoutInspectorDeclaresNoDerivation() {
		FlexoConcept without = concept("WithoutAnyInspector");
		assertFalse(without.hasDerivedInspector());
		assertNull(without.getInspectorComponentResource());
	}

	/**
	 * The binding is expressed on top of the concept's OWN binding model - <code>myConceptB</code> resolves directly as a role, not
	 * <code>instance.myConceptB</code> - exactly the shape {@code @Renderer} already has (see
	 * {@code TestFMLBindingModelManagement#testFlexoConceptInstanceRenderer}).
	 */
	@Test
	@TestOrder(4)
	public void test3DerivedInspectorContextExposesTheConceptsOwnRoles() {

		FlexoConcept conceptA = concept("ConceptA");

		assertNotNull(conceptA.getDerivedInspectorContext());
		assertNotNull(conceptA.getDerivedInspectorContext().getBindingModel().bindingVariableNamed(FlexoConceptBindingModel.THIS_PROPERTY_NAME));
		assertNotNull(conceptA.getDerivedInspectorContext().getBindingModel().bindingVariableNamed("myConceptB"));
		assertNotNull(conceptA.getDerivedInspectorContext().getBindingModel().bindingVariableNamed(FlexoConcept.DERIVED_INSPECTOR_INSTANCE_PROPERTY));
		assertEquals(FlexoConceptInstanceType.getFlexoConceptInstanceType(conceptA), conceptA.getDerivedInspectorContext().getBindingModel()
				.bindingVariableNamed(FlexoConcept.DERIVED_INSPECTOR_INSTANCE_PROPERTY).getType());
	}

	/**
	 * Declaring and reading back a derived inspector round-trips through the FML source: what
	 * {@code CompilationUnitResource.save()} writes, parsed back, resolves the same way.
	 */
	@Test
	@TestOrder(5)
	public void test4RoundTripsThroughTheFMLSource() throws Exception {

		// A DIFFERENT concept than "WithoutAnyInspector": this test mutates it, and later tests rely on "WithoutAnyInspector"
		// staying pristine (declaring nothing) - all ordered tests here share the same in-memory model.
		FlexoConcept withoutInspector = concept("AnotherWithoutAnyInspector");
		assertFalse(withoutInspector.hasDerivedInspector());

		withoutInspector.setDerivedInspector(new DataBinding<>("myConceptB"));
		assertTrue(withoutInspector.hasDerivedInspector());

		String after = virtualModel.getDeclaringCompilationUnit().getFMLPrettyPrint();
		assertTrue("The FML source does not declare the derived inspector:\n" + after, after.contains("@Inspector(derived=myConceptB)"));

		FMLCompilationUnit reparsed = new FMLCompilationUnitParser().parse(after, virtualModel.getDeclaringCompilationUnit().getFMLModelFactory(),
				modelSlotClasses -> null, false);
		FlexoConcept reparsedConcept = reparsed.getVirtualModel().getFlexoConcept("AnotherWithoutAnyInspector");
		assertNotNull(reparsedConcept);
		assertTrue("The saved source would not declare the derived inspector any more once parsed back",
				reparsedConcept.hasDerivedInspector());
		assertEquals("myConceptB", reparsedConcept.getDerivedInspector().toString());
	}

	/** {@link FlexoConcept.DerivedInspectorBindingMustBeValid}: silent otherwise, called directly rather than through the validation model. */
	@Test
	@TestOrder(6)
	public void test5BindingValidationRule() {

		FlexoConcept.DerivedInspectorBindingMustBeValid rule = new FlexoConcept.DerivedInspectorBindingMustBeValid();

		assertNotNull(rule.getBinding(concept("ConceptA")));
		assertNull("A concept without a derived inspector has nothing for the rule to check", rule.getBinding(concept("WithoutAnyInspector")));

		assertNull("A valid binding raises nothing", rule.applyValidation(concept("ConceptA")));
	}

	/**
	 * {@link FlexoConcept.DerivedInspectorMustBeExclusiveOfAnInspectorComponent}: called directly, same reason as above. Needs a
	 * concept that actually RESOLVES a <code>.inspector</code> component to be a genuine test - the in-memory fixture above is
	 * detached from any resource center, so nothing resolves there; borrows <code>TestContainerUI.fml</code>'s "Simple", which
	 * resolves <code>Simple.inspector</code> by convention (see {@link TestContainerUIComponents}), for this one case.
	 */
	@Test
	@TestOrder(7)
	public void test6ExclusivityValidationRule() {

		FlexoConcept.DerivedInspectorMustBeExclusiveOfAnInspectorComponent rule = new FlexoConcept.DerivedInspectorMustBeExclusiveOfAnInspectorComponent();

		assertNull("derived alone raises nothing", rule.applyValidation(concept("ConceptA")));
		assertNull("neither derived nor a component: nothing to raise either", rule.applyValidation(concept("WithoutAnyInspector")));

		CompilationUnitResource containerUIResource = serviceManager.getVirtualModelLibrary()
				.getCompilationUnitResource("http://openflexo.org/test/TestResourceCenter/TestContainerUI.fml");
		assertNotNull(containerUIResource);
		FlexoConcept simple = containerUIResource.getCompilationUnit().getVirtualModel().getFlexoConcept("Simple");
		assertNotNull(simple);
		assertNotNull("The fixture must have something for getInspectorComponentResource() to resolve, for this test to mean anything",
				simple.getInspectorComponentResource());
		assertNull("Simple.inspector alone, no derived: nothing to raise", rule.applyValidation(simple));

		simple.setDerivedInspector(new DataBinding<>("this"));
		assertNotNull("Declaring both must be flagged", rule.applyValidation(simple));
	}

	private static FlexoConcept concept(String name) {
		FlexoConcept returned = virtualModel.getFlexoConcept(name);
		assertNotNull("No concept " + name + " in the fixture", returned);
		return returned;
	}
}
