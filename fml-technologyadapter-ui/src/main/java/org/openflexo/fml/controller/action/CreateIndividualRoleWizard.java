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

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.fml.IndividualRole;
import org.openflexo.foundation.ontology.fml.action.CreateIndividualRole;
import org.openflexo.foundation.ontology.technologyadapter.FlexoOntologyModelSlot;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.view.controller.FlexoController;

public class CreateIndividualRoleWizard extends AbstractCreateFlexoRoleWizard<CreateIndividualRole, FlexoOntologyModelSlot<?, ?, ?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateIndividualRoleWizard.class.getPackage().getName());

	private static final String NO_INDIVIDUAL_TYPE = "please_choose_individual_type";

	public CreateIndividualRoleWizard(CreateIndividualRole action, FlexoController controller) {
		super(action, controller);
	}

	@Override
	protected DescribeIndividualRole makeDescriptionStep() {
		return new DescribeIndividualRole();
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_individual_role");
	}

	@Override
	public DescribeIndividualRole getDescribeProperty() {
		return (DescribeIndividualRole) super.getDescribeProperty();
	}

	/**
	 * This step is used to set general data on role
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeIndividualRole.fib")
	public class DescribeIndividualRole extends AbstractDescribeFlexoRole {

		@Override
		public boolean isValid() {

			if (!super.isValid()) {
				return false;
			}

			if (getFlexoRoleClass() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_ROLE_TYPE), IssueMessageType.ERROR);
				return false;
			}

			if (getIndividualType() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_INDIVIDUAL_TYPE), IssueMessageType.ERROR);
				return false;
			}

			return true;
		}

		@Override
		public void setModelSlot(FlexoOntologyModelSlot<?, ?, ?> modelSlot) {
			super.setModelSlot(modelSlot);
			getPropertyChangeSupport().firePropertyChange("availableFlexoRoleTypes", null, getAvailableFlexoRoleTypes());
			checkValidity();
		}

		@Override
		public Class<? extends IndividualRole<?>> getFlexoRoleClass() {
			return getAction().getFlexoRoleClass();
		}

		public void setFlexoRoleClass(Class<? extends IndividualRole<?>> flexoRoleClass) {
			if (getFlexoRoleClass() != flexoRoleClass) {
				Class<? extends FlexoRole> oldValue = getFlexoRoleClass();
				getAction().setFlexoRoleClass(flexoRoleClass);
				getPropertyChangeSupport().firePropertyChange("flexoRoleClass", oldValue, flexoRoleClass);
				getPropertyChangeSupport().firePropertyChange("roleName", null, getRoleName());
				checkValidity();
			}
		}

		public List<Class<? extends IndividualRole<?>>> getAvailableFlexoRoleTypes() {
			return getAction().getAvailableFlexoRoleTypes();
		}

		public IFlexoOntologyClass<?> getIndividualType() {
			return getAction().getIndividualType();
		}

		public void setIndividualType(IFlexoOntologyClass<?> individualType) {
			if (getIndividualType() != individualType) {
				IFlexoOntologyClass<?> oldValue = getIndividualType();
				getAction().setIndividualType(individualType);
				getPropertyChangeSupport().firePropertyChange("individualType", oldValue, individualType);
				checkValidity();
			}
		}

	}

}
