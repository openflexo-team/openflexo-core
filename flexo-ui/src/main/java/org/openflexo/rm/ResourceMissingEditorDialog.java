/*
 * (c) Copyright 2012-2014 Openflexo
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

package org.openflexo.rm;

import java.awt.Window;
import java.util.logging.Logger;

import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.controller.FIBDialog;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.MissingFlexoResource;
import org.openflexo.localization.FlexoLocalization;


public class ResourceMissingEditorDialog extends FIBDialog<ResourceMissingEditor>{

	static final Logger logger = Logger.getLogger(ResourceMissingEditorDialog.class.getPackage().getName());

	public static final Resource RESOURCE_MISSING_EDITOR_FIB = ResourceLocator.locateResource("Fib/ResourceMissingEditor.fib");

	private static ResourceMissingEditorDialog dialog;

	public static ResourceMissingEditorDialog getResourceMissingEditorDialog(MissingFlexoResource missingResource, ResourceConsistencyService service, Window parent) {
		if (dialog == null) {
			dialog = new ResourceMissingEditorDialog(missingResource, service, parent);
		}
		return dialog;
	}

	public static void showResourceMissingEditorDialog(MissingFlexoResource missingResource, ResourceConsistencyService service, Window parent) {
		if (dialog == null) {
			dialog = getResourceMissingEditorDialog(missingResource, service, parent);
		}
		dialog.setData(new ResourceMissingEditor(missingResource, service));
		dialog.showDialog();
	}

	public ResourceMissingEditorDialog(MissingFlexoResource missingResource, ResourceConsistencyService service, Window parent) {

		super(FIBLibrary.instance().retrieveFIBComponent(RESOURCE_MISSING_EDITOR_FIB,true), new ResourceMissingEditor(missingResource, service), parent,
				true, FlexoLocalization.getMainLocalizer());
		getData().setOwner(this);
		setTitle("Resource Missing Editor");
	}

}
	
