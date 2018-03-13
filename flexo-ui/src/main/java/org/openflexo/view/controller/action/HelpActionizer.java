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

package org.openflexo.view.controller.action;

import java.util.EventObject;

import javax.help.BadIDException;
import javax.swing.Icon;

import org.openflexo.action.HelpAction;
import org.openflexo.drm.DocItem;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.help.FlexoHelp;
import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;

public class HelpActionizer extends ActionInitializer<HelpAction, FlexoObject, FlexoObject> {

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger
			.getLogger(HelpActionizer.class.getPackage().getName());

	public HelpActionizer(ControllerActionInitializer actionInitializer) {
		super(HelpAction.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<HelpAction, FlexoObject, FlexoObject> getDefaultInitializer() {
		return new FlexoActionInitializer<HelpAction, FlexoObject, FlexoObject>() {
			@Override
			public boolean run(EventObject e, HelpAction action) {
				return true;
			}
		};
	}

	@Override
	protected FlexoActionFinalizer<HelpAction, FlexoObject, FlexoObject> getDefaultFinalizer() {
		return new FlexoActionFinalizer<HelpAction, FlexoObject, FlexoObject>() {
			@Override
			public boolean run(EventObject e, HelpAction action) {
				DocItem item = getController().getApplicationContext().getDocResourceManager().getDocItemFor(action.getFocusedObject());
				if (item != null) {
					try {
						logger.info("Trying to display help for " + item.getIdentifier());
						FlexoHelp.getHelpBroker().setCurrentID(item.getIdentifier());
						FlexoHelp.getHelpBroker().setDisplayed(true);
					} catch (BadIDException exception) {
						FlexoController.showError(FlexoLocalization.getMainLocalizer().localizedForKey("sorry_no_help_available_for") + " "
								+ item.getIdentifier());
						return false;
					}
					return true;
				}

				return false;
			}
		};
	}

	@Override
	protected Icon getEnabledIcon(FlexoActionFactory actionType) {
		return IconLibrary.HELP_ICON;
	}
}
