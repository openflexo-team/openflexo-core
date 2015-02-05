/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */


package org.openflexo.foundation.fml;

import java.lang.reflect.Type;

import org.openflexo.connie.type.CustomType;

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
