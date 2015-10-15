/**
 * 
 * Copyright (c) 2013-2014, Openflexo
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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.foundation.DefaultFlexoObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;

/**
 * A {@link TechnologyContextManager} manages for a technology, and shared by all {@link FlexoResourceCenter} declared in the scope of
 * {@link FlexoResourceCenterService}, all references and links between all resources managed by a given technology, it their original
 * technical space.
 * 
 * This class is responsible for maintaining consistency of related technology, in its original technological space (maintaining technical
 * links between all technology-specific stuff)
 * 
 * @author sylvain
 * 
 */
public abstract class TechnologyContextManager<TA extends TechnologyAdapter> extends DefaultFlexoObject {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TechnologyContextManager.class.getPackage().getName());

	private final TA adapter;
	private final FlexoResourceCenterService resourceCenterService;

	/** Stores all known resources where key is the URI of resource */
	protected Map<String, TechnologyAdapterResource<?, TA>> resources = new HashMap<String, TechnologyAdapterResource<?, TA>>();

	public TechnologyContextManager(TA adapter, FlexoResourceCenterService resourceCenterService) {
		this.adapter = adapter;
		this.resourceCenterService = resourceCenterService;
	}

	public FlexoServiceManager getServiceManager() {
		return getResourceCenterService().getServiceManager();
	}

	public TA getTechnologyAdapter() {
		return adapter;
	}

	public FlexoResourceCenterService getResourceCenterService() {
		return resourceCenterService;
	}

	/**
	 * Called when a new resource was registered, notify the {@link TechnologyContextManager}
	 * 
	 * @param newModel
	 */
	public void registerResource(TechnologyAdapterResource<?, TA> resource) {
		resources.put(resource.getURI(), resource);
	}

	/**
	 * Return resource identified by supplied uri, asserting this resource has been registered in this technology
	 * 
	 * @param uri
	 * @return
	 */
	public TechnologyAdapterResource<?, TA> getResourceWithURI(String uri) {
		return resources.get(uri);
	}

}
