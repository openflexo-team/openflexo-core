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

package org.openflexo.foundation.nature;

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Setter;

/**
 * Interface defining the nature of a {@link FlexoProject}<br>
 * 
 * A {@link ProjectNature} might be seen as an interpretation of a given {@link FlexoProject}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(ProjectNature.ProjectNatureImpl.class)
public interface ProjectNature<N extends ProjectNature<N>> extends FlexoNature<FlexoProject<?>>, NatureObject<N> {

	public static final String OWNER = "owner";

	/**
	 * Return owner for this project nature (the project where this nature is declared)
	 * 
	 * @return
	 */
	@Getter(value = OWNER, inverse = FlexoProject.PROJECT_NATURES)
	public FlexoProject<?> getOwner();

	/**
	 * Sets owner for this project nature (the project where this nature is declared)
	 * 
	 * @param project
	 *            the project where this nature is declared
	 */
	@Setter(OWNER)
	public void setOwner(FlexoProject<?> project);

	public abstract class ProjectNatureImpl<N extends ProjectNature<N>> extends FlexoProjectObjectImpl implements ProjectNature<N> {

		private static final Logger logger = FlexoLogger.getLogger(ProjectNature.class.getPackage().getName());

		@Override
		public FlexoProject<?> getProject() {
			return getOwner();
		}

		@Override
		public N getNature() {
			return (N) this;
		}

		@Override
		public FlexoProject<?> getResourceData() {
			return getProject();
		}

	}
}
