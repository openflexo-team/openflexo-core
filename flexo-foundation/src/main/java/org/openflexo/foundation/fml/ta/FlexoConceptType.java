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

package org.openflexo.foundation.fml.ta;

import java.beans.PropertyChangeSupport;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.type.ConnieType;
import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.connie.type.WildcardTypeImpl;
import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLRTType;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FMLType;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.TechnologyAdapterTypeFactory;
import org.openflexo.foundation.fml.TechnologySpecificType;
import org.openflexo.logging.FlexoLogger;

/**
 * Represent a FML reflexive type (a FlexoConcept) <br>
 * This type is parametered with the type of the FlexoConcept modeled by this {@code FlexoConceptType} object. <br>
 * For example, the type of FlexoConcept {@code Foo} is {@code
 * Concept<Foo>}, the type of a FlexoConcept subtyping {@code Foo} is {@code
 * Concept<? extends Foo>}. Use {@code Concept<?>} if the FlexoConcept being modeled is unknown.
 * 
 * @author sylvain
 * 
 */
public class FlexoConceptType implements FMLType, TechnologySpecificType<FMLTechnologyAdapter> {

	protected static final Logger logger = FlexoLogger.getLogger(FlexoConceptType.class.getPackage().getName());

	private FMLRTType type;

	private final PropertyChangeSupport pcSupport;

	public static FlexoConceptType UNDEFINED_FLEXO_CONCEPT_TYPE = new FlexoConceptType(null);

	public interface FlexoConceptTypeFactory extends CustomTypeFactory<FlexoConceptType> {
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
			return null;
		}

		@Override
		public String toString() {
			return "FlexoConceptType";
		}

		@Override
		public void configureFactory(FlexoConceptType type) {
		}

	}

	public FlexoConceptType(FMLRTType type) {
		pcSupport = new PropertyChangeSupport(this);
		this.type = type;
	}

	public FMLRTType getType() {
		return type;
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

	@Override
	public FMLTechnologyAdapter getSpecificTechnologyAdapter() {
		if (type instanceof TechnologySpecificType) {
			return (FMLTechnologyAdapter) ((TechnologySpecificType) type).getSpecificTechnologyAdapter();
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
			return (type == null) || (TypeUtils.isTypeAssignableFrom(type, ((FlexoConceptType) aType).getType()));
		}
		return false;
	}

	@Override
	public boolean isOfType(Object object, boolean permissive) {
		if (!(object instanceof FlexoConcept)) {
			return false;
		}
		if (permissive) {
			return true;
		}
		logger.warning("TODO: isOfType() not implemented for FlexoConceptType");
		return true;
	}

	@Override
	public String simpleRepresentation() {

		if (type == null) {
			return AbstractFMLTypingSpace.CONCEPT;
		}
		return AbstractFMLTypingSpace.CONCEPT + "<" + TypeUtils.simpleRepresentation(type) + ">";
	}

	@Override
	public String fullQualifiedRepresentation() {
		return AbstractFMLTypingSpace.CONCEPT + "<" + TypeUtils.fullQualifiedRepresentation(type) + ">";
	}

	@Override
	public String toString() {
		return simpleRepresentation();
	}

	@Override
	public String getSerializationRepresentation() {
		return fullQualifiedRepresentation();
	}

	@Override
	public boolean isResolved() {
		if (type == null) {
			return true;
		}
		if (type instanceof ConnieType) {
			return ((ConnieType) type).isResolved();
		}
		return false;
	}

	@Override
	public void resolve() {
		if (type instanceof CustomType) {
			((CustomType) type).resolve();
		}
		if (type instanceof WildcardTypeImpl) {
			((WildcardTypeImpl) type).resolve();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		if (type == null) {
			if (other.type != null)
				return false;
		}
		else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

}
