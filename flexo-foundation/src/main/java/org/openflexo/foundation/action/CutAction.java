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

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.fge.control.exceptions.CopyException;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.resource.PamelaResource;

public class CutAction extends AbstractCopyAction<CutAction> {

	private static final Logger logger = Logger.getLogger(CutAction.class.getPackage().getName());

	public static class CutActionType extends AbstractCopyActionType<CutAction> {

		public CutActionType(FlexoEditingContext editingContext) {
			super("cut", editingContext);
		}

		/**
		 * Factory method
		 */
		@Override
		public CutAction makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			try {
				prepareCopy(getGlobalSelectionAndFocusedObject(focusedObject, globalSelection));
			} catch (InvalidSelectionException e) {
				return null;
			}
			CutAction returned = new CutAction(this, focusedObject, globalSelection, editor);
			returned.editingContext = editingContext;
			returned.objectsToBeCopied = objectsToBeCopied;
			return returned;
		}
	};

	private FlexoEditingContext editingContext;
	protected Map<PamelaResource<?, ?>, List<FlexoObject>> objectsToBeCopied;

	private FlexoClipboard clipboard;

	CutAction(CutActionType actionType, FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws InvalidSelectionException {
		logger.info("CUT");
		try {
			clipboard = cut();
			editingContext.setClipboard(clipboard);
		} catch (CopyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Cut current selection in the clipboard
	 * 
	 * @throws CopyException
	 * @throws InvalidSelectionException
	 */
	private FlexoClipboard cut() throws CopyException, InvalidSelectionException {

		clipboard = FlexoClipboard.cut(objectsToBeCopied, getFocusedObject(), null);
		return clipboard;
	}

	public FlexoClipboard getClipboard() {
		return clipboard;
	}

}
