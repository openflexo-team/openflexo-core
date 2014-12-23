package org.openflexo.foundation.fml;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.PrimitiveActorReference;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceModelFactory;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(PrimitiveRole.PrimitiveRoleImpl.class)
@XMLElement
public interface PrimitiveRole<T> extends FlexoRole<T> {

	public static enum PrimitiveType {
		Boolean, String, LocalizedString, Integer, Float
	}

	@PropertyIdentifier(type = PrimitiveType.class)
	public static final String PRIMITIVE_TYPE_KEY = "primitiveType";

	@Getter(value = PRIMITIVE_TYPE_KEY)
	@XMLAttribute
	public PrimitiveType getPrimitiveType();

	@Setter(PRIMITIVE_TYPE_KEY)
	public void setPrimitiveType(PrimitiveType primitiveType);

	public static abstract class PrimitiveRoleImpl<T> extends FlexoRoleImpl<T> implements PrimitiveRole<T> {

		protected static final Logger logger = FlexoLogger.getLogger(PrimitiveRole.class.getPackage().getName());

		private PrimitiveType primitiveType;

		public PrimitiveRoleImpl() {
			super();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("FlexoRole " + getName() + " as " + getPreciseType() + ";", context);
			return out.toString();
		}

		@Override
		public PrimitiveType getPrimitiveType() {
			return primitiveType;
		}

		@Override
		public void setPrimitiveType(PrimitiveType primitiveType) {
			if (requireChange(getPrimitiveType(), primitiveType)) {
				PrimitiveType oldValue = this.primitiveType;
				this.primitiveType = primitiveType;
				notifyChange(PRIMITIVE_TYPE_KEY, oldValue, primitiveType);
			}
		}

		@Override
		public String getPreciseType() {
			if (primitiveType == null) {
				return null;
			}
			switch (primitiveType) {
			case String:
				return FlexoLocalization.localizedForKey("string");
			case LocalizedString:
				return FlexoLocalization.localizedForKey("localized_string");
			case Boolean:
				return FlexoLocalization.localizedForKey("boolean");
			case Integer:
				return FlexoLocalization.localizedForKey("integer");
			case Float:
				return FlexoLocalization.localizedForKey("float");
			default:
				return null;
			}
		}

		@Override
		public Type getType() {
			if (primitiveType == null) {
				return null;
			}
			switch (primitiveType) {
			case String:
				return String.class;
			case LocalizedString:
				return String.class;
			case Boolean:
				return Boolean.TYPE;
			case Integer:
				return Integer.TYPE;
			case Float:
				return Float.TYPE;
			default:
				return null;
			}
		}

		/*@Override
		public boolean getIsPrimaryRole() {
			return false;
		}

		@Override
		public void setIsPrimaryRole(boolean isPrimary) {
			// Not relevant
		}*/

		/**
		 * Encodes the default cloning strategy
		 * 
		 * @return
		 */
		@Override
		public RoleCloningStrategy defaultCloningStrategy() {
			return RoleCloningStrategy.Clone;
		}

		@Override
		public boolean defaultBehaviourIsToBeDeleted() {
			return true;
		}

		@Override
		public ActorReference<T> makeActorReference(T object, FlexoConceptInstance fci) {
			VirtualModelInstanceModelFactory factory = fci.getFactory();
			PrimitiveActorReference<T> returned = factory.newInstance(PrimitiveActorReference.class);
			returned.setFlexoRole(this);
			returned.setModellingElement(object);
			return returned;
		}

	}
}
