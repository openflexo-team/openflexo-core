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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.help.FlexoHelp;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.Language;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.toolbox.StringUtils;

/**
 * Encodes general preferences for the whole application
 * 
 * @author sguerin
 * 
 */
@ModelEntity
@ImplementationClass(GeneralPreferences.GeneralPreferencesImpl.class)
@XMLElement(xmlTag = "GeneralPreferences")
@Preferences(
		shortName = "General",
		longName = "General preferences",
		FIBPanel = "Fib/Prefs/GeneralPreferences.fib",
		smallIcon = "Icons/Flexo/OpenflexoNoText_16.png",
		bigIcon = "Icons/Flexo/OpenflexoNoText_64.png")
public interface GeneralPreferences extends PreferencesContainer {

	public static final String LANGUAGE_KEY = "language";
	public static final String SMTP_SERVER_KEY = "smtpServer";
	public static final String FAVORITE_MODULE_KEY = "favoriteModule";
	public static final String DEFAULT_DOC_FORMAT = "defaultDocFormat";
	public static final String USER_IDENTIFIER_KEY = "userIdentifier";
	public static final String USER_NAME_KEY = "userName";
	public static final String LAST_OPENED_PROJECTS_1 = "lastProjects_1";
	public static final String LAST_OPENED_PROJECTS_2 = "lastProjects_2";
	public static final String LAST_OPENED_PROJECTS_3 = "lastProjects_3";
	public static final String LAST_OPENED_PROJECTS_4 = "lastProjects_4";
	public static final String LAST_OPENED_PROJECTS_5 = "lastProjects_5";
	public static final String NOTIFY_VALID_PROJECT = "notify_valid_project";
	public static final String AUTO_SAVE_ENABLED = "autoSaveEnabled";
	public static final String AUTO_SAVE_INTERVAL = "autoSaveInterval";
	public static final String AUTO_SAVE_LIMIT = "autoSaveLimit";
	public static final String LAST_IMAGE_DIRECTORY = "LAST_IMAGE_DIRECTORY";

	public static final String LOCAL_RESOURCE_CENTER_DIRECTORY = "localResourceCenterDirectory";

	public static final String LOCAL_RESOURCE_CENTER_DIRECTORY2 = "localResourceCenterDirectory2";

	public static final String DIRECTORY_RESOURCE_CENTER_LIST = "directoryResourceCenterList";

	@Getter(value = LANGUAGE_KEY, isStringConvertable = true)
	@XMLAttribute
	public Language getLanguage();

	@Setter(LANGUAGE_KEY)
	public void setLanguage(Language language);

	public List<Language> getAvailableLanguages();

	@Getter(SMTP_SERVER_KEY)
	@XMLAttribute
	public String getSmtpServer();

	@Setter(SMTP_SERVER_KEY)
	public void setSmtpServer(String smtpServer);

	@Getter(FAVORITE_MODULE_KEY)
	@XMLAttribute
	public String getFavoriteModuleName();

	@Setter(FAVORITE_MODULE_KEY)
	public void setFavoriteModuleName(String value);

	@Getter(LAST_OPENED_PROJECTS_1)
	@XMLAttribute
	public String getLastOpenedProject1();

	@Setter(LAST_OPENED_PROJECTS_1)
	public void setLastOpenedProject1(String lastOpenedProjects);

	@Getter(LAST_OPENED_PROJECTS_2)
	@XMLAttribute
	public String getLastOpenedProject2();

	@Setter(LAST_OPENED_PROJECTS_2)
	public void setLastOpenedProject2(String lastOpenedProjects);

	@Getter(LAST_OPENED_PROJECTS_3)
	@XMLAttribute
	public String getLastOpenedProject3();

	@Setter(LAST_OPENED_PROJECTS_3)
	public void setLastOpenedProject3(String lastOpenedProjects);

	@Getter(LAST_OPENED_PROJECTS_4)
	@XMLAttribute
	public String getLastOpenedProject4();

	@Setter(LAST_OPENED_PROJECTS_4)
	public void setLastOpenedProject4(String lastOpenedProjects);

	@Getter(LAST_OPENED_PROJECTS_5)
	@XMLAttribute
	public String getLastOpenedProject5();

	@Setter(LAST_OPENED_PROJECTS_5)
	public void setLastOpenedProject5(String lastOpenedProjects);

	public List<File> getLastOpenedProjects();

	public void setLastOpenedProjects(List<File> files);

	public void addToLastOpenedProjects(File project);

