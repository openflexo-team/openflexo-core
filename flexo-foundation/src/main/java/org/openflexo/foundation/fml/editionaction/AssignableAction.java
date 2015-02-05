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

package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;

/**
 * Abstract class representing an {@link EditionAction} with the particularity of returning a value which can be assigned<br>
 * This value is of type T.<br>
 * An {@link AssignableAction} might be embedded in an {@link AbstractAssignationAction} ({@link AssignationAction} or
 * {@link DeclarationAction}) as right-hand side.
 * 
 * @author sylvain
 * 
 * @param <T>
 *            type of assignable
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AssignableAction.AssignableActionImpl.class)
public abstract interface AssignableAction<T> extends EditionAction {

	@Deprecated
	@PropertyIdentifier(type = String.class)
	public static final String DEPRECATED_VARIABLE_NAME_KEY = "deprecatedVariableName";
	@Deprecated
	@PropertyIdentifier(type = DataBinding.class)
	public static final String DEPRECATED_ASSIGNATION_KEY = "deprecatedAssignation";

	@Deprecated
	@Getter(value = DEPRECATED_VARIABLE_NAME_KEY)
	@XMLAttribute(xmlTag = "variableName")
	public String getDeprecatedVariableName();

	@Deprecated
	@Setter(DEPRECATED_VARIABLE_NAME_KEY)
	public void setDeprecatedVariableName(String variableName);

	@Deprecated
	@Getter(value = DEPRECATED_ASSIGNATION_KEY)
	@XMLAttribute(xmlTag = "assignation")
	public DataBinding<? super T> getDeprecatedAssignation();

	@Deprecated
	@Setter(DEPRECATED_ASSIGNATION_KEY)
	public void setDeprecatedAssignation(DataBinding<? super T> assignation);

	/**
	 * Execute edition action in the context provided by supplied {@link FlexoBehaviourAction}<br>
	 * 
	 * @param action
	 * @return
	 */
	@Override
	public T execute(FlexoBehaviourAction<?, ?, ?> action) throws FlexoException;

	/**
	 * Return role to which this action is bound with an assignation, if this action is the right-hand side of an {@link AssignationAction}
	 */
	public FlexoRole<T> getFlexoRole();

	/**
	 * Return type resulting of execution of this action
	 * 
	 * @return
	 */
	public Type getAssignableType();

	/**
	 * Return boolean indicating if assignable type resulting of this action is iterable
	 * 
	 * @return
	 */
	public boolean isIterable();

	/**
	 * Return type of iterated items when assignable type resulting of this action is iterable
	 * 
	 * @return
	 */
	public Type getIteratorType();

	public static abstract class AssignableActionImpl<T> extends EditionActionImpl implements AssignableAction<T> {

		private static final Logger logger = Logger.getLogger(AssignableAction.class.getPackage().getName());

		/**
		 * Return role to which this action is bound with an assignation, if this action is the right-hand side of an
		 * {@link AssignationAction}
		 */
		@Override
		public FlexoRole<T> getFlexoRole() {
			// We might find the FlexoRole is this action is the assignableAction of an AssignationAction
			if (getOwner() instanceof AssignationAction) {
				return ((AssignationAction) getOwner()).getFlexoRole();
			}
			return null;
		}

		@Override
		public abstract Type getAssignableType();

		@Override
		public boolean isIterable() {
			if (!TypeUtils.isTypeAssignableFrom(List.class, getAssignableType())) {
				return false;
			}
			return true;
		}

		@Override
		public Type getIteratorType() {
			if (!isIterable()) {
				return null;
			}
			if (getAssignableType() instanceof ParameterizedType) {
				return TypeUtils.getTypeArgument(getAssignableType(), List.class, 0);
			}
			return Object.class;
		}

		/**
		 * Execute edition action in the context provided by supplied {@link FlexoBehaviourAction}<br>
		 * 
		 * @param action
		 * @return
		 */
		@Override
		public abstract T execute(FlexoBehaviourAction<?, ?, ?> action) throws FlexoException;

	}

}
