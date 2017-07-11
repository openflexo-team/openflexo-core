/*
 * (c) Copyright 2013 Openflexo
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

package org.openflexo.foundation.fml.rt.rm;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.resource.PamelaResourceFactory;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.FlexoVersion;

/**
 * Generic implementation of PamelaResourceFactory for {@link VirtualModelInstanceResource}
 * 
 * @author sylvain
 *
 */
public abstract class VirtualModelInstanceResourceFactory<VMI extends VirtualModelInstance<VMI, VM>, VM extends VirtualModel<VM>, VMR extends VirtualModelInstanceResource<VMI, VM>>
		extends PamelaResourceFactory<VMR, VMI, FMLRTTechnologyAdapter, VirtualModelInstanceModelFactory<?>> {

	public static final FlexoVersion INITIAL_REVISION = new FlexoVersion("0.1");
	public static final FlexoVersion CURRENT_FML_RT_VERSION = new FlexoVersion("1.0");
	public static final String CORE_FILE_SUFFIX = ".xml";

	private static final Logger logger = Logger.getLogger(VirtualModelInstanceResourceFactory.class.getPackage().getName());

	public VirtualModelInstanceResourceFactory(Class<VMR> resourceClass) throws ModelDefinitionException {
		super(resourceClass);
	}

	@Override
	public abstract VMI makeEmptyResourceData(VMR resource);

}
