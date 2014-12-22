package org.openflexo.foundation.fmlrt;

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

/**
 * Implements {@link ActorReference} for {@link FlexoObject} as modelling elements.<br>
 * 
 * @author sylvain
 * 
 * @param <T>
 */
@ModelEntity
@ImplementationClass(ModelObjectActorReference.ModelObjectActorReferenceImpl.class)
@XMLElement
public interface ModelObjectActorReference<T extends FlexoObject> extends ActorReference<T> {

	@PropertyIdentifier(type = FlexoObjectReference.class)
	public static final String OBJECT_REFERENCE_KEY = "objectReference";

	@Getter(value = OBJECT_REFERENCE_KEY, isStringConvertable = true)
	@XMLAttribute
	public FlexoObjectReference<T> getObjectReference();

	@Setter(OBJECT_REFERENCE_KEY)
	public void setObjectReference(FlexoObjectReference<T> objectReference);

	public static abstract class ModelObjectActorReferenceImpl<T extends FlexoObject> extends ActorReferenceImpl<T> implements
			ModelObjectActorReference<T> {

		private static final Logger logger = FlexoLogger.getLogger(ModelObjectActorReference.class.getPackage().toString());

		private boolean isLoading = false;

		@Override
		public void setModellingElement(T object) {
			if (object != null) {
				setObjectReference(new FlexoObjectReference<T>(object));
			} else {
				setObjectReference(null);
			}
		}

		@Override
		public synchronized T getModellingElement() {
			if (isLoading) {
				return null;
			} else if (getObjectReference() != null) {
				isLoading = true;
				T returned = getObjectReference().getObject(true);
				if (returned == null) {
					logger.warning("Could not retrieve object " + getObjectReference());
				}
				isLoading = false;
				return returned;
			}
			isLoading = false;
			return null;
		}

		@Override
		public String toString() {
			return "ModelObjectActorReference [" + getRoleName() + "] " + Integer.toHexString(hashCode()) + " references "
					+ getModellingElement() + "[reference: " + getObjectReference() + "]";
		}
	}
}
