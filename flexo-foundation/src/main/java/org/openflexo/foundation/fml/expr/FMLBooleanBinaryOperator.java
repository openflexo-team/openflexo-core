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
import org.openflexo.connie.java.expr.JavaConstant.ArithmeticConstant;
import org.openflexo.connie.java.expr.JavaConstant.BooleanConstant;
import org.openflexo.connie.java.expr.JavaConstant.EnumConstant;
import org.openflexo.connie.java.expr.JavaConstant.ObjectConstant;
import org.openflexo.connie.java.expr.JavaConstant.ObjectSymbolicConstant;
import org.openflexo.connie.java.expr.JavaConstant.StringConstant;

public abstract class FMLBooleanBinaryOperator extends FMLBinaryOperator {

	public static final FMLBooleanBinaryOperator AND = new LogicalBinaryOperator() {
		@Override
		public int getPriority() {
			return 12;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg.equals(ObjectSymbolicConstant.NULL) || rightArg.equals(ObjectSymbolicConstant.NULL)) {
				throw new NullReferenceException(this);
			}
			if (leftArg instanceof BooleanConstant && rightArg instanceof BooleanConstant) {
				return BooleanConstant.get(((BooleanConstant) leftArg).getValue() && ((BooleanConstant) rightArg).getValue());
			}
			if (leftArg instanceof BooleanConstant && !((BooleanConstant) leftArg).getValue()) {
				return BooleanConstant.get(false);
			}
			if (rightArg instanceof BooleanConstant && !((BooleanConstant) rightArg).getValue()) {
				return BooleanConstant.get(false);
			}
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(), EvaluationType.BOOLEAN);
		}

		@Override
		public String getName() {
			return "logical_and";
		}

		@Override
		public Expression evaluate(Expression leftArg, Constant<?> rightArg) {
			if (rightArg == BooleanConstant.FALSE) {
				return BooleanConstant.FALSE;
			}
			if (rightArg == BooleanConstant.TRUE) {
				return leftArg;
			}
			return super.evaluate(leftArg, rightArg);
		}

		@Override
		public Expression evaluate(Constant<?> leftArg, Expression rightArg) {
			if (leftArg == BooleanConstant.FALSE) {
				return BooleanConstant.FALSE;
			}
			if (leftArg == BooleanConstant.TRUE) {
				return rightArg;
			}
			return super.evaluate(leftArg, rightArg);
		}
	};

	public static final FMLBooleanBinaryOperator OR = new LogicalBinaryOperator() {
		@Override
		public int getPriority() {
			return 13;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg.equals(ObjectSymbolicConstant.NULL) || rightArg.equals(ObjectSymbolicConstant.NULL)) {
				throw new NullReferenceException(this);
			}
			if (leftArg instanceof BooleanConstant && rightArg instanceof BooleanConstant) {
				return BooleanConstant.get(((BooleanConstant) leftArg).getValue() || ((BooleanConstant) rightArg).getValue());
			}
			if (leftArg instanceof BooleanConstant && ((BooleanConstant) leftArg).getValue()) {
				return BooleanConstant.get(true);
			}
			if (rightArg instanceof BooleanConstant && ((BooleanConstant) rightArg).getValue()) {
				return BooleanConstant.get(true);
			}
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(), EvaluationType.BOOLEAN);
		}

		@Override
		public String getName() {
			return "logical_or";
		}

		@Override
		public Expression evaluate(Expression leftArg, Constant<?> rightArg) {
			if (rightArg == BooleanConstant.FALSE) {
				return leftArg;
			}
			if (rightArg == BooleanConstant.TRUE) {
				return BooleanConstant.TRUE;
			}
			return super.evaluate(leftArg, rightArg);
		}

		@Override
		public Expression evaluate(Constant<?> leftArg, Expression rightArg) {
			if (leftArg == BooleanConstant.FALSE) {
				return rightArg;
			}
			if (leftArg == BooleanConstant.TRUE) {
				return BooleanConstant.TRUE;
			}
			return super.evaluate(leftArg, rightArg);
		}
	};

	public static final FMLBooleanBinaryOperator EQUALS = new ComparisonBinaryOperator() {
		@Override
		public int getPriority() {
			return 8;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException {
			//System.out.println("leftArg=" + leftArg + " of " + leftArg.getEvaluationType());
			//System.out.println("rightArg=" + rightArg + " of " + rightArg.getEvaluationType());
			if (leftArg instanceof ObjectConstant && rightArg instanceof ObjectConstant) {
				return ((ObjectConstant) leftArg).getValue().equals(((ObjectConstant) rightArg).getValue()) ? BooleanConstant.TRUE
						: BooleanConstant.FALSE;
			}
			if (leftArg instanceof BooleanConstant && rightArg instanceof BooleanConstant) {
				return ((BooleanConstant) leftArg).getValue() == ((BooleanConstant) rightArg).getValue() ? BooleanConstant.TRUE
						: BooleanConstant.FALSE;
			}
			if (leftArg instanceof ArithmeticConstant && rightArg instanceof ArithmeticConstant) {
				// TODO: we may work with all primitive types
				if (!((ArithmeticConstant) leftArg).isFloatingPointType() && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
					return ((ArithmeticConstant) leftArg).getLongValue() == ((ArithmeticConstant) rightArg).getLongValue()
							? BooleanConstant.TRUE
							: BooleanConstant.FALSE;
				}
				else {
					return ((ArithmeticConstant) leftArg).getDoubleValue() == ((ArithmeticConstant) rightArg).getDoubleValue()
							? BooleanConstant.TRUE
							: BooleanConstant.FALSE;
				}
			}
			if (leftArg instanceof StringConstant && rightArg instanceof StringConstant) {
				return ((StringConstant) leftArg).getValue().equals(((StringConstant) rightArg).getValue()) ? BooleanConstant.TRUE
						: BooleanConstant.FALSE;
			}
			if (leftArg instanceof EnumConstant && rightArg instanceof EnumConstant) {
				return ((EnumConstant<?>) leftArg).getName().equals(((EnumConstant<?>) rightArg).getName()) ? BooleanConstant.TRUE
						: BooleanConstant.FALSE;
			}
			if (leftArg instanceof EnumConstant && rightArg instanceof StringConstant) {
				return ((EnumConstant<?>) leftArg).getName().equals(((StringConstant) rightArg).getValue()) ? BooleanConstant.TRUE
						: BooleanConstant.FALSE;
			}
			// System.out.println("leftArg="+leftArg+" of "+leftArg.getEvaluationType());
			// System.out.println("rightArg="+rightArg+" of "+rightArg.getEvaluationType());
			if (rightArg == ObjectSymbolicConstant.NULL) {
				if (leftArg == ObjectSymbolicConstant.NULL) {
					return BooleanConstant.TRUE;
				}
				if (leftArg instanceof StringConstant && ((StringConstant) leftArg).getValue().equals("null")) {
					return BooleanConstant.TRUE;
				}
				return BooleanConstant.FALSE;
			}
			if (leftArg == ObjectSymbolicConstant.NULL) {
				if (rightArg == ObjectSymbolicConstant.NULL) {
					return BooleanConstant.TRUE;
				}
				if (rightArg instanceof StringConstant && ((StringConstant) rightArg).getValue().equals("null")) {
					return BooleanConstant.TRUE;
				}
				return BooleanConstant.FALSE;
			}
			// System.out.println("leftArg=" + leftArg + " of " + leftArg.getClass().getSimpleName() + " of " +
			// leftArg.getEvaluationType());
			// System.out.println("rightArg=" + rightArg + " of " + rightArg.getClass().getSimpleName() + " of "
			// + rightArg.getEvaluationType());
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(), EvaluationType.values());
		}

		@Override
		public String getName() {
			return "equals_operator";
		}

		@Override
		public EvaluationType getEvaluationType(EvaluationType leftOperandType, EvaluationType rightOperandType)
				throws TypeMismatchException {
			if (leftOperandType.isStringOrLiteral() && rightOperandType.isStringOrLiteral()) {
				return EvaluationType.BOOLEAN;
			}
			if (leftOperandType.isBooleanOrLiteral() && rightOperandType.isBooleanOrLiteral()) {
				return EvaluationType.BOOLEAN;
			}
			if (leftOperandType.isEnumOrLiteral() && rightOperandType.isEnumOrLiteral()) {
				return EvaluationType.BOOLEAN;
			}
			return super.getEvaluationType(leftOperandType, rightOperandType);
		}

	};

	public static final FMLBooleanBinaryOperator NOT_EQUALS = new ComparisonBinaryOperator() {
		@Override
		public int getPriority() {
			return 8;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			// TODO catch exception and replace EQUALS by NOT_EQUALS (not very important but who knows)
			return EQUALS.evaluate(leftArg, rightArg) == BooleanConstant.FALSE ? BooleanConstant.TRUE : BooleanConstant.FALSE;
		}

		@Override
		public String getName() {
			return "not_equals_operator";
		}

		@Override
		public EvaluationType getEvaluationType(EvaluationType leftOperandType, EvaluationType rightOperandType)
				throws TypeMismatchException {
			if (leftOperandType.isStringOrLiteral() && rightOperandType.isStringOrLiteral()) {
				return EvaluationType.BOOLEAN;
			}
			if (leftOperandType.isBooleanOrLiteral() && rightOperandType.isBooleanOrLiteral()) {
				return EvaluationType.BOOLEAN;
			}
			if (leftOperandType.isEnumOrLiteral() && rightOperandType.isEnumOrLiteral()) {
				return EvaluationType.BOOLEAN;
			}
			return super.getEvaluationType(leftOperandType, rightOperandType);
		}
	};

	public static final FMLBooleanBinaryOperator LESS_THAN = new ComparisonBinaryOperator() {
		@Override
		public int getPriority() {
			return 7;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg.equals(ObjectSymbolicConstant.NULL) || rightArg.equals(ObjectSymbolicConstant.NULL)) {
				throw new NullReferenceException(this);
			}
			if (leftArg instanceof ArithmeticConstant && rightArg instanceof ArithmeticConstant) {
				if (!((ArithmeticConstant) leftArg).isFloatingPointType() && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
					return ((ArithmeticConstant) leftArg).getLongValue() < ((ArithmeticConstant) rightArg).getLongValue()
							? BooleanConstant.TRUE
							: BooleanConstant.FALSE;
				}
				else {
					return ((ArithmeticConstant) leftArg).getDoubleValue() < ((ArithmeticConstant) rightArg).getDoubleValue()
							? BooleanConstant.TRUE
							: BooleanConstant.FALSE;
				}
			}
			// System.out.println("leftArg=" + leftArg + " of " + leftArg.getClass().getSimpleName() + " of " +
			// leftArg.getEvaluationType());
			// System.out.println("rightArg=" + rightArg + " of " + rightArg.getClass().getSimpleName() + " of "
			// + rightArg.getEvaluationType());
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER, EvaluationType.DATE, EvaluationType.DURATION);
		}

		@Override
		public String getName() {
			return "less_than_operator";
		}
	};

	public static final FMLBooleanBinaryOperator LESS_THAN_OR_EQUALS = new ComparisonBinaryOperator() {
		@Override
		public int getPriority() {
			return 7;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg.equals(ObjectSymbolicConstant.NULL) || rightArg.equals(ObjectSymbolicConstant.NULL)) {
				throw new NullReferenceException(this);
			}
			if (leftArg instanceof ArithmeticConstant && rightArg instanceof ArithmeticConstant) {
				if (!((ArithmeticConstant) leftArg).isFloatingPointType() && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
					return ((ArithmeticConstant) leftArg).getLongValue() <= ((ArithmeticConstant) rightArg).getLongValue()
							? BooleanConstant.TRUE
							: BooleanConstant.FALSE;
				}
				else {
					return ((ArithmeticConstant) leftArg).getDoubleValue() <= ((ArithmeticConstant) rightArg).getDoubleValue()
							? BooleanConstant.TRUE
							: BooleanConstant.FALSE;
				}
			}
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER, EvaluationType.DATE, EvaluationType.DURATION);
		}

		@Override
		public String getName() {
			return "less_than_or_equals_operator";
		}
	};

	public static final FMLBooleanBinaryOperator GREATER_THAN = new ComparisonBinaryOperator() {
		@Override
		public int getPriority() {
			return 7;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg.equals(ObjectSymbolicConstant.NULL) || rightArg.equals(ObjectSymbolicConstant.NULL)) {
				throw new NullReferenceException(this);
			}
			if (leftArg instanceof ArithmeticConstant && rightArg instanceof ArithmeticConstant) {
				if (!((ArithmeticConstant) leftArg).isFloatingPointType() && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
					return ((ArithmeticConstant) leftArg).getLongValue() > ((ArithmeticConstant) rightArg).getLongValue()
							? BooleanConstant.TRUE
							: BooleanConstant.FALSE;
				}
				else {
					return ((ArithmeticConstant) leftArg).getDoubleValue() > ((ArithmeticConstant) rightArg).getDoubleValue()
							? BooleanConstant.TRUE
							: BooleanConstant.FALSE;
				}
			}
			// System.out.println("leftArg=" + leftArg + " of " + leftArg.getClass());
			// System.out.println("rightArg=" + rightArg);
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER, EvaluationType.DATE, EvaluationType.DURATION);
		}

		@Override
		public String getName() {
			return "greater_than_operator";
		}
	};

	public static final FMLBooleanBinaryOperator GREATER_THAN_OR_EQUALS = new ComparisonBinaryOperator() {
		@Override
		public int getPriority() {
			return 7;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg.equals(ObjectSymbolicConstant.NULL) || rightArg.equals(ObjectSymbolicConstant.NULL)) {
				throw new NullReferenceException(this);
			}
			if (leftArg instanceof ArithmeticConstant && rightArg instanceof ArithmeticConstant) {
				if (!((ArithmeticConstant) leftArg).isFloatingPointType() && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
					return ((ArithmeticConstant) leftArg).getLongValue() >= ((ArithmeticConstant) rightArg).getLongValue()
							? BooleanConstant.TRUE
							: BooleanConstant.FALSE;
				}
				else {
					return ((ArithmeticConstant) leftArg).getDoubleValue() >= ((ArithmeticConstant) rightArg).getDoubleValue()
							? BooleanConstant.TRUE
							: BooleanConstant.FALSE;
				}
			}
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER, EvaluationType.DATE, EvaluationType.DURATION);
		}

		@Override
		public String getName() {
			return "greater_than_or_equals_operator";
		}
	};

	public static abstract class LogicalBinaryOperator extends FMLBooleanBinaryOperator {
		@Override
		public EvaluationType getEvaluationType(EvaluationType leftOperandType, EvaluationType rightOperandType)
				throws TypeMismatchException {
			if (leftOperandType.isBooleanOrLiteral() && rightOperandType.isBooleanOrLiteral()) {
				return EvaluationType.BOOLEAN;
			}
			throw new TypeMismatchException(this, leftOperandType, rightOperandType, EvaluationType.BOOLEAN, EvaluationType.LITERAL);
		}
	}

	public static abstract class ComparisonBinaryOperator extends FMLBooleanBinaryOperator {
		@Override
		public EvaluationType getEvaluationType(EvaluationType leftOperandType, EvaluationType rightOperandType)
				throws TypeMismatchException {
			if (leftOperandType.isArithmeticOrLiteral() && rightOperandType.isArithmeticOrLiteral()) {
				return EvaluationType.BOOLEAN;
			}
			if (leftOperandType.isDateOrLiteral() && rightOperandType.isDateOrLiteral()) {
				return EvaluationType.BOOLEAN;
			}
			if (leftOperandType.isDurationOrLiteral() && rightOperandType.isDurationOrLiteral()) {
				return EvaluationType.BOOLEAN;
			}
			throw new TypeMismatchException(this, leftOperandType, rightOperandType, EvaluationType.ARITHMETIC_INTEGER,
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.DATE, EvaluationType.DURATION, EvaluationType.LITERAL);
		}
	}

}
