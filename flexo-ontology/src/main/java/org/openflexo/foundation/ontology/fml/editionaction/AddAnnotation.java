package org.openflexo.foundation.ontology.fml.editionaction;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.ontology.*;
import org.openflexo.foundation.ontology.fml.AnnotationRole;
import org.openflexo.foundation.ontology.fml.DataPropertyRole;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.*;

import java.lang.reflect.Type;
import java.util.logging.Logger;

@ModelEntity(isAbstract = true)
@ImplementationClass(AddAnnotation.AddAnnotationImpl.class)
public interface AddAnnotation <MS extends TypeAwareModelSlot<M, ?, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T extends IFlexoOntologyAnnotation<?>>
        extends AddConcept<MS, M, T> {
    @PropertyIdentifier(type = DataBinding.class)
    public static final String PROPERTY_NAME_KEY = "propertyName";
    @PropertyIdentifier(type = DataBinding.class)
    public static final String DYNAMIC_DOMAIN_KEY = "dynamicDomain";
    @PropertyIdentifier(type = BuiltInDataType.class)
    public static final String DATA_TYPE_KEY = "dataType";
    @Getter(value = PROPERTY_NAME_KEY)
    @XMLAttribute
    @FMLAttribute(value = PROPERTY_NAME_KEY, required = true, description = "<html>Name of property to be created</html>")
    public DataBinding<String> getPropertyName();

    @Getter(value = DYNAMIC_DOMAIN_KEY)
    @XMLAttribute
    @FMLAttribute(value = DYNAMIC_DOMAIN_KEY, required = false, description = "<html>Domain of property to be created</html>")
    public DataBinding<IFlexoOntologyConcept<?>> getDynamicDomain();

    @Setter(DYNAMIC_DOMAIN_KEY)
    public void setDynamicDomain(DataBinding<IFlexoOntologyConcept<?>> dynamicDomain);


    @Setter(PROPERTY_NAME_KEY)
    public void setPropertyName(DataBinding<String> propertyName);
    @Getter(value = DATA_TYPE_KEY)
    @XMLAttribute
    @FMLAttribute(
            value = DATA_TYPE_KEY,
            required = false,
            description = "<html>Built-in datatype of the property</html>")
    public BuiltInDataType getDataType();

    @Setter(DATA_TYPE_KEY)
    public void setDataType(BuiltInDataType dataType);
    public static abstract class AddAnnotationImpl<MS extends TypeAwareModelSlot<M, ?, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T extends IFlexoOntologyAnnotation<?>>
            extends AddConceptImpl<MS, M, T> implements AddAnnotation<MS, M, T> {
        protected static final Logger logger = FlexoLogger.getLogger(AddDataProperty.class.getPackage().getName());

        private DataBinding<String> propertyName;
        private DataBinding<IFlexoOntologyConcept<?>> dynamicDomain;
        private BuiltInDataType dataType;

        public abstract Class<T> getOntologyAnnotationClass();

        @Override
        public AnnotationRole<T> getAssignedFlexoProperty() {
            return (AnnotationRole<T>) super.getAssignedFlexoProperty();
        }

        @Override
        public DataBinding<String> getPropertyName() {
            if (propertyName == null) {
                propertyName = new DataBinding<>(this, String.class, DataBinding.BindingDefinitionType.GET);
                propertyName.setBindingName("propertyName");
            }
            return propertyName;
        }

        @Override
        public void setPropertyName(DataBinding<String> propertyName) {
            if (propertyName != null) {
                propertyName.setOwner(this);
                propertyName.setDeclaredType(String.class);
                propertyName.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
                propertyName.setBindingName("propertyName");
            }
            this.propertyName = propertyName;
        }
        @Override
        public DataBinding<IFlexoOntologyConcept<?>> getDynamicDomain() {
            if (dynamicDomain == null) {
                dynamicDomain = new DataBinding<>(this, IFlexoOntologyConcept.class, DataBinding.BindingDefinitionType.GET);
                dynamicDomain.setBindingName("dynamicDomain");
            }
            return dynamicDomain;
        }

        @Override
        public void setDynamicDomain(DataBinding<IFlexoOntologyConcept<?>> dynamicDomain) {
            if (dynamicDomain != null) {
                dynamicDomain.setOwner(this);
                dynamicDomain.setDeclaredType(IFlexoOntologyConcept.class);
                dynamicDomain.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
                dynamicDomain.setBindingName("dynamicDomain");
            }
            this.dynamicDomain = dynamicDomain;
        }
        @Override
        public Type getAssignableType() {
            // if (getSuperProperty() == null) {
            return IFlexoOntologyAnnotation.class;
            // }
            // return SubPropertyOfProperty.getSubPropertyOfProperty(getSuperProperty());
        }


    }


}
