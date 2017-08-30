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
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.InvalidParametersException;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;

public class CreationSchemeAction extends FlexoBehaviourAction<CreationSchemeAction, CreationScheme, VirtualModelInstance<?, ?>> {

	private static final Logger logger = Logger.getLogger(CreationSchemeAction.class.getPackage().getName());

	public static FlexoActionFactory<CreationSchemeAction, VirtualModelInstance<?, ?>, VirtualModelInstanceObject> actionType = new FlexoActionFactory<CreationSchemeAction, VirtualModelInstance<?, ?>, VirtualModelInstanceObject>(
			"create_flexo_concept_instance", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreationSchemeAction makeNewAction(VirtualModelInstance<?, ?> focusedObject,
				Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
			return new CreationSchemeAction(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(VirtualModelInstance<?, ?> object, Vector<VirtualModelInstanceObject> globalSelection) {
			return false;
		}

		@Override
		public boolean isEnabledForSelection(VirtualModelInstance<?, ?> object, Vector<VirtualModelInstanceObject> globalSelection) {
			return true;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(actionType, FMLRTVirtualModelInstance.class);
	}

	private VirtualModelInstance<?, ?> vmInstance;
	private FlexoConceptInstance container;
	private CreationScheme _creationScheme;

	CreationSchemeAction(VirtualModelInstance<?, ?> focusedObject, Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	// private Hashtable<EditionAction,FlexoModelObject> createdObjects;

	private FlexoConceptInstance newFlexoConceptInstance;

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

		// If referenced FlexoConcept is a plain FlexoConcept, we create it here and then we apply
		// control graph associated to CreationScheme
		// But if referenced FlexoConcept is a VirtualModel or a ViewPoint, it must has been
		// initialized using #initWithFlexoConceptInstance(FlexoConceptInstance) method

		retrieveMissingDefaultParameters();

		if (newFlexoConceptInstance == null) {
			// We have to create the FCI by ourselve
			if (getCreationScheme().getFlexoConcept() instanceof VirtualModel) {
				// AbstractCreateVirtualModelAction should be used instead
				throw new InvalidParametersException(
						"Cannot create an FMLRTVirtualModelInstance this way (AbstractCreateVirtualModelAction should be used instead)");
			}
			else if (getCreationScheme().getFlexoConcept() != null) {
				newFlexoConceptInstance = getVirtualModelInstance().makeNewFlexoConceptInstance(getFlexoConcept());
				if (getContainer() != null) {
					// System.out.println(">>>>>> On ajoute " + flexoConceptInstance + " dans " + getContainer());
					getContainer().addToEmbeddedFlexoConceptInstances(newFlexoConceptInstance);
				}
			}
			else {
				logger.warning("Could not create new FlexoConceptInstance because creation scheme refers to null FlexoConcept");
				throw new InvalidParametersException(
						"Could not create new FlexoConceptInstance because creation scheme refers to null FlexoConcept");
			}
		}

		executeControlGraph();

	}

	/**
	 * Used when creation of FlexoConceptInstance initialization is beeing delegated to an other component.<br>
	 * This happens for example in the case of FMLRTVirtualModelInstance creation, where the creation of FlexoConceptInstance is performed
	 * in the {@link AbstractCreateVirtualModelInstance} action
	 * 
	 * @param flexoConceptInstance
	 */
	public void initWithFlexoConceptInstance(FlexoConceptInstance flexoConceptInstance) {
		this.newFlexoConceptInstance = flexoConceptInstance;
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
			/*if (parameter instanceof ListParameter) {
				List<Object> list = (List<Object>) ((ListParameter) parameter).getList(this);
				parameterListValues.put((ListParameter) parameter, list);
			}*/
		}
		return returned;
	}

	@Override
	public VirtualModelInstance<?, ?> getVirtualModelInstance() {
		if (vmInstance == null) {
			vmInstance = getFocusedObject();
		}
		return vmInstance;
	}

	public void setVirtualModelInstance(VirtualModelInstance<?, ?> vmInstance) {
		this.vmInstance = vmInstance;
	}

	public CreationScheme getCreationScheme() {
		return _creationScheme;
	}

	public void setCreationScheme(CreationScheme creationScheme) {
		_creationScheme = creationScheme;
	}

	public FlexoConceptInstance getContainer() {
		return container;
	}

	public void setContainer(FlexoConceptInstance container) {
		if ((container == null && this.container != null) || (container != null && !container.equals(this.container))) {
			FlexoConceptInstance oldValue = this.container;
			this.container = container;
			getPropertyChangeSupport().firePropertyChange("container", oldValue, container);
		}
	}

	@Override
	public CreationScheme getFlexoBehaviour() {
		return getCreationScheme();
	}

	/**
	 * Return the new {@link FlexoConceptInstance} beeing created by this action
	 * 
	 * @return
	 */
	public FlexoConceptInstance getNewFlexoConceptInstance() {
		return newFlexoConceptInstance;
	}

	/**
	 * Return the {@link FlexoConceptInstance} on which this {@link FlexoBehaviour} is applied.<br>
	 * 
	 * Be carefull here: this is not the {@link FlexoConceptInstance} beeing created by this action, but the {@link VirtualModelInstance} in
	 * which {@link FlexoConceptInstance} is created
	 */
	@Override
	public VirtualModelInstance<?, ?> getFlexoConceptInstance() {
		return vmInstance;
	}

	@Override
	public VirtualModelInstance<?, ?> retrieveVirtualModelInstance() {
		return getVirtualModelInstance();
	}

}
