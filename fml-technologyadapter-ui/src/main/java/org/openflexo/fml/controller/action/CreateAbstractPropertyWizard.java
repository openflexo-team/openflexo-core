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

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.action.CreateAbstractProperty;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.view.controller.FlexoController;

public class CreateAbstractPropertyWizard extends AbstractCreateFlexoPropertyWizard<CreateAbstractProperty> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateAbstractPropertyWizard.class.getPackage().getName());

	private static final String NO_PROPERTY_TYPE = "please_choose_a_property_type";

	public CreateAbstractPropertyWizard(CreateAbstractProperty action, FlexoController controller) {
		super(action, controller);
	}

	@Override
	protected DescribeAbstractProperty makeDescriptionStep() {
		return new DescribeAbstractProperty();
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_abstract_property");
	}

	@Override
	public DescribeAbstractProperty getDescribeProperty() {
		return (DescribeAbstractProperty) super.getDescribeProperty();
	}

	/**
	 * This step is used to set new property parameters
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeAbstractProperty.fib")
	public class DescribeAbstractProperty extends DescribeProperty {

		@Override
		public CreateAbstractProperty getAction() {
			return super.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_abstract_property");
		}

		@Override
		public boolean isValid() {

			if (!super.isValid()) {
				return false;
			}

			if (getPropertyType() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_PROPERTY_TYPE), IssueMessageType.ERROR);
				return false;
			}

			return true;
		}

		public Type getPropertyType() {
			return getAction().getPropertyType();
		}

		public void setPropertyType(Type type) {
			if (getPropertyType() != type) {
				Type oldValue = getPropertyType();
				getAction().setPropertyType(type);
				getPropertyChangeSupport().firePropertyChange("propertyType", oldValue, type);
				checkValidity();
			}
		}

	}

}
