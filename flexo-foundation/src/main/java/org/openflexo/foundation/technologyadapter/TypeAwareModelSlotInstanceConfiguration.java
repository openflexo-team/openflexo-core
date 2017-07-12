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

package org.openflexo.foundation.technologyadapter;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.TypeAwareModelSlotInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.action.ModelSlotInstanceConfiguration;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.toolbox.StringUtils;

/**
 * This class is used to stored the configuration of a {@link TypeAwareModelSlot} which has to be instantiated
 * 
 * 
 * @author sylvain
 * 
 */
@Deprecated
public abstract class TypeAwareModelSlotInstanceConfiguration<M extends FlexoModel<M, MM> & TechnologyObject<?>, MM extends FlexoMetaModel<MM> & TechnologyObject<?>, MS extends TypeAwareModelSlot<M, MM>>
		extends ModelSlotInstanceConfiguration<MS, M> {

	private static final Logger logger = Logger.getLogger(TypeAwareModelSlotInstanceConfiguration.class.getPackage().getName());

	protected List<ModelSlotInstanceConfigurationOption> options;

	protected FlexoResourceCenter<?> targetResourceCenter;
	protected FlexoModelResource<M, MM, ?, ?> modelResource;
	protected String modelUri;
	protected String relativePath;
	protected String filename;

	protected TypeAwareModelSlotInstanceConfiguration(MS ms, FlexoConceptInstance fci, FlexoResourceCenter<?> rc) {
		super(ms, fci, rc);
		FlexoResourceCenterService rcService = rc.getServiceManager().getResourceCenterService();
		if (rcService.getResourceCenters().size() > 0) {
			targetResourceCenter = rcService.getResourceCenters().get(0);
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

	@Override
	public List<ModelSlotInstanceConfigurationOption> getAvailableOptions() {
		return options;
	}

	@Override
	public TypeAwareModelSlotInstance<M, MM, MS> createModelSlotInstance(FlexoConceptInstance fci, View view) {
		AbstractVirtualModelInstanceModelFactory<?> factory = fci.getFactory();
		TypeAwareModelSlotInstance<M, MM, MS> returned = factory.newInstance(TypeAwareModelSlotInstance.class);
		returned.setModelSlot(getModelSlot());
		returned.setFlexoConceptInstance(fci);
		configureModelSlotInstance(returned, view);
		return returned;
	}

	protected TypeAwareModelSlotInstance<M, MM, MS> configureModelSlotInstance(TypeAwareModelSlotInstance<M, MM, MS> msInstance,
			View view) {
		if (getOption() == DefaultModelSlotInstanceConfigurationOption.SelectExistingModel) {
			if (modelResource != null) {
				System.out.println("Select model with uri " + getModelResource().getURI());
				msInstance.setAccessedResourceData(getModelResource().getModel());
				msInstance.setModelURI(getModelResource().getURI());
				msInstance.setView(view);
			}
			else {
				logger.warning("No model for model slot " + getModelSlot());
			}
		}
		else if (getOption() == DefaultModelSlotInstanceConfigurationOption.CreatePrivateNewModel) {
			modelResource = createProjectSpecificEmptyModel(msInstance, getModelSlot(), view.getResourceCenter());
			// System.out.println("***** modelResource = " + modelResource);
			// System.out.println("***** model = " + modelResource.getModel());
			// System.out.println("***** modelResource2 = " + modelResource.getModel().getResource());
			try {
				modelResource.loadResourceData(null);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ResourceLoadingCancelledException e) {
				e.printStackTrace();
			} catch (FlexoException e) {
				e.printStackTrace();
			}
			// System.out.println("***** model = " + modelResource.getModel());
			// System.out.println("***** model res = " + modelResource.getModel().getResource());
			if (modelResource != null) {
				msInstance.setAccessedResourceData(getModelResource().getModel());
				msInstance.setModelURI(getModelResource().getURI());
				msInstance.setView(view);
				// System.out.println("***** Created model resource " + getModelResource());
				// System.out.println("***** Created model " + getModelResource().getModel());
				// System.out.println("***** Created model with uri=" + getModelResource().getModel().getURI());
				// System.out.println("msInstance.getResource()=" + msInstance.getResource());
				// System.out.println("getModelResource().getModel().getResource()=" + getModelResource().getModel().getResource());
			}
			else {
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

	private FlexoModelResource<M, MM, ?, ?> createProjectSpecificEmptyModel(TypeAwareModelSlotInstance<M, MM, MS> msInstance, MS modelSlot,
			FlexoResourceCenter<?> rc) {
		return modelSlot.createProjectSpecificEmptyModel(rc, getFilename(), getRelativePath(), getModelUri(),
				modelSlot.getMetaModelResource());
	}

	/*private FlexoModelResource<M, MM> createSharedEmptyModel(TypeAwareModelSlotInstance<M, MM, MS> msInstance, MS modelSlot) {
		return modelSlot.createSharedEmptyModel(getResourceCenter(), getRelativePath(), getFilename(), getModelUri(),
				modelSlot.getMetaModelResource());
	}*/

	public abstract boolean isURIEditable();

	@Override
	public FlexoModelResource<M, MM, ?, ?> getResource() {
		return getModelResource();
	}

	public FlexoResourceCenter<?> getTargetResourceCenter() {
		return targetResourceCenter;
	}

	public void setTargetResourceCenter(FlexoResourceCenter<?> resourceCenter) {
		if (this.targetResourceCenter != resourceCenter) {
			FlexoResourceCenter<?> oldValue = this.targetResourceCenter;
			this.targetResourceCenter = resourceCenter;
			getPropertyChangeSupport().firePropertyChange("targetResourceCenter", oldValue, resourceCenter);
			getPropertyChangeSupport().firePropertyChange("modelUri", oldValue, getModelUri());

		}
	}

	public FlexoModelResource<M, MM, ?, ?> getModelResource() {
		return modelResource;
	}

	public void setModelResource(FlexoModelResource<M, MM, ?, ?> modelResource) {
		if (this.modelResource != modelResource) {
			FlexoModelResource<M, MM, ?, ?> oldValue = this.modelResource;
			this.modelResource = modelResource;
			getPropertyChangeSupport().firePropertyChange("modelResource", oldValue, modelResource);
		}
	}

	@Override
	public void setOption(ModelSlotInstanceConfigurationOption option) {

		if (option == DefaultModelSlotInstanceConfigurationOption.CreatePrivateNewModel) {
			setTargetResourceCenter(getResourceCenter());
		}
		super.setOption(option);
	}

	public String getModelUri() {
		if (modelUri == null) {
			FlexoResourceCenter localRC;
			if (getTargetResourceCenter() != null) {
				localRC = getTargetResourceCenter();
			}
			else {
				localRC = getResourceCenter();
			}
			String generatedUri = null;
			if (relativePath != null)
				generatedUri = localRC.getDefaultBaseURI() + relativePath + "/" + getFilename();
			else
				generatedUri = localRC.getDefaultBaseURI() + "/" + getFilename();
			return generatedUri;
		}
		else
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
			getPropertyChangeSupport().firePropertyChange("modelUri", oldValue, filename);
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
			getPropertyChangeSupport().firePropertyChange("modelUri", oldValue, filename);
		}
	}

	@Override
	public boolean isValidConfiguration() {
		String uri = getModelUri();
		if (!super.isValidConfiguration()) {
			return false;
		}
		if (getOption() == DefaultModelSlotInstanceConfigurationOption.SelectExistingModel) {
			if (getResource() == null) {
				setErrorMessage(getLocales().localizedForKey("no_model_selected"));
				return false;
			}
			return true;
		}
		else if (getOption() == DefaultModelSlotInstanceConfigurationOption.CreatePrivateNewModel) {
			if (StringUtils.isEmpty(uri)) {
				setErrorMessage(getLocales().localizedForKey("please_supply_valid_uri"));
				return false;
			}
			try {
				new URL(uri);
			} catch (MalformedURLException e) {
				setErrorMessage(getLocales().localizedForKey("malformed_uri"));
				return false;
			}
			if (StringUtils.isEmpty(getRelativePath())) {
				setErrorMessage(getLocales().localizedForKey("please_supply_valid_relative_path"));
				return false;
			}
			return checkValidFileName();
		}
		else if (getOption() == DefaultModelSlotInstanceConfigurationOption.CreateSharedNewModel) {
			if (getResourceCenter() == null) {
				setErrorMessage(getLocales().localizedForKey("please_select_a_resource_center"));
				return false;
			}
			if (StringUtils.isEmpty(uri)) {
				setErrorMessage(getLocales().localizedForKey("please_supply_valid_uri"));
				return false;
			}
			try {
				new URL(uri);
			} catch (MalformedURLException e) {
				setErrorMessage(getLocales().localizedForKey("malformed_uri"));
				return false;
			}
			if (StringUtils.isEmpty(getRelativePath())) {
				setErrorMessage(getLocales().localizedForKey("please_supply_valid_relative_path"));
				return false;
			}
			return checkValidFileName();
		}
		else if (getOption() == DefaultModelSlotInstanceConfigurationOption.LeaveEmpty) {
			if (getModelSlot().getIsRequired()) {
				setErrorMessage(getLocales().localizedForKey("model_is_required"));
				return false;
			}
			return true;
		}
		return false;
	}

	protected boolean checkValidFileName() {
		if (StringUtils.isEmpty(getFilename())) {
			setErrorMessage(getLocales().localizedForKey("please_supply_valid_file_name"));
			return false;
		}
		return true;
	}

}
