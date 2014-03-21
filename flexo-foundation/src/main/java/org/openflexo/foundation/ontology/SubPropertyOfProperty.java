package org.openflexo.foundation.ontology;

import java.lang.reflect.Type;

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.viewpoint.TechnologySpecificCustomType;

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
	public TA getTechnologyAdapter() {
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
