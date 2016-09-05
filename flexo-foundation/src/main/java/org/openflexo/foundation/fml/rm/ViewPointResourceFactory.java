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

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPoint.ViewPointImpl;
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
 * Implementation of PamelaResourceFactory for {@link ViewPointResource}
 * 
 * @author sylvain
 *
 */
public class ViewPointResourceFactory extends AbstractVirtualModelResourceFactory<ViewPoint, ViewPointResource> {

	private static final Logger logger = Logger.getLogger(ViewPointResourceFactory.class.getPackage().getName());

	public static final String VIEWPOINT_SUFFIX = ".viewpoint";

	private final VirtualModelResourceFactory virtualModelResourceFactory;

	public ViewPointResourceFactory() throws ModelDefinitionException {
		super(ViewPointResource.class);
		virtualModelResourceFactory = new VirtualModelResourceFactory();
	}

	public VirtualModelResourceFactory getVirtualModelResourceFactory() {
		return virtualModelResourceFactory;
	}

	@Override
	public ViewPoint makeEmptyResourceData(ViewPointResource resource) {

		ViewPointImpl viewpoint = (ViewPointImpl) resource.getFactory().newInstance(ViewPoint.class);

		return viewpoint;
	}

	public <I> ViewPointResource makeViewPointResource(String baseName, String viewpointURI, RepositoryFolder<ViewPointResource, I> folder,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager, boolean createEmptyContents)
					throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = folder.getResourceRepository().getResourceCenter();
		I serializationArtefact = resourceCenter.createDirectory(baseName + VIEWPOINT_SUFFIX, folder.getSerializationArtefact());

