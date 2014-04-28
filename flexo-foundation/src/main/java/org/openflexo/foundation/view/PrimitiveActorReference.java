package org.openflexo.foundation.view;

import java.util.logging.Logger;

import org.openflexo.foundation.viewpoint.PrimitiveRole;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.exceptions.InvalidDataException;

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

	@PropertyIdentifier(type = String.class)
	public static final String VALUE_AS_STRING_KEY = "valueAsString";

	@Getter(value = VALUE_AS_STRING_KEY)
	@XMLAttribute
	public String getValueAsString();

	@Setter(VALUE_AS_STRING_KEY)
	public void setValueAsString(String value);

	@Override
	public PrimitiveRole<T> getFlexoRole();

	public static abstract class PrimitiveActorReferenceImpl<T> extends ActorReferenceImpl<T> implements PrimitiveActorReference<T> {

		private static final Logger logger = FlexoLogger.getLogger(PrimitiveActorReference.class.getPackage().toString());

		private T modellingElement = null;

		@Override
		public PrimitiveRole<T> getFlexoRole() {
			return (PrimitiveRole<T>) super.getFlexoRole();
		}

		@Override
		public void setModellingElement(T object) {
			modellingElement = object;
		}

		@Override
		public T getModellingElement() {
			if (modellingElement == null && getValueAsString() != null && getFactory() != null) {
				try {
					modellingElement = getFactory().getStringEncoder().fromString(getActorClass(), getValueAsString());
				} catch (InvalidDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return modellingElement;
		}

		@Override
		public String getValueAsString() {
			if (modellingElement != null && getFactory() != null) {
				try {
					return getFactory().getStringEncoder().toString(modellingElement);
				} catch (InvalidDataException e) {
					e.printStackTrace();
				}
			}
			return (String) performSuperGetter(VALUE_AS_STRING_KEY);
		}

		@Override
		public Class<? extends T> getActorClass() {
			switch (getFlexoRole().getPrimitiveType()) {
			case String:
			case LocalizedString:
				return (Class<? extends T>) String.class;
			case Boolean:
				return (Class<? extends T>) Boolean.class;
			case Float:
				return (Class<? extends T>) Float.class;
			case Integer:
				return (Class<? extends T>) Integer.class;
			default:
				return (Class<? extends T>) Object.class;
			}
		}
	}
}
