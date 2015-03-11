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
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoObserver;
import org.openflexo.foundation.InvalidArgumentException;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance.VirtualModelInstanceImpl;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.resource.InvalidFileNameException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * This action is called to create a new {@link VirtualModelInstance} in a {@link View}
 * 
 * @author sylvain
 * 
 * @param <A>
 *            type of action, required to manage introspection for inheritance
 */
public abstract class CreateVirtualModelInstance<A extends CreateVirtualModelInstance<A>> extends FlexoAction<A, View, FlexoObject>
		implements FlexoObserver {

	private static final Logger logger = Logger.getLogger(CreateVirtualModelInstance.class.getPackage().getName());

	private VirtualModelInstance newVirtualModelInstance;

	private String newVirtualModelInstanceName;
	private String newVirtualModelInstanceTitle;
	private VirtualModel virtualModel;
	private CreationScheme creationScheme;

	public boolean skipChoosePopup = false;

	protected CreateVirtualModelInstance(FlexoActionType<A, View, FlexoObject> actionType, View focusedObject,
			Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		modelSlotConfigurations = new Hashtable<ModelSlot<?>, ModelSlotInstanceConfiguration<?, ?>>();
	}

	public VirtualModelInstanceResource makeVirtualModelInstanceResource() throws SaveResourceException {
		return VirtualModelInstanceImpl.newVirtualModelInstance(getNewVirtualModelInstanceName(), getNewVirtualModelInstanceTitle(),
				getVirtualModel(), getFocusedObject());
	}

	@Override
	protected void doAction(Object context) throws InvalidFileNameException, SaveResourceException, InvalidArgumentException {
		logger.info("Add virtual model instance in view " + getFocusedObject() + " creationSchemeAction=" + creationSchemeAction);

		newVirtualModelInstanceName = JavaUtils.getClassName(newVirtualModelInstanceName);

		if (StringUtils.isNotEmpty(newVirtualModelInstanceName) && StringUtils.isEmpty(newVirtualModelInstanceTitle)) {
			newVirtualModelInstanceTitle = newVirtualModelInstanceName;
		}

		if (StringUtils.isEmpty(newVirtualModelInstanceName)) {
			throw new InvalidParameterException("virtual model instance name is undefined");
		}

		int index = 1;
		String baseName = newVirtualModelInstanceName;
		while (!getFocusedObject().isValidVirtualModelName(newVirtualModelInstanceName)) {
			newVirtualModelInstanceName = baseName + index;
			index++;
		}

		VirtualModelInstanceResource newVirtualModelInstanceResource = makeVirtualModelInstanceResource();

		newVirtualModelInstance = newVirtualModelInstanceResource.getVirtualModelInstance();

		logger.info("Added virtual model instance " + newVirtualModelInstance + " in view " + getFocusedObject());

		// System.out.println("OK, we have created the file " + newVirtualModelInstanceResource.getFile().getAbsolutePath());
		System.out.println("OK, we have created the VirtualModelInstanceResource " + newVirtualModelInstanceResource.getURI()
				+ " delegate=" + newVirtualModelInstanceResource.getFlexoIODelegate().stringRepresentation());

		for (ModelSlot ms : virtualModel.getModelSlots()) {
			// System.out.println("*** ModelSlot: " + ms);
			ModelSlotInstanceConfiguration<?, ?> configuration = getModelSlotInstanceConfiguration(ms);
			if (configuration.isValidConfiguration()) {
				ModelSlotInstance msi = configuration.createModelSlotInstance(newVirtualModelInstance, getFocusedObject());
				msi.setVirtualModelInstance(newVirtualModelInstance);
				newVirtualModelInstance.addToModelSlotInstances(msi);
			} else {
				throw new InvalidArgumentException("Wrong configuration for model slot " + configuration.getModelSlot() + " configuration="
						+ configuration);
			}
		}

		if (creationSchemeAction != null) {
			creationSchemeAction.initWithFlexoConceptInstance(newVirtualModelInstance);
			creationSchemeAction.setFocusedObject(newVirtualModelInstance);
			creationSchemeAction.doAction();
		}

		// We add the VirtualModelInstance to the view
		getFocusedObject().addToVirtualModelInstances(newVirtualModelInstance);

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
		if (virtualModel == null) {
			return false;
		}
		if (StringUtils.isEmpty(getNewVirtualModelInstanceName())) {
			return false;
		}

		if (StringUtils.isEmpty(getNewVirtualModelInstanceTitle())) {
			return false;
		}
		if (getFocusedObject().getVirtualModelInstance(newVirtualModelInstanceName) != null) {
			return false;
		}
		return true;
	}

	public VirtualModelInstance getNewVirtualModelInstance() {
		return newVirtualModelInstance;
	}

	private final Hashtable<ModelSlot<?>, ModelSlotInstanceConfiguration<?, ?>> modelSlotConfigurations;

	public VirtualModel getVirtualModel() {
		return virtualModel;
	}

	public void setVirtualModel(VirtualModel virtualModel) {
		if (virtualModel != this.virtualModel) {
			this.virtualModel = virtualModel;
			modelSlotConfigurations.clear();
			if (this.virtualModel != null) {
				for (ModelSlot<?> ms : this.virtualModel.getModelSlots()) {
					modelSlotConfigurations.put(ms, ms.createConfiguration(this));
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
		return newVirtualModelInstanceName;
	}

	public void setNewVirtualModelInstanceName(String newVirtualModelInstanceName) {
		this.newVirtualModelInstanceName = newVirtualModelInstanceName;
	}

	public String getNewVirtualModelInstanceTitle() {
		if (newVirtualModelInstanceTitle == null) {
			return getNewVirtualModelInstanceName();
		}
		return newVirtualModelInstanceTitle;
	}

	public void setNewVirtualModelInstanceTitle(String newVirtualModelInstanceTitle) {
		this.newVirtualModelInstanceTitle = newVirtualModelInstanceTitle;
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

	@Override
	public void update(FlexoObservable observable, DataModification dataModification) {
		if (dataModification.propertyName().equals(FlexoBehaviourAction.PARAMETER_VALUE_CHANGED)) {
			setChanged();
			notifyObservers(new DataModification("isActionValidable", false, true));
		}
	}

	private CreationSchemeAction creationSchemeAction;

	public CreationSchemeAction getCreationSchemeAction() {
		return creationSchemeAction;
	}

	public ViewPoint getViewpoint() {
		return getFocusedObject().getViewPoint();
	}

	public boolean isVisible(VirtualModel virtualModel) {
		return true;
	}

	public View getView() {
		return getFocusedObject();
	}
}
