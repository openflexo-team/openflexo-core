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
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
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
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.FixProposal;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.model.validation.ValidationWarning;
import org.openflexo.toolbox.StringUtils;

/**
 * A {@link FlexoRole} is a structural element of an FlexoConcept, which plays a role in this {@link FlexoConcept}<br>
 * More formerly, a {@link FlexoRole} is the specification of an object accessed at run-time (inside an {@link FlexoConcept} instance)<br>
 * A model slot formalizes a contract for accessing to a data
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoRole.FlexoRoleImpl.class)
@Imports({ @Import(FlexoConceptInstanceRole.class), @Import(OntologicObjectRole.class), @Import(PrimitiveRole.class),
		@Import(OntologicObjectRole.class) })
public abstract interface FlexoRole<T> extends FlexoConceptObject {

	public static final String RESULTING_TYPE_PROPERTY = "resultingType";

	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String FLEXO_CONCEPT_KEY = "flexoConcept";
	@PropertyIdentifier(type = String.class)
	public static final String ROLE_NAME_KEY = "roleName";
	@PropertyIdentifier(type = String.class)
	public static final String DESCRIPTION_KEY = "description";
	@PropertyIdentifier(type = ModelSlot.class)
	public static final String MODEL_SLOT_KEY = "modelSlot";
	@PropertyIdentifier(type = RoleCloningStrategy.class)
	public static final String CLONING_STRATEGY_KEY = "cloningStrategy";
	@PropertyIdentifier(type = RoleCardinality.class)
	public static final String CARDINALITY_KEY = "cardinality";

	@Override
	@Getter(value = FLEXO_CONCEPT_KEY, inverse = FlexoConcept.FLEXO_ROLES_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoConcept getFlexoConcept();

	@Setter(FLEXO_CONCEPT_KEY)
	public void setFlexoConcept(FlexoConcept flexoConcept);

	@Getter(value = ROLE_NAME_KEY)
	public String getRoleName();

	@Setter(ROLE_NAME_KEY)
	public void setRoleName(String patternRoleName);

	@Getter(value = MODEL_SLOT_KEY)
	@XMLElement
	public ModelSlot<?> getModelSlot();

	@Setter(MODEL_SLOT_KEY)
	public void setModelSlot(ModelSlot<?> modelSlot);

	/**
	 * Return the type of any instance of modelling element handled by this role.<br>
	 * Note that if the cardinality of this role is multiple, this method will return the type of each modelling element.<br>
	 * Getting type of the list of all modelling elements for a multiple cardinality role is obtained by {@link #getResultingType()} method.
	 * 
	 * @return
	 */
	public Type getType();

	/**
	 * Return the resulting type of this {@link FlexoRole} access. This type is the same as the one obtained by {@link #getType()} method if
	 * the cardinality of this role is single, or a {@link List} of {@link #getType()} method if the cardinality of this role is multiple.
	 * 
	 * @return
	 */
	public Type getResultingType();

	public String getTypeDescription();

	/**
	 * Return cardinality of this role
	 * 
	 * @return
	 */
	@Getter(CARDINALITY_KEY)
	@XMLAttribute
	public RoleCardinality getCardinality();

	/**
	 * Sets cardinality of this role
	 * 
	 * @return
	 */
	@Setter(CARDINALITY_KEY)
	public void setCardinality(RoleCardinality cardinality);

	/**
	 * Return cloning strategy to be applied for this role
	 * 
	 * @return
	 */
	@Getter(CLONING_STRATEGY_KEY)
	@XMLAttribute
	public RoleCloningStrategy getCloningStrategy();

	/**
	 * Sets cloning strategy to be applied for this role
	 * 
	 * @return
	 */
	@Setter(CLONING_STRATEGY_KEY)
	public void setCloningStrategy(RoleCloningStrategy cloningStrategy);

	/**
	 * Encodes the default cloning strategy
	 * 
	 * @return
	 */
	public abstract RoleCloningStrategy defaultCloningStrategy();

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

	public static abstract class FlexoRoleImpl<T> extends FlexoConceptObjectImpl implements FlexoRole<T> {

		// private static final Logger logger = Logger.getLogger(FlexoRole.class.getPackage().getName());

		private ModelSlot<?> modelSlot;

		private Type resultingType;

		public FlexoRoleImpl() {
			super();
		}

		@Override
		public String getURI() {
			// Prevent NPE in case of null FlexoConcept (that should not happen, but....)
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getURI() + "." + getRoleName();
			} else {
				return null;
			}
		}

		@Override
		public ModelSlot<?> getModelSlot() {
			return modelSlot;
		}

		@Override
		public void setModelSlot(ModelSlot<?> modelSlot) {
			this.modelSlot = modelSlot;
			setChanged();
			notifyObservers(new DataModification("modelSlot", null, modelSlot));
		}

		@Override
		public String getRoleName() {
			return getName();
		}

		@Override
		public void setRoleName(String patternRoleName) {
			setName(patternRoleName);
		}

		@Override
		public RoleCardinality getCardinality() {
			RoleCardinality returned = (RoleCardinality) performSuperGetter(CARDINALITY_KEY);
			if (returned == null) {
				return RoleCardinality.ZeroOne;
			}
			return returned;
		}

		@Override
		public void setCardinality(RoleCardinality cardinality) {
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
					+ getRoleName()
					+ "[container="
					+ (getFlexoConcept() != null ? getFlexoConcept().getName()
							+ "/"
							+ (getFlexoConcept().getOwningVirtualModel() != null ? getFlexoConcept().getOwningVirtualModel().getName()
									: "null") : "null") + "][" + Integer.toHexString(hashCode()) + "]";
		}

		/**
		 * Return the type of any instance of modelling element handled by this role.<br>
		 * Note that if the cardinality of this role is multiple, this method will return the type of each modelling element.<br>
		 * Getting type of the list of all modelling elements for a multiple cardinality role is obtained by {@link #getResultingType()}
		 * method.
		 * 
		 * @return
		 */
		@Override
		public abstract Type getType();

		/**
		 * Return the resulting type of this {@link FlexoRole} access. This type is the same as the one obtained by {@link #getType()}
		 * method if the cardinality of this role is single, or a {@link List} of {@link #getType()} method if the cardinality of this role
		 * is multiple.
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

		/**
		 * Return cloning strategy to be applied for this role
		 * 
		 * @return
		 */
		@Override
		public RoleCloningStrategy getCloningStrategy() {
			RoleCloningStrategy returned = (RoleCloningStrategy) performSuperGetter(CLONING_STRATEGY_KEY);
			if (returned == null) {
				return defaultCloningStrategy();
			} else {
				return returned;
			}
		}

		@Override
		public abstract boolean defaultBehaviourIsToBeDeleted();

		@Override
		public abstract ActorReference<T> makeActorReference(T object, FlexoConceptInstance epi);

		// @Override
		// public abstract String getLanguageRepresentation();

	}

	public static enum RoleCloningStrategy {
		Clone, Reference, Ignore, Factory
	}

	@DefineValidationRule
	public static class FlexoRoleMustHaveAName extends ValidationRule<FlexoRoleMustHaveAName, FlexoRole> {
		public FlexoRoleMustHaveAName() {
			super(FlexoRole.class, "flexo_role_must_have_a_name");
		}

		@Override
		public ValidationIssue<FlexoRoleMustHaveAName, FlexoRole> applyValidation(FlexoRole flexoRole) {
			if (StringUtils.isEmpty(flexoRole.getRoleName())) {
				return new ValidationError<FlexoRoleMustHaveAName, FlexoRole>(this, flexoRole, "flexo_role_has_no_name");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class ShouldNotHaveReflexiveVirtualModelModelSlot extends
			ValidationRule<ShouldNotHaveReflexiveVirtualModelModelSlot, FlexoRole> {

		public ShouldNotHaveReflexiveVirtualModelModelSlot() {
			super(FlexoRole.class, "FlexoRole_should_not_have_reflexive_model_slot_no_more");
		}

		@Override
		public ValidationIssue<ShouldNotHaveReflexiveVirtualModelModelSlot, FlexoRole> applyValidation(FlexoRole aRole) {
			ModelSlot ms = aRole.getModelSlot();
			if (ms instanceof FMLRTModelSlot && "virtualModelInstance".equals(ms.getName())) {
				RemoveReflexiveVirtualModelModelSlot fixProposal = new RemoveReflexiveVirtualModelModelSlot(aRole);
				return new ValidationWarning<ShouldNotHaveReflexiveVirtualModelModelSlot, FlexoRole>(this, aRole,
						"FlexoRole_should_not_have_reflexive_model_slot_no_more", fixProposal);

			}
			return null;
		}

		protected static class RemoveReflexiveVirtualModelModelSlot extends
				FixProposal<ShouldNotHaveReflexiveVirtualModelModelSlot, FlexoRole> {

			private final FlexoRole role;

			public RemoveReflexiveVirtualModelModelSlot(FlexoRole aRole) {
				super("remove_reflexive_modelslot");
				this.role = aRole;
			}

			@Override
			protected void fixAction() {
				role.setModelSlot(null);
			}
		}

	}

}
