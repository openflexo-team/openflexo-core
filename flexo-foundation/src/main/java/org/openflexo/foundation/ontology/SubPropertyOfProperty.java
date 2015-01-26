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

import org.openflexo.foundation.fml.TechnologySpecificCustomType;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

public class SubPropertyOfProperty<TA extends TechnologyAdapter> implements TechnologySpecificCustomType<TA> {

	public static <TA extends TechnologyAdapter> SubPropertyOfProperty<TA> getSubPropertyOfProperty(
			IFlexoOntologyStructuralProperty<TA> anOntologyProperty) {
		if (anOntologyProperty == null) {
			return null;
		}
		return anOntologyProperty.getTechnologyAdapter().getTechnologyContextManager().getSubPropertyOfProperty(anOntologyProperty);
	}

	private final IFlexoOntologyStructuralProperty<TA> ontologyProperty;

	public SubPropertyOfProperty(IFlexoOntologyStructuralProperty<TA> anOntologyProperty) {
		this.ontologyProperty = anOntologyProperty;
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
	public String simpleRepresentation() {
		return "Property" + ":" + ontologyProperty.getName();
	}

	@Override
	public String fullQualifiedRepresentation() {
		return "Property" + ":" + ontologyProperty.getURI();
	}

	@Override
	public TA getSpecificTechnologyAdapter() {
		if (getOntologyProperty() != null) {
			return getOntologyProperty().getTechnologyAdapter();
		}
		return null;
	}

	public static class SubDataPropertyOfProperty<TA extends TechnologyAdapter> extends SubPropertyOfProperty<TA> {

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

	public static class SubObjectPropertyOfProperty<TA extends TechnologyAdapter> extends SubPropertyOfProperty<TA> {

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
