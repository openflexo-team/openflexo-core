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

import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.DeclareActorReferences;
import org.openflexo.foundation.fml.annotations.DeclareEditionActions;
import org.openflexo.foundation.fml.annotations.DeclareFlexoRoles;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * A {@link ModelSlot} allowing to reflexively access an FML language<br>
 * 
 * @author sylvain
 *
 */
@DeclareFlexoRoles({ FMLDataBindingRole.class, FlexoConceptRole.class, FlexoPropertyRole.class, PrimitiveRoleRole.class,
		FlexoConceptInstanceRoleRole.class, FlexoBehaviourRole.class, ActionSchemeRole.class })
@DeclareEditionActions({ CreateFlexoConcept.class, CreateTopLevelVirtualModel.class, CreateContainedVirtualModel.class,
		CreatePrimitiveRole.class, CreateFlexoConceptInstanceRole.class, CreateFlexoBehaviour.class })
// @DeclareFetchRequests({ SelectFlexoConceptInstance.class, SelectVirtualModelInstance.class })
@DeclareActorReferences({ FMLModelSlotInstance.class, FMLObjectActorReference.class, FMLDataBindingActorReference.class })
@ModelEntity
@ImplementationClass(FMLModelSlot.FMLModelSlotImpl.class)
@XMLElement
@FML(
		value = "FMLModelSlot",
		description = "<html>This ModelSlot represents access to a FML compilation unit<br>"
				+ "Such model slot allows reflective operations " + "</html>")
public interface FMLModelSlot extends ModelSlot<FMLCompilationUnit> {

	public static abstract class FMLModelSlotImpl extends ModelSlotImpl<FMLCompilationUnit> implements FMLModelSlot {

		private static final Logger logger = Logger.getLogger(FMLModelSlot.class.getPackage().getName());

		@Override
		public <PR extends FlexoRole<?>> String defaultFlexoRoleName(Class<PR> flexoRoleClass) {
			if (FlexoConceptRole.class.isAssignableFrom(flexoRoleClass)) {
				return "concept";
			}
			if (FlexoPropertyRole.class.isAssignableFrom(flexoRoleClass)) {
				return "property";
			}
			if (FlexoBehaviourRole.class.isAssignableFrom(flexoRoleClass)) {
				return "behaviour";
			}
			logger.warning("Unexpected role: " + flexoRoleClass.getName());
			return null;
		}

		@Override
		public Type getType() {
			return VirtualModel.class;
		}

		@Override
		public void setType(Type type) {
			// Not applicable
		}

		@Override
		public String getTypeDescription() {
			return "FMLCompilationUnit";
		};

		/**
		 * 
		 * @param msInstance
		 * @param o
		 * @return URI as String
		 */
		@Override
		public String getURIForObject(FMLCompilationUnit resourceData, Object o) {
			logger.warning("This method should be refined by child classes");
			return null;
		}

		/**
		 * @param msInstance
		 * @param objectURI
		 * @return the Object
		 */
		@Override
		public Object retrieveObjectWithURI(FMLCompilationUnit resourceData, String objectURI) {
			logger.warning("This method should be refined by child classes");
			return null;
		}

		@Override
		public String getModelSlotDescription() {
			return "Virtual Model";
		}

		@Override
		public FMLModelSlotInstance makeActorReference(FMLCompilationUnit virtualModel, FlexoConceptInstance fci) {
			AbstractVirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			FMLModelSlotInstance returned = factory.newInstance(FMLModelSlotInstance.class);
			returned.setModelSlot(this);
			returned.setFlexoConceptInstance(fci);
			returned.setVirtualModelURI(virtualModel.getVirtualModel().getURI());
			return returned;
		}

		@Override
		public Class<FMLTechnologyAdapter> getTechnologyAdapterClass() {
			return FMLTechnologyAdapter.class;
		}

	}

}
