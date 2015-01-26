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

import java.awt.event.KeyEvent;
import java.util.EventObject;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.openflexo.FlexoCst;
import org.openflexo.action.InspectAction;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;

public class InspectActionizer extends ActionInitializer<InspectAction, FlexoObject, FlexoObject> {

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger.getLogger(InspectActionizer.class.getPackage()
			.getName());

	public InspectActionizer(ControllerActionInitializer actionInitializer) {
		super(InspectAction.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionFinalizer<InspectAction> getDefaultFinalizer() {
		return new FlexoActionFinalizer<InspectAction>() {
			@Override
			public boolean run(EventObject e, InspectAction action) {
				getController().showInspector();
				return true;
			}
		};
	}

	@Override
	protected KeyStroke getShortcut() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_I, FlexoCst.META_MASK);
	}

	@Override
	protected Icon getEnabledIcon() {
		return IconLibrary.INSPECT_ICON;
	}
}
