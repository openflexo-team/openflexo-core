package org.openflexo.foundation.fml.parser;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openflexo.connie.expr.BinaryOperatorExpression;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.expr.CastExpression;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.foundation.fml.expr.FMLCastExpression;
import org.openflexo.foundation.fml.expr.FMLInstanceOfExpression;

@RunWith(JUnit4.class)
public class TestExpressionWithTypesParser extends ExpressionParserTestCase {

	// Test instanceof

	@Test
	public void testInstanceOf() {
		tryToParse("a instanceof Toto", "a instanceof Toto", FMLInstanceOfExpression.class, null, serviceManager, false);
	}

	@Test
	public void testInstanceOfInteger() {
		tryToParse("2 instanceof Integer", "true", FMLInstanceOfExpression.class, true, serviceManager, false);
	}

	@Test
	public void testInstanceOfInteger2() {
		tryToParse("2.2 instanceof Integer", "false", FMLInstanceOfExpression.class, false, serviceManager, false);
	}

	// Test new instance

	@Test
	public void testNewInstance() {
		tryToParse("new ArrayList()", "new ArrayList()", BindingPath.class, null, serviceManager, false);
	}

	@Test
	public void testNewInstance2() {
		tryToParse("new a.MyA()", "new a.MyA()", BindingPath.class, null, serviceManager, false);
	}

	@Test
	public void testNewInstance3() {
		tryToParse("new a.MyA(new a.MyB(),new a.MyC())", "new a.MyA(new a.MyB(),new a.MyC())", BindingPath.class, null, serviceManager,
				false);
	}

	@Test
	public void testNewInstance4() {
		tryToParse("new java.util.Hashtable<String,java.util.List<String>>()", "new Hashtable<String,List<String>>()", BindingPath.class,
				null, serviceManager, false);
	}

