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
import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.parser.node.AOpenDirective;
import org.openflexo.foundation.fml.parser.node.APathOpenDirective;
import org.openflexo.foundation.fml.parser.node.AResourceOpenDirective;
import org.openflexo.foundation.fml.parser.node.POpenDirective;
import org.openflexo.foundation.project.FlexoProjectResource;
import org.openflexo.foundation.project.FlexoProjectResourceFactory;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents open project directive in FML command-line interpreter
 * 
 * Usage: open <path> where <path> represents a .prj
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(OpenProject.OpenProjectImpl.class)
@DirectiveDeclaration(
		keyword = "open",
		usage = "open <project.prj> | -r <resource>",
		description = "Open project denoted by supplied path",
		syntax = "open <path> | -r <resource>")
public interface OpenProject extends Directive<AOpenDirective> {

	public File getProjectDirectory();

	public static abstract class OpenProjectImpl extends DirectiveImpl<AOpenDirective> implements OpenProject {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(OpenProject.class.getPackage().getName());

		private FlexoProjectResource<?> projectResource;
		private String projectPath;
		private File projectDirectory;

		@Override
		public void create(AOpenDirective node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
			performSuperInitializer(node, commandSemanticsAnalyzer);

			POpenDirective openDirective = node.getOpenDirective();

			if (openDirective instanceof AResourceOpenDirective) {
				projectResource = (FlexoProjectResource<?>) retrieveResource(((AResourceOpenDirective) openDirective).getReferenceByUri());
			}
			else if (openDirective instanceof APathOpenDirective) {
				projectPath = retrievePath(((APathOpenDirective) openDirective).getPath());
			}
		}

		@Override
		public String toString() {
			if (StringUtils.isNotEmpty(projectPath)) {
				return "open " + projectPath;
			}
			else if (projectResource != null) {
				return "open -r [\"" + projectResource.getURI() + "\"]";
			}
			return "open";
		}

		@Override
		public File getProjectDirectory() {
			if (projectDirectory == null) {
				if (StringUtils.isNotEmpty(projectPath)) {
					projectDirectory = new File(getCommandInterpreter().getWorkingDirectory(), projectPath);
				}
				else if (projectResource != null) {
					System.out.println("projectResource=" + projectResource);
					projectDirectory = (File) projectResource.getDelegateResourceCenter().getBaseArtefact();
					System.out.println("projectDirectory=" + projectDirectory);
				}
				try {
					projectDirectory = new File(projectDirectory.getCanonicalPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return projectDirectory;
		}

		@Override
		public boolean isSyntaxicallyValid() {
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
		public FlexoProject<?> execute() throws FMLCommandExecutionException {

			super.execute();
			output.clear();
			String cmdOutput;

			if (isSyntaxicallyValid()) {

				for (FlexoResourceCenter<?> rc : getCommandInterpreter().getServiceManager().getResourceCenterService()
						.getResourceCenters()) {
					if (rc instanceof FlexoProject && ((FlexoProject<?>) rc).getProjectDirectory().equals(getProjectDirectory())) {
						cmdOutput = "This project is already opened";

						output.add(cmdOutput);
						getOutStream().println(cmdOutput);
						return (FlexoProject<?>) rc;
					}
				}

				try {
					logger.info(
							"Open project " + getProjectDirectory() + " from currentPath=" + getCommandInterpreter().getWorkingDirectory());
					getOutStream().println("Open project " + getProjectDirectory());
					FlexoEditor editor = getCommandInterpreter().getServiceManager().getProjectLoaderService()
							.loadProject(getProjectDirectory());
					FlexoProject<?> project = editor.getProject();
					getCommandInterpreter().setWorkingDirectory(getProjectDirectory());
					getCommandInterpreter().enterProject(project, editor);

					cmdOutput = "Project " + project.getName() + " successfully opened.";

					output.add(cmdOutput);
					getOutStream().println(cmdOutput);
					return project;
				} catch (ProjectInitializerException e) {
					cmdOutput = "Project initializing exception";

					output.add(cmdOutput);
					throw new FMLCommandExecutionException(cmdOutput, e);
				} catch (ProjectLoadingCancelledException e) {
					output.add("Operation cancelled");
					throw new FMLCommandExecutionException(e);
				}
			}

			return null;
		}
	}
}
