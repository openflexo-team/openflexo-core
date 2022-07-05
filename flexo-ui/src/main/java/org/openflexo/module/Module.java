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

package org.openflexo.module;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.ApplicationContext;
import org.openflexo.foundation.task.Progress;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.prefs.ModulePreferences;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

/**
 * Represents a Module in Openflexo intrastructure. A module is a software component.
 * 
 * An instance of {@link Module} is a {@link FlexoModule} instance which is retrieved through service management.<br>
 * A {@link Module} is not loaded by default, but need to be loaded by the {@link ModuleLoader}.
 * 
 * @author sguerin
 * 
 */
public abstract class Module<M extends FlexoModule<M>> extends PropertyChangedSupportDefaultImplementation {

	private static final Logger logger = Logger.getLogger(Module.class.getPackage().getName());

	private final boolean notFoundNotified = false;

	private final Constructor<M> constructor;

	private final String name;
	private final String shortName;
	private final Class<M> moduleClass;
	private final Class<? extends ModulePreferences<M>> preferencesClass;
	private final String relativeDirectory;
	private final String jiraComponentID;
	private final String helpTopic;
	private final ImageIcon smallIcon;
	private final ImageIcon mediumIcon;
	private final ImageIcon mediumIconWithHover;
	private final ImageIcon bigIcon;

	private ModuleLoader moduleLoader;

	private M loadedModuleInstance;

	public Module(String name, String shortName, Class<M> moduleClass, Class<? extends ModulePreferences<M>> preferencesClass,
			String relativeDirectory, String jiraComponentID, String helpTopic, ImageIcon smallIcon, ImageIcon mediumIcon,
			ImageIcon mediumIconWithHover, ImageIcon bigIcon) {
		super();
		this.name = name;
		this.shortName = shortName;
		this.moduleClass = moduleClass;
		this.preferencesClass = preferencesClass;
		this.relativeDirectory = relativeDirectory;
		this.jiraComponentID = jiraComponentID;
		this.helpTopic = helpTopic;
		this.smallIcon = smallIcon;
		this.mediumIcon = mediumIcon;
		this.mediumIconWithHover = mediumIconWithHover;
		this.bigIcon = bigIcon;
		constructor = lookupConstructor();
	}

	public ModuleLoader getModuleLoader() {
		return moduleLoader;
	}

	public void setModuleLoader(ModuleLoader moduleLoader) {
		this.moduleLoader = moduleLoader;
	}

	public Constructor<M> getConstructor() {
		return constructor;
	}

	public Class<? extends ModulePreferences<M>> getPreferencesClass() {
		return preferencesClass;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public Class<M> getModuleClass() {
		return moduleClass;
	}

	protected String getRelativeDirectory() {
		return relativeDirectory;
	}

	public String getJiraComponentID() {
		return jiraComponentID;
	}

	public String getHelpTopic() {
		return helpTopic;
	}

	public ImageIcon getSmallIcon() {
		return smallIcon;
	}

	public ImageIcon getMediumIcon() {
		return mediumIcon;
	}

	public ImageIcon getMediumIconWithHover() {
		return mediumIconWithHover;
	}

	public ImageIcon getBigIcon() {
		return bigIcon;
	}

	public boolean isNotFoundNotified() {
		return notFoundNotified;
	}

	public final String getDescription() {
		return getName() + "_description";
	}

	public String getLocalizedDescription() {
		return FlexoLocalization.getMainLocalizer().localizedForKey(getDescription());
	}

	/**
	 * Internally used to lookup constructor
	 * 
	 */
	private Constructor<M> lookupConstructor() {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Registering module '" + getName() + "'");
		}
		Class<?>[] constructorSigner;
		constructorSigner = new Class[1];
		constructorSigner[0] = ApplicationContext.class;
		try {
			Constructor<M> constructor = getModuleClass().getDeclaredConstructor(constructorSigner);
			if (logger.isLoggable(Level.FINE)) {
				logger.finer("Contructor:" + constructor);
			}
			return constructor;
		} catch (SecurityException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("SecurityException raised during module " + getName() + " registering. Aborting.");
			}
		} catch (NoSuchMethodException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("NoSuchMethodException raised during module " + getName() + " registering. Aborting.");
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return getName();
	}

	public ApplicationContext getApplicationContext() {
		return getModuleLoader().getServiceManager();
	}

	public String getHTMLDescription() {
		return getLocalizedDescription();

		/*return "<html>No description available for <b>" + getLocalizedName() + "</b>" + "<br>"
				+ "Please submit documentation in documentation resource center" + "<br>" + "</html>";*/
	}

	/**
	 * Hook to initialize module<br>
	 * Default implementation does nothing
	 */
	public void initialize() {
	}

	/**
	 * Load the module if it is not already laoded
	 * 
	 * @return
	 * @throws Exception
	 */
	public M load() throws Exception {

		/*boolean createProgress = !ProgressWindow.hasInstance();
		if (createProgress) {
			ProgressWindow.showProgressWindow(FlexoLocalization.localizedForKey("loading_module") + " " + getLocalizedName(), 8);
		}
		ProgressWindow.setProgressInstance(FlexoLocalization.localizedForKey("loading_module") + " " + getLocalizedName());*/
		Progress.progress("load_module");
		loadedModuleInstance = getConstructor().newInstance(new Object[] { getModuleLoader().getServiceManager() });
		// doInternalLoadModule();
		/*if (createProgress) {
			ProgressWindow.hideProgressWindow();
		}*/

		Progress.progress("init_module");
		loadedModuleInstance.initModule();
		getPropertyChangeSupport().firePropertyChange("loaded", false, true);

		return loadedModuleInstance;
	}

	/**
	 * Unload the module (not implemented yet)
	 */
	public void unload() {
		// TODO
		loadedModuleInstance = null;
	}

	public boolean isLoaded() {
		return loadedModuleInstance != null;
	}

	public M getLoadedModuleInstance() {
		return loadedModuleInstance;
	}

	public int getExpectedProgressLoadingSteps() {
		return 150;
	}

	/*private FlexoModule doInternalLoadModule() throws Exception {
		ModuleLoaderCallable loader = new ModuleLoaderCallable(loadedModuleInstance);
		// return FlexoSwingUtils.syncRunInEDT(loader);
		return loader.call();
	}*/

	/*private class ModuleLoaderCallable implements Callable<FlexoModule> {
	
		private final FlexoModule module;
	
		public ModuleLoaderCallable(FlexoModule module) {
			this.module = module;
		}
	
		@Override
		public FlexoModule call() throws Exception {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Initialize module " + module.getName());
			}
			Progress.progress("init_module");
			module.initModule();
			if (getApplicationContext().getDocResourceManager() != null) {
				getApplicationContext().getDocResourceManager().ensureHelpEntryForModuleHaveBeenCreated(module);
			}
			return module;
		}
	}*/

}
