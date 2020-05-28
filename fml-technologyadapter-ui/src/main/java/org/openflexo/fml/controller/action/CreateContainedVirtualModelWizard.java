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
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.foundation.fml.action.CreateContainedVirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateContainedVirtualModelWizard extends AbstractCreateVirtualModelWizard<CreateContainedVirtualModel> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateContainedVirtualModelWizard.class.getPackage().getName());

	private final DescribeVirtualModel describeVirtualModel;

	public CreateContainedVirtualModelWizard(CreateContainedVirtualModel action, FlexoController controller) {
		super(action, controller);
		addStep(describeVirtualModel = new DescribeVirtualModel());
		createAdditionalSteps();
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_virtual_model");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.VIRTUAL_MODEL_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public DescribeVirtualModel getDescribeVirtualModel() {
		return describeVirtualModel;
	}

	@Override
	public VirtualModel getContainerVirtualModel() {
		return getAction().getFocusedObject().getVirtualModel();
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeVirtualModel.fib")
	public class DescribeVirtualModel extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateContainedVirtualModel getAction() {
			return CreateContainedVirtualModelWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_virtual_model");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getNewVirtualModelName())) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_supply_valid_virtual_model_name"), IssueMessageType.ERROR);
				return false;
			}
			else if (getAction().getFocusedObject().getVirtualModelNamed(getNewVirtualModelName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey("duplicated_virtual_model_name"), IssueMessageType.ERROR);
				return false;
			}
			else if (StringUtils.isEmpty(getNewVirtualModelDescription())) {
				setIssueMessage(getAction().getLocales().localizedForKey("it_is_recommanded_to_describe_virtual_model"),
						IssueMessageType.WARNING);
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
				getPropertyChangeSupport().firePropertyChange("newVirtualModelURI", null, getNewVirtualModelURI());
				checkValidity();
			}
		}

		public String getNewVirtualModelURI() {
			return getAction().getNewVirtualModelURI();
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

		public boolean getIsAbstract() {
			return getAction().getIsAbstract();
		}

		public void setIsAbstract(boolean isAbstract) {
			if (isAbstract != getIsAbstract()) {
				getAction().setIsAbstract(isAbstract);
				getPropertyChangeSupport().firePropertyChange("isAbstract", !isAbstract, isAbstract);
				checkValidity();
			}
		}

		public Visibility getVisibility() {
			return getAction().getVisibility();
		}

		public void setVisibility(Visibility visibility) {
			if ((visibility == null && getVisibility() != null) || (visibility != null && !visibility.equals(getVisibility()))) {
				Visibility oldValue = getVisibility();
				getAction().setVisibility(visibility);
				getPropertyChangeSupport().firePropertyChange("visibility", oldValue, visibility);
				checkValidity();
			}
		}

	}

}
