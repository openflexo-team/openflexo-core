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
import java.util.Iterator;
import java.util.logging.Logger;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.foundation.utils.XMLUtils;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;

/**
 * Implementation of PamelaResourceFactory for {@link ViewPointResource}
 * 
 * @author sylvain
 *
 */
public class VirtualModelResourceFactory extends AbstractVirtualModelResourceFactory<VirtualModel, VirtualModelResource> {

	private static final Logger logger = Logger.getLogger(VirtualModelResourceFactory.class.getPackage().getName());

	public VirtualModelResourceFactory() throws ModelDefinitionException {
		super(VirtualModelResource.class);
	}

	@Override
	public VirtualModel makeEmptyResourceData(VirtualModelResource resource) {
		return resource.getFactory().newVirtualModel();
	}

	@Override
	protected VirtualModel createEmptyContents(VirtualModelResource resource) {
		VirtualModel returned = super.createEmptyContents(resource);
		resource.getContainer().getViewPoint().addToVirtualModels(returned);
		return returned;
	}

	public <I> VirtualModelResource makeVirtualModelResource(String baseName, ViewPointResource viewPointResource,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager, boolean createEmptyContents)
					throws SaveResourceException, ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) viewPointResource.getResourceCenter();
		I serializationArtefact = resourceCenter.createDirectory(baseName,
				resourceCenter.getContainer((I) viewPointResource.getFlexoIODelegate().getSerializationArtefact()));

		VirtualModelResource returned = initResourceForCreation(serializationArtefact, resourceCenter, technologyContextManager,
				viewPointResource.getURI() + "/" + baseName);

		viewPointResource.addToContents(returned);

		registerResource(returned, resourceCenter, technologyContextManager);

		if (createEmptyContents) {
			createEmptyContents(returned);
			returned.save(null);
		}

		return returned;
	}

	public <I> VirtualModelResource retrieveVirtualModelResource(I serializationArtefact,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager, ViewPointResource viewPointResource)
					throws ModelDefinitionException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) viewPointResource.getResourceCenter();
		String name = resourceCenter.retrieveName(serializationArtefact);

		VirtualModelResource returned = initResourceForRetrieving(serializationArtefact, resourceCenter, technologyContextManager);
		returned.setURI(viewPointResource.getURI() + "/" + name);

		viewPointResource.addToContents(returned);
		viewPointResource.notifyContentsAdded(returned);

		registerResource(returned, resourceCenter, technologyContextManager);

		return returned;
	}

	@Override
	protected <I> VirtualModelResource initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager, String uri) throws ModelDefinitionException {
		VirtualModelResource returned = super.initResourceForCreation(serializationArtefact, resourceCenter, technologyContextManager, uri);
		returned.setVersion(INITIAL_REVISION);
		returned.setModelVersion(CURRENT_FML_VERSION);
		return returned;
	}

	@Override
	protected <I> VirtualModelResource initResourceForRetrieving(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager) throws ModelDefinitionException {

		VirtualModelResource returned = super.initResourceForRetrieving(serializationArtefact, resourceCenter, technologyContextManager);

		String artefactName = resourceCenter.retrieveName(serializationArtefact);
		VirtualModelInfo vpi = null;
		vpi = findVirtualModelInfo(serializationArtefact);
		if (vpi == null) {
			// Unable to retrieve infos, just abort
			logger.warning("Cannot retrieve info from " + serializationArtefact);
			return null;
		}

		returned.initName(artefactName);
		if (StringUtils.isNotEmpty(vpi.version)) {
			returned.setVersion(new FlexoVersion(vpi.version));
		}
		else {
			returned.setVersion(new FlexoVersion("0.1"));
		}
		if (StringUtils.isNotEmpty(vpi.modelVersion)) {
			returned.setModelVersion(new FlexoVersion(vpi.modelVersion));
		}
		else {
			returned.setModelVersion(CURRENT_FML_VERSION);
		}

		return returned;
	}

	private static class VirtualModelInfo {
		public String version;
		public String name;
		public String modelVersion;
	}

	private <I> VirtualModelInfo findVirtualModelInfo(I serializationArtefact) {
		Document document;
		if (serializationArtefact instanceof File) {
			try {
				File viewDirectory = (File) serializationArtefact;
				logger.fine("Try to find infos for " + viewDirectory);

				String baseName = viewDirectory.getName();
				File xmlFile = new File(viewDirectory, baseName + ".xml");

				if (xmlFile.exists()) {
					document = XMLUtils.readXMLFile(xmlFile);
					Element root = XMLUtils.getElement(document, "VirtualModel");
					if (root != null) {
						VirtualModelInfo returned = new VirtualModelInfo();
						Iterator<Attribute> it = root.getAttributes().iterator();
						while (it.hasNext()) {
							Attribute at = it.next();
							if (at.getName().equals("name")) {
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
							// returned.name = virtualModelDirectory.getName();
							returned.name = "NoName";
						}
						return returned;
					}
					/*} else {
					logger.warning("While analysing virtual model candidate: " + virtualModelDirectory.getAbsolutePath() + " cannot find file "
						+ xmlFile.getAbsolutePath());
					}*/
				}
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.fine("Returned null");
		return null;
	}

	@Override
	public <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		return true;
	}

	@Override
	protected <I> FlexoIODelegate<I> makeFlexoIODelegate(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		return resourceCenter.makeDirectoryBasedFlexoIODelegate(serializationArtefact, "", CORE_FILE_SUFFIX, this);
	}

	/*public static VirtualModelResource makeVirtualModelResource(String name, File containerDir, ViewPointResource viewPointResource,
			FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class, VirtualModelResource.class));
			VirtualModelResourceImpl returned = (VirtualModelResourceImpl) factory.newInstance(VirtualModelResource.class);
			returned.initName(name);
	
			returned.setFlexoIODelegate(DirectoryBasedFlexoIODelegateImpl.makeDirectoryBasedFlexoIODelegate(containerDir, "",
					CORE_FILE_SUFFIX, returned, factory));
	
	
			// returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(virtualModelXMLFile, factory));
			returned.setURI(viewPointResource.getURI() + "/" + name);
			returned.setResourceCenter(viewPointResource.getResourceCenter());
			returned.setServiceManager(serviceManager);
			viewPointResource.addToContents(returned);
			viewPointResource.notifyContentsAdded(returned);
	
			// TODO: the factory should be instantiated and managed by the ProjectNatureService, which should react to the registering
			// of a new TA, and which is responsible to update the VirtualModelFactory of all VirtualModelResource
			returned.setFactory(new FMLModelFactory(returned, serviceManager));
	
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static VirtualModelResource retrieveVirtualModelResource(File virtualModelDirectory,
			ViewPointResource viewPointResource, FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(DirectoryBasedFlexoIODelegate.class, VirtualModelResource.class));
			VirtualModelResourceImpl returned = (VirtualModelResourceImpl) factory.newInstance(VirtualModelResource.class);
			String baseName = virtualModelDirectory.getName();
			File xmlFile = new File(virtualModelDirectory, baseName + CORE_FILE_SUFFIX);
			VirtualModelInfo vpi = null;
			try {
				vpi = findVirtualModelInfo(new FileInputStream(xmlFile));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (vpi == null) {
				// Unable to retrieve infos, just abort
				return null;
			}
	
			returned.initName(baseName);
			returned.setFlexoIODelegate(DirectoryBasedFlexoIODelegateImpl
					.makeDirectoryBasedFlexoIODelegate(virtualModelDirectory.getParentFile(), "", CORE_FILE_SUFFIX, returned, factory));
	
			// returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(xmlFile, factory));
			returned.setURI(viewPointResource.getURI() + "/" + virtualModelDirectory.getName());
			if (StringUtils.isNotEmpty(vpi.version)) {
				returned.setVersion(new FlexoVersion(vpi.version));
			}
			returned.setModelVersion(new FlexoVersion(StringUtils.isNotEmpty(vpi.modelVersion) ? vpi.modelVersion : "0.1"));
	
			returned.setResourceCenter(viewPointResource.getResourceCenter());
			returned.setServiceManager(serviceManager);
	
			logger.fine("VirtualModelResource " + xmlFile.getAbsolutePath() + " version " + returned.getModelVersion());
	
			// TODO: the factory should be instantiated and managed by the ProjectNatureService, which should react to the registering
			// of a new TA, and which is responsible to update the VirtualModelFactory of all VirtualModelResource
			returned.setFactory(new FMLModelFactory(returned, serviceManager));
	
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static VirtualModelResource retrieveVirtualModelResource(InJarResourceImpl inJarResource, Resource parent,
			ViewPointResource viewPointResource, FlexoServiceManager serviceManager) {
		try {
			ModelFactory factory = new ModelFactory(
					ModelContextLibrary.getCompoundModelContext(InJarFlexoIODelegate.class, VirtualModelResource.class));
			VirtualModelResourceImpl returned = (VirtualModelResourceImpl) factory.newInstance(VirtualModelResource.class);
	
			returned.setFlexoIODelegate(InJarFlexoIODelegateImpl.makeInJarFlexoIODelegate(inJarResource, factory));
			VirtualModelInfo vpi = findVirtualModelInfo(returned.getFlexoIOStreamDelegate().getInputStream());
			if (vpi == null) {
				// Unable to retrieve infos, just abort
				return null;
			}
	
			// returned.setFile(xmlFile);
			// returned.setDirectory(parent);
			returned.initName(vpi.name);
			returned.setURI(viewPointResource.getURI() + "/" + FilenameUtils.getBaseName(inJarResource.getRelativePath()));
			if (StringUtils.isNotEmpty(vpi.version)) {
				returned.setVersion(new FlexoVersion(vpi.version));
			}
			returned.setModelVersion(new FlexoVersion(StringUtils.isNotEmpty(vpi.modelVersion) ? vpi.modelVersion : "0.1"));
	
			// If ViewPointLibrary not initialized yet, we will do it later in ViewPointLibrary.initialize() method
			//if (serviceManager.getViewPointLibrary() != null) {
			//	returned.setViewPointLibrary(serviceManager.getViewPointLibrary());
			//}
	
			returned.setResourceCenter(viewPointResource.getResourceCenter());
			returned.setServiceManager(serviceManager);
	
			logger.fine("VirtualModelResource " + returned.getFlexoIODelegate().toString() + " version " + returned.getModelVersion());
	
			// TODO: the factory should be instantiated and managed by the ProjectNatureService, which should react to the registering
			// of a new TA, and which is responsible to update the VirtualModelFactory of all VirtualModelResource
			returned.setFactory(new FMLModelFactory(returned, serviceManager));
	
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}*/

}
