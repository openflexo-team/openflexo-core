package org.openflexo.foundation.fml.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openflexo.foundation.fml.expr.FMLConstant.BooleanConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.CharConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.DoubleConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.FloatConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.IntegerConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.LongConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.ObjectSymbolicConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.StringConstant;
import org.openflexo.foundation.fml.expr.FMLUnaryOperatorExpression;

@RunWith(JUnit4.class)
public class TestLiteralParser extends ExpressionParserTestCase {

	@Test
	public void testNull() {
		tryToParse("null", "null", ObjectSymbolicConstant.class, null, serviceManager, false);
	}

	@Test
	public void testTrue() {
		tryToParse("true", "true", BooleanConstant.class, true, serviceManager, false);
	}

	@Test
	public void testFalse() {
		tryToParse("false", "false", BooleanConstant.class, false, serviceManager, false);
	}

	@Test
	public void testSimpleString() {
		tryToParse("\"aString\"", "\"aString\"", StringConstant.class, "aString", serviceManager, false);
	}

	@Test
	public void testSimpleCharacter() {
		tryToParse("'a'", "'a'", CharConstant.class, 'a', serviceManager, false);
	}

	@Test
	public void testSimpleInteger() {
		tryToParse("42", "42", IntegerConstant.class, 42, serviceManager, false);
	}

	@Test
	public void testLongIntegerWithFinalL() {
		tryToParse("42L", "42", LongConstant.class, 42L, serviceManager, false);
	}

	@Test
	public void testLongIntegerWithFinall() {
		tryToParse("42l", "42", LongConstant.class, 42L, serviceManager, false);
	}

	@Test
	public void testLongInteger() {
		tryToParse("4242424242242424242L", "4242424242242424242", LongConstant.class, 4242424242242424242L, serviceManager, false);
	}

	@Test
	public void testHexIntegerWithx() {
		tryToParse("0xFF", "255", IntegerConstant.class, 255, serviceManager, false);
	}

	@Test
	public void testHexIntegerWithX() {
		tryToParse("0xFE", "254", IntegerConstant.class, 254, serviceManager, false);
	}

	@Test
	public void testOctalInteger() {
		tryToParse("0123776", "43006", IntegerConstant.class, 0123776, serviceManager, false);
	}

	@Test
	public void testSimpleFloat() {
		tryToParse("3.1415", "3.1415", DoubleConstant.class, 3.1415, serviceManager, false);
	}

	@Test
	public void testSimpleFloatWithF() {
		tryToParse("3.1415F", null, FloatConstant.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleFloatWithf() {
		tryToParse("3.1415f", null, FloatConstant.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleDoubleWithD() {
		tryToParse("3.1415D", "3.1415", DoubleConstant.class, 3.1415D, serviceManager, false);
	}

	@Test
	public void testSimpleDoubleWithd() {
		tryToParse("3.1415d", "3.1415", DoubleConstant.class, 3.1415d, serviceManager, false);
	}

	@Test
	public void testExpNumber1() {
		tryToParse("1e42", "1.0E42", DoubleConstant.class, 1e42, serviceManager, false);
	}

	@Test
	public void testExpNumber2() {
		tryToParse("1.786e-42", "1.786E-42", DoubleConstant.class, 1.786e-42, serviceManager, false);
	}

	@Test
	public void testNumericValue1() {
		tryToParse("34", "34", IntegerConstant.class, 34, serviceManager, false);
	}

	@Test
	public void testNumericValue2() {
		tryToParse("7.8", "7.8", DoubleConstant.class, 7.8, serviceManager, false);
	}

	@Test
	public void testNumericValue3() {
		tryToParse("1.876E12", "1.876E12", DoubleConstant.class, 1.876E12, serviceManager, false);
	}

	@Test
	public void testNumericValue4() {
		tryToParse("0.876e-9", "8.76E-10", DoubleConstant.class, 8.76E-10, serviceManager, false);
	}

	@Test
	public void testNegativeInteger() {
		tryToParse("-89", "-89", FMLUnaryOperatorExpression.class, -89, serviceManager, false);
	}

	@Test
	public void testExplicitPositiveInteger() {
		tryToParse("+89", "89", FMLUnaryOperatorExpression.class, 89, serviceManager, false);
	}

	@Test
	public void testExplicitPositiveFloat() {
		tryToParse("+89.7856", "89.7856", FMLUnaryOperatorExpression.class, 89.7856, serviceManager, false);
	}

}
