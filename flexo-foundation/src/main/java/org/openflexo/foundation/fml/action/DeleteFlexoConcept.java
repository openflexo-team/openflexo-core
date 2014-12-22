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
package org.openflexo.foundation.fml.action;

import java.security.InvalidParameterException;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.ViewPointObject;
import org.openflexo.foundation.fml.VirtualModel;

public class DeleteFlexoConcept extends FlexoAction<DeleteFlexoConcept, FlexoConcept, ViewPointObject> {

	private static final Logger logger = Logger.getLogger(DeleteFlexoConcept.class.getPackage().getName());

	public static FlexoActionType<DeleteFlexoConcept, FlexoConcept, ViewPointObject> actionType = new FlexoActionType<DeleteFlexoConcept, FlexoConcept, ViewPointObject>(
			"delete_flexo_concept", FlexoActionType.editGroup, FlexoActionType.DELETE_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DeleteFlexoConcept makeNewAction(FlexoConcept focusedObject, Vector<ViewPointObject> globalSelection, FlexoEditor editor) {
			return new DeleteFlexoConcept(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConcept object, Vector<ViewPointObject> globalSelection) {
			return object != null && object instanceof FlexoConcept && !(object instanceof VirtualModel);
		}

		@Override
		public boolean isEnabledForSelection(FlexoConcept object, Vector<ViewPointObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(DeleteFlexoConcept.actionType, FlexoConcept.class);
	}

	DeleteFlexoConcept(FlexoConcept focusedObject, Vector<ViewPointObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		logger.info("Delete flexo concept");

		getFocusedObject().getVirtualModel().removeFromFlexoConcepts(getFocusedObject());
		getFocusedObject().delete();
	}

}