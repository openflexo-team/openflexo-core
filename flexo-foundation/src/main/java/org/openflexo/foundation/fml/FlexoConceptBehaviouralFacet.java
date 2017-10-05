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

import java.util.Collection;
import java.util.List;

import org.openflexo.connie.BindingModel;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.validation.Validable;

@ModelEntity
@ImplementationClass(FlexoConceptBehaviouralFacet.FlexoConceptBehaviouralFacetImpl.class)
public interface FlexoConceptBehaviouralFacet extends FlexoConceptObject, FlexoFacet<FlexoConcept> {

	@Override
	public FlexoConcept getFlexoConcept();

	public void setFlexoConcept(FlexoConcept flexoConcept);

	public List<FlexoBehaviour> getBehaviours();

	public abstract class FlexoConceptBehaviouralFacetImpl extends FlexoConceptObjectImpl implements FlexoConceptBehaviouralFacet {

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

		@Override
		public FlexoConcept getObject() {
			return getFlexoConcept();
		}

		@Override
		public String getURI() {
			return getFlexoConcept().getURI();
		}

		@Override
		public List<FlexoBehaviour> getBehaviours() {
			return getFlexoConcept().getDeclaredFlexoBehaviours();
		}

		protected void notifiedBehavioursChanged(FlexoBehaviour oldValue, FlexoBehaviour newValue) {
			getPropertyChangeSupport().firePropertyChange("behaviours", oldValue, newValue);
		}

		@Override
		public Collection<? extends Validable> getEmbeddedValidableObjects() {
			return getBehaviours();
		}

	}
}
