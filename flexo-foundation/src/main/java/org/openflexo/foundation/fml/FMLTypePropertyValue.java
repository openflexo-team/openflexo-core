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

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * A {@link FMLPropertyValue} which represents a {@link Type}
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(FMLTypePropertyValue.FMLTypePropertyValueImpl.class)
public interface FMLTypePropertyValue<M extends FMLObject, T extends Type> extends FMLPropertyValue<M, T> {

	@PropertyIdentifier(type = Type.class)
	public static final String TYPE_KEY = "type";

	@Getter(value = TYPE_KEY, ignoreType = true)
	public T getType();

	@Setter(TYPE_KEY)
	public void setType(T value);

	public static abstract class FMLTypePropertyValueImpl<M extends FMLObject, T extends Type> extends FMLPropertyValueImpl<M, T>
			implements FMLTypePropertyValue<M, T> {

		protected static final Logger logger = FlexoLogger.getLogger(FMLTypePropertyValue.class.getPackage().getName());

		@Override
		public T getValue() {
			return getType();
		}

		@Override
		public void applyPropertyValueToModelObject() {
			if (getProperty() != null && getObject() != null) {
				getProperty().set(getType(), getObject());
			}
		}

		@Override
		public void retrievePropertyValueFromModelObject() {
			if (getProperty() != null && getObject() != null) {
				setType(getProperty().get(getObject()));
			}
		}

		@Override
		public String toString() {

			return "FMLTypePropertyValue[" + Integer.toHexString(hashCode()) + "/"
					+ (getProperty() != null ? getProperty().getName() : "null") + "=" + getType() + ",required="
					+ (getProperty() != null ? getProperty().isRequired() : "?") + "]";
		}

	}
}
