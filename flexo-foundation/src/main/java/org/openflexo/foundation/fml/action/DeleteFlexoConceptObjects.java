/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.copypaste.AbstractCopyAction.InvalidSelectionException;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.pamela.factory.EmbeddingType;

public class DeleteFlexoConceptObjects extends FlexoAction<DeleteFlexoConceptObjects, FlexoConceptObject, FlexoConceptObject> {

	private static final Logger logger = Logger.getLogger(DeleteFlexoConceptObjects.class.getPackage().getName());

	public static FlexoActionFactory<DeleteFlexoConceptObjects, FlexoConceptObject, FlexoConceptObject> actionType = new FlexoActionFactory<DeleteFlexoConceptObjects, FlexoConceptObject, FlexoConceptObject>(
			"delete", FlexoActionFactory.editGroup, FlexoActionFactory.DELETE_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DeleteFlexoConceptObjects makeNewAction(FlexoConceptObject focusedObject, Vector<FlexoConceptObject> globalSelection,
				FlexoEditor editor) {
			return new DeleteFlexoConceptObjects(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptObject focusedObject, Vector<FlexoConceptObject> globalSelection) {
			return isEnabledForSelection(focusedObject, globalSelection);
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptObject focusedObject, Vector<FlexoConceptObject> globalSelection) {
			try {
				List<FlexoConceptObject> objectsToDelete = getObjectsToDelete(getGlobalSelection(focusedObject, globalSelection));
				return objectsToDelete.size() > 0;
			} catch (InvalidSelectionException e) {
				return false;
			}
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(DeleteFlexoConceptObjects.actionType, FlexoConceptObject.class);
	}

	private DeleteFlexoConceptObjects(FlexoConceptObject focusedObject, Vector<FlexoConceptObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws InvalidSelectionException {
		logger.info("Delete objects");

		List<FlexoConceptObject> objectsToDelete = getObjectsToDelete();

		for (FlexoConceptObject object : objectsToDelete) {
			object.delete(objectsToDelete);
		}
	}

	public List<FlexoConceptObject> getObjectsToDelete() throws InvalidSelectionException {
		return getObjectsToDelete(getGlobalSelection(getFocusedObject(), getGlobalSelection()));
	}

	protected static List<FlexoConceptObject> getObjectsToDelete(List<FlexoConceptObject> globalSelection)
			throws InvalidSelectionException {

		List<FlexoConceptObject> allObjects = new ArrayList<>();

		VirtualModelResource resource = null;
		FMLModelFactory modelFactory = null;

		for (FlexoConceptObject o : globalSelection) {
			if (o.getResourceData() != null) {
				if (resource == null) {
					resource = (VirtualModelResource) o.getResourceData().getResource();
					modelFactory = resource.getFactory();
				}
				else {
					if ((VirtualModelResource) o.getResourceData().getResource() != resource) {
						throw new InvalidSelectionException("Multiple virtual model impacted");
					}
				}
				allObjects.add(o);
			}
		}

		List<FlexoConceptObject> returned = new ArrayList<>();
		Map<FlexoConceptObject, List<Object>> allDerived = new HashMap<>();

		for (FlexoConceptObject o : allObjects) {
			if (isDeletable(o)) {
				List<Object> embeddedObjects = modelFactory.getEmbeddedObjects(o, EmbeddingType.DELETION,
						(Object[]) allObjects.toArray(new FlexoConceptObject[allObjects.size()]));
				// removes origin object from dependencies
				embeddedObjects.remove(o);
				allDerived.put(o, embeddedObjects);
			}
		}

		for (FlexoConceptObject o : allObjects) {
			boolean isRequired = false;
			if (isDeletable(o)) {
				isRequired = true;
				for (FlexoConceptObject o2 : allObjects) {
					if (allDerived.get(o2) != null && allDerived.get(o2).contains(o)) {
						isRequired = false;
						break;
					}
				}
			}
			if (isRequired) {
				returned.add(o);
			}
		}

		return returned;
	}

	protected static boolean isDeletable(FlexoConceptObject o) {
		// VirtualModel and ViewPoint are deleted using specific actions: DeleteViewpoint and DeleteVirtualModel
		if (o instanceof VirtualModel) {
			return false;
		}
		return true;
	}
}
