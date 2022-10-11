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

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.DefaultBindable;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.binding.ConstraintBindingModel;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

/**
 * An FlexoConceptConstraint represents a structural constraint attached to an FlexoConcept
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FlexoConceptConstraint.FlexoConceptConstraintImpl.class)
@XMLElement(xmlTag = "Constraint")
public interface FlexoConceptConstraint extends FlexoConceptObject {

	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String FLEXO_CONCEPT_KEY = "flexoConcept";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONSTRAINT_KEY = "constraint";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIOLATION_HOOK_KEY = "violationHook";
	@PropertyIdentifier(type = String.class)
	public static final String ITERATOR_NAME_KEY = "iteratorName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String ITERATION_KEY = "iteration";
	public static final String HAS_ITERATION_KEY = "hasIteration";

	@Getter(value = ITERATOR_NAME_KEY)
	@XMLAttribute
	public String getIteratorName();

	@Setter(ITERATOR_NAME_KEY)
	public void setIteratorName(String iteratorName);

	public Type getIteratorType();

	@Override
	@Getter(value = FLEXO_CONCEPT_KEY, inverse = FlexoConcept.FLEXO_CONCEPT_CONSTRAINTS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoConcept getFlexoConcept();

	@Setter(FLEXO_CONCEPT_KEY)
	public void setFlexoConcept(FlexoConcept flexoConcept);

	@Getter(value = CONSTRAINT_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getConstraint();

	@Setter(CONSTRAINT_KEY)
	public void setConstraint(DataBinding<Boolean> constraint);

	@Getter(value = VIOLATION_HOOK_KEY)
	@XMLAttribute
	public DataBinding<?> getViolationHook();

	@Setter(VIOLATION_HOOK_KEY)
	public void setViolationHook(DataBinding<?> violationHook);

	@Getter(value = ITERATION_KEY)
	@XMLAttribute
	public DataBinding<List> getIteration();

	@Setter(ITERATION_KEY)
	public void setIteration(DataBinding<List> iteration);

	public boolean getHasIteration();

	public void setHasIteration(Boolean hasIteration);

	public static abstract class FlexoConceptConstraintImpl extends FlexoConceptObjectImpl implements FlexoConceptConstraint {

		protected static final Logger logger = FlexoLogger.getLogger(FlexoConceptConstraint.class.getPackage().getName());

		private FlexoConcept flexoConcept;
		private DataBinding<Boolean> constraint;
		private DataBinding<?> violationHook;
		private DataBinding<List> iteration;
		private Boolean hasIteration = null;

		private ConstraintBindable constraintBindable;

		public FlexoConceptConstraintImpl() {
			super();
			constraintBindable = new ConstraintBindableImpl();
		}

		@Override
		public BindingModel getBindingModel() {
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getBindingModel();
			}
			return null;
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			return flexoConcept;
		}

		@Override
		public void setFlexoConcept(FlexoConcept flexoConcept) {
			FlexoConcept oldValue = this.flexoConcept;
			BindingModel oldBM = getFlexoConcept() != null ? getFlexoConcept().getBindingModel() : null;
			this.flexoConcept = flexoConcept;
			BindingModel newBM = getFlexoConcept() != null ? getFlexoConcept().getBindingModel() : null;
			getPropertyChangeSupport().firePropertyChange(Bindable.BINDING_MODEL_PROPERTY, oldBM, newBM);
			getPropertyChangeSupport().firePropertyChange("flexoConcept", oldValue, flexoConcept);
		}

		@Override
		public DataBinding<Boolean> getConstraint() {
			if (constraint == null) {
				constraint = new DataBinding<>(constraintBindable, Boolean.class, BindingDefinitionType.GET);
				constraint.setBindingName("constraint");
			}
			return constraint;
		}

		@Override
		public void setConstraint(DataBinding<Boolean> constraint) {
			if (constraint != null) {
				constraint.setOwner(constraintBindable);
				constraint.setBindingName("constraint");
				constraint.setDeclaredType(Boolean.class);
				constraint.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.constraint = constraint;
		}

		@Override
		public DataBinding<?> getViolationHook() {
			if (violationHook == null) {
				violationHook = new DataBinding<>(constraintBindable, Object.class, BindingDefinitionType.EXECUTE);
				violationHook.setBindingName("violationHook");
			}
			return violationHook;
		}

		@Override
		public void setViolationHook(DataBinding<?> violationHook) {
			if (violationHook != null) {
				violationHook.setOwner(constraintBindable);
				violationHook.setBindingName("violationHook");
				violationHook.setDeclaredType(Object.class);
				violationHook.setBindingDefinitionType(BindingDefinitionType.EXECUTE);
			}
			this.violationHook = violationHook;
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
		public boolean getHasIteration() {
			if (hasIteration == null) {
				hasIteration = StringUtils.isNotEmpty(getIteratorName());
			}
			return hasIteration;
		}

		@Override
		public void setHasIteration(Boolean hasIteration) {
			if (getHasIteration() != hasIteration) {
				this.hasIteration = hasIteration;
				if (hasIteration && StringUtils.isEmpty(getIteratorName())) {
					setIteratorName("iterator");
				}
				getPropertyChangeSupport().firePropertyChange(HAS_ITERATION_KEY, (Boolean) !hasIteration, hasIteration);
			}
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

		public class ConstraintBindableImpl extends DefaultBindable implements ConstraintBindable {
			private ConstraintBindingModel constraintBindingModel = null;

			@Override
			public BindingFactory getBindingFactory() {
				return FlexoConceptConstraintImpl.this.getBindingFactory();
			}

			@Override
			public ConstraintBindingModel getBindingModel() {
				if (constraintBindingModel == null) {
					constraintBindingModel = new ConstraintBindingModel(FlexoConceptConstraintImpl.this);
				}
				return constraintBindingModel;
			}

			@Override
			public void notifiedBindingChanged(DataBinding<?> dataBinding) {
				if (dataBinding == getIteration()) {
					System.out.println("Hop on change l'iteration");
					FlexoConceptConstraintImpl.this.notifiedBindingChanged(dataBinding);
				}
			}

			@Override
			public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
				if (dataBinding == getIteration()) {
					FlexoConceptConstraintImpl.this.notifiedBindingDecoded(dataBinding);
				}
			}

		}

	}

	public interface ConstraintBindable extends Bindable {

		@Override
		public ConstraintBindingModel getBindingModel();

	}
}
