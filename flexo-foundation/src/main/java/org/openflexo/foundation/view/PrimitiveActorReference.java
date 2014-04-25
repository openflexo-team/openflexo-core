package org.openflexo.foundation.view;

import java.util.logging.Logger;

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
 * Implements {@link ActorReference} for primitive types as modelling elements.<br>
 * 
 * @author sylvain
 * 
 * @param <T>
 */
@ModelEntity
@ImplementationClass(PrimitiveActorReference.PrimitiveActorReferenceImpl.class)
@XMLElement
public interface PrimitiveActorReference<T> extends ActorReference<T> {

	@PropertyIdentifier(type = FlexoObjectReference.class)
	public static final String VALUE_KEY = "value";

	@Getter(value = VALUE_KEY, isStringConvertable = true)
	@XMLAttribute
	public T getValue();

	@Setter(VALUE_KEY)
	public void setValue(T value);

	public static abstract class PrimitiveActorReferenceImpl<T> extends ActorReferenceImpl<T> implements PrimitiveActorReference<T> {

		private static final Logger logger = FlexoLogger.getLogger(PrimitiveActorReference.class.getPackage().toString());

		@Override
		public void setModellingElement(T object) {
			setValue(object);
		}

		@Override
		public T getModellingElement() {
			return getValue();
		}

	}
}
