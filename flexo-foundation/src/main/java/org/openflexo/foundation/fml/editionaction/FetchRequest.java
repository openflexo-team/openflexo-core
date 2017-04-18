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

package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.controlgraph.FetchRequestIterationAction;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;

/**
 * Abstract class representing a fetch request, which is a primitive allowing to browse in the model while configuring requests
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FetchRequest.FetchRequestImpl.class)
public abstract interface FetchRequest<MS extends ModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<?>, T>
		extends TechnologySpecificAction<MS, RD, List<T>> {

	@PropertyIdentifier(type = Vector.class)
	public static final String CONDITIONS_KEY = "conditions";

	@Getter(value = CONDITIONS_KEY, cardinality = Cardinality.LIST, inverse = FetchRequestCondition.ACTION_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<FetchRequestCondition> getConditions();

	@Setter(CONDITIONS_KEY)
	public void setConditions(List<FetchRequestCondition> conditions);

	@Adder(CONDITIONS_KEY)
	public void addToConditions(FetchRequestCondition aCondition);

	@Remover(CONDITIONS_KEY)
	public void removeFromConditions(FetchRequestCondition aCondition);

	public FetchRequestCondition createCondition();

	public void deleteCondition(FetchRequestCondition aCondition);

	@Deprecated
	public FetchRequestIterationAction getEmbeddingIteration();

	@Deprecated
	public void setEmbeddingIteration(FetchRequestIterationAction embeddingIteration);

	public Type getFetchedType();

	public static abstract class FetchRequestImpl<MS extends ModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<?>, T>
			extends TechnologySpecificActionImpl<MS, RD, List<T>> implements FetchRequest<MS, RD, T> {

		private static final Logger logger = Logger.getLogger(FetchRequest.class.getPackage().getName());

		// null in fetch request is not embedded in an iteration
		@Deprecated
		private FetchRequestIterationAction embeddingIteration;

		/*@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			if (getAssignation().isSet()) {
				out.append(getAssignation().toString() + " = ", context);
			}
			out.append(getImplementedInterface().getSimpleName(), context);
			return out.toString();
		}*/

		protected String getWhereClausesFMLRepresentation(FMLRepresentationContext context) {
			if (getConditions().size() > 0) {
				StringBuffer sb = new StringBuffer();
				sb.append("where=");
				if (getConditions().size() > 1) {
					sb.append("(");
				}
				boolean isFirst = true;
				for (FetchRequestCondition c : getConditions()) {
					sb.append(c.getCondition().toString() + (isFirst ? "" : " and "));
				}
				if (getConditions().size() > 1) {
					sb.append(")");
				}
				return sb.toString();
			}
			return null;
		}

		@Override
		public Type getIteratorType() {
			return getFetchedType();
		}

		@Override
		public abstract Type getFetchedType();

		@Override
		public Type getAssignableType() {
			return new ParameterizedTypeImpl(List.class, getFetchedType());
		}

		@Override
		public FetchRequestCondition createCondition() {
			FetchRequestCondition newCondition = getFMLModelFactory().newFetchRequestCondition();
			addToConditions(newCondition);
			return newCondition;
		}

		@Override
		public void deleteCondition(FetchRequestCondition aCondition) {
			removeFromConditions(aCondition);
		}

		public List<T> filterWithConditions(List<T> fetchResult, final RunTimeEvaluationContext evaluationContext) {
			if (getConditions().size() == 0) {
				return fetchResult;
			}
			else {
				// System.out
				// .println("Filtering with " + getConditions() + " fetchResult=" + fetchResult + " evalContext=" + evaluationContext);

				/*if (getConditions().size() > 0) {
					System.out.println("condition " + getConditions().get(0).getCondition());
					System.out.println("evalContext=" + evaluationContext + " hash=" + Integer.toHexString(evaluationContext.hashCode()));
					System.out.println("Je dois evaluer ");
				}*/
				// if (true)
				// return fetchResult;
				List<T> returned = new ArrayList<T>();
				for (final T proposedFetchResult : fetchResult) {
					boolean takeIt = true;
					for (FetchRequestCondition condition : getConditions()) {
						if (!condition.evaluateCondition(proposedFetchResult, evaluationContext)) {
							takeIt = false;
							// System.out.println("I dismiss " + proposedFetchResult + " because of " + condition.getCondition() + " valid="
							// + condition.getCondition().isValid());
							break;
						}
					}
					if (takeIt) {
						returned.add(proposedFetchResult);
						// System.out.println("I take " + proposedFetchResult);
					}
					else {
					}
				}
				return returned;
			}
		}

		@Deprecated
		@Override
		public FetchRequestIterationAction getEmbeddingIteration() {
			return embeddingIteration;
		}

		@Deprecated
		@Override
		public void setEmbeddingIteration(FetchRequestIterationAction embeddingIteration) {
			if (this.embeddingIteration != embeddingIteration) {
				FetchRequestIterationAction oldValue = this.embeddingIteration;
				this.embeddingIteration = embeddingIteration;
				getPropertyChangeSupport().firePropertyChange("embeddingIteration", oldValue, embeddingIteration);
			}
		}

		@Override
		public String getParametersStringRepresentation() {
			return "(" + getWhereClausesFMLRepresentation(null) + ")";
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append((getReceiver().isValid() ? getReceiver().toString() + "." : "") + getTechnologyAdapterIdentifier() + "::"
					+ getImplementedInterface().getSimpleName()
					+ (getConditions().size() > 0 ? " " + getWhereClausesFMLRepresentation(context) : "()"), context);
			return out.toString();
		}

	}
}
