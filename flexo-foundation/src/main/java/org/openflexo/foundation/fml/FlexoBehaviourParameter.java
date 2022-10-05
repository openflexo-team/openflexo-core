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

package org.openflexo.foundation.fml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.binding.Function;
import org.openflexo.connie.binding.Function.FunctionArgument;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.FlexoBehaviour.FlexoBehaviourImpl;
import org.openflexo.foundation.fml.md.ListMetaData;
import org.openflexo.foundation.fml.md.MultiValuedMetaData;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents a parameter definition of a {@link FlexoBehaviour}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FlexoBehaviourParameter.FlexoBehaviourParameterImpl.class)
@XMLElement(xmlTag = "GenericBehaviourParameter")
public interface FlexoBehaviourParameter extends FlexoBehaviourObject, FunctionArgument, WidgetContext, FMLPrettyPrintable {

	public static enum WidgetType {
		TEXT_FIELD {
			@Override
			public String getAnnotation() {
				return "TextField";
			}
		},
		TEXT_AREA {
			@Override
			public String getAnnotation() {
				return "TextArea";
			}
		},
		DATE {
			@Override
			public String getAnnotation() {
				return "DateWidget";
			}
		},
		URI {
			@Override
			public String getAnnotation() {
				return "URITextField";
			}
		},
		LOCALIZED_TEXT_FIELD {
			@Override
			public String getAnnotation() {
				return "LocalizedTextField";
			}
		},
		INTEGER {
			@Override
			public String getAnnotation() {
				return "IntegerWidget";
			}
		},
		FLOAT {
			@Override
			public String getAnnotation() {
				return "FloatWidget";
			}
		},
		CHECKBOX {
			@Override
			public String getAnnotation() {
				return "Checkbox";
			}
		},
		DROPDOWN {
			@Override
			public String getAnnotation() {
				return "DropDown";
			}
		},
		RADIO_BUTTON {
			@Override
			public String getAnnotation() {
				return "RadioButton";
			}
		},
		CHECKBOX_LIST {
			@Override
			public String getAnnotation() {
				return "Checkboxist";
			}
		},
		CUSTOM_WIDGET {
			@Override
			public String getAnnotation() {
				return "CustomWidget";
			}
		};

		public abstract String getAnnotation();
	}

	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String DEFAULT_VALUE_KEY = "defaultValue";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String LIST_KEY = "list";
	@PropertyIdentifier(type = String.class)
	public static final String DESCRIPTION_KEY = "description";
	@PropertyIdentifier(type = Type.class)
	public static final String TYPE_KEY = "type";
	@PropertyIdentifier(type = WidgetType.class)
	public static final String WIDGET_KEY = "widget";
	@PropertyIdentifier(type = boolean.class)
	public static final String IS_REQUIRED_KEY = "isRequired";
	@PropertyIdentifier(type = FlexoBehaviour.class)
	public static final String FLEXO_BEHAVIOUR_KEY = "flexoBehaviour";

	@Override
	@Getter(value = NAME_KEY)
	@XMLAttribute
	public String getName();

	@Override
	@Setter(NAME_KEY)
	public void setName(String name) throws InvalidNameException;

	@Override
	@Getter(value = TYPE_KEY, isStringConvertable = true)
	@XMLAttribute
	public abstract Type getType();

	@Setter(TYPE_KEY)
	public void setType(Type aType);

	/**
	 * We define an updater for TYPE property because we need to translate supplied Type to valid TypingSpace
	 * 
	 * @param type
	 */
	@Updater(TYPE_KEY)
	public void updateType(Type type);

	@Override
	@Getter(value = WIDGET_KEY)
	@XMLAttribute
	public WidgetType getWidget();

	@Setter(WIDGET_KEY)
	public void setWidget(WidgetType widget);

	@Getter(value = DEFAULT_VALUE_KEY)
	@XMLAttribute
	public DataBinding<?> getDefaultValue();

	@Setter(DEFAULT_VALUE_KEY)
	public void setDefaultValue(DataBinding<?> defaultValue);

