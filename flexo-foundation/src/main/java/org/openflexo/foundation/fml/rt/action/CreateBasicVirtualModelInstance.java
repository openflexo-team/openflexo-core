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

package org.openflexo.foundation.fml.rt.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.resource.RepositoryFolder;

/**
 * This action is called to create a regular {@link FMLRTVirtualModelInstance} either as top level in a repository folder, or as a contained
 * {@link FMLRTVirtualModelInstance} in a container {@link FMLRTVirtualModelInstance}
 * 
 * @author sylvain
 *
 * @param <T>
 *            type of container (a repository folder or a container FMLRTVirtualModelInstance)
 */
public class CreateBasicVirtualModelInstance extends CreateFMLRTVirtualModelInstance<CreateBasicVirtualModelInstance> {

	private static final Logger logger = Logger.getLogger(CreateBasicVirtualModelInstance.class.getPackage().getName());

	public static FlexoActionType<CreateBasicVirtualModelInstance, FlexoObject, FlexoObject> actionType = new FlexoActionType<CreateBasicVirtualModelInstance, FlexoObject, FlexoObject>(
			"instantiate_virtual_model", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateBasicVirtualModelInstance makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new CreateBasicVirtualModelInstance(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject container, Vector<FlexoObject> globalSelection) {
			if (container instanceof VirtualModelInstance || container instanceof RepositoryFolder) {
				return true;
			}
			return false;
		}

		@Override
		public boolean isEnabledForSelection(FlexoObject container, Vector<FlexoObject> globalSelection) {
			return isVisibleForSelection(container, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateBasicVirtualModelInstance.actionType, RepositoryFolder.class);
		FlexoObjectImpl.addActionForClass(CreateBasicVirtualModelInstance.actionType, VirtualModelInstance.class);
	}

	protected CreateBasicVirtualModelInstance(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

}
