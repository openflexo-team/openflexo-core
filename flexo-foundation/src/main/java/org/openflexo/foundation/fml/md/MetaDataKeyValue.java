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
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;

/**
 * A {@link MetaDataKeyValue} is a key-value data storing a <T> value <br>
 * 
 * @author sylvain
 *
 * @param <T>
 */
@ModelEntity
@ImplementationClass(MetaDataKeyValue.MetaDataKeyValueImpl.class)
@XMLElement
public interface MetaDataKeyValue<T> extends FMLObject, FMLPrettyPrintable {

	@PropertyIdentifier(type = String.class)
	public static final String KEY_KEY = "key";
	@PropertyIdentifier(type = FMLObject.class)
	public static final String OWNING_METADATA_KEY = "owningMetaData";

	@Getter(value = KEY_KEY)
	public String getKey();

	@Setter(KEY_KEY)
	public void setKey(String aKey);

	@Getter(value = OWNING_METADATA_KEY, ignoreForEquality = true)
	public MultiValuedMetaData getOwningMetaData();

	@Setter(OWNING_METADATA_KEY)
	public void setOwningMetaData(MultiValuedMetaData anObject);

	public DataBinding<T> getValueExpression();

	public void setValueExpression(DataBinding<T> aValue);

	public T getValue(Class<T> type);

	public void setValue(T value, Class<T> type);

	public String getSerializationRepresentation();

	public void setSerializationRepresentation(String s);

	public static abstract class MetaDataKeyValueImpl<T> extends FMLObjectImpl implements MetaDataKeyValue<T> {

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

		@Override
		public BindingModel getBindingModel() {
			if (getOwningMetaData() != null) {
				return getOwningMetaData().getBindingModel();
			}
			return null;
		}

		@Override
		public FMLCompilationUnit getResourceData() {
			if (getOwningMetaData() != null) {
				return getOwningMetaData().getResourceData();
			}
			return null;
		}

	}

}
