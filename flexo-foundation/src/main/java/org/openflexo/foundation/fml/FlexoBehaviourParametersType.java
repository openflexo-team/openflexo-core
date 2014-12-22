package org.openflexo.foundation.fml;

import java.lang.reflect.Type;
import java.util.List;

import org.openflexo.antar.binding.CustomType;

/**
 * Represent the type of the list of all parameters of an FlexoBehaviour (definition layer)
 * 
 * @author sylvain
 * 
 */
public class FlexoBehaviourParametersType implements CustomType {

	public static FlexoBehaviourParametersType getFlexoBehaviourParametersType(FlexoBehaviour aFlexoBehaviour) {
		if (aFlexoBehaviour == null) {
			return null;
		}
		return aFlexoBehaviour.getFlexoBehaviourParametersType();
	}

	private FlexoBehaviour flexoBehaviour;

	public FlexoBehaviourParametersType(FlexoBehaviour aFlexoBehaviour) {
		this.flexoBehaviour = aFlexoBehaviour;
	}

	public FlexoBehaviour getFlexoBehaviour() {
		return flexoBehaviour;
	}

	@Override
	public Class<List> getBaseClass() {
		return List.class;
	}

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");
		if (aType instanceof FlexoBehaviourParametersType) {
			return getFlexoBehaviour() == (((FlexoBehaviourParametersType) aType).getFlexoBehaviour());
		}
		return false;
	}

	@Override
	public String simpleRepresentation() {
		return "FlexoBehaviourParametersType" + ":" + getFlexoBehaviour();
	}

	@Override
	public String fullQualifiedRepresentation() {
		return "FlexoBehaviourParametersType" + ":" + getFlexoBehaviour();
	}

	@Override
	public String toString() {
		return simpleRepresentation();
	}
}