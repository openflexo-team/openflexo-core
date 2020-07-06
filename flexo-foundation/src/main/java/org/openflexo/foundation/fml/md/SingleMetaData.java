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
import org.openflexo.foundation.fml.md.FMLMetaData.FMLMetaDataImpl;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
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

	public DataBinding<T> getValueExpression();

	public void setValueExpression(DataBinding<T> aValue);

	public T getValue(Class<T> type);

	public void setValue(T value, Class<T> type);

	public String getSerializationRepresentation();

	public void setSerializationRepresentation(String s);

	public static abstract class SingleMetaDataImpl<T> extends FMLMetaDataImpl implements SingleMetaData<T> {

		private DataBinding<T> valueExpression;
		private T value;
		private String serializationRepresentation;

		private Converter<T> converterForClass(Class<?> objectType) {
			return (Converter<T>) getFMLModelFactory().getStringEncoder().converterForClass(objectType);
		}

		/*private <T> T getValueAs(Class<T> type) {
			Converter<T> converter = converterForClass(type);
			try {
				return converter.convertFromString(getStringValueRepresentation(), getFMLModelFactory());
			} catch (InvalidDataException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		private <T> void setValueAs(T aValue, Class<T> type) {
			Converter<T> converter = converterForClass(type);
			setStringValueRepresentation(converter.convertToString(aValue));
		}
		
		@Override
		public Class<?> getType() {
			return type;
		}
		
		@Override
		public String getFMLValueRepresentation() {
			if (type != null && type.equals(String.class)) {
				return "\"" + getStringValueRepresentation() + "\"";
			}
			return getStringValueRepresentation();
		}
		
		@Override
		public void setFMLValueRepresentation(String fmlValueRepresentation) {
			if (fmlValueRepresentation != null && fmlValueRepresentation.startsWith("\"") && fmlValueRepresentation.endsWith("\"")) {
				setStringValueRepresentation(fmlValueRepresentation.substring(1, fmlValueRepresentation.length() - 1));
				type = String.class;
			}
			else {
				setStringValueRepresentation(fmlValueRepresentation);
			}
		
		}*/

		/*@Override
		public DataBinding<T> getValue() {
			return value;
		}
		
		@Override
		public void setValue(DataBinding<T> aValue) {
			if ((aValue == null && value != null) || (aValue != null && !aValue.equals(value))) {
				DataBinding<T> oldValue = value;
				this.value = aValue;
				getPropertyChangeSupport().firePropertyChange("value", oldValue, aValue);
			}
		}*/

		@Override
		public DataBinding<T> getValueExpression() {
			/*if (valueExpression == null) {
				valueExpression = new DataBinding<T>(this, Object.class, DataBinding.BindingDefinitionType.GET);
				valueExpression.setBindingName("valueExpression");
				valueExpression.setMandatory(true);
			
			}*/
			return valueExpression;
		}

		@Override
		public void setValueExpression(DataBinding<T> value) {
			if (value != null) {
				this.valueExpression = new DataBinding<T>(value.toString(), this, Object.class, DataBinding.BindingDefinitionType.GET);
				this.valueExpression.setBindingName("valueExpression");
				this.valueExpression.setMandatory(true);
			}
			notifiedBindingChanged(value);
		}

		@Override
		public T getValue(Class<T> type) {
			if (getValueExpression() != null && getValueExpression().isSet() && getValueExpression().isValid()) {
				try {
					return getValueExpression().getBindingValue(getReflectedBindingEvaluationContext());
				} catch (TypeMismatchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullReferenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (value == null && StringUtils.isNotEmpty(serializationRepresentation)) {

				if (serializationRepresentation != null && serializationRepresentation.startsWith("\"")
						&& serializationRepresentation.endsWith("\"")) {
					serializationRepresentation = serializationRepresentation.substring(1, serializationRepresentation.length() - 1);
				}

				Converter<T> converter = converterForClass(type);
				try {
					value = converter.convertFromString(serializationRepresentation, getFMLModelFactory());
				} catch (InvalidDataException e) {
					e.printStackTrace();
				}
			}

			return value;
		}

		@Override
		public void setValue(T value, Class<T> type) {
			/*if (value == null) {
				setValueExpression(new DataBinding<T>("null"));
			}
			else if (value instanceof Character) {
				setValueExpression(new DataBinding<T>("'" + value + "'"));
			}
			else if (value instanceof String) {
				setValueExpression(new DataBinding<T>("\"" + value + "\""));
			}
			else {
				setValueExpression(new DataBinding<T>(value.toString()));
			}*/
			this.value = value;
		}

		@Override
		public String getSerializationRepresentation() {
			if (getValueExpression() != null && getValueExpression().isSet() && getValueExpression().isValid()) {
				return getValueExpression().toString();
			}
			if (value != null) {
				Converter<T> converter = converterForClass(value.getClass());
				return "\"" + converter.convertToString(value) + "\"";
			}
			if (serializationRepresentation != null) {
				return serializationRepresentation;
			}
			return "null";
		}

		@Override
		public void setSerializationRepresentation(String s) {
			this.serializationRepresentation = s;
		}

		/*@Override
		public String getSerializationRepresentation() {
			if (getValue() == null) {
				return "null";
			}
			if (getValue() instanceof String) {
				return "\"" + getValue() + "\"";
			}
			if (getValue() instanceof Character) {
				return "'" + getValue() + "'";
			}
			return getValue().toString();
		}*/

	}

}
