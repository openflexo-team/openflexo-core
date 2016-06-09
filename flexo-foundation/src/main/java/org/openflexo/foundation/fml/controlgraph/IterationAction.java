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
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext.ReturnException;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.toolbox.StringUtils;

@ModelEntity
@ImplementationClass(IterationAction.IterationActionImpl.class)
@XMLElement
public interface IterationAction extends AbstractIterationAction {

	@Deprecated
	@PropertyIdentifier(type = DataBinding.class)
	public static final String ITERATION_KEY = "iteration";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String ITERATION_CONTROL_GRAPH_KEY = "iterationControlGraph";

	@Deprecated
	@Getter(value = ITERATION_KEY)
	@XMLAttribute
	public DataBinding<List<?>> getIteration();

	@Deprecated
	@Setter(ITERATION_KEY)
	public void setIteration(DataBinding<List<?>> iteration);

	@Getter(value = ITERATION_CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@XMLElement(context = "Iteration_")
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

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("for (" + getIteratorName() + " : "
					+ (getIterationAction() != null ? getIterationAction().getFMLRepresentation() : "null"), context);
			out.append(") {", context);
			out.append(StringUtils.LINE_SEPARATOR, context);
			if (getControlGraph() != null) {
				out.append(getControlGraph().getFMLRepresentation(context), context, 1);
				out.append(StringUtils.LINE_SEPARATOR, context);
			}
			out.append("}", context);
			return out.toString();
		}

		private DataBinding<List<?>> iteration;

		@Deprecated
		@Override
		public DataBinding<List<?>> getIteration() {
			if (iteration == null) {
				iteration = new DataBinding<List<?>>(this, List.class, BindingDefinitionType.GET);
			}
			return iteration;
		}

		@Deprecated
		@Override
		public void setIteration(DataBinding<List<?>> iteration) {
			if (iteration != null) {
				iteration.setOwner(this);
				iteration.setBindingName("iteration");
				iteration.setDeclaredType(List.class);
				iteration.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.iteration = iteration;
			// rebuildInferedBindingModel();
		}

		@Override
		public Type getItemType() {
			if (getIterationAction() != null) {
				return getIterationAction().getIteratorType();
			}
			return Object.class;
		}

		public List<?> evaluateIteration(RunTimeEvaluationContext evaluationContext) throws FlexoException {
			try {
				return getIterationAction().execute(evaluationContext);
			} catch (ReturnException e) {
				return (List<?>) e.getReturnedValue();
			}
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FlexoException {

			List<?> items = evaluateIteration(evaluationContext);
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
			else
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
			logger.warning("Unexpected control graph: " + controlGraph);
			return null;
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			if (dataBinding == getIteration()) {
				getPropertyChangeSupport().firePropertyChange(ITERATION_KEY, null, getIteration());
			}
			super.notifiedBindingChanged(dataBinding);
		}

		@Override
		public void setOwner(FMLControlGraphOwner owner) {
			super.setOwner(owner);
			if (getIterationAction() != null) {
				getIterationAction().getBindingModel().setBaseBindingModel(getBaseBindingModel(getIterationAction()));
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
				return new ValidationError<IterationActionMustDefineAValidIteration, IterationAction>(this, action,
						"iteration_action_does_not_define_a_valid_iteration");
			}
			if (!action.getIterationAction().isIterable()) {
				return new ValidationError<IterationActionMustDefineAValidIteration, IterationAction>(this, action,
						"iteration_action_is_not_iterable");
			}

			return null;
		}

	}

}
