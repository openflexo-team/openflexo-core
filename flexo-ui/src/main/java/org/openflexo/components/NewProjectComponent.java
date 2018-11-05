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

import java.awt.Frame;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openflexo.ApplicationContext;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;

/**
 * Component allowing to choose a new flexo project
 * 
 * @author sguerin
 */
public class NewProjectComponent extends ProjectChooserComponent {

	private static final Logger logger = Logger.getLogger(OpenProjectComponent.class.getPackage().getName());

	protected NewProjectComponent(Frame owner, ApplicationContext applicationContext) {
		super(owner, applicationContext);
		setApproveButtonText(getLocales().localizedForKey("create"));
	}

	public static File getProjectDirectory(ApplicationContext applicationContext) {
		return getProjectDirectory(FlexoFrame.getActiveFrame(), applicationContext);
	}

	public static File getProjectDirectory(Frame owner, ApplicationContext applicationContext) {
		NewProjectComponent chooser = new NewProjectComponent(owner, applicationContext);
		File newProjectDir = null;
		int returnVal = chooser.showSaveDialog();
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (isValidProjectName(chooser.getSelectedFile().getName())) {
				newProjectDir = chooser.getSelectedFile();
				File newFileDir = chooser.getSelectedFile().getParentFile();
				applicationContext.getAdvancedPrefs().setLastVisitedDirectory(newFileDir);
				if (!newProjectDir.getName().toLowerCase().endsWith(".prj")) {
					newProjectDir = new File(newProjectDir.getAbsolutePath() + ".prj");
				}
				if (newProjectDir.exists()) {
					int option = FlexoController.confirmWithWarningYesNoCancel(
							getLocales(applicationContext).localizedForKey("project_already_exists_do_you_want_to_replace_it"),
							getLocales(applicationContext).localizedForKey("name_conflict"));

					if (option == JOptionPane.YES_OPTION) {
						// We continue with this folder
					}
					else if (option == JOptionPane.NO_OPTION) {
						return getProjectDirectory(owner, applicationContext);
					}
					else { /*Cancel*/
						newProjectDir = null;
					}
				}
			}
			else {
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Invalid project name. The following characters are not allowed: "
							+ FileUtils.BAD_CHARACTERS_FOR_FILE_NAME_REG_EXP);
				}
				FlexoController.notify(getLocales(applicationContext).localizedForKey("project_name_cannot_contain_\\___&_#_{_}_[_]_%_~"));
			}
		}
		else {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("No project specified !");
			}
			return null;
		}
		return newProjectDir;
	}

	/**
	 * @param absolutePath
	 * @return
	 */
	public static boolean isValidProjectName(String fileName) {
		String trimmed = fileName != null ? fileName.trim() : "";
		boolean notEmpty = trimmed.length() > 0;
		if (!notEmpty) {
			return false;
		}
		boolean containsInvalidChar = FileUtils.BAD_CHARACTERS_FOR_FILE_NAME_PATTERN.matcher(fileName).find();
		if (containsInvalidChar) {
			return false;
		}
		boolean isTooSmall = trimmed.length() < 3;
		if (isTooSmall) {
			return false;
		}
		return !Character.isDigit(trimmed.charAt(0));
	}

	public static void main(String[] args) {
		String s = "cou\\cou";
		if (isValidProjectName(s)) {
			System.err.println("Error for " + s);
		}
		s = "cou|couc";
		if (isValidProjectName(s)) {
			System.err.println("Error for " + s);
		}
		s = "cou/couc";
		if (isValidProjectName(s)) {
			System.err.println("Error for " + s);
		}
		s = "cou?couc";
		if (isValidProjectName(s)) {
			System.err.println("Error for " + s);
		}
		s = "cou:couc";
		if (isValidProjectName(s)) {
			System.err.println("Error for " + s);
		}
		s = "cou\"couc";
		if (isValidProjectName(s)) {
			System.err.println("Error for " + s);
		}
		s = "cou*couc";
		if (isValidProjectName(s)) {
			System.err.println("Error for " + s);
		}
		s = "cou?couc";
		if (isValidProjectName(s)) {
			System.err.println("Error for " + s);
		}
		s = "cou<couc";
		if (isValidProjectName(s)) {
			System.err.println("Error for " + s);
		}
		s = "cou>couc";
		if (isValidProjectName(s)) {
			System.err.println("Error for " + s);
		}
	}

}
