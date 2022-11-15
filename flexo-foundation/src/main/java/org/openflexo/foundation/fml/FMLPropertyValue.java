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
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * Abstraction used in the serialisation/deserialization process, used to map a {@link FMLProperty} with some values in the context of a
 * {@link FMLObject}
 *
 * We maintain here the order of property values of their deserialization as well as unresolved properties
 *
 * @author sylvain
 *
 */
@ModelEntity(isAbstract = true)
@Imports({ @Import(FMLSimplePropertyValue.class), @Import(FMLTypePropertyValue.class), @Import(FMLInstancePropertyValue.class),
		@Import(FMLInstancesListPropertyValue.class) })
public interface FMLPropertyValue<M extends FMLObject, T> extends FMLPrettyPrintable {

	@PropertyIdentifier(type = FMLProperty.class)
	public static final String PROPERTY_KEY = "property";
	@PropertyIdentifier(type = String.class)
	public static final String UNRESOLVED_PROPERTY_NAME_KEY = "unresolvedPropertyName";
	@PropertyIdentifier(type = FMLObject.class)
	public static final String OBJECT_KEY = "object";

	/**
	 * Return the {@link FMLObject} on which this property value applies
	 * 
	 * @return
	 */
	@Getter(OBJECT_KEY)
	public M getObject();

	/**
	 * Sets the {@link FMLObject} on which this property value applies
	 * 
	 * @param object
	 */
	@Setter(OBJECT_KEY)
	public void setObject(M object);

	/**
	 * Return addressed property by this property value
	 * 
	 * @return
	 */
	@Getter(value = PROPERTY_KEY, ignoreType = true)
	public FMLProperty<? super M, T> getProperty();

	/**
	 * Sets addressed property by this property value
	 * 
	 * @param property
	 */
	@Setter(PROPERTY_KEY)
	public void setProperty(FMLProperty<? super M, T> property);

	/**
	 * Return name of this property value in case of related property was not found
	 * 
	 * @return
	 */
	@Getter(UNRESOLVED_PROPERTY_NAME_KEY)
	public String getUnresolvedPropertyName();

	/**
	 * Sets name of this property value in case of related property was not found
	 * 
	 * @param propertyName
	 */
	@Setter(UNRESOLVED_PROPERTY_NAME_KEY)
	public void setUnresolvedPropertyName(String propertyName);

	/**
	 * Applies the property value to a {@link FMLObject}
	 * 
	 * @param object
	 */
	public void applyPropertyValueToModelObject();

	/**
	 * Retrieve property value from {@link FMLObject}
	 * 
	 * @param object
	 */
	public void retrievePropertyValueFromModelObject();

	/**
	 * Return the value of this property value
	 * 
	 * @return
	 */
	public T getValue();

	/**
	 * Return boolean indicating whether addressed property is required
	 * 
	 * @param factory
	 * @return
	 */
	public boolean isRequired(FMLModelFactory factory);

	/**
	 * Return name of property, which is name of property when existing, or unresolved property name when non existant
	 * 
	 * @return
	 */
	public String getPropertyName();

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

		@Override
		public String getPropertyName() {
			if (getProperty() != null) {
				return getProperty().getLabel();
			}
			else {
				return getUnresolvedPropertyName();
			}
		}

	}

	@DefineValidationRule
	class PropertyValueMustAddressAnExistingProperty
			extends ValidationRule<PropertyValueMustAddressAnExistingProperty, FMLPropertyValue<?, ?>> {
		public PropertyValueMustAddressAnExistingProperty() {
			super(FMLPropertyValue.class, "property_value_must_address_an_existing_property");
		}

		@Override
		public ValidationIssue<PropertyValueMustAddressAnExistingProperty, FMLPropertyValue<?, ?>> applyValidation(
				FMLPropertyValue<?, ?> propertyValue) {
			if (propertyValue.getProperty() == null) {
				return new ValidationError<>(this, propertyValue,
						"unknown_property_($validable.propertyName)_for_($validable.object.implementedInterface.simpleName)");
			}
			return null;
		}
	}

	@DefineValidationRule
	class RequiredPropertyValueMustDefineAValue extends ValidationRule<RequiredPropertyValueMustDefineAValue, FMLPropertyValue<?, ?>> {
		public RequiredPropertyValueMustDefineAValue() {
			super(FMLPropertyValue.class, "required_property_must_define_a_value");
		}

		@Override
		public ValidationIssue<RequiredPropertyValueMustDefineAValue, FMLPropertyValue<?, ?>> applyValidation(
				FMLPropertyValue<?, ?> propertyValue) {
			if (propertyValue.getProperty() != null && propertyValue.getProperty().isRequired() && propertyValue.getValue() == null) {
				return new ValidationError<>(this, propertyValue,
						"property_($validable.propertyName)_required_in_($validable.object.implementedInterface.simpleName)_is_not_defined");
			}
			return null;
		}
	}

}
