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

package org.openflexo.foundation.fml.rt;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
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
@Imports({ @Import(ModelObjectActorReference.class), @Import(PrimitiveActorReference.class), @Import(FlexoEnumValueActorReference.class),
		@Import(ModelSlotInstance.class) })
public abstract interface ActorReference<T> extends VirtualModelInstanceObject {

	@PropertyIdentifier(type = FlexoConceptInstance.class)
	public static final String FLEXO_CONCEPT_INSTANCE_KEY = "flexoConceptInstance";
	@PropertyIdentifier(type = String.class)
	public static final String ROLE_NAME_KEY = "roleName";
	@PropertyIdentifier(type = Object.class)
	public static final String MODELLING_ELEMENT_KEY = "modellingElement";

	@Getter(value = ROLE_NAME_KEY)
	@XMLAttribute
	@CloningStrategy(StrategyType.CLONE)
	public String getRoleName();

	@Setter(ROLE_NAME_KEY)
	public void setRoleName(String patternRoleName);

	/**
	 * Retrieve and return modelling element from informations stored in this {@link ActorReference}
	 * 
	 * @return
	 */
	@Getter(value = MODELLING_ELEMENT_KEY, ignoreType = true)
	@CloningStrategy(StrategyType.REFERENCE)
	public T getModellingElement();

	/**
	 * Retrieve and return modelling element from informations stored in this {@link ActorReference}<br>
	 * When forceLoading flag to false, and modelling element not loaded, return null
	 * 
	 * @return
	 */
	public T getModellingElement(boolean forceLoading);

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
	@Getter(value = FLEXO_CONCEPT_INSTANCE_KEY /*, inverse = FlexoConceptInstance.ACTORS_KEY*/)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoConceptInstance getFlexoConceptInstance();

	@Setter(FLEXO_CONCEPT_INSTANCE_KEY)
	public void setFlexoConceptInstance(FlexoConceptInstance epi);

	public FlexoRole<? super T> getFlexoRole();

	public void setFlexoRole(FlexoRole<? super T> patternRole);

	public ModelSlotInstance<?, ?> getModelSlotInstance();

	public Class<? extends T> getActorClass();

	public String getStringRepresentation();

	public static abstract class ActorReferenceImpl<T> extends VirtualModelInstanceObjectImpl implements ActorReference<T> {
		private FlexoRole<? super T> flexoRole;
		private String flexoRoleName;
		private FlexoConceptInstance flexoConceptInstance;

		@Override
		public VirtualModelInstance<?, ?> getResourceData() {
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
		public final T getModellingElement() {
			return getModellingElement(true);
		}

		/**
		 * Retrieve and return modelling element from informations stored in this {@link ActorReference}<br>
		 * When forceLoading flag to false, and modelling element not loaded, return null
		 * 
		 * @return
		 */
		@Override
		public abstract T getModellingElement(boolean forceLoading);

		@Override
		public FlexoConceptInstance getFlexoConceptInstance() {
			return flexoConceptInstance;
		}

		@Override
		public void setFlexoConceptInstance(FlexoConceptInstance fci) {
			this.flexoConceptInstance = fci;
		}

		@Override
		public FlexoRole<? super T> getFlexoRole() {
			if (flexoRole == null && flexoConceptInstance != null && flexoConceptInstance.getFlexoConcept() != null
					&& StringUtils.isNotEmpty(flexoRoleName)
					&& flexoConceptInstance.getFlexoConcept().getAccessibleProperty(flexoRoleName) instanceof FlexoRole) {
				flexoRole = (FlexoRole<T>) flexoConceptInstance.getFlexoConcept().getAccessibleProperty(flexoRoleName);
			}
			return flexoRole;
		}

		@Override
		public void setFlexoRole(FlexoRole<? super T> flexoRole) {
			this.flexoRole = flexoRole;
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
		public VirtualModelInstance<?, ?> getVirtualModelInstance() {
			if (getFlexoConceptInstance() instanceof FMLRTVirtualModelInstance) {
				return (FMLRTVirtualModelInstance) getFlexoConceptInstance();
			}
			if (getFlexoConceptInstance() != null) {
				return getFlexoConceptInstance().getVirtualModelInstance();
			}
			return null;
		}

		@Override
		public ModelSlotInstance<?, ?> getModelSlotInstance() {
			ModelSlotInstance<?, ?> returned = null;
			if (getFlexoRole() != null && getFlexoRole().getModelSlot() != null) {
				if (getVirtualModelInstance() != null) {
					returned = getVirtualModelInstance().getModelSlotInstance((ModelSlot) getFlexoRole().getModelSlot());
				}
			}
			return returned;
		}

		@Override
		public Class<? extends T> getActorClass() {
			return (Class<? extends T>) TypeUtils.getBaseClass(TypeUtils.getTypeArgument(getClass(), ActorReference.class, 0));
		}

		@Override
		public String getStringRepresentation() {
			if (getModellingElement() != null) {
				return getModellingElement().toString();
			}
			return "<null>";
		}
	}
}
