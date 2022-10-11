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

package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.FMLKeywords;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConcept.FlexoConceptImpl;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.technologyadapter.ModelSlot;

/**
 * This is the {@link BindingModel} exposed by a {@link FlexoConcept}<br>
 * This {@link BindingModel} is based on VirtualModel's (owner of this FlexoConcept) {@link BindingModel} if this owner is not null
 * 
 * Provides access to the {@link FMLRTVirtualModelInstance}<br>
 * Allows reflexive access to the {@link VirtualModel} itself<br>
 * 
 * Note that default {@link RunTimeEvaluationContext} corresponding to this {@link BindingModel} is a {@link FlexoConceptInstance}
 * 
 * 
 * @author sylvain
 * 
 */
public class FlexoConceptBindingModel extends BindingModel {

	private final FlexoConcept flexoConcept;

	private final Map<FlexoProperty<?>, FlexoPropertyBindingVariable> propertyVariablesMap;
	private final List<FlexoConcept> knownParentConcepts = new ArrayList<>();
	private FlexoConcept lastKnownContainer = null;

	private BindingVariable thisBindingVariable;
	protected BindingVariable containerBindingVariable;
	protected SuperBindingVariable superBindingVariable;
	private final Map<FlexoConcept, SuperBindingVariable> superVariablesMap;

	public static final String THIS_PROPERTY_NAME = "this";
	public static final String SUPER_PROPERTY_NAME = FMLKeywords.Super.getKeyword();
	public static final String CONTAINER_PROPERTY_NAME = "container";
	public static final String RENDERER_PROPERTY_NAME = "render";

	public static final FMLNativeProperty CONTAINER_PROPERTY = new FMLNativeProperty(CONTAINER_PROPERTY_NAME,
			FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE);
	public static final FMLNativeProperty RENDERER_PROPERTY = new FMLNativeProperty(RENDERER_PROPERTY_NAME, String.class);

	/**
	 * Build a new {@link BindingModel} dedicated to a FlexoConcept<br>
	 * Note that this constructor is called for final {@link FlexoConcept} (not for {@link VirtualModel} or any of future subclass of
	 * {@link FlexoConcept})
	 * 
	 * @param flexoConcept
	 */
	public FlexoConceptBindingModel(final FlexoConcept flexoConcept) {
		this(flexoConcept.getOwningVirtualModel() != null ? flexoConcept.getOwningVirtualModel().getBindingModel() : null, flexoConcept);
	}

