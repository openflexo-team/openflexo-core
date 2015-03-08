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
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.binding.IterationActionBindingModel;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
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
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.toolbox.StringUtils;

@FIBPanel("Fib/FML/IterationActionPanel.fib")
@ModelEntity
@ImplementationClass(IterationAction.IterationActionImpl.class)
@XMLElement
public interface IterationAction extends ControlStructureAction, FMLControlGraphOwner {

	@Deprecated
	@PropertyIdentifier(type = DataBinding.class)
	public static final String ITERATION_KEY = "iteration";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String ITERATION_CONTROL_GRAPH_KEY = "iterationControlGraph";
	@PropertyIdentifier(type = String.class)
	public static final String ITERATOR_NAME_KEY = "iteratorName";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String CONTROL_GRAPH_KEY = "controlGraph";

	@Deprecated
	@Getter(value = ITERATION_KEY)
	@XMLAttribute
	public DataBinding<List<?>> getIteration();

	@Deprecated
	@Setter(ITERATION_KEY)
	public void setIteration(DataBinding<List<?>> iteration);

	@Getter(value = ITERATION_CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@XMLElement(context = "Iteration_")
	public AssignableAction<? extends List<?>> getIterationAction();

	@Setter(ITERATION_CONTROL_GRAPH_KEY)
	public void setIterationAction(AssignableAction<? extends List<?>> iterationAction);

	@Getter(value = ITERATOR_NAME_KEY)
	@XMLAttribute
	public String getIteratorName();

	@Setter(ITERATOR_NAME_KEY)
	public void setIteratorName(String iteratorName);

	public Type getItemType();

	/**
	 * Returns the control graph on which we iterate
	 * 
	 * @return
	 */
	@Getter(value = CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	@XMLElement(context = "ControlGraph_")
	@Embedded
	public FMLControlGraph getControlGraph();

	@Setter(CONTROL_GRAPH_KEY)
	public void setControlGraph(FMLControlGraph aControlGraph);

	public static abstract class IterationActionImpl extends ControlStructureActionImpl implements IterationAction {

		private static final Logger logger = Logger.getLogger(IterationAction.class.getPackage().getName());

		private String iteratorName = "item";

		public IterationActionImpl() {
			super();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("for (" + getIteratorName() + " : "
					+ (getIterationAction() != null ? getIterationAction().getFMLRepresentation() : "null"), context);
			out.append(") {", context);
			out.append(StringUtils.LINE_SEPARATOR, context);
			out.append(getControlGraph().getFMLRepresentation(context), context, 1);
			out.append(StringUtils.LINE_SEPARATOR, context);
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

		/*@Override
		public void notifiedBindingChanged(DataBinding<?> binding) {
			super.notifiedBindingChanged(binding);
			if (binding == iteration) {
				rebuildInferedBindingModel();
			}
		}*/

		@Override
		public String getIteratorName() {
			return iteratorName;
		}

		@Override
		public void setIteratorName(String iteratorName) {
			if (!this.iteratorName.equals(iteratorName)) {
				String oldValue = this.iteratorName;
				this.iteratorName = iteratorName;
				// rebuildInferedBindingModel();
				getPropertyChangeSupport().firePropertyChange(ITERATOR_NAME_KEY, oldValue, iteratorName);
			}
		}

		@Override
		public Type getItemType() {
			/*if (iteration != null && iteration.isSet()) {
				Type accessedType = iteration.getAnalyzedType();
				if (accessedType instanceof ParameterizedType && ((ParameterizedType) accessedType).getActualTypeArguments().length > 0) {
					return ((ParameterizedType) accessedType).getActualTypeArguments()[0];
				}
			}*/
			if (getIterationAction() != null) {
				return getIterationAction().getIteratorType();
			}
			return Object.class;
		}

		/*@Override
		protected BindingModel buildInferedBindingModel() {
			BindingModel returned = super.buildInferedBindingModel();
			returned.addToBindingVariables(new BindingVariable(getIteratorName(), getItemType()) {
				@Override
				public Object getBindingValue(Object target, BindingEvaluationContext context) {
					logger.info("What should i return for " + getIteratorName() + " ? target " + target + " context=" + context);
					return super.getBindingValue(target, context);
				}

				@Override
				public Type getType() {
					return getItemType();
				}
			});
			return returned;
		}*/

		public List<?> evaluateIteration(FlexoBehaviourAction<?, ?, ?> action) throws FlexoException {
			return getIterationAction().execute(action);
			/*if (getIteration().isValid()) {
				try {
					return getIteration().getBindingValue(action);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			return null;*/
		}

		@Override
		public Object execute(FlexoBehaviourAction action) throws FlexoException {
			List<?> items = evaluateIteration(action);
			if (items != null) {
				for (Object item : items) {
					// System.out.println("> working with " + getIteratorName() + "=" + item);
					action.declareVariable(getIteratorName(), item);
					getControlGraph().execute(action);
				}
			}
			action.dereferenceVariable(getIteratorName());
			return null;
		}

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + " for (" + getIteratorName() + " : " + getIterationAction().getStringRepresentation() + ")";
		}

		@Override
		protected IterationActionBindingModel makeInferedBindingModel() {
			return new IterationActionBindingModel(this);
		}

		@Deprecated
		@Override
		public void addToActions(EditionAction anAction) {
			FMLControlGraphConverter.addToActions(this, CONTROL_GRAPH_KEY, anAction);
		}

		@Deprecated
		@Override
		public void removeFromActions(EditionAction anAction) {
			FMLControlGraphConverter.removeFromActions(this, CONTROL_GRAPH_KEY, anAction);
		}

		@Override
		public void reduce() {
			if (getControlGraph() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getControlGraph()).reduce();
			}
			if (getIterationAction() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getIterationAction()).reduce();
			}
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getControlGraph();
			} else if (ITERATION_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getIterationAction();
			}
			return null;
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setControlGraph(controlGraph);
			} else if (ITERATION_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setIterationAction((AssignableAction<List<?>>) controlGraph);
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
				// return getControlGraph().getBindingModel();
			} else if (controlGraph == getIterationAction()) {
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
			performSuperSetter(OWNER_KEY, owner);
			if (getControlGraph() != null) {
				getControlGraph().getBindingModel().setBaseBindingModel(getBaseBindingModel(getControlGraph()));
			}
			if (getIterationAction() != null) {
				getIterationAction().getBindingModel().setBaseBindingModel(getBaseBindingModel(getIterationAction()));
			}
		}

	}

	@DefineValidationRule
	public static class IterationActionMustDefineAValidIteration extends
			ValidationRule<IterationActionMustDefineAValidIteration, IterationAction> {
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
