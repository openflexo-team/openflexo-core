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

package org.openflexo.foundation.fml.rt.editionaction;

import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.editionaction.UniqueFetchRequest;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * A {@link FetchRequest} allowing to retrieve a unique {@link FlexoConceptInstance} matching some conditions and a given
 * {@link FlexoConcept}.<br>
 * 
 * Unicity must be guaranteed by business logic (semantics of build models).
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(AbstractSelectFlexoConceptInstance.AbstractSelectFlexoConceptInstanceImpl.class)
@XMLElement
@FML("SelectUniqueFlexoConceptInstance")
public interface SelectUniqueFlexoConceptInstance<VMI extends VirtualModelInstance<VMI, FMLRTTechnologyAdapter>>
		extends AbstractSelectFlexoConceptInstance<VMI, FlexoConceptInstance>,
		UniqueFetchRequest<FMLRTModelSlot<VMI, FMLRTTechnologyAdapter>, VMI, FlexoConceptInstance> {
}