	/**
	 * Base constructor for any subclass of {@link FlexoConcept} (eg {@link VirtualModel})
	 * 
	 * @param baseBindingModel
	 * @param flexoConcept
	 */
	protected FlexoConceptBindingModel(BindingModel baseBindingModel, FlexoConcept flexoConcept) {
		super(baseBindingModel);

		propertyVariablesMap = new HashMap<>();
		superVariablesMap = new HashMap<>();

		this.flexoConcept = flexoConcept;
		if (flexoConcept != null && flexoConcept.getPropertyChangeSupport() != null) {
			flexoConcept.getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		thisBindingVariable = new FlexoConceptBindingVariable(THIS_PROPERTY_NAME, flexoConcept);
		addToBindingVariables(thisBindingVariable);

		updateSuperBindingVariables();

		updateContainerBindingVariable();

		updatePropertyVariables();
		updateContainerFlexoConceptListener();
		updateParentFlexoConceptListeners();
	}

	public BindingVariable getThisBindingVariable() {
		return thisBindingVariable;
	}

	public FlexoConcept getFlexoConcept() {
		return flexoConcept;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == getBaseBindingModel()) {
			updatePropertyVariables();
		}
		if (evt.getSource() == flexoConcept) {
			if (evt.getPropertyName().equals(FlexoConcept.OWNER_KEY)) {
				// The FlexoConcept changes it's VirtualModel
				setBaseBindingModel(flexoConcept.getOwner() != null ? flexoConcept.getOwner().getBindingModel() : null);
				if (thisBindingVariable != null) {
					thisBindingVariable.setType(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept));
				}
				updateContainerBindingVariable();
				// virtualModelInstanceBindingVariable.setType(flexoConcept.getVirtualModel() != null ? VirtualModelInstanceType
				// .getFlexoConceptInstanceType(flexoConcept.getVirtualModel()) : FMLRTVirtualModelInstance.class);
			}
			else if (evt.getPropertyName().equals(FlexoConcept.FLEXO_PROPERTIES_KEY)) {
				// Roles were modified in related flexoConcept
				updatePropertyVariables();
			}
			else if (evt.getPropertyName().equals(FlexoConcept.PARENT_FLEXO_CONCEPTS_KEY)) {
				updateParentFlexoConceptListeners();
				updateSuperBindingVariables();
				updateContainerFlexoConceptListener();
				updateContainerBindingVariable();
				updatePropertyVariables();
			}
			else if ((evt.getPropertyName().equals(FlexoConcept.CONTAINER_FLEXO_CONCEPT_KEY)
					|| (evt.getPropertyName().equals(FlexoConcept.APPLICABLE_CONTAINER_FLEXO_CONCEPT_KEY)))) {
				updateContainerFlexoConceptListener();
				updateContainerBindingVariable();
				updatePropertyVariables();
			}
		}
		else if (knownParentConcepts.contains(evt.getSource()) || lastKnownContainer == evt.getSource()) {
			if (evt.getPropertyName().equals(FlexoConcept.FLEXO_PROPERTIES_KEY)) {
				// Roles were modified in any of parent or container FlexoConcept
				updatePropertyVariables();
			}
			else if (evt.getPropertyName().equals(FlexoConcept.PARENT_FLEXO_CONCEPTS_KEY)) {
				updateParentFlexoConceptListeners();
				updatePropertyVariables();
			}
			else if (evt.getPropertyName().equals(FlexoConcept.CONTAINER_FLEXO_CONCEPT_KEY)) {
				updateContainerFlexoConceptListener();
				updatePropertyVariables();
			}
		}
	}

	protected void updateContainerBindingVariable() {

		/*if (flexoConcept.getName().equals("CodingTaskType")) {
			System.out.println("Tiens, je l'ai, applicableContainerFlexoConcept=" + flexoConcept.getApplicableContainerFlexoConcept());
			System.out.println("containerFlexoConcept=" + flexoConcept.getContainerFlexoConcept());
		}*/

		if (flexoConcept.getApplicableContainerFlexoConcept() != null) {
			if (containerBindingVariable == null) {
				containerBindingVariable = new BindingVariable(CONTAINER_PROPERTY_NAME,
						flexoConcept.getApplicableContainerFlexoConcept().getInstanceType());
				addToBindingVariables(containerBindingVariable);
			}
			containerBindingVariable.setType(flexoConcept.getApplicableContainerFlexoConcept().getInstanceType());
		}
		else {
			if (flexoConcept.getOwner() != null) {
				if (containerBindingVariable == null) {
					containerBindingVariable = new BindingVariable(CONTAINER_PROPERTY_NAME, flexoConcept.getOwner().getInstanceType());
					addToBindingVariables(containerBindingVariable);
				}
				containerBindingVariable.setType(flexoConcept.getOwner().getInstanceType());
			}
			else {
				if (containerBindingVariable != null) {
					removeFromBindingVariables(containerBindingVariable);
					containerBindingVariable = null;
				}
			}
		}
	}

	protected void updateSuperBindingVariables() {

		if (flexoConcept.getParentFlexoConcepts().size() == 1) {
			if (superBindingVariable == null) {
				superBindingVariable = new SuperBindingVariable(flexoConcept.getParentFlexoConcepts().get(0), true);
				addToBindingVariables(superBindingVariable);
			}
			superBindingVariable.setType(flexoConcept.getParentFlexoConcepts().get(0).getInstanceType());
			clearSuperVariablesForMultipleInheritance();
		}
		else if (flexoConcept.getParentFlexoConcepts().size() > 1) {
			if (superBindingVariable != null) {
				removeFromBindingVariables(superBindingVariable);
				superBindingVariable = null;
			}
			updateSuperVariablesForMultipleInheritance();
		}
		else { // No parent
			if (superBindingVariable != null) {
				removeFromBindingVariables(superBindingVariable);
				superBindingVariable = null;
			}
			clearSuperVariablesForMultipleInheritance();
		}
	}

	private void clearSuperVariablesForMultipleInheritance() {
		if (!superVariablesMap.isEmpty()) {
			List<FlexoConcept> superVariablesToBeDeleted = new ArrayList<>(superVariablesMap.keySet());
			for (FlexoConcept concept : superVariablesToBeDeleted) {
				removeFromBindingVariables(superVariablesMap.get(concept));
				superVariablesMap.remove(concept);
			}
		}
	}

	private void updateSuperVariablesForMultipleInheritance() {

		List<FlexoConcept> superVariableToBeDeleted = new ArrayList<>(superVariablesMap.keySet());

		for (FlexoConcept superConcept : flexoConcept.getParentFlexoConcepts()) {
			if (superVariableToBeDeleted.contains(superConcept)) {
				superVariableToBeDeleted.remove(superConcept);
			}
			else if (superVariablesMap.get(superConcept) == null) {
				SuperBindingVariable bv = new SuperBindingVariable(superConcept, false);
				addToBindingVariables(bv);
				superVariablesMap.put(superConcept, bv);
			}
		}

		for (FlexoConcept superConcept : superVariableToBeDeleted) {
			SuperBindingVariable bvToRemove = superVariablesMap.get(superConcept);
			removeFromBindingVariables(bvToRemove);
			superVariablesMap.remove(superConcept);
			bvToRemove.delete();
		}

	}

	protected void updatePropertyVariables() {

		List<FlexoProperty<?>> propertiesToBeDeleted = new ArrayList<>(propertyVariablesMap.keySet());

		((FlexoConceptImpl) flexoConcept).clearAccessiblePropertiesCache();

		for (FlexoProperty<?> r : flexoConcept.getAccessibleProperties()) {
			if (propertiesToBeDeleted.contains(r)) {
				propertiesToBeDeleted.remove(r);
			}
			else if (propertyVariablesMap.get(r) == null
					&& (getBaseBindingModel() == null || getBaseBindingModel().bindingVariableNamed(r.getName()) == null)) {
				FlexoPropertyBindingVariable bv = null;
				if (r instanceof FlexoConceptInstanceRole) {
					bv = new FlexoConceptInstanceRoleBindingVariable((FlexoConceptInstanceRole) r);
				}
				else if (r instanceof ModelSlot) {
					bv = new ModelSlotBindingVariable((ModelSlot<?>) r);
				}
				else if (r instanceof FlexoRole) {
					bv = new FlexoRoleBindingVariable((FlexoRole<?>) r);
				}
				else if (r != null) {
					bv = new FlexoPropertyBindingVariable(r);
				}
				if (bv != null) {
					addToBindingVariables(bv);
					propertyVariablesMap.put(r, bv);
				}
			}
		}

		for (FlexoProperty<?> r : propertiesToBeDeleted) {
			FlexoPropertyBindingVariable bvToRemove = propertyVariablesMap.get(r);
			removeFromBindingVariables(bvToRemove);
			propertyVariablesMap.remove(r);
			bvToRemove.delete();
		}

	}

	private void updateParentFlexoConceptListeners() {

		List<FlexoConcept> parentConceptsNotToListenAnymore = new ArrayList<>();
		parentConceptsNotToListenAnymore.addAll(knownParentConcepts);

		for (FlexoConcept p : flexoConcept.getAllParentFlexoConcepts()) {
			if (parentConceptsNotToListenAnymore.contains(p)) {
				parentConceptsNotToListenAnymore.remove(p);
			}
			else {
				if (!knownParentConcepts.contains(p) && p.getPropertyChangeSupport() != null) {
					p.getPropertyChangeSupport().addPropertyChangeListener(this);
					knownParentConcepts.add(p);
				}
			}
		}

		for (FlexoConcept p : parentConceptsNotToListenAnymore) {
			if (p.getPropertyChangeSupport() != null) {
				p.getPropertyChangeSupport().removePropertyChangeListener(this);
				knownParentConcepts.remove(p);
			}
		}
	}

	private void updateContainerFlexoConceptListener() {

		if (lastKnownContainer != flexoConcept.getApplicableContainerFlexoConcept()) {
			if (lastKnownContainer != null) {
				if (lastKnownContainer.getPropertyChangeSupport() != null) {
					lastKnownContainer.getPropertyChangeSupport().removePropertyChangeListener(this);
				}
			}
			if (flexoConcept.getApplicableContainerFlexoConcept() != null) {
				if (flexoConcept.getApplicableContainerFlexoConcept().getPropertyChangeSupport() != null) {
					flexoConcept.getApplicableContainerFlexoConcept().getPropertyChangeSupport().addPropertyChangeListener(this);
				}
			}
			lastKnownContainer = flexoConcept.getApplicableContainerFlexoConcept();
		}

	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (flexoConcept != null && flexoConcept.getPropertyChangeSupport() != null) {
			flexoConcept.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		for (FlexoConcept p : new ArrayList<>(knownParentConcepts)) {
			if (p.getPropertyChangeSupport() != null) {
				p.getPropertyChangeSupport().removePropertyChangeListener(this);
				knownParentConcepts.remove(p);
			}
		}
		if (lastKnownContainer != null) {
			if (lastKnownContainer.getPropertyChangeSupport() != null) {
				lastKnownContainer.getPropertyChangeSupport().removePropertyChangeListener(this);
			}
		}
		super.delete();
		deleted = true;
	}

	private boolean deleted = false;

	public boolean isDeleted() {
		return deleted;
	}

	public void update() {
		if (isDeleted()) {
			return;
		}
		updateSuperBindingVariables();
		updateContainerBindingVariable();
		updatePropertyVariables();
		updateContainerFlexoConceptListener();
		updateParentFlexoConceptListeners();

	}

}
