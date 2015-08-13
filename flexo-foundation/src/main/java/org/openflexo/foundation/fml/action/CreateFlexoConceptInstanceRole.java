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

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoConceptStructuralFacet;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;

/**
 * Action allowing to create a {@link FlexoConceptInstanceRole}<br>
 * 
 * To be valid, such action:
 * <ul>
 * <li>must be configured with a {@link FlexoConceptObject} as focused object</li>
 * <li>must declare a valid property name</li>
 * <li>must declare a valid cardinality</li>
 * <li>may declare a valid {@link ModelSlot} (depending on {@link FlexoRole} class)</li>
 * <li>may declare a valid {@link FlexoConcept} as type of {@link FlexoConceptInstance}</li>
 * <li>may declare a valid description</li>
 * </ul>
 */
public class CreateFlexoConceptInstanceRole extends AbstractCreateFlexoRole<CreateFlexoConceptInstanceRole, FMLRTModelSlot> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateFlexoConceptInstanceRole.class.getPackage().getName());

	public static FlexoActionType<CreateFlexoConceptInstanceRole, FlexoConceptObject, FMLObject> actionType = new FlexoActionType<CreateFlexoConceptInstanceRole, FlexoConceptObject, FMLObject>(
			"create_flexo_concept_instance_role", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateFlexoConceptInstanceRole makeNewAction(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection,
				FlexoEditor editor) {
			return new CreateFlexoConceptInstanceRole(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateFlexoConceptInstanceRole.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(CreateFlexoConceptInstanceRole.actionType, FlexoConceptStructuralFacet.class);
	}

	private boolean useModelSlot;
	private FlexoConcept flexoConceptInstanceType;

	public CreateFlexoConceptInstanceRole(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		// logger.info("Add flexo role, flexoRoleClass=" + flexoRoleClass);
		// logger.info("modelSlot = " + getModelSlot());

		if (getModelSlot() != null) {
			newFlexoRole = getModelSlot().makeFlexoRole(FlexoConceptInstanceRole.class);
		} else {
			FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
			newFlexoRole = factory.newInstance(FlexoConceptInstanceRole.class);
		}

		newFlexoRole.setRoleName(getRoleName());
		newFlexoRole.setCardinality(getCardinality());

		((FlexoConceptInstanceRole) newFlexoRole).setFlexoConceptType(getFlexoConceptInstanceType());

		finalizeDoAction(context);

	}

	@Override
	public boolean isValid() {
		if (!super.isValid()) {
			return false;
		}
		if (getModelSlotVirtualModel() == null) {
			return false;
		}
		return true;
	}

	public AbstractVirtualModel<?> getModelSlotVirtualModel() {
		if (getModelSlot() == null || !useModelSlot) {
			return getFlexoConcept().getVirtualModel();
		} else {
			if (getModelSlot().getVirtualModelResource() != null) {
				return getModelSlot().getVirtualModelResource().getVirtualModel();
			}
		}
		return null;
	}

	// A GARDER
	@Override
	public Class<FMLRTModelSlot> getModelSlotType() {
		return FMLRTModelSlot.class;
	}

	// A GARDER
	@Override
	public List<FMLRTModelSlot> getAvailableModelSlots() {
		if (getFocusedObject() instanceof VirtualModel) {
			return ((VirtualModel) getFocusedObject()).getModelSlots(FMLRTModelSlot.class);
		} else if (getFocusedObject() != null && getFocusedObject().getOwningVirtualModel() != null) {
			return getFocusedObject().getOwningVirtualModel().getModelSlots(FMLRTModelSlot.class);
		}
		return null;
	}

	// A GARDER
	@Override
	protected FMLRTModelSlot retrieveDefaultModelSlot() {
		FlexoConcept flexoConcept = null;
		// The action is visible for FlexoConcept and StructuralFacet.
		if (getFocusedObject() instanceof FlexoConceptStructuralFacet) {
			flexoConcept = ((FlexoConceptStructuralFacet) getFocusedObject()).getFlexoConcept();
		} else {
			flexoConcept = (FlexoConcept) getFocusedObject();
		}
		if (flexoConcept instanceof VirtualModel && ((VirtualModel) flexoConcept).getModelSlots(FMLRTModelSlot.class).size() > 0) {
			return ((VirtualModel) flexoConcept).getModelSlots(FMLRTModelSlot.class).get(0);
		} else if (flexoConcept != null && flexoConcept.getOwningVirtualModel() != null
				&& flexoConcept.getOwningVirtualModel().getModelSlots(FMLRTModelSlot.class).size() > 0) {
			if (getFlexoRoleClass() == null) {
				return flexoConcept.getOwningVirtualModel().getModelSlots(FMLRTModelSlot.class).get(0);
			}
			// Trying to find the most adapted model slot
			for (FMLRTModelSlot ms : flexoConcept.getOwningVirtualModel().getModelSlots(FMLRTModelSlot.class)) {
				if (ms.getAddressedVirtualModel().getFlexoConcepts().contains(getFlexoConceptInstanceType())) {
					return ms;
				}
			}
		}
		return null;
	}

	@Override
	public void setModelSlot(FMLRTModelSlot modelSlot) {
		super.setModelSlot(modelSlot);
		getPropertyChangeSupport().firePropertyChange("modelSlotVirtualModel", null, getModelSlotVirtualModel());
	}

	public FlexoConcept getFlexoConceptInstanceType() {
		return flexoConceptInstanceType;
	}

	public void setFlexoConceptInstanceType(FlexoConcept flexoConceptInstanceType) {
		// The default model slot may change
		FMLRTModelSlot oldModelSlot = getModelSlot();
		this.flexoConceptInstanceType = flexoConceptInstanceType;
		defaultModelSlot = retrieveDefaultModelSlot();
		getPropertyChangeSupport().firePropertyChange("modelSlot", oldModelSlot, getModelSlot());

		getPropertyChangeSupport().firePropertyChange("flexoConceptInstanceType", null, flexoConceptInstanceType);
	}

	public boolean isUseModelSlot() {
		return useModelSlot;
	}

	public void setUseModelSlot(boolean useModelSlot) {
		this.useModelSlot = useModelSlot;
		if (!useModelSlot) {
			setModelSlot(null);
		}
		getPropertyChangeSupport().firePropertyChange("useModelSlot", null, useModelSlot);
		getPropertyChangeSupport().firePropertyChange("modelSlot", null, getModelSlot());
	}

	@Override
	public Class<FlexoConceptInstanceRole> getFlexoRoleClass() {
		return FlexoConceptInstanceRole.class;
	}

}
