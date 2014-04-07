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
package org.openflexo.foundation.action;

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.model.undo.CompoundEdit;
import org.openflexo.model.undo.UndoManager;

/**
 * An Openflexo-infrastructure specific UndoManager which manage undo though FlexoAction wrapping.<br>
 * 
 * More precisely, {@link CompoundEdit} managed by this {@link UndoManager} are {@link FlexoActionCompoundEdit} instances.<br>
 * Those instances should reference a {@link FlexoAction}, but sometimes this is not the case (when the developper has decided that the edit
 * was at really low-level and not embedded in a FlexoAction)
 * 
 * See {@link UndoManager} documentation for more details
 * 
 * @author sylvain
 * 
 */
@SuppressWarnings("serial")
public class FlexoUndoManager extends UndoManager {

	private static final Logger logger = Logger.getLogger(FlexoUndoManager.class.getPackage().getName());

	public static final String ACTION_HISTORY = "actionHistory";

	private FlexoAction<?, ?, ?> actionBeeingCurrentlyExecuted;

	/**
	 * Called when a FlexoAction is about to be executed
	 * 
	 * @param action
	 *            : the FlexoAction that will be executed
	 */
	public void actionWillBePerformed(FlexoAction<?, ?, ?> action) {
		willDo(action);
	}

	/**
	 * Called when a FlexoAction has just been successfully executed
	 * 
	 * @param action
	 *            : the FlexoAction that has just been successfully executed
	 */
	public <A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject> void actionHasBeenPerformed(A action,
			boolean success) {
		if (success) {
			System.out.println(">>>>>>>>>>>>>>>>>>> OK, l'action s'est bien passe: " + action);
			hasSuccessfullyDone(action);
		} else {
			System.out.println(">>>>>>>>>>>>>>>>>>> L'action ne s'est pas bien passe: " + action);
			compensateFailedAction(action);
		}
	}

	@Override
	public synchronized FlexoActionCompoundEdit startRecording(String presentationName) {
		return (FlexoActionCompoundEdit) super.startRecording(presentationName);
	}

	/**
	 * Returns the the next significant FlexoAction to be undone if <code>undo</code> is invoked. This returns <code>null</code> if there
	 * are no edits to be undone, or if next edit to be undone do not refer to any FlexoAction (a pathological case where an edit has been
	 * done outside FlexoAction context - this means that an abnormal situation occurs, please warn it)
	 * 
	 * @return the next significant edit to be undone
	 */
	public FlexoAction<?, ?, ?> actionToBeUndone() {
		CompoundEdit e = editToBeUndone();
		if (e instanceof FlexoActionCompoundEdit) {
			return ((FlexoActionCompoundEdit) e).getAction();
		}
		logger.warning("Edit to be undone do not refer to any FlexoAction !");
		return null;
	}

	/**
	 * Returns the the next significant edit to be redone if <code>redo</code> is invoked. This returns <code>null</code> if there are no
	 * edits to be redone, or if edit to be redone do not refer to any FlexoAction (a pathological case where an edit has been done outside
	 * FlexoAction context - this means that an abnormal situation occurs, please warn it)
	 * 
	 * @return the next significant edit to be redone
	 */
	public FlexoAction<?, ?, ?> actionToBeRedone() {
		CompoundEdit e = editToBeRedone();
		if (e instanceof FlexoActionCompoundEdit) {
			return ((FlexoActionCompoundEdit) e).getAction();
		}
		logger.warning("Edit to be redone do not refer to any FlexoAction !");
		return null;
	}

	/**
	 * Called when a FlexoAction is about to be executed
	 * 
	 * @param action
	 *            : the FlexoAction that will be executed
	 */
	private void willDo(FlexoAction<?, ?, ?> action) {
		if (action.getCompoundEdit() != null) {
			// CompoundEdit has already been initialized
		} else {
			actionBeeingCurrentlyExecuted = action;
			FlexoActionCompoundEdit compoundEdit = startRecording(action.getLocalizedName());
			action.setCompoundEdit(compoundEdit);
		}
	}

	/**
	 * Called when a FlexoAction has just been successfully executed
	 * 
	 * @param action
	 *            : the FlexoAction that has just been successfully executed
	 */
	private void hasSuccessfullyDone(FlexoAction<?, ?, ?> action) {
		stopRecording(getCurrentEdition());
		actionBeeingCurrentlyExecuted = null;
		getPropertyChangeSupport().firePropertyChange(ACTION_HISTORY, null, action);
	}

	/**
	 * Called when a FlexoAction has just been executed but returned with failure status. We try here to compensate all edits.
	 * 
	 * @param action
	 *            : the FlexoAction that has just been executed but returned with failure status
	 */
	private void compensateFailedAction(FlexoAction<?, ?, ?> action) {
		CompoundEdit currentEdition = getCurrentEdition();
		stopRecording(currentEdition);
		actionBeeingCurrentlyExecuted = null;
		if (canUndo()) {
			undo();
			currentEdition.die();
		}
	}

	@Override
	protected FlexoActionCompoundEdit makeCompoundEdit(String presentationName) {
		return new FlexoActionCompoundEdit(actionBeeingCurrentlyExecuted, presentationName);
	}

	/**
	 * An Openflexo-specific CompoundEdit wrapping all edits of a FlexoAction<br>
	 * Note that at the creation of this {@link CompoundEdit}, the {@link FlexoAction} might be null and set later<br>
	 * This allows to deal with actions requiring some work on model before to really execute it.
	 * 
	 * @author sylvain
	 * 
	 */
	public class FlexoActionCompoundEdit extends CompoundEdit {

		private FlexoAction<?, ?, ?> action;

		public FlexoActionCompoundEdit(FlexoAction<?, ?, ?> action, String presentationName) {
			super(action != null ? action.getLocalizedName() : presentationName);
			this.action = action;
		}

		public FlexoAction<?, ?, ?> getAction() {
			return action;
		}

		public void setAction(FlexoAction<?, ?, ?> action) {
			this.action = action;
		}
	}
}
