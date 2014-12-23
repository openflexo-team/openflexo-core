package org.openflexo.foundation.ontology;

import java.lang.reflect.Type;

import org.openflexo.foundation.fml.TechnologySpecificCustomType;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

public class SubClassOfClass<TA extends TechnologyAdapter> implements TechnologySpecificCustomType<TA> {

	public static <TA extends TechnologyAdapter> SubClassOfClass<TA> getSubClassOfClass(IFlexoOntologyClass<TA> anOntologyClass) {
		if (anOntologyClass == null) {
			return null;
		}
		return anOntologyClass.getTechnologyAdapter().getTechnologyContextManager().getSubClassOfClass(anOntologyClass);
	}

	private final IFlexoOntologyClass<TA> ontologyClass;

	public SubClassOfClass(IFlexoOntologyClass<TA> anOntologyClass) {
		this.ontologyClass = anOntologyClass;
	}

	public IFlexoOntologyClass<TA> getOntologyClass() {
		return ontologyClass;
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
	public String simpleRepresentation() {
		return "Class" + ":" + ontologyClass.getName();
	}

	@Override
	public String fullQualifiedRepresentation() {
		return "Class" + ":" + ontologyClass.getURI();
	}

	@Override
	public TA getSpecificTechnologyAdapter() {
		if (getOntologyClass() != null) {
			return getOntologyClass().getTechnologyAdapter();
		}
		return null;
	}

}
