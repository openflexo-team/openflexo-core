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

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * Represents access to a {@link DataBinding}
 * 
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(FMLDataBindingRole.FMLDataBindingRoleImpl.class)
@XMLElement
@FML("JavaRole")
public interface FMLDataBindingRole extends FlexoRole<DataBinding> {

	@PropertyIdentifier(type = Type.class)
	public static final String DECLARED_TYPE_KEY = "declaredType";

	@Getter(value = DECLARED_TYPE_KEY, isStringConvertable = true)
	@XMLAttribute
	public Type getDeclaredType();

	@Setter(DECLARED_TYPE_KEY)
	public void setDeclaredType(Type type);

	public static abstract class FMLDataBindingRoleImpl extends FlexoRoleImpl<DataBinding> implements FMLDataBindingRole {

		protected static final Logger logger = FlexoLogger.getLogger(FMLDataBindingRole.class.getPackage().getName());

		private Type declaredType;

		@Override
		public Type getType() {
			// TODO: exact type with accessed type as generics ?
			return DataBinding.class;
		}

		@Override
		public Type getDeclaredType() {
			return declaredType;
		}

		@Override
		public void setDeclaredType(Type declaredType) {
			if (requireChange(getDeclaredType(), declaredType)) {
				Type oldValue = this.declaredType;
				this.declaredType = declaredType;
				notifyChange(DECLARED_TYPE_KEY, oldValue, declaredType);
				notifyResultingTypeChanged();
			}
		}

		/**
		 * Encodes the default cloning strategy
		 * 
		 * @return
		 */
		@Override
		public RoleCloningStrategy defaultCloningStrategy() {
			return RoleCloningStrategy.Clone;
		}

		@Override
		public boolean defaultBehaviourIsToBeDeleted() {
			return true;
		}

		@Override
		public ActorReference<DataBinding> makeActorReference(DataBinding object, FlexoConceptInstance fci) {
			AbstractVirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			FMLDataBindingActorReference returned = factory.newInstance(FMLDataBindingActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(fci);
			returned.setModellingElement(object);
			return returned;
		}

		@Override
		public Class<? extends TechnologyAdapter> getRoleTechnologyAdapterClass() {
			return FMLTechnologyAdapter.class;
		}

	}
}
