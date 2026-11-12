/**
 * 
 * Copyright (c) 2026, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.Type;

import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.Constant;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.expr.FMLArithmeticBinaryOperator;
import org.openflexo.foundation.fml.expr.FMLConstant;

/**
 * The operator of an assignment statement ({@link AssignationAction}): the plain assignment {@code =}, or a compound one such as
 * {@code +=}, which assigns the result of the binary operator applied to the current value and to the assigned value (CORE-D-7).
 * <p>
 * As in Java, {@code x op= v} is {@code x = (T) (x op v)} where T is the type of x: the result is converted back to the type of
 * the target ({@code n /= 2} on an int gives an int), and {@code +=} on a String concatenates whatever the other operand is.
 * 
 * @author sylvain
 */
public enum AssignmentOperator {

	ASSIGN("=", null),
	PLUS_ASSIGN("+=", FMLArithmeticBinaryOperator.ADDITION),
	MINUS_ASSIGN("-=", FMLArithmeticBinaryOperator.SUBSTRACTION),
	STAR_ASSIGN("*=", FMLArithmeticBinaryOperator.MULTIPLICATION),
	SLASH_ASSIGN("/=", FMLArithmeticBinaryOperator.DIVISION),
	PERCENT_ASSIGN("%=", FMLArithmeticBinaryOperator.MOD),
	SHL_ASSIGN("<<=", FMLArithmeticBinaryOperator.SHIFT_LEFT),
	SHR_ASSIGN(">>=", FMLArithmeticBinaryOperator.SHIFT_RIGHT),
	USHR_ASSIGN(">>>=", FMLArithmeticBinaryOperator.SHIFT_RIGHT_2),
	AMP_ASSIGN("&=", FMLArithmeticBinaryOperator.BITWISE_AND),
	CARET_ASSIGN("^=", FMLArithmeticBinaryOperator.BITWISE_XOR),
	BAR_ASSIGN("|=", FMLArithmeticBinaryOperator.BITWISE_OR);

	private final String symbol;
	private final FMLArithmeticBinaryOperator binaryOperator;

	AssignmentOperator(String symbol, FMLArithmeticBinaryOperator binaryOperator) {
		this.symbol = symbol;
		this.binaryOperator = binaryOperator;
	}

	/**
	 * @return the operator as written in FML ({@code =}, {@code +=}...)
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * @return the binary operator a compound assignment applies, null for {@link #ASSIGN}
	 */
	public FMLArithmeticBinaryOperator getBinaryOperator() {
		return binaryOperator;
	}

	public boolean isCompound() {
		return binaryOperator != null;
	}

	/**
	 * Compute the value assigned by this operator
	 * 
	 * @param currentValue
	 *            the value of the assigned target before the assignment
	 * @param assignedValue
	 *            the value of the right-hand side
	 * @param targetType
	 *            the type of the assigned target, to which the result is converted (may be null: no conversion)
	 * @return the right-hand side for {@link #ASSIGN}, otherwise the binary operator applied to both values, converted to the type
	 *         of the target
	 */
	public Object apply(Object currentValue, Object assignedValue, Type targetType) throws TypeMismatchException, NullReferenceException {
		if (binaryOperator == null) {
			return assignedValue;
		}
		if (this == PLUS_ASSIGN && (currentValue instanceof String || TypeUtils.isString(targetType))) {
			// String concatenation, null included, as in Java
			return String.valueOf(currentValue) + String.valueOf(assignedValue);
		}
		Constant<?> result = binaryOperator.evaluate(FMLConstant.makeConstant(currentValue), FMLConstant.makeConstant(assignedValue));
		return convert(result != null ? result.getValue() : null, targetType);
	}

	/**
	 * The implicit narrowing of a Java compound assignment: a numeric result is converted to the numeric (or char) type of the target
	 */
	private static Object convert(Object value, Type targetType) {
		if (!(value instanceof Number) || targetType == null) {
			return value;
		}
		Number number = (Number) value;
		if (TypeUtils.isInteger(targetType)) {
			return number.intValue();
		}
		if (TypeUtils.isLong(targetType)) {
			return number.longValue();
		}
		if (TypeUtils.isShort(targetType)) {
			return number.shortValue();
		}
		if (TypeUtils.isByte(targetType)) {
			return number.byteValue();
		}
		if (TypeUtils.isFloat(targetType)) {
			return number.floatValue();
		}
		if (TypeUtils.isDouble(targetType)) {
			return number.doubleValue();
		}
		if (TypeUtils.isChar(targetType)) {
			return (char) number.intValue();
		}
		return value;
	}

	/**
	 * Tell if a compound assignment with this operator accepts a value of the supplied type for a target of the supplied type, as Java
	 * does: {@code +=} on a String accepts anything; an arithmetic operator on a numeric (or char) target accepts a numeric (or char)
	 * value, the result being converted to the type of the target
	 */
	public boolean acceptsCompound(Type targetType, Type valueType) {
		if (binaryOperator == null) {
			return false;
		}
		if (this == PLUS_ASSIGN && TypeUtils.isString(targetType)) {
			return true;
		}
		return isNumeric(targetType) && isNumeric(valueType);
	}

	private static boolean isNumeric(Type type) {
		return TypeUtils.isNumber(type) || TypeUtils.isChar(type);
	}

	/**
	 * @return the operator written with the supplied symbol, null if none
	 */
	public static AssignmentOperator fromSymbol(String symbol) {
		for (AssignmentOperator operator : values()) {
			if (operator.symbol.equals(symbol)) {
				return operator;
			}
		}
		return null;
	}
}
