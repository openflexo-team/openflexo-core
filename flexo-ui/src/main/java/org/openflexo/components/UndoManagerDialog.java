/*
 * (c) Copyright 2014 Openflexo
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
import java.util.logging.Logger;

import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.controller.FIBDialog;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.undo.UndoManager;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Dialog allowing to show UndoManager informations
 * 
 * @author sylvain
 * 
 */
@SuppressWarnings("serial")
public class UndoManagerDialog extends FIBDialog<UndoManager> {

	static final Logger logger = Logger.getLogger(UndoManagerDialog.class.getPackage().getName());

	public static final Resource UNDO_MANAGER_FIB = ResourceLocator.locateResource("Fib/UndoManager.fib");

	private static UndoManagerDialog dialog;

	public static UndoManagerDialog getUndoManagerDialog(FlexoServiceManager serviceManager, Window parent) {
		if (dialog == null) {
			dialog = new UndoManagerDialog(serviceManager, parent);
		}

		return dialog;
	}

	public static void showUndoManagerDialog(FlexoServiceManager serviceManager, Window parent) {
		System.out.println("showUndoManagerDialog with " + serviceManager);

		if (dialog == null) {
			dialog = getUndoManagerDialog(serviceManager, parent);
		}
		dialog.showDialog();
	}

	public UndoManagerDialog(FlexoServiceManager serviceManager, Window parent) {

		super(FIBLibrary.instance().retrieveFIBComponent(UNDO_MANAGER_FIB, true), serviceManager.getEditingContext().getUndoManager(),
				parent, false, FlexoLocalization.getMainLocalizer());

		setTitle("Undo Manager");

	}

}
