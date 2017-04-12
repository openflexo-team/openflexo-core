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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;

/**
 * Represents an {@link TechnologySpecificAction} which address a specific technology through the reference to a {@link FlexoRole}
 * 
 * Such action must reference a {@link FlexoRole}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(RoleSpecificAction.RoleSpecificActionImpl.class)
public abstract interface RoleSpecificAction<R extends FlexoRole<T>, MS extends ModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<?>, T>
		extends TechnologySpecificAction<MS, RD, T> {

	@PropertyIdentifier(type = FlexoRole.class)
	public static final String FLEXO_ROLE_KEY = "flexoRole";

	@Getter(value = FLEXO_ROLE_KEY)
	@XMLElement(primary = false, context = "Accessed")
	public R getFlexoRole();

	@Setter(FLEXO_ROLE_KEY)
	public void setFlexoRole(R role);

	public Class<R> getRoleClass();

	public List<R> getAvailableRoles();

	public static abstract class RoleSpecificActionImpl<R extends FlexoRole<T>, MS extends ModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<?>, T>
			extends TechnologySpecificActionImpl<MS, RD, T> implements RoleSpecificAction<R, MS, RD, T> {

		private static final Logger logger = Logger.getLogger(RoleSpecificAction.class.getPackage().getName());

		@Override
		public MS getModelSlot() {
			if (getFlexoRole() != null) {
				return (MS) getFlexoRole().getModelSlot();
			}
			return (MS) performSuperGetter(TechnologySpecificAction.MODEL_SLOT_KEY);
		}

		/**
		 * Return a string representation suitable for a common user<br>
		 * This representation will used in all GUIs
		 */
		@Override
		public String getStringRepresentation() {
			return (getFlexoRole() != null ? getFlexoRole().getName() + "." : "") + super.getStringRepresentation();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			return (getFlexoRole() != null ? getFlexoRole().getName() : (getModelSlot() != null ? getModelSlot().getName() : "???")) + "."
					+ getTechnologyAdapterIdentifier() + "::" + getImplementedInterface().getSimpleName() + "()";
		}

		@Override
		public List<R> getAvailableRoles() {
			List<R> conceptRoles = getFlexoConcept().getAccessibleProperties(getRoleClass());
			List<R> vmRoles = null;
			if (getFlexoConcept() != null && getFlexoConcept().getOwningVirtualModel() != null) {
				vmRoles = getFlexoConcept().getOwningVirtualModel().getAccessibleProperties(getRoleClass());
			}
			if (conceptRoles.size() > 0) {
				if (vmRoles != null && vmRoles.size() > 0) {
					List<R> returned = new ArrayList<R>(vmRoles);
					returned.addAll(conceptRoles);
					return returned;
				}
				return conceptRoles;
			}
			if (vmRoles != null && vmRoles.size() > 0) {
				return vmRoles;
			}
			return conceptRoles;
		}

	}

	@DefineValidationRule
	public static class RoleSpecificActionMustReferenceARole
			extends ValidationRule<RoleSpecificActionMustReferenceARole, RoleSpecificAction> {
		public RoleSpecificActionMustReferenceARole() {
			super(RoleSpecificAction.class, "role_specific_action_must_adress_a_valid_role");
		}

		@Override
		public ValidationIssue<RoleSpecificActionMustReferenceARole, RoleSpecificAction> applyValidation(RoleSpecificAction action) {
			if (action.getFlexoRole() == null) {
				return new ValidationError<RoleSpecificActionMustReferenceARole, RoleSpecificAction>(this, action,
						"action_does_not_define_any_role");
			}
			return null;
		}
	}

}
