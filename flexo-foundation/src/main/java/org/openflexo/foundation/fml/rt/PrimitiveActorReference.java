/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.fml.rt;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.exceptions.InvalidDataException;

/**
 * Implements {@link ActorReference} for primitive types as modelling elements.<br>
 * 
 * @author sylvain
 * 
 * @param <T>
 */
@ModelEntity
@ImplementationClass(PrimitiveActorReference.PrimitiveActorReferenceImpl.class)
@XMLElement
public interface PrimitiveActorReference<T> extends ActorReference<T> {

	@PropertyIdentifier(type = String.class)
	public static final String VALUE_AS_STRING_KEY = "valueAsString";

	@Getter(value = VALUE_AS_STRING_KEY)
	@XMLAttribute
	public String getValueAsString();

	@Setter(VALUE_AS_STRING_KEY)
	public void setValueAsString(String value);

	@Override
	public PrimitiveRole<T> getFlexoRole();

	public static abstract class PrimitiveActorReferenceImpl<T> extends ActorReferenceImpl<T> implements PrimitiveActorReference<T> {

		private static final Logger logger = FlexoLogger.getLogger(PrimitiveActorReference.class.getPackage().toString());

		private T modellingElement = null;

		@Override
		public PrimitiveRole<T> getFlexoRole() {
			return (PrimitiveRole<T>) super.getFlexoRole();
		}

		@Override
		public void setModellingElement(T object) {
			modellingElement = object;
		}

		@Override
		public T getModellingElement() {
			if (modellingElement == null && getValueAsString() != null && getFactory() != null) {
				try {
					modellingElement = getFactory().getStringEncoder().fromString(getActorClass(), getValueAsString());
				} catch (InvalidDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return modellingElement;
		}

		@Override
		public String getValueAsString() {
			if (modellingElement != null && getFactory() != null) {
				try {
					return getFactory().getStringEncoder().toString(modellingElement);
				} catch (InvalidDataException e) {
					e.printStackTrace();
				}
			}
			return (String) performSuperGetter(VALUE_AS_STRING_KEY);
		}

		@Override
		public Class<? extends T> getActorClass() {
			switch (getFlexoRole().getPrimitiveType()) {
			case String:
			case LocalizedString:
				return (Class<? extends T>) String.class;
			case Boolean:
				return (Class<? extends T>) Boolean.class;
			case Float:
				return (Class<? extends T>) Float.class;
			case Integer:
				return (Class<? extends T>) Integer.class;
			default:
				return (Class<? extends T>) Object.class;
			}
		}

		@Override
		public String toString() {
			return "PrimitiveActorReference [" + getRoleName() + "] " + Integer.toHexString(hashCode()) + " encodes "
					+ getModellingElement() + "[type: " + (getFlexoRole() != null ? getFlexoRole().getPrimitiveType() : "null") + "]";
		}

	}
}
