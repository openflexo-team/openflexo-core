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

import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;

/**
 * A {@link VirtualModelInstance} is the run-time concept (instance) of a {@link VirtualModel}.<br>
 * 
 * As such, a {@link VirtualModelInstance} is instantiated inside a {@link View}, and all model slot defined for the corresponding
 * {@link ViewPoint} are instantiated (reified) with existing or build-in managed {@link FlexoModel}.<br>
 * 
 * A {@link VirtualModelInstance} mostly manages a collection of {@link FlexoConceptInstance} and is itself an {@link FlexoConceptInstance}.
 * <br>
 * 
 * A {@link VirtualModelInstance} might be used in the Design Space (for example to encode a Diagram)
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(VirtualModelInstance.VirtualModelInstanceImpl.class)
@XMLElement
public interface VirtualModelInstance extends AbstractVirtualModelInstance<VirtualModelInstance, VirtualModel> {

	public static abstract class VirtualModelInstanceImpl extends AbstractVirtualModelInstanceImpl<VirtualModelInstance, VirtualModel>
			implements VirtualModelInstance {

		// private static final Logger logger = Logger.getLogger(VirtualModelInstance.class.getPackage().getName());

	}

}
