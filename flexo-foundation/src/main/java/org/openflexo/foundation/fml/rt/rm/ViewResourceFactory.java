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

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.ViewModelFactory;
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
 * Implementation of PamelaResourceFactory for {@link ViewResource}
 * 
 * @author sylvain
 *
 */
public class ViewResourceFactory extends AbstractVirtualModelInstanceResourceFactory<View, ViewPoint, ViewResource> {

	private static final Logger logger = Logger.getLogger(ViewResourceFactory.class.getPackage().getName());

	public static final String VIEW_SUFFIX = ".view";

	private final VirtualModelInstanceResourceFactory virtualModelInstanceResourceFactory;

	public ViewResourceFactory() throws ModelDefinitionException {
		super(ViewResource.class);
		virtualModelInstanceResourceFactory = new VirtualModelInstanceResourceFactory();
	}

	public VirtualModelInstanceResourceFactory getVirtualModelInstanceResourceFactory() {
		return virtualModelInstanceResourceFactory;
	}

	@Override
	public View makeEmptyResourceData(ViewResource resource) {
		return resource.getFactory().newInstance(View.class);
	}

	public <I> ViewResource makeViewResource(String baseName, String viewURI, ViewPointResource viewPointResource,
			RepositoryFolder<ViewResource, I> folder, TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager,
			boolean createEmptyContents) throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = folder.getResourceRepository().getResourceCenter();
		I serializationArtefact = resourceCenter.createDirectory((baseName.endsWith(VIEW_SUFFIX) ? baseName : baseName + VIEW_SUFFIX),
				folder.getSerializationArtefact());

