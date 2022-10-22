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

import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * Abstraction used in the deserialization process, used to map a {@link FMLProperty} with some values in the context of a {@link FMLObject}
 *
 *
 * @author sylvain
 *
 */
@ModelEntity(isAbstract = true)
@Imports({ @Import(FMLSimplePropertyValue.class), @Import(FMLInstancePropertyValue.class), @Import(FMLInstancesListPropertyValue.class) })
public interface FMLPropertyValue<M extends FMLObject, T> extends FMLPrettyPrintable {

	@PropertyIdentifier(type = FMLProperty.class)
	public static final String PROPERTY_KEY = "property";
	@PropertyIdentifier(type = FMLObject.class)
	public static final String OBJECT_KEY = "object";

	@Getter(value = PROPERTY_KEY, ignoreType = true)
	public FMLProperty<? super M, T> getProperty();

	@Setter(PROPERTY_KEY)
	public void setProperty(FMLProperty<? super M, T> property);

	@Getter(OBJECT_KEY)
	public M getObject();
	
	@Setter(OBJECT_KEY)
	public void setObject(M object);
	
	/**
	 * Applies the property value to a {@link FMLObject}
	 * 
	 * @param object
	 */
	public void applyPropertyValueToModelObject(M object);

	/**
	 * Retrieve property value from {@link FMLObject}
	 * 
	 * @param object
	 */
	public void retrievePropertyValueFromModelObject(M object);

	public T getValue();

	public boolean isRequired(FMLModelFactory factory);

	public static abstract class FMLPropertyValueImpl<M extends FMLObject, T> extends FMLObjectImpl implements FMLPropertyValue<M, T> {

		protected static final Logger logger = FlexoLogger.getLogger(FMLPropertyValue.class.getPackage().getName());

		@Override
		public boolean isRequired(FMLModelFactory factory) {
			if (getValue() == null) {
				return getProperty().isRequired();
			}
			if (getValue().equals(getProperty().getDefaultValue(factory))) {
				// No need to serialize this
				return false;
			}
			return true;
		}

		@Override
		public FMLCompilationUnit getResourceData() {
			if (getObject() != null) {
				return getObject().getResourceData();
			}
			return null;
		}

		@Override
		public BindingModel getBindingModel() {
			if (getObject() != null) {
				return getObject().getBindingModel();
			}
			return null;
		}

	}
}