	@Test
	public void testNewInstance5() {
		tryToParse("(Map)(new java.util.Hashtable(1,2))", "(Map)new Hashtable(1,2)", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testCombo() {
		tryToParse("new Object().toString()", "new Object().toString()", BindingPath.class, null, serviceManager, false);
	}

	@Test
	public void testInnerNewInstance() {
		tryToParse("a.b.new c.d.MyE()", "a.b.new c.d.MyE()", BindingPath.class, null, serviceManager, false);
	}

	// Test cast

	@Test
	public void testIntCast() {
		tryToParse("(int)2", "2", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testShortCast() {
		tryToParse("(short)2", "2", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testByteCast() {
		tryToParse("(byte)2", "2", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testLongCast() {
		tryToParse("(long)2", "2", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testFloatCast() {
		tryToParse("(float)2", "2.0", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testDoubleCast() {
		tryToParse("(double)2", "2.0", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testCharCast() {
		tryToParse("(char)2", "2", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testBooleanCast() {
		tryToParse("(boolean)a", "(boolean)a", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testCastWithClass() {
		tryToParse("(AType)2", "(AType)2", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testCast2() {
		tryToParse("(java.lang.Integer)2", "2", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testCast3() {
		tryToParse("(int)2+((float)2+(double)2)", "6.0", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	// CORE-F-5: the type of '(T) x' is a parenthesed expression read back as a type, so it must be a (qualified) name; a cast to a type
	// with arguments or wildcards is written 'x as T'. The normalized print keeps '(T) x' for a name and uses 'as' for the others

	@Test
	public void aGenericCastInParenthesesIsRejected() {
		tryToParse("(List<Tutu>)toto", null, CastExpression.class, null, serviceManager, true);
		tryToParse("(java.util.List<java.lang.String>)data.list", null, CastExpression.class, null, serviceManager, true);
	}

	@Test
	public void testCast4() {
		tryToParse("toto as java.util.List<Tutu>", "(toto as List<Tutu>)", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testCast5() {
		CastExpression e = (CastExpression) (tryToParse("toto as List<Tutu>", "(toto as List<Tutu>)", CastExpression.class, null,
				serviceManager, false));
		assertEquals(((ParameterizedTypeImpl) e.getCastType()).getRawType(), List.class);
	}

	@Test
	public void testCast6() {
		tryToParse("toto as Foo<Tutu>", "(toto as Foo<Tutu>)", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testParameteredCast() {
		tryToParse("data.list as java.util.List<java.lang.String>", "(data.list as List<String>)", CastExpression.class, null,
				serviceManager, false);
	}

	@Test
	public void testParameteredCast2() {
		tryToParse("data.map as java.util.Hashtable<java.lang.String,java.util.List<java.lang.String>, Boolean>",
				"(data.map as Hashtable<String,List<String>,Boolean>)", CastExpression.class, null, serviceManager, false);
	}

	@Test
	// Test with Shr syntax
	public void testParameteredCast3() {
		tryToParse("data.map as java.util.Hashtable<java.lang.String,java.util.List<java.lang.String>>",
				"(data.map as Hashtable<String,List<String>>)", CastExpression.class, null, serviceManager, false);
	}

	@Test
	// Test with Ushr syntax
	public void testParameteredCast4() {
		tryToParse("a.path as MyA<MyB<MyC<MyD>>>", "(a.path as MyA<MyB<MyC<MyD>>>)", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testWilcardUpperBound() {
		tryToParse("toto as List<? extends Tutu>", "(toto as List<? extends Tutu>)", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testWilcardLowerBound() {
		// 'super' is not a keyword of the binding entry point: a lower bound is written in FML proper only
		tryToParse("toto as List<? extends Tutu>", "(toto as List<? extends Tutu>)", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testWilcardUpperBounds() {
		tryToParse("toto as Map<? extends Key, ? extends Value>", "(toto as Map<? extends Key,? extends Value>)", CastExpression.class,
				null, serviceManager, false);
	}

	@Test
	public void aCastWithAsOfASimpleTypeIsPrintedInParentheses() {
		tryToParse("toto as Foo", "(Foo)toto", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void theOperandOfACastKeepsItsParentheses() {
		// '(AType)a + b' would cast 'a' only
		tryToParse("(AType)(a + b)", "(AType)(a + b)", CastExpression.class, null, serviceManager, false);
		tryToParse("(a + b) as AType", "(AType)(a + b)", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void asBindsLooserThanArithmetic() {
		// 'a + b as T' casts 'a + b', as 'instanceof' would test it
		CastExpression e = (CastExpression) tryToParse("a + b as List<Tutu>", "((a + b) as List<Tutu>)", CastExpression.class, null,
				serviceManager, false);
		assertTrue(e.getArgument() instanceof BinaryOperatorExpression);
	}

	@Test
	public void aCastOfASignedOperandIsWrittenWithParentheses() {
		// '(AType) -a' is a subtraction (CORE-F-5): the cast of a signed operand is written '(AType)(-a)'
		tryToParse("(AType) -a", "AType - a", BinaryOperatorExpression.class, null, serviceManager, false);
		tryToParse("(AType)(-a)", "(AType)(-a)", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void aOneLetterTypeCanBeCastTo() {
		// 'A' was a constant identifier before CORE-F-5, not a type name
		tryToParse("(A)a", "(A)a", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testImportType() {
		tryToParse("(java.lang.reflect.Type)object", "(Type)object", FMLCastExpression.class, null, serviceManager, false);
		assertTrue(getTypingSpace().isTypeImported(java.lang.reflect.Type.class));
	}

	// Test class methods

	@Test
	public void testClassMethod1() {
		tryToParse("Class.forName(\"Foo\")", "Class.forName(\"Foo\")", BindingPath.class, null, serviceManager, false);
	}

	@Test
	public void testClassMethod2() {
		tryToParse("java.lang.Class.forName(\"Foo\")", "Class.forName(\"Foo\")", BindingPath.class, null, serviceManager, false);
	}

	// Primitive conversions

	@Test
	public void testIntToDoubleCast() {
		tryToParse("(double)2", "2.0", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testIntToFloatCast() {
		tryToParse("(float)2", "2.0", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testDoubleToIntegerCast() {
		tryToParse("(int)3.14", "3", FMLCastExpression.class, null, serviceManager, false);
	}

}
