/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.view.menu;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.openflexo.FlexoCst;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.ControllerModel;

/**
 * 'Edit' menu
 * 
 * @author sguerin
 */
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
		return _controller.getEditingContext().getUndoManager();
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
			} else {
				if (evt.getPropertyName().equals(FlexoUndoManager.ACTION_HISTORY) || evt.getPropertyName().equals(FlexoUndoManager.ENABLED)
						|| evt.getPropertyName().equals(FlexoUndoManager.START_RECORDING)
						|| evt.getPropertyName().equals(FlexoUndoManager.STOP_RECORDING)
						|| evt.getPropertyName().equals(FlexoUndoManager.UNDONE) || evt.getPropertyName().equals(FlexoUndoManager.REDONE)) {
					updateWithUndoManagerState();
				}
			}
		}

		private void updateWithUndoManagerState() {
			if (getUndoManager() != null) {
				setEnabled(getUndoManager().canUndo());
				if (getUndoManager().canUndo()) {
					setText(FlexoLocalization.localizedForKey("undo") + " (" + getUndoManager().getUndoPresentationName() + ")");
				} else {
					setText(FlexoLocalization.localizedForKey("undo"));
				}
			} else {
				setText(FlexoLocalization.localizedForKey("undo"));
				setEnabled(false);
			}
		}

		@Override
		public void itemWillShow() {

		}
	}

	public class UndoAction extends AbstractAction {
		public UndoAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			if (getUndoManager().canUndo()) {
				logger.info("Undoing: " + getUndoManager().editToBeUndone().getPresentationName());
				getUndoManager().undo();
			} else if (getUndoManager().canUndoIfStoppingCurrentEdition()) {
				getUndoManager().stopRecording(getUndoManager().getCurrentEdition());
				if (getUndoManager().canUndo()) {
					logger.info("Undoing: " + getUndoManager().editToBeUndone().getPresentationName());
					getUndoManager().undo();
				}
			} else {
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
			if (evt.getSource() == _controller) {
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
			} else {
				if (evt.getPropertyName().equals(FlexoUndoManager.ACTION_HISTORY) || evt.getPropertyName().equals(FlexoUndoManager.ENABLED)
						|| evt.getPropertyName().equals(FlexoUndoManager.START_RECORDING)
						|| evt.getPropertyName().equals(FlexoUndoManager.STOP_RECORDING)
						|| evt.getPropertyName().equals(FlexoUndoManager.UNDONE) || evt.getPropertyName().equals(FlexoUndoManager.REDONE)) {
					updateWithUndoManagerState();
				}
			}
		}

		private void updateWithUndoManagerState() {
			if (getUndoManager() != null) {
				setEnabled(getUndoManager().canRedo());
				if (getUndoManager().canRedo()) {
					setText(FlexoLocalization.localizedForKey("redo") + " (" + getUndoManager().getRedoPresentationName() + ")");
				} else {
					setText(FlexoLocalization.localizedForKey("redo"));
				}
			} else {
				setEnabled(false);
				setText(FlexoLocalization.localizedForKey("redo"));
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
				System.out.println("Perform REDO");
				getUndoManager().redo();
			} else {
				System.out.println("Cannot REDO");
			}
		}

	}

	// ==============================================
	// ================== Copy ======================
	// ==============================================

	public class CopyItem extends FlexoMenuItem {

		public CopyItem() {
			super(_controller.getEditingContext().getCopyActionType(), KeyStroke.getKeyStroke(KeyEvent.VK_C, FlexoCst.META_MASK),
					IconLibrary.COPY_ICON, _controller);
		}
	}

	// ==============================================
	// ================== Paste ======================
	// ==============================================

	public class PasteItem extends FlexoMenuItem {

		public PasteItem() {
			super(_controller.getEditingContext().getPasteActionType(), KeyStroke.getKeyStroke(KeyEvent.VK_V, FlexoCst.META_MASK),
					IconLibrary.PASTE_ICON, _controller);
		}
	}

	// ==============================================
	// ================== Cut ======================
	// ==============================================

	public class CutItem extends FlexoMenuItem {

		public CutItem() {
			super(_controller.getEditingContext().getCutActionType(), KeyStroke.getKeyStroke(KeyEvent.VK_X, FlexoCst.META_MASK),
					IconLibrary.CUT_ICON, _controller);
		}
	}

	// ==============================================
	// ================== SelectAll ======================
	// ==============================================

	public class SelectAllItem extends FlexoMenuItem {

		public SelectAllItem() {
			super(_controller.getEditingContext().getSelectAllActionType(), _controller);
		}
	}

}
