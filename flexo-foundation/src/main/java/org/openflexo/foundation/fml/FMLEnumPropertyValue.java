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

import java.util.logging.Logger;

import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * A {@link FMLPropertyValue} which represents a {@link Enum} value
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(FMLEnumPropertyValue.FMLEnumPropertyValueImpl.class)
public interface FMLEnumPropertyValue<M extends FMLObject, E extends Enum<E>> extends FMLPropertyValue<M, E> {

	@PropertyIdentifier(type = Enum.class)
	public static final String ENUM_VALUE_KEY = "enum";

	@Getter(value = ENUM_VALUE_KEY, ignoreType = true)
	public E getEnumValue();

	@Setter(ENUM_VALUE_KEY)
	public void setEnumValue(E value);

	public static abstract class FMLEnumPropertyValueImpl<M extends FMLObject, E extends Enum<E>> extends FMLPropertyValueImpl<M, E>
			implements FMLEnumPropertyValue<M, E> {

		protected static final Logger logger = FlexoLogger.getLogger(FMLEnumPropertyValue.class.getPackage().getName());

		@Override
		public E getValue() {
			return getEnumValue();
		}

		@Override
		public void applyPropertyValueToModelObject() {
			if (getProperty() != null && getObject() != null) {
				getProperty().set(getEnumValue(), getObject());
			}
		}

		@Override
		public void retrievePropertyValueFromModelObject() {
			if (getProperty() != null && getObject() != null) {
				setEnumValue(getProperty().get(getObject()));
			}
		}

		@Override
		public String toString() {

			return "FMLEnumPropertyValue[" + Integer.toHexString(hashCode()) + "/"
					+ (getProperty() != null ? getProperty().getName() : "null") + "=" + getEnumValue() + ",required="
					+ (getProperty() != null ? getProperty().isRequired() : "?") + "]";
		}

	}
}
