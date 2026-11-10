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

package org.openflexo.foundation.fml.rt.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.FMLNamedReference;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.binding.MatchingCriteriaBindingModel;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.validation.BindingMustBeValid;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;

/**
 * A criterion of a {@code match} statement ({@link MatchFlexoConceptInstance}): one {@code property=value} of its {@code where} clause, as
 * {@code book=b} in {@code match IndexEntry in entries from this where (book=b) unmatched: new IndexEntry(b);}.
 * <p>
 * The value ({@link #getValue()}) is evaluated in the context of the executing behaviour, and a matching instance must hold that value for
 * the property ({@link #getFlexoProperty()}). A value which is not set, not valid, or evaluated to null is ignored: the criterion does not
 * restrict the match.
 */
@ModelEntity
@ImplementationClass(MatchingCriteria.MatchingCriteriaImpl.class)
public interface MatchingCriteria extends FlexoConceptObject {

	@PropertyIdentifier(type = MatchFlexoConceptInstance.class)
	public static final String ACTION_KEY = "action";

	@PropertyIdentifier(type = FMLNamedReference.class)
	public static final String PROPERTY_REFERENCE_KEY = "propertyReference";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String VALUE_KEY = "value";

	@Getter(value = ACTION_KEY)
	public MatchFlexoConceptInstance getAction();

	@Setter(ACTION_KEY)
	public void setAction(MatchFlexoConceptInstance action);

	/**
	 * The property of the matched concept this criterion compares, as a reference resolved in the concept the action addresses (see
	 * {@link #getFlexoProperty()})
	 *
	 * @return the reference to the property
	 */
	@Getter(value = PROPERTY_REFERENCE_KEY, ignoreType = true)
	public FMLNamedReference<FlexoProperty<?>> getPropertyReference();

	@Setter(PROPERTY_REFERENCE_KEY)
	public void setPropertyReference(FMLNamedReference<FlexoProperty<?>> propertyReference);

	/**
	 * Keeps the name only of a reference coming from another compilation unit (merge with {@code updateWith()})
	 *
	 * @param propertyReference
	 *            the reference held by the criterion of the other compilation unit
	 */
	@Updater(PROPERTY_REFERENCE_KEY)
	public void updatePropertyReference(FMLNamedReference<FlexoProperty<?>> propertyReference);

	@Getter(value = VALUE_KEY)
	public DataBinding<?> getValue();

	@Setter(VALUE_KEY)
	public void setValue(DataBinding<?> value);

	/**
	 * The property of the matched concept this criterion compares
	 *
	 * @return the property found under the name of {@link #getPropertyReference()} in the concept the action addresses
	 */
	public FlexoProperty<?> getFlexoProperty();

	public void setFlexoProperty(FlexoProperty<?> property);

	/**
	 * Sets the property of this criterion by its name, resolved in the concept the action addresses
	 *
	 * @param propertyName
	 *            name of the property
	 */
	public void setFlexoPropertyName(String propertyName);

	/**
	 * Evaluate the value of this criterion in supplied context
	 *
	 * @param evaluationContext
	 *            context of the executing behaviour
	 * @return the value, null when it is not set or not valid
	 */
	public Object evaluateCriteriaValue(RunTimeEvaluationContext evaluationContext);

	@Override
	public MatchingCriteriaBindingModel getBindingModel();

	public static abstract class MatchingCriteriaImpl extends FlexoConceptObjectImpl implements MatchingCriteria {

		private static final Logger logger = Logger.getLogger(MatchingCriteria.class.getPackage().getName());

		private FMLNamedReference<FlexoProperty<?>> propertyReference;
		private DataBinding<?> value;

		private MatchingCriteriaBindingModel bindingModel;

		// Use it only for deserialization
		public MatchingCriteriaImpl() {
			super();
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getAction() != null) {
				return getAction().getFlexoConcept();
			}
			return null;
		}

		@Override
		public DataBinding<?> getValue() {
			if (value == null) {
				value = new DataBinding<>(this, getFlexoProperty() != null ? getFlexoProperty().getResultingType() : Object.class,
						DataBinding.BindingDefinitionType.GET);
				value.setBindingName(getFlexoProperty() != null ? getFlexoProperty().getName() : "param");
			}
			return value;
		}

		@Override
		public void setValue(DataBinding<?> value) {
			if (value != null) {
				value.setOwner(this);
				value.setBindingName(getFlexoProperty() != null ? getFlexoProperty().getName() : "param");
				value.setDeclaredType(getFlexoProperty() != null ? getFlexoProperty().getResultingType() : Object.class);
				value.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.value = value;
		}

		@Override
		public Object evaluateCriteriaValue(RunTimeEvaluationContext evaluationContext) {
			if (getValue() == null || getValue().isUnset()) {
				return null;
			}
			else if (getValue().isValid()) {
				try {
					return getValue().getBindingValue(evaluationContext);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
				return null;
			}
			else {
				logger.warning("Invalid binding: " + getValue() + " Reason: " + getValue().invalidBindingReason());
			}
			return null;
		}

		@Override
		public MatchingCriteriaBindingModel getBindingModel() {
			if (bindingModel == null) {
				return bindingModel = new MatchingCriteriaBindingModel(this);
			}
			return bindingModel;
		}

		@Override
		public FMLNamedReference<FlexoProperty<?>> getPropertyReference() {
			return propertyReference;
		}

		@Override
		public void setPropertyReference(FMLNamedReference<FlexoProperty<?>> propertyReference) {
			FMLNamedReference<FlexoProperty<?>> oldValue = this.propertyReference;
			this.propertyReference = propertyReference;
			getPropertyChangeSupport().firePropertyChange(PROPERTY_REFERENCE_KEY, oldValue, propertyReference);
		}

		@Override
		public void updatePropertyReference(FMLNamedReference<FlexoProperty<?>> propertyReference) {
			setPropertyReference(propertyReference != null ? propertyReference.unresolved() : null);
		}

		@Override
		public FlexoProperty<?> getFlexoProperty() {
			if (propertyReference == null) {
				return null;
			}
			FlexoConcept matchedConcept = getAction() != null ? getAction().getFlexoConceptType() : null;
			return propertyReference.resolve(matchedConcept != null ? matchedConcept::getAccessibleProperty : null);
		}

		@Override
		public void setFlexoProperty(FlexoProperty<?> flexoProperty) {
			setPropertyReference(FMLNamedReference.to(flexoProperty));
		}

		@Override
		public void setFlexoPropertyName(String propertyName) {
			setPropertyReference(FMLNamedReference.named(propertyName));
		}

		@Override
		public String toString() {
			return "MatchingCriteria[" + (propertyReference != null ? propertyReference.getName() : null) + "=" + getValue() + "]";
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getValue().rebuild();
		}
	}

	@DefineValidationRule
	public static class ValueBindingMustBeValid extends BindingMustBeValid<MatchingCriteria> {
		public ValueBindingMustBeValid() {
			super("'value'_binding_must_be_valid", MatchingCriteria.class);
		}

		@Override
		public DataBinding<?> getBinding(MatchingCriteria object) {
			return object.getValue();
		}

	}

}
