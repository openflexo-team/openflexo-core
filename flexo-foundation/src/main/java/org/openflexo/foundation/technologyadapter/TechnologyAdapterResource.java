/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;

/**
 * A {@link TechnologyAdapterResource} is a {@link FlexoResource} specific to a technology
 * 
 * @author sylvain
 * 
 * @param <RD>
 */
@ModelEntity(isAbstract = true)
public interface TechnologyAdapterResource<RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter<TA>>
		extends FlexoResource<RD> {
	public static final String TECHNOLOGY_ADAPTER = "technologyAdapter";
	public static final String TECHNOLOGY_CONTEXT_MANAGER = "technologyContextManager";

	@Getter(value = TECHNOLOGY_ADAPTER, ignoreType = true)
	public TA getTechnologyAdapter();

	@Setter(TECHNOLOGY_ADAPTER)
	public void setTechnologyAdapter(TA technologyAdapter);

	@Getter(value = TECHNOLOGY_CONTEXT_MANAGER, ignoreType = true)
	public abstract TechnologyContextManager<TA> getTechnologyContextManager();

	@Setter(TECHNOLOGY_CONTEXT_MANAGER)
	public abstract void setTechnologyContextManager(TechnologyContextManager<TA> paramDOCXTechnologyContextManager);

}
