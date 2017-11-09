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

import java.io.IOException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.PamelaResourceFactory;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.xml.XMLRootElementInfo;

/**
 * Implementation of PamelaResourceFactory for {@link FlexoProjectResource}
 * 
 * @author sylvain
 *
 */
public class FlexoProjectResourceFactory<I> extends PamelaResourceFactory<FlexoProjectResource, FlexoProject<?>, FlexoProjectFactory> {

	private static final Logger logger = Logger.getLogger(FlexoProjectResourceFactory.class.getPackage().getName());

	public static final String CORE_FILE_SUFFIX = ".xml";
	public static final String PROJECT_SUFFIX = ".prj";
	public static final FlexoVersion INITIAL_REVISION = new FlexoVersion("0.1");
	public static final FlexoVersion CURRENT_MODEL_VERSION = new FlexoVersion("1.0");

	public FlexoProjectResourceFactory() throws ModelDefinitionException {
		super(FlexoProjectResource.class);
	}

	@Override
	public FlexoProject<?> makeEmptyResourceData(FlexoProjectResource resource) {
		return resource.getFactory().makeNewFlexoProject();
	}

	public FlexoProjectFactory makeResourceDataFactory(FlexoProjectResource resource) throws ModelDefinitionException {
		return new FlexoProjectFactory(resource, resource.getServiceManager().getEditingContext());
	}

	@Override
	protected <I> FlexoProjectResource initResourceForRetrieving(I serializationArtefact, FlexoResourceCenter<I> resourceCenter)
			throws ModelDefinitionException, IOException {
		FlexoProjectResource returned = super.initResourceForRetrieving(serializationArtefact, resourceCenter);

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
	protected <I> FlexoProjectResource initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter, String name,
			String uri) throws ModelDefinitionException {
		FlexoProjectResource returned = super.initResourceForCreation(serializationArtefact, resourceCenter, name, uri);

		returned.setVersion(INITIAL_REVISION);
		returned.setModelVersion(CURRENT_MODEL_VERSION);
		returned.setFactory(makeResourceDataFactory(returned));
		return returned;
	}

	@Override
	public <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {

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

	@Override
	public <I> I getConvertableArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		return null;
	}

	public <I> FlexoProjectResource makeFlexoProjectResource(String baseName, String uri, RepositoryFolder<FlexoProjectResource, I> folder,
			boolean createEmptyContents) throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = folder.getResourceRepository().getResourceCenter();

		String artefactName = baseName.endsWith(FlexoProjectResourceFactory.PROJECT_SUFFIX) ? baseName
				: baseName + FlexoProjectResourceFactory.PROJECT_SUFFIX;
		I serializationArtefact = resourceCenter.createDirectory(artefactName, folder.getSerializationArtefact());
		return makeResource(serializationArtefact, resourceCenter, baseName, uri, createEmptyContents);
	}

	@Override
	protected <I> FlexoProjectResource registerResource(FlexoProjectResource resource, FlexoResourceCenter<I> resourceCenter) {
		super.registerResource(resource, resourceCenter);

		System.out.println("HOP, je register la FlexoProjectResource");

		// Now look inside ???

		return resource;

	}

	@Override
	protected <I> FlexoIODelegate<I> makeFlexoIODelegate(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		return resourceCenter.makeDirectoryBasedFlexoIODelegate(serializationArtefact, PROJECT_SUFFIX, CORE_FILE_SUFFIX, this);
	}

	private static class FlexoProjectInfo {
		public String uri;
		public String version;
		public String modelVersion;
	}

	private <I> FlexoProjectInfo findFlexoProjectInfo(FlexoProjectResource resource, FlexoResourceCenter<I> resourceCenter) {

		FlexoProjectInfo returned = new FlexoProjectInfo();
		XMLRootElementInfo xmlRootElementInfo = resourceCenter
				.getXMLRootElementInfo((I) resource.getIODelegate().getSerializationArtefact());
		if (xmlRootElementInfo == null) {
			return null;
		}
		if (xmlRootElementInfo.getName().equals("DiagramSpecification")) {
			returned.uri = xmlRootElementInfo.getAttribute(FlexoProject.PROJECT_URI_KEY);
			returned.version = xmlRootElementInfo.getAttribute("version");
			returned.modelVersion = xmlRootElementInfo.getAttribute("modelVersion");
		}
		return returned;
	}

	/*public static DiagramSpecificationResource makeDiagramSpecificationResource(String name, RepositoryFolder<?, ?> folder, String uri,
			FlexoResourceCenter<?> resourceCenter, FlexoServiceManager serviceManager) {
		try {
			// File diagramSpecificationDirectory = new File(folder.getFile(), name + DIAGRAM_SPECIFICATION_SUFFIX);
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class, DiagramSpecificationResource.class));
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
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class, DiagramSpecificationResource.class));
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
				// TODO Auto-generated catch block
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
	
			// TODO: why do we set the name twice ???
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
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(InJarFlexoIODelegate.class, DiagramSpecificationResource.class));
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
