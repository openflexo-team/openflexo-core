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

import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateVirtualModel;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateVirtualModelWizard extends AbstractCreateVirtualModelWizard<CreateVirtualModel> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateVirtualModelWizard.class.getPackage().getName());

	private final DescribeVirtualModel describeVirtualModel;

	public CreateVirtualModelWizard(CreateVirtualModel action, FlexoController controller) {
		super(action, controller);
		addStep(describeVirtualModel = new DescribeVirtualModel());
		appendConfigureModelSlots();
	}

	@Override
	public String getWizardTitle() {
		return FlexoLocalization.localizedForKey("create_virtual_model");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.VIRTUAL_MODEL_BIG_ICON, IconLibrary.NEW_32_32).getImage();
	}

	public DescribeVirtualModel getDescribeVirtualModel() {
		return describeVirtualModel;
	}

	@Override
	public ViewPoint getViewPoint() {
		return getAction().getFocusedObject();
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeVirtualModel.fib")
	public class DescribeVirtualModel extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateVirtualModel getAction() {
			return CreateVirtualModelWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("describe_virtual_model");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getNewVirtualModelName())) {
				setIssueMessage(FlexoLocalization.localizedForKey("please_supply_valid_virtual_model_name"), IssueMessageType.ERROR);
				return false;
			} else if (getAction().getFocusedObject().getVirtualModelNamed(getNewVirtualModelName()) != null) {
				setIssueMessage(FlexoLocalization.localizedForKey("duplicated_virtual_model_name"), IssueMessageType.ERROR);
				return false;
			} else if (StringUtils.isEmpty(getNewVirtualModelDescription())) {
				setIssueMessage(FlexoLocalization.localizedForKey("it_is_recommanded_to_describe_virtual_model"), IssueMessageType.WARNING);
			}

			return true;
		}

		public String getNewVirtualModelName() {
			return getAction().getNewVirtualModelName();
		}

		public void setNewVirtualModelName(String newViewPointName) {
			if (!newViewPointName.equals(getNewVirtualModelName())) {
				String oldValue = getNewVirtualModelName();
				getAction().setNewVirtualModelName(newViewPointName);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelName", oldValue, newViewPointName);
				checkValidity();
			}
		}

		public String getNewVirtualModelDescription() {
			return getAction().getNewVirtualModelDescription();
		}

		public void setNewVirtualModelDescription(String newViewPointDescription) {
			if (!newViewPointDescription.equals(getNewVirtualModelDescription())) {
				String oldValue = getNewVirtualModelDescription();
				getAction().setNewVirtualModelDescription(newViewPointDescription);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelDescription", oldValue, newViewPointDescription);
				checkValidity();
			}
		}

	}

}
