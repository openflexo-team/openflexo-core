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

package org.openflexo.foundation.fml;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.PrimitiveActorReference;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

@ModelEntity
@ImplementationClass(PrimitiveRole.PrimitiveRoleImpl.class)
@XMLElement
@FML("PrimitiveRole")
public interface PrimitiveRole<T> extends FlexoRole<T> {

	@PropertyIdentifier(type = PrimitiveType.class)
	public static final String PRIMITIVE_TYPE_KEY = "primitiveType";

	@Getter(value = PRIMITIVE_TYPE_KEY)
	@XMLAttribute
	public PrimitiveType getPrimitiveType();

	@Setter(PRIMITIVE_TYPE_KEY)
	public void setPrimitiveType(PrimitiveType primitiveType);

	public static abstract class PrimitiveRoleImpl<T> extends FlexoRoleImpl<T> implements PrimitiveRole<T> {

		protected static final Logger logger = FlexoLogger.getLogger(PrimitiveRole.class.getPackage().getName());

		private PrimitiveType primitiveType;

		public PrimitiveRoleImpl() {
			super();
		}

		/*@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("FlexoRole " + getName() + " as " + getTypeDescription() + " cardinality=" + getCardinality() + ";", context);
			return out.toString();
		}*/

		@Override
		public PrimitiveType getPrimitiveType() {
			return primitiveType;
		}

		@Override
		public void setPrimitiveType(PrimitiveType primitiveType) {
			if (requireChange(getPrimitiveType(), primitiveType)) {
				PrimitiveType oldValue = this.primitiveType;
				this.primitiveType = primitiveType;
				notifyChange(PRIMITIVE_TYPE_KEY, oldValue, primitiveType);
				notifyResultingTypeChanged();
			}
		}

		@Override
		public String getTypeDescription() {
			if (primitiveType == null) {
				return null;
			}
			return FlexoLocalization.getMainLocalizer().localizedForKey(primitiveType.name());
		}

		@Override
		public Type getType() {
			if (primitiveType == null) {
				return null;
			}
			return primitiveType.getType();

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
		public ActorReference<T> makeActorReference(T object, FlexoConceptInstance fci) {
			AbstractVirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			PrimitiveActorReference<T> returned = factory.newInstance(PrimitiveActorReference.class);
			returned.setFlexoRole(this);
			returned.setModellingElement(object);
			return returned;
		}

		@Override
		public Class<? extends TechnologyAdapter> getRoleTechnologyAdapterClass() {
			return FMLRTTechnologyAdapter.class;
		}

	}
}
