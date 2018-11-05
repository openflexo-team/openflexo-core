/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.foundation.ontology;

import java.lang.reflect.Type;

import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.foundation.fml.TechnologyAdapterTypeFactory;
import org.openflexo.foundation.fml.TechnologySpecificType;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.ontology.technologyadapter.FlexoOntologyTechnologyContextManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.foundation.utils.FlexoObjectReference.ReferenceOwner;

public class SubClassOfClass<TA extends TechnologyAdapter> implements TechnologySpecificType<TA> {

	/**
	 * Factory for SubClassOfClass instances
	 * 
	 * @author sylvain
	 * 
	 */
	public static class SubClassOfClassTypeFactory extends TechnologyAdapterTypeFactory<SubClassOfClass<?>> implements ReferenceOwner {

		public SubClassOfClassTypeFactory(FMLRTTechnologyAdapter technologyAdapter) {
			super(technologyAdapter);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Class<SubClassOfClass<?>> getCustomType() {
			return (Class) SubClassOfClass.class;
		}

		@Override
		public SubClassOfClass<?> makeCustomType(String configuration) {

			FlexoObjectReference<IFlexoOntologyClass<?>> reference = new FlexoObjectReference<>(configuration, this);

			IFlexoOntologyClass<?> ontologyClass = reference.getObject();

			if (ontologyClass != null) {
				return getSubClassOfClass(ontologyClass);
			}
			return null;
		}

		@Override
		public void configureFactory(SubClassOfClass<?> type) {
			// TODO Auto-generated method stub
		}

		@Override
		public void notifyObjectLoaded(FlexoObjectReference<?> reference) {
		}

		@Override
		public void objectCantBeFound(FlexoObjectReference<?> reference) {
		}

		@Override
		public void objectDeleted(FlexoObjectReference<?> reference) {
		}

		@Override
		public void objectSerializationIdChanged(FlexoObjectReference<?> reference) {
		}

	}

	public static <TA extends TechnologyAdapter> SubClassOfClass<TA> getSubClassOfClass(IFlexoOntologyClass<TA> anOntologyClass) {
		if (anOntologyClass == null) {
			return null;
		}
		return ((FlexoOntologyTechnologyContextManager<TA>) anOntologyClass.getTechnologyAdapter().getTechnologyContextManager())
				.getSubClassOfClass(anOntologyClass);
	}

	private final IFlexoOntologyClass<TA> ontologyClass;

	public SubClassOfClass(IFlexoOntologyClass<TA> anOntologyClass) {
		this.ontologyClass = anOntologyClass;
	}

	public IFlexoOntologyClass<TA> getOntologyClass() {
		if (ontologyClass != null) {
			return ontologyClass;
		}
		return null;
	}

	@Override
	public Class<?> getBaseClass() {
		return IFlexoOntologyClass.class;
	}

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");
		if (aType instanceof SubClassOfClass) {
			return ontologyClass.isSuperConceptOf(((SubClassOfClass<TA>) aType).getOntologyClass());
		}
		return false;
	}

	@Override
	public boolean isOfType(Object object, boolean permissive) {
		if (!(object instanceof IFlexoOntologyClass)) {
			return false;
		}
		// TODO please implement me
		return true;
	}

	@Override
	public String simpleRepresentation() {
		return getClass().getSimpleName() + "(" + (ontologyClass != null ? ontologyClass.getName() : "") + ")";
	}

	@Override
	public String fullQualifiedRepresentation() {
		return getClass().getName() + "(" + getSerializationRepresentation() + ")";
	}

	@Override
	public String getSerializationRepresentation() {
		return new FlexoObjectReference<>(ontologyClass).getStringRepresentation();
	}

	@Override
	public TA getSpecificTechnologyAdapter() {
		if (getOntologyClass() != null) {
			return getOntologyClass().getTechnologyAdapter();
		}
		return null;
	}

	@Override
	public boolean isResolved() {
		return ontologyClass != null;
	}

	@Override
	public void resolve(CustomTypeFactory<?> factory) {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ontologyClass == null) ? 0 : ontologyClass.hashCode());
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
		SubClassOfClass<?> other = (SubClassOfClass<?>) obj;
		if (ontologyClass == null) {
			if (other.ontologyClass != null)
				return false;
		}
		else if (!ontologyClass.equals(other.ontologyClass))
			return false;
		return true;
	}

}
