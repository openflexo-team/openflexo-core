package org.openflexo.foundation.fml.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.foundation.fml.expr.FMLBinaryOperatorExpression;

@RunWith(JUnit4.class)
public class TestBindingValueParser extends ExpressionParserTestCase {

	@Test
	public void testSimpleIdentifier() {
		// initServiceManager();
		tryToParse("foo", "foo", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testUnderscoredIdentifier() {
		tryToParse("foo_foo2", "foo_foo2", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testConstantIdentifier1() {
		tryToParse("FOO", "FOO", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testConstantIdentifier2() {
		tryToParse("DEFAULT_EXAMPLE_DIAGRAM", "DEFAULT_EXAMPLE_DIAGRAM", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testComposedIdentifier() {
		tryToParse("foo.foo2.foo3", "foo.foo2.foo3", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleMethodNoArgs() {
		tryToParse("method()", "method()", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleMethodWith1Arg() {
		tryToParse("method(1)", "method(1)", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleMethodWith3Args() {
		tryToParse("method(1,2,3)", "method(1,2,3)", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testFullQualifiedMethod() {
		tryToParse("a.b.c.method(1)", "a.b.c.method(1)", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testImbricatedMethods() {
		tryToParse("a.b.c.method(m1(1),d.e.f.m2(1))", "a.b.c.method(m1(1),d.e.f.m2(1))", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testPipelineMethodField() {
		tryToParse("m().f", "m().f", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testPipelineMethods1() {
		tryToParse("m1().m2()", "m1().m2()", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testPipelineMethods2() {
		tryToParse("substring(23,27).toUpperCase()", "substring(23,27).toUpperCase()", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testExpressionWithBindings() {
		tryToParse("a.b.c.method1(1).method2(2)+c.d.e", "a.b.c.method1(1).method2(2) + c.d.e", FMLBinaryOperatorExpression.class, null,
				serviceManager, false);
	}

	@Test
	public void testBindingWithExpressions() {
		tryToParse("i.am.a(1,2+3,7.8,\"foo\",'a').little.test(1).foo()", "i.am.a(1,5,7.8,\"foo\",'a').little.test(1).foo()",
				BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testExpressionWithBindings2() {
		tryToParse("beginDate.toString.substring(0,(beginDate.toString.length - 9))",
		 "beginDate.toString.substring(0,beginDate.toString.length - 9)", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testWithSuper1() {
		tryToParse("super.a", "super.a", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testWithSuper2() {
		tryToParse("a.super.b", "a.super.b", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testWithSuper3() {
		tryToParse("a.b.super.c", "a.b.super.c", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testWithSuper4() {
		tryToParse("a.super.b.c", "a.super.b.c", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testWithSuper5() {
		tryToParse("super(1)", "super(1)", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testWithSuper6() {
		tryToParse("super.a(1)", "super.a(1)", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testWithSuper7() {
		tryToParse("a.super.b(1)", "a.super.b(1)", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testWithSuper8() {
		tryToParse("super.init()", "super.init()", BindingValue.class, null, serviceManager, false);
	}

	@Test
	public void testAccentCharacter() {
		tryToParse("flexoConcept.unité", "flexoConcept.unité", BindingValue.class, null, serviceManager, false);
	}

	/*public void testClassMethod1() {
		tryToParse("Class.forName(\"Toto\")", "toto--", BindingValue.class, null, false);
	}
	
	public void testClassMethod2() {
		tryToParse("java.Class.forName(\"Toto\")", "toto--", BindingValue.class, null, false);
	}*/

}
