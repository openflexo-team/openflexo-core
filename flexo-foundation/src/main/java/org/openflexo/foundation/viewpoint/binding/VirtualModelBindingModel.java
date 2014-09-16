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
package org.openflexo.foundation.viewpoint.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.antar.binding.BindingEvaluationContext;
import org.openflexo.antar.binding.BindingModel;
import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.view.View;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.viewpoint.ViewType;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.VirtualModelInstanceType;

/**
 * This is the {@link BindingModel} exposed by a {@link VirtualModel}<br>
 * This {@link BindingModel} is based on ViewPoint's (owner of this VirtualModel) {@link BindingModel}
 * 
 * Provides access to the {@link View}<br>
 * Allows reflexive access to the {@link VirtualModel} itself<br>
 * 
 * Note that default {@link BindingEvaluationContext} corresponding to this {@link BindingModel} is a {@link VirtualModelInstance}
 * 
 * 
 * @author sylvain
 * 
 */
public class VirtualModelBindingModel extends FlexoConceptBindingModel implements PropertyChangeListener {

	private final VirtualModel virtualModel;

	private final BindingVariable reflexiveAccessBindingVariable;
	private final BindingVariable viewBindingVariable;
	private final BindingVariable virtualModelInstanceBindingVariable;

	private final Map<ModelSlot<?>, ModelSlotBindingVariable> modelSlotVariablesMap;

	public static final String REFLEXIVE_ACCESS_PROPERTY = "virtualModelDefinition";
	public static final String VIEW_PROPERTY = "view";
	public static final String VIRTUAL_MODEL_INSTANCE_PROPERTY = "virtualModelInstance";

	/**
	 * Build a new {@link BindingModel} dedicated to a VirtualModel
	 * 
	 * @param viewPoint
	 */
	public VirtualModelBindingModel(VirtualModel virtualModel) {
		super(virtualModel.getViewPoint() != null ? virtualModel.getViewPoint().getBindingModel() : null, virtualModel);
		this.virtualModel = virtualModel;
		if (virtualModel != null && virtualModel.getPropertyChangeSupport() != null) {
			virtualModel.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		virtualModelInstanceBindingVariable = new BindingVariable(VIRTUAL_MODEL_INSTANCE_PROPERTY,
				VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel));
		addToBindingVariables(virtualModelInstanceBindingVariable);
		reflexiveAccessBindingVariable = new BindingVariable(REFLEXIVE_ACCESS_PROPERTY, VirtualModel.class);
		addToBindingVariables(reflexiveAccessBindingVariable);
		viewBindingVariable = new BindingVariable(VIEW_PROPERTY, virtualModel.getViewPoint() != null ? ViewType.getViewType(virtualModel
				.getViewPoint()) : View.class);
		addToBindingVariables(viewBindingVariable);
		modelSlotVariablesMap = new HashMap<ModelSlot<?>, ModelSlotBindingVariable>();
		updateModelSlotVariables();
	}

	public VirtualModel getVirtualModel() {
		return virtualModel;
	}

	public BindingVariable getVirtualModelInstanceBindingVariable() {
		return virtualModelInstanceBindingVariable;
	}

	/**
	 * Return the reflexive access {@link BindingVariable}<br>
	 * (Allows reflexive access to the {@link VirtualModel} itself)
	 * 
	 * @return
	 */
	@Override
	public BindingVariable getReflexiveAccessBindingVariable() {
		return reflexiveAccessBindingVariable;
	}

	public BindingVariable getViewBindingVariable() {
		return viewBindingVariable;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// System.out.println("Hop, je recois " + evt.getPropertyName() + " source =" + evt.getSource() + " evt=" + evt);
		super.propertyChange(evt);
		if (evt.getSource() == virtualModel) {
			if (evt.getPropertyName().equals(VirtualModel.VIEW_POINT_KEY)) {
				// The VirtualModel changes it's ViewPoint
				setBaseBindingModel(virtualModel.getViewPoint() != null ? virtualModel.getViewPoint().getBindingModel() : null);
				viewBindingVariable.setType(virtualModel.getViewPoint() != null ? ViewType.getViewType(virtualModel.getViewPoint())
						: View.class);
				virtualModelInstanceBindingVariable.setType(VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel));
			} else if (evt.getPropertyName().equals(VirtualModel.MODEL_SLOTS_KEY)) {
				// Model Slot were modified in related flexoConcept
				updateModelSlotVariables();
			}
		}
	}

	private void updateModelSlotVariables() {

		List<ModelSlot<?>> modelSlotToBeDeleted = new ArrayList<ModelSlot<?>>(modelSlotVariablesMap.keySet());

		for (ModelSlot<?> ms : virtualModel.getModelSlots()) {
			// if (ms != virtualModel.getReflexiveModelSlot()) {
			if (modelSlotToBeDeleted.contains(ms)) {
				modelSlotToBeDeleted.remove(ms);
			} else if (modelSlotVariablesMap.get(ms) == null) {
				ModelSlotBindingVariable bv = new ModelSlotBindingVariable(ms);
				addToBindingVariables(bv);
				modelSlotVariablesMap.put(ms, bv);
			}
			// }
		}

		for (ModelSlot<?> ms : modelSlotToBeDeleted) {
			// if (ms != virtualModel.getReflexiveModelSlot()) {
			ModelSlotBindingVariable bvToRemove = modelSlotVariablesMap.get(ms);
			removeFromBindingVariables(bvToRemove);
			modelSlotVariablesMap.remove(ms);
			bvToRemove.delete();
			// }
		}

	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		super.delete();
		if (virtualModel != null && virtualModel.getPropertyChangeSupport() != null) {
			virtualModel.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
	}
}
