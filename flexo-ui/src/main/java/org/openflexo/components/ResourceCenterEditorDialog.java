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

package org.openflexo.components;

import java.awt.Window;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.gina.FIBLibrary;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.controller.ResourceCenterEditor;

/**
 * Dialog allowing to edit all {@link FlexoResourceCenter}
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class ResourceCenterEditorDialog extends JFIBDialog<ResourceCenterEditor> {

	static final Logger logger = Logger.getLogger(ResourceCenterEditorDialog.class.getPackage().getName());

	public static final Resource RESOURCE_CENTER_EDITOR_FIB = ResourceLocator.locateResource("Fib/ResourceCenterEditor.fib");

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

		super(FIBLibrary.instance().retrieveFIBComponent(RESOURCE_CENTER_EDITOR_FIB, true), getResourceCenterEditor(serviceManager),
				parent, false, FlexoLocalization.getMainLocalizer());
		getData().setOwner(this);
		setTitle("Resource Center Editor");
	}

}