	public boolean isValidationRuleEnabled(ValidationRule<?, ?> rule);

	public void setValidationRuleEnabled(ValidationRule<?, ?> rule, boolean enabled);

	@Override
	@Getter(USER_IDENTIFIER_KEY)
	@XMLAttribute
	public String getUserIdentifier();

	@Override
	@Setter(USER_IDENTIFIER_KEY)
	public void setUserIdentifier(String aUserIdentifier);

	@Getter(USER_NAME_KEY)
	@XMLAttribute
	public String getUserName();

	@Setter(USER_NAME_KEY)
	public void setUserName(String aUserName);

	@Getter(value = NOTIFY_VALID_PROJECT, defaultValue = "true")
	@XMLAttribute
	public boolean getNotifyValidProject();

	@Setter(NOTIFY_VALID_PROJECT)
	public void setNotifyValidProject(boolean flag);

	@Getter(value = AUTO_SAVE_ENABLED, defaultValue = "true")
	@XMLAttribute
	public boolean getAutoSaveEnabled();

	@Setter(AUTO_SAVE_ENABLED)
	public void setAutoSaveEnabled(boolean enabled);

	@Getter(value = AUTO_SAVE_INTERVAL, defaultValue = "5")
	@XMLAttribute
	public int getAutoSaveInterval();

	@Setter(AUTO_SAVE_INTERVAL)
	public void setAutoSaveInterval(int interval);

	/**
	 * 
	 * @return the maximum number of automatic save to perform before deleting the first one
	 */
	@Getter(value = AUTO_SAVE_LIMIT, defaultValue = "12")
	@XMLAttribute
	public int getAutoSaveLimit();

	@Setter(AUTO_SAVE_LIMIT)
	public void setAutoSaveLimit(int limit);

	@Getter(LAST_IMAGE_DIRECTORY)
	@XMLAttribute
	public File getLastImageDirectory();

	@Setter(LAST_IMAGE_DIRECTORY)
	public void setLastImageDirectory(File f);

	@Getter(DIRECTORY_RESOURCE_CENTER_LIST)
	@XMLAttribute
	public String getDirectoryResourceCenterListAsString();

	@Setter(DIRECTORY_RESOURCE_CENTER_LIST)
	public void setDirectoryResourceCenterListAsString(String aString);

	/**
	 * Return the list all all {@link DirectoryResourceCenter} registered for the session
	 * 
	 * @return
	 */
	public List<File> getDirectoryResourceCenterList();

	public void assertDirectoryResourceCenterRegistered(File dirRC);

	public void setDirectoryResourceCenterList(List<File> rcList);

	public abstract class GeneralPreferencesImpl extends PreferencesContainerImpl implements GeneralPreferences {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(GeneralPreferences.class.getPackage().getName());

		@Override
		public String toString() {
			return "GeneralPreferences: " + super.toString();
		}

		@Override
		public Language getLanguage() {
			Language returned = (Language) performSuperGetter(LANGUAGE_KEY);
			if (returned == null) {
				return Language.get(Locale.getDefault());
			}
			return returned;
		}

		@Override
		public void setLanguage(Language language) {
			performSuperSetter(LANGUAGE_KEY, language);
			if (language != null && language.equals(Language.FRENCH)) {
				Locale.setDefault(Locale.FRANCE);
			}
			else {
				Locale.setDefault(Locale.US);
			}
			FlexoLocalization.setCurrentLanguage(language);
			// FlexoLocalization.updateGUILocalized();
			if (language != null) {
				FlexoHelp.configure(language.getIdentifier(), null/*UserType.getCurrentUserType().getIdentifier()*/);
				FlexoHelp.reloadHelpSet();
			}
		}

		@Override
		public List<Language> getAvailableLanguages() {
			return Language.availableValues();
		}

		@Override
		public List<File> getLastOpenedProjects() {
			List<File> files = new ArrayList<File>();
			String s1 = getLastOpenedProject1();
			String s2 = getLastOpenedProject2();
			String s3 = getLastOpenedProject3();
			String s4 = getLastOpenedProject4();
			String s5 = getLastOpenedProject5();
			File f1 = null;
			File f2 = null;
			File f3 = null;
			File f4 = null;
			File f5 = null;
			if (s1 != null) {
				f1 = new File(s1);
				if (f1.exists()) {
					files.add(f1);
				}
			}
			if (s2 != null) {
				f2 = new File(s2);
				if (f2.exists()) {
					files.add(f2);
				}
			}
			if (s3 != null) {
				f3 = new File(s3);
				if (f3.exists()) {
					files.add(f3);
				}
			}
			if (s4 != null) {
				f4 = new File(s4);
				if (f4.exists()) {
					files.add(f4);
				}
			}
			if (s5 != null) {
				f5 = new File(s5);
				if (f5.exists()) {
					files.add(f5);
				}
			}
			return files;
		}

