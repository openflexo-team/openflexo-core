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
import org.openflexo.localization.FlexoLocalization;


public class ConflictedResourceEditorDialog extends FIBDialog<ConflictedResourceEditor>{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6195720264442503633L;

	static final Logger logger = Logger.getLogger(ConflictedResourceEditorDialog.class.getPackage().getName());

	public static final Resource RESOURCE_CONSISTENCY_EDITOR_FIB = ResourceLocator.locateResource("Fib/ConflictedResourceEditor.fib");

	private static ConflictedResourceEditorDialog dialog;

	public static ConflictedResourceEditorDialog getResourceConsistencyEditorDialog(ConflictedResourceSet resources, ResourceConsistencyService service, Window parent) {
		if (dialog == null) {
			dialog = new ConflictedResourceEditorDialog(resources,service, parent);
		}
		return dialog;
	}

	public static void showResourceConsistencyEditorDialog(ConflictedResourceSet resources, ResourceConsistencyService service, Window parent) {
		if (dialog == null) {
			dialog = getResourceConsistencyEditorDialog(resources, service, parent);
		}
		dialog.setData(new ConflictedResourceEditor(resources, service));
		dialog.showDialog();
	}

	public ConflictedResourceEditorDialog(ConflictedResourceSet resources, ResourceConsistencyService service, Window parent) {

		super(FIBLibrary.instance().retrieveFIBComponent(RESOURCE_CONSISTENCY_EDITOR_FIB,true), new ConflictedResourceEditor(resources, service), parent,
				true, FlexoLocalization.getMainLocalizer());
		getData().setOwner(this);
		setTitle("Resource Consistency Editor");
	}

}
	
