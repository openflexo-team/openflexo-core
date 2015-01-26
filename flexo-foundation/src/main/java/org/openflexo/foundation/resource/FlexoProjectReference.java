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

package org.openflexo.foundation.resource;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.ProjectData;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Initializer;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Modify;
import org.openflexo.model.annotations.Parameter;
import org.openflexo.model.annotations.ReturnedValue;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.factory.AccessibleProxyObject;
import org.openflexo.model.factory.DeletableProxyObject;

@ModelEntity
@XMLElement
@ImplementationClass(FlexoProjectReferenceImpl.class)
@Modify(forward = FlexoProjectReference.PROJECT_DATA)
public interface FlexoProjectReference extends AccessibleProxyObject, DeletableProxyObject, FlexoResource<FlexoProject> {

	public static final String PROJECT_DATA = "projectData";
	public static final String REFERRING_PROJECT = "referringProject";
	public static final String REFERRED_PROJECT = "referredProject";
	public static final String PROJECT = "project";
	public static final String WORKFLOW = "workflow";

	@Initializer
	public FlexoProjectReference init(@Parameter(REFERRED_PROJECT) FlexoProject referredProject);

	/**
	 * Getter for the project data
	 * 
	 * @return the project data of this project reference
	 */
	@Getter(value = PROJECT_DATA, inverse = ProjectData.IMPORTED_PROJECTS)
	public ProjectData getProjectData();

	/**
	 * Sets the project data
	 * 
	 * @param data
	 *            the project data
	 */
	@Setter(PROJECT_DATA)
	public void setProjectData(ProjectData data);

	/**
	 * Returns the referring project, ie, the project in which this project reference belongs, is used. The returned value is equivalent
	 * {@link #getProjectData()}.{@link ProjectData#getProject()}.
	 * 
	 * @return the referring project.
	 */
	@Getter(value = REFERRING_PROJECT, ignoreType = true)
	@ReturnedValue(PROJECT_DATA + "." + ProjectData.PROJECT)
	public FlexoProject getReferringProject();

	/**
	 * Returns the referred project, the project to which this reference refers to.
	 * 
	 * @return the referred project
	 * 
	 */
	@Getter(value = REFERRED_PROJECT, ignoreType = true)
	public FlexoProject getReferredProject();

	/**
	 * Returns the referred project, the project to which this reference refers to.
	 * 
	 * @param force
	 *            flag to indicate whether all measures should be taken to load the project or not. Yet, passing <code>true</code> does not
	 *            guarantee that the project will eventually be loaded and callers should handle a <code>null</code> value.
	 * @return the referred project
	 * 
	 */
	public FlexoProject getReferredProject(boolean force);

	/**
	 * Sets the referred project.
	 */
	@Setter(value = REFERRED_PROJECT)
	public void setReferredProject(FlexoProject project);

	/**
	 * Returns the FlexoWorkflow of this project reference. This can either be a cached value or the live object (if the corresponding
	 * project has been loaded and set)
	 */
	// public FlexoWorkflow getWorkflow();

}
