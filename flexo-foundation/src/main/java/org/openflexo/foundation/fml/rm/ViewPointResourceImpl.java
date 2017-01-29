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

package org.openflexo.foundation.fml.rm;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoXMLFileResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;

public abstract class ViewPointResourceImpl extends AbstractVirtualModelResourceImpl<ViewPoint>implements ViewPointResource {

	static final Logger logger = Logger.getLogger(FlexoXMLFileResourceImpl.class.getPackage().getName());

	@Override
	public ViewPoint getViewPoint() {

		try {
			return getResourceData(null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ResourceLoadingCancelledException e) {
			e.printStackTrace();
		} catch (FlexoException e) {
			e.printStackTrace();
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
	public ViewPoint loadResourceData(IProgress progress) throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
			InconsistentDataException, InvalidModelDefinitionException {

		ViewPointImpl returned = (ViewPointImpl) super.loadResourceData(progress);

		returned.clearIsModified();

		// Now we have to activate all required technologies
		activateRequiredTechnologies();

		return returned;
	}

	@Override
	public Class<ViewPoint> getResourceDataClass() {
		return ViewPoint.class;
	}

	/**
	 * Return flag indicating if this resource is loadable<br>
	 * By default, such resource is loadable if based on 1.6 architecture (model version greater or equals to 1.0)
	 * 
	 * @return
	 */
	@Override
	public boolean isLoadable() {
		return super.isLoadable() && !isDeprecatedVersion();
	}

	@Override
	public VirtualModelResource getVirtualModelResource(String virtualModelNameOrURI) {
		for (VirtualModelResource vmRes : getVirtualModelResources()) {
			if (vmRes.getName().equals(virtualModelNameOrURI) || vmRes.getURI().equals(virtualModelNameOrURI)) {
				return vmRes;
			}
		}
		return null;
	}

	@Override
	public boolean isDeprecatedVersion() {
		if (getModelVersion() == null) {
			return true;
		}
		return getModelVersion().isLesserThan(new FlexoVersion("1.0"));
	}

	@Override
	public List<VirtualModelResource> getVirtualModelResources() {
		// We try not to load the ViewPoint yet
		// getViewPoint();
		return getContents(VirtualModelResource.class);
	}

	@Override
	public ViewPointLibrary getViewPointLibrary() {
		ViewPointLibrary returned = (ViewPointLibrary) performSuperGetter(VIEW_POINT_LIBRARY);
		if (returned == null && getServiceManager() != null) {
			return getServiceManager().getViewPointLibrary();
		}
		return returned;
	}

	@Override
	public void gitSave() {

	}

	@Override
	public void addToContents(FlexoResource<?> resource) {
		performSuperAdder(CONTENTS, resource);
		notifyContentsAdded(resource);
		/*if (resource instanceof VirtualModelResource) {
			System.out.println("getViewPoint()=" + getViewPoint());
			getViewPoint().addToVirtualModels(((VirtualModelResource) resource).getVirtualModel());
		}*/
	}
	
	@Override
	public void removeFromContents(FlexoResource<?> resource) {
		performSuperRemover(CONTENTS, resource);
		/*if (resource instanceof VirtualModelResource) {
			getViewPoint().removeFromVirtualModels(((VirtualModelResource) resource).getVirtualModel());
		}*/
	}
}
