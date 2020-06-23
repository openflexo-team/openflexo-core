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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.task.Progress;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * This action allows to create a {@link VirtualModel} in a {@link RepositoryFolder}<br>
 * (this {@link VirtualModel} is declared as top-level)
 * 
 * @author sylvain
 * 
 */
public class CreateTopLevelVirtualModel
		extends AbstractCreateVirtualModel<CreateTopLevelVirtualModel, RepositoryFolder<VirtualModelResource, ?>, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(CreateTopLevelVirtualModel.class.getPackage().getName());

	public static FlexoActionFactory<CreateTopLevelVirtualModel, RepositoryFolder<VirtualModelResource, ?>, FMLObject> actionType = new FlexoActionFactory<CreateTopLevelVirtualModel, RepositoryFolder<VirtualModelResource, ?>, FMLObject>(
			"create_virtual_model", FlexoActionFactory.newVirtualModelMenu, FlexoActionFactory.defaultGroup,
			FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateTopLevelVirtualModel makeNewAction(RepositoryFolder<VirtualModelResource, ?> focusedObject,
				Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateTopLevelVirtualModel(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(RepositoryFolder<VirtualModelResource, ?> object, Vector<FMLObject> globalSelection) {
			return object.getResourceRepository() != null;
		}

		@Override
		public boolean isEnabledForSelection(RepositoryFolder<VirtualModelResource, ?> object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateTopLevelVirtualModel.actionType, RepositoryFolder.class);
	}

	private String newVirtualModelName;
	private String newVirtualModelURI;
	private String newVirtualModelDescription;
	private VirtualModel newVirtualModel;

	CreateTopLevelVirtualModel(RepositoryFolder<VirtualModelResource, ?> focusedObject, Vector<FMLObject> globalSelection,
			FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	public VirtualModelLibrary getVirtualModelLibrary() {
		return getServiceManager().getVirtualModelLibrary();
		/*if (!(getFocusedObject().getResourceRepository() instanceof VirtualModelRepository)) {
			return null;
		}
		return ((VirtualModelRepository<?>) getFocusedObject().getResourceRepository()).getVirtualModelLibrary();*/
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		// logger.info("Create new viewpoint");

		FMLTechnologyAdapter fmlTechnologyAdapter = getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();

		try {
			VirtualModelResource newVirtualModelResource = factory.makeTopLevelVirtualModelResource(getBaseName(), getNewVirtualModelURI(),
					getVirtualModelFolder(), null, true);
			newVirtualModel = newVirtualModelResource.getLoadedResourceData();
			newVirtualModel.setDescription(getNewVirtualModelDescription());
			newVirtualModel.setVisibility(getVisibility());
			newVirtualModel.setAbstract(getIsAbstract());
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

	public String getNewVirtualModelName() {
		return newVirtualModelName;
	}

	public void setNewVirtualModelName(String newViewPointName) {
		this.newVirtualModelName = newViewPointName;
		getPropertyChangeSupport().firePropertyChange("newVirtualModelName", null, newViewPointName);
		getPropertyChangeSupport().firePropertyChange("newVirtualModelURI", null, getNewVirtualModelURI());
	}

	public String getNewVirtualModelURI() {
		if (newVirtualModelURI == null) {
			String baseURI = getFocusedObject().getDefaultBaseURI();
			if (!baseURI.endsWith("/")) {
				baseURI = baseURI + "/";
			}
			return baseURI + getBaseName() + VirtualModelResourceFactory.FML_SUFFIX;
		}

		return newVirtualModelURI;
	}

	public void setNewVirtualModelURI(String newVirtualModelURI) {
		this.newVirtualModelURI = newVirtualModelURI;
		getPropertyChangeSupport().firePropertyChange("newVirtualModelURI", null, newVirtualModelURI);

	}

	public String getNewVirtualModelDescription() {
		return newVirtualModelDescription;
	}

	public void setNewVirtualModelDescription(String newVirtualModelDescription) {
		this.newVirtualModelDescription = newVirtualModelDescription;
		getPropertyChangeSupport().firePropertyChange("newVirtualModelDescription", null, newVirtualModelDescription);
	}

	public RepositoryFolder<VirtualModelResource, ?> getVirtualModelFolder() {
		return getFocusedObject();
	}

	public boolean isNewVirtualModelNameValid() {
		if (StringUtils.isEmpty(getNewVirtualModelName())) {
			return false;
		}
		return true;
	}

	public boolean isNewVirtualModelURIValid() {
		if (StringUtils.isEmpty(getNewVirtualModelURI())) {
			return false;
		}
		try {
			new URL(getNewVirtualModelURI());
		} catch (MalformedURLException e) {
			return false;
		}
		if (getVirtualModelLibrary() == null) {
			return false;
		}
		if (getVirtualModelLibrary().getVirtualModelResource(getNewVirtualModelURI()) != null) {
			return false;
		}

		return true;
	}

	@Override
	public boolean isValid() {
		if (!isNewVirtualModelNameValid()) {
			return false;
		}
		if (!isNewVirtualModelURIValid()) {
			return false;
		}
		return true;
	}

	@Override
	public VirtualModel getNewVirtualModel() {
		return newVirtualModel;
	}

	private String getBaseName() {
		return JavaUtils.getClassName(getNewVirtualModelName());
	}

	@Override
	public int getExpectedProgressSteps() {
		return 10;
	}

}
