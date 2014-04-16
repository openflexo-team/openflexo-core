/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.nature;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;

/**
 * Represents a project with a specific {@link ProjectNature} <br>
 * 
 * This class is a wrapper class above {@link FlexoProject} and provides a specific interpretation of a generic {@link FlexoProject}<br>
 * Instances of {@link ProjectWrapper} are managed and should be retrieved from a specific {@link ProjectNature}.
 * 
 * @see FreeModelProjectNature
 * 
 * @author sylvain
 * 
 */
public interface ProjectWrapper<N extends ProjectNature> extends FlexoObject {

	public FlexoProject getProject();

	public N getProjectNature();
}
