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

import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyStructuralProperty;
import org.openflexo.foundation.ontology.SubPropertyOfProperty;
import org.openflexo.foundation.ontology.fml.rt.ConceptActorReference;
import org.openflexo.foundation.ontology.nature.FlexoOntologyVirtualModelNature;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;

@ModelEntity(isAbstract = true)
@ImplementationClass(PropertyRole.PropertyRoleImpl.class)
public interface PropertyRole<T extends IFlexoOntologyStructuralProperty> extends OntologicObjectRole<T> {

	@PropertyIdentifier(type = String.class)
	public static final String PARENT_PROPERTY_URI_KEY = "parentPropertyURI";
	@PropertyIdentifier(type = String.class)
	public static final String DOMAIN_URI = "domainURI";

	@Getter(PARENT_PROPERTY_URI_KEY)
	@XMLAttribute
	public String _getParentPropertyURI();

	@Setter(PARENT_PROPERTY_URI_KEY)
	public void _setParentPropertyURI(String parentPropertyURI);

	@Getter(DOMAIN_URI)
	@XMLAttribute
	public String _getDomainURI();

	@Setter(DOMAIN_URI)
	public void _setDomainURI(String domainURI);

	public IFlexoOntologyStructuralProperty<?> getParentProperty();

	public void setParentProperty(IFlexoOntologyStructuralProperty<?> ontologyProperty);

	public IFlexoOntologyClass<?> getDomain();

	public void setDomain(IFlexoOntologyClass<?> c);

	public abstract class PropertyRoleImpl<T extends IFlexoOntologyStructuralProperty> extends OntologicObjectRoleImpl<T>
			implements PropertyRole<T> {

		private String parentPropertyURI;
		private String domainURI;

		@Override
		public Type getType() {
			if (getParentProperty() == null) {
				return IFlexoOntologyStructuralProperty.class;
			}
			return SubPropertyOfProperty.getSubPropertyOfProperty(getParentProperty());
		}

		@Override
		public String _getParentPropertyURI() {
			return parentPropertyURI;
		}

		@Override
		public void _setParentPropertyURI(String parentPropertyURI) {
			this.parentPropertyURI = parentPropertyURI;
		}

		@Override
		public IFlexoOntologyStructuralProperty<?> getParentProperty() {
			if (FlexoOntologyVirtualModelNature.INSTANCE.hasNature(getOwningVirtualModel())) {
				return FlexoOntologyVirtualModelNature.getOntologyProperty(_getParentPropertyURI(), getOwningVirtualModel());
			}
			return null;
		}

		@Override
		public void setParentProperty(IFlexoOntologyStructuralProperty<?> ontologyProperty) {
			parentPropertyURI = ontologyProperty != null ? ontologyProperty.getURI() : null;
		}

		@Override
		public String _getDomainURI() {
			return domainURI;
		}

		@Override
		public void _setDomainURI(String domainURI) {
			this.domainURI = domainURI;
		}

		@Override
		public IFlexoOntologyClass<?> getDomain() {
			if (FlexoOntologyVirtualModelNature.INSTANCE.hasNature(getOwningVirtualModel())) {
				return FlexoOntologyVirtualModelNature.getOntologyClass(_getDomainURI(), getOwningVirtualModel());
			}
			return null;
		}

		@Override
		public void setDomain(IFlexoOntologyClass<?> c) {
			_setDomainURI(c != null ? c.getURI() : null);
		}

		@Override
		public String getTypeDescription() {
			if (getParentProperty() != null) {
				return getParentProperty().getName();
			}
			return "";
		}

		@Override
		public ActorReference<T> makeActorReference(T object, FlexoConceptInstance fci) {
			AbstractVirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			ConceptActorReference<T> returned = factory.newInstance(ConceptActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(fci);
			returned.setModellingElement(object);
			return returned;
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
		public TypeAwareModelSlot<?, ?> getModelSlot() {
			TypeAwareModelSlot<?, ?> returned = super.getModelSlot();
			if (returned == null) {
				if (getOwningVirtualModel() != null && getOwningVirtualModel().getModelSlots(TypeAwareModelSlot.class).size() > 0) {
					return getOwningVirtualModel().getModelSlots(TypeAwareModelSlot.class).get(0);
				}
			}
			return returned;
		}
	}
}
