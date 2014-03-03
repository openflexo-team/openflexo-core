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

public interface FlexoBehaviourParameters extends FlexoBehaviourObject, FlexoFacet<FlexoBehaviour> {

	@Override
	public FlexoBehaviour getFlexoBehaviour();

	public void setFlexoBehaviour(FlexoBehaviour flexoBehaviour);

	public abstract class EditionSchemeParametersImpl extends FlexoBehaviourObjectImpl implements FlexoBehaviourParameters {

		private FlexoBehaviour flexoBehaviour;

		@Override
		public FlexoBehaviour getFlexoBehaviour() {
			return flexoBehaviour;
		}

		@Override
		public void setFlexoBehaviour(FlexoBehaviour flexoBehaviour) {
			this.flexoBehaviour = flexoBehaviour;
		}

		@Override
		public BindingModel getBindingModel() {
			return getFlexoBehaviour().getBindingModel();
		}

		/*@Override
		public Collection<? extends Validable> getEmbeddedValidableObjects() {
			return getEditionScheme().getParameters();
		}*/

		@Override
		public FlexoBehaviour getObject() {
			return getFlexoBehaviour();
		}

		@Override
		public String getURI() {
			return getFlexoBehaviour().getURI();
		}

		@Override
		public VirtualModel getVirtualModel() {
			return getFlexoConcept().getVirtualModel();
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			return getFlexoBehaviour().getFlexoConcept();
		}
	}
}
