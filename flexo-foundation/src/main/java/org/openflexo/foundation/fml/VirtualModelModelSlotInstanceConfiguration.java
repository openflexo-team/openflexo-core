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
package org.openflexo.foundation.fml;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fmlrt.ModelSlotInstance;
import org.openflexo.foundation.fmlrt.View;
import org.openflexo.foundation.fmlrt.VirtualModelInstance;
import org.openflexo.foundation.fmlrt.VirtualModelInstanceModelFactory;
import org.openflexo.foundation.fmlrt.VirtualModelModelSlotInstance;
import org.openflexo.foundation.fmlrt.action.CreateVirtualModelInstance;
import org.openflexo.foundation.fmlrt.action.ModelSlotInstanceConfiguration;
import org.openflexo.foundation.fmlrt.rm.VirtualModelInstanceResource;
import org.openflexo.localization.FlexoLocalization;

/**
 * This class is used to stored the configuration of a {@link VirtualModelModelSlot} which has to be instantiated
 * 
 * 
 * @author sylvain
 * 
 */
public class VirtualModelModelSlotInstanceConfiguration extends ModelSlotInstanceConfiguration<VirtualModelModelSlot, VirtualModelInstance> {

	private static final Logger logger = Logger.getLogger(VirtualModelModelSlotInstanceConfiguration.class.getPackage().getName());

	private final List<ModelSlotInstanceConfigurationOption> options;
	private VirtualModelInstanceResource addressedVirtualModelInstanceResource;

	protected VirtualModelModelSlotInstanceConfiguration(VirtualModelModelSlot ms, CreateVirtualModelInstance action) {
		super(ms, action);
		options = new ArrayList<ModelSlotInstanceConfiguration.ModelSlotInstanceConfigurationOption>();
		/*if (ms.isReflexiveModelSlot()) {
			options.add(DefaultModelSlotInstanceConfigurationOption.Autoconfigure);
		} else {*/
		options.add(DefaultModelSlotInstanceConfigurationOption.SelectExistingVirtualModel);
		options.add(DefaultModelSlotInstanceConfigurationOption.CreateNewVirtualModel);
		if (!ms.getIsRequired()) {
			options.add(DefaultModelSlotInstanceConfigurationOption.LeaveEmpty);
		}
		setOption(DefaultModelSlotInstanceConfigurationOption.SelectExistingVirtualModel);
		// }
	}

	@Override
	public List<ModelSlotInstanceConfigurationOption> getAvailableOptions() {
		return options;
	}

	@Override
	public ModelSlotInstance<VirtualModelModelSlot, VirtualModelInstance> createModelSlotInstance(VirtualModelInstance vmInstance, View view) {
		VirtualModelInstanceModelFactory factory = vmInstance.getFactory();
		System.out.println("factory=" + factory);
		VirtualModelModelSlotInstance returned = factory.newInstance(VirtualModelModelSlotInstance.class);
		returned.setModelSlot(getModelSlot());
		returned.setVirtualModelInstance(vmInstance);
		if (getAddressedVirtualModelInstanceResource() != null) {
			returned.setVirtualModelInstanceURI(getAddressedVirtualModelInstanceResource().getURI());
		} else {
			logger.warning("Addressed virtual model instance is null");
		}
		return returned;
	}

	public VirtualModelInstanceResource getAddressedVirtualModelInstanceResource() {
		return addressedVirtualModelInstanceResource;
	}

	public void setAddressedVirtualModelInstanceResource(VirtualModelInstanceResource addressedVirtualModelInstanceResource) {
		if (this.addressedVirtualModelInstanceResource != addressedVirtualModelInstanceResource) {
			VirtualModelInstanceResource oldValue = this.addressedVirtualModelInstanceResource;
			this.addressedVirtualModelInstanceResource = addressedVirtualModelInstanceResource;
			getPropertyChangeSupport().firePropertyChange("addressedVirtualModelInstanceResource", oldValue,
					addressedVirtualModelInstanceResource);
		}
	}

	@Override
	public boolean isValidConfiguration() {
		if (!super.isValidConfiguration()) {
			return false;
		}
		if (getOption() == DefaultModelSlotInstanceConfigurationOption.SelectExistingVirtualModel) {
			if (getAddressedVirtualModelInstanceResource() == null) {
				setErrorMessage(FlexoLocalization.localizedForKey("no_virtual_model_instance_selected"));
				return false;
			}
			return true;
		} else if (getOption() == DefaultModelSlotInstanceConfigurationOption.CreateNewVirtualModel) {
			// Not implemented yet
			setErrorMessage(FlexoLocalization.localizedForKey("not_implemented_yet"));
			return false;

		} else if (getOption() == DefaultModelSlotInstanceConfigurationOption.LeaveEmpty) {
			if (getModelSlot().getIsRequired()) {
				setErrorMessage(FlexoLocalization.localizedForKey("virtual_model_instance_is_required"));
				return false;
			}
			return true;

		}
		return false;
	}

	@Override
	public VirtualModelInstanceResource getResource() {
		return getAddressedVirtualModelInstanceResource();
	}

}
