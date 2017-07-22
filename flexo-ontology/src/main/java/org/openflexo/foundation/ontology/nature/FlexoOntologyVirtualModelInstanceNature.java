/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
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

package org.openflexo.foundation.ontology.nature;

import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceNature;
import org.openflexo.foundation.ontology.technologyadapter.FlexoOntologyModelSlot;

/**
 * Define the "FlexoOntology" nature of a {@link FMLRTVirtualModelInstance}<br>
 * 
 * A {@link FMLRTVirtualModelInstance} with this nature has a least a {@link FlexoOntologyModelSlot}
 * 
 * @author sylvain
 * 
 */
public class FlexoOntologyVirtualModelInstanceNature implements VirtualModelInstanceNature {

	public static FlexoOntologyVirtualModelInstanceNature INSTANCE = new FlexoOntologyVirtualModelInstanceNature();

	// Prevent external instantiation
	private FlexoOntologyVirtualModelInstanceNature() {
	}

	/**
	 * Return boolean indicating if supplied {@link FMLRTVirtualModelInstance} might be interpreted as a FML-Controlled diagram
	 */
	@Override
	public boolean hasNature(FMLRTVirtualModelInstance virtualModelInstance) {

		if (virtualModelInstance == null) {
			return false;
		}

		// The corresponding VirtualModel should have FlexoOntologyVirtualModelNature
		if (virtualModelInstance.getVirtualModel().hasNature(FlexoOntologyVirtualModelNature.INSTANCE)) {
			return true;
		}

		/*List<FlexoOntologyModelSlot<?, ?, ?>> modelSlots = FlexoOntologyVirtualModelNature.getFlexoOntologyModelSlots(virtualModelInstance
				.getVirtualModel());
		
		for (FlexoOntologyModelSlot<?, ?, ?> modelSlot : modelSlots) {
			FlexoOntologyModelSlotInstance<?, ?, ?, ?> msInstance = (FlexoOntologyModelSlotInstance<?, ?, ?, ?>) virtualModelInstance
					.getModelSlotInstance(modelSlot);
			if (msInstance != null) {
				return false;
			}
			if (msInstance.getAccessedResourceData() == null) {
				return false;
			}
		
		}*/

		return false;
	}

}
