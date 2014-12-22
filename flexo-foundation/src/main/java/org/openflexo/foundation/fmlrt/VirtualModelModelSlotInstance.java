/*
 * (c) Copyright 2010-2012 AgileBirds
 * (c) Copyright 2013 Openflexo
 *
 * This file is part of Openflexo.
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
 * along with Openflexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openflexo.foundation.fmlrt;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelModelSlot;
import org.openflexo.foundation.fmlrt.rm.VirtualModelInstanceResource;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

/**
 * 
 * Concretize the binding of a {@link VirtualModelModelSlot} to a concrete {@link VirtualModelInstance} conform to a given
 * {@link VirtualModel}<br>
 * 
 * @author Sylvain Guerin
 * @see VirtualModelModelSlot
 * 
 */
@ModelEntity
@ImplementationClass(VirtualModelModelSlotInstance.VirtualModelModelSlotInstanceImpl.class)
@XMLElement
public interface VirtualModelModelSlotInstance extends ModelSlotInstance<VirtualModelModelSlot, VirtualModelInstance> {

	@PropertyIdentifier(type = String.class)
	public static final String VIRTUAL_MODEL_INSTANCE_URI_KEY = "virtualModelInstanceURI";

	@Getter(value = VIRTUAL_MODEL_INSTANCE_URI_KEY)
	@XMLAttribute
	public String getVirtualModelInstanceURI();

	@Setter(VIRTUAL_MODEL_INSTANCE_URI_KEY)
	public void setVirtualModelInstanceURI(String virtualModelInstanceURI);

	public static abstract class VirtualModelModelSlotInstanceImpl extends
			ModelSlotInstanceImpl<VirtualModelModelSlot, VirtualModelInstance> implements VirtualModelModelSlotInstance {

		private static final Logger logger = Logger.getLogger(VirtualModelModelSlotInstance.class.getPackage().getName());

		// Serialization/deserialization only, do not use
		private String virtualModelInstanceURI;

		/*public VirtualModelModelSlotInstanceImpl(View view, VirtualModelModelSlot modelSlot) {
			super(view, modelSlot);
		}*/

		/*public VirtualModelModelSlotInstanceImpl(VirtualModelInstance vmInstance, VirtualModelModelSlot modelSlot) {
			super(vmInstance, modelSlot);
		}*/

		/**
		 * Default constructor
		 */
		public VirtualModelModelSlotInstanceImpl() {
			super();
		}

		@Override
		public VirtualModelInstance getAccessedResourceData() {
			if (getVirtualModelInstance() != null && accessedResourceData == null && StringUtils.isNotEmpty(getVirtualModelInstanceURI())) {
				VirtualModelInstanceResource vmiResource;
				if (getProject() != null) {
					vmiResource = getProject().getViewLibrary().getVirtualModelInstance(getVirtualModelInstanceURI());
				} else {
					vmiResource = getVirtualModelInstance().getView().getProject().getViewLibrary()
							.getVirtualModelInstance(getVirtualModelInstanceURI());
				}
				if (vmiResource != null) {
					accessedResourceData = vmiResource.getVirtualModelInstance();
					resource = vmiResource;
				}
			}
			// Special case to handle reflexive model slots
			/*if (accessedResourceData == null && getVirtualModelInstance() != null
					&& getModelSlot().equals(getVirtualModelInstance().getVirtualModel().getReflexiveModelSlot())) {
				accessedResourceData = getVirtualModelInstance();
				if (accessedResourceData != null) {
					resource = (TechnologyAdapterResource<VirtualModelInstance, ?>) accessedResourceData.getResource();
				}
			}*/
			if (accessedResourceData == null && StringUtils.isNotEmpty(getVirtualModelInstanceURI())) {
				logger.warning("Cannot find virtual model instance " + getVirtualModelInstanceURI());
			}
			return accessedResourceData;
		}

		// Serialization/deserialization only, do not use
		@Override
		public String getVirtualModelInstanceURI() {
			if (getResource() != null) {
				return getResource().getURI();
			}
			return virtualModelInstanceURI;
		}

		// Serialization/deserialization only, do not use
		@Override
		public void setVirtualModelInstanceURI(String virtualModelInstanceURI) {
			this.virtualModelInstanceURI = virtualModelInstanceURI;
		}

		@Override
		public String getBindingDescription() {
			return getVirtualModelInstanceURI();
		}

	}
}
