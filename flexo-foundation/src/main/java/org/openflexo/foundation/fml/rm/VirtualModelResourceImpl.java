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

package org.openflexo.foundation.fml.rm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.DirectoryBasedIODelegate;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.UseModelSlotDeclaration;
import org.openflexo.model.factory.AccessibleProxyObject;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.IProgress;

public abstract class VirtualModelResourceImpl extends PamelaResourceImpl<VirtualModel, FMLModelFactory>
		implements VirtualModelResource, AccessibleProxyObject {

	static final Logger logger = Logger.getLogger(VirtualModelResourceImpl.class.getPackage().getName());

	@Override
	public void setName(String aName) throws CannotRenameException {
		String oldName = getName();
		super.setName(aName);
		if (getLoadedResourceData() != null && getLoadedResourceData().getPropertyChangeSupport() != null) {
			getLoadedResourceData().getPropertyChangeSupport().firePropertyChange(FlexoConcept.NAME_KEY, oldName, aName);
		}
	}

	@Override
	public FMLTechnologyAdapter getTechnologyAdapter() {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		}
		return null;
	}

	/**
	 * Return virtual model stored by this resource<br>
	 * Load the resource data when unloaded
	 */
	@Override
	public VirtualModel getVirtualModel() {
		try {
			return getResourceData(null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ResourceLoadingCancelledException e) {
			e.printStackTrace();
		} catch (FlexoException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void stopDeserializing() {
		// NPE Protection and warning
		VirtualModel data = getLoadedResourceData();
		if (data != null) {
			data.finalizeDeserialization();
		}
		if (data == null) {
			logger.warning("INVESTIGATE: NO DATA has been derserialized from VirtualModelResource - " + this.getURI());
		}
		else {
			for (FlexoConcept fc : data.getFlexoConcepts()) {
				fc.finalizeDeserialization();
			}
		}
		super.stopDeserializing();
	}

	@Override
	public boolean delete(Object... context) {
		if (super.delete(context)) {
			if (getServiceManager() != null) {
				getServiceManager().getResourceManager().addToFilesToDelete(ResourceLocator.retrieveResourceAsFile(getDirectory()));
			}
			return true;
		}
		return false;
	}

	@Override
	public Resource getDirectory() {
		if (getIODelegate() != null && getIODelegate().getSerializationArtefactAsResource() != null) {
			return getIODelegate().getSerializationArtefactAsResource().getContainer();
		}
		return null;

		/*if (getFlexoIODelegate() instanceof FileFlexoIODelegate) {
			String parentPath = getDirectoryPath();
			if (ResourceLocator.locateResource(parentPath) == null) {
				FileSystemResourceLocatorImpl.appendDirectoryToFileSystemResourceLocator(parentPath);
			}
			return ResourceLocator.locateResource(parentPath);
		}
		else if (getFlexoIODelegate() instanceof InJarFlexoIODelegate) {
			InJarResourceImpl resource = ((InJarFlexoIODelegate) getFlexoIODelegate()).getInJarResource();
			String parentPath = FilenameUtils.getFullPath(resource.getRelativePath());
			BasicResourceImpl parent = ((ClasspathResourceLocatorImpl) (resource.getLocator())).getJarResourcesList().get(parentPath);
			return parent;
		}
		return null;*/
	}

	public String getDirectoryPath() {
		if (getIODelegate() instanceof DirectoryBasedIODelegate) {
			return ((DirectoryBasedIODelegate) getIODelegate()).getDirectory().getAbsolutePath();
		}
		else if (getIODelegate() instanceof FileIODelegate) {
			return ((FileIODelegate) getIODelegate()).getFile().getParentFile().getAbsolutePath();
		}
		return null;
	}

	/**
	 * Activate all required technologies, while exploring declared model slots
	 */
	protected void activateRequiredTechnologies() {
		if (getLoadedResourceData() != null) {
			TechnologyAdapterService taService = getServiceManager().getTechnologyAdapterService();
			// FD unused FlexoTask activateFMLRT =
			taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class));
			/*if (activateFMLRT != null) {
				getServiceManager().getTaskManager().waitTask(activateFMLRT);
			}*/

			// System.out.println("Activate technologies for " + getLoadedResourceData());

			for (UseModelSlotDeclaration useDeclaration : getLoadedResourceData().getUseDeclarations()) {
				FlexoTask activateTA = taService
						.activateTechnologyAdapter(taService.getTechnologyAdapterForModelSlot(useDeclaration.getModelSlotClass()));
				/*if (activateTA != null) {
				getServiceManager().getTaskManager().waitTask(activateTA);
				}*/
			}

			/*for (ModelSlot<?> ms : getLoadedResourceData().getModelSlots()) {
				// System.out.println("Activate " + ms.getModelSlotTechnologyAdapter());
				FlexoTask activateTA = taService.activateTechnologyAdapter(ms.getModelSlotTechnologyAdapter());
				if (activateTA != null) {
					getServiceManager().getTaskManager().waitTask(activateTA);
				}
			}*/
		}
	}

	// DEBUT de VirtualModelResource

	/**
	 * Return virtual model stored by this resource when loaded<br>
	 * Do not force the resource data to be loaded
	 */
	@Override
	public VirtualModel getLoadedVirtualModel() {
		if (isLoaded()) {
			return getVirtualModel();
		}
		return null;
	}

	@Override
	public Class<VirtualModel> getResourceDataClass() {
		return VirtualModel.class;
	}

	/**
	 * Load the &quot;real&quot; load resource data of this resource.
	 * 
	 * @param progress
	 *            a progress monitor in case the resource data is not immediately available.
	 * @return the resource data.
	 * @throws ResourceLoadingCancelledException
	 * @throws ResourceDependencyLoopException
	 * @throws FileNotFoundException
	 */
	@Override
	public VirtualModel loadResourceData(IProgress progress) throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
			InconsistentDataException, InvalidModelDefinitionException {

		logger.info("*************** Loading " + this);

		VirtualModel returned = super.loadResourceData(progress);
		// We notify a deserialization start on ViewPoint AND VirtualModel, to avoid addToVirtualModel() and setViewPoint() to notify
		// UndoManager
		boolean containerWasDeserializing = getContainer() != null ? getContainer().isDeserializing() : true;
		if (!containerWasDeserializing) {
			getContainer().startDeserializing();
		}
		startDeserializing();
		if (getContainer() != null) {
			VirtualModel virtualModel = getContainer().getVirtualModel();
			if (virtualModel != null)
				virtualModel.addToVirtualModels(returned);
		}
		returned.clearIsModified();
		// And, we notify a deserialization stop
		stopDeserializing();
		if (!containerWasDeserializing) {
			getContainer().stopDeserializing();
		}

		// Now we have to activate all required technologies
		activateRequiredTechnologies();

		return returned;
	}

	@Override
	public void notifyResourceLoaded() {
		super.notifyResourceLoaded();
		getPropertyChangeSupport().firePropertyChange("virtualModel", null, getLoadedResourceData());
		getPropertyChangeSupport().firePropertyChange("loadedVirtualModel", null, getLoadedResourceData());
	}

	@Override
	public VirtualModelResource getContainer() {
		return (VirtualModelResource) performSuperGetter(CONTAINER);
	}

	// FIN de VirtualModelResource

	// DEBUT de ViewPointResource

	@Override
	public VirtualModelResource getVirtualModelResource(String virtualModelNameOrURI) {
		for (VirtualModelResource vmRes : getContainedVirtualModelResources()) {
			if (vmRes.getName().equals(virtualModelNameOrURI) || vmRes.getURI().equals(virtualModelNameOrURI)) {
				return vmRes;
			}
		}
		return null;
	}

	@Override
	public List<VirtualModelResource> getContainedVirtualModelResources() {
		return getContents(VirtualModelResource.class);
	}

	@Override
	public VirtualModelLibrary getVirtualModelLibrary() {
		VirtualModelLibrary returned = (VirtualModelLibrary) performSuperGetter(VIRTUAL_MODEL_LIBRARY);
		if (returned == null && getServiceManager() != null) {
			return getServiceManager().getVirtualModelLibrary();
		}
		return returned;
	}

	/*@Override
	public void gitSave() {
	
	}*/

	@Override
	public void addToContents(FlexoResource<?> resource) {
		performSuperAdder(CONTENTS, resource);
		notifyContentsAdded(resource);
		/*if (resource instanceof VirtualModelResource) {
			System.out.println("getViewPoint()=" + getViewPoint());
			getViewPoint().addToVirtualModels(((VirtualModelResource) resource).getVirtualModel());
		}*/
	}

	@Override
	public void removeFromContents(FlexoResource<?> resource) {
		performSuperRemover(CONTENTS, resource);
		notifyContentsRemoved(resource);
		/*if (resource instanceof VirtualModelResource) {
			getViewPoint().removeFromVirtualModels(((VirtualModelResource) resource).getVirtualModel());
		}*/
	}

	// FIN de ViewPointResource

	@Override
	protected void _saveResourceData(boolean clearIsModified) throws SaveResourceException {
		super._saveResourceData(clearIsModified);
		// Hook to write FML as well
		if (getIODelegate() instanceof DirectoryBasedIODelegate) {
			DirectoryBasedIODelegate ioDelegate = (DirectoryBasedIODelegate) getIODelegate();
			File fmlFile = new File(ioDelegate.getDirectory(), ioDelegate.getDirectory().getName());
			try {
				FileUtils.saveToFile(fmlFile, getLoadedResourceData().getFMLRepresentation());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
