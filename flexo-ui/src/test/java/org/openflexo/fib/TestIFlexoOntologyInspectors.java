package org.openflexo.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBInspectorTestCase;
import org.openflexo.toolbox.ResourceLocator;

public class TestIFlexoOntologyInspectors extends GenericFIBInspectorTestCase {

	public static void main(String[] args) {
		System.out.println(generateInspectorTestCaseClass(ResourceLocator.locateDirectory("Inspectors/IFlexoOntology"), "Inspectors/IFlexoOntology/"));
	}

	@Test
	public void testIFlexoOntologyInspector() {
		validateFIB("Inspectors/IFlexoOntology/IFlexoOntology.inspector");
	}

	@Test
	public void testIFlexoOntologyClassInspector() {
		validateFIB("Inspectors/IFlexoOntology/IFlexoOntologyClass.inspector");
	}

	@Test
	public void testIFlexoOntologyConceptInspector() {
		validateFIB("Inspectors/IFlexoOntology/IFlexoOntologyConcept.inspector");
	}

	@Test
	public void testIFlexoOntologyDataPropertyInspector() {
		validateFIB("Inspectors/IFlexoOntology/IFlexoOntologyDataProperty.inspector");
	}

	@Test
	public void testIFlexoOntologyIndividualInspector() {
		validateFIB("Inspectors/IFlexoOntology/IFlexoOntologyIndividual.inspector");
	}

	@Test
	public void testIFlexoOntologyObjectPropertyInspector() {
		validateFIB("Inspectors/IFlexoOntology/IFlexoOntologyObjectProperty.inspector");
	}
}
