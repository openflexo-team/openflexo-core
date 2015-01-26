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

package org.openflexo.drm;

import java.util.logging.Logger;

import javax.help.BadIDException;

import org.openflexo.ApplicationContext;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.help.FlexoHelp;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.Language;
import org.openflexo.view.controller.FlexoController;

public class DefaultInspectorHelpDelegate implements HelpDelegate {

	private static final Logger logger = Logger.getLogger(DefaultInspectorHelpDelegate.class.getPackage().getName());

	private final DocResourceManager docResourceManager;

	public DefaultInspectorHelpDelegate(DocResourceManager docResourceManager) {
		this.docResourceManager = docResourceManager;
	}

	@Override
	public boolean displayHelpFor(FlexoObject object) {
		DocItem item = docResourceManager.getDocItemFor(object);
		if (item != null) {
			try {
				logger.info("Trying to display help for " + item.getIdentifier());
				FlexoHelp.getHelpBroker().setCurrentID(item.getIdentifier());
				FlexoHelp.getHelpBroker().setDisplayed(true);
			} catch (BadIDException exception) {
				FlexoController.showError(FlexoLocalization.localizedForKey("sorry_no_help_available_for") + " " + item.getIdentifier());
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isHelpAvailableFor(Class<? extends FlexoObject> objectClass, String propertyName) {
		ApplicationContext applicationContext = (ApplicationContext) docResourceManager.getServiceManager();
		Language language = docResourceManager.getLanguage(applicationContext.getGeneralPreferences().getLanguage());
		DocItem propertyModelItem = docResourceManager.getDocItemFor(objectClass);
		if (propertyModelItem != null) {
			if (propertyModelItem.getLastApprovedActionForLanguage(language) != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getHelpFor(Class<? extends FlexoObject> objectClass, String propertyName) {
		ApplicationContext applicationContext = (ApplicationContext) docResourceManager.getServiceManager();
		Language language = docResourceManager.getLanguage(applicationContext.getGeneralPreferences().getLanguage());
		DocItem propertyModelItem = docResourceManager.getDocItemFor(objectClass);
		if (propertyModelItem != null) {
			if (propertyModelItem.getLastApprovedActionForLanguage(language) != null) {
				return propertyModelItem.getFullHTMLDescription();
			}
		}
		return null;
	}

}
