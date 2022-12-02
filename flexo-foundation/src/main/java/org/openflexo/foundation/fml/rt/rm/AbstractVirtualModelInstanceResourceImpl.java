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

package org.openflexo.foundation.fml.rt.rm;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.PamelaXMLSerializableResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Default implementation for {@link AbstractVirtualModelInstanceResource}
 * 
 * 
 * @author Sylvain
 * 
 */
public abstract class AbstractVirtualModelInstanceResourceImpl<VMI extends VirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter<TA>>
		extends PamelaXMLSerializableResourceImpl<VMI, AbstractVirtualModelInstanceModelFactory<?>>
		implements AbstractVirtualModelInstanceResource<VMI, TA> {

	static final Logger logger = Logger.getLogger(AbstractVirtualModelInstanceResourceImpl.class.getPackage().getName());

	@Override
	public VirtualModel getVirtualModel() {
		if (getVirtualModelResource() != null) {
			return getVirtualModelResource().getCompilationUnit().getVirtualModel();
		}
		return null;
	}

	@Override
	public VMI getVirtualModelInstance() {
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
	public VMI loadResourceData() throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException, InconsistentDataException,
			InvalidModelDefinitionException {
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

		VMI returned = super.loadResourceData();

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

	@Override
	public AbstractVirtualModelInstanceResource<?, ?> getContainer() {
		return (AbstractVirtualModelInstanceResource<?, ?>) performSuperGetter(CONTAINER);
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

}
