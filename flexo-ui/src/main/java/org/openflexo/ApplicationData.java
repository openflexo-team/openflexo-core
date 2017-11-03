/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.connie.annotations.NotificationUnsafe;
import org.openflexo.icon.IconLibrary;
import org.openflexo.module.Module;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

/**
 * Class storing general data for application
 * 
 * 
 * @author sguerin
 */
public class ApplicationData extends PropertyChangedSupportDefaultImplementation {
	private static final Logger logger = Logger.getLogger(ApplicationData.class.getPackage().getName());
	private final ApplicationContext applicationContext;

	public ApplicationData(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@NotificationUnsafe
	public List<Module<?>> getAvailableModules() {
		return applicationContext.getModuleLoader().getKnownModules();
	}

	// TODO: handle this
	public String getBusinessName() {
		return "Diatomée infrastructure";
	}

	public String getVersion() {
		return "Version " + FlexoCst.BUSINESS_APPLICATION_VERSION + " (build " + ApplicationVersion.BUILD_ID + ")";
	}

	public List<File> getLastOpenedProjects() {
		return applicationContext.getGeneralPreferences().getLastOpenedProjects();
	}

	public ImageIcon getProjectIcon() {
		return IconLibrary.OPENFLEXO_NOTEXT_16;
	}

	public ImageIcon getOpenflexoIcon() {
		return IconLibrary.OPENFLEXO_NOTEXT_64;
	}

	public ImageIcon getOpenflexoTextIcon() {
		return IconLibrary.OPENFLEXO_TEXT_SMALL_ICON;
	}

	@NotificationUnsafe
	public Module<?> getFavoriteModule() {
		Module<?> returned = applicationContext.getModuleLoader()
				.getModuleNamed(applicationContext.getGeneralPreferences().getFavoriteModuleName());
		if (returned == null) {
			if (getAvailableModules().size() > 0) {
				return getAvailableModules().iterator().next();
			}
			logger.severe("No module found.");
			return null;
		}
		else {
			return returned;
		}
	}

	public void setFavoriteModule(Module<?> aModule) {

		if (aModule != null) {
			applicationContext.getGeneralPreferences().setFavoriteModuleName(aModule.getName());
		}
		getPropertyChangeSupport().firePropertyChange("favoriteModule", null, aModule);
	}

}
