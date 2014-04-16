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

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;

/**
 * Interface defining the nature of a {@link FlexoProject}<br>
 * 
 * A {@link ProjectNature} might be seen as an interpretation of a given {@link FlexoProject}
 * 
 * @author sylvain
 * 
 */
public interface ProjectNature<N extends ProjectNature<N, P>, P extends ProjectWrapper<N>> extends FlexoNature<FlexoProject> {

	/**
	 * Gives to supplied FlexoProject this nature
	 * 
	 * @return
	 */
	public void givesNature(FlexoProject project, FlexoEditor editor);

	/**
	 * Return wrapping object representing the interpretation of supplied project with this nature
	 * 
	 * @param project
	 * @return
	 */
	public P getProjectWrapper(FlexoProject project);

	/**
	 * Returns service managing project natures
	 * 
	 * @return
	 */
	public ProjectNatureService getProjectNatureService();

	/**
	 * Sets service managing project natures
	 * 
	 * @param projectNatureService
	 */
	public void setProjectNatureService(ProjectNatureService projectNatureService);
}
