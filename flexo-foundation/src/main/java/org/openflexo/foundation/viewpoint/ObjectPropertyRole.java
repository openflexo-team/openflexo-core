package org.openflexo.foundation.viewpoint;

import java.lang.reflect.Type;

import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyObjectProperty;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;

@ModelEntity(isAbstract = true)
@ImplementationClass(ObjectPropertyRole.ObjectPropertyRoleImpl.class)
public abstract interface ObjectPropertyRole<P extends IFlexoOntologyObjectProperty> extends PropertyRole<P> {

	@PropertyIdentifier(type = String.class)
	public static final String RANGE_URI_KEY = "rangeURI";

	@Getter(value = RANGE_URI_KEY)
	@XMLAttribute(xmlTag = "range")
	public String _getRangeURI();

	@Setter(RANGE_URI_KEY)
	public void _setRangeURI(String rangeURI);

	public static abstract class ObjectPropertyRoleImpl<P extends IFlexoOntologyObjectProperty> extends PropertyRoleImpl<P>
			implements ObjectPropertyRole<P> {

		private String rangeURI;

		public ObjectPropertyRoleImpl() {
			super();
		}

		@Override
		public Type getType() {
			if (getParentProperty() == null) {
				return IFlexoOntologyObjectProperty.class;
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
		public IFlexoOntologyObjectProperty getParentProperty() {
			return (IFlexoOntologyObjectProperty) super.getParentProperty();
		}

		public void setParentProperty(IFlexoOntologyObjectProperty ontologyProperty) {
			super.setParentProperty(ontologyProperty);
		}

		@Override
		public String _getRangeURI() {
			return rangeURI;
		}

		@Override
		public void _setRangeURI(String domainURI) {
			this.rangeURI = domainURI;
		}

		public IFlexoOntologyClass getRange() {
			return getVirtualModel().getOntologyClass(_getRangeURI());
		}

		public void setRange(IFlexoOntologyClass c) {
			_setRangeURI(c != null ? c.getURI() : null);
		}

	}
}
