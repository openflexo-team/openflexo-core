package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openflexo.foundation.fml.parser.TestFMLReindent.parse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;

/**
 * CORE-D-68: the parameter of a setter keeps the name written ({@code set(int aValue)}): the model holds it, the body refers to it, and both
 * pretty-prints write it (it was always "value").
 */
public class TestCoreD68 {

	@BeforeClass
	public static void initServiceManager() {
		TestFMLReindent.initServiceManager();
	}

	private static final String UNIT = String.join("\n", //
			"use org.openflexo.foundation.fml.rt.FMLRTModelSlot as FMLRT;", //
			"@URI(\"http://openflexo.org/test/CoreD68.fml\")", //
			"public model CoreDefectSetter {", //
			"\tpublic concept Holder {", //
			"\t\tint internalValue;", //
			"\t\tint settable_value {", //
			"\t\t\tint get() { return internalValue; }", //
			"\t\t\tset(int aValue) { internalValue = aValue; }", //
			"\t\t};", //
			"\t}", //
			"}", "");

	@Test
	public void theNameOfTheSetterParameterIsKept() throws Exception {
		FMLCompilationUnit unit = parse(UNIT);
		GetSetProperty<?> property = (GetSetProperty<?>) unit.getVirtualModel().getFlexoConcept("Holder")
				.getAccessibleProperty("settable_value");
		assertEquals("aValue", property.getValueVariableName());
		AssignationAction<?> assignation = (AssignationAction<?>) property.getSetControlGraph();
		ExpressionAction<?> value = (ExpressionAction<?>) assignation.getAssignableAction();
		assertTrue(value.getExpression() + ": " + value.getExpression().invalidBindingReason(), value.getExpression().isValid());
		assertTrue(unit.getFMLPrettyPrint(), unit.getFMLPrettyPrint().contains("set(int aValue) { internalValue = aValue; }"));
		assertTrue(unit.getNormalizedFML(), unit.getNormalizedFML().contains("set(int aValue)"));
	}
}
