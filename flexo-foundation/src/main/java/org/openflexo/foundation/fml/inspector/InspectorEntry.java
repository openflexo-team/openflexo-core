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

package org.openflexo.foundation.fml.inspector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.FlexoBehaviourParameterImpl;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.WidgetType;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoEnumType;
import org.openflexo.foundation.fml.WidgetContext;
import org.openflexo.foundation.fml.binding.InspectorEntryBindingModel;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents an inspector entry (a data related to an flexo concept which can be inspected)
 * 
 * @author sylvain
 * 
 */
@ModelEntity()
@ImplementationClass(InspectorEntry.InspectorEntryImpl.class)
@XMLElement(xmlTag = "InspectorEntry", deprecatedXMLTags = "GenericInspectorEntry")
public interface InspectorEntry extends FlexoConceptObject, WidgetContext {

	@PropertyIdentifier(type = FlexoConceptInspector.class)
	public static final String INSPECTOR_KEY = "inspector";

	@PropertyIdentifier(type = String.class)
	public static final String LABEL_KEY = "label";
	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = boolean.class)
	public static final String IS_READ_ONLY_KEY = "isReadOnly";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String DATA_KEY = "data";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String LIST_KEY = "list";
	@PropertyIdentifier(type = Type.class)
	public static final String TYPE_KEY = "type";
	@PropertyIdentifier(type = WidgetType.class)
	public static final String WIDGET_KEY = "widget";

	@Getter(value = INSPECTOR_KEY/*, inverse = FlexoConceptInspector.ENTRIES_KEY*/)
	// @XMLElement(xmlTag = "Inspector")
	public FlexoConceptInspector getInspector();

	@Setter(INSPECTOR_KEY)
	public void setInspector(FlexoConceptInspector inspector);

	@Getter(value = LABEL_KEY)
	@XMLAttribute
	public String getLabel();

	@Setter(LABEL_KEY)
	public void setLabel(String label);

	@Override
	@Getter(value = NAME_KEY)
	@XMLAttribute
	public String getName();

	@Override
	@Setter(NAME_KEY)
	public void setName(String name) throws InvalidNameException;

	@Getter(value = IS_READ_ONLY_KEY, defaultValue = "false")
	@XMLAttribute(xmlTag = "readOnly")
	public boolean getIsReadOnly();

	@Setter(IS_READ_ONLY_KEY)
	public void setIsReadOnly(boolean isReadOnly);

	@Getter(value = DATA_KEY)
	@XMLAttribute
	public DataBinding<?> getData();

	@Setter(DATA_KEY)
	public void setData(DataBinding<?> data);

	/*@Getter(value = CONDITIONAL_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getConditional();
	
	@Setter(CONDITIONAL_KEY)
	public void setConditional(DataBinding<Boolean> conditional);*/

	@Override
	@Getter(value = TYPE_KEY, isStringConvertable = true)
	@XMLAttribute
	public abstract Type getType();

	@Setter(TYPE_KEY)
	public void setType(Type aType);

	@Override
	@Getter(value = WIDGET_KEY)
	@XMLAttribute
	public WidgetType getWidget();

	@Setter(WIDGET_KEY)
	public void setWidget(WidgetType widget);

	@Override
	@Getter(value = CONTAINER_KEY)
	@XMLAttribute
	public DataBinding<?> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<?> container);

	public Object getContainer(BindingEvaluationContext evaluationContext);

	@Getter(value = LIST_KEY)
	@XMLAttribute
	public DataBinding<List<?>> getList();

	@Setter(LIST_KEY)
	public void setList(DataBinding<List<?>> list);

	public Object getList(BindingEvaluationContext evaluationContext);

	public int getIndex();

	@Override
	public InspectorEntryBindingModel getBindingModel();

	public boolean isListType();

	public List<WidgetType> getAvailableWidgetTypes();

	public static abstract class InspectorEntryImpl extends FlexoConceptObjectImpl implements InspectorEntry {

		static final Logger logger = Logger.getLogger(InspectorEntry.class.getPackage().getName());

		// private FlexoConceptInspector inspector;
		// private String label;
		// private boolean readOnly;

		private DataBinding<?> data;

		private DataBinding<?> container;
		private DataBinding<List<?>> list;

		private InspectorEntryBindingModel bindingModel;

		public InspectorEntryImpl() {
			super();
		}

		@Override
		public void setType(Type aType) {
			performSuperSetter(TYPE_KEY, aType);
			listType = null;
			if (list != null) {
				list.setDeclaredType(getListType());
			}
			if (data != null) {
				data.setDeclaredType(aType);
			}
			getPropertyChangeSupport().firePropertyChange("availableWidgetTypes", null, getAvailableWidgetTypes());
			getPropertyChangeSupport().firePropertyChange("isListType", !isListType(), isListType());
			if (!getAvailableWidgetTypes().contains(getWidget()) && getAvailableWidgetTypes().size() > 0) {
				setWidget(getAvailableWidgetTypes().get(0));
			}
		}

		@Override
		public boolean isListType() {
			return TypeUtils.isList(getType());
		}

		@Override
		public List<WidgetType> getAvailableWidgetTypes() {
			return FlexoBehaviourParameterImpl.getAvailableWidgetTypes(getType());
		}

		// public abstract Class<?> getDefaultDataClass();

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getInspector() != null) {
				return getInspector().getFlexoConcept();
			}
			return null;
		}

		@Override
		public String getLabel() {
			String label = (String) performSuperGetter(LABEL_KEY);
			if (label == null || StringUtils.isEmpty(label)) {
				return getName();
			}
			return label;
		}

		@Override
		public void setLabel(String label) {
			if (label != null && label.equals(getName())) {
				return;
			}
			performSuperSetter(LABEL_KEY, label);
		}

		@Override
		public WidgetType getWidget() {
			WidgetType returned = (WidgetType) performSuperGetter(WIDGET_KEY);
			if (returned == null && getType() != null) {
				return getDefaultWidget();
			}
			return returned;
		}

		private WidgetType getDefaultWidget() {
			if (getType() != null) {
				return FlexoBehaviourParameterImpl.getAvailableWidgetTypes(getType()).get(0);
			}
			return WidgetType.TEXT_FIELD;
		}

		public boolean isSingleEntry() {
			return true;
		}

		@Override
		public int getIndex() {
			if (getInspector() == null) {
				return -1;
			}
			return getInspector().getEntries().indexOf(this);
		}

		/*@Override
		public boolean getIsReadOnly() {
			return readOnly;
		}*/

		@Override
		public void setIsReadOnly(boolean readOnly) {
			performSuperSetter(IS_READ_ONLY_KEY, readOnly);
			if (data != null) {
				data.setBindingDefinitionType(readOnly ? DataBinding.BindingDefinitionType.GET : DataBinding.BindingDefinitionType.GET_SET);
				notifiedBindingChanged(this.data);
			}
		}

		@Override
		public DataBinding<?> getData() {
			if (data == null) {
				data = new DataBinding<>(this, getType(),
						(getIsReadOnly() ? DataBinding.BindingDefinitionType.GET : DataBinding.BindingDefinitionType.GET_SET));
				data.setBindingName("data");
			}
			return data;
		}

		@Override
		public void setData(DataBinding<?> data) {
			if (data != null) {
				data.setOwner(this);
				data.setDeclaredType(getType());
				data.setBindingDefinitionType(
						getIsReadOnly() ? DataBinding.BindingDefinitionType.GET : DataBinding.BindingDefinitionType.GET_SET);
				data.setBindingName("data");
			}
			this.data = data;
			notifiedBindingChanged(this.data);
		}

		/*@Override
		public DataBinding<Boolean> getConditional() {
			if (conditional == null) {
				conditional = new DataBinding<Boolean>(this, Boolean.class, DataBinding.BindingDefinitionType.GET);
				conditional.setBindingName("conditional");
			}
			return conditional;
		}
		
		@Override
		public void setConditional(DataBinding<Boolean> conditional) {
			if (conditional != null) {
				conditional.setOwner(this);
				conditional.setDeclaredType(Boolean.class);
				conditional.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				conditional.setBindingName("conditional");
			}
			this.conditional = conditional;
		}*/

		@Override
		public DataBinding<?> getContainer() {
			if (container == null) {
				container = new DataBinding<>(this, Object.class, BindingDefinitionType.GET);
				container.setBindingName("container");
			}
			return container;
		}

		@Override
		public void setContainer(DataBinding<?> container) {
			if (container != null) {
				container.setOwner(this);
				container.setBindingName("container");
				container.setDeclaredType(Object.class);
				container.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.container = container;
		}

		@Override
		public Object getContainer(BindingEvaluationContext evaluationContext) {
			if (getContainer().isValid()) {
				try {
					return getContainer().getBindingValue(evaluationContext);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		public DataBinding<List<?>> getList() {
			if (list == null) {
				list = new DataBinding<>(this, getListType(), BindingDefinitionType.GET);
			}
			return list;
		}

		@Override
		public void setList(DataBinding<List<?>> list) {
			if (list != null) {
				list.setOwner(this);
				list.setBindingName("list");
				list.setDeclaredType(getListType());
				list.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.list = list;
		}

		private ParameterizedTypeImpl listType = null;

		private Type getListType() {
			if (listType == null) {
				listType = new ParameterizedTypeImpl(List.class, getType());
			}
			return listType;
		}

		@Override
		public Object getList(BindingEvaluationContext evaluationContext) {
			if (getList().isValid()) {
				try {
					return getList().getBindingValue(evaluationContext);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		/*@Override
		public DataBinding<?> getDefaultValue() {
			if (defaultValue == null) {
				defaultValue = new DataBinding<Object>(this, getType(), BindingDefinitionType.GET);
				defaultValue.setBindingName("defaultValue");
			}
			return defaultValue;
		}
		
		@Override
		public void setDefaultValue(DataBinding<?> defaultValue) {
			if (defaultValue != null) {
				defaultValue.setOwner(this);
				defaultValue.setBindingName("defaultValue");
				defaultValue.setDeclaredType(getType());
				defaultValue.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.defaultValue = defaultValue;
		}*/

		/*@Override
		public Object getDefaultValue(BindingEvaluationContext evaluationContext) {
			if (getDefaultValue().isValid()) {
				try {
					return getDefaultValue().getBindingValue(evaluationContext);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			return null;
		}*/

		@Override
		public InspectorEntryBindingModel getBindingModel() {
			if (bindingModel == null) {
				bindingModel = new InspectorEntryBindingModel(this);
			}
			return bindingModel;
		}

		/**
		 * Return a String encoding a {@link DataBinding} which should get access to represented data from the context beeing represented by
		 * this
		 * 
		 * @return
		 */
		@Override
		public String getWidgetDataAccess() {
			return getData().toString();
		}

		/**
		 * Return a String encoding a {@link DataBinding} which should get access to represented data definition (which is this object)
		 * 
		 * @return
		 */
		@Override
		public String getWidgetDefinitionAccess() {
			return "flexoConcept.inspector.getEntry(\"" + getName() + "\")";
		}

		/**
		 * Return a String encoding a {@link DataBinding} which should get access to instance of FlexoConcept
		 * 
		 * @return
		 */
		@Override
		public String getFlexoConceptInstanceAccess() {
			return null;
		}

		/**
		 * Depending of type of data to represent, return a list of objects which may be used to represented data
		 * 
		 * @return
		 */
		@Override
		public List<?> getListOfObjects() {
			if (getType() instanceof FlexoEnumType && ((FlexoEnumType) getType()).getFlexoEnum() != null) {
				return ((FlexoEnumType) getType()).getFlexoEnum().getInstances();
			}
			return null;
		}

	}

	@DefineValidationRule
	public static class DataBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<InspectorEntry> {
		public DataBindingIsRequiredAndMustBeValid() {
			super("'data'_binding_is_not_valid", InspectorEntry.class);
		}

		@Override
		public DataBinding<?> getBinding(InspectorEntry object) {
			return object.getData();
		}

	}

}
