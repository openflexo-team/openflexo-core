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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoConceptStructuralFacet;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.toolbox.StringUtils;

public class CreateModelSlot extends AbstractCreateFlexoProperty<CreateModelSlot> {

	private static final Logger logger = Logger.getLogger(CreateModelSlot.class.getPackage().getName());

	public static FlexoActionFactory<CreateModelSlot, FlexoConceptObject, FMLObject> actionType = new FlexoActionFactory<CreateModelSlot, FlexoConceptObject, FMLObject>(
			"create_model_slot", FlexoActionFactory.newPropertyMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateModelSlot makeNewAction(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateModelSlot(focusedObject.getFlexoConcept(), globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return object.getFlexoConcept() != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return true;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateModelSlot.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(CreateModelSlot.actionType, FlexoConceptStructuralFacet.class);
	}

	private String modelSlotName;
	private String description;
	private TechnologyAdapter<?> technologyAdapter;
	private FlexoMetaModelResource<?, ?, ?> mmRes;
	private CompilationUnitResource vmRes;
	private boolean required = false;
	private boolean readOnly = false;
	private Class<? extends ModelSlot<?>> modelSlotClass;

	private ModelSlot<?> newModelSlot;

	CreateModelSlot(FlexoConcept focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException, InvalidNameException {
		logger.info("Add model slot, technologyAdapter=" + technologyAdapter + " modelSlotClass=" + getModelSlotClass());

		if (technologyAdapter == null) {
			throw new InvalidParameterException("No technology adapter supplied");
		}

		if (technologyAdapter != null && getModelSlotClass() != null) {

			getVirtualModel().getCompilationUnit().ensureUse(getModelSlotClass());

			if (getVirtualModel() != null && !getVirtualModel().uses(getModelSlotClass())) {
				getVirtualModel().declareUse(getModelSlotClass());
			}

			// Add required uses declaration
			List<Class<? extends ModelSlot<?>>> msClasses = new ArrayList<>();
			for (UseModelSlotDeclaration useDecl : getVirtualModel().getCompilationUnit().getUseDeclarations()) {
				msClasses.add(useDecl.getModelSlotClass());
			}
			((CompilationUnitResource) getVirtualModel().getCompilationUnit().getResource()).updateFMLModelFactory(msClasses);

			newModelSlot = technologyAdapter.makeModelSlot(getModelSlotClass(), getFlexoConcept());
			newModelSlot.setName(modelSlotName);
			if (newModelSlot instanceof FMLRTModelSlot) {
				((FMLRTModelSlot<?, ?>) newModelSlot).setAccessedVirtualModelResource(vmRes);

			}
			else if (newModelSlot instanceof TypeAwareModelSlot) {
				((TypeAwareModelSlot) newModelSlot).setMetaModelResource(mmRes);
			}
			newModelSlot.setIsRequired(required);
			newModelSlot.setIsReadOnly(readOnly);
			newModelSlot.setDescription(description);
			getFlexoConcept().addToModelSlots(newModelSlot);

		}

	}

	public VirtualModel getVirtualModel() {
		if (getFlexoConcept() instanceof VirtualModel) {
			return (VirtualModel) getFlexoConcept();
		}
		else if (getFlexoConcept() != null) {
			return getFlexoConcept().getOwningVirtualModel();
		}
		return null;
	}

	public ModelSlot<?> getNewModelSlot() {
		return newModelSlot;
	}

	@Override
	public boolean isValid() {
		if (!super.isValid()) {
			return false;
		}
		if (StringUtils.isEmpty(modelSlotName)) {
			return false;
		}
		else if (getFlexoConcept().getModelSlot(modelSlotName) != null) {
			return false;
		}
		else if (technologyAdapter == null) {
			return false;
		}
		/*else if (technologyAdapter instanceof FMLTechnologyAdapter) {
			if (vmRes == null) {
				return false;
			}
			else {
				return true;
			}
		}
		else if (!(technologyAdapter instanceof FMLTechnologyAdapter)) {
			if (getModelSlotClass() == null) {
				return false;
			}
			if (mmRes == null && TypeAwareModelSlot.class.isAssignableFrom(getModelSlotClass())) {
				return false;
			}
			else {
				return true;
			}
		}*/
		return true;
	}

	public Class<? extends ModelSlot<?>> getModelSlotClass() {
		if (modelSlotClass == null && technologyAdapter != null && technologyAdapter.getAvailableModelSlotTypes().size() > 0) {
			return technologyAdapter.getAvailableModelSlotTypes().get(0);
		}
		return modelSlotClass;
	}

	public void setModelSlotClass(Class<? extends ModelSlot<?>> modelSlotClass) {
		this.modelSlotClass = modelSlotClass;
		getPropertyChangeSupport().firePropertyChange("modelSlotClass", modelSlotClass != null ? null : false, modelSlotClass);
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
			return (Class<? extends FlexoModel<?, ?>>) TypeUtils.getTypeArguments(getModelSlotClass(), TypeAwareModelSlot.class)
					.get(TypeAwareModelSlot.class.getTypeParameters()[0]);
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
			return (Class<? extends FlexoMetaModel<?>>) TypeUtils.getTypeArguments(getModelSlotClass(), TypeAwareModelSlot.class)
					.get(TypeAwareModelSlot.class.getTypeParameters()[1]);
		}
		return null;
	}

	public String getModelSlotName() {
		return modelSlotName;
	}

	public void setModelSlotName(String modelSlotName) {
		this.modelSlotName = modelSlotName;
		getPropertyChangeSupport().firePropertyChange("modelSlotName", null, modelSlotName);
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
		getPropertyChangeSupport().firePropertyChange("description", null, description);
	}

	public TechnologyAdapter<?> getTechnologyAdapter() {
		return technologyAdapter;
	}

	public void setTechnologyAdapter(TechnologyAdapter<?> technologyAdapter) {
		this.technologyAdapter = technologyAdapter;
		getPropertyChangeSupport().firePropertyChange("technologyAdapter", null, technologyAdapter);
		if (getModelSlotClass() != null && !technologyAdapter.getAvailableModelSlotTypes().contains(getModelSlotClass())) {
			// The ModelSlot class is not consistent anymore
			if (technologyAdapter.getAvailableModelSlotTypes().size() > 0) {
				setModelSlotClass(technologyAdapter.getAvailableModelSlotTypes().get(0));
			}
			else {
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
		this.mmRes = mmRes;
		getPropertyChangeSupport().firePropertyChange("mmRes", mmRes != null ? null : false, mmRes);
	}

	public CompilationUnitResource getVmRes() {
		return vmRes;
	}

	public void setVmRes(CompilationUnitResource vmRes) {
		this.vmRes = vmRes;
		getPropertyChangeSupport().firePropertyChange("vmRes", null, vmRes);
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
		getPropertyChangeSupport().firePropertyChange("required", null, required);
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		getPropertyChangeSupport().firePropertyChange("readOnly", null, readOnly);
	}

	@Override
	protected String getDefaultPropertyName() {
		return "modelSlot";
	}

	@Override
	public ModelSlot<?> getNewFlexoProperty() {
		return getNewModelSlot();
	}
}
