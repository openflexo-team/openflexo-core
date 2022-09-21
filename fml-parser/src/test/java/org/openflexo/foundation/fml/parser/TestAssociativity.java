package org.openflexo.foundation.fml.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openflexo.connie.expr.BinaryOperatorExpression;
import org.openflexo.connie.expr.UnaryOperatorExpression;

@RunWith(JUnit4.class)
public class TestAssociativity extends ExpressionParserTestCase {

	@Test
	public void testAssociativity() {
		tryToParse("a+b*c", "a + b * c", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testAssociativity2() {
		tryToParse("a+b+c", "a + b + c", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testAssociativity3() {
		tryToParse("a+(b+c)", "a + (b + c)", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testAssociativity4() {
		tryToParse("(a+b)+c", "a + b + c", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testAssociativity10() {
		tryToParse("(a+b)*c", "(a + b) * c", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testAssociativity11() {
		tryToParse("a*b+c", "a * b + c", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	@Test
	public void testAssociativity20() {
		tryToParse("-a+b", "-a + b", BinaryOperatorExpression.class, null, serviceManager, false);
	}

	public void testAssociativity21() {
		tryToParse("-(a+b)", "-(a + b)", UnaryOperatorExpression.class, null, serviceManager, false);
	}

}
