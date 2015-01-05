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
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;

/**
 * This is the root class for a {@link BindingModel} used in the context of a {@link FMLControlGraph}<br>
 * This {@link BindingModel} is based on FMLControlGraph's owner {@link BindingModel}
 * 
 * @author sylvain
 * 
 */
public class ControlGraphBindingModel<CG extends FMLControlGraph> extends BindingModel {

	private final CG controlGraph;

	public ControlGraphBindingModel(CG controlGraph) {
		super(controlGraph.getOwner() != null ? controlGraph.getOwner().getBaseBindingModel(controlGraph) : null);

		this.controlGraph = controlGraph;

		if (controlGraph != null && controlGraph.getPropertyChangeSupport() != null) {
			controlGraph.getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		if (controlGraph != null && controlGraph.getOwner() != null && controlGraph.getOwner().getPropertyChangeSupport() != null) {
			controlGraph.getOwner().getPropertyChangeSupport().addPropertyChangeListener(this);
		}

	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (controlGraph != null && controlGraph.getPropertyChangeSupport() != null) {
			controlGraph.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		if (controlGraph != null && controlGraph.getOwner() != null && controlGraph.getOwner().getPropertyChangeSupport() != null) {
			controlGraph.getOwner().getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// System.out.println("Received " + evt + " pour " + controlGraph + " bm=" + this);
		super.propertyChange(evt);
		if (evt.getSource() == controlGraph) {
			if (evt.getPropertyName().equals(FMLControlGraph.OWNER_KEY) || evt.getPropertyName().equals(FMLControlGraph.OWNER_CONTEXT_KEY)) {
				// The control graph changes it's owner or context
				System.out.println("owner=" + controlGraph.getOwner());
				setBaseBindingModel(controlGraph.getOwner() != null ? controlGraph.getOwner().getBaseBindingModel(controlGraph) : null);
			}
		} else if (evt.getSource() == controlGraph.getOwner()) {
			setBaseBindingModel(controlGraph.getOwner() != null ? controlGraph.getOwner().getBaseBindingModel(controlGraph) : null);
		}
		// System.out.println("Maintenant bm=" + this);
	}

	public CG getControlGraph() {
		return controlGraph;
	}
}
