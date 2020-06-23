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

package org.openflexo.view.controller.action;

import java.util.logging.Logger;

import javax.swing.Icon;

import org.openflexo.foundation.action.AddRepositoryFolder;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionRunnable;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;

public class AddRepositoryFolderInitializer extends ActionInitializer<AddRepositoryFolder, RepositoryFolder, RepositoryFolder> {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public AddRepositoryFolderInitializer(ControllerActionInitializer actionInitializer) {
		super(AddRepositoryFolder.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionRunnable<AddRepositoryFolder, RepositoryFolder, RepositoryFolder> getDefaultInitializer() {
		return (e, action) -> {
			if (action.getFocusedObject() != null) {
				String newFolderName = null;
				while (newFolderName == null) {
					/*newFolderName = FlexoController.askForStringMatchingPattern(
							FlexoLocalization.localizedForKey("enter_name_for_the_new_folder"),
							Pattern.compile(FileUtils.GOOD_CHARACTERS_REG_EXP + "+"),
							FlexoLocalization.localizedForKey("folder_name_cannot_contain_:_\\_\"_:_*_?_<_>_/"));*/
					newFolderName = FlexoController.askForString(action.getLocales().localizedForKey("enter_name_for_the_new_folder"));
					if (newFolderName == null) {
						return false;
					}
					if (newFolderName.trim().length() == 0) {
						FlexoController.showError(action.getLocales().localizedForKey("a_folder_name_cannot_be_empty"));
						return false;
					}
					if (action.getFocusedObject().getFolderNamed(newFolderName) != null) {
						FlexoController.notify(action.getLocales().localizedForKey("there_is_already_a_folder_with that name"));
						newFolderName = null;
					}
				}
				action.setNewFolderName(newFolderName);
				return true;
			}
			else {
				return false;
			}
		};
	}

	@Override
	protected FlexoActionRunnable<AddRepositoryFolder, RepositoryFolder, RepositoryFolder> getDefaultFinalizer() {
		return (e, action) -> {
			// Update ProjectBrowser (normally it should be done with a
			// notification)
			// TODO: do it properly with a notification
			/*if (action.getInvoker() instanceof JTree) {
				Component current = (JTree) action.getInvoker();
				while (current != null) {
					if (current instanceof BrowserView) {
						((BrowserView) current).getBrowser().update();
						return true;
					}
					current = current.getParent();
				}
			}*/
			if (getController().getApplicationContext().getPresentationPreferences().hideEmptyFolders()
					&& action.getNewFolder().getResources().size() == 0) {
				if (FlexoController.confirmWithWarning(
						getController().getApplicationContext().getLocalizationService().getFlexoLocalizer().localizedForKey(
								"<html>your_preferences_hide_empty_folder<br>would_you_like_to_show_empty_folders_?<br>you_can_change_this_option_in_presentation_preferences</html>"))) {
					getController().getApplicationContext().getPresentationPreferences().setHideEmptyFolders(false);
				}
			}
			return true;
		};
	}

	@Override
	protected Icon getEnabledIcon(FlexoActionFactory<AddRepositoryFolder, RepositoryFolder, RepositoryFolder> actionType) {
		return IconLibrary.FOLDER_ICON;
	}

}
