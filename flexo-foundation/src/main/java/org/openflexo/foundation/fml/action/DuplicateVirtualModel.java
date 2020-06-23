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

package org.openflexo.foundation.fml.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;

public class DuplicateVirtualModel extends FlexoAction<DuplicateVirtualModel, VirtualModel, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(DuplicateVirtualModel.class.getPackage().getName());

	public static FlexoActionFactory<DuplicateVirtualModel, VirtualModel, FMLObject> actionType = new FlexoActionFactory<DuplicateVirtualModel, VirtualModel, FMLObject>(
			"duplicate", FlexoActionFactory.refactorMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DuplicateVirtualModel makeNewAction(VirtualModel focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new DuplicateVirtualModel(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(VirtualModel object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(VirtualModel object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(DuplicateVirtualModel.actionType, VirtualModel.class);
	}

	private String newVirtualModelName;
	private String newVirtualModelURI;
	private String newVirtualModelDescription;

	DuplicateVirtualModel(VirtualModel focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		newVirtualModelName = focusedObject.getName();
		newVirtualModelDescription = focusedObject.getDescription();
		if (!focusedObject.getResource().computeDefaultURI().equals(focusedObject.getURI())) {
			newVirtualModelURI = focusedObject.getURI();
		}
	}

	private VirtualModel duplicate;
	private RepositoryFolder<VirtualModelResource, ?> targetFolder;
	private VirtualModelResource targetContainer;

	public RepositoryFolder<VirtualModelResource, ?> getTargetFolder() {
		return targetFolder;
	}

	public void setTargetFolder(RepositoryFolder<VirtualModelResource, ?> targetFolder) {
		if ((targetFolder == null && this.targetFolder != null) || (targetFolder != null && !targetFolder.equals(this.targetFolder))) {
			RepositoryFolder<VirtualModelResource, ?> oldValue = this.targetFolder;
			this.targetFolder = targetFolder;
			getPropertyChangeSupport().firePropertyChange("targetFolder", oldValue, targetFolder);
		}
	}

	public VirtualModelResource getTargetContainer() {
		return targetContainer;
	}

	public void setTargetContainer(VirtualModelResource targetContainer) {
		if ((targetContainer == null && this.targetContainer != null)
				|| (targetContainer != null && !targetContainer.equals(this.targetContainer))) {
			VirtualModelResource oldValue = this.targetContainer;
			this.targetContainer = targetContainer;
			getPropertyChangeSupport().firePropertyChange("targetContainer", oldValue, targetContainer);
		}
	}

	@Override
	protected void doAction(Object context) {

		// System.out.println("Duplicate " + getFocusedObject());

		getFocusedObject().loadContainedVirtualModelsWhenUnloaded();

		if (getFocusedObject().getContainerVirtualModel() == null) {
			RepositoryFolder currentFolder = getFocusedObject().getResource().getResourceCenter()
					.getRepositoryFolder(getFocusedObject().getResource());
			duplicate = duplicateVirtualModel(getFocusedObject(), getTargetFolder() != null ? getTargetFolder() : currentFolder,
					getNewVirtualModelName(), getNewVirtualModelURI(), getNewVirtualModelDescription());
		}
		else {
			duplicate = duplicateVirtualModel(getFocusedObject(),
					getTargetContainer() != null ? getTargetContainer()
							: (VirtualModelResource) getFocusedObject().getContainerVirtualModel().getResource(),
					getNewVirtualModelName(), getNewVirtualModelDescription());
		}

	}

	private VirtualModel duplicateVirtualModel(VirtualModel source, RepositoryFolder folder, String newName, String newURI,
			String newDescription) {

		// System.out.println("Duplicate top-level VM " + source);

		VirtualModel returned = null;

		VirtualModelResourceFactory resourceFactory = getFMLTechnologyAdapter().getVirtualModelResourceFactory();
		try {
			VirtualModelResource virtualModelResource = null;
			// This is a top-level VM
			virtualModelResource = resourceFactory.makeTopLevelVirtualModelResource(newName, null, folder, false);
			returned = (VirtualModel) source.cloneObject();
			System.out.println(source.getFMLModelFactory().stringRepresentation(returned));
			// Take care to disconnect the resource !
			returned.setResource(null);
			returned.setName(newName);
			returned.setURI(newURI);
			returned.setDescription(newDescription);
			virtualModelResource.setResourceData(returned);
			returned.setResource(virtualModelResource);
			virtualModelResource.save();

			for (VirtualModel virtualModel : source.getVirtualModels()) {
				duplicateVirtualModel(virtualModel, virtualModelResource, virtualModel.getName(), virtualModel.getDescription());
			}

		} catch (SaveResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModelDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returned;
	}

	private VirtualModel duplicateVirtualModel(VirtualModel source, VirtualModelResource containerResource, String newName,
			String newDescription) {

		// System.out.println("Duplicate contained VM " + source);

		VirtualModel returned = null;

		VirtualModelResourceFactory resourceFactory = getFMLTechnologyAdapter().getVirtualModelResourceFactory();
		try {
			VirtualModelResource virtualModelResource = null;
			virtualModelResource = resourceFactory.makeContainedVirtualModelResource(newName, containerResource, false);
			returned = (VirtualModel) source.cloneObject();
			System.out.println(source.getFMLModelFactory().stringRepresentation(returned));
			// Take care to disconnect the resource !
			returned.setResource(null);
			returned.setName(newName);
			returned.setDescription(newDescription);
			virtualModelResource.setResourceData(returned);
			returned.setResource(virtualModelResource);
			virtualModelResource.save();

			for (VirtualModel virtualModel : source.getVirtualModels()) {
				duplicateVirtualModel(virtualModel, virtualModelResource, virtualModel.getName(), virtualModel.getDescription());
			}

		} catch (SaveResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModelDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returned;
	}

	public VirtualModel getDuplicate() {
		return duplicate;
	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	public FMLTechnologyAdapter getFMLTechnologyAdapter() {
		return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
	}

	public String getNewVirtualModelName() {
		return newVirtualModelName;
	}

	public void setNewVirtualModelName(String newViewPointName) {
		this.newVirtualModelName = newViewPointName;
		getPropertyChangeSupport().firePropertyChange("newVirtualModelName", null, newViewPointName);
		getPropertyChangeSupport().firePropertyChange("newVirtualModelURI", null, getNewVirtualModelURI());
	}

	public String getNewVirtualModelDescription() {
		return newVirtualModelDescription;
	}

	public void setNewVirtualModelDescription(String newVirtualModelDescription) {
		this.newVirtualModelDescription = newVirtualModelDescription;
		getPropertyChangeSupport().firePropertyChange("newVirtualModelDescription", null, newVirtualModelDescription);
	}

	private String getBaseName() {
		return JavaUtils.getClassName(getNewVirtualModelName());
	}

	public String getNewVirtualModelURI() {
		if (newVirtualModelURI == null) {
			String baseURI;
			if (getFocusedObject().getContainerVirtualModel() != null) {
				VirtualModel containerVirtualModel = (getTargetContainer() != null ? getTargetContainer().getVirtualModel()
						: getFocusedObject().getContainerVirtualModel());
				// baseURI = getFocusedObject().getOwningVirtualModel().getURI();fds
				baseURI = containerVirtualModel.getURI();
			}
			else {
				RepositoryFolder currentFolder = getFocusedObject().getResource().getResourceCenter()
						.getRepositoryFolder(getFocusedObject().getResource());
				RepositoryFolder folder = (getTargetFolder() != null ? getTargetFolder() : currentFolder);
				baseURI = folder.getDefaultBaseURI();
			}
			if (!baseURI.endsWith("/")) {
				baseURI = baseURI + "/";
			}
			return baseURI + getBaseName() + VirtualModelResourceFactory.FML_SUFFIX;
		}

		return newVirtualModelURI;
	}

	public void setNewVirtualModelURI(String newVirtualModelURI) {
		this.newVirtualModelURI = newVirtualModelURI;
		getPropertyChangeSupport().firePropertyChange("newVirtualModelURI", null, newVirtualModelURI);

	}

	public VirtualModelLibrary getVirtualModelLibrary() {
		return getServiceManager().getVirtualModelLibrary();
	}

	public boolean isNewVirtualModelNameValid() {
		if (StringUtils.isEmpty(getNewVirtualModelName())) {
			//System.out.println("Empty name: " + getNewVirtualModelName());
			return false;
		}

		if (getFocusedObject().getContainerVirtualModel() == null) {
			RepositoryFolder currentFolder = getFocusedObject().getResource().getResourceCenter()
					.getRepositoryFolder(getFocusedObject().getResource());
			RepositoryFolder folder = (getTargetFolder() != null ? getTargetFolder() : currentFolder);
			if (folder.getResourceWithName(getNewVirtualModelName()) != null) {
				return false;
			}
		}
		else {
			VirtualModel containerVirtualModel = (getTargetContainer() != null ? getTargetContainer().getVirtualModel()
					: getFocusedObject().getContainerVirtualModel());
			if (containerVirtualModel.getVirtualModelNamed(getNewVirtualModelName()) != null) {
				//System.out.println("Existing resource : " + getNewVirtualModelName() + " in container " + containerVirtualModel);
				return false;
			}
		}

		return true;
	}

	public boolean isNewVirtualModelURIValid() {
		if (StringUtils.isEmpty(getNewVirtualModelURI())) {
			return false;
		}
		try {
			new URL(getNewVirtualModelURI());
		} catch (MalformedURLException e) {
			return false;
		}
		if (getVirtualModelLibrary() == null) {
			return false;
		}
		if (getVirtualModelLibrary().getVirtualModelResource(getNewVirtualModelURI()) != null) {
			return false;
		}

		return true;
	}

	@Override
	public boolean isValid() {
		if (!isNewVirtualModelNameValid()) {
			return false;
		}
		if (!isNewVirtualModelURIValid()) {
			//System.out.println("URI not valid: " + getNewVirtualModelURI());
			return false;
		}
		return true;
	}

}
