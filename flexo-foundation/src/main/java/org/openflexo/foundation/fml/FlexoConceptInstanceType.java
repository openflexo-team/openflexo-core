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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.logging.Logger;

import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.connie.type.TypingSpace;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.FlexoEventInstance;
import org.openflexo.foundation.technologyadapter.SpecificTypeInfo;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.toolbox.StringUtils;

/**
 * Represent the type of a FlexoConceptInstance of a given FlexoConcept
 * 
 * @author sylvain
 * 
 */
public class FlexoConceptInstanceType implements FMLRTType, TechnologySpecificType<FMLRTTechnologyAdapter>, PropertyChangeListener {

	protected FlexoConcept flexoConcept;
	protected String conceptURI;

	// factory stored for unresolved types
	protected CustomTypeFactory<?> customTypeFactory;

	private final PropertyChangeSupport pcSupport;

	protected static final Logger logger = FlexoLogger.getLogger(FlexoConceptInstanceType.class.getPackage().getName());

	public static FlexoConceptInstanceType UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE = new FlexoConceptInstanceType((FlexoConcept) null);

	public interface FlexoConceptInstanceTypeFactory extends CustomTypeFactory<FlexoConceptInstanceType> {
		public FlexoConcept resolveFlexoConcept(FlexoConceptInstanceType typeToResolve);
	}

