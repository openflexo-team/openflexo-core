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

/**
 * Represent the type of a View of a given ViewPoint
 * 
 * @author sylvain
 * 
 */
public class ViewType implements CustomType {

	protected ViewPoint viewPoint;

	public ViewType(ViewPoint aViewPoint) {
		this.viewPoint = aViewPoint;
	}

	public ViewPoint getViewPoint() {
		return viewPoint;
	}

	@Override
	public Class getBaseClass() {
		return ViewPoint.class;
	}

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");
		if (aType instanceof ViewType) {
			return viewPoint == ((ViewType) aType).getViewPoint();
		}
		return false;
	}

	@Override
	public String simpleRepresentation() {
		return "ViewType" + ":" + viewPoint.getName();
	}

	@Override
	public String fullQualifiedRepresentation() {
		return "ViewType" + ":" + viewPoint.getURI();
	}

	@Override
	public String toString() {
		return simpleRepresentation();
	}

	public static Type getViewType(ViewPoint viewPoint) {
		if (viewPoint != null) {
			return viewPoint.getViewType();
		} else {
			return null;
		}
	}
}