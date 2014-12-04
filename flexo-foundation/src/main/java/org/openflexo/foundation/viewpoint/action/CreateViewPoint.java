/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.viewpoint.action;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.ViewPoint.ViewPointImpl;
import org.openflexo.foundation.viewpoint.ViewPointLibrary;
import org.openflexo.foundation.viewpoint.ViewPointObject;
import org.openflexo.foundation.viewpoint.ViewPointRepository;
import org.openflexo.foundation.viewpoint.rm.ViewPointResource;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * This action allows to create a {@link ViewPoint} in a {@link RepositoryFolder}
 * 
 * @author sylvain
 * 
 */
public class CreateViewPoint extends FlexoAction<CreateViewPoint, RepositoryFolder<ViewPointResource>, ViewPointObject> {

	private static final Logger logger = Logger.getLogger(CreateViewPoint.class.getPackage().getName());

	public static FlexoActionType<CreateViewPoint, RepositoryFolder<ViewPointResource>, ViewPointObject> actionType = new FlexoActionType<CreateViewPoint, RepositoryFolder<ViewPointResource>, ViewPointObject>(
			"create_viewpoint", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateViewPoint makeNewAction(RepositoryFolder<ViewPointResource> focusedObject, Vector<ViewPointObject> globalSelection,
				FlexoEditor editor) {
			return new CreateViewPoint(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(RepositoryFolder<ViewPointResource> object, Vector<ViewPointObject> globalSelection) {
			return object.getResourceRepository() instanceof ViewPointRepository;
		}

		@Override
		public boolean isEnabledForSelection(RepositoryFolder<ViewPointResource> object, Vector<ViewPointObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateViewPoint.actionType, RepositoryFolder.class);
	}

	private String newViewPointName;
	private String newViewPointURI;
	private String newViewPointDescription;
	private ViewPoint newViewPoint;

	CreateViewPoint(RepositoryFolder focusedObject, Vector<ViewPointObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	public ViewPointLibrary getViewPointLibrary() {
		if (!(getFocusedObject().getResourceRepository() instanceof ViewPointRepository)) {
			return null;
		}
		return ((ViewPointRepository) getFocusedObject().getResourceRepository()).getViewPointLibrary();
	}

	@Override
	protected void doAction(Object context) throws IOFlexoException {

		if (!(getFocusedObject().getResourceRepository() instanceof ViewPointRepository)) {
			return;
		}

		logger.info("Create new viewpoint");

		ViewPointLibrary viewPointLibrary = getViewPointLibrary();
		ViewPointRepository vpRepository = (ViewPointRepository) getFocusedObject().getResourceRepository();

		File newViewPointDir = getDirectoryWhereToCreateTheViewPoint();

		logger.info("Creating viewpoint " + newViewPointDir.getAbsolutePath());

		// Instanciate new ViewPoint
		newViewPoint = ViewPointImpl.newViewPoint(getBaseName(), getNewViewPointURI(), newViewPointDir, viewPointLibrary);
		newViewPoint.setDescription(getNewViewPointDescription());

		vpRepository.registerResource((ViewPointResource) newViewPoint.getResource(), getFocusedObject());

	}

	public String getNewViewPointName() {
		return newViewPointName;
	}

	public void setNewViewPointName(String newViewPointName) {
		this.newViewPointName = newViewPointName;
		getPropertyChangeSupport().firePropertyChange("newViewPointName", null, newViewPointName);
	}

	public String getNewViewPointURI() {
		return newViewPointURI;
	}

	public void setNewViewPointURI(String newViewPointURI) {
		this.newViewPointURI = newViewPointURI;
		getPropertyChangeSupport().firePropertyChange("newViewPointURI", null, newViewPointURI);

	}

	public String getNewViewPointDescription() {
		return newViewPointDescription;
	}

	public void setNewViewPointDescription(String newViewPointDescription) {
		this.newViewPointDescription = newViewPointDescription;
		getPropertyChangeSupport().firePropertyChange("newViewPointDescription", null, newViewPointDescription);
	}

	public RepositoryFolder getViewPointFolder() {
		return getFocusedObject();
	}

	public boolean isNewViewPointNameValid() {
		if (StringUtils.isEmpty(getNewViewPointName())) {
			return false;
		}
		return true;
	}

	public boolean isNewViewPointURIValid() {
		if (StringUtils.isEmpty(getNewViewPointURI())) {
			return false;
		}
		try {
			new URL(getNewViewPointURI());
		} catch (MalformedURLException e) {
			return false;
		}
		if (getViewPointLibrary() == null) {
			return false;
		}
		if (getViewPointLibrary().getViewPointResource(getNewViewPointURI()) != null) {
			return false;
		}

		return true;
	}

	@Override
	public boolean isValid() {
		if (!isNewViewPointNameValid()) {
			return false;
		}
		if (!isNewViewPointURIValid()) {
			return false;
		}
		return true;
	}

	public ViewPoint getNewViewPoint() {
		return newViewPoint;
	}

	private String getBaseName() {
		return JavaUtils.getClassName(getNewViewPointName());
	}

	private File getDirectoryWhereToCreateTheViewPoint() {
		if (getFocusedObject() != null) {
			return getFocusedObject().getFile();
		}
		return null;
	}

}
