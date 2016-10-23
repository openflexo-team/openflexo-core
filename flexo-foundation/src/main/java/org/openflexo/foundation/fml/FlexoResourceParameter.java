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

import java.lang.reflect.Type;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(FlexoResourceParameter.FlexoResourceParameterImpl.class)
@XMLElement
public interface FlexoResourceParameter extends FlexoBehaviourParameter {

	@PropertyIdentifier(type = TechnologyAdapter.class)
	public static final String RESOURCE_TECHNOLOGY_ADAPTER_KEY = "resourceTechnologyAdapter";

	@PropertyIdentifier(type = Class.class)
	public static final String RESOURCE_TYPE_KEY = "technologyAdapterResourceType";

	@Getter(value = RESOURCE_TECHNOLOGY_ADAPTER_KEY, ignoreType = true)
	public TechnologyAdapter getResourceTechnologyAdapter();

	@Setter(RESOURCE_TECHNOLOGY_ADAPTER_KEY)
	public void setResourceTechnologyAdapter(TechnologyAdapter technologyAdapter);

	@Getter(value = RESOURCE_TYPE_KEY)
	@XMLAttribute
	public Class<? extends TechnologyAdapterResource<?, ?>> getTechnologyAdapterResourceType();

	@Setter(RESOURCE_TYPE_KEY)
	public void setTechnologyAdapterResourceType(Class<? extends TechnologyAdapterResource<?, ?>> type);

	public Type getResourceDataType();

	public static abstract class FlexoResourceParameterImpl extends FlexoBehaviourParameterImpl implements FlexoResourceParameter {

		@Override
		public Type getType() {
			Class<? extends TechnologyAdapterResource<?, ?>> rsc_Type = getTechnologyAdapterResourceType();
			if (rsc_Type != null) {
				return rsc_Type;
			}
			return TechnologyAdapterResource.class;
		}

		@Override
		public Type getResourceDataType() {
			if (getTechnologyAdapterResourceType() != null) {
				return TypeUtils.getTypeArgument(getTechnologyAdapterResourceType(), TechnologyAdapterResource.class, 0);
			}
			return ResourceData.class;
		}

		@Override
		public WidgetType getWidget() {
			return WidgetType.TECHNOLOGY_RESOURCE;
		}

		@Override
		public TechnologyAdapter getResourceTechnologyAdapter() {
			TechnologyAdapter returned = (TechnologyAdapter) performSuperGetter(RESOURCE_TECHNOLOGY_ADAPTER_KEY);
			if (returned == null && getTechnologyAdapterResourceType() != null && getServiceManager() != null) {
				Class<? extends TechnologyAdapter> taClass = (Class<? extends TechnologyAdapter>) TypeUtils
						.getTypeArgument(getTechnologyAdapterResourceType(), TechnologyAdapterResource.class, 1);
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(taClass);
			}
			return returned;
		}

		@Override
		public void setResourceTechnologyAdapter(TechnologyAdapter technologyAdapter) {
			TechnologyAdapter oldValue = (TechnologyAdapter) performSuperGetter(RESOURCE_TECHNOLOGY_ADAPTER_KEY);
			if (oldValue != technologyAdapter) {
				performSuperSetter(RESOURCE_TECHNOLOGY_ADAPTER_KEY, technologyAdapter);
				setTechnologyAdapterResourceType(null);
			}
		}

	}
}
