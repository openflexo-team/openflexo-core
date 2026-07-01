package org.openflexo.foundation.ontology.fml.editionaction;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.ontology.IFlexoOntologyAnnotation;
import org.openflexo.foundation.ontology.IFlexoOntologyConcept;
import org.openflexo.foundation.ontology.IFlexoOntologyDataProperty;

public interface SetAnnotationValueAction <ST, S extends IFlexoOntologyConcept<?>, P extends IFlexoOntologyAnnotation<?>, T>
        extends SetPropertyValueAction<ST, S, P> {
    public DataBinding<T> getValue();

    public void setValue(DataBinding<T> value);

    public P getAnnotation();

    public void setAnnotation(P aProperty);
}
