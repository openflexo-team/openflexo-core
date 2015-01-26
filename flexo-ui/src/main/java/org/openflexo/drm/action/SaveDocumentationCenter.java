/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.drm.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.drm.DRMObject;
import org.openflexo.drm.DocResourceManager;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;

public class SaveDocumentationCenter extends FlexoAction<SaveDocumentationCenter, DRMObject, DRMObject> {

	private static final Logger logger = Logger.getLogger(SaveDocumentationCenter.class.getPackage().getName());

	public static FlexoActionType<SaveDocumentationCenter, DRMObject, DRMObject> actionType = new FlexoActionType<SaveDocumentationCenter, DRMObject, DRMObject>(
			"save_documentation_center", FlexoActionType.defaultGroup, FlexoActionType.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public SaveDocumentationCenter makeNewAction(DRMObject focusedObject, Vector<DRMObject> globalSelection, FlexoEditor editor) {
			return new SaveDocumentationCenter(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(DRMObject object, Vector<DRMObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(DRMObject object, Vector<DRMObject> globalSelection) {
			return true;
		}

	};

	SaveDocumentationCenter(DRMObject focusedObject, Vector<DRMObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) {
		logger.info("SaveDocumentationCenter");
		DocResourceManager drm = getEditor().getServiceManager().getService(DocResourceManager.class);
		drm.save();
	}

}
