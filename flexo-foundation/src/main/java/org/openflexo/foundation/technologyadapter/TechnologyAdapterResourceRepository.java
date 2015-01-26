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

package org.openflexo.foundation.technologyadapter;

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceRepository;

/**
 * A {@link TechnologyAdapterResourceRepository} stores all resources storing resources relative to a given technology<br>
 * Resources are organized with a folder hierarchy inside a {@link ResourceRepository}
 * 
 * @author sylvain
 * 
 * @param <R>
 * @param <TA>
 */
public abstract class TechnologyAdapterResourceRepository<R extends TechnologyAdapterResource<RD, TA> & FlexoResource<RD>, TA extends TechnologyAdapter, RD extends ResourceData<RD> & TechnologyObject<TA>>
		extends ResourceRepository<R> {

	private static final Logger logger = Logger.getLogger(TechnologyAdapterResourceRepository.class.getPackage().getName());

	private final TA technologyAdapter;
	private FlexoResourceCenter<?> resourceCenter;

	public TechnologyAdapterResourceRepository(TA technologyAdapter, FlexoResourceCenter<?> resourceCenter) {
		this(technologyAdapter, resourceCenter,
				resourceCenter instanceof FileSystemBasedResourceCenter ? ((FileSystemBasedResourceCenter) resourceCenter)
						.getRootDirectory() : null);
	}

	public TechnologyAdapterResourceRepository(TA technologyAdapter, FlexoResourceCenter<?> resourceCenter, File directory) {
		super(resourceCenter);
		this.technologyAdapter = technologyAdapter;
		this.resourceCenter = resourceCenter;
		getRootFolder().setName(resourceCenter.getName());
		getRootFolder().setDescription(
				"Resource Repository for technology " + technologyAdapter.getName() + " resource center: " + resourceCenter);
	}

	public TA getTechnologyAdapter() {
		return technologyAdapter;
	}

	public FlexoResourceCenter getResourceCenter() {
		return resourceCenter;
	}

	public void setResourceCenter(FlexoResourceCenter resourceCenter) {
		this.resourceCenter = resourceCenter;
	}

}
