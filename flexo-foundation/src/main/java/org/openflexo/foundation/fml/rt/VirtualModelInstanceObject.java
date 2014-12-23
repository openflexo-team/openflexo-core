/*
 * (c) Copyright 2010-2011 AgileBirds
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
package org.openflexo.foundation.fml.rt;

import java.util.logging.Logger;

import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;

/**
 * A {@link VirtualModelInstanceObject} is an abstract run-time concept (instance) for an object "living" in a {@link VirtualModelInstance}
 * (instanceof a {@link VirtualModel})
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(VirtualModelInstanceObject.VirtualModelInstanceObjectImpl.class)
public interface VirtualModelInstanceObject extends ViewObject, InnerResourceData<VirtualModelInstance> {

	@PropertyIdentifier(type = VirtualModelInstance.class)
	public static final String VIRTUAL_MODEL_INSTANCE_KEY = "virtualModelInstance";

	/**
	 * Return the {@link VirtualModelInstance} where this object is declared and living
	 * 
	 * @return
	 */
	@Getter(value = VIRTUAL_MODEL_INSTANCE_KEY)
	public abstract VirtualModelInstance getVirtualModelInstance();

	@Setter(VIRTUAL_MODEL_INSTANCE_KEY)
	public void setVirtualModelInstance(VirtualModelInstance virtualModelInstance);

	public VirtualModelInstanceModelFactory getFactory();

	public abstract class VirtualModelInstanceObjectImpl extends ViewObjectImpl implements VirtualModelInstanceObject {

		private static final Logger logger = Logger.getLogger(VirtualModelInstanceObject.class.getPackage().getName());

		/**
		 * Return the {@link VirtualModelInstance} where this object is declared and living
		 * 
		 * @return
		 */
		@Override
		public abstract VirtualModelInstance getVirtualModelInstance();

		/**
		 * Return the {@link View} where this object is declared and living
		 * 
		 * @return
		 */
		@Override
		public View getView() {
			if (getVirtualModelInstance() != null) {
				return getVirtualModelInstance().getView();
			}
			return null;
		}

		/**
		 * Return the {@link ResourceData} where this object is defined (the global functional root object giving access to the
		 * {@link FlexoResource}): this object is here the {@link VirtualModelInstance}
		 * 
		 * @return
		 */
		@Override
		public VirtualModelInstance getResourceData() {
			return getVirtualModelInstance();
		}

		@Override
		public VirtualModelInstanceModelFactory getFactory() {
			if (getVirtualModelInstance() != null && getVirtualModelInstance().getResource() != null) {
				return ((VirtualModelInstanceResource) getVirtualModelInstance().getResource()).getFactory();
			}
			return null;
		}
	}
}
