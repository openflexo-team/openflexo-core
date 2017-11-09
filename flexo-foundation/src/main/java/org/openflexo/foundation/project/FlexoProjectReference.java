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

package org.openflexo.foundation.project;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Initializer;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Parameter;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.factory.AccessibleProxyObject;
import org.openflexo.model.factory.DeletableProxyObject;

/**
 * Encodes the reference to a {@link FlexoProject} (import relationship between two projects)
 * 
 * @author sylvain
 *
 */
@ModelEntity
@XMLElement
@ImplementationClass(FlexoProjectReferenceImpl.class)
public interface FlexoProjectReference
		extends AccessibleProxyObject, DeletableProxyObject, FlexoProjectObject /*, FlexoResource<FlexoProject>*/ {

	public static final String OWNER = "owner";
	public static final String REFERENCED_PROJECT = "referencedProject";
	public static final String URI = "URI";

	@Initializer
	public FlexoProjectReference init(@Parameter(REFERENCED_PROJECT) FlexoProject<?> referencedProject);

	/**
	 * Return owner for this reference (the project where this reference is declared)
	 * 
	 * @return
	 */
	@Getter(value = OWNER, inverse = FlexoProject.IMPORTED_PROJECTS)
	public FlexoProject<?> getOwner();

	/**
	 * Sets owner for this reference (the project where this reference is declared)
	 * 
	 * @param data
	 */
	@Setter(OWNER)
	public void setOwner(FlexoProject<?> data);

	/**
	 * Returns the referenced project, the project to which this reference refers to.
	 * 
	 * @return the referenced project.
	 */
	@Getter(value = REFERENCED_PROJECT)
	public FlexoProject<?> getReferencedProject();

	/**
	 * Sets the referred project.
	 */
	@Setter(value = REFERENCED_PROJECT)
	public void setReferencedProject(FlexoProject<?> project);

	/**
	 * Returns URI of referenced project
	 * 
	 * @return
	 */
	@Getter(value = URI)
	@XMLAttribute
	public String getURI();

	/**
	 * Sets
	 * 
	 * @param uri
	 */
	@Setter(value = URI)
	public void setURI(String uri);
}
