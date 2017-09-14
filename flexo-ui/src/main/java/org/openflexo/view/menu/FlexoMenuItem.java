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

package org.openflexo.view.menu;

/*
 * FlexoMenuItem.java
 * Project WorkflowEditor
 * 
 * Created by benoit on Mar 12, 2004
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionSource;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.selection.SelectionManager;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.action.EditionAction;

/**
 * Give a shortcut to the item and register the action near the FlexoMainController
 * 
 * @author benoit
 */
@SuppressWarnings("serial")
public class FlexoMenuItem extends JMenuItem implements FlexoActionSource, PropertyChangeListener {

	@SuppressWarnings("unused")
	private static final Logger logger = FlexoLogger.getLogger(FlexoMenuItem.class.getPackage().getName());

	private final FlexoController _controller;

	private FlexoActionFactory<?, ?, ?> actionType;

	public FlexoMenuItem(FlexoController controller, String unlocalizedMenuName) {
		super();
		_controller = controller;
		_controller.getPropertyChangeSupport().addPropertyChangeListener(this);
		setText(controller.getModuleLocales().localizedForKey(unlocalizedMenuName, this));
	}

	public FlexoMenuItem(AbstractAction action, String flexoActionName, KeyStroke accelerator, FlexoController controller,
			boolean localizeActionName) {
		super(action);
		_controller = controller;
		_controller.getPropertyChangeSupport().addPropertyChangeListener(this);
		if (accelerator != null) {
			setAccelerator(accelerator);
			_controller.registerActionForKeyStroke(action, accelerator, flexoActionName);
		}
		if (localizeActionName) {
			setText(controller.getModuleLocales().localizedForKey(flexoActionName, this));
		}
		else {
			setText(flexoActionName);
		}
	}

	public FlexoMenuItem(AbstractAction action, String flexoActionName, KeyStroke accelerator, Icon icon, FlexoController controller) {
		this(action, flexoActionName, accelerator, controller, true);
		setIcon(icon);
	}

	public FlexoMenuItem(FlexoActionFactory<?, ?, ?> actionType, KeyStroke accelerator, Icon icon, FlexoController controller) {
		this(actionType, controller);
		setIcon(icon);
		if (accelerator != null) {
			setAccelerator(accelerator);
		}
	}

	public FlexoMenuItem(FlexoActionFactory<?, ?, ?> actionType, Icon icon, FlexoController controller) {
		this(actionType, controller);
		setIcon(icon);
	}

	public FlexoMenuItem(FlexoActionFactory<?, ?, ?> actionType, FlexoController controller) {
		super();
		this.actionType = actionType;
		_controller = controller;
		setAction(new EditionAction<>(actionType, this));
		setText(controller.getModuleLocales().localizedForKey(actionType.getUnlocalizedName(), this));
	}

	@Override
	public FlexoObject getFocusedObject() {
		return _controller.getSelectionManager().getLastSelectedObject();
	}

	@Override
	public Vector<FlexoObject> getGlobalSelection() {
		return _controller.getSelectionManager().getSelection();
	}

	@Override
	public FlexoEditor getEditor() {
		return _controller.getEditor();
	}

	private SelectionManager getSelectionManager() {
		return _controller.getSelectionManager();
	}

	/**
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void itemWillShow() {
		if (actionType instanceof FlexoActionFactory && getSelectionManager() != null) {
			if (getFocusedObject() == null || getFocusedObject().getActionList().indexOf(actionType) > -1) {
				setEnabled(((FlexoActionFactory) actionType).isEnabled(getFocusedObject(), getGlobalSelection()));
			}
			else {
				setEnabled(false);
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Nothing is done by default
	}
}
