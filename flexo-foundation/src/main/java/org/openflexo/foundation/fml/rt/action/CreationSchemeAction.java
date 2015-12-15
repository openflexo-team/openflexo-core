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

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.InvalidParametersException;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.ListParameter;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;

public class CreationSchemeAction extends FlexoBehaviourAction<CreationSchemeAction, CreationScheme, AbstractVirtualModelInstance<?, ?>> {

	private static final Logger logger = Logger.getLogger(CreationSchemeAction.class.getPackage().getName());

	public static FlexoActionType<CreationSchemeAction, AbstractVirtualModelInstance<?, ?>, VirtualModelInstanceObject> actionType = new FlexoActionType<CreationSchemeAction, AbstractVirtualModelInstance<?, ?>, VirtualModelInstanceObject>(
			"create_flexo_concept_instance", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreationSchemeAction makeNewAction(AbstractVirtualModelInstance<?, ?> focusedObject,
				Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
			return new CreationSchemeAction(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(AbstractVirtualModelInstance<?, ?> object,
				Vector<VirtualModelInstanceObject> globalSelection) {
			return false;
		}

		@Override
		public boolean isEnabledForSelection(AbstractVirtualModelInstance<?, ?> object,
				Vector<VirtualModelInstanceObject> globalSelection) {
			return true;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(actionType, AbstractVirtualModelInstance.class);
	}

	private AbstractVirtualModelInstance<?, ?> vmInstance;
	private CreationScheme _creationScheme;

	CreationSchemeAction(AbstractVirtualModelInstance<?, ?> focusedObject, Vector<VirtualModelInstanceObject> globalSelection,
			FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	// private Hashtable<EditionAction,FlexoModelObject> createdObjects;

	private FlexoConceptInstance flexoConceptInstance;

	@Override
	public boolean isValid() {
		if (!super.isValid()) {
			return false;
		}
		if (getFlexoConceptInstance() == null) {
			return false;
		}
		return true;
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParametersException, FlexoException {
		// logger.info("Create FlexoConceptInstance using CreationScheme");

		retrieveMissingDefaultParameters();

		// getFlexoConcept().getViewPoint().getViewpointOntology().loadWhenUnloaded();

		// In case of this action is embedded in a CreateVirtualModelInstance action, the flexoConceptInstance (which will be here a
		// VirtualModelInstance) will be already initialized and should subsequently not been recreated)
		/*if (flexoConceptInstance == null) {
			if (getVirtualModelInstance() != null) {
				if (getCreationScheme().getFlexoConcept() instanceof ViewPoint) {
					System.out.println("OK faut creer une vue la");
				}
				else if (getCreationScheme().getFlexoConcept() instanceof VirtualModel) {
					CreateBasicVirtualModelInstance createVMIAction = CreateBasicVirtualModelInstance.actionType
							.makeNewEmbeddedAction((View) getFocusedObject(), null, this);
					createVMIAction.setSkipChoosePopup(true);
					createVMIAction.setEscapeModelSlotConfiguration(true);
					// createVMIAction.setCreationScheme(getCreationScheme());
					createVMIAction.setNewVirtualModelInstanceName("Prout");
					createVMIAction.setNewVirtualModelInstanceTitle("Un titre qui fait prout");
					createVMIAction.doAction();
					flexoConceptInstance = createVMIAction.getNewVirtualModelInstance();
					System.out.println("OK j'ai cree le nouveau vmi: " + flexoConceptInstance);
				}
				else if (getCreationScheme().getFlexoConcept() != null) {
					flexoConceptInstance = getVirtualModelInstance().makeNewFlexoConceptInstance(getFlexoConcept());
				}
				else {
					logger.warning("Could not create new FlexoConceptInstance because creation scheme refers to null FlexoConcept");
					throw new InvalidParametersException("CreationScheme");
				}
			}
			else {
				logger.warning("Could not create new FlexoConceptInstance because container VirtualModelInstance is null");
				throw new InvalidParametersException("VirtualModelInstance");
			}
		}*/

		executeControlGraph();

	}

	/**
	 * Used when creation of FlexoConceptInstance initialization is beeing delegated to an other component.<br>
	 * This happens for example in the case of VirtualModelInstance creation, where the creation of FlexoConceptInstance is performed in the
	 * {@link AbstractCreateVirtualModelInstance} action
	 * 
	 * @param flexoConceptInstance
	 */
	public void initWithFlexoConceptInstance(FlexoConceptInstance flexoConceptInstance) {
		this.flexoConceptInstance = flexoConceptInstance;
	}

	public boolean retrieveMissingDefaultParameters() {
		boolean returned = true;
		FlexoBehaviour flexoBehaviour = getFlexoBehaviour();
		for (final FlexoBehaviourParameter parameter : flexoBehaviour.getParameters()) {
			if (getParameterValue(parameter) == null) {
				logger.warning("Found not initialized parameter " + parameter);
				Object defaultValue = parameter.getDefaultValue(this);
				if (defaultValue != null) {
					logger.warning("Du coup je lui donne la valeur " + defaultValue);
					parameterValues.put(parameter, defaultValue);
					if (!parameter.isValid(this, defaultValue)) {
						logger.info("Parameter " + parameter + " is not valid for value " + defaultValue);
						returned = false;
					}
				}
			}
			if (parameter instanceof ListParameter) {
				List<Object> list = (List<Object>) ((ListParameter) parameter).getList(this);
				parameterListValues.put((ListParameter) parameter, list);
			}
		}
		return returned;
	}

	@Override
	public AbstractVirtualModelInstance<?, ?> getVirtualModelInstance() {
		if (vmInstance == null) {
			vmInstance = getFocusedObject();
		}
		return vmInstance;
	}

	public void setVirtualModelInstance(AbstractVirtualModelInstance<?, ?> vmInstance) {
		this.vmInstance = vmInstance;
	}

	public CreationScheme getCreationScheme() {
		return _creationScheme;
	}

	public void setCreationScheme(CreationScheme creationScheme) {
		_creationScheme = creationScheme;
	}

	@Override
	public CreationScheme getFlexoBehaviour() {
		return getCreationScheme();
	}

	@Override
	public FlexoConceptInstance getFlexoConceptInstance() {
		return flexoConceptInstance;
	}

	@Override
	public AbstractVirtualModelInstance<?, ?> retrieveVirtualModelInstance() {
		return getVirtualModelInstance();
	}

	/**
	 * This is the internal code performing execution of a single {@link EditionAction} defined to be part of the execution control graph of
	 * related {@link FlexoBehaviour}<br>
	 */
	/*@Override
	protected Object performAction(EditionAction action, Hashtable<EditionAction, Object> performedActions) throws FlexoException {
		Object assignedObject = super.performAction(action, performedActions);
		if (assignedObject != null && action instanceof AssignableAction) {
			AssignableAction assignableAction = (AssignableAction) action;
			if (assignableAction.getFlexoRole() != null) {
				getFlexoConceptInstance().setObjectForFlexoRole(assignedObject, assignableAction.getFlexoRole());
			}
		}
	
		return assignedObject;
	}*/

	/*@Override
	public Object getValue(BindingVariable variable) {
		return super.getValue(variable);
	}*/

	/*@Override
	public void setValue(Object value, BindingVariable variable) {
		if (variable instanceof FlexoPropertyBindingVariable) {
			getFlexoConceptInstance().setFlexoActor(value, (FlexoProperty) ((FlexoPropertyBindingVariable) variable).getFlexoProperty());
			return;
		}
		super.setValue(value, variable);
	}*/

}
