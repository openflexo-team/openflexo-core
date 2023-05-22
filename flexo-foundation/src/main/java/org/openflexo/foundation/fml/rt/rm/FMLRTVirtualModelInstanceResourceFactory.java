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

package org.openflexo.foundation.fml.rt.rm;

import java.io.IOException;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.CompilationUnitRepository;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelFactory;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.xml.XMLRootElementInfo;

/**
 * The resource factory for {@link FMLRTVirtualModelInstanceResource}
 * 
 * @author sylvain
 *
 */
public class FMLRTVirtualModelInstanceResourceFactory extends
		AbstractVirtualModelInstanceResourceFactory<FMLRTVirtualModelInstance, FMLRTTechnologyAdapter, FMLRTVirtualModelInstanceResource> {

	private static final Logger logger = Logger.getLogger(FMLRTVirtualModelInstanceResourceFactory.class.getPackage().getName());

	public static final FlexoVersion CURRENT_FML_RT_VERSION = new FlexoVersion("1.0");
	public static final String FML_RT_SUFFIX = ".fml.rt";
	public static final String XML_SUFFIX = ".fml.rt.xml";

	public FMLRTVirtualModelInstanceResourceFactory() throws ModelDefinitionException {
		super(FMLRTVirtualModelInstanceResource.class);
	}

	@Override
	public FMLRTVirtualModelInstance makeEmptyResourceData(FMLRTVirtualModelInstanceResource resource) {
		return resource.getFactory().newInstance(FMLRTVirtualModelInstance.class);
	}

	/**
	 * Build a new {@link FMLRTVirtualModelInstanceResource} with supplied baseName and URI, conform to supplied
	 * {@link CompilationUnitResource} and located in supplied folder
	 * 
	 * @param baseName
	 * @param uri
	 * @param virtualModelResource
	 * @param folder
	 * @param technologyContextManager
	 * @param createEmptyContents
	 * @return
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 */
	public <I> FMLRTVirtualModelInstanceResource makeTopLevelFMLRTVirtualModelInstanceResource(String baseName, String uri,
			CompilationUnitResource virtualModelResource, RepositoryFolder<FMLRTVirtualModelInstanceResource, I> folder,
			boolean createEmptyContents) throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = folder.getResourceRepository().getResourceCenter();
		I serializationArtefact = resourceCenter.createDirectory((baseName.endsWith(FML_RT_SUFFIX) ? baseName : baseName + FML_RT_SUFFIX),
				folder.getSerializationArtefact());

		FMLRTVirtualModelInstanceResource returned = initResourceForCreation(serializationArtefact, resourceCenter, baseName, uri);
		returned.setVirtualModelResource(virtualModelResource);
		registerResource(returned, resourceCenter);

		if (createEmptyContents) {
			FMLRTVirtualModelInstance resourceData = createEmptyContents(returned);
			resourceData.setVirtualModel(virtualModelResource.getCompilationUnit().getVirtualModel());
			returned.save();
			if (resourceData.getFMLRunTimeEngine() != null) {
				// TODO: today FMLRTVirtualModelInstance is a RunTimeEvaluationContext
				// TODO: design issue, we should separate FlexoConceptInstance from RunTimeEvaluationContext
				// This inheritance should disappear
				resourceData.getFMLRunTimeEngine().addToExecutionContext(resourceData, resourceData);
			}
		}

		return returned;
	}

	/**
	 * Build a new {@link FMLRTVirtualModelInstanceResource} with supplied baseName and URI, conform to supplied
	 * {@link CompilationUnitResource} and located in supplied container {@link AbstractVirtualModelInstanceResource}
	 * 
	 * @param baseName
	 * @param virtualModelResource
	 * @param containerResource
	 * @param technologyContextManager
	 * @param createEmptyContents
	 * @return
	 * @throws SaveResourceException
	 * @throws ModelDefinitionException
	 */
	public <I> FMLRTVirtualModelInstanceResource makeContainedFMLRTVirtualModelInstanceResource(String baseName,
			CompilationUnitResource virtualModelResource, AbstractVirtualModelInstanceResource<?, ?> containerResource,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) containerResource.getResourceCenter();
		I parentDir = resourceCenter.getContainer((I) containerResource.getIODelegate().getSerializationArtefact());
		I serializationArtefact = resourceCenter.createDirectory((baseName.endsWith(FML_RT_SUFFIX) ? baseName : (baseName + FML_RT_SUFFIX)),
				parentDir);

		String viewURI = containerResource.getURI() + "/" + (baseName.endsWith(FML_RT_SUFFIX) ? baseName : (baseName + FML_RT_SUFFIX));

		FMLRTVirtualModelInstanceResource returned = initResourceForCreation(serializationArtefact, resourceCenter, baseName, viewURI);
		returned.setVirtualModelResource(virtualModelResource);
		registerResource(returned, resourceCenter);

		FMLRTVirtualModelInstance resourceData = null;

		if (createEmptyContents) {
			resourceData = createEmptyContents(returned);
			resourceData.setVirtualModel(virtualModelResource.getCompilationUnit().getVirtualModel());
			returned.save();
		}

		containerResource.addToContents(returned);
		containerResource.notifyContentsAdded(returned);

		if (createEmptyContents && resourceData.getFMLRunTimeEngine() != null) {
			// TODO: today FMLRTVirtualModelInstance is a RunTimeEvaluationContext
			// TODO: design issue, we should separate FlexoConceptInstance from RunTimeEvaluationContext
			// This inheritance should disappear
			resourceData.getFMLRunTimeEngine().addToExecutionContext(resourceData, resourceData);
		}

		return returned;
	}

	/**
	 * Used to retrieve from serialization artefact a top-level {@link FMLRTVirtualModelInstanceResource}
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @param technologyContextManager
	 * @return
	 * @throws ModelDefinitionException
	 * @throws IOException
	 */
	public <I> FMLRTVirtualModelInstanceResource retrieveFMLRTVirtualModelInstanceResource(I serializationArtefact,
			FlexoResourceCenter<I> resourceCenter) throws ModelDefinitionException, IOException {
		FMLRTVirtualModelInstanceResource returned = retrieveResource(serializationArtefact, resourceCenter);
		return returned;
	}

	/**
	 * Used to retrieve from serialization artefact a contained {@link FMLRTVirtualModelInstanceResource} in supplied
	 * containerVirtualModelResource
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @param technologyContextManager
	 * @param containerResource
	 * @return
	 * @throws ModelDefinitionException
	 * @throws IOException
	 */
	public <I> FMLRTVirtualModelInstanceResource retrieveContainedFMLRTVirtualModelInstanceResource(I serializationArtefact,
			AbstractVirtualModelInstanceResource<?, ?> containerResource) throws ModelDefinitionException, IOException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) containerResource.getResourceCenter();
		String name = resourceCenter.retrieveName(serializationArtefact);

		FMLRTVirtualModelInstanceResource returned = initResourceForRetrieving(serializationArtefact, resourceCenter);
		returned.setURI(containerResource.getURI() + "/" + name);

		containerResource.addToContents(returned);
		containerResource.notifyContentsAdded(returned);

		registerResource(returned, resourceCenter);

		return returned;

	}

	/**
	 * Return boolean indicating is supplied serialization artefact seems to be a valid artefact encoding a
	 * {@link FMLRTVirtualModelInstance}<br>
	 * A valid {@link FMLRTVirtualModelInstance} is encoded in a directory ending with .fml.rt suffix
	 * 
	 * @param serializationArtefact
	 * @param resourceCenter
	 * @return
	 */
	@Override
	public <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {

		if (resourceCenter.exists(serializationArtefact) && resourceCenter.isDirectory(serializationArtefact)
				&& resourceCenter.canRead(serializationArtefact)
				&& resourceCenter.retrieveName(serializationArtefact).endsWith(FML_RT_SUFFIX)) {
			return true;
		}
		return false;
	}

	/**
	 * Build and return model factory to use for resource data managing
	 */
	@Override
	public FMLRTVirtualModelInstanceModelFactory makeModelFactory(FMLRTVirtualModelInstanceResource resource,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager) throws ModelDefinitionException {
		return new FMLRTVirtualModelInstanceModelFactory(resource,
				technologyContextManager.getTechnologyAdapter().getServiceManager().getEditingContext(),
				technologyContextManager.getTechnologyAdapter().getServiceManager().getTechnologyAdapterService());
	}

	@Override
	public <I> FMLRTVirtualModelInstanceResource registerResource(FMLRTVirtualModelInstanceResource resource,
			FlexoResourceCenter<I> resourceCenter) {
		super.registerResource(resource, resourceCenter);

		// Register the resource in the VirtualModelInstanceRepository of supplied resource center
		registerResourceInResourceRepository(resource,
				getTechnologyAdapter(resourceCenter.getServiceManager()).getVirtualModelInstanceRepository(resourceCenter));

		// Now look for virtual model instances and sub-views
		exploreViewContents(resource);

		// We lookup here resources with a null container which are defined inside a .fml directory
		// This means that VirtualModelInstance inside are declared as ContainedVMI of the corresponding VirtualModel
		if (resource.getContainer() == null) {
			RepositoryFolder<FlexoResource<?>, I> parentFolder = resourceCenter.getParentFolder(resource);
			if (parentFolder.getName().endsWith(CompilationUnitResourceFactory.FML_SUFFIX)) {
				FMLTechnologyAdapter fmlTA = resource.getServiceManager().getTechnologyAdapterService()
						.getTechnologyAdapter(FMLTechnologyAdapter.class);
				CompilationUnitRepository<I> virtualModelRepository = fmlTA.getVirtualModelRepository(resourceCenter);
				for (CompilationUnitResource virtualModelResource : virtualModelRepository.getAllResources()) {
					I serializationArtefact = (I) virtualModelResource.getIODelegate().getSerializationArtefact();
					I parentSerializationArtefact = resourceCenter.getContainer(serializationArtefact);
					if (parentSerializationArtefact.equals(parentFolder.getSerializationArtefact())) {
						virtualModelResource.addToContainedVMI(resource);
					}
				}
			}
		}

		return resource;
	}

	@Override
	protected <I> FMLRTVirtualModelInstanceResource initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			String name, String uri) throws ModelDefinitionException {
		FMLRTVirtualModelInstanceResource returned = super.initResourceForCreation(serializationArtefact, resourceCenter, name, uri);
		returned.setVersion(INITIAL_REVISION);
		returned.setModelVersion(CURRENT_FML_RT_VERSION);
		return returned;
	}

	@Override
	protected <I> FMLRTVirtualModelInstanceResource initResourceForRetrieving(I serializationArtefact,
			FlexoResourceCenter<I> resourceCenter) throws ModelDefinitionException, IOException {

		FMLRTVirtualModelInstanceResource returned = super.initResourceForRetrieving(serializationArtefact, resourceCenter);

		String artefactName = resourceCenter.retrieveName(serializationArtefact);

		String baseName = artefactName;
		if (artefactName.endsWith(FML_RT_SUFFIX)) {
			baseName = artefactName.substring(0, artefactName.length() - FML_RT_SUFFIX.length());
		}

		returned.initName(baseName);

		VirtualModelInstanceInfo vmiInfo = findVirtualModelInstanceInfo(returned, resourceCenter);
		if (vmiInfo != null) {
			returned.setURI(vmiInfo.uri);
			if (StringUtils.isNotEmpty(vmiInfo.version)) {
				returned.setVersion(new FlexoVersion(vmiInfo.version));
			}
			else {
				returned.setVersion(INITIAL_REVISION);
			}
			if (StringUtils.isNotEmpty(vmiInfo.modelVersion)) {
				returned.setModelVersion(new FlexoVersion(vmiInfo.modelVersion));
			}
			else {
				returned.setModelVersion(CURRENT_FML_RT_VERSION);
			}
			if (StringUtils.isNotEmpty(vmiInfo.virtualModelURI)) {
				CompilationUnitResource vmResource = resourceCenter.getServiceManager().getVirtualModelLibrary()
						.getCompilationUnitResource(vmiInfo.virtualModelURI);
				returned.setVirtualModelResource(vmResource);
				if (vmResource == null) {
					// In this case, serialize URI of virtual model, to give a chance to find it later
					returned.setVirtualModelURI(vmiInfo.virtualModelURI);
					logger.warning("Could not retrieve virtual model: " + vmiInfo.virtualModelURI);
				}
			}
		}
		else {
			// Unable to retrieve infos, just abort
			logger.warning("Cannot retrieve info from " + serializationArtefact);
			returned.setVersion(INITIAL_REVISION);
			returned.setModelVersion(CURRENT_FML_RT_VERSION);
		}

		return returned;

	}

	@Override
	protected <I> FlexoIODelegate<I> makeFlexoIODelegate(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		return resourceCenter.makeDirectoryBasedFlexoIODelegate(serializationArtefact, FML_RT_SUFFIX, XML_SUFFIX, this);
	}

	private void exploreViewContents(FMLRTVirtualModelInstanceResource viewResource) {

		exploreResource(viewResource.getIODelegate().getSerializationArtefact(), viewResource);
	}

	private <I> void exploreResource(I serializationArtefact, FMLRTVirtualModelInstanceResource containerResource) {
		if (serializationArtefact == null) {
			return;
		}

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) containerResource.getResourceCenter();

		for (I child : resourceCenter.getContents(resourceCenter.getContainer(serializationArtefact))) {
			if (isValidArtefact(child, resourceCenter)) {
				try {
					// Unused FMLRTVirtualModelInstanceResource virtualModelInstanceResource =
					retrieveContainedFMLRTVirtualModelInstanceResource(child, containerResource);
				} catch (ModelDefinitionException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static class VirtualModelInstanceInfo {
		public String virtualModelURI;
		@SuppressWarnings("unused")
		public String virtualModelVersion;
		// Unused public String name;
		public String uri;
		public String version;
		public String modelVersion;
	}

	private static <I> VirtualModelInstanceInfo findVirtualModelInstanceInfo(FMLRTVirtualModelInstanceResource resource,
			FlexoResourceCenter<I> resourceCenter) {

		VirtualModelInstanceInfo returned = new VirtualModelInstanceInfo();
		XMLRootElementInfo xmlRootElementInfo = resourceCenter
				.getXMLRootElementInfo((I) resource.getIODelegate().getSerializationArtefact());
		if (xmlRootElementInfo == null) {
			return null;
		}

		if (xmlRootElementInfo.getName().equals("FMLRTVirtualModelInstance")) {
			// Unused returned.name = xmlRootElementInfo.getAttribute("name");
			returned.uri = xmlRootElementInfo.getAttribute("uri");
			returned.virtualModelURI = xmlRootElementInfo.getAttribute("virtualModelURI");
			returned.virtualModelVersion = xmlRootElementInfo.getAttribute("virtualModelVersion");
			returned.version = xmlRootElementInfo.getAttribute("version");
			returned.modelVersion = xmlRootElementInfo.getAttribute("modelVersion");
		}
		return returned;
	}

}
