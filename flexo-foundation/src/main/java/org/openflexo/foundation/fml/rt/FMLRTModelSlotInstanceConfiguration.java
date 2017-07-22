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

package org.openflexo.foundation.fml.rt;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.rt.action.ModelSlotInstanceConfiguration;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * This class is used to stored the configuration of a {@link FMLRTModelSlot} which has to be instantiated
 * 
 * 
 * @author sylvain
 * 
 */
@Deprecated
public class FMLRTModelSlotInstanceConfiguration<VMI extends VirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter>
		extends ModelSlotInstanceConfiguration<FMLRTModelSlot<VMI, TA>, VMI> {

	private static final Logger logger = Logger.getLogger(FMLRTModelSlotInstanceConfiguration.class.getPackage().getName());

	private final List<ModelSlotInstanceConfigurationOption> options;
	private AbstractVirtualModelInstanceResource<VMI, TA> addressedVirtualModelInstanceResource;

	protected FMLRTModelSlotInstanceConfiguration(FMLRTModelSlot<VMI, TA> ms, FlexoConceptInstance fci, FlexoResourceCenter<?> rc) {
		super(ms, fci, rc);
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
	public ModelSlotInstance<FMLRTModelSlot<VMI, TA>, VMI> createModelSlotInstance(FlexoConceptInstance fci,
			VirtualModelInstance<?, ?> view) {
		AbstractVirtualModelInstanceModelFactory<?> factory = fci.getFactory();
		VirtualModelModelSlotInstance returned = factory.newInstance(VirtualModelModelSlotInstance.class);
		returned.setModelSlot(getModelSlot());
		returned.setFlexoConceptInstance(fci);
		if (getAddressedVirtualModelInstanceResource() != null) {
			returned.setVirtualModelInstanceURI(getAddressedVirtualModelInstanceResource().getURI());
		}
		else {
			logger.warning("Addressed virtual model instance is null");
		}
		return returned;
	}

	public AbstractVirtualModelInstanceResource<VMI, TA> getAddressedVirtualModelInstanceResource() {
		return addressedVirtualModelInstanceResource;
	}

	public void setAddressedVirtualModelInstanceResource(
			AbstractVirtualModelInstanceResource<VMI, TA> addressedVirtualModelInstanceResource) {
		if (this.addressedVirtualModelInstanceResource != addressedVirtualModelInstanceResource) {
			AbstractVirtualModelInstanceResource<VMI, TA> oldValue = this.addressedVirtualModelInstanceResource;
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
				setErrorMessage(getResourceCenter().getLocales().localizedForKey("no_virtual_model_instance_selected"));
				return false;
			}
			return true;
		}
		else if (getOption() == DefaultModelSlotInstanceConfigurationOption.CreateNewVirtualModel) {
			// Not implemented yet
			setErrorMessage(getResourceCenter().getLocales().localizedForKey("not_implemented_yet"));
			return false;

		}
		else if (getOption() == DefaultModelSlotInstanceConfigurationOption.LeaveEmpty) {
			if (getModelSlot().getIsRequired()) {
				setErrorMessage(getResourceCenter().getLocales().localizedForKey("virtual_model_instance_is_required"));
				return false;
			}
			return true;

		}
		return false;
	}

	@Override
	public AbstractVirtualModelInstanceResource<VMI, TA> getResource() {
		return getAddressedVirtualModelInstanceResource();
	}

}
