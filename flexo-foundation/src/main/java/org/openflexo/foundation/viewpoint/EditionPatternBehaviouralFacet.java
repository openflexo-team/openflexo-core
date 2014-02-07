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

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;

@ModelEntity
@ImplementationClass(EditionPatternBehaviouralFacet.EditionPatternBehaviouralFacetImpl.class)
public interface EditionPatternBehaviouralFacet extends EditionPatternObject, FlexoFacet<FlexoConcept> {

	@Override
	public FlexoConcept getFlexoConcept();

	public void setFlexoConcept(FlexoConcept flexoConcept);

	public abstract class EditionPatternBehaviouralFacetImpl extends EditionPatternObjectImpl implements EditionPatternBehaviouralFacet {

		private FlexoConcept flexoConcept;

		@Override
		public FlexoConcept getFlexoConcept() {
			return flexoConcept;
		}

		@Override
		public void setFlexoConcept(FlexoConcept flexoConcept) {
			this.flexoConcept = flexoConcept;
		}

		@Override
		public BindingModel getBindingModel() {
			return getFlexoConcept().getBindingModel();
		}

		/*@Override
		public Collection<? extends Validable> getEmbeddedValidableObjects() {
			return getEditionPattern().getEditionSchemes();
		}*/

		@Override
		public FlexoConcept getObject() {
			return getFlexoConcept();
		}

		@Override
		public String getURI() {
			return getFlexoConcept().getURI();
		}

		@Override
		public VirtualModel getVirtualModel() {
			return getFlexoConcept().getVirtualModel();
		}
	}
}
