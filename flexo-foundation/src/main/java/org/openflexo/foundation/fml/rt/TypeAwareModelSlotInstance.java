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

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.FlexoModelResource;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

/**
 * Concretize the binding of a {@link ModelSlot} to a concrete {@link FlexoModel} conform to a given {@link FlexoMetaModel}<br>
 * This is the binding point between a {@link TypeAwareModelSlot} and its concretization in a {@link FMLRTVirtualModelInstance}
 * 
 * @author Sylvain Guerin
 * @see TypeAwareModelSlot
 * 
 */
@ModelEntity
@ImplementationClass(TypeAwareModelSlotInstance.TypeAwareModelSlotInstanceImpl.class)
@XMLElement
public interface TypeAwareModelSlotInstance<M extends FlexoModel<M, MM> & TechnologyObject<?>, MM extends FlexoMetaModel<MM> & TechnologyObject<?>, MS extends TypeAwareModelSlot<M, MM>>
		extends ModelSlotInstance<MS, M> {

	@PropertyIdentifier(type = String.class)
	public static final String MODEL_URI_KEY = "modelURI";

	@Getter(value = MODEL_URI_KEY)
	@XMLAttribute
	public String getModelURI();

	@Setter(MODEL_URI_KEY)
	public void setModelURI(String modelURI);

	public M getModel();

	public static abstract class TypeAwareModelSlotInstanceImpl<M extends FlexoModel<M, MM> & TechnologyObject<?>, MM extends FlexoMetaModel<MM> & TechnologyObject<?>, MS extends TypeAwareModelSlot<M, MM>>
			extends ModelSlotInstanceImpl<MS, M> implements TypeAwareModelSlotInstance<M, MM, MS> {

		private static final Logger logger = Logger.getLogger(TypeAwareModelSlotInstance.class.getPackage().getName());

		// Serialization/deserialization only, do not use
		private String modelURI;

		/*public TypeAwareModelSlotInstanceImpl(View view, MS modelSlot) {
			super(view, modelSlot);
		}*/

		/*public TypeAwareModelSlotInstanceImpl(FMLRTVirtualModelInstance vmInstance, MS modelSlot) {
			super(vmInstance, modelSlot);
		}*/

		/**
		 * Default constructor
		 */
		public TypeAwareModelSlotInstanceImpl() {
			super();
		}

		/**
		 * Return the data this model slot gives access to.<br>
		 * This is the data contractualized by the related model slot
		 * 
		 * @return
		 */
		@Override
		public M getAccessedResourceData() {
			FlexoServiceManager svcManager = getServiceManager();
			if (getModelSlot() != null && getVirtualModelInstance() != null && svcManager != null && svcManager.getResourceManager() != null
					&& accessedResourceData == null && StringUtils.isNotEmpty(modelURI)) {
				FlexoModelResource<M, ?, ?, ?> modelResource = (FlexoModelResource<M, ?, ?, ?>) svcManager.getResourceManager()
						.getModelWithURI(modelURI, getModelSlot().getModelSlotTechnologyAdapter());
				if (modelResource != null) {
					accessedResourceData = modelResource.getModel();
					setResource(modelResource, false);
					// resource = modelResource;
				}
			}
			if (accessedResourceData == null && StringUtils.isNotEmpty(modelURI)) {
				logger.warning("cannot find model " + modelURI);
				/*for (FlexoResourceCenter<?> rc : svcManager.getResourceCenterService().getResourceCenters()) {
					System.out.println("--------------- RC: " + rc);
					for (FlexoResource<?> resource : rc.getAllResources()) {
						System.out.println(" > " + resource.getURI());
					}
				}*/
			}
			return accessedResourceData;
		}

		@Override
		public TechnologyAdapterResource<M, ?> getResource() {
			TechnologyAdapterResource<M, ?> returned = super.getResource();
			if (returned == null && getAccessedResourceData() != null) {
				return (TechnologyAdapterResource) getAccessedResourceData().getResource();
			}
			return returned;
		}

		// Serialization/deserialization only, do not use
		@Override
		public String getModelURI() {
			if (getResource() != null) {
				return getResource().getURI();
			}
			return modelURI;
		}

		// Serialization/deserialization only, do not use
		@Override
		public void setModelURI(String modelURI) {
			this.modelURI = modelURI;
		}

		@Override
		public M getModel() {
			return getAccessedResourceData();
		}

		@Override
		public String getBindingDescription() {
			return getModelURI();
		}

		@Override
		public void updateActorReferencesURI() {
			super.updateActorReferencesURI();
			setModelURI(getAccessedResourceData().getURI());
		}
	}
}
