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

package org.openflexo.foundation.fml.action;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoConceptStructuralFacet;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;

/**
 * Action allowing to create a {@link FlexoRole}<br>
 * 
 * To be valid, such action:
 * <ul>
 * <li>must be configured with a {@link FlexoConceptObject} as focused object</li>
 * <li>must declare a valid property name</li>
 * <li>must declare a valid cardinality</li>
 * <li>must declare a valid {@link FlexoRole} class</li>
 * <li>may declare a valid {@link ModelSlot} (depending on {@link FlexoRole} class)</li>
 * <li>may declare a valid {@link PrimitiveType} (if {@link FlexoRole} class is {@link PrimitiveRole})</li>
 * <li>may declare a valid {@link FlexoConcept} (if {@link FlexoRole} class is {@link FlexoConceptInstanceRole})</li>
 * <li>may declare a valid description</li>
 * </ul>
 */
public abstract class AbstractCreateFlexoRole<A extends AbstractCreateFlexoRole<A, MS>, MS extends ModelSlot>
		extends AbstractCreateFlexoProperty<A> {

	private static final Logger logger = Logger.getLogger(AbstractCreateFlexoRole.class.getPackage().getName());

	private MS modelSlot;
	protected MS defaultModelSlot;

	// private boolean useModelSlot;
	// private Class<? extends FlexoRole> flexoRoleClass;
	// private IFlexoOntologyClass individualType;
	// private FlexoConcept flexoConceptInstanceType;
	// private PrimitiveType primitiveType = PrimitiveType.String;

	private PropertyCardinality cardinality = PropertyCardinality.ZeroOne;

	protected FlexoRole<?> newFlexoRole;

	public AbstractCreateFlexoRole(FlexoActionType<A, FlexoConceptObject, FMLObject> actionType, FlexoConceptObject focusedObject,
			Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		defaultModelSlot = retrieveDefaultModelSlot();
	}

	public final String getRoleName() {
		return getPropertyName();
	}

	public final void setRoleName(String roleName) {
		setPropertyName(roleName);
	}

	@Override
	public final void setPropertyName(String propertyName) {
		super.setPropertyName(propertyName);
		getPropertyChangeSupport().firePropertyChange("roleName", null, getRoleName());
	}

	@Override
	protected final String getDefaultPropertyName() {
		if (getModelSlot() != null && getFlexoRoleClass() != null) {
			return getModelSlot().defaultFlexoRoleName(getFlexoRoleClass());
		}
		return "role";
	}

	public abstract Class<? extends FlexoRole<?>> getFlexoRoleClass();

	public final PropertyCardinality getCardinality() {
		return cardinality;
	}

	public final void setCardinality(PropertyCardinality propertyCardinality) {
		if (propertyCardinality != getCardinality()) {
			PropertyCardinality oldPropertyCardinality = getCardinality();
			this.cardinality = propertyCardinality;
			getPropertyChangeSupport().firePropertyChange("cardinality", oldPropertyCardinality, propertyCardinality);
		}
	}

	@Override
	public final FlexoRole<?> getNewFlexoProperty() {
		return getNewFlexoRole();
	}

	public FlexoRole<?> getNewFlexoRole() {
		return newFlexoRole;
	}

	@Override
	public boolean isValid() {
		if (!super.isValid()) {
			return false;
		}
		if (getCardinality() == null) {
			return false;
		}
		return true;
	}

	public abstract List<MS> getAvailableModelSlots();/* {
														
														if (getFocusedObject() instanceof VirtualModel) {
														return ((VirtualModel) getFocusedObject()).getModelSlots();
														} else if (getFocusedObject() != null && getFocusedObject().getOwningVirtualModel() != null) {
														return getFocusedObject().getOwningVirtualModel().getModelSlots();
														}
														return null;
														}*/

	public abstract Class<? extends MS> getModelSlotType();

	protected MS retrieveDefaultModelSlot() {
		FlexoConcept flexoConcept = null;
		// The action is visible for FlexoConcept and StructuralFacet.
		if (getFocusedObject() instanceof FlexoConceptStructuralFacet) {
			flexoConcept = ((FlexoConceptStructuralFacet) getFocusedObject()).getFlexoConcept();
		}
		else {
			flexoConcept = (FlexoConcept) getFocusedObject();
		}
		if (flexoConcept instanceof VirtualModel && ((VirtualModel) flexoConcept).getModelSlots(getModelSlotType()).size() > 0) {

			if (getFlexoRoleClass() == null) {
				return ((VirtualModel) flexoConcept).getModelSlots(getModelSlotType()).get(0);
			}
			// Trying to find the most adapted model slot
			for (ModelSlot<?> ms : ((VirtualModel) flexoConcept).getModelSlots(getModelSlotType())) {
				if (ms.getAvailableFlexoRoleTypes().contains(getFlexoRoleClass())) {
					return (MS) ms;
				}
			}
			return ((VirtualModel) flexoConcept).getModelSlots(getModelSlotType()).get(0);
		}
		else if (flexoConcept != null && flexoConcept.getOwningVirtualModel() != null
				&& flexoConcept.getOwningVirtualModel().getModelSlots(getModelSlotType()).size() > 0) {
			if (getFlexoRoleClass() == null) {
				return flexoConcept.getOwningVirtualModel().getModelSlots(getModelSlotType()).get(0);
			}
			// Trying to find the most adapted model slot
			for (ModelSlot<?> ms : flexoConcept.getOwningVirtualModel().getModelSlots(getModelSlotType())) {
				if (ms.getAvailableFlexoRoleTypes().contains(getFlexoRoleClass())) {
					return (MS) ms;
				}
			}

		}
		return null;
	}

	/**
	 * Return a metamodel adressed by a model slot
	 * 
	 * @return
	 */
	public FlexoMetaModel<?> getAdressedFlexoMetaModel() {
		if (getModelSlot() instanceof TypeAwareModelSlot) {
			TypeAwareModelSlot<?, ?> typeAwareModelSlot = (TypeAwareModelSlot<?, ?>) modelSlot;
			if (typeAwareModelSlot.getMetaModelResource() != null) {
				return typeAwareModelSlot.getMetaModelResource().getMetaModelData();
			}
		}
		return null;
	}

	public MS getModelSlot() {
		if (modelSlot == null) {
			if (defaultModelSlot == null) {
				defaultModelSlot = retrieveDefaultModelSlot();
			}
			return defaultModelSlot;
		}
		return modelSlot;
	}

	public void setModelSlot(MS modelSlot) {
		this.modelSlot = modelSlot;
		fireModelSlotChanged();
	}

	protected void fireModelSlotChanged() {
		getPropertyChangeSupport().firePropertyChange("modelSlot", null, modelSlot);
		getPropertyChangeSupport().firePropertyChange("roleName", null, getRoleName());
		getPropertyChangeSupport().firePropertyChange("propertyName", null, getRoleName());
	}

	public TechnologyAdapter getTechnologyAdapterForModelSlot() {
		if (getModelSlot() != null) {
			return getModelSlot().getModelSlotTechnologyAdapter();
		}
		else {
			return getFlexoConcept().getOwningVirtualModel().getTechnologyAdapter();
		}
	}

}
