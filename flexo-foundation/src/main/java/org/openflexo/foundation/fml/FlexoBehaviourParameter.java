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
import java.util.logging.Logger;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.binding.Function;
import org.openflexo.connie.binding.Function.FunctionArgument;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents a parameter of a {@link FlexoBehaviour}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoBehaviourParameter.FlexoBehaviourParameterImpl.class)
@Imports({ @Import(CheckboxParameter.class), @Import(DropDownParameter.class), @Import(FloatParameter.class),
		@Import(IntegerParameter.class), @Import(ListParameter.class), @Import(TextAreaParameter.class), @Import(TextFieldParameter.class),
		@Import(FlexoConceptInstanceParameter.class), @Import(ClassParameter.class), @Import(IndividualParameter.class),
		@Import(PropertyParameter.class), @Import(URIParameter.class), @Import(TechnologyObjectParameter.class) })
public interface FlexoBehaviourParameter extends FlexoBehaviourObject, FunctionArgument {

	public static enum WidgetType {
		URI,
		TEXT_FIELD,
		LOCALIZED_TEXT_FIELD,
		TEXT_AREA,
		INTEGER,
		FLOAT,
		CHECKBOX,
		DROPDOWN,
		INDIVIDUAL,
		CLASS,
		PROPERTY,
		OBJECT_PROPERTY,
		DATA_PROPERTY,
		FLEXO_OBJECT,
		LIST,
		FLEXO_CONCEPT,
		TECHNOLOGY_OBJECT;
	}

	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = String.class)
	public static final String LABEL_KEY = "label";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String DEFAULT_VALUE_KEY = "defaultValue";
	@PropertyIdentifier(type = String.class)
	public static final String DESCRIPTION_KEY = "description";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONDITIONAL_KEY = "conditional";
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
	public void setName(String name);

	@Getter(value = LABEL_KEY)
	@XMLAttribute
	public String getLabel();

	@Setter(LABEL_KEY)
	public void setLabel(String label);

	@Getter(value = DEFAULT_VALUE_KEY)
	@XMLAttribute
	public DataBinding<?> getDefaultValue();

	@Setter(DEFAULT_VALUE_KEY)
	public void setDefaultValue(DataBinding<?> defaultValue);

	@Override
	@Getter(value = DESCRIPTION_KEY)
	@XMLElement
	public String getDescription();

	@Override
	@Setter(DESCRIPTION_KEY)
	public void setDescription(String description);

	@Getter(value = CONDITIONAL_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getConditional();

	@Setter(CONDITIONAL_KEY)
	public void setConditional(DataBinding<Boolean> conditional);

	@Getter(value = IS_REQUIRED_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean getIsRequired();

	@Setter(IS_REQUIRED_KEY)
	public void setIsRequired(boolean isRequired);

	public abstract Type getType();

	public boolean isValid(FlexoBehaviourAction action, Object value);

	public Object getDefaultValue(FlexoBehaviourAction<?, ?, ?> action);

	public boolean evaluateCondition(BindingEvaluationContext parameterRetriever);

	public abstract WidgetType getWidget();

	public int getIndex();

	@Getter(value = FLEXO_BEHAVIOUR_KEY, inverse = FlexoBehaviour.PARAMETERS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoBehaviour getBehaviour();

	@Setter(FLEXO_BEHAVIOUR_KEY)
	public void setBehaviour(FlexoBehaviour flexoBehaviour);

	public static abstract class FlexoBehaviourParameterImpl extends FlexoBehaviourObjectImpl implements FlexoBehaviourParameter {

		private static final Logger logger = Logger.getLogger(FlexoBehaviourParameter.class.getPackage().getName());

		private String label;
		// private boolean usePaletteLabelAsDefaultValue;

		// private FlexoBehaviour _scheme;

		private DataBinding<Boolean> conditional;
		private DataBinding<?> defaultValue;

		public FlexoBehaviourParameterImpl() {
			super();
		}

		@Override
		public String getURI() {
			if (getFlexoBehaviour() != null) {
				return getFlexoBehaviour().getURI() + "." + getName();
			}
			return null;
		}

		@Override
		public String getStringRepresentation() {
			return (getOwningVirtualModel() != null ? getOwningVirtualModel().getStringRepresentation() : "null") + "#"
					+ (getFlexoConcept() != null ? getFlexoConcept().getName() : "null") + "."
					+ (getFlexoBehaviour() != null ? getFlexoBehaviour().getName() : "null") + "." + getName();
		}

		@Override
		public abstract Type getType();

		/*private final BindingDefinition CONDITIONAL = new BindingDefinition("conditional", Boolean.class,
				DataBinding.BindingDefinitionType.GET, false);
		private final BindingDefinition DEFAULT_VALUE = new BindingDefinition("defaultValue", Object.class,
				DataBinding.BindingDefinitionType.GET, false) {
			@Override
			public Type getType() {
				return FlexoBehaviourParameterImpl.this.getType();
			};
		};

		public BindingDefinition getConditionalBindingDefinition() {
			return CONDITIONAL;
		}

		public BindingDefinition getDefaultValueBindingDefinition() {
			return DEFAULT_VALUE;
		}*/

		/*@Override
		public void setBehaviour(FlexoBehaviour scheme) {
			_scheme = scheme;
		}*/

		@Override
		public FlexoBehaviour getFlexoBehaviour() {
			return getBehaviour();
		}

		/*@Override
		public FlexoBehaviour getBehaviour() {
			return getFlexoBehaviour();
		}*/

		/*@Override
		public VirtualModel getVirtualModel() {
			if (getBehaviour() != null) {
				return getBehaviour().getVirtualModel();
			}
			return null;
		}*/

		@Override
		public String getLabel() {
			if (label == null || StringUtils.isEmpty(label)) {
				return getName();
			}
			return label;
		}

		@Override
		public void setLabel(String label) {
			this.label = label;
		}

		/*public boolean getUsePaletteLabelAsDefaultValue() {
			return usePaletteLabelAsDefaultValue;
		}

		public void setUsePaletteLabelAsDefaultValue(boolean usePaletteLabelAsDefaultValue) {
			this.usePaletteLabelAsDefaultValue = usePaletteLabelAsDefaultValue;
		}*/

		@Override
		public boolean evaluateCondition(BindingEvaluationContext parameterRetriever) {
			if (getConditional().isValid()) {
				try {
					return getConditional().getBindingValue(parameterRetriever);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			return true;
		}

		@Override
		public String toString() {
			return "FlexoConceptParameter: " + getName();
		}

		@Override
		public int getIndex() {
			if (getBehaviour() != null) {
				return getBehaviour().getParameters().indexOf(this);
			}
			return -1;
		}

		@Override
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
		}

		@Override
		public Object getDefaultValue(FlexoBehaviourAction<?, ?, ?> action) {
			// DiagramPaletteElement paletteElement = action instanceof DropSchemeAction ? ((DropSchemeAction) action).getPaletteElement() :
			// null;

			// System.out.println("Default value for "+element.getName()+" ???");
			/*if (getUsePaletteLabelAsDefaultValue() && paletteElement != null) {
				return paletteElement.getName();
			}*/
			if (getDefaultValue().isValid()) {
				try {
					return getDefaultValue().getBindingValue(action);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		private boolean isRequired = false;

		@Override
		public boolean getIsRequired() {
			return isRequired;
		}

		@Override
		public final void setIsRequired(boolean flag) {
			isRequired = flag;
		}

		@Override
		public boolean isValid(FlexoBehaviourAction action, Object value) {
			return !getIsRequired() || value != null;
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

	}
}
