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
	public void testVoidCast() {
		tryToParse("(void)2", "(void)2", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testIntCast() {
		tryToParse("(int)2", "(int)2", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testShortCast() {
		tryToParse("(short)2", "(short)2", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testByteCast() {
		tryToParse("(byte)2", "(byte)2", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testLongCast() {
		tryToParse("(long)2", "(long)2", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testFloatCast() {
		tryToParse("(float)2", "(float)2", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testDoubleCast() {
		tryToParse("(double)2", "(double)2", FMLCastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testCharCast() {
		tryToParse("(char)2", "(char)2", FMLCastExpression.class, null, serviceManager, false);
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
		tryToParse("(java.lang.Integer)2", "(Integer)2", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testCast3() {
		tryToParse("(int)2+((float)2+(double)2)", "(int)2 + ((float)2 + (double)2)", BinaryOperatorExpression.class, null, serviceManager,
				false);
	}

	@Test
	public void testCast4() {
		tryToParse("(java.util.List<Tutu>)toto", "(List<Tutu>)toto", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testCast5() {
		CastExpression e = (CastExpression) (tryToParse("(List<Tutu>)toto", "(List<Tutu>)toto", CastExpression.class, null, serviceManager,
				false));
		assertEquals(((ParameterizedTypeImpl) e.getCastType()).getRawType(), List.class);
	}

	@Test
	public void testCast6() {
		tryToParse("(Foo<Tutu>)toto", "(Foo<Tutu>)toto", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testParameteredCast() {
		tryToParse("(java.util.List<java.lang.String>)data.list", "(List<String>)data.list", CastExpression.class, null, serviceManager,
				false);
	}

	@Test
	public void testParameteredCast2() {
		tryToParse("(java.util.Hashtable<java.lang.String,java.util.List<java.lang.String>, Boolean>)data.map",
				"(Hashtable<String,List<String>,Boolean>)data.map", CastExpression.class, null, serviceManager, false);
	}

	@Test
	// Test with Shr syntax
	public void testParameteredCast3() {
		tryToParse("(java.util.Hashtable<java.lang.String,java.util.List<java.lang.String>>)data.map",
				"(Hashtable<String,List<String>>)data.map", CastExpression.class, null, serviceManager, false);
	}

	@Test
	// Test with Ushr syntax
	public void testParameteredCast4() {
		tryToParse("(MyA<MyB<MyC<MyD>>>)a.path", "(MyA<MyB<MyC<MyD>>>)a.path", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testWilcardUpperBound() {
		tryToParse("(List<? extends Tutu>)toto", "(List<? extends Tutu>)toto", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testWilcardLowerBound() {
		tryToParse("(List<? extends Tutu>)toto", "(List<? extends Tutu>)toto", CastExpression.class, null, serviceManager, false);
	}

	@Test
	public void testWilcardUpperBounds() {
		tryToParse("(Map<? extends Key, ? extends Value>)toto", "(Map<? extends Key,? extends Value>)toto", CastExpression.class, null,
				serviceManager, false);
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

}
