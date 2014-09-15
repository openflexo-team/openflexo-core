package org.openflexo.foundation.viewpoint.binding;

import java.util.logging.Logger;

import org.openflexo.foundation.technologyadapter.ModelSlot;

public class ModelSlotBindingVariable extends FlexoRoleBindingVariable {
	static final Logger logger = Logger.getLogger(ModelSlotBindingVariable.class.getPackage().getName());

	public ModelSlotBindingVariable(ModelSlot<?> modelSlot) {
		super(modelSlot);
	}

	@Override
	public void delete() {
		super.delete();
	}

	public ModelSlot<?> getModelSlot() {
		return (ModelSlot<?>) getFlexoRole();
	}

}