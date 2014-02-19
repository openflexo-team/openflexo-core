package org.openflexo.foundation.view;

import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.viewpoint.PatternRole;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents run-time-level object encoding reference to object considered as a modelling element<br>
 * An {@link ActorReference} is always attached to a {@link FlexoConceptInstance}
 * 
 * @author sylvain
 * 
 * @param <T>
 *            type of modelling element referenced by this ActorReference
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(ActorReference.ActorReferenceImpl.class)
public abstract interface ActorReference<T> extends VirtualModelInstanceObject {

	@PropertyIdentifier(type = FlexoConceptInstance.class)
	public static final String FLEXO_CONCEPT_INSTANCE_KEY = "flexoConceptInstance";

	@PropertyIdentifier(type = String.class)
	public static final String PATTERN_ROLE_NAME_KEY = "patternRoleName";

	@Getter(value = PATTERN_ROLE_NAME_KEY)
	@XMLAttribute
	public String getPatternRoleName();

	@Setter(PATTERN_ROLE_NAME_KEY)
	public void setPatternRoleName(String patternRoleName);

	/**
	 * Retrieve and return modelling element from informations stored in this {@link ActorReference}
	 * 
	 * @return
	 */
	public T getModellingElement();

	/**
	 * Sets modelling element referenced by this {@link ActorReference}
	 * 
	 * @param object
	 */
	public void setModellingElement(T object);

	/**
	 * Return the {@link FlexoConceptInstance} where this reference "lives"
	 * 
	 * @return
	 */
	@Getter(value = FLEXO_CONCEPT_INSTANCE_KEY, inverse = FlexoConceptInstance.ACTOR_LIST_KEY)
	public FlexoConceptInstance getEditionPatternInstance();

	@Setter(FLEXO_CONCEPT_INSTANCE_KEY)
	public void setEditionPatternInstance(FlexoConceptInstance epi);

	public PatternRole<T> getPatternRole();

	public void setPatternRole(PatternRole<T> patternRole);

	public ModelSlotInstance<?, ?> getModelSlotInstance();

	public static abstract class ActorReferenceImpl<T> extends VirtualModelInstanceObjectImpl implements ActorReference<T> {
		private PatternRole<T> patternRole;
		private String patternRoleName;
		private ModelSlot modelSlot;
		private FlexoConceptInstance epi;

		public ModelSlot getModelSlot() {
			return modelSlot;
		}

		public void setModelSlot(ModelSlot modelSlot) {
			this.modelSlot = modelSlot;
		}

		@Override
		public VirtualModelInstance getResourceData() {
			if (getEditionPatternInstance() != null) {
				return getEditionPatternInstance().getResourceData();
			}
			return null;
		}

		/**
		 * Retrieve modelling element from informations stored in this {@link ActorReference}
		 * 
		 * @return
		 */
		@Override
		public abstract T getModellingElement();

		@Override
		public FlexoConceptInstance getEditionPatternInstance() {
			return epi;
		}

		@Override
		public void setEditionPatternInstance(FlexoConceptInstance epi) {
			this.epi = epi;
		}

		@Override
		public PatternRole<T> getPatternRole() {
			if (patternRole == null && epi != null && StringUtils.isNotEmpty(patternRoleName)) {
				patternRole = (PatternRole<T>) epi.getFlexoConcept().getPatternRole(patternRoleName);
			}
			return patternRole;
		}

		@Override
		public void setPatternRole(PatternRole<T> patternRole) {
			this.patternRole = patternRole;
		}

		@Override
		public String getPatternRoleName() {
			if (patternRole != null) {
				return patternRole.getPatternRoleName();
			}
			return patternRoleName;
		}

		@Override
		public void setPatternRoleName(String patternRoleName) {
			this.patternRoleName = patternRoleName;
		}

		@Override
		public VirtualModelInstance getVirtualModelInstance() {
			if (getEditionPatternInstance() != null) {
				return getEditionPatternInstance().getVirtualModelInstance();
			}
			return null;
		}

		@Override
		public ModelSlotInstance<?, ?> getModelSlotInstance() {
			if (getVirtualModelInstance() != null) {
				return getVirtualModelInstance().getModelSlotInstance(getPatternRole().getModelSlot());
			}
			return null;
		}

	}
}
