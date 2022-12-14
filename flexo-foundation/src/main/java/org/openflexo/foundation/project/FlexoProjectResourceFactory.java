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

package org.openflexo.foundation.project;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.PamelaResourceFactory;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.xml.XMLRootElementInfo;

/**
 * Implementation of PamelaResourceFactory for {@link FlexoProjectResource}
 * 
 * @author sylvain
 *
 */
public class FlexoProjectResourceFactory<I> extends PamelaResourceFactory<FlexoProjectResource<I>, FlexoProject<I>, FlexoProjectFactory> {

	private static final Logger logger = Logger.getLogger(FlexoProjectResourceFactory.class.getPackage().getName());

	public static final String PROJECT_DATA_FILENAME = "ProjectData.xml";
	public static final String PROJECT_SUFFIX = ".prj";
	public static final FlexoVersion INITIAL_REVISION = new FlexoVersion("0.1");
	public static final FlexoVersion CURRENT_MODEL_VERSION = new FlexoVersion("1.0");

	private FlexoServiceManager serviceManager;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public FlexoProjectResourceFactory(FlexoServiceManager serviceManager) throws ModelDefinitionException {
		super((Class) FlexoProjectResource.class);
		this.serviceManager = serviceManager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FlexoProject<I> makeEmptyResourceData(FlexoProjectResource<I> resource) {
		return (FlexoProject<I>) resource.getFactory().makeNewFlexoProject();
	}

	public FlexoProjectFactory makeResourceDataFactory(FlexoProjectResource<I> resource) throws ModelDefinitionException {
		return new FlexoProjectFactory(resource, resource.getServiceManager());
	}

	@Override
	protected <I2> FlexoProjectResource<I> initResourceForRetrieving(I2 serializationArtefact, FlexoResourceCenter<I2> resourceCenter)
			throws ModelDefinitionException, IOException {
		return _initResourceForRetrieving((I) serializationArtefact, (FlexoResourceCenter<I>) resourceCenter);
	}

	protected FlexoProjectResource<I> _initResourceForRetrieving(I serializationArtefact, FlexoResourceCenter<I> resourceCenter)
			throws ModelDefinitionException, IOException {
		FlexoProjectResource<I> returned = super.initResourceForRetrieving(serializationArtefact, resourceCenter);

		// the ResourceCenter might be null here, so we "force" the ServiceManager
		returned.setServiceManager(serviceManager);

		returned.setFactory(makeResourceDataFactory(returned));

		String artefactName = resourceCenter.retrieveName(serializationArtefact);
		String baseName = artefactName.endsWith(PROJECT_SUFFIX) ? artefactName.substring(0, artefactName.length() - PROJECT_SUFFIX.length())
				: artefactName;

		returned.initName(baseName);

		FlexoProjectInfo vpi = findFlexoProjectInfo(returned, resourceCenter);
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
				returned.setModelVersion(CURRENT_MODEL_VERSION);
			}
		}
		else {
			logger.warning("Cannot retrieve info from " + serializationArtefact);
			returned.setVersion(INITIAL_REVISION);
			returned.setModelVersion(CURRENT_MODEL_VERSION);
		}

