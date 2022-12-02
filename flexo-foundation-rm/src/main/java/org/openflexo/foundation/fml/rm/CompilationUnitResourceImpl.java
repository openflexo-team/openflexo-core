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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject.BindingIsRequiredAndMustBeValid.InvalidRequiredBindingIssue;
import org.openflexo.foundation.fml.FMLObject.FMLObjectImpl;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FMLValidationModel;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitParser;
import org.openflexo.foundation.fml.parser.FMLExpressionParser;
import org.openflexo.foundation.fml.parser.ParseException;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.DirectoryBasedIODelegate;
import org.openflexo.foundation.resource.DirectoryBasedJarIODelegate;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.FileWritingLock;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.PamelaResourceWithPotentialCrossReferencesImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.resource.SaveResourcePermissionDeniedException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.rm.BasicResourceImpl.LocatorNotFoundException;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.InJarResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.ResourceLocatorDelegate;
import org.openflexo.toolbox.FileSystemMetaDataManager;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.xml.XMLElementInfo;
import org.openflexo.xml.XMLRootElementInfo;

/**
 * Implementation for {@link CompilationUnitResource}
 * 
 * This resource stores a {@link FMLCompilationUnit} using a {@link FMLCompilationUnitParser} instance
 * 
 * 
 * @author sylvain
 *
 */
