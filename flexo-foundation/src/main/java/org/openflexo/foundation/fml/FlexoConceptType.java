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

import java.beans.PropertyChangeSupport;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.StringUtils;

/**
 * Represent the type of a FlexoConcept as a sub-concept of a given FlexoConcept
 * 
 * @author sylvain
 * 
 */
public class FlexoConceptType implements TechnologySpecificType<FMLTechnologyAdapter> {

	protected static final Logger logger = FlexoLogger.getLogger(FlexoConceptType.class.getPackage().getName());

	protected FlexoConcept flexoConcept;
	protected String conceptURI;

	// factory stored for unresolved types
	// should be nullified as quickly as possible (nullified when resolved)
	protected CustomTypeFactory<?> customTypeFactory;

	private final PropertyChangeSupport pcSupport;

	public static FlexoConceptType UNDEFINED_FLEXO_CONCEPT_TYPE = new FlexoConceptType((FlexoConcept) null);

	public interface FlexoConceptTypeFactory extends CustomTypeFactory<FlexoConceptType> {
		public FlexoConcept resolveFlexoConcept(FlexoConceptType typeToResolve);
	}

	/**
	 * Factory for FlexoConceptType instances
	 * 
	 * @author sylvain
	 * 
	 */
	public static class DefaultFlexoConceptTypeFactory extends TechnologyAdapterTypeFactory<FlexoConceptType, FMLTechnologyAdapter>
			implements FlexoConceptTypeFactory {

		@Override
		public Class<FlexoConceptType> getCustomType() {
			return FlexoConceptType.class;
		}

		public DefaultFlexoConceptTypeFactory(FMLTechnologyAdapter technologyAdapter) {
			super(technologyAdapter);
		}

		@Override
		public FlexoConceptType makeCustomType(String configuration) {
			FlexoConcept concept = null;

			if (configuration != null) {
				concept = getTechnologyAdapter().getTechnologyAdapterService().getServiceManager().getVirtualModelLibrary()
						.getFlexoConcept(configuration, false);
				// Do not load virtual models for that reason, resolving will be performed later
			}
			else
				concept = getFlexoConceptType();
			if (concept != null)
				return retrieveFlexoConceptType(concept);
			// We don't return UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE because we want here a mutable type
			// if FlexoConcept might be resolved later
			return new FlexoConceptType(configuration, this);
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
			return "SubConcept of FlexoConcept";
		}

		@Override
		public void configureFactory(FlexoConceptType type) {
			if (type != null) {
				setFlexoConceptType(type.getFlexoConcept());
			}
		}

		@Override
		public FlexoConcept resolveFlexoConcept(FlexoConceptType typeToResolve) {
			return getTechnologyAdapter().getTechnologyAdapterService().getServiceManager().getVirtualModelLibrary()
					.getFlexoConcept(typeToResolve.conceptURI);
		}
	}

	public FlexoConceptType(FlexoConcept anFlexoConcept) {
		pcSupport = new PropertyChangeSupport(this);
		this.flexoConcept = anFlexoConcept;
	}

	public FlexoConceptType(String flexoConceptURI, CustomTypeFactory<?> customTypeFactory) {
		pcSupport = new PropertyChangeSupport(this);
		this.conceptURI = flexoConceptURI;
		this.customTypeFactory = customTypeFactory;
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getConceptURI() {
		return conceptURI;
	}

	public FlexoConcept getFlexoConcept() {
		if (!isResolved() && customTypeFactory != null) {
			resolve(customTypeFactory);
		}
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
		return FlexoConcept.class;
	}

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");

		if (aType instanceof FlexoConceptType) {
			return (flexoConcept == null) || (flexoConcept.isAssignableFrom(((FlexoConceptType) aType).getFlexoConcept()));
		}
		return false;
	}

	@Override
	public boolean isOfType(Object object, boolean permissive) {
		if (!(object instanceof FlexoConcept)) {
			return false;
		}
		return getFlexoConcept() != null && getFlexoConcept().isAssignableFrom(((FlexoConceptInstance) object).getFlexoConcept());
	}

	@Override
	public String simpleRepresentation() {
		if (flexoConcept == null) {
			if (conceptURI != null) {
				return "UndefinedFlexoConceptType<" + conceptURI + ">";
			}
			return "UndefinedFlexoConceptType";
		}
		// IMPORTANT: do not use getFlexoConcept() here, as it may trigger too early type resolution !!!
		// return getClass().getSimpleName() + "<" + flexoConcept.getName() + ">";
		return "? extends " + flexoConcept.getName();
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
		// IMPORTANT: do not use getFlexoConcept() here, as it may trigger too early type resolution !!!
		return (flexoConcept != null ? flexoConcept.getURI() : conceptURI);
	}

	@Override
	public boolean isResolved() {
		return flexoConcept != null || StringUtils.isEmpty(conceptURI);
	}

	@Override
	public void resolve() {
		if (customTypeFactory != null) {
			resolve(customTypeFactory);
		}
	}

	protected void resolve(CustomTypeFactory<?> factory) {
		// System.out.println("******* resolve " + getSerializationRepresentation() + " with " + factory);
		if (factory instanceof FlexoConceptTypeFactory) {
			FlexoConcept concept = ((FlexoConceptTypeFactory) factory).resolveFlexoConcept(this);
			if (concept != null) {
				flexoConcept = concept;
				this.customTypeFactory = null;
			}
			else {
				this.customTypeFactory = factory;
			}
		}
	}

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
		FlexoConceptType other = (FlexoConceptType) obj;
		if (flexoConcept == null) {
			if (other.flexoConcept != null)
				return false;
		}
		else if (!flexoConcept.equals(other.flexoConcept)) {
			return false;
		}
		return true;
	}

	public static FlexoConceptType retrieveFlexoConceptType(FlexoConcept anFlexoConcept) {
		if (anFlexoConcept != null)
			return anFlexoConcept.getConceptType();
		// logger.warning("Trying to get a InstanceType for a null FlexoConcept");
		return UNDEFINED_FLEXO_CONCEPT_TYPE;
	}
}
