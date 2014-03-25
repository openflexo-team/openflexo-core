package org.openflexo.foundation.view;

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
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

	@Getter(value = OBJECT_REFERENCE_KEY, ignoreType = true)
	public FlexoObjectReference getObjectReference();

	@Setter(OBJECT_REFERENCE_KEY)
	public void setObjectReference(FlexoObjectReference objectReference);

	public static abstract class ModelObjectActorReferenceImpl<T extends FlexoObject> extends ActorReferenceImpl<T> implements
			ModelObjectActorReference<T> {

		private static final Logger logger = FlexoLogger.getLogger(ModelObjectActorReference.class.getPackage().toString());

		public T object;
		public FlexoObjectReference objectReference;

		/**
		 * Default constructor
		 */
		public ModelObjectActorReferenceImpl() {
			super();
		}

		/*public ModelObjectActorReferenceImpl(T o, FlexoRole aPatternRole, FlexoConceptInstance epi) {
			super(epi.getProject());
			setFlexoConceptInstance(epi);
			setPatternRole(aPatternRole);
			object = o;
			objectReference = new FlexoObjectReference(o, o.getProject());
		}*/

		@Override
		public void setModellingElement(T object) {
			this.object = object;
			objectReference = new FlexoObjectReference(object);
		}

		@Override
		public T getModellingElement() {
			if (object == null) {
				object = (T) objectReference.getObject(true);
			}
			if (object == null) {
				logger.warning("Could not retrieve object " + objectReference);
			}
			return object;
		}

	}
}