	/**
	 * Factory for FlexoConceptInstanceType instances
	 * 
	 * @author sylvain
	 * 
	 */
	public static class DefaultFlexoConceptInstanceTypeFactory extends
			TechnologyAdapterTypeFactory<FlexoConceptInstanceType, FMLTechnologyAdapter> implements FlexoConceptInstanceTypeFactory {

		@Override
		public Class<FlexoConceptInstanceType> getCustomType() {
			return FlexoConceptInstanceType.class;
		}

		public DefaultFlexoConceptInstanceTypeFactory(FMLTechnologyAdapter technologyAdapter) {
			super(technologyAdapter);
		}

		@Override
		public FlexoConceptInstanceType makeCustomType(String configuration) {

			if ("null".equals(configuration)) {
				configuration = null;
			}

			FlexoConcept concept = null;

			if (configuration != null) {
				concept = getTechnologyAdapter().getTechnologyAdapterService().getServiceManager().getVirtualModelLibrary()
						.getFlexoConcept(configuration, false);
				// Do not load virtual models for that reason, resolving will be performed later

			}
			else
				concept = getFlexoConceptType();
			if (concept != null)
				return getFlexoConceptInstanceType(concept);
			// We don't return UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE because we want here a mutable type
			// if FlexoConcept might be resolved later
			return new FlexoConceptInstanceType(configuration, this);
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

		/*@Override
		public String toString() {
			return "Instance of FlexoConcept";
		}*/

		@Override
		public void configureFactory(FlexoConceptInstanceType type) {
			if (type != null) {
				setFlexoConceptType(type.getFlexoConcept());
			}
		}

		@Override
		public FlexoConcept resolveFlexoConcept(FlexoConceptInstanceType typeToResolve) {
			return getTechnologyAdapter().getTechnologyAdapterService().getServiceManager().getVirtualModelLibrary()
					.getFlexoConcept(typeToResolve.conceptURI);
		}
	}

	public FlexoConceptInstanceType(FlexoConcept aFlexoConcept) {
		pcSupport = new PropertyChangeSupport(this);
		setFlexoConcept(aFlexoConcept);
		// System.out.println("Created: FlexoConceptInstanceType-[" + Integer.toHexString(super.hashCode()) + "]");
		// Thread.dumpStack();
	}

	public FlexoConceptInstanceType(String flexoConceptURI, CustomTypeFactory<?> customTypeFactory) {
		pcSupport = new PropertyChangeSupport(this);
		this.conceptURI = flexoConceptURI;
		this.customTypeFactory = customTypeFactory;
		// System.out.println("Created: FlexoConceptInstanceType-[" + Integer.toHexString(super.hashCode()) + "] for " + flexoConceptURI);
		// Thread.dumpStack();
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

	public CustomTypeFactory<?> getCustomTypeFactory() {
		return customTypeFactory;
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

	protected void setFlexoConcept(FlexoConcept flexoConcept) {
		this.flexoConcept = flexoConcept;
		if (flexoConcept != null) {
			flexoConcept.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public FMLRTTechnologyAdapter getSpecificTechnologyAdapter() {
		if (flexoConcept != null) {
			return flexoConcept.getFMLRTTechnologyAdapter();
		}
		return null;
	}

	@Override
	public Class<?> getBaseClass() {
		if (getFlexoConcept() instanceof FlexoEvent) {
			return FlexoEventInstance.class;
		}
		return FlexoConceptInstance.class;
	}

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");

		if (aType instanceof FlexoConceptInstanceType) {
			return (flexoConcept == null) || (flexoConcept.isAssignableFrom(((FlexoConceptInstanceType) aType).getFlexoConcept()));
		}

		if (permissive && aType.equals(FlexoConceptInstance.class)) {
			return true;
		}

		if (permissive && aType.equals(FlexoEventInstance.class)) {
			return true;
		}

		if (aType instanceof FMLRTWildcardType && ((FMLRTWildcardType) aType).getUpperBounds().length == 1) {
			return isTypeAssignableFrom(((WildcardType) aType).getUpperBounds()[0], permissive);
		}

		return false;
	}

	@Override
	public boolean isOfType(Object object, boolean permissive) {
		if (!(object instanceof FlexoConceptInstance)) {
			return false;
		}
		return getFlexoConcept() != null && getFlexoConcept().isAssignableFrom(((FlexoConceptInstance) object).getFlexoConcept());
	}

	protected String getLastPath() {
		if (conceptURI != null) {
			String returned = conceptURI;
			if (returned.contains("/")) {
				returned = returned.substring(returned.lastIndexOf("/") + 1);
			}
			if (returned.contains("#")) {
				returned = returned.substring(returned.lastIndexOf("#") + 1);
			}
			if (returned.endsWith(".fml")) {
				returned = returned.substring(0, returned.length() - 4);
			}
			return returned;
		}
		return null;
	}

	@Override
	public String simpleRepresentation() {
		if (flexoConcept == null) {
			if (getLastPath() != null) {
				return getLastPath();
			}
			return "UndefinedFlexoConceptInstanceType(" + getConceptURI() + ")";
		}
		// IMPORTANT: do not use getFlexoConcept() here, as it may trigger too early type resolution !!!
		// return getClass().getSimpleName() + "<" + flexoConcept.getName() + ">";
		return flexoConcept.getName();
	}

	@Override
	public String fullQualifiedRepresentation() {
		return getClass().getName() + "<" + getSerializationRepresentation() + ">";
	}

	@Override
	public String toString() {
		return simpleRepresentation() /*+ "-[" + Integer.toHexString(super.hashCode()) + "]"*/;
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
		if (flexoConcept == null && StringUtils.isNotEmpty(conceptURI) && customTypeFactory != null) {
			resolve(customTypeFactory);
		}
	}

	protected void resolve(CustomTypeFactory<?> factory) {
		// System.out.println("******* resolve " + getSerializationRepresentation() + " with " + factory);
		if (factory instanceof FlexoConceptInstanceTypeFactory) {
			FlexoConcept concept = ((FlexoConceptInstanceTypeFactory) factory).resolveFlexoConcept(this);
			if (concept != null) {
				setFlexoConcept(concept);
				// We dont nullify customTypeFactory anymore, since we need it for type translating
				// this.customTypeFactory = null;
				getPropertyChangeSupport().firePropertyChange(TYPE_CHANGED, false, true);
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
		if (flexoConcept == null) {
			result = prime * result + ((conceptURI == null) ? 0 : conceptURI.hashCode());
		}
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
		else if (!flexoConcept.equals(other.flexoConcept))
			return false;
		if (flexoConcept == null && other.flexoConcept == null) {
			if (conceptURI == null) {
				if (other.conceptURI != null)
					return false;
			}
			else if (!conceptURI.equals(other.conceptURI))
				return false;
		}
		return true;
	}

	public static FlexoConceptInstanceType getFlexoConceptInstanceType(FlexoConcept anFlexoConcept) {
		if (anFlexoConcept != null)
			return anFlexoConcept.getInstanceType();
		// logger.warning("Trying to get a InstanceType for a null FlexoConcept");
		return UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;
	}

	/**
	 * Return a new {@link FlexoConceptInstanceType} by translating current {@link FlexoConceptInstanceType} into the typing context denoted
	 * by supplied {@link TypingSpace}.<br>
	 * 
	 * We retrieve here {@link FlexoConcept} from supplied TypingSpace using its name
	 * 
	 * @param typingSpace
	 * @return
	 */
	@Override
	public FlexoConceptInstanceType translateTo(TypingSpace typingSpace) {
		String conceptName;
		if (isResolved() && getFlexoConcept() != null) {
			conceptName = getFlexoConcept().getName();
		}
		else {
			conceptName = getConceptURI();
		}
		Type returned = typingSpace.resolveType(conceptName);

		if (returned instanceof FlexoConceptInstanceType) {
			// Type was found and looked up
			return (FlexoConceptInstanceType) returned;
		}

		if (getCustomTypeFactory() != null) {
			// When not found, return a type which may be resolved later
			returned = new FlexoConceptInstanceType(conceptName, getCustomTypeFactory());
		}
		else if (typingSpace instanceof FMLTypingSpace) {
			// No factory, create one using FMLCompilationUnit
			returned = new FlexoConceptInstanceType(conceptName,
					new CompilationUnitFlexoConceptInstanceTypeFactory(((FMLTypingSpace) typingSpace).getFMLCompilationUnit()));
		}
		else {
			logger.warning("Cannot translate type with such a TypingSpace: " + typingSpace);
		}

		if (typingSpace instanceof FMLTypingSpace && returned instanceof FlexoConceptInstanceType
				&& !((FlexoConceptInstanceType) returned).isResolved()) {
			((FMLTypingSpace) typingSpace).addToTypesToResolve((FlexoConceptInstanceType) returned);
		}
		return (FlexoConceptInstanceType) returned;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof FlexoConcept) {
			// We listen here to the FlexoConcept which this type refers to
			// We need to notify a TYPE_CHANGED event for all objects listening to that type
			if (evt.getPropertyName().equals(FlexoConcept.PARENT_FLEXO_CONCEPTS_KEY)
					|| evt.getPropertyName().equals(FlexoConcept.ACCESSIBLE_PROPERTIES_KEY)
					|| evt.getPropertyName().equals(FlexoConcept.ACCESSIBLE_BEHAVIOURS_KEY)
					|| evt.getPropertyName().equals(FlexoConcept.CONTAINER_FLEXO_CONCEPT_KEY)) {
				// TODO: maybe other properties may involve TYPE_CHANGED ???
				getPropertyChangeSupport().firePropertyChange(TYPE_CHANGED, false, true);
			}
		}
	}

	@Override
	public void registerSpecificTypeInfo(SpecificTypeInfo<FMLRTTechnologyAdapter> typeInfo) {
		// Not relevant here
	}

}
