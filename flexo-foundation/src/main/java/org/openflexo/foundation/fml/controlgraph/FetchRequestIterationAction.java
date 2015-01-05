/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.fml.controlgraph;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.annotations.FIBPanel;
import org.openflexo.foundation.fml.binding.FetchRequestIterationActionBindingModel;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
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

@FIBPanel("Fib/FML/FetchRequestIterationActionPanel.fib")
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
	public FetchRequest<?, ?> getFetchRequest();

	@Setter(FETCH_REQUEST_KEY)
	public void setFetchRequest(FetchRequest<?, ?> fetchRequest);

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
			for (EditionAction action : getActions()) {
				out.append(action.getFMLRepresentation(context), context, 1);
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
		public void setFetchRequest(FetchRequest<?, ?> fetchRequest) {
			performSuperSetter(FETCH_REQUEST_KEY, fetchRequest);
			if (fetchRequest != null) {
				fetchRequest.setActionContainer(this);
				fetchRequest.setEmbeddingIteration(this);
			} else {
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

		private List<?> fetchItems(FlexoBehaviourAction action) {
			if (getFetchRequest() != null) {
				return getFetchRequest().performAction(action);
			}
			return Collections.emptyList();
		}

		@Override
		public Object performAction(FlexoBehaviourAction action) {
			List<?> items = fetchItems(action);
			if (items != null) {
				for (Object item : items) {
					action.declareVariable(getIteratorName(), item);
					performBatchOfActions(getActions(), action);
				}
			}
			action.dereferenceVariable(getIteratorName());
			return null;
		}

		/*@Override
		public void addToActions(EditionAction<?, ?> action) {
			// Big hack to prevent XMLCoDe to also append FetchRequest to the list of embedded actions
			// Should be removed either by the fixing of XMLCoDe or by the switch to PAMELA
			if (getFetchRequest() != action) {
				super.addToActions(action);
			}
		}*/

		@Override
		public void addToActions(EditionAction<?, ?> action) {
			if (getFetchRequest() != action) {
				performSuperAdder(ACTIONS_KEY, action);
			}

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

		/*@Override
		protected FetchRequestIterationActionBindingModel makeBindingModel() {
			return new FetchRequestIterationActionBindingModel(this);
		}*/

	}
}
