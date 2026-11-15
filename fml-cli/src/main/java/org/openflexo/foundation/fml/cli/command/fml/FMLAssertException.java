package org.openflexo.foundation.fml.cli.command.fml;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.TransformException;
import org.openflexo.connie.expr.BinaryOperatorExpression;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.expr.OperatorNotSupportedException;
import org.openflexo.foundation.fml.cli.AbstractCommandInterpreter;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.expr.FMLConstant;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;
import org.openflexo.foundation.fml.expr.FMLPrettyPrinter;

/**
 * This is an exception which is thrown when an assert has failed: its expression evaluates to false or to null. Its message shows the
 * expression, and for a binary expression the values of its operands (CORE-F-11)
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FMLAssertException extends FMLCommandExecutionException {

	private final DataBinding<?> expression;

	public FMLAssertException(int line, DataBinding<?> expression, AbstractCommandInterpreter commandInterpreter) {
		super(buildMessage(expression, commandInterpreter));
		this.expression = expression;
		if (line >= 0) {
			setLine(line);
		}
	}

	public DataBinding<?> getExpression() {
		return expression;
	}

	private static String buildMessage(DataBinding<?> expression, AbstractCommandInterpreter commandInterpreter) {
		String plain = "assert failed: " + expression;
		if (!(expression.getExpression() instanceof BinaryOperatorExpression)) {
			return plain;
		}
		// For a binary expression, a more explicit message, with the values of the operands
		BinaryOperatorExpression binOp = (BinaryOperatorExpression) expression.getExpression();
		try {
			return "assert failed: " + operand(binOp.getLeftArgument(), commandInterpreter) + " "
					+ FMLPrettyPrinter.getInstance().getGrammar().getSymbol(binOp.getOperator()) + " "
					+ operand(binOp.getRightArgument(), commandInterpreter);
		} catch (OperatorNotSupportedException e) {
			return plain;
		}
	}

	/** Text of supplied operand, with its value unless it is a constant */
	private static String operand(Expression operand, AbstractCommandInterpreter commandInterpreter) {
		if (operand instanceof FMLConstant) {
			return operand.toString();
		}
		String value;
		try {
			value = String.valueOf(operand.transform(new FMLExpressionEvaluator(commandInterpreter)));
		} catch (TransformException | RuntimeException e) {
			// Only the message of the assertion: its failure is the assertion itself
			value = "?";
		}
		return operand + "[" + value + "]";
	}

}
