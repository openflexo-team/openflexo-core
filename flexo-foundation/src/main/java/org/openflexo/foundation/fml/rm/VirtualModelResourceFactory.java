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

import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.resource.FlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.model.exceptions.ModelDefinitionException;

/**
 * Implementation of PamelaResourceFactory for {@link ViewPointResource}
 * 
 * @author sylvain
 *
 */
public abstract class VirtualModelResourceFactory extends AbstractVirtualModelResourceFactory<VirtualModel, VirtualModelResource> {

	private static final Logger logger = Logger.getLogger(VirtualModelResourceFactory.class.getPackage().getName());

	public VirtualModelResourceFactory() throws ModelDefinitionException {
		super(VirtualModelResource.class);
	}

	@Override
	public VirtualModel makeEmptyResourceData(VirtualModelResource resource) {
		// TODO
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