		ViewResource returned = makeResource(serializationArtefact, resourceCenter, technologyContextManager, viewURI, createEmptyContents);
		returned.setViewPointResource(viewPointResource);
		return returned;
	}

	public <I> ViewResource makeViewResource(String baseName, ViewPointResource viewPointResource, ViewResource parentViewResource,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager, boolean createEmptyContents)
					throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) parentViewResource.getResourceCenter();
		I serializationArtefact = resourceCenter.createDirectory((baseName.endsWith(VIEW_SUFFIX) ? baseName : (baseName + VIEW_SUFFIX)),
				(I) parentViewResource.getFlexoIODelegate().getSerializationArtefact());

		ViewResource returned = makeResource(serializationArtefact, resourceCenter, technologyContextManager,
				parentViewResource.getURI() + "/" + baseName + (baseName.endsWith(VIEW_SUFFIX) ? baseName : (baseName + VIEW_SUFFIX)),
				createEmptyContents);
		returned.setViewPointResource(viewPointResource);
		parentViewResource.addToContents(returned);
		parentViewResource.notifyContentsAdded(returned);
		return returned;
	}

	public <I> ViewResource retrieveViewResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager) throws ModelDefinitionException {
		ViewResource returned = retrieveResource(serializationArtefact, resourceCenter, technologyContextManager);
		return returned;
	}

	public <I> ViewResource retrieveViewResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager, ViewResource parentViewResource)
					throws ModelDefinitionException {
		ViewResource returned = retrieveResource(serializationArtefact, resourceCenter, technologyContextManager);
		parentViewResource.addToContents(returned);
		parentViewResource.notifyContentsAdded(returned);
		return returned;
	}

	@Override
	public <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {

		if (resourceCenter.exists(serializationArtefact) && resourceCenter.isDirectory(serializationArtefact)
				&& resourceCenter.canRead(serializationArtefact)
				&& resourceCenter.retrieveName(serializationArtefact).endsWith(VIEW_SUFFIX)) {
			/*final String baseName = candidateFile.getName().substring(0,
					candidateFile.getName().length() - ViewPointResource.VIEW_SUFFIX.length());
			final File xmlFile = new File(candidateFile, baseName + ".xml");
			return xmlFile.exists();*/
			return true;
		}
		return false;
	}

	@Override
	public ViewModelFactory makeResourceDataFactory(ViewResource resource,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager) throws ModelDefinitionException {
		return new ViewModelFactory(resource, technologyContextManager.getTechnologyAdapter().getServiceManager().getEditingContext(),
				technologyContextManager.getTechnologyAdapter().getServiceManager().getTechnologyAdapterService());
	}

	@Override
	protected <I> ViewResource registerResource(ViewResource resource, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager) {
		super.registerResource(resource, resourceCenter, technologyContextManager);

		// Register the resource in the ViewRepository of supplied resource center
		registerResourceInResourceRepository(resource, technologyContextManager.getTechnologyAdapter().getViewRepository(resourceCenter));

		// TODO: refactor this
		if (resourceCenter instanceof FlexoProject) {
			registerResourceInResourceRepository(resource, ((FlexoProject) resourceCenter).getViewLibrary());
		}

		// Now look for virtual model instances and sub-views
		exploreViewContents(resource, technologyContextManager);

		return resource;
	}

	/*@Override
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
	
	}*/

	@Override
	protected <I> ViewResource initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager, String uri) throws ModelDefinitionException {
		ViewResource returned = super.initResourceForCreation(serializationArtefact, resourceCenter, technologyContextManager, uri);
		returned.setVersion(INITIAL_REVISION);
		returned.setModelVersion(CURRENT_FML_RT_VERSION);
		return returned;
	}

	@Override
	protected <I> ViewResource initResourceForRetrieving(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLRTTechnologyAdapter> technologyContextManager) throws ModelDefinitionException {

		ViewResource returned = super.initResourceForRetrieving(serializationArtefact, resourceCenter, technologyContextManager);

		String artefactName = resourceCenter.retrieveName(serializationArtefact);

		String baseName = artefactName;
		if (artefactName.endsWith(VIEW_SUFFIX)) {
			baseName = artefactName.substring(0, artefactName.length() - VIEW_SUFFIX.length());
		}

		returned.initName(baseName);

		ViewInfo vpi = findViewInfo(returned, resourceCenter);
		if (vpi != null) {
			System.out.println("******************* on sette l'URI a " + vpi.uri);
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
				returned.setViewPointResource(
						resourceCenter.getServiceManager().getViewPointLibrary().getViewPointResource(vpi.viewPointURI));
				returned.setVirtualModelResource(
						resourceCenter.getServiceManager().getViewPointLibrary().getViewPointResource(vpi.viewPointURI));
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

		exploreResource(viewResource.getFlexoIODelegate().getSerializationArtefact(), viewResource, technologyContextManager);
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if (isValidArtefact(child, resourceCenter)) {
				try {
					ViewResource subViewResource = retrieveViewResource(child, resourceCenter, technologyContextManager, viewResource);
				} catch (ModelDefinitionException e) {
					// TODO Auto-generated catch block
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
				.getXMLRootElementInfo((I) resource.getFlexoIODelegate().getSerializationArtefact());
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

	/*private static <I> ViewInfo findViewInfo(I serializationArtefact) {
		Document document;
	
		if (serializationArtefact instanceof File) {
			try {
				File viewDirectory = (File) serializationArtefact;
				logger.fine("Try to find infos for " + viewDirectory);
	
				String baseName = viewDirectory.getName().substring(0, viewDirectory.getName().length() - 5);
				File xmlFile = new File(viewDirectory, baseName + ".xml");
	
				if (xmlFile.exists()) {
					document = XMLUtils.readXMLFile(xmlFile);
					Element root = XMLUtils.getElement(document, "View");
					if (root != null) {
						ViewInfo returned = new ViewInfo();
						returned.name = baseName;
						Iterator<Attribute> it = root.getAttributes().iterator();
						while (it.hasNext()) {
							Attribute at = it.next();
							if (at.getName().equals("uri")) {
								logger.fine("Returned " + at.getValue());
								returned.uri = at.getValue();
							}
							else if (at.getName().equals("viewPointURI")) {
								logger.fine("Returned " + at.getValue());
								returned.viewPointURI = at.getValue();
							}
							else if (at.getName().equals("viewPointVersion")) {
								logger.fine("Returned " + at.getValue());
								returned.viewPointVersion = at.getValue();
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
						return returned;
					}
				}
				else {
					logger.warning("Cannot find file: " + xmlFile.getAbsolutePath());
				}
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		return null;
	}*/

	/*public static ViewResource makeViewResource(String name, RepositoryFolder<ViewResource, ?> folder, ViewPoint viewPoint,
		ViewLibrary viewLibrary) {
	ViewResource returned = makeViewResourceInDirectory(name, folder.getFile(), viewPoint, viewLibrary);
	// returned.setURI(viewLibrary.getProject().getURI() + "/" + returned.getName());
	viewLibrary.registerResource(returned, folder);
	return returned;
	}
	
	public static ViewResource makeSubViewResource(String name, ViewResource container, ViewPoint viewPoint, ViewLibrary viewLibrary) {
	
	System.out.println("Et hop, on cree une nouvelle subview " + name + " pour " + container);
	System.out.println("container.getFlexoIODelegate()=" + container.getFlexoIODelegate());
	
	if (container.getFlexoIODelegate() instanceof FileFlexoIODelegate) {
		ViewResource returned = makeViewResourceInDirectory(name,
				((FileFlexoIODelegate) container.getFlexoIODelegate()).getFile().getParentFile(), viewPoint, viewLibrary);
				// returned.setURI(container.getURI() + "/" + returned.getName());
	
		// System.out.println("***************** COUCOU on ajoute la nouvelle SubView dans la View");
		// System.out.println("loaded=" + container.isLoaded());
		// System.out.println("***************** HOP1");
		viewLibrary.registerResource(returned, container);
		// System.out.println("***************** HOP2");
		return returned;
	}
	else {
		// TODO !!!
	}
	return null;
	}
	
	private static ViewResource makeViewResourceInDirectory(String name, File directory, ViewPoint viewPoint, ViewLibrary viewLibrary) {
	try {
		// File viewDirectory = new File(folder.getFile(), name + ViewResource.VIEW_SUFFIX);
		ModelFactory factory = new ModelFactory(
				ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class, ViewResource.class));
		ViewResourceImpl returned = (ViewResourceImpl) factory.newInstance(ViewResource.class);
		// String baseName = name;
		// File xmlFile = new File(viewDirectory, baseName + ".xml");
		returned.initName(name);
	
		// System.out.println("Je suis dans la vue " + directory);
		returned.setFlexoIODelegate(DirectoryBasedFlexoIODelegateImpl.makeDirectoryBasedFlexoIODelegate(directory, VIEW_SUFFIX,
				CORE_FILE_SUFFIX, returned, factory));
		// System.out.println("Je sauve la nouvelle sous-vue en " + ((FileFlexoIODelegate) returned.getFlexoIODelegate()).getFile());
	
		returned.setProject(viewLibrary.getProject());
		returned.setVersion(new FlexoVersion("1.0"));
		// returned.setURI(viewLibrary.getProject().getURI() + "/" + name);
		// returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(xmlFile, factory));
		returned.setViewLibrary(viewLibrary);
		returned.setViewPointResource((ViewPointResource) viewPoint.getResource());
		returned.setFactory(new ViewModelFactory(returned, viewLibrary.getServiceManager().getEditingContext(),
				viewLibrary.getServiceManager().getTechnologyAdapterService()));
	
		returned.setResourceCenter(viewLibrary.getProject());
		returned.setServiceManager(viewLibrary.getServiceManager());
	
		return returned;
	} catch (ModelDefinitionException e) {
		e.printStackTrace();
	}
	return null;
	}*/

	/*public static ViewResource retrieveViewResource(File viewDirectory, RepositoryFolder<ViewResource, ?> folder, ViewLibrary viewLibrary) {
	
	ViewResource returned = retrieveViewResourceFromDirectory(viewDirectory, viewLibrary);
	// returned.setURI(viewLibrary.getProject().getURI() + "/" + returned.getName());
	viewLibrary.registerResource(returned, folder);
	return returned;
	}
	
	public static ViewResource retrieveSubViewResource(File viewDirectory, ViewResource container, ViewLibrary viewLibrary) {
	
	ViewResource returned = retrieveViewResourceFromDirectory(viewDirectory, viewLibrary);
	// returned.setURI(container.getURI() + "/" + returned.getName());
	viewLibrary.registerResource(returned, container);
	return returned;
	}
	
	private static ViewResource retrieveViewResourceFromDirectory(File viewDirectory, ViewLibrary viewLibrary) {
	try {
		ModelFactory factory = new ModelFactory(
				ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class, ViewResource.class));
		ViewResourceImpl returned = (ViewResourceImpl) factory.newInstance(ViewResource.class);
		String baseName = viewDirectory.getName().substring(0, viewDirectory.getName().length() - ViewResource.VIEW_SUFFIX.length());
		File xmlFile = new File(viewDirectory, baseName + ".xml");
		ViewInfo vpi = findViewInfo(viewDirectory);
		if (vpi == null) {
			// Unable to retrieve infos, just abort
			return null;
		}
		returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(xmlFile, factory));
		returned.initName(vpi.name);
	
		returned.setProject(viewLibrary.getProject());
	
		if (StringUtils.isNotEmpty(vpi.viewPointURI)) {
			returned.viewpointURI = vpi.viewPointURI;
			returned.setViewPointResource(viewLibrary.getServiceManager().getViewPointLibrary().getViewPointResource(vpi.viewPointURI));
		}
		returned.setViewLibrary(viewLibrary);
		returned.setFactory(new ViewModelFactory(returned, viewLibrary.getServiceManager().getEditingContext(),
				viewLibrary.getServiceManager().getTechnologyAdapterService()));
	
		returned.setResourceCenter(viewLibrary.getProject());
		returned.setServiceManager(viewLibrary.getServiceManager());
	
		logger.fine("ViewResource " + xmlFile.getAbsolutePath() + " version " + returned.getModelVersion());
	
		// Now look for virtual model instances
		if (viewDirectory.exists() && viewDirectory.isDirectory()) {
			for (File virtualModelFile : viewDirectory.listFiles()) {
				if (virtualModelFile.getName().endsWith(VirtualModelInstanceResource.VIRTUAL_MODEL_SUFFIX)) {
					VirtualModelInstanceResource virtualModelInstanceResource = VirtualModelInstanceResourceImpl
							.retrieveVirtualModelInstanceResource(virtualModelFile, returned);
					returned.addToContents(virtualModelInstanceResource);
				}
				else if (virtualModelFile.getName().endsWith(ViewResource.VIEW_SUFFIX)) {
					ViewResource subViewResource = ViewResourceImpl.retrieveSubViewResource(virtualModelFile, returned, viewLibrary);
					returned.addToContents(subViewResource);
					System.out.println(">>>>>>>>> Hop j'ai trouve une subview " + virtualModelFile + " dans " + returned);
				}
			}
		}
		return returned;
	} catch (ModelDefinitionException e) {
		e.printStackTrace();
	}
	return null;
	}*/

}
