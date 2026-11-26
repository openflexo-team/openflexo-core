/**
 *
 * Copyright (c) 2026, Openflexo
 *
 * This file is part of Openflexo-core, a component of the software infrastructure
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
 *          of EPL 1.0, the licensors of this Program grant you additional
 *          permission to convey the resulting work. *
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 *
 * See http://www.openflexo.org/license.html for details.
 *
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 *
 */

package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.openflexo.foundation.fml.parser.TestFMLReindent.parse;

import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.SimpleMethodPathElement;
import org.openflexo.connie.binding.StaticMethodPathElement;
import org.openflexo.connie.binding.StaticMethodPathElementImpl;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.foundation.fml.ExpressionProperty;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.editionaction.ReturnStatement;
import org.openflexo.foundation.fml.FMLObject.VariableShouldNotHideAType;
import org.openflexo.foundation.fml.FMLObject.VariableShouldNotHideAType.VariableHidesATypeWarning;
import org.openflexo.pamela.validation.ValidationIssue;

/**
 * CORE-F-5: with a single identifier token, 'Foo.bar()' has one syntax for a static call and for a call on a value. As in Java, a variable
 * hides a type of the same name: the call is a call on the variable when one is visible, a static call on the type otherwise. While a unit
 * is parsed the variables are not known yet: the decision is taken again once the model is assembled. The other way round, a call on a
 * name that was neither a variable nor a type becomes a static call once that name resolves to a type. The rule
 * {@link VariableShouldNotHideAType} reports a variable that hides a type.
 */
public class TestStaticCallOrVariable {

	@BeforeClass
	public static void initServiceManager() {
		TestFMLReindent.initServiceManager();
	}

	private static final String UNIT = String.join("\n", //
			"use org.openflexo.foundation.fml.rt.FMLRTModelSlot as FMLRT;", //
			"@URI(\"http://openflexo.org/test/StaticCallOrVariable.fml\")", //
			"public model StaticCallOrVariable {", //
			"\tpublic concept WithAVariable {", //
			"\t\tString Math;", //
			"\t\tint length values Math.length();", //
			"\t}", //
			"\tpublic concept WithAParameter {", //
			"\t\tint count(String Math) {", //
			"\t\t\treturn Math.length();", //
			"\t\t}", //
			"\t}", //
			"\tpublic concept WithoutVariable {", //
			"\t\tint maximum values Math.max(1, 2);", //
			"\t\tint unknown values Foo.bar();", //
			"\t}", //
			"}", "");

	private static FMLCompilationUnit unit;

	private static ExpressionProperty<?> property(String concept, String name) throws Exception {
		if (unit == null) {
			unit = parse(UNIT);
		}
		return (ExpressionProperty<?>) unit.getVirtualModel().getFlexoConcept(concept).getAccessibleProperty(name);
	}

	private static BindingPath path(ExpressionProperty<?> property) {
		DataBinding<?> binding = property.getExpression();
		assertTrue(binding.getExpression() + " is a path", binding.getExpression() instanceof BindingPath);
		return (BindingPath) binding.getExpression();
	}

	@Test
	public void aVariableHidesATypeOfTheSameName() throws Exception {
		ExpressionProperty<?> length = property("WithAVariable", "length");
		BindingPath path = path(length);
		assertNotNull("'Math' is the property, not the type", path.getBindingVariable());
		assertEquals("Math", path.getBindingVariable().getVariableName());
		assertTrue(path.getBindingPathElementAtIndex(0) instanceof SimpleMethodPathElement);
		assertTrue(length.getExpression().invalidBindingReason(), length.getExpression().isValid());
	}

	@Test
	public void aParameterHidesATypeOfTheSameName() throws Exception {
		property("WithoutVariable", "maximum");
		FlexoBehaviour count = unit.getVirtualModel().getFlexoConcept("WithAParameter").getFlexoBehaviour("count");
		ReturnStatement<?> returnStatement = (ReturnStatement<?>) count.getControlGraph();
		DataBinding<?> expression = ((ExpressionAction<?>) returnStatement.getAssignableAction()).getExpression();
		assertTrue(expression + " is a path", expression.getExpression() instanceof BindingPath);
		BindingPath path = (BindingPath) expression.getExpression();
		assertNotNull("'Math' is the parameter, not the type", path.getBindingVariable());
		assertEquals("Math", path.getBindingVariable().getVariableName());
		assertTrue(expression.invalidBindingReason(), expression.isValid());
	}

