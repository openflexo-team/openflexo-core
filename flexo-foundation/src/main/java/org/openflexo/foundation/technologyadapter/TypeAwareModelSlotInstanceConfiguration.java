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
package org.openflexo.foundation.technologyadapter;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fmlrt.TypeAwareModelSlotInstance;
import org.openflexo.foundation.fmlrt.View;
import org.openflexo.foundation.fmlrt.VirtualModelInstance;
import org.openflexo.foundation.fmlrt.VirtualModelInstanceModelFactory;
import org.openflexo.foundation.fmlrt.action.CreateVirtualModelInstance;
import org.openflexo.foundation.fmlrt.action.ModelSlotInstanceConfiguration;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;

/**
 * This class is used to stored the configuration of a {@link TypeAwareModelSlot} which has to be instantiated
 * 
 * 
 * @author sylvain
 * 
 */
public abstract class TypeAwareModelSlotInstanceConfiguration<M extends FlexoModel<M, MM> & TechnologyObject<?>, MM extends FlexoMetaModel<MM> & TechnologyObject<?>, MS extends TypeAwareModelSlot<M, MM>>
		extends ModelSlotInstanceConfiguration<MS, M> {

	private static final Logger logger = Logger.getLogger(TypeAwareModelSlotInstanceConfiguration.class.getPackage().getName());

	protected List<ModelSlotInstanceConfigurationOption> options;

	protected FlexoResourceCenter<?> resourceCenter;
	protected FlexoModelResource<M, MM, ?> modelResource;
	protected String modelUri;
	protected String relativePath;
	protected String filename;

	protected TypeAwareModelSlotInstanceConfiguration(MS ms, CreateVirtualModelInstance<?> action) {
		super(ms, action);
		FlexoResourceCenterService rcService = action.getFocusedObject().getViewPoint().getViewPointLibrary().getServiceManager()
				.getResourceCenterService();
		if (rcService.getResourceCenters().size() > 0) {
			resourceCenter = rcService.getResourceCenters().get(0);
		}
		options = new ArrayList<ModelSlotInstanceConfiguration.ModelSlotInstanceConfigurationOption>();
		options.add(DefaultModelSlotInstanceConfigurationOption.SelectExistingModel);
		options.add(DefaultModelSlotInstanceConfigurationOption.CreatePrivateNewModel);
		options.add(DefaultModelSlotInstanceConfigurationOption.CreateSharedNewModel);
		if (!ms.getIsRequired()) {
			options.add(DefaultModelSlotInstanceConfigurationOption.LeaveEmpty);
		}
		setOption(DefaultModelSlotInstanceConfigurationOption.SelectExistingModel);
	}

	/*@Override
	public void setOption(org.openflexo.foundation.fmlrt.action.ModelSlotInstanceConfiguration.ModelSlotInstanceConfigurationOption option) {
		super.setOption(option);
		if (option == DefaultModelSlotInstanceConfigurationOption.SelectExistingModel) {
			modelUri = null;
			relativePath = null;
			filename = null;
		}
	}*/

	@Override
	public List<ModelSlotInstanceConfigurationOption> getAvailableOptions() {
		return options;
	}

	@Override
	public TypeAwareModelSlotInstance<M, MM, MS> createModelSlotInstance(VirtualModelInstance vmInstance, View view) {
		VirtualModelInstanceModelFactory factory = vmInstance.getFactory();
		TypeAwareModelSlotInstance<M, MM, MS> returned = factory.newInstance(TypeAwareModelSlotInstance.class);
		returned.setModelSlot(getModelSlot());
		returned.setVirtualModelInstance(vmInstance);
		configureModelSlotInstance(returned, view);
		return returned;
	}

	protected TypeAwareModelSlotInstance<M, MM, MS> configureModelSlotInstance(TypeAwareModelSlotInstance<M, MM, MS> msInstance, View view) {
		if (getOption() == DefaultModelSlotInstanceConfigurationOption.SelectExistingModel) {
			if (modelResource != null) {
				System.out.println("Select model with uri " + getModelResource().getURI());
				msInstance.setAccessedResourceData(getModelResource().getModel());
				msInstance.setModelURI(getModelResource().getURI());
				msInstance.setProject(view.getProject());
				msInstance.setView(view);
			} else {
				logger.warning("No model for model slot " + getModelSlot());
			}
		} else if (getOption() == DefaultModelSlotInstanceConfigurationOption.CreatePrivateNewModel) {
			modelResource = createProjectSpecificEmptyModel(msInstance, getModelSlot(), view.getProject());
			// System.out.println("***** modelResource = " + modelResource);
			// System.out.println("***** model = " + modelResource.getModel());
			// System.out.println("***** modelResource2 = " + modelResource.getModel().getResource());
			try {
				modelResource.loadResourceData(null);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ResourceLoadingCancelledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FlexoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println("***** model = " + modelResource.getModel());
			// System.out.println("***** model res = " + modelResource.getModel().getResource());
			if (modelResource != null) {
				msInstance.setAccessedResourceData(getModelResource().getModel());
				msInstance.setModelURI(getModelResource().getURI());
				msInstance.setProject(view.getProject());
				msInstance.setView(view);
				// System.out.println("***** Created model resource " + getModelResource());
				// System.out.println("***** Created model " + getModelResource().getModel());
				// System.out.println("***** Created model with uri=" + getModelResource().getModel().getURI());
				// System.out.println("msInstance.getResource()=" + msInstance.getResource());
				// System.out.println("getModelResource().getModel().getResource()=" + getModelResource().getModel().getResource());
			} else {
				logger.warning("Could not create ProjectSpecificEmtpyModel for model slot " + getModelSlot());
			}
		} /*else if (getOption() == DefaultModelSlotInstanceConfigurationOption.CreateSharedNewModel) {
			modelResource = createSharedEmptyModel(msInstance, getModelSlot());
			if (modelResource != null) {
				msInstance.setResourceData(getModelResource().getModel());
				msInstance.setModelURI(getModelResource().getURI());
			} else {
				logger.warning("Could not create SharedEmptyModel for model slot " + getModelSlot());
			}
			return msInstance;
			}*/
		return null;
	}

	private FlexoModelResource<M, MM, ?> createProjectSpecificEmptyModel(TypeAwareModelSlotInstance<M, MM, MS> msInstance, MS modelSlot,
			FlexoProject project) {
		return modelSlot.createProjectSpecificEmptyModel(project, getFilename(), getModelUri(), modelSlot.getMetaModelResource());
	}

	/*private FlexoModelResource<M, MM> createSharedEmptyModel(TypeAwareModelSlotInstance<M, MM, MS> msInstance, MS modelSlot) {
		return modelSlot.createSharedEmptyModel(getResourceCenter(), getRelativePath(), getFilename(), getModelUri(),
				modelSlot.getMetaModelResource());
	}*/

	public abstract boolean isURIEditable();

	@Override
	public FlexoModelResource<M, MM, ?> getResource() {
		return getModelResource();
	}

	public FlexoResourceCenter<?> getResourceCenter() {
		return resourceCenter;
	}

	public void setResourceCenter(FlexoResourceCenter<?> resourceCenter) {
		if (this.resourceCenter != resourceCenter) {
			FlexoResourceCenter<?> oldValue = this.resourceCenter;
			this.resourceCenter = resourceCenter;
			getPropertyChangeSupport().firePropertyChange("resourceCenter", oldValue, resourceCenter);
		}
	}

	public FlexoModelResource<M, MM, ?> getModelResource() {
		return modelResource;
	}

	public void setModelResource(FlexoModelResource<M, MM, ?> modelResource) {
		if (this.modelResource != modelResource) {
			FlexoModelResource<M, MM, ?> oldValue = this.modelResource;
			this.modelResource = modelResource;
			getPropertyChangeSupport().firePropertyChange("modelResource", oldValue, modelResource);
		}
	}

	public String getModelUri() {
		return modelUri;
	}

	public void setModelUri(String modelUri) {
		if ((modelUri == null && this.modelUri != null) || (modelUri != null && !modelUri.equals(this.modelUri))) {
			String oldValue = this.modelUri;
			this.modelUri = modelUri;
			getPropertyChangeSupport().firePropertyChange("modelUri", oldValue, modelUri);
		}
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		if ((relativePath == null && this.relativePath != null) || (relativePath != null && !relativePath.equals(this.relativePath))) {
			String oldValue = this.relativePath;
			this.relativePath = relativePath;
			getPropertyChangeSupport().firePropertyChange("relativePath", oldValue, relativePath);
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		if ((filename == null && this.filename != null) || (filename != null && !filename.equals(this.filename))) {
			String oldValue = this.filename;
			this.filename = filename;
			getPropertyChangeSupport().firePropertyChange("filename", oldValue, filename);
		}
	}

	@Override
	public boolean isValidConfiguration() {
		if (!super.isValidConfiguration()) {
			return false;
		}
		if (getOption() == DefaultModelSlotInstanceConfigurationOption.SelectExistingModel) {
			if (getResource() == null) {
				setErrorMessage(FlexoLocalization.localizedForKey("no_model_selected"));
				return false;
			}
			return true;
		} else if (getOption() == DefaultModelSlotInstanceConfigurationOption.CreatePrivateNewModel) {
			if (StringUtils.isEmpty(getModelUri())) {
				setErrorMessage(FlexoLocalization.localizedForKey("please_supply_valid_uri"));
				return false;
			}
			try {
				new URL(getModelUri());
			} catch (MalformedURLException e) {
				setErrorMessage(FlexoLocalization.localizedForKey("malformed_uri"));
				return false;
			}
			if (StringUtils.isEmpty(getRelativePath())) {
				setErrorMessage(FlexoLocalization.localizedForKey("please_supply_valid_relative_path"));
				return false;
			}
			return checkValidFileName();
		} else if (getOption() == DefaultModelSlotInstanceConfigurationOption.CreateSharedNewModel) {
			if (getResourceCenter() == null) {
				setErrorMessage(FlexoLocalization.localizedForKey("please_select_a_resource_center"));
				return false;
			}
			if (StringUtils.isEmpty(getModelUri())) {
				setErrorMessage(FlexoLocalization.localizedForKey("please_supply_valid_uri"));
				return false;
			}
			try {
				new URL(getModelUri());
			} catch (MalformedURLException e) {
				setErrorMessage(FlexoLocalization.localizedForKey("malformed_uri"));
				return false;
			}
			if (StringUtils.isEmpty(getRelativePath())) {
				setErrorMessage(FlexoLocalization.localizedForKey("please_supply_valid_relative_path"));
				return false;
			}
			return checkValidFileName();
		} else if (getOption() == DefaultModelSlotInstanceConfigurationOption.LeaveEmpty) {
			if (getModelSlot().getIsRequired()) {
				setErrorMessage(FlexoLocalization.localizedForKey("model_is_required"));
				return false;
			}
			return true;
		}
		return false;
	}

	protected boolean checkValidFileName() {
		if (StringUtils.isEmpty(getFilename())) {
			setErrorMessage(FlexoLocalization.localizedForKey("please_supply_valid_file_name"));
			return false;
		}
		return true;
	}

}
