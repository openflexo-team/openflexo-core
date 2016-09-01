/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.fml.rt;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;

/**
 * The {@link ViewLibrary} contains all {@link ViewResource} referenced in a {@link FlexoProject}<br>
 * 
 * This is a {@link ViewRepository} associated to a {@link FlexoProject} (the associated ResourceCenter is the project itself)
 * 
 * @author sylvain
 */

// TODO : Merge ViewRepository / ViewLibrary

public class ViewLibrary extends ViewRepository {

	private static final Logger logger = Logger.getLogger(ViewLibrary.class.getPackage().getName());

	private static final String VIEWS = "Views";

	/**
	 * Create a new ViewLibrary.
	 */
	public ViewLibrary(FlexoResourceCenter<?> rc) {
		super(rc.getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class), rc);
		getRootFolder().setName(rc.getName(),false);
		getRootFolder().setFullQualifiedPath("/");
		// exploreDirectoryLookingForViews(getDirectory(), getRootFolder());

	}

	public ViewLibrary(FMLRTTechnologyAdapter ta, FlexoResourceCenter<?> rc) {
		super(ta, rc);
		getRootFolder().setName(rc.getName());
		getRootFolder().setFullQualifiedPath("/");
		// exploreDirectoryLookingForViews(getDirectory(), getRootFolder());
	}

	@Override
	public FlexoServiceManager getServiceManager() {
		if (getResourceCenter() != null) {
			return getResourceCenter().getServiceManager();
		}
		return null;
	}

	/*public static File getExpectedViewLibraryDirectory(FlexoProject project) {
		File returned = new File(project.getProjectDirectory(), "Views");
		if (!returned.exists()) {
			returned.mkdir();
		}
		return returned;
	}*/

	/**
	 * Creates and returns a newly created view library
	 * 
	 * @return a newly created view library
	 */
	public static ViewLibrary createNewViewLibrary(FlexoProject project) {
		ViewLibrary returned = new ViewLibrary(project);
		project.registerRepository(returned, ViewLibrary.class,
				project.getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class));
		return returned;
	}
	
	public List<View> getViewsForViewPointWithURI(String vpURI) {
		List<View> views = new ArrayList<View>();
		for (ViewResource vr : getAllResources()) {
			if (vr.getViewPoint() != null && vr.getViewPointResource().getURI().equals(vpURI)) {
				views.add(vr.getView());
			}
		}
		return views;
	}

	/*public void delete(ViewResource vr) {
		logger.info("Remove view " + vr);
		unregisterResource(vr);
		vr.delete();
	}
	
	public void delete(View v) {
		delete(v.getResource());
	}*/

	public boolean isValidForANewViewName(String value) {
		if (value == null) {
			return false;
		}
		return getRootFolder().isValidResourceName(value);
	}

	public ViewResource getViewResourceNamed(String value) {
		if (value == null) {
			return null;
		}
		return getRootFolder().getResourceWithName(value);
	}

	public ViewResource getView(String viewURI) {
		if (viewURI == null) {
			return null;
		}
		return getResource(viewURI);
	}

	public AbstractVirtualModelInstanceResource<?, ?> getVirtualModelInstance(String virtualModelInstanceURI) {
		if (virtualModelInstanceURI == null) {
			return null;
		}
		if (getView(virtualModelInstanceURI) != null) {
			return getView(virtualModelInstanceURI);
		}
		// System.out.println("lookup mvi " + virtualModelInstanceURI);
		String viewURI = virtualModelInstanceURI.substring(0, virtualModelInstanceURI.lastIndexOf("/"));
		// System.out.println("lookup view " + viewURI);
		ViewResource vr = getView(viewURI);
		if (vr != null) {
			for (AbstractVirtualModelInstanceResource<?, ?> vmir : vr.getContents(AbstractVirtualModelInstanceResource.class)) {
				if (vmir.getURI().equals(virtualModelInstanceURI)) {
					// System.out.println("Found " + vmir);
					return vmir;
				}
			}
		}
		else {
			logger.warning("Cannot find View " + viewURI);
		}
		logger.warning("Cannot find VirtualModelInstance " + virtualModelInstanceURI);
		return null;
	}

	@Override
	public String getDefaultBaseURI() {
		return getResourceCenter().getDefaultBaseURI() + "/" + VIEWS;
	}

}
