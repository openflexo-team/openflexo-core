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
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.binding.FlexoConceptFlexoPropertyPathElement;
import org.openflexo.foundation.fml.binding.FlexoRoleBindingVariable;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;

/**
 * Represents an {@link TechnologySpecificAction} which address a specific technology object accessible as a {@link FlexoRole}
 * 
 * Such action must access via the getReceiver§) property to an object whose type match type declared by the {@link FlexoRole}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(RoleSpecificAction.RoleSpecificActionImpl.class)
public abstract interface RoleSpecificAction<R extends FlexoRole<T>, MS extends ModelSlot<?>, T extends TechnologyObject<?>>
		extends TechnologySpecificAction<MS, T, T> {

	@PropertyIdentifier(type = FlexoRole.class)
	public static final String FLEXO_ROLE_KEY = "flexoRole";

	@Deprecated
	@Getter(value = FLEXO_ROLE_KEY)
	@XMLElement(primary = false, context = "Accessed")
	public R getFlexoRole();

	@Deprecated
	@Setter(FLEXO_ROLE_KEY)
	public void setFlexoRole(R role);

	/**
	 * Compute and return infered {@link FlexoRole} from getReceiver() binding<br>
	 * Please not that infered role might be null if receiver value is not given through a {@link FlexoRole}
	 * 
	 * @return role beeing addressed
	 */
	public R getInferedFlexoRole();

	public static abstract class RoleSpecificActionImpl<R extends FlexoRole<T>, MS extends ModelSlot<?>, T extends TechnologyObject<?>>
			extends TechnologySpecificActionImpl<MS, T, T> implements RoleSpecificAction<R, MS, T> {

		private static final Logger logger = Logger.getLogger(RoleSpecificAction.class.getPackage().getName());

		@Deprecated
		@Override
		public void setFlexoRole(R role) {

			if (role != null) {
				getReceiver().setUnparsedBinding(role.getName());
			}
		}

		@Deprecated
		@Override
		public MS getDeprecatedModelSlot() {
			return null;
		}

		@Deprecated
		@Override
		public void setDeprecatedModelSlot(MS modelSlot) {
			// Do nothing, this will be handled by setFlexoRole()
		}

		/**
		 * Return a string representation suitable for a common user<br>
		 * This representation will used in all GUIs
		 */
		@Override
		public String getStringRepresentation() {
			return (getFlexoRole() != null ? getFlexoRole().getName() + "." : "") + super.getStringRepresentation();
		}

		/**
		 * Compute and return infered {@link FlexoRole} from getReceiver() binding<br>
		 * Please not that infered role might be null if receiver value is not given through a {@link FlexoRole}
		 * 
		 * @return role beeing addressed
		 */
		@Override
		public R getInferedFlexoRole() {
			if (getReceiver().isSet() && getReceiver().isValid() && getReceiver().isBindingValue()) {
				BindingValue bindingValue = ((BindingValue) getReceiver().getExpression());
				IBindingPathElement lastPathElement = bindingValue.getLastBindingPathElement();
				if (lastPathElement instanceof FlexoRoleBindingVariable) {
					return (R) ((FlexoRoleBindingVariable) lastPathElement).getFlexoRole();
				}
				else if (lastPathElement instanceof FlexoConceptFlexoPropertyPathElement
						&& ((FlexoConceptFlexoPropertyPathElement) lastPathElement).getFlexoProperty() instanceof FlexoRole) {
					return (R) ((FlexoConceptFlexoPropertyPathElement) lastPathElement).getFlexoProperty();
				}
			}
			return null;
		}

	}

}
