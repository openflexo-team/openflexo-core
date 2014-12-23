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

import java.util.logging.Logger;

import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.FlexoModelResource;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

/**
 * Concretize the binding of a {@link ModelSlot} to a concrete {@link FlexoModel} conform to a given {@link FlexoMetaModel}<br>
 * This is the binding point between a {@link TypeAwareModelSlot} and its concretization in a {@link VirtualModelInstance}
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

		/*public TypeAwareModelSlotInstanceImpl(VirtualModelInstance vmInstance, MS modelSlot) {
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
			if (getVirtualModelInstance() != null && accessedResourceData == null && StringUtils.isNotEmpty(modelURI)) {
				FlexoModelResource<M, ?, ?, ?> modelResource = (FlexoModelResource<M, ?, ?, ?>) getVirtualModelInstance()
						.getInformationSpace().getModelWithURI(modelURI, getModelSlot().getTechnologyAdapter());
				if (modelResource != null) {
					accessedResourceData = modelResource.getModel();
					resource = modelResource;
				}
			}
			if (accessedResourceData == null && StringUtils.isNotEmpty(modelURI)) {
				logger.warning("cannot find model " + modelURI);
			}
			return accessedResourceData;
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
			// Browse the epi and their actors
			for (FlexoConceptInstance epi : getVirtualModelInstance().getFlexoConceptInstances()) {
				for (ActorReference<?> actor : epi.getActors()) {
					// If it is provided by the right model slot
					if (actor instanceof ConceptActorReference && actor.getModelSlotInstance().equals(this)) {

						// This should be changed
						ConceptActorReference<?> conceptActorRef = (ConceptActorReference<?>) actor;
						String id = conceptActorRef.getConceptURI().substring(conceptActorRef.getConceptURI().lastIndexOf("#"));
						conceptActorRef.setConceptURI(getAccessedResourceData().getURI() + id);
						if (conceptActorRef.getModellingElement() == null) {
							logger.warning("cannot retrieve objects in this resource " + conceptActorRef);
							// conceptActorRef.delete();
						}
					}
				}
			}

			setModelURI(getAccessedResourceData().getURI());
		}
	}
}
