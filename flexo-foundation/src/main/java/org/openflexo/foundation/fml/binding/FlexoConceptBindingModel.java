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
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;

/**
 * This is the {@link BindingModel} exposed by a {@link FlexoConcept}<br>
 * This {@link BindingModel} is based on VirtualModel's (owner of this FlexoConcept) {@link BindingModel} if this owner is not null
 * 
 * Provides access to the {@link VirtualModelInstance}<br>
 * Allows reflexive access to the {@link VirtualModel} itself<br>
 * 
 * Note that default {@link RunTimeEvaluationContext} corresponding to this {@link BindingModel} is a {@link FlexoConceptInstance}
 * 
 * 
 * @author sylvain
 * 
 */
public class FlexoConceptBindingModel extends BindingModel implements PropertyChangeListener {

	private final FlexoConcept flexoConcept;

	private BindingVariable reflexiveAccessBindingVariable;
	// private BindingVariable virtualModelInstanceBindingVariable;
	private final Map<FlexoProperty<?>, FlexoPropertyBindingVariable> propertyVariablesMap;
	private final List<FlexoConcept> knownParentConcepts = new ArrayList<FlexoConcept>();

	private BindingVariable flexoConceptInstanceBindingVariable;

	public static final String REFLEXIVE_ACCESS_PROPERTY = "conceptDefinition";

	public static final String FLEXO_CONCEPT_INSTANCE_PROPERTY = "flexoConceptInstance";

	/**
	 * Build a new {@link BindingModel} dedicated to a FlexoConcept<br>
	 * Note that this constructor is called for final {@link FlexoConcept} (not for {@link VirtualModel} or any of future subclass of
	 * {@link FlexoConcept})
	 * 
	 * @param flexoConcept
	 */
	public FlexoConceptBindingModel(final FlexoConcept flexoConcept) {
		this(flexoConcept.getOwner() != null ? flexoConcept.getOwningVirtualModel().getBindingModel() : null, flexoConcept);
		reflexiveAccessBindingVariable = new BindingVariable(REFLEXIVE_ACCESS_PROPERTY, FlexoConcept.class);
		addToBindingVariables(reflexiveAccessBindingVariable);
		// TODO : Dirty, this should be fix when we have a clean type management system for the VM
		flexoConceptInstanceBindingVariable = new BindingVariable(FLEXO_CONCEPT_INSTANCE_PROPERTY,
				FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept)) {
			@Override
			public Type getType() {
				return FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept);
			}
		};
		addToBindingVariables(flexoConceptInstanceBindingVariable);
	}

	/**
	 * Base constructor for any subclass of {@link FlexoConcept} (eg {@link VirtualModel})
	 * 
	 * @param baseBindingModel
	 * @param flexoConcept
	 */
	protected FlexoConceptBindingModel(BindingModel baseBindingModel, FlexoConcept flexoConcept) {
		super(baseBindingModel);
		this.flexoConcept = flexoConcept;
		if (flexoConcept != null && flexoConcept.getPropertyChangeSupport() != null) {
			flexoConcept.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		propertyVariablesMap = new HashMap<FlexoProperty<?>, FlexoPropertyBindingVariable>();
		updatePropertyVariables();
		updateParentFlexoConceptListeners();
	}

	/**
	 * Return the reflexive access {@link BindingVariable}<br>
	 * (Allows reflexive access to the {@link FlexoConcept} itself)
	 * 
	 * @return
	 */
	public BindingVariable getReflexiveAccessBindingVariable() {
		return reflexiveAccessBindingVariable;
	}

	public BindingVariable getFlexoConceptInstanceBindingVariable() {
		return flexoConceptInstanceBindingVariable;
	}

	public FlexoConcept getFlexoConcept() {
		return flexoConcept;
	}

	/*public BindingVariable getVirtualModelInstanceBindingVariable() {
		return virtualModelInstanceBindingVariable;
	}*/

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == flexoConcept) {
			if (evt.getPropertyName().equals(FlexoConcept.OWNER_KEY)) {
				// The FlexoConcept changes it's VirtualModel
				setBaseBindingModel(flexoConcept.getOwner() != null ? flexoConcept.getOwner().getBindingModel() : null);
				flexoConceptInstanceBindingVariable.setType(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept));
				// virtualModelInstanceBindingVariable.setType(flexoConcept.getVirtualModel() != null ? VirtualModelInstanceType
				// .getFlexoConceptInstanceType(flexoConcept.getVirtualModel()) : VirtualModelInstance.class);
			}
			else if (evt.getPropertyName().equals(FlexoConcept.FLEXO_PROPERTIES_KEY)) {
				// Roles were modified in related flexoConcept
				updatePropertyVariables();
			}
			else if (evt.getPropertyName().equals(FlexoConcept.PARENT_FLEXO_CONCEPTS_KEY)) {
				updateParentFlexoConceptListeners();
				updatePropertyVariables();
			}
		}
		else if (knownParentConcepts.contains(evt.getSource())) {
			if (evt.getPropertyName().equals(FlexoConcept.FLEXO_PROPERTIES_KEY)) {
				// Roles were modified in any of parent FlexoConcept
				updatePropertyVariables();
			}
			else if (evt.getPropertyName().equals(FlexoConcept.PARENT_FLEXO_CONCEPTS_KEY)) {
				updateParentFlexoConceptListeners();
				updatePropertyVariables();
			}
		}
	}

	private void updatePropertyVariables() {

		List<FlexoProperty<?>> propertiesToBeDeleted = new ArrayList<FlexoProperty<?>>(propertyVariablesMap.keySet());

		for (FlexoProperty<?> r : flexoConcept.getAccessibleProperties()) {
			if (propertiesToBeDeleted.contains(r)) {
				propertiesToBeDeleted.remove(r);
			}
			else if (propertyVariablesMap.get(r) == null) {
				FlexoPropertyBindingVariable bv;
				if (r instanceof FlexoRole) {
					bv = new FlexoRoleBindingVariable((FlexoRole<?>) r);
				}
				else {
					bv = new FlexoPropertyBindingVariable(r);
				}
				addToBindingVariables(bv);
				propertyVariablesMap.put(r, bv);
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

		List<FlexoConcept> parentConceptsNotToListenAnymore = new ArrayList<FlexoConcept>();
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

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (flexoConcept != null && flexoConcept.getPropertyChangeSupport() != null) {
			flexoConcept.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		for (FlexoConcept p : new ArrayList<FlexoConcept>(knownParentConcepts)) {
			if (p.getPropertyChangeSupport() != null) {
				p.getPropertyChangeSupport().removePropertyChangeListener(this);
				knownParentConcepts.remove(p);
			}
		}
		super.delete();
	}
}
