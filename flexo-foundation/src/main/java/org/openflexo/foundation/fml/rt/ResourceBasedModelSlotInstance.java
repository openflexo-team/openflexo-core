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

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * Abstract representation of a {@link ModelSlotInstance} which represents a direct reference to a given resource presenting a
 * {@link ResourceData}
 * 
 * @param <MS>
 *            type of {@link ModelSlot} beeing connected
 * @param <R>
 *            type of resource being connected
 * @param <RD>
 *            type of resource data beeing exposed by the {@link ModelSlot}
 * 
 * @author Sylvain
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(ResourceBasedModelSlotInstance.ResourceBasedModelSlotInstanceImpl.class)
public abstract interface ResourceBasedModelSlotInstance<
//@formatter:off
	MS extends ModelSlot<? extends RD, R>, 
	R extends TechnologyAdapterResource<RD, ?>, 
	RD extends ResourceData<RD> & TechnologyObject<?>>
		extends ModelSlotInstance<MS, RD>, SettableModelSlotInstance<MS, RD> {
	//@formatter:on

	@PropertyIdentifier(type = TechnologyAdapterResource.class)
	public static final String RESOURCE_KEY = "resource";

	@PropertyIdentifier(type = ResourceData.class)
	public static final String ACCESSED_RESOURCE_DATA_KEY = "accessedResourceData";

	/**
	 * Return the data this model slot gives access to.<br>
	 * This is the data contractualized by the related model slot
	 * 
	 * @return
	 */
	@Override
	@Getter(value = ACCESSED_RESOURCE_DATA_KEY, ignoreType = true)
	public RD getAccessedResourceData();

	/**
	 * Sets the data this model slot gives access to.<br>
	 * 
	 * @param accessedResourceData
	 */
	@Override
	@Setter(ACCESSED_RESOURCE_DATA_KEY)
	public void setAccessedResourceData(RD accessedResourceData);

	/**
	 * Return the resource of the data this model slot gives access to.<br>
	 * This is the data contractualized by the related model slot
	 * 
	 * @return
	 */
	@Getter(value = RESOURCE_KEY, ignoreType = true)
	public R getResource();

	/**
	 * Sets the resource of the data this model slot gives access to.<br>
	 * This is the data contractualized by the related model slot
	 * 
	 * @param resource
	 */
	@Setter(RESOURCE_KEY)
	public void setResource(R resource);

	public static abstract class ResourceBasedModelSlotInstanceImpl<MS extends ModelSlot<RD, R>, R extends TechnologyAdapterResource<RD, ?>, RD extends ResourceData<RD> & TechnologyObject<?>>
			extends ModelSlotInstanceImpl<MS, RD> implements ResourceBasedModelSlotInstance<MS, R, RD> {

		private static final Logger logger = Logger.getLogger(ResourceBasedModelSlotInstance.class.getPackage().getName());

		protected RD accessedResourceData;
		protected R resource;

		/**
		 * Return the data this model slot gives access to.<br>
		 * This is the data contractualized by the related model slot
		 * 
		 * @return
		 */
		@Override
		public RD getAccessedResourceData() {
			if (accessedResourceData == null && getResource() != null) {
				try {
					accessedResourceData = getResource().getResourceData();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ResourceLoadingCancelledException e) {
					e.printStackTrace();
				} catch (FlexoException e) {
					e.printStackTrace();
				}
			}

			return accessedResourceData;
		}

		/**
		 * Sets the data this model slot gives access to.<br>
		 * 
		 * @param accessedResourceData
		 */
		@Override
		public void setAccessedResourceData(RD accessedResourceData) {
			// FD unused
			// boolean requiresUpdate = false;
			// if (this.accessedResourceData != accessedResourceData) {
			// requiresUpdate = true;
			// }

			// NPE Protection when deleting VMI
			if (accessedResourceData != null) {
				logger.info("resourceData will be set to " + accessedResourceData + " for ModelSlot: " + this.getModelSlotName());
				setResource((R) accessedResourceData.getResource());
			}
			this.accessedResourceData = accessedResourceData;

			/*if (requiresUpdate) {
				// The virtual model can be synchronized with the new resource data.
				updateActorReferencesURI();
				if (getVirtualModelInstance().isSynchronizable()) {
					getVirtualModelInstance().synchronize(null);
				}
			}*/

			if (getVirtualModelInstance() != null) {
				getVirtualModelInstance().setModified(true);
			}

		}

		/**
		 * Return the resource of the data this model slot gives access to.<br>
		 * This is the data contractualized by the related model slot
		 * 
		 * @return
		 */
		@Override
		public final R getResource() {
			if (resource == null && isResourceRetrievable()) {
				resource = retrieveResource();
			}
			return resource;
		}

		abstract protected boolean isResourceRetrievable();

		abstract protected R retrieveResource();

		@Override
		public final void setResource(R resource) {
			if ((resource == null && this.resource != null) || (resource != null && !resource.equals(this.resource))) {
				TechnologyAdapterResource<RD, ?> oldValue = this.resource;
				this.resource = resource;
				getPropertyChangeSupport().firePropertyChange(RESOURCE_KEY, oldValue, resource);
				if (getVirtualModelInstance() != null) {
					getVirtualModelInstance().setModified(true);
				}
			}
		}

		@Override
		public void setModellingElement(RD resourceData) {
			setAccessedResourceData(resourceData);
		}

	}
}
