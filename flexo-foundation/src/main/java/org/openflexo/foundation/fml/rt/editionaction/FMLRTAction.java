/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2013 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.fml.rt.editionaction;

import java.util.logging.Logger;

import org.openflexo.antar.binding.DataBinding;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.ViewObject;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;

/**
 * This action is used to handle data inside a {@link VirtualModelInstance}
 * 
 * @author sylvain
 * 
 */

@ModelEntity(isAbstract = true)
@ImplementationClass(FMLRTAction.FMLRTActionImpl.class)
public interface FMLRTAction<T extends ViewObject> extends TechnologySpecificAction<FMLRTModelSlot, T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_INSTANCE_KEY = "virtualModelInstance";

	@Getter(value = VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLAttribute
	public DataBinding<VirtualModelInstance> getVirtualModelInstance();

	@Setter(VIRTUAL_MODEL_INSTANCE_KEY)
	public void setVirtualModelInstance(DataBinding<VirtualModelInstance> virtualModelInstance);

	public static abstract class FMLRTActionImpl<T extends ViewObject> extends TechnologySpecificActionImpl<FMLRTModelSlot, T> implements
			FMLRTAction<T> {

		static final Logger logger = Logger.getLogger(FMLRTAction.class.getPackage().getName());

		private DataBinding<VirtualModelInstance> virtualModelInstance;

		@Override
		public DataBinding<VirtualModelInstance> getVirtualModelInstance() {
			if (virtualModelInstance == null) {
				virtualModelInstance = new DataBinding<VirtualModelInstance>(this, VirtualModelInstance.class,
						DataBinding.BindingDefinitionType.GET);
				virtualModelInstance.setBindingName("virtualModelInstance");
			}
			return virtualModelInstance;
		}

		@Override
		public void setVirtualModelInstance(DataBinding<VirtualModelInstance> aVirtualModelInstance) {
			if (aVirtualModelInstance != null) {
				aVirtualModelInstance.setOwner(this);
				aVirtualModelInstance.setBindingName("virtualModelInstance");
				aVirtualModelInstance.setDeclaredType(VirtualModelInstance.class);
				aVirtualModelInstance.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.virtualModelInstance = aVirtualModelInstance;
		}

	}

}
