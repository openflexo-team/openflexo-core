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
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoObserver;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * Abstract base implementation for an action which aims at creating a new {@link VirtualModelInstance}
 * 
 * @author sylvain
 * 
 * @param <A>
 *            type of action, required to manage introspection for inheritance
 * @param <T>
 *            type of container of newly created FMLRTVirtualModelInstance
 */
public abstract class AbstractCreateVirtualModelInstance<A extends AbstractCreateVirtualModelInstance<A, T, VMI, TA>, T extends FlexoObject, VMI extends VirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter<TA>>
		extends FlexoAction<A, T, FlexoObject> implements FlexoObserver {

	private static final Logger logger = Logger.getLogger(AbstractCreateVirtualModelInstance.class.getPackage().getName());

	private VMI newVirtualModelInstance;

	private String newVirtualModelInstanceName;
	private String newVirtualModelInstanceTitle;
	private VirtualModel virtualModel;
	private CreationScheme creationScheme;
	private CreationSchemeAction creationSchemeAction;

	private boolean skipChoosePopup = false;

	private boolean openAfterCreation = true;

	protected AbstractCreateVirtualModelInstance(FlexoActionFactory<A, T, FlexoObject> actionType, T focusedObject,
			Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws FlexoException {
		logger.info("Add virtual model instance in view " + getFocusedObject() + " creationSchemeAction=" + creationSchemeAction);

		// System.out.println("VirtualModelBeeing created: " + getVirtualModel());
		/*if (creationSchemeAction != null && creationSchemeAction.getCreationScheme() != null) {
			System.out.println("FML: " + creationSchemeAction.getCreationScheme().getFMLRepresentation());
		}*/

		// System.out.println("getNewVirtualModelInstanceName()=" + getNewVirtualModelInstanceName());
		// System.out.println("getNewVirtualModelInstanceTitle()=" + getNewVirtualModelInstanceTitle());

		if (StringUtils.isEmpty(getNewVirtualModelInstanceName())) {
			throw new InvalidParameterException("virtual model instance name is undefined");
		}

		/*int index = 1;
		String baseName = getNewVirtualModelInstanceName();
		while (!isValidVirtualModelInstanceName(getNewVirtualModelInstanceName())) {
			newVirtualModelInstanceName = baseName + index;
			index++;
		}*/

		AbstractVirtualModelInstanceResource<VMI, TA> newVirtualModelInstanceResource = makeVirtualModelInstanceResource();

		newVirtualModelInstance = newVirtualModelInstanceResource.getVirtualModelInstance();

		logger.info("Added virtual model instance " + newVirtualModelInstance + " in container " + getFocusedObject());

		// System.out.println("OK, we have created the file " + newVirtualModelInstanceResource.getFile().getAbsolutePath());
		// System.out.println("OK, we have created the VirtualModelInstanceResource " + newVirtualModelInstanceResource.getURI() + "
		// delegate="
		// + newVirtualModelInstanceResource.getIODelegate().stringRepresentation());

		// System.out.println("creationSchemeAction=" + creationSchemeAction);

		// We init the new VMI using a creation scheme
		if (creationSchemeAction != null) {

			// System.out.println("We now execute " + creationSchemeAction);
			// System.out.println("FML=" + creationSchemeAction.getCreationScheme().getFMLRepresentation());

			creationSchemeAction.initWithFlexoConceptInstance(newVirtualModelInstance);
			creationSchemeAction.setFocusedObject(newVirtualModelInstance);
			creationSchemeAction.doAction();
			if (creationSchemeAction.getThrownException() != null) {
				throw creationSchemeAction.getThrownException();
			}

		}

		// We add the FMLRTVirtualModelInstance to the view
		if (getContainerVirtualModelInstance() != null) {
			getContainerVirtualModelInstance().addToVirtualModelInstances(newVirtualModelInstance);
		}

		// System.out.println("Now, we try to synchronize the new virtual model instance");

		if (newVirtualModelInstance.isSynchronizable()) {
			// System.out.println("Go for it");
			newVirtualModelInstance.synchronize(getEditor());
		}

		// System.out.println("Saving file again...");
		newVirtualModelInstanceResource.save();
	}

	public int getStepsNumber() {
		if (virtualModel == null) {
			return 1;
		}
		else if (!getVirtualModel().hasCreationScheme()) {
			return virtualModel.getModelSlots().size() + 1;
		}
		else {
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

	public VirtualModel getVirtualModel() {
		return virtualModel;
	}

	public void setVirtualModel(VirtualModel virtualModel) {
		if (virtualModel != this.virtualModel) {
			this.virtualModel = virtualModel;
			setChanged();
			notifyObservers(new DataModification<>("isActionValidable", false, true));
		}
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
			creationSchemeAction = new CreationSchemeAction(creationScheme,
					getFocusedObject() instanceof FMLRTVirtualModelInstance ? (VirtualModelInstance<?, ?>) getFocusedObject() : null, null,
					this);
			/*creationSchemeAction = CreationSchemeAction.actionType.makeNewEmbeddedAction(
					getFocusedObject() instanceof FMLRTVirtualModelInstance ? (VirtualModelInstance<?, ?>) getFocusedObject() : null, null,
					this);
			creationSchemeAction.setCreationScheme(creationScheme);*/
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

	/**
	 * Return the VirtualModel of the container of currently created {@link FMLRTVirtualModelInstance}.<br>
	 * Note that if we are creating a top-level FMLRTVirtualModelInstance, container might be null, and this method will return null
	 * 
	 * @return
	 */
	public VirtualModel getContainerVirtualModel() {
		if (getContainerVirtualModelInstance() != null) {
			return getContainerVirtualModelInstance().getVirtualModel();
		}
		return null;
	}

	/**
	 * Return the VirtualModel resource of the container of currently created {@link FMLRTVirtualModelInstance}.<br>
	 * Note that if we are creating a top-level FMLRTVirtualModelInstance, container might be null, and this method will return null
	 * 
	 * @return
	 */
	public CompilationUnitResource getContainerVirtualModelResource() {
		if (getContainerVirtualModel() != null) {
			return getContainerVirtualModel().getResource();
		}
		return null;
	}

	/**
	 * Return the {@link VirtualModelInstance} acting as container of currently created {@link VirtualModelInstance}.<br>
	 * 
	 * Note that if we are creating a plain FMLRTVirtualModelInstance (in a folder for example), container might be null
	 * 
	 * @return
	 */
	public VirtualModelInstance<?, ?> getContainerVirtualModelInstance() {
		if (getFocusedObject() instanceof VirtualModelInstance) {
			return (VirtualModelInstance<?, ?>) getFocusedObject();
		}
		return null;
	}

	/**
	 * Return the folder in which the new {@link VirtualModelInstance} is to be created
	 * 
	 * @return
	 */
	public <I> RepositoryFolder<? extends AbstractVirtualModelInstanceResource<VMI, TA>, I> getFolder() {
		if (getFocusedObject() instanceof RepositoryFolder) {
			return (RepositoryFolder<AbstractVirtualModelInstanceResource<VMI, TA>, I>) getFocusedObject();
		}
		return null;
	}

	/*public boolean isVisible(VirtualModel virtualModel) {
		return true;
	}*/

	/**
	 * Return resource center on which this action applies
	 * 
	 * @return
	 */
	public FlexoResourceCenter<?> getResourceCenter() {
		if (getContainerVirtualModelInstance() != null) {
			return getContainerVirtualModelInstance().getResource().getResourceCenter();
		}
		if (getFolder() != null) {
			return getFolder().getResourceRepository().getResourceCenter();
		}
		return null;
	}

	public boolean skipChoosePopup() {
		return skipChoosePopup;
	}

	public void setSkipChoosePopup(boolean skipChoosePopup) {
		this.skipChoosePopup = skipChoosePopup;
	}

	public boolean openAfterCreation() {
		return openAfterCreation;
	}

	public void setOpenAfterCreation(boolean openAfterCreation) {
		if (openAfterCreation != this.openAfterCreation) {
			this.openAfterCreation = openAfterCreation;
			getPropertyChangeSupport().firePropertyChange("openAfterCreation", !openAfterCreation, openAfterCreation);
		}
	}

	/**
	 * Return boolean indicating if proposed name is a valid as name for the new FMLRTVirtualModelInstance
	 * 
	 * @param proposedName
	 * @return
	 */
	public boolean isValidVirtualModelInstanceName(String proposedName) {
		if (getContainerVirtualModelInstance() != null) {
			return getContainerVirtualModelInstance().isValidVirtualModelInstanceName(proposedName);
		}
		if (getFolder() != null) {
			return getFolder().getResourceWithName(proposedName) == null;
		}

		return false;
	}

	/**
	 * Effective build of the resource to be created
	 * 
	 * @return
	 * @throws SaveResourceException
	 */
	public abstract AbstractVirtualModelInstanceResource<VMI, TA> makeVirtualModelInstanceResource() throws SaveResourceException;

}
