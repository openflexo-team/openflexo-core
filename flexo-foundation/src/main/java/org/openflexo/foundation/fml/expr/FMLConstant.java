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

import org.openflexo.connie.expr.Constant;
import org.openflexo.connie.expr.EvaluationType;
import org.openflexo.connie.expr.ExpressionPrettyPrinter;
import org.openflexo.connie.type.ExplicitNullType;

/**
 * A Java constant value
 * 
 * @author sylvain
 *
 * @param <V>
 *            type of constant value (a Java type)
 */
public abstract class FMLConstant<V> extends Constant<V> {

	@Override
	public ExpressionPrettyPrinter getPrettyPrinter() {
		return FMLPrettyPrinter.getInstance();
	}

	@SuppressWarnings("unchecked")
	public static <O> FMLConstant<O> makeConstant(O value) {
		if (value == null) {
			return (FMLConstant<O>) FMLConstant.ObjectSymbolicConstant.NULL;
		}
		if (value instanceof Boolean) {
			if ((Boolean) value) {
				return (FMLConstant<O>) FMLConstant.BooleanConstant.TRUE;
			}
			return (FMLConstant<O>) FMLConstant.BooleanConstant.FALSE;
		}
		else if (value instanceof Character) {
			return (FMLConstant<O>) new FMLConstant.CharConstant((Character) value);
		}
		else if (value instanceof String) {
			return (FMLConstant<O>) new FMLConstant.StringConstant((String) value);
		}
		else if (value.getClass().isEnum()) {
			return (FMLConstant<O>) new FMLConstant.EnumConstant<>((Enum<?>) value);
		}
		else if (value instanceof Float) {
			return (FMLConstant<O>) new FMLConstant.FloatConstant((Float) value);
		}
		else if (value instanceof Double) {
			return (FMLConstant<O>) new FMLConstant.DoubleConstant((Double) value);
		}
		else if (value instanceof Integer) {
			return (FMLConstant<O>) new FMLConstant.IntegerConstant((Integer) value);
		}
		else if (value instanceof Short) {
			return (FMLConstant<O>) new FMLConstant.ShortConstant((Short) value);
		}
		else if (value instanceof Long) {
			return (FMLConstant<O>) new FMLConstant.LongConstant((Long) value);
		}
		else if (value instanceof Byte) {
			return (FMLConstant<O>) new FMLConstant.IntegerConstant((Byte) value);
		}
		return (FMLConstant<O>) new FMLConstant.ObjectConstant(value);
	}

	/**
	 * An arithmetic Java constant
	 * 
	 * @author sylvain
	 *
	 * @param <V>
	 *            type of constant value (a number)
	 */
	public static abstract class ArithmeticConstant<V extends Number> extends FMLConstant<V> {

		/**
		 * Return boolean indicating if this constant is a floating point type or a natural integer based type
		 * 
		 * @return
		 */
		public abstract boolean isFloatingPointType();

		/**
		 * Return the value of the constant
		 * 
		 * @return
		 */
		public abstract V getArithmeticValue();

		/**
		 * Return the double representation of constant value
		 * 
		 * @return
		 */
		public abstract double getDoubleValue();

		/**
		 * Return the long representation of constant value
		 * 
		 * @return
		 */
		public abstract long getLongValue();
	}

	public static abstract class BooleanConstant extends FMLConstant<Boolean> {
		public static BooleanConstant get(boolean value) {
			if (value) {
				return TRUE;
			}
			return FALSE;
		}

		@Override
		public EvaluationType getEvaluationType() {
			return EvaluationType.BOOLEAN;
		}

		@Override
		public abstract Boolean getValue();

		public static final BooleanConstant TRUE = new BooleanConstant() {
			@Override
			public Boolean getValue() {
				return true;
			}

		};

		public static final BooleanConstant FALSE = new BooleanConstant() {
			@Override
			public Boolean getValue() {
				return false;
			}

		};
	}

	public static class CharConstant extends FMLConstant<Character> {
		private char value;

		@Override
		public EvaluationType getEvaluationType() {
			return EvaluationType.STRING;
		}

		public CharConstant(char value) {
			super();
			this.value = value;
		}

		@Override
		public Character getValue() {
			return value;
		}

		public void setValue(Character value) {
			this.value = value;
		}

	}

	public static class StringConstant extends FMLConstant<String> {
		private String value;

		@Override
		public EvaluationType getEvaluationType() {
			return EvaluationType.STRING;
		}

		public StringConstant(String value) {
			super();
			this.value = value;
		}

		@Override
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

	public static class ObjectConstant extends FMLConstant<Object> {
		private Object value;

		@Override
		public EvaluationType getEvaluationType() {
			return EvaluationType.LITERAL;
		}

		public ObjectConstant(Object value) {
			super();
			this.value = value;
		}

		@Override
		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		@Override
		public Type getType() {
			return getValue().getClass();
		}

	}

	public static class EnumConstant<E extends Enum<E>> extends FMLConstant<Enum<E>> {
		private Enum<E> value;
		private String enumName;

		@Override
		public EvaluationType getEvaluationType() {
			return EvaluationType.ENUM;
		}

