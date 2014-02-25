/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.viewpoint;

import org.openflexo.foundation.ontology.IFlexoOntologyObject;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;

@ModelEntity(isAbstract = true)
@ImplementationClass(OntologicObjectRole.OntologicObjectRoleImpl.class)
public interface OntologicObjectRole<T extends IFlexoOntologyObject> extends FlexoRole<T> {

	public static abstract class OntologicObjectRoleImpl<T extends IFlexoOntologyObject> extends FlexoRoleImpl<T> implements
			OntologicObjectRole<T> {

		/*public boolean getIsPrimaryConceptRole() {
			if (getFlexoConcept() == null) {
				return false;
			}
			return getFlexoConcept().getPrimaryConceptRole() == this;
		}

		public void setIsPrimaryConceptRole(boolean isPrimary) {
			if (getFlexoConcept() == null) {
				return;
			}
			if (isPrimary) {
				getFlexoConcept().setPrimaryConceptRole(this);
			} else {
				getFlexoConcept().setPrimaryConceptRole(null);
			}
		}

		@Override
		public boolean getIsPrimaryRole() {
			return getIsPrimaryConceptRole();
		}

		@Override
		public void setIsPrimaryRole(boolean isPrimary) {
			setIsPrimaryConceptRole(isPrimary);
		}*/

		@Override
		public TypeAwareModelSlot<?, ?> getModelSlot() {
			TypeAwareModelSlot<?, ?> returned = null;
			ModelSlot<?> superMS = super.getModelSlot();
			if (superMS instanceof TypeAwareModelSlot) {
				returned = (TypeAwareModelSlot<?, ?>) super.getModelSlot();
			}
			if (returned == null) {
				if (getVirtualModel() != null && getVirtualModel().getModelSlots(TypeAwareModelSlot.class).size() > 0) {
					return getVirtualModel().getModelSlots(TypeAwareModelSlot.class).get(0);
				}
			}
			return returned;
		}

		@Override
		public void setModelSlot(ModelSlot<?> modelSlot) {
			if (modelSlot instanceof TypeAwareModelSlot) {
				super.setModelSlot(modelSlot);
			}
		}
	}
}
