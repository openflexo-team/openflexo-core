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

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.exception.NotSettableContextException;
import org.openflexo.connie.exception.TransformException;
import org.openflexo.connie.expr.BinaryOperator;
import org.openflexo.connie.expr.BinaryOperatorExpression;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.expr.Constant;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.expr.ExpressionPrettyPrinter;
import org.openflexo.connie.expr.ExpressionTransformer;

public class FMLBinaryOperatorExpression extends BinaryOperatorExpression {

	public FMLBinaryOperatorExpression(BinaryOperator operator, Expression leftArgument, Expression rightArgument) {
		super(operator, leftArgument, rightArgument);
	}

	@Override
	public ExpressionPrettyPrinter getPrettyPrinter() {
		return FMLPrettyPrinter.getInstance();
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) throws TransformException {

		Expression expression = this;
		Expression transformedLeftArgument = getLeftArgument().transform(transformer);
		Expression transformedRightArgument = getRightArgument().transform(transformer);

		if (!transformedLeftArgument.equals(getLeftArgument()) || !transformedRightArgument.equals(getRightArgument())) {
			expression = new FMLBinaryOperatorExpression(getOperator(), transformedLeftArgument, transformedRightArgument);
		}

		Expression returned = transformer.performTransformation(expression);

		// In case of assign operator, assign the resulted value
		if (getOperator() instanceof FMLAssignOperator && returned instanceof Constant) {
			if (transformer instanceof FMLExpressionEvaluator) {
				BindingEvaluationContext context = ((FMLExpressionEvaluator) transformer).getContext();
				if (!getLeftArgument().isSettable()) {
					throw new NotSettableContextException(
							"Invalid context for FMLAssignOperator, not settable argument : " + getLeftArgument());
				}
				else {
					Object value = ((Constant<?>) returned).getValue();
					if (getLeftArgument() instanceof BindingPath) {
						((BindingPath) getLeftArgument()).setBindingValue(value, context);
					}
					else {
						throw new NotSettableContextException(
								"Invalid context for FMLAssignOperator, don't know how to set for argument : " + getLeftArgument());
					}
				}
			}
			else {
				throw new NotSettableContextException("Invalid context for PostSettableUnaryOperator : " + transformer);
			}
		}

		return returned;

	}

}