	@Getter(value = IS_REQUIRED_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean getIsRequired();

	@Setter(IS_REQUIRED_KEY)
	public void setIsRequired(boolean isRequired);

	public boolean isValid(FlexoBehaviourAction<?, ?, ?> action, Object value);

	public Object getDefaultValue(BindingEvaluationContext evaluationContext);

	@Override
	@Getter(value = CONTAINER_KEY, ignoreForEquality = true)
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

	@Getter(value = FLEXO_BEHAVIOUR_KEY /*, inverse = FlexoBehaviour.PARAMETERS_KEY*/)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoBehaviour getBehaviour();

	@Setter(FLEXO_BEHAVIOUR_KEY)
	public void setBehaviour(FlexoBehaviour flexoBehaviour);

	public List<WidgetType> getAvailableWidgetTypes();

	public boolean isListType();

	public MultiValuedMetaData makeParameterMetaData();

	public MultiValuedMetaData getParameterMetaData(boolean ensureExistence);

	public static abstract class FlexoBehaviourParameterImpl extends FlexoBehaviourObjectImpl implements FlexoBehaviourParameter {

		private static final Logger logger = Logger.getLogger(FlexoBehaviourParameter.class.getPackage().getName());

		private DataBinding<?> defaultValue;
		private DataBinding<?> container;
		private DataBinding<List<?>> list;

		public static final List<String> AVAILABLE_ANNOTATIONS = new ArrayList<>();

		static {
			for (WidgetType widgetType : WidgetType.values()) {
				AVAILABLE_ANNOTATIONS.add(widgetType.getAnnotation());
			}
		}

		public FlexoBehaviourParameterImpl() {
			super();
		}

		@Override
		public String getStringRepresentation() {
			return (getOwningVirtualModel() != null ? getOwningVirtualModel().getStringRepresentation() : "null") + "#"
					+ (getFlexoConcept() != null ? getFlexoConcept().getName() : "null") + "."
					+ (getFlexoBehaviour() != null ? getFlexoBehaviour().getName() : "null") + "." + getName();
		}

		@Override
		public FlexoBehaviour getFlexoBehaviour() {
			return getBehaviour();
		}

		@Override
		public void setName(String name) throws InvalidNameException {
			String oldSignature = getFlexoBehaviour() != null ? getFlexoBehaviour().getSignature() : null;
			super.setName(name);
			if (getParameterMetaData(false) != null) {
				getParameterMetaData(false).setSingleMetaData("value", name, String.class);
			}
			if (getFlexoBehaviour() != null) {
				((FlexoBehaviourImpl) getFlexoBehaviour()).updateSignature(oldSignature);
			}
		}

		@Override
		public void setType(Type aType) {

			// TODO: to be removed: Conversion from XML
			if (aType != null && aType.equals(Boolean.class)) {
				aType = Boolean.TYPE;
			}
			if (aType != null && aType.equals(Byte.class)) {
				aType = Byte.TYPE;
			}
			if (aType != null && aType.equals(Short.class)) {
				aType = Short.TYPE;
			}
			if (aType != null && aType.equals(Integer.class)) {
				aType = Integer.TYPE;
			}
			if (aType != null && aType.equals(Long.class)) {
				aType = Long.TYPE;
			}
			if (aType != null && aType.equals(Float.class)) {
				aType = Float.TYPE;
			}
			if (aType != null && aType.equals(Double.class)) {
				aType = Double.TYPE;
			}
			if (aType != null && aType.equals(Character.class)) {
				aType = Character.TYPE;
			}

			performSuperSetter(TYPE_KEY, aType);
			listType = null;
			if (list != null) {
				list.setDeclaredType(getListType());
			}
			if (defaultValue != null) {
				defaultValue.setDeclaredType(aType);
			}
			getPropertyChangeSupport().firePropertyChange("availableWidgetTypes", null, getAvailableWidgetTypes());
			getPropertyChangeSupport().firePropertyChange("isListType", !isListType(), isListType());
			/*if (!getAvailableWidgetTypes().contains(getWidget()) && getAvailableWidgetTypes().size() > 0) {
				setWidget(getAvailableWidgetTypes().get(0));
			}*/
		}

		/**
		 * We define an updater for TYPE property because we need to translate supplied Type to valid TypingSpace
		 * 
		 * This updater is called during updateWith() processing (generally applied during the FML parsing phases)
		 * 
		 * @param type
		 */
		@Override
		public void updateType(Type type) {

			if (getDeclaringCompilationUnit() != null && type instanceof CustomType) {
				setType(((CustomType) type).translateTo(getDeclaringCompilationUnit().getTypingSpace()));
			}
			else {
				setType(type);
			}
		}

		private WidgetType getWidgetType(String metaDataKey) {
			for (WidgetType widgetType : WidgetType.values()) {
				if (metaDataKey.equals(widgetType.getAnnotation())) {
					return widgetType;
				}
			}
			return null;
		}

		private MultiValuedMetaData parameterMetaData;

		@Override
		public MultiValuedMetaData makeParameterMetaData() {
			MultiValuedMetaData returned = getFMLModelFactory()
					.newMultiValuedMetaData(getWidget() != null ? getWidget().getAnnotation() : "???");
			returned.setValue("value", getName(), String.class);
			return returned;
		}

		@Override
		public synchronized MultiValuedMetaData getParameterMetaData(boolean ensureExistence) {
			if (getFlexoBehaviour() != null) {
				if (parameterMetaData != null) {
					MultiValuedMetaData returned = parameterMetaData;
					ListMetaData md = getFlexoBehaviour().getUIMetaData(true);
					if (md != null) {
						parameterMetaData.setValue("value", getName(), String.class);
						md.addToMetaDataList(parameterMetaData);
						parameterMetaData = null;
					}
					return returned;
				}
				return getFlexoBehaviour().getMetaDataForParameter(this, ensureExistence);
			}
			if (parameterMetaData == null && ensureExistence) {
				parameterMetaData = makeParameterMetaData();
			}
			return parameterMetaData;
		}

		public WidgetType getDefaultWidget() {
			if (getAvailableWidgetTypes() != null && getAvailableWidgetTypes().size() > 0) {
				return getAvailableWidgetTypes().get(0);
			}
			return null;
		}

		@Override
		public WidgetType getWidget() {
			MultiValuedMetaData md = getParameterMetaData(false);
			if (md != null) {
				return getWidgetType(md.getKey());
			}
			return getDefaultWidget();
		}

		@Override
		public void setWidget(WidgetType widget) {
			/*if (widget == WidgetType.CUSTOM_WIDGET) {
				System.out.println("Tiens, ca vient d'ou ca ?");
				Thread.dumpStack();
			}*/
			// System.out.println("On fait setWidget avec " + widget);
			if (widget != null && widget != getWidget()) {
				MultiValuedMetaData md = getParameterMetaData(true);
				md.setKey(widget.getAnnotation());
				// System.out.println("KEY: " + widget.getAnnotation());
			}
			// System.out.println("getWidget=" + widget);
			// System.out.println("md=" + getParameterMetaData(false).getKey());
		}

		@Override
		public String toString() {
			return "FlexoBehaviourParameter: " + getName();
		}

		@Override
		public int getIndex() {
			if (getBehaviour() != null) {
				return getBehaviour().getParameters().indexOf(this);
			}
			return -1;
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			return getBehaviour() != null ? getBehaviour().getFlexoConcept() : null;
		}

		@Override
		public BindingModel getBindingModel() {
			if (getBehaviour() != null) {
				return getBehaviour().getBindingModel();
			}
			return null;
		}

		@Override
		public void setBehaviour(FlexoBehaviour flexoBehaviour) {
			BindingModel oldBM = getFlexoBehaviour() != null ? getFlexoBehaviour().getBindingModel() : null;
			performSuperSetter(FLEXO_BEHAVIOUR_KEY, flexoBehaviour);
			BindingModel newBM = getFlexoBehaviour() != null ? getFlexoBehaviour().getBindingModel() : null;
			getPropertyChangeSupport().firePropertyChange(Bindable.BINDING_MODEL_PROPERTY, oldBM, newBM);
		}

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

		@Override
		public DataBinding<?> getDefaultValue() {
			if (defaultValue == null) {
				defaultValue = new DataBinding<>(this, getType(), BindingDefinitionType.GET);
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
		}

		@Override
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
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		/*private boolean isRequired = false;
		
		@Override
		public boolean getIsRequired() {
			return isRequired;
		}
		
		@Override
		public final void setIsRequired(boolean flag) {
			isRequired = flag;
		}*/

		@Override
		public boolean isValid(FlexoBehaviourAction<?, ?, ?> action, Object value) {
			if (!getIsRequired()) {
				return true;
			}

			if (value instanceof String) {
				return StringUtils.isNotEmpty((String) value);
			}

			return value != null;
		}

		@Override
		public Function getFunction() {
			return getFlexoBehaviour();
		}

		@Override
		public String getArgumentName() {
			return getName();
		}

		@Override
		public Type getArgumentType() {
			return getType();
		}

		@Override
		public boolean isListType() {
			return TypeUtils.isList(getType());
		}

		@Override
		public List<WidgetType> getAvailableWidgetTypes() {
			return getAvailableWidgetTypes(getType());
		}

		private static WidgetType[] STRING_WIDGET_TYPES_ARRAY = { WidgetType.TEXT_FIELD, WidgetType.TEXT_AREA, WidgetType.URI,
				WidgetType.LOCALIZED_TEXT_FIELD, WidgetType.DROPDOWN, WidgetType.RADIO_BUTTON, WidgetType.CUSTOM_WIDGET };
		private static WidgetType[] DATE_WIDGET_TYPES_ARRAY = { WidgetType.DATE, WidgetType.CUSTOM_WIDGET };
		private static WidgetType[] BOOLEAN_WIDGET_TYPES_ARRAY = { WidgetType.CHECKBOX, WidgetType.CUSTOM_WIDGET };
		private static WidgetType[] FLOAT_WIDGET_TYPES_ARRAY = { WidgetType.FLOAT, WidgetType.CUSTOM_WIDGET };
		private static WidgetType[] INTEGER_WIDGET_TYPES_ARRAY = { WidgetType.INTEGER, WidgetType.CUSTOM_WIDGET };
		private static WidgetType[] LIST_WIDGET_TYPES_ARRAY = { WidgetType.DROPDOWN, WidgetType.RADIO_BUTTON, WidgetType.CHECKBOX_LIST,
				WidgetType.CUSTOM_WIDGET };
		private static WidgetType[] CUSTOM_WIDGET_TYPES_ARRAY = { WidgetType.CUSTOM_WIDGET };

		private static List<WidgetType> STRING_WIDGET_TYPES = Arrays.asList(STRING_WIDGET_TYPES_ARRAY);
		private static List<WidgetType> DATE_WIDGET_TYPES = Arrays.asList(DATE_WIDGET_TYPES_ARRAY);
		private static List<WidgetType> BOOLEAN_WIDGET_TYPES = Arrays.asList(BOOLEAN_WIDGET_TYPES_ARRAY);
		private static List<WidgetType> FLOAT_WIDGET_TYPES = Arrays.asList(FLOAT_WIDGET_TYPES_ARRAY);
		private static List<WidgetType> INTEGER_WIDGET_TYPES = Arrays.asList(INTEGER_WIDGET_TYPES_ARRAY);
		private static List<WidgetType> LIST_WIDGET_TYPES = Arrays.asList(LIST_WIDGET_TYPES_ARRAY);
		private static List<WidgetType> CUSTOM_WIDGET_TYPES = Arrays.asList(CUSTOM_WIDGET_TYPES_ARRAY);

		public static List<WidgetType> getAvailableWidgetTypes(Type type) {
			if (TypeUtils.isString(type)) {
				return STRING_WIDGET_TYPES;
			}
			if (TypeUtils.isDate(type)) {
				return DATE_WIDGET_TYPES;
			}
			else if (TypeUtils.isBoolean(type)) {
				return BOOLEAN_WIDGET_TYPES;
			}
			else if (TypeUtils.isDouble(type) || TypeUtils.isFloat(type)) {
				return FLOAT_WIDGET_TYPES;
			}
			else if (TypeUtils.isLong(type) || TypeUtils.isInteger(type) || TypeUtils.isShort(type)) {
				return INTEGER_WIDGET_TYPES;
			}
			else if (TypeUtils.isList(type)) {
				return LIST_WIDGET_TYPES;
			}
			else if (TypeUtils.isEnum(type)) {
				return LIST_WIDGET_TYPES;
			}
			else if (type instanceof FlexoEnumType) {
				return LIST_WIDGET_TYPES;
			}
			return CUSTOM_WIDGET_TYPES;
		}

		/**
		 * Return a String encoding a {@link DataBinding} which should get access to represented data from the context beeing represented by
		 * this
		 * 
		 * @return
		 */
		@Override
		public String getWidgetDataAccess() {
			return "parameters." + getName() + "";
		}

		/**
		 * Return a String encoding a {@link DataBinding} which should get access to represented data definition (which is this object)
		 * 
		 * @return
		 */
		@Override
		public String getWidgetDefinitionAccess() {
			return "parameters." + getName() + ".definition";
		}

		/**
		 * Return a String encoding a {@link DataBinding} which should get access to instance of FlexoConcept
		 * 
		 * @return
		 */
		@Override
		public String getFlexoConceptInstanceAccess() {
			return "flexoConceptInstance";
		}

		/**
		 * Depending of type of data to represent, return a list of objects which may be used to represented data
		 * 
		 * @return
		 */
		@Override
		public List<?> getListOfObjects() {
			if (getType() instanceof FlexoEnumType) {
				return ((FlexoEnumType) getType()).getFlexoEnum().getInstances();
			}
			return null;
		}

	}
}
