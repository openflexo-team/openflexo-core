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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateTopLevelVirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateTopLevelVirtualModelWizard extends AbstractCreateVirtualModelWizard<CreateTopLevelVirtualModel> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateTopLevelVirtualModelWizard.class.getPackage().getName());

	private final DescribeTopLevelVirtualModel describeTopLevelVirtualModel;

	public CreateTopLevelVirtualModelWizard(CreateTopLevelVirtualModel action, FlexoController controller) {
		super(action, controller);
		addStep(describeTopLevelVirtualModel = new DescribeTopLevelVirtualModel());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_view_point");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.VIEWPOINT_BIG_ICON, IconLibrary.NEW_32_32).getImage();
	}

	public DescribeTopLevelVirtualModel getDescribeViewPoint() {
		return describeTopLevelVirtualModel;
	}

	@Override
	public VirtualModel getContainerVirtualModel() {
		return null;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeTopLevelVirtualModel.fib")
	public class DescribeTopLevelVirtualModel extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateTopLevelVirtualModel getAction() {
			return CreateTopLevelVirtualModelWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_view_point");
		}

		@Override
		public boolean isValid() {

			if (getViewPointFolder() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("no_folder_defined"), IssueMessageType.ERROR);
				return false;
			}
			else if (StringUtils.isEmpty(getNewVirtualModelName())) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_supply_valid_view_point_name"), IssueMessageType.ERROR);
				return false;
			}
			else if (StringUtils.isEmpty(getNewVirtualModelURI())) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_supply_uri"), IssueMessageType.ERROR);
				return false;
			}
			else if (!isValidURI()) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_supply_valid_uri"), IssueMessageType.ERROR);
				return false;
			}
			else if (getAction().getVirtualModelLibrary() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("could_not_access_viewpoint_library"), IssueMessageType.ERROR);
				return false;
			}
			else if (getAction().getVirtualModelLibrary().getVirtualModelResource(getNewVirtualModelURI()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey("already_existing_viewpoint_uri"), IssueMessageType.ERROR);
				return false;
			}
			else if (getViewPointFolder().getResourceWithName(getNewVirtualModelName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey("already_existing_viewpoint_name"), IssueMessageType.ERROR);
				return false;
			}
			else if (StringUtils.isEmpty(getNewVirtualModelDescription())) {
				setIssueMessage(getAction().getLocales().localizedForKey("it_is_recommanded_to_describe_view_point"),
						IssueMessageType.WARNING);
			}

			return true;
		}

		public RepositoryFolder<VirtualModelResource, ?> getViewPointFolder() {
			return getAction().getVirtualModelFolder();
		}

		public String getNewVirtualModelName() {
			return getAction().getNewVirtualModelName();
		}

		public void setNewVirtualModelName(String newViewPointName) {
			if (!newViewPointName.equals(getNewVirtualModelName())) {
				String oldValue = getNewVirtualModelName();
				getAction().setNewVirtualModelName(newViewPointName);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelName", oldValue, newViewPointName);
				getPropertyChangeSupport().firePropertyChange("newVirtuelModelURI", null, getNewVirtualModelURI());
				checkValidity();
			}
		}

		public String getNewVirtualModelURI() {
			return getAction().getNewVirtualModelURI();
		}

		public void setNewVirtualModelURI(String newVirtualModelURI) {
			if (!newVirtualModelURI.equals(getNewVirtualModelURI())) {
				String oldValue = getNewVirtualModelURI();
				getAction().setNewVirtualModelURI(newVirtualModelURI);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelURI", oldValue, newVirtualModelURI);
				checkValidity();
			}
		}

		private boolean isValidURI() {
			try {
				new URL(getNewVirtualModelURI());
			} catch (MalformedURLException e) {
				return false;
			}
			return true;
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
