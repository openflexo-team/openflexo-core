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

package org.openflexo.foundation.fml.ta;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

@ModelEntity(isAbstract = true)
@ImplementationClass(FMLObjectRole.FMLObjectRoleImpl.class)
public interface FMLObjectRole<T extends FMLObject> extends FlexoRole<T> {

	public static abstract class FMLObjectRoleImpl<T extends FMLObject> extends FlexoRoleImpl<T> implements FMLObjectRole<T> {

		private static final Logger logger = Logger.getLogger(FMLObjectRole.class.getPackage().getName());

		/**
		 * Encodes the default cloning strategy
		 * 
		 * @return
		 */
		@Override
		public RoleCloningStrategy defaultCloningStrategy() {
			return RoleCloningStrategy.Reference;
		}

		@Override
		public boolean defaultBehaviourIsToBeDeleted() {
			return false;
		}

		@Override
		public Class<? extends TechnologyAdapter> getRoleTechnologyAdapterClass() {
			return FMLTechnologyAdapter.class;
		}

		/**
		 * Instanciate run-time-level object encoding reference to object (see {@link ActorReference})
		 * 
		 * @param object
		 *            the object which are pointing to
		 * @param fci
		 *            the {@link FlexoConceptInstance} where this {@link ActorReference} is defined
		 * 
		 */
		@Override
		public FMLObjectActorReference<T> makeActorReference(T object, FlexoConceptInstance fci) {
			AbstractVirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			FMLObjectActorReference<T> returned = factory.newInstance(FMLObjectActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(fci);
			returned.setModellingElement(object);
			return returned;
		}

	}

}
