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

package org.openflexo.foundation.fml.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.ViewPointResourceImpl;
import org.openflexo.foundation.utils.FlexoProgress;
import org.openflexo.logging.FlexoLogger;

/**
 * This action is called to convert a ViewPoint from 1.5 architecture to 1.6 architecture
 * 
 * @author sylvain
 * 
 */
@Deprecated
public class ConvertViewPoint1Action extends FlexoAction<ConvertViewPoint1Action, FlexoObject, FlexoObject> {

	private static final Logger logger = FlexoLogger.getLogger(ConvertViewPoint1Action.class.getPackage().getName());

	public static FlexoActionType<ConvertViewPoint1Action, FlexoObject, FlexoObject> actionType = new FlexoActionType<ConvertViewPoint1Action, FlexoObject, FlexoObject>(
			"convert_viewpoint", FlexoActionType.defaultGroup) {

		/**
		 * Factory method
		 */
		@Override
		public ConvertViewPoint1Action makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new ConvertViewPoint1Action(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return object instanceof ViewPointResource && !((ViewPointResource) object).isLoaded()
					&& ((ViewPointResource) object).isDeprecatedVersion();
		}

		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(ConvertViewPoint1Action.actionType, FlexoObject.class);
	}

	ConvertViewPoint1Action(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws FlexoException {
		if (getFocusedObject() instanceof ViewPointResourceImpl) {
			if (((ViewPointResource) getFocusedObject()).isDeprecatedVersion()) {
				ViewPointResource res = (ViewPointResource) getFocusedObject();
				FlexoProgress progress = getEditor().getFlexoProgressFactory().makeFlexoProgress("converting_resource", 3);
				progress.setProgress("converting_files");
				ViewPointResourceImpl.convertViewPoint(res);
				res.setModelVersion(res.latestVersion());
				progress.setProgress("loading_view_point");
				try {
					res.loadResourceData(progress);
				} catch (Exception e) {
					e.printStackTrace();
					throw new FlexoException(e);
				}
				progress.setProgress("saving_view_point");
				res.save(progress);
				progress.hideWindow();
			}
		}
	}
}
