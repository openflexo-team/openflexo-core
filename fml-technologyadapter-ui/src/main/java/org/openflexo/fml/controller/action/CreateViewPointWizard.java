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
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateViewPoint;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateViewPointWizard extends AbstractCreateVirtualModelWizard<CreateViewPoint> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateViewPointWizard.class.getPackage().getName());

	private final DescribeViewPoint describeViewPoint;

	public CreateViewPointWizard(CreateViewPoint action, FlexoController controller) {
		super(action, controller);
		addStep(describeViewPoint = new DescribeViewPoint());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_view_point");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.VIEWPOINT_BIG_ICON, IconLibrary.NEW_32_32).getImage();
	}

	public DescribeViewPoint getDescribeViewPoint() {
		return describeViewPoint;
	}

	@Override
	public ViewPoint getViewPoint() {
		return null;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeViewPoint.fib")
	public class DescribeViewPoint extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateViewPoint getAction() {
			return CreateViewPointWizard.this.getAction();
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
			else if (StringUtils.isEmpty(getNewViewPointName())) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_supply_valid_view_point_name"), IssueMessageType.ERROR);
				return false;
			}
			else if (StringUtils.isEmpty(getNewViewPointURI())) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_supply_uri"), IssueMessageType.ERROR);
				return false;
			}
			else if (!isValidURI()) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_supply_valid_uri"), IssueMessageType.ERROR);
				return false;
			}
			else if (getAction().getViewPointLibrary() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("could_not_access_viewpoint_library"), IssueMessageType.ERROR);
				return false;
			}
			else if (getAction().getViewPointLibrary().getViewPointResource(getNewViewPointURI()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey("already_existing_viewpoint_uri"), IssueMessageType.ERROR);
				return false;
			}
			else if (getViewPointFolder().getResourceWithName(getNewViewPointName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey("already_existing_viewpoint_name"), IssueMessageType.ERROR);
				return false;
			}
			else if (StringUtils.isEmpty(getNewViewPointDescription())) {
				setIssueMessage(getAction().getLocales().localizedForKey("it_is_recommanded_to_describe_view_point"),
						IssueMessageType.WARNING);
			}

			return true;
		}

		@SuppressWarnings("unchecked")
		public RepositoryFolder<ViewPointResource> getViewPointFolder() {
			return getAction().getViewPointFolder();
		}

		public String getNewViewPointName() {
			return getAction().getNewViewPointName();
		}

		public void setNewViewPointName(String newViewPointName) {
			if (!newViewPointName.equals(getNewViewPointName())) {
				String oldValue = getNewViewPointName();
				getAction().setNewViewPointName(newViewPointName);
				getPropertyChangeSupport().firePropertyChange("newViewPointName", oldValue, newViewPointName);
				checkValidity();
			}
		}

		public String getNewViewPointURI() {
			return getAction().getNewViewPointURI();
		}

		public void setNewViewPointURI(String newViewPointURI) {
			if (!newViewPointURI.equals(getNewViewPointURI())) {
				String oldValue = getNewViewPointURI();
				getAction().setNewViewPointURI(newViewPointURI);
				getPropertyChangeSupport().firePropertyChange("newViewPointURI", oldValue, newViewPointURI);
				checkValidity();
			}
		}

		private boolean isValidURI() {
			try {
				new URL(getNewViewPointURI());
			} catch (MalformedURLException e) {
				return false;
			}
			return true;
		}

		public String getNewViewPointDescription() {
			return getAction().getNewViewPointDescription();
		}

		public void setNewViewPointDescription(String newViewPointDescription) {
			if (!newViewPointDescription.equals(getNewViewPointDescription())) {
				String oldValue = getNewViewPointDescription();
				getAction().setNewViewPointDescription(newViewPointDescription);
				getPropertyChangeSupport().firePropertyChange("newViewPointDescription", oldValue, newViewPointDescription);
				checkValidity();
			}
		}

	}

}