		return returned;
	}

	@Override
	protected <I2> FlexoProjectResource<I> initResourceForCreation(I2 serializationArtefact, FlexoResourceCenter<I2> resourceCenter,
			String name, String uri) throws ModelDefinitionException {
		FlexoProjectResource<I> returned = super.initResourceForCreation(serializationArtefact, resourceCenter, name, uri);

		returned.setVersion(INITIAL_REVISION);
		returned.setModelVersion(CURRENT_MODEL_VERSION);
		return returned;
	}

	@Override
	public <I2> boolean isValidArtefact(I2 serializationArtefact, FlexoResourceCenter<I2> resourceCenter) {

		if (resourceCenter.exists(serializationArtefact) && resourceCenter.isDirectory(serializationArtefact)
				&& resourceCenter.canRead(serializationArtefact)
				&& (resourceCenter.retrieveName(serializationArtefact).endsWith(PROJECT_SUFFIX)
				/*|| resourceCenter.retrieveName(serializationArtefact).endsWith(DIAGRAM_SPECIFICATION_SUFFIX + "/")*/)) {
			/*final String baseName = candidateFile.getName().substring(0,
					candidateFile.getName().length() - ViewPointResource.DIAGRAM_SPECIFICATION_SUFFIX.length());
			final File xmlFile = new File(candidateFile, baseName + ".xml");
			return xmlFile.exists();*/
			return true;
		}
		return false;
	}

	public FlexoProjectResource<I> makeFlexoProjectResource(String baseName, String uri,
			RepositoryFolder<FlexoProjectResource<I>, I> folder, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException {
		FlexoResourceCenter<I> resourceCenter = folder.getResourceRepository().getResourceCenter();

		String artefactName = baseName.endsWith(FlexoProjectResourceFactory.PROJECT_SUFFIX) ? baseName
				: baseName + FlexoProjectResourceFactory.PROJECT_SUFFIX;
		I serializationArtefact = resourceCenter.createDirectory(artefactName, folder.getSerializationArtefact());

		FlexoResourceCenter<I> delegateResourceCenter = makeDelegateRC(serializationArtefact);
		FlexoProjectResource<I> returned = makeResource(serializationArtefact, resourceCenter, baseName, uri, createEmptyContents);
		returned.setDelegateResourceCenter(delegateResourceCenter);
		return returned;
	}

	@SuppressWarnings("unchecked")
	private <I2> FlexoResourceCenter<I2> makeDelegateRC(I2 serializationArtefact) {

		// System.out.println("*********** on cree un delegate RC pour " + serializationArtefact);

		if (serializationArtefact instanceof File) {
			try {
				FlexoResourceCenter<I2> returned = (FlexoResourceCenter<I2>) DirectoryResourceCenter
						.instanciateNewDirectoryResourceCenter((File) serializationArtefact, serviceManager.getResourceCenterService());
				return returned;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.warning("makeDelegateRC not implemented for " + serializationArtefact);
		return null;
	}

	public FlexoProjectResource<I> makeFlexoProjectResource(I serializationArtefact, String uri, boolean createEmptyContents)
			throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = serviceManager.getResourceCenterService()
				.getResourceCenterContaining(serializationArtefact);
		FlexoResourceCenter<I> delegateResourceCenter = makeDelegateRC(serializationArtefact);

		if (resourceCenter == null) {
			resourceCenter = delegateResourceCenter;
		}

		String baseName = resourceCenter.retrieveName(serializationArtefact);
		if (baseName.endsWith(PROJECT_SUFFIX)) {
			baseName = baseName.substring(0, baseName.length() - PROJECT_SUFFIX.length());
		}

		FlexoProjectResource<I> returned = makeResource(serializationArtefact, resourceCenter, baseName, uri, createEmptyContents);

		returned.setDelegateResourceCenter(delegateResourceCenter);
		return returned;
	}

	public FlexoProjectResource<I> makeFlexoProjectResource(String projectName, RepositoryFolder<FlexoProjectResource<I>, I> folder,
			String uri, boolean createEmptyContents) throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = folder.getResourceRepository().getResourceCenter();
		if (projectName.endsWith(PROJECT_SUFFIX)) {
			projectName = projectName.substring(0, projectName.length() - PROJECT_SUFFIX.length());
		}

		I serializationArtefact = resourceCenter.createDirectory(projectName + PROJECT_SUFFIX, folder.getSerializationArtefact());

		FlexoResourceCenter<I> delegateResourceCenter = makeDelegateRC(serializationArtefact);
		FlexoProjectResource<I> returned = makeResource(serializationArtefact, resourceCenter, projectName, uri, createEmptyContents);
		returned.setDelegateResourceCenter(delegateResourceCenter);
		return returned;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <I2> FlexoProjectResource<I> retrieveResource(I2 serializationArtefact, FlexoResourceCenter<I2> resourceCenter)
			throws ModelDefinitionException, IOException {

		FlexoResourceCenter<I2> delegateResourceCenter = makeDelegateRC(serializationArtefact);

		if (resourceCenter == null) {
			resourceCenter = delegateResourceCenter;
		}

		FlexoProjectResource returned = super.retrieveResource(serializationArtefact, resourceCenter);
		returned.setDelegateResourceCenter(delegateResourceCenter);
		return returned;
	}

	@Override
	public <I2> FlexoProjectResource<I> registerResource(FlexoProjectResource<I> resource, FlexoResourceCenter<I2> resourceCenter) {

		// the ResourceCenter might be null here, so we "force" the ServiceManager
		resource.setServiceManager(serviceManager);

		super.registerResource(resource, resourceCenter);

		try {
			resource.setFactory(makeResourceDataFactory(resource));
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}

		// Now look inside ???

		return resource;

	}

	@Override
	protected <I2> FlexoIODelegate<I2> makeFlexoIODelegate(I2 serializationArtefact, FlexoResourceCenter<I2> resourceCenter) {
		if (resourceCenter != null) {
			I2 singleFileArtefact = resourceCenter.getEntry(PROJECT_DATA_FILENAME, serializationArtefact);
			return resourceCenter.makeDirectoryBasedFlexoIODelegate(serializationArtefact, singleFileArtefact, this);
		}
		/*else {
			if (serializationArtefact instanceof File) {
				String baseName = ((File) serializationArtefact).getName().substring(0,
						((File) serializationArtefact).getName().length() - PROJECT_SUFFIX.length());
				return (FlexoIODelegate<I2>) DirectoryBasedIODelegateImpl.makeDirectoryBasedFlexoIODelegate(
						((File) serializationArtefact).getParentFile(), baseName, PROJECT_SUFFIX, CORE_FILE_SUFFIX, this);
			}
		}*/
		return null;
	}

	private static class FlexoProjectInfo {
		public String uri;
		public String version;
		public String revision;
		public String modelVersion;
	}

	private FlexoProjectInfo findFlexoProjectInfo(FlexoProjectResource<I> resource, FlexoResourceCenter<I> resourceCenter) {

		FlexoProjectInfo returned = new FlexoProjectInfo();
		XMLRootElementInfo xmlRootElementInfo = resourceCenter
				.getXMLRootElementInfo((I) resource.getIODelegate().getSerializationArtefact());
		if (xmlRootElementInfo == null) {
			return null;
		}
		if (xmlRootElementInfo.getName().equals("FlexoProject")) {
			returned.uri = xmlRootElementInfo.getAttribute("uri");
			returned.version = xmlRootElementInfo.getAttribute(FlexoProject.PROJECT_VERSION_KEY);
			returned.revision = xmlRootElementInfo.getAttribute(FlexoProject.PROJECT_REVISION_KEY);
			returned.modelVersion = xmlRootElementInfo.getAttribute("modelVersion");
		}
		return returned;
	}

	/*public static DiagramSpecificationResource makeDiagramSpecificationResource(String name, RepositoryFolder<?, ?> folder, String uri,
			FlexoResourceCenter<?> resourceCenter, FlexoServiceManager serviceManager) {
		try {
			// File diagramSpecificationDirectory = new File(folder.getFile(), name + DIAGRAM_SPECIFICATION_SUFFIX);
			PamelaModelFactory factory = new PamelaModelFactory(
					PamelaMetaModelLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class, DiagramSpecificationResource.class));
			DiagramSpecificationResourceImpl returned = (DiagramSpecificationResourceImpl) factory
					.newInstance(DiagramSpecificationResource.class);
	
			// String baseName = name;
			// File diagramSpecificationXMLFile = new File(diagramSpecificationDirectory, baseName + ".xml");
			returned.initName(name);
	
			returned.setFlexoIODelegate(DirectoryBasedFlexoIODelegateImpl.makeDirectoryBasedFlexoIODelegate(folder.getFile(),
					DIAGRAM_SPECIFICATION_SUFFIX, CORE_FILE_SUFFIX, returned, factory));
	
			// returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(diagramSpecificationXMLFile, factory));
			DiagramSpecificationFactory diagramSpecificationFactory = new DiagramSpecificationFactory(returned,
					serviceManager.getEditingContext());
			returned.setFactory(diagramSpecificationFactory);
			returned.setURI(uri);
			returned.setResourceCenter(resourceCenter);
			returned.setServiceManager(serviceManager);
			// viewPointResource.addToContents(returned);
			// viewPointResource.notifyContentsAdded(returned);
			DiagramSpecification newDiagram = returned.getFactory().makeNewDiagramSpecification();
			newDiagram.setResource(returned);
			returned.setResourceData(newDiagram);
			newDiagram.setURI(uri);
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static DiagramSpecificationResource retrieveDiagramSpecificationResource(File diagramSpecificationDirectory,
			RepositoryFolder<?> folder, FlexoResourceCenter<?> resourceCenter, FlexoServiceManager serviceManager) {
		try {
			PamelaModelFactory factory = new PamelaModelFactory(
					PamelaMetaModelLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class, DiagramSpecificationResource.class));
			DiagramSpecificationResourceImpl returned = (DiagramSpecificationResourceImpl) factory
					.newInstance(DiagramSpecificationResource.class);
	
			String baseName = diagramSpecificationDirectory.getName().substring(0,
					diagramSpecificationDirectory.getName().length() - DIAGRAM_SPECIFICATION_SUFFIX.length());
	
			returned.initName(baseName);
			File diagramSpecificationXMLFile = new File(diagramSpecificationDirectory, baseName + ".xml");
	
			DiagramSpecificationInfo vpi = null;
			try {
				vpi = findDiagramSpecificationInfo(new FileInputStream(diagramSpecificationXMLFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if (vpi == null) {
				// Unable to retrieve infos, just abort
				logger.warning("Cannot retrieve info for diagram specification " + diagramSpecificationDirectory);
				return null;
			}
			returned.setURI(vpi.uri);
			returned.initName(vpi.name);
	
			returned.setFlexoIODelegate(DirectoryBasedFlexoIODelegateImpl.makeDirectoryBasedFlexoIODelegate(
					diagramSpecificationDirectory.getParentFile(), DIAGRAM_SPECIFICATION_SUFFIX, CORE_FILE_SUFFIX, returned, factory));
	
			// returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(diagramSpecificationXMLFile, factory));
			DiagramSpecificationFactory diagramSpecificationFactory = new DiagramSpecificationFactory(returned,
					serviceManager.getEditingContext());
			returned.setFactory(diagramSpecificationFactory);
	
			returned.initName(vpi.name);
			if (StringUtils.isNotEmpty(vpi.version)) {
				returned.setVersion(new FlexoVersion(vpi.version));
			}
			returned.setModelVersion(new FlexoVersion(StringUtils.isNotEmpty(vpi.modelVersion) ? vpi.modelVersion : "0.1"));
	
			returned.setResourceCenter(resourceCenter);
			returned.setServiceManager(serviceManager);
	
			logger.fine("DiagramSpecificationResource " + diagramSpecificationDirectory.getAbsolutePath() + " version "
					+ returned.getModelVersion());
	
			if (returned.getDirectory() != null) {
				returned.exploreInternalResources(returned.getDirectory());
			}
	
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static DiagramSpecificationResource retrieveDiagramSpecificationResource(InJarResourceImpl inJarResource,
			FlexoResourceCenter<?> resourceCenter, FlexoServiceManager serviceManager) {
		try {
			PamelaModelFactory factory = new PamelaModelFactory(
					PamelaMetaModelLibrary.getCompoundModelContext(InJarFlexoIODelegate.class, DiagramSpecificationResource.class));
			DiagramSpecificationResourceImpl returned = (DiagramSpecificationResourceImpl) factory
					.newInstance(DiagramSpecificationResource.class);
			returned.setFlexoIODelegate(InJarFlexoIODelegateImpl.makeInJarFlexoIODelegate(inJarResource, factory));
			DiagramSpecificationFactory diagramSpecificationFactory = new DiagramSpecificationFactory(returned,
					serviceManager.getEditingContext());
			returned.setFactory(diagramSpecificationFactory);
			DiagramSpecificationInfo vpi = findDiagramSpecificationInfo(returned.getFlexoIOStreamDelegate().getInputStream());
			if (vpi == null) {
				// Unable to retrieve infos, just abort
				// logger.warning("Cannot retrieve info for diagram specification " + diagramSpecificationDirectory);
				return null;
			}
			returned.setURI(vpi.uri);
	
			returned.initName(vpi.name);
			if (StringUtils.isNotEmpty(vpi.version)) {
				returned.setVersion(new FlexoVersion(vpi.version));
			}
			returned.setModelVersion(new FlexoVersion(StringUtils.isNotEmpty(vpi.modelVersion) ? vpi.modelVersion : "0.1"));
	
			returned.setResourceCenter(resourceCenter);
			returned.setServiceManager(serviceManager);
	
			returned.exploreInternalResources(returned.getDirectory());
	
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void exploreInternalResources(Resource parent) {
		XMLRootElementInfo result = null;
	
		for (Resource child : parent.getContents()) {
			if (child.isContainer()) {
				exploreInternalResources(child);
			}
			else {
				try {
					if (child.getURI().endsWith(".diagram")) {
						result = reader.readRootElement(child);
						// Serialization artefact is File
						if (result.getName().equals("Diagram") && getFlexoIODelegate() instanceof FileFlexoIODelegate) {
							DiagramResource exampleDiagramResource = getTechnologyAdapter().getTechnologyContextManager()
									.getDiagramResource(((FileFlexoIODelegate) getFlexoIODelegate()).getFile());
							if (exampleDiagramResource == null) {
								exampleDiagramResource = DiagramResourceImpl.retrieveDiagramResource(
										ResourceLocator.retrieveResourceAsFile(child), getResourceCenter(), getServiceManager());
							}
							addToContents(exampleDiagramResource);
							if (exampleDiagramResource.getMetaModelResource() == null) {
								exampleDiagramResource.setMetaModelResource(this);
							}
							getTechnologyAdapter().getTechnologyContextManager().registerDiagram(exampleDiagramResource);
							logger.fine("ExampleDiagramResource " + exampleDiagramResource.getFlexoIODelegate().toString() + " version "
									+ exampleDiagramResource.getModelVersion());
						}
						// Serialization artefact is InJarResource
						else if (result.getName().equals("Diagram") && getFlexoIODelegate() instanceof InJarFlexoIODelegate) {
							DiagramResource exampleDiagramResource = DiagramResourceImpl.retrieveDiagramResource((InJarResourceImpl) child,
									getResourceCenter(), getServiceManager());
							addToContents(exampleDiagramResource);
							if (exampleDiagramResource.getMetaModelResource() == null) {
								exampleDiagramResource.setMetaModelResource(this);
							}
						}
					}
					if (child.getURI().endsWith(".palette")) {
						result = reader.readRootElement(child);
						// Serialization artefact is File
						if (result.getName().equals("DiagramPalette") && getFlexoIODelegate() instanceof FileFlexoIODelegate) {
							DiagramPaletteResource diagramPaletteResource = DiagramPaletteResourceImpl.retrieveDiagramPaletteResource(this,
									ResourceLocator.retrieveResourceAsFile(child), getServiceManager());
							addToContents(diagramPaletteResource);
						}
						// Serialization artefact is InJarResource
						else if (result.getName().equals("DiagramPalette") && getFlexoIODelegate() instanceof InJarFlexoIODelegate) {
							DiagramPaletteResource diagramPaletteResource = DiagramPaletteResourceImpl.retrieveDiagramPaletteResource(this,
									(InJarResourceImpl) child, getServiceManager());
							addToContents(diagramPaletteResource);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}*/

}