		/**
		 * @param files
		 */
		@Override
		public void setLastOpenedProjects(List<File> files) {
			if (files.size() > 0) {
				setLastOpenedProject1(files.get(0).getAbsolutePath());
			}
			if (files.size() > 1) {
				setLastOpenedProject2(files.get(1).getAbsolutePath());
			}
			if (files.size() > 2) {
				setLastOpenedProject3(files.get(2).getAbsolutePath());
			}
			if (files.size() > 3) {
				setLastOpenedProject4(files.get(3).getAbsolutePath());
			}
			if (files.size() > 4) {
				setLastOpenedProject5(files.get(4).getAbsolutePath());
			}
		}

		@Override
		public void addToLastOpenedProjects(File project) {
			List<File> files = getLastOpenedProjects();
			for (File f : new ArrayList<File>(files)) {
				if (project.equals(f)) {
					files.remove(f);
					break;
				}
			}

			files.add(0, project);
			setLastOpenedProjects(files);
		}

		@Override
		public boolean isValidationRuleEnabled(ValidationRule<?, ?> rule) {
			return assertProperty("VR-" + rule.getClass().getName()).booleanValue(true);
		}

		@Override
		public void setValidationRuleEnabled(ValidationRule<?, ?> rule, boolean enabled) {
			assertProperty("VR-" + rule.getClass().getName()).setBooleanValue(enabled);
		}

		@Override
		public String getUserIdentifier() {

			String returned = (String) performSuperGetter(GeneralPreferences.USER_IDENTIFIER_KEY);

			if (returned == null) {
				String userName = System.getProperty("user.name");
				if (userName.length() > 3) {
					returned = userName.substring(0, 3);
					returned = returned.toUpperCase();
				}
				else if (userName.length() > 0) {
					returned = userName.substring(0, userName.length());
					returned = returned.toUpperCase();
				}
				else {
					returned = "FLX";
				}
				// setUserIdentifier(returned);
			}

			return returned;
		}

		@Override
		public void setUserIdentifier(String aUserIdentifier) {
			performSuperSetter(GeneralPreferences.USER_IDENTIFIER_KEY, aUserIdentifier);
			FlexoObjectImpl.setCurrentUserIdentifier(aUserIdentifier);
		}

		@Override
		public String getUserName() {
			String returned = (String) performSuperGetter(GeneralPreferences.USER_NAME_KEY);

			if (returned == null) {
				returned = System.getProperty("user.name");
			}

			return returned;
		}

		/**
		 * Return the list all all {@link DirectoryResourceCenter} registered for the session
		 * 
		 * @return
		 */
		@Override
		public List<File> getDirectoryResourceCenterList() {
			String directoriesAsString = getDirectoryResourceCenterListAsString();
			if (StringUtils.isEmpty(directoriesAsString)) {
				return Collections.emptyList();
			}
			else {
				List<File> returned = new ArrayList<File>();
				StringTokenizer st = new StringTokenizer(directoriesAsString, ",");
				while (st.hasMoreTokens()) {
					String next = st.nextToken();
					File f = new File(next);
					if (f.exists()) {
						returned.add(f);
					}
				}
				return returned;
			}
		}

		@Override
		public void assertDirectoryResourceCenterRegistered(File dirRC) {
			List<File> alreadyRegistered = getDirectoryResourceCenterList();
			if (alreadyRegistered.contains(dirRC)) {
				return;
			}
			if (alreadyRegistered.size() == 0) {
				setDirectoryResourceCenterListAsString(dirRC.getAbsolutePath());
			}
			else {
				setDirectoryResourceCenterListAsString(getDirectoryResourceCenterList() + "," + dirRC.getAbsolutePath());
			}
		}

		@Override
		public void setDirectoryResourceCenterList(List<File> rcList) {
			boolean isFirst = true;
			StringBuffer s = new StringBuffer();
			for (File f : rcList) {
				s.append((isFirst ? "" : ",") + f.getAbsolutePath());
				isFirst = false;
			}
			System.out.println("Sets " + s.toString() + " for " + DIRECTORY_RESOURCE_CENTER_LIST);
			setDirectoryResourceCenterListAsString(s.toString());
		}

	}

}
