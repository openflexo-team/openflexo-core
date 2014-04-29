package org.openflexo.foundation.viewpoint;

import java.lang.reflect.Type;

import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.SubClassOfClass;
import org.openflexo.foundation.validation.ValidationError;
import org.openflexo.foundation.validation.ValidationIssue;
import org.openflexo.foundation.validation.ValidationRule;
import org.openflexo.foundation.view.ConceptActorReference;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.VirtualModelInstanceModelFactory;
import org.openflexo.foundation.viewpoint.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;

@ModelEntity(isAbstract = true)
@ImplementationClass(ClassRole.ClassRoleImpl.class)
public interface ClassRole<C extends IFlexoOntologyClass> extends OntologicObjectRole<IFlexoOntologyClass> {

	@PropertyIdentifier(type = String.class)
	public static final String CONCEPT_URI_KEY = "conceptURI";

	@Getter(value = CONCEPT_URI_KEY)
	@XMLAttribute(xmlTag = "ontologicType")
	public String _getConceptURI();

	@Setter(CONCEPT_URI_KEY)
	public void _setConceptURI(String conceptURI);

	public IFlexoOntologyClass getOntologicType();

	public void setOntologicType(IFlexoOntologyClass ontologyClass);

	public static abstract class ClassRoleImpl<C extends IFlexoOntologyClass> extends OntologicObjectRoleImpl<IFlexoOntologyClass>
			implements ClassRole<C> {

		public ClassRoleImpl() {
			super();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("FlexoRole " + getName() + " as Class conformTo " + getPreciseType() + " from " + "\""
					+ getModelSlot().getMetaModelURI() + "\"" + " ;", context);
			return out.toString();
		}

		@Override
		public Type getType() {
			if (getOntologicType() == null) {
				return IFlexoOntologyClass.class;
			}
			return SubClassOfClass.getSubClassOfClass(getOntologicType());
		}

		@Override
		public String getPreciseType() {
			if (getOntologicType() != null) {
				return getOntologicType().getName();
			}
			return "";
		}

		private String conceptURI;

		@Override
		public String _getConceptURI() {
			return conceptURI;
		}

		@Override
		public void _setConceptURI(String conceptURI) {
			this.conceptURI = conceptURI;
		}

		@Override
		public IFlexoOntologyClass getOntologicType() {
			return getVirtualModel().getOntologyClass(_getConceptURI());
		}

		@Override
		public void setOntologicType(IFlexoOntologyClass ontologyClass) {
			conceptURI = ontologyClass != null ? ontologyClass.getURI() : null;
		}

		/**
		 * Encodes the default cloning strategy
		 * 
		 * @return
		 */
		@Override
		public RoleCloningStrategy defaultCloningStrategy() {
			return RoleCloningStrategy.Reference;
		}

		@Override
		public boolean defaultBehaviourIsToBeDeleted() {
			return false;
		}

		@Override
		public ConceptActorReference<IFlexoOntologyClass> makeActorReference(IFlexoOntologyClass object, FlexoConceptInstance epi) {
			VirtualModelInstanceModelFactory factory = epi.getFactory();
			ConceptActorReference<IFlexoOntologyClass> returned = factory.newInstance(ConceptActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(epi);
			returned.setModellingElement(object);
			return returned;
		}
	}

	public static class ClassRoleMustDefineAValidConceptClass extends ValidationRule<ClassRoleMustDefineAValidConceptClass, ClassRole> {
		public ClassRoleMustDefineAValidConceptClass() {
			super(ClassRole.class, "pattern_role_must_define_a_valid_concept_class");
		}

		@Override
		public ValidationIssue<ClassRoleMustDefineAValidConceptClass, ClassRole> applyValidation(ClassRole patternRole) {
			if (patternRole.getOntologicType() == null) {
				return new ValidationError<ClassRoleMustDefineAValidConceptClass, ClassRole>(this, patternRole,
						"pattern_role_does_not_define_any_concept_class");
			}
			return null;
		}
	}

}
