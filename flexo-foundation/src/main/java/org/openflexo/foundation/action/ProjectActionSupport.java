/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceRepository;

/**
 * Finds the {@link FlexoProject} a project action applies to. The browsers show a project as the root folder of its delegate resource
 * center, so the project actions ({@link ValidateProject}, {@link CloseProject}) are declared on the {@link FlexoProject} and on that root
 * folder.
 */
class ProjectActionSupport {

	private ProjectActionSupport() {
	}

	/**
	 * Returns the project designated by the supplied object: the project itself, or the project whose delegate resource center has the
	 * supplied folder as root; null otherwise
	 */
	static FlexoProject<?> projectOf(FlexoObject object) {
		if (object instanceof FlexoProject) {
			return (FlexoProject<?>) object;
		}
		if (object instanceof RepositoryFolder && ((RepositoryFolder<?, ?>) object).isRootFolder()) {
			ResourceRepository<?, ?> repository = ((RepositoryFolder<?, ?>) object).getResourceRepository();
			FlexoResourceCenter<?> resourceCenter = repository != null ? repository.getResourceCenter() : null;
			if (resourceCenter != null && resourceCenter.getDelegatingProjectResource() != null) {
				// Not getFlexoProject(): it loads the project when it is not loaded, and this runs to build a menu
				return resourceCenter.getDelegatingProjectResource().getLoadedResourceData();
			}
		}
		return null;
	}
}
