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

package org.openflexo.rm;

import java.awt.Window;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.localization.FlexoLocalization;

public class ConflictedResourceEditorDialog extends JFIBDialog<ConflictedResourceEditor> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6195720264442503633L;

	static final Logger logger = Logger.getLogger(ConflictedResourceEditorDialog.class.getPackage().getName());

	public static final Resource RESOURCE_CONSISTENCY_EDITOR_FIB = ResourceLocator.locateResource("Fib/ConflictedResourceEditor.fib");

	private static ConflictedResourceEditorDialog dialog;

	public static ConflictedResourceEditorDialog getResourceConsistencyEditorDialog(ConflictedResourceSet resources,
			ResourceConsistencyService service, ApplicationContext applicationContext, Window parent) {
		if (dialog == null) {
			dialog = new ConflictedResourceEditorDialog(resources, service, applicationContext, parent);
		}
		return dialog;
	}

	public static void showResourceConsistencyEditorDialog(ConflictedResourceSet resources, ResourceConsistencyService service,
			ApplicationContext applicationContext, Window parent) {
		if (dialog == null) {
			dialog = getResourceConsistencyEditorDialog(resources, service, applicationContext, parent);
		}
		dialog.setData(new ConflictedResourceEditor(resources, service));
		dialog.showDialog();
	}

	public ConflictedResourceEditorDialog(ConflictedResourceSet resources, ResourceConsistencyService service,
			ApplicationContext applicationContext, Window parent) {

		super(applicationContext.getApplicationFIBLibraryService().retrieveFIBComponent(RESOURCE_CONSISTENCY_EDITOR_FIB, true),
				new ConflictedResourceEditor(resources, service), parent, true, FlexoLocalization.getMainLocalizer());
		getData().setOwner(this);
		setTitle("Resource Consistency Editor");
	}

}
