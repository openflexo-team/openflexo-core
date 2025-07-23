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

package org.openflexo.foundation.fml.rt.reflect;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance.ModelSlotInstanceImpl;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

/**
 * 
 * Concretize the binding of a {@link ReflectedFMLRTModelSlot} to a concrete resource and reflect the content as an instance of a FML
 * {@link VirtualModel}
 * 
 * @author sylvain
 * 
 * @param <VMI>
 *            type of {@link VirtualModelInstance} presented by this model slot
 * @param <R>
 *            type of resource beeing interpreted as an instance of a FML {@link VirtualModel}
 * @param <TA>
 *            technology providing this model slot
 * 
 * @see ReflectedFMLRTModelSlot
 * 
 */

@ModelEntity
@ImplementationClass(ReflectedFMLRTModelSlotInstance.ReflectedFMLRTModelSlotInstanceImpl.class)
@XMLElement
public interface ReflectedFMLRTModelSlotInstance<
//@formatter:off
	VMI extends ReflectedVirtualModelInstance<VMI, R, RD, TA>, 
	R extends TechnologyAdapterResource<RD, TA> & PamelaResource<RD, ?>, RD extends ResourceData<RD> & TechnologyObject<TA>, 
	TA extends TechnologyAdapter<TA>>
		extends ModelSlotInstance<ReflectedFMLRTModelSlot<VMI, R, RD, TA>, VMI> {
	//@formatter:on

	@PropertyIdentifier(type = String.class)
	public static final String REFLECTED_RESOURCE_URI_KEY = "reflectedResourceURI";

	@Getter(value = REFLECTED_RESOURCE_URI_KEY)
	@XMLAttribute
	public String getReflectedResourceURI();

	@Setter(REFLECTED_RESOURCE_URI_KEY)
	public void setReflectedResourceURI(String resourceURI);

	public R getReflectedResource();

	public void setReflectedResource(R reflectedResource);

	public static abstract class ReflectedFMLRTModelSlotInstanceImpl<VMI extends ReflectedVirtualModelInstance<VMI, R, RD, TA>, R extends TechnologyAdapterResource<RD, TA> & PamelaResource<RD, ?>, RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter<TA>>
			extends ModelSlotInstanceImpl<ReflectedFMLRTModelSlot<VMI, R, RD, TA>, VMI>
			implements ReflectedFMLRTModelSlotInstance<VMI, R, RD, TA> {

		private static final Logger logger = Logger.getLogger(ReflectedFMLRTModelSlotInstance.class.getPackage().getName());

		// Serialization/deserialization only, do not use
		private String reflectedResourceURI;
		private R reflectedResource;
		private VMI accessedResourceData;

		@Override
		public R getReflectedResource() {
			if (reflectedResource == null && StringUtils.isNotEmpty(reflectedResourceURI) && getServiceManager() != null
					&& getServiceManager().getResourceManager() != null) {
				// System.out.println("------------> OK, je cherche la resource " + reflectedResourceURI);
				reflectedResource = (R) getServiceManager().getResourceManager().getResource(reflectedResourceURI);
				// System.out.println("Je trouve " + returned);

				// if (returned == null) {
				// System.out.println("Bon, je trouve pas la resource " + reflectedResourceURI);
				// for (FlexoResourceCenter<?> rc : getServiceManager().getResourceCenterService().getResourceCenters()) {
				// System.out.println("> Dans " + rc);
				// for (FlexoResource<?> r : rc.getAllResources()) {
				// System.out.println(" >>> " + r.getURI());
				// }
				// }
				// }

				// setResource(returned, false);
			}
			return reflectedResource;
		}

		@Override
		public void setReflectedResource(R reflectedResource) {
			this.reflectedResource = reflectedResource;
		}

		@Override
		public VMI getAccessedResourceData() {
			return accessedResourceData;
		}

		@Override
		public void setAccessedResourceData(VMI accessedResourceData) {
			this.accessedResourceData = accessedResourceData;
		}

		/*@Override
		public RD getAccessedResourceData() {
			if (accessedResourceData == null && getServiceManager() != null) {
		
				TechnologyAdapterResource<RD, ?> resource = getResource();
		
				// if (resource == null && StringUtils.isNotEmpty(reflectedResourceURI) && getServiceManager() != null
				// && getServiceManager().getResourceManager() != null) {
				// resource = (TechnologyAdapterResource<RD, ?>) getServiceManager().getResourceManager().getResource(reflectedResourceURI,
				// getVersion());
				// setResource(resource, false);
				// }
				if (resource != null) {
					try {
						accessedResourceData = resource.getResourceData();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (ResourceLoadingCancelledException e) {
						e.printStackTrace();
					} catch (FlexoException e) {
						e.printStackTrace();
					}
				}
			}
			if (accessedResourceData == null && StringUtils.isNotEmpty(reflectedResourceURI)) {
				logger.warning("cannot find resource " + reflectedResourceURI);
			}
			return accessedResourceData;
		}*/

		// Serialization/deserialization only, do not use
		@Override
		public String getReflectedResourceURI() {
			if (reflectedResource != null) {
				return reflectedResource.getURI();
			}
			return reflectedResourceURI;
		}

		// Serialization/deserialization only, do not use
		@Override
		public void setReflectedResourceURI(String resourceURI) {
			this.reflectedResourceURI = resourceURI;
		}

		@Override
		public String getBindingDescription() {
			return getReflectedResourceURI();
		}
	}
}
