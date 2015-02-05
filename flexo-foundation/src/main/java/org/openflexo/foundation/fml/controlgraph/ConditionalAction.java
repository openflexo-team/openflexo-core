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
import java.util.logging.Logger;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.annotations.FIBPanel;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
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
import org.openflexo.toolbox.StringUtils;

@FIBPanel("Fib/FML/ConditionalActionPanel.fib")
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

	public static abstract class ConditionalActionImpl extends ControlStructureActionImpl implements ConditionalAction {

		private static final Logger logger = Logger.getLogger(ConditionalAction.class.getPackage().getName());

		private DataBinding<Boolean> condition;

		public ConditionalActionImpl() {
			super();
		}

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
			} else if (ELSE_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getElseControlGraph();
			}
			return null;
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (THEN_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setThenControlGraph(controlGraph);
			} else if (ELSE_CONTROL_GRAPH_KEY.equals(ownerContext)) {
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
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("if " + getCondition().toString() + "", context);
			out.append(" {", context);
			out.append(StringUtils.LINE_SEPARATOR, context);
			out.append(getThenControlGraph().getFMLRepresentation(context), context, 1);
			out.append(StringUtils.LINE_SEPARATOR, context);
			out.append("}", context);
			if (getElseControlGraph() != null) {
				out.append(" else {", context);
				out.append(StringUtils.LINE_SEPARATOR, context);
				out.append(getElseControlGraph().getFMLRepresentation(context), context, 1);
				out.append(StringUtils.LINE_SEPARATOR, context);
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
		public boolean evaluateCondition(FlexoBehaviourAction action) {
			if (getCondition().isSet() && getCondition().isValid()) {
				try {
					DataBinding condition = getCondition();
					Boolean returned = getCondition().getBindingValue(action);
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
				} catch (InvocationTargetException e) {
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
		public Object execute(FlexoBehaviourAction<?, ?, ?> action) throws FlexoException {
			if (evaluateCondition(action)) {
				return getThenControlGraph().execute(action);
				// performBatchOfActions(getActions(), action);
			} else {
				if (getElseControlGraph() != null) {
					return getElseControlGraph().execute(action);
				}
			}
			return null;
		}

		@Deprecated
		@Override
		public void addToActions(EditionAction anAction) {
			FMLControlGraphConverter.addToActions(this, THEN_CONTROL_GRAPH_KEY, anAction);
		}

		@Deprecated
		@Override
		public void removeFromActions(EditionAction anAction) {
			FMLControlGraphConverter.removeFromActions(this, THEN_CONTROL_GRAPH_KEY, anAction);
		}

		/*@Deprecated
		@Override
		public void addToActions(EditionAction anAction) {
			FMLControlGraph controlGraph = getThenControlGraph();
			if (controlGraph == null) {
				// If control graph is null, action will be new new control graph
				setThenControlGraph(anAction);
			} else {
				// Otherwise, sequentially append action
				controlGraph.sequentiallyAppend(anAction);
			}
			// performSuperAdder(ACTIONS_KEY, anAction);
		}

		@Deprecated
		@Override
		public void removeFromActions(EditionAction anAction) {
			anAction.delete();
		}*/

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

}