		public EnumConstant(Enum<E> value) {
			super();
			this.value = value;
		}

		public String getName() {
			if (value != null) {
				return value.name();
			}
			return enumName;
		}

		@Override
		public Enum<E> getValue() {
			return value;
		}
	}

	public static class ByteConstant extends ArithmeticConstant<Byte> {
		private byte value;

		@Override
		public boolean isFloatingPointType() {
			return false;
		}

		@Override
		public EvaluationType getEvaluationType() {
			return EvaluationType.ARITHMETIC_INTEGER;
		}

		public ByteConstant(byte value) {
			super();
			this.value = value;
		}

		@Override
		public Byte getValue() {
			return value;
		}

		public void setValue(Byte value) {
			this.value = value;
		}

		@Override
		public Byte getArithmeticValue() {
			return getValue();
		}

		@Override
		public double getDoubleValue() {
			return getValue();
		}

		@Override
		public long getLongValue() {
			return getValue();
		}
	}

	public static class ShortConstant extends ArithmeticConstant<Short> {
		private short value;

		@Override
		public boolean isFloatingPointType() {
			return false;
		}

		@Override
		public EvaluationType getEvaluationType() {
			return EvaluationType.ARITHMETIC_INTEGER;
		}

		public ShortConstant(short value) {
			super();
			this.value = value;
		}

		@Override
		public Short getValue() {
			return value;
		}

		public void setValue(Short value) {
			this.value = value;
		}

		@Override
		public Short getArithmeticValue() {
			return getValue();
		}

		@Override
		public double getDoubleValue() {
			return getValue();
		}

		@Override
		public long getLongValue() {
			return getValue();
		}
	}

	public static class IntegerConstant extends ArithmeticConstant<Integer> {
		private int value;

		@Override
		public boolean isFloatingPointType() {
			return false;
		}

		@Override
		public EvaluationType getEvaluationType() {
			return EvaluationType.ARITHMETIC_INTEGER;
		}

		public IntegerConstant(int value) {
			super();
			this.value = value;
		}

		@Override
		public Integer getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}

		@Override
		public Integer getArithmeticValue() {
			return getValue();
		}

		@Override
		public double getDoubleValue() {
			return getValue();
		}

		@Override
		public long getLongValue() {
			return getValue();
		}
	}

	public static class LongConstant extends ArithmeticConstant<Long> {
		private long value;

		@Override
		public boolean isFloatingPointType() {
			return false;
		}

		@Override
		public EvaluationType getEvaluationType() {
			return EvaluationType.ARITHMETIC_INTEGER;
		}

		public LongConstant(long value) {
			super();
			this.value = value;
		}

		@Override
		public Long getValue() {
			return value;
		}

		public void setValue(long value) {
			this.value = value;
		}

		@Override
		public Long getArithmeticValue() {
			return getValue();
		}

		@Override
		public double getDoubleValue() {
			return getValue();
		}

		@Override
		public long getLongValue() {
			return getValue();
		}
	}

	public static class FloatConstant extends ArithmeticConstant<Float> {
		private float value;

		@Override
		public boolean isFloatingPointType() {
			return true;
		}

		@Override
		public EvaluationType getEvaluationType() {
			return EvaluationType.ARITHMETIC_FLOAT;
		}

		public FloatConstant(float value) {
			super();
			this.value = value;
		}

		@Override
		public Float getValue() {
			return value;
		}

		public void setValue(Float value) {
			this.value = value;
		}

		@Override
		public Float getArithmeticValue() {
			return getValue();
		}

		@Override
		public double getDoubleValue() {
			return getValue();
		}

		@Override
		public long getLongValue() {
			return getValue().longValue();
		}
	}

	public static class DoubleConstant extends ArithmeticConstant<Double> {
		private double value;

		@Override
		public boolean isFloatingPointType() {
			return true;
		}

		@Override
		public EvaluationType getEvaluationType() {
			return EvaluationType.ARITHMETIC_FLOAT;
		}

		public DoubleConstant(double value) {
			super();
			this.value = value;
		}

		@Override
		public Double getValue() {
			return value;
		}

		public void setValue(Double value) {
			this.value = value;
		}

		@Override
		public Double getArithmeticValue() {
			return getValue();
		}

		@Override
		public double getDoubleValue() {
			return getValue();
		}

		@Override
		public long getLongValue() {
			return getArithmeticValue().longValue();
		}
	}

	public static class ObjectSymbolicConstant extends FMLConstant<Object> {
		private String symbol;

		private ObjectSymbolicConstant(String symbol) {
			super();
			this.symbol = symbol;
		}

		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}

		public static final ObjectSymbolicConstant NULL = new ObjectSymbolicConstant("null") {
			@Override
			public Type getType() {
				return ExplicitNullType.INSTANCE;
			}
		};

		public String getValueAsString() {
			return getSymbol();
		}

		@Override
		public EvaluationType getEvaluationType() {
			return EvaluationType.LITERAL;
		}

		@Override
		public Object getValue() {
			// TODO
			return null;
		}

	}

}
