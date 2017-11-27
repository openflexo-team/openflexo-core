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
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * Base implementation for an action creating a {@link VirtualModel} with a given nature
 * 
 * @author sylvain
 * 
 */

public abstract class AbstractCreateNatureSpecificVirtualModel<A extends AbstractCreateNatureSpecificVirtualModel<A>>
		extends AbstractCreateVirtualModel<A, FlexoObject, FMLObject> {

	private static final Logger logger = Logger.getLogger(AbstractCreateNatureSpecificVirtualModel.class.getPackage().getName());

	private String newVirtualModelName;
	private String newVirtualModelURI;
	private String newVirtualModelDescription;

	private Class<? extends VirtualModel> specializedVirtualModelClass;

	protected AbstractCreateNatureSpecificVirtualModel(FlexoActionFactory<A, FlexoObject, FMLObject> actionFactory,
			FlexoObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	@Override
	public LocalizedDelegate getLocales() {
		if (getServiceManager() != null) {
			return getTechnologyAdapter().getLocales();
		}
		return super.getLocales();
	}

	public abstract TechnologyAdapter getTechnologyAdapter();

	protected VirtualModelResource makeVirtualModelResource() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();

		if (getFocusedObject() instanceof RepositoryFolder) {
			RepositoryFolder<VirtualModelResource, ?> folder = (RepositoryFolder<VirtualModelResource, ?>) getFocusedObject();
			VirtualModelResource newTopLevelVirtualModelResource = factory.makeTopLevelVirtualModelResource(getNewVirtualModelName(),
					getNewVirtualModelURI(), folder, getSpecializedVirtualModelClass(), true);
			return newTopLevelVirtualModelResource;
		}

		else if (getFocusedObject() instanceof VirtualModel) {
			VirtualModel containerVM = (VirtualModel) getFocusedObject();
			VirtualModelResource containedVirtualModelResource = factory.makeContainedVirtualModelResource(getNewVirtualModelName(),
					(VirtualModelResource) containerVM.getResource(), getSpecializedVirtualModelClass(), true);
			return containedVirtualModelResource;
		}

		logger.warning("Unexpected focused object " + getFocusedObject());
		return null;
	}


	public boolean isNewVirtualModelNameValid() {
		if (StringUtils.isEmpty(newVirtualModelName)) {
			return false;
		}
		if (getFocusedObject() instanceof VirtualModel) {
			if (((VirtualModel) getFocusedObject()).getVirtualModelNamed(newVirtualModelName) != null) {
				return false;
			}
		}
		else if (getFocusedObject() instanceof RepositoryFolder) {
			if (((RepositoryFolder) getFocusedObject()).getResourceWithName(newVirtualModelName) != null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isValid() {
		if (!isNewVirtualModelNameValid()) {
			return false;
		}
		return true;
	}

	public String getNewVirtualModelName() {
		return newVirtualModelName;
	}

	public void setNewVirtualModelName(String newVirtualModelName) {
		this.newVirtualModelName = newVirtualModelName;

		getPropertyChangeSupport().firePropertyChange("newVirtualModelName", null, newVirtualModelName);

	}

	private String getBaseName() {
		return JavaUtils.getClassName(getNewVirtualModelName());
	}

	public String getNewVirtualModelURI() {
		if (newVirtualModelURI == null && getFocusedObject() instanceof RepositoryFolder) {
			String baseURI = ((RepositoryFolder) getFocusedObject()).getDefaultBaseURI();
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

	public Class<? extends VirtualModel> getSpecializedVirtualModelClass() {
		return specializedVirtualModelClass;
	}

	public void setSpecializedVirtualModelClass(Class<? extends VirtualModel> specializedVirtualModelClass) {
		if ((specializedVirtualModelClass == null && this.specializedVirtualModelClass != null)
				|| (specializedVirtualModelClass != null && !specializedVirtualModelClass.equals(this.specializedVirtualModelClass))) {
			Class<? extends VirtualModel> oldValue = this.specializedVirtualModelClass;
			this.specializedVirtualModelClass = specializedVirtualModelClass;
			getPropertyChangeSupport().firePropertyChange("specializedVirtualModelClass", oldValue, specializedVirtualModelClass);
		}
	}

	public String getNewVirtualModelDescription() {
		return newVirtualModelDescription;
	}

	public void setNewVirtualModelDescription(String newVirtualModelDescription) {
		this.newVirtualModelDescription = newVirtualModelDescription;
		getPropertyChangeSupport().firePropertyChange("newVirtualModelDescription", null, newVirtualModelDescription);
	}

	@Override
	public int getExpectedProgressSteps() {
		return 15;
	}

}
