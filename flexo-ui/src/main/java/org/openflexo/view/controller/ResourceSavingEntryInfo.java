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

import java.util.logging.Logger;

import javax.swing.Icon;

import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.logging.FlexoLogger;

public class ResourceSavingEntryInfo {
	private static final Logger logger = FlexoLogger.getLogger(ResourceSavingEntryInfo.class.getPackage().getName());

	protected FlexoResource<?> resource;
	protected boolean saveThisResource = true;

	public ResourceSavingEntryInfo(FlexoResource<?> resource) {
		this.resource = resource;
		reviewModifiedResource();
	}

	public void delete() {
		resource = null;
	}

	public Icon getIcon() {
		return FlexoController.statelessIconForObject(resource);
	}

	public String getName() {
		return resource.getName() + (isModified() ? " [" + FlexoLocalization.getMainLocalizer().localizedForKey("modified") + "]" : "");
	}

	public String getType() {
		if (resource.getResourceDataClass() == null) {
			logger.warning("Resource " + resource + " has no resource data class");
			return null;
		}
		return resource.getResourceDataClass().getSimpleName();
	}

	public boolean isModified() {
		/*System.out.println("Est ce que la resource " + resource + " est modifiee ?");
		if (resource.isLoaded()) {
			System.out.println("chargee=true modified=" + resource.getLoadedResourceData().isModified());
		}*/
		return resource.isLoaded() && resource.getLoadedResourceData().isModified();

	}

	public boolean saveThisResource() {
		return saveThisResource;
	}

	public void setSaveThisResource(boolean saveThisResource) {
		this.saveThisResource = saveThisResource;
	}

	public void reviewModifiedResource() {
		saveThisResource = isModified() && resource.getIODelegate().hasWritePermission();
	}

	public void saveModified() throws SaveResourceException {
		// progress.setProgress(FlexoLocalization.getMainLocalizer().localizedForKey("saving") + " " + resource.getName());
		if (saveThisResource && resource.getIODelegate().hasWritePermission()) {
			resource.save();
		}
	}

}
