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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.ViewPointRepository;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.ViewPointResourceFactory;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.task.Progress;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * This action allows to create a {@link ViewPoint} in a {@link RepositoryFolder}
 * 
 * @author sylvain
 * 
 */
public class CreateViewPoint extends AbstractCreateVirtualModel<CreateViewPoint, RepositoryFolder<ViewPointResource, ?>, FMLObject> {

	private static final Logger logger = Logger.getLogger(CreateViewPoint.class.getPackage().getName());

	public static FlexoActionType<CreateViewPoint, RepositoryFolder<ViewPointResource, ?>, FMLObject> actionType = new FlexoActionType<CreateViewPoint, RepositoryFolder<ViewPointResource, ?>, FMLObject>(
			"create_viewpoint", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateViewPoint makeNewAction(RepositoryFolder<ViewPointResource, ?> focusedObject, Vector<FMLObject> globalSelection,
				FlexoEditor editor) {
			return new CreateViewPoint(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(RepositoryFolder<ViewPointResource, ?> object, Vector<FMLObject> globalSelection) {
			return object.getResourceRepository() instanceof ViewPointRepository;
		}

		@Override
		public boolean isEnabledForSelection(RepositoryFolder<ViewPointResource, ?> object, Vector<FMLObject> globalSelection) {
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

	CreateViewPoint(RepositoryFolder focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	public VirtualModelLibrary getViewPointLibrary() {
		if (!(getFocusedObject().getResourceRepository() instanceof ViewPointRepository)) {
			return null;
		}
		return ((ViewPointRepository) getFocusedObject().getResourceRepository()).getViewPointLibrary();
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		if (!(getFocusedObject().getResourceRepository() instanceof ViewPointRepository)) {
			return;
		}

		logger.info("Create new viewpoint");

		// VirtualModelLibrary viewPointLibrary = getViewPointLibrary();
		// ViewPointRepository vpRepository = (ViewPointRepository) getFocusedObject().getResourceRepository();

		File newViewPointDir = getDirectoryWhereToCreateTheViewPoint();

		logger.info("Creating viewpoint " + newViewPointDir.getAbsolutePath());

		FMLTechnologyAdapter fmlTechnologyAdapter = getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		ViewPointResourceFactory factory = fmlTechnologyAdapter.getViewPointResourceFactory();

		try {
			ViewPointResource newViewPointResource = factory.makeViewPointResource(getBaseName(), getNewViewPointURI(),
					getViewPointFolder(), fmlTechnologyAdapter.getTechnologyContextManager(), true);
			newViewPoint = newViewPointResource.getLoadedResourceData();
			newViewPoint.setDescription(getNewViewPointDescription());
		} catch (SaveResourceException e) {
			throw new SaveResourceException(null);
		} catch (ModelDefinitionException e) {
			throw new FlexoException(e);
		}

		Progress.progress(getLocales().localizedForKey("create_model_slots"));
		performCreateModelSlots();

		Progress.progress(getLocales().localizedForKey("set_parent_concepts"));
		performSetParentConcepts();

		Progress.progress(getLocales().localizedForKey("create_properties"));
		performCreateProperties();

		Progress.progress(getLocales().localizedForKey("create_behaviours"));
		performCreateBehaviours();

		Progress.progress(getLocales().localizedForKey("create_inspector"));
		performCreateInspectors();

		Progress.progress(getLocales().localizedForKey("perform_post_processings"));
		performPostProcessings();

	}

	public String getNewViewPointName() {
		return newViewPointName;
	}

	public void setNewViewPointName(String newViewPointName) {
		this.newViewPointName = newViewPointName;
		getPropertyChangeSupport().firePropertyChange("newViewPointName", null, newViewPointName);
		getPropertyChangeSupport().firePropertyChange("newViewPointURI", null, getNewViewPointURI());
	}

	public String getNewViewPointURI() {
		if (newViewPointURI == null) {
			String baseURI = getFocusedObject().getDefaultBaseURI();
			if (!baseURI.endsWith("/")) {
				baseURI = baseURI + "/";
			}
			return baseURI + getBaseName() + ".viewpoint";
		}

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

	@Override
	public VirtualModel getNewVirtualModel() {
		// TODO return getNewViewPoint() when ViewPoint will inherits from VirtualModel
		return null;
	}

	public ViewPoint getNewViewPoint() {
		return newViewPoint;
	}

	private String getBaseName() {
		return JavaUtils.getClassName(getNewViewPointName());
	}

	@Deprecated
	private File getDirectoryWhereToCreateTheViewPoint() {
		if (getFocusedObject() != null && getFocusedObject().getSerializationArtefact() instanceof File) {
			return (File) getFocusedObject().getSerializationArtefact();
		}
		return null;
	}

	@Override
	public int getExpectedProgressSteps() {
		return 10;
	}

}
