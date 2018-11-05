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

import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.WidgetType;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.toolbox.StringUtils;

public class CreateInspectorEntry extends FlexoAction<CreateInspectorEntry, FlexoConceptInspector, FMLObject> implements Bindable {

	private static final Logger logger = Logger.getLogger(CreateInspectorEntry.class.getPackage().getName());

	public static FlexoActionFactory<CreateInspectorEntry, FlexoConceptInspector, FMLObject> actionType = new FlexoActionFactory<CreateInspectorEntry, FlexoConceptInspector, FMLObject>(
			"create_inspector_entry", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

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

	private Type entryType;
	private WidgetType widgetType;

	private DataBinding<?> data;
	private DataBinding<?> container;
	private DataBinding<List<?>> list;

	private String description;
	// private Class<? extends InspectorEntry> inspectorEntryClass;

	private int index = -1;

	private InspectorEntry newEntry;

	private CreateInspectorEntry(FlexoConceptInspector focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);

	}

	public String getEntryName() {
		if (StringUtils.isEmpty(entryName)) {
			return getFocusedObject().getAvailableEntryName(getDefaultEntryName());
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

	public String getDefaultEntryName() {
		if (getEntryType() != null) {
			Class<?> baseClass = TypeUtils.getBaseClass(getEntryType());
			return "a" + baseClass.getSimpleName();
		}
		return "entry";
	}

	public Type getEntryType() {
		if (getData().isValid()) {
			return getData().getAnalyzedType();
		}
		return entryType;
	}

	public void setEntryType(Type entryType) {
		if ((entryType == null && getEntryType() != null) || (entryType != null && !entryType.equals(getEntryType()))) {
			Type oldValue = this.entryType;
			this.entryType = entryType;
			getPropertyChangeSupport().firePropertyChange("entryType", oldValue, entryType);
		}
	}

	public WidgetType getWidgetType() {
		return widgetType;
	}

	public void setWidgetType(WidgetType widgetType) {
		if ((widgetType == null && this.widgetType != null) || (widgetType != null && !widgetType.equals(this.widgetType))) {
			WidgetType oldValue = this.widgetType;
			this.widgetType = widgetType;
			getPropertyChangeSupport().firePropertyChange("widgetType", oldValue, widgetType);
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		if (index != this.index) {
			int oldValue = this.index;
			this.index = index;
			getPropertyChangeSupport().firePropertyChange("index", oldValue, index);
		}
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		logger.info("Add InspectorEntry, name=" + getEntryName() + " type1=" + entryType + " analyzed_type="
				+ (getData().isValid() ? getData().getAnalyzedType() : "???") + " widget=" + getWidgetType());

		if (getFocusedObject() != null) {
			FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
			newEntry = factory.newInspectorEntry();
			newEntry.setName(getEntryName());
			newEntry.setType(getEntryType());
			newEntry.setWidget(getWidgetType());
			newEntry.setContainer(getContainer());
			newEntry.setData(getData());
			newEntry.setList(getList());
			newEntry.setIsReadOnly(getIsReadOnly());
			newEntry.setDescription(getDescription());
			getFocusedObject().addToEntries(newEntry);
			if (getIndex() > -1) {
				getFocusedObject().moveInspectorEntryToIndex(newEntry, getIndex());
			}
		}
		else {
			logger.warning("Cannot create inspector entry for null inspector");
		}

	}

	public InspectorEntry getNewEntry() {
		return newEntry;
	}

	private boolean readOnly;

	public boolean getIsReadOnly() {
		return readOnly;
	}

	public void setIsReadOnly(boolean readOnly) {
		if (readOnly != this.readOnly) {
			this.readOnly = readOnly;
			getPropertyChangeSupport().firePropertyChange("readOnly", !readOnly, readOnly);
		}
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(getEntryName())) {
			return false;
		}
		else if (getFocusedObject().getEntry(getEntryName()) != null) {
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

	public DataBinding<?> getContainer() {
		if (container == null) {
			container = new DataBinding<>(this, Object.class, BindingDefinitionType.GET);
			container.setBindingName("container");
		}
		return container;
	}

	public void setContainer(DataBinding<?> container) {
		if (container != null) {
			container.setOwner(this);
			container.setBindingName("container");
			container.setDeclaredType(Object.class);
			container.setBindingDefinitionType(BindingDefinitionType.GET);
		}
		this.container = container;
	}

	public DataBinding<?> getData() {
		if (data == null) {
			data = new DataBinding<>(this, Object.class, BindingDefinitionType.GET);
			data.setBindingName("data");
		}
		return data;
	}

	public void setData(DataBinding<?> data) {
		if (data != null) {
			data.setOwner(this);
			data.setBindingName("data");
			data.setDeclaredType(Object.class);
			data.setBindingDefinitionType(BindingDefinitionType.GET);
		}
		this.data = data;
	}

	public DataBinding<List<?>> getList() {
		if (list == null) {
			list = new DataBinding<>(this, List.class, BindingDefinitionType.GET);
		}
		return list;
	}

	public void setList(DataBinding<List<?>> list) {
		if (list != null) {
			list.setOwner(this);
			list.setBindingName("list");
			list.setDeclaredType(List.class);
			list.setBindingDefinitionType(BindingDefinitionType.GET);
		}
		this.list = list;
	}

	public List<WidgetType> getAvailableWidgetTypes() {
		return Arrays.asList(WidgetType.values());
	}

	@Override
	public BindingModel getBindingModel() {
		if (getFocusedObject() != null) {
			return getFocusedObject().getBindingModel();
		}
		return null;
	}

	@Override
	public BindingFactory getBindingFactory() {
		if (getFocusedObject() != null) {
			return getFocusedObject().getBindingFactory();
		}
		return null;
	}

	@Override
	public void notifiedBindingChanged(DataBinding<?> dataBinding) {
		getPropertyChangeSupport().firePropertyChange(dataBinding.getBindingName(), null, dataBinding);
	}

	@Override
	public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
		// TODO Auto-generated method stub

	}

	/*private List<Class<? extends InspectorEntry>> availableInspectorEntryTypes;
	
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
		if (getFocusedObject().getOwningVirtualModel() != null) {
			for (ModelSlot<?> ms : getFocusedObject().getOwningVirtualModel().getModelSlots()) {
				for (Class<? extends InspectorEntry> entryType : ms.getAvailableInspectorEntryTypes()) {
					if (!availableInspectorEntryTypes.contains(entryType)) {
						availableInspectorEntryTypes.add(entryType);
					}
				}
			}
		}
		return availableInspectorEntryTypes;
	}*/

}
