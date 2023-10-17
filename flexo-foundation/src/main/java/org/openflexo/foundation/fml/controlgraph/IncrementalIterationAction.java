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

package org.openflexo.foundation.fml.controlgraph;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLMigration;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

@ModelEntity
@ImplementationClass(IncrementalIterationAction.IncrementalIterationActionImpl.class)
@XMLElement
@FMLMigration("Should be replaced by ExpressionIterationAction")
@Deprecated
public interface IncrementalIterationAction extends AbstractIterationAction {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String START_VALUE_KEY = "startValue";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String EXCLUSIVE_END_VALUE_KEY = "exclusiveEndValue";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String INCREMENT_KEY = "increment";

	@Getter(value = START_VALUE_KEY)
	@XMLAttribute
	public DataBinding<Number> getStartValue();

	@Setter(START_VALUE_KEY)
	public void setStartValue(DataBinding<Number> iteration);

	@Getter(value = EXCLUSIVE_END_VALUE_KEY)
	@XMLAttribute
	public DataBinding<Number> getExclusiveEndValue();

	@Setter(EXCLUSIVE_END_VALUE_KEY)
	public void setExclusiveEndValue(DataBinding<Number> iteration);

	@Getter(value = INCREMENT_KEY)
	@XMLAttribute
	public DataBinding<Number> getIncrement();

	@Setter(INCREMENT_KEY)
	public void setIncrement(DataBinding<Number> increment);

	public static abstract class IncrementalIterationActionImpl extends AbstractIterationActionImpl implements IncrementalIterationAction {

		private static final Logger logger = Logger.getLogger(IncrementalIterationAction.class.getPackage().getName());

		private DataBinding<Number> startValue;
		private DataBinding<Number> exclusiveEndValue;
		private DataBinding<Number> increment;

		private boolean isStartValueBuilding = false;
		private boolean isExclusiveEndValueBuilding = false;
		private boolean isIncrementBuilding = false;

		@Override
		public DataBinding<Number> getStartValue() {
			if (startValue == null && !isStartValueBuilding) {
				try {
					isStartValueBuilding = true;
					startValue = new DataBinding<>("0", this, Number.class, BindingDefinitionType.GET);
					startValue.setBindingName("startValue");
				} finally {
					isStartValueBuilding = false;
				}
			}
			return startValue;
		}