public abstract class CompilationUnitResourceImpl
		extends PamelaResourceWithPotentialCrossReferencesImpl<FMLCompilationUnit, FMLModelFactory> implements CompilationUnitResource {

	private static final Logger logger = Logger.getLogger(CompilationUnitResourceImpl.class.getPackage().getName());

	public enum PersistencyStrategy {
		XML, XML2FML, FML
	}

	private static final PersistencyStrategy DEFAULT_PERSISTENCY_STRATEGY = PersistencyStrategy.XML2FML;
	private PersistencyStrategy persistencyStrategy = DEFAULT_PERSISTENCY_STRATEGY;

	public PersistencyStrategy getPersistencyStrategy() {
		return persistencyStrategy;
	}

	public void setPersistencyStrategy(PersistencyStrategy persistencyStrategy) {
		this.persistencyStrategy = persistencyStrategy;
	}

	private final FMLCompilationUnitParser fmlParser;

	public CompilationUnitResourceImpl() {
		fmlParser = new FMLCompilationUnitParser();
	}

	public FMLCompilationUnitParser getFMLParser() {
		return fmlParser;
	}

	@Override
	public Expression parseExpression(String expressionAsString, Bindable bindable) throws ParseException {
		return FMLExpressionParser.parse(expressionAsString, bindable, getCompilationUnit().getTypingSpace(),
				getCompilationUnit().getFMLModelFactory());
	}

	@Override
	public String computeDefaultURI() {
		String returned = super.computeDefaultURI();
		if (!returned.endsWith(CompilationUnitResourceFactory.FML_SUFFIX)) {
			return returned + CompilationUnitResourceFactory.FML_SUFFIX;
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

	/*@Override
	public FMLCompilationUnit getResourceData()
			throws ResourceLoadingCancelledException, ResourceLoadingCancelledException, FileNotFoundException, FlexoException {
		System.out.println("OK on veut charger la resource " + this);
		System.out.println("Infos: " + getVirtualModelInfo(getResourceCenter()));
		System.exit(-1);
		return super.getResourceData();
	}*/

	/**
	 * Return virtual model stored by this resource<br>
	 * Load the resource data when unloaded
	 */
	@Override
	public FMLCompilationUnit getCompilationUnit() {
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
		FMLCompilationUnit data = getLoadedResourceData();
		if (data != null) {
			data.finalizeDeserialization();
		}
		if (data == null) {
			logger.warning("INVESTIGATE: NO DATA has been dereserialized from CompilationUnitResource - " + this.getURI());
		}
		else {
			if (data.getVirtualModel() != null) {
				data.getVirtualModel().finalizeDeserialization();
				for (FlexoConcept fc : data.getVirtualModel().getFlexoConcepts()) {
					fc.finalizeDeserialization();
				}
			}
			else {
				logger.warning("INVESTIGATE: NO VirtualModel in CompilationUnitResource - " + this.getURI());
			}
		}
		super.stopDeserializing();

		if (willConvertFromXMLtoFML) {
			convertFromXMLToFML();
		}
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
	public FMLCompilationUnit getLoadedCompilationUnit() {
		if (isLoaded()) {
			return getCompilationUnit();
		}
		return null;
	}

	@Override
	public Class<FMLCompilationUnit> getResourceDataClass() {
		/*if (getSpecializedResourceDataClass() != null) {
			return getSpecializedResourceDataClass();
		}*/
		return FMLCompilationUnit.class;
	}

	private boolean hasParseErrors = false;

	@Override
	public boolean isLoadable() {
		return super.isLoadable() && !hasParseErrors;
	}

	@Override
	protected boolean doesIODelegateExist() {
		if (getIODelegate() != null && !getIODelegate().exists()) {
			// Special case for XML serialized VMs > no FML
			try {
				if (getXMLArtefactResource() != null && getXMLArtefactResource().exists()) {
					return true;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (LocatorNotFoundException e) {
				e.printStackTrace();
			}
		}

		return super.doesIODelegateExist();
	}

	@Override
	public boolean isLoading() {
		return isLoading;
	}

	@Override
	public void setLoading(boolean isLoading) {
		this.isLoading = isLoading;
	}

	private boolean isLoading;

	@Override
	public void finalizeLoadResourceData() throws ResourceLoadingCancelledException, FileNotFoundException, FlexoException {
		FMLCompilationUnitNode cuNode = (FMLCompilationUnitNode) getLoadedResourceData().getPrettyPrintDelegate();
		cuNode.getSemanticsAnalyzer().finalizeDeserialization();
		notifyResourceLoaded();
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
	public FMLCompilationUnit initializeLoadResourceData() throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
			InconsistentDataException, InvalidModelDefinitionException {

		if (isLoaded()) {
			return resourceData;
		}

		setLoading(true);

		// Now we have to activate all required technologies
		activateRequiredTechnologies();

		startDeserializing();

		notifyResourceWillLoad();

		try {
			resourceData = performLoad();
			resourceData.setResource(this);
			resourceData.clearIsModified();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			hasParseErrors = true;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			setLoading(false);
			// notifyResourceLoaded();
		}

		// We notify a deserialization start on ViewPoint AND VirtualModel, to avoid addToVirtualModel() and setViewPoint() to notify
		// UndoManager
		boolean containerWasDeserializing = getContainer() != null ? getContainer().isDeserializing() : true;
		if (!containerWasDeserializing) {
			getContainer().startDeserializing();
		}
		startDeserializing();
		if (getContainer() != null && getContainer().getCompilationUnit() != null) {
			VirtualModel virtualModel = getContainer().getCompilationUnit().getVirtualModel();
			if (virtualModel != null) {
				// System.out.println("loadResourceData() for " + this);
				// System.out.println(" ----> On met " + resourceData.getVirtualModel() + " dans " + virtualModel);
				// virtualModel.addToVirtualModels(resourceData.getVirtualModel());
			}
		}
		if (resourceData != null) {
			resourceData.clearIsModified();
		}
		// And, we notify a deserialization stop
		stopDeserializing();
		if (!containerWasDeserializing) {
			getContainer().stopDeserializing();
		}

		if (needsConversion() || (getContainer() != null && getContainer().needsConversion())) {
			logger.info("Converting " + this);
			FMLValidationModel validationModel = getServiceManager().getVirtualModelLibrary().getFMLValidationModel();
			try {
				ValidationReport validationReport = validationModel.validate(resourceData);
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

		try {
			// When some contained VMI are declared for this resource, load them now
			for (FMLRTVirtualModelInstanceResource containedVMIResource : getContainedVMI()) {
				containedVMIResource.loadResourceData();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOFlexoException(e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning("Unhandled Exception");
		}

		return resourceData;
	}

	@Override
	public void notifyResourceLoaded() {
		super.notifyResourceLoaded();
		getPropertyChangeSupport().firePropertyChange("compilationUnit", null, getLoadedResourceData());
		getPropertyChangeSupport().firePropertyChange("loadedCompilationUnit", null, getLoadedResourceData());
	}

	@Override
	public CompilationUnitResource getContainer() {
		return (CompilationUnitResource) performSuperGetter(CONTAINER);
	}

	@Override
	public CompilationUnitResource getCompilationUnitResource(String virtualModelNameOrURI) {
		for (CompilationUnitResource vmRes : getContainedVirtualModelResources()) {
			if (vmRes.getName().equals(virtualModelNameOrURI) || vmRes.getURI().equals(virtualModelNameOrURI)) {
				return vmRes;
			}
		}
		return null;
	}

	@Override
	public List<CompilationUnitResource> getContainedVirtualModelResources() {
		return getContents(CompilationUnitResource.class);
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

	/*@Override
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
	}*/

	private void saveMetaData() {
		if (getResourceCenter() instanceof FileSystemBasedResourceCenter) {
			FileSystemBasedResourceCenter rc = (FileSystemBasedResourceCenter) getResourceCenter();
			FileSystemMetaDataManager metaDataManager = rc.getMetaDataManager();
			File file = ((File) getIODelegate().getSerializationArtefact());
			metaDataManager.setProperty("uri", getURI(), file, false);
			metaDataManager.setProperty("name", getName(), file, false);
			metaDataManager.setProperty("version", getVersion().toString(), file, false);
			// metaDataManager.setProperty("modelVersion", getModelVersion().toString(), file, false);
			metaDataManager.setProperty("requiredModelSlotList", getUsedModelSlotsAsString(), file, false);
			if (getVirtualModelClass() != null) {
				metaDataManager.setProperty("virtualModelClassName", getVirtualModelClass().getName(), file, false);
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
	@Override
	@Deprecated
	public void setUsedModelSlots(String usedModelSlotClasses) throws ClassNotFoundException {
		usedModelSlots.clear();
		if (usedModelSlotClasses != null) {
			StringTokenizer st = new StringTokenizer(usedModelSlotClasses, ",");
			while (st.hasMoreTokens()) {
				String next = st.nextToken();
				usedModelSlots.add((Class<? extends ModelSlot<?>>) Class.forName(next));
			}
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
	public FMLModelFactory updateFMLModelFactory(List<Class<? extends ModelSlot<?>>> usedModelSlots) {
		this.usedModelSlots = usedModelSlots;
		try {
			FMLModelFactory modelFactory = new FMLModelFactory(this, getServiceManager());
			setFactory(modelFactory);
			if (fmlParser.getSemanticsAnalyzer() != null) {
				fmlParser.getSemanticsAnalyzer().setModelFactory(modelFactory);
			}
			return modelFactory;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Class<? extends VirtualModel> getVirtualModelClass() {
		Class<? extends VirtualModel> returned = (Class<? extends VirtualModel>) performSuperGetter(VIRTUAL_MODEL_CLASS);
		if (returned == null) {
			return VirtualModel.class;
		}
		return returned;
	}

	@Override
	public void setVirtualModelClass(Class<? extends VirtualModel> specializedResourceDataClass) {
		performSuperSetter(VIRTUAL_MODEL_CLASS, specializedResourceDataClass);
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

	@Override
	public String getRawSource() {
		if (getLoadedResourceData() != null) {
			return getLoadedResourceData().getFMLPrettyPrint();
		}
		return null;
	}

	@Override
	protected FMLCompilationUnit performLoad() throws IOException, ParseException {
		switch (getPersistencyStrategy()) {
			case XML:
				return loadFromXML();
			case XML2FML:
				Resource fmlArtefactResource = getIODelegate().getSerializationArtefactAsResource();
				Resource xmlArtefactResource = null;
				try {
					xmlArtefactResource = getXMLArtefactResource();
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (LocatorNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if (fmlArtefactResource != null && fmlArtefactResource.exists()) {
					if (xmlArtefactResource != null && xmlArtefactResource.exists() && fmlArtefactResource instanceof FileResourceImpl
							&& xmlArtefactResource instanceof FileResourceImpl) {
						// In this case, both resources exist, take the recent one
						FileTime fmlLastModified = Files.getLastModifiedTime(((FileResourceImpl) fmlArtefactResource).getFile().toPath());
						FileTime xmlLastModified = Files.getLastModifiedTime(((FileResourceImpl) xmlArtefactResource).getFile().toPath());
						System.out.println("Dir: " + fmlArtefactResource.getContainer());
						System.out.println("FML: " + fmlArtefactResource + " lastModified: " + fmlLastModified);
						System.out.println("XML: " + xmlArtefactResource + " lastModified: " + xmlLastModified);
						if (xmlLastModified.compareTo(fmlLastModified) >= 0) {
							// Loading using XML file
							System.out.println("Loading as XML " + xmlArtefactResource);
							return loadFromXML();
						}
						else {
							// Loading using FML file
							try {
								System.out.println("Loading as FML " + fmlArtefactResource);
								return loadFromFML();
							} catch (ParseException e) {
								logger.warning("ParseException raised while loading " + fmlArtefactResource);
								logger.warning("Try to load using XML version: " + xmlArtefactResource);
								return loadFromXML();
							}
						}
					}
					else {
						return loadFromFML();
					}
				}
				if (xmlArtefactResource != null && xmlArtefactResource.exists()) {
					return loadFromXML();
				}
				logger.warning("Cannot load " + this);
				return null;

			case FML:
				return loadFromFML();

			default:
				return null;
		}

	}

	/**
	 * Resource saving safe implementation<br>
	 * Initial resource is first copied, then we write in a temporary file, renamed at the end when the seriaization has been successfully
	 * performed
	 */
	@Override
	protected void performSave(boolean clearIsModified) throws SaveResourceException {

		getLoadedResourceData().manageImports();

		if (getPersistencyStrategy() == PersistencyStrategy.XML) {
			saveToXML(getLoadedResourceData());
		}

		saveToFML(getLoadedResourceData());

		// Save meta data
		saveMetaData();

		if (clearIsModified) {
			notifyResourceStatusChanged();
		}
	}

	private FMLCompilationUnit loadFromFML() throws ParseException, IOException {
		logger.info("Loading from FML " + getIODelegate().getSerializationArtefact());
		InputStream inputStream = getInputStream();
		try {
			FMLCompilationUnit returned = getFMLParser().parse(inputStream, getFactory(), (modelSlotClasses) -> {
				return updateFMLModelFactory(modelSlotClasses);
			}, false); // In this case, don't perform deserialization now, this will be done later in a second pass
			returned.setResource(this);
			return returned;
		} catch (ParseException e) {
			System.out.println("ParseException while reading " + getIODelegate().getSerializationArtefact());
			throw e;
		} finally {
			inputStream.close();
		}

		/*
		
		if (getIODelegate() instanceof DirectoryBasedIODelegate && getFMLParser() != null) {
			DirectoryBasedIODelegate ioDelegate = (DirectoryBasedIODelegate) getIODelegate();
			File fmlFile = new File(ioDelegate.getDirectory(), ioDelegate.getDirectory().getName());
			System.out.println("Tiens faudrait aussi charger le fichier " + fmlFile);
			if (fmlFile.exists()) {
				try {
					FMLCompilationUnit parsedCompilationUnit = getFMLParser().parse(fmlFile, getFactory());
				} catch (ParseException e) {
					logger.warning("Failed to parse " + fmlFile);
					requiresFMLPrettyPrintInitialization = true;
				}
			}
			else {
				requiresFMLPrettyPrintInitialization = true;
			}
		}*/

	}

	private void saveToFML(FMLCompilationUnit toBeSaved) throws SaveResourceException {
		if (getFlexoIOStreamDelegate() == null) {
			throw new SaveResourceException(getIODelegate());
		}

		if (toBeSaved.getPrettyPrintDelegate() == null) {
			toBeSaved.setResource(this);
			getFMLParser().initPrettyPrint(toBeSaved);
		}

		FileWritingLock lock = getFlexoIOStreamDelegate().willWriteOnDisk();

		if (logger.isLoggable(Level.INFO)) {
			logger.info("Saving resource " + this + " : " + getIODelegate().getSerializationArtefact());
		}

		if (getFlexoIOStreamDelegate() instanceof FileIODelegate) {
			File temporaryFile = null;
			try {
				File fileToSave = ((FileIODelegate) getFlexoIOStreamDelegate()).getFile();
				// Make local copy
				makeLocalCopy(fileToSave);
				// Using temporary file
				temporaryFile = ((FileIODelegate) getIODelegate()).createTemporaryArtefact(".txt");
				if (logger.isLoggable(Level.FINE)) {
					logger.finer("Creating temp file " + temporaryFile.getAbsolutePath());
				}
				try (FileOutputStream fos = new FileOutputStream(temporaryFile)) {
					write(toBeSaved, fos);
				}
				System.out.println("Renamed " + temporaryFile + " to " + fileToSave);
				FileUtils.rename(temporaryFile, fileToSave);
			} catch (IOException e) {
				e.printStackTrace();
				if (temporaryFile != null) {
					temporaryFile.delete();
				}
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Failed to save resource " + getIODelegate().getSerializationArtefact());
				}
				getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
				throw new SaveResourceException(getIODelegate(), e);
			}
		}
		else {
			write(toBeSaved, getOutputStream());
		}

		getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);

	}

	/**
	 * 
	 * @throws IOException
	 */
	private void write(FMLCompilationUnit compilationUnit, OutputStream out) throws SaveResourceException {
		logger.info("Writing " + getIODelegate().getSerializationArtefact());
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			bw.write(compilationUnit.getFMLPrettyPrint());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new SaveResourceException(getIODelegate());
		} finally {
			try {
				out.close();
			} catch (IOException e) {
			}
		}
		logger.info("Wrote " + getIODelegate().getSerializationArtefact());
	}

	/**
	 * Return XML file when relevant and existant (deprecated)
	 * 
	 * @return
	 * @throws LocatorNotFoundException
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 */
	@Deprecated
	private <I> I getXMLArtefact() {
		if (getIODelegate() instanceof DirectoryBasedIODelegate) {
			DirectoryBasedIODelegate ioDelegate = (DirectoryBasedIODelegate) getIODelegate();

			String artefactName = ioDelegate.getDirectory().getName();
			String baseName = artefactName.substring(0, artefactName.length() - CompilationUnitResourceFactory.FML_SUFFIX.length());
			return (I) new File(ioDelegate.getDirectory(), baseName + CompilationUnitResourceFactory.FML_XML_SUFFIX);
			/*
			File f = new File(ioDelegate.getDirectory(), baseName + CompilationUnitResourceFactory.FML_XML_SUFFIX);
			ResourceLocatorDelegate locator = ioDelegate.getSerializationArtefactAsResource().getLocator();		
			return new FileResourceImpl(locator,f);*/
			// return new FileResourceImpl(f);
		}
		if (getIODelegate() instanceof DirectoryBasedJarIODelegate) {
			DirectoryBasedJarIODelegate ioDelegate = (DirectoryBasedJarIODelegate) getIODelegate();
			String artefactName = ioDelegate.getDirectory().getName();
			String baseName = artefactName.substring(0, artefactName.length() - CompilationUnitResourceFactory.FML_SUFFIX.length());
			List<? extends Resource> contents = ioDelegate.getDirectory().getContents();
			for (Resource child : contents) {
				InJarResourceImpl entry = (InJarResourceImpl) child;
				if (entry.getName().contains(CompilationUnitResourceFactory.FML_XML_SUFFIX)) {
					return (I) entry;
				}
			}
		}
		return null;
	}

	private Resource getXMLArtefactResource() throws MalformedURLException, LocatorNotFoundException {
		if (getIODelegate() instanceof DirectoryBasedIODelegate) {
			DirectoryBasedIODelegate ioDelegate = (DirectoryBasedIODelegate) getIODelegate();
			String artefactName = ioDelegate.getDirectory().getName();
			String baseName = artefactName.substring(0, artefactName.length() - CompilationUnitResourceFactory.FML_SUFFIX.length());
			File f = new File(ioDelegate.getDirectory(), baseName + CompilationUnitResourceFactory.FML_XML_SUFFIX);
			ResourceLocatorDelegate locator = ioDelegate.getSerializationArtefactAsResource().getLocator();
			return new FileResourceImpl(locator, f);
		}
		if (getIODelegate() instanceof DirectoryBasedJarIODelegate) {
			DirectoryBasedJarIODelegate ioDelegate = (DirectoryBasedJarIODelegate) getIODelegate();
			String artefactName = ioDelegate.getDirectory().getName();
			String baseName = artefactName.substring(0, artefactName.length() - CompilationUnitResourceFactory.FML_SUFFIX.length());
			List<? extends Resource> contents = ioDelegate.getDirectory().getContents();
			for (Resource child : contents) {
				InJarResourceImpl entry = (InJarResourceImpl) child;
				if (entry.getName().contains(CompilationUnitResourceFactory.FML_XML_SUFFIX)) {
					return entry;
				}
			}
		}
		return null;
	}

	/**
	 * Return XML input stream (deprecated)
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws LocatorNotFoundException
	 * @throws MalformedURLException
	 */
	@Deprecated
	private InputStream getXMLInputStream() throws FileNotFoundException, MalformedURLException, LocatorNotFoundException {
		Resource xmlArtefactResource = getXMLArtefactResource();
		if (xmlArtefactResource != null) {
			return xmlArtefactResource.openInputStream();
		}
		return null;
	}

	private FMLCompilationUnit loadFromXML() {

		if (getPersistencyStrategy() == PersistencyStrategy.XML2FML) {
			willConvertFromXMLtoFML();
		}

		logger.info("Loading from XML " + getXMLArtefact());
		try {
			logger.info("Resource " + getXMLArtefactResource());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (LocatorNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		VirtualModel virtualModel = null;
		InputStream ioStream = null;
		try {
			ioStream = getXMLInputStream();
			// startDeserializing();
			virtualModel = (VirtualModel) getFactory().deserialize(ioStream);
			// stopDeserializing();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (ioStream != null) {
					ioStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		FMLCompilationUnit returned = getFactory().newCompilationUnit();
		returned.setVirtualModel(virtualModel);
		for (UseModelSlotDeclaration useModelSlotDeclaration : virtualModel.getUseDeclarations()) {
			// System.out.println("Hop: " + useModelSlotDeclaration + " of " + useModelSlotDeclaration.getClass());
			returned.addToUseDeclarations(useModelSlotDeclaration);
		}
		for (UseModelSlotDeclaration useModelSlotDeclaration : new ArrayList<UseModelSlotDeclaration>(virtualModel.getUseDeclarations())) {
			virtualModel.removeFromUseDeclarations(useModelSlotDeclaration);
		}
		returned.setResource(this);

		getFMLParser().initPrettyPrint(returned);

		if (getPersistencyStrategy() == PersistencyStrategy.XML2FML) {
			FMLObjectImpl.IS_CONVERTING_FROM_XML = false;
		}

		return returned;
	}

	private boolean willConvertFromXMLtoFML = false;

	private void willConvertFromXMLtoFML() {
		FMLObjectImpl.IS_CONVERTING_FROM_XML = true;
		willConvertFromXMLtoFML = true;
	}

	private void convertFromXMLToFML() {

		new XMLToFMLConverter(getLoadedResourceData()).convert();

	}

	private void saveToXML(FMLCompilationUnit toBeSaved) throws SaveResourceException {
		File temporaryFile = null;
		// FileWritingLock lock = getFlexoIOStreamDelegate().willWriteOnDisk();

		/*if (getFlexoIOStreamDelegate() != null && getFlexoIOStreamDelegate().getSaveToSourceResource()
				&& getFlexoIOStreamDelegate().getSourceResource() != null) {
			logger.info("Saving SOURCE resource " + this + " : " + getFlexoIOStreamDelegate().getSourceResource().getFile() + " version="
					+ getModelVersion());
		}
		else {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Saving resource " + this + " : " + getFile() + " version=" + getModelVersion());
			}
		}*/
		try {
			/*
			 * File dir = getFile().getParentFile(); willWrite(dir); if
			 * (!dir.exists()) { dir.mkdirs(); } willWrite(getFile());
			 */
			// Make local copy
			makeLocalCopy();
			// Using temporary file

			temporaryFile = File.createTempFile("temp", ".xml", getFile().getParentFile());
			if (logger.isLoggable(Level.FINE)) {
				logger.finer("Creating temp file " + temporaryFile.getAbsolutePath());
			}
			performXMLSerialization(toBeSaved, temporaryFile);
			if (logger.isLoggable(Level.FINE)) {
				logger.finer("Renaming temp file " + temporaryFile.getAbsolutePath() + " to " + getFile().getAbsolutePath());
			}
			// Renaming temporary file is done in post serialization
			postXMLSerialization(temporaryFile /*, lock*/);
		} catch (IOException e) {
			e.printStackTrace();
			if (temporaryFile != null) {
				temporaryFile.delete();
			}
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Failed to save resource " + this + " with model version " + getModelVersion());
			}
			// getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
			throw new SaveResourceException(getIODelegate(), e);
		} /*
			* finally { hasWritten(getFile());
			* hasWritten(getFile().getParentFile()); }
			*/
	}

	/**
	 * @param version
	 * @param temporaryFile
	 * @param lock
	 * @param clearIsModified
	 * @throws IOException
	 */
	private void postXMLSerialization(File temporaryFile/*, FileWritingLock lock*/) throws IOException {
		if (getXMLArtefact() instanceof File) {
			File xmlFile = (File) getXMLArtefact();
			FileUtils.rename(temporaryFile, xmlFile);
		}
		// getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
		/*if (clearIsModified) {
			notifyResourceStatusChanged();
		}*/
	}

	private void performXMLSerialization(FMLCompilationUnit toBeSaved, File temporaryFile) throws IOException {
		try (FileOutputStream out = new FileOutputStream(temporaryFile)) {
			getFactory().serialize(toBeSaved.getVirtualModel(), out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	private VirtualModelInfo virtualModelInfo;

	@Override
	public <I> void forceUpdateDependencies(FlexoResourceCenter<I> resourceCenter) {
		virtualModelInfo = findVirtualModelInfo(resourceCenter, true);
		if (virtualModelInfo != null) {
			for (String dependencyURI : virtualModelInfo.getDependencies()) {
				FlexoResource dependency = resourceCenter.getServiceManager().getResourceManager().getResource(dependencyURI);
				if (dependency != null && !getDependencies().contains(dependency)) {
					this.addToDependencies(dependency);
				}
				else {
					// Dependency not yet found, register as pending
					resourceCenter.getServiceManager().getResourceManager().registerPendingDependencyResource(this, dependencyURI);
				}
			}
		}
		else {
			logger.warning("No VirtualModelInfo for " + this);
			System.out.println(getIODelegate().getSerializationArtefact());
		}
	}

	@Override
	public <I> VirtualModelInfo getVirtualModelInfo(FlexoResourceCenter<I> resourceCenter) {
		if (virtualModelInfo == null) {
			virtualModelInfo = findVirtualModelInfo(resourceCenter, false);
		}
		return virtualModelInfo;
	}

	private <I> VirtualModelInfo findVirtualModelInfo(FlexoResourceCenter<I> resourceCenter, boolean forceRebuild) {
		if (resourceCenter instanceof FlexoProject) {
			resourceCenter = ((FlexoProject<I>) resourceCenter).getDelegateResourceCenter();
		}

		if (resourceCenter instanceof FileSystemBasedResourceCenter) {
			FileSystemMetaDataManager metaDataManager = ((FileSystemBasedResourceCenter) resourceCenter).getMetaDataManager();
			File file = (File) getIODelegate().getSerializationArtefact();

			if (!forceRebuild && (file.lastModified() < metaDataManager.metaDataLastModified(file))) {
				// OK, in this case the metadata file is there and more recent than .fml.xml file
				// Attempt to retrieve metadata from cache
				String uri = metaDataManager.getProperty("uri", file);
				String name = metaDataManager.getProperty("name", file);
				String version = metaDataManager.getProperty("version", file);
				String requiredModelSlotList = metaDataManager.getProperty("requiredModelSlotList", file);
				String dependenciesList = metaDataManager.getProperty("dependenciesList", file);
				String flexoConceptsList = metaDataManager.getProperty("flexoConceptsList", file);
				String virtualModelClassName = metaDataManager.getProperty("virtualModelClassName", file);
				if (uri != null && name != null && version != null /*&& modelVersion != null*/ && requiredModelSlotList != null) {
					// Metadata are present, take it from cache
					// System.out.println("Return info from cache for " + this);
					return new VirtualModelInfo(uri, version, name, requiredModelSlotList, dependenciesList, flexoConceptsList,
							virtualModelClassName);
				}
			}
			else {
				// No way, metadata are either not present or older than file version, we should parse XML file, continuing...
			}
		}

		// System.out.println("Retrieve info from file for " + this);

		VirtualModelInfo returned = null;

		switch (getPersistencyStrategy()) {
			case XML:
			case XML2FML:
				if (getXMLArtefact() != null && resourceCenter.exists(getXMLArtefact())) {
					returned = retrieveInfoFromXML(resourceCenter);
					break;
				}
				else {
					// Retrieve infos from FML file
					returned = retrieveInfoFromFML(resourceCenter);
					break;
				}
			case FML:
				returned = retrieveInfoFromFML(resourceCenter);
				break;
			default:
				break;
		}

		/*try {
		if (getXMLArtefact() != null) {
			returned = retrieveInfoFromXML(resourceCenter);
		}
		else {
			returned = retrieveInfoFromFML(resourceCenter);
		}
		} catch (Exception e) {
		e.printStackTrace();
			returned = retrieveInfoFromFML(resourceCenter);
		}*/

		if (resourceCenter instanceof FileSystemBasedResourceCenter && returned != null) {
			// Save metadata !!!
			FileSystemMetaDataManager metaDataManager = ((FileSystemBasedResourceCenter) resourceCenter).getMetaDataManager();
			File file = (File) getIODelegate().getSerializationArtefact();

			metaDataManager.setProperty("uri", returned.getURI(), file, false);
			metaDataManager.setProperty("name", returned.getName(), file, false);
			metaDataManager.setProperty("version", returned.getVersion(), file, false);
			metaDataManager.setProperty("requiredModelSlotList", returned.getRequiredModelSlotListAsString(), file, false);
			metaDataManager.setProperty("dependenciesList", returned.getDependenciesListAsString(), file, false);
			metaDataManager.setProperty("flexoConceptsList", returned.getFlexoConceptsListAsString(), file, false);
			metaDataManager.setProperty("virtualModelClassName", returned.getVirtualModelClassName(), file, false);

			metaDataManager.saveMetaDataProperties(file);
			// System.out.println("********* On sauve les infos pour " + this);
		}

		return returned;
	}

	private <I> VirtualModelInfo retrieveInfoFromXML(FlexoResourceCenter resourceCenter) {

		XMLRootElementInfo xmlRootElementInfo = resourceCenter.getXMLRootElementInfo(getXMLArtefact(), true, "UseModelSlotDeclaration");

		if (xmlRootElementInfo == null) {
			return null;
		}

		String uri = xmlRootElementInfo.getAttribute("uri");
		String name = xmlRootElementInfo.getAttribute("name");
		String version = xmlRootElementInfo.getAttribute("version");
		String virtualModelClassName = xmlRootElementInfo.getAttribute("virtualModelClass");

		if (StringUtils.isEmpty(name)) {
			if (StringUtils.isNotEmpty(uri)) {
				if (uri.indexOf("/") > -1) {
					name = uri.substring(uri.lastIndexOf("/") + 1);
				}
				else if (uri.indexOf("\\") > -1) {
					name = uri.substring(uri.lastIndexOf("\\") + 1);
				}
				else {
					name = uri;
				}
			}
		}

		String requiredModelSlotList = "";
		boolean isFirst = true;
		for (XMLElementInfo elInfo : xmlRootElementInfo.getElements()) {
			requiredModelSlotList = requiredModelSlotList + (isFirst ? "" : ",") + elInfo.getAttribute("modelSlotClass");
			isFirst = false;
		}

		return new VirtualModelInfo(uri, version, name, requiredModelSlotList, "", "", virtualModelClassName);

	}

	private <I> VirtualModelInfo retrieveInfoFromFML(FlexoResourceCenter<I> resourceCenter) {

		// System.out.println("***** On cherche les infos pour " + this);
		InputStream inputStream = getInputStream();
		try {
			return getFMLParser().findVirtualModelInfo(inputStream, getFactory());
		} catch (ParseException e) {
			e.printStackTrace();
			System.out.println("ParseException while reading " + getIODelegate().getSerializationArtefact());
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		return "CompilationUnitResource@" + Integer.toHexString(hashCode()) + "/" + getName();
	}

	@Override
	public void resolvedCrossReferenceDependency(FlexoResource<?> requestedResource) {
		super.resolvedCrossReferenceDependency(requestedResource);
		// System.out.println("resolvedCrossReferenceDependancy for " + getName());
	}

}
