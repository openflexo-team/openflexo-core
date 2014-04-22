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

import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.model.factory.ModelFactory;

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

	public static abstract class AbstractCopyActionType<A extends AbstractCopyAction<A>> extends
			FlexoActionType<A, FlexoObject, FlexoObject> {

		protected final FlexoEditingContext editingContext;
		protected List<Object> objectsToBeCopied;
		protected PamelaResource<?, ?> pamelaResource;
		protected Object copyContext;

		public AbstractCopyActionType(String actionName, FlexoEditingContext editingContext) {
			super(actionName, FlexoActionType.editGroup);
			this.editingContext = editingContext;
		}

		// Override parent implementation by preventing check that this ActionType is registered in FlexoObject
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

			List<Object> returned = new ArrayList<Object>();

			ModelFactory modelFactory = null;

			pamelaResource = null;

			for (Object o : effectiveSelection) {
				returned.add(o);
				if (o instanceof InnerResourceData) {
					if (((InnerResourceData) o).getResourceData() != null) {
						FlexoResource<?> resource = ((InnerResourceData) o).getResourceData().getResource();
						if (resource instanceof PamelaResource) {
							if (pamelaResource == null) {
								pamelaResource = (PamelaResource) resource;
							} else if (pamelaResource == resource) {
								// Nice, we are on the same resource
							} else {
								throw new InvalidSelectionException(
										"Incompatible global model context: found objects in many PamelaResources");
							}
						} else {
							throw new InvalidSelectionException("Incompatible global model context: could not access PamelaResource");
						}
					} else {
						throw new InvalidSelectionException("Incompatible global model context: could not access ResourceData");
					}
				}
			}

			if (pamelaResource == null) {
				throw new InvalidSelectionException("Incompatible global model context: could not access any PamelaResource");
			}

			modelFactory = pamelaResource.getFactory();
			if (modelFactory == null) {
				throw new InvalidSelectionException("Incompatible global model context: could not access any model factory");
			}

			try {
				copyContext = pamelaResource.getResourceData(null);
			} catch (Exception e) {
				throw new InvalidSelectionException("Unexpected exception", e);
			}

			// TODO compute closure and covering tree
			objectsToBeCopied = returned;

			System.out.println("DONE prepare copy");

		}

	};

	AbstractCopyAction(AbstractCopyActionType<A> actionType, FlexoObject focusedObject, Vector<FlexoObject> globalSelection,
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
