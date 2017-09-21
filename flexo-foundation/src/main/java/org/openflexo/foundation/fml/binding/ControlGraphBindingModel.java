/**
 * 
 * Copyright (c) 2015, Openflexo
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

package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;

import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphOwner;

/**
 * This is the root class for a {@link BindingModel} used in the context of a {@link FMLControlGraph}<br>
 * This {@link BindingModel} is based on FMLControlGraph's owner {@link BindingModel}
 * 
 * @author sylvain
 * 
 */
public class ControlGraphBindingModel<CG extends FMLControlGraph> extends BindingModel {

	private final CG controlGraph;
	private SetValueBindingVariable<?> setValueBindingVariable = null;

	public ControlGraphBindingModel(CG controlGraph) {
		super(controlGraph.getOwner() != null ? controlGraph.getOwner().getBaseBindingModel(controlGraph) : null);

		this.controlGraph = controlGraph;

		if (controlGraph != null && controlGraph.getPropertyChangeSupport() != null) {
			controlGraph.getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		if (controlGraph != null && controlGraph.getOwner() != null && controlGraph.getOwner().getPropertyChangeSupport() != null) {
			controlGraph.getOwner().getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		handleSetValueBindingVariable();
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
		// System.out.println("Received " + evt.getPropertyName() + " evt=" + evt + " pour " + controlGraph + " bm=" + this);
		super.propertyChange(evt);
		if (evt.getSource() == controlGraph) {
			if (evt.getPropertyName().equals(FMLControlGraph.OWNER_CONTEXT_KEY)) {
				// The control graph changes it's context
				setBaseBindingModel(controlGraph.getOwner() != null ? controlGraph.getOwner().getBaseBindingModel(controlGraph) : null);
			}
			else if (evt.getPropertyName().equals(FMLControlGraph.OWNER_KEY)) {
				// The control graph changes it's owner
				setBaseBindingModel(controlGraph.getOwner() != null ? controlGraph.getOwner().getBaseBindingModel(controlGraph) : null);
				FMLControlGraphOwner oldOwner = (FMLControlGraphOwner) evt.getOldValue();
				if (oldOwner != null && oldOwner.getPropertyChangeSupport() != null) {
					oldOwner.getPropertyChangeSupport().removePropertyChangeListener(this);
				}
				if (controlGraph != null && controlGraph.getOwner() != null && controlGraph.getOwner().getPropertyChangeSupport() != null) {
					controlGraph.getOwner().getPropertyChangeSupport().addPropertyChangeListener(this);
				}
			}
		}
		else if (evt.getSource() == controlGraph.getOwner()) {
			setBaseBindingModel(controlGraph.getOwner() != null ? controlGraph.getOwner().getBaseBindingModel(controlGraph) : null);
		}
		handleSetValueBindingVariable();
	}

	public CG getControlGraph() {
		return controlGraph;
	}

	private void handleSetValueBindingVariable() {
		if (controlGraph != null && controlGraph.getOwner() instanceof GetSetProperty && controlGraph.getOwnerContext() != null
				&& controlGraph.getOwnerContext().equals(GetSetProperty.SET_CONTROL_GRAPH_KEY)) {
			// In this case, we detect that we are in a context of a SET control graph of a GetSetProperty
			if (setValueBindingVariable == null) {
				setValueBindingVariable = new SetValueBindingVariable<>((GetSetProperty<?>) controlGraph.getOwner());
				addToBindingVariables(setValueBindingVariable);
			}
		}

		else {
			// We are not in the context of a SET control graph of a GetSetProperty
			if (setValueBindingVariable != null) {
				removeFromBindingVariables(setValueBindingVariable);
				setValueBindingVariable.delete();
				setValueBindingVariable = null;
			}
		}

	}

}
