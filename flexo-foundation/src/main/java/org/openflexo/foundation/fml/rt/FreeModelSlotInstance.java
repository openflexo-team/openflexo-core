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

import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.FreeModelSlot;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;

/**
 * Concretize the binding of a {@link ModelSlot} to a concrete {@link FlexoModel}<br>
 * This is the binding point between a {@link FreeModelSlot} and its concretization in a {@link FMLRTVirtualModelInstance}
 * 
 * @author sylvain
 * @see FreeModelSlot
 * 
 */
@ModelEntity
@ImplementationClass(FreeModelSlotInstance.FreeModelSlotInstanceImpl.class)
@XMLElement
public interface FreeModelSlotInstance<MS extends FreeModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<?>>
		extends ResourceBasedModelSlotInstance<MS, TechnologyAdapterResource<RD, ?>, RD> {

	@PropertyIdentifier(type = String.class)
	public static final String RESOURCE_URI_KEY = "resourceURI";

	@Getter(value = RESOURCE_URI_KEY)
	@XMLAttribute
	public String getResourceURI();

	@Setter(RESOURCE_URI_KEY)
	public void setResourceURI(String resourceURI);

	public static abstract class FreeModelSlotInstanceImpl<MS extends FreeModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<?>>
			extends ResourceBasedModelSlotInstanceImpl<MS, TechnologyAdapterResource<RD, ?>, RD> implements FreeModelSlotInstance<MS, RD> {

		private static final Logger logger = Logger.getLogger(FreeModelSlotInstance.class.getPackage().getName());

		// Serialization/deserialization only, do not use
		private String resourceURI;

		private FlexoVersion version;

		@Override
		protected boolean isResourceRetrievable() {
			return StringUtils.isNotEmpty(resourceURI) && getServiceManager() != null && getServiceManager().getResourceManager() != null;
		}

		@Override
		protected TechnologyAdapterResource<RD, ?> retrieveResource() {
			TechnologyAdapterResource<RD, ?> returned = (TechnologyAdapterResource<RD, ?>) getServiceManager().getResourceManager()
					.getResource(resourceURI, getVersion());
			if (returned == null) {
				logger.warning("cannot find resource " + resourceURI);
				/*for (FlexoResourceCenter<?> rc : getServiceManager().getResourceCenterService().getResourceCenters()) {
				System.out.println("--------------- RC: " + rc);
				for (FlexoResource<?> resource : rc.getAllResources()) {
					System.out.println(" > " + resource.getURI());
				}
				}*/
			}
			return returned;
		}

		/*@Override
		public RD getAccessedResourceData() {
			if (accessedResourceData == null && getServiceManager() != null) {
		
				TechnologyAdapterResource<RD, ?> resource = getResource();
		
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
			if (accessedResourceData == null && StringUtils.isNotEmpty(resourceURI)) {
				logger.warning("cannot find resource " + resourceURI);
			}
			return accessedResourceData;
		}*/

		// Serialization/deserialization only, do not use
		@Override
		public String getResourceURI() {
			if (getResource() != null) {
				return getResource().getURI();
			}
			return resourceURI;
		}

		// Serialization/deserialization only, do not use
		@Override
		public void setResourceURI(String resourceURI) {
			this.resourceURI = resourceURI;
		}

		public FlexoVersion getVersion() {
			return version;
		}

		public void setVersion(FlexoVersion version) {
			this.version = version;
		}

		public RD getModel() {
			return getAccessedResourceData();
		}

		@Override
		public String getBindingDescription() {
			return getResourceURI();
		}
	}
}
