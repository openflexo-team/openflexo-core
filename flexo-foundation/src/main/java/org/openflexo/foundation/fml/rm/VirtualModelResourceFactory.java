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

import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.PamelaResourceFactory;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.xml.XMLRootElementInfo;

/**
 * Implementation of {@link PamelaResourceFactory} for {@link VirtualModelResource}<br>
 * 
 * This factory is responsible to create or retrieve {@link VirtualModel} objects
 * 
 * @author sylvain
 *
 */
public class VirtualModelResourceFactory
		extends PamelaResourceFactory<VirtualModelResource, VirtualModel, FMLTechnologyAdapter, FMLModelFactory> {

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
	public FMLModelFactory makeResourceDataFactory(VirtualModelResource resource,
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
			RepositoryFolder<VirtualModelResource, I> folder, TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager,
			boolean createEmptyContents) throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = folder.getResourceRepository().getResourceCenter();
		I serializationArtefact = resourceCenter.createDirectory(baseName.endsWith(FML_SUFFIX) ? baseName : baseName + FML_SUFFIX,
				folder.getSerializationArtefact());

		return makeResource(serializationArtefact, resourceCenter, technologyContextManager, baseName, virtualModelURI,
				createEmptyContents);
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
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) containerVirtualModelResource.getResourceCenter();
		I serializationArtefact = resourceCenter.createDirectory(baseName.endsWith(FML_SUFFIX) ? baseName : baseName + FML_SUFFIX,
				resourceCenter.getContainer((I) containerVirtualModelResource.getIODelegate().getSerializationArtefact()));

		VirtualModelResource returned = initResourceForCreation(serializationArtefact, resourceCenter, technologyContextManager, baseName,
				containerVirtualModelResource.getURI() + "/" + baseName);

		containerVirtualModelResource.addToContents(returned);

		registerResource(returned, resourceCenter, technologyContextManager);

		if (createEmptyContents) {
			createEmptyContents(returned);
			returned.save(null);
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
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager, VirtualModelResource containerVirtualModelResource)
			throws ModelDefinitionException, IOException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) containerVirtualModelResource.getResourceCenter();
		String name = resourceCenter.retrieveName(serializationArtefact);

		VirtualModelResource returned = initResourceForRetrieving(serializationArtefact, resourceCenter, technologyContextManager);
		returned.setURI(containerVirtualModelResource.getURI() + "/" + name);

		containerVirtualModelResource.addToContents(returned);
		containerVirtualModelResource.notifyContentsAdded(returned);

		registerResource(returned, resourceCenter, technologyContextManager);

		return returned;
	}

	@Override
	protected <I> VirtualModelResource initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager, String name, String uri)
			throws ModelDefinitionException {
		VirtualModelResource returned = super.initResourceForCreation(serializationArtefact, resourceCenter, technologyContextManager, name,
				uri);

		returned.setVersion(INITIAL_REVISION);
		returned.setModelVersion(CURRENT_FML_VERSION);

		return returned;
	}

	@Override
	protected <I> VirtualModelResource initResourceForRetrieving(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager) throws ModelDefinitionException, IOException {

		VirtualModelResource returned = super.initResourceForRetrieving(serializationArtefact, resourceCenter, technologyContextManager);

		String artefactName = resourceCenter.retrieveName(serializationArtefact);
		String baseName = artefactName.substring(0, artefactName.length() - FML_SUFFIX.length());

		returned.initName(baseName);

		VirtualModelInfo vpi = findViewPointInfo(returned, resourceCenter);
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
	protected <I> VirtualModelResource registerResource(VirtualModelResource resource, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager) {
		super.registerResource(resource, resourceCenter, technologyContextManager);

		// Register the resource in the VirtualModelRepository of supplied resource center
		registerResourceInResourceRepository(resource,
				technologyContextManager.getTechnologyAdapter().getVirtualModelRepository(resourceCenter));

		// If VirtualModelLibrary not initialized yet, we will do it later in
		// VirtualModelLibrary.initialize() method
		if (technologyContextManager.getServiceManager().getVirtualModelLibrary() != null) {
			resource.setVirtualModelLibrary(technologyContextManager.getServiceManager().getVirtualModelLibrary());
			technologyContextManager.getServiceManager().getVirtualModelLibrary().registerVirtualModel(resource);
		}

		System.out.println("On vient d'enregistrer la resource " + resource);
		System.out.println("On regarde maintenant dedans");
		System.out.println("URI: " + resource.getURI());

		// Now look for virtual models
		exploreVirtualModels(resource, technologyContextManager);

		return resource;

	}

	/**
	 * Internally called to explore contained {@link VirtualModel} in supplied {@link VirtualModelResource}
	 * 
	 * @param virtualModelResource
	 * @param technologyContextManager
	 */
	private <I> void exploreVirtualModels(VirtualModelResource virtualModelResource,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager) {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) virtualModelResource.getResourceCenter();
		I directory = resourceCenter.getContainer((I) virtualModelResource.getIODelegate().getSerializationArtefact());

		exploreResource(directory, virtualModelResource, technologyContextManager);
	}

	/**
	 * Internally called to explore contained {@link VirtualModel} in supplied {@link VirtualModelResource}
	 * 
	 * @param serializationArtefact
	 * @param virtualModelResource
	 * @param technologyContextManager
	 */
	private <I> void exploreResource(I serializationArtefact, VirtualModelResource virtualModelResource,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager) {

		if (serializationArtefact == null) {
			return;
		}

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) virtualModelResource.getResourceCenter();

		for (I child : resourceCenter.getContents(serializationArtefact)) {
			String childName = resourceCenter.retrieveName(child);
			if (isValidArtefact(child, resourceCenter)) {
				I xmlFile = resourceCenter.createEntry(childName + ".xml", child);
				if (resourceCenter.exists(xmlFile)) {
					XMLRootElementInfo result = resourceCenter.getXMLRootElementInfo(xmlFile);
					if (result != null && result.getName().equals("VirtualModel")) {
						try {
							VirtualModelResource childVirtualModelResource = retrieveContainedVirtualModelResource(child,
									technologyContextManager, virtualModelResource);
						} catch (ModelDefinitionException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

			// recursively call
			exploreResource(child, virtualModelResource, technologyContextManager);
		}
	}

	private static class VirtualModelInfo {
		public String uri;
		public String version;
		public String name;
		public String modelVersion;
	}

	private <I> VirtualModelInfo findViewPointInfo(VirtualModelResource resource, FlexoResourceCenter<I> resourceCenter) {

		VirtualModelInfo returned = new VirtualModelInfo();
		XMLRootElementInfo xmlRootElementInfo = resourceCenter
				.getXMLRootElementInfo((I) resource.getIODelegate().getSerializationArtefact());
		if (xmlRootElementInfo == null) {
			return null;
		}
		if (xmlRootElementInfo.getName().equals("VirtualModel")) {
			returned.uri = xmlRootElementInfo.getAttribute("uri");
			returned.name = xmlRootElementInfo.getAttribute("name");
			returned.version = xmlRootElementInfo.getAttribute("version");
			returned.modelVersion = xmlRootElementInfo.getAttribute("modelVersion");

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

		}
		return returned;
	}

}
