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

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPointObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModel.VirtualModelImpl;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.task.Progress;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;

public class CreateVirtualModel extends AbstractCreateVirtualModel<CreateVirtualModel, ViewPoint, ViewPointObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateVirtualModel.class.getPackage().getName());

	public static FlexoActionType<CreateVirtualModel, ViewPoint, ViewPointObject> actionType = new FlexoActionType<CreateVirtualModel, ViewPoint, ViewPointObject>(
			"create_virtual_model", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateVirtualModel makeNewAction(ViewPoint focusedObject, Vector<ViewPointObject> globalSelection, FlexoEditor editor) {
			return new CreateVirtualModel(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(ViewPoint object, Vector<ViewPointObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(ViewPoint object, Vector<ViewPointObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateVirtualModel.actionType, ViewPoint.class);
	}

	private String newVirtualModelName;
	private String newVirtualModelDescription;
	private VirtualModel newVirtualModel;

	// public Vector<IFlexoOntology> importedOntologies = new Vector<IFlexoOntology>();

	// private boolean createsOntology = false;

	CreateVirtualModel(ViewPoint focusedObject, Vector<ViewPointObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws IOFlexoException, SaveResourceException {

		Progress.progress(FlexoLocalization.localizedForKey("create_virtual_model"));

		newVirtualModel = VirtualModelImpl.newVirtualModel(newVirtualModelName, getFocusedObject());
		newVirtualModel.setDescription(newVirtualModelDescription);

		Progress.progress(FlexoLocalization.localizedForKey("create_model_slots"));
		performCreateModelSlots();

		newVirtualModel.getPropertyChangeSupport().firePropertyChange("name", null, newVirtualModel.getName());
		newVirtualModel.getResource().getPropertyChangeSupport().firePropertyChange("name", null, newVirtualModel.getName());
	}

	public boolean isNewVirtualModelNameValid() {
		if (StringUtils.isEmpty(newVirtualModelName)) {
			return false;
		}
		if (getFocusedObject().getVirtualModelNamed(newVirtualModelName) != null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid() {
		if (!isNewVirtualModelNameValid()) {
			return false;
		}
		return true;
	}

	@Override
	public VirtualModel getNewVirtualModel() {
		return newVirtualModel;
	}

	public String getNewVirtualModelName() {
		return newVirtualModelName;
	}

	public void setNewVirtualModelName(String newVirtualModelName) {
		this.newVirtualModelName = newVirtualModelName;
		getPropertyChangeSupport().firePropertyChange("newVirtualModelName", null, newVirtualModelName);

	}

	public String getNewVirtualModelDescription() {
		return newVirtualModelDescription;
	}

	public void setNewVirtualModelDescription(String newVirtualModelDescription) {
		this.newVirtualModelDescription = newVirtualModelDescription;
		getPropertyChangeSupport().firePropertyChange("newVirtualModelDescription", null, newVirtualModelDescription);
	}

	@Override
	public int getExpectedProgressSteps() {
		return 15;
	}

}
