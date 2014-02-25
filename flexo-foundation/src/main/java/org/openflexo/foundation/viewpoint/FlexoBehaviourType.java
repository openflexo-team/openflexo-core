package org.openflexo.foundation.viewpoint;

import java.lang.reflect.Type;

import org.openflexo.antar.binding.CustomType;

/**
 * Represent the type of a FlexoBehaviour
 * 
 * @author sylvain
 * 
 */
public class FlexoBehaviourType implements CustomType {

	public static FlexoBehaviourType getFlexoBehaviourType(FlexoBehaviour aFlexoBehaviour) {
		if (aFlexoBehaviour == null) {
			return null;
		}
		return aFlexoBehaviour.getFlexoBehaviourType();
	}

	private final FlexoBehaviour flexoBehaviour;

	public FlexoBehaviourType(FlexoBehaviour aFlexoBehaviour) {
		this.flexoBehaviour = aFlexoBehaviour;
	}

	public FlexoBehaviour getFlexoBehaviour() {
		return flexoBehaviour;
	}

	public static String getName() {
		return FlexoBehaviourType.class.getSimpleName();
	}

	@Override
	public Class getBaseClass() {
		return FlexoBehaviour.class;
	}

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");
		if (aType instanceof FlexoBehaviourType) {
			return getFlexoBehaviour() == (((FlexoBehaviourType) aType).getFlexoBehaviour());
		}
		return false;
	}

	@Override
	public String simpleRepresentation() {
		return "FlexoBehaviourType" + ":" + getFlexoBehaviour();
	}

	@Override
	public String fullQualifiedRepresentation() {
		return "FlexoBehaviourType" + ":" + getFlexoBehaviour();
	}

	@Override
	public String toString() {
		return simpleRepresentation();
	}
}