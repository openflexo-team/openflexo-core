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
import org.openflexo.foundation.fml.expr.FMLConstant.ArithmeticConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.BooleanConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.ByteConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.CharConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.DoubleConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.FloatConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.IntegerConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.LongConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.ObjectConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.ObjectSymbolicConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.ShortConstant;
import org.openflexo.foundation.fml.expr.FMLConstant.StringConstant;

public abstract class FMLArithmeticBinaryOperator extends FMLBinaryOperator {

	public static final FMLArithmeticBinaryOperator ADDITION = new FMLArithmeticBinaryOperator() {
		@Override
		public int getPriority() {
			return 5;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg instanceof ArithmeticConstant) {
				if (rightArg instanceof ArithmeticConstant) {
					// TODO: handle all number types
					if (!((ArithmeticConstant) leftArg).isFloatingPointType() && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
						return FMLConstant.makeConstant(
								((ArithmeticConstant) leftArg).getLongValue() + ((ArithmeticConstant) rightArg).getLongValue());
					}
					return FMLConstant.makeConstant(
							((ArithmeticConstant<?>) leftArg).getDoubleValue() + ((ArithmeticConstant<?>) rightArg).getDoubleValue());
				}
				throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(), EvaluationType.values());
			}
			else if (leftArg instanceof StringConstant) {
				if (rightArg instanceof StringConstant) {
					return new StringConstant(((StringConstant) leftArg).getValue() + ((StringConstant) rightArg).getValue());
				}
				else if (rightArg instanceof CharConstant) {
					return new StringConstant(((StringConstant) leftArg).getValue() + ((CharConstant) rightArg).getValue());
				}
				else if (rightArg instanceof ByteConstant) {
					return new StringConstant(((StringConstant) leftArg).getValue() + ((ByteConstant) rightArg).getValue());
				}
				else if (rightArg instanceof ShortConstant) {
					return new StringConstant(((StringConstant) leftArg).getValue() + ((ShortConstant) rightArg).getValue());
				}
				else if (rightArg instanceof IntegerConstant) {
					return new StringConstant(((StringConstant) leftArg).getValue() + ((IntegerConstant) rightArg).getValue());
				}
				else if (rightArg instanceof LongConstant) {
					return new StringConstant(((StringConstant) leftArg).getValue() + ((LongConstant) rightArg).getValue());
				}
				else if (rightArg instanceof FloatConstant) {
					return new StringConstant(((StringConstant) leftArg).getValue() + ((FloatConstant) rightArg).getValue());
				}
				else if (rightArg instanceof DoubleConstant) {
					return new StringConstant(((StringConstant) leftArg).getValue() + ((DoubleConstant) rightArg).getValue());
				}
				else if (rightArg instanceof BooleanConstant) {
					return new StringConstant(((StringConstant) leftArg).getValue() + ((BooleanConstant) rightArg).getValue());
				}
				else if (rightArg == ObjectSymbolicConstant.NULL) {
					return new StringConstant(((StringConstant) leftArg).getValue() + "null");
				}
				else if (rightArg instanceof ObjectConstant) {
					return new StringConstant(((StringConstant) leftArg).getValue() + ((ObjectConstant) rightArg).getValue().toString());
				}
			}

