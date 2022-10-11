/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.FlexoEnumInstance;

/**
 * Represent the type of an instance of a {@link FlexoEnum}
 * 
 * @author sylvain
 * 
 */
public class FlexoEnumType extends FlexoConceptInstanceType {

	public static FlexoEnumType UNDEFINED_FLEXO_ENUM_TYPE = new FlexoEnumType((FlexoEnum) null);

	public FlexoEnumType(FlexoEnum aFlexoEnum) {
		super(aFlexoEnum);
	}

	public FlexoEnumType(String flexoEnumURI, CustomTypeFactory<?> factory) {
		super(flexoEnumURI, factory);
	}

	@Override
	public Class<?> getBaseClass() {
		return FlexoEnumInstance.class;
	}

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");

		if (aType instanceof FlexoEnumType) {
			return (flexoConcept == null) || (flexoConcept.isAssignableFrom(((FlexoEnumType) aType).getFlexoConcept()));
		}
		
		if (permissive && aType.equals(FlexoEnumInstance.class)) {
			return true;
		}
		
		return false;
	}


	@Override
	public FlexoEnum getFlexoConcept() {
		return (FlexoEnum) super.getFlexoConcept();
	}

	public FlexoEnum getFlexoEnum() {
		return getFlexoConcept();
	}

	public static FlexoEnumType getFlexoEnumType(FlexoEnum aFlexoEnum) {
		if (aFlexoEnum != null) {
			return aFlexoEnum.getInstanceType();
		}
		return UNDEFINED_FLEXO_ENUM_TYPE;
	}

	@Override
	public void resolve(CustomTypeFactory<?> factory) {
		if (factory instanceof FlexoEnumTypeFactory) {
			FlexoConcept concept = ((FlexoEnumTypeFactory) factory).getTechnologyAdapter().getTechnologyAdapterService().getServiceManager()
					.getVirtualModelLibrary().getFlexoConcept(conceptURI);
			if (concept != null) {
				flexoConcept = concept;
				this.customTypeFactory = null;
			}
			else {
				this.customTypeFactory = factory;
			}
		}
	}

	/**
	 * Factory for FlexoConceptInstanceType instances
	 * 
	 * @author sylvain
	 * 
	 */
	public static class FlexoEnumTypeFactory extends TechnologyAdapterTypeFactory<FlexoEnumType, FMLTechnologyAdapter> {

		@Override
		public Class<FlexoEnumType> getCustomType() {
			return FlexoEnumType.class;
		}

		public FlexoEnumTypeFactory(FMLTechnologyAdapter technologyAdapter) {
			super(technologyAdapter);
		}

		@Override
		public FlexoEnumType makeCustomType(String configuration) {

			FlexoEnum concept = null;

			if (configuration != null) {
				concept = (FlexoEnum) getTechnologyAdapter().getTechnologyAdapterService().getServiceManager().getVirtualModelLibrary()
						.getFlexoConcept(configuration, false);

				// Do not load virtual models for that reason, resolving will be performed later
			}
			else {
				concept = getFlexoEnum();
			}

			if (concept != null)
				return getFlexoEnumType(concept);
			// We don't return UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE because we want here a mutable type
			// if FlexoConcept might be resolved later
			return new FlexoEnumType(configuration, this);
		}

		private FlexoEnum flexoEnum;

		public FlexoEnum getFlexoEnum() {
			return flexoEnum;
		}

		public void setFlexoEnum(FlexoEnum flexoEnum) {
			if (flexoEnum != this.flexoEnum) {
				FlexoConcept oldFlexoConceptType = this.flexoEnum;
				this.flexoEnum = flexoEnum;
				getPropertyChangeSupport().firePropertyChange("flexoEnum", oldFlexoConceptType, flexoEnum);
			}
		}

		@Override
		public String toString() {
			return "Flexo enumeration";
		}

		@Override
		public void configureFactory(FlexoEnumType type) {
			if (type != null) {
				setFlexoEnum(type.getFlexoEnum());
			}
		}
	}

}
