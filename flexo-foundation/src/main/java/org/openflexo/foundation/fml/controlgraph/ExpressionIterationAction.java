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

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLMigration;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
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
import org.openflexo.pamela.annotations.Updater;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

/**
 * This construction implements a <code>for</code> loop<br>
 * 
 * General syntax is :<br>
 * <code> 
 * for (Type var=[initValue:expression] : [condition:expression] : [statement]) {<br> 
 * 		// code lock to be executed<br>
 * }<br>
 * </code>
 * 
 * 
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(ExpressionIterationAction.ExpressionIterationActionImpl.class)
@XMLElement
public interface ExpressionIterationAction extends AbstractIterationAction {

	@PropertyIdentifier(type = Type.class)
	public static final String DECLARED_TYPE_KEY = "declaredType";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String INIT_EXPRESSION_KEY = "initExpression";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONDITION_EXPRESSION_KEY = "conditionExpression";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String STATEMENT_EXPRESSION_KEY = "statementExpression";

	@FMLMigration("ignoreForEquality=true to be removed")
	@Getter(value = DECLARED_TYPE_KEY, isStringConvertable = true, ignoreForEquality = true)
	@XMLAttribute
	public Type getDeclaredType();

	@Setter(DECLARED_TYPE_KEY)
	public void setDeclaredType(Type type);

	/**
	 * We define an updater for DECLARED_TYPE property because we need to translate supplied Type to valid TypingSpace
	 * 
	 * @param type
	 */
	@Updater(DECLARED_TYPE_KEY)
	public void updateDeclaredType(Type type);

	/**
	 * Returns the control graph to be executed after each iteration
	 * 
	 * @return
	 */
	@Getter(value = STATEMENT_EXPRESSION_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement(context = "StatementExpression_")
	@Embedded
	public FMLControlGraph getStatementExpression();

	@Setter(STATEMENT_EXPRESSION_KEY)
	public void setStatementExpression(FMLControlGraph statementExpression);

	@Getter(value = INIT_EXPRESSION_KEY)
	@XMLAttribute
	public DataBinding<?> getInitExpression();

	@Setter(INIT_EXPRESSION_KEY)
	public void setInitExpression(DataBinding<?> initExpression);

	@Getter(value = CONDITION_EXPRESSION_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getConditionExpression();

	@Setter(CONDITION_EXPRESSION_KEY)
	public void setConditionExpression(DataBinding<Boolean> conditionExpression);

	public static abstract class ExpressionIterationActionImpl extends AbstractIterationActionImpl implements ExpressionIterationAction {

		private static final Logger logger = Logger.getLogger(ExpressionIterationAction.class.getPackage().getName());

		private DataBinding<?> initExpression;
		private DataBinding<Boolean> conditionExpression;

		/**
		 * We define an updater for DECLARED_TYPE property because we need to translate supplied Type to valid TypingSpace
		 * 
		 * This updater is called during updateWith() processing (generally applied during the FML parsing phases)
		 * 
		 * @param type
		 */
		@Override
		public void updateDeclaredType(Type type) {

			if (getDeclaringCompilationUnit() != null && type instanceof CustomType) {
				setDeclaredType(((CustomType) type).translateTo(getDeclaringCompilationUnit().getTypingSpace()));
			}
			else {
				setDeclaredType(type);
			}
		}

		@Override
		public Type getItemType() {
			if (getDeclaredType() != null) {
				return getDeclaredType();
			}
			return Object.class;
		}

		@Override
		public DataBinding<?> getInitExpression() {
			if (initExpression == null) {
				initExpression = new DataBinding<>(this, Object.class, BindingDefinitionType.GET);
				initExpression.setBindingName(INIT_EXPRESSION_KEY);
			}
			return initExpression;
		}

		@Override
		public void setInitExpression(DataBinding<?> initExpression) {
			if (initExpression != null) {
				initExpression.setOwner(this);
				initExpression.setBindingName(INIT_EXPRESSION_KEY);
				initExpression.setDeclaredType(Object.class);
				initExpression.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.initExpression = initExpression;
		}

		@Override
		public DataBinding<Boolean> getConditionExpression() {
			if (conditionExpression == null) {
				conditionExpression = new DataBinding<>(conditionBindable, Boolean.class, DataBinding.BindingDefinitionType.GET);
				conditionExpression.setBindingName(CONDITION_EXPRESSION_KEY);
			}
			return conditionExpression;
		}

		@Override
		public void setConditionExpression(DataBinding<Boolean> conditionExpression) {
			if (conditionExpression != null) {
				conditionExpression.setOwner(conditionBindable);
				conditionExpression.setDeclaredType(Boolean.class);
				conditionExpression.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				conditionExpression.setBindingName(CONDITION_EXPRESSION_KEY);
			}
			this.conditionExpression = conditionExpression;
		}

		public Object evaluateInitExpression(BindingEvaluationContext evaluationContext) {
			DataBinding<?> initExpression = getInitExpression();
			if (initExpression.isSet() && initExpression.isValid()) {
				try {
					return initExpression.getBindingValue(evaluationContext);
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

		public boolean evaluateCondition(BindingEvaluationContext evaluationContext) {
			DataBinding<Boolean> conditionExpression = getConditionExpression();
			// System.out.println("conditionExpression=" + conditionExpression);
			// System.out.println("valid=" + conditionExpression.isValid() + " reason: " + conditionExpression.invalidBindingReason());
			if (conditionExpression.isSet() && conditionExpression.isValid()) {
				try {
					Boolean returned = conditionExpression.getBindingValue(evaluationContext);
					if (returned == null) {
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
			return false;
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException {

			// System.out.println("Execute iteration");

			// Initialize iterator
			Object initValue = evaluateInitExpression(evaluationContext);
			evaluationContext.declareVariable(getIteratorName(), initValue);

			while (evaluateCondition(evaluationContext)) {

				// Evaluate CG
				try {
					getControlGraph().execute(evaluationContext);
				} catch (ReturnException e) {
					evaluationContext.dereferenceVariable(getIteratorName());
					throw e;
				}

				// Evaluate statement expression
				getStatementExpression().execute(evaluationContext);

			}

			evaluationContext.dereferenceVariable(getIteratorName());

			return null;
		}

		@Override
		public String getStringRepresentation() {
			// NPE Protection when action has been deleted e.g.
			if (getStatementExpression() != null) {
				return getHeaderContext() + " for (" + TypeUtils.simpleRepresentation(getItemType()) + " " + getIteratorName() + "="
						+ getInitExpression() + " ; " + getConditionExpression() + " ; "
						+ getStatementExpression().getStringRepresentation() + ")";
			}
			return "NULL ITERATION ACTION";
		}

		@Override
		public void reduce() {
			super.reduce();
			if (getStatementExpression() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getStatementExpression()).reduce();
			}
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (STATEMENT_EXPRESSION_KEY.equals(ownerContext)) {
				return getStatementExpression();
			}
			return super.getControlGraph(ownerContext);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (STATEMENT_EXPRESSION_KEY.equals(ownerContext)) {
				setStatementExpression(controlGraph);
			}
			else {
				super.setControlGraph(controlGraph, ownerContext);
			}
		}

		@Override
		public BindingModel getBaseBindingModel(FMLControlGraph controlGraph) {
			if (controlGraph == getControlGraph()) {
				return getInferedBindingModel();
			}
			else if (controlGraph == getStatementExpression()) {
				return getInferedBindingModel();
			}
			// logger.warning("Unexpected control graph: " + controlGraph);
			return null;
		}

		@Override
		public void setOwner(FMLControlGraphOwner owner) {
			super.setOwner(owner);
			if (getStatementExpression() != null) {
				getStatementExpression().getBindingModel().setBaseBindingModel(getBaseBindingModel(getStatementExpression()));
			}
		}

		@Override
		public void accept(FMLControlGraphVisitor visitor) {
			super.accept(visitor);
			if (getStatementExpression() != null) {
				getStatementExpression().accept(visitor);
			}
		}

		private ConditionBindable conditionBindable = new ConditionBindable();

		class ConditionBindable extends PropertyChangedSupportDefaultImplementation implements Bindable {

			@Override
			public BindingModel getBindingModel() {
				return getInferedBindingModel();
			}

			@Override
			public BindingFactory getBindingFactory() {
				return ExpressionIterationActionImpl.this.getBindingFactory();
			}

			@Override
			public void notifiedBindingChanged(DataBinding<?> dataBinding) {
				// TODO
			}

			@Override
			public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
				// TODO
			}

		}

	}

	@DefineValidationRule
	public static class InitExpressionBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<ExpressionIterationAction> {
		public InitExpressionBindingIsRequiredAndMustBeValid() {
			super("'init_expression'_binding_is_not_valid", ExpressionIterationAction.class);
		}

		@Override
		public DataBinding<?> getBinding(ExpressionIterationAction object) {
			return object.getInitExpression();
		}
	}

	@DefineValidationRule
	public static class ConditionExpressionBindingIsRequiredAndMustBeValid
			extends BindingIsRequiredAndMustBeValid<ExpressionIterationAction> {
		public ConditionExpressionBindingIsRequiredAndMustBeValid() {
			super("'condition_expression'_binding_is_not_valid", ExpressionIterationAction.class);
		}

		@Override
		public DataBinding<?> getBinding(ExpressionIterationAction object) {
			return object.getConditionExpression();
		}
	}

	@DefineValidationRule
	public static class ExpressionIterationActionMustDefineAStatementExpression
			extends ValidationRule<ExpressionIterationActionMustDefineAStatementExpression, ExpressionIterationAction> {
		public ExpressionIterationActionMustDefineAStatementExpression() {
			super(ExpressionIterationAction.class, "iteration_action_must_define_a_valid_statement_expression");
		}

		@Override
		public ValidationIssue<ExpressionIterationActionMustDefineAStatementExpression, ExpressionIterationAction> applyValidation(
				ExpressionIterationAction action) {
			if (action.getStatementExpression() == null) {
				return new ValidationError<>(this, action, "iteration_action_does_not_define_a_valid_statement_expression");
			}

			return null;
		}

	}

}
