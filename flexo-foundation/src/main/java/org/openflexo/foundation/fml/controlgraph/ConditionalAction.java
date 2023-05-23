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
import org.openflexo.connie.type.ExplicitNullType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.connie.type.UndefinedType;
import org.openflexo.foundation.fml.FMLUtils;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Embedded;
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
@ImplementationClass(ConditionalAction.ConditionalActionImpl.class)
@XMLElement
public interface ConditionalAction extends ControlStructureAction, FMLControlGraphOwner {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONDITION_KEY = "condition";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String THEN_CONTROL_GRAPH_KEY = "thenControlGraph";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String ELSE_CONTROL_GRAPH_KEY = "elseControlGraph";

	@Getter(value = CONDITION_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getCondition();

	@Setter(CONDITION_KEY)
	public void setCondition(DataBinding<Boolean> condition);

	@Getter(value = THEN_CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement(context = "ThenControlGraph_")
	public FMLControlGraph getThenControlGraph();

	@Setter(THEN_CONTROL_GRAPH_KEY)
	public void setThenControlGraph(FMLControlGraph aControlGraph);

	@Getter(value = ELSE_CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement(context = "ElseControlGraph_")
	public FMLControlGraph getElseControlGraph();

	@Setter(ELSE_CONTROL_GRAPH_KEY)
	public void setElseControlGraph(FMLControlGraph aControlGraph);

	public boolean evaluateCondition(BindingEvaluationContext evaluationContext);

	public static abstract class ConditionalActionImpl extends ControlStructureActionImpl implements ConditionalAction {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(ConditionalAction.class.getPackage().getName());

		private DataBinding<Boolean> condition;

		@Override
		public void setThenControlGraph(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(THEN_CONTROL_GRAPH_KEY);
			}
			performSuperSetter(THEN_CONTROL_GRAPH_KEY, aControlGraph);
		}

		@Override
		public void setElseControlGraph(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(ELSE_CONTROL_GRAPH_KEY);
			}
			performSuperSetter(ELSE_CONTROL_GRAPH_KEY, aControlGraph);
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (THEN_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getThenControlGraph();
			}
			else if (ELSE_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getElseControlGraph();
			}
			return null;
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (THEN_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setThenControlGraph(controlGraph);
			}
			else if (ELSE_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setElseControlGraph(controlGraph);
			}
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
		public DataBinding<Boolean> getCondition() {
			if (condition == null) {
				condition = new DataBinding<>(this, Boolean.class, DataBinding.BindingDefinitionType.GET);
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
						/*System.out.println("Evaluation of " + getCondition() + " returns null");
						DataBinding db1 = new DataBinding<Object>("city1.name", getCondition().getOwner(), Object.class,
								BindingDefinitionType.GET);
						DataBinding db2 = new DataBinding<Object>("city2.mayor.name", getCondition().getOwner(), Object.class,
								BindingDefinitionType.GET);
						System.out.println("city1.name=" + db1.getBindingValue(action));
						System.out.println("city2.mayor.name=" + db2.getBindingValue(action));*/
						return false;
					}
					return returned;
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
					return false;
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
			return true;
		}

		@Override
		public String getStringRepresentation() {
			if (getCondition().isSet() && getCondition().isValid()) {
				return getHeaderContext() + getCondition() + " ?";
			}
			return getHeaderContext() + " ? ";
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException {
			if (evaluateCondition(evaluationContext))
				return getThenControlGraph().execute(evaluationContext);
			if (getElseControlGraph() != null)
				return getElseControlGraph().execute(evaluationContext);
			return null;
		}

		@Override
		public void reduce() {
			if (getThenControlGraph() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getThenControlGraph()).reduce();
			}
			if (getElseControlGraph() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getElseControlGraph()).reduce();
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
			if (getThenControlGraph() != null) {
				getThenControlGraph().getBindingModel().setBaseBindingModel(getInferedBindingModel());
			}
			if (getElseControlGraph() != null) {
				getElseControlGraph().getBindingModel().setBaseBindingModel(getInferedBindingModel());
			}
		}

		@Override
		public Type getInferedType() {

			if (getThenControlGraph() != null) {

				Type inferedType1 = getThenControlGraph().getInferedType();

				if (getElseControlGraph() != null) {
					Type inferedType2 = getElseControlGraph().getInferedType();

					if (inferedType1 instanceof ExplicitNullType) {
						if (inferedType2 instanceof ExplicitNullType) {
							return Object.class;
						}
						return inferedType2;
					}

					if (inferedType1 instanceof UndefinedType) {
						if (inferedType2 instanceof UndefinedType) {
							return UndefinedType.INSTANCE;
						}
						return inferedType2;
					}

					if (inferedType2 instanceof ExplicitNullType || inferedType2 instanceof UndefinedType) {
						return inferedType1;
					}

					if (TypeUtils.isTypeAssignableFrom(inferedType1, inferedType2)) {
						return inferedType1;
					}
					if (TypeUtils.isTypeAssignableFrom(inferedType2, inferedType1)) {
						return inferedType2;
					}
					if (inferedType1 instanceof FlexoConceptInstanceType && inferedType2 instanceof FlexoConceptInstanceType) {
						FlexoConcept ancestor = FMLUtils.getMostSpecializedAncestor(
								((FlexoConceptInstanceType) inferedType1).getFlexoConcept(),
								((FlexoConceptInstanceType) inferedType2).getFlexoConcept());
						if (ancestor != null) {
							return ancestor.getInstanceType();
						}
						else {
							return FlexoConceptInstance.class;
						}
					}
				}

				return inferedType1;
			}

			return Void.class;
		}

		@Override
		public void accept(FMLControlGraphVisitor visitor) {
			super.accept(visitor);
			if (getThenControlGraph() != null) {
				getThenControlGraph().accept(visitor);
			}
			if (getElseControlGraph() != null) {
				getElseControlGraph().accept(visitor);
			}
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getCondition().rebuild();
		}

	}

	@DefineValidationRule
	public static class ConditionBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<ConditionalAction> {
		public ConditionBindingIsRequiredAndMustBeValid() {
			super("'condition'_binding_is_not_valid", ConditionalAction.class);
		}

		@Override
		public DataBinding<Boolean> getBinding(ConditionalAction object) {
			return object.getCondition();
		}

	}

	@DefineValidationRule
	public static class InferedTypesMustBeCompatible extends ValidationRule<InferedTypesMustBeCompatible, ConditionalAction> {
		public InferedTypesMustBeCompatible() {
			super(ConditionalAction.class, "infered_types_must_be_compatible_in_a_conditional");
		}

		@Override
		public ValidationIssue<InferedTypesMustBeCompatible, ConditionalAction> applyValidation(ConditionalAction conditional) {

			if (conditional.getThenControlGraph() != null) {
				Type inferedType1 = conditional.getThenControlGraph().getInferedType();
				if (conditional.getElseControlGraph() != null) {
					Type inferedType2 = conditional.getElseControlGraph().getInferedType();
					if (inferedType1 != Void.class && inferedType2 != Void.class
							&& !TypeUtils.isTypeAssignableFrom(inferedType1, inferedType2)
							&& !TypeUtils.isTypeAssignableFrom(inferedType2, inferedType1)) {

						if (inferedType1 instanceof FlexoConceptInstanceType && inferedType2 instanceof FlexoConceptInstanceType) {
						}
						else {
							return new ValidationError<>(this, conditional,
									"types_are_not_compatible (" + TypeUtils.simpleRepresentation(inferedType1) + " and "
											+ TypeUtils.simpleRepresentation(inferedType2) + ")");
						}
					}
				}
			}

			return null;
		}
	}

}
