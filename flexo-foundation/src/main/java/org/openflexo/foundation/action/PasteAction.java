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

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.fge.control.exceptions.PasteException;
import org.openflexo.fge.geom.FGEPoint;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.exceptions.ModelExecutionException;
import org.openflexo.model.factory.Clipboard;
import org.openflexo.model.factory.ModelFactory;

public class PasteAction extends FlexoAction<PasteAction, FlexoObject, FlexoObject> {

	private static final Logger logger = Logger.getLogger(PasteAction.class.getPackage().getName());

	public static class PasteActionType extends FlexoActionType<PasteAction, FlexoObject, FlexoObject> {

		private final FlexoEditingContext editingContext;

		public PasteActionType(FlexoEditingContext editingContext) {
			super("paste", FlexoActionType.editGroup);
			this.editingContext = editingContext;
		}

		/**
		 * Factory method
		 */
		@Override
		public PasteAction makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {

			PasteAction returned = new PasteAction(this, focusedObject, globalSelection, editor);
			returned.editingContext = editingContext;
			return returned;
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return isPastable(retrievePastingContext(object, globalSelection));
		}

		/**
		 * Return boolean indicating if the current selection is suitable for a PASTE action in supplied context and position
		 * 
		 * @return
		 */
		public boolean isPastable(FlexoObject pastingContext) {

			// System.out.println("Is pastable in context: " + pastingContext + " ??");

			if (editingContext == null) {
				logger.warning("Unexpected null EditingContext in PASTE");
				return false;
			}

			// System.out.println("clipboard:" + editingContext.getClipboard());

			if (editingContext.getClipboard() == null) {
				return false;
			}
			// System.out.println("clipboard:" + editingContext.getClipboard().debug());
			if (pastingContext == null) {
				return false;
			}
			ModelFactory factory = editingContext.getClipboard().getModelFactory();

			// System.out.println("returning: " + factory.isPastable(editingContext.getClipboard(), pastingContext));

			return factory.isPastable(editingContext.getClipboard(), pastingContext);
		}

		public FlexoObject retrievePastingContext(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return object;
		}

	};

	private FlexoEditingContext editingContext;

	PasteAction(PasteActionType actionType, FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) {
		// TODO
		logger.info("Perform PASTE");
		try {
			paste();
		} catch (PasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModelExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModelDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public PasteActionType getActionType() {
		return (PasteActionType) super.getActionType();
	}

	/**
	 * Paste current Clipboard in supplied context and position
	 * 
	 * @throws PasteException
	 * @throws CloneNotSupportedException
	 * @throws ModelDefinitionException
	 * @throws ModelExecutionException
	 * 
	 */
	private Object paste() throws PasteException, ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {

		Clipboard clipboard = editingContext.getClipboard();

		// System.out.println("===========================>>>>>>>>>>>>> OK, we perform paste now with clipboard: ");
		// System.out.println(clipboard.debug());

		FlexoObject pastingContext = getActionType().retrievePastingContext(getFocusedObject(), getGlobalSelection());

		// System.out.println("Perform paste in pastingContext=" + pastingContext);

		ModelFactory factory = editingContext.getClipboard().getModelFactory();
		return factory.paste(editingContext.getClipboard(), pastingContext);

		/*if (clipboard != null) {

			// System.out.println("Pasting in " + pastingContext + " at "+pastingLocation);
			FGEPoint p = FGEUtils.convertNormalizedPoint(getDrawing().getRoot(), pastingLocation, pastingContext);

			// This point is valid for RootNode, but need to be translated in a ShapeNode
			if (pastingContext instanceof ShapeNode) {
				p.x = p.x * ((ShapeNode<?>) pastingContext).getWidth();
				p.y = p.y * ((ShapeNode<?>) pastingContext).getHeight();
			}

			prepareClipboardForPasting(p);

			// Prevent pastingContext to be changed
			isSelectingAfterPaste = true;

			// Do the paste
			try {
				Object pasted = getFactory().paste(clipboard, pastingContext.getDrawable());

				// Try to select newly created objects
				clearSelection();
				if (clipboard.isSingleObject()) {
					addToSelectedObjects(getDrawing().getDrawingTreeNode(pasted));
				} else {
					for (Object o : (List<?>) pasted) {
						addToSelectedObjects(getDrawing().getDrawingTreeNode(o));
					}
				}
			} catch (Throwable e) {
				throw new PasteException(e, getFactory());
			}

			// OK, now we can track again new selection to set pastingContext
			isSelectingAfterPaste = false;

			pastingLocation.x = pastingLocation.x + PASTE_DELTA;
			pastingLocation.y = pastingLocation.y + PASTE_DELTA;

		}*/
	}

	/**
	 * This is a hook to set and/or translate some properties of clipboard beeing pasted<br>
	 * This is model-specific, and thus, default implementation does nothing. Please override this
	 * 
	 * @param proposedPastingLocation
	 */
	protected void prepareClipboardForPasting(FGEPoint proposedPastingLocation) {
	}

}
