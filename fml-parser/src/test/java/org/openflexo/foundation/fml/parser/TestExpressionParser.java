package org.openflexo.foundation.fml.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openflexo.connie.expr.BinaryOperatorExpression;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.expr.ConditionalExpression;
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.fml.expr.FMLBinaryOperatorExpression;
import org.openflexo.foundation.fml.expr.FMLBooleanBinaryOperator;
import org.openflexo.foundation.fml.expr.FMLConditionalExpression;
import org.openflexo.foundation.fml.expr.FMLConstant.BooleanConstant;
import org.openflexo.foundation.fml.expr.FMLUnaryOperatorExpression;

@RunWith(JUnit4.class)
public class TestExpressionParser extends ExpressionParserTestCase {

	// Test Conditional

	@Test
	public void testSimpleConditional() {
		tryToParse("1 < 2 ? true : false", "true", FMLConditionalExpression.class, true, serviceManager, false);
	}

	@Test
	public void testSymbolicConditional() {
		tryToParse("a > b ? c : d", "(a > b ? c : d)", FMLConditionalExpression.class, null, serviceManager, false);
	}

	// Test comparison

	@Test
	public void testSimpleEq() {
		tryToParse("2 == 2 ? true : false", "true", FMLConditionalExpression.class, true, serviceManager, false);
	}

