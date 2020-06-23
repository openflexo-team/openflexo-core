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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.openflexo.FlexoCst;
import org.openflexo.action.CopyActionInitializer;
import org.openflexo.action.CutActionInitializer;
import org.openflexo.action.PasteActionInitializer;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.action.SelectAllAction;
import org.openflexo.foundation.action.copypaste.CopyAction;
import org.openflexo.foundation.action.copypaste.CutAction;
import org.openflexo.foundation.action.copypaste.PasteAction;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.ControllerModel;

/**
 * 'Edit' menu
 * 
 * @author sguerin
 */
@SuppressWarnings("serial")
public class EditMenu extends FlexoMenu {

	static final Logger logger = Logger.getLogger(EditMenu.class.getPackage().getName());

	public FlexoMenuItem undoItem;
	public FlexoMenuItem redoItem;

	// Following fields might be null if non-implemented
	public FlexoMenuItem deleteItem;
	public FlexoMenuItem cutItem;
	public FlexoMenuItem copyItem;
	public FlexoMenuItem pasteItem;
	public FlexoMenuItem selectAllItem;

	protected FlexoController _controller;

	public EditMenu(FlexoController controller) {
		super("edit", controller);
		_controller = controller;

		add(undoItem = new UndoItem());
		add(redoItem = new RedoItem());
		undoItem.setEnabled(false);
		redoItem.setEnabled(false);

		addSeparator();

		add(copyItem = new CopyItem());
		add(cutItem = new CutItem());
		add(pasteItem = new PasteItem());
		add(selectAllItem = new SelectAllItem());

	}

	public FlexoUndoManager getUndoManager() {
		if (_controller != null) {
			FlexoEditingContext ec = _controller.getEditingContext();
			if (ec != null)
				return ec.getUndoManager();
		}
		return null;
	}

	// ==============================================
	// ================== Undo ======================
	// ==============================================

	public class UndoItem extends FlexoMenuItem {

		public UndoItem() {
			super(new UndoAction(), "undo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, FlexoCst.META_MASK), IconLibrary.UNDO_ICON,
					getController());
			manager.addListener(ControllerModel.CURRENT_EDITOR, this, _controller.getControllerModel());
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == _controller && evt.getPropertyName().equals(FlexoController.EDITOR)) {

				if (evt.getOldValue() != null) {
					FlexoEditor old = (FlexoEditor) evt.getOldValue();
					if (old.getUndoManager() != null) {
						manager.removeListener(FlexoUndoManager.ACTION_HISTORY, this, old.getUndoManager());
						manager.removeListener(FlexoUndoManager.ENABLED, this, old.getUndoManager());
						manager.removeListener(FlexoUndoManager.START_RECORDING, this, old.getUndoManager());
						manager.removeListener(FlexoUndoManager.STOP_RECORDING, this, old.getUndoManager());
						manager.removeListener(FlexoUndoManager.UNDONE, this, old.getUndoManager());
						manager.removeListener(FlexoUndoManager.REDONE, this, old.getUndoManager());
					}
				}
				if (evt.getNewValue() != null) {
					FlexoEditor editor = (FlexoEditor) evt.getNewValue();
					if (editor.getUndoManager() != null) {
						manager.addListener(FlexoUndoManager.ACTION_HISTORY, this, editor.getUndoManager());
						manager.addListener(FlexoUndoManager.ENABLED, this, editor.getUndoManager());
						manager.addListener(FlexoUndoManager.START_RECORDING, this, editor.getUndoManager());
						manager.addListener(FlexoUndoManager.STOP_RECORDING, this, editor.getUndoManager());
						manager.addListener(FlexoUndoManager.UNDONE, this, editor.getUndoManager());
						manager.addListener(FlexoUndoManager.REDONE, this, editor.getUndoManager());
					}
					updateWithUndoManagerState();
				}
			}
			else {
				if (evt.getPropertyName().equals(FlexoUndoManager.ACTION_HISTORY) || evt.getPropertyName().equals(FlexoUndoManager.ENABLED)
						|| evt.getPropertyName().equals(FlexoUndoManager.START_RECORDING)
						|| evt.getPropertyName().equals(FlexoUndoManager.STOP_RECORDING)
						|| evt.getPropertyName().equals(FlexoUndoManager.UNDONE) || evt.getPropertyName().equals(FlexoUndoManager.REDONE)) {
					updateWithUndoManagerState();
				}
			}
		}

