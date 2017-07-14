/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.fml.rt.rm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.rm.FileSystemResourceLocatorImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Default implementation for {@link FMLRTVirtualModelInstanceResource}
 * 
 * 
 * @author Sylvain
 * 
 */
public abstract class FMLRTVirtualModelInstanceResourceImpl
		extends AbstractVirtualModelInstanceResourceImpl<VirtualModelInstance, FMLRTTechnologyAdapter>
		implements FMLRTVirtualModelInstanceResource {

	static final Logger logger = Logger.getLogger(FMLRTVirtualModelInstanceResourceImpl.class.getPackage().getName());

	@Override
	public Class<VirtualModelInstance> getResourceDataClass() {
		return VirtualModelInstance.class;
	}

	@Override
	public FMLRTTechnologyAdapter getTechnologyAdapter() {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
		}
		return null;
	}

	@Override
	public List<FMLRTVirtualModelInstanceResource> getVirtualModelInstanceResources() {
		return getContents(FMLRTVirtualModelInstanceResource.class);
	}

	/**
	 * Return the list of all {@link VirtualModelInstanceResource} defined in this {@link ViewResource} conform to supplied
	 * {@link VirtualModel}
	 * 
	 * @return
	 */
	@Override
	public List<FMLRTVirtualModelInstanceResource> getVirtualModelInstanceResources(VirtualModel virtualModel) {
		List<FMLRTVirtualModelInstanceResource> returned = new ArrayList<>();
		for (FMLRTVirtualModelInstanceResource vmiRes : getVirtualModelInstanceResources()) {
			if (virtualModel.isAssignableFrom(vmiRes.getVirtualModelResource().getVirtualModel())) {
				returned.add(vmiRes);
			}
		}
		return returned;
	}

	@Override
	public boolean delete(Object... context) {
		// gets service manager before deleting otherwise the service manager is null
		FlexoServiceManager serviceManager = getServiceManager();
		Object serializationArtefact = getIODelegate().getSerializationArtefact();
		if (super.delete(context)) {
			if (serializationArtefact instanceof File) {
				serviceManager.getResourceManager().addToFilesToDelete((File) serializationArtefact);
			}
			return true;
		}
		return false;
	}

	@Override
	public Resource getDirectory() {
		String parentPath = getDirectoryPath();
		if (ResourceLocator.locateResource(parentPath) == null) {
			FileSystemResourceLocatorImpl.appendDirectoryToFileSystemResourceLocator(parentPath);
		}
		return ResourceLocator.locateResource(parentPath);
	}

	public String getDirectoryPath() {
		if (getIODelegate() instanceof FileIODelegate) {
			FileIODelegate ioDelegate = (FileIODelegate) getIODelegate();
			return ioDelegate.getFile().getParentFile().getAbsolutePath();
		}
		return "";
	}

	@Override
	public VirtualModelInstance getModelData() {
		return getVirtualModelInstance();
	}

	@Override
	public VirtualModelInstance getModel() {
		return getVirtualModelInstance();
	}

	@Override
	public String computeDefaultURI() {
		if (getContainer() != null) {
			return getContainer().getURI() + "/" + (getName().endsWith(FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX) ? getName()
					: getName() + FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX);
		}
		if (getResourceCenter() != null) {
			return getResourceCenter().getDefaultBaseURI() + "/"
					+ (getName().endsWith(FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX) ? getName()
							: getName() + FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX);
		}
		return null;
	}

	/*@Override
	public VirtualModelResource<ViewPoint> getVirtualModelResource() {
		return getViewPointResource();
	}*/

	/*private String viewpointURI;
	
	@Override
	public String getViewpointURI() {
		if (getViewPointResource() != null) {
			return getViewPointResource().getURI();
		}
		return viewpointURI;
	}
	
	@Override
	public void setViewpointURI(String viewpointURI) {
		this.viewpointURI = viewpointURI;
	}*/
}
