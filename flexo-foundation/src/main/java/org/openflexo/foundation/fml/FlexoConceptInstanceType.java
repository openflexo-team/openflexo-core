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
import java.util.logging.Logger;

import org.openflexo.antar.binding.CustomType;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.logging.FlexoLogger;

/**
 * Represent the type of a FlexoConceptInstance of a given FlexoConcept
 * 
 * @author sylvain
 * 
 */
public class FlexoConceptInstanceType implements CustomType {

	protected FlexoConcept flexoConcept;

	protected static final Logger logger = FlexoLogger.getLogger(FlexoConceptInstanceType.class.getPackage().getName());

	public static FlexoConceptInstanceType UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE = new FlexoConceptInstanceType(null);

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
		return "FlexoConceptInstanceType" + (flexoConcept != null ? ":" + flexoConcept.toString() : "");
	}

	@Override
	public String fullQualifiedRepresentation() {
		return "FlexoConceptInstanceType" + (flexoConcept != null ? ":" + flexoConcept.toString() : "");
	}

	@Override
	public String toString() {
		return simpleRepresentation();
	}

	public static Type getFlexoConceptInstanceType(FlexoConcept anFlexoConcept) {
		if (anFlexoConcept != null) {
			return anFlexoConcept.getInstanceType();
		} else {
			// logger.warning("Trying to get a InstanceType for a null FlexoConcept");
			return UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;
		}
	}
}
