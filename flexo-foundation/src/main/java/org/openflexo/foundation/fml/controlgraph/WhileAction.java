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

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext.ReturnException;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

@ModelEntity
@ImplementationClass(WhileAction.WhileActionImpl.class)
@XMLElement
public interface WhileAction extends ControlStructureAction, FMLControlGraphOwner {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONDITION_KEY = "condition";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String CONTROL_GRAPH_KEY = "controlGraph";
	@PropertyIdentifier(type = Boolean.class)
	public static final String EVALUATE_CONDITION_AFTER_CYCLE_KEY = "evaluateConditionAfterCycle";

	@Getter(value = CONDITION_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getCondition();

	@Setter(CONDITION_KEY)
	public void setCondition(DataBinding<Boolean> condition);

	@Getter(value = CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement(context = "ControlGraph_")
	public FMLControlGraph getControlGraph();

	@Setter(CONTROL_GRAPH_KEY)
	public void setControlGraph(FMLControlGraph aControlGraph);

	@Getter(value = EVALUATE_CONDITION_AFTER_CYCLE_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean getEvaluateConditionAfterCycle();

	@Setter(EVALUATE_CONDITION_AFTER_CYCLE_KEY)
	public void setEvaluateConditionAfterCycle(boolean evaluateAfter);

	public boolean evaluateCondition(BindingEvaluationContext evaluationContext);

	public static abstract class WhileActionImpl extends ControlStructureActionImpl implements WhileAction {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(WhileAction.class.getPackage().getName());

		private DataBinding<Boolean> condition;

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getControlGraph();
			}
			return null;
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setControlGraph(controlGraph);
			}
		}

		@Override
		public void setControlGraph(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(CONTROL_GRAPH_KEY);
			}
			performSuperSetter(CONTROL_GRAPH_KEY, aControlGraph);
		}

		@Override
		public BindingModel getBaseBindingModel(FMLControlGraph controlGraph) {
			return getBindingModel();
		}

		// No need to rebuild a BindingModel here, since nothing is appened to the context
		@Override
		protected ControlGraphBindingModel<?> makeInferedBindingModel() {
			return getBindingModel();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			if (getEvaluateConditionAfterCycle()) {
				out.append("do {", context);
				out.append(StringUtils.LINE_SEPARATOR, context);
				if (getControlGraph() != null) {
					out.append(getControlGraph().getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context);
				}
				out.append("} while " + getCondition().toString() + "", context);
			}
			else {
				out.append("while " + getCondition().toString() + "", context);
				out.append(" {", context);
				out.append(StringUtils.LINE_SEPARATOR, context);
				if (getControlGraph() != null) {
					out.append(getControlGraph().getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context);
				}
				out.append("}", context);
			}

			return out.toString();
		}

		@Override
		public DataBinding<Boolean> getCondition() {
			if (condition == null) {
				condition = new DataBinding<Boolean>(this, Boolean.class, DataBinding.BindingDefinitionType.GET);
				condition.setBindingName("condition");
			}
			return condition;
		}

		@Override
		public void setCondition(DataBinding<Boolean> condition) {
			if (condition != null) {
				condition.setOwner(this);
				condition.setDeclaredType(Boolean.class);
				condition.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				condition.setBindingName("condition");
			}
			this.condition = condition;
		}

		@Override
		public boolean evaluateCondition(BindingEvaluationContext evaluationContext) {
			DataBinding<Boolean> condition = getCondition();
			if (condition.isSet() && condition.isValid()) {
				try {
					Boolean returned = condition.getBindingValue(evaluationContext);
					if (returned == null) {
						return false;
					}
					return returned;
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
		public String getStringRepresentation() {
			if (getEvaluateConditionAfterCycle()) {
				if (getCondition().isSet() && getCondition().isValid()) {
					return getHeaderContext() + " do while " + getCondition();
				}
				return getHeaderContext() + " do while ";
			}
			else {
				if (getCondition().isSet() && getCondition().isValid()) {
					return getHeaderContext() + " while " + getCondition();
				}
				return getHeaderContext() + " while ";
			}
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FlexoException {

			if (getEvaluateConditionAfterCycle()) {
				do {
					try {
						getControlGraph().execute(evaluationContext);
					} catch (ReturnException e) {
						throw e;
					}
				} while (evaluateCondition(evaluationContext));
			}
			else {
				while (evaluateCondition(evaluationContext)) {
					try {
						getControlGraph().execute(evaluationContext);
					} catch (ReturnException e) {
						throw e;
					}
				}
			}
			return null;
		}

		@Override
		public void reduce() {
			if (getControlGraph() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getControlGraph()).reduce();
			}
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			if (dataBinding == getCondition()) {
				getPropertyChangeSupport().firePropertyChange(CONDITION_KEY, null, getCondition());
			}
			super.notifiedBindingChanged(dataBinding);
		}

		@Override
		public void setOwner(FMLControlGraphOwner owner) {
			super.setOwner(owner);
			if (getControlGraph() != null) {
				getControlGraph().getBindingModel().setBaseBindingModel(getInferedBindingModel());
			}
		}

		@Override
		public Type getInferedType() {
			if (getControlGraph() != null) {
				return getControlGraph().getInferedType();
			}
			return Void.class;
		}

		@Override
		public void accept(FMLControlGraphVisitor visitor) {
			super.accept(visitor);
			if (getControlGraph() != null) {
				getControlGraph().accept(visitor);
			}
		}

	}

	@DefineValidationRule
	public static class ConditionBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<WhileAction> {
		public ConditionBindingIsRequiredAndMustBeValid() {
			super("'condition'_binding_is_not_valid", WhileAction.class);
		}

		@Override
		public DataBinding<Boolean> getBinding(WhileAction object) {
			return object.getCondition();
		}

	}

}