	@Test
	public void testSymbolicEq() {
		tryToParse("a == b", "a == b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleNeq() {
		tryToParse("1 != 2 ? true : false", "true", FMLConditionalExpression.class, true, serviceManager, false);
	}

	@Test
	public void testSymbolicNeq() {
		tryToParse("a != b", "a != b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleLt() {
		tryToParse("1 < 2 ? true : false", "true", FMLConditionalExpression.class, true, serviceManager, false);
	}

	@Test
	public void testSymbolicLt() {
		tryToParse("a < b", "a < b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleGt() {
		tryToParse("2 > 1 ? true : false", "true", FMLConditionalExpression.class, true, serviceManager, false);
	}

	@Test
	public void testSymbolicGt() {
		tryToParse("a > b", "a > b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleLtEq() {
		tryToParse("2 <= 2 ? true : false", "true", FMLConditionalExpression.class, true, serviceManager, false);
	}

	@Test
	public void testSymbolicLtEq() {
		tryToParse("a <= b", "a <= b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleGtEq() {
		tryToParse("1 >= 1 ? true : false", "true", FMLConditionalExpression.class, true, serviceManager, false);
	}

	@Test
	public void testSymbolicGtEq() {
		tryToParse("a >= b", "a >= b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleNumberAddition() {
		tryToParse("1+1", "2", FMLBinaryOperatorExpression.class, 2, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicAddition() {
		tryToParse("a+b", "a + b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleSubstraction() {
		tryToParse("7-8", "-1", FMLBinaryOperatorExpression.class, -1, serviceManager, false);
	}

	@Test
	public void testSymbolicEquals() {
		tryToParse("a==b", "a == b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleEquals() {
		tryToParse("1==2", "false", FMLBinaryOperatorExpression.class, false, serviceManager, false);
	}

	@Test
	public void testSymbolicShiftLeft() {
		tryToParse("a<<b", "a << b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleShiftLeft1() {
		tryToParse("1<<2", "4", FMLBinaryOperatorExpression.class, 1 << 2, serviceManager, false);
	}

	@Test
	public void testSimpleShiftLeft2() {
		tryToParse("109675<<265", "56153600", FMLBinaryOperatorExpression.class, 109675 << 265, serviceManager, false);
	}

	@Test
	public void testSymbolicShiftRight() {
		tryToParse("a>>b", "a >> b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleShiftRight1() {
		tryToParse("1>>3", "0", FMLBinaryOperatorExpression.class, 1 >> 3, serviceManager, false);
	}

	@Test
	public void testSimpleShiftRight2() {
		tryToParse("256>>3", "32", FMLBinaryOperatorExpression.class, 256 >> 3, serviceManager, false);
	}

	@Test
	public void testSymbolicUShiftRight() {
		tryToParse("a>>>b", "a >>> b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleUShiftRight1() {
		tryToParse("1>>>3", "0", FMLBinaryOperatorExpression.class, 1 >>> 3, serviceManager, false);
	}

	@Test
	public void testSimpleUShiftRight2() {
		tryToParse("256>>>3", "32", FMLBinaryOperatorExpression.class, 256 >>> 3, serviceManager, false);
	}

	@Test
	public void testSimpleIntegerMultiplication() {
		tryToParse("2*2", "4", FMLBinaryOperatorExpression.class, 4, serviceManager, false);
	}

	@Test
	public void testSimpleDoubleMultiplication() {
		tryToParse("3.1415*3.1415", "9.86902225", FMLBinaryOperatorExpression.class, 3.1415 * 3.1415, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicMultiplication() {
		tryToParse("a*b", "a * b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleIntegerDivision() {
		tryToParse("27/3", "9.0", FMLBinaryOperatorExpression.class, 9.0, serviceManager, false);
	}

	@Test
	public void testSimpleDoubleDivision() {
		tryToParse("3.1415/2.1", "1.495952380952381", FMLBinaryOperatorExpression.class, 3.1415 / 2.1, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicDivision() {
		tryToParse("a/b", "a / b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleModDivision() {
		tryToParse("32%3", "2.0", FMLBinaryOperatorExpression.class, 2.0, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicModDivision() {
		tryToParse("a%b", "a % b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicPreIncrement() {
		tryToParse("++a", "++a", FMLUnaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicPreIncrement2() {
		tryToParse("++a.b", "++a.b", FMLUnaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicPreDecrement() {
		tryToParse("--a", "--a", FMLUnaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicPreDecrement2() {
		tryToParse("--a.b", "--a.b", FMLUnaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicPostIncrement() {
		tryToParse("a++", "a++", FMLUnaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicPostIncrement2() {
		tryToParse("a.b++", "a.b++", FMLUnaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicPostDecrement() {
		tryToParse("a--", "a--", FMLUnaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicPostDecrement2() {
		tryToParse("a.b--", "a.b--", FMLUnaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicBitwiseComplement() {
		tryToParse("~a", "~a", FMLUnaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleBitwiseComplement() {
		tryToParse("~123", "-124", FMLUnaryOperatorExpression.class, ~123, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicNot() {
		tryToParse("!a", "!a", FMLUnaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleNot() {
		tryToParse("!true", "false", FMLUnaryOperatorExpression.class, false, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicBitwiseAnd() {
		tryToParse("a & b", "a & b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleBitwiseAnd() {
		tryToParse("255 & 13", "13", FMLBinaryOperatorExpression.class, 255 & 13, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicBitwiseOr() {
		tryToParse("a | b", "a | b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleBitwiseOr() {
		tryToParse("128 | 15", "143", FMLBinaryOperatorExpression.class, 128 | 15, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicBitwiseXOr() {
		tryToParse("a ^ b", "a ^ b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleBitwiseXOr() {
		tryToParse("8 ^ 15", "7", FMLBinaryOperatorExpression.class, 8 ^ 15, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicOr() {
		tryToParse("a || b", "a || b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleOr() {
		tryToParse("(1<2)||(2<1)", "true", FMLBinaryOperatorExpression.class, true, serviceManager, false);
	}

	@Test
	public void testSimpleSymbolicAnd() {
		tryToParse("a && b", "a && b", FMLBinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testSimpleAnd() {
		tryToParse("(1<2)&&(2<1)", "false", FMLBinaryOperatorExpression.class, false, serviceManager, false);
	}

	@Test
	public void testNumericValue8() {
		tryToParse("1+(2*7-9)", "6", BinaryOperatorExpression.class, 6, serviceManager, false);
	}

	@Test
	public void testNumericValue9() {
		tryToParse("1+((298*7.1e-3)-9)", "-5.8842", BinaryOperatorExpression.class, -5.8842, serviceManager, false);
	}

	@Test
	public void testStringExpression() {
		tryToParse("\"foo1\"+\"foo2\"", "\"foo1foo2\"", BinaryOperatorExpression.class, "foo1foo2", serviceManager, false);
	}

	@Test
	public void testExpression1() {
		tryToParse("machin+1", "machin + 1", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testExpression2() {
		tryToParse("machin+1*6-8/7+bidule", "machin + 6 - 1.1428571428571428 + bidule", BinaryOperatorExpression.class, null,
				serviceManager, false);
	}

	@Test
	public void testExpression3() {
		tryToParse("7-x-(-x-6-8*2)", "7 - x - (-x - 6 - 16)", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testExpression4() {
		tryToParse("1+function(test,4<7-x)", "1 + function(test,4 < 7 - x)", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testEquality() {
		Expression e = tryToParse("a==b", "a == b", BinaryOperatorExpression.class, null, serviceManager, false);
		assertEquals(FMLBooleanBinaryOperator.EQUALS, ((BinaryOperatorExpression) e).getOperator());
	}

	@Test
	public void testEquality2() {
		tryToParse("binding1.a.b == binding2.a.b*7", "binding1.a.b == binding2.a.b * 7", BinaryOperatorExpression.class, null,
				serviceManager, false);
	}

	@Test
	public void testBoolean1() {
		tryToParse("false", "false", BooleanConstant.FALSE.getClass(), false, serviceManager, false);
	}

	@Test
	public void testBoolean2() {
		tryToParse("true", "true", BooleanConstant.TRUE.getClass(), true, serviceManager, false);
	}

	@Test
	public void testBoolean3() {
		tryToParse("false && true", "false", BinaryOperatorExpression.class, false, serviceManager, false);
	}

	@Test
	public void testBooleanExpression1() {
		tryToParse("!a&&b", "!a && b", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testImbricatedCall() {
		tryToParse("function1(function2(8+1,9,10-1))", "function1(function2(9,9,9))", BindingPath.class, null, serviceManager, false);
	}

	@Test
	public void testEmptyCall() {
		tryToParse("function1()", "function1()", BindingPath.class, null, serviceManager, false);
	}

	@Test
	public void testComplexBooleanExpression() {
		tryToParse("a && (c || d && (!f)) ||b", "a && (c || d && !f) || b", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testArithmeticNumberComparison1() {
		tryToParse("1 < 2", "true", BinaryOperatorExpression.class, true, serviceManager, false);
	}

	@Test
	public void testArithmeticNumberComparison2() {
		tryToParse("0.1109 < 1.1108E-03", "false", BinaryOperatorExpression.class, false, serviceManager, false);
	}

	@Test
	public void testStringConcatenation() {
		tryToParse("\"a + ( 2 + b )\"+2", "\"a + ( 2 + b )2\"", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testParsingError1() {
		tryToParse("a\"b", "", null, null, serviceManager, true);
	}

	@Test
	public void testParsingError2() {
		tryToParse("a'b", "", null, null, serviceManager, true);
	}

	@Test
	public void testParsingError3() {
		tryToParse("\"", "", null, null, serviceManager, true);
	}

	@Test
	public void testParsingError4() {
		tryToParse("test23 ( fdfd + 1", "", null, null, serviceManager, true);
	}

	@Test
	public void testParsingError5() {
		tryToParse("test24 [ fdfd + 1", "", null, null, serviceManager, true);
	}

	@Test
	public void testParsingError6() {
		tryToParse("obj..f()", "", null, null, serviceManager, true);
	}

	@Test
	public void testIgnoredChars() {
		tryToParse(" test  \n\n", "test", BindingPath.class, null, serviceManager, false);
	}

	// Test conditionals

	@Test
	public void testConditional1() {
		tryToParse("a?b:c", "(a ? b : c)", ConditionalExpression.class, null, serviceManager, false);
	}

	@Test
	public void testConditional2() {
		tryToParse("a > 9 ?true:false", "(a > 9 ? true : false)", ConditionalExpression.class, null, serviceManager, false);
	}

	@Test
	public void testConditional3() {
		tryToParse("a+1 > 10-7 ?8+4:5", "(a + 1 > 3 ? 12 : 5)", ConditionalExpression.class, null, serviceManager, false);
	}

	@Test
	public void testConditional4() {
		tryToParse("a+1 > (a?1:2) ?8+4:5", "(a + 1 > ((a ? 1 : 2)) ? 12 : 5)", ConditionalExpression.class, null, serviceManager, false);
	}

	@Test
	public void testConditional5() {
		tryToParse("2 < 3 ? 4:2", "4", ConditionalExpression.class, 4, serviceManager, false);
	}

	@Test
	public void testConditional6() {
		tryToParse("2 > 3 ? 4:2", "2", ConditionalExpression.class, 2, serviceManager, false);
	}

	@Test
	public void testInvalidConditional() {
		tryToParse("2 > 3 ? 3", "", ConditionalExpression.class, null, serviceManager, true);
	}

}
