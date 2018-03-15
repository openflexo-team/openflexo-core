/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.action;

import java.io.File;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JFileChooser;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionRunnable;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;

/**
 * @author gpolet
 * 
 */
public class ProjectExcelExportInitializer extends ActionInitializer<ProjectExcelExportAction, FlexoProject<?>, FlexoObject> {
	/**
	 * @param actionType
	 * @param controllerActionInitializer
	 */
	public ProjectExcelExportInitializer(ControllerActionInitializer controllerActionInitializer) {
		super(ProjectExcelExportAction.actionType, controllerActionInitializer);
	}

	/**
	 * Overrides getDefaultInitializer
	 * 
	 * @see org.openflexo.view.controller.ActionInitializer#getDefaultInitializer()
	 */
	@Override
	protected FlexoActionRunnable<ProjectExcelExportAction, FlexoProject<?>, FlexoObject> getDefaultInitializer() {
		return (e, action) -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setSelectedFile(new File(System.getProperty("user.home"), action.getFocusedObject().getProjectName() + ".csv"));
			int ret = chooser.showSaveDialog(FlexoFrame.getActiveFrame());
			if (ret == JFileChooser.APPROVE_OPTION) {
				// action.getFocusedObject().getStatistics().refresh();
				// String s = action.getFocusedObject().getStatistics().excel();
				// TODO !
				String s = null;
				File out = chooser.getSelectedFile();
				if (!out.getName().endsWith(".csv")) {
					out = new File(out.getAbsolutePath() + ".csv");
				}
				boolean doIt = false;
				if (out.exists()) {
					if (FlexoController.confirm(action.getLocales().localizedForKey("the_file") + " " + out.getName() + " "
							+ action.getLocales().localizedForKey("already_exists") + "\n"
							+ action.getLocales().localizedForKey("do_you_want_to_replace_it?"))) {
						doIt = true; // the file exists but the user has confirmed the replacement
					}
				}
				else {
					doIt = true; // the file does not exist
				}
				if (doIt) {
					try {
						FileUtils.saveToFile(out, s);
						return true;
					} catch (IOException ex) {
						ex.printStackTrace();
						FlexoController.showError(action.getLocales().localizedForKey("export_failed"));
						return false;
					}
				}
			}
			return false;
		};
	}

	@Override
	protected Icon getEnabledIcon(FlexoActionFactory<ProjectExcelExportAction, FlexoProject<?>, FlexoObject> actionType) {
		return IconLibrary.BIG_EXCEL_ICON;
	}

}
