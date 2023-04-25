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

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * Abstract class representing a fetch request, which is a primitive allowing to browse in the model while configuring requests
 * 
 * Note the presence of isUnique property:
 * <ul>
 * <li>if isUnique set to false (default), manage a non-null list of values (might be empty)</li>
 * <li>if isUnique set to true, manage a single value (if none value were found, value is null, when many values match conditions, return
 * first found). Unicity must be guaranteed by business logic (semantics of build models).
 * </ul>
 * 
 * @author sylvain
 *
 * @param <MS>
 *            Type of model slot which contractualize access to a given technology resource on which this action applies
 * @param <R>
 *            Type of receiver on this action (the precise technology object on which this action apply)
 * @param <T>
 *            Type of fetched value
 * @param <AT>
 *            Type of assigned value, T if the FetchRequest is unique, or List<T> if the FetchRequest is multiple
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractFetchRequest.AbstractFetchRequestImpl.class)
public abstract interface AbstractFetchRequest<MS extends ModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<?>, T, AT>
		extends TechnologySpecificActionDefiningReceiver<MS, RD, AT> {

	@PropertyIdentifier(type = Vector.class)
	public static final String CONDITIONS_KEY = "conditions";

	@PropertyIdentifier(type = DataBinding.class)
	String RECEIVER_KEY = "receiver";

	// No more deprecated here, but part of AbstractFetchRequest
	@Override
	@Getter(value = RECEIVER_KEY, ignoreForEquality = true)
	DataBinding<RD> getReceiver();

	// No more deprecated here, but part of AbstractFetchRequest
	@Override
	@Setter(RECEIVER_KEY)
	void setReceiver(DataBinding<RD> receiver);

	@Getter(value = CONDITIONS_KEY, cardinality = Cardinality.LIST, inverse = FetchRequestCondition.ACTION_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	@FMLAttribute(value = CONDITIONS_KEY, required = false, description = "<html>condition of the request</html>")
	public List<FetchRequestCondition> getConditions();

	@Setter(CONDITIONS_KEY)
	public void setConditions(List<FetchRequestCondition> conditions);

	@Adder(CONDITIONS_KEY)
	public void addToConditions(FetchRequestCondition aCondition);

	@Remover(CONDITIONS_KEY)
	public void removeFromConditions(FetchRequestCondition aCondition);

	public FetchRequestCondition createCondition();

	public void deleteCondition(FetchRequestCondition aCondition);

	public Type getFetchedType();

	public void setFetchedType(Type type);

	public static abstract class AbstractFetchRequestImpl<MS extends ModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<?>, T, AT>
			extends TechnologySpecificActionDefiningReceiverImpl<MS, RD, AT> implements AbstractFetchRequest<MS, RD, T, AT> {

		private static final Logger logger = Logger.getLogger(AbstractFetchRequestImpl.class.getPackage().getName());

		private Type fetchedType;

		protected String getWhereClausesFMLRepresentation() {
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
		public Type getFetchedType() {
			return fetchedType;
		}

		@Override
		public void setFetchedType(Type type) {
			this.fetchedType = type;
		}

		@Override
		public final Type getAssignableType() {
			if (this instanceof UniqueFetchRequest) {
				return getFetchedType();
			}
			else {
				return new ParameterizedTypeImpl(List.class, getFetchedType());
			}
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
			List<T> returned = new ArrayList<>();
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

		@Override
		public String getParametersStringRepresentation() {
			return "(" + getWhereClausesFMLRepresentation() + ")";
		}

		@Override
		public final AT execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FMLExecutionException {
			List<T> computedValues = performExecute(evaluationContext);
			if (this instanceof UniqueFetchRequest) {
				if (computedValues.size() > 1) {
					logger.warning("More than one value found for a UNIQUE request, return first one: " + computedValues);
					return (AT) computedValues.get(0);
				}
				if (computedValues.size() == 1) {
					// Normal case
					return (AT) computedValues.get(0);
				}
				return null;
			}
			if (this instanceof FetchRequest) {
				return (AT) computedValues;
			}
			logger.warning("Unexpected AbstractFetchRequest " + this);
			throw new FMLExecutionException("Unexpected AbstractFetchRequest " + this);

		}

		protected abstract List<T> performExecute(RunTimeEvaluationContext evaluationContext);

	}
}
