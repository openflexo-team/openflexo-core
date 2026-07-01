package org.openflexo.foundation.ontology.dm;

import org.openflexo.foundation.ontology.IFlexoOntologyAnnotation;
import org.openflexo.foundation.ontology.IFlexoOntologyDataProperty;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

public class OntologyAnnotationRemoved <TA extends TechnologyAdapter<TA>>
        extends OntologyDataModification<IFlexoOntologyAnnotation<TA>> {
    public OntologyAnnotationRemoved(IFlexoOntologyAnnotation<TA> property) {
        super(property, null);
    }
}
