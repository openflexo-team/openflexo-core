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

import org.openflexo.foundation.fml.FMLModelSlot;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
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
 * Concretize the binding of a {@link FMLModelSlot} to a concrete {@link VirtualModel}
 * 
 * @author Sylvain Guerin
 * 
 * @see FMLRTModelSlot
 * 
 */
@ModelEntity
@ImplementationClass(FMLModelSlotInstance.FMLModelSlotInstanceImpl.class)
@XMLElement
public interface FMLModelSlotInstance extends ModelSlotInstance<FMLModelSlot, VirtualModel> {

	@PropertyIdentifier(type = String.class)
	public static final String VIRTUAL_MODEL_URI_KEY = "virtualModelURI";

	@Getter(value = VIRTUAL_MODEL_URI_KEY)
	@XMLAttribute
	public String getVirtualModelURI();

	@Setter(VIRTUAL_MODEL_URI_KEY)
	public void setVirtualModelURI(String virtualModelInstanceURI);

	public static abstract class FMLModelSlotInstanceImpl extends ModelSlotInstanceImpl<FMLModelSlot, VirtualModel>
			implements FMLModelSlotInstance {

		private static final Logger logger = Logger.getLogger(FMLModelSlotInstance.class.getPackage().getName());

		// Serialization/deserialization only, do not use
		private String virtualModelURI;

		@Override
		public VirtualModelResource getResource() {
			if (getVirtualModelInstance() != null && resource == null && StringUtils.isNotEmpty(virtualModelURI)
					&& getServiceManager() != null && getServiceManager().getResourceManager() != null) {

				resource = (VirtualModelResource) getServiceManager().getResourceManager().getResource(virtualModelURI);
			}

			if (resource == null && StringUtils.isNotEmpty(virtualModelURI)) {
				logger.warning("Cannot find virtual model instance " + virtualModelURI);
			}
			return (VirtualModelResource) resource;
		}

		// Serialization/deserialization only, do not use
		@Override
		public String getVirtualModelURI() {
			if (getResource() != null) {
				return getResource().getURI();
			}
			return virtualModelURI;
		}

		// Serialization/deserialization only, do not use
		@Override
		public void setVirtualModelURI(String virtualModelInstanceURI) {
			this.virtualModelURI = virtualModelInstanceURI;
		}

		@Override
		public String getBindingDescription() {
			return "Bound to " + getVirtualModelURI();
		}

	}
}
