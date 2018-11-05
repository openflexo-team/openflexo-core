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

package org.openflexo.action;

import java.util.Vector;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoGUIAction;

public class SubmitDocumentationAction extends FlexoGUIAction<SubmitDocumentationAction, FlexoObject, FlexoObject> {

	public static class SubmitDocumentationActionType extends FlexoActionFactory<SubmitDocumentationAction, FlexoObject, FlexoObject> {
		protected SubmitDocumentationActionType() {
			super("submit_documentation", null, FlexoActionFactory.helpGroup, NORMAL_ACTION_TYPE);
		}

		private boolean allowsDocSubmission = false;

		/**
		 * Factory method
		 */
		@Override
		public SubmitDocumentationAction makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new SubmitDocumentationAction(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return allowsDocSubmission;
		}

		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return allowsDocSubmission && object != null;
		}

		public boolean allowsDocSubmission() {
			return allowsDocSubmission;
		}

		public void setAllowsDocSubmission(boolean allows_Doc_Submission) {
			this.allowsDocSubmission = allows_Doc_Submission;
		}

	}

	public static final SubmitDocumentationActionType actionType = new SubmitDocumentationActionType();

	static {
		FlexoObjectImpl.addActionForClass(SubmitDocumentationAction.actionType, FlexoObject.class);
	}

	SubmitDocumentationAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

}
