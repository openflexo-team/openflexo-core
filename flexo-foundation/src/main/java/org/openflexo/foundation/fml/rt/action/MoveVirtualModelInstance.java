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
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.resource.RepositoryFolder;

public class MoveVirtualModelInstance
		extends FlexoAction<MoveVirtualModelInstance, AbstractVirtualModelInstance<?, ?>, AbstractVirtualModelInstance<?, ?>> {

	private static final Logger logger = Logger.getLogger(MoveVirtualModelInstance.class.getPackage().getName());

	public static final FlexoActionType<MoveVirtualModelInstance, AbstractVirtualModelInstance<?, ?>, AbstractVirtualModelInstance<?, ?>> actionType = new FlexoActionType<MoveVirtualModelInstance, AbstractVirtualModelInstance<?, ?>, AbstractVirtualModelInstance<?, ?>>(
			"move_virtual_model_instance") {

		@Override
		public boolean isEnabledForSelection(AbstractVirtualModelInstance<?, ?> object,
				Vector<AbstractVirtualModelInstance<?, ?>> globalSelection) {
			return true;
		}

		@Override
		public boolean isVisibleForSelection(AbstractVirtualModelInstance<?, ?> object,
				Vector<AbstractVirtualModelInstance<?, ?>> globalSelection) {
			return false;
		}

		@Override
		public MoveVirtualModelInstance makeNewAction(AbstractVirtualModelInstance<?, ?> focusedObject,
				Vector<AbstractVirtualModelInstance<?, ?>> globalSelection, FlexoEditor editor) {
			return new MoveVirtualModelInstance(focusedObject, globalSelection, editor);
		}

	};

	private RepositoryFolder<AbstractVirtualModelInstanceResource<?, ?>, ?> folder;

	static {
		FlexoObjectImpl.addActionForClass(actionType, AbstractVirtualModelInstance.class);
	}

	protected MoveVirtualModelInstance(AbstractVirtualModelInstance<?, ?> focusedObject,
			Vector<AbstractVirtualModelInstance<?, ?>> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws FlexoException {
		if (getFolder() == null) {
			logger.warning("Cannot move: null folder");
			return;
		}
		for (AbstractVirtualModelInstance<?, ?> v : getGlobalSelection()) {
			moveToFolder(v, folder);
		}
	}

	private void moveToFolder(AbstractVirtualModelInstance<?, ?> v,
			RepositoryFolder<AbstractVirtualModelInstanceResource<?, ?>, ?> folder) {
		// TODO: reimplement this
		// RepositoryFolder<AbstractVirtualModelInstanceResource<?,?>, ?> oldFolder = v.getFolder();
		// v.getViewLibrary().moveResource((ViewResource) v.getResource(), (RepositoryFolder) oldFolder, (RepositoryFolder) folder);
	}

	public RepositoryFolder<AbstractVirtualModelInstanceResource<?, ?>, ?> getFolder() {
		return folder;
	}

	public void setFolder(RepositoryFolder<AbstractVirtualModelInstanceResource<?, ?>, ?> folder) {
		this.folder = folder;
	}
}
