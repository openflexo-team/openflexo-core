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

import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.ontology.IFlexoOntologyObject;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;

@ModelEntity(isAbstract = true)
@ImplementationClass(OntologicObjectRole.OntologicObjectRoleImpl.class)
@Imports({ @Import(ClassRole.class), @Import(IndividualRole.class), @Import(PropertyRole.class), @Import(OntologicObjectRole.class) })
public interface OntologicObjectRole<T extends IFlexoOntologyObject> extends FlexoRole<T> {

	@Override
	public TypeAwareModelSlot<?, ?> getModelSlot();

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
				if (getOwningVirtualModel() != null && getOwningVirtualModel().getModelSlots(TypeAwareModelSlot.class).size() > 0) {
					return getOwningVirtualModel().getModelSlots(TypeAwareModelSlot.class).get(0);
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
