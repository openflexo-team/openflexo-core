/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.prefs;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

@ModelEntity
@ImplementationClass(PreferenceProperty.PreferencePropertyImpl.class)
@XMLElement(xmlTag = "CoreProperty")
public interface PreferenceProperty extends FlexoObject {

	@PropertyIdentifier(type = FlexoObject.class)
	public static final String OWNER_KEY = "owner";
	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = String.class)
	public static final String VALUE_KEY = "value";

	@Getter(NAME_KEY)
	@XMLAttribute
	public String getName();

	@Setter(NAME_KEY)
	public void setName(String name);

	@Getter(VALUE_KEY)
	@XMLAttribute
	public String getValue();

	@Setter(VALUE_KEY)
	public void setValue(String value);

	@Getter(value = OWNER_KEY, ignoreType = true, inverse = PreferencesContainer.CUSTOM_PROPERTIES_KEY)
	public PreferencesContainer getOwner();

	@Setter(OWNER_KEY)
	public void setOwner(PreferencesContainer owner);

	public boolean booleanValue();

	public boolean booleanValue(boolean defaultValue);

	public void setBooleanValue(boolean flag);

	public int integerValue();

	public int integerValue(int defaultValue);

	public void setIntegerValue(int value);

	public Integer getIntegerValue();

	public Boolean getBooleanValue();

	public static abstract class PreferencePropertyImpl extends FlexoObjectImpl implements PreferenceProperty {

		@Override
		public boolean delete(Object... context) {
			if (getOwner() != null) {
				getOwner().removeFromCustomProperties(this);
			}
			return performSuperDelete(context);
		}

		@Override
		public boolean booleanValue() {
			return getValue() != null && getValue().equalsIgnoreCase("true");
		}

		@Override
		public boolean booleanValue(boolean defaultValue) {
			if (getBooleanValue() == null) {
				return defaultValue;
			}
			return booleanValue();
		}

		@Override
		public Boolean getBooleanValue() {
			if (getValue() == null) {
				return null;
			}
			return booleanValue();
		}

		@Override
		public void setBooleanValue(boolean flag) {
			setValue(flag ? "true" : "false");
		}

		@Override
		public int integerValue() {
			return Integer.parseInt(getValue());
		}

		@Override
		public int integerValue(int defaultValue) {
			if (getIntegerValue() == null) {
				return defaultValue;
			}
			return integerValue();
		}

		@Override
		public Integer getIntegerValue() {
			if (getValue() == null) {
				return null;
			}
			return integerValue();
		}

		@Override
		public void setIntegerValue(int value) {
			setValue("" + value);
		}

	}

}
