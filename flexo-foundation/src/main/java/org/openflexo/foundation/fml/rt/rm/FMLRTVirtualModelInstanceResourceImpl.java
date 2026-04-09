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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelFactory;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.PamelaXMLSerializableResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.rm.Resource;

/**
 * Default implementation for {@link FMLRTVirtualModelInstanceResource}
 * 
 * 
 * @author Sylvain
 * 
 */
public abstract class FMLRTVirtualModelInstanceResourceImpl

		extends PamelaXMLSerializableResourceImpl<FMLRTVirtualModelInstance, FMLRTVirtualModelInstanceModelFactory>
		implements FMLRTVirtualModelInstanceResource {

	static final Logger logger = Logger.getLogger(FMLRTVirtualModelInstanceResourceImpl.class.getPackage().getName());

	@Override
	public VirtualModel getVirtualModel() {
		if (getVirtualModelResource() != null) {
			return getVirtualModelResource().getCompilationUnit().getVirtualModel();
		}
		return null;
	}

	@Override
	public FMLRTVirtualModelInstance getVirtualModelInstance() {
		try {
			return getResourceData();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ResourceLoadingCancelledException e) {
			e.printStackTrace();
		} catch (FlexoException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public FMLRTVirtualModelInstance loadResourceData() throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
			InconsistentDataException, InvalidModelDefinitionException {
		// We notify a deserialization start on ViewPoint AND VirtualModel, to avoid addToVirtualModel() and setViewPoint() to notify
		// UndoManager
		boolean containerWasDeserializing = (getContainer() != null && getContainer().isDeserializing());
		if (!containerWasDeserializing) {
			if (getContainer() != null) {
				getContainer().startDeserializing();
			}
		}
		startDeserializing();

		// Another chance to retrieve VirtualModel
		if (getVirtualModelResource() == null && StringUtils.isNotEmpty(getVirtualModelURI())) {
			CompilationUnitResource vmResource = getServiceManager().getVirtualModelLibrary()
					.getCompilationUnitResource(getVirtualModelURI());
			setVirtualModelResource(vmResource);
		}

		VirtualModel virtualModel = null;
		if (getVirtualModelResource() != null) {
			virtualModel = getVirtualModelResource().getCompilationUnit().getVirtualModel();
		}

		FMLRTVirtualModelInstance returned = super.loadResourceData();

		if (virtualModel != null) {
			returned.setVirtualModel(virtualModel);
		}

		if (getContainer() != null && getContainer().getVirtualModelInstance() != null) {
			getContainer().getVirtualModelInstance().addToVirtualModelInstances(returned);
		}
		returned.clearIsModified();
		/*if (returned.isSynchronizable()) {
			returned.synchronize(null);
		}*/
		// And, we notify a deserialization stop
		stopDeserializing();
		if (!containerWasDeserializing) {
			if (getContainer() != null) {
				getContainer().stopDeserializing();
			}
		}

		returned.reindexAllConceptInstances();

		/*if (!getContainer().isDeserializing()) {
			if (getLoadedResourceData() != null && getLoadedResourceData().isSynchronizable()) {
				getLoadedResourceData().synchronize(null);
			}
		}*/

		if (returned.getFMLRunTimeEngine() != null) {
			// TODO: today FMLRTVirtualModelInstance is a RunTimeEvaluationContext
			// TODO: design issue, we should separate FlexoConceptInstance from RunTimeEvaluationContext
			// This inheritance should disappear
			returned.getFMLRunTimeEngine().addToExecutionContext(returned, returned);
		}

		return returned;
	}

	@Override
	public void unloadResourceData(boolean deleteResourceData) {
		if (getLoadedResourceData().getFMLRunTimeEngine() != null) {
			// TODO: today FMLRTVirtualModelInstance is a RunTimeEvaluationContext
			// TODO: design issue, we should separate FlexoConceptInstance from RunTimeEvaluationContext
			// This inheritance should disappear
			getLoadedResourceData().getFMLRunTimeEngine().removeFromExecutionContext(getLoadedResourceData(), getLoadedResourceData());
		}
		super.unloadResourceData(deleteResourceData);
	}

	@Override
	public void setLoading(boolean isLoading) {
		super.setLoading(isLoading);
		// Just after the loading occurs, apply synchronization.
		if (!isLoading()) {
			if (getLoadedResourceData() != null && getLoadedResourceData().isSynchronizable()) {
				getLoadedResourceData().synchronize(null);
			}
		}
	}

	/*@Override
	public FMLRTVirtualModelInstanceResource getContainer() {
		return (FMLRTVirtualModelInstanceResource) performSuperGetter(CONTAINER);
	}*/

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

	/////////////////////////////////////////////////////////////

	/**
	 * Return displayable name for this FlexoResource<br>
	 * Overrides default dehaviour by using renderer of represented data when loaded
	 * 
	 * @return
	 */
	@Override
	public String getDisplayName() {
		// logger.info("VMI " + getName() + " isLoading=" + isLoading() + " isLoaded=" + isLoaded());
		if (isLoading()) {
			return super.getDisplayName() + "[Loading]";
		}
		if (isLoaded() && getLoadedResourceData().getFlexoConcept() != null
				&& getLoadedResourceData().getFlexoConcept().getInspector() != null
				&& getLoadedResourceData().getFlexoConcept().getInspector().getRenderer().isValid()) {
			return getLoadedResourceData().getStringRepresentation();
		}
		return super.getDisplayName();
	}

	@Override
	public Class<FMLRTVirtualModelInstance> getResourceDataClass() {
		return FMLRTVirtualModelInstance.class;
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
			if (virtualModel.isAssignableFrom(vmiRes.getVirtualModelResource().getCompilationUnit().getVirtualModel())) {
				returned.add(vmiRes);
			}
		}
		return returned;
	}

	@Override
	public Resource getDirectory() {
		if (getIODelegate() != null && getIODelegate().getSerializationArtefactAsResource() != null) {
			return getIODelegate().getSerializationArtefactAsResource().getContainer();
		}
		return null;
	}

	@Override
	public String computeDefaultURI() {
		if (getContainer() != null) {
			return getContainer().getURI() + (!getContainer().getURI().endsWith("/") ? "/" : "")
					+ (getName().endsWith(FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX) ? getName()
							: getName() + FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX);
		}
		if (getResourceCenter() != null && getResourceCenter().getDefaultBaseURI() != null) {
			return getResourceCenter().getDefaultBaseURI() + (!getResourceCenter().getDefaultBaseURI().endsWith("/") ? "/" : "")
					+ (getName().endsWith(FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX) ? getName()
							: getName() + FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX);
		}
		return null;
	}

	@Override
	public Class<FMLRTTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLRTTechnologyAdapter.class;
	}

	private String virtualModelURI;

	@Override
	public String getVirtualModelURI() {
		if (getVirtualModelResource() != null) {
			return getVirtualModelResource().getURI();
		}
		return virtualModelURI;
	}

	@Override
	public void setVirtualModelURI(String virtualModelURI) {
		this.virtualModelURI = virtualModelURI;
	}

}
