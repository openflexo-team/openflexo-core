/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;

@ModelEntity
@ImplementationClass(FlexoProperty.FlexoPropertyImpl.class)
public interface FlexoProperty extends FlexoObject {

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

	@Getter(value = OWNER_KEY, ignoreType = true, inverse = FlexoObject.CUSTOM_PROPERTIES_KEY)
	public FlexoObject getOwner();

	@Setter(OWNER_KEY)
	public void setOwner(FlexoObject owner);

	public boolean booleanValue();

	public void setBooleanValue(boolean flag);

	public int integerValue();

	public void setIntegerValue(int value);

	public static abstract class FlexoPropertyImpl extends FlexoObservable implements FlexoProperty {

		private FlexoObject owner;

		private String name;
		private String value;

		/*public FlexoProperty(FlexoBuilder<?> builder) {
			this(builder.getProject());
		}

		public FlexoProperty(FlexoProjectBuilder builder) {
			this(builder.project);
		}*/

		@Override
		public boolean delete(Object... context) {
			if (getOwner() != null) {
				getOwner().removeFromCustomProperties(this);
			}
			return performSuperDelete(context);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			this.name = name;
			setChanged();
			notifyObservers(new DataModification("name", null, name));
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public void setValue(String value) {
			this.value = value;
			setChanged();
			notifyObservers(new DataModification("value", null, value));
		}

		@Override
		public FlexoObject getOwner() {
			return owner;
		}

		@Override
		public void setOwner(FlexoObject owner) {
			this.owner = owner;
		}

		@Override
		public boolean booleanValue() {
			return getValue() != null && getValue().equalsIgnoreCase("true");
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
		public void setIntegerValue(int value) {
			this.value = "" + value;
		}

	}

}