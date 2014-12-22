package org.openflexo.foundation.fml;

import java.lang.reflect.Type;
import java.util.Hashtable;

import org.openflexo.antar.binding.CustomType;

/**
 * Represent the type of the list of all parameters of an FlexoBehaviour (run-time). Internal representation is given by an
 * Hashtable<FlexoBehaviourParameter,Object)
 * 
 * @author sylvain
 * 
 */
public class FlexoBehaviourParametersValuesType implements CustomType {

	public static FlexoBehaviourParametersValuesType getFlexoBehaviourParametersValuesType(FlexoBehaviour aFlexoBehaviour) {
		if (aFlexoBehaviour == null) {
			return null;
		}
		return aFlexoBehaviour.getFlexoBehaviourParametersValuesType();
	}

	private FlexoBehaviour flexoBehaviour;

	public FlexoBehaviourParametersValuesType(FlexoBehaviour aFlexoBehaviour) {
		this.flexoBehaviour = aFlexoBehaviour;
	}

	public FlexoBehaviour getFlexoBehaviour() {
		return flexoBehaviour;
	}

	@Override
	public Class getBaseClass() {
		return Hashtable.class;
	}

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");
		if (aType instanceof FlexoBehaviourParametersValuesType) {
			return getFlexoBehaviour() == (((FlexoBehaviourParametersValuesType) aType).getFlexoBehaviour());
		}
		return false;
	}

	@Override
	public String simpleRepresentation() {
		return "FlexoBehaviourParametersValuesType" + ":" + getFlexoBehaviour();
	}

	@Override
	public String fullQualifiedRepresentation() {
		return "FlexoBehaviourParametersValuesType" + ":" + getFlexoBehaviour();
	}

	@Override
	public String toString() {
		return simpleRepresentation();
	}
}