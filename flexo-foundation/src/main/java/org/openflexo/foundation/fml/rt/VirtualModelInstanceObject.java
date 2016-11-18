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

import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;

import java.util.logging.Logger;

/**
 * A {@link VirtualModelInstanceObject} is an abstract run-time concept (instance) for an object "living" in a {@link VirtualModelInstance}
 * (instanceof a {@link VirtualModel})
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(VirtualModelInstanceObject.VirtualModelInstanceObjectImpl.class)
public interface VirtualModelInstanceObject extends ViewObject, InnerResourceData<AbstractVirtualModelInstance<?, ?>> {

	@PropertyIdentifier(type = VirtualModelInstance.class)
	public static final String VIRTUAL_MODEL_INSTANCE_KEY = "virtualModelInstance";

	/**
	 * Return the {@link VirtualModelInstance} where this object is declared and living
	 * 
	 * @return
	 */
	@Getter(value = VIRTUAL_MODEL_INSTANCE_KEY)
	public abstract AbstractVirtualModelInstance<?, ?> getVirtualModelInstance();

	@Setter(VIRTUAL_MODEL_INSTANCE_KEY)
	public void setVirtualModelInstance(AbstractVirtualModelInstance<?, ?> virtualModelInstance);

	public AbstractVirtualModelInstanceModelFactory<?> getFactory();

	
	public abstract class VirtualModelInstanceObjectImpl extends ViewObjectImpl implements VirtualModelInstanceObject {

		private static final Logger logger = Logger.getLogger(VirtualModelInstanceObject.class.getPackage().getName());

		/**
		 * Return the {@link VirtualModelInstance} where this object is declared and living
		 * 
		 * @return
		 */
		@Override
		public abstract AbstractVirtualModelInstance<?, ?> getVirtualModelInstance();

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
		 * Returns FlexoResourceCenter that contains the ViewResource containing this ViewObject
		 * 
		 * @return
		 */
		@Override
		public FlexoResourceCenter<?> getResourceCenter() {
			FlexoResource<?> resource = getVirtualModelInstance().getResource();
			return resource != null ? resource.getResourceCenter() : null;
		}

		/**
		 * Return the {@link ResourceData} where this object is defined (the global functional root object giving access to the
		 * {@link FlexoResource}): this object is here the {@link VirtualModelInstance}
		 * 
		 * @return
		 */
		@Override
		public AbstractVirtualModelInstance<?, ?> getResourceData() {
			return getVirtualModelInstance();
		}

		@Override
		public AbstractVirtualModelInstanceModelFactory<?> getFactory() {
			if (getVirtualModelInstance() != null && getVirtualModelInstance().getResource() != null) {
				return ((AbstractVirtualModelInstanceResource<?, ?>) getVirtualModelInstance().getResource()).getFactory();
			}
			return null;
		}
	}
}
