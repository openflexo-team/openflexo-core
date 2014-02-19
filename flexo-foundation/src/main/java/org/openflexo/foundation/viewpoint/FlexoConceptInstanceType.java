/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2013 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openflexo.foundation.viewpoint;

import java.lang.reflect.Type;

import org.openflexo.antar.binding.CustomType;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.VirtualModelInstance;

/**
 * Represent the type of a FlexoConceptInstance of a given FlexoConcept
 * 
 * @author sylvain
 * 
 */
public class FlexoConceptInstanceType implements CustomType {

	protected FlexoConcept flexoConcept;

	public FlexoConceptInstanceType(FlexoConcept anFlexoConcept) {
		this.flexoConcept = anFlexoConcept;
	}

	public FlexoConcept getFlexoConcept() {
		return flexoConcept;
	}

	@Override
	public Class getBaseClass() {
		if (getFlexoConcept() instanceof VirtualModel) {
			return VirtualModelInstance.class;
		} else {
			return FlexoConceptInstance.class;
		}
	}

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");
		if (aType instanceof FlexoConceptInstanceType) {
			return flexoConcept.isAssignableFrom(((FlexoConceptInstanceType) aType).getFlexoConcept());
		}
		return false;
	}

	@Override
	public String simpleRepresentation() {
		return "FlexoConceptInstanceType" + ":" + flexoConcept.toString();
	}

	@Override
	public String fullQualifiedRepresentation() {
		return "FlexoConceptInstanceType" + ":" + flexoConcept.toString();
	}

	@Override
	public String toString() {
		return simpleRepresentation();
	}

	public static Type getFlexoConceptInstanceType(FlexoConcept anFlexoConcept) {
		if (anFlexoConcept != null && anFlexoConcept.getViewPoint() != null) {
			return anFlexoConcept.getViewPoint().getInstanceType(anFlexoConcept);
		} else {
			return null;
		}
	}
}