			// Special case to handle String concatenation with null
			if (leftArg == ObjectSymbolicConstant.NULL && rightArg instanceof StringConstant) {
				return evaluate(new StringConstant("null"), rightArg);
			}
			if (leftArg == ObjectSymbolicConstant.NULL && rightArg == ObjectSymbolicConstant.NULL) {
				throw new NullReferenceException();
			}
			System.out.println("leftArg=" + leftArg + " of " + leftArg.getClass() + " eval type =" + leftArg.getEvaluationType());
			System.out.println("rightArg=" + rightArg + " of " + rightArg.getClass() + " eval type =" + rightArg.getEvaluationType());
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(), EvaluationType.values());
		}

		@Override
		public EvaluationType getEvaluationType(EvaluationType leftOperandType, EvaluationType rightOperandType)
				throws TypeMismatchException {
			if (leftOperandType.isLiteral()) {
				return EvaluationType.LITERAL; // Undecided
			}
			else if (leftOperandType.isArithmetic()) {
				if (rightOperandType.isArithmetic()) {
					if (leftOperandType.isArithmeticInteger() && rightOperandType.isArithmeticInteger()) {
						return EvaluationType.ARITHMETIC_INTEGER;
					}
					return EvaluationType.ARITHMETIC_FLOAT;
				}
				else if (rightOperandType.isLiteral()) {
					return EvaluationType.ARITHMETIC_FLOAT; // Undecided
				}
			}
			else if (leftOperandType.isString()) {
				return EvaluationType.STRING;
			}
			else if (leftOperandType.isDuration()) {
				if (rightOperandType.isDurationOrLiteral()) {
					return EvaluationType.DURATION;
				}
				if (rightOperandType.isDateOrLiteral()) {
					return EvaluationType.DATE;
				}
			}
			else if (leftOperandType.isDate() && rightOperandType.isDurationOrLiteral()) {
				return EvaluationType.DATE;
			}
			throw new TypeMismatchException(this, leftOperandType, rightOperandType, EvaluationType.ARITHMETIC_FLOAT,
					EvaluationType.ARITHMETIC_INTEGER, EvaluationType.DATE, EvaluationType.DURATION, EvaluationType.LITERAL);
		}

		@Override
		public String getName() {
			return "addition";
		}
	};

	public static final FMLArithmeticBinaryOperator SUBSTRACTION = new FMLArithmeticBinaryOperator() {
		@Override
		public int getPriority() {
			return 5;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg instanceof ArithmeticConstant && rightArg instanceof ArithmeticConstant) {
				// TODO: handle all number types
				if (!((ArithmeticConstant) leftArg).isFloatingPointType() && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
					return FMLConstant
							.makeConstant(((ArithmeticConstant) leftArg).getLongValue() - ((ArithmeticConstant) rightArg).getLongValue());
				}
				return FMLConstant.makeConstant(
						((ArithmeticConstant<?>) leftArg).getDoubleValue() - ((ArithmeticConstant<?>) rightArg).getDoubleValue());
			}

			if (rightArg == ObjectSymbolicConstant.NULL) {
				throw new NullReferenceException(SUBSTRACTION);
			}
			if (leftArg == ObjectSymbolicConstant.NULL) {
				throw new NullReferenceException(SUBSTRACTION);
			}

			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER, EvaluationType.DATE, EvaluationType.DURATION);
		}

		@Override
		public EvaluationType getEvaluationType(EvaluationType leftOperandType, EvaluationType rightOperandType)
				throws TypeMismatchException {

			if (leftOperandType.isLiteral()) {
				return EvaluationType.LITERAL; // Undecided
			}
			else if (leftOperandType.isArithmetic()) {
				if (rightOperandType.isArithmetic()) {
					if (leftOperandType.isArithmeticInteger() && rightOperandType.isArithmeticInteger()) {
						return EvaluationType.ARITHMETIC_INTEGER;
					}
					return EvaluationType.ARITHMETIC_FLOAT;
				}
				else if (rightOperandType.isLiteral()) {
					return EvaluationType.LITERAL; // Undecided
				}
			}
			else if (leftOperandType.isDuration()) {
				if (rightOperandType.isDurationOrLiteral()) {
					return EvaluationType.DURATION;
				}
			}
			else if (leftOperandType.isDate()) {
				if (rightOperandType.isDurationOrLiteral()) {
					return EvaluationType.DATE;
				}
				if (rightOperandType.isDateOrLiteral()) {
					return EvaluationType.DURATION;
				}
			}
			throw new TypeMismatchException(this, leftOperandType, rightOperandType, EvaluationType.ARITHMETIC_FLOAT,
					EvaluationType.ARITHMETIC_INTEGER, EvaluationType.DATE, EvaluationType.DURATION, EvaluationType.LITERAL);
		}

		@Override
		public String getName() {
			return "substraction";
		}
	};

	public static final FMLArithmeticBinaryOperator MULTIPLICATION = new FMLArithmeticBinaryOperator() {
		@Override
		public int getPriority() {
			return 4;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg instanceof ArithmeticConstant && rightArg instanceof ArithmeticConstant) {
				// TODO: handle all number types
				if (!((ArithmeticConstant) leftArg).isFloatingPointType() && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
					return FMLConstant
							.makeConstant(((ArithmeticConstant) leftArg).getLongValue() * ((ArithmeticConstant) rightArg).getLongValue());
				}
				return FMLConstant.makeConstant(
						((ArithmeticConstant<?>) leftArg).getDoubleValue() * ((ArithmeticConstant<?>) rightArg).getDoubleValue());
			}
			if (leftArg == ObjectSymbolicConstant.NULL || rightArg == ObjectSymbolicConstant.NULL) {
				throw new NullReferenceException(this);
			}
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER);
		}

		@Override
		public EvaluationType getEvaluationType(EvaluationType leftOperandType, EvaluationType rightOperandType)
				throws TypeMismatchException {
			if (leftOperandType.isArithmeticOrLiteral()) {
				if (rightOperandType.isArithmeticOrLiteral()) {
					if (leftOperandType.isArithmeticInteger() && rightOperandType.isArithmeticInteger()) {
						return EvaluationType.ARITHMETIC_INTEGER;
					}
					return EvaluationType.ARITHMETIC_FLOAT;
				}
			}
			throw new TypeMismatchException(this, leftOperandType, rightOperandType, EvaluationType.ARITHMETIC_FLOAT,
					EvaluationType.ARITHMETIC_INTEGER, EvaluationType.LITERAL);
		}

		@Override
		public String getName() {
			return "multiplication";
		}
	};

	public static final FMLArithmeticBinaryOperator DIVISION = new FMLArithmeticBinaryOperator() {
		@Override
		public int getPriority() {
			return 4;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg instanceof ArithmeticConstant && rightArg instanceof ArithmeticConstant) {
				return FMLConstant.makeConstant(
						((ArithmeticConstant<?>) leftArg).getDoubleValue() / ((ArithmeticConstant<?>) rightArg).getDoubleValue());
			}
			if (leftArg == ObjectSymbolicConstant.NULL || rightArg == ObjectSymbolicConstant.NULL) {
				throw new NullReferenceException(this);
			}
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER);
		}

		@Override
		public EvaluationType getEvaluationType(EvaluationType leftOperandType, EvaluationType rightOperandType)
				throws TypeMismatchException {
			if (leftOperandType.isArithmeticOrLiteral()) {
				if (rightOperandType.isArithmeticOrLiteral()) {
					return EvaluationType.ARITHMETIC_FLOAT;
				}
			}
			throw new TypeMismatchException(this, leftOperandType, rightOperandType, EvaluationType.ARITHMETIC_FLOAT,
					EvaluationType.ARITHMETIC_INTEGER, EvaluationType.LITERAL);
		}

		@Override
		public String getName() {
			return "division";
		}
	};

	public static final FMLArithmeticBinaryOperator MOD = new FMLArithmeticBinaryOperator() {
		@Override
		public int getPriority() {
			return 4;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg instanceof ArithmeticConstant && rightArg instanceof ArithmeticConstant) {
				return FMLConstant.makeConstant(
						((ArithmeticConstant<?>) leftArg).getDoubleValue() % ((ArithmeticConstant<?>) rightArg).getDoubleValue());
			}
			if (leftArg == ObjectSymbolicConstant.NULL || rightArg == ObjectSymbolicConstant.NULL) {
				throw new NullReferenceException(this);
			}
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER);
		}

		@Override
		public EvaluationType getEvaluationType(EvaluationType leftOperandType, EvaluationType rightOperandType)
				throws TypeMismatchException {
			if (leftOperandType.isArithmeticOrLiteral()) {
				if (rightOperandType.isArithmeticOrLiteral()) {
					return EvaluationType.ARITHMETIC_FLOAT;
				}
			}
			throw new TypeMismatchException(this, leftOperandType, rightOperandType, EvaluationType.ARITHMETIC_FLOAT,
					EvaluationType.ARITHMETIC_INTEGER, EvaluationType.LITERAL);
		}

		@Override
		public String getName() {
			return "mod";
		}
	};

	public static abstract class BitwiseBinaryOperator extends FMLArithmeticBinaryOperator {
		@Override
		public EvaluationType getEvaluationType(EvaluationType leftOperandType, EvaluationType rightOperandType)
				throws TypeMismatchException {
			if (leftOperandType.isArithmeticOrLiteral() && rightOperandType.isArithmeticOrLiteral()) {
				return EvaluationType.ARITHMETIC_INTEGER;
			}
			throw new TypeMismatchException(this, leftOperandType, rightOperandType, EvaluationType.ARITHMETIC_INTEGER,
					EvaluationType.LITERAL);
		}
	}

	public static final FMLArithmeticBinaryOperator SHIFT_LEFT = new BitwiseBinaryOperator() {
		@Override
		public int getPriority() {
			return 6;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg.equals(ObjectSymbolicConstant.NULL) || rightArg.equals(ObjectSymbolicConstant.NULL)) {
				throw new NullReferenceException(this);
			}
			if (leftArg instanceof ArithmeticConstant && !((ArithmeticConstant) leftArg).isFloatingPointType()
					&& rightArg instanceof ArithmeticConstant && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
				if (leftArg instanceof ByteConstant && rightArg instanceof ByteConstant) {
					return FMLConstant.makeConstant(((ByteConstant) leftArg).getValue() << ((ByteConstant) rightArg).getValue());
				}
				else if (leftArg instanceof ShortConstant && rightArg instanceof ShortConstant) {
					return FMLConstant.makeConstant(((ShortConstant) leftArg).getValue() << ((ShortConstant) rightArg).getValue());
				}
				else if (leftArg instanceof IntegerConstant && rightArg instanceof IntegerConstant) {
					return FMLConstant.makeConstant(((IntegerConstant) leftArg).getValue() << ((IntegerConstant) rightArg).getValue());
				}
				return FMLConstant.makeConstant(
						((ArithmeticConstant<?>) leftArg).getLongValue() << ((ArithmeticConstant<?>) rightArg).getLongValue());
			}
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER);
		}

		@Override
		public String getName() {
			return "shift_left";
		}
	};

	public static final FMLArithmeticBinaryOperator SHIFT_RIGHT = new BitwiseBinaryOperator() {
		@Override
		public int getPriority() {
			return 6;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg.equals(ObjectSymbolicConstant.NULL) || rightArg.equals(ObjectSymbolicConstant.NULL)) {
				throw new NullReferenceException(this);
			}
			if (leftArg instanceof ArithmeticConstant && !((ArithmeticConstant) leftArg).isFloatingPointType()
					&& rightArg instanceof ArithmeticConstant && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
				if (leftArg instanceof ByteConstant && rightArg instanceof ByteConstant) {
					return FMLConstant.makeConstant(((ByteConstant) leftArg).getValue() >> ((ByteConstant) rightArg).getValue());
				}
				else if (leftArg instanceof ShortConstant && rightArg instanceof ShortConstant) {
					return FMLConstant.makeConstant(((ShortConstant) leftArg).getValue() >> ((ShortConstant) rightArg).getValue());
				}
				else if (leftArg instanceof IntegerConstant && rightArg instanceof IntegerConstant) {
					return FMLConstant.makeConstant(((IntegerConstant) leftArg).getValue() >> ((IntegerConstant) rightArg).getValue());
				}
				return FMLConstant.makeConstant(
						((ArithmeticConstant<?>) leftArg).getLongValue() >> ((ArithmeticConstant<?>) rightArg).getLongValue());
			}
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER);
		}

		@Override
		public String getName() {
			return "shift_right";
		}
	};

	public static final FMLArithmeticBinaryOperator SHIFT_RIGHT_2 = new BitwiseBinaryOperator() {
		@Override
		public int getPriority() {
			return 6;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg.equals(ObjectSymbolicConstant.NULL) || rightArg.equals(ObjectSymbolicConstant.NULL)) {
				throw new NullReferenceException(this);
			}
			if (leftArg instanceof ArithmeticConstant && !((ArithmeticConstant) leftArg).isFloatingPointType()
					&& rightArg instanceof ArithmeticConstant && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
				if (leftArg instanceof ByteConstant && rightArg instanceof ByteConstant) {
					return FMLConstant.makeConstant(((ByteConstant) leftArg).getValue() >>> ((ByteConstant) rightArg).getValue());
				}
				else if (leftArg instanceof ShortConstant && rightArg instanceof ShortConstant) {
					return FMLConstant.makeConstant(((ShortConstant) leftArg).getValue() >>> ((ShortConstant) rightArg).getValue());
				}
				else if (leftArg instanceof IntegerConstant && rightArg instanceof IntegerConstant) {
					return FMLConstant.makeConstant(((IntegerConstant) leftArg).getValue() >>> ((IntegerConstant) rightArg).getValue());
				}
				return FMLConstant.makeConstant(
						((ArithmeticConstant<?>) leftArg).getLongValue() >>> ((ArithmeticConstant<?>) rightArg).getLongValue());
			}
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER);
		}

		@Override
		public String getName() {
			return "shift_right_2";
		}
	};

	public static final FMLArithmeticBinaryOperator BITWISE_AND = new BitwiseBinaryOperator() {
		@Override
		public int getPriority() {
			return 9;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg.equals(ObjectSymbolicConstant.NULL) || rightArg.equals(ObjectSymbolicConstant.NULL)) {
				throw new NullReferenceException(this);
			}
			if (leftArg instanceof ArithmeticConstant && !((ArithmeticConstant) leftArg).isFloatingPointType()
					&& rightArg instanceof ArithmeticConstant && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
				if (leftArg instanceof ByteConstant && rightArg instanceof ByteConstant) {
					return FMLConstant.makeConstant(((ByteConstant) leftArg).getValue() & ((ByteConstant) rightArg).getValue());
				}
				else if (leftArg instanceof ShortConstant && rightArg instanceof ShortConstant) {
					return FMLConstant.makeConstant(((ShortConstant) leftArg).getValue() & ((ShortConstant) rightArg).getValue());
				}
				else if (leftArg instanceof IntegerConstant && rightArg instanceof IntegerConstant) {
					return FMLConstant.makeConstant(((IntegerConstant) leftArg).getValue() & ((IntegerConstant) rightArg).getValue());
				}
				return FMLConstant
						.makeConstant(((ArithmeticConstant<?>) leftArg).getLongValue() & ((ArithmeticConstant<?>) rightArg).getLongValue());
			}
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER);
		}

		@Override
		public String getName() {
			return "bitwise_and";
		}
	};

	public static final FMLArithmeticBinaryOperator BITWISE_OR = new BitwiseBinaryOperator() {
		@Override
		public int getPriority() {
			return 11;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg.equals(ObjectSymbolicConstant.NULL) || rightArg.equals(ObjectSymbolicConstant.NULL)) {
				throw new NullReferenceException(this);
			}
			if (leftArg instanceof ArithmeticConstant && !((ArithmeticConstant) leftArg).isFloatingPointType()
					&& rightArg instanceof ArithmeticConstant && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
				if (leftArg instanceof ByteConstant && rightArg instanceof ByteConstant) {
					return FMLConstant.makeConstant(((ByteConstant) leftArg).getValue() | ((ByteConstant) rightArg).getValue());
				}
				else if (leftArg instanceof ShortConstant && rightArg instanceof ShortConstant) {
					return FMLConstant.makeConstant(((ShortConstant) leftArg).getValue() | ((ShortConstant) rightArg).getValue());
				}
				else if (leftArg instanceof IntegerConstant && rightArg instanceof IntegerConstant) {
					return FMLConstant.makeConstant(((IntegerConstant) leftArg).getValue() | ((IntegerConstant) rightArg).getValue());
				}
				return FMLConstant
						.makeConstant(((ArithmeticConstant<?>) leftArg).getLongValue() | ((ArithmeticConstant<?>) rightArg).getLongValue());
			}
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER);
		}

		@Override
		public String getName() {
			return "bitwise_or";
		}
	};

	public static final FMLArithmeticBinaryOperator BITWISE_XOR = new BitwiseBinaryOperator() {
		@Override
		public int getPriority() {
			return 10;
		}

		@Override
		public Constant<?> evaluate(Constant<?> leftArg, Constant<?> rightArg) throws TypeMismatchException, NullReferenceException {
			if (leftArg.equals(ObjectSymbolicConstant.NULL) || rightArg.equals(ObjectSymbolicConstant.NULL)) {
				throw new NullReferenceException(this);
			}
			if (leftArg instanceof ArithmeticConstant && !((ArithmeticConstant) leftArg).isFloatingPointType()
					&& rightArg instanceof ArithmeticConstant && !((ArithmeticConstant) rightArg).isFloatingPointType()) {
				if (leftArg instanceof ByteConstant && rightArg instanceof ByteConstant) {
					return FMLConstant.makeConstant(((ByteConstant) leftArg).getValue() ^ ((ByteConstant) rightArg).getValue());
				}
				else if (leftArg instanceof ShortConstant && rightArg instanceof ShortConstant) {
					return FMLConstant.makeConstant(((ShortConstant) leftArg).getValue() ^ ((ShortConstant) rightArg).getValue());
				}
				else if (leftArg instanceof IntegerConstant && rightArg instanceof IntegerConstant) {
					return FMLConstant.makeConstant(((IntegerConstant) leftArg).getValue() ^ ((IntegerConstant) rightArg).getValue());
				}
				return FMLConstant
						.makeConstant(((ArithmeticConstant<?>) leftArg).getLongValue() ^ ((ArithmeticConstant<?>) rightArg).getLongValue());
			}
			throw new TypeMismatchException(this, leftArg.getEvaluationType(), rightArg.getEvaluationType(),
					EvaluationType.ARITHMETIC_FLOAT, EvaluationType.ARITHMETIC_INTEGER);
		}

		@Override
		public String getName() {
			return "bitwise_xor";
		}
	};

}
