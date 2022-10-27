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
 * Represents an {@link TechnologySpecificAction} which address a specific technology object accessible as a {@link FlexoRole}
 * 
 * Such action must access via the {@link #getReceiver()} property to an object whose type match type declared by the {@link FlexoRole}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(RoleSpecificAction.RoleSpecificActionImpl.class)
public abstract interface RoleSpecificAction<R extends FlexoRole<T>, MS extends ModelSlot<?>, T extends TechnologyObject<?>>
		extends TechnologySpecificActionDefiningReceiver<MS, T, T> {

	/**
	 * Compute and return infered {@link FlexoRole} from getReceiver() binding<br>
	 * Please note that infered role might be null if receiver value is not given through a {@link FlexoRole}
	 * 
	 * @return role beeing addressed
	 */
	public R getInferedFlexoRole();

	/**
	 * Compute and return assigned flexo role asserting this action is assigned to requested {@link FlexoRole}<br>
	 * 
	 * Please not there is absolutely no guarantee that this {@link EditionAction} is assigned to a {@link FlexoRole}<br>
	 * 
	 * @return null if this {@link EditionAction} is not assigned to a {@link ModelSlot}
	 */
	public R getAssignedFlexoRole();

	/**
	 * Return type of {@link FlexoRole} this {@link EditionAction} refer to
	 * 
	 * @return
	 */
	public Class<? extends R> getFlexoRoleClass();

	public static abstract class RoleSpecificActionImpl<R extends FlexoRole<T>, MS extends ModelSlot<?>, T extends TechnologyObject<?>>
			extends TechnologySpecificActionDefiningReceiverImpl<MS, T, T> implements RoleSpecificAction<R, MS, T> {

		private static final Logger logger = Logger.getLogger(RoleSpecificAction.class.getPackage().getName());

		/**
		 * Return a string representation suitable for a common user<br>
		 * This representation will used in all GUIs
		 */
		@Override
		public String getStringRepresentation() {
			return (getInferedFlexoRole() != null ? getInferedFlexoRole().getName() + "." : "") + super.getStringRepresentation();
		}

		/**
		 * Compute and return infered {@link FlexoRole} from getReceiver() binding<br>
		 * Please not that infered role might be null if receiver value is not given through a {@link FlexoRole}
		 * 
		 * @return role beeing addressed
		 */
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

		/**
		 * Return type of {@link FlexoRole} this {@link EditionAction} refer to
		 * 
		 * @return
		 */
		@SuppressWarnings("unchecked")
		@Override
		public final Class<? extends R> getFlexoRoleClass() {
			return (Class<? extends R>) TypeUtils.getBaseClass(TypeUtils.getTypeArgument(getClass(), RoleSpecificAction.class, 0));
		}

		/**
		 * Compute and return assigned flexo role asserting this action is assigned to requested {@link FlexoRole}<br>
		 * 
		 * Please not there is absolutely no guarantee that this {@link EditionAction} is assigned to a {@link FlexoRole}<br>
		 * 
		 * @return null if this {@link EditionAction} is not assigned to a {@link ModelSlot}
		 */
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
