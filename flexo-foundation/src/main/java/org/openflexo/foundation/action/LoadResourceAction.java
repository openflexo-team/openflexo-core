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

package org.openflexo.foundation.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.project.FlexoProjectResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.logging.FlexoLogger;

/**
 * This action is called to load a {@link FlexoResource}
 * 
 * @author sylvain
 * 
 */
public class LoadResourceAction extends FlexoAction<LoadResourceAction, FlexoObject, FlexoObject> implements LongRunningAction {

	private static final Logger logger = FlexoLogger.getLogger(LoadResourceAction.class.getPackage().getName());

	public static FlexoActionFactory<LoadResourceAction, FlexoObject, FlexoObject> actionType = new FlexoActionFactory<LoadResourceAction, FlexoObject, FlexoObject>(
			"load_resource", FlexoActionFactory.defaultGroup) {

		/**
		 * Factory method
		 */
		@Override
		public LoadResourceAction makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new LoadResourceAction(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return object instanceof FlexoResource && !((FlexoResource<?>) object).isLoaded();
		}

		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return object instanceof FlexoResource && ((FlexoResource<?>) object).isLoadable() && !((FlexoResource<?>) object).isLoaded();
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(LoadResourceAction.actionType, FlexoObject.class);
	}

	private LoadResourceAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws FlexoException {
		if (getFocusedObject() instanceof FlexoResource) {
			if (getFocusedObject() instanceof FlexoProjectResource) {
				logger.info("Loading project " + getFocusedObject());
				FlexoProjectResource<Object> prjResource = (FlexoProjectResource<Object>) getFocusedObject();
				Object serializationArtefact = prjResource.getIODelegate().getSerializationArtefact();
				Object projectDirectory = prjResource.getDelegateResourceCenter().getContainer(serializationArtefact);
				try {
					getServiceManager().getProjectLoaderService().loadProject(projectDirectory);
				} catch (ProjectInitializerException e) {
					throw new FlexoException(e);
				}
			}
			else if (!((FlexoResource<?>) getFocusedObject()).isLoaded()) {
				logger.info("Loading resource " + getFocusedObject());
				// FlexoProgress progress = getEditor().getFlexoProgressFactory().makeFlexoProgress("loading_resource", 3);
				Progress.progress("loading_resource");
				try {
					((FlexoResource<?>) getFocusedObject()).getResourceData();
				} catch (Exception e) {
					e.printStackTrace();
					throw new FlexoException(e);
				}
				// progress.hideWindow();
			}
		}
	}

	@Override
	public int getExpectedProgressSteps() {
		return 10;
	}
}
