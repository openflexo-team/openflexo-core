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

import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
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
 * Concretize the binding of a {@link FMLRTModelSlot} to a concrete {@link FMLRTVirtualModelInstance} conform to a given {@link VirtualModel}<br>
 * 
 * @author Sylvain Guerin
 * 
 * @see FMLRTModelSlot
 * 
 */
@ModelEntity
@ImplementationClass(VirtualModelModelSlotInstance.VirtualModelModelSlotInstanceImpl.class)
@XMLElement
public interface VirtualModelModelSlotInstance<VMI extends VirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter>
		extends ModelSlotInstance<FMLRTModelSlot<VMI, TA>, VMI> {

	@PropertyIdentifier(type = String.class)
	public static final String VIRTUAL_MODEL_INSTANCE_URI_KEY = "virtualModelInstanceURI";

	@Getter(value = VIRTUAL_MODEL_INSTANCE_URI_KEY)
	@XMLAttribute
	public String getVirtualModelInstanceURI();

	@Setter(VIRTUAL_MODEL_INSTANCE_URI_KEY)
	public void setVirtualModelInstanceURI(String virtualModelInstanceURI);

	public static abstract class VirtualModelModelSlotInstanceImpl<VMI extends VirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter>
			extends ModelSlotInstanceImpl<FMLRTModelSlot<VMI, TA>, VMI> implements VirtualModelModelSlotInstance<VMI, TA> {

		private static final Logger logger = Logger.getLogger(VirtualModelModelSlotInstance.class.getPackage().getName());

		// Serialization/deserialization only, do not use
		private String virtualModelInstanceURI;

		@Override
		public AbstractVirtualModelInstanceResource<VMI, TA> getResource() {
			if (getVirtualModelInstance() != null && resource == null && StringUtils.isNotEmpty(virtualModelInstanceURI)) {

				resource = (AbstractVirtualModelInstanceResource<VMI, TA>) getServiceManager().getResourceManager()
						.getResource(virtualModelInstanceURI);

				/*FMLRTTechnologyAdapter fmlRTTA = getServiceManager().getTechnologyAdapterService()
						.getTechnologyAdapter(FMLRTTechnologyAdapter.class);
				ViewLibrary<?> viewRepository;
				
				if (getResourceCenter() != null) {
					viewRepository = (ViewLibrary<?>) fmlRTTA.getViewRepository(getResourceCenter());
				}
				else {
					viewRepository = (ViewLibrary<?>) fmlRTTA.getViewRepository(getVirtualModelInstance().getView().getResourceCenter());
				}
				resource = (VirtualModelInstanceResource<VMI, VM>) viewRepository.getVirtualModelInstance(virtualModelInstanceURI);
				*/
			}

			if (resource == null && StringUtils.isNotEmpty(virtualModelInstanceURI)) {
				logger.warning("Cannot find virtual model instance " + virtualModelInstanceURI);
			}
			return (AbstractVirtualModelInstanceResource<VMI, TA>) resource;
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
