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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResource;

/**
 * Represents abstraction for Copy and Cut action (clipboard operation)<br>
 * 
 * Note that clipboard operations are managed at very low level using PAMELA framework
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractCopyAction<A extends AbstractCopyAction<A>> extends FlexoAction<A, FlexoObject, FlexoObject> {

	private static final Logger logger = Logger.getLogger(AbstractCopyAction.class.getPackage().getName());

	public static abstract class AbstractCopyActionType<A extends AbstractCopyAction<A>>
			extends FlexoActionFactory<A, FlexoObject, FlexoObject> {

		protected final FlexoEditingContext editingContext;

		protected Map<PamelaResource<?, ?>, List<FlexoObject>> objectsToBeCopied;

		public AbstractCopyActionType(String actionName, FlexoEditingContext editingContext) {
			super(actionName, FlexoActionFactory.editGroup);
			this.editingContext = editingContext;
			objectsToBeCopied = new HashMap<>();
		}

		// Override parent implementation by preventing check that this
		// ActionType is registered in FlexoObject
		// (This is to be assumed, as action type is here dynamic)
		@Override
		public boolean isEnabled(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return isEnabledForSelection(object, globalSelection);
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {

			try {
				prepareCopy(getGlobalSelectionAndFocusedObject(object, globalSelection));
				return true;
			} catch (InvalidSelectionException e) {
				// e.printStackTrace();
				// logger.info("Could not COPY for this selection");
				return false;
			}
		}

		/**
		 * Return boolean indicating if the current selection is suitable for a COPY action
		 * 
		 * @return
		 * @throws InvalidSelectionException
		 */
		public void prepareCopy(List<FlexoObject> effectiveSelection) throws InvalidSelectionException {

			objectsToBeCopied.clear();

			// PamelaModelFactory modelFactory = null;

			for (FlexoObject o : effectiveSelection) {
				if (o instanceof InnerResourceData) {
					InnerResourceData<?> data = (InnerResourceData<?>) o;
					if (data.getResourceData() != null) {
						FlexoResource<?> resource = data.getResourceData().getResource();
						if (resource instanceof PamelaResource) {
							List<FlexoObject> objectsInResource = objectsToBeCopied.get(resource);
							if (objectsInResource == null) {
								objectsInResource = new ArrayList<>();
								objectsToBeCopied.put((PamelaResource<?, ?>) resource, objectsInResource);
							}
							objectsInResource.add(o);
						}
						else {
							throw new InvalidSelectionException("Incompatible global model context: could not access PamelaResource");
						}
					}
					else {
						throw new InvalidSelectionException("Incompatible global model context: could not access ResourceData");
					}
				}
			}

			if (objectsToBeCopied.size() == 0) {
				throw new InvalidSelectionException("Incompatible global model context: could not access any PamelaResource");
			}

			// System.out.println("DONE prepare copy");

		}

	};

	protected AbstractCopyAction(AbstractCopyActionType<A> actionType, FlexoObject focusedObject, Vector<FlexoObject> globalSelection,
			FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@SuppressWarnings("serial")
	public static class InvalidSelectionException extends FlexoException {

		public InvalidSelectionException(String message) {
			super(message);
		}

		public InvalidSelectionException(String message, Exception cause) {
			super(message, cause);
		}

	}

}
