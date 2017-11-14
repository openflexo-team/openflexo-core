/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.action;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.project.FlexoProjectResource;
import org.openflexo.foundation.project.FlexoProjectResourceFactory;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * This action allows to create a new {@link FlexoProject} in a {@link RepositoryFolder}<br>
 * 
 * @author sylvain
 * 
 */
public class CreateProject extends FlexoAction<CreateProject, RepositoryFolder<FlexoProjectResource, ?>, FlexoObject> {

	private static final Logger logger = Logger.getLogger(CreateProject.class.getPackage().getName());

	public static FlexoActionFactory<CreateProject, RepositoryFolder<FlexoProjectResource, ?>, FlexoObject> actionType = new FlexoActionFactory<CreateProject, RepositoryFolder<FlexoProjectResource, ?>, FlexoObject>(
			"create_project", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateProject makeNewAction(RepositoryFolder<FlexoProjectResource, ?> focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new CreateProject(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(RepositoryFolder<FlexoProjectResource, ?> object, Vector<FlexoObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(RepositoryFolder<FlexoProjectResource, ?> object, Vector<FlexoObject> globalSelection) {
			return true;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateProject.actionType, RepositoryFolder.class);
	}

	private String newProjectName;
	private String newProjectURI;
	private String newProjectDescription;
	private FlexoProject newFlexoProject;

	private Object serializationArtefact;

	CreateProject(RepositoryFolder<FlexoProjectResource, ?> focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		logger.info("Create new FlexoProject");

		System.out.println("Create new FlexoProject");
		System.out.println("getEditor()=" + getEditor());
		System.out.println("getServiceManager()=" + getServiceManager());
		System.out.println("getSerializationArtefact()=" + getSerializationArtefact());

		FlexoProjectResourceFactory factory = getServiceManager().getProjectLoaderService().getFlexoProjectResourceFactory();

		try {

			FlexoProjectResource newProjectResource = null;
			if (getFocusedObject() != null) {
				System.out.println("Hop, on cree un projet dans un RC");
				newProjectResource = factory.makeFlexoProjectResource(getNewProjectName(), getNewProjectURI(), getFocusedObject(), true);
				System.out.println("et donc =" + newProjectResource.getDelegateResourceCenter());
			}
			else {
				newProjectResource = factory.makeFlexoProjectResource(getSerializationArtefact(), getNewProjectURI(), true);
			}

			newFlexoProject = newProjectResource.getLoadedResourceData();
			newFlexoProject.setProjectDescription(getNewProjectDescription());
			newFlexoProject.setLastUniqueID(0);
			newFlexoProject.setCreationUserId(FlexoObjectImpl.getCurrentUserIdentifier());
			newFlexoProject.setCreationDate(new Date());
			newProjectResource.save(null);

			// We add the newly created project as a ResourceCenter
			// Maybe this will be done now, but it may also be done in a task
			// In this case, we have to reference the task to wait for its execution

			System.out.println("Hop, newProjectResource=" + newProjectResource);
			System.out.println("Hop, newProjectResource.getDelegateResourceCenter()=" + newProjectResource.getDelegateResourceCenter());

			FlexoTask addResourceCenterTask = getServiceManager().resourceCenterAdded(newFlexoProject);
			if (addResourceCenterTask != null) {
				getServiceManager().getTaskManager().waitTask(addResourceCenterTask);
			}

		} catch (SaveResourceException e) {
			throw new SaveResourceException(null);
		} catch (ModelDefinitionException e) {
			throw new FlexoException(e);
		}

	}

	public FlexoProject<?> getNewFlexoProject() {
		return newFlexoProject;
	}

	public String getNewProjectName() {
		return newProjectName;
	}

	public void setNewProjectName(String newViewPointName) {
		this.newProjectName = newViewPointName;
		getPropertyChangeSupport().firePropertyChange("newProjectName", null, newViewPointName);
		getPropertyChangeSupport().firePropertyChange("newProjectURI", null, getNewProjectURI());
	}

	public String getNewProjectURI() {
		if (newProjectURI == null && getFocusedObject() != null) {
			String baseURI = getFocusedObject().getDefaultBaseURI();
			if (!baseURI.endsWith("/")) {
				baseURI = baseURI + "/";
			}
			return baseURI + getBaseName() + VirtualModelResourceFactory.FML_SUFFIX;
		}

		return newProjectURI;
	}

	public void setNewProjectURI(String newProjectURI) {
		this.newProjectURI = newProjectURI;
		getPropertyChangeSupport().firePropertyChange("newProjectURI", null, newProjectURI);

	}

	public String getNewProjectDescription() {
		return newProjectDescription;
	}

	public void setNewProjectDescription(String newProjectDescription) {
		this.newProjectDescription = newProjectDescription;
		getPropertyChangeSupport().firePropertyChange("newProjectDescription", null, newProjectDescription);
	}

	public boolean isNewProjectNameValid() {
		if (StringUtils.isEmpty(getNewProjectName())) {
			return false;
		}
		return true;
	}

	public boolean isNewProjectURIValid() {
		if (StringUtils.isEmpty(getNewProjectURI())) {
			return false;
		}
		try {
			new URL(getNewProjectURI());
		} catch (MalformedURLException e) {
			return false;
		}

		if (getServiceManager() == null) {
			return false;
		}
		if (getServiceManager().getResourceCenterService().getFlexoResourceCenter(getNewProjectURI()) != null) {
			return false;
		}

		return true;
	}

	@Override
	public boolean isValid() {
		if (!isNewProjectNameValid()) {
			return false;
		}
		if (!isNewProjectURIValid()) {
			return false;
		}
		return true;
	}

	public FlexoProject<?> getNewProject() {
		return newFlexoProject;
	}

	private String getBaseName() {
		return JavaUtils.getClassName(getNewProjectName());
	}

	// " | ? * [ ] / < > = { } & % # ~ \ _
	public static final String BAD_CHARACTERS_REG_EXP = "[\"|\\?\\*\\[\\]/<>:{}&%#~\\\\_]";

	public static final Pattern BAD_CHARACTERS_PATTERN = Pattern.compile(BAD_CHARACTERS_REG_EXP);

	public static String nameFromDirectory(File projectDirectory) {
		String projectName = projectDirectory.getName().replaceAll(BAD_CHARACTERS_REG_EXP, " ");
		if (projectName.endsWith(".prj")) {
			projectName = projectName.substring(0, projectName.length() - 4);
		}
		else {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Project directory does not end with '.prj'");
			}
		}
		return projectName;
	}

	public Object getSerializationArtefact() {
		return serializationArtefact;
	}

	public void setSerializationArtefact(Object serializationArtefact) {
		if ((serializationArtefact == null && this.serializationArtefact != null)
				|| (serializationArtefact != null && !serializationArtefact.equals(this.serializationArtefact))) {
			Object oldValue = this.serializationArtefact;
			this.serializationArtefact = serializationArtefact;
			getPropertyChangeSupport().firePropertyChange("serializationArtefact", oldValue, serializationArtefact);
		}
	}

}
