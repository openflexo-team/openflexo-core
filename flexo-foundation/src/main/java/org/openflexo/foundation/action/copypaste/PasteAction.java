/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.action.copypaste;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.exceptions.PasteException;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.Clipboard;
import org.openflexo.pamela.factory.PamelaModelFactory;

public class PasteAction extends FlexoAction<PasteAction, FlexoObject, FlexoObject> {

	static final Logger logger = Logger.getLogger(PasteAction.class.getPackage().getName());

	public static class PasteActionType extends FlexoActionFactory<PasteAction, FlexoObject, FlexoObject> {

		private final FlexoEditingContext editingContext;

		public PasteActionType(FlexoEditingContext editingContext) {
			super("paste", FlexoActionFactory.editGroup);
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

			PasteHandler<?> handler = editingContext.getPasteHandler(focusedObject, globalSelection);

			if (handler == null) {
				System.out.println("Could not find any PasteHandler for focused=" + focusedObject + " and clipboard type: "
						+ editingContext.getClipboard().getLeaderClipboard().getTypes()[0]);
				return false;
			}

			// The checks are performed ONLY on leader clipboard (others clipboards are ignored)

			PastingContext pastingContext = handler.retrievePastingContext(focusedObject, globalSelection, editingContext.getClipboard());

			if (pastingContext == null) {
				return false;
			}

			return handler.isPastable(editingContext.getClipboard(), pastingContext);

			/*PamelaModelFactory factory = editingContext.getClipboard().getLeaderClipboard().getModelFactory();
			
			// System.out.println("returning: " + factory.isPastable(editingContext.getClipboard(), pastingContext));
			
			try {
				return factory.isPastable(editingContext.getClipboard().getLeaderClipboard(), pastingContext.getPastingPointHolder());
			} catch (ClipboardOperationException e) {
				return false;
			}*/
		}

		/*public FlexoObject retrievePastingContext(FlexoObject focusedObject, Vector<FlexoObject> globalSelection) {
			return focusedObject;
		}*/

	};

	private FlexoEditingContext editingContext;
	private List<FlexoObject> pastedObjects = null;

	private PasteAction(PasteActionType actionType, FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) {
		// TODO
		logger.info("Perform PASTE");

		try {
			// System.out.println("--------- START PASTE");
			Object pasted = paste();
			pastedObjects = new ArrayList<>();
			if (pasted instanceof List) {
				pastedObjects.addAll((List<FlexoObject>) pasted);
			}
			else if (pasted instanceof FlexoObject) {
				pastedObjects.add((FlexoObject) pasted);
			}
			else {
				logger.warning("Unexpected " + pasted);
			}
			// System.out.println("--------- END PASTE");

		} catch (PasteException e) {
			e.printStackTrace();
		} catch (ModelExecutionException e) {
			e.printStackTrace();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	public List<FlexoObject> getPastedObjects() {
		return pastedObjects;
	}

	@Override
	public PasteActionType getActionFactory() {
		return (PasteActionType) super.getActionFactory();
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
		PamelaModelFactory factory = leaderClipboard.getModelFactory();

		if (factory == null) {
			throw new PasteException("Unexpected null PamelaModelFactory in PASTE", null);
		}

		if (getFocusedObject() == null) {
			throw new PasteException("Unexpected null focused object in PASTE");
		}

		PasteHandler<FlexoObject> handler = (PasteHandler<FlexoObject>) editingContext.getPasteHandler(getFocusedObject(),
				getGlobalSelection());

		System.out.println("PasteHandler=" + handler);

		PastingContext<FlexoObject> pastingContext = handler.retrievePastingContext(getFocusedObject(), getGlobalSelection(),
				editingContext.getClipboard());

		System.out.println("PastingContext=" + pastingContext);

		if (pastingContext == null) {
			throw new PasteException("Unexpected null pasting context in PASTE while using handler " + handler);
		}

		handler.prepareClipboardForPasting(editingContext.getClipboard(), pastingContext);

		Object returned = handler.paste(editingContext.getClipboard(), pastingContext);

		handler.finalizePasting(editingContext.getClipboard(), pastingContext);

		return returned;

	}
}
