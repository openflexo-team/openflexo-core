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

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
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
 * This is the run-time object for a {@link ModelSlot}
 * 
 * It concretized the effective binding of a {@link ModelSlot} to its data<br>
 * A {@link ModelSlotInstance} persists the connection of a {@link ModelSlot} at run-time
 * 
 * @param <MS>
 *            type of {@link ModelSlot} beeing connected
 * @param <RD>
 *            type of resource data beeing exposed by the {@link ModelSlot}
 * 
 * @author sylvain
 * @see ModelSlot
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(ModelSlotInstance.ModelSlotInstanceImpl.class)
@Imports({ @Import(FreeModelSlotInstance.class), @Import(TypeAwareModelSlotInstance.class), @Import(FMLRTModelSlotInstance.class) })
public abstract interface ModelSlotInstance<MS extends ModelSlot<? extends RD, ?>, RD extends ResourceData<RD> & TechnologyObject<?>>
		extends ActorReference<RD> {

	@PropertyIdentifier(type = String.class)
	public static final String MODEL_SLOT_NAME_KEY = "modelSlotName";

	@Getter(value = MODEL_SLOT_NAME_KEY)
	@XMLAttribute
	public String getModelSlotName();

	@Setter(MODEL_SLOT_NAME_KEY)
	public void setModelSlotName(String modelSlotName);

	public void setModelSlot(MS modelSlot);

	public MS getModelSlot();

	/**
	 * Return the data this model slot gives access to.<br>
	 * This is the data contractualized by the related model slot
	 * 
	 * @return
	 */
	public RD getAccessedResourceData();

	/**
	 * Sets the data this model slot gives access to.<br>
	 * 
	 * @param accessedResourceData
	 */
	// public void setAccessedResourceData(RD accessedResourceData);

	public static abstract class ModelSlotInstanceImpl<MS extends ModelSlot<RD, ?>, RD extends ResourceData<RD> & TechnologyObject<?>>
			extends ActorReferenceImpl<RD> implements ModelSlotInstance<MS, RD> {

		private static final Logger logger = Logger.getLogger(ModelSlotInstance.class.getPackage().getName());

		private MS modelSlot;
		private String modelSlotName;

		@Override
		public VirtualModelInstance<?, ?> getResourceData() {
			return getVirtualModelInstance();
		}

		@Override
		public MS getModelSlot() {

			if (getFlexoConceptInstance() != null && getFlexoConceptInstance().getFlexoConcept() != null && modelSlot == null
					&& StringUtils.isNotEmpty(modelSlotName)) {
				FlexoProperty<?> foundModelSlot = getFlexoConceptInstance().getFlexoConcept().getAccessibleProperty(modelSlotName);
				if (foundModelSlot instanceof ModelSlot) {
					modelSlot = (MS) foundModelSlot;
				}
			}
			return modelSlot;
		}

		@Override
		public void setModelSlot(MS modelSlot) {
			this.modelSlot = modelSlot;
		}

		public void updateActorReferencesURI() {
		}

		// Serialization/deserialization only, do not use
		@Override
		public String getModelSlotName() {
			if (getModelSlot() != null) {
				return getModelSlot().getName();
			}
			return modelSlotName;
		}

		// Serialization/deserialization only, do not use
		@Override
		public void setModelSlotName(String modelSlotName) {
			this.modelSlotName = modelSlotName;
		}

		@Override
		public String toString() {
			return "ModelSlotInstance:" + (getModelSlot() != null
					? getModelSlot().getName() + ":" + getModelSlot().getClass().getSimpleName() + "_" + getFlexoID()
					: "null");
		}

		/**
		 * Returns a string describing how the model slot instance is bound to a data source
		 * 
		 * @return
		 */
		public abstract String getBindingDescription();

		@Override
		public String getRoleName() {
			return getModelSlotName();
		}

		@Override
		public void setRoleName(String roleName) {
			setModelSlotName(roleName);
		}

		@Override
		public RD getModellingElement(boolean forceLoading) {
			return getAccessedResourceData();
		}

		@Override
		public MS getFlexoRole() {
			return getModelSlot();
		}

		@Override
		public void setFlexoRole(FlexoRole<? super RD> flexoRole) {
			setModelSlot((MS) flexoRole);
		}

		/*@Override
		public ModelSlotInstance<?, ?, TA> getModelSlotInstance() {
			return null;
		}*/
	}
}
