/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.foundation.action;

import java.util.Vector;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoProperty;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.model.factory.ModelFactory;

public class AddFlexoProperty extends FlexoAction<AddFlexoProperty, FlexoObject, FlexoObject> {

	public static final FlexoActionType<AddFlexoProperty, FlexoObject, FlexoObject> actionType = new FlexoActionType<AddFlexoProperty, FlexoObject, FlexoObject>(
			"add_flexo_property") {

		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return false;
		}

		@Override
		public AddFlexoProperty makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new AddFlexoProperty(focusedObject, globalSelection, editor);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(actionType, FlexoObject.class);
	}

	private String name;
	private String value;

	private boolean insertSorted = false;

	private FlexoProperty createdProperty;

	public AddFlexoProperty(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws FlexoException {
		if (getFocusedObject() != null) {

			if (getFocusedObject() instanceof InnerResourceData
					&& ((InnerResourceData<?>) getFocusedObject()).getResourceData().getResource() instanceof PamelaResource) {

				ModelFactory factory = ((PamelaResource<?, ?>) ((InnerResourceData<?>) getFocusedObject()).getResourceData().getResource())
						.getFactory();

				createdProperty = factory.newInstance(FlexoProperty.class);
				createdProperty.setOwner(getFocusedObject());

				if (getName() != null) {
					createdProperty.setName(getName());
				}
				else {
					createdProperty.setName(getNextPropertyName(getFocusedObject()));
				}
				if (getValue() != null) {
					createdProperty.setValue(getValue());
				}
				getFocusedObject().addToCustomProperties(createdProperty);
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public FlexoProperty getCreatedProperty() {
		return createdProperty;
	}

	public void setCreatedProperty(FlexoProperty createdProperty) {
		this.createdProperty = createdProperty;
	}

	public boolean isInsertSorted() {
		return insertSorted;
	}

	public void setInsertSorted(boolean insertSorted) {
		this.insertSorted = insertSorted;
	}

	public String getNextPropertyName(FlexoObject owner) {
		String base = getLocales().localizedForKey("property");
		String attempt = base;
		int i = 1;
		while (owner.getPropertyNamed(attempt) != null) {
			attempt = base + "-" + i++;
		}
		return attempt;
	}

}
