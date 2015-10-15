/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

import org.openflexo.foundation.doc.fml.FlexoDocumentModelSlot;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelNature;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;

/**
 * Define the "controlled-document" nature of a {@link VirtualModel}<br>
 * 
 * A {@link FMLControlledDocumentVirtualModelNature} might be seen as an interpretation of a given {@link VirtualModelInstance}
 * 
 * @author sylvain
 * 
 */
public abstract class FMLControlledDocumentVirtualModelNature<MS extends FlexoDocumentModelSlot<?>> implements VirtualModelNature {

	// Prevent external instantiation
	protected FMLControlledDocumentVirtualModelNature() {
	}

	public abstract Class<MS> getModelSlotClass();

	/**
	 * Return boolean indicating if supplied {@link VirtualModel} might be interpreted as a FML-Controlled document
	 */
	@Override
	public boolean hasNature(AbstractVirtualModel<?> virtualModel) {

		// VirtualModel should have one and only one TypedDocumentModelSlot
		if (virtualModel.getModelSlots(getModelSlotClass()).size() != 1) {
			return false;
		}

		MS documentMS = virtualModel.getModelSlots(getModelSlotClass()).get(0);

		// The unique DocXModelSlot should define a template (a DocXDocument)
		if (documentMS.getTemplateResource() == null) {
			return false;
		}

		return true;
	}

	protected MS _getDocumentModelSlot(AbstractVirtualModel<?> virtualModel) {
		if (virtualModel != null && virtualModel.getModelSlots(getModelSlotClass()).size() == 1) {
			return virtualModel.getModelSlots(getModelSlotClass()).get(0);
		}
		return null;
	}
}
