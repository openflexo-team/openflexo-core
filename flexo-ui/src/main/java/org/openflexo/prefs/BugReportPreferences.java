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

import java.util.logging.Logger;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * Encodes general preferences for the whole application
 * 
 * @author sguerin
 * 
 */
@ModelEntity
@ImplementationClass(BugReportPreferences.BugReportPreferencesImpl.class)
@XMLElement(xmlTag = "BugReportPreferences")
@Preferences(
		shortName = "Bug reporting",
		longName = "Bug reporting preferences",
		FIBPanel = "Fib/Prefs/BugReportPreferences.fib",
		smallIcon = "Icons/Common/BugReportService.png",
		bigIcon = "Icons/Common/BugReportService_64x64.png")
public interface BugReportPreferences extends PreferencesContainer {

	public static final String BUG_REPORT_URL_KEY = "bug_report_url";
	public static final String BUG_REPORT_USER = "bug_report_user";
	public static final String BUG_REPORT_PASSWORD = "bug_report_password";

	@Getter(value = BUG_REPORT_URL_KEY, defaultValue = "https://bugs.openflexo.org")
	@XMLAttribute
	public String getBugReportUrl();

	@Setter(BUG_REPORT_URL_KEY)
	public void setBugReportUrl(String bugReportURL);

	@Getter(BUG_REPORT_PASSWORD)
	@XMLAttribute
	public String getBugReportPassword();

	@Setter(BUG_REPORT_PASSWORD)
	public void setBugReportPassword(String password);

	@Getter(BUG_REPORT_USER)
	@XMLAttribute
	public String getBugReportUser();

	@Setter(BUG_REPORT_USER)
	public void setBugReportUser(String user);

	public abstract class BugReportPreferencesImpl extends PreferencesContainerImpl implements BugReportPreferences {

		private static final Logger logger = Logger.getLogger(BugReportPreferences.class.getPackage().getName());

	}

}
