/**
 * 
 * Copyright (c) 2015, Openflexo
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

import java.util.logging.Logger;

import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.binding.FlexoPropertyPathElement;
import org.openflexo.foundation.fml.binding.FlexoRoleBindingVariable;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * A {@link TechnologySpecificActionDefiningReceiver} whose receiver ({@link #getReceiver()}) is a technology object reached through a
 * {@link FlexoRole}: the receiver and the action both have the type of the objects played by that role. The type argument {@code R}
 * designates the class of the role ({@link #getFlexoRoleClass()}).
 * <p>
 * Such actions are contributed by technology adapters, for instance the actions on paragraphs, tables and images of the docx technology
 * adapter.
 *
 * @author sylvain
 *
 * @param <R>
 *            type of the role through which the receiver is reached
 * @param <MS>
 *            type of the model slot class declaring this action
 * @param <T>
 *            type of the receiver, which is also the type of the assigned value
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(RoleSpecificAction.RoleSpecificActionImpl.class)
public abstract interface RoleSpecificAction<R extends FlexoRole<T>, MS extends ModelSlot<?,?>, T extends TechnologyObject<?>>
		extends TechnologySpecificActionDefiningReceiver<MS, T, T> {

	/**
	 * Return the role through which the receiver is reached, when the receiver binding ends with a role
	 *
	 * @return the role addressed by the receiver, null when the receiver is not given through a {@link FlexoRole}
	 */
	public R getInferedFlexoRole();

	/**
	 * Return the role assigned by this action, when this action is the right-hand side of an assignation whose target is a role of type
	 * {@code R}.
	 * <p>
	 * Beware: this method must only be called when this action is the right-hand side of the assignation of a property (see
	 * {@link #getAssignedFlexoProperty()}); otherwise it throws a {@link NullPointerException}.
	 *
	 * @return the assigned role, null when the assigned property is not a role of type {@code R}
	 */
	public R getAssignedFlexoRole();

	/**
	 * Return the class of the role addressed by this action, given by the type argument {@code R}
	 *
	 * @return the role class
	 */
	public Class<? extends R> getFlexoRoleClass();

	public static abstract class RoleSpecificActionImpl<R extends FlexoRole<T>, MS extends ModelSlot<?,?>, T extends TechnologyObject<?>>
			extends TechnologySpecificActionDefiningReceiverImpl<MS, T, T> implements RoleSpecificAction<R, MS, T> {

		private static final Logger logger = Logger.getLogger(RoleSpecificAction.class.getPackage().getName());

		/**
		 * Return a string representation suitable for a common user<br>
		 * This representation will be used in all GUIs
		 */
		@Override
		public String getStringRepresentation() {
			return (getInferedFlexoRole() != null ? getInferedFlexoRole().getName() + "." : "") + super.getStringRepresentation();
		}

		@Override
		public R getInferedFlexoRole() {
			if (getReceiver().isSet() && getReceiver().isValid() && getReceiver().isBindingPath()) {
				BindingPath bindingPath = ((BindingPath) getReceiver().getExpression());
				IBindingPathElement lastPathElement = bindingPath.getLastBindingPathElement();
				if (lastPathElement instanceof FlexoRoleBindingVariable) {
					return (R) ((FlexoRoleBindingVariable) lastPathElement).getFlexoRole();
				}
				else if (lastPathElement instanceof FlexoPropertyPathElement
						&& ((FlexoPropertyPathElement) lastPathElement).getFlexoProperty() instanceof FlexoRole) {
					return (R) ((FlexoPropertyPathElement) lastPathElement).getFlexoProperty();
				}
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public final Class<? extends R> getFlexoRoleClass() {
			return (Class<? extends R>) TypeUtils.getBaseClass(TypeUtils.getTypeArgument(getClass(), RoleSpecificAction.class, 0));
		}

		@SuppressWarnings("unchecked")
		@Override
		public R getAssignedFlexoRole() {
			if (getFlexoRoleClass().isAssignableFrom(getAssignedFlexoProperty().getClass())) {
				return (R) getAssignedFlexoProperty();
			}
			return null;
		}

	}

}
