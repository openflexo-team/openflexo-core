/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexodocument, a component of the software infrastructure 
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

package org.openflexo.foundation.doc.nature;

import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.fml.FlexoDocumentModelSlot;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceNature;

/**
 * Define the "controlled-document" nature of a {@link FMLRTVirtualModelInstance}<br>
 * 
 * A {@link FMLControlledDocumentVirtualModelInstanceNature} might be seen as an interpretation of a given {@link FMLRTVirtualModelInstance}
 * 
 * @author sylvain
 * 
 */
public abstract class FMLControlledDocumentVirtualModelInstanceNature<MS extends FlexoDocumentModelSlot<D>, D extends FlexoDocument<D, ?>>
		implements VirtualModelInstanceNature {

	// Prevent external instantiation
	protected FMLControlledDocumentVirtualModelInstanceNature() {
	}

	public abstract Class<MS> getModelSlotClass();

	/**
	 * Return boolean indicating if supplied {@link FMLRTVirtualModelInstance} might be interpreted as a FML-Controlled document
	 */
	public boolean hasNature(FMLRTVirtualModelInstance virtualModelInstance, FMLControlledDocumentVirtualModelNature<MS> vmNature) {

		// The corresponding VirtualModel should have FMLControlledDiagramVirtualModelNature
		if (!virtualModelInstance.getVirtualModel().hasNature(vmNature)) {
			return false;
		}

		MS documentMS = virtualModelInstance.getVirtualModel().getModelSlots(getModelSlotClass()).get(0);

		ModelSlotInstance<MS, D> msInstance = virtualModelInstance.getModelSlotInstance(documentMS);

		if (msInstance == null) {
			return false;
		}

		if (msInstance.getAccessedResourceData() == null) {
			return false;
		}

		return true;
	}

	protected MS _getModelSlot(FMLRTVirtualModelInstance virtualModelInstance) {
		return virtualModelInstance.getVirtualModel().getModelSlots(getModelSlotClass()).get(0);
	}

	protected ModelSlotInstance<MS, D> _getModelSlotInstance(FMLRTVirtualModelInstance virtualModelInstance) {

		MS documentMS = _getModelSlot(virtualModelInstance);

		return virtualModelInstance.getModelSlotInstance(documentMS);

	}

	protected D _getDocument(FMLRTVirtualModelInstance virtualModelInstance) {
		return _getModelSlotInstance(virtualModelInstance).getAccessedResourceData();
	}

}
