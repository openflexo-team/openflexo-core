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

package org.openflexo.foundation.fml.rt.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceFactory;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.model.exceptions.ModelDefinitionException;

/**
 * This action is called to create a regular {@link FMLRTVirtualModelInstance} either as top level in a repository folder, or as a contained
 * {@link FMLRTVirtualModelInstance} in a container {@link FMLRTVirtualModelInstance}
 * 
 * @author sylvain
 *
 * @param <T>
 *            type of container (a repository folder or a container FMLRTVirtualModelInstance)
 */
public abstract class CreateFMLRTVirtualModelInstance<A extends CreateFMLRTVirtualModelInstance<A>>
		extends AbstractCreateVirtualModelInstance<A, FlexoObject, FMLRTVirtualModelInstance, FMLRTTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(CreateFMLRTVirtualModelInstance.class.getPackage().getName());

	protected CreateFMLRTVirtualModelInstance(FlexoActionFactory<A, FlexoObject, FlexoObject> actionType, FlexoObject focusedObject,
			Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public FMLRTVirtualModelInstanceResource makeVirtualModelInstanceResource() throws SaveResourceException {

		FMLRTTechnologyAdapter fmlRTTechnologyAdapter = getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(FMLRTTechnologyAdapter.class);
		FMLRTVirtualModelInstanceResourceFactory factory = fmlRTTechnologyAdapter.getFMLRTVirtualModelInstanceResourceFactory();

		FMLRTVirtualModelInstanceResource returned = null;
		try {
			if (getContainerVirtualModelResource() != null) {
				returned = factory.makeContainedFMLRTVirtualModelInstanceResource(getNewVirtualModelInstanceName(),
						(VirtualModelResource) getVirtualModel().getResource(),
						(AbstractVirtualModelInstanceResource<?, ?>) getContainerVirtualModelInstance().getResource(),
						fmlRTTechnologyAdapter.getTechnologyContextManager(), true);
			}
			else if (getFolder() != null) {
				returned = factory.makeTopLevelFMLRTVirtualModelInstanceResource(getNewVirtualModelInstanceName(), null,
						// Let URI be automatically computed
						(VirtualModelResource) getVirtualModel().getResource(), getFolder(),
						fmlRTTechnologyAdapter.getTechnologyContextManager(), true);
			}

			if (returned != null) {
				returned.getLoadedResourceData().setTitle(getNewVirtualModelInstanceTitle());
			}

			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			return null;
		}

		// return FMLRTVirtualModelInstanceImpl.newVirtualModelInstance(getNewVirtualModelInstanceName(), getNewVirtualModelInstanceTitle(),
		// getVirtualModel(), getFocusedObject());
	}

	@Override
	public <I> RepositoryFolder<FMLRTVirtualModelInstanceResource, I> getFolder() {
		return (RepositoryFolder<FMLRTVirtualModelInstanceResource, I>) super.getFolder();
	}

	public boolean isVisible(VirtualModel virtualModel) {
		return true;
	}

}
