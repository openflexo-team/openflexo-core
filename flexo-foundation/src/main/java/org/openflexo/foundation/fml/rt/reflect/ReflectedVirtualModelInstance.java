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

package org.openflexo.foundation.fml.rt.reflect;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * Implementation of an instance of a plain {@link VirtualModel} natively managed by the {@link FMLRTTechnologyAdapter}<br>
 * 
 * Such {@link VirtualModel} instance might be serialized using XML
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(ReflectedVirtualModelInstance.ReflectedVirtualModelInstanceImpl.class)
@XMLElement
public interface ReflectedVirtualModelInstance<VMI extends VirtualModelInstance<VMI, TA>, R extends TechnologyAdapterResource<RD, TA> & PamelaResource<RD, ?>, RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter<TA>>
		extends VirtualModelInstance<VMI, TA> {

	@PropertyIdentifier(type = TechnologyAdapterResource.class)
	public static final String REFLECTED_RESOURCE = "reflectedResource";
	@PropertyIdentifier(type = ReflectedVirtualModelInstanceModelFactory.class)
	public static final String REFLECTED_MODEL_FACTORY = "reflectedModelFactory";

	@Getter(value = REFLECTED_RESOURCE, ignoreType = true)
	public R getReflectedResource();

	@Setter(REFLECTED_RESOURCE)
	public void setReflectedResource(R resource);

	@Getter(value = REFLECTED_MODEL_FACTORY, ignoreType = true)
	public ReflectedVirtualModelInstanceModelFactory<R, RD, TA, ?> getReflectedModelFactory();

	@Setter(REFLECTED_MODEL_FACTORY)
	public void setReflectedModelFactory(ReflectedVirtualModelInstanceModelFactory<R, RD, TA, ?> factory);

	public static abstract class ReflectedVirtualModelInstanceImpl<VMI extends VirtualModelInstance<VMI, TA>, R extends TechnologyAdapterResource<RD, TA> & PamelaResource<RD, ?>, RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter<TA>>
			extends VirtualModelInstanceImpl<VMI, TA> implements ReflectedVirtualModelInstance<VMI, R, RD, TA> {

		private static final Logger logger = Logger.getLogger(ReflectedVirtualModelInstance.class.getPackage().getName());

		@Override
		public TA getTechnologyAdapter() {
			if (getReflectedResource() != null) {
				return getReflectedResource().getTechnologyAdapter();
			}
			return null;
		}

		@Override
		public FMLRTVirtualModelInstanceRepository<?> getVirtualModelInstanceRepository() {
			if (getResource() != null) {
				return getResource().getResourceCenter().getVirtualModelInstanceRepository();
			}
			return null;
		}
	}

}
