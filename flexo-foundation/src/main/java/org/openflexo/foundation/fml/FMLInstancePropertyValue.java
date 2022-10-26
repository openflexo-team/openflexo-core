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
 * A {@link FMLPropertyValue} which has a {@link FMLObject} value (wrapped in a {@link WrappedFMLObject})
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(FMLInstancePropertyValue.FMLInstancePropertyValueImpl.class)
public interface FMLInstancePropertyValue<M extends FMLObject, T extends FMLObject> extends FMLPropertyValue<M, T> {

	@PropertyIdentifier(type = WrappedFMLObject.class)
	public static final String INSTANCE_KEY = "instance";

	@Getter(value = INSTANCE_KEY)
	public WrappedFMLObject<T> getInstance();

	@Setter(INSTANCE_KEY)
	public void setInstance(WrappedFMLObject<T> value);

	public static abstract class FMLInstancePropertyValueImpl<M extends FMLObject, T extends FMLObject> extends FMLPropertyValueImpl<M, T>
	implements FMLInstancePropertyValue<M, T> {

		protected static final Logger logger = FlexoLogger.getLogger(FMLInstancePropertyValue.class.getPackage().getName());

		@Override
		public void applyPropertyValueToModelObject() {
			if (getProperty() != null && getObject() != null) {
				getProperty().set(getValue(), getObject());
			}
		}

		@Override
		public void retrievePropertyValueFromModelObject() {
			if (getProperty() != null && getObject() != null && getObject().getFMLModelFactory() != null) {
				setInstance(getObject().getFMLModelFactory().getWrappedFMLObject(getProperty().get(getObject())));
			}
		}

		@Override
		public T getValue() {
			if (getInstance() != null) {
				return getInstance().getObject();
			}
			return null;
		}

		@Override
		public String toString() {

			return "FMLInstancePropertyValue[" + (getProperty() != null ? getProperty().getName() : "null") + "=" + getValue() + "]";
		}

	}
}
