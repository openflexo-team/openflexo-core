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
package org.openflexo.foundation.viewpoint.action;

import java.security.InvalidParameterException;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.ViewPointObject;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.VirtualModelModelFactory;
import org.openflexo.toolbox.StringUtils;

public class CreateFlexoConcept extends AbstractCreateFlexoConcept<CreateFlexoConcept, VirtualModel, ViewPointObject> {

	private static final Logger logger = Logger.getLogger(CreateFlexoConcept.class.getPackage().getName());

	public static FlexoActionType<CreateFlexoConcept, VirtualModel, ViewPointObject> actionType = new FlexoActionType<CreateFlexoConcept, VirtualModel, ViewPointObject>(
			"add_new_flexo_concept", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateFlexoConcept makeNewAction(VirtualModel focusedObject, Vector<ViewPointObject> globalSelection, FlexoEditor editor) {
			return new CreateFlexoConcept(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(VirtualModel object, Vector<ViewPointObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(VirtualModel object, Vector<ViewPointObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateFlexoConcept.actionType, VirtualModel.class);
	}

	private String newFlexoConceptName;
	private String newFlexoConceptDescription;
	private FlexoConcept newFlexoConcept;

	public boolean switchNewlyCreatedFlexoConcept = true;

	CreateFlexoConcept(VirtualModel focusedObject, Vector<ViewPointObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {

		VirtualModelModelFactory factory = getFocusedObject().getVirtualModelFactory();

		newFlexoConcept = factory.newFlexoConcept();
		newFlexoConcept.setName(getNewFlexoConceptName());

		performSetParentConcepts();

		getFocusedObject().addToFlexoConcepts(newFlexoConcept);
	}

	@Override
	public FlexoConcept getNewFlexoConcept() {
		return newFlexoConcept;
	}

	public String getNewFlexoConceptName() {
		return newFlexoConceptName;
	}

	public void setNewFlexoConceptName(String newFlexoConceptName) {
		if ((newFlexoConceptName == null && this.newFlexoConceptName != null)
				|| (newFlexoConceptName != null && !newFlexoConceptName.equals(this.newFlexoConceptName))) {
			String oldValue = this.newFlexoConceptName;
			this.newFlexoConceptName = newFlexoConceptName;
			getPropertyChangeSupport().firePropertyChange("newFlexoConceptName", oldValue, newFlexoConceptName);
		}
	}

	public String getNewFlexoConceptDescription() {
		return newFlexoConceptDescription;
	}

	public void setNewFlexoConceptDescription(String newFlexoConceptDescription) {
		if ((newFlexoConceptDescription == null && this.newFlexoConceptDescription != null)
				|| (newFlexoConceptDescription != null && !newFlexoConceptDescription.equals(this.newFlexoConceptDescription))) {
			String oldValue = this.newFlexoConceptDescription;
			this.newFlexoConceptDescription = newFlexoConceptDescription;
			getPropertyChangeSupport().firePropertyChange("newFlexoConceptDescription", oldValue, newFlexoConceptDescription);
		}
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(newFlexoConceptName)) {
			return false;
		} else if (getFocusedObject() instanceof VirtualModel && getFocusedObject().getFlexoConcept(newFlexoConceptName) != null) {
			return false;
		}
		return true;
	}

	@Override
	public int getExpectedProgressSteps() {
		return 10;
	}

}
