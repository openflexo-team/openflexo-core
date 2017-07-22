/**
 * 
 * Copyright (c) 2015, Openflexo
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

package org.openflexo.foundation.fml.rt.editionaction;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;

/**
 * This action is used to handle data inside a {@link FMLRTVirtualModelInstance}
 * 
 * @author sylvain
 * 
 * @param <T>
 *            type of object beeing handled by this action
 * @param <VMI>
 *            type of the container of object beeing handled by this action
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FMLRTAction.FMLRTActionImpl.class)
public interface FMLRTAction<T extends VirtualModelInstanceObject, VMI extends VirtualModelInstance<VMI, ?>>
		extends TechnologySpecificAction<FMLRTModelSlot<VMI, ?>, VMI, T> {

	/*@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_INSTANCE_KEY = "virtualModelInstance";
	
	@Getter(value = VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLAttribute
	public DataBinding<VMI> getVirtualModelInstance();
	
	@Setter(VIRTUAL_MODEL_INSTANCE_KEY)
	public void setVirtualModelInstance(DataBinding<VMI> virtualModelInstance);*/

	public static final String DEPRECATED_VIRTUAL_MODEL_INSTANCE_KEY = "virtualModelInstance";

	// TODO: remove from new releases after 1.8.1
	@Deprecated
	@Getter(value = DEPRECATED_VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLAttribute(xmlTag = "virtualModelInstance")
	public String getDeprecatedVirtualModelInstance();

	// TODO: remove from new releases after 1.8.1
	@Setter(DEPRECATED_VIRTUAL_MODEL_INSTANCE_KEY)
	public void setDeprecatedVirtualModelInstance(String virtualModelInstanceAsString);

	public abstract Class<VMI> getVirtualModelInstanceClass();

	/**
	 * Return type of FMLRTVirtualModelInstance, when {@link #getVirtualModelInstance()} is set and valid
	 * 
	 * @return
	 */
	public VirtualModel getOwnerVirtualModelType();

	public static abstract class FMLRTActionImpl<T extends VirtualModelInstanceObject, VMI extends VirtualModelInstance<VMI, ?>>
			extends TechnologySpecificActionImpl<FMLRTModelSlot<VMI, ?>, VMI, T> implements FMLRTAction<T, VMI> {

		static final Logger logger = Logger.getLogger(FMLRTAction.class.getPackage().getName());

		@Override
		public void setDeprecatedVirtualModelInstance(String virtualModelInstanceAsString) {

			if (virtualModelInstanceAsString != null) {
				getReceiver().setUnparsedBinding(virtualModelInstanceAsString);
				notifiedBindingChanged(getReceiver());
			}
		}

		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			if (getServiceManager() != null) {
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
			}
			return super.getModelSlotTechnologyAdapter();
		}

		@Override
		public VirtualModel getOwnerVirtualModelType() {
			if (getReceiver().isSet() && getReceiver().isValid()) {
				Type type = getReceiver().getAnalyzedType();
				if (type instanceof VirtualModelInstanceType) {
					return ((VirtualModelInstanceType) type).getVirtualModel();
				}
			}
			return null;
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			super.notifiedBindingChanged(dataBinding);
			if (dataBinding == getReceiver()) {
				getPropertyChangeSupport().firePropertyChange("ownerVirtualModelType", null, getOwnerVirtualModelType());
			}
		}

	}

}
