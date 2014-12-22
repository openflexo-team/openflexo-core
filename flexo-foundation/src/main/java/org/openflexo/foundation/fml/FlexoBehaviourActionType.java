package org.openflexo.foundation.fml;

import java.lang.reflect.Type;

import org.openflexo.antar.binding.CustomType;
import org.openflexo.foundation.fmlrt.action.FlexoBehaviourAction;

/**
 * Represent the type of an FlexoBehaviour
 * 
 * @author sylvain
 * 
 */
public class FlexoBehaviourActionType implements CustomType {

	public static FlexoBehaviourActionType getFlexoBehaviourActionType(FlexoBehaviour aFlexoBehaviour) {
		if (aFlexoBehaviour == null) {
			return null;
		}
		return aFlexoBehaviour.getFlexoBehaviourActionType();
	}

	private FlexoBehaviour flexoBehaviour;

	public FlexoBehaviourActionType(FlexoBehaviour aFlexoBehaviour) {
		this.flexoBehaviour = aFlexoBehaviour;
	}

	public FlexoBehaviour getFlexoBehaviour() {
		return flexoBehaviour;
	}

	@Override
	public Class getBaseClass() {
		return FlexoBehaviourAction.class;
	}

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");
		if (aType instanceof FlexoBehaviourActionType) {
			return getFlexoBehaviour() == (((FlexoBehaviourActionType) aType).getFlexoBehaviour());
		}
		return false;
	}

	@Override
	public String simpleRepresentation() {
		return "FlexoBehaviourActionType" + ":" + getFlexoBehaviour();
	}

	@Override
	public String fullQualifiedRepresentation() {
		return "FlexoBehaviourActionType" + ":" + getFlexoBehaviour();
	}

	@Override
	public String toString() {
		return simpleRepresentation();
	}
}