		return makeResource(serializationArtefact, resourceCenter, technologyContextManager, viewpointURI, createEmptyContents);
	}

	/*public static ViewPoint newViewPoint(String baseName, String viewpointURI, File containerDir, ViewPointLibrary library,
			FlexoResourceCenter<?> resourceCenter) {
		ViewPointResource vpRes = ViewPointResourceImpl.makeViewPointResource(baseName, viewpointURI, containerDir, resourceCenter,
				library.getServiceManager());
		ViewPointImpl viewpoint = (ViewPointImpl) vpRes.getFactory().newInstance(ViewPoint.class);
		vpRes.setResourceData(viewpoint);
		viewpoint.setResource(vpRes);
		// And register it to the library
		library.registerViewPoint(vpRes);
		viewpoint.init(baseName, library);
		try {
			vpRes.save(null);
		} catch (SaveResourceException e) {
			e.printStackTrace();
		}
		return viewpoint;
	}*/

	@Override
	public <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {

		if (resourceCenter.exists(serializationArtefact) && resourceCenter.isDirectory(serializationArtefact)
				&& resourceCenter.canRead(serializationArtefact)
				&& (resourceCenter.retrieveName(serializationArtefact).endsWith(VIEWPOINT_SUFFIX)
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
	protected <I> ViewPointResource registerResource(ViewPointResource resource, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager) {
		super.registerResource(resource, resourceCenter, technologyContextManager);

		// Register the resource in the ViewPointRepository of supplied resource center
		registerResourceInResourceRepository(resource,
				technologyContextManager.getTechnologyAdapter().getViewPointRepository(resourceCenter));

		// If ViewPointLibrary not initialized yet, we will do it later in
		// ViewPointLibrary.initialize() method
		if (technologyContextManager.getServiceManager().getViewPointLibrary() != null) {
			resource.setViewPointLibrary(technologyContextManager.getServiceManager().getViewPointLibrary());
			technologyContextManager.getServiceManager().getViewPointLibrary().registerViewPoint(resource);
		}

		// Now look for virtual models
		exploreVirtualModels(resource, technologyContextManager);

		return resource;

	}

	@Override
	protected <I> ViewPointResource initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager, String uri) throws ModelDefinitionException {
		ViewPointResource returned = super.initResourceForCreation(serializationArtefact, resourceCenter, technologyContextManager, uri);

		returned.setVersion(INITIAL_REVISION);
		returned.setModelVersion(CURRENT_FML_VERSION);

		return returned;
	}

	@Override
	protected <I> ViewPointResource initResourceForRetrieving(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager) throws ModelDefinitionException {

		ViewPointResource returned = super.initResourceForRetrieving(serializationArtefact, resourceCenter, technologyContextManager);

		String artefactName = resourceCenter.retrieveName(serializationArtefact);
		String baseName = artefactName.substring(0, artefactName.length() - VIEWPOINT_SUFFIX.length());

		returned.initName(baseName);

		ViewPointInfo vpi = findViewPointInfo(returned, resourceCenter);
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
		return resourceCenter.makeDirectoryBasedFlexoIODelegate(serializationArtefact, VIEWPOINT_SUFFIX, CORE_FILE_SUFFIX, this);
	}

	private <I> void exploreVirtualModels(ViewPointResource viewPointResource,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager) {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) viewPointResource.getResourceCenter();
		I directory = resourceCenter.getContainer((I) viewPointResource.getFlexoIODelegate().getSerializationArtefact());

		exploreResource(directory, viewPointResource, technologyContextManager);
	}

	private <I> void exploreResource(I serializationArtefact, ViewPointResource viewPointResource,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager) {

		if (serializationArtefact == null) {
			return;
		}

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) viewPointResource.getResourceCenter();

		for (I child : resourceCenter.getContents(serializationArtefact)) {
			String childName = resourceCenter.retrieveName(child);
			if (childName.endsWith(".xml")) {
				XMLRootElementInfo result = resourceCenter.getXMLRootElementInfo(child);
				if (result != null && result.getName().equals("VirtualModel")) {
					VirtualModelResource virtualModelResource;
					try {
						virtualModelResource = getVirtualModelResourceFactory().retrieveVirtualModelResource(serializationArtefact,
								technologyContextManager, viewPointResource);
					} catch (ModelDefinitionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			/*try {
				if (resourceCenter.retrieveName(child).endsWith(".xml")) {
					// TODO refactor this
					XMLRootElementInfo result = null;
					if (child instanceof File) {
						result = reader.readRootElement((File) child);
					}
					if (result != null && result.getName().equals("VirtualModel")) {
						VirtualModelResource virtualModelResource;
						try {
							virtualModelResource = getVirtualModelResourceFactory().retrieveVirtualModelResource(serializationArtefact,
									technologyContextManager, viewPointResource);
						} catch (ModelDefinitionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			} catch (IOException e) {
				logger.warning("Unexpected IOException while reading " + child);
				e.printStackTrace();
			}*/

			// recursively call
			exploreResource(child, viewPointResource, technologyContextManager);
		}
	}

	private static class ViewPointInfo {
		public String uri;
		public String version;
		public String name;
		public String modelVersion;
	}

	private <I> ViewPointInfo findViewPointInfo(ViewPointResource resource, FlexoResourceCenter<I> resourceCenter) {

		ViewPointInfo returned = new ViewPointInfo();
		XMLRootElementInfo xmlRootElementInfo = resourceCenter
				.getXMLRootElementInfo((I) resource.getFlexoIODelegate().getSerializationArtefact());
		if (xmlRootElementInfo.getName().equals("ViewPoint")) {
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

		/*if (serializationArtefact instanceof File) {
			try {
				File viewpointDirectory = (File) serializationArtefact;
				logger.fine("Try to find infos for " + viewpointDirectory);
		
				String baseName = viewpointDirectory.getName().substring(0,
						viewpointDirectory.getName().length() - VIEWPOINT_SUFFIX.length());
				File xmlFile = new File(viewpointDirectory, baseName + ".xml");
		
				if (xmlFile.exists()) {
					document = XMLUtils.readXMLFile(xmlFile);
					Element root = XMLUtils.getElement(document, "ViewPoint");
					if (root != null) {
						ViewPointInfo returned = new ViewPointInfo();
						Iterator<Attribute> it = root.getAttributes().iterator();
						while (it.hasNext()) {
							Attribute at = it.next();
							if (at.getName().equals("uri")) {
								logger.fine("Returned " + at.getValue());
								returned.uri = at.getValue();
							}
							else if (at.getName().equals("name")) {
								logger.fine("Returned " + at.getValue());
								returned.name = at.getValue();
							}
							else if (at.getName().equals("version")) {
								logger.fine("Returned " + at.getValue());
								returned.version = at.getValue();
							}
							else if (at.getName().equals("modelVersion")) {
								logger.fine("Returned " + at.getValue());
								returned.modelVersion = at.getValue();
							}
						}
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
						return returned;
		
					}
				}
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.fine("Returned null");
		return null;*/
	}

	/*public static ViewPointResource makeViewPointResource(String name, String uri, File containerDir, FlexoResourceCenter<?> resourceCenter,
			FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class, ViewPointResource.class));
			ViewPointResourceImpl returned = (ViewPointResourceImpl) factory.newInstance(ViewPointResource.class);
			returned.initName(name);
			returned.setURI(uri);
			returned.setVersion(new FlexoVersion("0.1"));
			returned.setModelVersion(new FlexoVersion("1.0"));
	
			returned.setFlexoIODelegate(DirectoryBasedFlexoIODelegateImpl.makeDirectoryBasedFlexoIODelegate(containerDir, VIEWPOINT_SUFFIX,
					CORE_FILE_SUFFIX, returned, factory));
	
			// If ViewPointLibrary not initialized yet, we will do it later in
			// ViewPointLibrary.initialize() method
			if (serviceManager.getViewPointLibrary() != null) {
				returned.setViewPointLibrary(serviceManager.getViewPointLibrary());
				serviceManager.getViewPointLibrary().registerViewPoint(returned);
			}
	
			returned.setResourceCenter(resourceCenter);
			returned.setServiceManager(serviceManager);
			returned.setFactory(new FMLModelFactory(returned, serviceManager));
	
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}*/

	/*public static ViewPointResource makeGitViewPointResource(String name, String uri, File workTree, FlexoResourceCenter<?> resourceCenter,
			FlexoServiceManager serviceManager) throws IOException {
		try {
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(FlexoIOGitDelegate.class, ViewPointResource.class));
			ViewPointResourceImpl returned = (ViewPointResourceImpl) factory.newInstance(ViewPointResource.class);
			returned.initName(name);
			returned.setURI(uri);
			returned.setVersion(new FlexoVersion("0.1"));
			returned.setModelVersion(new FlexoVersion("1.0"));
	
			GitResourceCenter gitResourceCenter = (GitResourceCenter) resourceCenter;
	
			// Set the Git IO Flexo Delegate
			// returned.setFlexoIODelegate(FlexoIOGitDelegateImpl.makeFlexoIOGitDelegate(name,factory,workTree,
			// gitResourceCenter.getGitRepository()));
			returned.setFlexoIODelegate(gitResourceCenter.getGitIODelegateFactory().makeNewInstance(returned));
	
			returned.setResourceCenter(resourceCenter);
	
			returned.getFlexoIODelegate().save(returned);
			// If ViewPointLibrary not initialized yet, we will do it later in
			// ViewPointLibrary.initialize() method
			if (serviceManager.getViewPointLibrary() != null) {
				returned.setViewPointLibrary(serviceManager.getViewPointLibrary());
				serviceManager.getViewPointLibrary().registerViewPoint(returned);
			}
	
			returned.setServiceManager(serviceManager);
			returned.setFactory(new FMLModelFactory(returned, serviceManager));
	
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		} catch (NotImplementedException e) {
			e.printStackTrace();
		}
		return null;
	}*/

	/*public static ViewPointResource retrieveViewPointResource(InJarResourceImpl inJarResource, FlexoResourceCenter<?> resourceCenter,
			FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(InJarFlexoIODelegate.class, ViewPointResource.class));
			ViewPointResourceImpl returned = (ViewPointResourceImpl) factory.newInstance(ViewPointResource.class);
	
			returned.setFlexoIODelegate(InJarFlexoIODelegateImpl.makeInJarFlexoIODelegate(inJarResource, factory));
	
			// String parentPath =
			// FilenameUtils.getFullPath(inJarResource.getRelativePath());
			// BasicResourceImpl parent = (BasicResourceImpl)
			// ((ClasspathResourceLocatorImpl)(inJarResource.getLocator())).getJarResourcesList().get(parentPath);
	
			ViewPointInfo vpi = findViewPointInfo(returned.getFlexoIOStreamDelegate().getInputStream());
			if (vpi == null) {
				// Unable to retrieve infos, just abort
				return null;
			}
	
			// returned.setDirectory(parent);
			returned.setURI(vpi.uri);
			returned.initName(vpi.name);
			if (StringUtils.isNotEmpty(vpi.version)) {
				returned.setVersion(new FlexoVersion(vpi.version));
			}
	
			if (StringUtils.isEmpty(vpi.modelVersion)) {
				returned.setModelVersion(new FlexoVersion("0.1"));
			}
			else {
				returned.setModelVersion(new FlexoVersion(vpi.modelVersion));
			}
	
			returned.setFactory(new FMLModelFactory(returned, serviceManager));
	
			// If ViewPointLibrary not initialized yet, we will do it later in
			// ViewPointLibrary.initialize() method
			if (serviceManager.getViewPointLibrary() != null) {
				returned.setViewPointLibrary(serviceManager.getViewPointLibrary());
				serviceManager.getViewPointLibrary().registerViewPoint(returned);
			}
	
			returned.setResourceCenter(resourceCenter);
			returned.setServiceManager(serviceManager);
	
			// Now look for virtual models
			returned.exploreVirtualModels(returned.getDirectory());
	
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}*/

	/*public static ViewPointResource retrieveViewPointResource(File viewPointDirectory, FlexoResourceCenter<?> resourceCenter,
			FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class, ViewPointResource.class));
			ViewPointResourceImpl returned = (ViewPointResourceImpl) factory.newInstance(ViewPointResource.class);
			String baseName = viewPointDirectory.getName().substring(0, viewPointDirectory.getName().length() - VIEWPOINT_SUFFIX.length());
			File xmlFile = new File(viewPointDirectory, baseName + CORE_FILE_SUFFIX);
			ViewPointInfo vpi = null;
			try {
				vpi = findViewPointInfo(new FileInputStream(xmlFile));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (vpi == null) {
				// Unable to retrieve infos, just abort
				return null;
			}
	
			returned.setURI(vpi.uri);
			returned.initName(baseName);
	
			returned.setFlexoIODelegate(DirectoryBasedFlexoIODelegateImpl.makeDirectoryBasedFlexoIODelegate(
					viewPointDirectory.getParentFile(), VIEWPOINT_SUFFIX, CORE_FILE_SUFFIX, returned, factory));
	
			if (StringUtils.isNotEmpty(vpi.version)) {
				returned.setVersion(new FlexoVersion(vpi.version));
			}
			if (StringUtils.isEmpty(vpi.modelVersion)) {
				returned.setModelVersion(new FlexoVersion("0.1"));
			}
			else {
				returned.setModelVersion(new FlexoVersion(vpi.modelVersion));
			}
	
			returned.setFactory(new FMLModelFactory(returned, serviceManager));
	
			// If ViewPointLibrary not initialized yet, we will do it later in
			// ViewPointLibrary.initialize() method
			if (serviceManager.getViewPointLibrary() != null) {
				returned.setViewPointLibrary(serviceManager.getViewPointLibrary());
				serviceManager.getViewPointLibrary().registerViewPoint(returned);
			}
	
			returned.setResourceCenter(resourceCenter);
			returned.setServiceManager(serviceManager);
	
			logger.fine("ViewPointResource " + xmlFile.getAbsolutePath() + " version " + returned.getModelVersion());
	
			// Now look for virtual models
			returned.exploreVirtualModels(returned.getDirectory());
	
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}*/

}
