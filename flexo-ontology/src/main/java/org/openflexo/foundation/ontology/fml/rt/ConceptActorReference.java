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

import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.ontology.IFlexoOntologyObject;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * Implements {@link ActorReference} for {@link IFlexoOntologyObject} as modelling elements.<br>
 * Such objects are identifiable using an URI.<br>
 * In this context, serialization of ActorReference might be supported by a single URI defined as a String.
 * 
 * @author sylvain
 * 
 * @param <T>
 */
@ModelEntity
@ImplementationClass(ConceptActorReference.ConceptActorReferenceImpl.class)
@XMLElement
public interface ConceptActorReference<T extends IFlexoOntologyObject> extends ActorReference<T> {

	@PropertyIdentifier(type = String.class)
	public static final String CONCEPT_URI_KEY = "conceptURI";

	@Getter(value = CONCEPT_URI_KEY)
	@XMLAttribute
	@FMLAttribute(value = CONCEPT_URI_KEY, required = false, description = "<html>URI of the concept</html>")
	public String getConceptURI();

	@Setter(CONCEPT_URI_KEY)
	public void setConceptURI(String objectURI);

	public static abstract class ConceptActorReferenceImpl<T extends IFlexoOntologyObject> extends ActorReferenceImpl<T>
			implements ConceptActorReference<T> {

		private static final Logger logger = FlexoLogger.getLogger(ConceptActorReference.class.getPackage().toString());

		private T concept;
		private String conceptURI;

		/**
		 * Default constructor
		 */
		public ConceptActorReferenceImpl() {
			super();
		}

		/*public ConceptActorReferenceImpl(T o, OntologicObjectRole<T> aPatternRole, FlexoConceptInstance epi) {
			super(epi.getProject());
			setFlexoConceptInstance(epi);
			setPatternRole(aPatternRole);
			concept = o;
		
			ModelSlotInstance msInstance = getModelSlotInstance();
			// Model Slot is responsible for URI mapping
			conceptURI = msInstance.getModelSlot().getURIForObject(msInstance, o);
		}*/

		@Override
		public void setModellingElement(T concept) {
			this.concept = concept;
			if (concept != null && getModelSlotInstance() != null) {
				ModelSlotInstance msInstance = getModelSlotInstance();
				/** Model Slot is responsible for URI mapping */
				conceptURI = msInstance.getModelSlot().getURIForObject(msInstance.getAccessedResourceData(), concept);
			}
		}

		@Override
		public T getModellingElement(boolean forceLoading) {
			if (concept == null) {
				ModelSlotInstance msInstance = getModelSlotInstance();
				if (msInstance == null) {
					logger.warning("Could not access model slot instance while looking up " + getConceptURI() + " role=" + getFlexoRole());
				}
				else {
					if (msInstance.getResourceData() != null) {
						// object = (T) getProject().getObject(objectURI);
						/** Model Slot is responsible for URI mapping */
						concept = (T) msInstance.getModelSlot().retrieveObjectWithURI(msInstance.getAccessedResourceData(), conceptURI);
					}
					else {
						logger.warning("Could not access to model in model slot " + getModelSlotInstance());
						// logger.warning("Searched " + getModelSlotInstance().getModelURI());
					}
				}
			}
			if (concept == null) {
				logger.warning("Could not retrieve object " + conceptURI);
			}
			return concept;
		}

		@Override
		public String getConceptURI() {
			ModelSlotInstance msInstance = getModelSlotInstance();
			if (concept != null && msInstance != null) {
				conceptURI = msInstance.getModelSlot().getURIForObject(msInstance.getAccessedResourceData(), concept);
			}
			return conceptURI;
		}

		@Override
		public void setConceptURI(String objectURI) {
			this.conceptURI = objectURI;
		}

	}
}
