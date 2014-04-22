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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.fge.control.exceptions.PasteException;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.exceptions.ModelExecutionException;
import org.openflexo.model.factory.Clipboard;
import org.openflexo.model.factory.ClipboardOperationException;
import org.openflexo.model.factory.ModelFactory;

public class PasteAction extends FlexoAction<PasteAction, FlexoObject, FlexoObject> {

	private static final Logger logger = Logger.getLogger(PasteAction.class.getPackage().getName());

	public static class PasteActionType extends FlexoActionType<PasteAction, FlexoObject, FlexoObject> {

		private final FlexoEditingContext editingContext;

		public PasteActionType(FlexoEditingContext editingContext) {
			super("paste", FlexoActionType.editGroup);
			this.editingContext = editingContext;
		}

		// Override parent implementation by preventing check that this ActionType is registered in FlexoObject
		// (This is to be assumed, as action type is here dynamic)
		@Override
		public boolean isEnabled(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return isEnabledForSelection(object, globalSelection);
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
		public boolean isVisibleForSelection(FlexoObject focusedObject, Vector<FlexoObject> globalSelection) {
			return true;
		}

		/**
		 * Return boolean indicating if the current selection is suitable for a PASTE action in supplied context and position
		 * 
		 * @return
		 */
		@Override
		public boolean isEnabledForSelection(FlexoObject focusedObject, Vector<FlexoObject> globalSelection) {

			if (focusedObject == null) {
				logger.warning("Unexpected null focused object in PASTE");
				return false;
			}

			if (editingContext == null) {
				logger.warning("Unexpected null EditingContext in PASTE");
				return false;
			}

			if (editingContext.getClipboard() == null) {
				return false;
			}

			PasteHandler<?> handler = editingContext.getPasteHandler(focusedObject);

			FlexoObject pastingContext = handler.retrievePastingContext(focusedObject, globalSelection, editingContext.getClipboard());

			if (pastingContext == null) {
				return false;
			}

			ModelFactory factory = editingContext.getClipboard().getModelFactory();

			// System.out.println("returning: " + factory.isPastable(editingContext.getClipboard(), pastingContext));

			try {
				return factory.isPastable(editingContext.getClipboard(), pastingContext);
			} catch (ClipboardOperationException e) {
				return false;
			}
		}

		/*public FlexoObject retrievePastingContext(FlexoObject focusedObject, Vector<FlexoObject> globalSelection) {
			return focusedObject;
		}*/

	};

	private FlexoEditingContext editingContext;
	private List<FlexoObject> pastedObjects = null;

	PasteAction(PasteActionType actionType, FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) {
		// TODO
		logger.info("Perform PASTE");
		try {
			Object pasted = paste();
			pastedObjects = new ArrayList<FlexoObject>();
			if (pasted instanceof List) {
				pastedObjects.addAll((List) pasted);
			} else if (pasted instanceof FlexoObject) {
				pastedObjects.add((FlexoObject) pasted);
			} else {
				logger.warning("Unexpected " + pasted);
			}

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

	public List<FlexoObject> getPastedObjects() {
		return pastedObjects;
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

		if (editingContext == null) {
			throw new PasteException("Unexpected null EditingContext in PASTE", null);
		}

		if (editingContext.getClipboard() == null) {
			throw new PasteException("Unexpected null Clipboard in PASTE", null);
		}

		Clipboard clipboard = editingContext.getClipboard();
		ModelFactory factory = clipboard.getModelFactory();

		if (factory == null) {
			throw new PasteException("Unexpected null ModelFactory in PASTE", null);
		}

		if (getFocusedObject() == null) {
			throw new PasteException("Unexpected null focused object in PASTE", factory);
		}

		PasteHandler<FlexoObject> handler = (PasteHandler<FlexoObject>) editingContext.getPasteHandler(getFocusedObject());

		System.out.println("PasteHandler=" + handler);

		FlexoObject pastingContext = handler.retrievePastingContext(getFocusedObject(), getGlobalSelection(), clipboard);

		if (pastingContext == null) {
			throw new PasteException("Unexpected null pasting context in PASTE while using handler " + handler, factory);
		}

		handler.prepareClipboardForPasting(clipboard, pastingContext);

		// System.out.println("===========================>>>>>>>>>>>>> OK, we perform paste now with clipboard: ");
		// System.out.println(clipboard.debug());
		// System.out.println("Perform paste in pastingContext=" + pastingContext);

		return factory.paste(editingContext.getClipboard(), pastingContext);

	}

	/**
	 * An handler which is used to intercept and translate paste actions from/to the right context
	 * 
	 * @author sylvain
	 * 
	 * @param <T>
	 *            type of target object where this handler applies
	 */
	public static interface PasteHandler<T extends FlexoObject> {

		public T retrievePastingContext(FlexoObject focusedObject, List<FlexoObject> globalSelection, Clipboard clipboard);

		/**
		 * This is a hook to set and/or translate some properties of clipboard beeing pasted
		 * 
		 */
		public void prepareClipboardForPasting(Clipboard clipboard, T pastingContext);
	}

	/**
	 * This is the default implementation of {@link PasteHandler}<br>
	 * Pasting context is retrieved as focused object, and default paste is performed without any data translation
	 * 
	 * @author sylvain
	 * 
	 */
	public static class DefaultPasteHandler implements PasteHandler<FlexoObject> {

		@Override
		public FlexoObject retrievePastingContext(FlexoObject focusedObject, List<FlexoObject> globalSelection, Clipboard clipboard) {
			return focusedObject;
		}

		@Override
		public void prepareClipboardForPasting(Clipboard clipboard, FlexoObject pastingContext) {
			logger.info("prepareClipboardForPasting() called in DefaultPasteHandler");
		}

	}

}
