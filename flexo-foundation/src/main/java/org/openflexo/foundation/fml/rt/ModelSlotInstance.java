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
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
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
 * Concretize the binding of a {@link ModelSlot} to a concrete {@link FlexoModel}<br>
 * This is the binding point between a {@link ModelSlot} and its concretization in a {@link VirtualModelInstance}
 * 
 * The {@link ModelSlotInstance} are instantiated inside a {@link View}
 * 
 * @author Luka Le Roux, Sylvain Guerin
 * @see ModelSlot
 * @see FlexoModel
 * @see View
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(ModelSlotInstance.ModelSlotInstanceImpl.class)
@Imports({ @Import(FreeModelSlotInstance.class), @Import(TypeAwareModelSlotInstance.class), @Import(VirtualModelModelSlotInstance.class) })
public abstract interface ModelSlotInstance<MS extends ModelSlot<? extends RD>, RD extends ResourceData<RD> & TechnologyObject<?>>
		extends ActorReference<RD> {

	@PropertyIdentifier(type = String.class)
	public static final String MODEL_SLOT_NAME_KEY = "modelSlotName";

	@PropertyIdentifier(type = TechnologyAdapterResource.class)
	public static final String RESOURCE_KEY = "resource";

	@PropertyIdentifier(type = ResourceData.class)
	public static final String ACCESSED_RESOURCE_DATA_KEY = "accessedResourceData";

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
	@Getter(value = ACCESSED_RESOURCE_DATA_KEY, ignoreType = true)
	public RD getAccessedResourceData();

	/**
	 * Sets the data this model slot gives access to.<br>
	 * 
	 * @param accessedResourceData
	 */
	@Setter(ACCESSED_RESOURCE_DATA_KEY)
	public void setAccessedResourceData(RD accessedResourceData);

	/**
	 * Return the resource of the data this model slot gives access to.<br>
	 * This is the data contractualized by the related model slot
	 * 
	 * @return
	 */
	@Getter(value = RESOURCE_KEY, ignoreType = true)
	public TechnologyAdapterResource<RD, ?> getResource();

	/**
	 * Sets the resource of the data this model slot gives access to.<br>
	 * This is the data contractualized by the related model slot
	 * 
	 * @param resource
	 */
	@Setter(RESOURCE_KEY)
	public void setResource(TechnologyAdapterResource<RD, ?> resource);

	/**
	 * Sets the resource of the data this model slot gives access to.<br>
	 * This is the data contractualized by the related model slot<br>
	 * A flag allows to declare enclosing resource to be modified or not
	 * 
	 * @param resource
	 * @param declareAsModified
	 */
	public void setResource(TechnologyAdapterResource<RD, ?> resource, boolean declareAsModified);

	public static abstract class ModelSlotInstanceImpl<MS extends ModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<?>>
			extends ActorReferenceImpl<RD> implements ModelSlotInstance<MS, RD> {

		private static final Logger logger = Logger.getLogger(ModelSlotInstance.class.getPackage().getName());

		private View view;
		// private AbstractVirtualModelInstance<?, ?> vmInstance;
		private MS modelSlot;
		protected RD accessedResourceData;
		protected TechnologyAdapterResource<RD, ?> resource;
		// Serialization/deserialization only, do not use
		private String modelSlotName;

		/*public ModelSlotInstanceImpl(FlexoProject project) {
			super(project);
		}*/

		/*public ModelSlotInstanceImpl(View view, MS modelSlot) {
			super(view.getProject());
			this.view = view;
			this.modelSlot = modelSlot;
		}*/

		/*public ModelSlotInstanceImpl(VirtualModelInstance vmInstance, MS modelSlot) {
			super(vmInstance.getProject());
			this.vmInstance = vmInstance;
			this.view = vmInstance.getView();
			this.modelSlot = modelSlot;
		}*/

		/**
		 * Default constructor
		 */
		public ModelSlotInstanceImpl() {
			super();
		}

		@Override
		public AbstractVirtualModelInstance<?, ?> getResourceData() {
			return getVirtualModelInstance();
		}

		@Override
		public void setView(View view) {
			this.view = view;
		}

		@Override
		public View getView() {
			return view;
		}

		/*@Override
		public AbstractVirtualModelInstance<?, ?> getVirtualModelInstance() {
			return vmInstance;
		}
		
		@Override
		public void setVirtualModelInstance(AbstractVirtualModelInstance<?, ?> vmInstance) {
			this.vmInstance = vmInstance;
		}*/

		@Override
		public MS getModelSlot() {
			if (getFlexoConceptInstance() != null && getFlexoConceptInstance().getFlexoConcept() != null && modelSlot == null
					&& StringUtils.isNotEmpty(modelSlotName)) {
				modelSlot = (MS) getFlexoConceptInstance().getFlexoConcept().getModelSlot(modelSlotName);
			}
			if (getVirtualModelInstance() != null && getVirtualModelInstance().getVirtualModel() != null && modelSlot == null
					&& StringUtils.isNotEmpty(modelSlotName)) {
				modelSlot = (MS) getVirtualModelInstance().getVirtualModel().getModelSlot(modelSlotName);
			}
			return modelSlot;
		}

		@Override
		public void setModelSlot(MS modelSlot) {
			this.modelSlot = modelSlot;
		}

		public void updateActorReferencesURI() {
		}

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
					accessedResourceData = getResource().getResourceData(null);
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
				logger.warning("resourceData will be set to null for ModelSlot: " + this.modelSlotName);
				setResource((TechnologyAdapterResource<RD, ?>) accessedResourceData.getResource());
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
		public TechnologyAdapterResource<RD, ?> getResource() {
			return resource;
		}

		@Override
		public void setResource(TechnologyAdapterResource<RD, ?> resource) {
			setResource(resource, true);
		}

		/**
		 * Sets the resource of the data this model slot gives access to.<br>
		 * This is the data contractualized by the related model slot<br>
		 * A flag allows to declare enclosing resource to be modified or not
		 * 
		 * @param resource
		 * @param declareAsModified
		 */
		@Override
		public void setResource(TechnologyAdapterResource<RD, ?> resource, boolean declareAsModified) {
			if ((resource == null && this.resource != null) || (resource != null && !resource.equals(this.resource))) {
				TechnologyAdapterResource<RD, ?> oldValue = this.resource;
				this.resource = resource;
				getPropertyChangeSupport().firePropertyChange("resource", oldValue, resource);
				if (declareAsModified && (getVirtualModelInstance() != null)) {
					getVirtualModelInstance().setModified(true);
				}

			}
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
					? getModelSlot().getName() + ":" + getModelSlot().getClass().getSimpleName() + "_" + getFlexoID() : "null");
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
		public RD getModellingElement() {
			return getAccessedResourceData();
		}

		@Override
		public void setModellingElement(RD resourceData) {
			setAccessedResourceData(resourceData);
		}

		@Override
		public MS getFlexoRole() {
			return getModelSlot();
		}

		@Override
		public void setFlexoRole(FlexoRole<RD> flexoRole) {
			setModelSlot((MS) flexoRole);
		}

		@Override
		public ModelSlotInstance<?, ?> getModelSlotInstance() {
			return null;
		}
	}
}
