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

package org.openflexo.view.listener;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionSource;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.view.controller.FlexoController;

public class FlexoActionButton<A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> extends JButton {
	private final ButtonAction action;
	private final FlexoActionSource<T1, T2> actionSource;
	private final FlexoController controller;

	public FlexoActionButton(FlexoActionFactory<A, T1, T2> actionType, FlexoActionSource<T1, T2> source, FlexoController controller) {
		this(actionType, null, source, controller);
	}

	public FlexoActionButton(FlexoActionFactory<A, T1, T2> actionType, String unlocalizedActionName, FlexoActionSource<T1, T2> source,
			FlexoController controller) {
		super();
		actionSource = source;
		this.controller = controller;
		action = new ButtonAction(actionType, unlocalizedActionName);
		setText(action.getLocalizedName(this));
		setToolTipText(controller.getModuleLocales().localizedTooltipForKey(action._unlocalizedName, this));
		if (getEditor() != null) {
			if (getEditor().getEnabledIconFor(actionType) != null) {
				setIcon(getEditor().getEnabledIconFor(actionType));
			}
			if (getEditor().getDisabledIconFor(actionType) != null) {
				setDisabledIcon(getEditor().getDisabledIconFor(actionType));
			}
		}
		addActionListener(action);
	}

	private FlexoEditor getEditor() {
		if (controller != null) {
			return controller.getEditor();
		}
		else {
			return null;
		}
	}

	public void update() {
		setEnabled(action.isEnabled());
	}

	protected List<? extends FlexoObject> getGlobalSelection() {
		return actionSource.getGlobalSelection();
	}

	protected FlexoObject getFocusedObject() {
		return actionSource.getFocusedObject();
	}

	public class ButtonAction implements ActionListener {

		private final FlexoActionFactory<A, T1, T2> actionType;
		private String _unlocalizedName = null;

		public ButtonAction(FlexoActionFactory<A, T1, T2> actionType) {
			super();
			this.actionType = actionType;
		}

		public ButtonAction(FlexoActionFactory<A, T1, T2> actionType, String actionName) {
			this(actionType);
			_unlocalizedName = actionName;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent event) {
			List<? extends FlexoObject> globalSelection = getGlobalSelection();
			if (TypeUtils.isAssignableTo(getFocusedObject(), actionType.getFocusedObjectType())
					&& (globalSelection == null || TypeUtils.isAssignableTo(globalSelection, actionType.getGlobalSelectionType()))) {
				getEditor().performActionFactory(actionType, (T1) getFocusedObject(), (Vector<T2>) globalSelection, event);
			}
		}

		@SuppressWarnings("unchecked")
		public boolean isEnabled() {
			List<? extends FlexoObject> globalSelection = getGlobalSelection();
			if (TypeUtils.isAssignableTo(getFocusedObject(), actionType.getFocusedObjectType())
					&& (globalSelection == null || TypeUtils.isAssignableTo(globalSelection, actionType.getGlobalSelectionType()))) {
				return getEditor().isActionEnabled(actionType, (T1) getFocusedObject(), (Vector<T2>) globalSelection);
			}
			return false;
		}

		public FlexoActionFactory<A, T1, T2> getActionType() {
			return actionType;
		}

		public String getLocalizedName(Component component) {
			LocalizedDelegate locales = actionType.getLocales(getEditor().getServiceManager());
			if (_unlocalizedName == null) {
				return locales.localizedForKey(actionType.getActionName(), component);
			}
			else {
				return locales.localizedForKey(_unlocalizedName, component);
			}
		}

	}
}
