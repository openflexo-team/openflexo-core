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

package org.openflexo.foundation.fml;

import org.openflexo.connie.DataBinding;
import org.openflexo.pamela.StringConverterLibrary.Converter;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.InvalidDataException;

/**
 * A {@link FMLMetaData} is a key-value data<br>
 * 
 * The stored value is a {@link String}
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FMLMetaData.FMLMetaDataImpl.class)
@XMLElement
public abstract interface FMLMetaData extends FMLObject, FMLPrettyPrintable {

	@PropertyIdentifier(type = String.class)
	public static final String KEY_KEY = "key";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String STRING_VALUE_REPRESENTATION_KEY = "stringValueRepresentation";
	@PropertyIdentifier(type = FMLObject.class)
	public static final String OWNER_KEY = "owner";

	@Getter(value = KEY_KEY)
	public String getKey();

	@Setter(KEY_KEY)
	public void setKey(String aKey);

	@Getter(value = STRING_VALUE_REPRESENTATION_KEY)
	public String getStringValueRepresentation();

	@Setter(STRING_VALUE_REPRESENTATION_KEY)
	public void setStringValueRepresentation(String stringValueRepresentation);

	public <T> T getValueAs(Class<T> type);

	public <T> void setValueAs(T aValue, Class<T> type);

	@Getter(value = OWNER_KEY)
	public FMLObject getOwner();

	@Setter(OWNER_KEY)
	public void setOwner(FMLObject anObject);

	public String getFMLValueRepresentation();

	public void setFMLValueRepresentation(String fmlValueRepresentation);

	public Class<?> getType();

	public static abstract class FMLMetaDataImpl extends FMLObjectImpl implements FMLMetaData {

		private Class<?> type;

		@Override
		public FMLCompilationUnit getResourceData() {
			if (getOwner() != null) {
				return getOwner().getResourceData();
			}
			return null;
		}

		private <T> Converter<T> converterForClass(Class<T> objectType) {
			return getFMLModelFactory().getStringEncoder().converterForClass(objectType);
		}

		@Override
		public <T> T getValueAs(Class<T> type) {
			Converter<T> converter = converterForClass(type);
			try {
				return converter.convertFromString(getStringValueRepresentation(), getFMLModelFactory());
			} catch (InvalidDataException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public <T> void setValueAs(T aValue, Class<T> type) {
			this.type = type;
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

		}

	}

}
