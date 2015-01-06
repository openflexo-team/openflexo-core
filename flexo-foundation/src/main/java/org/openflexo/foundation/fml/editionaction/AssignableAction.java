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
package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.DataBinding;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.controlgraph.AssignableControlGraph;
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
public abstract interface AssignableAction<T> extends EditionAction, AssignableControlGraph<T> {

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

	@Override
	public Type getAssignableType();

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
