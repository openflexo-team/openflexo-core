/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2013 Openflexo
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

package org.openflexo.foundation.view.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.view.View;
import org.openflexo.foundation.view.VirtualModelInstance;

/**
 * This action is called to create a regular {@link VirtualModelInstance} in a {@link View}
 * 
 * @author sylvain
 * 
 */
public class CreateBasicVirtualModelInstance extends CreateVirtualModelInstance<CreateBasicVirtualModelInstance> {

	private static final Logger logger = Logger.getLogger(CreateBasicVirtualModelInstance.class.getPackage().getName());

	public static FlexoActionType<CreateBasicVirtualModelInstance, View, FlexoObject> actionType = new FlexoActionType<CreateBasicVirtualModelInstance, View, FlexoObject>(
			"instantiate_virtual_model", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateBasicVirtualModelInstance makeNewAction(View focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new CreateBasicVirtualModelInstance(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(View object, Vector<FlexoObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(View object, Vector<FlexoObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateBasicVirtualModelInstance.actionType, View.class);
	}

	protected CreateBasicVirtualModelInstance(View focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}
}
