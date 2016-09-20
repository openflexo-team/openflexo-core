/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-rt-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.rt.controller.action;

import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateViewInFolder;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.action.AbstractCreateVirtualModelInstanceWizard;

public class CreateViewWizard extends AbstractCreateVirtualModelInstanceWizard<CreateViewInFolder> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateViewWizard.class.getPackage().getName());

	// private final ChooseViewPoint chooseViewPoint;
	// private final ChooseAndConfigureCreationScheme chooseAndConfigureCreationScheme = null;

	public CreateViewWizard(CreateViewInFolder action, FlexoController controller) {
		super(action, controller);
		// this.action = action;
		// addStep(chooseViewPoint = new ChooseViewPoint());
	}

	@Override
	protected ChooseViewPoint makeChooseVirtualModel() {
		return new ChooseViewPoint();
	}

	@Override
	protected ChooseAndConfigureCreationSchemeForViewPoint makeChooseAndConfigureCreationScheme() {
		return new ChooseAndConfigureCreationSchemeForViewPoint();
	}

	@Override
	public String getWizardTitle() {
		return action.getLocales().localizedForKey("create_view");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLRTIconLibrary.VIEW_BIG_ICON, IconLibrary.NEW_32_32).getImage();
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/Wizard/CreateView/ChooseViewPoint.fib")
	public class ChooseViewPoint extends AbstractChooseVirtualModel<ViewPoint> {

		@Override
		public String getTitle() {
			return action.getLocales().localizedForKey("choose_viewpoint");
		}

		@Override
		public boolean isValid() {

			if (getFolder() == null) {
				setIssueMessage(action.getLocales().localizedForKey("no_folder_defined"), IssueMessageType.ERROR);
				return false;
			}
			if (getViewPoint() == null) {
				setIssueMessage(action.getLocales().localizedForKey("no_viewpoint_selected"), IssueMessageType.ERROR);
				return false;
			}
			if (StringUtils.isEmpty(getNewVirtualModelInstanceName())) {
				setIssueMessage(action.getLocales().localizedForKey("no_view_name_defined"), IssueMessageType.ERROR);
				return false;
			}

			if (StringUtils.isEmpty(getNewVirtualModelInstanceTitle())) {
				setIssueMessage(action.getLocales().localizedForKey("no_view_title_defined"), IssueMessageType.ERROR);
				return false;
			}
			if (!action.isValidVirtualModelInstanceName(getNewVirtualModelInstanceName())) {
				setIssueMessage(action.getLocales().localizedForKey("a_view_with_that_name_already_exists"), IssueMessageType.ERROR);
				return false;
			}

			if (!getNewVirtualModelInstanceName().equals(JavaUtils.getClassName(getNewVirtualModelInstanceName()))
					&& !getNewVirtualModelInstanceName().equals(JavaUtils.getVariableName(getNewVirtualModelInstanceName()))) {
				setIssueMessage(action.getLocales().localizedForKey("discouraged_name_for_new_view"), IssueMessageType.WARNING);
			}

			return true;

		}

		public RepositoryFolder<ViewResource, ?> getFolder() {
			return action.getFolder();
		}

		public ViewPointResource getViewpointResource() {
			return action.getViewpointResource();
		}

		public void setViewpointResource(ViewPointResource viewpointResource) {
			if (viewpointResource != getViewpointResource()) {
				ViewPointResource oldValue = getViewpointResource();
				action.setViewpointResource(viewpointResource);
				getPropertyChangeSupport().firePropertyChange("viewpointResource", oldValue, viewpointResource);
				checkValidity();
			}
		}

		public ViewPoint getViewPoint() {
			if (getViewpointResource() != null) {
				return getViewpointResource().getViewPoint();
			}
			return null;
		}

	}

	@FIBPanel("Fib/Wizard/CreateView/ChooseAndConfigureCreationSchemeForViewPoint.fib")
	public class ChooseAndConfigureCreationSchemeForViewPoint extends AbstractChooseAndConfigureCreationScheme<ViewPoint> {

		public ChooseAndConfigureCreationSchemeForViewPoint() {
			super(action.getCreationSchemeAction());
		}
	}

}
