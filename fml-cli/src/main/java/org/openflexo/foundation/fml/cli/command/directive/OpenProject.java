/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.cli.command.directive;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.cli.AbstractCommandInterpreter;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.parser.node.AOpenDirective;
import org.openflexo.foundation.project.FlexoProjectResourceFactory;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;

/**
 * Represents open project directive in FML command-line interpreter
 * 
 * Usage: open <path> where <path> represents a .prj
 * 
 * @author sylvain
 * 
 */
@DirectiveDeclaration(
		keyword = "open",
		usage = "open <project.prj>",
		description = "Open project denoted by supplied path",
		syntax = "open <path>")
public class OpenProject extends Directive {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OpenProject.class.getPackage().getName());

	private String projectPath;
	private File projectDirectory;

	public OpenProject(AOpenDirective node, AbstractCommandInterpreter commandInterpreter) {
		super(node, commandInterpreter);
		projectPath = retrievePath(node.getPath());
	}

	public File getProjectDirectory() {
		if (projectDirectory == null) {
			projectDirectory = new File(getCommandInterpreter().getWorkingDirectory(), projectPath);
			try {
				projectDirectory = new File(projectDirectory.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return projectDirectory;
	}

	@Override
	public boolean isValid() {
		return getProjectDirectory() != null && getProjectDirectory().isDirectory() && getProjectDirectory().exists()
				&& getProjectDirectory().getName().endsWith(FlexoProjectResourceFactory.PROJECT_SUFFIX);
	}

	@Override
	public String invalidCommandReason() {
		if (getProjectDirectory() == null) {
			return "No project specified";
		}
		else if (!getProjectDirectory().exists()) {
			return "Cannot find project: " + getProjectDirectory().getName();
		}
		else if (!getProjectDirectory().getName().endsWith(FlexoProjectResourceFactory.PROJECT_SUFFIX)) {
			return getProjectDirectory().getName() + " does not seems to be project: should end with "
					+ FlexoProjectResourceFactory.PROJECT_SUFFIX;
		}
		else if (!getProjectDirectory().isDirectory()) {
			return getProjectDirectory().getName() + " does not seems to be project: not a directory";
		}
		return null;
	}

	@Override
	public void execute() {

		if (isValid()) {

			for (FlexoResourceCenter<?> rc : getCommandInterpreter().getServiceManager().getResourceCenterService().getResourceCenters()) {
				if (rc instanceof FlexoProject && ((FlexoProject<?>) rc).getProjectDirectory().equals(getProjectDirectory())) {
					getOutStream().println("This project is already opened");
					return;
				}
			}

			try {
				getOutStream().println("Open project " + getProjectDirectory());
				FlexoEditor editor = getCommandInterpreter().getServiceManager().getProjectLoaderService()
						.loadProject(getProjectDirectory());
				FlexoProject<?> project = editor.getProject();
				getCommandInterpreter().setWorkingDirectory(getProjectDirectory());
				getOutStream().println("Project " + project.getName() + " successfully opened.");
			} catch (ProjectInitializerException e) {
				getErrStream().println("Project initializing exception: " + e.getMessage());
				e.printStackTrace();
			} catch (ProjectLoadingCancelledException e) {
			}
		}
	}
}
