/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.action;

import java.util.logging.Logger;

import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.view.controller.FlexoController;

public class CreatePrimitiveRoleWizard extends AbstractCreateFlexoRoleWizard<CreatePrimitiveRole, ModelSlot<?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreatePrimitiveRoleWizard.class.getPackage().getName());

	private static final String NO_PRIMITIVE_TYPE = "please_choose_primitive_type";

	public CreatePrimitiveRoleWizard(CreatePrimitiveRole action, FlexoController controller) {
		super(action, controller);
	}

	@Override
	protected DescribePrimitiveRole makeDescriptionStep() {
		return new DescribePrimitiveRole();
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_primitive_role");
	}

	@Override
	public DescribePrimitiveRole getDescribeProperty() {
		return (DescribePrimitiveRole) super.getDescribeProperty();
	}

	/**
	 * This step is used to set general data on role
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribePrimitiveRole.fib")
	public class DescribePrimitiveRole extends AbstractDescribeFlexoRole {

		@Override
		public boolean isValid() {

			if (!super.isValid()) {
				return false;
			}

			if (getPrimitiveType() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_PRIMITIVE_TYPE), IssueMessageType.ERROR);
				return false;
			}

			return true;
		}

		public PrimitiveType getPrimitiveType() {
			return getAction().getPrimitiveType();
		}

		public void setPrimitiveType(PrimitiveType primitiveType) {
			if (getPrimitiveType() != primitiveType) {
				PrimitiveType oldValue = getPrimitiveType();
				getAction().setPrimitiveType(primitiveType);
				getPropertyChangeSupport().firePropertyChange("primitiveType", oldValue, primitiveType);
				checkValidity();
			}
		}

	}

}
