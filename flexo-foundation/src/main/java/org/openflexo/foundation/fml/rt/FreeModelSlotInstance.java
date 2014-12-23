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

package org.openflexo.foundation.fml.rt;

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.FreeModelSlot;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;

/**
 * Concretize the binding of a {@link ModelSlot} to a concrete {@link FlexoModel}<br>
 * This is the binding point between a {@link FreeModelSlot} and its concretization in a {@link VirtualModelInstance}
 * 
 * @author Sylvain Guerin, Vincent LeildÃ©
 * @see FreeModelSlot
 * 
 */
@ModelEntity
@ImplementationClass(FreeModelSlotInstance.FreeModelSlotInstanceImpl.class)
@XMLElement
public interface FreeModelSlotInstance<RD extends ResourceData<RD> & TechnologyObject<?>, MS extends FreeModelSlot<RD>> extends
		ModelSlotInstance<MS, RD> {

	@PropertyIdentifier(type = String.class)
	public static final String RESOURCE_URI_KEY = "resourceURI";

	@Getter(value = RESOURCE_URI_KEY)
	@XMLAttribute
	public String getResourceURI();

	@Setter(RESOURCE_URI_KEY)
	public void setResourceURI(String resourceURI);

	public static abstract class FreeModelSlotInstanceImpl<RD extends ResourceData<RD> & TechnologyObject<?>, MS extends FreeModelSlot<RD>>
			extends ModelSlotInstanceImpl<MS, RD> implements FreeModelSlotInstance<RD, MS> {

		private static final Logger logger = Logger.getLogger(FreeModelSlotInstance.class.getPackage().getName());

		// Serialization/deserialization only, do not use
		private String resourceURI;

		private FlexoVersion version;

		/*public FreeModelSlotInstanceImpl(View view, MS modelSlot) {
			super(view, modelSlot);
		}*/

		/*public FreeModelSlotInstanceImpl(VirtualModelInstance vmInstance, MS modelSlot) {
			super(vmInstance, modelSlot);
		}*/

		/**
		 * Default constructor
		 */
		public FreeModelSlotInstanceImpl() {
			super();
		}

		@Override
		public RD getAccessedResourceData() {
			if (getVirtualModelInstance() != null && accessedResourceData == null && StringUtils.isNotEmpty(resourceURI)) {
				TechnologyAdapterResource<RD, ?> resource = (TechnologyAdapterResource<RD, ?>) getVirtualModelInstance()
						.getInformationSpace().getResource(resourceURI, getVersion());
				if (resource != null) {
					try {
						accessedResourceData = resource.getResourceData(null);
						this.resource = resource;
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ResourceLoadingCancelledException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FlexoException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (accessedResourceData == null && StringUtils.isNotEmpty(resourceURI)) {
				logger.warning("cannot find resource " + resourceURI);
			}
			return accessedResourceData;
		}

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
