package org.openflexo.foundation.view;

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.foundation.viewpoint.PatternRole;
import org.openflexo.logging.FlexoLogger;

@ModelEntity
@ImplementationClass(ModelObjectActorReference.ModelObjectActorReferenceImpl.class)
@XMLElement
public interface ModelObjectActorReference<T extends FlexoProjectObject> extends ActorReference<T>{

@PropertyIdentifier(type=FlexoObjectReference.class)
public static final String OBJECT_REFERENCE_KEY = "objectReference";

@Getter(value=OBJECT_REFERENCE_KEY)
@XMLAttribute
public FlexoObjectReference getObjectReference();

@Setter(OBJECT_REFERENCE_KEY)
public void setObjectReference(FlexoObjectReference objectReference);


public static abstract  class ModelObjectActorReference<TImpl extends FlexoProjectObject> extends ActorReference<T>Impl implements ModelObjectActorReference<T
{

	private static final Logger logger = FlexoLogger.getLogger(ModelObjectActorReference.class.getPackage().toString());

	public T object;
	public FlexoObjectReference objectReference;

	public ModelObjectActorReferenceImpl(T o, PatternRole aPatternRole, EditionPatternInstance epi) {
		super(epi.getProject());
		setEditionPatternInstance(epi);
		setPatternRole(aPatternRole);
		object = o;
		objectReference = new FlexoObjectReference(o, o.getProject());
	}

	@Override
	public T retrieveObject() {
		if (object == null) {
			object = (T) objectReference.getObject(true);
		}
		if (object == null) {
			logger.warning("Could not retrieve object " + objectReference);
		}
		return object;
	}
}}
