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

package org.openflexo.market;

import java.awt.Window;
import java.util.logging.Logger;

import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.swing.utils.JFIBDialog;
import org.openflexo.foundation.remoteresources.FlexoUpdateService;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;


public class FlexoMarketEditorDialog extends JFIBDialog<FlexoMarketEditor>{

	
	static final Logger logger = Logger.getLogger(FlexoMarketEditorDialog.class.getPackage().getName());

	public static final Resource FLEXO_MARKET_EDITOR_FIB = ResourceLocator.locateResource("Fib/FlexoMarketEditor.fib");

	private static FlexoMarketEditor flexoMarketEditor = null;

	private static FlexoMarketEditorDialog dialog;

	public static FlexoMarketEditorDialog getFlexoMarketEditorDialog(FlexoUpdateService service, Window parent) {
		System.out.println("showFlexoMarketEditorDialog with " + service);

		if (dialog == null) {
			dialog = new FlexoMarketEditorDialog(service, parent);
		}

		return dialog;
	}

	public static void showFlexoMarketEditorDialog(FlexoUpdateService service, Window parent) {
		System.out.println("showFlexoMarketEditor with " + service);

		if (dialog == null) {
			dialog = getFlexoMarketEditorDialog(service, parent);
		}
		dialog.showDialog();
	}

	public static FlexoMarketEditor getFlexoMarketEditor(FlexoUpdateService service) {
		if (flexoMarketEditor == null) {
			flexoMarketEditor = new FlexoMarketEditor(service);
		}
		return flexoMarketEditor;
	}

	public FlexoMarketEditorDialog(FlexoUpdateService service, Window parent) {

		super(FIBLibrary.instance().retrieveFIBComponent(FLEXO_MARKET_EDITOR_FIB,true), getFlexoMarketEditor(service), parent,
				true, FlexoLocalization.getMainLocalizer());
		getData().setOwner(this);
		setTitle("Flexo Market Editor");
	}

}
	
