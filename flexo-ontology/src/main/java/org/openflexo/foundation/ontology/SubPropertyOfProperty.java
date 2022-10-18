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

import java.beans.PropertyChangeSupport;
import java.lang.reflect.Type;

import org.openflexo.foundation.fml.TechnologyAdapterTypeFactory;
import org.openflexo.foundation.fml.TechnologySpecificType;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.ontology.technologyadapter.FlexoOntologyTechnologyContextManager;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.foundation.utils.FlexoObjectReference.ReferenceOwner;

public class SubPropertyOfProperty<TA extends TechnologyAdapter<TA>> implements TechnologySpecificType<TA> {

	/**
	 * Factory for SubPropertyOfProperty instances
	 * 
	 * @author sylvain
	 * 
	 */
	public static class SubPropertyOfPropertyTypeFactory
			extends TechnologyAdapterTypeFactory<SubPropertyOfProperty<?>, FMLRTTechnologyAdapter> implements ReferenceOwner {

		public SubPropertyOfPropertyTypeFactory(FMLRTTechnologyAdapter technologyAdapter) {
			super(technologyAdapter);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Class<SubPropertyOfProperty<?>> getCustomType() {
			return (Class) SubPropertyOfProperty.class;
		}

		@Override
		public SubPropertyOfProperty<?> makeCustomType(String configuration) {

			FlexoObjectReference<IFlexoOntologyStructuralProperty<?>> reference = new FlexoObjectReference<>(configuration, this);

			IFlexoOntologyStructuralProperty<?> property = reference.getObject();

			if (property != null) {
				return getSubPropertyOfProperty(property);
			}
			return null;
		}

		@Override
		public void configureFactory(SubPropertyOfProperty<?> type) {
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

	public static <TA extends TechnologyAdapter<TA>> SubPropertyOfProperty<TA> getSubPropertyOfProperty(
			IFlexoOntologyStructuralProperty<TA> anOntologyProperty) {
		if (anOntologyProperty == null) {
			return null;
		}
		return ((FlexoOntologyTechnologyContextManager<TA>) anOntologyProperty.getTechnologyAdapter().getTechnologyContextManager())
				.getSubPropertyOfProperty(anOntologyProperty);
	}

	private final IFlexoOntologyStructuralProperty<TA> ontologyProperty;
	private final PropertyChangeSupport pcSupport;

	public SubPropertyOfProperty(IFlexoOntologyStructuralProperty<TA> anOntologyProperty) {
		pcSupport = new PropertyChangeSupport(this);
		this.ontologyProperty = anOntologyProperty;
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
	
	public IFlexoOntologyStructuralProperty<TA> getOntologyProperty() {
		return ontologyProperty;
	}

	@Override
	public Class<?> getBaseClass() {
		return IFlexoOntologyStructuralProperty.class;
	}

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");
		if (aType instanceof SubPropertyOfProperty) {
			return ontologyProperty.isSuperConceptOf(((SubPropertyOfProperty<TA>) aType).getOntologyProperty());
		}
		return false;
	}

	@Override
	public boolean isOfType(Object object, boolean permissive) {
		if (!(object instanceof IFlexoOntologyStructuralProperty)) {
			return false;
		}
		// TODO please implement me
		return true;
	}

	@Override
	public String simpleRepresentation() {
		return getClass().getSimpleName() + "(" + (ontologyProperty != null ? ontologyProperty.getName() : "") + ")";
	}

	@Override
	public String fullQualifiedRepresentation() {
		return getClass().getName() + "(" + getSerializationRepresentation() + ")";
	}

	@Override
	public String getSerializationRepresentation() {
		return new FlexoObjectReference<>(ontologyProperty).getStringRepresentation();
	}

	@Override
	public TA getSpecificTechnologyAdapter() {
		if (getOntologyProperty() != null) {
			return getOntologyProperty().getTechnologyAdapter();
		}
		return null;
	}

	@Override
	public boolean isResolved() {
		return ontologyProperty != null;
	}

	@Override
	public void resolve() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ontologyProperty == null) ? 0 : ontologyProperty.hashCode());
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
		SubPropertyOfProperty<?> other = (SubPropertyOfProperty<?>) obj;
		if (ontologyProperty == null) {
			if (other.ontologyProperty != null)
				return false;
		}
		else if (!ontologyProperty.equals(other.ontologyProperty))
			return false;
		return true;
	}

	public static class SubDataPropertyOfProperty<TA extends TechnologyAdapter<TA>> extends SubPropertyOfProperty<TA> {

		private SubDataPropertyOfProperty(IFlexoOntologyDataProperty<TA> anOntologyProperty) {
			super(anOntologyProperty);
		}

		@Override
		public IFlexoOntologyDataProperty<TA> getOntologyProperty() {
			return (IFlexoOntologyDataProperty<TA>) super.getOntologyProperty();
		}

		@Override
		public Class<?> getBaseClass() {
			return IFlexoOntologyDataProperty.class;
		}

		@Override
		public String simpleRepresentation() {
			return "DataProperty" + ":" + getOntologyProperty().getName();
		}

		@Override
		public String fullQualifiedRepresentation() {
			return "DataProperty" + ":" + getOntologyProperty().getURI();
		}

	}

	public static class SubObjectPropertyOfProperty<TA extends TechnologyAdapter<TA>> extends SubPropertyOfProperty<TA> {

		private SubObjectPropertyOfProperty(IFlexoOntologyObjectProperty<TA> anOntologyProperty) {
			super(anOntologyProperty);
		}

		@Override
		public IFlexoOntologyObjectProperty<TA> getOntologyProperty() {
			return (IFlexoOntologyObjectProperty<TA>) super.getOntologyProperty();
		}

		@Override
		public Class<?> getBaseClass() {
			return IFlexoOntologyObjectProperty.class;
		}

		@Override
		public String simpleRepresentation() {
			return "ObjectProperty" + ":" + getOntologyProperty().getName();
		}

		@Override
		public String fullQualifiedRepresentation() {
			return "ObjectProperty" + ":" + getOntologyProperty().getURI();
		}

	}

}