		private void updateWithUndoManagerState() {
			// Fixed OP-11 (DeadLock when opening several diagram in FME)
			// This issue was caused be this method invokation during an application task (FlexoTask)
			// This make a call to Swing (to enable/disable undo/redo item), and might lead to a DeadLock
			// The solution is here to delay this update in the event dispatch thread
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(() -> updateWithUndoManagerState());
				return;
			}
			if (getUndoManager() != null) {
				setEnabled(getUndoManager().canUndo());
				if (getUndoManager().canUndo()) {
					setText(_controller.getFlexoLocales().localizedForKey("undo") + " (" + getUndoManager().getUndoPresentationName()
							+ ")");
				}
				else {
					setText(_controller.getFlexoLocales().localizedForKey("undo"));
				}
			}
			else {
				setText(_controller.getFlexoLocales().localizedForKey("undo"));
				setEnabled(false);
			}
		}
	}

	public class UndoAction extends AbstractAction {
		public UndoAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			if (getUndoManager().canUndo()) {
				String presentationName = getUndoManager().editToBeUndone().getPresentationName();
				logger.info("Undoing: " + presentationName);
				getUndoManager().undo();
				_controller.setInfoMessage("Undone " + presentationName, true);
			}
			else if (getUndoManager().canUndoIfStoppingCurrentEdition()) {
				getUndoManager().stopRecording(getUndoManager().getCurrentEdition());
				if (getUndoManager().canUndo()) {
					String presentationName = getUndoManager().editToBeUndone().getPresentationName();
					logger.info("Undoing: " + presentationName);
					getUndoManager().undo();
					_controller.setInfoMessage("Undone " + presentationName, true);
				}
			}
			else {
				_controller.setInfoMessage("Cannot UNDO", true);
				logger.info("Cannot UNDO");
				getUndoManager().debug();
			}
		}

	}

	// ==============================================
	// ================== Redo ======================
	// ==============================================

	public class RedoItem extends FlexoMenuItem {

		public RedoItem() {
			super(new RedoAction(), "redo", KeyStroke.getKeyStroke(KeyEvent.VK_Y, FlexoCst.META_MASK), IconLibrary.REDO_ICON,
					getController());
			manager.addListener(ControllerModel.CURRENT_EDITOR, this, _controller.getControllerModel());
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == _controller && evt.getPropertyName().equals(FlexoController.EDITOR)) {
				if (evt.getOldValue() != null) {
					FlexoEditor old = (FlexoEditor) evt.getOldValue();
					if (old.getUndoManager() != null) {
						manager.removeListener(FlexoUndoManager.ACTION_HISTORY, this, old.getUndoManager());
						manager.removeListener(FlexoUndoManager.ENABLED, this, old.getUndoManager());
						manager.removeListener(FlexoUndoManager.START_RECORDING, this, old.getUndoManager());
						manager.removeListener(FlexoUndoManager.STOP_RECORDING, this, old.getUndoManager());
						manager.removeListener(FlexoUndoManager.UNDONE, this, old.getUndoManager());
						manager.removeListener(FlexoUndoManager.REDONE, this, old.getUndoManager());
					}
				}
				if (evt.getNewValue() != null) {
					FlexoEditor editor = (FlexoEditor) evt.getNewValue();
					if (editor.getUndoManager() != null) {
						manager.addListener(FlexoUndoManager.ACTION_HISTORY, this, editor.getUndoManager());
						manager.addListener(FlexoUndoManager.ENABLED, this, editor.getUndoManager());
						manager.addListener(FlexoUndoManager.START_RECORDING, this, editor.getUndoManager());
						manager.addListener(FlexoUndoManager.STOP_RECORDING, this, editor.getUndoManager());
						manager.addListener(FlexoUndoManager.UNDONE, this, editor.getUndoManager());
						manager.addListener(FlexoUndoManager.REDONE, this, editor.getUndoManager());
					}
					updateWithUndoManagerState();
				}
			}
			else {
				if (evt.getPropertyName().equals(FlexoUndoManager.ACTION_HISTORY) || evt.getPropertyName().equals(FlexoUndoManager.ENABLED)
						|| evt.getPropertyName().equals(FlexoUndoManager.START_RECORDING)
						|| evt.getPropertyName().equals(FlexoUndoManager.STOP_RECORDING)
						|| evt.getPropertyName().equals(FlexoUndoManager.UNDONE) || evt.getPropertyName().equals(FlexoUndoManager.REDONE)) {
					updateWithUndoManagerState();
				}
			}
		}

		private void updateWithUndoManagerState() {
			// Fix a Deadlock Similar to OP-11 (DeadLock when opening a diagram in FME after redoing/undoing on some other)
			// This issue was caused be this method invokation during an application task (FlexoTask)
			// This make a call to Swing (to enable/disable undo/redo item), and might lead to a DeadLock
			// The solution is here to delay this update in the event dispatch thread
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(() -> updateWithUndoManagerState());
				return;
			}
			if (getUndoManager() != null) {
				setEnabled(getUndoManager().canRedo());
				if (getUndoManager().canRedo()) {
					setText(_controller.getFlexoLocales().localizedForKey("redo") + " (" + getUndoManager().getRedoPresentationName()
							+ ")");
				}
				else {
					setText(_controller.getFlexoLocales().localizedForKey("redo"));
				}
			}
			else {
				setText(_controller.getFlexoLocales().localizedForKey("redo"));
				setEnabled(false);
			}
		}

	}

	public class RedoAction extends AbstractAction {
		public RedoAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			if (getUndoManager().canRedo()) {
				String presentationName = getUndoManager().editToBeRedone().getPresentationName();
				logger.info("Redoing: " + presentationName);
				getUndoManager().redo();
				_controller.setInfoMessage("Redone " + presentationName, true);
			}
			else {
				_controller.setInfoMessage("Cannot REDO", true);
			}
		}

	}

	// ==============================================
	// ================== Copy ======================
	// ==============================================

	public class CopyItem extends FlexoMenuItemWithFactory<CopyAction> {

		public CopyItem() {
			super(_controller.getEditingContext().getCopyActionType(), CopyActionInitializer.ACCELERATOR, IconLibrary.COPY_ICON,
					_controller);
		}
	}

	// ==============================================
	// ================== Paste ======================
	// ==============================================

	public class PasteItem extends FlexoMenuItemWithFactory<PasteAction> {

		public PasteItem() {
			super(_controller.getEditingContext().getPasteActionType(), PasteActionInitializer.ACCELERATOR, IconLibrary.PASTE_ICON,
					_controller);
		}
	}

	// ==============================================
	// ================== Cut ======================
	// ==============================================

	public class CutItem extends FlexoMenuItemWithFactory<CutAction> {

		public CutItem() {
			super(_controller.getEditingContext().getCutActionType(), CutActionInitializer.ACCELERATOR, IconLibrary.CUT_ICON, _controller);
		}
	}

	// ==============================================
	// ================== SelectAll ======================
	// ==============================================

	public class SelectAllItem extends FlexoMenuItemWithFactory<SelectAllAction> {

		public SelectAllItem() {
			super(_controller.getEditingContext().getSelectAllActionType(), _controller);
		}
	}

}
