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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject.BindingIsRequiredAndMustBeValid.InvalidRequiredBindingIssue;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FMLValidationModel;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.DirectoryBasedIODelegate;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.FileIODelegate.FileHasBeenWrittenOnDiskNotification;
import org.openflexo.foundation.resource.FileIODelegate.WillWriteFileOnDiskNotification;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.resource.SaveResourcePermissionDeniedException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FileSystemMetaDataManager;
import org.openflexo.toolbox.FileUtils;

public abstract class VirtualModelResourceImpl extends PamelaResourceImpl<VirtualModel, FMLModelFactory> implements VirtualModelResource {

	private static final Logger logger = Logger.getLogger(VirtualModelResourceImpl.class.getPackage().getName());

	@Override
	public String computeDefaultURI() {
		String returned = super.computeDefaultURI();
		if (!returned.endsWith(VirtualModelResourceFactory.FML_SUFFIX)) {
			return returned + VirtualModelResourceFactory.FML_SUFFIX;
		}
		return returned;
	}

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
			return getResourceData();
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
		logger.info("activateRequiredTechnologies() for " + this + " used: " + getUsedModelSlots());

		TechnologyAdapterService taService = getServiceManager().getTechnologyAdapterService();
		List<TechnologyAdapter<?>> requiredTAList = new ArrayList<>();
		requiredTAList.add(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class));
		for (Class<? extends ModelSlot<?>> msClass : getUsedModelSlots()) {
			TechnologyAdapter<?> requiredTA = taService.getTechnologyAdapterForModelSlot(msClass);
			if (!requiredTAList.contains(requiredTA)) {
				requiredTAList.add(requiredTA);
			}
		}
		for (TechnologyAdapter requiredTA : requiredTAList) {
			logger.info("Activating " + requiredTA);
			taService.activateTechnologyAdapter(requiredTA, true);
		}
	}

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
	public Class<? extends VirtualModel> getResourceDataClass() {
		if (getSpecializedResourceDataClass() != null) {
			return getSpecializedResourceDataClass();
		}
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
	public VirtualModel loadResourceData() throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
			InconsistentDataException, InvalidModelDefinitionException {

		logger.info("*************** Loading " + this);

		// Now we have to activate all required technologies
		activateRequiredTechnologies();

		VirtualModel returned = super.loadResourceData();
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

		if (needsConversion() || (getContainer() != null && getContainer().needsConversion())) {
			logger.info("Converting " + this);
			FMLValidationModel validationModel = getServiceManager().getVirtualModelLibrary().getFMLValidationModel();
			try {
				ValidationReport validationReport = validationModel.validate(returned);
				for (ValidationIssue<?, ?> issue : validationReport.getAllIssues()) {
					if (issue instanceof InvalidRequiredBindingIssue) {
						InvalidRequiredBindingIssue<?> invalidBinding = (InvalidRequiredBindingIssue<?>) issue;
						if (invalidBinding.getFixProposals().size() > 0) {
							invalidBinding.getFixProposals().get(0).apply(false);
						}
					}
				}
				saveResourceData(true);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (SaveResourcePermissionDeniedException e) {
				e.printStackTrace();
			} catch (SaveResourceException e) {
				e.printStackTrace();
			}

		}

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

	@Override
	public void addToContents(FlexoResource<?> resource) {
		performSuperAdder(CONTENTS, resource);
		notifyContentsAdded(resource);
	}

	@Override
	public void removeFromContents(FlexoResource<?> resource) {
		performSuperRemover(CONTENTS, resource);
		notifyContentsRemoved(resource);
	}

	@Override
	protected void _saveResourceData(boolean clearIsModified) throws SaveResourceException {
		super._saveResourceData(clearIsModified);
		// Hook to write FML as well
		if (getIODelegate() instanceof DirectoryBasedIODelegate) {
			DirectoryBasedIODelegate ioDelegate = (DirectoryBasedIODelegate) getIODelegate();
			File fmlFile = new File(ioDelegate.getDirectory(), ioDelegate.getDirectory().getName());
			if (!fmlFile.isDirectory()) {
				try {
					// Warn directory watcher about .fml file to be saved
					getServiceManager().notify(null, new WillWriteFileOnDiskNotification(fmlFile));
					FileUtils.saveToFile(fmlFile, getLoadedResourceData().getFMLRepresentation());
					getServiceManager().notify(null, new FileHasBeenWrittenOnDiskNotification(fmlFile));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// Save locales
		if (getLoadedResourceData().getLocalizedDictionary() instanceof LocalizedDelegateImpl) {
			((LocalizedDelegateImpl) getLoadedResourceData().getLocalizedDictionary()).save();
		}
		// Save meta data
		saveMetaData();
	}

	private void saveMetaData() {
		if (getResourceCenter() instanceof FileSystemBasedResourceCenter) {
			FileSystemBasedResourceCenter rc = (FileSystemBasedResourceCenter) getResourceCenter();
			FileSystemMetaDataManager metaDataManager = rc.getMetaDataManager();
			File file = ((File) getIODelegate().getSerializationArtefact());
			metaDataManager.setProperty("uri", getURI(), file, false);
			metaDataManager.setProperty("name", getName(), file, false);
			metaDataManager.setProperty("version", getVersion().toString(), file, false);
			metaDataManager.setProperty("modelVersion", getModelVersion().toString(), file, false);
			metaDataManager.setProperty("requiredModelSlotList", getUsedModelSlotsAsString(), file, false);
			if (getSpecializedResourceDataClass() != null) {
				metaDataManager.setProperty("virtualModelClassName", getSpecializedResourceDataClass().getName(), file, false);
			}
			metaDataManager.saveMetaDataProperties(file);
		}
	}

	private List<Class<? extends ModelSlot<?>>> usedModelSlots = new ArrayList<>();

	/**
	 * Return {@link ModelSlot} classes used in this {@link VirtualModel} resource<br>
	 * Note that this information is extracted from metadata or from reading XML file before effective parsing<br>
	 * This information is used to determine which technology adapters have to be activated before {@link VirtualModel} is loaded
	 * 
	 * @return
	 */
	@Override
	public List<Class<? extends ModelSlot<?>>> getUsedModelSlots() {
		return usedModelSlots;
	}

	@Override
	public String getUsedModelSlotsAsString() {
		boolean isFirst = true;
		StringBuffer sb = new StringBuffer();
		for (Class<? extends ModelSlot<?>> msClass : usedModelSlots) {
			sb.append((isFirst ? "" : ",") + msClass.getName());
			isFirst = false;
		}
		return sb.toString();
	}

	/**
	 * Internally sets UsedModelSlots
	 * 
	 * @param usedModelSlotClasses
	 * @throws ClassNotFoundException
	 */
	protected void setUsedModelSlots(String usedModelSlotClasses) throws ClassNotFoundException {
		usedModelSlots.clear();
		StringTokenizer st = new StringTokenizer(usedModelSlotClasses, ",");
		while (st.hasMoreTokens()) {
			String next = st.nextToken();
			usedModelSlots.add((Class<? extends ModelSlot<?>>) Class.forName(next));
		}
	}

	/**
	 * Rebuild a new {@link FMLModelFactory} using supplied use declarations, and set this new factory as model factory to use for this
	 * resource<br>
	 * This call is required for example when a new technology is required for a {@link VirtualModel}
	 * 
	 * @param useDeclarations
	 */
	@Override
	public void updateFMLModelFactory(List<Class<? extends ModelSlot<?>>> usedModelSlots) {
		this.usedModelSlots = usedModelSlots;
		try {
			FMLModelFactory modelFactory = new FMLModelFactory(this, getServiceManager());
			setFactory(modelFactory);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setSpecializedResourceDataClass(Class<? extends VirtualModel> specializedResourceDataClass) {
		performSuperSetter(SPECIALIZED_VIRTUAL_MODEL_CLASS, specializedResourceDataClass);
		if (getServiceManager() != null) {
			try {
				FMLModelFactory modelFactory = new FMLModelFactory(this, getServiceManager());
				setFactory(modelFactory);
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
		}
		/*else {
			logger.warning("Could not access to ServiceManager");
		}*/
	}

	@Override
	public void setServiceManager(FlexoServiceManager serviceManager) {
		super.setServiceManager(serviceManager);
		if (serviceManager != null) {
			try {
				FMLModelFactory modelFactory = new FMLModelFactory(this, getServiceManager());
				setFactory(modelFactory);
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
		}
	}

}
