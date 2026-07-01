package org.openflexo.foundation.ontology.fml;

import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.ontology.BuiltInDataType;
import org.openflexo.foundation.ontology.IFlexoOntologyAnnotation;
import org.openflexo.foundation.ontology.IFlexoOntologyDataProperty;
import org.openflexo.foundation.ontology.fml.rt.ConceptActorReference;
import org.openflexo.pamela.annotations.*;

import java.lang.reflect.Type;

@ModelEntity(isAbstract = true)
@ImplementationClass(AnnotationRole.AnnotationRoleImpl.class)
public interface AnnotationRole <P extends IFlexoOntologyAnnotation> extends PropertyRole<P>{
    @PropertyIdentifier(type = BuiltInDataType.class)
    public static final String DATA_TYPE_KEY = "dataType";

    @Getter(value = DATA_TYPE_KEY)
    @XMLAttribute
    public BuiltInDataType getDataType();

    @Setter(DATA_TYPE_KEY)
    public void setDataType(BuiltInDataType dataType);
    public static abstract class AnnotationRoleImpl<P extends IFlexoOntologyAnnotation> extends PropertyRoleImpl<P>
            implements AnnotationRole<P> {

        private BuiltInDataType dataType;

        public AnnotationRoleImpl() {
            super();
        }

        @Override
        public Type getType() {
            if (getParentProperty() == null) {
                return IFlexoOntologyAnnotation.class;
            }
            return super.getType();
        }

        @Override
        public String getTypeDescription() {
            if (getParentProperty() != null) {
                return getParentProperty().getName();
            }
            return "";
        }

        @Override
        public IFlexoOntologyAnnotation getParentProperty() {
            return (IFlexoOntologyAnnotation) super.getParentProperty();
        }

        public void setParentProperty(IFlexoOntologyAnnotation ontologyProperty) {
            super.setParentProperty(ontologyProperty);
        }

        @Override
        public BuiltInDataType getDataType() {
            return dataType;
        }

        @Override
        public void setDataType(BuiltInDataType dataType) {
            this.dataType = dataType;
        }

        @Override
        public ActorReference<P> makeActorReference(P object, FlexoConceptInstance fci) {
            AbstractVirtualModelInstanceModelFactory factory = fci.getFactory();
            ConceptActorReference<P> returned = factory.newInstance(ConceptActorReference.class);
            returned.setFlexoRole(this);
            returned.setFlexoConceptInstance(fci);
            returned.setModellingElement(object);
            return returned;
        }
    }
}
