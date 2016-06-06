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

import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.StringUtils;

/**
 * Represent the type of a FlexoConceptInstance of a given FlexoConcept
 * 
 * @author sylvain
 * 
 */
public class FlexoConceptInstanceType implements TechnologySpecificType<FMLTechnologyAdapter> {

	protected FlexoConcept flexoConcept;
	protected String conceptURI;

	protected static final Logger logger = FlexoLogger.getLogger(FlexoConceptInstanceType.class.getPackage().getName());

	public static FlexoConceptInstanceType UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE = new FlexoConceptInstanceType((FlexoConcept) null);

	/**
	 * Factory for FlexoConceptInstanceType instances
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/CustomType/FlexoConceptInstanceTypeFactory.fib")
	public static class FlexoConceptInstanceTypeFactory extends TechnologyAdapterTypeFactory<FlexoConceptInstanceType> {

		@Override
		public Class<FlexoConceptInstanceType> getCustomType() {
			return FlexoConceptInstanceType.class;
		}

		public FlexoConceptInstanceTypeFactory(FMLRTTechnologyAdapter technologyAdapter) {
			super(technologyAdapter);
		}

		@Override
		public FlexoConceptInstanceType makeCustomType(String configuration) {

			FlexoConcept concept = null;

			if (configuration != null) {
				concept = getTechnologyAdapter().getTechnologyAdapterService().getServiceManager().getViewPointLibrary()
						.getFlexoConcept(configuration);
			}
			else {
				concept = getFlexoConceptType();
			}

			if (concept != null) {
				return getFlexoConceptInstanceType(concept);
			}
			else {
				// We don't return UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE because we want here a mutable type
				// if FlexoConcept might be resolved later
				return new FlexoConceptInstanceType(configuration);
			}
		}

		private FlexoConcept flexoConceptType;

		public FlexoConcept getFlexoConceptType() {
			return flexoConceptType;
		}

		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			if (flexoConceptType != this.flexoConceptType) {
				FlexoConcept oldFlexoConceptType = this.flexoConceptType;
				this.flexoConceptType = flexoConceptType;
				getPropertyChangeSupport().firePropertyChange("flexoConceptType", oldFlexoConceptType, flexoConceptType);
			}
		}

		@Override
		public String toString() {
			return "Instance of FlexoConcept";
		}

		@Override
		public void configureFactory(FlexoConceptInstanceType type) {
			if (type != null) {
				setFlexoConceptType(type.getFlexoConcept());
			}
		}
	}

	public FlexoConceptInstanceType(FlexoConcept anFlexoConcept) {
		this.flexoConcept = anFlexoConcept;
	}

	protected FlexoConceptInstanceType(String flexoConceptURI) {
		this.conceptURI = flexoConceptURI;
	}

	public FlexoConcept getFlexoConcept() {
		return flexoConcept;
	}

	@Override
	public FMLTechnologyAdapter getSpecificTechnologyAdapter() {
		if (flexoConcept != null) {
			return flexoConcept.getTechnologyAdapter();
		}
		return null;
	}

	@Override
	public Class<?> getBaseClass() {
		return FlexoConceptInstance.class;
	}

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");

		if (aType instanceof FlexoConceptInstanceType) {
			return (flexoConcept == null) || (flexoConcept.isAssignableFrom(((FlexoConceptInstanceType) aType).getFlexoConcept()));
		}
		return false;
	}

	@Override
	public String simpleRepresentation() {
		return getClass().getSimpleName() + "<" + (flexoConcept != null ? flexoConcept.getName() : "") + ">";
	}

	@Override
	public String fullQualifiedRepresentation() {
		return getClass().getName() + "<" + getSerializationRepresentation() + ">";
	}

	@Override
	public String toString() {
		return simpleRepresentation();
	}

	@Override
	public String getSerializationRepresentation() {
		return (flexoConcept != null ? flexoConcept.getURI() : conceptURI);
	}

	@Override
	public boolean isResolved() {
		return flexoConcept != null || StringUtils.isEmpty(conceptURI);
	}

	@Override
	public void resolve(CustomTypeFactory<?> factory) {
		if (factory instanceof FlexoConceptInstanceTypeFactory) {
			FlexoConcept concept = ((FlexoConceptInstanceTypeFactory) factory).getTechnologyAdapter().getTechnologyAdapterService()
					.getServiceManager().getViewPointLibrary().getFlexoConcept(conceptURI);
			if (concept != null) {
				flexoConcept = concept;
			}
		}
	}

	/*@Override
	public void setSerializedConfiguration(String aConfiguration, ModelFactory factory) {
		System.out.println("Tiens, faudrait que je puisse configurer " + this + " avec " + aConfiguration + " for " + factory);
	}*/

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((flexoConcept == null) ? 0 : flexoConcept.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FlexoConceptInstanceType other = (FlexoConceptInstanceType) obj;
		if (flexoConcept == null) {
			if (other.flexoConcept != null)
				return false;
		}
		else if (!flexoConcept.equals(other.flexoConcept)) {
			return false;
		}
		return true;
	}

	public static FlexoConceptInstanceType getFlexoConceptInstanceType(FlexoConcept anFlexoConcept) {
		if (anFlexoConcept != null) {
			return anFlexoConcept.getInstanceType();
		}
		else {
			// logger.warning("Trying to get a InstanceType for a null FlexoConcept");
			return UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;
		}
	}

}
