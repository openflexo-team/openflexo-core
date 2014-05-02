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

import java.awt.Event;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

			PasteHandler<?> handler = editingContext.getPasteHandler(focusedObject, globalSelection, null);

			if (handler == null) {
				System.out.println("Could not find any PasteHandler for focused=" + focusedObject + " and clipboard type: "
						+ editingContext.getClipboard().getLeaderClipboard().getTypes()[0]);
				return false;
			}

			// The checks are performed ONLY on leader clipboard (others clipboards are ignored)

			PastingContext pastingContext = handler.retrievePastingContext(focusedObject, globalSelection, editingContext.getClipboard(),
					null);

			if (pastingContext == null) {
				return false;
			}

			ModelFactory factory = editingContext.getClipboard().getLeaderClipboard().getModelFactory();

			// System.out.println("returning: " + factory.isPastable(editingContext.getClipboard(), pastingContext));

			try {
				return factory.isPastable(editingContext.getClipboard().getLeaderClipboard(), pastingContext.getPastingPointHolder());
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
	private Event event;

	PasteAction(PasteActionType actionType, FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) {
		// TODO
		logger.info("Perform PASTE");

		if (context instanceof Event) {
			this.event = (Event) context;
		}

		try {
			System.out.println("--------- START PASTE");
			Object pasted = paste();
			pastedObjects = new ArrayList<FlexoObject>();
			if (pasted instanceof List) {
				pastedObjects.addAll((List) pasted);
			} else if (pasted instanceof FlexoObject) {
				pastedObjects.add((FlexoObject) pasted);
			} else {
				logger.warning("Unexpected " + pasted);
			}
			System.out.println("--------- END PASTE");

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

	public Event getEvent() {
		return event;
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

		Clipboard leaderClipboard = editingContext.getClipboard().getLeaderClipboard();
		ModelFactory factory = leaderClipboard.getModelFactory();

		if (factory == null) {
			throw new PasteException("Unexpected null ModelFactory in PASTE", null);
		}

		if (getFocusedObject() == null) {
			throw new PasteException("Unexpected null focused object in PASTE", factory);
		}

		PasteHandler<FlexoObject> handler = (PasteHandler<FlexoObject>) editingContext.getPasteHandler(getFocusedObject(),
				getGlobalSelection(), getEvent());

		System.out.println("PasteHandler=" + handler);

		PastingContext pastingContext = handler.retrievePastingContext(getFocusedObject(), getGlobalSelection(),
				editingContext.getClipboard(), getEvent());

		System.out.println("PastingContext=" + pastingContext);

		if (pastingContext == null) {
			throw new PasteException("Unexpected null pasting context in PASTE while using handler " + handler, factory);
		}

		handler.prepareClipboardForPasting(editingContext.getClipboard(), pastingContext);

		// System.out.println("===========================>>>>>>>>>>>>> OK, we perform paste now with clipboard: ");
		// System.out.println(clipboard.debug());
		// System.out.println("Perform paste in pastingContext=" + pastingContext);

		Object returned = factory.paste(editingContext.getClipboard().getLeaderClipboard(), pastingContext.getPastingPointHolder());

		handler.finalizePasting(editingContext.getClipboard(), pastingContext);

		return returned;

	}

	/**
	 * An handler which is used to intercept and translate paste actions from/to the right pasting context
	 * 
	 * @author sylvain
	 * 
	 * @param <T>
	 *            type of target object where this handler applies
	 */
	public static interface PasteHandler<T extends FlexoObject> {

		/**
		 * Return the type of pasting point holder this paste handler might handle
		 * 
		 * @return
		 */
		public Class<T> getPastingPointHolderType();

		/**
		 * Return a {@link PastingContext} if current selection and clipboard allows it.<br>
		 * Otherwise return null
		 * 
		 * @param focusedObject
		 * @param globalSelection
		 * @param clipboard
		 * @return
		 */
		public PastingContext<T> retrievePastingContext(FlexoObject focusedObject, List<FlexoObject> globalSelection,
				FlexoClipboard clipboard, Event event);

		/**
		 * This is a hook to set and/or translate some properties of clipboard beeing pasted
		 * 
		 */
		public void prepareClipboardForPasting(FlexoClipboard clipboard, PastingContext<T> pastingContext);

		/**
		 * This is a hook to finalize paste operation after clipboard has beeing pasted
		 * 
		 */
		public void finalizePasting(FlexoClipboard FlexoClipboard, PastingContext<T> pastingContext);
	}

	/**
	 * A {@link PastingContext} contains all informations to manage a pasting operation<br>
	 * {@link PastingContext} instances should be retrieved from {@link PasteHandler} instances.
	 * 
	 * @author sylvain
	 * 
	 */
	public static interface PastingContext<T extends FlexoObject> {

		/**
		 * Return the object that will hold pasting point for this {@link PastingContext}
		 * 
		 * @return
		 */
		public T getPastingPointHolder();

		/**
		 * Return the (not required) {@link java.awt.Event} from which originate the paste operation<br>
		 * (might be null)
		 * 
		 * @return
		 */
		public Event getEvent();

		public String getPasteProperty(String key);

		public void setPasteProperty(String key, String value);

	}

	/**
	 * This is the default implementation of {@link PastingContext} <br>
	 * Contains all informations to manage a pasting operation
	 * 
	 * @author sylvain
	 * 
	 */
	public static class DefaultPastingContext<T extends FlexoObject> implements PastingContext<T> {

		private final T pastingPointHolder;
		private final Event event;
		private final Map<String, String> pasteProperties = new HashMap<String, String>();

		public DefaultPastingContext(T holder, Event event) {
			this.pastingPointHolder = holder;
			this.event = event;
		}

		/**
		 * Return the object that will hold pasting point for this {@link PastingContext}
		 * 
		 * @return
		 */
		@Override
		public T getPastingPointHolder() {
			return pastingPointHolder;
		}

		/**
		 * Return the (not required) {@link java.awt.Event} from which originate the paste operation<br>
		 * (might be null)
		 * 
		 * @return
		 */
		@Override
		public Event getEvent() {
			return event;
		}

		@Override
		public String getPasteProperty(String key) {
			return pasteProperties.get(key);
		}

		@Override
		public void setPasteProperty(String key, String value) {
			pasteProperties.put(key, value);
		}

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
		public Class<FlexoObject> getPastingPointHolderType() {
			return FlexoObject.class;
		}

		@Override
		public DefaultPastingContext<FlexoObject> retrievePastingContext(FlexoObject focusedObject, List<FlexoObject> globalSelection,
				FlexoClipboard clipboard, Event event) {
			return new DefaultPastingContext<FlexoObject>(focusedObject, event);
		}

		/**
		 * Default implementation does nothing
		 */
		@Override
		public void prepareClipboardForPasting(FlexoClipboard clipboard, PastingContext<FlexoObject> pastingContext) {
			logger.info("prepareClipboardForPasting() called in DefaultPasteHandler");
		}

		/**
		 * Default implementation does nothing
		 */
		@Override
		public void finalizePasting(FlexoClipboard clipboard, PastingContext<FlexoObject> pastingContext) {
			logger.info("finalizePasting() called in DefaultPasteHandler");
		}

	}

}
