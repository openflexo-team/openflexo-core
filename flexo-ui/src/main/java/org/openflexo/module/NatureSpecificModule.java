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
package org.openflexo.module;

import javax.swing.ImageIcon;

import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.prefs.ModulePreferences;

/**
 * Represents a Module in Openflexo intrastructure managing a specific {@link ProjectNature}
 * 
 * @author sguerin
 * 
 */
public abstract class NatureSpecificModule<M extends FlexoModule<M>, N extends ProjectNature<?, ?>> extends Module<M> {

	private final Class<N> natureClass;

	public NatureSpecificModule(String name, String shortName, Class<M> moduleClass,
			Class<? extends ModulePreferences<M>> preferencesClass, String relativeDirectory, String jiraComponentID, String helpTopic,
			ImageIcon smallIcon, ImageIcon mediumIcon, ImageIcon mediumIconWithHover, ImageIcon bigIcon, boolean requiresProject,
			Class<N> natureClass) {
		super(name, shortName, moduleClass, preferencesClass, relativeDirectory, jiraComponentID, helpTopic, smallIcon, mediumIcon,
				mediumIconWithHover, bigIcon, requiresProject);
		this.natureClass = natureClass;
	}

	public Class<N> getNatureClass() {
		return natureClass;
	}
}