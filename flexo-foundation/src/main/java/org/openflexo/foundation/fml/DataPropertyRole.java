package org.openflexo.foundation.fml;

import java.lang.reflect.Type;

import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.ConceptActorReference;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceModelFactory;
import org.openflexo.foundation.ontology.BuiltInDataType;
import org.openflexo.foundation.ontology.IFlexoOntologyDataProperty;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;

@ModelEntity(isAbstract = true)
@ImplementationClass(DataPropertyRole.DataPropertyRoleImpl.class)
public abstract interface DataPropertyRole<P extends IFlexoOntologyDataProperty> extends PropertyRole<P> {

	@PropertyIdentifier(type = BuiltInDataType.class)
	public static final String DATA_TYPE_KEY = "dataType";

	@Getter(value = DATA_TYPE_KEY)
	@XMLAttribute
	public BuiltInDataType getDataType();

	@Setter(DATA_TYPE_KEY)
	public void setDataType(BuiltInDataType dataType);

	public static abstract class DataPropertyRoleImpl<P extends IFlexoOntologyDataProperty> extends PropertyRoleImpl<P>
			implements DataPropertyRole<P> {

		private BuiltInDataType dataType;

		public DataPropertyRoleImpl() {
			super();
		}

		@Override
		public Type getType() {
			if (getParentProperty() == null) {
				return IFlexoOntologyDataProperty.class;
			}
			return super.getType();
		}

		@Override
		public String getPreciseType() {
			if (getParentProperty() != null) {
				return getParentProperty().getName();
			}
			return "";
		}

		@Override
		public IFlexoOntologyDataProperty getParentProperty() {
			return (IFlexoOntologyDataProperty) super.getParentProperty();
		}

		public void setParentProperty(IFlexoOntologyDataProperty ontologyProperty) {
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
		public ActorReference<P> makeActorReference(P object, FlexoConceptInstance epi) {
			VirtualModelInstanceModelFactory factory = epi.getFactory();
			ConceptActorReference<P> returned = factory.newInstance(ConceptActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(epi);
			returned.setModellingElement(object);
			return returned;
		}
	}
}
