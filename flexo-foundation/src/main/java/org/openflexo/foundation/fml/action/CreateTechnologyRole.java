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

import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoConceptStructuralFacet;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;

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
public class CreateTechnologyRole extends AbstractCreateFlexoRole<CreateTechnologyRole, ModelSlot<?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateTechnologyRole.class.getPackage().getName());

	public static FlexoActionType<CreateTechnologyRole, FlexoConceptObject, FMLObject> actionType = new FlexoActionType<CreateTechnologyRole, FlexoConceptObject, FMLObject>(
			"create_technology_role", FlexoActionType.newPropertyMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateTechnologyRole makeNewAction(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateTechnologyRole(focusedObject, globalSelection, editor);
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
		FlexoObjectImpl.addActionForClass(CreateTechnologyRole.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(CreateTechnologyRole.actionType, FlexoConceptStructuralFacet.class);
	}

	private Class<? extends FlexoRole<?>> flexoRoleClass;

	public CreateTechnologyRole(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class<? extends ModelSlot<?>> getModelSlotType() {
		return (Class) ModelSlot.class;
	}

	public List<Class<? extends FlexoRole<?>>> getAvailableFlexoRoleTypes() {
		if (getModelSlot() != null) {
			return getModelSlot().getAvailableFlexoRoleTypes();
		}
		return null;
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		// logger.info("Add flexo role, flexoRoleClass=" + flexoRoleClass);
		// logger.info("modelSlot = " + getModelSlot());

		if (flexoRoleClass != null) {
			if (getModelSlot() != null) {
				newFlexoRole = getModelSlot().makeFlexoRole(flexoRoleClass);
				newFlexoRole.setModelSlot(getModelSlot());
			}
			else {
				FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
				newFlexoRole = factory.newInstance(flexoRoleClass);
			}

			if (newFlexoRole != null) {
				newFlexoRole.setRoleName(getRoleName());
				newFlexoRole.setCardinality(getCardinality());
				newFlexoRole.setModelSlot(getModelSlot());
				finalizeDoAction(context);
			}
		}

		else {
			throw new InvalidParameterException("No FlexoRole class defined");
		}

	}

	@Override
	public boolean isValid() {
		if (!super.isValid()) {
			return false;
		}
		if (flexoRoleClass == null) {
			return false;
		}
		return true;
	}

	@Override
	public List<ModelSlot<?>> getAvailableModelSlots() {

		if (getFocusedObject() instanceof VirtualModel) {
			return ((VirtualModel) getFocusedObject()).getModelSlots();
		}
		else if (getFocusedObject() != null && getFocusedObject().getOwningVirtualModel() != null) {
			return getFocusedObject().getOwningVirtualModel().getModelSlots();
		}
		return null;
	}

	@Override
	public void fireModelSlotChanged() {
		super.fireModelSlotChanged();
		getPropertyChangeSupport().firePropertyChange("availableFlexoRoleTypes", null, getAvailableFlexoRoleTypes());
		if (getFlexoRoleClass() != null && !getAvailableFlexoRoleTypes().contains(getFlexoRoleClass())) {
			if (getAvailableFlexoRoleTypes().size() > 0) {
				setFlexoRoleClass(getAvailableFlexoRoleTypes().get(0));
			}
			else {
				setFlexoRoleClass(null);
			}
		}
		if (getModelSlot() != null && getFlexoRoleClass() == null && getAvailableFlexoRoleTypes().size() > 0) {
			setFlexoRoleClass(getAvailableFlexoRoleTypes().get(0));
		}
	}

	@Override
	public Class<? extends FlexoRole<?>> getFlexoRoleClass() {
		return flexoRoleClass;
	}

	public void setFlexoRoleClass(Class<? extends FlexoRole<?>> flexoRoleClass) {
		this.flexoRoleClass = flexoRoleClass;

		// The default model slot may change
		ModelSlot<?> oldModelSlot = getModelSlot();
		defaultModelSlot = retrieveDefaultModelSlot();
		getPropertyChangeSupport().firePropertyChange("modelSlot", oldModelSlot, getModelSlot());

		getPropertyChangeSupport().firePropertyChange("roleName", null, getRoleName());
		getPropertyChangeSupport().firePropertyChange("propertyName", null, getRoleName());
		getPropertyChangeSupport().firePropertyChange("flexoRoleClass", flexoRoleClass != null ? null : false, flexoRoleClass);
	}

	@Override
	public final FlexoRole<?> getNewFlexoRole() {
		return super.getNewFlexoRole();
	}

}
