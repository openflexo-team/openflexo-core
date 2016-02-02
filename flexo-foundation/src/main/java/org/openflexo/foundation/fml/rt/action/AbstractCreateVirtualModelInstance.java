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

import java.security.InvalidParameterException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoObserver;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.InvalidArgumentException;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * Abstract base implementation for an action which aims at creating a new AbstractVirtualModelInstance (a View or a VirtualModelInstance)
 * This action is called to create a new {@link VirtualModelInstance} in a {@link View}
 * 
 * @author sylvain
 * 
 * @param <A>
 *            type of action, required to manage introspection for inheritance
 * @param <T>
 *            type of container of newly created AbstractVirtualModelInstance
 */
public abstract class AbstractCreateVirtualModelInstance<A extends AbstractCreateVirtualModelInstance<A, T, VMI, VM>, T extends FlexoObject, VMI extends AbstractVirtualModelInstance<VMI, VM>, VM extends AbstractVirtualModel<VM>>
		extends FlexoAction<A, T, FlexoObject> implements FlexoObserver {

	private static final Logger logger = Logger.getLogger(AbstractCreateVirtualModelInstance.class.getPackage().getName());

	private VMI newVirtualModelInstance;

	private String newVirtualModelInstanceName;
	private String newVirtualModelInstanceTitle;
	private VM virtualModel;
	private CreationScheme creationScheme;
	private CreationSchemeAction creationSchemeAction;

	private boolean skipChoosePopup = false;
	private boolean escapeModelSlotConfiguration = false;

	protected AbstractCreateVirtualModelInstance(FlexoActionType<A, T, FlexoObject> actionType, T focusedObject,
			Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		modelSlotConfigurations = new Hashtable<ModelSlot<?>, ModelSlotInstanceConfiguration<?, ?>>();
	}

	public abstract AbstractVirtualModelInstanceResource<VMI, VM> makeVirtualModelInstanceResource() throws SaveResourceException;

	/**
	 * Return boolean indicating if proposed name is a valid as name for the new AbstractVirtualModelInstance
	 * 
	 * @param proposedName
	 * @return
	 */
	public abstract boolean isValidVirtualModelInstanceName(String proposedName);

	@Override
	protected void doAction(Object context) throws FlexoException {
		logger.info("Add virtual model instance in view " + getFocusedObject() + " creationSchemeAction=" + creationSchemeAction);

		System.out.println("getNewVirtualModelInstanceName()=" + getNewVirtualModelInstanceName());
		System.out.println("getNewVirtualModelInstanceTitle()=" + getNewVirtualModelInstanceTitle());

		if (StringUtils.isEmpty(getNewVirtualModelInstanceName())) {
			throw new InvalidParameterException("virtual model instance name is undefined");
		}

		/*int index = 1;
		String baseName = getNewVirtualModelInstanceName();
		while (!isValidVirtualModelInstanceName(getNewVirtualModelInstanceName())) {
			newVirtualModelInstanceName = baseName + index;
			index++;
		}*/

		AbstractVirtualModelInstanceResource<VMI, VM> newVirtualModelInstanceResource = makeVirtualModelInstanceResource();

		newVirtualModelInstance = newVirtualModelInstanceResource.getVirtualModelInstance();

		logger.info("Added virtual model instance " + newVirtualModelInstance + " in container " + getFocusedObject());

		// System.out.println("OK, we have created the file " + newVirtualModelInstanceResource.getFile().getAbsolutePath());
		System.out.println("OK, we have created the VirtualModelInstanceResource " + newVirtualModelInstanceResource.getURI()
				+ " delegate=" + newVirtualModelInstanceResource.getFlexoIODelegate().stringRepresentation());

		System.out.println("creationSchemeAction=" + creationSchemeAction);

		// If we do not escape model slot configuration, this is the right time to do it
		if (!escapeModelSlotConfiguration()) {
			for (ModelSlot ms : virtualModel.getModelSlots()) {
				// System.out.println("*** ModelSlot: " + ms);
				ModelSlotInstanceConfiguration<?, ?> configuration = getModelSlotInstanceConfiguration(ms);
				if (configuration.isValidConfiguration()) {
					ModelSlotInstance msi = configuration.createModelSlotInstance(newVirtualModelInstance, getContainerView());
					msi.setVirtualModelInstance(newVirtualModelInstance);
					newVirtualModelInstance.addToModelSlotInstances(msi);
				} else {
					logger.warning("Wrong configuration for model slot: " + configuration.getModelSlot() + " error: "
							+ configuration.getErrorMessage());
					throw new InvalidArgumentException("Wrong configuration for model slot " + configuration.getModelSlot()
							+ " configuration=" + configuration);
				}
			}
		}

		// We init the new VMI using a creation scheme
		if (creationSchemeAction != null) {

			System.out.println("We now execute " + creationSchemeAction);
			System.out.println("FML=" + creationSchemeAction.getCreationScheme().getFMLRepresentation());

			creationSchemeAction.initWithFlexoConceptInstance(newVirtualModelInstance);
			creationSchemeAction.setFocusedObject(newVirtualModelInstance);
			creationSchemeAction.doAction();
			if (creationSchemeAction.getThrownException() != null) {
				throw creationSchemeAction.getThrownException();
			}

		}

		// We add the VirtualModelInstance to the view
		if (getContainerView() != null) {
			getContainerView().addToVirtualModelInstances(newVirtualModelInstance);
		}

		System.out.println("Now, we try to synchronize the new virtual model instance");

		if (newVirtualModelInstance.isSynchronizable()) {
			System.out.println("Go for it");
			newVirtualModelInstance.synchronize(null);
		}

		System.out.println("Saving file again...");
		newVirtualModelInstanceResource.save(null);
	}

	/*private String errorMessage;
	
	public String getErrorMessage() {
		isValid();
		// System.out.println("valid=" + isValid());
		// System.out.println("errorMessage=" + errorMessage);
		return errorMessage;
	}*/

	public int getStepsNumber() {
		if (virtualModel == null) {
			return 1;
		} else if (!getVirtualModel().hasCreationScheme()) {
			return virtualModel.getModelSlots().size() + 1;
		} else {
			return virtualModel.getModelSlots().size() + 2;
		}
	}

	@Override
	public boolean isValid() {
		if (getVirtualModel() == null) {
			return false;
		}
		if (StringUtils.isEmpty(getNewVirtualModelInstanceName())) {
			return false;
		}

		if (StringUtils.isEmpty(getNewVirtualModelInstanceTitle())) {
			return false;
		}

		if (!isValidVirtualModelInstanceName(getNewVirtualModelInstanceName())) {
			return false;
		}

		return true;
	}

	public VMI getNewVirtualModelInstance() {
		return newVirtualModelInstance;
	}

	private final Hashtable<ModelSlot<?>, ModelSlotInstanceConfiguration<?, ?>> modelSlotConfigurations;

	public VM getVirtualModel() {
		return virtualModel;
	}

	public void setVirtualModel(VM virtualModel) {
		if (virtualModel != this.virtualModel) {
			this.virtualModel = virtualModel;
			modelSlotConfigurations.clear();
			if (this.virtualModel != null) {
				for (ModelSlot<?> ms : this.virtualModel.getModelSlots()) {
					modelSlotConfigurations.put(ms, ms.createConfiguration(null, getProject()));
				}
			}
			setChanged();
			notifyObservers(new DataModification("isActionValidable", false, true));
		}
	}

	/*public DiagramSpecification getDiagramSpecification() {
		if (virtualModel instanceof DiagramSpecification) {
			return (DiagramSpecification) virtualModel;
		} else {
			return null;
		}
	}
	
	public void setDiagramSpecification(DiagramSpecification diagramSpecification) {
		if (diagramSpecification != this.virtualModel) {
			this.virtualModel = diagramSpecification;
			modelSlotConfigurations.clear();
			if (this.virtualModel != null) {
				for (ModelSlot<?> ms : this.virtualModel.getModelSlots()) {
					modelSlotConfigurations.put(ms, ms.createConfiguration(this));
				}
			}
		}
	}*/

	public ModelSlotInstanceConfiguration<?, ?> getModelSlotInstanceConfiguration(ModelSlot<?> ms) {
		return modelSlotConfigurations.get(ms);
	}

	/**
	 * Return a boolean indicating if all options are enough to execute the action
	 * 
	 * @return
	 */
	public boolean isActionValidable() {

		if (!isValid()) {
			return false;
		}
		if (getVirtualModel() == null) {
			return false;
		}
		for (ModelSlot ms : virtualModel.getModelSlots()) {
			ModelSlotInstanceConfiguration<?, ?> configuration = getModelSlotInstanceConfiguration(ms);
			if (!configuration.isValidConfiguration()) {
				return false;
			}
		}
		if (getVirtualModel().hasCreationScheme()) {
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

	public String getNewVirtualModelInstanceName() {
		if (StringUtils.isEmpty(newVirtualModelInstanceName) && StringUtils.isNotEmpty(newVirtualModelInstanceTitle)) {
			return JavaUtils.getClassName(newVirtualModelInstanceTitle);
		}
		return newVirtualModelInstanceName;
	}

	public void setNewVirtualModelInstanceName(String newVirtualModelInstanceName) {
		String oldVirtualModelInstanceTitle = getNewVirtualModelInstanceTitle();
		String oldVirtualModelInstanceName = getNewVirtualModelInstanceName();
		this.newVirtualModelInstanceName = newVirtualModelInstanceName;
		getPropertyChangeSupport().firePropertyChange("newVirtualModelInstanceName", oldVirtualModelInstanceName,
				getNewVirtualModelInstanceName());
		getPropertyChangeSupport().firePropertyChange("newVirtualModelInstanceTitle", oldVirtualModelInstanceTitle,
				getNewVirtualModelInstanceTitle());
	}

	public String getNewVirtualModelInstanceTitle() {
		if (newVirtualModelInstanceTitle == null) {
			return getNewVirtualModelInstanceName();
		}
		return newVirtualModelInstanceTitle;
	}

	public void setNewVirtualModelInstanceTitle(String newVirtualModelInstanceTitle) {
		String oldVirtualModelInstanceTitle = getNewVirtualModelInstanceTitle();
		String oldVirtualModelInstanceName = getNewVirtualModelInstanceName();
		this.newVirtualModelInstanceTitle = newVirtualModelInstanceTitle;
		getPropertyChangeSupport().firePropertyChange("newVirtualModelInstanceTitle", oldVirtualModelInstanceTitle,
				getNewVirtualModelInstanceTitle());
		getPropertyChangeSupport().firePropertyChange("newVirtualModelInstanceName", oldVirtualModelInstanceName,
				getNewVirtualModelInstanceName());
	}

	public CreationScheme getCreationScheme() {
		return creationScheme;
	}

	public void setCreationScheme(CreationScheme creationScheme) {
		boolean wasValidable = isActionValidable();
		this.creationScheme = creationScheme;
		if (creationScheme != null) {
			creationSchemeAction = CreationSchemeAction.actionType.makeNewEmbeddedAction(null, null, this);
			creationSchemeAction.setCreationScheme(creationScheme);
			creationSchemeAction.addObserver(this);
			getPropertyChangeSupport().firePropertyChange("creationSchemeAction", null, creationSchemeAction);
		} else {
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
	public void update(FlexoObservable observable, DataModification dataModification) {
		if (dataModification.propertyName().equals(FlexoBehaviourAction.PARAMETER_VALUE_CHANGED)) {
			setChanged();
			notifyObservers(new DataModification("isActionValidable", false, true));
		}
	}

	public CreationSchemeAction getCreationSchemeAction() {
		return creationSchemeAction;
	}

	/**
	 * Return the ViewPoint of the View acting as container of currently created {@link AbstractVirtualModelInstance}.<br>
	 * Note that if we are creating a plain View, container might be null, and this method wil return null
	 * 
	 * @return
	 */
	public ViewPoint getContainerViewpoint() {
		if (getContainerView() != null) {
			return getContainerView().getViewPoint();
		}
		return null;
	}

	/**
	 * Return the View acting as container of currently created {@link AbstractVirtualModelInstance}.<br>
	 * Note that if we are creating a plain View, container might be null
	 * 
	 * @return
	 */
	public abstract View getContainerView();

	public boolean isVisible(VM virtualModel) {
		return true;
	}

	/**
	 * Return project on which this action applies
	 * 
	 * @return
	 */
	public abstract FlexoProject getProject();

	public boolean skipChoosePopup() {
		return skipChoosePopup;
	}

	public void setSkipChoosePopup(boolean skipChoosePopup) {
		this.skipChoosePopup = skipChoosePopup;
	}

	public boolean escapeModelSlotConfiguration() {
		return escapeModelSlotConfiguration;
	}

	public void setEscapeModelSlotConfiguration(boolean escapeModelSlotConfiguration) {
		this.escapeModelSlotConfiguration = escapeModelSlotConfiguration;
	}
}
