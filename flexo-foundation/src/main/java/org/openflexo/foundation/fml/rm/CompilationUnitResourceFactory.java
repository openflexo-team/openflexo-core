/*
 * (c) Copyright 2013 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openflexo.foundation.fml.rm;

import java.io.IOException;
import java.util.logging.Logger;

import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource.VirtualModelInfo;
import org.openflexo.foundation.resource.DirectoryBasedJarIODelegate;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.resource.TechnologySpecificFlexoResourceFactory;
import org.openflexo.foundation.resource.TechnologySpecificPamelaResourceFactory;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.xml.XMLRootElementInfo;

/**
 * Implementation of {@link TechnologySpecificPamelaResourceFactory} for {@link CompilationUnitResource}<br>
 * 
 * This factory is responsible to create or retrieve {@link VirtualModel} objects
 * 
 * @author sylvain
 *
 */
public class CompilationUnitResourceFactory
		extends TechnologySpecificFlexoResourceFactory<CompilationUnitResource, FMLCompilationUnit, FMLTechnologyAdapter> {

	public static final FlexoVersion INITIAL_REVISION = new FlexoVersion("0.1");
	// public static final FlexoVersion CURRENT_FML_VERSION = new FlexoVersion("2.0");
	public static final String FML_SUFFIX = ".fml";
	public static final String FML_XML_SUFFIX = ".fml.xml";

	private static final Logger logger = Logger.getLogger(CompilationUnitResourceFactory.class.getPackage().getName());

	/**
	 * Build new VirtualModelResourceFactory
	 * 
	 * @throws ModelDefinitionException
	 */
	public CompilationUnitResourceFactory() throws ModelDefinitionException {
		super(CompilationUnitResource.class);
		// TODO: find a better way to initialize this
		try {
			setImplementingClassForInterface(
					(Class<? extends CompilationUnitResource>) Class.forName("org.openflexo.foundation.fml.rm.CompilationUnitResourceImpl"),
					CompilationUnitResource.class);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Build and return model factory to use for resource data managing
	 */
	// @Override
	public FMLModelFactory makeModelFactory(CompilationUnitResource resource,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager) throws ModelDefinitionException {
		return new FMLModelFactory(resource, technologyContextManager.getServiceManager());
	}

	/**
	 * Build and return an empty VirtualModel for the supplied resource
	 * 
	 * @return
	 * @throws InvalidNameException
	 */
	@Override
	public FMLCompilationUnit makeEmptyResourceData(CompilationUnitResource resource) {
		/*if (resource.getSpecializedResourceDataClass() != null) {
			// System.out.println("Plutot que de creer un VirtualModel, je cree un " + resource.getSpecializedResourceDataClass());
			return resource.getFactory().newInstance(resource.getSpecializedResourceDataClass());
		}*/
		FMLCompilationUnit returned = resource.getFactory().newCompilationUnit();
		VirtualModel virtualModel = resource.getFactory().newVirtualModel();
		try {
			virtualModel.setName(resource.getName());
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		virtualModel.setURI(resource.getURI());
		returned.setVirtualModel(virtualModel);
		return returned;
	}

	@Override
	protected FMLCompilationUnit createEmptyContents(CompilationUnitResource resource) {
		FMLCompilationUnit returned = super.createEmptyContents(resource);
		/*if (resource.getContainer() != null) {
			resource.getContainer().getCompilationUnit().getVirtualModel().addToVirtualModels(returned.getVirtualModel());
		}*/
		return returned;
	}

	/**
	 * Build a new {@link CompilationUnitResource} with supplied baseName and URI, and located in supplied folder No specialization for
	 * resource data class
	 * 
	 * 
	 * @param baseName
	 * @param virtualModelURI
	 * @param folder
	 * @param technologyContextManager
	 * @param createEmptyContents
	 * @return
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 */
	public <I> CompilationUnitResource makeTopLevelCompilationUnitResource(String baseName, String virtualModelURI,
			RepositoryFolder<CompilationUnitResource, I> folder, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException {
		return makeTopLevelCompilationUnitResource(baseName, virtualModelURI, folder, null, createEmptyContents);
	}

	/**
	 * Build a new {@link CompilationUnitResource} with supplied baseName and URI, and located in supplied folder
	 * 
	 * 
	 * @param baseName
	 * @param virtualModelURI
	 * @param folder
	 * @param technologyContextManager
	 * @param createEmptyContents
	 * @return
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 */
	public <I> CompilationUnitResource makeTopLevelCompilationUnitResource(String baseName, String virtualModelURI,
			RepositoryFolder<CompilationUnitResource, I> folder, Class<? extends VirtualModel> specializedVirtualModelClass,
			boolean createEmptyContents) throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = folder.getResourceRepository().getResourceCenter();
		I serializationArtefact = resourceCenter.createDirectory(baseName.endsWith(FML_SUFFIX) ? baseName : baseName + FML_SUFFIX,
				folder.getSerializationArtefact());

		CompilationUnitResource returned = makeResource(serializationArtefact, resourceCenter, baseName,
				virtualModelURI/*, specializedVirtualModelClass*/, createEmptyContents);

		returned.setVirtualModelClass(specializedVirtualModelClass);
		return returned;
	}

	/**
	 * Build a new {@link CompilationUnitResource} with supplied baseName and URI, and located in supplied VirtualModelResource<br>
	 * No specialization for resource data class
	 * 
	 * @param baseName
	 * @param containerCompilationUnitResource
	 * @param technologyContextManager
	 * @param createEmptyContents
	 * @return
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 */
	public <I> CompilationUnitResource makeContainedCompilationUnitResource(String baseName,
			CompilationUnitResource containerCompilationUnitResource, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException {
		return makeContainedCompilationUnitResource(baseName, containerCompilationUnitResource, null, createEmptyContents);
	}

	/**
	 * Build a new {@link CompilationUnitResource} with supplied baseName and URI, and located in supplied VirtualModelResource
	 * 
	 * @param baseName
	 * @param containerCompilationUnitResource
	 * @param technologyContextManager
	 * @param createEmptyContents
	 * @return
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 */
	public <I> CompilationUnitResource makeContainedCompilationUnitResource(String baseName,
			CompilationUnitResource containerCompilationUnitResource, Class<? extends VirtualModel> specializedVirtualModelClass,
			boolean createEmptyContents) throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) containerCompilationUnitResource.getResourceCenter();
		I serializationArtefact = resourceCenter.createDirectory(baseName.endsWith(FML_SUFFIX) ? baseName : baseName + FML_SUFFIX,
				resourceCenter.getContainer((I) containerCompilationUnitResource.getIODelegate().getSerializationArtefact()));

		CompilationUnitResource returned = initResourceForCreation(serializationArtefact, resourceCenter, baseName,
				containerCompilationUnitResource.getURI() + "/" + baseName + (baseName.endsWith(FML_SUFFIX) ? "" : FML_SUFFIX));
		returned.setVirtualModelClass(specializedVirtualModelClass);

		containerCompilationUnitResource.addToContents(returned);

		registerResource(returned, resourceCenter);

		if (createEmptyContents) {
			createEmptyContents(returned);
			returned.save();
		}

		return returned;
	}

	/**
	 * Used to retrieve a contained VirtualModelResource for supplied containerVirtualModelResource
	 * 
	 * @param serializationArtefact
	 * @param technologyContextManager
	 * @param containerVirtualModelResource
	 * @return
	 * @throws ModelDefinitionException
	 * @throws IOException
	 */
	public <I> CompilationUnitResource retrieveContainedVirtualModelResource(I serializationArtefact,
			CompilationUnitResource containerVirtualModelResource) throws ModelDefinitionException, IOException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) containerVirtualModelResource.getResourceCenter();
		String name = resourceCenter.retrieveName(serializationArtefact);

		CompilationUnitResource returned = initResourceForRetrieving(serializationArtefact, resourceCenter);
		returned.setURI(containerVirtualModelResource.getURI() + "/" + name);

		containerVirtualModelResource.addToContents(returned);
		containerVirtualModelResource.notifyContentsAdded(returned);

		registerResource(returned, resourceCenter);

		return returned;
	}

	@Override
	protected <I> CompilationUnitResource initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			String name, String uri) throws ModelDefinitionException {

		if (name.endsWith(FML_SUFFIX)) {
			name = name.substring(0, name.length() - FML_SUFFIX.length());
		}

		CompilationUnitResource returned = super.initResourceForCreation(serializationArtefact, resourceCenter, name, uri);

		returned.setVersion(INITIAL_REVISION);
		// returned.setModelVersion(CURRENT_FML_VERSION);

		return returned;
	}

	@Override
	protected <I> CompilationUnitResource initResourceForRetrieving(I serializationArtefact, FlexoResourceCenter<I> resourceCenter)
			throws ModelDefinitionException, IOException {

		CompilationUnitResource returned = super.initResourceForRetrieving(serializationArtefact, resourceCenter);

		String artefactName = resourceCenter.retrieveName(serializationArtefact);
		String baseName = artefactName.substring(0, artefactName.length() - FML_SUFFIX.length());

		returned.initName(baseName);

		// We initiate a first factory (that may evolve regarding requiredModelSlotList
		returned.setFactory(makeModelFactory(returned, getTechnologyContextManager(resourceCenter.getServiceManager())));

		// VirtualModelInfo vpi = findVirtualModelInfo(returned, resourceCenter);
		VirtualModelInfo vpi = returned.getVirtualModelInfo(resourceCenter);

		// logger.fine("Found " + vpi.name + " uri=" + vpi.uri + " version=" + vpi.version + " " + vpi.requiredModelSlotList);

		if (vpi != null) {
			returned.setURI(vpi.getURI());
			if (StringUtils.isNotEmpty(vpi.getVersion())) {
				returned.setVersion(new FlexoVersion(vpi.getVersion()));
			}
			else {
				returned.setVersion(INITIAL_REVISION);
			}
			if (vpi.getDependencies() != null) {
				for (String dependencyURI : vpi.getDependencies()) {
					FlexoResource dependency = resourceCenter.getServiceManager().getResourceManager().getResource(dependencyURI);
					if (dependency != null) {
						returned.addToDependencies(dependency);
					}
					else {
						// Dependency not yet found, register as pending
						resourceCenter.getServiceManager().getResourceManager().registerPendingDependencyResource(returned, dependencyURI);
					}
				}
			}
			try {
				returned.setUsedModelSlots(vpi.getRequiredModelSlotListAsString());
			} catch (ClassNotFoundException e) {
				logger.warning("Could not find " + e.getMessage());
			}
			// We set a new factory because of required model slots
			if (StringUtils.isNotEmpty(vpi.getRequiredModelSlotListAsString())) {
				returned.setFactory(makeModelFactory(returned, getTechnologyContextManager(resourceCenter.getServiceManager())));
			}
			if (StringUtils.isNotEmpty(vpi.getVirtualModelClassName())) {
				Class<? extends VirtualModel> virtualModelClass = null;
				try {
					virtualModelClass = (Class<? extends VirtualModel>) Class.forName(vpi.getVirtualModelClassName());
					returned.setVirtualModelClass(virtualModelClass);
				} catch (ClassNotFoundException e) {
					logger.warning("Cannot find class " + vpi.getVirtualModelClassName());
				}
			}
		}
		else {
			logger.warning("Cannot retrieve info from " + serializationArtefact);
			returned.setVersion(INITIAL_REVISION);
			// returned.setModelVersion(CURRENT_FML_VERSION);
		}

		return returned;
	}

	@Override
	protected <I> FlexoIODelegate<I> makeFlexoIODelegate(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		return resourceCenter.makeDirectoryBasedFlexoIODelegate(serializationArtefact, FML_SUFFIX, FML_SUFFIX, this);
	}

	/**
	 * Return boolean indicating is supplied serialization artefact seems to be a valid artefact encoding a {@link VirtualModel}<br>
	 * A valid {@link VirtualModel} is encoded in a directory ending with .fml suffix
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @return
	 */
	@Override
	public <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {

		if (resourceCenter.exists(serializationArtefact) && resourceCenter.isDirectory(serializationArtefact)
				&& resourceCenter.canRead(serializationArtefact) && (resourceCenter.retrieveName(serializationArtefact).endsWith(FML_SUFFIX)
				/*|| resourceCenter.retrieveName(serializationArtefact).endsWith(VIEWPOINT_SUFFIX + "/")*/)) {
			/*final String baseName = candidateFile.getName().substring(0,
					candidateFile.getName().length() - ViewPointResource.VIEWPOINT_SUFFIX.length());
			final File xmlFile = new File(candidateFile, baseName + ".xml");
			return xmlFile.exists();*/
			return true;
		}
		return false;
	}

	@Override
	public <I> CompilationUnitResource registerResource(CompilationUnitResource resource, FlexoResourceCenter<I> resourceCenter) {
		return registerResource(resource, resourceCenter, true);
	}

	public <I> CompilationUnitResource registerResource(CompilationUnitResource resource, FlexoResourceCenter<I> resourceCenter,
			boolean exploreVirtualModels) {
		super.registerResource(resource, resourceCenter);

		// Register the resource in the VirtualModelRepository of supplied resource center
		registerResourceInResourceRepository(resource,
				getTechnologyAdapter(resourceCenter.getServiceManager()).getVirtualModelRepository(resourceCenter));

		// If VirtualModelLibrary not initialized yet, we will do it later in
		// VirtualModelLibrary.initialize() method
		if (resourceCenter.getServiceManager().getVirtualModelLibrary() != null) {
			resource.setVirtualModelLibrary(resourceCenter.getServiceManager().getVirtualModelLibrary());
			resourceCenter.getServiceManager().getVirtualModelLibrary().registerCompilationUnit(resource);
		}

		// Now look for contained virtual models
		if (exploreVirtualModels) {
			exploreVirtualModels(resource);
		}

		return resource;

	}

	/**
	 * Internally called to explore contained {@link VirtualModel} in supplied {@link CompilationUnitResource}
	 * 
	 * @param virtualModelResource
	 * @param technologyContextManager
	 */
	private <I> void exploreVirtualModels(CompilationUnitResource virtualModelResource) {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) virtualModelResource.getResourceCenter();
		I directory = resourceCenter.getContainer((I) virtualModelResource.getIODelegate().getSerializationArtefact());

		// Fixed issue when no .fml is defined (only a .fml.xml)
		if (directory == null && virtualModelResource.getIODelegate() instanceof DirectoryBasedJarIODelegate) {
			directory = (I)((DirectoryBasedJarIODelegate)virtualModelResource.getIODelegate()).getDirectory();
		}
		
		exploreResource(directory, virtualModelResource);
	}

	/**
	 * Internally called to explore contained {@link VirtualModel} in supplied {@link CompilationUnitResource}
	 * 
	 * @param serializationArtefact
	 * @param virtualModelResource
	 * @param technologyContextManager
	 */
	private <I> void exploreResource(I serializationArtefact, CompilationUnitResource virtualModelResource) {

		if (serializationArtefact == null) {
			return;
		}

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) virtualModelResource.getResourceCenter();

		for (I child : resourceCenter.getContents(serializationArtefact)) {
			String childName = resourceCenter.retrieveName(child);
			if (isValidArtefact(child, resourceCenter)) {
				// Following code is deprecated, as it is based on XML version
				I xmlFile = resourceCenter.getEntry(childName + ".xml", child);
				if (resourceCenter.exists(xmlFile)) {
					XMLRootElementInfo result = resourceCenter.getXMLRootElementInfo(xmlFile, true, "UseModelSlotDeclaration");
					if (result != null && (result.getName().equals("VirtualModel")
							|| StringUtils.isNotEmpty(result.getAttribute("virtualModelClass")))) {
						try {
							// Unused CompilationUnitResource childCompilationUnitResource =
							retrieveContainedVirtualModelResource(child, virtualModelResource);
						} catch (ModelDefinitionException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				else { // Handle as FML
					I fmlFile = resourceCenter.getEntry(childName, child);
					if (resourceCenter.exists(fmlFile)) {
						try {
							// Unused CompilationUnitResource childCompilationUnitResource =
							retrieveContainedVirtualModelResource(child, virtualModelResource);
						} catch (ModelDefinitionException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

		}
	}

	/*private static class VirtualModelInfo {
		public String uri;
		public String version;
		public String name;
		// public String modelVersion;
		public String requiredModelSlotList;
		public String virtualModelClassName;
	
		VirtualModelInfo() {
		}
	
		VirtualModelInfo(String uri, String version, String name, String requiredModelSlotList,
				String virtualModelClassName) {
			super();
			this.uri = uri;
			this.version = version;
			this.name = name;
			// this.modelVersion = modelVersion;
			this.requiredModelSlotList = requiredModelSlotList;
			this.virtualModelClassName = virtualModelClassName;
		}
	}
	
	private static <I> VirtualModelInfo findVirtualModelInfo(CompilationUnitResource resource, FlexoResourceCenter<I> resourceCenter) {
	
		if (resourceCenter instanceof FlexoProject) {
			resourceCenter = ((FlexoProject<I>) resourceCenter).getDelegateResourceCenter();
		}
	
		if (resourceCenter instanceof FileSystemBasedResourceCenter) {
			FileSystemMetaDataManager metaDataManager = ((FileSystemBasedResourceCenter) resourceCenter).getMetaDataManager();
			File file = (File) resource.getIODelegate().getSerializationArtefact();
	
			System.out.println("Je cherche les metadonnees de " + file);
			System.out.println("file.lastModified()=" + file.lastModified());
			System.out.println("metaDataManager.metaDataLastModified(file)=" + metaDataManager.metaDataLastModified(file));
	
			if (file.lastModified() < metaDataManager.metaDataLastModified(file)) {
				// OK, in this case the metadata file is there and more recent than .fml.xml file
				// Attempt to retrieve metadata from cache
				String uri = metaDataManager.getProperty("uri", file);
				String name = metaDataManager.getProperty("name", file);
				String version = metaDataManager.getProperty("version", file);
				// String modelVersion = metaDataManager.getProperty("modelVersion", file);
				String requiredModelSlotList = metaDataManager.getProperty("requiredModelSlotList", file);
				String virtualModelClassName = metaDataManager.getProperty("virtualModelClassName", file);
				if (uri != null && name != null && version != null && requiredModelSlotList != null) {
					// Metadata are present, take it from cache
					return new VirtualModelInfo(uri, version, name, requiredModelSlotList, virtualModelClassName);
				}
				System.out.println("prout");
			}
			else {
				// No way, metadata are either not present or older than file version, we should parse XML file, continuing...
			}
		}
	
		VirtualModelInfo returned = new VirtualModelInfo();
		XMLRootElementInfo xmlRootElementInfo = resourceCenter
				.getXMLRootElementInfo((I) resource.getIODelegate().getSerializationArtefact(), true, "UseModelSlotDeclaration");
		if (xmlRootElementInfo == null) {
			return null;
		}
	
		returned.uri = xmlRootElementInfo.getAttribute("uri");
		returned.name = xmlRootElementInfo.getAttribute("name");
		returned.version = xmlRootElementInfo.getAttribute("version");
		// returned.modelVersion = xmlRootElementInfo.getAttribute("modelVersion");
		returned.virtualModelClassName = xmlRootElementInfo.getAttribute("virtualModelClass");
	
		if (StringUtils.isEmpty(returned.name)) {
			if (StringUtils.isNotEmpty(returned.uri)) {
				if (returned.uri.indexOf("/") > -1) {
					returned.name = returned.uri.substring(returned.uri.lastIndexOf("/") + 1);
				}
				else if (returned.uri.indexOf("\\") > -1) {
					returned.name = returned.uri.substring(returned.uri.lastIndexOf("\\") + 1);
				}
				else {
					returned.name = returned.uri;
				}
			}
		}
	
		String requiredModelSlotList = "";
		boolean isFirst = true;
		for (XMLElementInfo elInfo : xmlRootElementInfo.getElements()) {
			requiredModelSlotList = requiredModelSlotList + (isFirst ? "" : ",") + elInfo.getAttribute("modelSlotClass");
			isFirst = false;
		}
	
		returned.requiredModelSlotList = requiredModelSlotList;
	
		if (resourceCenter instanceof FileSystemBasedResourceCenter) {
			// Save metadata !!!
			FileSystemMetaDataManager metaDataManager = ((FileSystemBasedResourceCenter) resourceCenter).getMetaDataManager();
			File file = (File) resource.getIODelegate().getSerializationArtefact();
	
			metaDataManager.setProperty("uri", returned.uri, file, false);
			metaDataManager.setProperty("name", returned.name, file, false);
			metaDataManager.setProperty("version", returned.version, file, false);
			// metaDataManager.setProperty("modelVersion", returned.modelVersion, file, false);
			metaDataManager.setProperty("requiredModelSlotList", returned.requiredModelSlotList, file, false);
			metaDataManager.setProperty("virtualModelClassName", returned.virtualModelClassName, file, false);
	
			metaDataManager.saveMetaDataProperties(file);
		}
	
		return returned;
	}*/

}
