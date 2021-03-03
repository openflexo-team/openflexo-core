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

package org.openflexo.foundation.fml.rt.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoObserver;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;

/**
 * Action used to create a FlexoConceptInstance
 * 
 * @author sylvain
 * 
 */
public class CreateFlexoConceptInstance extends FlexoAction<CreateFlexoConceptInstance, FlexoConceptInstance, FlexoObject>
		implements FlexoObserver, TechnologySpecificFlexoAction<FMLRTTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(CreateFlexoConceptInstance.class.getPackage().getName());

	public static FlexoActionFactory<CreateFlexoConceptInstance, FlexoConceptInstance, FlexoObject> actionType = new FlexoActionFactory<CreateFlexoConceptInstance, FlexoConceptInstance, FlexoObject>(
			"instantiate_flexo_concept", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateFlexoConceptInstance makeNewAction(FlexoConceptInstance focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new CreateFlexoConceptInstance(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptInstance object, Vector<FlexoObject> globalSelection) {
			if (object instanceof FMLRTVirtualModelInstance) {
				if (((FMLRTVirtualModelInstance) object).getVirtualModel() != null) {
					return ((FMLRTVirtualModelInstance) object).getVirtualModel().getAllRootFlexoConcepts(true, false).size() > 0;
				}
				return false;
			}
			if (object.getFlexoConcept() != null) {
				return object.getFlexoConcept().getEmbeddedFlexoConcepts().size() > 0;
			}
			return false;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptInstance object, Vector<FlexoObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateFlexoConceptInstance.actionType, FlexoConceptInstance.class);
	}

	private FlexoConceptInstance fciBeingCreated;

	private FlexoConceptInstance container;

	private FlexoConcept concept;
	private CreationScheme creationScheme;
	private CreationSchemeAction creationSchemeAction;

	protected CreateFlexoConceptInstance(FlexoConceptInstance focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public Class<? extends FMLRTTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLRTTechnologyAdapter.class;
	}

	@Override
	protected void doAction(Object context) throws FlexoException {
		logger.info("Add flexo concept instance in container " + getFocusedObject() + " creationSchemeAction=" + creationSchemeAction);

		// We create the FCI
		fciBeingCreated = getFocusedObject().getVirtualModelInstance().makeNewFlexoConceptInstance(getFlexoConcept());

		// We add the FlexoConceptInstance to the container BEFORE to execute creation scheme
		if (getContainer() instanceof FMLRTVirtualModelInstance) {
			// ((FMLRTVirtualModelInstance) getContainer()).addToFlexoConceptInstances(fciBeingCreated);
		}
		else {
			getContainer().addToEmbeddedFlexoConceptInstances(fciBeingCreated);
			// getFocusedObject().getVirtualModelInstance().removeFromFlexoConceptInstances(fciBeingCreated);
		}

		// We init the new FCI using a creation scheme
		if (creationSchemeAction != null) {

			// System.out.println("We now execute " + creationSchemeAction);
			// System.out.println("FML=" + creationSchemeAction.getCreationScheme().getFMLRepresentation());

			creationSchemeAction.initWithFlexoConceptInstance(fciBeingCreated);
			creationSchemeAction.doAction();

			fciBeingCreated = creationSchemeAction.getFlexoConceptInstance();
		}

	}

	@Override
	public boolean isValid() {
		if (getFlexoConcept() == null) {
			return false;
		}
		if (getFlexoConcept().hasCreationScheme()) {
			if (getCreationScheme() == null) {
				return false;
			}
			if (getCreationSchemeAction() == null) {
				return false;
			}
			if (!getCreationSchemeAction().areRequiredParametersSetAndValid()) {
				return false;
			}
		}
		return true;
	}

	public FlexoConceptInstance getNewFlexoConceptInstance() {
		return fciBeingCreated;
	}

	public FlexoConceptInstance getContainer() {
		if (container == null) {
			return getFocusedObject();
		}
		return container;
	}

	public void setContainer(FlexoConceptInstance container) {
		if (this.container != container) {
			FlexoConceptInstance oldValue = this.container;
			this.container = container;
			getPropertyChangeSupport().firePropertyChange("container", oldValue, container);
			setChanged();
			notifyObservers(new DataModification<>("isActionValidable", false, true));
		}
	}

	public FlexoConcept getFlexoConcept() {
		return concept;
	}

	public void setFlexoConcept(FlexoConcept concept) {
		if (this.concept != concept) {
			FlexoConcept oldValue = this.concept;
			this.concept = concept;
			getPropertyChangeSupport().firePropertyChange("flexoConcept", oldValue, concept);
			setChanged();
			notifyObservers(new DataModification<>("isActionValidable", false, true));
		}
	}

	/**
	 * Return a boolean indicating if all options are enough to execute the action
	 * 
	 * @return
	 */
	@Deprecated
	private boolean isActionValidable() {

		if (!isValid()) {
			return false;
		}
		if (getFlexoConcept() == null) {
			return false;
		}

		if (getFlexoConcept().hasCreationScheme()) {
			if (getCreationScheme() == null) {
				return false;
			}
			if (getCreationSchemeAction() == null) {
				return false;
			}
			if (!getCreationSchemeAction().areRequiredParametersSetAndValid()) {
				return false;
			}
		}
		return true;
	}

	public CreationScheme getCreationScheme() {
		return creationScheme;
	}

	public void setCreationScheme(CreationScheme creationScheme) {
		boolean wasValidable = isActionValidable();
		this.creationScheme = creationScheme;
		if (creationScheme != null) {
			creationSchemeAction = new CreationSchemeAction(creationScheme, getFocusedObject().getOwningVirtualModelInstance(), null, this);
			creationSchemeAction.addObserver(this);
			getPropertyChangeSupport().firePropertyChange("creationSchemeAction", null, creationSchemeAction);
		}
		else {
			creationSchemeAction = null;
		}
		getPropertyChangeSupport().firePropertyChange("creationScheme", null, creationScheme);
		getPropertyChangeSupport().firePropertyChange("creationSchemeAction", null, creationScheme);
		getPropertyChangeSupport().firePropertyChange("isActionValidable", wasValidable, isActionValidable());

	}

	public void setParameterValue(FlexoBehaviourParameter parameter, Object value) {
		if (creationSchemeAction != null) {
			creationSchemeAction.setParameterValue(parameter, value);
		}
	}

	@Override
	public void update(FlexoObservable observable, DataModification<?> dataModification) {
		if (dataModification.propertyName().equals(FlexoBehaviourAction.PARAMETER_VALUE_CHANGED)) {
			setChanged();
			notifyObservers(new DataModification<>("isActionValidable", false, true));
		}
	}

	public CreationSchemeAction getCreationSchemeAction() {
		return creationSchemeAction;
	}

}
