package org.openflexo.foundation.fml.cli.command.fml;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.TransformException;
import org.openflexo.connie.expr.BinaryOperatorExpression;
import org.openflexo.connie.expr.OperatorNotSupportedException;
import org.openflexo.foundation.fml.cli.AbstractCommandInterpreter;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.expr.FMLConstant;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;
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
	private int line;
	private String message;

	public FMLAssertException(int line, DataBinding<?> expression, AbstractCommandInterpreter commandInterpreter) {
		super("Line " + line + ": assert failed: " + expression);
		this.line = line;
		if (expression.getExpression() instanceof BinaryOperatorExpression) {
			// In this case, we try to get a more explicit message
			try {
				binOp = (BinaryOperatorExpression) expression.getExpression();
				leftV = binOp.getLeftArgument().transform(new FMLExpressionEvaluator(commandInterpreter));
				rightV = binOp.getRightArgument().transform(new FMLExpressionEvaluator(commandInterpreter));
			} catch (TransformException e) {
				e.printStackTrace();
			}

			String leftMessage = binOp.getLeftArgument().toString();
			if (!(binOp.getLeftArgument() instanceof FMLConstant)) {
				leftMessage += "[" + leftV + "]";
			}
			String rightMessage = binOp.getRightArgument().toString();
			if (!(binOp.getRightArgument() instanceof FMLConstant)) {
				rightMessage += "[" + rightV + "]";
			}

			try {
				message = "Line " + line + ": assert failed: " + leftMessage + " "
						+ FMLPrettyPrinter.getInstance().getGrammar().getSymbol(binOp.getOperator()) + " " + rightMessage;
			} catch (OperatorNotSupportedException e) {
				message = "Line " + line + ": assert failed: " + expression;
				e.printStackTrace();
			}

		}
	}

	@Override
	public String getMessage() {
		return message;
	}

}
