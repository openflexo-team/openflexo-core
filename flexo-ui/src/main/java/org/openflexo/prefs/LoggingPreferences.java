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

package org.openflexo.prefs;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.ToolBox;

/**
 * Encodes general preferences for the whole application
 * 
 * @author sguerin
 * 
 */
@ModelEntity
@ImplementationClass(LoggingPreferences.LoggingPreferencesImpl.class)
@XMLElement(xmlTag = "LoggingPreferences")
@Preferences(
		shortName = "Logging",
		longName = "Logging preferences",
		FIBPanel = "Fib/Prefs/LoggingPreferences.fib",
		smallIcon = "Icons/Common/Logging.png",
		bigIcon = "Icons/Common/Logging_64x64.png")
public interface LoggingPreferences extends PreferencesContainer {

	public static final String ENABLE_LOGGING = "enableLogging";
	public static final String LOGCOUNT = "maxLogCount";
	public static final String KEEPLOGTRACE = "keepLogTrace";
	public static final String DEFAULT_LOG_LEVEL = "default.logging.level";
	public static final String CUSTOM_LOG_CONFIG_FILE = "logging.file.name";

	@Getter(value = ENABLE_LOGGING, defaultValue = "true")
	@XMLAttribute
	public boolean getEnableLogging();

	@Setter(ENABLE_LOGGING)
	public void setEnableLogging(boolean b);

	@Getter(value = KEEPLOGTRACE, defaultValue = "true")
	@XMLAttribute
	public boolean getIsLoggingTrace();

	@Setter(KEEPLOGTRACE)
	public void setIsLoggingTrace(boolean b);

	@Getter(CUSTOM_LOG_CONFIG_FILE)
	@XMLAttribute
	public String getLoggingFileName();

	@Setter(CUSTOM_LOG_CONFIG_FILE)
	public void setLoggingFileName(String loggingFileName);

	public Resource getCustomLoggingFile();

	@Getter(DEFAULT_LOG_LEVEL)
	@XMLAttribute
	public String getDefaultLoggingLevelAsString();

	@Setter(DEFAULT_LOG_LEVEL)
	public void setDefaultLoggingLevelAsString(String aString);

	public Level[] getAvailableLoggingLevels();

	public Level getDefaultLoggingLevel();

	public void setDefaultLoggingLevel(Level l);

	@Getter(value = LOGCOUNT, defaultValue = "-1")
	@XMLAttribute
	public int getMaxLogCount();

	@Setter(LOGCOUNT)
	public void setMaxLogCount(int c);

	public boolean limitLogCount();

	public void setLimitLogCount(boolean limitLogCount);

	public File getLogDirectory();

	public abstract class LoggingPreferencesImpl extends PreferencesContainerImpl implements LoggingPreferences {

		private static final Logger logger = Logger.getLogger(LoggingPreferences.class.getPackage().getName());

		private Level[] AVAILABLE_LEVELS = { Level.SEVERE, Level.WARNING, Level.INFO, Level.FINE, Level.FINER, Level.FINEST };

		@Override
		public File getLogDirectory() {
			File outputDir = new File(System.getProperty("user.home") + "/Library/Logs/OpenFlexo");
			if (ToolBox.isWindows()) {
				boolean ok = false;
				String appData = System.getenv("LOCALAPPDATA");
				if (appData != null) {
					File f = new File(appData);
					if (f.isDirectory() && f.canWrite()) {
						outputDir = new File(f, "OpenFlexo/Logs");
						ok = true;
					}
					if (!ok) {
						appData = System.getenv("APPDATA");
						if (appData != null) {
							f = new File(appData);
							if (f.isDirectory() && f.canWrite()) {
								outputDir = new File(f, "OpenFlexo/Logs");
								ok = true;
							}
						}
					}
				}
			}
			else if (ToolBox.isLinux()) {
				outputDir = new File(System.getProperty("user.home"), ".openflexo/logs");
			}
			return outputDir;
		}

		@Override
		public Resource getCustomLoggingFile() {
			if (getLoggingFileName() == null) {
				return null;
			}
			return ResourceLocator.locateResource(getLoggingFileName());
		}

		@Override
		public Level getDefaultLoggingLevel() {
			String returned = getDefaultLoggingLevelAsString();
			if (returned == null) {
				return null;
			}
			else if (returned.equals("SEVERE")) {
				return Level.SEVERE;
			}
			else if (returned.equals("WARNING")) {
				return Level.WARNING;
			}
			else if (returned.equals("INFO")) {
				return Level.INFO;
			}
			else if (returned.equals("FINE")) {
				return Level.FINE;
			}
			else if (returned.equals("FINER")) {
				return Level.FINER;
			}
			else if (returned.equals("FINEST")) {
				return Level.FINEST;
			}
			return null;
		}

		@Override
		public void setDefaultLoggingLevel(Level l) {
			setDefaultLoggingLevelAsString(l.getName());
		}

		@Override
		public boolean limitLogCount() {
			return getMaxLogCount() > -1;
		}

		@Override
		public void setLimitLogCount(boolean limitLogCount) {
			if (limitLogCount != limitLogCount()) {
				if (limitLogCount) {
					setMaxLogCount(500);
				}
				else {
					setMaxLogCount(-1);
				}
				getPropertyChangeSupport().firePropertyChange("limitLogCount", !limitLogCount, limitLogCount);
			}
		}

		@Override
		public void setMaxLogCount(int c) {
			boolean oldValue = limitLogCount();
			performSuperSetter(LOGCOUNT, c);
			getPropertyChangeSupport().firePropertyChange("limitLogCount", oldValue, limitLogCount());
		}

		@Override
		public Level[] getAvailableLoggingLevels() {
			return AVAILABLE_LEVELS;
		}
	}

}
