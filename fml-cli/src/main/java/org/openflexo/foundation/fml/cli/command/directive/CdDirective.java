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

import org.openflexo.foundation.fml.cli.AbstractCommandSemanticsAnalyzer;
import org.openflexo.foundation.fml.cli.command.Directive;
import org.openflexo.foundation.fml.cli.command.DirectiveDeclaration;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.parser.node.ACdDirective;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents #cd directive in FML command-line interpreter
 * 
 * Usage: cd <directory>
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(CdDirective.CdDirectiveImpl.class)
@DirectiveDeclaration(keyword = "cd", usage = "cd <directory>", description = "Change working directory", syntax = "cd <path>")
public interface CdDirective extends Directive<ACdDirective> {

	public static abstract class CdDirectiveImpl extends DirectiveImpl<ACdDirective> implements CdDirective {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(CdDirective.class.getPackage().getName());

		private String path;

		@Override
		public void create(ACdDirective node, AbstractCommandSemanticsAnalyzer commandSemanticsAnalyzer) {
			performSuperInitializer(node, commandSemanticsAnalyzer);
			path = retrievePath(node.getPath());
		}

		@Override
		public String toString() {
			return "cd " + path;
		}

		public File getNewDirectory() {
			File newDirectory;
			if (path.startsWith("/")) {
				newDirectory = new File(path);
			}
			else {
				newDirectory = new File(getCommandInterpreter().getWorkingDirectory(), path);
			}
			try {
				newDirectory = new File(newDirectory.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return newDirectory;
		}

		@Override
		public boolean isSyntaxicallyValid() {
			return StringUtils.isNotEmpty(path);
		}

		@Override
		public boolean isValidInThatContext() {
			return getNewDirectory() != null && getNewDirectory().isDirectory() && getNewDirectory().exists();
		}

		@Override
		public String invalidCommandReason() {
			if (getNewDirectory() == null) {
				return "No directory";
			}
			else if (!getNewDirectory().exists()) {
				return "Cannot find directory: " + getNewDirectory().getName();
			}
			else if (!getNewDirectory().isDirectory()) {
				return getNewDirectory().getName() + " is not a directory";
			}
			return null;
		}

		@Override
		public File execute() throws FMLCommandExecutionException {
			super.execute();
			output.clear();

			if (isValidInThatContext()) {
				File newDirectory = getNewDirectory();
				getCommandInterpreter().setWorkingDirectory(newDirectory);
				return newDirectory;
			}

			String cmdOutput = invalidCommandReason();

			output.add(cmdOutput);
			throw new FMLCommandExecutionException(cmdOutput);
		}
	}
}
