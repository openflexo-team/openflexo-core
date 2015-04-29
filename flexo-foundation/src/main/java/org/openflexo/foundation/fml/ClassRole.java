/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.fml;

import java.lang.reflect.Type;

import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.rt.ConceptActorReference;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceModelFactory;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.SubClassOfClass;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;

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
			out.append("FlexoRole " + getName() + " as Class conformTo " + getTypeDescription() + " from " + "\""
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
		public String getTypeDescription() {
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
			if (getOwningVirtualModel() != null) {
				return getOwningVirtualModel().getOntologyClass(_getConceptURI());
			} else {
				return null;
			}
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

	@DefineValidationRule
	public static class ClassRoleMustDefineAValidConceptClass extends ValidationRule<ClassRoleMustDefineAValidConceptClass, ClassRole> {
		public ClassRoleMustDefineAValidConceptClass() {
			super(ClassRole.class, "flexo_role_must_define_a_valid_concept_class");
		}

		@Override
		public ValidationIssue<ClassRoleMustDefineAValidConceptClass, ClassRole> applyValidation(ClassRole patternRole) {
			if (patternRole.getOntologicType() == null) {
				return new ValidationError<ClassRoleMustDefineAValidConceptClass, ClassRole>(this, patternRole,
						"flexo_role_does_not_define_any_concept_class");
			}
			return null;
		}
	}

}
