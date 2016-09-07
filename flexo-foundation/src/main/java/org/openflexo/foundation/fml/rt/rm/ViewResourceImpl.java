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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.rm.AbstractVirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.rm.FileSystemResourceLocatorImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.IProgress;

/**
 * Default implementation for {@link ViewResource}
 * 
 * 
 * @author Sylvain
 * 
 */
public abstract class ViewResourceImpl extends AbstractVirtualModelInstanceResourceImpl<View, ViewPoint>implements ViewResource {

	static final Logger logger = Logger.getLogger(ViewResourceImpl.class.getPackage().getName());

	@Override
	public View getView() {
		return getVirtualModelInstance();
	}

	@Override
	public ViewPoint getViewPoint() {
		if (getViewPointResource() != null) {
			return getViewPointResource().getViewPoint();
		}
		return null;
	}

	/**
	 * Load the &quot;real&quot; load resource data of this resource.
	 * 
	 * @param progress
	 *            a progress monitor in case the resource data is not immediately available.
	 * @return the resource data.
	 * @throws ResourceLoadingCancelledException
	 * @throws ResourceDependencyLoopException
	 * @throws FileNotFoundException
	 */
	@Override
	public View loadResourceData(IProgress progress) throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
			InconsistentDataException, InvalidModelDefinitionException {
		View returned = super.loadResourceData(progress);

		return returned;
	}

	@Override
	public Class<View> getResourceDataClass() {
		return View.class;
	}

	@Override
	public FMLRTTechnologyAdapter getTechnologyAdapter() {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
		}
		return null;
	}

	@Override
	public List<VirtualModelInstanceResource> getVirtualModelInstanceResources() {
		// View view = getView();
		return getContents(VirtualModelInstanceResource.class);
	}

	/**
	 * Return the list of all {@link VirtualModelInstanceResource} defined in this {@link ViewResource} conform to supplied
	 * {@link AbstractVirtualModel}
	 * 
	 * @return
	 */
	@Override
	public List<VirtualModelInstanceResource> getVirtualModelInstanceResources(AbstractVirtualModel<?> virtualModel) {
		List<VirtualModelInstanceResource> returned = new ArrayList<VirtualModelInstanceResource>();
		for (VirtualModelInstanceResource vmiRes : getVirtualModelInstanceResources()) {
			if (virtualModel.isAssignableFrom(vmiRes.getVirtualModelResource().getVirtualModel())) {
				returned.add(vmiRes);
			}
		}
		return returned;
	}

	@Override
	public boolean delete(Object... context) {
		if (super.delete(context)) {
			getServiceManager().getResourceManager().addToFilesToDelete(ResourceLocator.retrieveResourceAsFile(getDirectory()));
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
		return ((FileFlexoIODelegate) getFlexoIODelegate()).getFile().getParentFile().getAbsolutePath();
	}

	@Override
	public View getModelData() {
		return getView();
	}

	@Override
	public View getModel() {
		return getView();
	}

	@Override
	public String computeDefaultURI() {
		if (getContainer() != null) {
			return getContainer().getURI() + "/"
					+ (getName().endsWith(ViewResourceFactory.VIEW_SUFFIX) ? getName() : getName() + ViewResourceFactory.VIEW_SUFFIX);
		}
		if (getResourceCenter() != null) {
			return getResourceCenter().getDefaultBaseURI() + "/"
					+ (getName().endsWith(ViewResourceFactory.VIEW_SUFFIX) ? getName() : getName() + ViewResourceFactory.VIEW_SUFFIX);
		}
		return null;
	}

	@Override
	public AbstractVirtualModelResource<ViewPoint> getVirtualModelResource() {
		return getViewPointResource();
	}

}
