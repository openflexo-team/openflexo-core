package org.openflexo.foundation.ontology;

import java.lang.reflect.Type;

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.viewpoint.TechnologySpecificCustomType;

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
	public TA getTechnologyAdapter() {
		if (getOntologyClass() != null) {
			return getOntologyClass().getTechnologyAdapter();
		}
		return null;
	}
}
