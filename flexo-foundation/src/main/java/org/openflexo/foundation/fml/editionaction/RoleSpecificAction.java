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

import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;

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
public abstract interface RoleSpecificAction<R extends FlexoRole<T>, MS extends ModelSlot<?>, T> extends TechnologySpecificAction<MS, T> {

	@PropertyIdentifier(type = FlexoRole.class)
	public static final String FLEXO_ROLE_KEY = "flexoRole";

	@Getter(value = FLEXO_ROLE_KEY)
	@XMLElement(primary = false, context = "Accessed")
	public R getFlexoRole();

	@Setter(FLEXO_ROLE_KEY)
	public void setFlexoRole(R role);

	public static abstract class RoleSpecificActionImpl<R extends FlexoRole<T>, MS extends ModelSlot<?>, T>
			extends TechnologySpecificActionImpl<MS, T>implements RoleSpecificAction<R, MS, T> {

		private static final Logger logger = Logger.getLogger(RoleSpecificAction.class.getPackage().getName());

		@Override
		public MS getModelSlot() {
			if (getFlexoRole() != null) {
				return (MS) getFlexoRole().getModelSlot();
			}
			return super.getModelSlot();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			return getFlexoRole().getName() + "." + getTechnologyAdapterIdentifier() + "::" + getImplementedInterface().getSimpleName()
					+ "()";
		}

	}

}
