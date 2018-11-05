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

package org.openflexo.components;

import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.RequestLoginDialog.LoginData;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.FlexoFrame;

/**
 * Allow to ask a login and a password
 * 
 * @author sguerin
 */
public class RequestLoginDialog extends JFIBDialog<LoginData> {

	private static final Logger logger = FlexoLogger.getLogger(RequestLoginDialog.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/RequestLoginDialog.fib");

	public RequestLoginDialog(ApplicationContext applicationContext) {
		super(applicationContext.getApplicationFIBLibraryService().retrieveFIBComponent(FIB_FILE, true), new LoginData(applicationContext),
				FlexoFrame.getActiveFrame(), true, FlexoLocalization.getMainLocalizer());
		setResizable(false);
	}

	public static class LoginData {

		public String login;
		public String password;

		public LoginData(ApplicationContext applicationContext) {
			login = applicationContext.getAdvancedPrefs().getProxyLogin();
			password = applicationContext.getAdvancedPrefs().getProxyPassword();
		}

	}
}
