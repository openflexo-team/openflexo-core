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

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.ViewLibrary;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.model.exceptions.ModelDefinitionException;
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
		AbstractVirtualModelInstanceResourceFactory<VirtualModelInstance, FMLRTTechnologyAdapter, FMLRTVirtualModelInstanceResource> {

	private static final Logger logger = Logger.getLogger(FMLRTVirtualModelInstanceResourceFactory.class.getPackage().getName());

	public static final FlexoVersion CURRENT_FML_RT_VERSION = new FlexoVersion("1.0");
	public static final String FML_RT_SUFFIX = ".fml.rt";
	public static final String XML_SUFFIX = ".fml.rt.xml";

	public FMLRTVirtualModelInstanceResourceFactory() throws ModelDefinitionException {
		super(FMLRTVirtualModelInstanceResource.class);
	}

	@Override
	public VirtualModelInstance makeEmptyResourceData(FMLRTVirtualModelInstanceResource resource) {
		return resource.getFactory().newInstance(VirtualModelInstance.class);
	}

	/**
	 * Build a new {@link FMLRTVirtualModelInstanceResource} with supplied baseName and URI, conform to supplied
	 * {@link VirtualModelResource} and located in supplied folder
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
			VirtualModelResource virtualModelResource, RepositoryFolder<FMLRTVirtualModelInstanceResource, I> folder,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = folder.getResourceRepository().getResourceCenter();
		I serializationArtefact = resourceCenter.createDirectory((baseName.endsWith(FML_RT_SUFFIX) ? baseName : baseName + FML_RT_SUFFIX),
				folder.getSerializationArtefact());

		FMLRTVirtualModelInstanceResource returned = initResourceForCreation(serializationArtefact, resourceCenter,
				technologyContextManager, baseName, uri);
		returned.setVirtualModelResource(virtualModelResource);
		registerResource(returned, resourceCenter, technologyContextManager);

		if (createEmptyContents) {
			createEmptyContents(returned);
			returned.save(null);
		}

		return returned;
	}

	/**
	 * Build a new {@link FMLRTVirtualModelInstanceResource} with supplied baseName and URI, conform to supplied
	 * {@link VirtualModelResource} and located in supplied container {@link AbstractVirtualModelInstanceResource}
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
			VirtualModelResource virtualModelResource, AbstractVirtualModelInstanceResource<?, ?> containerResource,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) containerResource.getResourceCenter();
		I parentDir = resourceCenter.getContainer((I) containerResource.getIODelegate().getSerializationArtefact());
		I serializationArtefact = resourceCenter.createDirectory((baseName.endsWith(FML_RT_SUFFIX) ? baseName : (baseName + FML_RT_SUFFIX)),
				parentDir);

		String viewURI = containerResource.getURI() + "/" + baseName
				+ (baseName.endsWith(FML_RT_SUFFIX) ? baseName : (baseName + FML_RT_SUFFIX));

		FMLRTVirtualModelInstanceResource returned = initResourceForCreation(serializationArtefact, resourceCenter,
				technologyContextManager, baseName, viewURI);
		returned.setVirtualModelResource(virtualModelResource);
		registerResource(returned, resourceCenter, technologyContextManager);

		if (createEmptyContents) {
			VirtualModelInstance resourceData = createEmptyContents(returned);
			returned.save(null);
			if (resourceData.getFMLRunTimeEngine() != null) {
				// TODO: today VirtualModelInstance is a RunTimeEvaluationContext
				// TODO: design issue, we should separate FlexoConceptInstance from RunTimeEvaluationContext
				// This inheritance should disappear
				resourceData.getFMLRunTimeEngine().addToExecutionContext(resourceData, resourceData);
			}
		}

		containerResource.addToContents(returned);
		containerResource.notifyContentsAdded(returned);
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
			FlexoResourceCenter<I> resourceCenter, TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager)
			throws ModelDefinitionException, IOException {
		FMLRTVirtualModelInstanceResource returned = retrieveResource(serializationArtefact, resourceCenter, technologyContextManager);
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
	public <I> FMLRTVirtualModelInstanceResource retrieveFMLRTVirtualModelInstanceResource(I serializationArtefact,
			FlexoResourceCenter<I> resourceCenter, TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager,
			AbstractVirtualModelInstanceResource<?, ?> containerResource) throws ModelDefinitionException, IOException {
		FMLRTVirtualModelInstanceResource returned = retrieveResource(serializationArtefact, resourceCenter, technologyContextManager);
		containerResource.addToContents(returned);
		containerResource.notifyContentsAdded(returned);
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
			/*final String baseName = candidateFile.getName().substring(0,
					candidateFile.getName().length() - ViewPointResource.VIEW_SUFFIX.length());
			final File xmlFile = new File(candidateFile, baseName + ".xml");
			return xmlFile.exists();*/

			return true;
		}
		return false;
	}

	/**
	 * Build and return model factory to use for resource data managing
	 */
	@Override
	public FMLRTVirtualModelInstanceModelFactory makeResourceDataFactory(FMLRTVirtualModelInstanceResource resource,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager) throws ModelDefinitionException {
		return new FMLRTVirtualModelInstanceModelFactory(resource,
				technologyContextManager.getTechnologyAdapter().getServiceManager().getEditingContext(),
				technologyContextManager.getTechnologyAdapter().getServiceManager().getTechnologyAdapterService());
	}

	@Override
	protected <I> FMLRTVirtualModelInstanceResource registerResource(FMLRTVirtualModelInstanceResource resource,
			FlexoResourceCenter<I> resourceCenter, TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager) {
		super.registerResource(resource, resourceCenter, technologyContextManager);

		ViewLibrary<I> viewLibrary = technologyContextManager.getTechnologyAdapter().getViewRepository(resourceCenter);

		// Sets the ViewLibrary
		// Register the resource in the FMLRTVirtualModelInstanceRepository of supplied resource center
		// resource.setViewLibrary(viewLibrary);
		registerResourceInResourceRepository(resource, viewLibrary);

		// TODO: refactor this
		if (resourceCenter instanceof FlexoProject) {
			registerResourceInResourceRepository(resource, ((FlexoProject) resourceCenter).getViewLibrary());
		}

		// Now look for virtual model instances and sub-views
		exploreViewContents(resource, technologyContextManager);

		return resource;
	}

	@Override
	protected <I> FMLRTVirtualModelInstanceResource initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager, String name, String uri)
			throws ModelDefinitionException {
		FMLRTVirtualModelInstanceResource returned = super.initResourceForCreation(serializationArtefact, resourceCenter,
				technologyContextManager, name, uri);
		returned.setVersion(INITIAL_REVISION);
		returned.setModelVersion(CURRENT_FML_RT_VERSION);
		return returned;
	}

	@Override
	protected <I> FMLRTVirtualModelInstanceResource initResourceForRetrieving(I serializationArtefact,
			FlexoResourceCenter<I> resourceCenter, TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager)
			throws ModelDefinitionException, IOException {

		FMLRTVirtualModelInstanceResource returned = super.initResourceForRetrieving(serializationArtefact, resourceCenter,
				technologyContextManager);

		String artefactName = resourceCenter.retrieveName(serializationArtefact);

		String baseName = artefactName;
		if (artefactName.endsWith(VIEW_SUFFIX)) {
			baseName = artefactName.substring(0, artefactName.length() - VIEW_SUFFIX.length());
		}

		returned.initName(baseName);

		ViewInfo vpi = findViewInfo(returned, resourceCenter);
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
				returned.setModelVersion(CURRENT_FML_RT_VERSION);
			}
			if (StringUtils.isNotEmpty(vpi.viewPointURI)) {
				ViewPointResource vpResource = resourceCenter.getServiceManager().getVirtualModelLibrary()
						.getViewPointResource(vpi.viewPointURI);
				returned.setViewPointResource(vpResource);
				returned.setVirtualModelResource(vpResource);
				if (vpResource == null) {
					// In this case, serialize URI of viewpoint, to give a chance to find it later
					returned.setViewpointURI(vpi.viewPointURI);
					logger.warning("Could not retrieve viewpoint: " + vpi.viewPointURI);
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
		return resourceCenter.makeDirectoryBasedFlexoIODelegate(serializationArtefact, VIEW_SUFFIX, CORE_FILE_SUFFIX, this);
	}

	private void exploreViewContents(ViewResource viewResource, TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager) {

		exploreResource(viewResource.getIODelegate().getSerializationArtefact(), viewResource, technologyContextManager);
	}

	private <I> void exploreResource(I serializationArtefact, ViewResource viewResource,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager) {
		if (serializationArtefact == null) {
			return;
		}

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) viewResource.getResourceCenter();

		for (I child : resourceCenter.getContents(resourceCenter.getContainer(serializationArtefact))) {
			if (getVirtualModelInstanceResourceFactory().isValidArtefact(child, resourceCenter)) {
				try {
					VirtualModelInstanceResource virtualModelInstanceResource = getVirtualModelInstanceResourceFactory()
							.retrieveVirtualModelInstanceResource(child, technologyContextManager, viewResource);
				} catch (ModelDefinitionException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if (isValidArtefact(child, resourceCenter, false)) { // We don't ignore subviews here !!!
				try {
					ViewResource subViewResource = retrieveViewResource(child, resourceCenter, technologyContextManager, viewResource);
				} catch (ModelDefinitionException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// recursively call
			// exploreResource(child, viewResource, technologyContextManager);
		}
	}

	private static class ViewInfo {
		public String viewPointURI;
		@SuppressWarnings("unused")
		public String viewPointVersion;
		public String name;
		public String uri;
		public String version;
		public String modelVersion;
	}

	private <I> ViewInfo findViewInfo(ViewResource resource, FlexoResourceCenter<I> resourceCenter) {

		ViewInfo returned = new ViewInfo();
		XMLRootElementInfo xmlRootElementInfo = resourceCenter
				.getXMLRootElementInfo((I) resource.getIODelegate().getSerializationArtefact());
		if (xmlRootElementInfo == null) {
			return null;
		}
		if (xmlRootElementInfo.getName().equals("View")) {
			returned.uri = xmlRootElementInfo.getAttribute("uri");
			returned.name = xmlRootElementInfo.getAttribute("name");
			returned.viewPointURI = xmlRootElementInfo.getAttribute("viewPointURI");
			returned.viewPointVersion = xmlRootElementInfo.getAttribute("viewPointVersion");
			returned.version = xmlRootElementInfo.getAttribute("version");
			returned.modelVersion = xmlRootElementInfo.getAttribute("modelVersion");

		}
		return returned;
	}

}
