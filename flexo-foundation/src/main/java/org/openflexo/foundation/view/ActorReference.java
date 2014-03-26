package org.openflexo.foundation.view;

import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.viewpoint.FlexoRole;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
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
@Imports({ @Import(ConceptActorReference.class), @Import(ModelObjectActorReference.class) })
public abstract interface ActorReference<T> extends VirtualModelInstanceObject {

	@PropertyIdentifier(type = FlexoConceptInstance.class)
	public static final String FLEXO_CONCEPT_INSTANCE_KEY = "flexoConceptInstance";
	@PropertyIdentifier(type = String.class)
	public static final String ROLE_NAME_KEY = "roleName";
	@PropertyIdentifier(type = Object.class)
	public static final String MODELLING_ELEMENT_KEY = "modellingElement";

	@Getter(value = ROLE_NAME_KEY)
	@XMLAttribute
	public String getRoleName();

	@Setter(ROLE_NAME_KEY)
	public void setRoleName(String patternRoleName);

	/**
	 * Retrieve and return modelling element from informations stored in this {@link ActorReference}
	 * 
	 * @return
	 */
	@Getter(value = MODELLING_ELEMENT_KEY, ignoreType = true)
	public T getModellingElement();

	/**
	 * Sets modelling element referenced by this {@link ActorReference}
	 * 
	 * @param object
	 */
	@Setter(value = MODELLING_ELEMENT_KEY)
	public void setModellingElement(T object);

	/**
	 * Return the {@link FlexoConceptInstance} where this reference "lives"
	 * 
	 * @return
	 */
	@Getter(value = FLEXO_CONCEPT_INSTANCE_KEY, inverse = FlexoConceptInstance.ACTORS_KEY)
	public FlexoConceptInstance getFlexoConceptInstance();

	@Setter(FLEXO_CONCEPT_INSTANCE_KEY)
	public void setFlexoConceptInstance(FlexoConceptInstance epi);

	public FlexoRole<T> getFlexoRole();

	public void setFlexoRole(FlexoRole<T> patternRole);

	public ModelSlotInstance<?, ?> getModelSlotInstance();

	public static abstract class ActorReferenceImpl<T> extends VirtualModelInstanceObjectImpl implements ActorReference<T> {
		private FlexoRole<T> flexoRole;
		private String flexoRoleName;
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
			if (getFlexoConceptInstance() != null) {
				return getFlexoConceptInstance().getResourceData();
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
		public FlexoConceptInstance getFlexoConceptInstance() {
			return epi;
		}

		@Override
		public void setFlexoConceptInstance(FlexoConceptInstance epi) {
			this.epi = epi;
		}

		@Override
		public FlexoRole<T> getFlexoRole() {
			if (flexoRole == null && epi != null && StringUtils.isNotEmpty(flexoRoleName)) {
				System.out.println("epi=" + epi);
				System.out.println("epi.getFlexoConcept()=" + epi.getFlexoConcept());
				flexoRole = (FlexoRole<T>) epi.getFlexoConcept().getFlexoRole(flexoRoleName);
			}
			return flexoRole;
		}

		@Override
		public void setFlexoRole(FlexoRole<T> patternRole) {
			this.flexoRole = patternRole;
		}

		@Override
		public String getRoleName() {
			if (flexoRole != null) {
				return flexoRole.getRoleName();
			}
			return flexoRoleName;
		}

		@Override
		public void setRoleName(String patternRoleName) {
			this.flexoRoleName = patternRoleName;
		}

		@Override
		public VirtualModelInstance getVirtualModelInstance() {
			if (getFlexoConceptInstance() != null) {
				return getFlexoConceptInstance().getVirtualModelInstance();
			}
			return null;
		}

		@Override
		public ModelSlotInstance<?, ?> getModelSlotInstance() {
			if (getVirtualModelInstance() != null) {
				return getVirtualModelInstance().getModelSlotInstance(getFlexoRole().getModelSlot());
			}
			return null;
		}

	}
}
