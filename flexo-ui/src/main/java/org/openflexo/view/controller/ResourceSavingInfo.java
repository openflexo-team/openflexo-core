/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.view.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.logging.FlexoLogger;

public class ResourceSavingInfo {

	private static final Logger logger = FlexoLogger.getLogger(ResourceSavingInfo.class.getPackage().getName());

	private final ResourceManager resourceManager;
	private final List<ResourceSavingEntryInfo> entries;

	public ResourceSavingInfo(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
		entries = new ArrayList<ResourceSavingEntryInfo>();
		update();
	}

	private void update() {
		entries.clear();
		for (FlexoResource<?> r : resourceManager.getLoadedResources()) {
			ResourceSavingEntryInfo newResourceEntryInfo = new ResourceSavingEntryInfo(r);
			entries.add(newResourceEntryInfo);
		}
	}

	public List<ResourceSavingEntryInfo> getEntries() {
		return entries;
	}

	public void saveSelectedResources() {
		List<ResourceSavingEntryInfo> resourcesToSave = new ArrayList<ResourceSavingEntryInfo>();
		for (ResourceSavingEntryInfo e : entries) {
			if (e.saveThisResource()) {
				resourcesToSave.add(e);
			}
		}

		for (ResourceSavingEntryInfo e : entries) {
			if (e.saveThisResource()) {
				try {
					logger.info("Saving " + e.resource);
					e.saveModified();
				} catch (SaveResourceException e1) {
					logger.warning("Could not save resource " + e.resource);
					e1.printStackTrace();
				}
			}
		}
	}
}
