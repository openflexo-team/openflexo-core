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

import java.beans.PropertyChangeSupport;
import java.lang.reflect.Type;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.ConnieType;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * A {@link AbstractProperty} represents a pure declaration of a {@link FlexoProperty} with no implementation<br>
 * This property must be overriden in child {@link FlexoConcept}
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(AbstractProperty.AbstractPropertyImpl.class)
@XMLElement
public abstract interface AbstractProperty<T> extends FlexoProperty<T> {

	@PropertyIdentifier(type = Type.class)
	public static final String TYPE_KEY = "type";

	@PropertyIdentifier(type = Boolean.class)
	public static final String READ_ONLY_KEY = "readOnly";

	@Getter(value = READ_ONLY_KEY, defaultValue = "false")
	@XMLAttribute
	@Override
	public boolean isReadOnly();

	@Setter(READ_ONLY_KEY)
	public void setReadOnly(boolean readOnly);

	@Override
	@Getter(value = TYPE_KEY, isStringConvertable = true)
	@XMLAttribute
	public Type getType();

	@Setter(TYPE_KEY)
	public void setType(Type type);

	/**
	 * We define an updater for TYPE property because we need to translate supplied Type to valid TypingSpace
	 * 
	 * @param type
	 */
	@Updater(TYPE_KEY)
	public void updateType(Type type);

	public static abstract class AbstractPropertyImpl<T> extends FlexoPropertyImpl<T> implements AbstractProperty<T> {

		@Override
		public boolean defaultBehaviourIsToBeDeleted() {
			return false;
		}

		/**
		 * Return flag indicating whether this property is abstract
		 * 
		 * @return
		 */
		@Override
		public boolean isAbstract() {
			return true;
		}

		/**
		 * Return boolean indicating if this {@link FlexoProperty} is notification-safe (all modifications of data retrived from that
		 * property are notified using {@link PropertyChangeSupport} scheme)<br>
		 * 
		 * When tagged as unsafe, disable caching while evaluating related {@link DataBinding}.
		 * 
		 * @return
		 */
		@Override
		public boolean isNotificationSafe() {
			return true;
		}

		/**
		 * We define an updater for TYPE property because we need to translate supplied Type to valid TypingSpace
		 * 
		 * This updater is called during updateWith() processing (generally applied during the FML parsing phases)
		 * 
		 * @param type
		 */
		@Override
		public void updateType(Type type) {

			if (getDeclaringCompilationUnit() != null && type instanceof ConnieType) {
				setType(((ConnieType) type).translateTo(getDeclaringCompilationUnit().getTypingSpace()));
			}
			else {
				setType(type);
			}
		}

	}
}
