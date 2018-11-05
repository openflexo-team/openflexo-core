/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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
public abstract class NatureSpecificModule<M extends FlexoModule<M>, N extends ProjectNature> extends Module<M> {

	private final Class<N> projectNatureClass;

	public NatureSpecificModule(String name, String shortName, Class<M> moduleClass, Class<? extends ModulePreferences<M>> preferencesClass,
			String relativeDirectory, String jiraComponentID, String helpTopic, ImageIcon smallIcon, ImageIcon mediumIcon,
			ImageIcon mediumIconWithHover, ImageIcon bigIcon, Class<N> projectNatureClass) {
		super(name, shortName, moduleClass, preferencesClass, relativeDirectory, jiraComponentID, helpTopic, smallIcon, mediumIcon,
				mediumIconWithHover, bigIcon);
		this.projectNatureClass = projectNatureClass;
	}

	public Class<N> getProjectNatureClass() {
		return projectNatureClass;
	}
}
