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

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.editionaction.AddToListAction;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.ReturnStatement;
import org.openflexo.toolbox.StringUtils;

/**
 * Instantiate a new DeclarationAction while embedding focused {@link AssignableAction} in a variable declaration
 * 
 * @author sylvain
 *
 */
public class DeclareNewVariableAction extends FlexoAction<DeclareNewVariableAction, AssignableAction<?>, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DeclareNewVariableAction.class.getPackage().getName());

	public static FlexoActionFactory<DeclareNewVariableAction, AssignableAction<?>, FMLObject> actionType = new FlexoActionFactory<DeclareNewVariableAction, AssignableAction<?>, FMLObject>(
			"declares_new_variable", FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DeclareNewVariableAction makeNewAction(AssignableAction<?> focusedObject, Vector<FMLObject> globalSelection,
				FlexoEditor editor) {
			return new DeclareNewVariableAction(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(AssignableAction<?> object, Vector<FMLObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(AssignableAction<?> object, Vector<FMLObject> globalSelection) {
			return object != null && !(object instanceof DeclarationAction) && !(object instanceof AssignationAction)
					&& !(object instanceof AddToListAction) && !(object instanceof ReturnStatement);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(DeclareNewVariableAction.actionType, AssignableAction.class);
	}

	private DeclarationAction<?> declarationAction;
	private String newVariableName;

	private DeclareNewVariableAction(AssignableAction<?> focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		declarationAction = getFocusedObject().declaresNewVariable(getNewVariableName());

	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(getNewVariableName())) {
			return false;
		}
		if (getFocusedObject().getBindingModel().bindingVariableNamed(getNewVariableName()) != null) {
			return false;
		}
		return true;
	}

	public String getNewVariableName() {
		return newVariableName;
	}

	public void setNewVariableName(String newVariableName) {
		if ((newVariableName == null && this.newVariableName != null)
				|| (newVariableName != null && !newVariableName.equals(this.newVariableName))) {
			String oldValue = this.newVariableName;
			this.newVariableName = newVariableName;
			getPropertyChangeSupport().firePropertyChange("newVariableName", oldValue, newVariableName);
		}
	}

	public DeclarationAction<?> getDeclarationAction() {
		return declarationAction;
	}

}
