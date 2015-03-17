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

package org.openflexo.foundation.fml;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.toolbox.StringUtils;

/**
 * A {@link FlexoProperty} is a structural element of an FlexoConcept, which contractualize the access to a typed data<br>
 * More formerly, a {@link FlexoProperty} is the specification of a data accessed at run-time (inside an {@link FlexoConcept} instance)<br>
 * A {@link FlexoProperty} formalizes a contract for accessing to a data.<br>
 * A {@link FlexoProperty} is abstract and might be implemented by a sub-class of {@link FlexoProperty} (a {@link FlexoRole} is for instance
 * a direct reference to a modelling element stored in an external resource accessed by a {@link ModelSlot})
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoProperty.FlexoPropertyImpl.class)
@Imports({ @Import(FlexoConceptInstanceRole.class), @Import(OntologicObjectRole.class), @Import(PrimitiveRole.class),
		@Import(OntologicObjectRole.class) })
public abstract interface FlexoProperty<T> extends FlexoConceptObject {

	public static final String RESULTING_TYPE_PROPERTY = "resultingType";

	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String FLEXO_CONCEPT_KEY = "flexoConcept";
	@PropertyIdentifier(type = String.class)
	public static final String PROPERTY_NAME_KEY = "propertyName";
	@PropertyIdentifier(type = String.class)
	public static final String DESCRIPTION_KEY = "description";
	@PropertyIdentifier(type = PropertyCardinality.class)
	public static final String CARDINALITY_KEY = "cardinality";

	@Override
	@Getter(value = FLEXO_CONCEPT_KEY /*, inverse = FlexoConcept.FLEXO_PROPERTIES_KEY*/)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoConcept getFlexoConcept();

	@Setter(FLEXO_CONCEPT_KEY)
	public void setFlexoConcept(FlexoConcept flexoConcept);

	@Getter(value = PROPERTY_NAME_KEY)
	public String getPropertyName();

	@Setter(PROPERTY_NAME_KEY)
	public void setPropertyName(String propertyName);

	/**
	 * Return the type of any instance of data handled by this property.<br>
	 * Note that if the cardinality of this property is multiple, this method will return the type of each item of data.<br>
	 * Getting type of the list of all modelling elements for a multiple cardinality property is obtained by {@link #getResultingType()}
	 * method.
	 * 
	 * @return
	 */
	public Type getType();

	/**
	 * Return the resulting type of this {@link FlexoProperty} access. This type is the same as the one obtained by {@link #getType()}
	 * method if the cardinality of this property is single, or a {@link List} of {@link #getType()} method if the cardinality of this
	 * property is multiple.
	 * 
	 * @return
	 */
	public Type getResultingType();

	public String getTypeDescription();

	/**
	 * Return cardinality of this property
	 * 
	 * @return
	 */
	@Getter(CARDINALITY_KEY)
	@XMLAttribute
	public PropertyCardinality getCardinality();

	/**
	 * Sets cardinality of this property
	 * 
	 * @return
	 */
	@Setter(CARDINALITY_KEY)
	public void setCardinality(PropertyCardinality cardinality);

	/**
	 * Encodes the default deletion strategy
	 * 
	 * @return
	 */
	public abstract boolean defaultBehaviourIsToBeDeleted();

	/**
	 * Instanciate run-time-level object encoding reference to object (see {@link ActorReference})
	 * 
	 * @param object
	 * @param epi
	 * @return
	 */
	public abstract ActorReference<T> makeActorReference(T object, FlexoConceptInstance epi);

	public static abstract class FlexoPropertyImpl<T> extends FlexoConceptObjectImpl implements FlexoProperty<T> {

		// private static final Logger logger = Logger.getLogger(FlexoRole.class.getPackage().getName());

		private ModelSlot<?> modelSlot;

		private Type resultingType;

		@Override
		public String getPropertyName() {
			return getName();
		}

		@Override
		public void setPropertyName(String patternRoleName) {
			setName(patternRoleName);
		}

		@Override
		public String getURI() {
			// Prevent NPE in case of null FlexoConcept (that should not happen, but....)
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getURI() + "." + getPropertyName();
			} else {
				return null;
			}
		}

		@Override
		public PropertyCardinality getCardinality() {
			PropertyCardinality returned = (PropertyCardinality) performSuperGetter(CARDINALITY_KEY);
			if (returned == null) {
				return PropertyCardinality.ZeroOne;
			}
			return returned;
		}

		@Override
		public void setCardinality(PropertyCardinality cardinality) {
			if (cardinality != getCardinality()) {
				performSuperSetter(CARDINALITY_KEY, cardinality);
				notifyResultingTypeChanged();
			}
		}

		protected void notifyResultingTypeChanged() {
			resultingType = null;
			getPropertyChangeSupport().firePropertyChange(RESULTING_TYPE_PROPERTY, null, getResultingType());
		}

		@Override
		public String toString() {
			return getClass().getSimpleName()
					+ ":"
					+ getPropertyName()
					+ "[container="
					+ (getFlexoConcept() != null ? getFlexoConcept().getName()
							+ "/"
							+ (getFlexoConcept().getOwningVirtualModel() != null ? getFlexoConcept().getOwningVirtualModel().getName()
									: "null") : "null") + "][" + Integer.toHexString(hashCode()) + "]";
		}

		/**
		 * Return the type of any instance of modelling element handled by this property.<br>
		 * Note that if the cardinality of this property is multiple, this method will return the type of each modelling element.<br>
		 * Getting type of the list of all modelling elements for a multiple cardinality property is obtained by {@link #getResultingType()}
		 * method.
		 * 
		 * @return
		 */
		@Override
		public abstract Type getType();

		/**
		 * Return the resulting type of this {@link FlexoProperty} access. This type is the same as the one obtained by {@link #getType()}
		 * method if the cardinality of this property is single, or a {@link List} of {@link #getType()} method if the cardinality of this
		 * property is multiple.
		 * 
		 * @return
		 */
		@Override
		public final Type getResultingType() {
			if (resultingType != null) {
				// Check type
				if (getCardinality().isMultipleCardinality()) {
					if (resultingType instanceof ParameterizedType
							&& ((ParameterizedType) resultingType).getActualTypeArguments().length == 1
							&& ((ParameterizedType) resultingType).getActualTypeArguments()[0].equals(getType())) {
						// OK type is valid
						return resultingType;
					}
				} else {
					if (resultingType.equals(getType())) {
						return resultingType;
					}
				}
			}

			// Otherwise, compute the resulting type again
			if (getCardinality().isMultipleCardinality()) {
				resultingType = new ParameterizedTypeImpl(List.class, getType());
			} else {
				resultingType = getType();
			}

			return resultingType;
		}

		@Override
		public abstract String getTypeDescription();

		@Override
		public void finalizeDeserialization() {
			super.finalizeDeserialization();
		}

		@Override
		public final BindingModel getBindingModel() {
			return getFlexoConcept().getBindingModel();
		}

		@Override
		public abstract boolean defaultBehaviourIsToBeDeleted();

		@Override
		public abstract ActorReference<T> makeActorReference(T object, FlexoConceptInstance epi);

		// @Override
		// public abstract String getLanguageRepresentation();

	}

	@DefineValidationRule
	public static class FlexoPropertyMustHaveAName extends ValidationRule<FlexoPropertyMustHaveAName, FlexoProperty> {
		public FlexoPropertyMustHaveAName() {
			super(FlexoProperty.class, "flexo_property_must_have_a_name");
		}

		@Override
		public ValidationIssue<FlexoPropertyMustHaveAName, FlexoProperty> applyValidation(FlexoProperty flexoRole) {
			if (StringUtils.isEmpty(flexoRole.getPropertyName())) {
				return new ValidationError<FlexoPropertyMustHaveAName, FlexoProperty>(this, flexoRole, "flexo_property_has_no_name");
			}
			return null;
		}
	}

}
