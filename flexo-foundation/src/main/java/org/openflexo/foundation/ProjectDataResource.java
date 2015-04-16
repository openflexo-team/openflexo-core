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

package org.openflexo.foundation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

import org.openflexo.foundation.ProjectData.ProjectDataFactory;
import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.FileFlexoIODelegate.FileFlexoIODelegateImpl;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoProjectResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

/**
 * This is the {@link FlexoResource} encoding a {@link ProjectData}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(ProjectDataResource.ProjectDataResourceImpl.class)
@XMLElement
public interface ProjectDataResource extends PamelaResource<ProjectData, ProjectDataFactory>, FlexoProjectResource<ProjectData> {

	public static final String FILE_NAME = "ProjectData.xml";

	public ProjectData getProjectData();

	/**
	 * Default implementation for {@link ProjectDataResource}
	 * 
	 * 
	 * @author Sylvain
	 * 
	 */
	public static abstract class ProjectDataResourceImpl extends PamelaResourceImpl<ProjectData, ProjectDataFactory> implements
			ProjectDataResource {

		public static ProjectDataResource makeProjectDataResource(FlexoProject project) {
			try {
				ModelFactory resourceFactory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(FileFlexoIODelegate.class,
						ProjectDataResource.class));
				ProjectDataResourceImpl returned = (ProjectDataResourceImpl) resourceFactory.newInstance(ProjectDataResource.class);
				File xmlFile = new File(project.getProjectDirectory(), FILE_NAME);
				returned.setProject(project);
				ProjectDataFactory projectDataFactory = new ProjectDataFactory(returned, project.getServiceManager().getEditingContext());
				returned.setFactory(projectDataFactory);
				returned.initName(project.getProjectName() + "-data");
				returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(xmlFile, resourceFactory));
				Date currentDate = new Date();
				String projectURI = FlexoProject.BASE_PROJECT_URI + "/" + (1900 + currentDate.getYear()) + "/"
						+ (currentDate.getMonth() + 1) + "/" + project.projectName + "_" + System.currentTimeMillis();
				returned.setURI(projectURI);
				returned.setServiceManager(project.getServiceManager());
				if (xmlFile.exists()) {
					returned.loadResourceData(null);
				} else {
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
	}
}
