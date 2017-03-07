/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml;

import java.io.FileNotFoundException;

import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.gina.annotation.FIBPanel;

/**
 * Represent the type of a DiagramInstance of a given Diagram
 * 
 * @author sylvain
 * 
 */
public class VirtualModelInstanceType extends FlexoConceptInstanceType {

	public static VirtualModelInstanceType UNDEFINED_VIRTUAL_MODEL_INSTANCE_TYPE = new VirtualModelInstanceType(
			(AbstractVirtualModel<?>) null);

	public VirtualModelInstanceType(AbstractVirtualModel<?> aVirtualModel) {
		super(aVirtualModel);
	}

	protected VirtualModelInstanceType(String virtualModelURI) {
		super(virtualModelURI);
	}

	@Override
	public Class<?> getBaseClass() {
		return VirtualModelInstance.class;
	}

	public AbstractVirtualModel<?> getVirtualModel() {
		return (AbstractVirtualModel<?>) getFlexoConcept();
	}

	@Override
	public void resolve(CustomTypeFactory<?> factory) {
		if (factory instanceof VirtualModelInstanceTypeFactory) {
			VirtualModel virtualModel;
			try {
				virtualModel = ((VirtualModelInstanceTypeFactory) factory).getTechnologyAdapter().getTechnologyAdapterService()
						.getServiceManager().getViewPointLibrary().getVirtualModel(conceptURI);
				if (virtualModel != null) {
					flexoConcept = virtualModel;
					this.customTypeFactory = null;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ResourceLoadingCancelledException e) {
				e.printStackTrace();
			} catch (FlexoException e) {
				e.printStackTrace();
			}
		}
		else {
			super.resolve(factory);
		}
	}

	public static VirtualModelInstanceType getVirtualModelInstanceType(AbstractVirtualModel<?> aVirtualModel) {
		if (aVirtualModel != null) {
			return (VirtualModelInstanceType) aVirtualModel.getInstanceType();
		}
		else {
			// logger.warning("Trying to get a VirtualModelInstanceType for a null VirtualModel");
			return UNDEFINED_VIRTUAL_MODEL_INSTANCE_TYPE;
		}
	}

	/**
	 * Factory for FlexoConceptInstanceType instances
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/CustomType/VirtualModelInstanceTypeFactory.fib")
	public static class VirtualModelInstanceTypeFactory extends TechnologyAdapterTypeFactory<VirtualModelInstanceType> {

		public VirtualModelInstanceTypeFactory(FMLTechnologyAdapter technologyAdapter) {
			super(technologyAdapter);
		}

		@Override
		public Class<VirtualModelInstanceType> getCustomType() {
			return VirtualModelInstanceType.class;
		}

		@Override
		public VirtualModelInstanceType makeCustomType(String configuration) {

			AbstractVirtualModel<?> virtualModel = null;

			if (configuration != null) {
				VirtualModelResource virtualModelResource = getTechnologyAdapter().getTechnologyAdapterService().getServiceManager()
						.getViewPointLibrary().getVirtualModelResource(configuration);
				if (virtualModelResource != null && virtualModelResource.isLoaded()) {
					virtualModel = virtualModelResource.getLoadedResourceData();
				}
			}
			else {
				virtualModel = getVirtualModelType();
			}

			if (virtualModel != null) {
				return getVirtualModelInstanceType(virtualModel);
			}
			else {
				// We don't return UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE because we want here a mutable type
				// if FlexoConcept might be resolved later
				return new VirtualModelInstanceType(configuration);
			}
		}

		private AbstractVirtualModel<?> virtualModelType;

		public AbstractVirtualModel<?> getVirtualModelType() {
			return virtualModelType;
		}

		public void setVirtualModelType(AbstractVirtualModel<?> virtualModelType) {
			if (virtualModelType != this.virtualModelType) {
				AbstractVirtualModel<?> oldVirtualModelType = this.virtualModelType;
				this.virtualModelType = virtualModelType;
				getPropertyChangeSupport().firePropertyChange("virtualModelType", oldVirtualModelType, virtualModelType);
			}
		}

		@Override
		public String toString() {
			return "Instance of VirtualModel";
		}

		@Override
		public void configureFactory(VirtualModelInstanceType type) {
			if (type != null) {
				setVirtualModelType(type.getVirtualModel());
			}
		}
	}

}
