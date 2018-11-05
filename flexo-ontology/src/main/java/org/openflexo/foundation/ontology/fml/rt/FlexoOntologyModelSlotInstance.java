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

package org.openflexo.foundation.ontology.fml.rt;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.TypeAwareModelSlotInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.technologyadapter.FlexoOntologyModelSlot;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;

/**
 * Concretize the binding of a {@link FlexoOntologyModelSlot} to a concrete {@link FlexoModel} conform to a given {@link FlexoMetaModel}
 * defined as {@link IFlexoOntology}<br>
 * This is the binding point between a {@link TypeAwareModelSlot} and its concretization in a {@link FMLRTVirtualModelInstance}
 * 
 * @author Sylvain Guerin
 * @see TypeAwareModelSlot
 * 
 */
@ModelEntity
@ImplementationClass(FlexoOntologyModelSlotInstance.FlexoOntologyModelSlotInstanceImpl.class)
@XMLElement
public interface FlexoOntologyModelSlotInstance<M extends FlexoModel<M, MM> & IFlexoOntology<TA>, MM extends FlexoMetaModel<MM> & IFlexoOntology<TA>, MS extends FlexoOntologyModelSlot<M, MM, TA>, TA extends TechnologyAdapter>
		extends TypeAwareModelSlotInstance<M, MM, MS> {

	public static abstract class FlexoOntologyModelSlotInstanceImpl<M extends FlexoModel<M, MM> & IFlexoOntology<TA>, MM extends FlexoMetaModel<MM> & IFlexoOntology<TA>, MS extends FlexoOntologyModelSlot<M, MM, TA>, TA extends TechnologyAdapter>
			extends TypeAwareModelSlotInstanceImpl<M, MM, MS> implements FlexoOntologyModelSlotInstance<M, MM, MS, TA> {

		private static final Logger logger = Logger.getLogger(FlexoOntologyModelSlotInstance.class.getPackage().getName());

		/**
		 * Default constructor
		 */
		public FlexoOntologyModelSlotInstanceImpl() {
			super();
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

			super.updateActorReferencesURI();
		}
	}
}
