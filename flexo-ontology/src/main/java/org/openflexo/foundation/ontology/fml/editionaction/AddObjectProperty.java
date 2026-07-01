package org.openflexo.foundation.ontology.fml.editionaction;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.ontology.BuiltInDataType;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyDataProperty;
import org.openflexo.foundation.ontology.IFlexoOntologyObjectProperty;
import org.openflexo.foundation.ontology.fml.DataPropertyRole;
import org.openflexo.foundation.ontology.fml.ObjectPropertyRole;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ModelEntity(isAbstract = true)
@ImplementationClass(AddObjectProperty.AddObjectPropertyImpl.class)
public interface AddObjectProperty<MS extends TypeAwareModelSlot<M, ?, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T extends IFlexoOntologyObjectProperty<?>>
        extends AddConcept<MS, M, T> {
    @PropertyIdentifier(type = DataBinding.class)
    public static final String PROPERTY_NAME_KEY = "propertyName";
    @PropertyIdentifier(type = DataBinding.class)
    public static final String DYNAMIC_DOMAIN_KEY = "dynamicDomain";
    @PropertyIdentifier(type = DataBinding.class)
    public static final String Range_KEY = "range";


    @Getter(value = PROPERTY_NAME_KEY)
    @XMLAttribute
    @FMLAttribute(value = PROPERTY_NAME_KEY, required = true, description = "<html>Name of property to be created</html>")
    public DataBinding<String> getPropertyName();

    @Setter(PROPERTY_NAME_KEY)
    public void setPropertyName(DataBinding<String> propertyName);

    @Getter(value = DYNAMIC_DOMAIN_KEY)
    @XMLAttribute
    @FMLAttribute(value = DYNAMIC_DOMAIN_KEY, required = false, description = "<html>Domain of property to be created</html>")
    public DataBinding<IFlexoOntologyClass<?>> getDynamicDomain();

    @Setter(DYNAMIC_DOMAIN_KEY)
    public void setDynamicDomain(DataBinding<IFlexoOntologyClass<?>> dynamicDomain);

    @Getter(value = Range_KEY)
    @XMLAttribute
    @FMLAttribute(value = Range_KEY, required = false, description = "<html>Range of property to be created</html>")
    public  DataBinding<IFlexoOntologyClass<?>> getRange();

    @Setter(Range_KEY)
    public void setRange( DataBinding<IFlexoOntologyClass<?>> range);

    @Remover(DYNAMIC_DOMAIN_KEY)
    public void removeDynamicDomain(IFlexoOntologyClass<?> cls);

    @Adder(DYNAMIC_DOMAIN_KEY)
    @PastingPoint
    public void addDynamicDomain(IFlexoOntologyClass<?> cls);

    @Remover(Range_KEY)
    public void removeRange(IFlexoOntologyClass<?> cls);

    @Adder(Range_KEY)
    @PastingPoint
    public void addRange(IFlexoOntologyClass<?> cls);


    public static abstract class AddObjectPropertyImpl<MS extends TypeAwareModelSlot<M, ?, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T extends IFlexoOntologyObjectProperty<?>>
            extends AddConceptImpl<MS, M, T> implements AddObjectProperty<MS, M, T> {

        protected static final Logger logger = FlexoLogger.getLogger(AddDataProperty.class.getPackage().getName());

        private DataBinding<String> propertyName;
        private DataBinding<IFlexoOntologyClass<?>> dynamicDomain;
        private DataBinding<IFlexoOntologyClass<?>> range;
        public abstract Class<T> getOntologyObjectPropertyClass();

        @Override
        public ObjectPropertyRole<T> getAssignedFlexoProperty() {
            return (ObjectPropertyRole<T>) super.getAssignedFlexoProperty();
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
        public DataBinding<IFlexoOntologyClass<?>> getDynamicDomain() {
            if (dynamicDomain == null) {
                dynamicDomain = new DataBinding<>(this, IFlexoOntologyClass.class,
                        DataBinding.BindingDefinitionType.GET);
                dynamicDomain.setBindingName("dynamicDomain");
            }
            return dynamicDomain;
        }

        @Override
        public void setDynamicDomain(DataBinding<IFlexoOntologyClass<?>> dynamicDomain) {
            if (dynamicDomain != null) {
                dynamicDomain.setOwner(this);
                dynamicDomain.setDeclaredType(IFlexoOntologyClass.class);
                dynamicDomain.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
                dynamicDomain.setBindingName("dynamicDomain");
            }
            this.dynamicDomain = dynamicDomain;
        }
        @Override
        public void addDynamicDomain(IFlexoOntologyClass<?> cls) {
            if (cls == null) {
                return;
            }
            /*
            List<IFlexoOntologyClass<?>> list = getDynamicDomain().getBindingValue();
            if (!list.contains(cls)) {
                list.add(cls);
            }

             */
        }

        @Override
        public DataBinding<IFlexoOntologyClass<?>> getRange() {
            if (range == null) {
                range = new DataBinding<>(this, IFlexoOntologyClass.class,
                        DataBinding.BindingDefinitionType.GET);
                range.setBindingName("range");
            }
            return range;
        }

        @Override
        public void setRange(DataBinding<IFlexoOntologyClass<?>> range) {
            if (range != null) {
                range.setOwner(this);
                range.setDeclaredType(IFlexoOntologyClass.class);
                range.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
                range.setBindingName("range");
            }
            this.range = range;
        }
        @Override
        public Type getAssignableType() {
            // if (getSuperProperty() == null) {
            return IFlexoOntologyObjectProperty.class;
            // }
            // return SubPropertyOfProperty.getSubPropertyOfProperty(getSuperProperty());
        }
    }

}
