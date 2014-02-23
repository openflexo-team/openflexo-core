/*
 * (c) Copyright 2010-2011 AgileBirds
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
package org.openflexo.components;

import java.awt.Window;
import java.io.File;
import java.util.logging.Logger;

import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.controller.FIBDialog;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.FileResource;
import org.openflexo.view.controller.ResourceCenterEditor;

/**
 * Dialog allowing to edit all {@link FlexoResourceCenter}
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class ResourceCenterEditorDialog extends FIBDialog<ResourceCenterEditor> {

	static final Logger logger = Logger.getLogger(ResourceCenterEditorDialog.class.getPackage().getName());

	public static final File RESOURCE_CENTER_EDITOR_FIB = new FileResource("Fib/ResourceCenterEditor.fib");

	private static ResourceCenterEditor resourceCenterEditor = null;

	// private Window parent;

	// private static ResourceCenterEditor instance;
	private static ResourceCenterEditorDialog dialog;

	public static ResourceCenterEditorDialog getResourceCenterEditorDialog(FlexoServiceManager serviceManager, Window parent) {
		System.out.println("showResourceCenterEditor with " + serviceManager);

		if (dialog == null) {
			dialog = new ResourceCenterEditorDialog(serviceManager, parent);
		}

		return dialog;
	}

	public static void showResourceCenterEditorDialog(FlexoServiceManager serviceManager, Window parent) {
		System.out.println("showResourceCenterEditor with " + serviceManager);

		if (dialog == null) {
			dialog = getResourceCenterEditorDialog(serviceManager, parent);
		}
		dialog.showDialog();
	}

	public static ResourceCenterEditor getResourceCenterEditor(FlexoServiceManager serviceManager) {
		if (resourceCenterEditor == null) {
			resourceCenterEditor = new ResourceCenterEditor(serviceManager.getResourceCenterService());
		}
		return resourceCenterEditor;
	}

	public ResourceCenterEditorDialog(FlexoServiceManager serviceManager, Window parent) {

		super(FIBLibrary.instance().retrieveFIBComponent(RESOURCE_CENTER_EDITOR_FIB), getResourceCenterEditor(serviceManager), parent,
				true, FlexoLocalization.getMainLocalizer());

		setTitle("Resource Center Editor");

	}

}
