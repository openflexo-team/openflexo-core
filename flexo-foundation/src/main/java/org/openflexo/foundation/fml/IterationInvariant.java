/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.fml;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.binding.IterationInvariantBindingModel;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * An {@link IterationInvariant} represents a structural constraint attached to an FlexoConcept
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(IterationInvariant.IterationInvariantImpl.class)
public interface IterationInvariant extends AbstractInvariant {

	@PropertyIdentifier(type = String.class)
	public static final String ITERATOR_NAME_KEY = "iteratorName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String ITERATION_KEY = "iteration";
	@PropertyIdentifier(type = SimpleInvariant.class, cardinality = Cardinality.LIST)
	public static final String SIMPLE_INVARIANTS_KEY = "invariants";

	@Getter(value = ITERATOR_NAME_KEY)
	@XMLAttribute
	public String getIteratorName();

	@Setter(ITERATOR_NAME_KEY)
	public void setIteratorName(String iteratorName);

	public Type getIteratorType();

	@Getter(value = ITERATION_KEY)
	@XMLAttribute
	public DataBinding<List> getIteration();

	@Setter(ITERATION_KEY)
	public void setIteration(DataBinding<List> iteration);

	@Getter(value = SIMPLE_INVARIANTS_KEY, cardinality = Cardinality.LIST, inverse = SimpleInvariant.PARENT_ITERATION_INVARIANT_KEY)
	@XMLElement
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<SimpleInvariant> getSimpleInvariants();

	@Setter(SIMPLE_INVARIANTS_KEY)
	public void setSimpleInvariants(List<SimpleInvariant> invariants);

	@Adder(SIMPLE_INVARIANTS_KEY)
	@PastingPoint
	public void addToSimpleInvariants(SimpleInvariant anInvariant);

	@Remover(SIMPLE_INVARIANTS_KEY)
	public void removeFromSimpleInvariants(SimpleInvariant anInvariant);

	@Reindexer(SIMPLE_INVARIANTS_KEY)
	public void moveSimpleInvariantToIndex(SimpleInvariant constraint, int index);

	public IterationInvariantBindingModel getIterationInvariantBindingModel();

	public static abstract class IterationInvariantImpl extends AbstractInvariantImpl implements IterationInvariant {

		protected static final Logger logger = FlexoLogger.getLogger(IterationInvariant.class.getPackage().getName());

		private DataBinding<List> iteration;

		private IterationInvariantBindingModel iterationInvariantBindingModel = null;

		@Override
		public IterationInvariantBindingModel getIterationInvariantBindingModel() {
			if (iterationInvariantBindingModel == null) {
				iterationInvariantBindingModel = new IterationInvariantBindingModel(this);
			}
			return iterationInvariantBindingModel;
		}

		@Override
		public DataBinding<List> getIteration() {
			if (iteration == null) {
				iteration = new DataBinding<>(this, List.class, BindingDefinitionType.GET);
				iteration.setBindingName("iteration");
			}
			return iteration;
		}

		@Override
		public void setIteration(DataBinding<List> iteration) {
			if (iteration != null) {
				iteration.setOwner(this);
				iteration.setBindingName("iteration");
				iteration.setDeclaredType(List.class);
				iteration.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.iteration = iteration;
		}

		@Override
		public Type getIteratorType() {
			if (getIteration().isSet() && getIteration().isValid()) {
				Type iterationType = getIteration().getAnalyzedType();
				if (iterationType instanceof ParameterizedType) {
					return TypeUtils.getTypeArgument(iterationType, List.class, 0);
				}
			}
			return Object.class;
		}

	}
}
