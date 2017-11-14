/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
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

package org.openflexo.foundation.project;

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.toolbox.IProgress;

/**
 * Default implementation for {@link FlexoProjectResource}
 * 
 * 
 * @author Sylvain
 * 
 */
public abstract class FlexoProjectResourceImpl extends PamelaResourceImpl<FlexoProject<?>, FlexoProjectFactory>
		implements FlexoProjectResource {

	static final Logger logger = Logger.getLogger(FlexoProjectResourceImpl.class.getPackage().getName());

	/**
	 * Instantiation of a delegate RC if project is stand-alone (not contained in another RC)
	 */
	private FlexoResourceCenter<?> delegateResourceCenter = null;

	@Override
	public FlexoResourceCenter<?> getDelegateResourceCenter() {
		return delegateResourceCenter;
	}

	@Override
	public void setDelegateResourceCenter(FlexoResourceCenter<?> delegateResourceCenter) {
		if ((delegateResourceCenter == null && this.delegateResourceCenter != null)
				|| (delegateResourceCenter != null && !delegateResourceCenter.equals(this.delegateResourceCenter))) {
			FlexoResourceCenter<?> oldValue = this.delegateResourceCenter;
			this.delegateResourceCenter = delegateResourceCenter;
			getPropertyChangeSupport().firePropertyChange("delegateResourceCenter", oldValue, delegateResourceCenter);
		}
	}

	@Override
	public FlexoProject<?> getFlexoProject() {
		try {
			return getResourceData(null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ResourceLoadingCancelledException e) {
			e.printStackTrace();
		} catch (FlexoException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Class<FlexoProject<?>> getResourceDataClass() {
		return (Class) FlexoProject.class;
	}

	@Override
	public FlexoProject<?> loadResourceData(IProgress progress) throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
			InconsistentDataException, InvalidModelDefinitionException {
		FlexoProject<?> returned = super.loadResourceData(progress);
		returned.setLastUniqueID(0);

		// We add the newly created project as a ResourceCenter
		FlexoTask addResourceCenterTask = getServiceManager().resourceCenterAdded(returned);

		// If resource center adding is executing in a task, we have to wait the task to be finished
		if (addResourceCenterTask != null) {
			getServiceManager().getTaskManager().waitTask(addResourceCenterTask);
		}

		return returned;
	}

	/*
	public static abstract class ProjectDataResourceImpl extends PamelaResourceImpl<ProjectData, ProjectDataFactory>
	implements ProjectDataResource {
	
	public static ProjectDataResource makeProjectDataResource(FlexoProject project) {
	try {
		ModelFactory resourceFactory = new ModelFactory(
				ModelContextLibrary.getCompoundModelContext(FileIODelegate.class, ProjectDataResource.class));
		ProjectDataResourceImpl returned = (ProjectDataResourceImpl) resourceFactory.newInstance(ProjectDataResource.class);
		File xmlFile = new File(project.getProjectDirectory(), FILE_NAME);
		returned.setProject(project);
		ProjectDataFactory projectDataFactory = new ProjectDataFactory(returned, project.getServiceManager().getEditingContext());
		returned.setFactory(projectDataFactory);
		returned.initName(project.getProjectName() + "-data");
		returned.setIODelegate(FileIODelegateImpl.makeFileFlexoIODelegate(xmlFile, resourceFactory));
		returned.setURI(project.buildProjectURI());
	
		returned.setResourceCenter(project);
		returned.setServiceManager(project.getServiceManager());
		if (xmlFile.exists()) {
			returned.loadResourceData(null);
		}
		else {
			ProjectData newProjectData = returned.getFactory().newInstance(ProjectData.class);
			returned.setResourceData(newProjectData);
		}
		return returned;
	} catch (ModelDefinitionException e) {
		e.printStackTrace();
	} catch (FlexoFileNotFoundException e) {
		e.printStackTrace();
	} catch (IOFlexoException e) {
		e.printStackTrace();
	} catch (InvalidXMLException e) {
		e.printStackTrace();
	} catch (InconsistentDataException e) {
		e.printStackTrace();
	} catch (InvalidModelDefinitionException e) {
		e.printStackTrace();
	}
	return null;
	}
	
	@Override
	public ProjectData getProjectData() {
	try {
		return getResourceData(null);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (ResourceLoadingCancelledException e) {
		e.printStackTrace();
	} catch (FlexoException e) {
		e.printStackTrace();
	}
	return null;
	}
	
	@Override
	public Class<ProjectData> getResourceDataClass() {
	return ProjectData.class;
	}
	}*/

}
