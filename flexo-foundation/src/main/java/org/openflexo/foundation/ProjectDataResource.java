package org.openflexo.foundation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

import org.openflexo.foundation.ProjectData.ProjectDataFactory;
import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoProjectResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.FileFlexoIODelegate.FileFlexoIODelegateImpl;
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
				ModelFactory resourceFactory = new ModelFactory(ModelContextLibrary.getCompoundModelContext( 
						FileFlexoIODelegate.class,ProjectDataResource.class));
				ProjectDataResourceImpl returned = (ProjectDataResourceImpl) resourceFactory.newInstance(ProjectDataResource.class);
				File xmlFile = new File(project.getProjectDirectory(), FILE_NAME);
				returned.setProject(project);
				ProjectDataFactory projectDataFactory = new ProjectDataFactory(returned, project.getServiceManager().getEditingContext());
				returned.setFactory(projectDataFactory);
				returned.setName(project.getProjectName() + "-data");
				returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(xmlFile, resourceFactory));
				Date currentDate = new Date();
				String projectURI = FlexoProject.BASE_PROJECT_URI + "/" + (1900 + currentDate.getYear()) + "/" + (currentDate.getMonth() + 1) + "/"
						+ project.projectName + "_" + System.currentTimeMillis();
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
