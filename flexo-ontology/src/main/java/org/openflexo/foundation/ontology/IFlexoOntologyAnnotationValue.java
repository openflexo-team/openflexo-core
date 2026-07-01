package org.openflexo.foundation.ontology;

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

import java.util.List;

public interface IFlexoOntologyAnnotationValue <TA extends TechnologyAdapter<TA>> extends IFlexoOntologyPropertyValue<TA> {

    /**
     * Data Property.
     *
     * @return
     */
    public IFlexoOntologyAnnotation<TA> getAnnotation();

    /**
     * Value of Data Property.
     *
     * @return
     */
    @Override
    public List<Object> getValues();
}
