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

import java.util.Date;
import java.util.List;

import org.openflexo.foundation.resource.FlexoProjectReference;
import org.openflexo.foundation.resource.ProjectImportLoopException;
import org.openflexo.foundation.resource.ProjectResourceData;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Finder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.converter.FlexoVersionConverter;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.AccessibleProxyObject;
import org.openflexo.model.factory.EditingContext;
import org.openflexo.toolbox.FlexoVersion;

/**
 * Encodes meta-data relative to project
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(ProjectData.ProjectDataImpl.class)
@XMLElement
public interface ProjectData extends FlexoProjectObject, AccessibleProxyObject, ProjectResourceData<ProjectData> {

	@PropertyIdentifier(type = String.class)
	public static final String URI_KEY = "uri";
	@PropertyIdentifier(type = Date.class)
	public static final String CREATION_DATE_KEY = "creationDate";
	@PropertyIdentifier(type = String.class)
	public static final String CREATION_USER_ID_KEY = "creationUserId";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String PROJECT_VERSION_KEY = "projectVersion";
	@PropertyIdentifier(type = FlexoProjectReference.class, cardinality = Cardinality.LIST)
	public static final String IMPORTED_PROJECTS = "importedProjects";

	@Getter(value = URI_KEY)
	@XMLAttribute
	public String getURI();

	@Setter(URI_KEY)
	public void setURI(String projectURI);

	@Getter(value = CREATION_DATE_KEY)
	@XMLAttribute
	public Date getCreationDate();

	@Setter(CREATION_DATE_KEY)
	public void setCreationDate(Date creationDate);

	@Getter(value = CREATION_USER_ID_KEY)
	@XMLAttribute
	public String getCreationUserId();

	@Setter(CREATION_USER_ID_KEY)
	public void setCreationUserId(String creationUser);

	@Getter(value = PROJECT_VERSION_KEY, isStringConvertable = true)
	@XMLAttribute
	public FlexoVersion getProjectVersion();

	@Setter(PROJECT_VERSION_KEY)
	public void setProjectVersion(FlexoVersion version);

	@Finder(collection = IMPORTED_PROJECTS, attribute = FlexoProjectReference.URI, isMultiValued = false)
	public FlexoProjectReference getProjectReferenceWithURI(String uri);

	public FlexoProjectReference getProjectReferenceWithURI(String projectURI, boolean searchRecursively);

	@Finder(collection = IMPORTED_PROJECTS, attribute = FlexoProjectReference.NAME, isMultiValued = true)
	public List<FlexoProjectReference> getProjectReferenceWithName(String name);

	public List<FlexoProjectReference> getProjectReferenceWithName(String name, boolean searchRecursively);

	@Getter(value = IMPORTED_PROJECTS, cardinality = Cardinality.LIST, inverse = FlexoProjectReference.PROJECT_DATA)
	@XMLElement(xmlTag = "ImportedProjects")
	@Embedded
	public List<FlexoProjectReference> getImportedProjects();

	@Setter(value = IMPORTED_PROJECTS)
	public void setImportedProjects(List<FlexoProjectReference> importedProjects);

	@Adder(IMPORTED_PROJECTS)
	public void addToImportedProjects(FlexoProjectReference projectReference) throws ProjectImportLoopException,
			ProjectLoadingCancelledException;

	@Remover(value = IMPORTED_PROJECTS)
	public void removeFromImportedProjects(FlexoProjectReference projectReference);

	public String canImportProject(FlexoProject project);

	public void removeFromImportedProjects(FlexoProject project);

	public static abstract class ProjectDataImpl extends FlexoProjectObjectImpl implements ProjectData {

		@Override
		public FlexoProjectReference getProjectReferenceWithURI(String projectURI, boolean searchRecursively) {
			FlexoProjectReference ref = getProjectReferenceWithURI(projectURI);
			if (ref != null) {
				return ref;
			}
			if (searchRecursively) {
				for (FlexoProjectReference ref2 : getImportedProjects()) {
					FlexoProjectReference projectWithURI = null;
					if (ref2.getReferredProject() != null) {
						ProjectData projectData = ref2.getReferredProject().getProjectData();
						if (projectData != null) {
							projectWithURI = projectData.getProjectReferenceWithURI(projectURI, searchRecursively);
						}
						if (projectWithURI != null) {
							return projectWithURI;
						}
					}

				}
			}
			return null;
		}

		@Override
		public List<FlexoProjectReference> getProjectReferenceWithName(String name, boolean searchRecursively) {
			List<FlexoProjectReference> refs = getProjectReferenceWithName(name);
			if (searchRecursively) {
				for (FlexoProjectReference ref2 : getImportedProjects()) {
					if (ref2.getReferredProject() != null) {
						ProjectData projectData = ref2.getReferredProject().getProjectData();
						if (projectData != null) {
							List<FlexoProjectReference> projectReferenceWithName = projectData.getProjectReferenceWithName(name,
									searchRecursively);
							for (FlexoProjectReference ref : projectReferenceWithName) {
								if (!refs.contains(ref)) {
									refs.add(ref);
								}
							}
						}
					}

				}
			}
			return refs;
		}

		@Override
		public void addToImportedProjects(FlexoProjectReference projectReference) throws ProjectImportLoopException {
			if (!isDeserializing()) {
				if (getImportedProjects().contains(projectReference)) {
					return;
				}
				String reason = canImportProject(projectReference.getReferredProject());
				if (reason != null) {
					throw new ProjectImportLoopException(reason);
				}
			}
			performSuperAdder(IMPORTED_PROJECTS, projectReference);
			/*if (!isDeserializing() && projectReference.getReferredProject() != null) {
				getProject().getFlexoRMResource().addToDependentResources(projectReference.getReferredProject().getFlexoRMResource());
				getProject().getImportedWorkflow(projectReference, true);
			}*/
		}

		@Override
		public void removeFromImportedProjects(FlexoProjectReference projectReference) {
			// TODO: remove dependency toward external resource.
			// getProject().getFlexoRMResource().removeFromDependentResources(projectReference.getReferredProject().getFlexoRMResource());
			performSuperRemover(IMPORTED_PROJECTS, projectReference);
		}

		@Override
		public String canImportProject(FlexoProject project) {
			if (project.getProjectURI().equals(getProject().getProjectURI())) {
				return FlexoLocalization.localizedForKey("cannot_import_itself");
			}
			if (getProjectReferenceWithURI(project.getProjectURI()) != null) {
				return FlexoLocalization.localizedForKey("project_already_imported");
			}
			return null;
		}

		@Override
		public void removeFromImportedProjects(FlexoProject project) {
			for (FlexoProjectReference ref : getImportedProjects()) {
				if (ref.getReferredProject() == project) {
					removeFromImportedProjects(ref);
					break;
				}
			}
		}

	}

	public static class ProjectDataFactory extends DefaultPamelaResourceModelFactory<ProjectDataResource> {

		public ProjectDataFactory(ProjectDataResource resource, EditingContext editingContext) throws ModelDefinitionException {
			super(resource, ModelContextLibrary.getModelContext(ProjectData.class));
			setEditingContext(editingContext);
			addConverter(new FlexoVersionConverter());
		}
	}

}
