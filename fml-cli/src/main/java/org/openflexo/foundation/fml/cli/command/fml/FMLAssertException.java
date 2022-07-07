package org.openflexo.foundation.fml.cli.command.fml;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.TransformException;
import org.openflexo.connie.expr.BinaryOperatorExpression;
import org.openflexo.connie.expr.OperatorNotSupportedException;
import org.openflexo.connie.java.expr.JavaExpressionEvaluator;
import org.openflexo.foundation.fml.cli.AbstractCommandInterpreter;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.expr.FMLPrettyPrinter;

/**
 * This is an exception which is thrown when an assert has failed
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FMLAssertException extends FMLCommandExecutionException {

	private DataBinding<?> expression;

	private BinaryOperatorExpression binOp;
	private Object leftV;
	private Object rightV;

	public FMLAssertException(DataBinding<?> expression, AbstractCommandInterpreter commandInterpreter) {
		super("Assert failed: " + expression);
		if (expression.getExpression() instanceof BinaryOperatorExpression) {
			// In this case, we try to get a more explicit message
			try {
				binOp = (BinaryOperatorExpression) expression.getExpression();
				leftV = binOp.getLeftArgument().transform(new JavaExpressionEvaluator(commandInterpreter));
				rightV = binOp.getRightArgument().transform(new JavaExpressionEvaluator(commandInterpreter));
			} catch (TransformException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getMessage() {
		if (binOp != null) {
			try {
				return "Assert failed: " + leftV + " " + FMLPrettyPrinter.getInstance().getGrammar().getSymbol(binOp.getOperator()) + " "
						+ rightV;
			} catch (OperatorNotSupportedException e) {
				e.printStackTrace();
			}
		}
		return super.getMessage();
	}

}
