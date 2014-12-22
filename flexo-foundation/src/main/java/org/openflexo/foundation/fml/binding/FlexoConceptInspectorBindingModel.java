/*
  * (c) Copyright 2014-2015 Openflexo
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
package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;

/**
 * This is the {@link BindingModel} exposed by a FlexoConceptInspector<br>
 * 
 * @author sylvain
 * 
 */
public class FlexoConceptInspectorBindingModel extends BindingModel {

	private final FlexoConceptInspector inspector;

	public FlexoConceptInspectorBindingModel(FlexoConceptInspector inspector) {
		super(inspector.getFlexoConcept() != null ? inspector.getFlexoConcept().getBindingModel() : null);
		this.inspector = inspector;
		if (inspector != null && inspector.getPropertyChangeSupport() != null) {
			inspector.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (inspector != null && inspector.getPropertyChangeSupport() != null) {
			inspector.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == inspector) {
			if (evt.getPropertyName().equals(FlexoConceptInspector.FLEXO_CONCEPT_KEY)) {
				setBaseBindingModel(inspector.getFlexoConcept() != null ? inspector.getFlexoConcept().getBindingModel() : null);
			}
		}
	}

	public FlexoConceptInspector getInspector() {
		return inspector;
	}
}
