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

import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.TechnologySpecificPamelaResourceFactory;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.FlexoVersion;

/**
 * Generic implementation of PamelaResourceFactory for {@link AbstractVirtualModelInstanceResource}
 * 
 * @author sylvain
 *
 */
public abstract class AbstractVirtualModelInstanceResourceFactory<VMI extends VirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter<TA>, VMR extends AbstractVirtualModelInstanceResource<VMI, TA>>
		extends TechnologySpecificPamelaResourceFactory<VMR, VMI, TA, AbstractVirtualModelInstanceModelFactory<?>> {

	public static final FlexoVersion INITIAL_REVISION = new FlexoVersion("0.1");

	private static final Logger logger = Logger.getLogger(AbstractVirtualModelInstanceResourceFactory.class.getPackage().getName());

	public AbstractVirtualModelInstanceResourceFactory(Class<VMR> resourceClass) throws ModelDefinitionException {
		super(resourceClass);
	}

	@Override
	public abstract VMI makeEmptyResourceData(VMR resource);

}
