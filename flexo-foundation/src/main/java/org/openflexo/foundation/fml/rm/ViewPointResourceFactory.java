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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Logger;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.resource.DirectoryBasedFlexoIODelegate;
import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.DirectoryBasedFlexoIODelegate.DirectoryBasedFlexoIODelegateImpl;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.InJarFlexoIODelegate;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.InJarResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.xml.XMLRootElementInfo;
import org.openflexo.xml.XMLRootElementReader;

/**
 * Implementation of PamelaResourceFactory for {@link ViewPointResource}
 * 
 * @author sylvain
 *
 */
public abstract class ViewPointResourceFactory extends AbstractVirtualModelResourceFactory<ViewPoint, ViewPointResource> {

	private static final Logger logger = Logger.getLogger(ViewPointResourceFactory.class.getPackage().getName());

	public static final String VIEWPOINT_SUFFIX = ".viewpoint";

	private static XMLRootElementReader reader = new XMLRootElementReader();

	public ViewPointResourceFactory() throws ModelDefinitionException {
		super(ViewPointResource.class);
	}

	@Override
	public ViewPoint makeEmptyResourceData(ViewPointResource resource) {
		// TODO
		return null;
	}

	@Override
	public <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		return resourceCenter.retrieveName(serializationArtefact).endsWith(VIEWPOINT_SUFFIX);
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
		exploreVirtualModels(resource, resource.getFlexoIODelegate().getSerializationArtefactAsResource());

		return resource;

	}

	@Override
	protected <I> ViewPointResource initResourceForCreation(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager, String uri) throws ModelDefinitionException {
		ViewPointResource returned = super.initResourceForCreation(serializationArtefact, resourceCenter, technologyContextManager, uri);

		// SGU: why this ???
		returned.setVersion(new FlexoVersion("0.1"));
		returned.setModelVersion(new FlexoVersion("1.0"));

		return returned;
	}

	@Override
	protected <I> ViewPointResource initResourceForRetrieving(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager, String uri) throws ModelDefinitionException {

		ViewPointResource returned = super.initResourceForRetrieving(serializationArtefact, resourceCenter, technologyContextManager, uri);

		// TODO: refactor this
		if (serializationArtefact instanceof File) {
			String artefactName = resourceCenter.retrieveName(serializationArtefact);
			String baseName = artefactName.substring(0, artefactName.length() - VIEWPOINT_SUFFIX.length());
			File xmlFile = new File((File) serializationArtefact, baseName + CORE_FILE_SUFFIX);
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

		}

		// SGU: why this ???
		returned.setModelVersion(new FlexoVersion("1.0"));

		return returned;
	}

	@Override
	protected <I> FlexoIODelegate<I> makeFlexoIODelegate(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		return resourceCenter.makeDirectoryBasedFlexoIODelegate(serializationArtefact, VIEWPOINT_SUFFIX, CORE_FILE_SUFFIX, this);
	}

	private void exploreVirtualModels(ViewPointResource viewPointResource, Resource parent) {
		XMLRootElementInfo result = null;

			if (parent == null) {
			return;
		}
		
		for (Resource child : parent.getContents()) {
			if (child.isContainer()) {
				exploreVirtualModels(viewPointResource, child);
			}
			else {
				try {
					if (child.getURI().endsWith(".xml")) {
						result = reader.readRootElement(child);
						// Serialization artefact is File
						if (result.getName().equals("VirtualModel") && child instanceof FileResourceImpl) {
							VirtualModelResource virtualModelResource = VirtualModelResourceImpl.retrieveVirtualModelResource(
									((FileResourceImpl) child).getFile().getParentFile(), this, viewPointResource.getServiceManager());
							viewPointResource.addToContents(virtualModelResource);
						}
						// Serialization artefact is InJarResource
						else if (result.getName().equals("VirtualModel") && viewPointResource.getFlexoIODelegate() instanceof InJarFlexoIODelegate) {
							VirtualModelResource virtualModelResource = VirtualModelResourceImpl
									.retrieveVirtualModelResource((InJarResourceImpl) child, parent, this, viewPointResource.getServiceManager());
							viewPointResource.addToContents(virtualModelResource);
						}
					}
				} catch (IOException e) {
					logger.warning("Unexpected IOException while reading " + child);
					e.printStackTrace();
				}

			}

		}

	private static class ViewPointInfo {
		public String uri;
		public String version;
		public String name;
		public String modelVersion;
	}

	private static ViewPointInfo findViewPointInfo(InputStream inputStream) {
		Document document;
		try {
			document = PamelaResourceImpl.readXMLInputStream(inputStream);
			Element root = PamelaResourceImpl.getElement(document, "ViewPoint");
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
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.fine("Returned null");
		return null;
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

	@Override
	public <I> ViewPointResource retrieveResource(I serializationArtefact, FlexoResourceCenter<I> resourceCenter,
			TechnologyContextManager<FMLTechnologyAdapter> technologyContextManager) throws ModelDefinitionException {
		// TODO Auto-generated method stub
		return super.retrieveResource(serializationArtefact, resourceCenter, technologyContextManager);
	}

	public static ViewPointResource retrieveViewPointResource(File viewPointDirectory, FlexoResourceCenter<?> resourceCenter,
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
	}

}
