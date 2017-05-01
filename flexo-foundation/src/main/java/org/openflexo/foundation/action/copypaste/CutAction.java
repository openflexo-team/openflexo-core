/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.action.copypaste;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import org.openflexo.exceptions.CopyException;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.resource.PamelaResource;

/**
 * Represents a Cut action (clipboard operation)<br>
 * 
 * Note that clipboard operations are managed at very low level using PAMELA framework
 * 
 * @author sylvain
 * 
 */
public class CutAction extends AbstractCopyAction<CutAction> {

	private static final Logger logger = Logger.getLogger(CutAction.class.getPackage().getName());

	public static class CutActionType extends AbstractCopyActionType<CutAction> {

		public CutActionType(FlexoEditingContext editingContext) {
			super("cut", editingContext);
		}

		/**
		 * Factory method
		 */
		@Override
		public CutAction makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			try {
				prepareCopy(getGlobalSelectionAndFocusedObject(focusedObject, globalSelection));
			} catch (InvalidSelectionException e) {
				return null;
			}
			CutAction returned = new CutAction(this, focusedObject, globalSelection, editor);
			returned.editingContext = editingContext;
			returned.objectsToBeCopied = objectsToBeCopied;
			return returned;
		}
	};

	private FlexoEditingContext editingContext;
	protected Map<PamelaResource<?, ?>, List<FlexoObject>> objectsToBeCopied;

	private FlexoClipboard clipboard;

	CutAction(CutActionType actionType, FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws InvalidSelectionException {
		logger.info("CUT");
		try {
			clipboard = cut();
			editingContext.setClipboard(clipboard);
		} catch (CopyException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cut current selection in the clipboard
	 * 
	 * @throws CopyException
	 * @throws InvalidSelectionException
	 */
	private FlexoClipboard cut() throws CopyException, InvalidSelectionException {

		clipboard = FlexoClipboard.cut(objectsToBeCopied, getFocusedObject(), null);
		return clipboard;
	}

	public FlexoClipboard getClipboard() {
		return clipboard;
	}

}
