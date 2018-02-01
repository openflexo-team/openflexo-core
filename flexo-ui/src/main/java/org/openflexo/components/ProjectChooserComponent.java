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

package org.openflexo.components;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JFileChooser;

import org.openflexo.ApplicationContext;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.swing.FlexoFileChooser;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.utils.FlexoFileChooserUtils;

/**
 * Abstract component allowing to choose a {@link FlexoProject}.<br>
 * 
 * We use here the {@link FlexoFileChooser} component which abstract both JFileChooser or FileDialog depending on desired implementation
 * 
 * 
 * @author sguerin
 */
public abstract class ProjectChooserComponent {

	@SuppressWarnings("unused")
	private static final Logger logger = FlexoLogger.getLogger(ProjectChooserComponent.class.getPackage().getName());

	private final FlexoFileChooser fileChooser;
	private final ApplicationContext applicationContext;
	private String approveButtonText;

	public ProjectChooserComponent(Window owner, ApplicationContext applicationContext) {
		super();

		this.applicationContext = applicationContext;
		fileChooser = new FlexoFileChooser(owner);
		fileChooser.setCurrentDirectory(applicationContext.getAdvancedPrefs().getLastVisitedDirectory());
		fileChooser.setDialogTitle(ToolBox.isMacOS() ? getLocales().localizedForKey("select_a_prj_file")
				: getLocales().localizedForKey("select_a_prj_directory"));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setFileFilter(FlexoFileChooserUtils.PROJECT_FILE_FILTER);
		fileChooser.setFileView(FlexoFileChooserUtils.PROJECT_FILE_VIEW);

	}

	public static LocalizedDelegate getLocales(FlexoServiceManager serviceManager) {
		if (serviceManager != null) {
			return serviceManager.getLocalizationService().getFlexoLocalizer();
		}
		return FlexoLocalization.getMainLocalizer();
	}

	public LocalizedDelegate getLocales() {
		return getLocales(applicationContext);
	}

	protected void setApproveButtonText(String text) {
		approveButtonText = text;
		fileChooser.setApproveButtonText(text);
	}

	public Component getComponent() {
		return fileChooser.getComponent();
	}

	public void setTitle(String title) {
		fileChooser.setDialogTitle(title);
	}

	public void setOpenMode() {
		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
	}

	public void setSaveMode() {
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
	}

	public int showOpenDialog() throws HeadlessException {

		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		fileChooser.setDialogTitle(getLocales().localizedForKey("select_openflexo_project"));
		fileChooser.setApproveButtonText(approveButtonText);
		return fileChooser.showOpenDialog();
	}

	public int showSaveDialog() throws HeadlessException {

		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setDialogTitle(getLocales().localizedForKey("set_name_for_new_prj_in_selected_directory"));
		fileChooser.setApproveButtonText(approveButtonText);
		return fileChooser.showSaveDialog();

	}

	public File getSelectedFile() {
		return fileChooser.getSelectedFile();
	}

	public void setSelectedFile(File selectedFile) {
		fileChooser.setSelectedFile(selectedFile);
	}
}
