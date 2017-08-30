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

import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.AbstractAction;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionSource;
import org.openflexo.foundation.action.FlexoActionFactory;

public class EditionAction<A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> extends AbstractAction {

	private FlexoActionSource actionSource;
	private FlexoActionFactory<A, T1, T2> actionType;

	private T1 focusedObject;
	private Vector<T2> globalSelection;
	private FlexoEditor editor;

	public EditionAction(FlexoActionFactory<A, T1, T2> actionType, FlexoActionSource actionSource) {
		super();
		this.actionSource = actionSource;
		this.actionType = actionType;
	}

	public EditionAction(FlexoActionFactory<A, T1, T2> actionType, T1 focusedObject, Vector<T2> globalSelection, FlexoEditor editor) {
		super();
		this.actionType = actionType;
		this.focusedObject = focusedObject;
		this.globalSelection = globalSelection;
		this.editor = editor;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && getEditor() != null && getEditor().isActionEnabled(actionType, focusedObject, globalSelection);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getEditor().performActionType(actionType, getFocusedObject(), getGlobalSelection(), e);
	}

	private FlexoEditor getEditor() {
		if (actionSource != null) {
			return actionSource.getEditor();
		}
		return editor;
	}

	public T1 getFocusedObject() {
		if (actionSource != null) {
			return (T1) actionSource.getFocusedObject();
		}
		return focusedObject;
	}

	public Vector<T2> getGlobalSelection() {
		if (actionSource != null) {
			return (Vector<T2>) actionSource.getGlobalSelection();
		}
		return globalSelection;
	}

}
