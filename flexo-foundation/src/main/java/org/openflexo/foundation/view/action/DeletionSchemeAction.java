/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.view.action;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.InvalidParametersException;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.view.VirtualModelInstanceObject;
import org.openflexo.foundation.viewpoint.DeletionScheme;
import org.openflexo.foundation.viewpoint.FlexoBehaviour;
import org.openflexo.foundation.viewpoint.FlexoRole;
import org.openflexo.foundation.viewpoint.binding.FlexoRoleBindingVariable;

public class DeletionSchemeAction extends FlexoBehaviourAction<DeletionSchemeAction, DeletionScheme, FlexoConceptInstance> {

	private static final Logger logger = Logger.getLogger(DeletionSchemeAction.class.getPackage().getName());

	public static FlexoActionType<DeletionSchemeAction, FlexoConceptInstance, VirtualModelInstanceObject> actionType = new FlexoActionType<DeletionSchemeAction, FlexoConceptInstance, VirtualModelInstanceObject>(
			"delete_flexo_concept_instance", FlexoActionType.editGroup, FlexoActionType.DELETE_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DeletionSchemeAction makeNewAction(FlexoConceptInstance focusedObject, Vector<VirtualModelInstanceObject> globalSelection,
				FlexoEditor editor) {
			return new DeletionSchemeAction(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptInstance object, Vector<VirtualModelInstanceObject> globalSelection) {
			return false;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptInstance object, Vector<VirtualModelInstanceObject> globalSelection) {
			return true;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(actionType, FlexoConceptInstance.class);
	}

	private VirtualModelInstance vmInstance;
	private DeletionScheme deletionScheme;
	private FlexoConceptInstance flexoConceptInstanceToDelete;

	DeletionSchemeAction(FlexoConceptInstance focusedObject, Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParametersException, FlexoException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Delete FlexoConceptInstance using DeletionScheme");
			logger.fine("getDeletionScheme()=" + getDeletionScheme());
			logger.fine("getFlexoConceptInstance()=" + getFlexoConceptInstance());
		}
		applyEditionActions();

	}

	/**
	 * Return the {@link FlexoConceptInstance} on which this {@link FlexoBehaviour} is applied.<br>
	 * This is this instance that will be deleted.
	 * 
	 * @return
	 */
	@Override
	public final FlexoConceptInstance getFlexoConceptInstance() {
		if (flexoConceptInstanceToDelete == null && getFocusedObject() != null) {
			flexoConceptInstanceToDelete = getFocusedObject();
		}
		return flexoConceptInstanceToDelete;
	}

	@Override
	public VirtualModelInstance getVirtualModelInstance() {
		if (vmInstance == null) {
			FlexoConceptInstance vObject = getFocusedObject();
			if (vObject instanceof VirtualModelInstance) {
				vmInstance = (VirtualModelInstance) getFocusedObject();
			}
			else if (vObject instanceof FlexoConceptInstance) {
				vmInstance = vObject.getVirtualModelInstance();
			}
		}
		return vmInstance;
	}

	public void setVirtualModelInstance(VirtualModelInstance vmInstance) {
		this.vmInstance = vmInstance;
	}

	public DeletionScheme getDeletionScheme() {
		return deletionScheme;
	}

	public void setDeletionScheme(DeletionScheme deletionScheme) {
		this.deletionScheme = deletionScheme;
	}

	@Override
	public DeletionScheme getEditionScheme() {
		return getDeletionScheme();
	}

	/*@Override
	public FlexoConceptInstance getFlexoConceptInstance() {
		return getFlexoConceptInstanceToDelete();
	}

	public FlexoConceptInstance getFlexoConceptInstanceToDelete() {
		if (flexoConceptInstanceToDelete == null && getFocusedObject() instanceof DiagramElement) {
			flexoConceptInstanceToDelete = ((DiagramElement) getFocusedObject()).getFlexoConceptInstance();
		}
		return flexoConceptInstanceToDelete;
	}

	public void setFlexoConceptInstanceToDelete(FlexoConceptInstance flexoConceptInstanceToDelete) {
		this.flexoConceptInstanceToDelete = flexoConceptInstanceToDelete;
	}*/

	@Override
	public VirtualModelInstance retrieveVirtualModelInstance() {
		return getVirtualModelInstance();
	}

	@Override
	public Object getValue(BindingVariable variable) {
		FlexoConceptInstance fci = this.getFlexoConceptInstanceToDelete();
		if (fci != null && variable != null) {
			if (variable instanceof FlexoRoleBindingVariable) {
				FlexoRole role = ((FlexoRoleBindingVariable) variable).getFlexoRole();
				if (role != null) {
					return fci.getFlexoActor(role);
				}
				else {
					logger.warning("Trying to delete a null actor for : " + fci.getFlexoConceptURI() + "/" + variable.toString());
				}
			}
			else if (variable.getVariableName().equals(FlexoBehaviour.FLEXO_BEHAVIOUR_INSTANCE)) {
				return getFlexoConceptInstance();
			}
			return super.getValue(variable);
		}
		return null;
	}

	@Override
	public void setValue(Object value, BindingVariable variable) {
		if (variable instanceof FlexoRoleBindingVariable) {
			getFlexoConceptInstance().setFlexoActor(value, ((FlexoRoleBindingVariable) variable).getFlexoRole());
			return;
		}
		super.setValue(value, variable);
	}

	public FlexoConceptInstance getFlexoConceptInstanceToDelete() {
		if (flexoConceptInstanceToDelete == null && getFocusedObject() != null) {
			flexoConceptInstanceToDelete = getFocusedObject();
		}
		return flexoConceptInstanceToDelete;
	}

	public void setFlexoConceptInstanceToDelete(FlexoConceptInstance flexoConceptInstanceToDelete) {
		this.flexoConceptInstanceToDelete = flexoConceptInstanceToDelete;
	}

}