		@Override
		public void setStartValue(DataBinding<Number> startValue) {
			if (startValue != null) {
				startValue.setOwner(this);
				startValue.setBindingName("startValue");
				startValue.setDeclaredType(Number.class);
				startValue.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.startValue = startValue;
		}

		@Override
		public DataBinding<Number> getExclusiveEndValue() {
			if (exclusiveEndValue == null && !isExclusiveEndValueBuilding) {
				try {
					isExclusiveEndValueBuilding = true;
					exclusiveEndValue = new DataBinding<>(this, Number.class, BindingDefinitionType.GET);
					exclusiveEndValue.setBindingName("exclusiveEndValue");
				} finally {
					isExclusiveEndValueBuilding = false;
				}
			}
			return exclusiveEndValue;
		}

		@Override
		public void setExclusiveEndValue(DataBinding<Number> exclusiveEndValue) {
			if (exclusiveEndValue != null) {
				exclusiveEndValue.setOwner(this);
				exclusiveEndValue.setBindingName("exclusiveEndValue");
				exclusiveEndValue.setDeclaredType(Number.class);
				exclusiveEndValue.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.exclusiveEndValue = exclusiveEndValue;
		}

		@Override
		public DataBinding<Number> getIncrement() {
			if (increment == null && !isIncrementBuilding) {
				try {
					isIncrementBuilding = true;
					increment = new DataBinding<>("1", this, Number.class, BindingDefinitionType.GET);
					increment.setBindingName("increment");
				} finally {
					isIncrementBuilding = false;
				}
			}
			return increment;
		}

		@Override
		public void setIncrement(DataBinding<Number> increment) {
			if (increment != null) {
				increment.setOwner(this);
				increment.setBindingName("increment");
				increment.setDeclaredType(Number.class);
				increment.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.increment = increment;
		}

		@Override
		public Type getItemType() {
			if (getStartValue() != null && getStartValue().isSet() && getStartValue().isValid()) {
				return getStartValue().getAnalyzedType();
			}
			return Object.class;
		}

		private Number evaluateStartValue(RunTimeEvaluationContext evaluationContext) {
			if (getStartValue() != null && getStartValue().isSet() && getStartValue().isValid()) {
				try {
					return getStartValue().getBindingValue(evaluationContext);
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
			return 0;
		}

		private Number evaluateExclusiveEndValue(RunTimeEvaluationContext evaluationContext) {
			if (getExclusiveEndValue() != null && getExclusiveEndValue().isSet() && getExclusiveEndValue().isValid()) {
				try {
					return getExclusiveEndValue().getBindingValue(evaluationContext);
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
			return 0;
		}

		private Number evaluateIncrement(RunTimeEvaluationContext evaluationContext) {
			if (getIncrement() != null && getIncrement().isSet() && getIncrement().isValid()) {
				try {
					return getIncrement().getBindingValue(evaluationContext);
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
			return 1;
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException {

			Number startValue = evaluateStartValue(evaluationContext);
			Number exclusiveEndValue = evaluateExclusiveEndValue(evaluationContext);
			Number increment = evaluateIncrement(evaluationContext);

			if (TypeUtils.isTypeAssignableFrom(Integer.class, getItemType(), true)) {
				for (long currentValue = startValue.longValue(); currentValue < exclusiveEndValue.longValue(); currentValue = currentValue
						+ increment.longValue()) {
					// System.out.println("> working with " + getIteratorName() + "=" + currentValue);
					evaluationContext.declareVariable(getIteratorName(), currentValue);
					try {
						getControlGraph().execute(evaluationContext);
					} catch (ReturnException e) {
						evaluationContext.dereferenceVariable(getIteratorName());
						throw e;
					}
				}
			}
			else {
				for (double currentValue = startValue.doubleValue(); currentValue < exclusiveEndValue
						.doubleValue(); currentValue = currentValue + increment.doubleValue()) {
					// System.out.println("> working with " + getIteratorName() + "=" + currentValue);
					evaluationContext.declareVariable(getIteratorName(), currentValue);
					try {
						getControlGraph().execute(evaluationContext);
					} catch (ReturnException e) {
						evaluationContext.dereferenceVariable(getIteratorName());
						throw e;
					}
				}
			}

			return null;
		}

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + "for (" + getItemType() + " " + getIteratorName() + "=" + getStartValue() + " ; "
					+ getIteratorName() + "<" + getExclusiveEndValue() + " ; " + getIncrement() + "++" + ")";
		}

		@Override
		public BindingModel getBaseBindingModel(FMLControlGraph controlGraph) {
			if (controlGraph == getControlGraph()) {
				return getInferedBindingModel();
				// return getControlGraph().getBindingModel();
			}
			logger.warning("Unexpected control graph: " + controlGraph);
			return null;
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			if (dataBinding == getStartValue()) {
				getPropertyChangeSupport().firePropertyChange(START_VALUE_KEY, null, getStartValue());
			}
			if (dataBinding == getExclusiveEndValue()) {
				getPropertyChangeSupport().firePropertyChange(EXCLUSIVE_END_VALUE_KEY, null, getExclusiveEndValue());
			}
			if (dataBinding == getIncrement()) {
				getPropertyChangeSupport().firePropertyChange(INCREMENT_KEY, null, getIncrement());
			}
			super.notifiedBindingChanged(dataBinding);
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getExclusiveEndValue().rebuild();
			getIncrement().rebuild();
			getStartValue().rebuild();
		}

	}

	@DefineValidationRule
	public static class IncrementalIterationActionMustDefineAValidIteration
			extends ValidationRule<IncrementalIterationActionMustDefineAValidIteration, IncrementalIterationAction> {
		public IncrementalIterationActionMustDefineAValidIteration() {
			super(IncrementalIterationAction.class, "iteration_action_must_define_a_valid_iteration");
		}

		@Override
		public ValidationIssue<IncrementalIterationActionMustDefineAValidIteration, IncrementalIterationAction> applyValidation(
				IncrementalIterationAction action) {
			if (action.getStartValue() == null || !action.getStartValue().isValid()) {
				return new ValidationError<>(this, action, "iteration_action_does_not_define_a_valid_start_value");
			}
			if (action.getExclusiveEndValue() == null || !action.getExclusiveEndValue().isValid()) {
				return new ValidationError<>(this, action, "iteration_action_does_not_define_a_valid_exclusive_end_value_value");
			}
			if (action.getIncrement() == null || !action.getIncrement().isValid()) {
				return new ValidationError<>(this, action, "iteration_action_does_not_define_a_valid_increment");
			}
			if (!TypeUtils.isTypeAssignableFrom(action.getItemType(), action.getExclusiveEndValue().getAnalyzedType())
					|| !TypeUtils.isTypeAssignableFrom(action.getItemType(), action.getIncrement().getAnalyzedType())) {
				return new ValidationError<>(this, action, "types_are_incompatible");
			}
			return null;
		}

	}

}
