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

package org.openflexo.foundation.fml.rt.action;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.action.InvalidParametersException;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.binding.FlexoPropertyBindingVariable;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;

public class DeletionSchemeAction extends FlexoBehaviourAction<DeletionSchemeAction, DeletionScheme, FlexoConceptInstance> {

	private static final Logger logger = Logger.getLogger(DeletionSchemeAction.class.getPackage().getName());

	/*public static FlexoActionType<DeletionSchemeAction, FlexoConceptInstance, VirtualModelInstanceObject> actionType = new FlexoActionType<DeletionSchemeAction, FlexoConceptInstance, VirtualModelInstanceObject>(
			"delete_flexo_concept_instance", FlexoActionType.editGroup, FlexoActionType.DELETE_ACTION_TYPE) {

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
	}*/

	private final DeletionSchemeActionType actionType;

	public DeletionSchemeAction(DeletionSchemeActionType actionType, FlexoConceptInstance focusedObject,
			Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		this.actionType = actionType;
	}

	public DeletionScheme getDeletionScheme() {
		if (actionType != null) {
			return actionType.getDeletionScheme();
		}
		return null;
	}

	private VirtualModelInstance vmInstance;
	// private DeletionScheme deletionScheme;
	private FlexoConceptInstance flexoConceptInstanceToDelete;

	/*DeletionSchemeAction(FlexoConceptInstance focusedObject, Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}*/

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
			} else if (vObject instanceof FlexoConceptInstance) {
				vmInstance = vObject.getVirtualModelInstance();
			}
		}
		return vmInstance;
	}

	public void setVirtualModelInstance(VirtualModelInstance vmInstance) {
		this.vmInstance = vmInstance;
	}

	/*public DeletionScheme getDeletionScheme() {
		return deletionScheme;
	}

	public void setDeletionScheme(DeletionScheme deletionScheme) {
		this.deletionScheme = deletionScheme;
	}*/

	@Override
	public DeletionScheme getFlexoBehaviour() {
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
			if (variable instanceof FlexoPropertyBindingVariable) {
				FlexoProperty<?> role = ((FlexoPropertyBindingVariable) variable).getFlexoProperty();
				if (role != null) {
					return fci.getFlexoActor(role);
				} else {
					logger.warning("Trying to delete a null actor for : " + fci.getFlexoConceptURI() + "/" + variable.toString());
				}
			}
			return super.getValue(variable);
		}
		return null;
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
