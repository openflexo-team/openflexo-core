/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexodocresourcemanager, a component of the software infrastructure 
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

import org.openflexo.drm.DocItem;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;

public class RemoveEmbeddingChildItem extends FlexoAction {

	public static FlexoActionFactory actionType = new FlexoActionFactory("remove_embedding_child", FlexoActionFactory.defaultGroup,
			FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public FlexoAction makeNewAction(FlexoObject focusedObject, Vector globalSelection, FlexoEditor editor) {
			return new RemoveEmbeddingChildItem(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector globalSelection) {
			return object != null && object instanceof DocItem && ((DocItem) object).getEmbeddingParentItem() != null;
		}

	};

	private DocItem _docItemToRemove;

	RemoveEmbeddingChildItem(FlexoObject focusedObject, Vector globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) {
		if (getParentDocItem() != null && getDocItemToRemove() != null) {
			getParentDocItem().removeFromEmbeddingChildItems(getDocItemToRemove());
		}
	}

	public void setDocItemToRemove(DocItem docItemToRemove) {
		_docItemToRemove = docItemToRemove;
	}

	public DocItem getDocItemToRemove() {
		if (_docItemToRemove == null) {
			if (getFocusedObject() != null && getFocusedObject() instanceof DocItem) {
				_docItemToRemove = (DocItem) getFocusedObject();
			}
		}
		return _docItemToRemove;
	}

	public DocItem getParentDocItem() {
		if (getDocItemToRemove() != null) {
			return getDocItemToRemove().getEmbeddingParentItem();
		}
		return null;
	}

}
