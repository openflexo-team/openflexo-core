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

package org.openflexo.foundation.fml.rt;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.annotations.DeclareEditionActions;
import org.openflexo.foundation.fml.annotations.DeclareFetchRequests;
import org.openflexo.foundation.fml.annotations.DeclareFlexoRoles;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.CreateTopLevelVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.ExecuteFML;
import org.openflexo.foundation.fml.rt.editionaction.ExecuteFlexoBehaviour;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectUniqueFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectUniqueVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectVirtualModelInstance;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * Model slot allowing to access a {@link FMLRTVirtualModelInstance} (native implementation of a VirtualModel instance in Openflexo
 * infrastructure)
 * 
 * @author sylvain
 * 
 */
@DeclareFlexoRoles({ FlexoConceptInstanceRole.class, PrimitiveRole.class })
@DeclareEditionActions({ AddFlexoConceptInstance.class, CreateTopLevelVirtualModelInstance.class, AddVirtualModelInstance.class,
		ExecuteFlexoBehaviour.class, ExecuteFML.class })
@DeclareFetchRequests({ SelectFlexoConceptInstance.class, SelectVirtualModelInstance.class, SelectUniqueFlexoConceptInstance.class,
		SelectUniqueVirtualModelInstance.class })
@ModelEntity
@ImplementationClass(FMLRTVirtualModelInstanceModelSlot.FMLRTVirtualModelInstanceModelSlotImpl.class)
@XMLElement(xmlTag = "FMLRTVirtualModelInstanceModelSlot", deprecatedXMLTags = "ViewModelSlot,VirtualModelInstanceModelSlot")
@FML(AbstractFMLTypingSpace.MODEL_INSTANCE)
public interface FMLRTVirtualModelInstanceModelSlot extends FMLRTModelSlot<FMLRTVirtualModelInstance, FMLRTTechnologyAdapter> {

	public static abstract class FMLRTVirtualModelInstanceModelSlotImpl
			extends FMLRTModelSlotImpl<FMLRTVirtualModelInstance, FMLRTTechnologyAdapter> implements FMLRTVirtualModelInstanceModelSlot {

		private static final Logger logger = Logger.getLogger(FMLRTVirtualModelInstanceModelSlot.class.getPackage().getName());

		@Override
		public Class<FMLRTTechnologyAdapter> getTechnologyAdapterClass() {
			return FMLRTTechnologyAdapter.class;
		}

		@Override
		public String getStringRepresentation() {
			return "FMLRTVirtualModelInstanceModelSlot";
		}

		@Override
		public void handleRequiredImports(FMLCompilationUnit compilationUnit) {
			super.handleRequiredImports(compilationUnit);
			if (compilationUnit != null) {
				compilationUnit.ensureUse(FMLRTVirtualModelInstanceModelSlot.class);
			}
		}

	}
}
