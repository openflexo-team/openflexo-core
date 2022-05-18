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

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.resource.TechnologySpecificPamelaResourceFactory;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.FileSystemMetaDataManager;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.xml.XMLElementInfo;
import org.openflexo.xml.XMLRootElementInfo;

/**
 * Implementation of {@link TechnologySpecificPamelaResourceFactory} for {@link VirtualModelResource}<br>
 * 
 * This factory is responsible to create or retrieve {@link VirtualModel} objects
 * 
 * @author sylvain
 *
 */
public class VirtualModelResourceFactory
extends TechnologySpecificPamelaResourceFactory<VirtualModelResource, VirtualModel, FMLTechnologyAdapter, FMLModelFactory> {

	public static final FlexoVersion INITIAL_REVISION = new FlexoVersion("0.1");
	public static final FlexoVersion CURRENT_FML_VERSION = new FlexoVersion("2.0");
	public static final String FML_SUFFIX = ".fml";
	public static final String FML_XML_SUFFIX = ".fml.xml";

	private static final Logger logger = Logger.getLogger(VirtualModelResourceFactory.class.getPackage().getName());

	/**
	 * Build new VirtualModelResourceFactory
	 * 
	 * @throws ModelDefinitionException
	 */
	public VirtualModelResourceFactory() throws ModelDefinitionException {
		super(VirtualModelResource.class);
	}

	/**
	 * Build and return model factory to use for resource data managing
	 */
	@Override
	public FMLModelFactory makeModelFactory(VirtualModelResource resource,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager) throws ModelDefinitionException {
		return new FMLModelFactory(resource, technologyContextManager.getServiceManager());
	}

	/**
	 * Build and return an empty VirtualModel for the supplied resource
	 * 
	 * @return
	 */
	@Override
	public VirtualModel makeEmptyResourceData(VirtualModelResource resource) {
		if (resource.getSpecializedResourceDataClass() != null) {
			// System.out.println("Plutot que de creer un VirtualModel, je cree un " + resource.getSpecializedResourceDataClass());
			return resource.getFactory().newInstance(resource.getSpecializedResourceDataClass());
		}
		return resource.getFactory().newVirtualModel();
	}

	@Override
	protected VirtualModel createEmptyContents(VirtualModelResource resource) {
		VirtualModel returned = super.createEmptyContents(resource);
		if (resource.getContainer() != null) {
			resource.getContainer().getVirtualModel().addToVirtualModels(returned);
		}
		return returned;
	}

	/**
	 * Build a new {@link VirtualModelResource} with supplied baseName and URI, and located in supplied folder No specialization for
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
	public <I> VirtualModelResource makeTopLevelVirtualModelResource(String baseName, String virtualModelURI,
			RepositoryFolder<VirtualModelResource, I> folder, boolean createEmptyContents)
					throws SaveResourceException, ModelDefinitionException {
		return makeTopLevelVirtualModelResource(baseName, virtualModelURI, folder, null, createEmptyContents);
	}

	/**
	 * Build a new {@link VirtualModelResource} with supplied baseName and URI, and located in supplied folder
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
	public <I> VirtualModelResource makeTopLevelVirtualModelResource(String baseName, String virtualModelURI,
			RepositoryFolder<VirtualModelResource, I> folder, Class<? extends VirtualModel> specializedVirtualModelClass,
			boolean createEmptyContents) throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = folder.getResourceRepository().getResourceCenter();
		I serializationArtefact = resourceCenter.createDirectory(baseName.endsWith(FML_SUFFIX) ? baseName : baseName + FML_SUFFIX,
				folder.getSerializationArtefact());

		return makeResource(serializationArtefact, resourceCenter, baseName, virtualModelURI, specializedVirtualModelClass,
				createEmptyContents);
	}

	/**
	 * Build a new {@link VirtualModelResource} with supplied baseName and URI, and located in supplied VirtualModelResource<br>
	 * No specialization for resource data class
	 * 
	 * @param baseName
	 * @param containerVirtualModelResource
	 * @param technologyContextManager
	 * @param createEmptyContents
	 * @return
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 */
	public <I> VirtualModelResource makeContainedVirtualModelResource(String baseName, VirtualModelResource containerVirtualModelResource,
			boolean createEmptyContents) throws SaveResourceException, ModelDefinitionException {
		return makeContainedVirtualModelResource(baseName, containerVirtualModelResource, null, createEmptyContents);
	}

	/**
	 * Build a new {@link VirtualModelResource} with supplied baseName and URI, and located in supplied VirtualModelResource
	 * 
	 * @param baseName
	 * @param containerVirtualModelResource
	 * @param technologyContextManager
	 * @param createEmptyContents
	 * @return
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 */
	public <I> VirtualModelResource makeContainedVirtualModelResource(String baseName, VirtualModelResource containerVirtualModelResource,
			Class<? extends VirtualModel> specializedVirtualModelClass, boolean createEmptyContents)
					throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) containerVirtualModelResource.getResourceCenter();
		I serializationArtefact = resourceCenter.createDirectory(baseName.endsWith(FML_SUFFIX) ? baseName : baseName + FML_SUFFIX,
				resourceCenter.getContainer((I) containerVirtualModelResource.getIODelegate().getSerializationArtefact()));

		VirtualModelResource returned = initResourceForCreation(serializationArtefact, resourceCenter, baseName,
				containerVirtualModelResource.getURI() + "/" + baseName + (baseName.endsWith(FML_SUFFIX) ? "" : FML_SUFFIX));
		returned.setSpecializedResourceDataClass(specializedVirtualModelClass);

		containerVirtualModelResource.addToContents(returned);

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
	public <I> VirtualModelResource retrieveContainedVirtualModelResource(I serializationArtefact,
			VirtualModelResource containerVirtualModelResource) throws ModelDefinitionException, IOException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) containerVirtualModelResource.getResourceCenter();
		String name = resourceCenter.retrieveName(serializationArtefact);

		VirtualModelResource returned = initResourceForRetrieving(serializationArtefact, resourceCenter);
		returned.setURI(containerVirtualModelResource.getURI() + "/" + name);

		containerVirtualModelResource.addToContents(returned);
		containerVirtualModelResource.notifyContentsAdded(returned);

		registerResource(returned, resourceCenter);

		return returned;
	}

	@Override
	protected <I> VirtualModelResource initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter, String name,
			String uri) throws ModelDefinitionException {

		if (name.endsWith(FML_SUFFIX)) {
			name = name.substring(0, name.length() - FML_SUFFIX.length());
		}

		VirtualModelResource returned = super.initResourceForCreation(serializationArtefact, resourceCenter, name, uri);

		returned.setVersion(INITIAL_REVISION);
		returned.setModelVersion(CURRENT_FML_VERSION);

		return returned;
	}

	@Override
	protected <I> VirtualModelResource initResourceForRetrieving(I serializationArtefact, FlexoResourceCenter<I> resourceCenter)
			throws ModelDefinitionException, IOException {

		VirtualModelResource returned = super.initResourceForRetrieving(serializationArtefact, resourceCenter);

		String artefactName = resourceCenter.retrieveName(serializationArtefact);
		String baseName = artefactName.substring(0, artefactName.length() - FML_SUFFIX.length());

		returned.initName(baseName);

		VirtualModelInfo vpi = findVirtualModelInfo(returned, resourceCenter);

		if (vpi != null) {
			returned.setURI(vpi.uri);
			if (StringUtils.isNotEmpty(vpi.version)) {
				returned.setVersion(new FlexoVersion(vpi.version));
			}
			else {
				returned.setVersion(INITIAL_REVISION);
			}
			if (StringUtils.isNotEmpty(vpi.modelVersion)) {
				returned.setModelVersion(new FlexoVersion(vpi.modelVersion));
			}
			else {
				returned.setModelVersion(CURRENT_FML_VERSION);
			}
			try {
				((VirtualModelResourceImpl) returned).setUsedModelSlots(vpi.requiredModelSlotList);
			} catch (ClassNotFoundException e) {
				logger.warning("Could not find " + e.getMessage());
			}
			// We set a new factory because of required model slots
			if (StringUtils.isNotEmpty(vpi.requiredModelSlotList)) {
				returned.setFactory(makeModelFactory(returned, getTechnologyContextManager(resourceCenter.getServiceManager())));
			}
			if (StringUtils.isNotEmpty(vpi.virtualModelClassName)) {
				Class<? extends VirtualModel> virtualModelClass = null;
				try {
					virtualModelClass = (Class<? extends VirtualModel>) Class.forName(vpi.virtualModelClassName);
					returned.setSpecializedResourceDataClass(virtualModelClass);
				} catch (ClassNotFoundException e) {
					logger.warning("Cannot find class " + vpi.virtualModelClassName);
				}
			}
		}
		else {
			logger.warning("Cannot retrieve info from " + serializationArtefact);
			returned.setVersion(INITIAL_REVISION);
			returned.setModelVersion(CURRENT_FML_VERSION);
		}

		return returned;
	}

	@Override
	protected <I> FlexoIODelegate<I> makeFlexoIODelegate(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		return resourceCenter.makeDirectoryBasedFlexoIODelegate(serializationArtefact, FML_SUFFIX, FML_XML_SUFFIX, this);
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
	public <I> VirtualModelResource registerResource(VirtualModelResource resource, FlexoResourceCenter<I> resourceCenter) {
		return registerResource(resource, resourceCenter, true);

	}

	public <I> VirtualModelResource registerResource(VirtualModelResource resource, FlexoResourceCenter<I> resourceCenter,
			boolean exploreVirtualModels) {
		super.registerResource(resource, resourceCenter);

		// Register the resource in the VirtualModelRepository of supplied resource center
		registerResourceInResourceRepository(resource,
				getTechnologyAdapter(resourceCenter.getServiceManager()).getVirtualModelRepository(resourceCenter));

		// If VirtualModelLibrary not initialized yet, we will do it later in
		// VirtualModelLibrary.initialize() method
		if (resourceCenter.getServiceManager().getVirtualModelLibrary() != null) {
			resource.setVirtualModelLibrary(resourceCenter.getServiceManager().getVirtualModelLibrary());
			resourceCenter.getServiceManager().getVirtualModelLibrary().registerVirtualModel(resource);
		}

		if (exploreVirtualModels) {
			// Now look for virtual models
			exploreVirtualModels(resource);
		}

		return resource;

	}

	/**
	 * Internally called to explore contained {@link VirtualModel} in supplied {@link VirtualModelResource}
	 * 
	 * @param virtualModelResource
	 * @param technologyContextManager
	 */
	private <I> void exploreVirtualModels(VirtualModelResource virtualModelResource) {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) virtualModelResource.getResourceCenter();
		I directory = resourceCenter.getContainer((I) virtualModelResource.getIODelegate().getSerializationArtefact());
		if (directory != null) {
			exploreResource(directory, virtualModelResource);
		}
	}

	/**
	 * Internally called to explore contained {@link VirtualModel} in supplied {@link VirtualModelResource}
	 * 
	 * @param serializationArtefact
	 * @param virtualModelResource
	 * @param technologyContextManager
	 */
	private <I> void exploreResource(I serializationArtefact, VirtualModelResource virtualModelResource) {

		if (serializationArtefact == null) {
			return;
		}

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) virtualModelResource.getResourceCenter();

		for (I child : resourceCenter.getContents(serializationArtefact)) {
			String childName = resourceCenter.retrieveName(child);
			if (isValidArtefact(child, resourceCenter)) {
				I xmlFile = resourceCenter.getEntry(childName + ".xml", child);
				if (resourceCenter.exists(xmlFile)) {
					XMLRootElementInfo result = resourceCenter.getXMLRootElementInfo(xmlFile, true, "UseModelSlotDeclaration");
					if (result != null && (result.getName().equals("VirtualModel")
							|| StringUtils.isNotEmpty(result.getAttribute("virtualModelClass")))) {
						try {
							// Unused VirtualModelResource childVirtualModelResource =
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

	private static class VirtualModelInfo {
		public String uri;
		public String version;
		public String name;
		public String modelVersion;
		public String requiredModelSlotList;
		public String virtualModelClassName;

		VirtualModelInfo() {
		}

		VirtualModelInfo(String uri, String version, String name, String modelVersion, String requiredModelSlotList,
				String virtualModelClassName) {
			super();
			this.uri = uri;
			this.version = version;
			this.name = name;
			this.modelVersion = modelVersion;
			this.requiredModelSlotList = requiredModelSlotList;
			this.virtualModelClassName = virtualModelClassName;
		}
	}

	private static <I> VirtualModelInfo findVirtualModelInfo(VirtualModelResource resource, FlexoResourceCenter<I> resourceCenter) {

		if (resourceCenter instanceof FlexoProject) {
			resourceCenter = ((FlexoProject<I>) resourceCenter).getDelegateResourceCenter();
		}

		if (resourceCenter instanceof FileSystemBasedResourceCenter) {
			FileSystemMetaDataManager metaDataManager = ((FileSystemBasedResourceCenter) resourceCenter).getMetaDataManager();
			File file = (File) resource.getIODelegate().getSerializationArtefact();
			if (file.lastModified() < metaDataManager.metaDataLastModified(file)) {
				// OK, in this case the metadata file is there and more recent than .fml.xml file
				// Attempt to retrieve metadata from cache
				String uri = metaDataManager.getProperty("uri", file);
				String name = metaDataManager.getProperty("name", file);
				String version = metaDataManager.getProperty("version", file);
				String modelVersion = metaDataManager.getProperty("modelVersion", file);
				String requiredModelSlotList = metaDataManager.getProperty("requiredModelSlotList", file);
				String virtualModelClassName = metaDataManager.getProperty("virtualModelClassName", file);
				if (uri != null && name != null && version != null && modelVersion != null && requiredModelSlotList != null) {
					// Metadata are present, take it from cache
					return new VirtualModelInfo(uri, version, name, modelVersion, requiredModelSlotList, virtualModelClassName);
				}
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
		returned.modelVersion = xmlRootElementInfo.getAttribute("modelVersion");
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
			metaDataManager.setProperty("modelVersion", returned.modelVersion, file, false);
			metaDataManager.setProperty("requiredModelSlotList", returned.requiredModelSlotList, file, false);
			metaDataManager.setProperty("virtualModelClassName", returned.virtualModelClassName, file, false);

			metaDataManager.saveMetaDataProperties(file);
		}

		return returned;
	}

}
