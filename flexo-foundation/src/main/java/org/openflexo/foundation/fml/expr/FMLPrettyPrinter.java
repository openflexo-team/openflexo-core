/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.fml.expr;

import java.lang.reflect.Type;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.ContextualizedBindable;
import org.openflexo.connie.expr.BinaryOperatorExpression;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.expr.CastExpression;
import org.openflexo.connie.expr.ConditionalExpression;
import org.openflexo.connie.expr.Constant;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.expr.ExpressionGrammar;
import org.openflexo.connie.expr.ExpressionPrettyPrinter;
import org.openflexo.connie.expr.OperatorNotSupportedException;
import org.openflexo.connie.expr.UnaryOperatorExpression;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLGrammar;
import org.openflexo.foundation.fml.expr.FMLConstant.BooleanConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.ByteConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.CharConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.DoubleConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.EnumConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.FloatConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.IntegerConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.LongConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.ObjectConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.ObjectSymbolicConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.ShortConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.StringConstant;
import org.openflexo.foundation.fml.expr.FMLUnaryOperator.PostSettableUnaryOperator;

public class FMLPrettyPrinter extends ExpressionPrettyPrinter {

	private static final FMLPrettyPrinter instance = new FMLPrettyPrinter();

	// private StringEncoder.DateConverter dateConverter = new StringEncoder.DateConverter();
	// private Duration.DurationStringConverter durationConverter = new Duration.DurationStringConverter();

	public static FMLPrettyPrinter getInstance() {
		return instance;
	}

	public FMLPrettyPrinter() {
		this(new FMLGrammar());
	}

	protected FMLPrettyPrinter(ExpressionGrammar grammar) {
		super(grammar);
	}

	@Override
	public String getStringRepresentation(Expression expression, Bindable context) {
		if (expression instanceof FMLInstanceOfExpression) {
			return makeStringRepresentation((FMLInstanceOfExpression) expression, context);
		}
		return super.getStringRepresentation(expression, context);
	}

	@Override
	protected String makeStringRepresentation(Constant<?> constant, Bindable context) {
		if (constant instanceof BooleanConstant) {
			return makeStringRepresentation((BooleanConstant) constant);
		}
		else if (constant instanceof FloatConstant) {
			return makeStringRepresentation((FloatConstant) constant);
		}
		else if (constant instanceof DoubleConstant) {
			return makeStringRepresentation((DoubleConstant) constant);
		}
		else if (constant instanceof ByteConstant) {
			return makeStringRepresentation((ByteConstant) constant);
		}
		else if (constant instanceof ShortConstant) {
			return makeStringRepresentation((ShortConstant) constant);
		}
		else if (constant instanceof IntegerConstant) {
			return makeStringRepresentation((IntegerConstant) constant);
		}
		else if (constant instanceof LongConstant) {
			return makeStringRepresentation((LongConstant) constant);
		}
		else if (constant instanceof CharConstant) {
			return makeStringRepresentation((CharConstant) constant);
		}
		else if (constant instanceof StringConstant) {
			return makeStringRepresentation((StringConstant) constant);
		}
		else if (constant instanceof EnumConstant) {
			return makeStringRepresentation((EnumConstant<?>) constant);
		}
		else if (constant instanceof ObjectConstant) {
			return makeStringRepresentation((ObjectConstant) constant);
		}
		else if (constant == ObjectSymbolicConstant.NULL) {
			return "null";
		}
		return "???";
	}

	protected String makeStringRepresentation(BooleanConstant constant) {
		if (constant == BooleanConstant.FALSE) {
			return "false";
		}
		else if (constant == BooleanConstant.TRUE) {
			return "true";
		}
		return "???";
	}

	protected String makeStringRepresentation(FloatConstant constant) {
		return Float.toString(constant.getValue());
	}

	protected String makeStringRepresentation(DoubleConstant constant) {
		return Double.toString(constant.getValue());
	}

	protected String makeStringRepresentation(ByteConstant constant) {
		return Byte.toString(constant.getValue());
	}

	protected String makeStringRepresentation(ShortConstant constant) {
		return Short.toString(constant.getValue());
	}

