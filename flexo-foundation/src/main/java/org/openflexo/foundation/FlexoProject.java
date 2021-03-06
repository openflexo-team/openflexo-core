/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

import javax.naming.InvalidNameException;

import org.openflexo.connie.annotations.NotificationUnsafe;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.project.FlexoProjectFactory;
import org.openflexo.foundation.project.FlexoProjectImpl;
import org.openflexo.foundation.project.FlexoProjectReference;
import org.openflexo.foundation.project.FlexoProjectResource;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ProjectImportLoopException;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.toolbox.FlexoVersion;

/**
 * This class represents an Openflexo project.<br>
 * 
 * A {@link FlexoProject} is a logical container of resources located in project directory.<br>
 * 
 * The purpose of a {@link FlexoProject} is to get a self-contained and transportable set of resources.<br>
 * 
 * @author sylvain
 * @param <I>
 *            type of serialization artefact this project stores
 */
@ModelEntity
@ImplementationClass(FlexoProjectImpl.class)
@XMLElement(deprecatedXMLTags = "ProjectData")
public interface FlexoProject<I> extends FlexoResourceCenter<I>, FlexoProjectObject<I>, ResourceData<FlexoProject<I>> {

	@PropertyIdentifier(type = String.class)
	public static final String PROJECT_NAME_KEY = "projectName";
	@PropertyIdentifier(type = String.class)
	public static final String PROJECT_URI_KEY = "projectURI";
	@PropertyIdentifier(type = Date.class)
	public static final String CREATION_DATE_KEY = "creationDate";
	@PropertyIdentifier(type = String.class)
	public static final String CREATION_USER_ID_KEY = "creationUserId";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String PROJECT_VERSION_KEY = "projectVersion";
	@PropertyIdentifier(type = Long.class)
	public static final String PROJECT_REVISION_KEY = "projectRevision";
	@PropertyIdentifier(type = FlexoProjectReference.class, cardinality = Cardinality.LIST)
	public static final String IMPORTED_PROJECTS = "importedProjects";
	@PropertyIdentifier(type = ProjectNature.class, cardinality = Cardinality.LIST)
	public static final String PROJECT_NATURES = "projectNatures";
	@PropertyIdentifier(type = FlexoEditor.class, cardinality = Cardinality.LIST)
	public static final String EDITORS = "editors";
	@PropertyIdentifier(type = String.class)
	String PROJECT_DESCRIPTION_KEY = "projectDescription";

	public String getProjectName();

	@Getter(value = PROJECT_URI_KEY)
	@XMLAttribute(xmlTag = "uri")
	public String getProjectURI();

	@Setter(PROJECT_URI_KEY)
	public void setProjectURI(String projectURI);

	@Getter(value = CREATION_DATE_KEY)
	@XMLAttribute
	public Date getCreationDate();

	@Setter(CREATION_DATE_KEY)
	public void setCreationDate(Date creationDate);

	public String getCreationDateAsString();

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

	@Getter(value = PROJECT_REVISION_KEY, defaultValue = "1")
	@XMLAttribute
	public long getProjectRevision();

	@Setter(PROJECT_REVISION_KEY)
	public void setProjectRevision(long revision);

	@Getter(value = PROJECT_DESCRIPTION_KEY)
	@XMLAttribute
	public String getProjectDescription();

	@Setter(PROJECT_DESCRIPTION_KEY)
	public void setProjectDescription(String description);

	public String getPrefix();

	public I getProjectDirectory();

	@Finder(collection = IMPORTED_PROJECTS, attribute = FlexoProjectReference.URI, isMultiValued = false)
	public FlexoProjectReference<?> getProjectReferenceWithURI(String uri);

	public FlexoProjectReference<?> getProjectReferenceWithURI(String projectURI, boolean searchRecursively);

	@Getter(value = IMPORTED_PROJECTS, cardinality = Cardinality.LIST, inverse = FlexoProjectReference.OWNER)
	@XMLElement(xmlTag = "ImportedProjects")
	@Embedded
	public List<FlexoProjectReference<?>> getImportedProjects();

	@Setter(value = IMPORTED_PROJECTS)
	public void setImportedProjects(List<FlexoProjectReference<?>> importedProjects);

	@Adder(IMPORTED_PROJECTS)
	public void addToImportedProjects(FlexoProjectReference<?> projectReference)
			throws ProjectImportLoopException, ProjectLoadingCancelledException;

	@Remover(value = IMPORTED_PROJECTS)
	public void removeFromImportedProjects(FlexoProjectReference<?> projectReference);

	public String canImportProject(FlexoProject<?> project);

	public void removeFromImportedProjects(FlexoProject<?> project);

