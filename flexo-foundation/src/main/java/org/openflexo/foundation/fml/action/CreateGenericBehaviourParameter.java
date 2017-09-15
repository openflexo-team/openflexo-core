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
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourObject;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.WidgetType;
import org.openflexo.toolbox.StringUtils;

public class CreateGenericBehaviourParameter extends FlexoAction<CreateGenericBehaviourParameter, FlexoBehaviourObject, FMLObject>
		implements Bindable {

	private static final Logger logger = Logger.getLogger(CreateGenericBehaviourParameter.class.getPackage().getName());

	public static FlexoActionFactory<CreateGenericBehaviourParameter, FlexoBehaviourObject, FMLObject> actionType = new FlexoActionFactory<CreateGenericBehaviourParameter, FlexoBehaviourObject, FMLObject>(
			"create_behaviour_parameter", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateGenericBehaviourParameter makeNewAction(FlexoBehaviourObject focusedObject, Vector<FMLObject> globalSelection,
				FlexoEditor editor) {
			return new CreateGenericBehaviourParameter(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoBehaviourObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoBehaviourObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateGenericBehaviourParameter.actionType, FlexoBehaviour.class);
	}

	private String parameterName;
	private Type parameterType;
	private WidgetType widgetType;

	private DataBinding<?> defaultValue;
	private DataBinding<?> container;
	private DataBinding<List<?>> list;

	private String description;

	private FlexoBehaviourParameter newParameter;

	CreateGenericBehaviourParameter(FlexoBehaviourObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);

	}

	public FlexoBehaviour getFlexoBehaviour() {
		if (getFocusedObject() != null) {
			return getFocusedObject().getFlexoBehaviour();
		}
		return null;
	}

	public String getParameterName() {
		if (StringUtils.isEmpty(parameterName)) {
			return getFlexoBehaviour().getAvailableParameterName(getDefaultParameterName());
		}
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		if ((parameterName == null && this.parameterName != null) || (parameterName != null && !parameterName.equals(this.parameterName))) {
			String oldValue = this.parameterName;
			this.parameterName = parameterName;
			getPropertyChangeSupport().firePropertyChange("parameterName", oldValue, parameterName);
		}
	}

	public String getDefaultParameterName() {
		if (getParameterType() != null) {
			Class<?> baseClass = TypeUtils.getBaseClass(getParameterType());
			return "a" + baseClass.getSimpleName();
		}
		return "aParam";
	}

	public Type getParameterType() {
		return parameterType;
	}

	public void setParameterType(Type parameterType) {
		if ((parameterType == null && this.parameterType != null) || (parameterType != null && !parameterType.equals(this.parameterType))) {
			Type oldValue = this.parameterType;
			this.parameterType = parameterType;
			getPropertyChangeSupport().firePropertyChange("parameterType", oldValue, parameterType);
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

	private boolean isRequired;

	public boolean getIsRequired() {
		return isRequired;
	}

	public void setIsRequired(boolean isRequired) {
		if (isRequired != this.isRequired) {
			this.isRequired = isRequired;
			getPropertyChangeSupport().firePropertyChange("isRequired", !isRequired, isRequired);
		}
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		logger.info("Add FlexoBehaviourParameter, name=" + getParameterName() + " type=" + getParameterType());

		FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
		newParameter = factory.newParameter(getFlexoBehaviour());
		newParameter.setName(getParameterName());
		newParameter.setType(getParameterType());
		newParameter.setContainer(getContainer());
		newParameter.setDefaultValue(getDefaultValue());
		newParameter.setList(getList());
		newParameter.setIsRequired(getIsRequired());
		newParameter.setDescription(getDescription());
	}

	public FlexoBehaviourParameter getNewParameter() {
		return newParameter;
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(getParameterName())) {
			return false;
		}
		else if (getFlexoBehaviour().getParameter(getParameterName()) != null) {
			return false;
		}
		if (getParameterType() == null) {
			return false;
		}
		if (getWidgetType() == null) {
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

	public DataBinding<?> getDefaultValue() {
		if (defaultValue == null) {
			defaultValue = new DataBinding<>(this, Object.class, BindingDefinitionType.GET);
			defaultValue.setBindingName("defaultValue");
		}
		return defaultValue;
	}

	public void setDefaultValue(DataBinding<?> defaultValue) {
		if (defaultValue != null) {
			defaultValue.setOwner(this);
			defaultValue.setBindingName("defaultValue");
			defaultValue.setDeclaredType(Object.class);
			defaultValue.setBindingDefinitionType(BindingDefinitionType.GET);
		}
		this.defaultValue = defaultValue;
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
		if (getFlexoBehaviour() != null) {
			return getFlexoBehaviour().getBindingModel();
		}
		return null;
	}

	@Override
	public BindingFactory getBindingFactory() {
		if (getFlexoBehaviour() != null) {
			return getFlexoBehaviour().getBindingFactory();
		}
		return null;
	}

	@Override
	public void notifiedBindingChanged(DataBinding<?> dataBinding) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
		// TODO Auto-generated method stub

	}

}
