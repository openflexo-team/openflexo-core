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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.inspector.CheckboxInspectorEntry;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.inspector.FloatInspectorEntry;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.inspector.IntegerInspectorEntry;
import org.openflexo.foundation.fml.inspector.TextAreaInspectorEntry;
import org.openflexo.foundation.fml.inspector.TextFieldInspectorEntry;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.toolbox.StringUtils;

public class CreateInspectorEntry extends FlexoAction<CreateInspectorEntry, FlexoConceptInspector, FMLObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateInspectorEntry.class.getPackage().getName());

	public static FlexoActionType<CreateInspectorEntry, FlexoConceptInspector, FMLObject> actionType = new FlexoActionType<CreateInspectorEntry, FlexoConceptInspector, FMLObject>(
			"create_inspector_entry", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateInspectorEntry makeNewAction(FlexoConceptInspector focusedObject, Vector<FMLObject> globalSelection,
				FlexoEditor editor) {
			return new CreateInspectorEntry(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptInspector object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptInspector object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateInspectorEntry.actionType, FlexoConceptInspector.class);
	}

	private String entryName;
	private String description;
	private Class<? extends InspectorEntry> inspectorEntryClass;

	private InspectorEntry newEntry;

	CreateInspectorEntry(FlexoConceptInspector focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);

	}

	public String getEntryName() {
		if (StringUtils.isEmpty(entryName) && inspectorEntryClass != null) {
			return getFocusedObject().getAvailableEntryName(inspectorEntryClass.getSimpleName().toLowerCase());
		}
		return entryName;
	}

	public void setEntryName(String entryName) {
		if ((entryName == null && this.entryName != null) || (entryName != null && !entryName.equals(this.entryName))) {
			String oldValue = this.entryName;
			this.entryName = entryName;
			getPropertyChangeSupport().firePropertyChange("entryName", oldValue, entryName);
		}
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		// logger.info("Add InspectorEntry, name=" + getEntryName() + " type=" + inspectorEntryClass);

		if (inspectorEntryClass != null) {
			FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
			newEntry = factory.newInstance(inspectorEntryClass);
			newEntry.setName(getEntryName());
			getFocusedObject().addToEntries(newEntry);
		}

	}

	public InspectorEntry getNewEntry() {
		return newEntry;
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(getEntryName())) {
			return false;
		} else if (getFocusedObject().getEntry(getEntryName()) != null) {
			return false;
		}
		return true;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if ((description == null && this.description != null) || (description != null && !description.equals(this.description))) {
			String oldValue = this.description;
			this.description = description;
			getPropertyChangeSupport().firePropertyChange("description", oldValue, description);
		}
	}

	public Class<? extends InspectorEntry> getInspectorEntryClass() {
		return inspectorEntryClass;
	}

	public void setInspectorEntryClass(Class<? extends InspectorEntry> inspectorEntryClass) {
		if (inspectorEntryClass != this.inspectorEntryClass) {
			Class<? extends InspectorEntry> oldValue = this.inspectorEntryClass;
			this.inspectorEntryClass = inspectorEntryClass;
			getPropertyChangeSupport().firePropertyChange("inspectorEntryClass", oldValue, inspectorEntryClass);
		}
	}

	private List<Class<? extends InspectorEntry>> availableInspectorEntryTypes;

	public List<Class<? extends InspectorEntry>> getAvailableInspectorEntryTypes() {
		if (availableInspectorEntryTypes == null) {
			availableInspectorEntryTypes = computeAvailableInspectorEntryTypes();
		}
		return availableInspectorEntryTypes;
	}

	private List<Class<? extends InspectorEntry>> computeAvailableInspectorEntryTypes() {
		availableInspectorEntryTypes = new ArrayList<Class<? extends InspectorEntry>>();
		availableInspectorEntryTypes.add(TextFieldInspectorEntry.class);
		availableInspectorEntryTypes.add(TextAreaInspectorEntry.class);
		availableInspectorEntryTypes.add(CheckboxInspectorEntry.class);
		availableInspectorEntryTypes.add(FloatInspectorEntry.class);
		availableInspectorEntryTypes.add(IntegerInspectorEntry.class);
		for (ModelSlot<?> ms : getFocusedObject().getOwningVirtualModel().getModelSlots()) {
			for (Class<? extends InspectorEntry> entryType : ms.getAvailableInspectorEntryTypes()) {
				if (!availableInspectorEntryTypes.contains(entryType)) {
					availableInspectorEntryTypes.add(entryType);
				}
			}
		}
		return availableInspectorEntryTypes;
	}

}
