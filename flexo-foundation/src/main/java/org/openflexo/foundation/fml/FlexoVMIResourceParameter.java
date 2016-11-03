/**
 * 
 * Copyright (c) 2016, Openflexo
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

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(FlexoVMIResourceParameter.FlexoVMIResourceParameterImpl.class)
@XMLElement
// TODO: deprecated, use generic FlexoBehaviourParameter instead
@Deprecated
public interface FlexoVMIResourceParameter extends FlexoResourceParameter {

	@PropertyIdentifier(type = String.class)
	public static final String VIRTUAL_MODEL_URI_KEY = "virtualModelURI";
	@PropertyIdentifier(type = VirtualModelResource.class)
	public static final String VIRTUAL_MODEL_RESOURCE_KEY = "virtualModelResource";

	@Getter(value = VIRTUAL_MODEL_URI_KEY)
	@XMLAttribute(xmlTag = "virtualModelURI")
	public String getVirtualModelURI();

	@Setter(VIRTUAL_MODEL_URI_KEY)
	public void setVirtualModelURI(String virtualModelURI);

	@Getter(value = VIRTUAL_MODEL_RESOURCE_KEY)
	public VirtualModelResource getVirtualModelResource();

	@Setter(VIRTUAL_MODEL_RESOURCE_KEY)
	public void setVirtualModelResource(VirtualModelResource virtualModelResource);

	public static abstract class FlexoVMIResourceParameterImpl extends FlexoResourceParameterImpl implements FlexoVMIResourceParameter {

		private static final Logger logger = Logger.getLogger(FlexoBehaviourParameter.class.getPackage().getName());

		@Override
		public void setVirtualModelURI(String virtualModelURI) {
			performSuperSetter(VIRTUAL_MODEL_URI_KEY, virtualModelURI);
			// TODO: find and set resource!
			this.getServiceManager().getResourceManager().getResource(virtualModelURI);
		}

		@Override
		public Type getType() {
			return VirtualModelInstanceResource.class;
		}

		@Override
		public Type getResourceDataType() {
			return VirtualModelInstance.class;
		}

		@Override
		public WidgetType getWidget() {
			// return WidgetType.VMI_RESOURCE;
			return WidgetType.CUSTOM_WIDGET;
		}

		@Override
		public TechnologyAdapter getResourceTechnologyAdapter() {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
		}

		@Override
		public void setResourceTechnologyAdapter(TechnologyAdapter technologyAdapter) {
			if (technologyAdapter != getResourceTechnologyAdapter()) {
				logger.warning("Trying to set an incompatible TA for FlexoVMIInstanceParameter");
			}
		}

	}
}
