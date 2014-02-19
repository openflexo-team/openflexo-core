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
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.InvalidParametersException;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.view.VirtualModelInstanceObject;
import org.openflexo.foundation.viewpoint.DeletionScheme;
import org.openflexo.foundation.viewpoint.EditionScheme;
import org.openflexo.foundation.viewpoint.binding.PatternRoleBindingVariable;

public class DeletionSchemeAction extends EditionSchemeAction<DeletionSchemeAction, DeletionScheme, FlexoConceptInstance> {

	private static final Logger logger = Logger.getLogger(DeletionSchemeAction.class.getPackage().getName());

	public static FlexoActionType<DeletionSchemeAction, FlexoConceptInstance, VirtualModelInstanceObject> actionType = new FlexoActionType<DeletionSchemeAction, FlexoConceptInstance, VirtualModelInstanceObject>(
			"delete_edition_pattern_instance", FlexoActionType.editGroup, FlexoActionType.DELETE_ACTION_TYPE) {

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
	private FlexoConceptInstance editionPatternInstanceToDelete;

	DeletionSchemeAction(FlexoConceptInstance focusedObject, Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParametersException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Delete FlexoConceptInstance using DeletionScheme");
			logger.fine("getDeletionScheme()=" + getDeletionScheme());
			logger.fine("getEditionPatternInstance()=" + getEditionPatternInstance());
		}
		applyEditionActions();

	}

	/**
	 * Return the {@link FlexoConceptInstance} on which this {@link EditionScheme} is applied.<br>
	 * This is this instance that will be deleted.
	 * 
	 * @return
	 */
	@Override
	public final FlexoConceptInstance getEditionPatternInstance() {
		return getFocusedObject();
	}

	@Override
	public VirtualModelInstance getVirtualModelInstance() {
		if (vmInstance == null) {
			FlexoConceptInstance vObject = getFocusedObject();
			if (vObject instanceof VirtualModelInstance) {
				vmInstance = (VirtualModelInstance) getFocusedObject();
			} else if (vObject instanceof FlexoConceptInstance) {
				vmInstance = ((FlexoConceptInstance) vObject).getVirtualModelInstance();
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
	public FlexoConceptInstance getEditionPatternInstance() {
		return getEditionPatternInstanceToDelete();
	}

	public FlexoConceptInstance getEditionPatternInstanceToDelete() {
		if (editionPatternInstanceToDelete == null && getFocusedObject() instanceof DiagramElement) {
			editionPatternInstanceToDelete = ((DiagramElement) getFocusedObject()).getEditionPatternInstance();
		}
		return editionPatternInstanceToDelete;
	}

	public void setEditionPatternInstanceToDelete(FlexoConceptInstance editionPatternInstanceToDelete) {
		this.editionPatternInstanceToDelete = editionPatternInstanceToDelete;
	}*/

	@Override
	public VirtualModelInstance retrieveVirtualModelInstance() {
		return getVirtualModelInstance();
	}

	@Override
	public Object getValue(BindingVariable variable) {
		if (variable instanceof PatternRoleBindingVariable) {
			return getEditionPatternInstance().getPatternActor(((PatternRoleBindingVariable) variable).getPatternRole());
		} else if (variable.getVariableName().equals(EditionScheme.THIS)) {
			return getEditionPatternInstance();
		}
		return super.getValue(variable);
	}

	@Override
	public void setValue(Object value, BindingVariable variable) {
		if (variable instanceof PatternRoleBindingVariable) {
			getEditionPatternInstance().setPatternActor(value, ((PatternRoleBindingVariable) variable).getPatternRole());
			return;
		}
		super.setValue(value, variable);
	}

	public FlexoConceptInstance getEditionPatternInstanceToDelete() {
		if (editionPatternInstanceToDelete == null && getFocusedObject() != null) {
			editionPatternInstanceToDelete = getFocusedObject();
		}
		return editionPatternInstanceToDelete;
	}

	public void setEditionPatternInstanceToDelete(FlexoConceptInstance editionPatternInstanceToDelete) {
		this.editionPatternInstanceToDelete = editionPatternInstanceToDelete;
	}

}
