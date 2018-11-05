/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.br.view;

import org.openflexo.ApplicationContext;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.view.FlexoFrame;

public class JIRAURLCredentialsDialog {

	public static final Resource URL_FIB_FILE = ResourceLocator.locateResource("Fib/JIRAURLCredentialsDialog.fib");

	private String login;
	private String password;
	private final ApplicationContext applicationContext;

	public JIRAURLCredentialsDialog(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		login = applicationContext.getBugReportPreferences().getBugReportUser();
		password = applicationContext.getBugReportPreferences().getBugReportPassword();
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrlLabel() {
		return "<html><a href=\"" + applicationContext.getBugReportPreferences().getBugReportUrl() + "\">"
				+ applicationContext.getBugReportPreferences().getBugReportUrl() + "</a></html>";
	}

	public void openUrl() {
		ToolBox.openURL(applicationContext.getBugReportPreferences().getBugReportUrl());
	}

	public static boolean askLoginPassword(ApplicationContext applicationContext) {
		JIRAURLCredentialsDialog credentialsDialog = new JIRAURLCredentialsDialog(applicationContext);
		JFIBDialog<JIRAURLCredentialsDialog> dialog = JFIBDialog.instanciateAndShowDialog(URL_FIB_FILE, credentialsDialog,
				applicationContext.getApplicationFIBLibraryService().getApplicationFIBLibrary(), FlexoFrame.getActiveFrame(), true,
				FlexoLocalization.getMainLocalizer());
		if (dialog.getStatus() == Status.VALIDATED) {
			applicationContext.getBugReportPreferences().setBugReportUser(credentialsDialog.login);
			applicationContext.getBugReportPreferences().setBugReportPassword(credentialsDialog.password);
			applicationContext.getPreferencesService().savePreferences();
			// AdvancedPrefs.save();
			return true;
		}
		return false;
	}
}
