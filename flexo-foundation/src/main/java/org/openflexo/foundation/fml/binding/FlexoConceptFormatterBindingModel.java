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
import java.lang.reflect.Type;

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;

/**
 * This is the {@link BindingModel} exposed by the FlexoConceptInstance formatter This {@link BindingModel} is based on ActionContainer's
 * {@link BindingModel}
 * 
 * @author sylvain
 * 
 */
public class FlexoConceptFormatterBindingModel extends BindingModel {

	private final FlexoConceptInspector conceptInspector;

	public FlexoConceptFormatterBindingModel(FlexoConceptInspector conceptInspector) {
		super(conceptInspector.getFlexoConcept() != null ? conceptInspector.getFlexoConcept().getBindingModel() : null);
		this.conceptInspector = conceptInspector;
		if (conceptInspector != null && conceptInspector.getPropertyChangeSupport() != null) {
			conceptInspector.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		addToBindingVariables(new BindingVariable(FlexoConceptInspector.FORMATTER_INSTANCE_PROPERTY,
				FlexoConceptInstanceType.getFlexoConceptInstanceType(conceptInspector.getFlexoConcept())) {
			@Override
			public Type getType() {
				return FlexoConceptInstanceType.getFlexoConceptInstanceType(getConceptInspector().getFlexoConcept());
			}
		});
	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (conceptInspector != null && conceptInspector.getPropertyChangeSupport() != null) {
			conceptInspector.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == conceptInspector) {
			if (evt.getPropertyName().equals(FlexoConceptInspector.FLEXO_CONCEPT_KEY)) {
				setBaseBindingModel(conceptInspector.getFlexoConcept() != null ? conceptInspector.getFlexoConcept().getBindingModel()
						: null);
			}
		}
	}

	public FlexoConceptInspector getConceptInspector() {
		return conceptInspector;
	}
}
