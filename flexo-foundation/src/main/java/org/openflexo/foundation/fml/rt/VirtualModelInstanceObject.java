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

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * A {@link VirtualModelInstanceObject} is an abstract run-time concept (instance) for an object "living" in a
 * {@link FMLRTVirtualModelInstance} (instanceof a {@link VirtualModel})
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(VirtualModelInstanceObject.VirtualModelInstanceObjectImpl.class)
public interface VirtualModelInstanceObject extends InnerResourceData<VirtualModelInstance<?, ?>>, FlexoObject {

	/**
	 * Return the {@link FMLRTVirtualModelInstance} where this object is declared and living
	 * 
	 * @return
	 */
	public abstract VirtualModelInstance<?, ?> getVirtualModelInstance();

	public AbstractVirtualModelInstanceModelFactory<?> getFactory();

	/**
	 * Returns FlexoResourceCenter where resides related resource
	 * 
	 * @return
	 */
	public FlexoResourceCenter<?> getResourceCenter();

	public abstract class VirtualModelInstanceObjectImpl extends FlexoObjectImpl implements VirtualModelInstanceObject {

		private static final Logger logger = Logger.getLogger(VirtualModelInstanceObject.class.getPackage().getName());

		/**
		 * Return the {@link FMLRTVirtualModelInstance} where this object is declared and living
		 * 
		 * @return
		 */
		@Override
		public abstract VirtualModelInstance<?, ?> getVirtualModelInstance();

		/**
		 * Returns FlexoResourceCenter that contains the ViewResource containing this ViewObject
		 * 
		 * @return
		 */
		@Override
		public FlexoResourceCenter<?> getResourceCenter() {
			VirtualModelInstance<?, ?> virtualModelInstance = getVirtualModelInstance();
			if (virtualModelInstance == null)
				return null;
			FlexoResource<?> resource = virtualModelInstance.getResource();
			if (resource == null)
				return null;
			return resource.getResourceCenter();
		}

		/**
		 * Return the {@link ResourceData} where this object is defined (the global functional root object giving access to the
		 * {@link FlexoResource}): this object is here the {@link FMLRTVirtualModelInstance}
		 * 
		 * @return
		 */
		@Override
		public VirtualModelInstance<?, ?> getResourceData() {
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
