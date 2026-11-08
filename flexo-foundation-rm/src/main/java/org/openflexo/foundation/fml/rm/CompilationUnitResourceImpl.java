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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FMLValidationModel;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitParser;
import org.openflexo.foundation.fml.parser.FMLExpressionParser;
import org.openflexo.foundation.fml.parser.ParseException;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.fml.validation.BindingIsRequiredAndMustBeValid.InvalidRequiredBindingIssue;
import org.openflexo.foundation.fml.visitor.FlexoConceptVisitor;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.DirectoryBasedIODelegate;
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
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FileSystemMetaDataManager;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.StringUtils;

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
		if (returned == null) {
			// No default URI from the resource center: a project being closed has lost its delegate resource center (CORE-D-27).
			// FlexoResourceImpl.getURI() falls back on the serialization artefact
			return null;
		}
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
		for (Class<? extends ModelSlot<?, ?>> msClass : getUsedModelSlots()) {
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

	@Override
	public boolean isUnparseable() {
		return StringUtils.isNotEmpty(getUnparseableContents());
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

	/**
	 * Flag indicating that the second loading pass of this resource is done
	 */
	private boolean isFinalized = false;

	@Override
	public boolean isWaitingForReferencedUnits() {
		for (FlexoResource<?> dependency : getCrossReferenceDependencies()) {
			if (dependency instanceof CompilationUnitResourceImpl && !((CompilationUnitResourceImpl) dependency).isFinalized) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The compilation units finalized while this one was not yet (compilation units importing each other), and to revalidate once it is
	 */
	private final List<CompilationUnitResourceImpl> resourcesToRevalidateOnceFinalized = new ArrayList<>();

	@Override
	public void finalizeLoadResourceData() throws ResourceLoadingCancelledException, FileNotFoundException, FlexoException {
		// The second pass analyzes the bindings the first one deferred, and analyzing a binding sets its expression, which notifies its
		// owner and marks this unit modified. That is still loading, not an edition: what was loaded from disk and not edited must not
		// be proposed for saving (CORE-D-26)
		boolean wasModifiedBeforeSecondPass = getLoadedResourceData().isModified();
		FMLCompilationUnitNode cuNode = (FMLCompilationUnitNode) getLoadedResourceData().getPrettyPrintDelegate();
		if (cuNode != null) {
			cuNode.getSemanticsAnalyzer().finalizeDeserialization(true);
		}
		if (!wasModifiedBeforeSecondPass) {
			getLoadedResourceData().clearIsModified();
		}
		isFinalized = true;
		// A compilation unit referenced by this one, but not finalized yet, is still loading higher in the stack: the bindings of this
		// unit were analyzed against its partial content (members not declared yet are unresolved), and those found invalid must be
		// analyzed again once it is complete (CORE-D-14)
		for (FlexoResource<?> dependency : getCrossReferenceDependencies()) {
			if (dependency instanceof CompilationUnitResourceImpl && !((CompilationUnitResourceImpl) dependency).isFinalized) {
				((CompilationUnitResourceImpl) dependency).resourcesToRevalidateOnceFinalized.add(this);
			}
		}
		if (!resourcesToRevalidateOnceFinalized.isEmpty()) {
			List<CompilationUnitResourceImpl> resourcesToRevalidate = new ArrayList<>(resourcesToRevalidateOnceFinalized);
			resourcesToRevalidateOnceFinalized.clear();
			// Analyzing a binding again is part of loading, not an edition: it must not leave the unit modified
			Map<CompilationUnitResourceImpl, Boolean> wasModified = new HashMap<>();
			// What was postponed is resolved for all these units first: a binding of one of them may rely on a parent concept (and the
			// members it brings, such as declaringElement) attached to another one, whatever the order in which they were finalized
			for (CompilationUnitResourceImpl resource : resourcesToRevalidate) {
				FMLCompilationUnitNode node = getSemanticsNode(resource);
				if (node != null) {
					wasModified.put(resource, resource.getLoadedResourceData().isModified());
					node.getSemanticsAnalyzer().retryPendingResolutions();
				}
			}
			for (CompilationUnitResourceImpl resource : resourcesToRevalidate) {
				FMLCompilationUnitNode node = getSemanticsNode(resource);
				if (node != null) {
					// What is still invalid once no referenced unit is left to complete is reported, as the first pass did not
					node.getSemanticsAnalyzer().attemptToFixInvalidBindings(!resource.isWaitingForReferencedUnits());
					if (!wasModified.get(resource)) {
						resource.getLoadedResourceData().clearIsModified();
					}
				}
			}
		}
		notifyResourceLoaded();
	}

	private static FMLCompilationUnitNode getSemanticsNode(CompilationUnitResourceImpl resource) {
		return resource.getLoadedResourceData() != null ? (FMLCompilationUnitNode) resource.getLoadedResourceData().getPrettyPrintDelegate()
				: null;
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

		isFinalized = false;
		setLoading(true);

		// Now we have to activate all required technologies
		activateRequiredTechnologies();

		try {
			// Now load the non cross-reference dependencies
			for (FlexoResource<?> dependency : getNonCrossReferenceDependencies()) {
				logger.fine("While loading " + this + " load non cross-referenced dependency " + dependency);
				dependency.loadResourceData();
			}

			startDeserializing();

			notifyResourceWillLoad();

			resourceData = performLoad();
			resourceData.setResource(this);
			resourceData.clearIsModified();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			/*} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// hasParseErrors = true;
			 */
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
		for (CompilationUnitResource vmRes : getContainedCompilationUnitResources()) {
			if (vmRes.getName().equals(virtualModelNameOrURI) || vmRes.getURI().equals(virtualModelNameOrURI)) {
				return vmRes;
			}
		}
		return null;
	}

	@Override
	public List<CompilationUnitResource> getContainedCompilationUnitResources() {
		return getContents(CompilationUnitResource.class);
	}

	@Override
	public LocalizedDictionaryResource getLocalizedDictionaryResource() {
		List<LocalizedDictionaryResource> dictionaries = getContents(LocalizedDictionaryResource.class);
		return dictionaries.isEmpty() ? null : dictionaries.get(0);
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

	private FlexoResourceCenter getSerializationRC() {
		if (getResourceCenter() instanceof FlexoProject) {
			return ((FlexoProject) getResourceCenter()).getDelegateResourceCenter();
		}
		return getResourceCenter();
	}

	private void saveMetaData() {
		if (getSerializationRC() instanceof FileSystemBasedResourceCenter) {
			FileSystemBasedResourceCenter rc = (FileSystemBasedResourceCenter) getSerializationRC();
			FileSystemMetaDataManager metaDataManager = rc.getMetaDataManager();
			File file = ((File) getIODelegate().getSerializationArtefact());
			metaDataManager.setProperty("uri", getURI(), file, false);
			metaDataManager.setProperty("name", getName(), file, false);
			metaDataManager.setProperty("version", getVersion().toString(), file, false);
			// metaDataManager.setProperty("modelVersion", getModelVersion().toString(), file, false);
			metaDataManager.setProperty("requiredModelSlotList", getUsedModelSlotsAsString(), file, false);
			metaDataManager.setProperty("flexoConceptsList", getFlexoConceptsAsString(), file, false);
			metaDataManager.setProperty("dependenciesList", getDependenciesAsString(), file, false);
			if (getVirtualModelClass() != null) {
				metaDataManager.setProperty("virtualModelClassName", getVirtualModelClass().getName(), file, false);
			}
			metaDataManager.saveMetaDataProperties(file);
		}
	}

	private List<Class<? extends ModelSlot<?, ?>>> usedModelSlots = new ArrayList<>();

	/**
	 * Return {@link ModelSlot} classes used in this {@link VirtualModel} resource<br>
	 * Note that this information is extracted from metadata or from reading the FML file before effective parsing<br>
	 * This information is used to determine which technology adapters have to be activated before {@link VirtualModel} is loaded
	 * 
	 * @return
	 */
	@Override
	public List<Class<? extends ModelSlot<?, ?>>> getUsedModelSlots() {
		return usedModelSlots;
	}

	@Override
	public String getUsedModelSlotsAsString() {
		boolean isFirst = true;
		StringBuffer sb = new StringBuffer();
		for (Class<? extends ModelSlot<?, ?>> msClass : usedModelSlots) {
			sb.append((isFirst ? "" : ",") + msClass.getName());
			isFirst = false;
		}
		return sb.toString();
	}

	public String getFlexoConceptsAsString() {
		final StringBuffer sb = new StringBuffer();
		getCompilationUnit().getVirtualModel().accept(new FlexoConceptVisitor() {
			@Override
			public void visitVirtualModel(VirtualModel virtualModel) {
				// System.out.println("visitVirtualModel: " + virtualModel);
			}

			@Override
			public void visitFlexoConcept(FlexoConcept flexoConcept) {
				if (sb.length() == 0) {
					sb.append(flexoConcept.getName());
				}
				else {
					sb.append("," + flexoConcept.getName());
				}
			}
		});
		return sb.toString();
	}

	public String getDependenciesAsString() {

		boolean isFirst = true;
		StringBuffer sb = new StringBuffer();
		for (ElementImportDeclaration importDeclaration : getCompilationUnit().getElementImports()) {
			try {
				String importedResourceURI = importDeclaration.getResourceReference()
						.getBindingValue(getCompilationUnit().getReflectedBindingEvaluationContext());
				// System.out.println("Found import " + importedResourceURI);
				if (StringUtils.isNotEmpty(importedResourceURI)) {
					sb.append((isFirst ? "" : ",") + importedResourceURI);
					isFirst = false;
				}
				else {
					logger.warning("Cannot not find resource identified by " + importDeclaration.getResourceReference());
				}
			} catch (TypeMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullReferenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassCastException e) {
				logger.warning("Unexpected type for resourceReference=" + importDeclaration.getResourceReference());
				e.printStackTrace();
			}
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
				usedModelSlots.add((Class<? extends ModelSlot<?, ?>>) Class.forName(next));
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
	public FMLModelFactory updateFMLModelFactory(List<Class<? extends ModelSlot<?, ?>>> usedModelSlots) {
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
		if (isUnparseable()) {
			return getUnparseableContents();
		}
		if (getLoadedResourceData() != null) {
			return getLoadedResourceData().getFMLPrettyPrint();
		}
		return null;
	}

	@Override
	protected FMLCompilationUnit performLoad() throws IOException {
		return loadFromFML();
	}

	/**
	 * Resource saving safe implementation<br>
	 * Initial resource is first copied, then we write in a temporary file, renamed at the end when the seriaization has been successfully
	 * performed
	 */
	@Override
	protected void performSave(boolean clearIsModified) throws SaveResourceException {

		getLoadedResourceData().manageImports();

		saveToFML();

		// Save meta data
		saveMetaData();

		if (clearIsModified) {
			notifyResourceStatusChanged();
		}
	}

	private FMLCompilationUnit loadFromFML() throws /*ParseException,*/ IOException {
		logger.info("Loading from FML " + getIODelegate().getSerializationArtefact());
		InputStream inputStream = getInputStream();
		try {
			FMLCompilationUnit returned = getFMLParser().parse(inputStream, getFactory(), (modelSlotClasses) -> {
				return updateFMLModelFactory(modelSlotClasses);
			}, false); // In this case, don't perform deserialization now, this will be done later in a second pass
			setUnparseableContents(null);
			returned.setResource(this);
			logger.info("DONE load from FML " + getIODelegate().getSerializationArtefact());
			return returned;
		} catch (ParseException e) {
			setUnparseableContents(e.getRawSource().getRawText());
			logger.info("ParseException while reading " + getIODelegate().getSerializationArtefact());
			FMLCompilationUnit returned = getFactory().newCompilationUnit();
			VirtualModel virtualModel = getFactory().newVirtualModel();
			returned.setVirtualModel(virtualModel);
			try {
				returned.setName(getName());
				virtualModel.setName(getName());
			} catch (InvalidNameException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return returned;
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

	private void saveToFML() throws SaveResourceException {

		logger.info("Saving FML " + this);

		if (getFlexoIOStreamDelegate() == null) {
			throw new SaveResourceException(getIODelegate());
		}

		if (getLoadedResourceData().getPrettyPrintDelegate() == null) {
			getLoadedResourceData().setResource(this);
			getFMLParser().initPrettyPrint(getLoadedResourceData());
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
					write(fos);
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
			write(getOutputStream());
		}

		getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);

	}

	/**
	 * 
	 * @throws IOException
	 */
	private void write(OutputStream out) throws SaveResourceException {
		logger.info("Writing " + getIODelegate().getSerializationArtefact());
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			bw.write(getRawSource());
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
				// OK, in this case the metadata file is there and more recent than the .fml file
				// Attempt to retrieve metadata from cache
				String uri = metaDataManager.getProperty("uri", file);
				String name = metaDataManager.getProperty("name", file);
				String version = metaDataManager.getProperty("version", file);
				String requiredModelSlotList = metaDataManager.getProperty("requiredModelSlotList", file);
				String dependenciesList = metaDataManager.getProperty("dependenciesList", file);
				String flexoConceptsList = metaDataManager.getProperty("flexoConceptsList", file);
				String virtualModelClassName = metaDataManager.getProperty("virtualModelClassName", file);
				if (uri != null && name != null /*&& version != null*/ /*&& modelVersion != null*/ /*&& requiredModelSlotList != null*/) {
					// Metadata are present, take it from cache
					// System.out.println("Return info from cache for " + this);
					return new VirtualModelInfo(uri, version, name, requiredModelSlotList, dependenciesList, flexoConceptsList,
							virtualModelClassName);
				}
			}
			else {
				// No way, metadata are either not present or older than file version, we should parse the FML file, continuing...
			}
		}

		// System.out.println("Retrieve info from file for " + this);

		VirtualModelInfo returned = retrieveInfoFromFML(resourceCenter);

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

	private <I> VirtualModelInfo retrieveInfoFromFML(FlexoResourceCenter<I> resourceCenter) {

		// System.out.println("***** On cherche les infos pour " + this);
		InputStream inputStream = null;
		try {
			inputStream = getInputStream();
			// TODO : would be better to handle IOException !
			if (inputStream != null) {
				return getFMLParser().findVirtualModelInfo(inputStream, getFactory());
			}
			else {
				return null;
			}
		} catch (ParseException e) {
			e.printStackTrace();
			logger.info("ParseException while reading " + getIODelegate().getSerializationArtefact());
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
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
