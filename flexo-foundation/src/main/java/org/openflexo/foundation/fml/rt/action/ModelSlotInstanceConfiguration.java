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

import java.util.List;

import org.openflexo.foundation.DefaultFlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.localization.FlexoLocalization;

/**
 * This class is used to stored the configuration of a {@link ModelSlot} which has to be instantiated
 * 
 * 
 * @author sylvain
 * 
 */
public abstract class ModelSlotInstanceConfiguration<MS extends ModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<?>>
		extends DefaultFlexoObject {

	private final AbstractCreateVirtualModelInstance<?, ?, ?, ?> action;
	private final MS modelSlot;
	private ModelSlotInstanceConfigurationOption option;

	public static interface ModelSlotInstanceConfigurationOption {
		public String name();

		public String getDescriptionKey();
	}

	public static enum DefaultModelSlotInstanceConfigurationOption implements ModelSlotInstanceConfigurationOption {

		/**
		 * Retrieve an existing resource from a ResourceCenter
		 */
		SelectExistingResource, /**
								 * Retrieve an existing model from a ResourceCenter
								 */
		SelectExistingModel, /**
								 * Retrieve an existing metamodel from a ResourceCenter
								 */
		SelectExistingMetaModel, /**
									 * Create a dedicated model in the scope of current {@link FlexoProject}
									 */
		CreatePrivateNewModel, /**
								 * Create a resource in a ResourceCenter (the resource might be shared and concurrently accessed)
								 */
		CreatePrivateNewResource, /**
									 * Create a model in a ResourceCenter (the model might be shared and concurrently accessed)
									 */
		CreateSharedNewModel, /**
								 * Create a resource in a ResourceCenter (the resource might be shared and concurrently accessed)
								 */
		CreateSharedNewResource, /**
									 * Retrieve an existing virtual model instance
									 */
		SelectExistingVirtualModel, /**
									 * Creates a new virtual model
									 */
		CreateNewVirtualModel, /**
								 * Leave empty and decide later
								 */
		LeaveEmpty, /**
					 * Let Openflexo manage this
					 */
		Autoconfigure;

		@Override
		public String getDescriptionKey() {
			return name() + "_description";
		}
	}

	protected ModelSlotInstanceConfiguration(MS ms, AbstractCreateVirtualModelInstance<?, ?, ?, ?> action) {
		this.action = action;
		modelSlot = ms;
	}

	public AbstractCreateVirtualModelInstance<?, ?, ?, ?> getAction() {
		return action;
	}

	public MS getModelSlot() {
		return modelSlot;
	}

	public FlexoMetaModelResource<?, ?, ?> getMetaModelResource() {
		if (getModelSlot() instanceof TypeAwareModelSlot) {
			return ((TypeAwareModelSlot) getModelSlot()).getMetaModelResource();
		}
		return null;
	}

	public ModelSlotInstanceConfigurationOption getOption() {
		return option;
	}

	public void setOption(ModelSlotInstanceConfigurationOption option) {
		if (option != this.option) {
			ModelSlotInstanceConfigurationOption oldValue = this.option;
			this.option = option;
			getPropertyChangeSupport().firePropertyChange("option", oldValue, option);
		}
	}

	/*public void setOption(ModelSlotInstanceConfigurationOption option) {
		this.option = option;
	}*/

	public abstract List<ModelSlotInstanceConfigurationOption> getAvailableOptions();

	public boolean isValid() {
		return isValidConfiguration();
	}

	public boolean isValidConfiguration() {
		if (option == null) {
			setErrorMessage(FlexoLocalization.localizedForKey("please_select_an_option"));
			return false;
		}
		return true;
	}

	/**
	 * Called to instantiate a {@link ModelSlotInstance} using this configuration, in supplied VirtualModelInstance
	 * 
	 * @param vmInstance
	 *            the virtual model instance where the ModelSlotInstance should be created
	 * @param view
	 *            the view in which the VirtualModelInstance will be added
	 * @return
	 */
	public abstract ModelSlotInstance<MS, RD> createModelSlotInstance(AbstractVirtualModelInstance<?, ?> vmInstance, View view);

	private String errorMessage;

	public String getErrorMessage() {
		isValidConfiguration();
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		if (!errorMessage.equals(this.errorMessage)) {
			String oldValue = this.errorMessage;
			this.errorMessage = errorMessage;
			getPropertyChangeSupport().firePropertyChange("errorMessage", oldValue, errorMessage);
		}
	}

	public abstract TechnologyAdapterResource<RD, ?> getResource();
}
