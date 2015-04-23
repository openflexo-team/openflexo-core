/**
 * 
 * Copyright (c) 2014, Openflexo
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
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;

public class CreateModelSlot extends FlexoAction<CreateModelSlot, AbstractVirtualModel<?>, FMLObject> {

	private static final Logger logger = Logger.getLogger(CreateModelSlot.class.getPackage().getName());

	public static FlexoActionType<CreateModelSlot, AbstractVirtualModel<?>, FMLObject> actionType = new FlexoActionType<CreateModelSlot, AbstractVirtualModel<?>, FMLObject>(
			"create_model_slot", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateModelSlot makeNewAction(AbstractVirtualModel<?> focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateModelSlot(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(AbstractVirtualModel<?> object, Vector<FMLObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(AbstractVirtualModel<?> object, Vector<FMLObject> globalSelection) {
			return true;
		}

	};

	static {
		// FlexoModelObject.addActionForClass(CreateModelSlot.actionType, ViewPoint.class);
		FlexoObjectImpl.addActionForClass(CreateModelSlot.actionType, AbstractVirtualModel.class);
	}

	private String modelSlotName;
	private String description;
	private TechnologyAdapter technologyAdapter;
	private FlexoMetaModelResource<?, ?, ?> mmRes;
	private VirtualModelResource vmRes;
	private boolean required = true;
	private boolean readOnly = false;
	private Class<? extends ModelSlot<?>> modelSlotClass;

	private ModelSlot newModelSlot;

	CreateModelSlot(AbstractVirtualModel<?> focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		logger.info("Add model slot, technologyAdapter=" + technologyAdapter);

		if (technologyAdapter == null) {
			throw new InvalidParameterException("No technology adapter supplied");
		}

		/*if (technologyAdapter instanceof FMLTechnologyAdapter) {
			FMLTechnologyAdapter virtualModelTechnologyAdapter = (FMLTechnologyAdapter) technologyAdapter;
			newModelSlot = virtualModelTechnologyAdapter.makeModelSlot(FMLRTModelSlot.class, getFocusedObject());
			newModelSlot.setName(modelSlotName);
			((FMLRTModelSlot) newModelSlot).setVirtualModelResource(vmRes);
			newModelSlot.setIsRequired(required);
			newModelSlot.setIsReadOnly(readOnly);
			newModelSlot.setDescription(description);
			getFocusedObject().addToModelSlots(newModelSlot);
		}*/

		if (technologyAdapter != null && getModelSlotClass() != null) {
			// if (getFocusedObject() instanceof VirtualModel) {
			newModelSlot = technologyAdapter.makeModelSlot(getModelSlotClass(), getFocusedObject());
			/*} else if (getFocusedObject() instanceof ViewPoint) {
				newModelSlot = technologyAdapter.createNewModelSlot((ViewPoint) getFocusedObject());
			}*/
			newModelSlot.setName(modelSlotName);
			if (newModelSlot instanceof FMLRTModelSlot) {
				((FMLRTModelSlot) newModelSlot).setVirtualModelResource(vmRes);

			} else if (newModelSlot instanceof TypeAwareModelSlot) {
				((TypeAwareModelSlot) newModelSlot).setMetaModelResource(mmRes);
			}
			newModelSlot.setIsRequired(required);
			newModelSlot.setIsReadOnly(readOnly);
			newModelSlot.setDescription(description);
			// if (getFocusedObject() instanceof VirtualModel) {
			getFocusedObject().addToModelSlots(newModelSlot);
			/*} else if (getFocusedObject() instanceof ViewPoint) {
				((ViewPoint) getFocusedObject()).addToModelSlots(newModelSlot);
			}*/
		}

	}

	public ModelSlot getNewModelSlot() {
		return newModelSlot;
	}

	private String validityMessage = EMPTY_NAME;

	private static final String DUPLICATED_NAME = FlexoLocalization.localizedForKey("this_name_is_already_used_please_choose_an_other_one");
	private static final String EMPTY_NAME = FlexoLocalization.localizedForKey("model_slot_must_have_an_non_empty_and_unique_name");
	private static final String NO_TECHNOLOGY_ADAPTER = FlexoLocalization.localizedForKey("please_choose_a_technology_adapter");
	private static final String NO_MODEL_SLOT_TYPE = FlexoLocalization.localizedForKey("please_choose_a_model_slot_type");
	private static final String NO_META_MODEL = FlexoLocalization.localizedForKey("please_choose_a_valid_metamodel");

	public String getValidityMessage() {
		return validityMessage;
	}

	@Override
	public boolean isValid() {

		if (StringUtils.isEmpty(modelSlotName)) {
			validityMessage = EMPTY_NAME;
			return false;
		} else if (getFocusedObject() instanceof VirtualModel && getFocusedObject().getModelSlot(modelSlotName) != null) {
			validityMessage = DUPLICATED_NAME;
			return false;
		} /*else if (getFocusedObject() instanceof ViewPoint && ((ViewPoint) getFocusedObject()).getModelSlot(modelSlotName) != null) {
			validityMessage = DUPLICATED_NAME;
			return false;
			}*/else if (technologyAdapter == null) {
			validityMessage = NO_TECHNOLOGY_ADAPTER;
			return false;
		} else if (technologyAdapter instanceof FMLTechnologyAdapter) {
			if (vmRes == null) {
				return false;
			} else {
				validityMessage = "";
				return true;
			}
		} else if (!(technologyAdapter instanceof FMLTechnologyAdapter)) {
			if (getModelSlotClass() == null) {
				validityMessage = NO_MODEL_SLOT_TYPE;
				return false;
			}
			if (mmRes == null && TypeAwareModelSlot.class.isAssignableFrom(getModelSlotClass())) {
				validityMessage = NO_META_MODEL;
				return false;
			} else {
				validityMessage = "";
				return true;
			}
		}
		return false;
	}

	private void fireChanges(boolean wasValid) {
		getPropertyChangeSupport().firePropertyChange("isTypeAwareModelSlot", null, isTypeAwareModelSlot());
		getPropertyChangeSupport().firePropertyChange("isVirtualModelModelSlot", null, isVirtualModelModelSlot());
		getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
		getPropertyChangeSupport().firePropertyChange("validityMessage", null, getValidityMessage());
	}

	public Class<? extends ModelSlot<?>> getModelSlotClass() {
		if (modelSlotClass == null && technologyAdapter != null && technologyAdapter.getAvailableModelSlotTypes().size() > 0) {
			return technologyAdapter.getAvailableModelSlotTypes().get(0);
		}
		return modelSlotClass;
	}

	public void setModelSlotClass(Class<? extends ModelSlot<?>> modelSlotClass) {
		boolean wasValid = isValid();
		this.modelSlotClass = modelSlotClass;
		getPropertyChangeSupport().firePropertyChange("modelSlotClass", modelSlotClass != null ? null : false, modelSlotClass);
		fireChanges(wasValid);
	}

	public boolean isTypeAwareModelSlot() {
		// System.out.println("isTypeAwareModelSlot ? with " + getModelSlotClass());
		// System.out.println("return " + (getModelSlotClass() != null && TypeAwareModelSlot.class.isAssignableFrom(getModelSlotClass())));
		return getModelSlotClass() != null && !isVirtualModelModelSlot() && TypeAwareModelSlot.class.isAssignableFrom(getModelSlotClass());
	}

	public boolean isVirtualModelModelSlot() {
		// System.out.println("isTypeAwareModelSlot ? with " + getModelSlotClass());
		// System.out.println("return " + (getModelSlotClass() != null && TypeAwareModelSlot.class.isAssignableFrom(getModelSlotClass())));
		return getModelSlotClass() != null && getModelSlotClass().equals(FMLRTModelSlot.class);
	}

	/**
	 * Return class of models this repository contains, in case of selected model slot class is a TypeAwareModelSlot
	 * 
	 * @return
	 */
	public final Class<? extends FlexoModel<?, ?>> getModelClass() {
		if (getModelSlotClass() != null && TypeAwareModelSlot.class.isAssignableFrom(getModelSlotClass())) {
			return (Class<? extends FlexoModel<?, ?>>) TypeUtils.getTypeArguments(getModelSlotClass(), TypeAwareModelSlot.class).get(
					TypeAwareModelSlot.class.getTypeParameters()[0]);
		}
		return null;
	}

	/**
	 * Return class of models this repository contains, in case of selected model slot class is a TypeAwareModelSlot
	 * 
	 * @return
	 */
	public final Class<? extends FlexoMetaModel<?>> getMetaModelClass() {
		if (getModelSlotClass() != null && TypeAwareModelSlot.class.isAssignableFrom(getModelSlotClass())) {
			return (Class<? extends FlexoMetaModel<?>>) TypeUtils.getTypeArguments(getModelSlotClass(), TypeAwareModelSlot.class).get(
					TypeAwareModelSlot.class.getTypeParameters()[1]);
		}
		return null;
	}

	public String getModelSlotName() {
		return modelSlotName;
	}

	public void setModelSlotName(String modelSlotName) {
		boolean wasValid = isValid();
		this.modelSlotName = modelSlotName;
		getPropertyChangeSupport().firePropertyChange("modelSlotName", null, modelSlotName);
		fireChanges(wasValid);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		boolean wasValid = isValid();
		this.description = description;
		getPropertyChangeSupport().firePropertyChange("description", null, description);
		fireChanges(wasValid);
	}

	public TechnologyAdapter getTechnologyAdapter() {
		return technologyAdapter;
	}

	public void setTechnologyAdapter(TechnologyAdapter technologyAdapter) {
		boolean wasValid = isValid();
		this.technologyAdapter = technologyAdapter;
		getPropertyChangeSupport().firePropertyChange("technologyAdapter", null, technologyAdapter);
		fireChanges(wasValid);
		if (getModelSlotClass() != null && !technologyAdapter.getAvailableModelSlotTypes().contains(getModelSlotClass())) {
			// The ModelSlot class is not consistent anymore
			if (technologyAdapter.getAvailableModelSlotTypes().size() > 0) {
				setModelSlotClass(technologyAdapter.getAvailableModelSlotTypes().get(0));
			} else {
				setModelSlotClass(null);
			}
		}
		if (getMmRes() != null && getMmRes().getTechnologyAdapter() != technologyAdapter) {
			// The MetaModel is not consistent anymore, nullify it
			setMmRes(null);
		}
	}

	public FlexoMetaModelResource<?, ?, ?> getMmRes() {
		return mmRes;
	}

	public void setMmRes(FlexoMetaModelResource<?, ?, ?> mmRes) {
		boolean wasValid = isValid();
		this.mmRes = mmRes;
		getPropertyChangeSupport().firePropertyChange("mmRes", mmRes != null ? null : false, mmRes);
		fireChanges(wasValid);
	}

	public VirtualModelResource getVmRes() {
		return vmRes;
	}

	public void setVmRes(VirtualModelResource vmRes) {
		boolean wasValid = isValid();
		this.vmRes = vmRes;
		getPropertyChangeSupport().firePropertyChange("vmRes", null, vmRes);
		fireChanges(wasValid);
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		boolean wasValid = isValid();
		this.required = required;
		getPropertyChangeSupport().firePropertyChange("required", null, required);
		fireChanges(wasValid);
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		boolean wasValid = isValid();
		this.readOnly = readOnly;
		getPropertyChangeSupport().firePropertyChange("readOnly", null, readOnly);
		fireChanges(wasValid);
	}

}
