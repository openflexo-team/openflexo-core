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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
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
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

@ModelEntity
@ImplementationClass(IterationAction.IterationActionImpl.class)
@XMLElement
public interface IterationAction extends AbstractIterationAction {
	@PropertyIdentifier(type = DataBinding.class)
	public static final String ITERATION_CONTROL_GRAPH_KEY = "iterationControlGraph";

	@Getter(value = ITERATION_CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@XMLElement(context = "Iteration_")
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public AssignableAction<? extends List<?>> getIterationAction();

	@Setter(ITERATION_CONTROL_GRAPH_KEY)
	public void setIterationAction(AssignableAction<? extends List<?>> iterationAction);

	@Override
	@Getter(value = ITERATOR_NAME_KEY)
	@XMLAttribute
	public String getIteratorName();

	public static abstract class IterationActionImpl extends AbstractIterationActionImpl implements IterationAction {

		private static final Logger logger = Logger.getLogger(IterationAction.class.getPackage().getName());

		private DataBinding<List<?>> iteration;

		@Override
		public Type getItemType() {
			if (getIterationAction() != null) {
				return getIterationAction().getIteratorType();
			}
			return Object.class;
		}

		public List<?> evaluateIteration(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
			try {
				return getIterationAction().execute(evaluationContext);
			} catch (ReturnException e) {
				return (List<?>) e.getReturnedValue();
			}
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException {

			// System.out.println("Execute iteration");
			// System.out.println("InferedBM=" + getInferedBindingModel());
			// IterationActionBindingVariable bv = ((IterationActionBindingModel) getInferedBindingModel()).getIteratorBindingVariable();
			// System.out.println("bv=" + bv + " type=" + bv.getType());
			List<?> items = evaluateIteration(evaluationContext);
			// System.out.println("items=" + items);
			if (items != null) {
				for (Object item : new ArrayList<Object>(items)) {
					// System.out.println("> working with " + getIteratorName() + "=" + item);
					evaluationContext.declareVariable(getIteratorName(), item);
					try {
						getControlGraph().execute(evaluationContext);
					} catch (ReturnException e) {
						evaluationContext.dereferenceVariable(getIteratorName());
						throw e;
					}
				}
				evaluationContext.dereferenceVariable(getIteratorName());
			}
			return null;
		}

		@Override
		public String getStringRepresentation() {
			// NPE Protection when action has been deleted e.g.
			if (getIterationAction() != null) {
				return getHeaderContext() + " for (" + getIteratorName() + " : " + getIterationAction().getStringRepresentation() + ")";
			}
			return "NULL ITERATION ACTION";
		}

		@Override
		public void reduce() {
			super.reduce();
			if (getIterationAction() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getIterationAction()).reduce();
			}
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (ITERATION_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getIterationAction();
			}
			return super.getControlGraph(ownerContext);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (ITERATION_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setIterationAction((AssignableAction<List<?>>) controlGraph);
			}
			else {
				super.setControlGraph(controlGraph, ownerContext);
			}
		}

		@Override
		public void setIterationAction(AssignableAction<? extends List<?>> iterationControlGraph) {
			if (iterationControlGraph != null) {
				iterationControlGraph.setOwner(this);
				iterationControlGraph.setOwnerContext(ITERATION_CONTROL_GRAPH_KEY);
			}
			performSuperSetter(ITERATION_CONTROL_GRAPH_KEY, iterationControlGraph);
		}

		@Override
		public BindingModel getBaseBindingModel(FMLControlGraph controlGraph) {
			if (controlGraph == getControlGraph()) {
				return getInferedBindingModel();
			}
			else if (controlGraph == getIterationAction()) {
				return getBindingModel();
			}
			// logger.warning("Unexpected control graph: " + controlGraph);
			return null;
		}

		@Override
		public void setOwner(FMLControlGraphOwner owner) {
			super.setOwner(owner);
			if (getIterationAction() != null) {
				getIterationAction().getBindingModel().setBaseBindingModel(getBaseBindingModel(getIterationAction()));
			}
		}

		@Override
		public void accept(FMLControlGraphVisitor visitor) {
			super.accept(visitor);
			if (getIterationAction() != null) {
				getIterationAction().accept(visitor);
			}
		}

		@Override
		public void handleRequiredImports(FMLCompilationUnit compilationUnit) {
			super.handleRequiredImports(compilationUnit);
			if (compilationUnit != null) {
				compilationUnit.ensureJavaImportForType(getItemType());
			}
		}

	}

	@DefineValidationRule
	public static class IterationActionMustDefineAValidIteration
			extends ValidationRule<IterationActionMustDefineAValidIteration, IterationAction> {
		public IterationActionMustDefineAValidIteration() {
			super(IterationAction.class, "iteration_action_must_define_a_valid_iteration");
		}

		@Override
		public ValidationIssue<IterationActionMustDefineAValidIteration, IterationAction> applyValidation(IterationAction action) {
			if (action.getIterationAction() == null) {
				return new ValidationError<>(this, action, "iteration_action_does_not_define_a_valid_iteration");
			}
			if (!action.getIterationAction().isIterable()) {
				/*System.out.println("iteration_action_is_not_iterable a cause de ");
				System.out.println("action=" + action.getIterationAction());
				System.out.println("FML=" + action.getIterationAction());
				if (action.getIterationAction() instanceof ExpressionAction) {
					ExpressionAction exp = (ExpressionAction) action.getIterationAction();
					System.out.println("exp " + exp.getExpression());
					System.out.println("valide? " + exp.getExpression().isValid());
					System.out.println("reason " + exp.getExpression().invalidBindingReason());
				}*/
				return new ValidationError<>(this, action, "iteration_action_is_not_iterable");
			}

			return null;
		}

	}

}
