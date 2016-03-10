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

package org.openflexo.foundation.ontology.fml;

import java.lang.reflect.Type;

import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyIndividual;
import org.openflexo.foundation.ontology.IndividualOfClass;
import org.openflexo.foundation.ontology.fml.rt.ConceptActorReference;
import org.openflexo.foundation.ontology.nature.FlexoOntologyVirtualModelNature;
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
@ImplementationClass(IndividualRole.IndividualRoleImpl.class)
public interface IndividualRole<I extends IFlexoOntologyIndividual<?>> extends OntologicObjectRole<I> {

	@PropertyIdentifier(type = String.class)
	public static final String CONCEPT_URI_KEY = "conceptURI";

	@Getter(value = CONCEPT_URI_KEY)
	@XMLAttribute(xmlTag = "ontologicType")
	public String _getConceptURI();

	@Setter(CONCEPT_URI_KEY)
	public void _setConceptURI(String conceptURI);

	public IFlexoOntologyClass<?> getOntologicType();

	public void setOntologicType(IFlexoOntologyClass<?> ontologyClass);

	public static abstract class IndividualRoleImpl<I extends IFlexoOntologyIndividual<?>> extends OntologicObjectRoleImpl<I> implements
			IndividualRole<I> {

		public IndividualRoleImpl() {
			super();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("FlexoRole " + getName() + " as Individual conformTo " + getTypeDescription() + " from " + getModelSlot().getName()
					+ " ;", context);
			return out.toString();
		}

		/**
		 * Encodes the default cloning strategy
		 * 
		 * @return
		 */
		@Override
		public RoleCloningStrategy defaultCloningStrategy() {
			return RoleCloningStrategy.Clone;
		}

		@Override
		public boolean defaultBehaviourIsToBeDeleted() {
			return true;
		}

		private Type lastKnownType = null;

		@Override
		public Type getType() {
			Type returned;
			if (getOntologicType() == null) {
				returned = IFlexoOntologyIndividual.class;
			} else {
				returned = IndividualOfClass.getIndividualOfClass(getOntologicType());
			}
			if (lastKnownType == null || !lastKnownType.equals(returned)) {
				Type oldType = lastKnownType;
				lastKnownType = returned;
				getPropertyChangeSupport().firePropertyChange(BindingVariable.TYPE_PROPERTY, oldType, returned);
			}
			return returned;
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
		public IFlexoOntologyClass<?> getOntologicType() {
			if (FlexoOntologyVirtualModelNature.INSTANCE.hasNature(getOwningVirtualModel())) {
				return FlexoOntologyVirtualModelNature.getOntologyClass(_getConceptURI(), getOwningVirtualModel());
			}
			return null;
		}

		@Override
		public void setOntologicType(IFlexoOntologyClass<?> ontologyClass) {
			conceptURI = ontologyClass != null ? ontologyClass.getURI() : null;
		}

		@Override
		public ConceptActorReference<I> makeActorReference(I object, FlexoConceptInstance fci) {
			AbstractVirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			ConceptActorReference<I> returned = factory.newInstance(ConceptActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(fci);
			returned.setModellingElement(object);
			return returned;
		}

	}

	@DefineValidationRule
	public static class IndividualFlexoRoleMustDefineAValidConceptClass extends
			ValidationRule<IndividualFlexoRoleMustDefineAValidConceptClass, IndividualRole> {
		public IndividualFlexoRoleMustDefineAValidConceptClass() {
			super(IndividualRole.class, "individual_role_must_define_a_valid_concept_class");
		}

		@Override
		public ValidationIssue<IndividualFlexoRoleMustDefineAValidConceptClass, IndividualRole> applyValidation(IndividualRole patternRole) {
			if (patternRole.getOntologicType() == null) {
				return new ValidationError<IndividualFlexoRoleMustDefineAValidConceptClass, IndividualRole>(this, patternRole,
						"individual_role_does_not_define_any_concept_class");
			}
			return null;
		}
	}

}