	@Test
	public void withoutAVariableTheCallIsStatic() throws Exception {
		ExpressionProperty<?> maximum = property("WithoutVariable", "maximum");
		BindingPath path = path(maximum);
		assertNull(path.getBindingVariable());
		assertTrue(path.getBindingPathElementAtIndex(0) instanceof StaticMethodPathElement);
		assertTrue(maximum.getExpression().invalidBindingReason(), maximum.getExpression().isValid());
	}

	@Test
	public void aReceiverThatIsNeitherAVariableNorATypeIsAnUnresolvedVariable() throws Exception {
		ExpressionProperty<?> unknown = property("WithoutVariable", "unknown");
		BindingPath path = path(unknown);
		assertNotNull(path.getBindingVariable());
		assertEquals("Foo", path.getBindingVariable().getVariableName());
		assertFalse(unknown.getExpression().isValid());
	}

	@Test
	public void theTextIsKept() throws Exception {
		property("WithAVariable", "length");
		assertTrue(unit.getFMLPrettyPrint(), unit.getFMLPrettyPrint().contains("int length values Math.length();"));
		assertTrue(unit.getFMLPrettyPrint(), unit.getFMLPrettyPrint().contains("int maximum values Math.max(1, 2);"));
		assertTrue(unit.getNormalizedFML(), unit.getNormalizedFML().contains("Math.length()"));
	}

	@Test
	public void theRuleReportsAVariableThatHidesAType() throws Exception {
		VariableShouldNotHideAType rule = new VariableShouldNotHideAType();
		ValidationIssue<?, ?> issue = rule.applyValidation(property("WithAVariable", "length"));
		assertTrue(String.valueOf(issue), issue instanceof VariableHidesATypeWarning);
		assertEquals("Math", ((VariableHidesATypeWarning) issue).getHidingNames());
		assertNull(rule.applyValidation(property("WithoutVariable", "maximum")));
		assertNull(rule.applyValidation(property("WithoutVariable", "unknown")));
	}

	/** A unit whose receiver 'Pattern' is not a type when it is parsed: the import that makes it one is added afterwards */
	private static final String LATE_TYPE_UNIT = String.join("\n", //
			"use org.openflexo.foundation.fml.rt.FMLRTModelSlot as FMLRT;", //
			"@URI(\"http://openflexo.org/test/StaticCallOnALateType.fml\")", //
			"public model StaticCallOnALateType {", //
			"\tpublic concept WithALateType {", //
			"\t\tint count values Pattern.quote(\"a\").length();", //
			"\t}", //
			"}", "");

	@Test
	public void aCallOnANameThatBecomesATypeBecomesStatic() throws Exception {
		FMLCompilationUnit lateUnit = parse(LATE_TYPE_UNIT);
		ExpressionProperty<?> count = (ExpressionProperty<?>) lateUnit.getVirtualModel().getFlexoConcept("WithALateType")
				.getAccessibleProperty("count");
		BindingPath path = path(count);
		assertNotNull("'Pattern' names nothing yet: a variable that is not resolved", path.getBindingVariable());
		assertEquals("Pattern", path.getBindingVariable().getVariableName());
		assertFalse(count.getExpression().isValid());

		JavaImportDeclaration patternImport = lateUnit.getFMLModelFactory().newJavaImportDeclaration();
		patternImport.setFullQualifiedClassName("java.util.regex.Pattern");
		lateUnit.addToJavaImports(patternImport);
		count.getExpression().invalidate();

		assertTrue(count.getExpression().invalidBindingReason(), count.getExpression().isValid());
		assertSame("the path is kept, its elements change", path, count.getExpression().getExpression());
		assertNull(path.getBindingVariable());
		assertEquals(2, path.getBindingPathElementCount());
		assertTrue(path.getBindingPathElementAtIndex(0) instanceof StaticMethodPathElement);
		assertEquals(Pattern.class, ((StaticMethodPathElementImpl<?>) path.getBindingPathElementAtIndex(0)).getDeclaringType());
		assertTrue("the element after the call is built on the static call", path.getBindingPathElementAtIndex(1).isResolved());
		assertTrue(lateUnit.getNormalizedFML(), lateUnit.getNormalizedFML().contains("int count values Pattern.quote(\"a\").length();"));
	}
}
