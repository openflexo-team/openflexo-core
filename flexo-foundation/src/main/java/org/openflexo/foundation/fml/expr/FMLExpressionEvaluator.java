/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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
import org.openflexo.connie.exception.InvalidCastException;
import org.openflexo.connie.exception.TransformException;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.expr.Constant;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.expr.FMLConstant.BooleanConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.ObjectSymbolicConstant;

/**
 * This {@link FMLExpressionEvaluator} is used to evaluate Java expressions
 * 
 * @author sylvain
 * 
 */
public class FMLExpressionEvaluator extends ExpressionEvaluator {

	public FMLExpressionEvaluator(BindingEvaluationContext context) {
		super(context);
	}

	/**
	 * Performs the transformation of a resulting expression e, asserting that all contained expressions have already been transformed (this
	 * method is not recursive, to do so, use Expression.transform(ExpressionTransformer) API)
	 */
	@Override
	public Expression performTransformation(Expression e) throws TransformException {
		if (e instanceof BindingPath) {
			if (((BindingPath) e).isValid()) {
				Object o = ((BindingPath) e).getBindingValue(getContext());
				return FMLConstant.makeConstant(o);
			}
			return e;
		}
		else if (e instanceof FMLConditionalExpression) {
			return transformConditionalExpression((FMLConditionalExpression) e);
		}
		else if (e instanceof FMLInstanceOfExpression) {
			return transformInstanceOfExpression((FMLInstanceOfExpression) e);
		}
		else if (e instanceof FMLCastExpression) {
			return transformCastExpression((FMLCastExpression) e);
		}
		return super.performTransformation(e);
	}

	private static Expression transformConditionalExpression(FMLConditionalExpression e) {
		if (e.getCondition() == BooleanConstant.TRUE) {
			return e.getThenExpression();
		}
		else if (e.getCondition() == BooleanConstant.FALSE) {
			return e.getElseExpression();
		}
		return e;
	}

	private static Expression transformInstanceOfExpression(FMLInstanceOfExpression e) {

		if (e.getArgument() == ObjectSymbolicConstant.NULL) {
			return BooleanConstant.FALSE;
		}

		if (e.getArgument() instanceof Constant) {
			if (TypeUtils.isOfType(((Constant<?>) e.getArgument()).getValue(), e.getType())) {
				return BooleanConstant.TRUE;
			}
			else {
				return BooleanConstant.FALSE;
			}
		}
		return e;
	}

	private static Expression transformCastExpression(FMLCastExpression e) throws InvalidCastException {

		if (e.getArgument() == ObjectSymbolicConstant.NULL) {
			return ObjectSymbolicConstant.NULL;
		}

		if (e.getArgument() instanceof Constant) {
			if (TypeUtils.isOfType(((Constant<?>) e.getArgument()).getValue(), e.getCastType())) {
				return e.getArgument();
			}
			else {
				throw new InvalidCastException(e.getCastType(), ((Constant<?>) e.getArgument()).getValue().getClass());
			}
		}
		return e;
	}

}
