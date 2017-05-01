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
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.binding.FetchRequestIterationActionBindingModel;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext.ReturnException;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
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
@ImplementationClass(FetchRequestIterationAction.FetchRequestIterationActionImpl.class)
@XMLElement
@Deprecated
public interface FetchRequestIterationAction extends ControlStructureAction, FMLControlGraphOwner {

	@PropertyIdentifier(type = String.class)
	public static final String ITERATOR_NAME_KEY = "iteratorName";
	@PropertyIdentifier(type = FetchRequest.class)
	public static final String FETCH_REQUEST_KEY = "fetchRequest";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String CONTROL_GRAPH_KEY = "controlGraph";

	@Getter(value = ITERATOR_NAME_KEY)
	@XMLAttribute
	public String getIteratorName();

	@Setter(ITERATOR_NAME_KEY)
	public void setIteratorName(String iteratorName);

	@Getter(value = FETCH_REQUEST_KEY)
	@XMLElement(context = "FetchRequest_")
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public FetchRequest<?, ?, ?> getFetchRequest();

	@Setter(FETCH_REQUEST_KEY)
	public void setFetchRequest(FetchRequest<?, ?, ?> fetchRequest);

	public Type getItemType();

	@Getter(value = CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	@XMLElement(context = "IterationControlGraph_")
	@Embedded
	public FMLControlGraph getControlGraph();

	@Setter(CONTROL_GRAPH_KEY)
	public void setControlGraph(FMLControlGraph aControlGraph);

	public static abstract class FetchRequestIterationActionImpl extends ControlStructureActionImpl implements FetchRequestIterationAction {

		private static final Logger logger = Logger.getLogger(FetchRequestIterationAction.class.getPackage().getName());

		private String iteratorName = "item";

		// private FetchRequest fetchRequest;

		public FetchRequestIterationActionImpl() {
			super();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("for (" + getIteratorName() + " in (", context);
			out.append(getFetchRequest() != null ? getFetchRequest().getFMLRepresentation() : "Null fetch request", context);
			out.append(")) {", context);
			out.append(StringUtils.LINE_SEPARATOR, context);
			if (getControlGraph() != null) {
				out.append(getControlGraph().getFMLRepresentation(context), context, 1);
				out.append(StringUtils.LINE_SEPARATOR, context);
			}
			out.append("}", context);
			return out.toString();
		}

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
		public void setFetchRequest(FetchRequest<?, ?, ?> fetchRequest) {
			performSuperSetter(FETCH_REQUEST_KEY, fetchRequest);
			if (fetchRequest != null) {
				fetchRequest.setActionContainer(this);
				fetchRequest.setEmbeddingIteration(this);
			}
			else {
				logger.warning("INVESTIGATE : Setting a Null FetchRequest");
			}
		}

		@Override
		public Type getItemType() {
			if (getFetchRequest() != null && getFetchRequest().getFetchedType() != null) {
				return getFetchRequest().getFetchedType();
			}
			return Object.class;
		}

		/*@Override
		protected BindingModel buildInferedBindingModel() {
			BindingModel returned = super.buildInferedBindingModel();
			returned.addToBindingVariables(new BindingVariable(getIteratorName(), getItemType()) {
				@Override
				public Object getBindingValue(Object target, RunTimeEvaluationContext context) {
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

		private List<?> fetchItems(RunTimeEvaluationContext evaluationContext) throws FlexoException {
			if (getFetchRequest() != null) {
				try {
					return getFetchRequest().execute(evaluationContext);
				} catch (ReturnException e) {
					e.printStackTrace();
				}
			}
			return Collections.emptyList();
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) throws FlexoException {
			List<?> items = fetchItems(evaluationContext);
			if (items != null) {
				for (Object item : items) {
					evaluationContext.declareVariable(getIteratorName(), item);
					try {
						getControlGraph().execute(evaluationContext);
					} catch (ReturnException e) {
						evaluationContext.dereferenceVariable(getIteratorName());
						return e.getReturnedValue();
					}
					// performBatchOfActions(getActions(), action);
				}
			}
			evaluationContext.dereferenceVariable(getIteratorName());
			return null;
		}

		/*@Override
		public void addToActions(EditionAction action) {
			// Big hack to prevent XMLCoDe to also append FetchRequest to the list of embedded actions
			// Should be removed either by the fixing of XMLCoDe or by the switch to PAMELA
			if (getFetchRequest() != action) {
				super.addToActions(action);
			}
		}*/

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
			/*if (getIterationAction() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getIterationAction()).reduce();
			}*/
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getControlGraph();
			} /*else if (ITERATION_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getIterationAction();
				}*/
			return null;
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setControlGraph(controlGraph);
			} /*else if (ITERATION_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setIterationAction((AssignableAction<List<?>>) controlGraph);
				}*/
		}

		@Override
		public void setControlGraph(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(CONTROL_GRAPH_KEY);
			}
			performSuperSetter(CONTROL_GRAPH_KEY, aControlGraph);
		}

		@Override
		public String getStringRepresentation() {
			if (getFetchRequest() != null) {
				return getIteratorName() + " : " + getFetchRequest().getStringRepresentation();
			}
			return super.getStringRepresentation();
		}

		@Override
		protected FetchRequestIterationActionBindingModel makeInferedBindingModel() {
			return new FetchRequestIterationActionBindingModel(this);
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
		public void setOwner(FMLControlGraphOwner owner) {
			super.setOwner(owner);
			if (getControlGraph() != null) {
				getControlGraph().getBindingModel().setBaseBindingModel(getBaseBindingModel(getControlGraph()));
			}
			/*if (getIterationAction() != null) {
				getIterationAction().getBindingModel().setBaseBindingModel(getBaseBindingModel(getIterationAction()));
			}*/
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
}
