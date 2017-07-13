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
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResourceFactory;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.model.exceptions.ModelDefinitionException;

/**
 * Abstract base implementation for an action which aims at creating a new {@link VirtualModelInstance} in a {@link View}
 * 
 * @author sylvain
 * 
 * @param <A>
 *            type of action, required to manage introspection for inheritance
 */
public abstract class CreateVirtualModelInstance<A extends CreateVirtualModelInstance<A>>
		extends AbstractCreateVirtualModelInstance<A, View, VirtualModelInstance, VirtualModel> {

	private static final Logger logger = Logger.getLogger(CreateVirtualModelInstance.class.getPackage().getName());

	protected CreateVirtualModelInstance(FlexoActionType<A, View, FlexoObject> actionType, View focusedObject,
			Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public VirtualModelInstanceResource makeVirtualModelInstanceResource() throws SaveResourceException {

		FMLRTTechnologyAdapter fmlRTTechnologyAdapter = getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(FMLRTTechnologyAdapter.class);
		VirtualModelInstanceResourceFactory factory = fmlRTTechnologyAdapter.getViewResourceFactory()
				.getVirtualModelInstanceResourceFactory();

		VirtualModelInstanceResource returned;
		try {
			returned = factory.makeVirtualModelInstanceResource(getNewVirtualModelInstanceName(), getVirtualModel(),
					(ViewResource) getFocusedObject().getResource(), fmlRTTechnologyAdapter.getTechnologyContextManager(), true);
			returned.getLoadedResourceData().setTitle(getNewVirtualModelInstanceTitle());
			return returned;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			return null;
		}

		// return VirtualModelInstanceImpl.newVirtualModelInstance(getNewVirtualModelInstanceName(), getNewVirtualModelInstanceTitle(),
		// getVirtualModel(), getFocusedObject());
	}

	@Override
	public boolean isValidVirtualModelInstanceName(String proposedName) {
		return getFocusedObject().isValidVirtualModelInstanceName(proposedName);
	}

	@Override
	public View getContainerVirtualModelInstance() {
		return getFocusedObject();
	}

	@Override
	public FlexoResourceCenter<?> getResourceCenter() {
		if (getFocusedObject() != null) {
			return getFocusedObject().getResourceCenter();
		}
		return null;
	}

}
