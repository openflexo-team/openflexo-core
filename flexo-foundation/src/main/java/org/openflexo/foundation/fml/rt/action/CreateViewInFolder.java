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

package org.openflexo.foundation.fml.rt.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.View.ViewImpl;
import org.openflexo.foundation.fml.rt.ViewLibrary;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;

/**
 * Action used to create a new {@link View} in a repository folder
 * 
 * @author sylvain
 * 
 */
public class CreateViewInFolder extends CreateView<CreateViewInFolder, RepositoryFolder<ViewResource>> {

	private static final Logger logger = Logger.getLogger(CreateViewInFolder.class.getPackage().getName());

	public static FlexoActionType<CreateViewInFolder, RepositoryFolder<ViewResource>, FlexoObject> actionType = new FlexoActionType<CreateViewInFolder, RepositoryFolder<ViewResource>, FlexoObject>(
			"create_view", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateViewInFolder makeNewAction(RepositoryFolder<ViewResource> focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new CreateViewInFolder(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(RepositoryFolder<ViewResource> object, Vector<FlexoObject> globalSelection) {
			return object.getResourceRepository() instanceof ViewLibrary;
		}

		@Override
		public boolean isEnabledForSelection(RepositoryFolder<ViewResource> object, Vector<FlexoObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateViewInFolder.actionType, RepositoryFolder.class);
	}

	public CreateViewInFolder(
			/*FlexoActionType<CreateView<RepositoryFolder<ViewResource>>, RepositoryFolder<ViewResource>, FlexoObject> actionType,*/
			RepositoryFolder<ViewResource> focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	/*CreateViewInFolder(RepositoryFolder<ViewResource> focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}*/

	// When creating a View in a folder, there is no container view
	@Override
	public View getContainerView() {
		return null;
	}

	@Override
	public ViewLibrary getViewLibrary() {
		if (getFocusedObject().getResourceRepository() instanceof ViewLibrary) {
			return (ViewLibrary) getFocusedObject().getResourceRepository();
		}
		return null;
	}

	public RepositoryFolder<ViewResource> getFolder() {
		return getFocusedObject();
	}

	@Override
	public boolean isValidVirtualModelInstanceName(String proposedName) {
		return getFocusedObject().isValidResourceName(proposedName);
	}

	@Override
	public ViewResource makeVirtualModelInstanceResource() throws SaveResourceException {
		return ViewImpl.newView(getNewViewName(), getNewViewTitle(), getVirtualModel(), getFolder(), getProject());
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		super.doAction(context);

		logger.info("Added view " + getNewView() + " in folder " + getFolder() + " for project " + getProject());

		getViewLibrary().registerResource((ViewResource) getNewView().getResource(), getFocusedObject());

	}

}
