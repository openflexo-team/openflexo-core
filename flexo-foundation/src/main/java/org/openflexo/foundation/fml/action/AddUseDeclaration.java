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

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.InconsistentFlexoConceptHierarchyException;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.UseModelSlotDeclaration;

/**
 * This action allows to explicitely declare use of a {@link ModelSlot} class in a Virtual Model
 * 
 * @author sylvain
 *
 */
public class AddUseDeclaration extends FlexoAction<AddUseDeclaration, VirtualModel, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(AddUseDeclaration.class.getPackage().getName());

	public static FlexoActionFactory<AddUseDeclaration, VirtualModel, FMLObject> actionType = new FlexoActionFactory<AddUseDeclaration, VirtualModel, FMLObject>(
			"declare_use_of_model_slot", FlexoActionFactory.advancedGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public AddUseDeclaration makeNewAction(VirtualModel focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new AddUseDeclaration(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(VirtualModel object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(VirtualModel object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(AddUseDeclaration.actionType, VirtualModel.class);
	}

	private AddUseDeclaration(VirtualModel focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	private UseModelSlotDeclaration newUseDeclaration;

	@Override
	protected void doAction(Object context) throws InconsistentFlexoConceptHierarchyException {
		logger.info("Declare use");
		newUseDeclaration = getFocusedObject().declareUse(getModelSlotClass());
	}

	public UseModelSlotDeclaration getNewUseDeclaration() {
		return newUseDeclaration;
	}

	private TechnologyAdapter<?> modelSlotTechnologyAdapter;
	private Class<? extends ModelSlot<?>> modelSlotClass;

	public TechnologyAdapter<?> getModelSlotTechnologyAdapter() {
		return modelSlotTechnologyAdapter;
	}

	public void setModelSlotTechnologyAdapter(TechnologyAdapter<?> technologyAdapter) {
		this.modelSlotTechnologyAdapter = technologyAdapter;
		getPropertyChangeSupport().firePropertyChange("modelSlotTechnologyAdapter", null, technologyAdapter);
		if (getModelSlotClass() != null && !technologyAdapter.getAvailableModelSlotTypes().contains(getModelSlotClass())) {
			// The ModelSlot class is not consistent anymore
			if (technologyAdapter.getAvailableModelSlotTypes().size() > 0) {
				setModelSlotClass(technologyAdapter.getAvailableModelSlotTypes().get(0));
			}
			else {
				setModelSlotClass(null);
			}
		}
	}

	public Class<? extends ModelSlot<?>> getModelSlotClass() {
		if (modelSlotClass == null && modelSlotTechnologyAdapter != null
				&& modelSlotTechnologyAdapter.getAvailableModelSlotTypes().size() > 0) {
			return modelSlotTechnologyAdapter.getAvailableModelSlotTypes().get(0);
		}
		return modelSlotClass;
	}

	public void setModelSlotClass(Class<? extends ModelSlot<?>> modelSlotClass) {
		this.modelSlotClass = modelSlotClass;
		getPropertyChangeSupport().firePropertyChange("modelSlotClass", modelSlotClass != null ? null : false, modelSlotClass);
	}

}