	@Getter(value = EDITORS, cardinality = Cardinality.LIST, ignoreType = true)
	public List<FlexoEditor> getEditors();

	@Setter(value = EDITORS)
	public void setEditors(List<FlexoEditor> editors);

	@Adder(EDITORS)
	public void addToEditors(FlexoEditor editor);

	@Remover(EDITORS)
	public void removeFromEditors(FlexoEditor editor);

	/**
	 * Return the list of {@link TechnologyAdapter} used in the context of this {@link FlexoProject}
	 * 
	 * @return
	 */
	public List<TechnologyAdapter<?>> getRequiredTechnologyAdapters();

	public FlexoProjectFactory getModelFactory();

	public List<FlexoObjectReference<?>> getObjectReferences();

	public void addToObjectReferences(FlexoObjectReference<?> objectReference);

	public void removeObjectReferences(FlexoObjectReference<?> objectReference);

	public boolean areAllImportedProjectsLoaded();

	public boolean lastUniqueIDHasBeenSet();

	public long getNewFlexoID();

	/**
	 * @return Returns the lastUniqueID.
	 */
	public long getLastID();

	/**
	 * @param lastUniqueID
	 *            The lastUniqueID to set.
	 */
	public void setLastID(long lastUniqueID);

	/**
	 * Don't use this method to get a new ID. Use getNewUniqueID instead
	 * 
	 * @return Returns the lastUniqueID.
	 */
	public long getLastUniqueID();

	/**
	 * @param lastUniqueID
	 *            The lastUniqueID to set.
	 */
	public void setLastUniqueID(long lastUniqueID);

	/**
	 * Save this project
	 * 
	 */
	public void save() throws SaveResourceException;

	/**
	 * Save this project using FlexoEditingContext scheme<br>
	 * Additionnaly save all known resources related to this project
	 */
	public void saveModifiedResources() throws SaveResourceException;

	/**
	 * Save this project<br>
	 * Additionally save all known resources related to this project
	 * 
	 * Overrides
	 * 
	 * @param clearModifiedStatus
	 */
	public void saveModifiedResources(boolean clearModifiedStatus) throws SaveResourceException;

	/**
	 * Close this project by de-referencing all contents of that project and removing it from {@link FlexoResourceCenterService}<br>
	 * Don't save anything if not done before<br>
	 * Take care that after this call, this {@link FlexoProject} has been fully nullifed and should not be used anymore
	 * 
	 */
	public void close();

	public void copyTo(I newProjectDirectory) throws SaveResourceException, InvalidNameException, CannotRenameException;

	public boolean hasUnsavedResources();

	public boolean importsProject(FlexoProject<?> project);

	public boolean importsProjectWithURI(String projectURI);

	/**
	 * When true, indicates that this {@link FlexoProject} has no parent {@link FlexoResourceCenter}
	 * 
	 * @return
	 */
	public boolean isStandAlone();

	/**
	 * Return {@link FlexoResourceCenter} acting as a delegate for the {@link FlexoProject}
	 */
	public FlexoResourceCenter<I> getDelegateResourceCenter();

	public FlexoProjectResource<I> getProjectResource();

	public ValidationModel getProjectValidationModel();

	@Getter(value = PROJECT_NATURES, cardinality = Cardinality.LIST, inverse = ProjectNature.OWNER)
	@XMLElement
	@Embedded
	public List<ProjectNature<?>> getProjectNatures();

	@Setter(value = PROJECT_NATURES)
	public void setProjectNatures(List<ProjectNature<?>> projectNatures);

	@Adder(PROJECT_NATURES)
	public void addToProjectNatures(ProjectNature<?> projectNature);

	@Remover(value = PROJECT_NATURES)
	public void removeFromProjectNatures(ProjectNature<?> projectNature);

	/**
	 * Return boolean indicating if this project might be interpreted according to this project nature
	 * 
	 * @param projectNatureClass
	 * @return
	 */
	@NotificationUnsafe
	public boolean hasNature(Class<? extends ProjectNature> projectNatureClass);

	/**
	 * Return boolean indicating if this project might be interpreted according to this project nature (supplied as string representing full
	 * class name)
	 * 
	 * @param projectNatureClassName
	 * @return
	 */
	@NotificationUnsafe
	public boolean hasNature(String projectNatureClassName);

	/**
	 * Return nature of supplied class when existing.<br>
	 * 
	 * @param projectNatureClass
	 * @return
	 */
	@NotificationUnsafe
	public <N extends ProjectNature<N>> N getNature(Class<N> projectNatureClass);

	/**
	 * Return nature of supplied class when existing.<br>
	 * 
	 * @param projectNatureClassName
	 * @return
	 */
	@NotificationUnsafe
	public <N extends ProjectNature<N>> N getNature(String projectNatureClassName);

}
