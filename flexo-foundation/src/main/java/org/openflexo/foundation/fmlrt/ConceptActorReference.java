package org.openflexo.foundation.fmlrt;

import java.util.logging.Logger;

import org.openflexo.foundation.ontology.IFlexoOntologyObject;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

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
	public String getConceptURI();

	@Setter(CONCEPT_URI_KEY)
	public void setConceptURI(String objectURI);

	public static abstract class ConceptActorReferenceImpl<T extends IFlexoOntologyObject> extends ActorReferenceImpl<T> implements
			ConceptActorReference<T> {

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
				conceptURI = msInstance.getModelSlot().getURIForObject(msInstance, concept);
			}
		}

		@Override
		public T getModellingElement() {
			if (concept == null) {
				ModelSlotInstance msInstance = getModelSlotInstance();
				if (msInstance.getResourceData() != null) {
					// object = (T) getProject().getObject(objectURI);
					/** Model Slot is responsible for URI mapping */
					concept = (T) msInstance.getModelSlot().retrieveObjectWithURI(msInstance, conceptURI);
				} else {
					logger.warning("Could not access to model in model slot " + getModelSlotInstance());
					// logger.warning("Searched " + getModelSlotInstance().getModelURI());
				}
			}
			if (concept == null) {
				logger.warning("Could not retrieve object " + conceptURI);
			}
			return concept;
		}

		@Override
		public String getConceptURI() {
			if (concept != null) {
				ModelSlotInstance msInstance = getModelSlotInstance();
				conceptURI = msInstance.getModelSlot().getURIForObject(msInstance, concept);
			}
			return conceptURI;
		}

		@Override
		public void setConceptURI(String objectURI) {
			this.conceptURI = objectURI;
		}

	}
}
