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

import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.Constant;
import org.openflexo.connie.expr.EvaluationType;
import org.openflexo.connie.expr.Expression;

// TODO: do other assign operators
public abstract class FMLAssignOperator extends FMLBooleanBinaryOperator {

	@Override
	public int getPriority() {
		return 15;
	}

	public static final FMLAssignOperator ASSIGN = new FMLAssignOperator() {
		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			return rightArg;
		}

		@Override
		public String getName() {
			return "assign";
		}

		@Override
		public Expression evaluate(Expression leftArg, Constant<?> rightArg) {
			return rightArg;
		}

		@Override
		public EvaluationType getEvaluationType(EvaluationType leftOperandType, EvaluationType rightOperandType)
				throws TypeMismatchException {
			return rightOperandType;
		}

	};

	public static final FMLAssignOperator PLUS_ASSIGN = new FMLAssignOperator() {
		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			return FMLArithmeticBinaryOperator.ADDITION.evaluate(leftArg, rightArg);
		}

		@Override
		public String getName() {
			return "plus_assign";
		}

		@Override
		public Expression evaluate(Expression leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			return FMLArithmeticBinaryOperator.ADDITION.evaluate(leftArg, rightArg);
		}

		@Override
		public EvaluationType getEvaluationType(EvaluationType leftOperandType, EvaluationType rightOperandType)
				throws TypeMismatchException {
			return FMLArithmeticBinaryOperator.ADDITION.getEvaluationType(leftOperandType, rightOperandType);
		}

	};

}
