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

import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.DeclareEditionActions;
import org.openflexo.foundation.fml.annotations.DeclareFetchRequests;
import org.openflexo.foundation.fml.annotations.DeclareFlexoRoles;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.DeleteFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;

/**
 * Implementation of the ModelSlot for a FML {@link AbstractVirtualModelInstance} (a {@link VirtualModelInstance} or a {@link View})
 * 
 * @author sylvain, christophe
 * 
 */
@DeclareFlexoRoles({ FlexoConceptInstanceRole.class, PrimitiveRole.class })
@DeclareEditionActions({ AddFlexoConceptInstance.class, DeleteFlexoConceptInstance.class })
@DeclareFetchRequests({ SelectFlexoConceptInstance.class })
@ModelEntity
@ImplementationClass(VirtualModelInstanceModelSlot.VirtualModelInstanceModelSlotImpl.class)
@XMLElement(xmlTag = "VirtualModelInstanceModelSlot", deprecatedXMLTags = "FMLRTModelSlot")
@FML("VirtualModelInstanceModelSlot")
public interface VirtualModelInstanceModelSlot extends FMLRTModelSlot<VirtualModelInstance, VirtualModel> {

	public static abstract class VirtualModelInstanceModelSlotImpl extends FMLRTModelSlotImpl<VirtualModelInstance, VirtualModel>
			implements VirtualModelInstanceModelSlot {

		private static final Logger logger = Logger.getLogger(VirtualModelInstanceModelSlot.class.getPackage().getName());

	}
}
