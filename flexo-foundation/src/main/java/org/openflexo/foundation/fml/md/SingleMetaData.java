/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml.md;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;

/**
 * A {@link SingleMetaData} is a key-value data storing a <T> value <br>
 * 
 * @author sylvain
 *
 * @param <T>
 */
@ModelEntity
@ImplementationClass(SingleMetaData.SingleMetaDataImpl.class)
@XMLElement
public interface SingleMetaData<T> extends FMLMetaData {

	public static final String VALUE_EXPRESSION_KEY = "valueExpression";
	public static final String SERIALIZATION_REPRESENTATION_KEY = "serializationRepresentation";

	@Getter(VALUE_EXPRESSION_KEY)
	public DataBinding<T> getValueExpression();

	@Setter(VALUE_EXPRESSION_KEY)
	public void setValueExpression(DataBinding<T> aValue);

	public T getValue(Class<T> type);

	public void setValue(T value, Class<T> type);

	@Getter(SERIALIZATION_REPRESENTATION_KEY)
	public String getSerializationRepresentation();

	@Setter(SERIALIZATION_REPRESENTATION_KEY)
	public void setSerializationRepresentation(String s);

	public static abstract class SingleMetaDataImpl<T> extends FMLMetaDataImpl implements SingleMetaData<T> {

		private DataBinding<T> valueExpression;
		private T value;
		private String serializationRepresentation;

		private Converter<T> converterForClass(Class<?> objectType) {
			return (Converter<T>) getFMLModelFactory().getStringEncoder().converterForClass(objectType);
		}

		@Override
		public DataBinding<T> getValueExpression() {
			return valueExpression;
		}

		@Override
		public void setValueExpression(DataBinding<T> valueExpression) {

			if (valueExpression != null) {
				this.valueExpression = new DataBinding<T>(valueExpression.toString(), this, Object.class,
						DataBinding.BindingDefinitionType.GET);
				this.valueExpression.setBindingName(VALUE_EXPRESSION_KEY);
				this.valueExpression.setMandatory(true);
			}
			notifiedBindingChanged(valueExpression);
		}

		@Override
		public T getValue(Class<T> type) {
			if (DataBinding.class.equals(type) && value == null && getValueExpression() != null) {
				// Special case : this is the DataBinding itself who is the value
				// Couldn't know it before (during serialization since this is interpretation of the metadata)
				value = (T) valueExpression;
				valueExpression = null;
				return value;
			}

			if (getValueExpression() != null && getValueExpression().isSet() && getValueExpression().isValid()) {
				try {
					return getValueExpression().getBindingValue(getReflectedBindingEvaluationContext());
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}

			if (value == null && StringUtils.isNotEmpty(serializationRepresentation)) {

				if (String.class.isAssignableFrom(type) && serializationRepresentation != null
						&& serializationRepresentation.startsWith("\"") && serializationRepresentation.endsWith("\"")) {
					serializationRepresentation = serializationRepresentation.substring(1, serializationRepresentation.length() - 1);
				}

				Converter<T> converter = converterForClass(type);
				try {
					value = converter.convertFromString(serializationRepresentation, getFMLModelFactory());
				} catch (InvalidDataException e) {
					e.printStackTrace();
				}

				// System.err.println("Decoding [" + serializationRepresentation + "] as " + type + " returning " + value);
			}

			return value;
		}

		@Override
		public void setValue(T value, Class<T> type) {
			this.value = value;
		}

		@Override
		public String getSerializationRepresentation() {
			if (getValueExpression() != null && getValueExpression().isSet() && getValueExpression().isValid()) {
				return getValueExpression().toString();
			}
			if (value != null) {
				Converter<T> converter = converterForClass(value.getClass());
				String convertedValue = converter.convertToString(value);
				if (value instanceof String) {
					return "\"" + convertedValue + "\"";
				}
				else {
					return convertedValue;
				}
			}
			if (serializationRepresentation != null) {
				return serializationRepresentation;
			}
			return "null";
		}

		@Override
		public void setSerializationRepresentation(String serializationRepresentation) {
			// System.out.println("---> On sette avec " + serializationRepresentation);
			if ((serializationRepresentation == null && this.serializationRepresentation != null)
					|| (serializationRepresentation != null && !serializationRepresentation.equals(this.serializationRepresentation))) {
				// System.out.println("Et donc la");
				String oldSerializationRepresentation = this.serializationRepresentation;
				this.serializationRepresentation = serializationRepresentation;
				value = null;
				getPropertyChangeSupport().firePropertyChange(SERIALIZATION_REPRESENTATION_KEY, oldSerializationRepresentation,
						this.serializationRepresentation);
			}
		}

		@Override
		public String toString() {
			return "@" + getKey() + "(" + getSerializationRepresentation() + ")";
		}
	}

}
