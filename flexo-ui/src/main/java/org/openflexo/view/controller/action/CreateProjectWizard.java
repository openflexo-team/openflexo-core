/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Freemodellingeditor, a component of the software infrastructure 
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

package org.openflexo.view.controller.action;

import java.awt.Dimension;
import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.action.CreateProject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.project.FlexoProjectResource;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateProjectWizard extends FlexoActionWizard<CreateProject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateProjectWizard.class.getPackage().getName());

	private final ConfigureNewProject configureNewProject;

	private static final Dimension DIMENSIONS = new Dimension(700, 450);

	public CreateProjectWizard(CreateProject action, FlexoController controller) {
		super(action, controller);
		addStep(configureNewProject = new ConfigureNewProject());
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_openflexo_project");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.OPENFLEXO_NOTEXT_64, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public ConfigureNewProject getConfigureFormoseProject() {
		return configureNewProject;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/ConfigureNewProject.fib")
	public class ConfigureNewProject extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateProject getAction() {
			return CreateProjectWizard.this.getAction();
		}

		public RepositoryFolder<FlexoProjectResource<?>, ?> getRepositoryFolder() {
			return getAction().getFocusedObject();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("configure_new_project");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getProjectName())) {
				setIssueMessage(getAction().getLocales().localizedForKey("you_must_define_project_name"), IssueMessageType.ERROR);
				return false;
			}
			if (StringUtils.isEmpty(getProjectURI())) {
				setIssueMessage(getAction().getLocales().localizedForKey("you_must_define_project_uri"), IssueMessageType.ERROR);
				return false;
			}
			if (getAction().isDefaultProjectURI()) {
				setIssueMessage(getAction().getLocales().localizedForKey("don't_you_want_to_set_a_more_user_friendly_uri_for_your_project"),
						IssueMessageType.WARNING);
			}

			return true;

		}

		public String getProjectName() {
			return getAction().getNewProjectName();
		}

		public void setProjectName(String projectName) {
			if (!projectName.equals(getProjectName())) {
				String oldValue = getProjectName();
				getAction().setNewProjectName(projectName);
				getPropertyChangeSupport().firePropertyChange("projectName", oldValue, projectName);
				getPropertyChangeSupport().firePropertyChange("projectURI", null, getProjectURI());
				checkValidity();
			}
		}

		public String getProjectURI() {
			return getAction().getNewProjectURI();
		}

		public void setProjectURI(String projectURI) {
			if (!projectURI.equals(getProjectURI())) {
				String oldValue = getProjectURI();
				getAction().setNewProjectURI(projectURI);
				getPropertyChangeSupport().firePropertyChange("projectURI", oldValue, projectURI);
				checkValidity();
			}
		}

		public String getDescription() {
			return getAction().getNewProjectDescription();
		}

		public void setDescription(String newDescription) {
			if (!newDescription.equals(getDescription())) {
				String oldValue = getDescription();
				getAction().setNewProjectDescription(newDescription);
				getPropertyChangeSupport().firePropertyChange("newDescription", oldValue, newDescription);
				checkValidity();
			}
		}

	}

}
