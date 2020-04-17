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
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.toolbox.StringUtils;

public class RenameFlexoConcept extends FlexoAction<RenameFlexoConcept, FlexoConcept, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(RenameFlexoConcept.class.getPackage().getName());

	public static FlexoActionFactory<RenameFlexoConcept, FlexoConcept, FMLObject> actionType = new FlexoActionFactory<RenameFlexoConcept, FlexoConcept, FMLObject>(
			"rename", FlexoActionFactory.refactorMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public RenameFlexoConcept makeNewAction(FlexoConcept focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new RenameFlexoConcept(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConcept object, Vector<FMLObject> globalSelection) {
			return object != null && !(object instanceof VirtualModel);
		}

		@Override
		public boolean isEnabledForSelection(FlexoConcept object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(RenameFlexoConcept.actionType, FlexoConcept.class);
	}

	private String newFlexoConceptName;
	private String newFlexoConceptDescription;

	RenameFlexoConcept(FlexoConcept focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		newFlexoConceptName = focusedObject.getName();
		newFlexoConceptDescription = focusedObject.getDescription();
	}

	@Override
	protected void doAction(Object context) {

		System.out.println("Rename concept to " + getNewFlexoConceptName());

		getFocusedObject().setName(getNewFlexoConceptName());
		getFocusedObject().setDescription(getNewFlexoConceptDescription());

	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	public FMLTechnologyAdapter getFMLTechnologyAdapter() {
		return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
	}

	public String getNewFlexoConceptName() {
		return newFlexoConceptName;
	}

	public void setNewFlexoConceptName(String newFlexoConceptName) {
		this.newFlexoConceptName = newFlexoConceptName;
		getPropertyChangeSupport().firePropertyChange("newFlexoConceptName", null, newFlexoConceptName);
	}

	public String getNewFlexoConceptDescription() {
		return newFlexoConceptDescription;
	}

	public void setNewFlexoConceptDescription(String newFlexoConceptDescription) {
		this.newFlexoConceptDescription = newFlexoConceptDescription;
		getPropertyChangeSupport().firePropertyChange("newFlexoConceptDescription", null, newFlexoConceptDescription);
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(getNewFlexoConceptName())) {
			return false;
		}
		if (getFocusedObject().getDeclaringVirtualModel().getFlexoConcept(getNewFlexoConceptName()) != null) {
			return false;
		}
		return true;
	}

}
