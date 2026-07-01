package org.openflexo.foundation.ontology.dm;

import org.openflexo.foundation.ontology.IFlexoOntologyAnnotation;
import org.openflexo.foundation.ontology.IFlexoOntologyDataProperty;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

public class OntologyAnnotationInserted <TA extends TechnologyAdapter<TA>>
        extends OntologyDataModification<IFlexoOntologyAnnotation<TA>> {
    public OntologyAnnotationInserted(IFlexoOntologyAnnotation<TA> property) {
        super(null, property);
    }
}