	protected String makeStringRepresentation(IntegerConstant constant) {
		return Integer.toString(constant.getValue());
	}

	protected String makeStringRepresentation(LongConstant constant) {
		return Long.toString(constant.getValue());
	}

	protected String makeStringRepresentation(StringConstant constant) {
		return '"' + constant.getValue() + '"';
	}

	protected String makeStringRepresentation(CharConstant constant) {
		return "'" + constant.getValue() + "'";
	}

	@Override
	protected String makeStringRepresentation(UnaryOperatorExpression expression, Bindable context) {

		int currentPriority = expression.getPriority();
		int argPriority = expression.getArgument() != null ? expression.getArgument().getPriority() : -1;
		boolean parenthesisRequired = argPriority > currentPriority;

		if (expression.getOperator() instanceof PostSettableUnaryOperator) {
			try {
				return (parenthesisRequired ? "(" : "") + getStringRepresentation(expression.getArgument(), context)
						+ (parenthesisRequired ? ")" : "") + getSymbol(expression.getOperator());
			} catch (OperatorNotSupportedException e) {
				return "<unsupported>";
			}
		}
		try {
			return getSymbol(expression.getOperator()) + (parenthesisRequired ? "(" : "")
					+ getStringRepresentation(expression.getArgument(), context) + (parenthesisRequired ? ")" : "");
		} catch (OperatorNotSupportedException e) {
			return "<unsupported>";
		}
	}

	@Override
	protected String makeStringRepresentation(BinaryOperatorExpression expression, Bindable context) {

		// System.out.println(
		// "----> Prettyprint " + expression.getClass().getSimpleName() + " priority " + expression.getOperator().getPriority());

		try {
			int currentPriority = expression.getPriority();
			int leftPriority = expression.getLeftArgument() != null ? expression.getLeftArgument().getPriority() : -1;
			int rightPriority = expression.getRightArgument() != null ? expression.getRightArgument().getPriority() : -1;
			boolean parenthesisLeftRequired = leftPriority > currentPriority;
			boolean parenthesisRightRequired = rightPriority >= currentPriority;

			return (parenthesisLeftRequired ? "(" : "") + getStringRepresentation(expression.getLeftArgument(), context)
					+ (parenthesisLeftRequired ? ")" : "") + " " + getSymbol(expression.getOperator()) + " "
					+ (parenthesisRightRequired ? "(" : "") + getStringRepresentation(expression.getRightArgument(), context)
					+ (parenthesisRightRequired ? ")" : "");
		} catch (OperatorNotSupportedException e) {
			return "<unsupported>";
		}
	}

	protected final String makeStringRepresentation(EnumConstant<?> constant) {
		return constant.getName();
	}

	protected String makeStringRepresentation(ObjectConstant constant) {
		return "[Object:" + constant.getValue().toString() + "]";
	}

	@Override
	protected String makeStringRepresentation(BindingPath bv, Bindable context) {
		return bv.toString();
	}

	@Override
	protected String makeStringRepresentation(ConditionalExpression expression, Bindable context) {
		return "(" + getStringRepresentation(expression.getCondition(), context) + " ? "
				+ getStringRepresentation(expression.getThenExpression(), context) + " : "
				+ getStringRepresentation(expression.getElseExpression(), context) + ")";
	}

	@Override
	protected String makeStringRepresentation(CastExpression expression, Bindable context) {
		return "(" + makeStringRepresentation(expression.getCastType(), context) + ")"
				+ getStringRepresentation(expression.getArgument(), context);
	}

	private String makeStringRepresentation(FMLInstanceOfExpression expression, Bindable context) {
		return getStringRepresentation(expression.getArgument(), context) + " instanceof "
				+ makeStringRepresentation(expression.getType(), context);
	}

	@Override
	protected String makeStringRepresentation(Type type, Bindable aContext) {
		if (aContext instanceof ContextualizedBindable) {
			ContextualizedBindable context = (ContextualizedBindable) aContext;
			if (!context.isTypeImported(type)) {
				context.importType(type);
			}
		}
		return TypeUtils.simpleRepresentation(type);
	}
}
