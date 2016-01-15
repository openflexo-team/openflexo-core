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

package org.openflexo.foundation.doc.fml.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.doc.nature.FMLControlledDocumentViewNature;
import org.openflexo.foundation.doc.nature.FMLControlledDocumentVirtualModelNature;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.action.CreateVirtualModelInstance;

public abstract class CreateFMLControlledDocumentVirtualModelInstance<A extends CreateFMLControlledDocumentVirtualModelInstance<A>>
		extends CreateVirtualModelInstance<A> {

	private static final Logger logger = Logger.getLogger(CreateFMLControlledDocumentVirtualModelInstance.class.getPackage().getName());

	public static abstract class CreateFMLControlledDocumentVirtualModelInstanceActionType<A extends CreateFMLControlledDocumentVirtualModelInstance<A>>
			extends FlexoActionType<A, View, FlexoObject> {

		private final FMLControlledDocumentViewNature<?> nature;

		public CreateFMLControlledDocumentVirtualModelInstanceActionType(FMLControlledDocumentViewNature<?> nature) {
			super("create_fml_controlled_document", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE);
			this.nature = nature;
		}

		@Override
		public boolean isVisibleForSelection(View view, Vector<FlexoObject> globalSelection) {
			return view.hasNature(nature);
		}

		@Override
		public boolean isEnabledForSelection(View view, Vector<FlexoObject> globalSelection) {
			return isVisibleForSelection(view, globalSelection);
		}

	}

	protected CreateFMLControlledDocumentVirtualModelInstance(CreateFMLControlledDocumentVirtualModelInstanceActionType<A> actionType,
			View focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	public abstract FMLControlledDocumentVirtualModelNature<?> getVirtualModelNature();

	@Override
	public boolean isVisible(VirtualModel virtualModel) {
		if (virtualModel.hasNature(getVirtualModelNature())) {
			return true;
		}
		else {
			return false;
		}
	}

}
