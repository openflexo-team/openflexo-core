package org.openflexo.foundation.ontology;

import java.lang.reflect.Type;

import org.openflexo.foundation.fml.TechnologySpecificCustomType;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

public class IndividualOfClass<TA extends TechnologyAdapter> implements TechnologySpecificCustomType<TA> {

	public static <TA extends TechnologyAdapter> IndividualOfClass<TA> getIndividualOfClass(IFlexoOntologyClass<TA> anOntologyClass) {
		if (anOntologyClass == null) {
			return null;
		}
		return anOntologyClass.getTechnologyAdapter().getTechnologyContextManager().getIndividualOfClass(anOntologyClass);
	}

	private final IFlexoOntologyClass<TA> ontologyClass;

	public IndividualOfClass(IFlexoOntologyClass<TA> anOntologyClass) {
		this.ontologyClass = anOntologyClass;
	}

	public IFlexoOntologyClass<TA> getOntologyClass() {
		return ontologyClass;
	}

	@Override
	public Class<?> getBaseClass() {
		return IFlexoOntologyIndividual.class;
	}

	@Override
	public boolean isTypeAssignableFrom(Type aType, boolean permissive) {
		// System.out.println("isTypeAssignableFrom " + aType + " (i am a " + this + ")");
		if (aType instanceof IndividualOfClass) {
			return ontologyClass.isSuperConceptOf(((IndividualOfClass<TA>) aType).getOntologyClass());
		}
		return false;
	}

	@Override
	public String simpleRepresentation() {
		return "Individual" + ":" + ontologyClass.getName();
	}

	@Override
	public String fullQualifiedRepresentation() {
		return "Individual" + ":" + ontologyClass.getURI();
	}

	@Override
	public String toString() {
		return simpleRepresentation();
	}

	@Override
	public TA getSpecificTechnologyAdapter() {
		if (getOntologyClass() != null) {
			return getOntologyClass().getTechnologyAdapter();
		}
		return null;
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
		IndividualOfClass other = (IndividualOfClass) obj;
		if (ontologyClass == null) {
			if (other.ontologyClass != null)
				return false;
		} else if (!ontologyClass.equals(other.ontologyClass))
			return false;
		return true;
	}

}
