/**
 * 
 * Copyright (c) 2026, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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


package org.openflexo.foundation.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.pamela.undo.AtomicEdit;

/**
 * The compensation of a failed action by {@link FlexoUndoManager}: only the edits of that action are undone (CORE-D-81). An action that
 * edited nothing used to undo the previous action, for instance the creation of the instance it ran on.
 */
public class TestFlexoUndoManager {

	/** An action that does nothing: the test drives the undo manager around it */
	private static class TestAction extends FlexoAction<TestAction, FlexoObject, FlexoObject> {
		TestAction() {
			super(null, null, null, null);
		}

		@Override
		protected void doAction(Object context) throws FlexoException {
		}
	}

	/** A significant edit that records whether it was undone */
	private static class TestEdit extends AtomicEdit<Object> {
		boolean undone = false;

		TestEdit() {
			super(null, null);
		}

		@Override
		public Object getObject() {
			return null;
		}

		@Override
		public String getType() {
			return "test";
		}

		@Override
		public void undo() {
			undone = true;
		}

		@Override
		public boolean canUndo() {
			return !undone;
		}

		@Override
		public void redo() {
			undone = false;
		}

		@Override
		public boolean canRedo() {
			return undone;
		}

		@Override
		public boolean isSignificant() {
			return true;
		}

		@Override
		public String getPresentationName() {
			return "test edit";
		}
	}

	private static TestEdit runAction(FlexoUndoManager undoManager, boolean edits, boolean success) {
		TestAction action = new TestAction();
		undoManager.actionWillBePerformed(action);
		TestEdit edit = null;
		if (edits) {
			edit = new TestEdit();
			undoManager.addEdit(edit);
		}
		undoManager.actionHasBeenPerformed(action, success);
		return edit;
	}

	@Test
	public void aFailedActionThatEditedNothingUndoesNothing() {
		FlexoUndoManager undoManager = new FlexoUndoManager(null);
		TestEdit creation = runAction(undoManager, true, true);
		runAction(undoManager, false, false);
		assertFalse("The previous action was undone by the compensation of a failed one", creation.undone);
		assertTrue(undoManager.canUndo());
	}

	@Test
	public void aFailedActionIsCompensated() {
		FlexoUndoManager undoManager = new FlexoUndoManager(null);
		TestEdit creation = runAction(undoManager, true, true);
		TestEdit failedEdit = runAction(undoManager, true, false);
		assertTrue("The edits of the failed action are not undone", failedEdit.undone);
		assertFalse("The previous action was undone too", creation.undone);
	}
}
