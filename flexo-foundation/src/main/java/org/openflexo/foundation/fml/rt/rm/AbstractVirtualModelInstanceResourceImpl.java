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

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.toolbox.IProgress;

/**
 * Default implementation for {@link AbstractVirtualModelInstanceResource}
 * 
 * 
 * @author Sylvain
 * 
 */
public abstract class AbstractVirtualModelInstanceResourceImpl<VMI extends AbstractVirtualModelInstance<VMI, VM>, VM extends AbstractVirtualModel<VM>>
		extends PamelaResourceImpl<VMI, AbstractVirtualModelInstanceModelFactory<?>>
		implements AbstractVirtualModelInstanceResource<VMI, VM> {

	static final Logger logger = Logger.getLogger(AbstractVirtualModelInstanceResourceImpl.class.getPackage().getName());

	@Override
	public VMI getVirtualModelInstance() {
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

	@Override
	public VMI loadResourceData(IProgress progress) throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException,
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
		VMI returned = super.loadResourceData(progress);

		System.out.println("Je suis " + getClass().getSimpleName());
		System.out.println("getVirtualModelResource()=" + getVirtualModelResource());
		if (this instanceof ViewResourceImpl) {
			System.out.println("getViewPointResource()=" + ((ViewResourceImpl) this).getViewPointResource());
		}

		returned.setVirtualModel(getVirtualModelResource().getVirtualModel());

		if (getContainer() != null) {
			getContainer().getView().addToVirtualModelInstances(returned);
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

		/*if (!getContainer().isDeserializing()) {
			if (getLoadedResourceData() != null && getLoadedResourceData().isSynchronizable()) {
				getLoadedResourceData().synchronize(null);
			}
		}*/

		return returned;
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
	public FMLRTTechnologyAdapter getTechnologyAdapter() {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
		}
		return null;
	}

	@Override
	public ViewResource getContainer() {
		return (ViewResource) performSuperGetter(CONTAINER);
	}
}
