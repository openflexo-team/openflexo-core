package org.openflexo.foundation.view;

import java.util.logging.Logger;

import org.openflexo.foundation.ontology.IFlexoOntologyObject;
import org.openflexo.foundation.viewpoint.OntologicObjectPatternRole;
import org.openflexo.logging.FlexoLogger;

@ModelEntity
@ImplementationClass(ConceptActorReference.ConceptActorReferenceImpl.class)
@XMLElement
public interface ConceptActorReference<T extends IFlexoOntologyObject> extends ActorReference<T>{

@PropertyIdentifier(type=String.class)
public static final String OBJECT_URI_KEY = "objectURI";

@Getter(value=OBJECT_URI_KEY)
@XMLAttribute
public String _getObjectURI();

@Setter(OBJECT_URI_KEY)
public void _setObjectURI(String objectURI);


public static abstract  class ConceptActorReference<TImpl extends IFlexoOntologyObject> extends ActorReference<T>Impl implements ConceptActorReference<T
{

	private static final Logger logger = FlexoLogger.getLogger(ConceptActorReference.class.getPackage().toString());

	private T object;
	private String objectURI;

	public ConceptActorReferenceImpl(T o, OntologicObjectPatternRole<T> aPatternRole, EditionPatternInstance epi) {
		super(epi.getProject());
		setEditionPatternInstance(epi);
		setPatternRole(aPatternRole);
		object = o;

		ModelSlotInstance msInstance = getModelSlotInstance();
		/** Model Slot is responsible for URI mapping */
		objectURI = msInstance.getModelSlot().getURIForObject(msInstance, o);
	}

	@Override
	public T retrieveObject() {
		if (object == null) {
			ModelSlotInstance msInstance = getModelSlotInstance();
			if (msInstance.getResourceData() != null) {
				// object = (T) getProject().getObject(objectURI);
				/** Model Slot is responsible for URI mapping */
				object = (T) msInstance.getModelSlot().retrieveObjectWithURI(msInstance, objectURI);
			} else {
				logger.warning("Could not access to model in model slot " + getModelSlotInstance());
				// logger.warning("Searched " + getModelSlotInstance().getModelURI());
			}
		}
		if (object == null) {
			logger.warning("Could not retrieve object " + objectURI);
		}
		return object;
	}

	public String _getObjectURI() {
		if (object != null) {
			ModelSlotInstance msInstance = getModelSlotInstance();
			objectURI = msInstance.getModelSlot().getURIForObject(msInstance, object);
		}
		return objectURI;
	}

	public void _setObjectURI(String objectURI) {
		this.objectURI = objectURI;
	}

	public IFlexoOntologyObject getObject() {
		return object;
	}
}
}
