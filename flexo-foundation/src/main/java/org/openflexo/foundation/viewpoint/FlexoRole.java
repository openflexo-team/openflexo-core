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
package org.openflexo.foundation.viewpoint;

import java.lang.reflect.Type;

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.validation.ValidationError;
import org.openflexo.foundation.validation.ValidationIssue;
import org.openflexo.foundation.validation.ValidationRule;
import org.openflexo.foundation.view.ActorReference;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DeserializationFinalizer;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;
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
@Imports({ @Import(FlexoConceptInstanceRole.class), @Import(OntologicObjectRole.class), @Import(PrimitiveRole.class) })
public abstract interface FlexoRole<T> extends FlexoConceptObject {

	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String FLEXO_CONCEPT_KEY = "flexoConcept";
	@PropertyIdentifier(type = String.class)
	public static final String ROLE_NAME_KEY = "roleName";
	@PropertyIdentifier(type = String.class)
	public static final String DESCRIPTION_KEY = "description";
	@PropertyIdentifier(type = ModelSlot.class)
	public static final String MODEL_SLOT_KEY = "modelSlot";

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

	@DeserializationFinalizer
	public void finalizePatternRoleDeserialization();

	public Type getType();

	public String getPreciseType();

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

		private FlexoConcept _pattern;

		private ModelSlot<?> modelSlot;

		public FlexoRoleImpl() {
			super();
		}

		@Override
		public String getURI() {
			return getFlexoConcept().getURI() + "." + getRoleName();
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
		public void setFlexoConcept(FlexoConcept pattern) {
			_pattern = pattern;
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			return _pattern;
		}

		@Override
		public VirtualModel getVirtualModel() {
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getVirtualModel();
			}
			return null;
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
		public String toString() {
			return getClass().getSimpleName()
					+ ":"
					+ getRoleName()
					+ "[container="
					+ (getFlexoConcept() != null ? getFlexoConcept().getName() + "/"
							+ (getFlexoConcept().getVirtualModel() != null ? getFlexoConcept().getVirtualModel().getName() : "null")
							: "null") + "][" + Integer.toHexString(hashCode()) + "]";
		}

		@Override
		public abstract Type getType();

		@Override
		public abstract String getPreciseType();

		@Override
		public void finalizePatternRoleDeserialization() {
		}

		@Override
		public final BindingModel getBindingModel() {
			return getFlexoConcept().getBindingModel();
		}

		// public abstract boolean getIsPrimaryRole();

		// public abstract void setIsPrimaryRole(boolean isPrimary);

		@Override
		public abstract boolean defaultBehaviourIsToBeDeleted();

		@Override
		public abstract ActorReference<T> makeActorReference(T object, FlexoConceptInstance epi);

		// @Override
		// public abstract String getLanguageRepresentation();

	}

	public static class PatternRoleMustHaveAName extends ValidationRule<PatternRoleMustHaveAName, FlexoRole> {
		public PatternRoleMustHaveAName() {
			super(FlexoRole.class, "pattern_role_must_have_a_name");
		}

		@Override
		public ValidationIssue<PatternRoleMustHaveAName, FlexoRole> applyValidation(FlexoRole flexoRole) {
			if (StringUtils.isEmpty(flexoRole.getRoleName())) {
				return new ValidationError<PatternRoleMustHaveAName, FlexoRole>(this, flexoRole, "pattern_role_has_no_name");
			}
			return null;
		}
	}

}
