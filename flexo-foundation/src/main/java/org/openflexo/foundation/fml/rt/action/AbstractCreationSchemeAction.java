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
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.InvalidParametersException;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.AbstractActionScheme;
import org.openflexo.foundation.fml.AbstractCreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;

/**
 * Provides execution environment of a {@link AbstractCreationScheme} on a given {@link VirtualModelInstance} as a {@link FlexoAction}<br>
 * The {@link VirtualModelInstance} is the instance where new {@link FlexoConceptInstance} will be instantiated
 *
 * Abstract base implementation for a {@link FlexoAction} which aims at executing a {@link AbstractActionScheme}
 * 
 * An {@link AbstractCreationSchemeAction} represents the execution (in the "instances" world) of an {@link AbstractCreationScheme} in a
 * given {@link VirtualModelInstance}.<br>
 * To be used and executed on Openflexo platform, it is wrapped in a {@link FlexoAction}.<br>
 * 
 * @author sylvain
 *
 * @param <A>
 *            type of {@link AbstractCreationSchemeAction} beeing executed
 * @param <FB>
 *            type of {@link AbstractCreationScheme}
 * @param <O>
 *            type of {@link VirtualModelInstance} on which this action applies
 */
public class AbstractCreationSchemeAction<A extends AbstractCreationSchemeAction<A, FB, O>, FB extends AbstractCreationScheme, O extends VirtualModelInstance<?, ?>>
		extends FlexoBehaviourAction<A, FB, O> {

	private static final Logger logger = Logger.getLogger(AbstractCreationSchemeAction.class.getPackage().getName());

	private FlexoConceptInstance container;
	private FlexoConceptInstance newFlexoConceptInstance;

	/**
	 * Constructor to be used with a factory
	 * 
	 * @param actionFactory
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	public AbstractCreationSchemeAction(AbstractCreationSchemeActionFactory<A, FB, O> actionFactory, O focusedObject,
			Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	/**
	 * Constructor to be used for creating a new action without factory
	 * 
	 * @param creationScheme
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	public AbstractCreationSchemeAction(FB creationScheme, O focusedObject, Vector<VirtualModelInstanceObject> globalSelection,
			FlexoEditor editor) {
		super(creationScheme, focusedObject, globalSelection, editor);
	}

	/**
	 * Constructor to be used for creating a new action as an action embedded in another one
	 * 
	 * @param creationScheme
	 * @param focusedObject
	 * @param globalSelection
	 * @param ownerAction
	 */
	public AbstractCreationSchemeAction(FB creationScheme, O focusedObject, Vector<VirtualModelInstanceObject> globalSelection,
			FlexoAction<?, ?, ?> ownerAction) {
		super(creationScheme, focusedObject, globalSelection, ownerAction);
	}

	public FB getCreationScheme() {
		return getFlexoBehaviour();
	}

	public FlexoConcept getFlexoConceptBeingCreated() {
		if (getFlexoBehaviour() != null) {
			return getFlexoBehaviour().getFlexoConcept();
		}
		return null;
	}

	@Override
	public boolean isValid() {
		if (!super.isValid()) {
			return false;
		}

		if (getFlexoBehaviour() == null) {
			return false;
		}

		if (getVirtualModelInstance() == null) {
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
			newFlexoConceptInstance = makeNewFlexoConceptInstance();
		}

		// System.out.println("OK on execute " + getFlexoBehaviour().getFMLRepresentation());

		executeControlGraph();
	}

	/**
	 * Called to create new {@link FlexoConceptInstance} when not externally initialized
	 * 
	 * @return
	 * @throws InvalidParametersException
	 */
	protected FlexoConceptInstance makeNewFlexoConceptInstance() throws InvalidParametersException {
		// We have to create the FCI by ourselve
		if (getFlexoConceptBeingCreated() instanceof VirtualModel) {
			// AbstractCreateVirtualModelAction should be used instead
			throw new InvalidParametersException(
					"Cannot create an FMLRTVirtualModelInstance this way (AbstractCreateVirtualModelAction should be used instead)");
		}
		else if (getFlexoConceptBeingCreated() != null) {
			if (getContainer() != null) {
				return getVirtualModelInstance().makeNewFlexoConceptInstance(getFlexoConcept(), getContainer());
			}
			return getVirtualModelInstance().makeNewFlexoConceptInstance(getFlexoConcept());
		}
		else {
			logger.warning("Could not create new FlexoConceptInstance because creation scheme refers to null FlexoConcept");
			throw new InvalidParametersException(
					"Could not create new FlexoConceptInstance because creation scheme refers to null FlexoConcept");
		}
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
					logger.fine("Giving value " + defaultValue);
					parameterValues.put(parameter.getArgumentName(), defaultValue);
					if (!parameter.isValid(this, defaultValue)) {
						logger.info("Parameter " + parameter + " is not valid for value " + defaultValue);
						returned = false;
					}
				}
			}
		}
		return returned;
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
	 * Take care that on a {@link AbstractCreationSchemeAction}, returned {@link FlexoConceptInstance} is the created instance
	 * 
	 * @return
	 */
	@Override
	public final FlexoConceptInstance getFlexoConceptInstance() {
		return getNewFlexoConceptInstance();
	}

	/**
	 * Return the {@link VirtualModelInstance} on which we work<br>
	 * Take care that on a {@link AbstractCreationSchemeAction}, returned {@link VirtualModelInstance} is the container of created
	 * {@link FlexoConceptInstance}
	 */
	@Override
	public final VirtualModelInstance<?, ?> getVirtualModelInstance() {
		return (VirtualModelInstance<?, ?>) super.getFlexoConceptInstance();
	}

	@Override
	protected void compensateCancelledExecution() {
		// We will simply delete the created instance
		if (newFlexoConceptInstance != null) {
			System.out.println("Delete FlexoConceptInstance as compensation of Cancel");
			newFlexoConceptInstance.delete();
		}
	}

}
